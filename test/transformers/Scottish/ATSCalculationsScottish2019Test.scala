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

import models.{Amount, PensionTaxRate, TaxSummaryLiability}
import play.api.libs.json.Json
import services.TaxRateService
import utils.{BaseSpec, JsonUtil}

class ATSCalculationsScottish2019Test extends BaseSpec {

  val taxYear = 2020
  def rate(key: String): Double = {
    val percentage: Double = applicationConfig.ratePercentages(taxYear).getOrElse(key, 0)
    percentage / 100.0
  }

  val json: String = JsonUtil.load("/utr_random_values.json")
  val taxSummaryLiability: TaxSummaryLiability = Json
    .parse(json)
    .as[TaxSummaryLiability]
    .copy(incomeTaxStatus = Some("0002"))

  val taxRate = new TaxRateService(taxYear, applicationConfig.ratePercentages)

  class FakeATSCalculationScottish2019(taxSummaryLiability: TaxSummaryLiability)
      extends ATSCalculationsScottish2019(taxSummaryLiability, taxRate)

  def sut(taxSummaryLiability: TaxSummaryLiability = taxSummaryLiability): ATSCalculationsScottish2019 =
    new FakeATSCalculationScottish2019(taxSummaryLiability)

  "Scottish 2021" must {
    "return empty" when {
      "scottishIncomeTax is called" in {
        sut().scottishIncomeTax mustBe Amount.empty
      }

      "savingsRate is called" in {
        sut().savingsRate mustBe Amount.empty
      }

      "savingsRateAmount is called" in {
        sut().savingsRateAmount mustBe Amount.empty
      }

      "basicRateIncomeTaxAmount is called" in {
        sut().basicRateIncomeTaxAmount mustBe Amount.empty
      }

      "higherRateIncomeTaxAmount is called" in {
        sut().higherRateIncomeTaxAmount mustBe Amount.empty
      }

      "additionalRateIncomeTaxAmount is called" in {
        sut().additionalRateIncomeTaxAmount mustBe Amount.empty
      }

      "basicRateIncomeTax is called" in {
        sut().basicRateIncomeTax mustBe Amount.empty
      }

      "higherRateIncomeTax is called" in {
        sut().higherRateIncomeTax mustBe Amount.empty
      }

      "additionalRateIncomeTax is called" in {
        sut().additionalRateIncomeTax mustBe Amount.empty
      }
    }

    "return scottishStarterRateTax" in {
      sut().scottishStarterRateTax mustBe Amount(24.22, "GBP")
    }

    "return scottishBasicRateTax" in {
      sut().scottishBasicRateTax mustBe Amount(495.40, "GBP")
    }

    "return scottishBasicRateTax intermediate rate" in {
      val taxSummaryLiabilityIntermediateRate: TaxSummaryLiability =
        taxSummaryLiability.copy(pensionLumpSumTaxRate = PensionTaxRate(rate("scottishIntermediateRate")))
      sut(taxSummaryLiabilityIntermediateRate).scottishBasicRateTax mustBe Amount(372.00, "GBP")
    }

    "return scottishIntermediateRateTax with basic rate" in {
      sut().scottishIntermediateRateTax mustBe Amount(54.22, "GBP")
    }

    "return scottishIntermediateRateTax" in {
      val taxSummaryLiabilityIntermediateRate: TaxSummaryLiability =
        taxSummaryLiability.copy(pensionLumpSumTaxRate = PensionTaxRate(rate("scottishIntermediateRate")))
      sut(taxSummaryLiabilityIntermediateRate).scottishIntermediateRateTax mustBe Amount(177.62, "GBP")
    }

    "return scottishHigherRateTax" in {
      val taxSummaryLiabilityHigherRate: TaxSummaryLiability =
        taxSummaryLiability.copy(pensionLumpSumTaxRate = PensionTaxRate(rate("scottishHigherRate")))
      sut(taxSummaryLiabilityHigherRate).scottishHigherRateTax mustBe Amount(156.93, "GBP")
    }

    "return scottishAdditionalRateTax" in {
      val taxSummaryLiabilityAdditionalRate: TaxSummaryLiability =
        taxSummaryLiability.copy(pensionLumpSumTaxRate = PensionTaxRate(rate("scottishAdditionalRate")))
      sut(taxSummaryLiabilityAdditionalRate).scottishAdditionalRateTax mustBe Amount(189.62, "GBP")
    }

    "does not return scottishAdditionalRateTax when used with basic rate" in {
      sut().scottishAdditionalRateTax mustBe Amount(66.22, "GBP")
    }

    "return scottishStarterRateIncome" in {
      val taxSummaryLiabilityStarterRate: TaxSummaryLiability =
        taxSummaryLiability.copy(pensionLumpSumTaxRate = PensionTaxRate(rate("scottishStarterRate")))
      sut(taxSummaryLiabilityStarterRate).scottishStarterRateIncome mustBe Amount(34.25, "GBP")
    }

    "does not return scottishStarterRateIncome when used with basic rate" in {
      sut().scottishStarterRateIncome mustBe Amount(21.11, "GBP")
    }

    "return scottishBasicRateIncome" in {
      sut().scottishBasicRateIncome mustBe Amount(1873.14, "GBP")
    }

    "return scottishIntermediateRateIncome" in {
      val taxSummaryLiabilityIntermediateRate: TaxSummaryLiability =
        taxSummaryLiability.copy(pensionLumpSumTaxRate = PensionTaxRate(rate("scottishIntermediateRate")))
      sut(taxSummaryLiabilityIntermediateRate).scottishIntermediateRateIncome mustBe Amount(1247.35, "GBP")
    }

    "return scottishHigherRateIncome" in {
      val taxSummaryLiabilityHigherRate: TaxSummaryLiability =
        taxSummaryLiability.copy(pensionLumpSumTaxRate = PensionTaxRate(rate("scottishHigherRate")))
      sut(taxSummaryLiabilityHigherRate).scottishHigherRateIncome mustBe Amount(18.37, "GBP")
    }

    "return scottishAdditionalRateIncome" in {
      val taxSummaryLiabilityAdditionalRate: TaxSummaryLiability =
        taxSummaryLiability.copy(pensionLumpSumTaxRate = PensionTaxRate(rate("scottishAdditionalRate")))
      sut(taxSummaryLiabilityAdditionalRate).scottishAdditionalRateIncome mustBe Amount(16.36, "GBP")
    }

    "return savingsBasicRateTax" in {
      sut().savingsBasicRateTax mustBe Amount(2.30, "GBP")
    }

    "return savingsHigherRateTax" in {
      val taxSummaryLiabilityHigherRate: TaxSummaryLiability =
        taxSummaryLiability.copy(pensionLumpSumTaxRate = PensionTaxRate(rate("scottishHigherRate")))
      sut(taxSummaryLiabilityHigherRate).savingsHigherRateTax mustBe Amount(25.55, "GBP")
    }

    "return savingsAdditionalRateTax" in {
      val taxSummaryLiabilityAdditionalRate: TaxSummaryLiability =
        taxSummaryLiability.copy(pensionLumpSumTaxRate = PensionTaxRate(rate("scottishAdditionalRate")))
      sut(taxSummaryLiabilityAdditionalRate).savingsAdditionalRateTax mustBe Amount(2.31, "GBP")
    }

    "return savingsBasicRateIncome" in {
      sut().savingsBasicRateIncome mustBe Amount(5.05, "GBP")
    }

    "return savingsHigherRateIncome" in {
      val taxSummaryLiabilityHigherRate: TaxSummaryLiability =
        taxSummaryLiability.copy(pensionLumpSumTaxRate = PensionTaxRate(rate("scottishHigherRate")))
      sut(taxSummaryLiabilityHigherRate).savingsHigherRateIncome mustBe Amount(2.35, "GBP")
    }

    "return savingsAdditionalRateIncome" in {
      val taxSummaryLiabilityAdditionalRate: TaxSummaryLiability =
        taxSummaryLiability.copy(pensionLumpSumTaxRate = PensionTaxRate(rate("scottishAdditionalRate")))
      sut(taxSummaryLiabilityAdditionalRate).savingsAdditionalRateIncome mustBe Amount(55.52, "GBP")
    }

    "return totalIncomeTaxAmount" in {
      sut().totalIncomeTaxAmount mustBe Amount(626.93, "GBP")
    }

  }

}
