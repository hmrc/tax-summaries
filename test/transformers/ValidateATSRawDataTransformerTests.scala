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

import errors.AtsError
import models.TaxSummaryLiability
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.libs.json.{JsNull, Json}
import uk.gov.hmrc.play.test.UnitSpec
import utils.AtsJsonDataUpdate

import scala.io.Source

class ValidateATSRawDataTransformerTests extends UnitSpec with AtsJsonDataUpdate with GuiceOneAppPerTest {

  val taxpayerDetailsJson = Source.fromURL(getClass.getResource("/taxpayerData/test_individual_utr.json")).mkString
  val parsedTaxpayerDetailsJson = Json.parse(taxpayerDetailsJson)
  val taxYear: Int = 2014

  "With base data for utr" should {

    "gracefully handle a null" in {

      val originalJson = getClass.getResource("/utr_2014.json")

      val update = Json.obj(
        "itf4GiftsInvCharitiesAmo" -> JsNull
      )

      val transformedJson = transformation(sourceJson = originalJson, tliSlpAtsUpdate = update)

      val returnValue =
        ATSRawDataTransformer(transformedJson.as[TaxSummaryLiability], parsedTaxpayerDetailsJson, "", taxYear).atsDataDTO
      returnValue.income_data shouldBe None
      returnValue.errors shouldBe Some(AtsError("itf4GiftsInvCharitiesAmo"))
    }

    "gracefully handle a missing field" in {

      val originalJson = Source.fromURL(getClass.getResource("/utr_2014_field_removed.json")).mkString

      val parsedJson = Json.parse(originalJson)

      val returnValue =
        ATSRawDataTransformer(parsedJson.as[TaxSummaryLiability], parsedTaxpayerDetailsJson, "", taxYear).atsDataDTO
      returnValue.income_data shouldBe None
      returnValue.errors shouldBe Some(AtsError("itf4GiftsInvCharitiesAmo"))
    }

    "gracefully handle malformed JSON" in {

      val originalJson = Source.fromURL(getClass.getResource("/utr_2014_incorrect_amount_format.json")).mkString

      val parsedJson = Json.parse(originalJson)
      val returnValue =
        ATSRawDataTransformer(parsedJson.as[TaxSummaryLiability], parsedTaxpayerDetailsJson, "", taxYear).atsDataDTO
      returnValue.income_data shouldBe None
      returnValue.errors shouldBe Some(AtsError("ctnSummaryTotShareOptions"))
    }

    "return a JSON containing taxpayer name errors" in {

      val taxpayerJson =
        Source.fromURL(getClass.getResource("/taxpayerData/incorrect_format_taxpayer_json_utr.json")).mkString
      val originalJson = Source.fromURL(getClass.getResource("/utr_2014.json")).mkString

      val parsedJson = Json.parse(originalJson)
      val parsedTaxpayerDetailsJson = Json.parse(taxpayerJson)
      val returnValue =
        ATSRawDataTransformer(parsedJson.as[TaxSummaryLiability], parsedTaxpayerDetailsJson, "", taxYear).atsDataDTO

      returnValue.errors shouldBe Some(AtsError("surname"))
    }
  }
}
