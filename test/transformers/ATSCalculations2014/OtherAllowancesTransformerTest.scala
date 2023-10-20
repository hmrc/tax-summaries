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

package transformers.ATSCalculations2014

import models.LiabilityKey._
import models.{Amount, AtsMiddleTierData, TaxSummaryLiability}
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import services.TaxRateService
import transformers.{ATSCalculations, ATSRawDataTransformer}
import utils._

import scala.io.Source

class OtherAllowancesTransformerTest extends BaseSpec with AtsJsonDataUpdate with JsonUtil {

  val taxpayerDetailsJson        = JsonUtil.load("/taxpayerData/test_individual_utr.json")
  val parsedTaxpayerDetailsJson  = Json.parse(taxpayerDetailsJson)
  val taxYear: Int               = 2014
  val taxRate                    = new TaxRateService(taxYear, applicationConfig.ratePercentages)
  val SUT: ATSRawDataTransformer = inject[ATSRawDataTransformer]

  "The tax free amount" must {
    "parse the allowance data" in {
      val sampleJson = JsonUtil.load("/test_case_3.json")

      val parsedJson   = Json.parse(sampleJson)
      val calculations = ATSCalculations.make(parsedJson.as[TaxSummaryLiability], taxRate)

      val returnValue: AtsMiddleTierData =
        SUT.atsDataDTO(taxRate, calculations, parsedTaxpayerDetailsJson, "", taxYear)

      val parsedYear    = returnValue.taxYear
      val testYear: Int = 2014

      testYear mustEqual parsedYear

      val parsedPayload = returnValue.allowance_data.get.payload.get
      val testPayload   =
        Map(
          PersonalTaxFreeAmount              -> Amount(9440.0, "GBP", Some("9440.00(ctnPersonalAllowance)")),
          MarriageAllowanceTransferredAmount -> Amount(0.00, "GBP", Some("0.00(ctnMarriageAllceOutAmt)")),
          OtherAllowancesAmount              -> Amount(
            300.0,
            "GBP",
            Some(
              "0.00(ctnEmploymentExpensesAmt) + 0.00(ctnSummaryTotalDedPpr) + 0.00(ctnSumTotForeignTaxRelief) + 0.00(ctnSumTotLoanRestricted) + 300.00(ctnSumTotLossRestricted) + 0.00(grossAnnuityPayts) + 0.00(itf4GiftsInvCharitiesAmo) + 0.00(itfTradeUnionDeathBenefits) + 0.00(ctnBpaAllowanceAmt) + 0.00(itfBpaAmount) + 0.00(grossExcludedIncome)"
            )
          ),
          TotalTaxFreeAmount                 -> Amount(
            9740.0,
            "GBP",
            Some(
              "0.00(ctnEmploymentExpensesAmt) + 0.00(ctnSummaryTotalDedPpr) + 0.00(ctnSumTotForeignTaxRelief) + 0.00(ctnSumTotLoanRestricted) + 300.00(ctnSumTotLossRestricted) + 0.00(grossAnnuityPayts) + 0.00(itf4GiftsInvCharitiesAmo) + 0.00(itfTradeUnionDeathBenefits) + 0.00(ctnBpaAllowanceAmt) + 0.00(itfBpaAmount) + 0.00(grossExcludedIncome) + 9440.00(ctnPersonalAllowance) - 0.00(ctnMarriageAllceOutAmt)"
            )
          )
        )
      testPayload mustEqual parsedPayload
    }

    "parse the allowance data where the marriage allowance is not present in API data so defaults to 0" in {

      val sampleJson = JsonUtil.load("/utr_2014_income_status_and_fields_missing.json")

      val parsedJson   = Json.parse(sampleJson)
      val calculations = ATSCalculations.make(parsedJson.as[TaxSummaryLiability], taxRate)

      val returnValue: AtsMiddleTierData =
        SUT.atsDataDTO(taxRate, calculations, parsedTaxpayerDetailsJson, "", taxYear)

      val parsedYear    = returnValue.taxYear
      val testYear: Int = 2014

      testYear mustEqual parsedYear

      val parsedPayload = returnValue.allowance_data.get.payload.get
      val testPayload   =
        Map(
          PersonalTaxFreeAmount              -> Amount(9440.0, "GBP", Some("9440.00(ctnPersonalAllowance)")),
          MarriageAllowanceTransferredAmount -> Amount(0.00, "GBP", Some("0(ctnMarriageAllceOutAmt)")),
          OtherAllowancesAmount              -> Amount(
            300.0,
            "GBP",
            Some(
              "0.00(ctnEmploymentExpensesAmt) + 0.00(ctnSummaryTotalDedPpr) + 0.00(ctnSumTotForeignTaxRelief) + 0.00(ctnSumTotLoanRestricted) + 300.00(ctnSumTotLossRestricted) + 0.00(grossAnnuityPayts) + 0.00(itf4GiftsInvCharitiesAmo) + 0.00(itfTradeUnionDeathBenefits) + 0.00(ctnBpaAllowanceAmt) + 0.00(itfBpaAmount) + 0.00(grossExcludedIncome)"
            )
          ),
          TotalTaxFreeAmount                 -> Amount(
            9740.0,
            "GBP",
            Some(
              "0.00(ctnEmploymentExpensesAmt) + 0.00(ctnSummaryTotalDedPpr) + 0.00(ctnSumTotForeignTaxRelief) + 0.00(ctnSumTotLoanRestricted) + 300.00(ctnSumTotLossRestricted) + 0.00(grossAnnuityPayts) + 0.00(itf4GiftsInvCharitiesAmo) + 0.00(itfTradeUnionDeathBenefits) + 0.00(ctnBpaAllowanceAmt) + 0.00(itfBpaAmount) + 0.00(grossExcludedIncome) + 9440.00(ctnPersonalAllowance) - 0(ctnMarriageAllceOutAmt)"
            )
          )
        )
      testPayload mustEqual parsedPayload
    }

    "parse the allowance data with Marriage Allowance Amount subtracted" in {

      val data         = Json.obj("ctnMarriageAllceOutAmt" -> Amount(200.00, "GBP"))
      val amendedJson  = JsonUtil.loadAndReplace("/test_case_3.json", data)
      val calculations = ATSCalculations.make(amendedJson.as[TaxSummaryLiability], taxRate)

      val returnValue: AtsMiddleTierData =
        SUT.atsDataDTO(taxRate, calculations, parsedTaxpayerDetailsJson, "", taxYear)

      val parsedYear    = returnValue.taxYear
      val testYear: Int = 2014

      testYear mustEqual parsedYear

      val parsedPayload = returnValue.allowance_data.get.payload.get
      val testPayload   =
        Map(
          PersonalTaxFreeAmount              -> Amount(9440.0, "GBP", Some("9440.00(ctnPersonalAllowance)")),
          MarriageAllowanceTransferredAmount -> Amount(200.00, "GBP", Some("200.0(ctnMarriageAllceOutAmt)")),
          OtherAllowancesAmount              -> Amount(
            300.0,
            "GBP",
            Some(
              "0.00(ctnEmploymentExpensesAmt) + 0.00(ctnSummaryTotalDedPpr) + 0.00(ctnSumTotForeignTaxRelief) + 0.00(ctnSumTotLoanRestricted) + 300.00(ctnSumTotLossRestricted) + 0.00(grossAnnuityPayts) + 0.00(itf4GiftsInvCharitiesAmo) + 0.00(itfTradeUnionDeathBenefits) + 0.00(ctnBpaAllowanceAmt) + 0.00(itfBpaAmount) + 0.00(grossExcludedIncome)"
            )
          ),
          TotalTaxFreeAmount                 -> Amount(
            9540.0,
            "GBP",
            Some(
              "0.00(ctnEmploymentExpensesAmt) + 0.00(ctnSummaryTotalDedPpr) + 0.00(ctnSumTotForeignTaxRelief) + 0.00(ctnSumTotLoanRestricted) + 300.00(ctnSumTotLossRestricted) + 0.00(grossAnnuityPayts) + 0.00(itf4GiftsInvCharitiesAmo) + 0.00(itfTradeUnionDeathBenefits) + 0.00(ctnBpaAllowanceAmt) + 0.00(itfBpaAmount) + 0.00(grossExcludedIncome) + 9440.00(ctnPersonalAllowance) - 200.0(ctnMarriageAllceOutAmt)"
            )
          )
        )
      testPayload mustEqual parsedPayload
    }
  }

  "With base data for utr" must {

    "have the correct other allowances data" in {

      val sampleJson = Source.fromURL(getClass.getResource("/utr_2014.json")).mkString

      val parsedJson   = Json.parse(sampleJson)
      val calculations = ATSCalculations.make(parsedJson.as[TaxSummaryLiability], taxRate)

      val returnValue: AtsMiddleTierData =
        SUT.atsDataDTO(taxRate, calculations, parsedTaxpayerDetailsJson, "", taxYear)

      val parsedYear    = returnValue.taxYear
      val testYear: Int = 2014
      testYear mustEqual parsedYear

      val parsedPayload = returnValue.allowance_data.get.payload.get

      parsedPayload(OtherAllowancesAmount) must equal(
        new Amount(
          300.0,
          "GBP",
          Some(
            "0.00(ctnEmploymentExpensesAmt) + 0.00(ctnSummaryTotalDedPpr) + 0.00(ctnSumTotForeignTaxRelief) + 0.00(ctnSumTotLoanRestricted) + 300.00(ctnSumTotLossRestricted) + 0.00(grossAnnuityPayts) + 0.00(itf4GiftsInvCharitiesAmo) + 0.00(itfTradeUnionDeathBenefits) + 0.00(ctnBpaAllowanceAmt) + 0.00(itfBpaAmount) + 0.00(grossExcludedIncome)"
          )
        )
      )
    }

    "have the correct summed other allowances data" in {

      val update = Json.obj(
        "ctnEmploymentExpensesAmt"   -> Amount(10.0, "GBP"),
        "ctnSummaryTotalDedPpr"      -> Amount(20.0, "GBP"),
        "ctnSumTotForeignTaxRelief"  -> Amount(30.0, "GBP"),
        "ctnSumTotLoanRestricted"    -> Amount(40.0, "GBP"),
        "ctnSumTotLossRestricted"    -> Amount(50.0, "GBP"),
        "grossAnnuityPayts"          -> Amount(60.0, "GBP"),
        "itf4GiftsInvCharitiesAmo"   -> Amount(70.0, "GBP"),
        "itfTradeUnionDeathBenefits" -> Amount(80.0, "GBP"),
        "ctnBpaAllowanceAmt"         -> Amount(90.0, "GBP"),
        "itfBpaAmount"               -> Amount(100.0, "GBP"),
        "grossExcludedIncome"        -> Amount(110.0, "GBP")
      )

      val transformedJson = JsonUtil.loadAndReplace("/utr_2014.json", update)
      val calculations    = ATSCalculations.make(transformedJson.as[TaxSummaryLiability], taxRate)

      val returnValue: AtsMiddleTierData =
        SUT.atsDataDTO(taxRate, calculations, parsedTaxpayerDetailsJson, "", taxYear)

      val parsedPayload = returnValue.allowance_data.get.payload.get

      parsedPayload(OtherAllowancesAmount) must equal(
        new Amount(
          660.0,
          "GBP",
          Some(
            "10.0(ctnEmploymentExpensesAmt) + 20.0(ctnSummaryTotalDedPpr) + 30.0(ctnSumTotForeignTaxRelief) + 40.0(ctnSumTotLoanRestricted) + 50.0(ctnSumTotLossRestricted) + 60.0(grossAnnuityPayts) + 70.0(itf4GiftsInvCharitiesAmo) + 80.0(itfTradeUnionDeathBenefits) + 90.0(ctnBpaAllowanceAmt) + 100.0(itfBpaAmount) + 110.0(grossExcludedIncome)"
          )
        )
      )
    }

    "have the correct summed other allowances data (with 'other_allowances_amount' roundup)" in {

      val update = Json.obj(
        "ctnEmploymentExpensesAmt"   -> Amount(10.0, "GBP"),
        "ctnSummaryTotalDedPpr"      -> Amount(20.0, "GBP"),
        "ctnSumTotForeignTaxRelief"  -> Amount(30.0, "GBP"),
        "ctnSumTotLoanRestricted"    -> Amount(40.0, "GBP"),
        "ctnSumTotLossRestricted"    -> Amount(50.0, "GBP"),
        "grossAnnuityPayts"          -> Amount(59.01, "GBP"),
        "itf4GiftsInvCharitiesAmo"   -> Amount(70.0, "GBP"),
        "itfTradeUnionDeathBenefits" -> Amount(80.0, "GBP"),
        "ctnBpaAllowanceAmt"         -> Amount(90.0, "GBP"),
        "itfBpaAmount"               -> Amount(100.0, "GBP"),
        "grossExcludedIncome"        -> Amount(110.0, "GBP")
      )

      val transformedJson = JsonUtil.loadAndReplace("/utr_2014.json", update)
      val calculations    = ATSCalculations.make(transformedJson.as[TaxSummaryLiability], taxRate)

      val returnValue: AtsMiddleTierData =
        SUT.atsDataDTO(taxRate, calculations, parsedTaxpayerDetailsJson, "", taxYear)

      val parsedPayload = returnValue.allowance_data.get.payload.get

      parsedPayload(OtherAllowancesAmount) must equal(
        new Amount(
          660.0,
          "GBP",
          Some(
            "10.0(ctnEmploymentExpensesAmt) + 20.0(ctnSummaryTotalDedPpr) + 30.0(ctnSumTotForeignTaxRelief) + 40.0(ctnSumTotLoanRestricted) + 50.0(ctnSumTotLossRestricted) + 59.01(grossAnnuityPayts) + 70.0(itf4GiftsInvCharitiesAmo) + 80.0(itfTradeUnionDeathBenefits) + 90.0(ctnBpaAllowanceAmt) + 100.0(itfBpaAmount) + 110.0(grossExcludedIncome)"
          )
        )
      )
    }
  }
}
