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

class AmountSpec extends AnyWordSpec with Matchers with MockitoSugar with BeforeAndAfterEach {

  override def beforeEach(): Unit =
    reset()

  "Amount" must {
    "correctly identify zero values" in {
      Amount(0, "GBP").isZero  shouldBe true
      Amount(10, "GBP").isZero shouldBe false
    }

    "correctly identify zero or less values" in {
      Amount(-5, "GBP").isZeroOrLess shouldBe true
      Amount(5, "GBP").isZeroOrLess  shouldBe false
    }

    "perform addition correctly" in {
      val a1 = Amount(10, "GBP")
      val a2 = Amount(5, "GBP")
      (a1 + a2).amount shouldBe 15
    }

    "perform subtraction correctly" in {
      val a1 = Amount(10, "GBP")
      val a2 = Amount(5, "GBP")
      (a1 - a2).amount shouldBe 5
    }

    "perform multiplication correctly" in {
      val a = Amount(10, "GBP")
      (a * 2).amount shouldBe 20
    }

    "correctly round amounts up" in {
      val a = Amount(10.4, "GBP")
      a.roundAmountUp().amount shouldBe 11
    }

    "correctly round amounts with HALF_EVEN rounding mode" in {
      val a = Amount(10.555, "GBP")
      a.roundAmount().amount shouldBe 10.56
    }

    "divide amounts with precision correctly" in {
      val a1 = Amount(10, "GBP")
      val a2 = Amount(3, "GBP")
      a1.divideWithPrecision(a2, 2).amount shouldBe BigDecimal(3.33)
    }

    "serialize and deserialize correctly" in {
      val amount = Amount(15, "GBP", Some("test"))
      val json   = Json.toJson(amount)
      json.as[Amount] shouldBe amount
    }

    "log an error when both Amount calculus values are None" in {
      val a1 = Amount(10, "GBP", None)
      val a2 = Amount(5, "GBP", None)

      val result = a1 + a2

      result.calculus shouldBe None
    }
  }
}
