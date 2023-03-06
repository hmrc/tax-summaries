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

import models.{Amount, PensionTaxRate, Scottish, TaxSummaryLiability}
import play.api.libs.json.Json
import services.TaxRateService
import utils.{BaseSpec, JsonUtil}

class ATSCalculationsScottish2019Test extends BaseSpec {

  val taxYear = 2020
  def rate(key: String): Double = {
    val percentage: Double = applicationConfig.ratePercentages(taxYear).getOrElse(key, 0)
    percentage / 100.0
  }

  val json: String                             = JsonUtil.load("/utr_random_values.json", Map("<taxYear>" -> taxYear.toString))
  val taxSummaryLiability: TaxSummaryLiability = Json
    .parse(json)
    .as[TaxSummaryLiability]
    .copy(incomeTaxStatus = Some(Scottish()))

  val taxRate = new TaxRateService(taxYear, applicationConfig.ratePercentages)

  class FakeATSCalculationScottish2019(taxSummaryLiability: TaxSummaryLiability)
      extends ATSCalculationsScottish2019(taxSummaryLiability, taxRate)

  def sut(taxSummaryLiability: TaxSummaryLiability = taxSummaryLiability): ATSCalculationsScottish2019 =
    new FakeATSCalculationScottish2019(taxSummaryLiability)

  "Scottish 2019" must {
    "return empty" when {
      "scottishIncomeTax is called" in {
        sut().scottishIncomeTax mustBe Amount.empty("scottishIncomeTaxScottish1019")
      }

      "savingsRate is called" in {
        sut().savingsRate mustBe Amount.empty("savingsRateScottish2019")
      }

      "savingsRateAmount is called" in {
        sut().savingsRateAmount mustBe Amount.empty("savingsRateAmountScottish2019")
      }

      "basicRateIncomeTaxAmount is called" in {
        sut().basicRateIncomeTaxAmount mustBe Amount.empty("basicRateIncomeTaxAmountScottish2019")
      }

      "higherRateIncomeTaxAmount is called" in {
        sut().higherRateIncomeTaxAmount mustBe Amount.empty("higherRateIncomeTaxAmountScottish2019")
      }

      "additionalRateIncomeTaxAmount is called" in {
        sut().additionalRateIncomeTaxAmount mustBe Amount.empty("additionalRateIncomeTaxAmountScottish2019")
      }

      "basicRateIncomeTax is called" in {
        sut().basicRateIncomeTax mustBe Amount.empty("basicRateIncomeTaxScottish2019")
      }

      "higherRateIncomeTax is called" in {
        sut().higherRateIncomeTax mustBe Amount.empty("higherRateIncomeTaxScottish2019")
      }

      "additionalRateIncomeTax is called" in {
        sut().additionalRateIncomeTax mustBe Amount.empty("additionalRateIncomeTaxScottish2019")
      }
    }

    "return scottishStarterRateTax" in {
      sut().scottishStarterRateTax mustBe Amount(
        24.22,
        "GBP",
        Some("24.22(taxOnPaySSR) + null (ctnPensionLsumTaxDueAmt)")
      )
    }

    "return scottishBasicRateTax" in {
      sut().scottishBasicRateTax mustBe Amount(
        495.40,
        "GBP",
        Some("372.00(ctnIncomeTaxBasicRate) + 123.40(ctnPensionLsumTaxDueAmt)")
      )
    }

    "return scottishBasicRateTax intermediate rate" in {
      val taxSummaryLiabilityIntermediateRate: TaxSummaryLiability =
        taxSummaryLiability.copy(pensionLumpSumTaxRate = PensionTaxRate(rate("scottishIntermediateRate")))
      sut(taxSummaryLiabilityIntermediateRate).scottishBasicRateTax mustBe Amount(
        372.00,
        "GBP",
        Some("372.00(ctnIncomeTaxBasicRate) + null (ctnPensionLsumTaxDueAmt)")
      )
    }

    "return scottishIntermediateRateTax with basic rate" in {
      sut().scottishIntermediateRateTax mustBe Amount(
        54.22,
        "GBP",
        Some("54.22(taxOnPaySIR) + null (ctnPensionLsumTaxDueAmt)")
      )
    }

    "return scottishIntermediateRateTax" in {
      val taxSummaryLiabilityIntermediateRate: TaxSummaryLiability =
        taxSummaryLiability.copy(pensionLumpSumTaxRate = PensionTaxRate(rate("scottishIntermediateRate")))
      sut(taxSummaryLiabilityIntermediateRate).scottishIntermediateRateTax mustBe Amount(
        177.62,
        "GBP",
        Some("54.22(taxOnPaySIR) + 123.40(ctnPensionLsumTaxDueAmt)")
      )
    }

    "return scottishHigherRateTax" in {
      val taxSummaryLiabilityHigherRate: TaxSummaryLiability =
        taxSummaryLiability.copy(pensionLumpSumTaxRate = PensionTaxRate(rate("scottishHigherRate")))
      sut(taxSummaryLiabilityHigherRate).scottishHigherRateTax mustBe Amount(
        156.93,
        "GBP",
        Some("33.53(ctnIncomeTaxHigherRate) + 123.40(ctnPensionLsumTaxDueAmt)")
      )
    }

    "return scottishAdditionalRateTax" in {
      val taxSummaryLiabilityAdditionalRate: TaxSummaryLiability =
        taxSummaryLiability.copy(pensionLumpSumTaxRate = PensionTaxRate(rate("scottishAdditionalRate")))
      sut(taxSummaryLiabilityAdditionalRate).scottishAdditionalRateTax mustBe Amount(
        189.62,
        "GBP",
        Some("66.22(ctnIncomeTaxAddHighRate) + 123.40(ctnPensionLsumTaxDueAmt)")
      )
    }

    "does not return scottishAdditionalRateTax when used with basic rate" in {
      sut().scottishAdditionalRateTax mustBe Amount(
        66.22,
        "GBP",
        Some("66.22(ctnIncomeTaxAddHighRate) + null (ctnPensionLsumTaxDueAmt)")
      )
    }

    "return scottishStarterRateIncome" in {
      val taxSummaryLiabilityStarterRate: TaxSummaryLiability =
        taxSummaryLiability.copy(pensionLumpSumTaxRate = PensionTaxRate(rate("scottishStarterRate")))
      sut(taxSummaryLiabilityStarterRate).scottishStarterRateIncome mustBe Amount(
        34.25,
        "GBP",
        Some("21.11(taxablePaySSR) + 13.14(itfStatePensionLsGrossAmt)")
      )
    }

    "does not return scottishStarterRateIncome when used with basic rate" in {
      sut().scottishStarterRateIncome mustBe Amount(
        21.11,
        "GBP",
        Some("21.11(taxablePaySSR) + null (itfStatePensionLsGrossAmt)")
      )
    }

    "return scottishBasicRateIncome" in {
      sut().scottishBasicRateIncome mustBe Amount(
        1873.14,
        "GBP",
        Some("1860.00(ctnIncomeChgbleBasicRate) + 13.14(itfStatePensionLsGrossAmt)")
      )
    }

    "return scottishIntermediateRateIncome" in {
      val taxSummaryLiabilityIntermediateRate: TaxSummaryLiability =
        taxSummaryLiability.copy(pensionLumpSumTaxRate = PensionTaxRate(rate("scottishIntermediateRate")))
      sut(taxSummaryLiabilityIntermediateRate).scottishIntermediateRateIncome mustBe Amount(
        1247.35,
        "GBP",
        Some("1234.21(taxablePaySIR) + 13.14(itfStatePensionLsGrossAmt)")
      )
    }

    "return scottishHigherRateIncome" in {
      val taxSummaryLiabilityHigherRate: TaxSummaryLiability =
        taxSummaryLiability.copy(pensionLumpSumTaxRate = PensionTaxRate(rate("scottishHigherRate")))
      sut(taxSummaryLiabilityHigherRate).scottishHigherRateIncome mustBe Amount(
        18.37,
        "GBP",
        Some("5.23(ctnIncomeChgbleHigherRate) + 13.14(itfStatePensionLsGrossAmt)")
      )
    }

    "return scottishAdditionalRateIncome" in {
      val taxSummaryLiabilityAdditionalRate: TaxSummaryLiability =
        taxSummaryLiability.copy(pensionLumpSumTaxRate = PensionTaxRate(rate("scottishAdditionalRate")))
      sut(taxSummaryLiabilityAdditionalRate).scottishAdditionalRateIncome mustBe Amount(
        16.36,
        "GBP",
        Some("3.22(ctnIncomeChgbleAddHRate) + 13.14(itfStatePensionLsGrossAmt)")
      )
    }

    "return savingsBasicRateTax" in {
      sut().savingsBasicRateTax mustBe Amount(2.30, "GBP", Some("2.30(ctnSavingsTaxLowerRate)"))
    }

    "return savingsHigherRateTax" in {
      val taxSummaryLiabilityHigherRate: TaxSummaryLiability =
        taxSummaryLiability.copy(pensionLumpSumTaxRate = PensionTaxRate(rate("scottishHigherRate")))
      sut(taxSummaryLiabilityHigherRate).savingsHigherRateTax mustBe Amount(
        25.55,
        "GBP",
        Some("25.55(ctnSavingsTaxHigherRate)")
      )
    }

    "return savingsAdditionalRateTax" in {
      val taxSummaryLiabilityAdditionalRate: TaxSummaryLiability =
        taxSummaryLiability.copy(pensionLumpSumTaxRate = PensionTaxRate(rate("scottishAdditionalRate")))
      sut(taxSummaryLiabilityAdditionalRate).savingsAdditionalRateTax mustBe Amount(
        2.31,
        "GBP",
        Some("2.31(ctnSavingsTaxAddHighRate)")
      )
    }

    "return savingsBasicRateIncome" in {
      sut().savingsBasicRateIncome mustBe Amount(5.05, "GBP", Some("5.05(ctnSavingsChgbleLowerRate)"))
    }

    "return savingsHigherRateIncome" in {
      val taxSummaryLiabilityHigherRate: TaxSummaryLiability =
        taxSummaryLiability.copy(pensionLumpSumTaxRate = PensionTaxRate(rate("scottishHigherRate")))
      sut(taxSummaryLiabilityHigherRate).savingsHigherRateIncome mustBe Amount(
        2.35,
        "GBP",
        Some("2.35(ctnSavingsChgbleHigherRate)")
      )
    }

    "return savingsAdditionalRateIncome" in {
      val taxSummaryLiabilityAdditionalRate: TaxSummaryLiability =
        taxSummaryLiability.copy(pensionLumpSumTaxRate = PensionTaxRate(rate("scottishAdditionalRate")))
      sut(taxSummaryLiabilityAdditionalRate).savingsAdditionalRateIncome mustBe Amount(
        55.52,
        "GBP",
        Some("55.52(ctnSavingsChgbleAddHRate)")
      )
    }

    "return totalIncomeTaxAmount" in {
      sut().totalIncomeTaxAmount mustBe Amount(
        627.14,
        "GBP",
        Some(
          "null (savingsRateAmountScottish2019) + null (basicRateIncomeTaxAmountScottish2019) + null (higherRateIncomeTaxAmountScottish2019) + null (additionalRateIncomeTaxAmountScottish2019) + 6.27(ctnDividendTaxLowRate) + 6.67(ctnDividendTaxHighRate) + 4.66(ctnDividendTaxAddHighRate) + 66.66(nonDomChargeAmount) + 10.10(taxExcluded) + 0.35(incomeTaxDue) + 5.50(netAnnuityPaytsTaxDue) + 5.55(ctnChildBenefitChrgAmt) + 50.50(ctnPensionSavingChrgbleAmt) - 3.33(ctn4TaxDueAfterAllceRlf) - 10.30(ctnDeficiencyRelief) + 10.40(topSlicingRelief) + 10.50(ctnVctSharesReliefAmt) + 10.60(ctnEisReliefAmt) + 10.70(ctnSeedEisReliefAmt) + 10.80(ctnCommInvTrustRelAmt) + 10.19(atsSurplusMcaAlimonyRel) + 10.90(ctnNotionalTaxCegs) + 11.22(ctnNotlTaxOthrSrceAmo) + 11.23(ctnTaxCredForDivs) + 12.21(ctnQualDistnReliefAmt) + 14.44(figTotalTaxCreditRelief) + 80.30(ctnNonPayableTaxCredits) + 8.00(reliefForFinanceCosts) + 0(lfiRelief) + 0(alimony) - 7.75(ctnMarriageAllceInAmt) + 24.22(taxOnPaySSR) + null (ctnPensionLsumTaxDueAmt) + 372.00(ctnIncomeTaxBasicRate) + 123.40(ctnPensionLsumTaxDueAmt) + 54.22(taxOnPaySIR) + null (ctnPensionLsumTaxDueAmt) + 33.53(ctnIncomeTaxHigherRate) + null (ctnPensionLsumTaxDueAmt) + 66.22(ctnIncomeTaxAddHighRate) + null (ctnPensionLsumTaxDueAmt) + 2.30(ctnSavingsTaxLowerRate) + 25.55(ctnSavingsTaxHigherRate) + 2.31(ctnSavingsTaxAddHighRate)"
        )
      )
    }

  }

}
