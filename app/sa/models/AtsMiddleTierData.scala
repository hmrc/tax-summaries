/*
 * Copyright 2026 HM Revenue & Customs
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

package sa.models

import common.errors.AtsError
import common.models.{Amount, DataHolder, GovernmentSpendingOutputWrapper}
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
  taxPayerData: Option[Map[String, String]],
  errors: Option[AtsError],
  taxLiability: Option[Amount]
)

object AtsMiddleTierData {
  implicit val formats: Format[AtsMiddleTierData] = Json.format[AtsMiddleTierData]

  // scalastyle:off parameter.number
  def make(
    taxYear: Int,
    utr: String,
    incomeTax: DataHolder,
    summary: DataHolder,
    income: DataHolder,
    allowance: DataHolder,
    capitalGains: DataHolder,
    govSpending: GovernmentSpendingOutputWrapper,
    taxPayer: Option[Map[String, String]],
    taxLiability: Option[Amount]
  ): AtsMiddleTierData =
    AtsMiddleTierData(
      taxYear = taxYear,
      utr = Some(utr),
      income_tax = Some(incomeTax),
      summary_data = Some(summary),
      income_data = Some(income),
      allowance_data = Some(allowance),
      capital_gains_data = Some(capitalGains),
      gov_spending = Some(govSpending),
      taxPayerData = taxPayer,
      errors = None,
      taxLiability = taxLiability
    )

  def error(taxYear: Int, message: String): AtsMiddleTierData =
    AtsMiddleTierData(
      taxYear = taxYear,
      utr = None,
      income_tax = None,
      summary_data = None,
      income_data = None,
      allowance_data = None,
      capital_gains_data = None,
      gov_spending = None,
      taxPayerData = None,
      errors = Option(AtsError(message)),
      taxLiability = None
    )

  def noAtsResult(taxYear: Int): AtsMiddleTierData = error(taxYear, "NoAtsError")
}
