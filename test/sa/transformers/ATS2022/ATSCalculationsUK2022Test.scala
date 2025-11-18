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

package sa.transformers.ATS2022

import common.models.{Amount, Rate}
import common.utils.{BaseSpec, JsonUtil}
import play.api.libs.json.Json
import sa.models.{TaxSummaryLiability, UK}
import sa.services.TaxRateService

class ATSCalculationsUK2022Test extends BaseSpec {

  val taxYear                      = 2022
  def getRate(key: String): Double = {
    val percentage: Rate = applicationConfig.rates(taxYear).getOrElse(key, Rate.empty)
    percentage.percent / 100.0
  }

  val json: String                             = JsonUtil.load("/sa/utr_random_values.json", Map("<taxYear>" -> taxYear.toString))
  val taxSummaryLiability: TaxSummaryLiability = Json
    .parse(json)
    .as[TaxSummaryLiability]
    .copy(incomeTaxStatus = Some(UK()))

  val taxRateService = new TaxRateService(applicationConfig.rates(taxYear))

  class FakeATSCalculationUK2022(taxSummaryLiability: TaxSummaryLiability)
      extends ATSCalculationsUK2022(taxSummaryLiability, taxRateService)

  def sut(taxSummaryLiability: TaxSummaryLiability = taxSummaryLiability): ATSCalculationsUK2022 =
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
