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
import sa.models.{SelfAssessmentAPIResponse, UK}

class ATSCalculationsUK2022Test extends BaseSpec {

  val taxYear                      = 2022
  def getRate(key: String): Double = {
    val percentage: Rate = applicationConfig.taxRates(taxYear).getOrElse(key, Rate.empty)
    percentage.percent / 100.0
  }

  val json: String                                   = JsonUtil.load("/sa/utr_random_values.json", Map("<taxYear>" -> taxYear.toString))
  val taxSummaryLiability: SelfAssessmentAPIResponse = Json
    .parse(json)
    .as[SelfAssessmentAPIResponse]
    .copy(incomeTaxStatus = Some(UK()))

  val taxRates: Map[String, Rate] = applicationConfig.taxRates(taxYear)

  class FakeATSCalculationUK2022(taxSummaryLiability: SelfAssessmentAPIResponse)
      extends ATSCalculationsUK2022(taxSummaryLiability, taxRates)

  def sut(taxSummaryLiability: SelfAssessmentAPIResponse = taxSummaryLiability): ATSCalculationsUK2022 =
    new FakeATSCalculationUK2022(taxSummaryLiability)

  "UK 2022" must {
    "return empty" when {
      "scottishIncomeTax is called" in {
        sut().scottishIncomeTax mustBe Amount.empty("scottishIncomeTaxUK2022")
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
  }
}
