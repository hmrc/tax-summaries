/*
 * Copyright 2022 HM Revenue & Customs
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

import models.{DataHolder, GovernmentSpendingOutputWrapper}
import play.api.libs.json.{Format, Json}

case class PayeAtsMiddleTier(
  taxYear: Int,
  nino: String,
  income_tax: Option[DataHolder],
  summary_data: Option[DataHolder],
  income_data: Option[DataHolder],
  allowance_data: Option[DataHolder],
  gov_spending: Option[GovernmentSpendingOutputWrapper]
)

object PayeAtsMiddleTier {

  implicit val format: Format[PayeAtsMiddleTier] = Json.format[PayeAtsMiddleTier]

}
