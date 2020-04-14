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

package utils

import models.Amount
import org.scalatest.prop.PropertyChecks
import uk.gov.hmrc.play.test.UnitSpec

class CalculationHelperSpec extends UnitSpec with PropertyChecks {

  "CalculationHelper" when {

    "positiveOrZero is called" should {

      def createAmount(dec: BigDecimal) = Amount(dec, "gdp")

      "return the given amount object if the amount is positive" in {

        forAll { dec: BigDecimal =>
          whenever(dec > 0) {
            val amount = createAmount(dec)
            CalculationHelper.positiveOrZero(amount) shouldBe amount
          }
        }
      }

      "return the given amount object if the amount is zero" in {
        val amount = Amount(BigDecimal(0), "gdp")

        CalculationHelper.positiveOrZero(amount) shouldBe amount
      }

      "return an amount object with zero as the amount if the given amount is negative" in {
        forAll { dec: BigDecimal =>
          whenever(dec < 0) {
            CalculationHelper.positiveOrZero(createAmount(dec)) shouldBe
              createAmount(BigDecimal(0))
          }
        }
      }
    }
  }
}
