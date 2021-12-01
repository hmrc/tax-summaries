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

package transformers.Scottish

import models.Liability.{IncomeChargeableAddHRate, IncomeChargeableBasicRate, IncomeChargeableHigherRate, IncomeTaxAddHighRate, IncomeTaxBasicRate, IncomeTaxHigherRate, SavingsChargeableAddHRate, SavingsChargeableHigherRate, SavingsChargeableLowerRate, SavingsTaxAddHighRate, SavingsTaxHigherRate, SavingsTaxLowerRate, TaxOnPayScottishIntermediateRate, TaxOnPayScottishStarterRate, TaxablePayScottishIntermediateRate, TaxablePayScottishStarterRate}
import models.{Amount, TaxSummaryLiability}
import play.api.libs.json.Json
import services.TaxRateService
import utils.{BaseSpec, JsonUtil}

class ATSCalculationsScottish2021Test extends BaseSpec {

  val taxYear = 2021

  val json: String = JsonUtil.load("/utr_scottish_2021.json")
  val taxSummaryLiability: TaxSummaryLiability = Json.parse(json).as[TaxSummaryLiability]

  val taxRate = new TaxRateService(taxYear, applicationConfig.ratePercentages)

  class FakeATSCalculationScottish2021 extends ATSCalculationsScottish2021(taxSummaryLiability, taxRate)

  val sut = new FakeATSCalculationScottish2021

  "Scottish 2021" must {
    "return empty" when {
      "scottishIncomeTax is called" in {
        sut.scottishIncomeTax mustBe Amount.empty
      }

      "savingsRate is called" in {
        sut.savingsRate mustBe Amount.empty
      }

      "savingsRateAmount is called" in {
        sut.savingsRateAmount mustBe Amount.empty
      }

      "basicRateIncomeTaxAmount is called" in {
        sut.basicRateIncomeTaxAmount mustBe Amount.empty
      }

      "higherRateIncomeTaxAmount is called" in {
        sut.higherRateIncomeTaxAmount mustBe Amount.empty
      }

      "additionalRateIncomeTaxAmount is called" in {
        sut.additionalRateIncomeTaxAmount mustBe Amount.empty
      }

      "basicRateIncomeTax is called" in {
        sut.basicRateIncomeTax mustBe Amount.empty
      }

      "higherRateIncomeTax is called" in {
        sut.higherRateIncomeTax mustBe Amount.empty
      }

      "additionalRateIncomeTax is called" in {
        sut.additionalRateIncomeTax mustBe Amount.empty
      }
    }

    "return scottishStarterRateTax" in {

    }
  }

}
