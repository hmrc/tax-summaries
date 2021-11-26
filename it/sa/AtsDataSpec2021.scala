/*
 * Copyright 2021 HM Revenue & Customs
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

package sa

import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, ok, urlEqualTo}
import models.LiabilityKey.SelfEmploymentIncome
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.FileHelper

class AtsDataSpec2021 extends SaTestHelper {

  def odsUrl(taxYear: Int) = s"/self-assessment/individuals/" + utr + s"/annual-tax-summaries/$taxYear"

  def apiUrl(taxYear: Int) = s"/taxs/$utr/$taxYear/ats-data"

  "HasSummary" must {

    "return an OK when data for 2021 is retrieved from ODS (TC256)" in {

      val taxYear = 2021

      server.stubFor(
        WireMock.get(urlEqualTo(odsUrl(taxYear)))
          .willReturn(ok(FileHelper.loadFile("2020-21/TC256.json")))
      )

      val request = FakeRequest(GET, apiUrl(taxYear))
      val result = route(app, request).get

      status(result) mustBe OK
      val body = Json.parse(contentAsString(result))

      val otherPensionIncome = (body \ "income_data" \ "payload" \ "other_pension_income" \ "amount").as[Double]
      otherPensionIncome mustBe 62732.0

      val otherIncome = (body \ "income_data" \ "payload" \ "other_income" \ "amount").as[Double]
      otherIncome mustBe 1057.0

    }

    "return an OK when data for 2021 is retrieved from ODS (TC13)" in {

      val taxYear = 2021

      server.stubFor(
        WireMock.get(urlEqualTo(odsUrl(taxYear)))
          .willReturn(ok(FileHelper.loadFile("2020-21/TC13.json")))
      )

      val request = FakeRequest(GET, apiUrl(taxYear))
      val result = resultToAtsData(route(app, request))


//      val otherPensionIncome = (body \ "income_data" \ "payload" \ "other_pension_income" \ "amount").as[Double]
//      otherPensionIncome mustBe 62732.0
//
//      val otherIncome = (body \ "income_data" \ "payload" \ "other_income" \ "amount").as[Double]
//      otherIncome mustBe 1057.0

//      val selfEmploymentIncome = (body \ "income_data" \ "payload" \ "self_employment_income" \ "amount").as[Double]
//      selfEmploymentIncome mustBe 21055.0

      checkResult(result, Map(SelfEmploymentIncome -> 21055.0))

    }

    "return NOT_FOUND when ODS returns NOT_FOUND response" in {

      val taxYear = 2021

      server.stubFor(
        WireMock.get(urlEqualTo(odsUrl(taxYear)))
          .willReturn(aResponse().withStatus(NOT_FOUND))
      )

      val request = FakeRequest(GET, apiUrl(taxYear))

      val result = route(app, request)

      result.map(status) mustBe Some(NOT_FOUND)
    }

    "return an exception when ODS returns an empty ok" in {

      val taxYear = 2021

      server.stubFor(
        WireMock.get(urlEqualTo(odsUrl(taxYear)))
          .willReturn(ok())
      )

      val request = FakeRequest(GET, apiUrl(taxYear))

      val result = route(app, request)

      whenReady(result.get.failed) { e =>
        e mustBe a[MismatchedInputException]
      }
    }

    List(
      IM_A_TEAPOT,
      LOCKED
    ).foreach { httpResponse =>
      s"return an $httpResponse when data is retrieved from ODS" in {
        val taxYear = 2021

        server.stubFor(
          WireMock.get(urlEqualTo(odsUrl(taxYear)))
            .willReturn(aResponse().withStatus(httpResponse))
        )

        val request = FakeRequest(GET, apiUrl(taxYear))

        val result = route(app, request)

        result.map(status) mustBe Some(INTERNAL_SERVER_ERROR)
      }
    }

    List(
      INTERNAL_SERVER_ERROR,
      BAD_GATEWAY,
      SERVICE_UNAVAILABLE
    ).foreach { httpResponse =>
      s"return an 502 when $httpResponse status is received from ODS" in {

        val taxYear = 2021

        server.stubFor(
          WireMock.get(urlEqualTo(odsUrl(taxYear)))
            .willReturn(aResponse().withStatus(httpResponse))
        )

        val request = FakeRequest(GET, apiUrl(taxYear))

        val result = route(app, request)

        result.map(status) mustBe Some(BAD_GATEWAY)
      }
    }
  }
}
