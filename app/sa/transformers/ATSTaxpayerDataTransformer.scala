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

package sa.transformers

import play.api.Logger
import play.api.libs.json.{JsError, JsSuccess, JsValue, Reads}
import sa.models.AtsMiddleTierTaxpayerData

case class ATSTaxpayerDataTransformer(rawJsonFromStub: JsValue) {

  private val logger = Logger(getClass.getName)

  def atsTaxpayerDataDTO: AtsMiddleTierTaxpayerData = createATSDataDTO

  private def createATSDataDTO =
    AtsMiddleTierTaxpayerData(createTaxpayerDetailsBreakdown, None)

  private def createTaxpayerDetailsBreakdown: Option[Map[String, String]] = {
    val mapOfItems = Seq(
      getTaxpayerNameData("title").toSeq.map(v => "title" -> v),
      getTaxpayerNameData("forename").toSeq.map(v => "forename" -> v),
      getTaxpayerNameData("surname").toSeq.map(v => "surname" -> v)
    ).flatten.toMap

    if (mapOfItems.isEmpty) {
      None
    } else {
      Some(mapOfItems)
    }
  }

  private def getTaxpayerNameData(key: String): Option[String] =
    jsonValLookupWithErrorHandling[String](key, "name")

  private def jsonValLookupWithErrorHandling[T: Reads](key: String, topLevelContainer: String): Option[T] = {
    val theOption = (rawJsonFromStub \ topLevelContainer \ key).validate[T]

    theOption match {
      case s: JsSuccess[T] => s.asOpt
      case e: JsError      =>
        println("\nHERE:" + e)
        logger.error(
          "Errors: " + JsError.toJson(e).toString() + " we were looking for " + key + " in " + topLevelContainer
        )
        None
    }
  }
}
