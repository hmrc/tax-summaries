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

import models.LiabilityKey.SelfEmploymentIncome
import models.{Amount, AtsMiddleTierData, TaxSummaryLiability}
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.libs.json.{JsValue, Json}
import services.TaxRateService
import uk.gov.hmrc.http.HeaderCarrier
import utils._

import scala.concurrent.ExecutionContext
import scala.io.{BufferedSource, Source}

class SelfEmploymentTransformerTest extends BaseSpec with AtsJsonDataUpdate {

  val taxpayerDetailsJsonSource: BufferedSource =
    Source.fromURL(getClass.getResource("/taxpayerData/test_individual_utr.json"))
  val taxpayerDetailsJson: String               = taxpayerDetailsJsonSource.mkString
  taxpayerDetailsJsonSource.close()

  val parsedTaxpayerDetailsJson: JsValue = Json.parse(taxpayerDetailsJson)
  val taxYear: Int                       = 2014
  val taxRate                            = new TaxRateService(taxYear, applicationConfig.ratePercentages)
  val SUT: ATSRawDataTransformer         = inject[ATSRawDataTransformer]
  implicit val ec: ExecutionContext      = inject[ExecutionContext]
  implicit val hc: HeaderCarrier         = HeaderCarrier()

  "With base data for utr" must {

    "have the correct self employment data" in {

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

      parsedPayload(SelfEmploymentIncome) must equal(
        Amount(1100.0, "GBP", Some("1100.00(ctnSummaryTotalScheduleD) + 0.00(ctnSummaryTotalPartnership)"))
      )
    }

    "have the correct summed self employment income data" in {

      val originalJson = getClass.getResource("/utr_2014.json")

      val update          =
        Json.obj("ctnSummaryTotalScheduleD" -> Amount(11.0, "GBP"), "ctnSummaryTotalPartnership" -> Amount(11.0, "GBP"))

      val transformedJson = transformation(sourceJson = originalJson, tliSlpAtsUpdate = update)
      val calculations    = ATSCalculations.make(transformedJson.as[TaxSummaryLiability], taxRate)

      val returnValue: AtsMiddleTierData =
        SUT.atsDataDTO(taxRate, calculations, parsedTaxpayerDetailsJson, "", taxYear)

      val parsedYear    = returnValue.taxYear
      val testYear: Int = 2014
      testYear mustEqual parsedYear

      val parsedPayload = returnValue.income_data.get.payload.get

      parsedPayload(SelfEmploymentIncome) must equal(
        Amount(22.0, "GBP", Some("11.0(ctnSummaryTotalScheduleD) + 11.0(ctnSummaryTotalPartnership)"))
      )
    }
  }
}
