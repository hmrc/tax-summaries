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

package paye.services

import com.google.inject.Inject
import common.config.ApplicationConfig
import play.api.libs.json.*
import uk.gov.hmrc.crypto.{Crypted, Decrypter, Encrypter, PlainText, Sensitive}

import scala.util.{Failure, Success, Try}

class SensitiveFormatService @Inject() (encrypterDecrypter: Encrypter with Decrypter, config: ApplicationConfig) {
  import SensitiveFormatService.*

  private def writeJsObjectWithEncryption(jsObject: JsObject): JsValue =
    if (config.mongoEncryptionEnabled) {
      JsString(encrypterDecrypter.encrypt(PlainText(Json.stringify(jsObject))).value)
    } else {
      jsObject
    }

  private def sensitiveReadsJsObject: Reads[SensitiveJsObject] = {
    case JsString(s)        =>
      Try(encrypterDecrypter.decrypt(Crypted(s))) match {
        case Success(plainText) =>
          JsSuccess(SensitiveJsObject(Json.parse(plainText.value).as[JsObject]))

        /*
            Both of the below cases cater for two scenarios where the value is not encrypted:-
              either an unencrypted JsString or any other JsValue.
            This is to avoid breaking users' session in case data written before encryption introduced.
         */

        case Failure(_: SecurityException) => JsSuccess(SensitiveJsObject(JsString(s).as[JsObject]))
        case Failure(exception)            => throw exception
      }
    case jsObject: JsObject => JsSuccess(SensitiveJsObject(jsObject))
    case other              => JsError(s"Unexpected JsValue: $other")
  }

  def sensitiveFormatJsObject: Format[SensitiveJsObject] =
    Format(
      sensitiveReadsJsObject,
      sensitiveJsObject => writeJsObjectWithEncryption(sensitiveJsObject.decryptedValue)
    )
}

object SensitiveFormatService {
  case class SensitiveJsObject(override val decryptedValue: JsObject) extends Sensitive[JsObject]
}
