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

import models.LiabilityKey.SelfEmploymentIncome
import models.{Amount, TaxSummaryLiability}
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import uk.gov.hmrc.play.test.UnitSpec
import utils._

import scala.io.Source

class SelfEmploymentTransformerTest extends UnitSpec with AtsJsonDataUpdate with GuiceOneAppPerTest {

  val taxpayerDetailsJson = Source.fromURL(getClass.getResource("/taxpayerData/test_individual_utr.json")).mkString
  val parsedTaxpayerDetailsJson = Json.parse(taxpayerDetailsJson)
  val taxYear: Int = 2014

  "With base data for utr" should {

    "have the correct self employment data" in {

      val sampleJson = Source.fromURL(getClass.getResource("/utr_2014.json")).mkString

      val parsedJson = Json.parse(sampleJson)
      val returnValue =
        ATSRawDataTransformer(parsedJson.as[TaxSummaryLiability], parsedTaxpayerDetailsJson, "", taxYear).atsDataDTO

      val parsedYear = returnValue.taxYear
      val testYear: Int = 2014
      testYear shouldEqual parsedYear

      val parsedPayload = returnValue.income_data.get.payload.get

      parsedPayload(SelfEmploymentIncome) should equal(new Amount(1100.0, "GBP"))
    }

    "have the correct summed self employment income data" in {

      val originalJson = getClass.getResource("/utr_2014.json")

      val update =
        Json.obj("ctnSummaryTotalScheduleD" -> Amount(11.0, "GBP"), "ctnSummaryTotalPartnership" -> Amount(11.0, "GBP"))

      val transformedJson = transformation(sourceJson = originalJson, tliSlpAtsUpdate = update)

      val returnValue =
        ATSRawDataTransformer(transformedJson.as[TaxSummaryLiability], parsedTaxpayerDetailsJson, "", taxYear).atsDataDTO

      val parsedYear = returnValue.taxYear
      val testYear: Int = 2014
      testYear shouldEqual parsedYear

      val parsedPayload = returnValue.income_data.get.payload.get

      parsedPayload(SelfEmploymentIncome) should equal(new Amount(22.0, "GBP"))
    }
  }
}
