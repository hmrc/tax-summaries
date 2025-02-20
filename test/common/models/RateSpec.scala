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

package common.models

import org.mockito.Mockito.reset
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Json

class RateSpec extends AnyWordSpec with Matchers with MockitoSugar with BeforeAndAfterEach {

  override def beforeEach(): Unit =
    reset()

  "Rate" must {
    "correctly format apiValue" in {
      val rate = Rate(12.5)
      rate.apiValue.percent shouldBe "12.5%"
    }

    "serialize and deserialize correctly" in {
      val rate = Rate(15.0)
      val json = Json.toJson(rate)
      json.as[Rate] shouldBe rate
    }
  }

  "RateKey" must {
    "correctly store and retrieve apiValue" in {
      RateKey.Additional.apiValue               shouldBe "additional_rate_rate"
      RateKey.CapitalGainsEntrepreneur.apiValue shouldBe "cg_entrepreneurs_rate"
    }
  }

  "ApiRate" must {
    "serialize and deserialize correctly" in {
      val apiRate = ApiRate("10%")
      val json    = Json.toJson(apiRate)
      json.as[ApiRate] shouldBe apiRate
    }
  }
}
