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

import models.LiabilityKey.TaxableStateBenefits
import models.{Amount, AtsMiddleTierData, TaxSummaryLiability}
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.libs.json.{JsValue, Json}
import services.TaxRateService
import utils._

import scala.concurrent.ExecutionContext
import scala.io.{BufferedSource, Source}

class TaxableStateBenefitsTransformerTest extends BaseSpec with AtsJsonDataUpdate {

  val taxpayerDetailsJsonSource: BufferedSource =
    Source.fromURL(getClass.getResource("/taxpayerData/test_individual_utr.json"))
  val taxpayerDetailsJson: String               = taxpayerDetailsJsonSource.mkString
  taxpayerDetailsJsonSource.close()

  val parsedTaxpayerDetailsJson: JsValue = Json.parse(taxpayerDetailsJson)
  val taxYear: Int                       = 2014
  val taxRate                            = new TaxRateService(taxYear, applicationConfig.ratePercentages)
  val SUT: ATSRawDataTransformer         = inject[ATSRawDataTransformer]
  implicit val ec: ExecutionContext      = inject[ExecutionContext]

  "With base data for utr" must {

    "have the correct taxable state benefits data" in {

      val sampleJsonSource = Source.fromURL(getClass.getResource("/utr_2014.json"))
      val sampleJson       = sampleJsonSource.mkString
      sampleJsonSource.close()

      val parsedJson   = Json.parse(sampleJson)
      val calculations = ATSCalculations.make(parsedJson.as[TaxSummaryLiability], taxRate)

      val returnValue: AtsMiddleTierData =
        SUT.atsDataDTO(taxRate, calculations, parsedTaxpayerDetailsJson, "", taxYear)

      val parsedYear    = returnValue.taxYear
      val testYear: Int = 2014
      testYear mustEqual parsedYear

      val parsedPayload = returnValue.income_data.get.payload.get

      parsedPayload(TaxableStateBenefits) must equal(
        new Amount(
          0.0,
          "GBP",
          Some("0.00(atsIncBenefitSuppAllowAmt) + 0.00(atsJobSeekersAllowanceAmt) + 0.00(atsOthStatePenBenefitsAmt)")
        )
      )
    }

    "have the correct summed other pension income data" in {

      val originalJson = getClass.getResource("/utr_2014.json")

      val update = Json.obj(
        "atsIncBenefitSuppAllowAmt" -> Amount(100.0, "GBP"),
        "atsJobSeekersAllowanceAmt" -> Amount(200.0, "GBP"),
        "atsOthStatePenBenefitsAmt" -> Amount(300.0, "GBP")
      )

      val transformedJson = transformation(sourceJson = originalJson, tliSlpAtsUpdate = update)
      val calculations    = ATSCalculations.make(transformedJson.as[TaxSummaryLiability], taxRate)

      val returnValue: AtsMiddleTierData =
        SUT.atsDataDTO(taxRate, calculations, parsedTaxpayerDetailsJson, "", taxYear)

      val parsedPayload = returnValue.income_data.get.payload.get
      parsedPayload(TaxableStateBenefits) must equal(
        new Amount(
          600.0,
          "GBP",
          Some("100.0(atsIncBenefitSuppAllowAmt) + 200.0(atsJobSeekersAllowanceAmt) + 300.0(atsOthStatePenBenefitsAmt)")
        )
      )
    }
  }
}
