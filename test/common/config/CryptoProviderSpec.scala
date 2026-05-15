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
import play.api.Configuration
import uk.gov.hmrc.crypto.{Decrypter, Encrypter}

class CryptoProviderSpec extends BaseSpec {

  "CryptoProvider" must {
    "return FakeEncrypterDecrypter when mongo encryption is disabled" in {
      val configuration = Configuration(
        "mongo.encryption.enabled" -> false
      )

      val fakeEncrypterDecrypter = new FakeEncrypterDecrypter()
      val provider               = new CryptoProvider(configuration, fakeEncrypterDecrypter)

      provider.get() mustBe fakeEncrypterDecrypter
    }

    "return crypto when mongo encryption is enabled" in {
      val configuration = Configuration(
        "mongo.encryption.enabled" -> true,
        "mongo.encryption.key"     -> "12345678901234567890123456789012"
      )

      val fakeEncrypterDecrypter = new FakeEncrypterDecrypter()
      val provider               = new CryptoProvider(configuration, fakeEncrypterDecrypter)

      provider.get() mustBe a[Encrypter with Decrypter]
    }
  }
}
