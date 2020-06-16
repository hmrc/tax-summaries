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

package utils

import models.ODSModels.{AnnualTaxSummary, SelfAssessmentList}
import transformers.ATSParsingException
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import play.api.libs.json._
import uk.gov.hmrc.play.test.UnitSpec
import utils.TestConstants._

class TaxsJsonHelperTest extends UnitSpec with MockitoSugar with ScalaFutures {

  val populatedSaList: SelfAssessmentList = SelfAssessmentList(
    List(
      AnnualTaxSummary(Some(2014), List.empty),
      AnnualTaxSummary(Some(2015), List.empty)
    ),
    List.empty)

  "hasAtsForPreviousPeriod" should {

    "return true when json response has non empty annual tax summaries data" in new TaxsJsonHelper {

      val result = hasAtsForPreviousPeriod(populatedSaList)

      result shouldBe true
    }

    "return false when json response has no annual tax summaries data" in new TaxsJsonHelper {

      val list: SelfAssessmentList = SelfAssessmentList(
        List(
          AnnualTaxSummary(None, List.empty)
        ),
        List.empty)

      val result = hasAtsForPreviousPeriod(list)

      result shouldBe false
    }

    "return false when json response has partial annual tax summaries data" in new TaxsJsonHelper {

      val list: SelfAssessmentList = SelfAssessmentList(
        List(
          AnnualTaxSummary(None, List.empty),
          AnnualTaxSummary(Some(2015), List.empty)
        ),
        List.empty)

      val result = hasAtsForPreviousPeriod(list)

      result shouldBe false
    }
  }

  "createTaxYearJson" should {

    "return a jsvalue with correct data when passed correct format" in new TaxsJsonHelper {

      val rawTaxpayerJson = Json.parse("""
                                         |{
                                         |  "name": {
                                         |    "title": "Mr",
                                         |    "forename": "forename",
                                         |    "surname": "surname"
                                         |  }
                                         | }
        """.stripMargin)

      val result = createTaxYearJson(populatedSaList, testUtr, rawTaxpayerJson)

      result \ "utr" shouldBe JsDefined(JsString(testUtr))
      result \ "taxPayer" shouldBe JsDefined(
        Json.parse("""{"taxpayer_name":{"title":"Mr","forename":"forename","surname":"surname"}}"""))
      result \ "atsYearList" shouldBe JsDefined(Json.parse("[2014, 2015]"))
    }

    "return an exception when passed badly formed json" in new TaxsJsonHelper {

      val rawTaxpayerJson = Json.parse("""
                                         |{
                                         |  "name": {
                                         |    "title": "Mr"
                                         |  },
                                         |  "forename": "forename",
                                         |  "surname": "surname"
                                         |}
        """.stripMargin)

      intercept[ATSParsingException] {
        createTaxYearJson(populatedSaList, testUtr, rawTaxpayerJson)
      }
    }
  }
}
