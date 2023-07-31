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

import errors.AtsError
import models.{AtsMiddleTierData, TaxSummaryLiability}
import play.api.libs.json.{JsNull, JsValue, Json}
import services.TaxRateService
import uk.gov.hmrc.http.HeaderCarrier
import utils.{AtsJsonDataUpdate, BaseSpec}

import scala.concurrent.ExecutionContext
import scala.io.{BufferedSource, Source}

class ValidateTaxpayerDataTransformerTests extends BaseSpec with AtsJsonDataUpdate {
  val dataJsonSource: BufferedSource = Source.fromURL(getClass.getResource("/utr_2014.json"))
  val dataJson: JsValue              = Json.parse(dataJsonSource.mkString)
  dataJsonSource.close()
  val taxYear: Int                   = 2014
  val taxRate                        = new TaxRateService(taxYear, applicationConfig.ratePercentages)
  val calculations: ATSCalculations  = ATSCalculations.make(dataJson.as[TaxSummaryLiability], taxRate)
  val SUT: ATSRawDataTransformer     = inject[ATSRawDataTransformer]
  implicit val ec: ExecutionContext  = inject[ExecutionContext]
  implicit val hc: HeaderCarrier     = HeaderCarrier()

  "With base data for utr" must {

    "gracefully handle a null" in {

      val originalJson = getClass.getResource("/taxpayerData/test_individual_utr.json")

      val update = Json.obj(
        "title" -> JsNull
      )

      val transformedJson = transformTaxpayerData(sourceJson = originalJson, jsonUpdateObject = update)

      val returnValue: AtsMiddleTierData =
        SUT.atsDataDTO(taxRate, calculations, transformedJson, "", taxYear)

      returnValue.taxPayerData mustBe None
      returnValue.errors mustBe Some(AtsError("title"))
    }

    "gracefully handle a missing field" in {

      val originalJsonSource =
        Source.fromURL(getClass.getResource("/taxpayerData/missing_field_taxpayer_json_utr.json"))
      val originalJson       = originalJsonSource.mkString
      originalJsonSource.close()

      val parsedJson = Json.parse(originalJson)

      val returnValue: AtsMiddleTierData =
        SUT.atsDataDTO(taxRate, calculations, parsedJson, "", taxYear)

      returnValue.taxPayerData mustBe None

      returnValue.errors mustBe Some(AtsError("forename"))
    }

    "gracefully handle incorrect value type" in {

      val originalJsonSource =
        Source.fromURL(getClass.getResource("/taxpayerData/incorrect_format_taxpayer_json_utr.json"))
      val originalJson       = originalJsonSource.mkString
      originalJsonSource.close()

      val parsedJson = Json.parse(originalJson)

      val returnValue: AtsMiddleTierData =
        SUT.atsDataDTO(taxRate, calculations, parsedJson, "", taxYear)

      returnValue.taxPayerData mustBe None
      returnValue.errors mustBe Some(AtsError("surname"))
    }
  }
}
