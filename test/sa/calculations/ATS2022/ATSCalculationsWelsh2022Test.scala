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

package sa.calculations.ATS2022

import common.models.{Amount, Rate}
import common.utils.{BaseSpec, JsonUtil}
import play.api.libs.json.Json
import sa.models.{SelfAssessmentAPIResponse, Welsh}

class ATSCalculationsWelsh2022Test extends BaseSpec {
  val taxYear                      = 2022
  def getRate(key: String): Double = {
    val percentage: Rate = applicationConfig.taxRates(taxYear).getOrElse(key, Rate.empty)
    percentage.percent / 100.0
  }

  val json: String                                   = JsonUtil.load("/sa/utr_random_values.json", Map("<taxYear>" -> taxYear.toString))
  val taxSummaryLiability: SelfAssessmentAPIResponse = Json
    .parse(json)
    .as[SelfAssessmentAPIResponse]
    .copy(incomeTaxStatus = Some(Welsh()))

  val taxRates: Map[String, Rate] = applicationConfig.taxRates(taxYear)

  class FakeATSCalculationWelsh2022(taxSummaryLiability: SelfAssessmentAPIResponse)
      extends ATSCalculationsWelsh2022(taxSummaryLiability, taxRates)

  def sut(taxSummaryLiability: SelfAssessmentAPIResponse = taxSummaryLiability): FakeATSCalculationWelsh2022 =
    new FakeATSCalculationWelsh2022(taxSummaryLiability)

  "Welsh 2022" must {
    "return empty" when {
      "scottishIncomeTax is called" in {
        sut().scottishIncomeTax mustBe Amount.empty("scottishIncomeTaxWelsh2022")
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
