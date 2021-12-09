/*
 * Copyright 2021 HM Revenue & Customs
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
import models.{Amount, AtsMiddleTierData, LiabilityKey, TaxSummaryLiability}
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import services.TaxRateService
import utils._

import scala.io.Source

class IncomeTaxRatesTransformerTest extends BaseSpec with AtsJsonDataUpdate {

  val taxpayerDetailsJson = Source.fromURL(getClass.getResource("/taxpayerData/test_individual_utr.json")).mkString
  val parsedTaxpayerDetailsJson = Json.parse(taxpayerDetailsJson)
  val taxYear: Int = 2014
  val taxRate = new TaxRateService(taxYear, applicationConfig.ratePercentages)

  val SUT: ATSRawDataTransformer = inject[ATSRawDataTransformer]

  "With base data for utr" must {

    "have the correct chargeable basic rate amount" in {

      val originalJson = getClass.getResource("/utr_2014.json")

      val update = Json.obj(
        "ctnIncomeChgbleBasicRate"  -> Amount(100.0, "GBP"),
        "ctnSavingsChgbleLowerRate" -> Amount(200.0, "GBP")
      )

      val transformedJson = transformation(sourceJson = originalJson, tliSlpAtsUpdate = update)

      val calculations = ATSCalculations.make(transformedJson.as[TaxSummaryLiability], taxRate)

      val returnValue: AtsMiddleTierData =
        SUT.atsDataDTO(taxRate, calculations, parsedTaxpayerDetailsJson, "", taxYear)
      val parsedPayload = returnValue.income_tax.get.payload.get

      parsedPayload(BasicRateIncomeTax) must equal(new Amount(300.0, "GBP"))
    }

    "have the correct basic rate amount" in {

      val originalJson = getClass.getResource("/utr_2014.json")

      val update = Json.obj(
        "ctnIncomeTaxBasicRate"  -> Amount(300.0, "GBP"),
        "ctnSavingsTaxLowerRate" -> Amount(400.0, "GBP")
      )

      val transformedJson = transformation(sourceJson = originalJson, tliSlpAtsUpdate = update)
      val calculations = ATSCalculations.make(transformedJson.as[TaxSummaryLiability], taxRate)

      val returnValue: AtsMiddleTierData =
        SUT.atsDataDTO(taxRate, calculations, parsedTaxpayerDetailsJson, "", taxYear)

      val parsedPayload = returnValue.income_tax.get.payload.get

      parsedPayload(BasicRateIncomeTaxAmount) must equal(new Amount(700.0, "GBP"))
    }

    "have the correct chargeable higher rate amount" in {

      val originalJson = getClass.getResource("/utr_2014.json")

      val update = Json.obj(
        "ctnIncomeChgbleHigherRate"  -> Amount(500.0, "GBP"),
        "ctnSavingsChgbleHigherRate" -> Amount(600.0, "GBP")
      )

      val transformedJson = transformation(sourceJson = originalJson, tliSlpAtsUpdate = update)
      val calculations = ATSCalculations.make(transformedJson.as[TaxSummaryLiability], taxRate)

      val returnValue: AtsMiddleTierData =
        SUT.atsDataDTO(taxRate, calculations, parsedTaxpayerDetailsJson, "", taxYear)

      val parsedPayload = returnValue.income_tax.get.payload.get

      parsedPayload(HigherRateIncomeTax) must equal(new Amount(1100.0, "GBP"))
    }

    "have the correct higher rate tax amount" in {

      val originalJson = getClass.getResource("/utr_2014.json")

      val update = Json.obj(
        "ctnIncomeTaxHigherRate"  -> Amount(600.0, "GBP"),
        "ctnSavingsTaxHigherRate" -> Amount(700.0, "GBP")
      )

      val transformedJson = transformation(sourceJson = originalJson, tliSlpAtsUpdate = update)
      val calculations = ATSCalculations.make(transformedJson.as[TaxSummaryLiability], taxRate)

      val returnValue: AtsMiddleTierData =
        SUT.atsDataDTO(taxRate, calculations, parsedTaxpayerDetailsJson, "", taxYear)

      val parsedPayload = returnValue.income_tax.get.payload.get

      parsedPayload(HigherRateIncomeTaxAmount) must equal(new Amount(1300.0, "GBP"))
    }

    "have the correct chargeable additional rate amount" in {

      val originalJson = getClass.getResource("/utr_2014.json")

      val update = Json.obj(
        "ctnIncomeChgbleAddHRate"  -> Amount(10.0, "GBP"),
        "ctnSavingsChgbleAddHRate" -> Amount(20.0, "GBP")
      )

      val transformedJson = transformation(sourceJson = originalJson, tliSlpAtsUpdate = update)
      val calculations = ATSCalculations.make(transformedJson.as[TaxSummaryLiability], taxRate)

      val returnValue: AtsMiddleTierData =
        SUT.atsDataDTO(taxRate, calculations, parsedTaxpayerDetailsJson, "", taxYear)

      val parsedPayload = returnValue.income_tax.get.payload.get

      parsedPayload(AdditionalRateIncomeTax) must equal(new Amount(30.0, "GBP"))
    }

    "have the correct additional rate amount" in {

      val originalJson = getClass.getResource("/utr_2014.json")

      val update = Json.obj(
        "ctnIncomeTaxAddHighRate"  -> Amount(99.0, "GBP"),
        "ctnSavingsTaxAddHighRate" -> Amount(99.0, "GBP")
      )

      val transformedJson = transformation(sourceJson = originalJson, tliSlpAtsUpdate = update)
      val calculations = ATSCalculations.make(transformedJson.as[TaxSummaryLiability], taxRate)

      val returnValue: AtsMiddleTierData =
        SUT.atsDataDTO(taxRate, calculations, parsedTaxpayerDetailsJson, "", taxYear)

      val parsedPayload = returnValue.income_tax.get.payload.get

      parsedPayload(AdditionalRateIncomeTaxAmount) must equal(new Amount(198.0, "GBP"))
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
      val calculations = ATSCalculations.make(transformedJson.as[TaxSummaryLiability], taxRate)

      val returnValue: AtsMiddleTierData =
        SUT.atsDataDTO(taxRate, calculations, parsedTaxpayerDetailsJson, "", taxYear)

      val parsedPayload = returnValue.income_tax.get.payload.get

      parsedPayload(BasicRateIncomeTaxAmount) must equal(new Amount(750.0, "GBP"))
      parsedPayload(HigherRateIncomeTaxAmount) must equal(new Amount(1300.0, "GBP"))
      parsedPayload(AdditionalRateIncomeTaxAmount) must equal(new Amount(300.0, "GBP"))
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
      val calculations = ATSCalculations.make(transformedJson.as[TaxSummaryLiability], taxRate)

      val returnValue: AtsMiddleTierData =
        SUT.atsDataDTO(taxRate, calculations, parsedTaxpayerDetailsJson, "", taxYear)

      val parsedPayload = returnValue.income_tax.get.payload.get

      parsedPayload(BasicRateIncomeTaxAmount) must equal(new Amount(700.0, "GBP"))
      parsedPayload(HigherRateIncomeTaxAmount) must equal(new Amount(1350.0, "GBP"))
      parsedPayload(AdditionalRateIncomeTaxAmount) must equal(new Amount(300.0, "GBP"))
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
      val calculations = ATSCalculations.make(transformedJson.as[TaxSummaryLiability], taxRate)

      val returnValue: AtsMiddleTierData =
        SUT.atsDataDTO(taxRate, calculations, parsedTaxpayerDetailsJson, "", taxYear)

      val parsedPayload = returnValue.income_tax.get.payload.get

      parsedPayload(BasicRateIncomeTaxAmount) must equal(new Amount(700.0, "GBP"))
      parsedPayload(HigherRateIncomeTaxAmount) must equal(new Amount(1300.0, "GBP"))
      parsedPayload(AdditionalRateIncomeTaxAmount) must equal(new Amount(350.0, "GBP"))
    }
  }

  "new SRIT values" must {

    val json = Json.parse(JsonUtil.load("/srit_values.json"))
    val calculations = ATSCalculations.make(json.as[TaxSummaryLiability], taxRate)

    val returnValue: AtsMiddleTierData =
      SUT.atsDataDTO(taxRate, calculations, parsedTaxpayerDetailsJson, "", 2019)

    def payload(key: LiabilityKey): Option[Amount] =
      returnValue.income_tax.flatMap(_.payload.flatMap(_.get(key)))

    "return the correct values for scottish tax on income" in {
      payload(ScottishStarterRateTax) mustBe Some(Amount.gbp(1.01))
      payload(ScottishBasicRateTax) mustBe Some(Amount.gbp(2.02))
      payload(ScottishIntermediateRateTax) mustBe Some(Amount.gbp(3.03))
      payload(ScottishHigherRateTax) mustBe Some(Amount.gbp(4.04))
      payload(ScottishAdditionalRateTax) mustBe Some(Amount.gbp(5.05))
    }

    "return the correct values for scottish income" in {
      payload(ScottishStarterIncome) mustBe Some(Amount.gbp(6.06))
      payload(ScottishBasicIncome) mustBe Some(Amount.gbp(7.07))
      payload(ScottishIntermediateIncome) mustBe Some(Amount.gbp(8.08))
      payload(ScottishHigherIncome) mustBe Some(Amount.gbp(9.09))
      payload(ScottishAdditionalIncome) mustBe Some(Amount.gbp(10.10))
    }

    "return the correct values for savings tax" in {
      payload(SavingsLowerRateTax) mustBe Some(Amount.gbp(11.11))
      payload(SavingsHigherRateTax) mustBe Some(Amount.gbp(12.12))
      payload(SavingsAdditionalRateTax) mustBe Some(Amount.gbp(13.13))
    }

    "return the correct values for savings totals" in {
      payload(SavingsLowerIncome) mustBe Some(Amount.gbp(14.14))
      payload(SavingsHigherIncome) mustBe Some(Amount.gbp(15.15))
      payload(SavingsAdditionalIncome) mustBe Some(Amount.gbp(16.16))
    }

    "return the correct values for total scottish tax paid" in {
      payload(ScottishTotalTax) mustBe Some(Amount.gbp(15.15))
    }
  }
}
