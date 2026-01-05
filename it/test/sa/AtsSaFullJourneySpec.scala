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
import common.utils.FileHelper
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import sa.models.*

class AtsSaFullJourneySpec extends SaTestHelper {
  val taxPayerFile = "sa/taxPayerDetails.json"

  "/:UTR/:TAX_YEAR/ats-data" must {
    "return each section in the middle tier data returned including gov spending data and tax data for latest tax year" in {
      val taxYear = 2025

      def odsUrl(taxYear: Int): String = s"/self-assessment/individuals/" + utr + s"/annual-tax-summaries/$taxYear"

      def apiUrl(taxYear: Int): String = s"/taxs/$utr/$taxYear/ats-data"

      def request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, apiUrl(taxYear))
        .withHeaders((AUTHORIZATION, "Bearer 123"))
      server.stubFor(
        WireMock
          .get(urlEqualTo(odsUrl(taxYear)))
          .willReturn(ok(FileHelper.loadFile("sa/saFullJourney.json")))
      )

      val result: AtsMiddleTierData = resultToAtsData(route(app, request))
      result.income_data mustBe defined
      result.allowance_data mustBe defined
      result.capital_gains_data mustBe defined
      result.income_tax mustBe defined
      result.gov_spending mustBe defined
    }

    List(BAD_REQUEST, UNAUTHORIZED, FORBIDDEN, CONFLICT, PRECONDITION_FAILED).foreach { errStatus =>
      s"handle 4xx error response $errStatus correctly as internal server error" in {
        val taxYear = 2025

        def odsUrl(taxYear: Int): String = s"/self-assessment/individuals/" + utr + s"/annual-tax-summaries/$taxYear"

        def apiUrl(taxYear: Int): String = s"/taxs/$utr/$taxYear/ats-data"

        def request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, apiUrl(taxYear))
          .withHeaders((AUTHORIZATION, "Bearer 123"))

        server.stubFor(
          WireMock
            .get(urlEqualTo(odsUrl(taxYear)))
            .willReturn(aResponse().withStatus(errStatus))
        )

        status(route(app, request).get) mustBe INTERNAL_SERVER_ERROR
      }
    }

    "handle 4xx error response 404 correctly as internal server error" in {
      val taxYear = 2025

      def odsUrl(taxYear: Int): String = s"/self-assessment/individuals/" + utr + s"/annual-tax-summaries/$taxYear"

      def apiUrl(taxYear: Int): String = s"/taxs/$utr/$taxYear/ats-data"

      def request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, apiUrl(taxYear))
        .withHeaders((AUTHORIZATION, "Bearer 123"))

      server.stubFor(
        WireMock
          .get(urlEqualTo(odsUrl(taxYear)))
          .willReturn(aResponse().withStatus(NOT_FOUND))
      )

      status(route(app, request).get) mustBe NOT_FOUND
    }

    List(INTERNAL_SERVER_ERROR, NOT_IMPLEMENTED, BAD_GATEWAY, SERVICE_UNAVAILABLE, GATEWAY_TIMEOUT).foreach {
      errStatus =>
        s"handle 5xx error response $errStatus correctly as bad gateway" in {
          val taxYear = 2025

          def odsUrl(taxYear: Int): String = s"/self-assessment/individuals/" + utr + s"/annual-tax-summaries/$taxYear"

          def apiUrl(taxYear: Int): String = s"/taxs/$utr/$taxYear/ats-data"

          def request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, apiUrl(taxYear))
            .withHeaders((AUTHORIZATION, "Bearer 123"))

          server.stubFor(
            WireMock
              .get(urlEqualTo(odsUrl(taxYear)))
              .willReturn(aResponse().withStatus(errStatus))
          )

          status(route(app, request).get) mustBe BAD_GATEWAY
        }
    }
  }

  "/:UTR/:ENDYEAR/:NUMBEROFYEARS/ats-list" must {
    "return each section in the middle tier data returned including gov spending data and tax data for latest tax year" in {
      val taxYear = 2025

      def odsUrl(taxYear: Int): String = s"/self-assessment/individuals/" + utr + s"/annual-tax-summaries/$taxYear"

      def apiUrl(taxYear: Int): String = s"/taxs/$utr/$taxYear/2/ats-list"

      def request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, apiUrl(taxYear))
        .withHeaders((AUTHORIZATION, "Bearer 123"))
      server.stubFor(
        WireMock
          .get(urlEqualTo(odsUrl(taxYear)))
          .willReturn(ok(FileHelper.loadFile("sa/saFullJourney.json")))
      )

      server.stubFor(
        WireMock
          .get(urlEqualTo(odsUrl(taxYear - 1)))
          .willReturn(ok(FileHelper.loadFile("sa/saFullJourney.json")))
      )

      val result         = contentAsString(route(app, request).get)
      val parsedResponse = Json.parse(result).as[JsObject]
      val parsedYearList = (parsedResponse \ "atsYearList").toOption.map(x => x.as[Seq[Int]])
      parsedYearList mustBe Some(Seq(taxYear - 1, taxYear))
    }

    List(BAD_REQUEST, UNAUTHORIZED, FORBIDDEN, CONFLICT, PRECONDITION_FAILED).foreach { errStatus =>
      s"handle 4xx error response $errStatus correctly returning empty list" in {
        val taxYear = 2025

        def odsUrl(taxYear: Int): String = s"/self-assessment/individuals/" + utr + s"/annual-tax-summaries/$taxYear"

        def apiUrl(taxYear: Int): String = s"/taxs/$utr/$taxYear/2/ats-list"

        def request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, apiUrl(taxYear))
          .withHeaders((AUTHORIZATION, "Bearer 123"))

        server.stubFor(
          WireMock
            .get(urlEqualTo(odsUrl(taxYear)))
            .willReturn(aResponse().withStatus(errStatus))
        )

        server.stubFor(
          WireMock
            .get(urlEqualTo(odsUrl(taxYear - 1)))
            .willReturn(aResponse().withStatus(errStatus))
        )

        val result         = contentAsString(route(app, request).get)
        val parsedResponse = Json.parse(result).as[JsObject]
        val parsedYearList = (parsedResponse \ "atsYearList").toOption.map(x => x.as[Seq[Int]])
        parsedYearList mustBe Some(Nil)

      }
    }

    List(INTERNAL_SERVER_ERROR, NOT_IMPLEMENTED, BAD_GATEWAY, SERVICE_UNAVAILABLE, GATEWAY_TIMEOUT).foreach {
      errStatus =>
        s"handle 5xx error response $errStatus correctly as bad gateway" in {
          val taxYear = 2025

          def odsUrl(taxYear: Int): String = s"/self-assessment/individuals/" + utr + s"/annual-tax-summaries/$taxYear"

          def apiUrl(taxYear: Int): String = s"/taxs/$utr/$taxYear/2/ats-list"

          def request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, apiUrl(taxYear))
            .withHeaders((AUTHORIZATION, "Bearer 123"))

          server.stubFor(
            WireMock
              .get(urlEqualTo(odsUrl(taxYear)))
              .willReturn(aResponse().withStatus(errStatus))
          )

          server.stubFor(
            WireMock
              .get(urlEqualTo(odsUrl(taxYear - 1)))
              .willReturn(aResponse().withStatus(errStatus))
          )

          status(route(app, request).get) mustBe BAD_GATEWAY

        }
    }

    s"handle 4xx error response 404 correctly as not found" in {
      val taxYear = 2025

      def odsUrl(taxYear: Int): String = s"/self-assessment/individuals/" + utr + s"/annual-tax-summaries/$taxYear"

      def apiUrl(taxYear: Int): String = s"/taxs/$utr/$taxYear/2/ats-list"

      def request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, apiUrl(taxYear))
        .withHeaders((AUTHORIZATION, "Bearer 123"))

      server.stubFor(
        WireMock
          .get(urlEqualTo(odsUrl(taxYear)))
          .willReturn(aResponse().withStatus(NOT_FOUND))
      )

      server.stubFor(
        WireMock
          .get(urlEqualTo(odsUrl(taxYear - 1)))
          .willReturn(aResponse().withStatus(NOT_FOUND))
      )

      status(route(app, request).get) mustBe NOT_FOUND

    }

  }

  "/:UTR/has_summary_for_previous_period" must {
    "return an OK when data is retrieved from ODS" in {
      val odsUrl                                       = s"/self-assessment/individuals/$utr/annual-tax-summaries"
      val apiUrl                                       = s"/taxs/$utr/has_summary_for_previous_period"
      val request: FakeRequest[AnyContentAsEmpty.type] =
        FakeRequest(GET, apiUrl).withHeaders((AUTHORIZATION, "Bearer 123"))
      server.stubFor(
        WireMock
          .get(urlEqualTo(odsUrl))
          .willReturn(ok(FileHelper.loadFile("paye/odsData.json")))
      )

      val result = route(app, request)
      result.map(status) mustBe Some(OK)
    }

    "return NOT_FOUND when ODS returns NOT_FOUND response" in {
      val odsUrl                                       = s"/self-assessment/individuals/$utr/annual-tax-summaries"
      val apiUrl                                       = s"/taxs/$utr/has_summary_for_previous_period"
      val request: FakeRequest[AnyContentAsEmpty.type] =
        FakeRequest(GET, apiUrl).withHeaders((AUTHORIZATION, "Bearer 123"))
      server.stubFor(
        WireMock
          .get(urlEqualTo(odsUrl))
          .willReturn(aResponse().withStatus(NOT_FOUND))
      )

      val result = route(app, request)
      result.map(status) mustBe Some(NOT_FOUND)
    }

    "return an exception when ODS returns an empty ok" in {
      val odsUrl                                       = s"/self-assessment/individuals/$utr/annual-tax-summaries"
      val apiUrl                                       = s"/taxs/$utr/has_summary_for_previous_period"
      val request: FakeRequest[AnyContentAsEmpty.type] =
        FakeRequest(GET, apiUrl).withHeaders((AUTHORIZATION, "Bearer 123"))
      server.stubFor(
        WireMock
          .get(urlEqualTo(odsUrl))
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
        val odsUrl                                       = s"/self-assessment/individuals/$utr/annual-tax-summaries"
        val apiUrl                                       = s"/taxs/$utr/has_summary_for_previous_period"
        val request: FakeRequest[AnyContentAsEmpty.type] =
          FakeRequest(GET, apiUrl).withHeaders((AUTHORIZATION, "Bearer 123"))
        server.stubFor(
          WireMock
            .get(urlEqualTo(odsUrl))
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
        val odsUrl                                       = s"/self-assessment/individuals/$utr/annual-tax-summaries"
        val apiUrl                                       = s"/taxs/$utr/has_summary_for_previous_period"
        val request: FakeRequest[AnyContentAsEmpty.type] =
          FakeRequest(GET, apiUrl).withHeaders((AUTHORIZATION, "Bearer 123"))
        server.stubFor(
          WireMock
            .get(urlEqualTo(odsUrl))
            .willReturn(aResponse().withStatus(httpResponse))
        )

        val result = route(app, request)
        result.map(status) mustBe Some(BAD_GATEWAY)
      }
    }

    s"return an 502 when ODS is timing out" in {
      val odsUrl                                       = s"/self-assessment/individuals/$utr/annual-tax-summaries"
      val apiUrl                                       = s"/taxs/$utr/has_summary_for_previous_period"
      val request: FakeRequest[AnyContentAsEmpty.type] =
        FakeRequest(GET, apiUrl).withHeaders((AUTHORIZATION, "Bearer 123"))
      server.stubFor(
        WireMock
          .get(urlEqualTo(odsUrl))
          .willReturn(ok(FileHelper.loadFile("paye/odsData.json")).withFixedDelay(10000))
      )

      val result = route(app, request)
      result.map(status) mustBe Some(BAD_GATEWAY)
    }
  }
}
