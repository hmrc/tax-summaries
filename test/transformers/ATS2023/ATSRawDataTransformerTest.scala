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

package transformers.ATS2023

import models.LiabilityKey._
import models._
import play.api.libs.json.Json
import services.TaxRateService
import transformers.{ATSCalculations, ATSRawDataTransformer}
import utils.{AtsJsonDataUpdate, BaseSpec, JsonUtil}

class ATSRawDataTransformerTest extends BaseSpec with AtsJsonDataUpdate {

  private val taxpayerDetailsJson       = JsonUtil.load("/taxpayerData/test_individual_utr.json")
  private val parsedTaxpayerDetailsJson = Json.parse(taxpayerDetailsJson)
  private val taxYear: Int              = 2023
  private val taxRate                   = new TaxRateService(taxYear, applicationConfig.ratePercentages)

  private val SUT: ATSRawDataTransformer = inject[ATSRawDataTransformer]

  "The total income before tax" must {
    "parse the tax rates transformation (based on utr year:2023 data)" in {
      val sampleJson = JsonUtil.load("/test_case_5.json")

      val parsedJson   = Json.parse(sampleJson)
      val calculations = ATSCalculations.make(parsedJson.as[TaxSummaryLiability], taxRate)

      val returnValue: AtsMiddleTierData =
        SUT.atsDataDTO(taxRate, calculations, parsedTaxpayerDetailsJson, "", taxYear)

      val parsedYear    = returnValue.taxYear
      val testYear: Int = 2023
      testYear mustEqual parsedYear

      val parsedPayload = returnValue.income_tax.get.payload.get
      val testPayload   =
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
          TotalIncomeTax                  -> Amount(344.83, "GBP")
        )
      parsedPayload.map(x => x._1 -> x._2.amount) must contain allElementsOf testPayload.map(x => x._1 -> x._2.amount)

      val parsedRates   = returnValue.income_tax.get.rates.get
      val testRates     =
        Map(
          "starting_rate_for_savings_rate"  -> ApiRate("0%"),
          "basic_rate_income_tax_rate"      -> ApiRate("20%"),
          "higher_rate_income_tax_rate"     -> ApiRate("40%"),
          "additional_rate_income_tax_rate" -> ApiRate("45%"),
          "ordinary_rate_tax_rate"          -> ApiRate("8.75%"),
          "upper_rate_rate"                 -> ApiRate("33.75%"),
          "additional_rate_rate"            -> ApiRate("39.35%"),
          "scottish_starter_rate"           -> ApiRate("19%"),
          "scottish_basic_rate"             -> ApiRate("20%"),
          "scottish_intermediate_rate"      -> ApiRate("21%"),
          "scottish_higher_rate"            -> ApiRate("41%"),
          "scottish_additional_rate"        -> ApiRate("46%"),
          "savings_lower_rate"              -> ApiRate("20%"),
          "savings_higher_rate"             -> ApiRate("40%"),
          "savings_additional_rate"         -> ApiRate("45%")
        )

      testRates mustEqual parsedRates.map { case (k, v) => (k.apiValue, v) }
    }
  }
}
