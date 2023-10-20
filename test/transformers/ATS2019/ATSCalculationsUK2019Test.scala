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

package transformers.ATS2019

import models.{Amount, TaxSummaryLiability, UK}
import play.api.libs.json.Json
import services.TaxRateService
import transformers.UK.ATSCalculationsUK2019
import utils.{BaseSpec, JsonUtil}

class ATSCalculationsUK2019Test extends BaseSpec {

  val taxYear = 2019
  def rate(key: String): Double = {
    val percentage: Double = applicationConfig.ratePercentages(taxYear).getOrElse(key, 0)
    percentage / 100.0
  }

  val json: String                             = JsonUtil.load("/utr_random_values.json", Map("<taxYear>" -> taxYear.toString))
  val taxSummaryLiability: TaxSummaryLiability = Json
    .parse(json)
    .as[TaxSummaryLiability]
    .copy(incomeTaxStatus = Some(UK()))

  val taxRate = new TaxRateService(taxYear, applicationConfig.ratePercentages)

  class FakeATSCalculationUK2019(taxSummaryLiability: TaxSummaryLiability)
      extends ATSCalculationsUK2019(taxSummaryLiability, taxRate)

  def sut(taxSummaryLiability: TaxSummaryLiability = taxSummaryLiability): ATSCalculationsUK2019 =
    new FakeATSCalculationUK2019(taxSummaryLiability)

  "UK 2019" must {
    "return empty" when {
      "scottishIncomeTax is called" in {
        sut().scottishIncomeTax mustBe Amount.empty("scottishIncomeTaxUK2019")
      }

      "savingsRate is called" in {
        sut().savingsRate mustBe Amount.empty("savingsRateUK2019")
      }

      "savingsRateAmount is called" in {
        sut().savingsRateAmount mustBe Amount.empty("savingsRateAmountUK2019")
      }
    }
  }
}
