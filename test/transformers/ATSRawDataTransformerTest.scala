/*
 * Copyright 2017 HM Revenue & Customs
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

import models.{Amount, AtsMiddleTierData, Rate}
import play.api.libs.json.Json
import uk.gov.hmrc.play.test.UnitSpec
import utils.AtsJsonDataUpdate

import scala.io.Source

class ATSRawDataTransformerTest extends UnitSpec with AtsJsonDataUpdate {

  val taxpayerDetailsJson = Source.fromURL(getClass.getResource("/taxpayerData/test_individual_utr.json")).mkString
  val parsedTaxpayerDetailsJson = Json.parse(taxpayerDetailsJson)
  val taxYear: Int = 2014

  "The income before tax" should {

    "parse the income values for utr year:2014" in {

      val sampleJson = Source.fromURL(getClass.getResource("/utr_2014.json")).mkString

      val parsedJson = Json.parse(sampleJson)
      val returnValue: AtsMiddleTierData = ATSRawDataTransformer(parsedJson, parsedTaxpayerDetailsJson, "", taxYear).atsDataDTO

      val parsedYear = returnValue.taxYear
      val testYear: Int = 2014
      testYear shouldEqual parsedYear

      val parsedPayload = returnValue.income_data.get.payload.get
      val testPayload =
        Map("self_employment_income" -> Amount(1100.0, "GBP"),
          "income_from_employment" -> Amount(10500.0, "GBP"),
          "state_pension" -> Amount(0.0, "GBP"),
          "other_pension_income" -> Amount(0.0, "GBP"),
          "taxable_state_benefits" -> Amount(0.0, "GBP"),
          "other_income" -> Amount(0.0, "GBP"),
          "benefits_from_employment" -> Amount(0.0, "GBP"),
          "total_income_before_tax" -> Amount(11600.0, "GBP"))
      testPayload shouldEqual parsedPayload
    }

    "parse the income values for test case 2" in {
      val sampleJson = Source.fromURL(getClass.getResource("/test_case_2.json")).mkString

      val parsedJson = Json.parse(sampleJson)
      val returnValue: AtsMiddleTierData = ATSRawDataTransformer(parsedJson, parsedTaxpayerDetailsJson, "", taxYear).atsDataDTO

      val parsedYear = returnValue.taxYear
      val testYear: Int = 2014
      testYear shouldEqual parsedYear

      val parsedPayload = returnValue.income_data.get.payload.get
      val testPayload =
        Map("self_employment_income" -> Amount(6.0, "GBP"),
          "income_from_employment" -> Amount(8.0, "GBP"),
          "state_pension" -> Amount(16.0, "GBP"),
          "other_pension_income" -> Amount(96.0, "GBP"),
          "taxable_state_benefits" -> Amount(896.0, "GBP"),
          "other_income" -> Amount(523264.0, "GBP"),
          "benefits_from_employment" -> Amount(1.0, "GBP"),
          "total_income_before_tax" -> Amount(524287, "GBP"))
      testPayload shouldEqual parsedPayload
    }
  }

  "The summary page data" should {
    "parse the NICs data" in {
      val sampleJson = Source.fromURL(getClass.getResource("/test_case_4.json")).mkString

      val parsedJson = Json.parse(sampleJson)
      val returnValue: AtsMiddleTierData = ATSRawDataTransformer(parsedJson, parsedTaxpayerDetailsJson, "", taxYear).atsDataDTO

      val parsedYear = returnValue.taxYear
      val testYear: Int = 2014
      testYear shouldEqual parsedYear

      val parsedPayload = returnValue.summary_data.get.payload.get
      val testPayload =
        Map("employee_nic_amount" -> Amount(200.00, "GBP"),
          "total_income_tax_and_nics" -> Amount(572.00, "GBP"),
          "your_total_tax" -> Amount(572.00, "GBP"),
          "personal_tax_free_amount" -> Amount(9440.00,"GBP"),
          "total_tax_free_amount" -> Amount(9740.00,"GBP"),
          "total_income_before_tax" -> Amount(11600.00,"GBP"),
          "total_income_tax" -> Amount(372.00,"GBP"),
          "total_cg_tax" -> Amount(0.00,"GBP"),
          "taxable_gains" -> Amount(0.00,"GBP"),
          "cg_tax_per_currency_unit" -> Amount(0.00,"GBP"),
          "nics_and_tax_per_currency_unit" -> Amount(0.0493,"GBP")
          )
      testPayload shouldEqual parsedPayload
      
      val parsedRates = returnValue.summary_data.get.rates.get
      val testRates = Map("total_cg_tax_rate" -> Rate("0%"),
          "nics_and_tax_rate" -> Rate("4.93%")
          )
      testRates shouldEqual parsedRates
    }
    
    "parse the NICs data with 'other_adjustments_reducing' roundup" in {

      val originalJson = getClass.getResource("/test_case_4.json")

      val update = Json.obj("ctnDeficiencyRelief" -> Amount(0.01, "GBP"))

      val transformedJson = transformation(sourceJson = originalJson, tliSlpAtsUpdate = update)

      val returnValue = ATSRawDataTransformer(transformedJson, parsedTaxpayerDetailsJson, "", taxYear).atsDataDTO

      val parsedYear = returnValue.taxYear
      val testYear: Int = 2014
      testYear shouldEqual parsedYear

      val parsedPayload = returnValue.summary_data.get.payload.get
      val testPayload =
        Map("employee_nic_amount" -> Amount(200.00, "GBP"),
          "total_income_tax_and_nics" -> Amount(571.00, "GBP"),
          "your_total_tax" -> Amount(571.00, "GBP"),
          "personal_tax_free_amount" -> Amount(9440.00,"GBP"),
          "total_tax_free_amount" -> Amount(9740.00,"GBP"),
          "total_income_before_tax" -> Amount(11600.00,"GBP"),
          "total_income_tax" -> Amount(371.00,"GBP"),
          "total_cg_tax" -> Amount(0.00,"GBP"),
          "taxable_gains" -> Amount(0.00,"GBP"),
          "cg_tax_per_currency_unit" -> Amount(0.00,"GBP"),
          "nics_and_tax_per_currency_unit" -> Amount(0.0492,"GBP")
          )
      testPayload shouldEqual parsedPayload
      
      val parsedRates = returnValue.summary_data.get.rates.get
      val testRates = Map("total_cg_tax_rate" -> Rate("0%"),
          "nics_and_tax_rate" -> Rate("4.92%")
          )
      testRates shouldEqual parsedRates
    }

    "parse the NICs data for utr year:2014" in {

      val sampleJson = Source.fromURL(getClass.getResource("/test_case_5.json")).mkString

      val parsedJson = Json.parse(sampleJson)
      val returnValue: AtsMiddleTierData = ATSRawDataTransformer(parsedJson, parsedTaxpayerDetailsJson, "", taxYear).atsDataDTO

      val parsedYear = returnValue.taxYear
      val testYear: Int = 2014
      testYear shouldEqual parsedYear

      val parsedPayload = returnValue.summary_data.get.payload.get
      val testPayload =
        Map("employee_nic_amount" -> Amount(200.0, "GBP"),
          "total_income_tax_and_nics" -> Amount(572.0, "GBP"),
          "your_total_tax" -> Amount(6127.00, "GBP"),
          "personal_tax_free_amount" -> Amount(9440.00,"GBP"),
          "total_tax_free_amount" -> Amount(9740.00,"GBP"),
          "total_income_before_tax" -> Amount(11600.00,"GBP"),
          "total_income_tax" -> Amount(372.00,"GBP"),
          "total_cg_tax" -> Amount(5555.00,"GBP"),
          "taxable_gains" -> Amount(12250.00,"GBP"),
          "cg_tax_per_currency_unit" -> Amount(0.4534,"GBP"),
          "nics_and_tax_per_currency_unit" -> Amount(0.0493,"GBP"))
      testPayload shouldEqual parsedPayload
      
      val parsedRates = returnValue.summary_data.get.rates.get
      val testRates = Map("total_cg_tax_rate" -> Rate("45.34%"),
          "nics_and_tax_rate" -> Rate("4.93%")
          )
      testRates shouldEqual parsedRates
    }
  }

  "The total income before tax" should {
    "parse the tax rates transformation (based on utr year:2014 data)" in {

      val sampleJson = Source.fromURL(getClass.getResource("/test_case_5.json")).mkString

      val parsedJson = Json.parse(sampleJson)
      val returnValue: AtsMiddleTierData = ATSRawDataTransformer(parsedJson, parsedTaxpayerDetailsJson, "", taxYear).atsDataDTO

      val parsedYear = returnValue.taxYear
      val testYear: Int = 2014
      testYear shouldEqual parsedYear

      val parsedPayload = returnValue.income_tax.get.payload.get
      val testPayload =
        Map("starting_rate_for_savings" -> Amount(0.00, "GBP"),
          "starting_rate_for_savings_amount" -> Amount(0.00, "GBP"),
          "basic_rate_income_tax" -> Amount(1860.00, "GBP"),
          "basic_rate_income_tax_amount" -> Amount(372.00, "GBP"),
          "higher_rate_income_tax" -> Amount(0.00, "GBP"),
          "higher_rate_income_tax_amount" -> Amount(0.00, "GBP"),
          "additional_rate_income_tax" -> Amount(0.00, "GBP"),
          "additional_rate_income_tax_amount" -> Amount(0.00, "GBP"),
          "ordinary_rate" -> Amount(0.00, "GBP"),
          "ordinary_rate_amount" -> Amount(0.00, "GBP"),
          "upper_rate" -> Amount(0.00, "GBP"),
          "upper_rate_amount" -> Amount(0.00, "GBP"),
          "additional_rate" -> Amount(0.00, "GBP"),
          "additional_rate_amount" -> Amount(0.00, "GBP"),
          "other_adjustments_increasing" -> Amount(0.00, "GBP"),
          "marriage_allowance_received_amount" -> Amount(0.00, "GBP"),
          "other_adjustments_reducing" -> Amount(0.00, "GBP"),
          "total_income_tax" -> Amount(372.00, "GBP"))
      testPayload shouldEqual parsedPayload

      val parsedRates = returnValue.income_tax.get.rates.get
      val testRates =
        Map("starting_rate_for_savings_rate" -> Rate("10%"),
          "basic_rate_income_tax_rate" -> Rate("20%"),
          "higher_rate_income_tax_rate" -> Rate("40%"),
          "additional_rate_income_tax_rate" -> Rate("45%"),
          "ordinary_rate_tax_rate" -> Rate("10%"),
          "upper_rate_rate" -> Rate("32.5%"),
          "additional_rate_rate" -> Rate("37.5%"))
      testRates shouldEqual parsedRates
    }

    "ATS raw data transformer" should {
      "produce a no ats error if the total income tax is -500 and capital gains tax is 200" in {

        val sampleJson = Source.fromURL(getClass.getResource("/test_case_7.json")).mkString

        val parsedJson = Json.parse(sampleJson)
        val returnValue: AtsMiddleTierData = ATSRawDataTransformer(parsedJson, parsedTaxpayerDetailsJson, "", taxYear).atsDataDTO

        returnValue.errors.get.error shouldBe "NoAtsError"
      }

      "produce a no ats error if the total income tax is 200 and capital gains tax is -500" in {

        val sampleJson = Source.fromURL(getClass.getResource("/test_case_8.json")).mkString

        val parsedJson = Json.parse(sampleJson)
        val returnValue: AtsMiddleTierData = ATSRawDataTransformer(parsedJson, parsedTaxpayerDetailsJson, "", taxYear).atsDataDTO

        returnValue.errors.get.error shouldBe "NoAtsError"
      }

      "produce a no ats error if both total income tax and capital gains tax are negative" in {

        val sampleJson = Source.fromURL(getClass.getResource("/test_case_9.json")).mkString

        val parsedJson = Json.parse(sampleJson)
        val returnValue: AtsMiddleTierData = ATSRawDataTransformer(parsedJson, parsedTaxpayerDetailsJson, "", taxYear).atsDataDTO

        returnValue.errors.get.error shouldBe "NoAtsError"
      }
    }
  }
}
