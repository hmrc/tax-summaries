/*
 * Copyright 2021 HM Revenue & Customs
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
import models.{Amount, Liability, TaxSummaryLiability}
import play.api.libs.json.Json
import services.TaxRateService
import utils.{BaseSpec, JsonUtil}

class ATSCalculations2021Test extends BaseSpec {
  val taxYear = 2021
  val json: String = JsonUtil.load("/utr_random_values.json")
  val taxSummaryLiability: TaxSummaryLiability = Json.parse(json).as[TaxSummaryLiability]

  val taxRate = new TaxRateService(taxYear, applicationConfig.ratePercentages)

  class FakeATSCalculation2021(val summaryData: TaxSummaryLiability, val taxRates: TaxRateService)
      extends ATSCalculations2021

  def sut(taxSummaryLiability: TaxSummaryLiability = taxSummaryLiability): FakeATSCalculation2021 =
    new FakeATSCalculation2021(taxSummaryLiability, taxRate)

  "Generic calculations 2021" must {
    "calculate selfEmployment" in {
      sut().selfEmployment mustBe Amount(4379.77, "GBP")
    }

    "calculate otherIncome" in {
      sut().otherIncome mustBe Amount(338.87, "GBP")
    }

    "calculate otherAllowances" in {
      sut().otherAllowances mustBe Amount(847, "GBP")
    }

    "calculate otherAdjustmentsIncreasing" in {
      sut().otherAdjustmentsIncreasing mustBe Amount(125.23, "GBP")
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
