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

package common.utils

import common.utils.TestConstants.testUtr
import play.api.libs.json.{JsObject, Json}

import scala.io.Source

object JsonUtil extends JsonUtil

trait JsonUtil {

  lazy val dummyDataMap: Map[String, String] = Map("$utr" -> testUtr)

  def load(path: String, replacements: Map[String, String] = Map.empty): String = {
    val content = bracket(Source.fromInputStream(getClass.getResourceAsStream(path)))(_.close())(_.mkString)
    replacements.foldLeft(content) { case (acc, (key, value)) =>
      acc.replace(key, value)
    }
  }

  def loadAndParseJsonWithDummyUTRData(path: String, replaceMap: Map[String, String]): String =
    bracket(Source.fromURL(getClass.getResource(path)))(_.close()) { json =>
      var jsonString = json.mkString
      for ((key, value) <- replaceMap)
        jsonString = jsonString.replace(key, value)
      jsonString
    }

  def loadAndReplace(path: String, replaceMap: JsObject): JsObject =
    bracket(Source.fromURL(getClass.getResource(path)))(_.close()) { json =>
      val parsedJson     = Json.parse(json.mkString)
      val tlData         = parsedJson.as[JsObject] \ "tliSlpAtsData"
      val updatedtliSlp  = tlData.as[JsObject] ++ replaceMap
      val theUpdatedJson = JsObject(Seq("tliSlpAtsData" -> updatedtliSlp))
      parsedJson.as[JsObject] ++ theUpdatedJson
    }

  def bracket[A, B](acquire: => A)(release: A => Unit)(use: A => B): B = {
    var a: Any = null

    try {
      a = acquire
      use(a.asInstanceOf[A])
    } finally release(a.asInstanceOf[A])
  }
}
