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

import errors.AtsError
import models.{DataHolder, GovernmentSpendingOutputWrapper}
import play.api.libs.json.{Json, Writes}

case class PayeAtsMiddeTier(
  taxYear: Int,
  nino: String,
  income_tax: Option[DataHolder],
  summary_data: Option[DataHolder],
  income_data: Option[DataHolder],
  allowance_data: Option[DataHolder],
  gov_spending: Option[GovernmentSpendingOutputWrapper],
  errors: Option[AtsError]
)

object PayeAtsMiddeTier {
  implicit val writes: Writes[PayeAtsMiddeTier] = Json.writes[PayeAtsMiddeTier]

  def make(
    taxYear: Int,
    nino: String,
    incomeTax: DataHolder,
    summary: DataHolder,
    income: DataHolder,
    allowance: DataHolder,
    govSpending: GovernmentSpendingOutputWrapper
  ): PayeAtsMiddeTier =
    PayeAtsMiddeTier(
      taxYear,
      nino,
      Some(incomeTax),
      Some(summary),
      Some(income),
      Some(allowance),
      Some(govSpending),
      None
    )

  def error(nino: String, taxYear: Int, message: String): PayeAtsMiddeTier =
    PayeAtsMiddeTier(taxYear, nino, None, None, None, None, None, Option(AtsError(message)))

  def noAtsResult(nino: String, taxYear: Int): PayeAtsMiddeTier = error(nino, taxYear, "NoAtsError")
}
