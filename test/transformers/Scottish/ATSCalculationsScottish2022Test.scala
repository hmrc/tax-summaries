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

package transformers.Scottish

import models.{Amount, PensionTaxRate, TaxSummaryLiability}
import play.api.libs.json.Json
import services.TaxRateService
import utils.{BaseSpec, JsonUtil}

class ATSCalculationsScottish2022Test extends BaseSpec {

  val taxYear = 2022
  def getRate(key: String): Double = {
    val percentage: Double = applicationConfig.ratePercentages(taxYear).getOrElse(key, 0)
    percentage / 100.0
  }

  val json: String                             = JsonUtil.load("/utr_random_values.json")
  val taxSummaryLiability: TaxSummaryLiability = Json
    .parse(json)
    .as[TaxSummaryLiability]
    .copy(incomeTaxStatus = Some("0002"))

  val taxRate = new TaxRateService(taxYear, applicationConfig.ratePercentages)

  class FakeATSCalculationScottish2022(taxSummaryLiability: TaxSummaryLiability)
      extends ATSCalculationsScottish2022(taxSummaryLiability, taxRate)

  def sut(taxSummaryLiability: TaxSummaryLiability = taxSummaryLiability): ATSCalculationsScottish2022 =
    new FakeATSCalculationScottish2022(taxSummaryLiability)

  "Scottish 2022" must {
    "return empty" when {
      "scottishIncomeTax is called" in {
        sut().scottishIncomeTax mustBe Amount.empty("scottishIncomeTaxScottish2022")
      }

      "savingsRate is called" in {
        sut().savingsRate mustBe Amount.empty("savingsRateScottish2022")
      }

      "savingsRateAmount is called" in {
        sut().savingsRateAmount mustBe Amount.empty("savingsRateAmountScottish2022")
      }

      "basicRateIncomeTaxAmount is called" in {
        sut().basicRateIncomeTaxAmount mustBe Amount.empty("basicRateIncomeTaxAmountScottish2022")
      }

      "higherRateIncomeTaxAmount is called" in {
        sut().higherRateIncomeTaxAmount mustBe Amount.empty("higherRateIncomeTaxAmountScottish2022")
      }

      "additionalRateIncomeTaxAmount is called" in {
        sut().additionalRateIncomeTaxAmount mustBe Amount.empty("additionalRateIncomeTaxAmountScottish2022")
      }

      "basicRateIncomeTax is called" in {
        sut().basicRateIncomeTax mustBe Amount.empty("basicRateIncomeTaxScottish2022")
      }

      "higherRateIncomeTax is called" in {
        sut().higherRateIncomeTax mustBe Amount.empty("higherRateIncomeTaxScottish2022")
      }

      "additionalRateIncomeTax is called" in {
        sut().additionalRateIncomeTax mustBe Amount.empty("additionalRateIncomeTaxScottish2022")
      }
    }

    "return scottishStarterRateTax" in {
      sut().scottishStarterRateTax mustBe Amount(
        113.55,
        "GBP",
        Some("24.22(taxOnPaySSR) + 89.33(ctnTaxOnRedundancySsr) + null (ctnPensionLsumTaxDueAmt)")
      )
    }

    "return scottishBasicRateTax" in {
      sut().scottishBasicRateTax mustBe Amount(
        574.73,
        "GBP",
        Some("372.00(ctnIncomeTaxBasicRate) + 79.33(ctnTaxOnRedundancyBr) + 123.40(ctnPensionLsumTaxDueAmt)")
      )
    }

    "return scottishBasicRateTax intermediate rate" in {
      val taxSummaryLiabilityIntermediateRate: TaxSummaryLiability =
        taxSummaryLiability.copy(pensionLumpSumTaxRate = PensionTaxRate(getRate("scottishIntermediateRate")))
      sut(taxSummaryLiabilityIntermediateRate).scottishBasicRateTax mustBe Amount(
        451.33,
        "GBP",
        Some("372.00(ctnIncomeTaxBasicRate) + 79.33(ctnTaxOnRedundancyBr) + null (ctnPensionLsumTaxDueAmt)")
      )
    }

    "return scottishIntermediateRateTax with basic rate" in {
      sut().scottishIntermediateRateTax mustBe Amount(
        145.55,
        "GBP",
        Some("54.22(taxOnPaySIR) + 91.33(ctnTaxOnRedundancySir) + null (ctnPensionLsumTaxDueAmt)")
      )
    }

    "return scottishIntermediateRateTax" in {
      val taxSummaryLiabilityIntermediateRate: TaxSummaryLiability =
        taxSummaryLiability.copy(pensionLumpSumTaxRate = PensionTaxRate(getRate("scottishIntermediateRate")))
      sut(taxSummaryLiabilityIntermediateRate).scottishIntermediateRateTax mustBe Amount(
        268.95,
        "GBP",
        Some("54.22(taxOnPaySIR) + 91.33(ctnTaxOnRedundancySir) + 123.40(ctnPensionLsumTaxDueAmt)")
      )
    }

    "return scottishHigherRateTax" in {
      val taxSummaryLiabilityHigherRate: TaxSummaryLiability =
        taxSummaryLiability.copy(pensionLumpSumTaxRate = PensionTaxRate(getRate("scottishHigherRate")))
      sut(taxSummaryLiabilityHigherRate).scottishHigherRateTax mustBe Amount(
        238.26,
        "GBP",
        Some("33.53(ctnIncomeTaxHigherRate) + 81.33(ctnTaxOnRedundancyHr) + 123.40(ctnPensionLsumTaxDueAmt)")
      )
    }

    "return scottishAdditionalRateTax" in {
      val taxSummaryLiabilityAdditionalRate: TaxSummaryLiability =
        taxSummaryLiability.copy(pensionLumpSumTaxRate = PensionTaxRate(getRate("scottishAdditionalRate")))
      sut(taxSummaryLiabilityAdditionalRate).scottishAdditionalRateTax mustBe Amount(
        275.95,
        "GBP",
        Some("66.22(ctnIncomeTaxAddHighRate) + 86.33(ctnTaxOnRedundancyAhr) + 123.40(ctnPensionLsumTaxDueAmt)")
      )
    }

    "does not return scottishAdditionalRateTax when used with basic rate" in {
      sut().scottishAdditionalRateTax mustBe Amount(
        152.55,
        "GBP",
        Some("66.22(ctnIncomeTaxAddHighRate) + 86.33(ctnTaxOnRedundancyAhr) + null (ctnPensionLsumTaxDueAmt)")
      )
    }

    "return scottishStarterRateIncome" in {
      val taxSummaryLiabilityStarterRate: TaxSummaryLiability =
        taxSummaryLiability.copy(pensionLumpSumTaxRate = PensionTaxRate(getRate("scottishStarterRate")))
      sut(taxSummaryLiabilityStarterRate).scottishStarterRateIncome mustBe Amount(
        122.58,
        "GBP",
        Some("21.11(taxablePaySSR) + 88.33(ctnTaxableRedundancySsr) + 13.14(itfStatePensionLsGrossAmt)")
      )
    }

    "does not return scottishStarterRateIncome when used with basic rate" in {
      sut().scottishStarterRateIncome mustBe Amount(
        109.44,
        "GBP",
        Some("21.11(taxablePaySSR) + 88.33(ctnTaxableRedundancySsr) + null (itfStatePensionLsGrossAmt)")
      )
    }

    "return scottishBasicRateIncome" in {
      sut().scottishBasicRateIncome mustBe Amount(
        1951.47,
        "GBP",
        Some("1860.00(ctnIncomeChgbleBasicRate) + 78.33(ctnTaxableRedundancyBr) + 13.14(itfStatePensionLsGrossAmt)")
      )
    }

    "return scottishIntermediateRateIncome" in {
      val taxSummaryLiabilityIntermediateRate: TaxSummaryLiability =
        taxSummaryLiability.copy(pensionLumpSumTaxRate = PensionTaxRate(getRate("scottishIntermediateRate")))
      sut(taxSummaryLiabilityIntermediateRate).scottishIntermediateRateIncome mustBe Amount(
        1337.68,
        "GBP",
        Some("1234.21(taxablePaySIR) + 90.33(ctnTaxableRedundancySir) + 13.14(itfStatePensionLsGrossAmt)")
      )
    }

    "return scottishHigherRateIncome" in {
      val taxSummaryLiabilityHigherRate: TaxSummaryLiability =
        taxSummaryLiability.copy(pensionLumpSumTaxRate = PensionTaxRate(getRate("scottishHigherRate")))
      sut(taxSummaryLiabilityHigherRate).scottishHigherRateIncome mustBe Amount(
        111.70,
        "GBP",
        Some("5.23(ctnIncomeChgbleHigherRate) + 93.33(ctnTaxableRedundancyHr) + 13.14(itfStatePensionLsGrossAmt)")
      )
    }

    "return scottishAdditionalRateIncome" in {
      val taxSummaryLiabilityAdditionalRate: TaxSummaryLiability =
        taxSummaryLiability.copy(pensionLumpSumTaxRate = PensionTaxRate(getRate("scottishAdditionalRate")))
      sut(taxSummaryLiabilityAdditionalRate).scottishAdditionalRateIncome mustBe Amount(
        100.69,
        "GBP",
        Some("3.22(ctnIncomeChgbleAddHRate) + 84.33(ctnTaxableRedundancyAhr) + 13.14(itfStatePensionLsGrossAmt)")
      )
    }

    "return savingsBasicRateTax" in {
      sut().savingsBasicRateTax mustBe Amount(2.30, "GBP", Some("2.30(ctnSavingsTaxLowerRate)"))
    }

    "return savingsHigherRateTax" in {
      val taxSummaryLiabilityHigherRate: TaxSummaryLiability =
        taxSummaryLiability.copy(pensionLumpSumTaxRate = PensionTaxRate(getRate("scottishHigherRate")))
      sut(taxSummaryLiabilityHigherRate).savingsHigherRateTax mustBe Amount(
        25.55,
        "GBP",
        Some("25.55(ctnSavingsTaxHigherRate)")
      )
    }

    "return savingsAdditionalRateTax" in {
      val taxSummaryLiabilityAdditionalRate: TaxSummaryLiability =
        taxSummaryLiability.copy(pensionLumpSumTaxRate = PensionTaxRate(getRate("scottishAdditionalRate")))
      sut(taxSummaryLiabilityAdditionalRate).savingsAdditionalRateTax mustBe Amount(
        89.64,
        "GBP",
        Some("2.31(ctnSavingsTaxAddHighRate) + 87.33(ctnTaxOnCegAhr)")
      )
    }

    "return savingsBasicRateIncome" in {
      sut().savingsBasicRateIncome mustBe Amount(5.05, "GBP", Some("5.05(ctnSavingsChgbleLowerRate)"))
    }

    "return savingsHigherRateIncome" in {
      val taxSummaryLiabilityHigherRate: TaxSummaryLiability =
        taxSummaryLiability.copy(pensionLumpSumTaxRate = PensionTaxRate(getRate("scottishHigherRate")))
      sut(taxSummaryLiabilityHigherRate).savingsHigherRateIncome mustBe Amount(
        2.35,
        "GBP",
        Some("2.35(ctnSavingsChgbleHigherRate)")
      )
    }

    "return savingsAdditionalRateIncome" in {
      val taxSummaryLiabilityAdditionalRate: TaxSummaryLiability =
        taxSummaryLiability.copy(pensionLumpSumTaxRate = PensionTaxRate(getRate("scottishAdditionalRate")))
      sut(taxSummaryLiabilityAdditionalRate).savingsAdditionalRateIncome mustBe Amount(
        55.52,
        "GBP",
        Some("55.52(ctnSavingsChgbleAddHRate)")
      )
    }

    "return totalIncomeTaxAmount" in {
      sut().totalIncomeTaxAmount mustBe Amount(
        1352.05,
        "GBP",
        Some(
          "10.10(taxExcluded) + 123.22(taxOnNonExcludedInc) + 24.22(taxOnPaySSR) + 89.33(ctnTaxOnRedundancySsr) + null (ctnPensionLsumTaxDueAmt) + 372.00(ctnIncomeTaxBasicRate) + 79.33(ctnTaxOnRedundancyBr) + 123.40(ctnPensionLsumTaxDueAmt) + 54.22(taxOnPaySIR) + 91.33(ctnTaxOnRedundancySir) + null (ctnPensionLsumTaxDueAmt) + 33.53(ctnIncomeTaxHigherRate) + 81.33(ctnTaxOnRedundancyHr) + null (ctnPensionLsumTaxDueAmt) + 66.22(ctnIncomeTaxAddHighRate) + 86.33(ctnTaxOnRedundancyAhr) + null (ctnPensionLsumTaxDueAmt) + 2.30(ctnSavingsTaxLowerRate) + 25.55(ctnSavingsTaxHigherRate) + 2.31(ctnSavingsTaxAddHighRate) + 87.33(ctnTaxOnCegAhr)"
        )
      )
    }

  }

}
