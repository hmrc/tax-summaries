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

package sa.transformers

import com.google.inject.{Inject, Singleton}
import common.config.ApplicationConfig
import common.models.*
import common.models.LiabilityKey.{AdditionalRate, AdditionalRateAmount, AdditionalRateIncomeTax, AdditionalRateIncomeTaxAmount, Adjustments, AmountAtEntrepreneursRate, AmountAtHigherRate, AmountAtOrdinaryRate, AmountAtRPCIHigheRate, AmountAtRPCILowerRate, AmountDueAtEntrepreneursRate, AmountDueAtHigherRate, AmountDueAtOrdinaryRate, AmountDueRPCIHigherRate, AmountDueRPCILowerRate, BasicRateIncomeTax, BasicRateIncomeTaxAmount, BenefitsFromEmployment, CgTaxPerCurrencyUnit, DividendOrdinaryRate, DividendOrdinaryRateAmount, DividendUpperRate, DividendUpperRateAmount, EmployeeNicAmount, HigherRateIncomeTax, HigherRateIncomeTaxAmount, IncomeFromEmployment, LessTaxFreeAmount, MarriageAllowanceReceivedAmount, MarriageAllowanceTransferredAmount, NicsAndTaxPerCurrencyUnit, OtherAdjustmentsIncreasing, OtherAdjustmentsReducing, OtherAllowancesAmount, OtherIncome, OtherPensionIncome, PayCgTaxOn, PersonalTaxFreeAmount, SavingsAdditionalIncome, SavingsAdditionalRateTax, SavingsHigherIncome, SavingsHigherRateTax, SavingsLowerIncome, SavingsLowerRateTax, ScottishAdditionalIncome, ScottishAdditionalRateTax, ScottishAdvancedIncome, ScottishBasicIncome, ScottishBasicRateTax, ScottishHigherIncome, ScottishHigherRateTax, ScottishIncomeTax, ScottishIntermediateIncome, ScottishIntermediateRateTax, ScottishStarterIncome, ScottishStarterRateTax, ScottishTopIncome, ScottishTopRateTax, ScottishTotalTax, SelfEmploymentIncome, StartingRateForSavings, StartingRateForSavingsAmount, TaxableGains, TaxableStateBenefits, TotalCgTax, TotalIncomeBeforeTax, TotalIncomeTax, TotalIncomeTaxAndNics, TotalTaxFreeAmount, WelshIncomeTax, YourTotalTax}
import common.models.RateKey.{Additional, CapitalGainsEntrepreneur, CapitalGainsOrdinary, CapitalGainsUpper, IncomeAdditional, IncomeBasic, IncomeHigher, InterestHigher, InterestLower, NICS, Ordinary, Savings, SavingsAdditionalRate, SavingsHigherRate, SavingsLowerRate, ScottishAdditionalRate, ScottishAdvancedRate, ScottishBasicRate, ScottishHigherRate, ScottishIntermediateRate, ScottishStarterRate, TotalCapitalGains, Upper}
import play.api.libs.json.*
import play.api.{Logger, Logging}
import sa.models.AtsMiddleTierData.noAtsResult
import sa.models.ODSLiabilities.ODSLiabilities.*
import sa.models.{AtsMiddleTierData, Nationality, TaxSummaryLiability}
import sa.services.TaxRateService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.model.DataEvent

import scala.concurrent.{ExecutionContext, Future}

case class ATSParsingException(s: String) extends Exception(s)

@Singleton
class ATSRawDataTransformer @Inject() (applicationConfig: ApplicationConfig, auditConnector: AuditConnector)(implicit
  ec: ExecutionContext
) extends Logging {

  private def initiateAudit(
    UTR: String,
    taxYear: Int,
    calculations: ATSCalculations
  )(implicit hc: HeaderCarrier): Future[Unit] = auditConnector
    .sendEvent(
      DataEvent(
        auditSource = applicationConfig.appName,
        auditType = "TaxLiability",
        detail = Map(
          "utr"                      -> UTR,
          "taxYear"                  -> taxYear.toString,
          "liabilityAmount"          -> calculations.taxLiability.amount.toString,
          "LiabilityCalculationUsed" -> calculations.taxLiability.calculus.getOrElse(
            "No calculation details present"
          )
        )
      )
    )
    .map(_ => (): Unit)

  def atsDataDTO(
    rawPayloadJson: JsValue,
    rawTaxPayerJson: JsValue,
    UTR: String,
    taxYear: Int
  )(implicit hc: HeaderCarrier): AtsMiddleTierData = {
    val taxRate = new TaxRateService(taxYear, applicationConfig.ratePercentages)
    val logger  = Logger(getClass.getName)
    ATSCalculations.make(rawPayloadJson.as[TaxSummaryLiability], taxRate) match {
      case Some(calculations) =>
        logger.debug(
          s"Liability for utr $UTR for tax year $taxYear is ${calculations.taxLiability.calculus.getOrElse("")}"
        )
        initiateAudit(UTR, taxYear, calculations)

        try
          AtsMiddleTierData.make(
            taxYear = taxYear,
            utr = UTR,
            incomeTax = createIncomeTaxData(calculations, taxRate, calculations.incomeTaxStatus),
            summary = createSummaryData(calculations: ATSCalculations),
            income = createIncomeData(calculations: ATSCalculations),
            allowance = createAllowanceData(calculations: ATSCalculations),
            capitalGains = createCapitalGainsData(calculations, taxRate),
            govSpending = createGovSpendData(calculations.totalTax, taxYear),
            taxPayer = createTaxPayerData(rawTaxPayerJson),
            taxLiability =
              Some(calculations.taxLiability) // Careful: taxLiability overridden based on Nationality/ tax year
          )
        catch {
          case x @ ATSParsingException(message) => AtsMiddleTierData.error(taxYear, message)
          case otherError: Throwable            =>
            logger.error("Unexpected error has occurred", otherError)
            AtsMiddleTierData.error(taxYear, "Other Error")
        }

      case _ => noAtsResult(taxYear)
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
      Adjustments                  -> calculations.adjustmentsToCapitalGains,
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
      DividendOrdinaryRate            -> calculations.get(DividendChargeableLowRate),
      DividendOrdinaryRateAmount      -> calculations.get(DividendTaxLowRate),
      DividendUpperRate               -> calculations.get(DividendChargeableHighRate),
      DividendUpperRateAmount         -> calculations.get(DividendTaxHighRate),
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
      ScottishTopRateTax              -> calculations.scottishTopRateTax,
      ScottishTotalTax                -> calculations.scottishTotalTax,
      ScottishStarterIncome           -> calculations.scottishStarterRateIncome,
      ScottishBasicIncome             -> calculations.scottishBasicRateIncome,
      ScottishIntermediateIncome      -> calculations.scottishIntermediateRateIncome,
      ScottishHigherIncome            -> calculations.scottishHigherRateIncome,
      ScottishAdvancedIncome          -> calculations.scottishAdvancedRateIncome,
      ScottishAdditionalIncome        -> calculations.scottishAdditionalRateIncome,
      ScottishTopIncome               -> calculations.scottishTopRateIncome,
      SavingsLowerRateTax             -> calculations.savingsBasicRateTax,
      SavingsHigherRateTax            -> calculations.savingsHigherRateTax,
      SavingsAdditionalRateTax        -> calculations.savingsAdditionalRateTax,
      SavingsLowerIncome              -> calculations.savingsBasicRateIncome,
      SavingsHigherIncome             -> calculations.savingsHigherRateIncome,
      SavingsAdditionalIncome         -> calculations.savingsAdditionalRateIncome,
      LiabilityKey.BrdCharge          -> calculations.brdCharge,
      LiabilityKey.BrdReduction       -> calculations.brdReduction
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
      ScottishAdvancedRate     -> taxRate.scottishAdvancedRate,
      ScottishAdditionalRate   -> taxRate.scottishAdditionalRate,
      SavingsLowerRate         -> taxRate.basicRateIncomeTaxRate(),
      SavingsHigherRate        -> taxRate.higherRateIncomeTaxRate(),
      SavingsAdditionalRate    -> taxRate.additionalRateIncomeTaxRate()
    ).view.mapValues(_.apiValue).toMap
}
