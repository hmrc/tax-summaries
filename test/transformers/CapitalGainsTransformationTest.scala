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

import models.Liability.{CgGainsAfterLosses, CgTotGainsAfterLosses}
import models.LiabilityKey._
import models._
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.libs.json.Json
import uk.gov.hmrc.play.test.UnitSpec
import utils._

import scala.io.Source

class CapitalGainsTransformationTest extends UnitSpec with AtsJsonDataUpdate with GuiceOneAppPerTest {

  val taxpayerDetailsJson = Source.fromURL(getClass.getResource("/taxpayerData/test_individual_utr.json")).mkString
  val parsedTaxpayerDetailsJson = Json.parse(taxpayerDetailsJson)
  val taxYear: Int = 2014

  "The capital gains" should {

    "display rates (based on test_case_5.json)" in {

      val sampleJson = Source.fromURL(getClass.getResource("/test_case_5.json")).mkString

      val parsedJson = Json.parse(sampleJson)
      val returnValue: AtsMiddleTierData =
        ATSRawDataTransformer(parsedJson.as[TaxSummaryLiability], parsedTaxpayerDetailsJson, "", taxYear).atsDataDTO

      val parsedRates = returnValue.capital_gains_data.get.rates.get
      val testRates =
        Map(
          "cg_entrepreneurs_rate"          -> ApiRate("10%"),
          "cg_ordinary_rate"               -> ApiRate("18%"),
          "cg_upper_rate"                  -> ApiRate("28%"),
          "total_cg_tax_rate"              -> ApiRate("45.34%"),
          "prop_interest_rate_lower_rate"  -> ApiRate("0%"),
          "prop_interest_rate_higher_rate" -> ApiRate("0%")
        )

      testRates shouldEqual parsedRates.map { case (k, v) => (k.apiValue, v) }
    }

    "display the user's capital gains earned in the selected tax year (based on test_case_5.json)" in {

      val sampleJson = Source.fromURL(getClass.getResource("/test_case_5.json")).mkString

      val parsedJson = Json.parse(sampleJson)
      val returnValue: AtsMiddleTierData =
        ATSRawDataTransformer(parsedJson.as[TaxSummaryLiability], parsedTaxpayerDetailsJson, "", taxYear).atsDataDTO

      val parsedPayload = returnValue.capital_gains_data.get.payload.get
      val testPayload =
        Map(
          TaxableGains                 -> Amount(12250.00, "GBP"),
          LessTaxFreeAmount            -> Amount(10600.00, "GBP"),
          PayCgTaxOn                   -> Amount(1650.00, "GBP"),
          AmountAtEntrepreneursRate    -> Amount(1111.00, "GBP"),
          AmountDueAtEntrepreneursRate -> Amount(2222.00, "GBP"),
          AmountAtOrdinaryRate         -> Amount(3333.00, "GBP"),
          AmountDueAtOrdinaryRate      -> Amount(4444.00, "GBP"),
          AmountAtHigherRate           -> Amount(5555.00, "GBP"),
          AmountDueAtHigherRate        -> Amount(6666.00, "GBP"),
          Adjustments                  -> Amount(7777.00, "GBP"),
          TotalCgTax                   -> Amount(5555.00, "GBP"),
          CgTaxPerCurrencyUnit         -> Amount(0.4534, "GBP"),
          AmountAtRPCILowerRate        -> Amount(0.00, "GBP"),
          AmountDueRPCILowerRate       -> Amount(0.00, "GBP"),
          AmountAtRPCIHigheRate        -> Amount(0.00, "GBP"),
          AmountDueRPCIHigherRate      -> Amount(0.00, "GBP")
        )
      testPayload shouldEqual parsedPayload

      val parsedRates = returnValue.capital_gains_data.get.rates.get
      val testRates =
        Map(
          "cg_entrepreneurs_rate"          -> ApiRate("10%"),
          "cg_ordinary_rate"               -> ApiRate("18%"),
          "cg_upper_rate"                  -> ApiRate("28%"),
          "total_cg_tax_rate"              -> ApiRate("45.34%"),
          "prop_interest_rate_lower_rate"  -> ApiRate("0%"),
          "prop_interest_rate_higher_rate" -> ApiRate("0%")
        )
      testRates shouldEqual parsedRates.map { case (k, v) => (k.apiValue, v) }
    }

    "display the user's capital gains earned in the selected tax year (based on test_case_6.json)" in {

      val sampleJson = Source.fromURL(getClass.getResource("/test_case_6.json")).mkString

      val parsedJson = Json.parse(sampleJson)
      val returnValue: AtsMiddleTierData =
        ATSRawDataTransformer(parsedJson.as[TaxSummaryLiability], parsedTaxpayerDetailsJson, "", taxYear).atsDataDTO

      val parsedYear = returnValue.taxYear
      val testYear: Int = 2014
      testYear shouldEqual parsedYear

      val parsedPayload = returnValue.capital_gains_data.get.payload.get
      val testPayload =
        Map(
          TaxableGains                 -> Amount(20000.00, "GBP"),
          LessTaxFreeAmount            -> Amount(10600.00, "GBP"),
          PayCgTaxOn                   -> Amount(9400.00, "GBP"),
          AmountAtEntrepreneursRate    -> Amount(0.00, "GBP"),
          AmountDueAtEntrepreneursRate -> Amount(0.00, "GBP"),
          AmountAtOrdinaryRate         -> Amount(0.00, "GBP"),
          AmountDueAtOrdinaryRate      -> Amount(0.00, "GBP"),
          AmountAtHigherRate           -> Amount(0.00, "GBP"),
          AmountDueAtHigherRate        -> Amount(0.00, "GBP"),
          Adjustments                  -> Amount(0.00, "GBP"),
          TotalCgTax                   -> Amount(0.00, "GBP"),
          CgTaxPerCurrencyUnit         -> Amount(0.00, "GBP"),
          AmountAtRPCILowerRate        -> Amount(0.00, "GBP"),
          AmountDueRPCILowerRate       -> Amount(0.00, "GBP"),
          AmountAtRPCIHigheRate        -> Amount(0.00, "GBP"),
          AmountDueRPCIHigherRate      -> Amount(0.00, "GBP")
        )
      testPayload shouldEqual parsedPayload
    }

    "return an amount of 0 when CapitalGains Annual Exempt is larger than the Taxable Gains" in {

      val sampleJson = getClass.getResource("/test_case_6.json")

      val update = Json.obj(
        CgTotGainsAfterLosses.apiValue -> Amount(100.0, "GBP"),
        CgGainsAfterLosses.apiValue    -> Amount(200.0, "GBP")
      )

      val transformedData = transformation(sourceJson = sampleJson, tliSlpAtsUpdate = update)
      val returnValue: AtsMiddleTierData =
        ATSRawDataTransformer(transformedData.as[TaxSummaryLiability], parsedTaxpayerDetailsJson, "", taxYear).atsDataDTO

      val parsedPayload = returnValue.capital_gains_data.get.payload.get
      parsedPayload(PayCgTaxOn) should equal(Amount.empty)
    }
  }
}
