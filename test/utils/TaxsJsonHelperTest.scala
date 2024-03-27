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

package utils

import play.api.libs.json._
import transformers.{ATSParsingException, ATSRawDataTransformer}
import utils.TestConstants._

class TaxsJsonHelperTest extends BaseSpec {
  private val prevTaxYear = 2022
  private val taxYear     = 2023

  val aTSRawDataTransformer = inject[ATSRawDataTransformer]

  class SetUp extends TaxsJsonHelper(applicationConfig, aTSRawDataTransformer)

  "hasAtsForPreviousPeriod" must {

    "return true when json response has non empty annual tax summaries data" in new SetUp {

      val rawJson = Json.parse(s"""
                                 | {
                                 |   "annualTaxSummaries" : [
                                 |   { "taxYearEnd" : $prevTaxYear },
                                 |   { "taxYearEnd" : $taxYear }
                                 |   ]
                                 | }
        """.stripMargin)

      val result = hasAtsForPreviousPeriod(rawJson)

      result mustBe true
    }

    "return false when json response has no annual tax summaries data" in new SetUp {

      val rawJson = Json.parse("""
                                 | {
                                 |   "annualTaxSummaries" : []
                                 | }
        """.stripMargin)

      val result = hasAtsForPreviousPeriod(rawJson)

      result mustBe false
    }

    "return false for badly formed json" in new SetUp {

      val rawJson = Json.parse(s"""
                                 | {
                                 |   "annualTaxSummaries" : [
                                 |   { "userName" : "" }
                                 |   ],
                                 |   "taxYearEnd" : $prevTaxYear
                                 | }
        """.stripMargin)

      val result = hasAtsForPreviousPeriod(rawJson)

      result mustBe false
    }
  }

  "createTaxYearJson" must {

    "return a jsvalue with correct data when passed correct format" in new SetUp {

      val rawJson = Json.parse(s"""
                                 | {
                                 |   "annualTaxSummaries" : [
                                 |   { "taxYearEnd" : $prevTaxYear },
                                 |   { "taxYearEnd" : $taxYear }
                                 |   ]
                                 | }
        """.stripMargin)

      val rawTaxpayerJson = Json.parse("""
                                         |{
                                         |  "name": {
                                         |    "title": "Mr",
                                         |    "forename": "forename",
                                         |    "surname": "surname"
                                         |  }
                                         | }
        """.stripMargin)

      val result = createTaxYearJson(rawJson, testUtr, rawTaxpayerJson)

      result \ "utr" mustBe JsDefined(JsString(testUtr))
      result \ "taxPayer" mustBe JsDefined(
        Json.parse("""{"taxpayer_name":{"title":"Mr","forename":"forename","surname":"surname"}}""")
      )
      result \ "atsYearList" mustBe JsDefined(Json.parse(s"[$prevTaxYear, $taxYear]"))
    }

    "return an exception when passed badly formed json" in new SetUp {

      val rawJson = Json.parse(s"""
                                 | {
                                 |   "annualTaxSummaries" : [
                                 |   { "taxYearEnd" : $taxYear }
                                 |   ]
                                 | }
        """.stripMargin)

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
        createTaxYearJson(rawJson, testUtr, rawTaxpayerJson)
      }
    }

  }

}
