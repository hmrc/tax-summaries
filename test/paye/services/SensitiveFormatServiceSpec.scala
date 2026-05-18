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

import common.config.{ApplicationConfig, FakeEncrypterDecrypter}
import common.utils.BaseSpec
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import paye.services.SensitiveFormatService.SensitiveJsObject
import play.api.libs.json.{JsObject, JsString, JsValue, Json}

class SensitiveFormatServiceSpec extends BaseSpec with BeforeAndAfterEach {

  private val crypto = new FakeEncrypterDecrypter()
  private val config = mock[ApplicationConfig]

  private val service = new SensitiveFormatService(crypto, config)

  private val json: JsObject = Json.obj("nino" -> "MW609922A")
  private val sensitiveJson  = SensitiveJsObject(json)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(config)
  }

  "SensitiveFormatService" must {
    "write encrypted JsString when encryption is enabled" in {
      when(config.mongoEncryptionEnabled).thenReturn(true)

      val result: JsValue =
        Json.toJson(sensitiveJson)(service.sensitiveFormatJsObject)
      result mustBe JsString(Json.stringify(json))
    }

    "read encrypted JsString by decrypting it" in {
      val result =
        JsString(Json.stringify(json))
          .as[SensitiveJsObject](service.sensitiveFormatJsObject)

      result mustBe sensitiveJson
    }
  }
}
