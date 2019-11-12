/*
 * Copyright 2019 HM Revenue & Customs
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
import models.LiabilityTransformer._
import models._
import play.api.Logger
import play.api.libs.json._
import services.TaxRateService

case class ATSParsingException(s: String) extends Exception(s)

case class ATSRawDataTransformer(summaryLiability: TaxSummaryLiability, rawTaxPayerJson: JsValue, UTR: String, taxYear: Int) {

  val calculations = new ATSCalculations(summaryLiability)

  def atsDataDTO: AtsMiddleTierData = {
    try {
      if(calculations.hasLiability) {
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
    }
    catch {
      case ATSParsingException(message) => AtsMiddleTierData.error(taxYear, message)
      case otherError: Throwable =>
        Logger.error("Unexpected error has occurred", otherError)
        AtsMiddleTierData.error(taxYear, "Other Error")
    }
  }

  private def createGovSpendData =
    GovSpendingDataTransformer(calculations.totalTax, taxYear).govSpendReferenceDTO

  private def createSummaryData = DataHolder.make(createSummaryPageBreakdown, createSummaryPageRates)
  private def createIncomeData = DataHolder.make(createYourIncomeBeforeTaxBreakdown)
  private def createIncomeTaxData = DataHolder.make(createTotalIncomeTaxPageBreakdown, createTotalIncomeTaxPageRates, summaryLiability.incomeTaxStatus)
  private def createAllowanceData = DataHolder.make(createYourTaxFreeAmountBreakdown)
  private def createCapitalGainsData = DataHolder.make(createCapitalGainsTaxBreakdown, createCapitalGainsTaxRates)

  private def createTaxPayerData = ATSTaxpayerDataTransformer(rawTaxPayerJson).atsTaxpayerDataDTO

  private def createCapitalGainsTaxBreakdown: Map[LiabilityTransformer, Amount] =
    Map(
      TaxableGains -> calculations.taxableGains,
      LessTaxFreeAmount -> calculations.get(CgAnnualExempt),
      PayCgTaxOn -> calculations.payCapitalGainsTaxOn,
      AmountAtEntrepreneursRate -> calculations.get(CgAtEntrepreneursRate),
      AmountDueAtEntrepreneursRate -> calculations.get(CgDueEntrepreneursRate),
      AmountAtOrdinaryRate -> calculations.get(CgAtLowerRate),
      AmountDueAtOrdinaryRate -> calculations.get(CgDueLowerRate),
      AmountAtHigherRate -> calculations.get(CgAtHigherRate),
      AmountDueAtHigherRate -> calculations.get(CgDueHigherRate),
      Adjustments -> calculations.get(CapAdjustment),
      TotalCgTax -> calculations.totalCapitalGainsTax,
      CgTaxPerCurrencyUnit -> calculations.capitalGainsTaxPerCurrency,
      AmountAtRPCILowerRate -> calculations.get(CGAtLowerRateRPCI),
      AmountDueRPCILowerRate -> calculations.get(LowerRateCgtRPCI),
      AmountAtRPCIHigheRate -> calculations.get(CGAtHigherRateRPCI),
      AmountDueRPCIHigherRate -> calculations.get(HigherRateCgtRPCI)
    )

  private def createYourIncomeBeforeTaxBreakdown: Map[LiabilityTransformer, Amount] =
    Map(
      SelfEmploymentIncome -> calculations.selfEmployment,
      IncomeFromEmployment -> calculations.get(SummaryTotalEmployment),
      LiabilityTransformer.StatePension -> calculations.get(StatePension),
      OtherPensionIncome -> calculations.otherPension,
      TaxableStateBenefits -> calculations.taxableStateBenefits,
      OtherIncome -> calculations.otherIncome,
      BenefitsFromEmployment -> calculations.get(EmploymentBenefits),
      TotalIncomeBeforeTax -> calculations.totalIncomeBeforeTax
    )

  private def createYourTaxFreeAmountBreakdown: Map[LiabilityTransformer, Amount] =
    Map(
      PersonalTaxFreeAmount -> calculations.get(PersonalAllowance),
      MarriageAllowanceTransferredAmount -> calculations.get(MarriageAllceOut),
      OtherAllowancesAmount -> calculations.otherAllowances,
      TotalTaxFreeAmount -> calculations.totalTaxFreeAmount
    )

  private def createSummaryPageBreakdown: Map[LiabilityTransformer, Amount] =
    Map(
      EmployeeNicAmount -> calculations.totalAmountEmployeeNic,
      TotalIncomeTaxAndNics -> calculations.totalAmountTaxAndNics,
      YourTotalTax -> calculations.totalTax,
      PersonalTaxFreeAmount -> calculations.get(PersonalAllowance),
      TotalTaxFreeAmount -> calculations.totalTaxFreeAmount,
      TotalIncomeBeforeTax -> calculations.totalIncomeBeforeTax,
      TotalIncomeTax -> calculations.totalIncomeTaxAmount,
      TotalCgTax -> calculations.totalCapitalGainsTax,
      TaxableGains -> calculations.taxableGains,
      CgTaxPerCurrencyUnit -> calculations.capitalGainsTaxPerCurrency,
      NicsAndTaxPerCurrencyUnit -> calculations.nicsAndTaxPerCurrency
    ) //TODO Percentage done RATES

  private def createTotalIncomeTaxPageBreakdown: Map[LiabilityTransformer, Amount] =
    Map(
      StartingRateForSavings -> calculations.get(SavingsChargeableStartRate),
      StartingRateForSavingsAmount -> calculations.get(SavingsTaxStartingRate),
      BasicRateIncomeTax -> calculations.basicIncomeRateIncomeTax,
      BasicRateIncomeTaxAmount -> calculations. basicRateIncomeTaxAmount,
      HigherRateIncomeTax -> calculations.higherRateIncomeTax,
      HigherRateIncomeTaxAmount -> calculations. higherRateIncomeTaxAmount,
      AdditionalRateIncomeTax -> calculations.additionalRateIncomeTax,
      AdditionalRateIncomeTaxAmount -> calculations.additionalRateIncomeTaxAmount,
      OrdinaryRate -> calculations.get(DividendChargeableLowRate),
      OrdinaryRateAmount -> calculations.get(DividendTaxLowRate),
      UpperRate -> calculations.get(DividendChargeableHighRate),
      UpperRateAmount -> calculations.get(DividendTaxHighRate),
      AdditionalRate -> calculations.get(DividendChargeableAddHRate),
      AdditionalRateAmount -> calculations.get(DividendTaxAddHighRate),
      OtherAdjustmentsIncreasing -> calculations.otherAdjustmentsIncreasing,
      MarriageAllowanceReceivedAmount -> calculations.get(MarriageAllceIn),
      OtherAdjustmentsReducing -> calculations.otherAdjustmentsReducing,
      TotalIncomeTax -> calculations.totalIncomeTaxAmount,
      ScottishIncomeTax -> calculations.scottishIncomeTax
    )

  //TODO RATES
  private def createCapitalGainsTaxRates: Map[String, ApiRate] =
    Map(
      "cg_entrepreneurs_rate" -> TaxRateService.cgEntrepreneursRate(taxYear),
      "cg_ordinary_rate" -> TaxRateService.cgOrdinaryRate(taxYear),
      "cg_upper_rate" -> TaxRateService.cgUpperRate(taxYear),
      "total_cg_tax_rate" -> calculations.totalCgTaxLiabilityAsPercentage,
      "prop_interest_rate_lower_rate" -> TaxRateService.individualsForResidentialPropertyAndCarriedInterestLowerRate(taxYear),
      "prop_interest_rate_higher_rate" -> TaxRateService.individualsForResidentialPropertyAndCarriedInterestHigherRate(taxYear)
    ).collect {
      case (k, v) => (k, v.apiValue)
    }

  //TODO RATES
  private def createSummaryPageRates: Map[String, ApiRate] =
    Map(
      "total_cg_tax_rate" -> calculations.totalCgTaxLiabilityAsPercentage.apiValue, //TODO RATES
      "nics_and_tax_rate" -> calculations.totalNicsAndTaxLiabilityAsPercentage.apiValue
    )

  //rates TODO
  private def createTotalIncomeTaxPageRates: Map[String, ApiRate] =
    Map(
      "starting_rate_for_savings_rate" -> TaxRateService.startingRateForSavingsRate(taxYear),
      "basic_rate_income_tax_rate" -> TaxRateService.basicRateIncomeTaxRate(taxYear),
      "higher_rate_income_tax_rate" -> TaxRateService.higherRateIncomeTaxRate(taxYear),
      "additional_rate_income_tax_rate" -> TaxRateService.additionalRateIncomeTaxRate(taxYear),
      "ordinary_rate_tax_rate" -> TaxRateService.dividendsOrdinaryRate(taxYear),
      "upper_rate_rate" -> TaxRateService.dividendUpperRateRate(taxYear),
      "additional_rate_rate" -> TaxRateService.dividendAdditionalRate(taxYear)
    ).collect {
      case (k, v) => (k, v.apiValue)
    }
}


