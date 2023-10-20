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

package transformers.ATS2020

import models.{Amount, TaxSummaryLiability, Welsh}
import play.api.libs.json.Json
import services.TaxRateService
import transformers.Welsh.ATSCalculationsWelsh2020
import utils.{BaseSpec, JsonUtil}

class ATSCalculationsWelsh2020Test extends BaseSpec {
  val taxYear = 2020
  def rate2021(key: String): Double = {
    val percentage: Double = applicationConfig.ratePercentages(taxYear).getOrElse(key, 0)
    percentage / 100.0
  }

  val json: String                             = JsonUtil.load("/utr_random_values.json", Map("<taxYear>" -> taxYear.toString))
  val taxSummaryLiability: TaxSummaryLiability = Json
    .parse(json)
    .as[TaxSummaryLiability]
    .copy(incomeTaxStatus = Some(Welsh()))

  val taxRate = new TaxRateService(taxYear, applicationConfig.ratePercentages)

  class FakeATSCalculationWelsh2020(taxSummaryLiability: TaxSummaryLiability)
      extends ATSCalculationsWelsh2020(taxSummaryLiability, taxRate)

  def sut(taxSummaryLiability: TaxSummaryLiability = taxSummaryLiability): FakeATSCalculationWelsh2020 =
    new FakeATSCalculationWelsh2020(taxSummaryLiability)

  "Welsh 2020" must {
    "return empty" when {
      "scottishIncomeTax is called" in {
        sut().scottishIncomeTax mustBe Amount.empty("scottishIncomeTaxWelsh2020")
      }

      "savingsRate is called" in {
        sut().savingsRate mustBe Amount.empty("savingsRateWelsh2020")
      }

      "savingsRateAmount is called" in {
        sut().savingsRateAmount mustBe Amount.empty("savingsRateAmountWelsh2020")
      }
    }

    "return welshIncomeTax" in {
      sut().welshIncomeTax mustBe Amount(
        186.845,
        "GBP",
        Some(
          "0.1 * (1860.00(ctnIncomeChgbleBasicRate) + 5.23(ctnIncomeChgbleHigherRate) + 3.22(ctnIncomeChgbleAddHRate))"
        )
      )
    }
  }
}
