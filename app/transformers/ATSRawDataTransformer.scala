/*
 * Copyright 2020 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package transformers

import models.Liability.{StatePension, _}
import models.LiabilityKey._
import models.RateKey._
import models._
import play.api.Logger
import play.api.libs.json._
import services.DefaultTaxRateService

case class ATSParsingException(s: String) extends Exception(s)

case class ATSRawDataTransformer(
  summaryLiability: TaxSummaryLiability,
  rawTaxPayerJson: JsValue,
  UTR: String,
  taxYear: Int) {

  val taxRate = new DefaultTaxRateService(taxYear)
  val calculations = ATSCalculations.make(summaryLiability, taxRate)

  def atsDataDTO: AtsMiddleTierData =
    try {
      if (calculations.hasLiability) {

        AtsMiddleTierData.make(
          taxYear,
          UTR,
          createIncomeTaxData,
          createSummaryData,
          createIncomeData,
          createAllowanceData,
          createCapitalGainsData,
          createGovSpendData,
          createTaxPayerData
        )
      } else {
        AtsMiddleTierData.noAtsResult(taxYear)
      }
    } catch {
      case ATSParsingException(message) => AtsMiddleTierData.error(taxYear, message)
      case otherError: Throwable =>
        Logger.error("Unexpected error has occurred", otherError)
        AtsMiddleTierData.error(taxYear, "Other Error")
    }

  private def createGovSpendData =
    GovSpendingDataTransformer(calculations.totalTax, taxYear).govSpendReferenceDTO

  private def createSummaryData =
    DataHolder.make(createSummaryPageBreakdown, createSummaryPageRates)

  private def createIncomeData =
    DataHolder.make(createYourIncomeBeforeTaxBreakdown)

  private def createIncomeTaxData =
    DataHolder.make(createTotalIncomeTaxPageBreakdown, createTotalIncomeTaxPageRates, summaryLiability.incomeTaxStatus)

  private def createAllowanceData =
    DataHolder.make(createYourTaxFreeAmountBreakdown)

  private def createCapitalGainsData =
    DataHolder.make(createCapitalGainsTaxBreakdown, createCapitalGainsTaxRates)

  private def createTaxPayerData = ATSTaxpayerDataTransformer(rawTaxPayerJson).atsTaxpayerDataDTO

  private def createCapitalGainsTaxBreakdown: Map[LiabilityKey, Amount] =
    Map(
      TaxableGains                 -> calculations.taxableGains,
      LessTaxFreeAmount            -> calculations.get(CgAnnualExempt),
      PayCgTaxOn                   -> calculations.payCapitalGainsTaxOn,
      AmountAtEntrepreneursRate    -> calculations.get(CgAtEntrepreneursRate),
      AmountDueAtEntrepreneursRate -> calculations.get(CgDueEntrepreneursRate),
      AmountAtOrdinaryRate         -> calculations.get(CgAtLowerRate),
      AmountDueAtOrdinaryRate      -> calculations.get(CgDueLowerRate),
      AmountAtHigherRate           -> calculations.get(CgAtHigherRate),
      AmountDueAtHigherRate        -> calculations.get(CgDueHigherRate),
      Adjustments                  -> calculations.get(CapAdjustment),
      TotalCgTax                   -> calculations.totalCapitalGainsTax,
      CgTaxPerCurrencyUnit         -> calculations.capitalGainsTaxPerCurrency,
      AmountAtRPCILowerRate        -> calculations.getWithDefaultAmount(CGAtLowerRateRPCI),
      AmountDueRPCILowerRate       -> calculations.getWithDefaultAmount(LowerRateCgtRPCI),
      AmountAtRPCIHigheRate        -> calculations.getWithDefaultAmount(CGAtHigherRateRPCI),
      AmountDueRPCIHigherRate      -> calculations.getWithDefaultAmount(HigherRateCgtRPCI)
    )

  private def createYourIncomeBeforeTaxBreakdown: Map[LiabilityKey, Amount] =
    Map(
      SelfEmploymentIncome      -> calculations.selfEmployment,
      IncomeFromEmployment      -> calculations.get(SummaryTotalEmployment),
      LiabilityKey.StatePension -> calculations.get(StatePension),
      OtherPensionIncome        -> calculations.otherPension,
      TaxableStateBenefits      -> calculations.taxableStateBenefits,
      OtherIncome               -> calculations.otherIncome,
      BenefitsFromEmployment    -> calculations.get(EmploymentBenefits),
      TotalIncomeBeforeTax      -> calculations.totalIncomeBeforeTax
    )

  private def createYourTaxFreeAmountBreakdown: Map[LiabilityKey, Amount] =
    Map(
      PersonalTaxFreeAmount              -> calculations.get(PersonalAllowance),
      MarriageAllowanceTransferredAmount -> calculations.getWithDefaultAmount(MarriageAllceOut),
      OtherAllowancesAmount              -> calculations.otherAllowances,
      TotalTaxFreeAmount                 -> calculations.totalTaxFreeAmount
    )

  private def createSummaryPageBreakdown: Map[LiabilityKey, Amount] =
    Map(
      EmployeeNicAmount         -> calculations.totalAmountEmployeeNic,
      TotalIncomeTaxAndNics     -> calculations.totalAmountTaxAndNics,
      YourTotalTax              -> calculations.totalTax,
      PersonalTaxFreeAmount     -> calculations.get(PersonalAllowance),
      TotalTaxFreeAmount        -> calculations.totalTaxFreeAmount,
      TotalIncomeBeforeTax      -> calculations.totalIncomeBeforeTax,
      TotalIncomeTax            -> calculations.totalIncomeTaxAmount,
      TotalCgTax                -> calculations.totalCapitalGainsTax,
      TaxableGains              -> calculations.taxableGains,
      CgTaxPerCurrencyUnit      -> calculations.capitalGainsTaxPerCurrency,
      NicsAndTaxPerCurrencyUnit -> calculations.nicsAndTaxPerCurrency
    )

  private def createTotalIncomeTaxPageBreakdown: Map[LiabilityKey, Amount] =
    Map(
      StartingRateForSavings          -> calculations.savingsRate,
      StartingRateForSavingsAmount    -> calculations.savingsRateAmount,
      BasicRateIncomeTax              -> calculations.basicRateIncomeTax,
      BasicRateIncomeTaxAmount        -> calculations.basicRateIncomeTaxAmount,
      HigherRateIncomeTax             -> calculations.higherRateIncomeTax,
      HigherRateIncomeTaxAmount       -> calculations.higherRateIncomeTaxAmount,
      AdditionalRateIncomeTax         -> calculations.additionalRateIncomeTax,
      AdditionalRateIncomeTaxAmount   -> calculations.additionalRateIncomeTaxAmount,
      OrdinaryRate                    -> calculations.get(DividendChargeableLowRate),
      OrdinaryRateAmount              -> calculations.get(DividendTaxLowRate),
      UpperRate                       -> calculations.get(DividendChargeableHighRate),
      UpperRateAmount                 -> calculations.get(DividendTaxHighRate),
      AdditionalRate                  -> calculations.get(DividendChargeableAddHRate),
      AdditionalRateAmount            -> calculations.get(DividendTaxAddHighRate),
      OtherAdjustmentsIncreasing      -> calculations.otherAdjustmentsIncreasing,
      MarriageAllowanceReceivedAmount -> calculations.getWithDefaultAmount(MarriageAllceIn),
      OtherAdjustmentsReducing        -> calculations.otherAdjustmentsReducing,
      TotalIncomeTax                  -> calculations.totalIncomeTaxAmount,
      ScottishIncomeTax               -> calculations.scottishIncomeTax,
      ScottishStarterRateTax          -> calculations.scottishStarterRateTax,
      ScottishBasicRateTax            -> calculations.scottishBasicRateTax,
      ScottishIntermediateRateTax     -> calculations.scottishIntermediateRateTax,
      ScottishHigherRateTax           -> calculations.scottishHigherRateTax,
      ScottishAdditionalRateTax       -> calculations.scottishAdditionalRateTax,
      ScottishTotalTax                -> calculations.scottishTotalTax,
      ScottishStarterIncome           -> calculations.scottishStarterRateIncome,
      ScottishBasicIncome             -> calculations.scottishBasicRateIncome,
      ScottishIntermediateIncome      -> calculations.scottishIntermediateRateIncome,
      ScottishHigherIncome            -> calculations.scottishHigherRateIncome,
      ScottishAdditionalIncome        -> calculations.scottishAdditionalRateIncome,
      SavingsLowerRateTax             -> calculations.savingsBasicRateTax,
      SavingsHigherRateTax            -> calculations.savingsHigherRateTax,
      SavingsAdditionalRateTax        -> calculations.savingsAdditionalRateTax,
      SavingsLowerIncome              -> calculations.savingsBasicRateIncome,
      SavingsHigherIncome             -> calculations.savingsHigherRateIncome,
      SavingsAdditionalIncome         -> calculations.savingsAdditionalRateIncome
    )

  private def createCapitalGainsTaxRates: Map[RateKey, ApiRate] =
    Map[RateKey, Rate](
      CapitalGainsEntrepreneur -> taxRate.cgEntrepreneursRate,
      CapitalGainsOrdinary     -> taxRate.cgOrdinaryRate,
      CapitalGainsUpper        -> taxRate.cgUpperRate,
      TotalCapitalGains        -> calculations.totalCgTaxLiabilityAsPercentage,
      InterestLower            -> taxRate.individualsForResidentialPropertyAndCarriedInterestLowerRate,
      InterestHigher           -> taxRate.individualsForResidentialPropertyAndCarriedInterestHigherRate
    ).mapValues(_.apiValue)

  private def createSummaryPageRates: Map[RateKey, ApiRate] =
    Map(
      TotalCapitalGains -> calculations.totalCgTaxLiabilityAsPercentage.apiValue,
      NICS              -> calculations.totalNicsAndTaxLiabilityAsPercentage.apiValue
    )

  private def createTotalIncomeTaxPageRates: Map[RateKey, ApiRate] =
    Map[RateKey, Rate](
      Savings                  -> taxRate.startingRateForSavingsRate,
      IncomeBasic              -> taxRate.basicRateIncomeTaxRate,
      IncomeHigher             -> taxRate.higherRateIncomeTaxRate,
      IncomeAdditional         -> taxRate.additionalRateIncomeTaxRate,
      Ordinary                 -> taxRate.dividendsOrdinaryRate,
      Upper                    -> taxRate.dividendUpperRateRate,
      Additional               -> taxRate.dividendAdditionalRate,
      ScottishStarterRate      -> taxRate.scottishStarterRate,
      ScottishBasicRate        -> taxRate.scottishBasicRate,
      ScottishIntermediateRate -> taxRate.scottishIntermediateRate,
      ScottishHigherRate       -> taxRate.scottishHigherRate,
      ScottishAdditionalRate   -> taxRate.scottishAdditionalRate,
      SavingsLowerRate         -> taxRate.basicRateIncomeTaxRate,
      SavingsHigherRate        -> taxRate.higherRateIncomeTaxRate,
      SavingsAdditionalRate    -> taxRate.additionalRateIncomeTaxRate
    ).mapValues(_.apiValue)
}
