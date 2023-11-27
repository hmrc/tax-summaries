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

package sa

import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, ok, urlEqualTo}
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.time.TaxYear
import utils.{FileHelper, IntegrationSpec}

class HasSummarySpec extends IntegrationSpec {

  private def odsUrl(taxYear: Int) = s"/self-assessment/individuals/$utr/annual-tax-summaries/" + taxYear

  private val request: FakeRequest[AnyContentAsEmpty.type] = {
    val apiUrl = s"/taxs/$utr/has_summary_for_previous_period"
    FakeRequest(GET, apiUrl).withHeaders((AUTHORIZATION, "Bearer 123"))
  }

  "HasSummary" must {

    "return NOT_FOUND when ODS returns NOT_FOUND response" in {

      server.stubFor(
        WireMock
          .get(urlEqualTo(odsUrl(TaxYear.current.currentYear)))
          .willReturn(aResponse().withStatus(NOT_FOUND))
      )

      val result = route(app, request)
      result.map(status) mustBe Some(NOT_FOUND)
    }

    "return an OK with true json when data is retrieved from ODS with liability" in {
      server.stubFor(
        WireMock
          .get(urlEqualTo(odsUrl(TaxYear.current.currentYear)))
          .willReturn(ok(FileHelper.loadFile("odsSADetailData.json")))
      )

      val result  = route(app, request)
      val jsValue = contentAsJson(result.get)
      jsValue mustBe Json.obj("has_ats" -> true)
      result.map(status) mustBe Some(OK)
    }

    "return an exception when ODS returns an empty ok" in {

      server.stubFor(
        WireMock
          .get(urlEqualTo(odsUrl(TaxYear.current.currentYear)))
          .willReturn(ok())
      )

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
        server.stubFor(
          WireMock
            .get(urlEqualTo(odsUrl(TaxYear.current.currentYear)))
            .willReturn(aResponse().withStatus(httpResponse))
        )

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

        server.stubFor(
          WireMock
            .get(urlEqualTo(odsUrl(TaxYear.current.currentYear)))
            .willReturn(aResponse().withStatus(httpResponse))
        )

        val result = route(app, request)
        result.map(status) mustBe Some(BAD_GATEWAY)
      }
    }

    s"return an 502 when ODS is timing out" in {

      server.stubFor(
        WireMock
          .get(urlEqualTo(odsUrl(TaxYear.current.currentYear)))
          .willReturn(ok(FileHelper.loadFile("odsSADetailData.json")).withFixedDelay(10000))
      )

      val result = route(app, request)
      result.map(status) mustBe Some(BAD_GATEWAY)
    }
  }
}
