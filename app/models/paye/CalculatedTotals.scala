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

import play.api.libs.json.{Json, Reads}

case class CalculatedTotals(
  totalIncomeTax: Option[Double],
  totalIncomeTaxNics: Option[Double],
  totalScottishIncomeTax: Option[Double],
  totalIncomeTax2: Option[Double],
  totalUKIncomeTax: Option[Double],
  totalIncomeTax2Nics: Option[Double],
  incomeAfterTaxNics: Option[Double],
  liableTaxAmount: Option[Double],
  incomeAfterTax2Nics: Option[Double]
)

object CalculatedTotals {
  implicit val reads: Reads[CalculatedTotals] = Json.reads[CalculatedTotals]
}
