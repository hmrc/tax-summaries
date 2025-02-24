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

package paye.models

import org.mockito.Mockito.reset
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.*

class SavingsStarterBandSpec extends AnyWordSpec with Matchers with MockitoSugar with BeforeAndAfterEach {

  override def beforeEach(): Unit =
    reset()

  "SavingsStarterBand" must {
    "deserialize correctly from valid JSON" in {
      val jsonString =
        """
          |{
          |  "savingsStarterRateTaxAmount": 100.0,
          |  "savingsStarterRateTax": 50.0,
          |  "savingsStarterRate": 20.0
          |}
        """.stripMargin

      val json   = Json.parse(jsonString)
      val result = json.validate[SavingsStarterBand]

      result match {
        case JsSuccess(band, _) =>
          band.savingsStarterRateTaxAmount shouldEqual 100.0
          band.savingsStarterRateTax       shouldEqual 50.0
          band.savingsStarterRate          shouldEqual 20.0
        case JsError(errors)    =>
          fail(s"Deserialization failed with errors: $errors")
      }
    }

    "fail to deserialize from invalid JSON" in {
      val jsonString =
        """
          |{
          |  "savingsStarterRateTaxAmount": "invalid",
          |  "savingsStarterRateTax": 50.0,
          |  "savingsStarterRate": 20.0
          |}
        """.stripMargin

      val json   = Json.parse(jsonString)
      val result = json.validate[SavingsStarterBand]

      result shouldBe a[JsError]
    }
  }
}
