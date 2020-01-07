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

import errors.AtsError
import play.api.libs.json.{Format, Json}

case class AtsMiddleTierData(
  taxYear: Int,
  utr: Option[String],
  income_tax: Option[DataHolder],
  summary_data: Option[DataHolder],
  income_data: Option[DataHolder],
  allowance_data: Option[DataHolder],
  capital_gains_data: Option[DataHolder],
  gov_spending: Option[GovernmentSpendingOutputWrapper],
  taxPayerData: Option[AtsMiddleTierTaxpayerData],
  errors: Option[AtsError])

object AtsMiddleTierData {
  implicit val formats: Format[AtsMiddleTierData] = Json.format[AtsMiddleTierData]

  def make(
    taxYear: Int,
    utr: String,
    incomeTax: DataHolder,
    summary: DataHolder,
    income: DataHolder,
    allowance: DataHolder,
    capitalGains: DataHolder,
    govSpending: GovernmentSpendingOutputWrapper,
    taxPayer: AtsMiddleTierTaxpayerData
  ): AtsMiddleTierData =
    AtsMiddleTierData(
      taxYear,
      Some(utr),
      Some(incomeTax),
      Some(summary),
      Some(income),
      Some(allowance),
      Some(capitalGains),
      Some(govSpending),
      Some(taxPayer),
      None
    )

  def error(taxYear: Int, message: String): AtsMiddleTierData =
    AtsMiddleTierData(taxYear, None, None, None, None, None, None, None, None, Option(AtsError(message)))

  def noAtsResult(taxYear: Int): AtsMiddleTierData = error(taxYear, "NoAtsError")
}
