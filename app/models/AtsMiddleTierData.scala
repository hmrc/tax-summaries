/*
 * Copyright 2019 HM Revenue & Customs
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

case class AtsMiddleTierData[A](taxYear: Int, utr: Option[String], income_tax: Option[DataHolder[A]], summary_data: Option[DataHolder[A]],
                         income_data: Option[DataHolder[A]], allowance_data: Option[DataHolder[A]], capital_gains_data: Option[DataHolder[A]],
                         gov_spending: Option[GovernmentSpendingOutputWrapper], taxPayerData: Option[AtsMiddleTierTaxpayerData],
                         errors: Option[AtsError])

object AtsMiddleTierData {
  implicit def formats[A:Format]:Format[AtsMiddleTierData[A]] = Json.format[AtsMiddleTierData[A]]
}
