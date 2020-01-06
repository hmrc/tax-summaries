/*
 * Copyright 2020 HM Revenue & Customs
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

package transformers

import models.LiabilityKey._
import models._
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.libs.json.Json
import uk.gov.hmrc.play.test.UnitSpec
import utils.{AtsJsonDataUpdate, JsonUtil}

import scala.io.Source

class ATSRawDataTransformerTest extends UnitSpec with AtsJsonDataUpdate with GuiceOneAppPerTest {

  val taxpayerDetailsJson = JsonUtil.load("/taxpayerData/test_individual_utr.json")
  val parsedTaxpayerDetailsJson = Json.parse(taxpayerDetailsJson)
  val taxYear: Int = 2014

  "The income before tax" should {

    "parse the income values for utr year:2014" in {

      val sampleJson = JsonUtil.load("/utr_2014.json")

      val parsedJson = Json.parse(sampleJson)
      val returnValue: AtsMiddleTierData =
        ATSRawDataTransformer(parsedJson.as[TaxSummaryLiability], parsedTaxpayerDetailsJson, "", taxYear).atsDataDTO

      val parsedYear = returnValue.taxYear
      val testYear: Int = 2014
      testYear shouldEqual parsedYear

      val parsedPayload = returnValue.income_data.get.payload.get
      val testPayload =
        Map(
          SelfEmploymentIncome   -> Amount(1100.0, "GBP"),
          IncomeFromEmployment   -> Amount(10500.0, "GBP"),
          StatePension           -> Amount(0.0, "GBP"),
          OtherPensionIncome     -> Amount(0.0, "GBP"),
          TaxableStateBenefits   -> Amount(0.0, "GBP"),
          OtherIncome            -> Amount(18.18, "GBP"),
          BenefitsFromEmployment -> Amount(0.0, "GBP"),
          TotalIncomeBeforeTax   -> Amount(11618.18, "GBP")
        )
      testPayload shouldEqual parsedPayload
    }

    "parse the income values for test case 2" in {
      val sampleJson = JsonUtil.load("/test_case_2.json")

      val parsedJson = Json.parse(sampleJson)
      val returnValue: AtsMiddleTierData =
        ATSRawDataTransformer(parsedJson.as[TaxSummaryLiability], parsedTaxpayerDetailsJson, "", taxYear).atsDataDTO

      val parsedYear = returnValue.taxYear
      val testYear: Int = 2014
      testYear shouldEqual parsedYear

      val parsedPayload = returnValue.income_data.get.payload.get
      val testPayload =
        Map(
          SelfEmploymentIncome   -> Amount(6.0, "GBP"),
          IncomeFromEmployment   -> Amount(8.0, "GBP"),
          StatePension           -> Amount(16.0, "GBP"),
          OtherPensionIncome     -> Amount(96.0, "GBP"),
          TaxableStateBenefits   -> Amount(896.0, "GBP"),
          OtherIncome            -> Amount(523282.18, "GBP"),
          BenefitsFromEmployment -> Amount(1.0, "GBP"),
          TotalIncomeBeforeTax   -> Amount(524305.18, "GBP")
        )
      testPayload shouldEqual parsedPayload
    }
  }

  "The summary page data" should {
    "parse the NICs data" in {
      val sampleJson = JsonUtil.load("/test_case_4.json")

      val parsedJson = Json.parse(sampleJson)
      val returnValue: AtsMiddleTierData =
        ATSRawDataTransformer(parsedJson.as[TaxSummaryLiability], parsedTaxpayerDetailsJson, "", taxYear).atsDataDTO

      val parsedYear = returnValue.taxYear
      val testYear: Int = 2014
      testYear shouldEqual parsedYear

      val parsedPayload = returnValue.summary_data.get.payload.get
      val testPayload =
        Map(
          EmployeeNicAmount         -> Amount(200.00, "GBP"),
          TotalIncomeTaxAndNics     -> Amount(554.00, "GBP"),
          YourTotalTax              -> Amount(554.00, "GBP"),
          PersonalTaxFreeAmount     -> Amount(9440.00, "GBP"),
          TotalTaxFreeAmount        -> Amount(9740.00, "GBP"),
          TotalIncomeBeforeTax      -> Amount(11618.18, "GBP"),
          TotalIncomeTax            -> Amount(354.00, "GBP"),
          TotalCgTax                -> Amount(0.00, "GBP"),
          TaxableGains              -> Amount(0.00, "GBP"),
          CgTaxPerCurrencyUnit      -> Amount(0.00, "GBP"),
          NicsAndTaxPerCurrencyUnit -> Amount(0.0476, "GBP")
        )
      testPayload shouldEqual parsedPayload

      val parsedRates = returnValue.summary_data.get.rates.get

      val testRates = Map(
        "total_cg_tax_rate" -> ApiRate("0%"),
        "nics_and_tax_rate" -> ApiRate("4.76%")
      )
      testRates shouldEqual parsedRates.map { case (k, v) => (k.apiValue, v) }
    }

    "parse the NICs data with 'other_adjustments_reducing' roundup" in {

      val update = Json.obj("ctnDeficiencyRelief" -> Amount(0.01, "GBP"))
      val amendedJson = JsonUtil.loadAndReplace("/test_case_4.json", update)

      val returnValue =
        ATSRawDataTransformer(amendedJson.as[TaxSummaryLiability], parsedTaxpayerDetailsJson, "", taxYear).atsDataDTO

      val parsedYear = returnValue.taxYear
      val testYear: Int = 2014
      testYear shouldEqual parsedYear

      val parsedPayload = returnValue.summary_data.get.payload.get
      val testPayload =
        Map(
          EmployeeNicAmount         -> Amount(200.00, "GBP"),
          TotalIncomeTaxAndNics     -> Amount(554.00, "GBP"),
          YourTotalTax              -> Amount(554.00, "GBP"),
          PersonalTaxFreeAmount     -> Amount(9440.00, "GBP"),
          TotalTaxFreeAmount        -> Amount(9740.00, "GBP"),
          TotalIncomeBeforeTax      -> Amount(11618.18, "GBP"),
          TotalIncomeTax            -> Amount(354.00, "GBP"),
          TotalCgTax                -> Amount(0.00, "GBP"),
          TaxableGains              -> Amount(0.00, "GBP"),
          CgTaxPerCurrencyUnit      -> Amount(0.00, "GBP"),
          NicsAndTaxPerCurrencyUnit -> Amount(0.0476, "GBP")
        )
      testPayload shouldEqual parsedPayload

      val parsedRates = returnValue.summary_data.get.rates.get

      val testRates = Map(
        "total_cg_tax_rate" -> ApiRate("0%"),
        "nics_and_tax_rate" -> ApiRate("4.76%")
      )

      testRates shouldEqual parsedRates.map { case (k, v) => (k.apiValue, v) }
    }

    "parse the NICs data for utr year:2014" in {

      val sampleJson = JsonUtil.load("/test_case_5.json")

      val parsedJson = Json.parse(sampleJson)
      val returnValue: AtsMiddleTierData =
        ATSRawDataTransformer(parsedJson.as[TaxSummaryLiability], parsedTaxpayerDetailsJson, "", taxYear).atsDataDTO

      val parsedYear = returnValue.taxYear
      val testYear: Int = 2014
      testYear shouldEqual parsedYear

      val parsedPayload = returnValue.summary_data.get.payload.get
      val testPayload =
        Map(
          EmployeeNicAmount         -> Amount(200.0, "GBP"),
          TotalIncomeTaxAndNics     -> Amount(544.0, "GBP"),
          YourTotalTax              -> Amount(6099.00, "GBP"),
          PersonalTaxFreeAmount     -> Amount(9440.00, "GBP"),
          TotalTaxFreeAmount        -> Amount(9740.00, "GBP"),
          TotalIncomeBeforeTax      -> Amount(11618.18, "GBP"),
          TotalIncomeTax            -> Amount(344.00, "GBP"),
          TotalCgTax                -> Amount(5555.00, "GBP"),
          TaxableGains              -> Amount(12250.00, "GBP"),
          CgTaxPerCurrencyUnit      -> Amount(0.4534, "GBP"),
          NicsAndTaxPerCurrencyUnit -> Amount(0.0468, "GBP")
        )
      testPayload shouldEqual parsedPayload

      val parsedRates = returnValue.summary_data.get.rates.get

      val testRates = Map(
        "total_cg_tax_rate" -> ApiRate("45.34%"),
        "nics_and_tax_rate" -> ApiRate("4.68%")
      )
      testRates shouldEqual parsedRates.map { case (k, v) => (k.apiValue, v) }
    }
  }

  "The total income before tax" should {
    "parse the tax rates transformation (based on utr year:2014 data)" in {

      val sampleJson = JsonUtil.load("/test_case_5.json")

      val parsedJson = Json.parse(sampleJson)
      val returnValue: AtsMiddleTierData =
        ATSRawDataTransformer(parsedJson.as[TaxSummaryLiability], parsedTaxpayerDetailsJson, "", taxYear).atsDataDTO

      val parsedYear = returnValue.taxYear
      val testYear: Int = 2014
      testYear shouldEqual parsedYear

      val parsedPayload = returnValue.income_tax.get.payload.get
      val testPayload =
        Map(
          StartingRateForSavings          -> Amount(0.00, "GBP"),
          StartingRateForSavingsAmount    -> Amount(0.00, "GBP"),
          BasicRateIncomeTax              -> Amount(1860.00, "GBP"),
          BasicRateIncomeTaxAmount        -> Amount(372.00, "GBP"),
          HigherRateIncomeTax             -> Amount(0.00, "GBP"),
          HigherRateIncomeTaxAmount       -> Amount(0.00, "GBP"),
          AdditionalRateIncomeTax         -> Amount(0.00, "GBP"),
          AdditionalRateIncomeTaxAmount   -> Amount(0.00, "GBP"),
          OrdinaryRate                    -> Amount(0.00, "GBP"),
          OrdinaryRateAmount              -> Amount(0.00, "GBP"),
          UpperRate                       -> Amount(0.00, "GBP"),
          UpperRateAmount                 -> Amount(0.00, "GBP"),
          AdditionalRate                  -> Amount(0.00, "GBP"),
          AdditionalRateAmount            -> Amount(0.00, "GBP"),
          OtherAdjustmentsIncreasing      -> Amount(0.00, "GBP"),
          MarriageAllowanceReceivedAmount -> Amount(0.00, "GBP"),
          OtherAdjustmentsReducing        -> Amount(28.0, "GBP"),
          ScottishIncomeTax               -> Amount(186.00, "GBP"),
          TotalIncomeTax                  -> Amount(344.00, "GBP")
        )
      parsedPayload should contain allElementsOf testPayload

      val parsedRates = returnValue.income_tax.get.rates.get
      val testRates =
        Map(
          "starting_rate_for_savings_rate"  -> ApiRate("10%"),
          "basic_rate_income_tax_rate"      -> ApiRate("20%"),
          "higher_rate_income_tax_rate"     -> ApiRate("40%"),
          "additional_rate_income_tax_rate" -> ApiRate("45%"),
          "ordinary_rate_tax_rate"          -> ApiRate("10%"),
          "upper_rate_rate"                 -> ApiRate("32.5%"),
          "additional_rate_rate"            -> ApiRate("37.5%"),
          "scottish_starter_rate"           -> ApiRate("19%"),
          "scottish_basic_rate"             -> ApiRate("20%"),
          "scottish_intermediate_rate"      -> ApiRate("21%"),
          "scottish_higher_rate"            -> ApiRate("41%"),
          "scottish_additional_rate"        -> ApiRate("46%"),
          "savings_lower_rate"              -> ApiRate("20%"),
          "savings_higher_rate"             -> ApiRate("40%"),
          "savings_additional_rate"         -> ApiRate("45%")
        )

      testRates shouldEqual parsedRates.map { case (k, v) => (k.apiValue, v) }
    }

    "Calculate the correct Total Income Tax" in {

      val update = Json.obj(
        "ctnPensionLumpSumTaxRate"   -> 0.45,
        "ctnSavingsTaxStartingRate"  -> Amount(991.0, "GBP"),
        "ctnIncomeTaxBasicRate"      -> Amount(1153.0, "GBP"),
        "ctnSavingsTaxLowerRate"     -> Amount(1174.0, "GBP"),
        "ctnPensionLsumTaxDueAmt"    -> Amount(2458.0, "GBP"),
        "ctnIncomeTaxHigherRate"     -> Amount(1816.0, "GBP"),
        "ctnSavingsTaxHigherRate"    -> Amount(1725.0, "GBP"),
        "ctnIncomeTaxAddHighRate"    -> Amount(1366.0, "GBP"),
        "ctnSavingsTaxAddHighRate"   -> Amount(2061.0, "GBP"),
        "ctnDividendTaxLowRate"      -> Amount(293.0, "GBP"),
        "ctnDividendTaxHighRate"     -> Amount(487.0, "GBP"),
        "ctnDividendTaxAddHighRate"  -> Amount(725.0, "GBP"),
        "nonDomChargeAmount"         -> Amount(64.0, "GBP"),
        "taxExcluded"                -> Amount(88.0, "GBP"),
        "incomeTaxDue"               -> Amount(75.0, "GBP"),
        "netAnnuityPaytsTaxDue"      -> Amount(111.0, "GBP"),
        "ctnChildBenefitChrgAmt"     -> Amount(119.0, "GBP"),
        "ctnPensionSavingChrgbleAmt" -> Amount(127.0, "GBP"),
        "ctn4TaxDueAfterAllceRlf"    -> Amount(100.0, "GBP"),
        "ctnDeficiencyRelief"        -> Amount(612.2, "GBP"),
        "topSlicingRelief"           -> Amount(134.0, "GBP"),
        "ctnVctSharesReliefAmt"      -> Amount(532.0, "GBP"),
        "ctnEisReliefAmt"            -> Amount(762.0, "GBP"),
        "ctnSeedEisReliefAmt"        -> Amount(159.0, "GBP"),
        "ctnCommInvTrustRelAmt"      -> Amount(854.0, "GBP"),
        "atsSurplusMcaAlimonyRel"    -> Amount(137.0, "GBP"),
        "ctnNotionalTaxCegs"         -> Amount(99.0, "GBP"),
        "ctnNotlTaxOthrSrceAmo"      -> Amount(87.0, "GBP"),
        "ctnTaxCredForDivs"          -> Amount(166.0, "GBP"),
        "ctnQualDistnReliefAmt"      -> Amount(258, "GBP"),
        "figTotalTaxCreditRelief"    -> Amount(789.0, "GBP"),
        "ctnNonPayableTaxCredits"    -> Amount(198.0, "GBP"),
        "reliefForFinanceCosts"      -> Amount(469.0, "GBP"),
        "ctnMarriageAllceInAmt"      -> Amount(587.0, "GBP")
      )

      val amendedJson = JsonUtil.loadAndReplace("/test_case_5.json", update)

      val returnValue =
        ATSRawDataTransformer(amendedJson.as[TaxSummaryLiability], parsedTaxpayerDetailsJson, "", taxYear).atsDataDTO
      val parsedPayload = returnValue.income_tax.get.payload.get
      parsedPayload(TotalIncomeTax) shouldEqual Amount(8872, "GBP")
    }

    "Calculate the Scottish Rate" in {

      val update = Json.obj(
        "ctnIncomeChgbleBasicRate"  -> Amount(469.0, "GBP"),
        "ctnIncomeChgbleHigherRate" -> Amount(267.0, "GBP"),
        "ctnIncomeChgbleAddHRate"   -> Amount(568.0, "GBP")
      )

      val amendedJson = JsonUtil.loadAndReplace("/test_case_5.json", update)

      val returnValue =
        ATSRawDataTransformer(amendedJson.as[TaxSummaryLiability], parsedTaxpayerDetailsJson, "", taxYear).atsDataDTO
      val parsedPayload = returnValue.income_tax.get.payload.get
      parsedPayload(ScottishIncomeTax) shouldEqual Amount(130.4, "GBP")
    }

    "ATS raw data transformer" should {
      "produce a no ats error if the total income tax is -500 and capital gains tax is 200" in {

        val sampleJson = JsonUtil.load("/test_case_7.json")

        val parsedJson = Json.parse(sampleJson)
        val returnValue: AtsMiddleTierData =
          ATSRawDataTransformer(parsedJson.as[TaxSummaryLiability], parsedTaxpayerDetailsJson, "", taxYear).atsDataDTO

        returnValue.errors.get.error shouldBe "NoAtsError"
      }

      "produce a no ats error if the total income tax is 200 and capital gains tax is -500" in {

        val sampleJson = JsonUtil.load("/test_case_8.json")

        val parsedJson = Json.parse(sampleJson)
        val returnValue: AtsMiddleTierData =
          ATSRawDataTransformer(parsedJson.as[TaxSummaryLiability], parsedTaxpayerDetailsJson, "", taxYear).atsDataDTO

        returnValue.errors.get.error shouldBe "NoAtsError"
      }

      "produce a no ats error if both total income tax and capital gains tax are negative" in {

        val sampleJson = JsonUtil.load("/test_case_9.json")

        val parsedJson = Json.parse(sampleJson)
        val returnValue: AtsMiddleTierData =
          ATSRawDataTransformer(parsedJson.as[TaxSummaryLiability], parsedTaxpayerDetailsJson, "", taxYear).atsDataDTO

        returnValue.errors.get.error shouldBe "NoAtsError"
      }
    }
  }
}
