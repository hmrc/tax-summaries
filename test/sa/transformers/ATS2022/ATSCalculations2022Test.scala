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

import common.models.Amount
import common.utils.{BaseSpec, JsonUtil}
import play.api.libs.json.Json
import sa.models.ODSLiabilities.ODSLiabilities.TaxOnNonExcludedIncome
import sa.models.TaxSummaryLiability
import sa.services.TaxRateService

class ATSCalculations2022Test extends BaseSpec {
  val taxYear                                  = 2022
  val json: String                             = JsonUtil.load("/sa/utr_random_values.json", Map("<taxYear>" -> taxYear.toString))
  val taxSummaryLiability: TaxSummaryLiability = Json.parse(json).as[TaxSummaryLiability]

  val taxRateService = new TaxRateService(taxYear, applicationConfig.ratePercentages)

  class FakeATSCalculation2022(val summaryData: TaxSummaryLiability, val taxRateService: TaxRateService)
      extends ATSCalculations2022 {
    override def scottishIncomeTax: Amount = Amount.empty("Dummy scottish income tax amount")
  }

  def sut(taxSummaryLiability: TaxSummaryLiability = taxSummaryLiability): FakeATSCalculation2022 =
    new FakeATSCalculation2022(taxSummaryLiability, taxRateService)

  "Generic calculations 2022" must {
    "calculate totalAmountEmployeeNic" in {
      sut().totalAmountEmployeeNic mustBe Amount(
        225.66,
        "GBP",
        Some("100.00(employeeClass1Nic) + 92.33(ctnClass2NicAmt) + 33.33(class4Nic)")
      )
    }

    "calculate basicRateIncomeTax" in {
      sut().basicRateIncomeTax mustBe Amount(
        2032.85,
        "GBP",
        Some(
          "1860.00(ctnIncomeChgbleBasicRate) + 5.05(ctnSavingsChgbleLowerRate) + 78.33(ctnTaxableRedundancyBr) + 76.33(ctnTaxableCegBr) + 13.14(itfStatePensionLsGrossAmt)"
        )
      )
    }

    "calculate basicRateIncomeTaxAmount" in {
      sut().basicRateIncomeTaxAmount mustBe Amount(
        657.36,
        "GBP",
        Some(
          "372.00(ctnIncomeTaxBasicRate) + 2.30(ctnSavingsTaxLowerRate) + 79.33(ctnTaxOnRedundancyBr) + 80.33(ctnTaxOnCegBr) + 123.40(ctnPensionLsumTaxDueAmt)"
        )
      )
    }

    "calculate higherRateIncomeTax" in {
      sut().higherRateIncomeTax mustBe Amount(
        183.24,
        "GBP",
        Some(
          "5.23(ctnIncomeChgbleHigherRate) + 2.35(ctnSavingsChgbleHigherRate) + 93.33(ctnTaxableRedundancyHr) + 82.33(ctnTaxableCegHr) + null (itfStatePensionLsGrossAmt)"
        )
      )
    }

    "calculate higherRateIncomeTaxAmount" in {
      sut().higherRateIncomeTaxAmount mustBe Amount(
        223.74,
        "GBP",
        Some(
          "33.53(ctnIncomeTaxHigherRate) + 25.55(ctnSavingsTaxHigherRate) + 81.33(ctnTaxOnRedundancyHr) + 83.33(ctnTaxOnCegHr) + null (ctnPensionLsumTaxDueAmt)"
        )
      )
    }

    "calculate additionalRateIncomeTaxAmount" in {
      sut().additionalRateIncomeTaxAmount mustBe Amount(
        242.19,
        "GBP",
        Some(
          "66.22(ctnIncomeTaxAddHighRate) + 2.31(ctnSavingsTaxAddHighRate) + 86.33(ctnTaxOnRedundancyAhr) + 87.33(ctnTaxOnCegAhr) + null (ctnPensionLsumTaxDueAmt)"
        )
      )
    }

    "calculate additionalRateIncomeTax" in {
      sut().additionalRateIncomeTax mustBe Amount(
        228.40,
        "GBP",
        Some(
          "3.22(ctnIncomeChgbleAddHRate) + 55.52(ctnSavingsChgbleAddHRate) + 84.33(ctnTaxableRedundancyAhr) + 85.33(ctnTaxableCegAhr) + null (itfStatePensionLsGrossAmt)"
        )
      )
    }

    "calculate selfEmployment" in {
      sut().selfEmployment mustBe Amount(
        4379.77,
        "GBP",
        Some(
          "1100.00(ctnSummaryTotalScheduleD) + 23.23(ctnSummaryTotalPartnership) + 3000.43(ctnSavingsPartnership) + 256.11(ctnDividendsPartnership)"
        )
      )
    }

    "calculate otherIncome" in {
      sut().otherIncome mustBe Amount(
        1289.99,
        "GBP",
        Some(
          "20.21(ctnSummaryTotShareOptions) + 23.11(ctnSummaryTotalUklProperty) + 24.13(ctnSummaryTotForeignIncome) + 14.19(ctnSummaryTotTrustEstates) + 29.21(ctnSummaryTotalOtherIncome) + 41.89(ctnSummaryTotalUkInterest) + 51.15(ctnSummaryTotForeignDiv) + 56.55(ctnSummaryTotalUkIntDivs) + 22.22(ctn4SumTotLifePolicyGains) + 56.21(ctnSummaryTotForeignSav) + 876.91(ctnForeignCegDedn) + 74.21(itfCegReceivedAfterTax)"
        )
      )
    }

    "calculate otherAllowances" in {
      sut().otherAllowances mustBe Amount(
        713,
        "GBP",
        Some(
          "21.34(ctnEmploymentExpensesAmt) + 27.31(ctnSummaryTotalDedPpr) + 99.12(ctnSumTotForeignTaxRelief) + 300.00(ctnSumTotLossRestricted) + 87.66(grossAnnuityPayts) + 76.65(itf4GiftsInvCharitiesAmo) + 55.55(ctnBpaAllowanceAmt) + 44.56(itfBpaAmount)"
        )
      )
    }

    "calculate otherAdjustmentsIncreasing" in {
      sut().otherAdjustmentsIncreasing mustBe Amount(
        1004.42,
        "GBP",
        Some(
          "66.66(nonDomChargeAmount) + 876.21(giftAidTaxReduced) + 5.50(netAnnuityPaytsTaxDue) + 5.55(ctnChildBenefitChrgAmt) + 50.50(ctnPensionSavingChrgbleAmt)"
        )
      )
    }

    "calculate totalIncomeTaxAmount" when {
      "totalIncomeTaxAmount lower than taxExcluded + taxOnNonExcludedInc" in {
        sut().totalIncomeTaxAmount mustBe Amount(
          133.32,
          "GBP",
          Some("10.10(taxExcluded) + 123.22(taxOnNonExcludedInc)")
        )
      }

      "totalIncomeTaxAmount greater than taxExcluded + taxOnNonExcludedInc" in {
        val newAtsData = taxSummaryLiability.atsData - TaxOnNonExcludedIncome +
          (TaxOnNonExcludedIncome -> Amount(0.4, "GBP"))
        val newLiability = taxSummaryLiability.copy(atsData = newAtsData)
        sut(newLiability).totalIncomeTaxAmount mustBe Amount(
          10.50,
          "GBP",
          Some("10.10(taxExcluded) + 0.4(taxOnNonExcludedInc)")
        )
      }
    }
    "calculate taxLiability" in {
      sut(taxSummaryLiability).taxLiability mustBe Amount(
        203.82,
        "GBP",
        Some(
          "max(0, Some(2.00(ctnLowerRateCgtRPCI) + 7.00(ctnHigherRateCgtRPCI) + 45.00(ctnCgDueEntrepreneursRate) + 5.40(ctnCgDueLowerRate) + 3.40(ctnCgDueHigherRate) + 7.70(capAdjustmentAmt))) + 10.10(taxExcluded) + 123.22(taxOnNonExcludedInc)"
        )
      )

    }
  }
}
