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

import models.ApiValue.mapReads
import play.api.libs.json._

abstract class ApiValue(val apiValue: String)

object ApiValue extends DefaultReads {

  def formatMap[A <: ApiValue, V: Format](ls: List[A]): Format[Map[A, V]] = Format(
    mapReads(s => ls.find(_.apiValue == s).fold[JsResult[A]](JsError(""))(JsSuccess(_))),
    Writes[Map[A, V]] { o =>
      JsObject(o.map {
        case (k, v) =>
          k.apiValue -> Json.toJson(v)
      })
    }
  )

  def readFromList[A <: ApiValue](ls: List[A]): Reads[A] = Reads[A] {
    case JsString(s) =>
      ls.find(l => l.apiValue == s)
        .fold[JsResult[A]](JsError("error.expected.apivalue"))(JsSuccess(_))

    case _ => JsError("error.expected.jsstring")
  }
}
