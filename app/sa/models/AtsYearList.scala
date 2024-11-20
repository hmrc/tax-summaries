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

package sa.models

import play.api.libs.json.{JsNumber, Json, OFormat}

case class AtsYearList(utr: String, taxPayer: Option[AtsMiddleTierTaxpayerData], atsYearList: Option[List[JsNumber]])

object AtsYearList {
  implicit val formats: OFormat[AtsYearList] = Json.format[AtsYearList]
}
