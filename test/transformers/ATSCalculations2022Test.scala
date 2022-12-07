/*
 * Copyright 2022 HM Revenue & Customs
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

package transformers

import models.Liability.TaxOnNonExcludedIncome
import models.{Amount, TaxSummaryLiability}
import play.api.libs.json.Json
import services.TaxRateService
import utils.{BaseSpec, JsonUtil}

class ATSCalculations2022Test extends BaseSpec {
  val taxYear                                  = 2022
  val json: String                             = JsonUtil.load("/utr_random_values.json")
  val taxSummaryLiability: TaxSummaryLiability = Json.parse(json).as[TaxSummaryLiability]

  val taxRate = new TaxRateService(taxYear, applicationConfig.ratePercentages)

  class FakeATSCalculation2022(val summaryData: TaxSummaryLiability, val taxRates: TaxRateService)
      extends ATSCalculations2022

  def sut(taxSummaryLiability: TaxSummaryLiability = taxSummaryLiability): FakeATSCalculation2022 =
    new FakeATSCalculation2022(taxSummaryLiability, taxRate)

  "Generic calculations 2022" must {
    "calculate totalAmountEmployeeNic" in {
      sut().totalAmountEmployeeNic mustBe Amount(8893.54, "GBP")
    }

    "calculate basicRateIncomeTax" in {
      sut().basicRateIncomeTax mustBe Amount(8176.78, "GBP")
    }

    "calculate basicRateIncomeTaxAmount" in {
      sut().basicRateIncomeTaxAmount mustBe Amount(11850.94, "GBP")
    }

    "calculate higherRateIncomeTax" in {
      sut().higherRateIncomeTax mustBe Amount(1460.91, "GBP")
    }

    "calculate higherRateIncomeTaxAmount" in {
      sut().higherRateIncomeTaxAmount mustBe Amount(39412.32, "GBP")
    }

    "calculate additionalRateIncomeTaxAmount" in {
      sut().additionalRateIncomeTaxAmount mustBe Amount(10621.32, "GBP")
    }

    "calculate additionalRateIncomeTax" in {
      sut().additionalRateIncomeTax mustBe Amount(12316.98, "GBP")
    }

    "calculate selfEmployment" in {
      sut().selfEmployment mustBe Amount(4379.77, "GBP")
    }

    "calculate otherIncome" in {
      sut().otherIncome mustBe Amount(17481.29, "GBP")
    }

    "calculate otherAllowances" in {
      sut().otherAllowances mustBe Amount(713, "GBP")
    }

    "calculate otherAdjustmentsIncreasing" in {
      sut().otherAdjustmentsIncreasing mustBe Amount(1004.42, "GBP")
    }

    "calculate totalIncomeTaxAmount" when {
      "totalIncomeTaxAmount lower than taxExcluded + taxOnNonExcludedInc" in {
        sut().totalIncomeTaxAmount mustBe Amount(133.32, "GBP")
      }

      "totalIncomeTaxAmount greater than taxExcluded + taxOnNonExcludedInc" in {
        val newAtsData = taxSummaryLiability.atsData - TaxOnNonExcludedIncome +
          (TaxOnNonExcludedIncome -> Amount(0.4, "GBP"))
        val newLiability = taxSummaryLiability.copy(atsData = newAtsData)
        sut(newLiability).totalIncomeTaxAmount mustBe Amount(10.50, "GBP")
      }
    }
  }
}
