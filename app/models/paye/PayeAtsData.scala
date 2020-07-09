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

package models.paye

import config.ApplicationConfig
import models.LiabilityKey._
import models.RateKey._
import models._
import play.api.libs.json.{Json, Reads}
import transformers.GovSpendingDataTransformer

case class PayeAtsData(
  taxableStateBenefits: Option[Double],
  averageRateTax: Option[Int],
  scottishIncomeTax: Option[Double],
  adjustments: Option[Adjustments],
  income: Option[Income],
  nationalInsurance: Option[NationalInsurance],
  calculatedTotals: Option[CalculatedTotals],
  basicRateBand: Option[BasicRateBand],
  higherRateBand: Option[HigherRateBand],
  dividendLowerBand: Option[DividendLowerBand],
  dividendHigherBand: Option[DividendHigherBand],
  scottishStarterBand: Option[ScottishStarterBand],
  scottishBasicBand: Option[ScottishBasicBand],
  scottishIntermediateBand: Option[ScottishIntermediateBand],
  scottishHigherBand: Option[ScottishHigherBand],
  savingsStarterBand: Option[SavingsStarterBand]
) {

  def transformToPayeMiddleTier(applicationConfig: ApplicationConfig, nino: String, taxYear: Int): PayeAtsMiddleTier =
    PayeAtsMiddleTier(
      taxYear,
      nino,
      Some(createIncomeTaxData),
      Some(createSummaryData),
      Some(createIncomeData),
      Some(createAllowanceData),
      Some(createGovSpendData(applicationConfig, taxYear))
    )

  def optionToAmount(opt: Option[Double]): Amount = opt.fold(Amount.empty)(Amount.gbp(_))
  def optionToRate(opt: Option[Double]): ApiRate = Rate(opt.getOrElse(0)).apiValue

  private def createIncomeTaxData: DataHolder =
    DataHolder.make(createIncomeTaxPayload, createIncomeTaxRates)

  private def createIncomeTaxPayload: Map[LiabilityKey, Amount] =
    Map(
      BasicRateIncomeTaxAmount           -> optionToAmount(basicRateBand.map(_.basicRateTax)),
      BasicRateIncomeTax                 -> optionToAmount(basicRateBand.map(_.basicRateTaxAmount)),
      HigherRateIncomeTaxAmount          -> optionToAmount(higherRateBand.map(_.higherRateTax)),
      HigherRateIncomeTax                -> optionToAmount(higherRateBand.map(_.higherRateTaxAmount)),
      OrdinaryRateAmount                 -> optionToAmount(dividendLowerBand.map(_.dividendLowRateTax)),
      OrdinaryRate                       -> optionToAmount(dividendLowerBand.map(_.dividendLowRateAmount)),
      UpperRateAmount                    -> optionToAmount(dividendHigherBand.map(_.dividendHigherRateTax)),
      UpperRate                          -> optionToAmount(dividendHigherBand.map(_.dividendHigherRateAmount)),
      MarriedCouplesAllowance            -> optionToAmount(adjustments.flatMap(_.marriedCouplesAllowanceAdjustment)),
      MarriageAllowanceReceivedAmount    -> optionToAmount(adjustments.flatMap(_.marriageAllowanceReceived)),
      LessTaxAdjustmentPrevYear          -> optionToAmount(adjustments.flatMap(_.lessTaxAdjustmentPreviousYear)),
      TaxUnderpaidPrevYear               -> optionToAmount(adjustments.flatMap(_.taxUnderpaidPreviousYear)),
      TotalIncomeTax                     -> optionToAmount(calculatedTotals.flatMap(_.totalIncomeTax)),
      TotalUKIncomeTax                   -> optionToAmount(calculatedTotals.flatMap(_.totalUKIncomeTax)),
      TotalIncomeTax2                    -> optionToAmount(calculatedTotals.flatMap(_.totalIncomeTax2)),
      ScottishTotalTax                   -> optionToAmount(calculatedTotals.flatMap(_.totalScottishIncomeTax)),
      ScottishStarterRateIncomeTaxAmount -> optionToAmount(scottishStarterBand.map(_.scottishStarterRateTax)),
      ScottishStarterRateIncomeTax       -> optionToAmount(scottishStarterBand.map(_.scottishStarterRateTaxAmount)),
      ScottishBasicRateIncomeTaxAmount   -> optionToAmount(scottishBasicBand.map(_.scottishBasicRateTax)),
      ScottishBasicRateIncomeTax         -> optionToAmount(scottishBasicBand.map(_.scottishBasicRateTaxAmount)),
      ScottishIntermediateRateIncomeTaxAmount -> optionToAmount(
        scottishIntermediateBand.map(_.scottishIntermediateRateTax)),
      ScottishIntermediateRateIncomeTax -> optionToAmount(
        scottishIntermediateBand.map(_.scottishIntermediateRateTaxAmount)),
      ScottishHigherRateIncomeTaxAmount -> optionToAmount(scottishHigherBand.map(_.scottishHigherRateTax)),
      ScottishHigherRateIncomeTax       -> optionToAmount(scottishHigherBand.map(_.scottishHigherRateTaxAmount))
    )

  private def createIncomeTaxRates: Map[RateKey, ApiRate] =
    Map(
      PayeDividendOrdinaryRate     -> optionToRate(dividendLowerBand.map(_.dividendLowRate)),
      PayeHigherRateIncomeTax      -> optionToRate(higherRateBand.map(_.higherRate)),
      PayeBasicRateIncomeTax       -> optionToRate(basicRateBand.map(_.basicRate)),
      PayeDividendUpperRate        -> optionToRate(dividendHigherBand.map(_.dividendHigherRate)),
      PayeScottishStarterRate      -> optionToRate(scottishStarterBand.map(_.scottishStarterRate)),
      PayeScottishBasicRate        -> optionToRate(scottishBasicBand.map(_.scottishBasicRate)),
      PayeScottishIntermediateRate -> optionToRate(scottishIntermediateBand.map(_.scottishIntermediateRate)),
      PayeScottishHigherRate       -> optionToRate(scottishHigherBand.map(_.scottishHigherRate))
    )

  private def createSummaryData: DataHolder =
    DataHolder.make(createSummaryDataMap, createSummaryRates)

  private def createSummaryDataMap: Map[LiabilityKey, Amount] =
    Map(
      TotalIncomeBeforeTax  -> optionToAmount(income.flatMap(_.incomeBeforeTax)),
      TotalTaxFreeAmount    -> optionToAmount(income.flatMap(_.taxableIncome)),
      TotalIncomeTaxAndNics -> optionToAmount(calculatedTotals.flatMap(_.totalIncomeTaxNics)),
      IncomeAfterTaxAndNics -> optionToAmount(calculatedTotals.flatMap(_.incomeAfterTaxNics)),
      TotalIncomeTax        -> optionToAmount(calculatedTotals.flatMap(_.totalIncomeTax2)),
      TotalIncomeTax2Nics   -> optionToAmount(calculatedTotals.flatMap(_.totalIncomeTax2Nics)),
      EmployeeNicAmount     -> optionToAmount(nationalInsurance.flatMap(_.employeeContributions)),
      EmployerNicAmount     -> optionToAmount(nationalInsurance.flatMap(_.employerContributions)),
      LiableTaxAmount       -> optionToAmount(calculatedTotals.flatMap(_.liableTaxAmount))
    )

  private def createSummaryRates: Map[RateKey, ApiRate] = Map(
    NICS -> optionToRate(averageRateTax.map(_.toDouble))
  )

  private def createIncomeData: DataHolder =
    DataHolder.make(createIncomePayload)

  private def createIncomePayload: Map[LiabilityKey, Amount] =
    Map(
      IncomeFromEmployment   -> optionToAmount(income.flatMap(_.incomeFromEmployment)),
      StatePension           -> optionToAmount(income.flatMap(_.statePension)),
      OtherPensionIncome     -> optionToAmount(income.flatMap(_.otherPensionIncome)),
      OtherIncome            -> optionToAmount(income.flatMap(_.otherIncome)),
      TotalIncomeBeforeTax   -> optionToAmount(income.flatMap(_.incomeBeforeTax)),
      BenefitsFromEmployment -> optionToAmount(income.flatMap(_.employmentBenefits)),
      TaxableStateBenefits   -> optionToAmount(taxableStateBenefits)
    )

  private def createAllowanceData: DataHolder =
    DataHolder.make(createAllowancePayload)

  private def createAllowancePayload: Map[LiabilityKey, Amount] =
    Map(
      PersonalTaxFreeAmount              -> optionToAmount(adjustments.flatMap(_.taxFreeAmount)),
      MarriageAllowanceTransferredAmount -> optionToAmount(adjustments.flatMap(_.marriageAllowanceTransferred)),
      OtherAllowancesAmount              -> optionToAmount(income.flatMap(_.otherAllowancesDeductionsExpenses)),
      TotalTaxFreeAmount                 -> optionToAmount(income.flatMap(_.taxableIncome)),
      TotalIncomeBeforeTax               -> optionToAmount(income.flatMap(_.incomeBeforeTax))
    )

  private def createGovSpendData(
    applicationConfig: ApplicationConfig,
    taxYear: Int): GovernmentSpendingOutputWrapper = {
    val totalIncome = optionToAmount(
      if (nationalInsurance.flatMap(_.employeeContributions).isDefined) {
        calculatedTotals.flatMap(_.totalIncomeTaxNics)
      } else {
        calculatedTotals.flatMap(_.totalIncomeTax)
      }
    )
    val correctYearForGovSpendCategories = taxYear + 1
    GovSpendingDataTransformer(applicationConfig, totalIncome, correctYearForGovSpendCategories).govSpendReferenceDTO
  }

}

object PayeAtsData {
  implicit val reads: Reads[PayeAtsData] = Json.reads[PayeAtsData]
}
