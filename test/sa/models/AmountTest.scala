/*
 * Copyright 2023 HM Revenue & Customs
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

import common.models.Amount
import common.utils.BaseSpec
import play.api.libs.json.Json

class AmountTest extends BaseSpec {
  "Testing Amount" must {

    "not change constructor parameter values" in {
      val testValue: Double    = 1.0
      val testCurrency: String = "GBP"
      val testAmount: Amount   = new Amount(1.0, "GBP")
      testValue mustEqual testAmount.amount
      testCurrency mustEqual testAmount.currency
    }

    "calculate this : £1.00 + £2.00 = £3.00" in {
      Amount(3.0, "GBP", Some("a + b")) mustEqual Amount(1.0, "GBP", Some("a")) + Amount(2.0, "GBP", Some("b"))
    }

    "calculate this : £3.00 - £1.00 = £2.00" in {
      Amount(2.0, "GBP", Some("a - b")) mustEqual Amount(3.0, "GBP", Some("a")) - Amount(1.0, "GBP", Some("b"))
    }

    "properly compare £1.00 < £2.00" in {
      Amount(1.0, "GBP") must be < Amount(2.0, "GBP")
    }

    "properly compare £2.00 < £1.00 as false" in {
      Amount(2.0, "GBP") mustNot be < Amount(1.0, "GBP")
    }

    "properly compare £1.00 <= £1.00" in {
      Amount(1.0, "GBP") must be <= Amount(1.0, "GBP")
    }

    "properly divide amounts, (with 4 digit precision 1/3 = 0.3333)" in {
      Amount(1.0, "GBP").divideWithPrecision(Amount(3.0, "GBP"), 4) must be(Amount(0.3333, "GBP"))
    }

    "throw IllegalArgumentException when currency is not GBP, e.g. EUR" in {
      an[IllegalArgumentException] must be thrownBy {
        Amount.apply(1.0, "EUR")
      }
    }

    "throw IllegalArgumentException when summing different currencies (£1 + €1)" in {
      an[IllegalArgumentException] must be thrownBy {
        Amount(1.0, "GBP") + Amount(1.0, "EUR")
      }
    }

    "throw IllegalArgumentException when subtracting different currencies (£1 - €1)" in {
      an[IllegalArgumentException] must be thrownBy {
        Amount(1.0, "GBP") - Amount(1.0, "EUR")
      }
    }

    "throw IllegalArgumentException when comparing different currencies (£1 < €1)" in {
      an[IllegalArgumentException] must be thrownBy {
        Amount(1.0, "GBP") < Amount(1.0, "EUR")
      }
    }

    "transform to JSON where there is no calculus" in {
      val amountText     = """{"amount":1.0,"currency":"GBP"}"""
      val jsonFromText   = Json.parse(amountText)
      val amountObject   = Amount(1.0, "GBP")
      val jsonFromObject = Json.toJson(amountObject)
      jsonFromText mustEqual jsonFromObject
    }

    "transform to JSON where there is a calculus" in {
      val amountText     = """{"amount":1.0,"currency":"GBP","calculus":"calculus"}"""
      val jsonFromText   = Json.parse(amountText)
      val amountObject   = Amount(1.0, "GBP", Some("calculus"))
      val jsonFromObject = Json.toJson(amountObject)
      jsonFromText mustEqual jsonFromObject
    }

    "return false if isZeroOrLess is called on a positive amount" in {
      val positiveAmount = Amount(1.0, "GBP")
      positiveAmount.isZeroOrLess mustBe false
    }

    "return true if isZeroOrLess is called on a negative amount" in {
      val negativeAmount = Amount(-1.0, "GBP")
      negativeAmount.isZeroOrLess mustBe true
    }
  }
}
