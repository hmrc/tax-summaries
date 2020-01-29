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

import models.LiabilityKey.{MarriageAllowanceTransferredAmount, OtherAllowancesAmount, PersonalTaxFreeAmount, TotalTaxFreeAmount}
import models.paye.{PayeAtsData, PayeAtsMiddeTier}
import models.{Amount, DataHolder, GovernmentSpendingOutputWrapper, LiabilityKey}

//TODO do we need nino/taxyear in class cons
class PayeAtsDataTransformer(nino: String, taxYear: Int, atsData: PayeAtsData) {

  def transformToPayeMiddleTier: PayeAtsMiddeTier =
    PayeAtsMiddeTier.make(
      taxYear,
      nino,
      emptyDataHolder,
      emptyDataHolder,
      emptyDataHolder,
      createAllowanceData,
      createGovSpendData
    )
  //TODO remove
  def emptyDataHolder: DataHolder = DataHolder(None, None, None)
//TODO check if this is the case for all values
  implicit def optionToAmount(opt: Option[Double]): Amount = opt.fold(Amount.empty)(Amount.gbp(_))

  private def createAllowanceData: DataHolder =
    DataHolder.make(createYourTaxFreeAmountBreakdown)

  private def createYourTaxFreeAmountBreakdown: Map[LiabilityKey, Amount] =
    Map(
      PersonalTaxFreeAmount              -> atsData.adjustments.flatMap(_.taxFreeAmount),
      MarriageAllowanceTransferredAmount -> atsData.adjustments.flatMap(_.marriageAllowanceTransferred),
      OtherAllowancesAmount              -> atsData.income.flatMap(_.otherAllowancesDeductionsExpenses),
      TotalTaxFreeAmount                 -> atsData.income.flatMap(_.taxableIncome)
    )

  private def createGovSpendData: GovernmentSpendingOutputWrapper =
    GovSpendingDataTransformer(atsData.calculatedTotals.flatMap(_.totalIncomeTaxNics), taxYear).govSpendReferenceDTO
}
