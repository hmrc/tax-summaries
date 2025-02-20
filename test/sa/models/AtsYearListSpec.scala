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
import play.api.libs.json.{JsNumber, JsObject, JsString, Json}

class AtsYearListSpec extends AnyWordSpec with Matchers with MockitoSugar with BeforeAndAfterEach {

  override def beforeEach(): Unit =
    reset()

  "AtsYearList" must {
    "serialize to JSON correctly" in {
      val atsYearList =
        AtsYearList("1234567890", Some(Map("name" -> "John Doe")), Some(List(JsNumber(2021), JsNumber(2022))))
      val json        = Json.toJson(atsYearList)

      (json \ "utr").as[String]                 shouldBe "1234567890"
      (json \ "taxPayer").as[JsObject]          shouldBe JsObject(Map("name" -> JsString("John Doe")))
      (json \ "atsYearList").as[List[JsNumber]] shouldBe List(JsNumber(2021), JsNumber(2022))
    }

    "deserialize from JSON correctly" in {
      val json = Json.parse("""{
        "utr": "1234567890",
        "taxPayer": {"name": "John Doe"},
        "atsYearList": [2021, 2022]
      }""")

      val expected =
        AtsYearList("1234567890", Some(Map("name" -> "John Doe")), Some(List(JsNumber(2021), JsNumber(2022))))
      json.as[AtsYearList] shouldBe expected
    }

    "handle missing optional fields during deserialization" in {
      val json = Json.parse("""{
        "utr": "1234567890"
      }""")

      val expected = AtsYearList("1234567890", None, None)
      json.as[AtsYearList] shouldBe expected
    }
  }
}
