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

package common.config

import common.utils.BaseSpec
import uk.gov.hmrc.crypto.{Crypted, PlainText}

class FakeEncrypterDecrypterSpec extends BaseSpec {

  private val crypto = new FakeEncrypterDecrypter()

  "FakeEncrypterDecrypter" must {

    "encrypt plain text into a Crypted value" in {
      val text = PlainText("hello")

      val result = crypto.encrypt(text)

      result mustBe Crypted("hello")
    }

    "decrypt crypted text into a PlainText value" in {
      val encrypted = Crypted("hello")

      val result = crypto.decrypt(encrypted)

      result mustBe PlainText("hello")
    }
  }
}
