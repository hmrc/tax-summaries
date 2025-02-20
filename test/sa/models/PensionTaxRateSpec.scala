/*
 * Copyright 2025 HM Revenue & Customs
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

package sa.models

import org.mockito.Mockito.reset
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.{JsError, JsNumber, JsString, JsSuccess}

class PensionTaxRateSpec extends AnyWordSpec with Matchers with MockitoSugar with BeforeAndAfterEach {

  override def beforeEach(): Unit =
    reset()

  "PensionTaxRate" must {
    "correctly calculate the percentage value" in {
      val rate = PensionTaxRate(0.2)
      rate.percentage shouldBe 20.0
    }

    "deserialize from valid JSON" in {
      val json = JsNumber(0.25)
      PensionTaxRate.reads.reads(json) shouldBe JsSuccess(PensionTaxRate(0.25))
    }

    "fail to deserialize from invalid JSON" in {
      val json = JsString("invalid")
      PensionTaxRate.reads.reads(json) shouldBe a[JsError]
    }
  }
}
