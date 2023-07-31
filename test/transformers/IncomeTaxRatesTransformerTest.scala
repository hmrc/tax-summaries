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

package transformers

import models.LiabilityKey._
import models.{Amount, AtsMiddleTierData, LiabilityKey, TaxSummaryLiability}
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.libs.json.{JsValue, Json}
import services.TaxRateService
import uk.gov.hmrc.http.HeaderCarrier
import utils._

import scala.concurrent.ExecutionContext
import scala.io.{BufferedSource, Source}

class IncomeTaxRatesTransformerTest extends BaseSpec with AtsJsonDataUpdate {

  val taxpayerDetailsJsonSource: BufferedSource =
    Source.fromURL(getClass.getResource("/taxpayerData/test_individual_utr.json"))
  val taxpayerDetailsJson: String               = taxpayerDetailsJsonSource.mkString
  taxpayerDetailsJsonSource.close()
  val parsedTaxpayerDetailsJson: JsValue        = Json.parse(taxpayerDetailsJson)
  val taxYear: Int                              = 2014
  val taxRate                                   = new TaxRateService(taxYear, applicationConfig.ratePercentages)
  implicit val ec: ExecutionContext             = inject[ExecutionContext]
  implicit val hc: HeaderCarrier                = HeaderCarrier()

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
      val parsedPayload                  = returnValue.income_tax.get.payload.get

      parsedPayload(BasicRateIncomeTax) must equal(
        new Amount(
          300.0,
          "GBP",
          Some("100.0(ctnIncomeChgbleBasicRate) + 200.0(ctnSavingsChgbleLowerRate) + null (itfStatePensionLsGrossAmt)")
        )
      )
    }

    "have the correct basic rate amount" in {

      val originalJson = getClass.getResource("/utr_2014.json")

      val update = Json.obj(
        "ctnIncomeTaxBasicRate"  -> Amount(
          300.0,
          "GBP",
          Some("100.0(ctnIncomeChgbleBasicRate) + 200.0(ctnSavingsChgbleLowerRate) + null (itfStatePensionLsGrossAmt)")
        ),
        "ctnSavingsTaxLowerRate" -> Amount(400.0, "GBP")
      )

      val transformedJson = transformation(sourceJson = originalJson, tliSlpAtsUpdate = update)
      val calculations    = ATSCalculations.make(transformedJson.as[TaxSummaryLiability], taxRate)

      val returnValue: AtsMiddleTierData =
        SUT.atsDataDTO(taxRate, calculations, parsedTaxpayerDetailsJson, "", taxYear)

      val parsedPayload = returnValue.income_tax.get.payload.get

      parsedPayload(BasicRateIncomeTaxAmount) must equal(
        new Amount(
          700.0,
          "GBP",
          Some("300.0(ctnIncomeTaxBasicRate) + 400.0(ctnSavingsTaxLowerRate) + null (ctnPensionLsumTaxDueAmt)")
        )
      )
    }

    "have the correct chargeable higher rate amount" in {

      val originalJson = getClass.getResource("/utr_2014.json")

      val update = Json.obj(
        "ctnIncomeChgbleHigherRate"  -> Amount(500.0, "GBP"),
        "ctnSavingsChgbleHigherRate" -> Amount(600.0, "GBP")
      )

      val transformedJson = transformation(sourceJson = originalJson, tliSlpAtsUpdate = update)
      val calculations    = ATSCalculations.make(transformedJson.as[TaxSummaryLiability], taxRate)

      val returnValue: AtsMiddleTierData =
        SUT.atsDataDTO(taxRate, calculations, parsedTaxpayerDetailsJson, "", taxYear)

      val parsedPayload = returnValue.income_tax.get.payload.get

      parsedPayload(HigherRateIncomeTax) must equal(
        new Amount(
          1100.0,
          "GBP",
          Some(
            "500.0(ctnIncomeChgbleHigherRate) + 600.0(ctnSavingsChgbleHigherRate) + null (itfStatePensionLsGrossAmt)"
          )
        )
      )
    }

    "have the correct higher rate tax amount" in {

      val originalJson = getClass.getResource("/utr_2014.json")

      val update = Json.obj(
        "ctnIncomeTaxHigherRate"  -> Amount(600.0, "GBP"),
        "ctnSavingsTaxHigherRate" -> Amount(700.0, "GBP")
      )

      val transformedJson = transformation(sourceJson = originalJson, tliSlpAtsUpdate = update)
      val calculations    = ATSCalculations.make(transformedJson.as[TaxSummaryLiability], taxRate)

      val returnValue: AtsMiddleTierData =
        SUT.atsDataDTO(taxRate, calculations, parsedTaxpayerDetailsJson, "", taxYear)

      val parsedPayload = returnValue.income_tax.get.payload.get

      parsedPayload(HigherRateIncomeTaxAmount) must equal(
        new Amount(
          1300.0,
          "GBP",
          Some("600.0(ctnIncomeTaxHigherRate) + 700.0(ctnSavingsTaxHigherRate) + null (ctnPensionLsumTaxDueAmt)")
        )
      )
    }

    "have the correct chargeable additional rate amount" in {

      val originalJson = getClass.getResource("/utr_2014.json")

      val update = Json.obj(
        "ctnIncomeChgbleAddHRate"  -> Amount(10.0, "GBP"),
        "ctnSavingsChgbleAddHRate" -> Amount(20.0, "GBP")
      )

      val transformedJson = transformation(sourceJson = originalJson, tliSlpAtsUpdate = update)
      val calculations    = ATSCalculations.make(transformedJson.as[TaxSummaryLiability], taxRate)

      val returnValue: AtsMiddleTierData =
        SUT.atsDataDTO(taxRate, calculations, parsedTaxpayerDetailsJson, "", taxYear)

      val parsedPayload = returnValue.income_tax.get.payload.get

      parsedPayload(AdditionalRateIncomeTax) must equal(
        new Amount(
          30.0,
          "GBP",
          Some("10.0(ctnIncomeChgbleAddHRate) + 20.0(ctnSavingsChgbleAddHRate) + null (itfStatePensionLsGrossAmt)")
        )
      )
    }

    "have the correct additional rate amount" in {

      val originalJson = getClass.getResource("/utr_2014.json")

      val update = Json.obj(
        "ctnIncomeTaxAddHighRate"  -> Amount(99.0, "GBP"),
        "ctnSavingsTaxAddHighRate" -> Amount(99.0, "GBP")
      )

      val transformedJson = transformation(sourceJson = originalJson, tliSlpAtsUpdate = update)
      val calculations    = ATSCalculations.make(transformedJson.as[TaxSummaryLiability], taxRate)

      val returnValue: AtsMiddleTierData =
        SUT.atsDataDTO(taxRate, calculations, parsedTaxpayerDetailsJson, "", taxYear)

      val parsedPayload = returnValue.income_tax.get.payload.get

      parsedPayload(AdditionalRateIncomeTaxAmount) must equal(
        new Amount(
          198.0,
          "GBP",
          Some("99.0(ctnIncomeTaxAddHighRate) + 99.0(ctnSavingsTaxAddHighRate) + null (ctnPensionLsumTaxDueAmt)")
        )
      )
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
      val calculations    = ATSCalculations.make(transformedJson.as[TaxSummaryLiability], taxRate)

      val returnValue: AtsMiddleTierData =
        SUT.atsDataDTO(taxRate, calculations, parsedTaxpayerDetailsJson, "", taxYear)

      val parsedPayload = returnValue.income_tax.get.payload.get

      parsedPayload(BasicRateIncomeTaxAmount)      must equal(
        new Amount(
          750.0,
          "GBP",
          Some("300.0(ctnIncomeTaxBasicRate) + 400.0(ctnSavingsTaxLowerRate) + 50.0(ctnPensionLsumTaxDueAmt)")
        )
      )
      parsedPayload(HigherRateIncomeTaxAmount)     must equal(
        new Amount(
          1300.0,
          "GBP",
          Some("600.0(ctnIncomeTaxHigherRate) + 700.0(ctnSavingsTaxHigherRate) + null (ctnPensionLsumTaxDueAmt)")
        )
      )
      parsedPayload(AdditionalRateIncomeTaxAmount) must equal(
        new Amount(
          300.0,
          "GBP",
          Some("100.0(ctnIncomeTaxAddHighRate) + 200.0(ctnSavingsTaxAddHighRate) + null (ctnPensionLsumTaxDueAmt)")
        )
      )
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
      val calculations    = ATSCalculations.make(transformedJson.as[TaxSummaryLiability], taxRate)

      val returnValue: AtsMiddleTierData =
        SUT.atsDataDTO(taxRate, calculations, parsedTaxpayerDetailsJson, "", taxYear)

      val parsedPayload = returnValue.income_tax.get.payload.get

      parsedPayload(BasicRateIncomeTaxAmount)      must equal(
        new Amount(
          700.0,
          "GBP",
          Some("300.0(ctnIncomeTaxBasicRate) + 400.0(ctnSavingsTaxLowerRate) + null (ctnPensionLsumTaxDueAmt)")
        )
      )
      parsedPayload(HigherRateIncomeTaxAmount)     must equal(
        new Amount(
          1350.0,
          "GBP",
          Some("600.0(ctnIncomeTaxHigherRate) + 700.0(ctnSavingsTaxHigherRate) + 50.0(ctnPensionLsumTaxDueAmt)")
        )
      )
      parsedPayload(AdditionalRateIncomeTaxAmount) must equal(
        new Amount(
          300.0,
          "GBP",
          Some("100.0(ctnIncomeTaxAddHighRate) + 200.0(ctnSavingsTaxAddHighRate) + null (ctnPensionLsumTaxDueAmt)")
        )
      )
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
      val calculations    = ATSCalculations.make(transformedJson.as[TaxSummaryLiability], taxRate)

      val returnValue: AtsMiddleTierData =
        SUT.atsDataDTO(taxRate, calculations, parsedTaxpayerDetailsJson, "", taxYear)

      val parsedPayload = returnValue.income_tax.get.payload.get

      parsedPayload(BasicRateIncomeTaxAmount)      must equal(
        new Amount(
          700.0,
          "GBP",
          Some("300.0(ctnIncomeTaxBasicRate) + 400.0(ctnSavingsTaxLowerRate) + null (ctnPensionLsumTaxDueAmt)")
        )
      )
      parsedPayload(HigherRateIncomeTaxAmount)     must equal(
        new Amount(
          1300.0,
          "GBP",
          Some("600.0(ctnIncomeTaxHigherRate) + 700.0(ctnSavingsTaxHigherRate) + null (ctnPensionLsumTaxDueAmt)")
        )
      )
      parsedPayload(AdditionalRateIncomeTaxAmount) must equal(
        new Amount(
          350.0,
          "GBP",
          Some("100.0(ctnIncomeTaxAddHighRate) + 200.0(ctnSavingsTaxAddHighRate) + 50.0(ctnPensionLsumTaxDueAmt)")
        )
      )
    }
  }

  "new SRIT values" must {

    val json         = Json.parse(JsonUtil.load("/srit_values.json"))
    val calculations = ATSCalculations.make(json.as[TaxSummaryLiability], taxRate)

    val returnValue: AtsMiddleTierData =
      SUT.atsDataDTO(taxRate, calculations, parsedTaxpayerDetailsJson, "", 2019)

    def payload(key: LiabilityKey): Option[Amount] =
      returnValue.income_tax.flatMap(_.payload.flatMap(_.get(key)))

    "return the correct values for scottish tax on income" in {
      payload(ScottishStarterRateTax) mustBe Some(
        Amount.gbp(1.01, "1.01(taxOnPaySSR) + null (ctnPensionLsumTaxDueAmt)")
      )
      payload(ScottishBasicRateTax) mustBe Some(
        Amount.gbp(2.02, "2.02(ctnIncomeTaxBasicRate) + null (ctnPensionLsumTaxDueAmt)")
      )
      payload(ScottishIntermediateRateTax) mustBe Some(
        Amount.gbp(3.03, "3.03(taxOnPaySIR) + null (ctnPensionLsumTaxDueAmt)")
      )
      payload(ScottishHigherRateTax) mustBe Some(
        Amount.gbp(4.04, "4.04(ctnIncomeTaxHigherRate) + null (ctnPensionLsumTaxDueAmt)")
      )
      payload(ScottishAdditionalRateTax) mustBe Some(
        Amount.gbp(5.05, "5.05(ctnIncomeTaxAddHighRate) + null (ctnPensionLsumTaxDueAmt)")
      )
    }

    "return the correct values for scottish income" in {
      payload(ScottishStarterIncome) mustBe Some(
        Amount.gbp(6.06, "6.06(taxablePaySSR) + null (itfStatePensionLsGrossAmt)")
      )
      payload(ScottishBasicIncome) mustBe Some(
        Amount.gbp(7.07, "7.07(ctnIncomeChgbleBasicRate) + null (itfStatePensionLsGrossAmt)")
      )
      payload(ScottishIntermediateIncome) mustBe Some(
        Amount.gbp(8.08, "8.08(taxablePaySIR) + null (itfStatePensionLsGrossAmt)")
      )
      payload(ScottishHigherIncome) mustBe Some(
        Amount.gbp(9.09, "9.09(ctnIncomeChgbleHigherRate) + null (itfStatePensionLsGrossAmt)")
      )
      payload(ScottishAdditionalIncome) mustBe Some(
        Amount.gbp(10.10, "10.10(ctnIncomeChgbleAddHRate) + null (itfStatePensionLsGrossAmt)")
      )
    }

    "return the correct values for savings tax" in {
      payload(SavingsLowerRateTax) mustBe Some(Amount.gbp(11.11, "11.11(ctnSavingsTaxLowerRate)"))
      payload(SavingsHigherRateTax) mustBe Some(Amount.gbp(12.12, "12.12(ctnSavingsTaxHigherRate)"))
      payload(SavingsAdditionalRateTax) mustBe Some(Amount.gbp(13.13, "13.13(ctnSavingsTaxAddHighRate)"))
    }

    "return the correct values for savings totals" in {
      payload(SavingsLowerIncome) mustBe Some(Amount.gbp(14.14, "14.14(ctnSavingsChgbleLowerRate)"))
      payload(SavingsHigherIncome) mustBe Some(Amount.gbp(15.15, "15.15(ctnSavingsChgbleHigherRate)"))
      payload(SavingsAdditionalIncome) mustBe Some(Amount.gbp(16.16, "16.16(ctnSavingsChgbleAddHRate)"))
    }

    "return the correct values for total scottish tax paid" in {
      payload(ScottishTotalTax) mustBe Some(
        Amount.gbp(
          15.15,
          "1.01(taxOnPaySSR) + null (ctnPensionLsumTaxDueAmt) + 2.02(ctnIncomeTaxBasicRate) + null (ctnPensionLsumTaxDueAmt) + 3.03(taxOnPaySIR) + null (ctnPensionLsumTaxDueAmt) + 4.04(ctnIncomeTaxHigherRate) + null (ctnPensionLsumTaxDueAmt) + 5.05(ctnIncomeTaxAddHighRate) + null (ctnPensionLsumTaxDueAmt)"
        )
      )
    }
  }
}
