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
import play.api.libs.json.{JsNull, JsResultException, Json}
import services.TaxRateService
import utils.{AtsJsonDataUpdate, BaseSpec}

import scala.io.Source

class ValidateATSRawDataTransformerTests extends BaseSpec with AtsJsonDataUpdate {

  val taxpayerDetailsJson        = Source.fromURL(getClass.getResource("/taxpayerData/test_individual_utr.json")).mkString
  val parsedTaxpayerDetailsJson  = Json.parse(taxpayerDetailsJson)
  val taxYear: Int               = 2014
  val taxRate                    = new TaxRateService(taxYear, applicationConfig.ratePercentages)
  val SUT: ATSRawDataTransformer = inject[ATSRawDataTransformer]

  "With base data for utr" must {

    "gracefully handle an null" ignore { // Todo fix test

      val originalJson = getClass.getResource("/utr_2014.json")

      val update = Json.obj(
        "itf4GiftsInvCharitiesAmo" -> JsNull
      )

      val transformedJson = transformation(sourceJson = originalJson, tliSlpAtsUpdate = update)
      val calculations    = ATSCalculations.make(transformedJson.as[TaxSummaryLiability], taxRate)

      val returnValue: AtsMiddleTierData =
        SUT.atsDataDTO(taxRate, calculations, parsedTaxpayerDetailsJson, "", taxYear)

      returnValue.income_data mustBe None
      returnValue.errors mustBe Some(AtsError("itf4GiftsInvCharitiesAmo"))
    }

    "gracefully handle a missing field" in {

      val originalJson = Source.fromURL(getClass.getResource("/utr_2014_field_removed.json")).mkString

      val parsedJson   = Json.parse(originalJson)
      val calculations = ATSCalculations.make(parsedJson.as[TaxSummaryLiability], taxRate)

      val returnValue: AtsMiddleTierData =
        SUT.atsDataDTO(taxRate, calculations, parsedTaxpayerDetailsJson, "", taxYear)

      returnValue.income_data mustBe None
      returnValue.errors mustBe Some(AtsError("itf4GiftsInvCharitiesAmo"))
    }

    "Invalid Json causes a Json parser exception" in {
      val originalJson = Source.fromURL(getClass.getResource("/utr_2014_incorrect_amount_format.json")).mkString

      val parsedJson = Json.parse(originalJson)
      a[JsResultException] mustBe thrownBy(
        ATSCalculations.make(parsedJson.as[TaxSummaryLiability], taxRate)
      )
    }

    "return a JSON containing taxpayer name errors" in {

      val taxpayerJson =
        Source.fromURL(getClass.getResource("/taxpayerData/incorrect_format_taxpayer_json_utr.json")).mkString
      val originalJson = Source.fromURL(getClass.getResource("/utr_2014.json")).mkString

      val parsedJson                = Json.parse(originalJson)
      val parsedTaxpayerDetailsJson = Json.parse(taxpayerJson)
      val calculations              = ATSCalculations.make(parsedJson.as[TaxSummaryLiability], taxRate)

      val returnValue: AtsMiddleTierData =
        SUT.atsDataDTO(taxRate, calculations, parsedTaxpayerDetailsJson, "", taxYear)

      returnValue.errors mustBe Some(AtsError("surname"))
    }
  }
}
