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
    try if (hasTaxpayerName) {
      AtsMiddleTierTaxpayerData(createTaxpayerDetailsBreakdown, None)
    } else {
      throw ATSParsingException("NoAtsTaxpayerDataError")
    } catch {
      case ATSParsingException(message) => throw ATSParsingException(message)
      case otherError: Throwable        =>
        logger.error("Unexpected error has occurred", otherError)
        throw ATSParsingException("Other Error")
    }

  private def hasTaxpayerName = createTaxpayerDetailsBreakdown.nonEmpty

  private def createTaxpayerDetailsBreakdown =
    Option(
      Map(
        "title"    -> getTaxpayerNameData("title"),
        "forename" -> getTaxpayerNameData("forename"),
        "surname"  -> getTaxpayerNameData("surname")
      )
    )

  private def getTaxpayerNameData(key: String): String =
    jsonValLookupWithErrorHandling[String](key, "name")

  private def jsonValLookupWithErrorHandling[T: Reads](key: String, topLevelContainer: String): T = {
    val theOption = (rawJsonFromStub \ topLevelContainer \ key).validate[T]

    theOption match {
      case s: JsSuccess[T] => s.get
      case e: JsError      =>
        logger.error(
          "Errors: " + JsError.toJson(e).toString() + " we were looking for " + key + " in " + topLevelContainer
        )
        throw ATSParsingException(key)
    }
  }
}
