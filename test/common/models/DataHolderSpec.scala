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

package common.models

import org.mockito.Mockito.reset
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.*
import sa.models.{Nationality, UK}

class DataHolderSpec extends AnyWordSpec with Matchers with MockitoSugar with BeforeAndAfterEach {

  override def beforeEach(): Unit =
    reset()

  "DataHolder" must {
    "serialize and deserialize correctly" in {
      val dataHolder = DataHolder(Some(Map.empty), Some(Map.empty), Some(UK()))
      val json       = Json.toJson(dataHolder)
      json.as[DataHolder] shouldBe dataHolder
    }

    "create instances using make methods" in {
      val payload = Map.empty[LiabilityKey, Amount]
      val rates   = Map.empty[RateKey, ApiRate]

      val dh1 = DataHolder.make(payload)
      dh1.payload         shouldBe Some(payload)
      dh1.rates           shouldBe None
      dh1.incomeTaxStatus shouldBe None

      val dh2 = DataHolder.make(payload, rates)
      dh2.payload         shouldBe Some(payload)
      dh2.rates           shouldBe Some(rates)
      dh2.incomeTaxStatus shouldBe None

      val dh3 = DataHolder.make(payload, rates, Some(UK()))
      dh3.payload         shouldBe Some(payload)
      dh3.rates           shouldBe Some(rates)
      dh3.incomeTaxStatus shouldBe Some(UK())
    }
  }
}
