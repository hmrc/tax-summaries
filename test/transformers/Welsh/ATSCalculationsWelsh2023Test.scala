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

package transformers.Welsh

import models.{Amount, TaxSummaryLiability, Welsh}
import play.api.libs.json.Json
import services.TaxRateService
import utils.{BaseSpec, JsonUtil}

class ATSCalculationsWelsh2023Test extends BaseSpec {
  val taxYear = 2023
  def getRate(key: String): Double = {
    val percentage: Double = applicationConfig.ratePercentages(taxYear).getOrElse(key, 0)
    percentage / 100.0
  }

  val json: String                             = JsonUtil.load("/utr_random_values.json", Map("<taxYear>" -> taxYear.toString))
  val taxSummaryLiability: TaxSummaryLiability = Json
    .parse(json)
    .as[TaxSummaryLiability]
    .copy(incomeTaxStatus = Some(Welsh()))

  val taxRate = new TaxRateService(taxYear, applicationConfig.ratePercentages)

  class FakeATSCalculationWelsh2023(taxSummaryLiability: TaxSummaryLiability)
      extends ATSCalculationsWelsh2023(taxSummaryLiability, taxRate)

  def sut(taxSummaryLiability: TaxSummaryLiability = taxSummaryLiability): FakeATSCalculationWelsh2023 =
    new FakeATSCalculationWelsh2023(taxSummaryLiability)

  "Welsh 2023" must {
    "return empty" when {
      "scottishIncomeTax is called" in {
        sut().scottishIncomeTax mustBe Amount.empty("scottishIncomeTaxWelsh2023")
      }

      "savingsRate is called" in {
        sut().savingsRate mustBe Amount(87.43, "GBP", Some("11.10(ctnSavingsChgbleStartRate) + 76.33(ctnTaxableCegSr)"))
      }

      "savingsRateAmount is called" in {
        sut().savingsRateAmount mustBe Amount(
          80.53,
          "GBP",
          Some("3.20(ctnSavingsTaxStartingRate) + 77.33(ctnTaxOnCegSr)")
        )
      }
    }

    "return welshIncomeTax" in {
      sut().welshIncomeTax mustBe Amount(
        212.444,
        "GBP",
        Some(
          "0.1 * (1860.00(ctnIncomeChgbleBasicRate) + 78.33(ctnTaxableRedundancyBr) + 5.23(ctnIncomeChgbleHigherRate) + 93.33(ctnTaxableRedundancyHr) + 3.22(ctnIncomeChgbleAddHRate) + 84.33(ctnTaxableRedundancyAhr))"
        )
      )
    }
  }
}
