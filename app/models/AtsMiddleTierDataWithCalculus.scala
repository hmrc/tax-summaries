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

package models

import play.api.libs.json.{Format, Json}

case class AtsMiddleTierDataWithCalculus(
  taxYear: Int,
  utr: Option[String],
  odsValues: OdsValues
)

object AtsMiddleTierDataWithCalculus {
  implicit val formats: Format[AtsMiddleTierDataWithCalculus] = Json.format[AtsMiddleTierDataWithCalculus]
}

case class OdsValues(
  income_tax: Option[List[DataHolderWithCalculus]],
  summary_data: Option[List[DataHolderWithCalculus]],
  income_data: Option[List[DataHolderWithCalculus]],
  allowance_data: Option[List[DataHolderWithCalculus]],
  capital_gains_data: Option[List[DataHolderWithCalculus]]
)

object OdsValues {
  implicit val formats: Format[OdsValues] = Json.format[OdsValues]
}

case class DataHolderWithCalculus(
  fieldName: Option[String],
  amount: Option[BigDecimal],
  calculus: Option[String]
)

object DataHolderWithCalculus {
  implicit val formats: Format[DataHolderWithCalculus] = Json.format[DataHolderWithCalculus]
}
