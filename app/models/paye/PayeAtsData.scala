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

package models.paye

import config.ApplicationConfig
import models.LiabilityKey._
import models.RateKey._
import models._
import play.api.libs.json.{Json, Reads}

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
      Some(createIncomeData(applicationConfig)),
      Some(createAllowanceData),
      Some(createGovSpendData(applicationConfig, taxYear))
    )

  def optionToAmount(key: LiabilityKey, opt: Option[Double]): Amount =
    opt.fold(Amount.empty(key.apiValue))(Amount.gbp(_, key.apiValue))
  def optionToRate(opt: Option[Double]): ApiRate                     = Rate(opt.getOrElse(0)).apiValue

  private def createIncomeTaxData: DataHolder =
    DataHolder.make(createIncomeTaxPayload, createIncomeTaxRates)

  private def createIncomeTaxPayload: Map[LiabilityKey, Amount] =
    Map(
      BasicRateIncomeTaxAmount                -> optionToAmount(BasicRateIncomeTaxAmount, basicRateBand.map(_.basicRateTax)),
      BasicRateIncomeTax                      -> optionToAmount(BasicRateIncomeTax, basicRateBand.map(_.basicRateTaxAmount)),
      HigherRateIncomeTaxAmount               -> optionToAmount(HigherRateIncomeTaxAmount, higherRateBand.map(_.higherRateTax)),
      HigherRateIncomeTax                     -> optionToAmount(HigherRateIncomeTax, higherRateBand.map(_.higherRateTaxAmount)),
      OrdinaryRateAmount                      -> optionToAmount(OrdinaryRateAmount, dividendLowerBand.map(_.dividendLowRateTax)),
      OrdinaryRate                            -> optionToAmount(OrdinaryRate, dividendLowerBand.map(_.dividendLowRateAmount)),
      UpperRateAmount                         -> optionToAmount(UpperRateAmount, dividendHigherBand.map(_.dividendHigherRateTax)),
      UpperRate                               -> optionToAmount(UpperRate, dividendHigherBand.map(_.dividendHigherRateAmount)),
      MarriedCouplesAllowance                 -> optionToAmount(
        MarriedCouplesAllowance,
        adjustments.flatMap(_.marriedCouplesAllowanceAdjustment)
      ),
      MarriageAllowanceReceivedAmount         -> optionToAmount(
        MarriageAllowanceReceivedAmount,
        adjustments.flatMap(_.marriageAllowanceReceived)
      ),
      LessTaxAdjustmentPrevYear               -> optionToAmount(
        LessTaxAdjustmentPrevYear,
        adjustments.flatMap(_.lessTaxAdjustmentPreviousYear)
      ),
      TaxUnderpaidPrevYear                    -> optionToAmount(TaxUnderpaidPrevYear, adjustments.flatMap(_.taxUnderpaidPreviousYear)),
      TotalIncomeTax                          -> optionToAmount(TotalIncomeTax, calculatedTotals.flatMap(_.totalIncomeTax)),
      TotalUKIncomeTax                        -> optionToAmount(TotalUKIncomeTax, calculatedTotals.flatMap(_.totalUKIncomeTax)),
      TotalIncomeTax2                         -> optionToAmount(TotalIncomeTax2, calculatedTotals.flatMap(_.totalIncomeTax2)),
      ScottishTotalTax                        -> optionToAmount(ScottishTotalTax, calculatedTotals.flatMap(_.totalScottishIncomeTax)),
      ScottishStarterRateIncomeTaxAmount      -> optionToAmount(
        ScottishStarterRateIncomeTaxAmount,
        scottishStarterBand.map(_.scottishStarterRateTax)
      ),
      ScottishStarterRateIncomeTax            -> optionToAmount(
        ScottishStarterRateIncomeTax,
        scottishStarterBand.map(_.scottishStarterRateTaxAmount)
      ),
      ScottishBasicRateIncomeTaxAmount        -> optionToAmount(
        ScottishBasicRateIncomeTaxAmount,
        scottishBasicBand.map(_.scottishBasicRateTax)
      ),
      ScottishBasicRateIncomeTax              -> optionToAmount(
        ScottishBasicRateIncomeTax,
        scottishBasicBand.map(_.scottishBasicRateTaxAmount)
      ),
      ScottishIntermediateRateIncomeTaxAmount -> optionToAmount(
        ScottishIntermediateRateIncomeTaxAmount,
        scottishIntermediateBand.map(_.scottishIntermediateRateTax)
      ),
      ScottishIntermediateRateIncomeTax       -> optionToAmount(
        ScottishIntermediateRateIncomeTax,
        scottishIntermediateBand.map(_.scottishIntermediateRateTaxAmount)
      ),
      ScottishHigherRateIncomeTaxAmount       -> optionToAmount(
        ScottishHigherRateIncomeTaxAmount,
        scottishHigherBand.map(_.scottishHigherRateTax)
      ),
      ScottishHigherRateIncomeTax             -> optionToAmount(
        ScottishHigherRateIncomeTax,
        scottishHigherBand.map(_.scottishHigherRateTaxAmount)
      )
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
      TotalIncomeBeforeTax  -> optionToAmount(TotalIncomeBeforeTax, income.flatMap(_.incomeBeforeTax)),
      TotalTaxFreeAmount    -> optionToAmount(TotalTaxFreeAmount, income.flatMap(_.taxableIncome)),
      TotalIncomeTaxAndNics -> optionToAmount(TotalIncomeTaxAndNics, calculatedTotals.flatMap(_.totalIncomeTaxNics)),
      IncomeAfterTaxAndNics -> optionToAmount(IncomeAfterTaxAndNics, calculatedTotals.flatMap(_.incomeAfterTaxNics)),
      TotalIncomeTax        -> optionToAmount(TotalIncomeTax, calculatedTotals.flatMap(_.totalIncomeTax2)),
      TotalIncomeTax2Nics   -> optionToAmount(TotalIncomeTax2Nics, calculatedTotals.flatMap(_.totalIncomeTax2Nics)),
      EmployeeNicAmount     -> optionToAmount(EmployeeNicAmount, nationalInsurance.flatMap(_.employeeContributions)),
      EmployerNicAmount     -> optionToAmount(EmployerNicAmount, nationalInsurance.flatMap(_.employerContributions)),
      LiableTaxAmount       -> optionToAmount(LiableTaxAmount, calculatedTotals.flatMap(_.liableTaxAmount))
    )

  private def createSummaryRates: Map[RateKey, ApiRate] = Map(
    NICS -> optionToRate(averageRateTax.map(_.toDouble))
  )

  private def createIncomeData(applicationConfig: ApplicationConfig): DataHolder =
    DataHolder.make(createIncomePayload)

  private def createIncomePayload: Map[LiabilityKey, Amount] =
    Map(
      IncomeFromEmployment   -> optionToAmount(IncomeFromEmployment, income.flatMap(_.incomeFromEmployment)),
      StatePension           -> optionToAmount(StatePension, income.flatMap(_.statePension)),
      OtherPensionIncome     -> optionToAmount(OtherPensionIncome, income.flatMap(_.otherPensionIncome)),
      OtherIncome            -> optionToAmount(OtherIncome, income.flatMap(_.otherIncome)),
      TotalIncomeBeforeTax   -> optionToAmount(TotalIncomeBeforeTax, income.flatMap(_.incomeBeforeTax)),
      BenefitsFromEmployment -> optionToAmount(BenefitsFromEmployment, income.flatMap(_.employmentBenefits)),
      TaxableStateBenefits   -> optionToAmount(TaxableStateBenefits, taxableStateBenefits),
      ScottishIncomeTax      -> optionToAmount(ScottishIncomeTax, scottishIncomeTax)
    )

  private def createAllowanceData: DataHolder =
    DataHolder.make(createAllowancePayload)

  private def createAllowancePayload: Map[LiabilityKey, Amount] =
    Map(
      PersonalTaxFreeAmount              -> optionToAmount(PersonalTaxFreeAmount, adjustments.flatMap(_.taxFreeAmount)),
      MarriageAllowanceTransferredAmount -> optionToAmount(
        MarriageAllowanceTransferredAmount,
        adjustments.flatMap(_.marriageAllowanceTransferred)
      ),
      OtherAllowancesAmount              -> optionToAmount(
        OtherAllowancesAmount,
        income.flatMap(_.otherAllowancesDeductionsExpenses)
      ),
      TotalTaxFreeAmount                 -> optionToAmount(TotalTaxFreeAmount, income.flatMap(_.taxableIncome)),
      TotalIncomeBeforeTax               -> optionToAmount(TotalIncomeBeforeTax, income.flatMap(_.incomeBeforeTax))
    )

  private def createGovSpendData(
    applicationConfig: ApplicationConfig,
    taxYear: Int
  ): GovernmentSpendingOutputWrapper = {
    val totalIncome = optionToAmount(
      TotalIncomeTax,
      if (nationalInsurance.flatMap(_.employeeContributions).isDefined) {
        calculatedTotals.flatMap(_.totalIncomeTaxNics)
      } else {
        calculatedTotals.flatMap(_.totalIncomeTax)
      }
    )
    GovernmentSpendingOutputWrapper(applicationConfig, totalIncome, taxYear)
  }

}

object PayeAtsData {
  implicit val reads: Reads[PayeAtsData] = Json.reads[PayeAtsData]
}
