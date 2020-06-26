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

package transformers

import models.AtsMiddleTierTaxpayerData
import play.api.Logger
import play.api.libs.json.{JsError, JsSuccess, JsValue, Reads}

case class ATSTaxpayerDataTransformer(rawJsonFromStub: JsValue) {

  def atsTaxpayerDataDTO = createATSDataDTO

  private def createATSDataDTO =
    try {
      hasTaxpayerName match {
        case true  => AtsMiddleTierTaxpayerData(createTaxpayerDetailsBreakdown, None)
        case false => throw new ATSParsingException("NoAtsTaxpayerDataError")
      }
    } catch {
      case ATSParsingException(message) => throw new ATSParsingException(message)
      case otherError: Throwable =>
        Logger.error("Unexpected error has occurred", otherError)
        throw new ATSParsingException("Other Error")
    }

  private def hasTaxpayerName = createTaxpayerDetailsBreakdown.nonEmpty

  private def createTaxpayerDetailsBreakdown =
    Option(
      Map(
        "title"    -> getTaxpayerNameData("title"),
        "forename" -> getTaxpayerNameData("forename"),
        "surname"  -> getTaxpayerNameData("surname")))

  private def getTaxpayerNameData(key: String): String =
    jsonValLookupWithErrorHandling[String](key, "name")

  private def jsonValLookupWithErrorHandling[T: Reads](key: String, topLevelContainer: String): T = {
    val theOption = (rawJsonFromStub \ topLevelContainer \ key).validate[T]

    theOption match {
      case s: JsSuccess[T] => s.get
      case e: JsError =>
        Logger.error(
          "Errors: " + JsError.toJson(e).toString() + " we were looking for " + key + " in " + topLevelContainer)
        throw new ATSParsingException(key)
    }
  }
}
