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
import models.{Amount, LiabilityKey, TaxSummaryLiability}
import org.scalatest.OptionValues
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
        "ctnIncomeChgbleBasicRate"  -> Amount(100.0, "GBP"),
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
        "ctnIncomeTaxBasicRate"  -> Amount(300.0, "GBP"),
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
        "ctnIncomeChgbleHigherRate"  -> Amount(500.0, "GBP"),
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
        "ctnIncomeTaxHigherRate"  -> Amount(600.0, "GBP"),
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
        "ctnIncomeChgbleAddHRate"  -> Amount(10.0, "GBP"),
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
        "ctnIncomeTaxAddHighRate"  -> Amount(99.0, "GBP"),
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
        "ctnIncomeTaxBasicRate"    -> Amount(300.0, "GBP"),
        "ctnSavingsTaxLowerRate"   -> Amount(400.0, "GBP"),
        "ctnIncomeTaxHigherRate"   -> Amount(600.0, "GBP"),
        "ctnSavingsTaxHigherRate"  -> Amount(700.0, "GBP"),
        "ctnIncomeTaxAddHighRate"  -> Amount(100.0, "GBP"),
        "ctnSavingsTaxAddHighRate" -> Amount(200.0, "GBP"),
        "ctnPensionLumpSumTaxRate" -> BigDecimal(0.20),
        "ctnPensionLsumTaxDueAmt"  -> Amount(50.00, "GBP")
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
        "ctnIncomeTaxBasicRate"    -> Amount(300.0, "GBP"),
        "ctnSavingsTaxLowerRate"   -> Amount(400.0, "GBP"),
        "ctnIncomeTaxHigherRate"   -> Amount(600.0, "GBP"),
        "ctnSavingsTaxHigherRate"  -> Amount(700.0, "GBP"),
        "ctnIncomeTaxAddHighRate"  -> Amount(100.0, "GBP"),
        "ctnSavingsTaxAddHighRate" -> Amount(200.0, "GBP"),
        "ctnPensionLumpSumTaxRate" -> BigDecimal(0.40),
        "ctnPensionLsumTaxDueAmt"  -> Amount(50.00, "GBP")
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
        "ctnIncomeTaxBasicRate"    -> Amount(300.0, "GBP"),
        "ctnSavingsTaxLowerRate"   -> Amount(400.0, "GBP"),
        "ctnIncomeTaxHigherRate"   -> Amount(600.0, "GBP"),
        "ctnSavingsTaxHigherRate"  -> Amount(700.0, "GBP"),
        "ctnIncomeTaxAddHighRate"  -> Amount(100.0, "GBP"),
        "ctnSavingsTaxAddHighRate" -> Amount(200.0, "GBP"),
        "ctnPensionLumpSumTaxRate" -> BigDecimal(0.45),
        "ctnPensionLsumTaxDueAmt"  -> Amount(50.00, "GBP")
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

  "new SRIT values" should {

    val json = JsonUtil.load("/srit_values.json")
    val sut = ATSRawDataTransformer(Json.parse(json).as[TaxSummaryLiability], parsedTaxpayerDetailsJson, "", 2019)

    def payload(key: LiabilityKey): Option[Amount] =
      sut.atsDataDTO.income_tax.flatMap(_.payload.flatMap(_.get(key)))

    "return the correct values for scottish tax on income" in {
      payload(ScottishStarterRateTax) shouldBe Some(Amount.gbp(1.01))
      payload(ScottishBasicRateTax) shouldBe Some(Amount.gbp(2.02))
      payload(ScottishIntermediateRateTax) shouldBe Some(Amount.gbp(3.03))
      payload(ScottishHigherRateTax) shouldBe Some(Amount.gbp(4.04))
      payload(ScottishAdditionalRateTax) shouldBe Some(Amount.gbp(5.05))
    }

    "return the correct values for scottish income" in {
      payload(ScottishStarterIncome) shouldBe Some(Amount.gbp(6.06))
      payload(ScottishBasicIncome) shouldBe Some(Amount.gbp(7.07))
      payload(ScottishIntermediateIncome) shouldBe Some(Amount.gbp(8.08))
      payload(ScottishHigherIncome) shouldBe Some(Amount.gbp(9.09))
      payload(ScottishAdditionalIncome) shouldBe Some(Amount.gbp(10.10))
    }

    "return the correct values for savings tax" in {
      payload(SavingsLowerRateTax) shouldBe Some(Amount.gbp(11.11))
      payload(SavingsHigherRateTax) shouldBe Some(Amount.gbp(12.12))
      payload(SavingsAdditionalRateTax) shouldBe Some(Amount.gbp(13.13))
    }

    "return the correct values for savings totals" in {
      payload(SavingsLowerIncome) shouldBe Some(Amount.gbp(14.14))
      payload(SavingsHigherIncome) shouldBe Some(Amount.gbp(15.15))
      payload(SavingsAdditionalIncome) shouldBe Some(Amount.gbp(16.16))
    }

    "return the correct values for total scottish tax paid" in {
      payload(ScottishTotalTax) shouldBe Some(Amount.gbp(15.15))
    }
  }
}
