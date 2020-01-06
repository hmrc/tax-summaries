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

package utils

import java.net.URL
import scala.io.Source
import play.api.libs.json.{JsObject, Json}

trait AtsJsonDataUpdate {

  def transformation(sourceJson: URL, tliSlpAtsUpdate: JsObject): JsObject = {
    val sampleJson = Source.fromURL(sourceJson).mkString
    val parsedJson = Json.parse(sampleJson)
    val tlData = parsedJson.as[JsObject] \ "tliSlpAtsData"
    val updatedtliSlp = tlData.as[JsObject] ++ tliSlpAtsUpdate
    val theUpdatedJson = JsObject(Seq("tliSlpAtsData" -> updatedtliSlp))
    parsedJson.as[JsObject] ++ theUpdatedJson
  }

  def transformTaxpayerData(sourceJson: URL, jsonUpdateObject: JsObject): JsObject = {
    val sampleJson = Source.fromURL(sourceJson).mkString
    val parsedJson = Json.parse(sampleJson)
    val tlData = parsedJson.as[JsObject] \ "name"
    val updatedNameField = tlData.as[JsObject] ++ jsonUpdateObject
    val theUpdatedJson = JsObject(Seq("name" -> updatedNameField))
    parsedJson.as[JsObject] ++ theUpdatedJson
  }
}
