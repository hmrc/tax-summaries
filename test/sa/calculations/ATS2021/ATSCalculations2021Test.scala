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

package sa.calculations.ATS2021

import common.models.{Amount, Rate}
import common.utils.{BaseSpec, JsonUtil}
import play.api.libs.json.Json
import sa.models.ODSLiabilities.ODSLiabilities.TaxOnNonExcludedIncome
import sa.models.SelfAssessmentAPIResponse

class ATSCalculations2021Test extends BaseSpec {
  val taxYear                                        = 2021
  val json: String                                   = JsonUtil.load("/sa/utr_random_values.json", Map("<taxYear>" -> taxYear.toString))
  val taxSummaryLiability: SelfAssessmentAPIResponse = Json.parse(json).as[SelfAssessmentAPIResponse]

  val taxRates: Map[String, Rate] = applicationConfig.taxRates(taxYear)

  class FakeATSCalculation2021(
    val selfAssessmentAPIResponse: SelfAssessmentAPIResponse,
    val taxRates: Map[String, Rate]
  ) extends ATSCalculations2021 {
    override def scottishIncomeTax: Amount = Amount.empty("Dummy scottish income tax amount")
  }

  def sut(taxSummaryLiability: SelfAssessmentAPIResponse = taxSummaryLiability): FakeATSCalculation2021 =
    new FakeATSCalculation2021(taxSummaryLiability, taxRates)

  "Generic calculations 2021" must {
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
        338.87,
        "GBP",
        Some(
          "20.21(ctnSummaryTotShareOptions) + 23.11(ctnSummaryTotalUklProperty) + 24.13(ctnSummaryTotForeignIncome) + 14.19(ctnSummaryTotTrustEstates) + 29.21(ctnSummaryTotalOtherIncome) + 41.89(ctnSummaryTotalUkInterest) + 51.15(ctnSummaryTotForeignDiv) + 56.55(ctnSummaryTotalUkIntDivs) + 22.22(ctn4SumTotLifePolicyGains) + 56.21(ctnSummaryTotForeignSav)"
        )
      )
    }

    "calculate otherAllowances" in {
      sut().otherAllowances mustBe Amount(
        835,
        "GBP",
        Some(
          "21.34(ctnEmploymentExpensesAmt) + 27.31(ctnSummaryTotalDedPpr) + 99.12(ctnSumTotForeignTaxRelief) + 66.66(ctnSumTotLoanRestricted) + 300.00(ctnSumTotLossRestricted) + 87.66(grossAnnuityPayts) + 76.65(itf4GiftsInvCharitiesAmo) + 55.54(itfTradeUnionDeathBenefits) + 55.55(ctnBpaAllowanceAmt) + 44.56(itfBpaAmount)"
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

    "calculate basicRateIncomeTaxAmount" in {
      sut(taxSummaryLiability).basicRateIncomeTaxAmount mustBe Amount(
        497.70,
        "GBP",
        Some("372.00(ctnIncomeTaxBasicRate) + 2.30(ctnSavingsTaxLowerRate) + 123.40(ctnPensionLsumTaxDueAmt)")
      )
    }

    "calculate basicRateIncomeTax" in {
      sut(taxSummaryLiability).basicRateIncomeTax mustBe Amount(
        1878.19,
        "GBP",
        Some("1860.00(ctnIncomeChgbleBasicRate) + 5.05(ctnSavingsChgbleLowerRate) + 13.14(itfStatePensionLsGrossAmt)")
      )
    }

    "calculate higherRateIncomeTax" in {
      sut(taxSummaryLiability).higherRateIncomeTax mustBe Amount(
        7.58,
        "GBP",
        Some("5.23(ctnIncomeChgbleHigherRate) + 2.35(ctnSavingsChgbleHigherRate) + null (itfStatePensionLsGrossAmt)")
      )
    }

    "calculate additionalRateIncomeTax" in {
      sut(taxSummaryLiability).additionalRateIncomeTax mustBe Amount(
        58.74,
        "GBP",
        Some("3.22(ctnIncomeChgbleAddHRate) + 55.52(ctnSavingsChgbleAddHRate) + null (itfStatePensionLsGrossAmt)")
      )
    }
    "calculate totalAmountEmployeeNic" in {
      sut(taxSummaryLiability).totalAmountEmployeeNic mustBe Amount(
        233.33,
        "GBP",
        Some("100.00(employeeClass1Nic) + 100.00(employeeClass2Nic) + 33.33(class4Nic)")
      )
    }
    "calculate savingsRate" in {
      sut(taxSummaryLiability).savingsRate mustBe Amount(11.10, "GBP", Some("11.10(ctnSavingsChgbleStartRate)"))
    }

  }
}
