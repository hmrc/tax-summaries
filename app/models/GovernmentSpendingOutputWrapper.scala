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

package models

import config.ApplicationConfig
import errors.AtsError
import play.api.libs.json.Json
import services.{GoodsAndServices, GovSpendService}

case class GovernmentSpendingOutputWrapper(
  taxYear: Int,
  govSpendAmountData: Map[GoodsAndServices, SpendData],
  totalAmount: Amount,
  errors: Option[AtsError])

object GovernmentSpendingOutputWrapper {
  implicit val formats = Json.format[GovernmentSpendingOutputWrapper]

  def apply(
    applicationConfig: ApplicationConfig,
    totalTaxAmount: Amount,
    taxYear: Int): GovernmentSpendingOutputWrapper = {

    def createSpendDataItem(spendCategory: GoodsAndServices, spendPercentage: BigDecimal, amount: Amount): SpendData = {
      val monetaryBD = getMonetaryAmount(spendPercentage, amount)
      val monetaryAmount = Amount.gbp(monetaryBD)
      SpendData(monetaryAmount, spendPercentage)
    }

    def createGovernmentSpendingAmounts = new GovSpendService(applicationConfig).govSpending(taxYear) map {
      case (key, value) =>
        (key, createSpendDataItem(key, value, totalTaxAmount))
    }

    def getMonetaryAmount(percentage: BigDecimal, amount: Amount) =
      ((percentage / 100) * amount.amount).setScale(2, BigDecimal.RoundingMode.HALF_UP)

    GovernmentSpendingOutputWrapper(taxYear, createGovernmentSpendingAmounts, totalTaxAmount, None)
  }
}
