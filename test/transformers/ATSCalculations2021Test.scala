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
import models.{Amount, AmountWithAudit, Liability, TaxSummaryLiability}
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
      sut().otherAllowances mustBe Amount(835, "GBP")
    }

    "calculate otherAdjustmentsIncreasing" in {
      sut().otherAdjustmentsIncreasing mustBe Amount(1004.42, "GBP")
    }

    "calculate totalIncomeTaxAmount" when {
      "totalIncomeTaxAmount lower than taxExcluded + taxOnNonExcludedInc" in {
        val expected = AmountWithAudit(
          Amount(133.32, "GBP"),
          Map(
            "rateDividendAdjustmentTax" ->
              """savingsRateAmount (3.20) +
                |basicRateIncomeTaxAmount (497.70) +
                |higherRateIncomeTaxAmount (59.08) +
                |additionalRateIncomeTaxAmount (68.53) +
                |get(DividendTaxLowRate) (6.27) +
                |get(DividendTaxHighRate) (6.67) +
                |get(DividendTaxAddHighRate) (4.66) +
                |otherAdjustmentsIncreasing (1004.42) -
                |otherAdjustmentsReducing (221.79) -
                |getWithDefaultAmount(MarriageAllceIn) (7.75)""".stripMargin,
            "excludedAndNonExcludedTax" ->
              s"""get(TaxExcluded) (10.10) +
                 |getWithDefaultAmount(TaxOnNonExcludedIncome) (123.22)""".stripMargin,
            "totalIncomeTaxAmount" -> """133.32 which is the minimum of
                                        |excludedAndNonExcludedTax (1420.99) and
                                        |excludedAndNonExcludedTax (133.32)""".stripMargin
          )
        )

        sut().totalIncomeTaxAmount mustBe expected
      }

      "totalIncomeTaxAmount greater than taxExcluded + taxOnNonExcludedInc" in {
        val newAtsData = taxSummaryLiability.atsData - TaxOnNonExcludedIncome +
          (TaxOnNonExcludedIncome -> Amount(0.4, "GBP"))
        val newLiability = taxSummaryLiability.copy(atsData = newAtsData)
        val expected = AmountWithAudit(
          Amount(10.50, "GBP"),
          Map(
            "rateDividendAdjustmentTax" ->
              """savingsRateAmount (3.20) +
                |basicRateIncomeTaxAmount (497.70) +
                |higherRateIncomeTaxAmount (59.08) +
                |additionalRateIncomeTaxAmount (68.53) +
                |get(DividendTaxLowRate) (6.27) +
                |get(DividendTaxHighRate) (6.67) +
                |get(DividendTaxAddHighRate) (4.66) +
                |otherAdjustmentsIncreasing (1004.42) -
                |otherAdjustmentsReducing (221.79) -
                |getWithDefaultAmount(MarriageAllceIn) (7.75)""".stripMargin,
            "excludedAndNonExcludedTax" ->
              s"""get(TaxExcluded) (10.10) +
                 |getWithDefaultAmount(TaxOnNonExcludedIncome) (0.4)""".stripMargin,
            "totalIncomeTaxAmount" ->
              s"""10.50 which is the minimum of
                 |excludedAndNonExcludedTax (1420.99) and
                 |excludedAndNonExcludedTax (10.50)""".stripMargin
          )
        )
        sut(newLiability).totalIncomeTaxAmount mustBe expected
      }
    }
  }
}
