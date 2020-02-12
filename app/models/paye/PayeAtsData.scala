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

import models._
import models.LiabilityKey._
import models.RateKey.{IncomeBasic, IncomeHigher, NICS, Ordinary, Upper}
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

  def transformToPayeMiddleTier(nino: String, taxYear: Int): PayeAtsMiddleTier =
    PayeAtsMiddleTier(
      taxYear,
      nino,
      createIncomeTaxData,
      createSummaryData,
      createIncomeData,
      createAllowanceData,
      createGovSpendData(taxYear)
    )

  implicit def optionToAmount(opt: Option[Double]): Amount = opt.fold(Amount.empty)(Amount.gbp(_))
  implicit def optionToRate(opt: Option[Double]): ApiRate = Rate(opt.getOrElse(0)).apiValue

  private def createIncomeTaxData: DataHolder =
    DataHolder.make(createIncomeTaxPayload, createIncomeTaxRates)

  private def createIncomeTaxPayload: Map[LiabilityKey, Amount] =
    Map(
      BasicRateIncomeTaxAmount        -> basicRateBand.map(_.basicRateTax),
      BasicRateIncomeTax              -> basicRateBand.map(_.basicRateTaxAmount),
      HigherRateIncomeTaxAmount       -> higherRateBand.map(_.higherRateTax),
      HigherRateIncomeTax             -> higherRateBand.map(_.higherRateTaxAmount),
      OrdinaryRateAmount              -> dividendLowerBand.map(_.dividendLowRateTax),
      OrdinaryRate                    -> dividendLowerBand.map(_.dividendLowRateAmount),
      UpperRateAmount                 -> dividendHigherBand.map(_.dividendHigherRateTax),
      UpperRate                       -> dividendHigherBand.map(_.dividendHigherRateAmount),
      MarriedCouplesAllowance         -> adjustments.flatMap(_.marriedCouplesAllowanceAdjustment),
      MarriageAllowanceReceivedAmount -> adjustments.flatMap(_.marriageAllowanceReceived),
      LessTaxAdjustmentPrevYear       -> adjustments.flatMap(_.lessTaxAdjustmentPreviousYear),
      TaxUnderpaidPrevYear            -> adjustments.flatMap(_.taxUnderpaidPreviousYear),
      TotalIncomeTax                  -> calculatedTotals.flatMap(_.totalIncomeTax)
    )

  private def createIncomeTaxRates: Map[RateKey, ApiRate] =
    Map(
      Ordinary     -> dividendLowerBand.map(_.dividendLowRate),
      IncomeHigher -> higherRateBand.map(_.higherRate),
      IncomeBasic  -> basicRateBand.map(_.basicRate),
      Upper        -> dividendHigherBand.map(_.dividendHigherRate)
    )

  private def createSummaryData: DataHolder =
    DataHolder.make(createSummaryDataMap, createSummaryRates)

  private def createSummaryDataMap: Map[LiabilityKey, Amount] =
    Map(
      TotalIncomeBeforeTax  -> income.flatMap(_.incomeBeforeTax),
      TotalTaxFreeAmount    -> income.flatMap(_.taxableIncome),
      TotalIncomeTaxAndNics -> calculatedTotals.flatMap(_.totalIncomeTaxNics),
      IncomeAfterTaxAndNics -> calculatedTotals.flatMap(_.incomeAfterTaxNics),
      TotalIncomeTax        -> calculatedTotals.flatMap(_.totalIncomeTax2),
      EmployeeNicAmount     -> nationalInsurance.flatMap(_.employeeContributions),
      EmployerNicAmount     -> nationalInsurance.flatMap(_.employerContributions)
    )

  private def createSummaryRates: Map[RateKey, ApiRate] = Map(
    NICS -> averageRateTax.map(_.toDouble)
  )

  private def createIncomeData: DataHolder =
    DataHolder.make(createIncomePayload)

  private def createIncomePayload: Map[LiabilityKey, Amount] =
    Map(
      IncomeFromEmployment   -> income.flatMap(_.incomeFromEmployment),
      StatePension           -> income.flatMap(_.statePension),
      OtherPensionIncome     -> income.flatMap(_.otherPensionIncome),
      OtherIncome            -> income.flatMap(_.otherIncome),
      TotalIncomeBeforeTax   -> income.flatMap(_.incomeBeforeTax),
      BenefitsFromEmployment -> income.flatMap(_.employmentBenefits),
      TaxableStateBenefits   -> taxableStateBenefits
    )

  private def createAllowanceData: DataHolder =
    DataHolder.make(createAllowancePayload)

  private def createAllowancePayload: Map[LiabilityKey, Amount] =
    Map(
      PersonalTaxFreeAmount              -> adjustments.flatMap(_.taxFreeAmount),
      MarriageAllowanceTransferredAmount -> adjustments.flatMap(_.marriageAllowanceTransferred),
      OtherAllowancesAmount              -> income.flatMap(_.otherAllowancesDeductionsExpenses),
      TotalTaxFreeAmount                 -> income.flatMap(_.taxableIncome),
      TotalIncomeBeforeTax               -> income.flatMap(_.incomeBeforeTax)
    )

  private def createGovSpendData(taxYear: Int): GovernmentSpendingOutputWrapper =
    GovSpendingDataTransformer(calculatedTotals.flatMap(_.totalIncomeTaxNics), taxYear).govSpendReferenceDTO

}

object PayeAtsData {
  implicit val reads: Reads[PayeAtsData] = Json.reads[PayeAtsData]
}
