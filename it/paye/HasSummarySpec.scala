/*
 * Copyright 2022 HM Revenue & Customs
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

package paye

import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, ok, urlEqualTo}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.{FileHelper, IntegrationSpec}

class HasSummarySpec extends IntegrationSpec {

  val odsUrl = s"/self-assessment/individuals/$utr/annual-tax-summaries"

  val apiUrl = s"/taxs/$utr/has_summary_for_previous_period"

  "HasSummary" must {
    "return an OK when data is retrieved from ODS" in {

      server.stubFor(
        WireMock.get(urlEqualTo(odsUrl))
          .willReturn(ok(FileHelper.loadFile("odsData.json")))
      )

      val request = FakeRequest(GET, apiUrl)

      val result = route(app, request)

      result.map(status) mustBe Some(OK)
    }

    "return NOT_FOUND when ODS returns NOT_FOUND response" in {

      server.stubFor(
        WireMock.get(urlEqualTo(odsUrl))
          .willReturn(aResponse().withStatus(NOT_FOUND))
      )

      val request = FakeRequest(GET, apiUrl)

      val result = route(app, request)

      result.map(status) mustBe Some(NOT_FOUND)
    }

    "return an exception when ODS returns an empty ok" in {

      server.stubFor(
        WireMock.get(urlEqualTo(odsUrl))
          .willReturn(ok())
      )

      val request = FakeRequest(GET, apiUrl)

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
          WireMock.get(urlEqualTo(odsUrl))
            .willReturn(aResponse().withStatus(httpResponse))
        )

        val request = FakeRequest(GET, apiUrl)

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
          WireMock.get(urlEqualTo(odsUrl))
            .willReturn(aResponse().withStatus(httpResponse))
        )

        val request = FakeRequest(GET, apiUrl)

        val result = route(app, request)

        result.map(status) mustBe Some(BAD_GATEWAY)
      }
    }

    s"return an 502 when ODS is timing out" in {

      server.stubFor(
        WireMock.get(urlEqualTo(odsUrl))
          .willReturn(ok(FileHelper.loadFile("odsData.json")).withFixedDelay(10000))
      )

      val request = FakeRequest(GET, apiUrl)

      val result = route(app, request)

      result.map(status) mustBe Some(BAD_GATEWAY)
    }
  }
}
