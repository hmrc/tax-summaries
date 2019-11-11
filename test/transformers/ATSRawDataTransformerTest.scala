/*
 * Copyright 2019 HM Revenue & Customs
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

import models.LiabilityTransformer._
import models._
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.libs.json.Json
import uk.gov.hmrc.play.test.UnitSpec
import utils.AtsJsonDataUpdate

import scala.io.Source

class ATSRawDataTransformerTest extends UnitSpec with AtsJsonDataUpdate with GuiceOneAppPerTest {

  val taxpayerDetailsJson = Source.fromURL(getClass.getResource("/taxpayerData/test_individual_utr.json")).mkString
  val parsedTaxpayerDetailsJson = Json.parse(taxpayerDetailsJson)
  val taxYear: Int = 2014

  "The income before tax" should {

    "parse the income values for utr year:2014" in {

      val sampleJson = Source.fromURL(getClass.getResource("/utr_2014.json")).mkString

      val parsedJson = Json.parse(sampleJson)
      val returnValue: AtsMiddleTierData =
        ATSRawDataTransformer(parsedJson.as[TaxSummaryLiability], parsedTaxpayerDetailsJson, "", taxYear).atsDataDTO

      val parsedYear = returnValue.taxYear
      val testYear: Int = 2014
      testYear shouldEqual parsedYear

      val parsedPayload = returnValue.income_data.get.payload.get
      val testPayload =
        Map(SelfEmploymentIncome -> Amount(1100.0, "GBP"),
          IncomeFromEmployment -> Amount(10500.0, "GBP"),
          StatePension -> Amount(0.0, "GBP"),
          OtherPensionIncome -> Amount(0.0, "GBP"),
          TaxableStateBenefits -> Amount(0.0, "GBP"),
          OtherIncome -> Amount(0.0, "GBP"),
          BenefitsFromEmployment-> Amount(0.0, "GBP"),
          TotalIncomeBeforeTax -> Amount(11600.0, "GBP"))
      testPayload shouldEqual parsedPayload
    }

    "parse the income values for test case 2" in {
      val sampleJson = Source.fromURL(getClass.getResource("/test_case_2.json")).mkString

      val parsedJson = Json.parse(sampleJson)
      val returnValue: AtsMiddleTierData =
        ATSRawDataTransformer(parsedJson.as[TaxSummaryLiability], parsedTaxpayerDetailsJson, "", taxYear).atsDataDTO

      val parsedYear = returnValue.taxYear
      val testYear: Int = 2014
      testYear shouldEqual parsedYear

      val parsedPayload = returnValue.income_data.get.payload.get
      val testPayload =
        Map(SelfEmploymentIncome -> Amount(6.0, "GBP"),
          IncomeFromEmployment -> Amount(8.0, "GBP"),
          StatePension -> Amount(16.0, "GBP"),
          OtherPensionIncome -> Amount(96.0, "GBP"),
          TaxableStateBenefits -> Amount(896.0, "GBP"),
          OtherIncome -> Amount(523264.0, "GBP"),
          BenefitsFromEmployment -> Amount(1.0, "GBP"),
          TotalIncomeBeforeTax -> Amount(524287, "GBP"))
      testPayload shouldEqual parsedPayload
    }
  }

  "The summary page data" should {
    "parse the NICs data" in {
      val sampleJson = Source.fromURL(getClass.getResource("/test_case_4.json")).mkString

      val parsedJson = Json.parse(sampleJson)
      val returnValue: AtsMiddleTierData =
        ATSRawDataTransformer(parsedJson.as[TaxSummaryLiability], parsedTaxpayerDetailsJson, "", taxYear).atsDataDTO

      val parsedYear = returnValue.taxYear
      val testYear: Int = 2014
      testYear shouldEqual parsedYear

      val parsedPayload = returnValue.summary_data.get.payload.get
      val testPayload =
        Map(EmployeeNicAmount -> Amount(200.00, "GBP"),
          TotalIncomeTaxAndNics -> Amount(572.00, "GBP"),
          YourTotalTax-> Amount(572.00, "GBP"),
          PersonalTaxFreeAmount-> Amount(9440.00,"GBP"),
          TotalTaxFreeAmount -> Amount(9740.00,"GBP"),
          TotalIncomeBeforeTax -> Amount(11600.00,"GBP"),
          TotalIncomeTax -> Amount(372.00,"GBP"),
          TotalCgTax -> Amount(0.00,"GBP"),
          TaxableGains -> Amount(0.00,"GBP"),
          CgTaxPerCurrencyUnit -> Amount(0.00,"GBP"),
          NicsAndTaxPerCurrencyUnit -> Amount(0.0493,"GBP")
          )
      testPayload shouldEqual parsedPayload
      
      val parsedRates = returnValue.summary_data.get.rates.get
      val testRates = Map("total_cg_tax_rate" -> ApiRate("0%"),
          "nics_and_tax_rate" -> ApiRate("4.93%")
          )
      testRates shouldEqual parsedRates
    }
    
    "parse the NICs data with 'other_adjustments_reducing' roundup" in {

      val originalJson = getClass.getResource("/test_case_4.json")

      val update = Json.obj("ctnDeficiencyRelief" -> Amount(0.01, "GBP"))

      val transformedJson = transformation(sourceJson = originalJson, tliSlpAtsUpdate = update)

      val returnValue =
        ATSRawDataTransformer(transformedJson.as[TaxSummaryLiability], parsedTaxpayerDetailsJson, "", taxYear).atsDataDTO

      val parsedYear = returnValue.taxYear
      val testYear: Int = 2014
      testYear shouldEqual parsedYear

      val parsedPayload = returnValue.summary_data.get.payload.get
      val testPayload =
        Map(EmployeeNicAmount -> Amount(200.00, "GBP"),
          TotalIncomeTaxAndNics -> Amount(571.00, "GBP"),
          YourTotalTax-> Amount(571.00, "GBP"),
          PersonalTaxFreeAmount-> Amount(9440.00,"GBP"),
          TotalTaxFreeAmount -> Amount(9740.00,"GBP"),
          TotalIncomeBeforeTax-> Amount(11600.00,"GBP"),
          TotalIncomeTax-> Amount(371.00,"GBP"),
          TotalCgTax -> Amount(0.00,"GBP"),
          TaxableGains-> Amount(0.00,"GBP"),
          CgTaxPerCurrencyUnit -> Amount(0.00,"GBP"),
          NicsAndTaxPerCurrencyUnit -> Amount(0.0492,"GBP")
          )
      testPayload shouldEqual parsedPayload
      
      val parsedRates = returnValue.summary_data.get.rates.get
      val testRates = Map("total_cg_tax_rate" -> ApiRate("0%"),
          "nics_and_tax_rate" -> ApiRate("4.92%")
          )
      testRates shouldEqual parsedRates
    }

    "parse the NICs data for utr year:2014" in {

      val sampleJson = Source.fromURL(getClass.getResource("/test_case_5.json")).mkString

      val parsedJson = Json.parse(sampleJson)
      val returnValue: AtsMiddleTierData =
        ATSRawDataTransformer(parsedJson.as[TaxSummaryLiability], parsedTaxpayerDetailsJson, "", taxYear).atsDataDTO

      val parsedYear = returnValue.taxYear
      val testYear: Int = 2014
      testYear shouldEqual parsedYear

      val parsedPayload = returnValue.summary_data.get.payload.get
      val testPayload =
        Map(EmployeeNicAmount -> Amount(200.0, "GBP"),
          TotalIncomeTaxAndNics -> Amount(562.0, "GBP"),
          YourTotalTax -> Amount(6117.00, "GBP"),
          PersonalTaxFreeAmount-> Amount(9440.00,"GBP"),
          TotalTaxFreeAmount -> Amount(9740.00,"GBP"),
          TotalIncomeBeforeTax -> Amount(11600.00,"GBP"),
          TotalIncomeTax -> Amount(362.00,"GBP"),
          TotalCgTax -> Amount(5555.00,"GBP"),
          TaxableGains-> Amount(12250.00,"GBP"),
          CgTaxPerCurrencyUnit -> Amount(0.4534,"GBP"),
          NicsAndTaxPerCurrencyUnit -> Amount(0.0484,"GBP"))
      testPayload shouldEqual parsedPayload
      
      val parsedRates = returnValue.summary_data.get.rates.get
      val testRates = Map("total_cg_tax_rate" -> ApiRate("45.34%"),
          "nics_and_tax_rate" -> ApiRate("4.84%")
          )
      testRates shouldEqual parsedRates
    }
  }

  "The total income before tax" should {
    "parse the tax rates transformation (based on utr year:2014 data)" in {

      val sampleJson = Source.fromURL(getClass.getResource("/test_case_5.json")).mkString

      val parsedJson = Json.parse(sampleJson)
      val returnValue: AtsMiddleTierData =
        ATSRawDataTransformer(parsedJson.as[TaxSummaryLiability], parsedTaxpayerDetailsJson, "", taxYear).atsDataDTO

      val parsedYear = returnValue.taxYear
      val testYear: Int = 2014
      testYear shouldEqual parsedYear

      val parsedPayload = returnValue.income_tax.get.payload.get
      val testPayload =
        Map(StartingRateForSavings -> Amount(0.00, "GBP"),
          StartingRateForSavingsAmount -> Amount(0.00, "GBP"),
          BasicRateIncomeTax -> Amount(1860.00, "GBP"),
          BasicRateIncomeTaxAmount -> Amount(372.00, "GBP"),
          HigherRateIncomeTax -> Amount(0.00, "GBP"),
          HigherRateIncomeTaxAmount -> Amount(0.00, "GBP"),
          AdditionalRateIncomeTax -> Amount(0.00, "GBP"),
          AdditionalRateIncomeTaxAmount -> Amount(0.00, "GBP"),
          OrdinaryRate -> Amount(0.00, "GBP"),
          OrdinaryRateAmount -> Amount(0.00, "GBP"),
          UpperRate -> Amount(0.00, "GBP"),
          UpperRateAmount -> Amount(0.00, "GBP"),
          AdditionalRate -> Amount(0.00, "GBP"),
          AdditionalRateAmount -> Amount(0.00, "GBP"),
          OtherAdjustmentsIncreasing -> Amount(0.00, "GBP"),
          MarriageAllowanceReceivedAmount -> Amount(0.00, "GBP"),
          OtherAdjustmentsReducing -> Amount(10.0, "GBP"),
          ScottishIncomeTax -> Amount(186.00, "GBP"),
          TotalIncomeTax -> Amount(362.00, "GBP"))
      testPayload shouldEqual parsedPayload

      val parsedRates = returnValue.income_tax.get.rates.get
      val testRates =
        Map("starting_rate_for_savings_rate" -> ApiRate("10%"),
          "basic_rate_income_tax_rate" -> ApiRate("20%"),
          "higher_rate_income_tax_rate" ->ApiRate("40%"),
          "additional_rate_income_tax_rate" ->ApiRate( "45%"),
          "ordinary_rate_tax_rate" -> ApiRate("10%"),
          "upper_rate_rate" -> ApiRate("32.5%"),
          "additional_rate_rate" -> ApiRate("37.5%")
        )
      testRates shouldEqual parsedRates
    }

    "ATS raw data transformer" should {
      "produce a no ats error if the total income tax is -500 and capital gains tax is 200" in {

        val sampleJson = Source.fromURL(getClass.getResource("/test_case_7.json")).mkString

        val parsedJson = Json.parse(sampleJson)
        val returnValue: AtsMiddleTierData =
          ATSRawDataTransformer(parsedJson.as[TaxSummaryLiability], parsedTaxpayerDetailsJson, "", taxYear).atsDataDTO

        returnValue.errors.get.error shouldBe "NoAtsError"
      }

      "produce a no ats error if the total income tax is 200 and capital gains tax is -500" in {

        val sampleJson = Source.fromURL(getClass.getResource("/test_case_8.json")).mkString

        val parsedJson = Json.parse(sampleJson)
        val returnValue: AtsMiddleTierData =
          ATSRawDataTransformer(parsedJson.as[TaxSummaryLiability], parsedTaxpayerDetailsJson, "", taxYear).atsDataDTO

        returnValue.errors.get.error shouldBe "NoAtsError"
      }

      "produce a no ats error if both total income tax and capital gains tax are negative" in {

        val sampleJson = Source.fromURL(getClass.getResource("/test_case_9.json")).mkString

        val parsedJson = Json.parse(sampleJson)
        val returnValue: AtsMiddleTierData =
          ATSRawDataTransformer(parsedJson.as[TaxSummaryLiability], parsedTaxpayerDetailsJson, "", taxYear).atsDataDTO

        returnValue.errors.get.error shouldBe "NoAtsError"
      }
    }
  }
}
