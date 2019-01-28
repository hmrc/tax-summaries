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

import models.{Amount, AtsMiddleTierData, Rate}
import play.api.libs.json.Json
import uk.gov.hmrc.play.test.UnitSpec
import utils._

import scala.io.Source

class CapitalGainsTransformationTest extends UnitSpec with AtsJsonDataUpdate {

  val taxpayerDetailsJson = Source.fromURL(getClass.getResource("/taxpayerData/test_individual_utr.json")).mkString
  val parsedTaxpayerDetailsJson = Json.parse(taxpayerDetailsJson)
  val taxYear: Int = 2014

  "The capital gains" should {
    
    "display rates (based on test_case_5.json)" in {

      val sampleJson = Source.fromURL(getClass.getResource("/test_case_5.json")).mkString

      val parsedJson = Json.parse(sampleJson)
      val returnValue: AtsMiddleTierData = ATSRawDataTransformer(parsedJson, parsedTaxpayerDetailsJson, "", taxYear).atsDataDTO

      val parsedRates = returnValue.capital_gains_data.get.rates.get
      val testRates =
        Map("cg_entrepreneurs_rate" -> Rate("10%"),
          "cg_ordinary_rate" -> Rate("18%"),
          "cg_upper_rate" -> Rate("28%"),
          "total_cg_tax_rate" -> Rate("45.34%")
          )
      testRates shouldEqual parsedRates
    }

    "display the user's capital gains earned in the selected tax year (based on test_case_5.json)" in {

      val sampleJson = Source.fromURL(getClass.getResource("/test_case_5.json")).mkString

      val parsedJson = Json.parse(sampleJson)
      val returnValue: AtsMiddleTierData = ATSRawDataTransformer(parsedJson, parsedTaxpayerDetailsJson, "", taxYear).atsDataDTO

      val parsedPayload = returnValue.capital_gains_data.get.payload.get
      val testPayload =
        Map("taxable_gains" -> Amount(12250.00, "GBP"),
            "less_tax_free_amount" -> Amount(10600.00, "GBP"),
            "pay_cg_tax_on" -> Amount(1650.00, "GBP"),
            "amount_at_entrepreneurs_rate" -> Amount(1111.00, "GBP"),
            "amount_due_at_entrepreneurs_rate" -> Amount(2222.00, "GBP"),
            "amount_at_ordinary_rate" -> Amount(3333.00, "GBP"),
            "amount_due_at_ordinary_rate" -> Amount(4444.00, "GBP"),
            "amount_at_higher_rate" -> Amount(5555.00, "GBP"),
            "amount_due_at_higher_rate" -> Amount(6666.00, "GBP"),
            "adjustments" -> Amount(7777.00, "GBP"),
            "total_cg_tax" -> Amount(5555.00, "GBP"),
            "cg_tax_per_currency_unit" -> Amount(0.4534, "GBP"))
      testPayload shouldEqual parsedPayload

      val parsedRates = returnValue.capital_gains_data.get.rates.get
      val testRates =
        Map("cg_entrepreneurs_rate" -> Rate("10%"),
          "cg_ordinary_rate" -> Rate("18%"),
          "cg_upper_rate" -> Rate("28%"),
          "total_cg_tax_rate" -> Rate("45.34%"))
      testRates shouldEqual parsedRates
    }

    "display the user's capital gains earned in the selected tax year (based on test_case_6.json)" in {

      val sampleJson = Source.fromURL(getClass.getResource("/test_case_6.json")).mkString

      val parsedJson = Json.parse(sampleJson)
      val returnValue: AtsMiddleTierData = ATSRawDataTransformer(parsedJson, parsedTaxpayerDetailsJson, "", taxYear).atsDataDTO

      val parsedYear = returnValue.taxYear
      val testYear: Int = 2014
      testYear shouldEqual parsedYear

      val parsedPayload = returnValue.capital_gains_data.get.payload.get
      val testPayload =
        Map("taxable_gains" -> Amount(20000.00, "GBP"),
            "less_tax_free_amount" -> Amount(10600.00, "GBP"),
            "pay_cg_tax_on" -> Amount(9400.00, "GBP"),
            "amount_at_entrepreneurs_rate" -> Amount(0.00, "GBP"),
            "amount_due_at_entrepreneurs_rate" -> Amount(0.00, "GBP"),
            "amount_at_ordinary_rate" -> Amount(0.00, "GBP"),
            "amount_due_at_ordinary_rate" -> Amount(0.00, "GBP"),
            "amount_at_higher_rate" -> Amount(0.00, "GBP"),
            "amount_due_at_higher_rate" -> Amount(0.00, "GBP"),
            "adjustments" -> Amount(0.00, "GBP"),
            "total_cg_tax" -> Amount(0.00, "GBP"),
            "cg_tax_per_currency_unit" -> Amount(0.00, "GBP"))
      testPayload shouldEqual parsedPayload
    }
  }
}
