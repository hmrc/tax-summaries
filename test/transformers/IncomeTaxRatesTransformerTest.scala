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

import models.LiabilityTransformer.{AdditionalRateIncomeTax, AdditionalRateIncomeTaxAmount, BasicRateIncomeTax, BasicRateIncomeTaxAmount, HigherRateIncomeTax, HigherRateIncomeTaxAmount}
import models.{Amount, TaxSummaryLiability}
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import uk.gov.hmrc.play.test.UnitSpec
import utils._

import scala.io.Source

class IncomeTaxRatesTransformerTest extends UnitSpec with AtsJsonDataUpdate with GuiceOneAppPerTest {

  val taxpayerDetailsJson = Source.fromURL(getClass.getResource("/taxpayerData/test_individual_utr.json")).mkString
  val parsedTaxpayerDetailsJson = Json.parse(taxpayerDetailsJson)
  val taxYear: Int = 2014

  "With base data for utr" should {

    "have the correct chargeable basic rate amount" in {

      val originalJson = getClass.getResource("/utr_2014.json")

      val update = Json.obj(
        "ctnIncomeChgbleBasicRate" -> Amount(100.0, "GBP"),
        "ctnSavingsChgbleLowerRate" -> Amount(200.0, "GBP")
      )

      val transformedJson = transformation(sourceJson = originalJson, tliSlpAtsUpdate = update)

      val returnValue =
        ATSRawDataTransformer(transformedJson.as[TaxSummaryLiability], parsedTaxpayerDetailsJson, "", taxYear).atsDataDTO
      val parsedPayload = returnValue.income_tax.get.payload.get

      parsedPayload(BasicRateIncomeTax) should equal(new Amount(300.0, "GBP"))
    }

    "have the correct basic rate amount" in {

      val originalJson = getClass.getResource("/utr_2014.json")

      val update = Json.obj(
        "ctnIncomeTaxBasicRate" -> Amount(300.0, "GBP"),
        "ctnSavingsTaxLowerRate" -> Amount(400.0, "GBP")
      )

      val transformedJson = transformation(sourceJson = originalJson, tliSlpAtsUpdate = update)

      val returnValue =
        ATSRawDataTransformer(transformedJson.as[TaxSummaryLiability], parsedTaxpayerDetailsJson, "", taxYear).atsDataDTO
      val parsedPayload = returnValue.income_tax.get.payload.get

      parsedPayload(BasicRateIncomeTaxAmount) should equal(new Amount(700.0, "GBP"))
    }

    "have the correct chargeable higher rate amount" in {

      val originalJson = getClass.getResource("/utr_2014.json")

      val update = Json.obj(
        "ctnIncomeChgbleHigherRate" -> Amount(500.0, "GBP"),
        "ctnSavingsChgbleHigherRate" -> Amount(600.0, "GBP")
      )

      val transformedJson = transformation(sourceJson = originalJson, tliSlpAtsUpdate = update)

      val returnValue =
        ATSRawDataTransformer(transformedJson.as[TaxSummaryLiability], parsedTaxpayerDetailsJson, "", taxYear).atsDataDTO
      val parsedPayload = returnValue.income_tax.get.payload.get

      parsedPayload(HigherRateIncomeTax) should equal(new Amount(1100.0, "GBP"))
    }

    "have the correct higher rate tax amount" in {

      val originalJson = getClass.getResource("/utr_2014.json")

      val update = Json.obj(
        "ctnIncomeTaxHigherRate" -> Amount(600.0, "GBP"),
        "ctnSavingsTaxHigherRate" -> Amount(700.0, "GBP")
      )

      val transformedJson = transformation(sourceJson = originalJson, tliSlpAtsUpdate = update)

      val returnValue =
        ATSRawDataTransformer(transformedJson.as[TaxSummaryLiability], parsedTaxpayerDetailsJson, "", taxYear).atsDataDTO
      val parsedPayload = returnValue.income_tax.get.payload.get

      parsedPayload(HigherRateIncomeTaxAmount) should equal(new Amount(1300.0, "GBP"))
    }

    "have the correct chargeable additional rate amount" in {

      val originalJson = getClass.getResource("/utr_2014.json")

      val update = Json.obj(
        "ctnIncomeChgbleAddHRate" -> Amount(10.0, "GBP"),
        "ctnSavingsChgbleAddHRate" -> Amount(20.0, "GBP")
      )

      val transformedJson = transformation(sourceJson = originalJson, tliSlpAtsUpdate = update)

      val returnValue =
        ATSRawDataTransformer(transformedJson.as[TaxSummaryLiability], parsedTaxpayerDetailsJson, "", taxYear).atsDataDTO
      val parsedPayload = returnValue.income_tax.get.payload.get

      parsedPayload(AdditionalRateIncomeTax) should equal(new Amount(30.0, "GBP"))
    }
    
    "have the correct additional rate amount" in {

      val originalJson = getClass.getResource("/utr_2014.json")

      val update = Json.obj(
        "ctnIncomeTaxAddHighRate" -> Amount(99.0, "GBP"),
        "ctnSavingsTaxAddHighRate" -> Amount(99.0, "GBP")
      )

      val transformedJson = transformation(sourceJson = originalJson, tliSlpAtsUpdate = update)

      val returnValue =
        ATSRawDataTransformer(transformedJson.as[TaxSummaryLiability], parsedTaxpayerDetailsJson, "", taxYear).atsDataDTO
      val parsedPayload = returnValue.income_tax.get.payload.get

      parsedPayload(AdditionalRateIncomeTaxAmount) should equal(new Amount(198.0, "GBP"))
    }

    "have the correct basic, higher and additional rate amount at ctnPensionLumpSumTaxRate = 20%" in {

      val originalJson = getClass.getResource("/utr_2014.json")

      val update = Json.obj(
        "ctnIncomeTaxBasicRate" -> Amount(300.0, "GBP"),
        "ctnSavingsTaxLowerRate" -> Amount(400.0, "GBP"),
        "ctnIncomeTaxHigherRate" -> Amount(600.0, "GBP"),
        "ctnSavingsTaxHigherRate" -> Amount(700.0, "GBP"),
        "ctnIncomeTaxAddHighRate" -> Amount(100.0, "GBP"),
        "ctnSavingsTaxAddHighRate" -> Amount(200.0, "GBP"),
        "ctnPensionLumpSumTaxRate" -> BigDecimal(0.20),
        "ctnPensionLsumTaxDueAmt" -> Amount(50.00, "GBP")
      )

      val transformedJson = transformation(sourceJson = originalJson, tliSlpAtsUpdate = update)

      val returnValue =
        ATSRawDataTransformer(transformedJson.as[TaxSummaryLiability], parsedTaxpayerDetailsJson, "", taxYear).atsDataDTO
      
      val parsedPayload = returnValue.income_tax.get.payload.get
      
      parsedPayload(BasicRateIncomeTaxAmount) should equal(new Amount(750.0, "GBP"))
      parsedPayload(HigherRateIncomeTaxAmount) should equal(new Amount(1300.0, "GBP"))
      parsedPayload(AdditionalRateIncomeTaxAmount) should equal(new Amount(300.0, "GBP"))
    }
    
    "have the correct basic, higher and additional rate amount at ctnPensionLumpSumTaxRate = 40%" in {

      val originalJson = getClass.getResource("/utr_2014.json")

      val update = Json.obj(
        "ctnIncomeTaxBasicRate" -> Amount(300.0, "GBP"),
        "ctnSavingsTaxLowerRate" -> Amount(400.0, "GBP"),
        "ctnIncomeTaxHigherRate" -> Amount(600.0, "GBP"),
        "ctnSavingsTaxHigherRate" -> Amount(700.0, "GBP"),
        "ctnIncomeTaxAddHighRate" -> Amount(100.0, "GBP"),
        "ctnSavingsTaxAddHighRate" -> Amount(200.0, "GBP"),
        "ctnPensionLumpSumTaxRate" -> BigDecimal(0.40),
        "ctnPensionLsumTaxDueAmt" -> Amount(50.00, "GBP")
      )

      val transformedJson = transformation(sourceJson = originalJson, tliSlpAtsUpdate = update)

      val returnValue =
        ATSRawDataTransformer(transformedJson.as[TaxSummaryLiability], parsedTaxpayerDetailsJson, "", taxYear).atsDataDTO
      
      val parsedPayload = returnValue.income_tax.get.payload.get
      
      parsedPayload(BasicRateIncomeTaxAmount) should equal(new Amount(700.0, "GBP"))
      parsedPayload(HigherRateIncomeTaxAmount) should equal(new Amount(1350.0, "GBP"))
      parsedPayload(AdditionalRateIncomeTaxAmount) should equal(new Amount(300.0, "GBP"))
    }

    "have the correct basic, higher and additional rate amount at ctnPensionLumpSumTaxRate = 45%" in {

      val originalJson = getClass.getResource("/utr_2014.json")

      val update = Json.obj(
        "ctnIncomeTaxBasicRate" -> Amount(300.0, "GBP"),
        "ctnSavingsTaxLowerRate" -> Amount(400.0, "GBP"),
        "ctnIncomeTaxHigherRate" -> Amount(600.0, "GBP"),
        "ctnSavingsTaxHigherRate" -> Amount(700.0, "GBP"),
        "ctnIncomeTaxAddHighRate" -> Amount(100.0, "GBP"),
        "ctnSavingsTaxAddHighRate" -> Amount(200.0, "GBP"),
        "ctnPensionLumpSumTaxRate" -> BigDecimal(0.45),
        "ctnPensionLsumTaxDueAmt" -> Amount(50.00, "GBP")
      )

      val transformedJson = transformation(sourceJson = originalJson, tliSlpAtsUpdate = update)

      val returnValue =
        ATSRawDataTransformer(transformedJson.as[TaxSummaryLiability], parsedTaxpayerDetailsJson, "", taxYear).atsDataDTO
      
      val parsedPayload = returnValue.income_tax.get.payload.get
      
      parsedPayload(BasicRateIncomeTaxAmount) should equal(new Amount(700.0, "GBP"))
      parsedPayload(HigherRateIncomeTaxAmount) should equal(new Amount(1300.0, "GBP"))
      parsedPayload(AdditionalRateIncomeTaxAmount) should equal(new Amount(350.0, "GBP"))
    }
  }
}
