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

import models.LiabilityKey._
import models.RateKey.NICS
import models.paye.{PayeAtsData, PayeAtsMiddeTier}
import models.{Amount, ApiRate, DataHolder, GovernmentSpendingOutputWrapper, LiabilityKey, Rate, RateKey}

//TODO do we need nino/taxyear in class cons
class PayeAtsDataTransformer(nino: String, taxYear: Int, atsData: PayeAtsData) {

  def transformToPayeMiddleTier: PayeAtsMiddeTier =
    PayeAtsMiddeTier.make(
      taxYear,
      nino,
      emptyDataHolder,
      createSummaryData,
      createIncomeData,
      createAllowanceData,
      createGovSpendData
    )
  //TODO remove
  def emptyDataHolder: DataHolder = DataHolder(None, None, None)
//TODO check if this is the case for all values
  implicit def optionToAmount(opt: Option[Double]): Amount = opt.fold(Amount.empty)(Amount.gbp(_))

  private def createSummaryData: DataHolder =
    DataHolder.make(createSummaryDataMap, createSummaryRates)

  private def createSummaryDataMap: Map[LiabilityKey, Amount] =
    Map(
      TotalIncomeBeforeTax  -> atsData.income.flatMap(_.incomeBeforeTax),
      TotalTaxFreeAmount    -> atsData.income.flatMap(_.taxableIncome),
      TotalIncomeTaxAndNics -> atsData.calculatedTotals.flatMap(_.totalIncomeTaxNics),
      IncomeAfterTaxAndNics -> atsData.calculatedTotals.flatMap(_.incomeAfterTaxNics),
      TotalIncomeTax        -> atsData.calculatedTotals.flatMap(_.totalIncomeTax2),
      EmployeeNicAmount     -> atsData.nationalInsurance.flatMap(_.employeeContributions),
      EmployerNicAmount     -> atsData.nationalInsurance.flatMap(_.employerContributions)
    )

  private def createSummaryRates: Map[RateKey, ApiRate] = Map(
    NICS -> Rate(atsData.averageRateTax.getOrElse(0).toDouble).apiValue
  )

  private def createIncomeData: DataHolder =
    DataHolder.make(createIncomePayload)

  private def createIncomePayload: Map[LiabilityKey, Amount] =
    Map(
      IncomeFromEmployment   -> atsData.income.flatMap(_.incomeFromEmployment),
      StatePension           -> atsData.income.flatMap(_.statePension),
      OtherPensionIncome     -> atsData.income.flatMap(_.otherPensionIncome),
      OtherIncome            -> atsData.income.flatMap(_.otherIncome),
      TotalIncomeBeforeTax   -> atsData.income.flatMap(_.incomeBeforeTax),
      BenefitsFromEmployment -> atsData.income.flatMap(_.employmentBenefits),
      TaxableStateBenefits   -> atsData.taxableStateBenefits
    )

  private def createAllowanceData: DataHolder =
    DataHolder.make(createAllowancePayload)

  private def createAllowancePayload: Map[LiabilityKey, Amount] =
    Map(
      PersonalTaxFreeAmount              -> atsData.adjustments.flatMap(_.taxFreeAmount),
      MarriageAllowanceTransferredAmount -> atsData.adjustments.flatMap(_.marriageAllowanceTransferred),
      OtherAllowancesAmount              -> atsData.income.flatMap(_.otherAllowancesDeductionsExpenses),
      TotalTaxFreeAmount                 -> atsData.income.flatMap(_.taxableIncome),
      TotalIncomeBeforeTax               -> atsData.income.flatMap(_.incomeBeforeTax)
    )

  private def createGovSpendData: GovernmentSpendingOutputWrapper =
    GovSpendingDataTransformer(atsData.calculatedTotals.flatMap(_.totalIncomeTaxNics), taxYear).govSpendReferenceDTO
}
