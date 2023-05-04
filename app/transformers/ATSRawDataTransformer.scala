/*
 * Copyright 2023 HM Revenue & Customs
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

import com.google.inject.{Inject, Singleton}
import config.ApplicationConfig
import models.LiabilityKey.{ScottishIncomeTax, _}
import models.ODSLiabilities.ODSLiabilities.{StatePension, _}
import models.RateKey._
import models._
import play.api.libs.json._
import play.api.{Logger, Logging}
import services.TaxRateService
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.model.DataEvent

import scala.concurrent.ExecutionContext

case class ATSParsingException(s: String) extends Exception(s)

@Singleton
class ATSRawDataTransformer @Inject() (applicationConfig: ApplicationConfig, auditConnector: AuditConnector)
    extends Logging {

  def atsDataDTO(
    taxRate: TaxRateService,
    calculations: ATSCalculations,
    rawTaxPayerJson: JsValue,
    UTR: String,
    taxYear: Int
  )(implicit ec: ExecutionContext): AtsMiddleTierData = {
    val logger = Logger(getClass.getName)
    logger.debug(s"Liability for utr $UTR for tax year $taxYear is ${calculations.taxLiability.calculus.getOrElse("")}")
    try if (calculations.hasLiability) { // Careful hasLiability is overridden depending on Nationality and tax year
      auditConnector.sendEvent(
        DataEvent(
          auditSource = applicationConfig.appName,
          auditType = "taxLiability",
          detail = Map(
            "utr"              -> UTR,
            "taxYear"          -> taxYear.toString,
            "liabilityAmount"  -> calculations.taxLiability.amount.toString,
            "LiabilityDetails" -> calculations.taxLiability.calculus.getOrElse("No calculation details present")
          )
        )
      )

      AtsMiddleTierData.make(
        taxYear,
        UTR,
        createIncomeTaxData(calculations, taxRate, calculations.incomeTaxStatus),
        createSummaryData(calculations: ATSCalculations),
        createIncomeData(calculations: ATSCalculations),
        createAllowanceData(calculations: ATSCalculations),
        createCapitalGainsData(calculations, taxRate),
        createGovSpendData(calculations.totalTax, taxYear),
        createTaxPayerData(rawTaxPayerJson)
      )
    } else {

      logger.warn(s"There is no liability for the year $taxYear")
      AtsMiddleTierData.noAtsResult(taxYear)
    } catch {
      case ATSParsingException(message) =>
        AtsMiddleTierData.error(taxYear, message)
      case otherError: Throwable =>
        logger.error("Unexpected error has occurred", otherError)
        AtsMiddleTierData.error(taxYear, "Other Error")
    }
  }

  private def createGovSpendData(totalTax: Amount, taxYear: Int) =
    GovernmentSpendingOutputWrapper(applicationConfig, totalTax, taxYear)

  private def createSummaryData(calculations: ATSCalculations) =
    DataHolder.make(createSummaryPageBreakdown(calculations), createSummaryPageRates(calculations))

  private def createIncomeData(calculations: ATSCalculations) =
    DataHolder.make(createYourIncomeBeforeTaxBreakdown(calculations))

  private def createIncomeTaxData(
    calculations: ATSCalculations,
    taxRate: TaxRateService,
    incomeTaxStatus: Option[Nationality]
  ) =
    DataHolder
      .make(createTotalIncomeTaxPageBreakdown(calculations), createTotalIncomeTaxPageRates(taxRate), incomeTaxStatus)

  private def createAllowanceData(calculations: ATSCalculations) =
    DataHolder.make(createYourTaxFreeAmountBreakdown(calculations))

  private def createCapitalGainsData(calculations: ATSCalculations, taxRate: TaxRateService) =
    DataHolder.make(createCapitalGainsTaxBreakdown(calculations), createCapitalGainsTaxRates(calculations, taxRate))

  private def createTaxPayerData(rawTaxPayerJson: JsValue) =
    ATSTaxpayerDataTransformer(rawTaxPayerJson).atsTaxpayerDataDTO

  private def createCapitalGainsTaxBreakdown(calculations: ATSCalculations): Map[LiabilityKey, Amount] =
    Map(
      TaxableGains                 -> calculations.taxableGains(),
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

  private def createYourIncomeBeforeTaxBreakdown(calculations: ATSCalculations): Map[LiabilityKey, Amount] =
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

  private def createYourTaxFreeAmountBreakdown(calculations: ATSCalculations): Map[LiabilityKey, Amount] =
    Map(
      PersonalTaxFreeAmount              -> calculations.get(PersonalAllowance),
      MarriageAllowanceTransferredAmount -> calculations.getWithDefaultAmount(MarriageAllceOut),
      OtherAllowancesAmount              -> calculations.otherAllowances,
      TotalTaxFreeAmount                 -> calculations.totalTaxFreeAmount
    )

  private def createSummaryPageBreakdown(calculations: ATSCalculations): Map[LiabilityKey, Amount] =
    Map(
      EmployeeNicAmount         -> calculations.totalAmountEmployeeNic,
      TotalIncomeTaxAndNics     -> calculations.totalAmountTaxAndNics,
      YourTotalTax              -> calculations.totalTax,
      PersonalTaxFreeAmount     -> calculations.get(PersonalAllowance),
      TotalTaxFreeAmount        -> calculations.totalTaxFreeAmount,
      TotalIncomeBeforeTax      -> calculations.totalIncomeBeforeTax,
      TotalIncomeTax            -> calculations.totalIncomeTaxAmount,
      TotalCgTax                -> calculations.totalCapitalGainsTax,
      TaxableGains              -> calculations.taxableGains(),
      CgTaxPerCurrencyUnit      -> calculations.capitalGainsTaxPerCurrency,
      NicsAndTaxPerCurrencyUnit -> calculations.nicsAndTaxPerCurrency
    )

  private def createTotalIncomeTaxPageBreakdown(calculations: ATSCalculations): Map[LiabilityKey, Amount] =
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
      OtherAdjustmentsReducing        -> calculations.otherAdjustmentsReducing.roundAmountUp(),
      TotalIncomeTax                  -> calculations.totalIncomeTaxAmount,
      ScottishIncomeTax               -> calculations.scottishIncomeTax,
      WelshIncomeTax                  -> calculations.welshIncomeTax,
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

  private def createCapitalGainsTaxRates(
    calculations: ATSCalculations,
    taxRate: TaxRateService
  ): Map[RateKey, ApiRate] =
    Map[RateKey, Rate](
      CapitalGainsEntrepreneur -> taxRate.cgEntrepreneursRate(),
      CapitalGainsOrdinary     -> taxRate.cgOrdinaryRate(),
      CapitalGainsUpper        -> taxRate.cgUpperRate(),
      TotalCapitalGains        -> calculations.totalCgTaxLiabilityAsPercentage,
      InterestLower            -> taxRate.individualsForResidentialPropertyAndCarriedInterestLowerRate(),
      InterestHigher           -> taxRate.individualsForResidentialPropertyAndCarriedInterestHigherRate()
    ).view.mapValues(_.apiValue).toMap

  private def createSummaryPageRates(calculations: ATSCalculations): Map[RateKey, ApiRate] =
    Map(
      TotalCapitalGains -> calculations.totalCgTaxLiabilityAsPercentage.apiValue,
      NICS              -> calculations.totalNicsAndTaxLiabilityAsPercentage.apiValue
    )

  private def createTotalIncomeTaxPageRates(taxRate: TaxRateService): Map[RateKey, ApiRate] =
    Map[RateKey, Rate](
      Savings                  -> taxRate.startingRateForSavingsRate(),
      IncomeBasic              -> taxRate.basicRateIncomeTaxRate(),
      IncomeHigher             -> taxRate.higherRateIncomeTaxRate(),
      IncomeAdditional         -> taxRate.additionalRateIncomeTaxRate(),
      Ordinary                 -> taxRate.dividendsOrdinaryRate(),
      Upper                    -> taxRate.dividendUpperRateRate(),
      Additional               -> taxRate.dividendAdditionalRate(),
      ScottishStarterRate      -> taxRate.scottishStarterRate,
      ScottishBasicRate        -> taxRate.scottishBasicRate,
      ScottishIntermediateRate -> taxRate.scottishIntermediateRate,
      ScottishHigherRate       -> taxRate.scottishHigherRate,
      ScottishAdditionalRate   -> taxRate.scottishAdditionalRate,
      SavingsLowerRate         -> taxRate.basicRateIncomeTaxRate(),
      SavingsHigherRate        -> taxRate.higherRateIncomeTaxRate(),
      SavingsAdditionalRate    -> taxRate.additionalRateIncomeTaxRate()
    ).view.mapValues(_.apiValue).toMap
}
