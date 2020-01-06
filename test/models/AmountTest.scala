/*
 * Copyright 2020 HM Revenue & Customs
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

package models

import play.api.libs.json.Json
import uk.gov.hmrc.play.test.UnitSpec

class AmountTest extends UnitSpec {
  "Testing Amount" should {

    "not change constructor parameter values" in {
      val testValue: Double = 1.0
      val testCurrency: String = "GBP"
      val testAmount: Amount = new Amount(1.0, "GBP")
      testValue shouldEqual testAmount.amount
      testCurrency shouldEqual testAmount.currency
    }

    "calculate this : £1.00 + £2.00 = £3.00" in {
      Amount(3.0, "GBP") shouldEqual Amount(1.0, "GBP") + Amount(2.0, "GBP")
    }

    "calculate this : £3.00 - £1.00 = £2.00" in {
      Amount(2.0, "GBP") shouldEqual Amount(3.0, "GBP") - Amount(1.0, "GBP")
    }

    "properly compare £1.00 < £2.00" in {
      Amount(1.0, "GBP") should be < Amount(2.0, "GBP")
    }

    "properly compare £2.00 < £1.00 as false" in {
      Amount(2.0, "GBP") shouldNot be < Amount(1.0, "GBP")
    }

    "properly compare £1.00 <= £1.00" in {
      Amount(1.0, "GBP") should be <= Amount(1.0, "GBP")
    }

    "properly divide amounts, (with 4 digit precision 1/3 = 0.3333)" in {
      Amount(1.0, "GBP").divideWithPrecision(Amount(3.0, "GBP"), 4) should be(Amount(0.3333, "GBP"))
    }

    "throw IllegalArgumentException when summing different currencies (£1 + €1)" in {
      an[IllegalArgumentException] should be thrownBy {
        Amount(1.0, "GBP") + Amount(1.0, "EUR")
      }
    }

    "throw IllegalArgumentException when subtracting different currencies (£1 - €1)" in {
      an[IllegalArgumentException] should be thrownBy {
        Amount(1.0, "GBP") - Amount(1.0, "EUR")
      }
    }

    "throw IllegalArgumentException when comparing different currencies (£1 < €1)" in {
      an[IllegalArgumentException] should be thrownBy {
        Amount(1.0, "GBP") < Amount(1.0, "EUR")
      }
    }

    "transform to JSON" in {
      val amountText = """{"amount":1.0,"currency":"GBP"}"""
      val jsonFromText = Json.parse(amountText)
      val amountObject = Amount(1.0, "GBP")
      val jsonFromObject = Json.toJson(amountObject)
      jsonFromText shouldEqual jsonFromObject
    }

    "return false if isZeroOrLess is called on a positive amount" in {
      val positiveAmount = Amount(1.0, "GBP")
      positiveAmount.isZeroOrLess shouldBe false
    }

    "return true if isZeroOrLess is called on a negative amount" in {
      val negativeAmount = Amount(-1.0, "GBP")
      negativeAmount.isZeroOrLess shouldBe true
    }
  }
}
