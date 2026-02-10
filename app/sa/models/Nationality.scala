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

import play.api.libs.json.{JsResult, JsString, JsSuccess, JsValue, Reads, Writes}

sealed trait Nationality
case class Scottish() extends Nationality
case class Welsh() extends Nationality
case class UK() extends Nationality

object Nationality {
  implicit val reads: Reads[Nationality] = new Reads[Nationality] {
    override def reads(json: JsValue): JsResult[Nationality] =
      json.asOpt[String] match {
        case Some("0002") => JsSuccess(Scottish())
        case Some("0003") => JsSuccess(Welsh())
        case _            => JsSuccess(UK())
      }
  }

  implicit val writes: Writes[Nationality] = new Writes[Nationality] {
    override def writes(o: Nationality): JsValue =
      o match {
        case Scottish() => JsString("0002")
        case Welsh()    => JsString("0003")
        case UK()       => JsString("0001")
      }
  }

}
