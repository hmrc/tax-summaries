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

import play.api.libs.json.{Format, Json}

case class DataHolder(
  payload: Option[Map[LiabilityKey, Amount]],
  rates: Option[Map[RateKey, ApiRate]],
  incomeTaxStatus: Option[String])

object DataHolder {
  implicit val formats: Format[DataHolder] = Json.format[DataHolder]

  def make(payload: Map[LiabilityKey, Amount]): DataHolder =
    DataHolder(Some(payload), None, None)

  def make(payload: Map[LiabilityKey, Amount], rates: Map[RateKey, ApiRate]): DataHolder =
    DataHolder(Some(payload), Some(rates), None)

  def make(
    payload: Map[LiabilityKey, Amount],
    rates: Map[RateKey, ApiRate],
    incomeTaxStatus: Option[String]): DataHolder =
    DataHolder(Some(payload), Some(rates), incomeTaxStatus)
}
