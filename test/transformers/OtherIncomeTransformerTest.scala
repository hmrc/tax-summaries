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

import models.LiabilityKey.OtherIncome
import models.{Amount, AtsMiddleTierData, TaxSummaryLiability}
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import services.TaxRateService
import utils._

import scala.io.Source

class OtherIncomeTransformerTest extends BaseSpec with AtsJsonDataUpdate {

  val taxpayerDetailsJson        = Source.fromURL(getClass.getResource("/taxpayerData/test_individual_utr.json")).mkString
  val parsedTaxpayerDetailsJson  = Json.parse(taxpayerDetailsJson)
  val taxYear: Int               = 2014
  val taxRate                    = new TaxRateService(taxYear, applicationConfig.ratePercentages)
  val SUT: ATSRawDataTransformer = inject[ATSRawDataTransformer]

  "With base data for utr" must {

    "have the correct other income data" in {

      val sampleJson = Source.fromURL(getClass.getResource("/utr_2014.json")).mkString

      val parsedJson   = Json.parse(sampleJson)
      val calculations = ATSCalculations.make(parsedJson.as[TaxSummaryLiability], taxRate)

      val returnValue: AtsMiddleTierData =
        SUT.atsDataDTO(taxRate, calculations, parsedTaxpayerDetailsJson, "", taxYear)

      val parsedYear = returnValue.taxYear
      taxYear mustEqual parsedYear

      val parsedPayload = returnValue.income_data.get.payload.get

      parsedPayload(OtherIncome) must equal(Amount.empty)
    }

    "have the correct summed other income data" in {

      val originalJson = getClass.getResource("/utr_2014.json")

      val update = Json.obj(
        "ctnSummaryTotShareOptions"  -> Amount(10.0, "GBP"),
        "ctnSummaryTotalUklProperty" -> Amount(20.0, "GBP"),
        "ctnSummaryTotForeignIncome" -> Amount(30.0, "GBP"),
        "ctnSummaryTotTrustEstates"  -> Amount(40.0, "GBP"),
        "ctnSummaryTotalOtherIncome" -> Amount(50.0, "GBP"),
        "ctnSummaryTotalUkInterest"  -> Amount(60.0, "GBP"),
        "ctnSummaryTotForeignDiv"    -> Amount(70.0, "GBP"),
        "ctnSummaryTotalUkIntDivs"   -> Amount(80.0, "GBP"),
        "ctn4SumTotLifePolicyGains"  -> Amount(90.0, "GBP")
      )

      val transformedJson = transformation(sourceJson = originalJson, tliSlpAtsUpdate = update)
      val calculations    = ATSCalculations.make(transformedJson.as[TaxSummaryLiability], taxRate)

      val returnValue: AtsMiddleTierData =
        SUT.atsDataDTO(taxRate, calculations, parsedTaxpayerDetailsJson, "", taxYear)

      val parsedPayload = returnValue.income_data.get.payload.get

      parsedPayload(OtherIncome) must equal(new Amount(450.00, "GBP"))
    }
  }
}
