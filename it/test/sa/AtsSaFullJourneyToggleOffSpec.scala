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

import cats.instances.future.*
import cats.data.EitherT
import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, ok, urlEqualTo}
import common.models.admin.SaDetailsFromHipToggle
import common.utils.FileHelper
import org.mockito.Mockito.when
import play.api.Application
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import sa.models.*
import uk.gov.hmrc.mongoFeatureToggles.model.FeatureFlag

class AtsSaFullJourneyToggleOffSpec extends SaTestHelper {
  override val taxYear = 2025
  val taxPayerFile     = "sa/taxPayerDetails.json"

  private val apiUrlAtsData: String = s"/taxs/$utr/$taxYear/ats-data"
  private val apiUrlAtsList: String = s"/taxs/$utr/$taxYear/2/ats-list"

  private val odsUrlData: String             = s"/self-assessment/individuals/$utr/annual-tax-summaries/$taxYear"
  private val odsUrlDataPreviousYear: String = s"/self-assessment/individuals/$utr/annual-tax-summaries/${taxYear - 1}"
  private val odsUrlList                     = s"/self-assessment/individuals/$utr/annual-tax-summaries"
  private val apiUrlHasSummary               = s"/taxs/$utr/has_summary_for_previous_period"

  private def getRequest(url: String): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, url).withHeaders((AUTHORIZATION, "Bearer 123"))

  private lazy val appn: Application = fakeApplication()
  override def beforeEach(): Unit = {
    super.beforeEach()

    when(mockFeatureFlagService.getAsEitherT(org.mockito.ArgumentMatchers.eq(SaDetailsFromHipToggle))) thenReturn
      EitherT.rightT(FeatureFlag(SaDetailsFromHipToggle, isEnabled = false))

    ()
    
  }
  s"GET on $apiUrlAtsData"    must {
    "return each section in the middle tier data returned including gov spending data and tax data for latest tax year" in {
      server.stubFor(
        WireMock
          .get(urlEqualTo(odsUrlData))
          .willReturn(ok(FileHelper.loadFile("sa/saFullJourneyAtsData.json")))
      )

      val result: AtsMiddleTierData = resultToAtsData(route(appn, getRequest(apiUrlAtsData)))
      result.income_data mustBe defined
      result.allowance_data mustBe defined
      result.capital_gains_data mustBe defined
      result.income_tax mustBe defined
      result.gov_spending mustBe defined
    }

    List(BAD_REQUEST, UNAUTHORIZED, FORBIDDEN, CONFLICT, PRECONDITION_FAILED).foreach { errStatus =>
      s"handle 4xx error response $errStatus correctly as internal server error" in {
        server.stubFor(
          WireMock
            .get(urlEqualTo(odsUrlData))
            .willReturn(aResponse().withStatus(errStatus))
        )

        status(route(appn, getRequest(apiUrlAtsData)).get) mustBe INTERNAL_SERVER_ERROR
      }
    }

    "handle 4xx error response 404 correctly as internal server error" in {
      server.stubFor(
        WireMock
          .get(urlEqualTo(odsUrlData))
          .willReturn(aResponse().withStatus(NOT_FOUND))
      )

      status(route(appn, getRequest(apiUrlAtsData)).get) mustBe NOT_FOUND
    }

    List(INTERNAL_SERVER_ERROR, NOT_IMPLEMENTED, BAD_GATEWAY, SERVICE_UNAVAILABLE, GATEWAY_TIMEOUT).foreach {
      errStatus =>
        s"handle 5xx error response $errStatus correctly as bad gateway" in {
          server.stubFor(
            WireMock
              .get(urlEqualTo(odsUrlData))
              .willReturn(aResponse().withStatus(errStatus))
          )

          status(route(appn, getRequest(apiUrlAtsData)).get) mustBe BAD_GATEWAY
        }
    }
  }

  s"GET on $apiUrlAtsList"    must {
    "return each section in the middle tier data returned including gov spending data and tax data for latest tax year" in {
      server.stubFor(
        WireMock
          .get(urlEqualTo(odsUrlData))
          .willReturn(ok(FileHelper.loadFile("sa/saFullJourneyAtsData.json")))
      )

      server.stubFor(
        WireMock
          .get(urlEqualTo(odsUrlDataPreviousYear))
          .willReturn(ok(FileHelper.loadFile("sa/saFullJourneyAtsData.json")))
      )

      val result         = contentAsString(route(appn, getRequest(apiUrlAtsList)).get)
      val parsedResponse = Json.parse(result).as[JsObject]
      val parsedYearList = (parsedResponse \ "atsYearList").toOption.map(x => x.as[Seq[Int]])
      parsedYearList mustBe Some(Seq(taxYear - 1, taxYear))
    }

    List(BAD_REQUEST, UNAUTHORIZED, FORBIDDEN, CONFLICT, PRECONDITION_FAILED).foreach { errStatus =>
      s"handle 4xx error response $errStatus correctly returning empty list" in {
        server.stubFor(
          WireMock
            .get(urlEqualTo(odsUrlData))
            .willReturn(aResponse().withStatus(errStatus))
        )

        server.stubFor(
          WireMock
            .get(urlEqualTo(odsUrlDataPreviousYear))
            .willReturn(aResponse().withStatus(errStatus))
        )

        val result         = contentAsString(route(appn, getRequest(apiUrlAtsList)).get)
        val parsedResponse = Json.parse(result).as[JsObject]
        val parsedYearList = (parsedResponse \ "atsYearList").toOption.map(x => x.as[Seq[Int]])
        parsedYearList mustBe Some(Nil)

      }
    }

    List(INTERNAL_SERVER_ERROR, NOT_IMPLEMENTED, BAD_GATEWAY, SERVICE_UNAVAILABLE, GATEWAY_TIMEOUT).foreach {
      errStatus =>
        s"handle 5xx error response $errStatus correctly as bad gateway" in {
          server.stubFor(
            WireMock
              .get(urlEqualTo(odsUrlData))
              .willReturn(aResponse().withStatus(errStatus))
          )

          server.stubFor(
            WireMock
              .get(urlEqualTo(odsUrlDataPreviousYear))
              .willReturn(aResponse().withStatus(errStatus))
          )

          status(route(appn, getRequest(apiUrlAtsList)).get) mustBe BAD_GATEWAY

        }
    }

    s"handle 4xx error response 404 correctly as not found" in {
      server.stubFor(
        WireMock
          .get(urlEqualTo(odsUrlData))
          .willReturn(aResponse().withStatus(NOT_FOUND))
      )

      server.stubFor(
        WireMock
          .get(urlEqualTo(odsUrlDataPreviousYear))
          .willReturn(aResponse().withStatus(NOT_FOUND))
      )

      status(route(appn, getRequest(apiUrlAtsList)).get) mustBe NOT_FOUND
    }
  }

  s"GET on $apiUrlHasSummary" must {
    "return an OK when data is retrieved from ODS" in {
      server.stubFor(
        WireMock
          .get(urlEqualTo(odsUrlList))
          .willReturn(ok(FileHelper.loadFile("sa/saFullJourneyAtsListData.json")))
      )

      val result = route(appn, getRequest(apiUrlHasSummary))
      result.map(status) mustBe Some(OK)
    }

    "return NOT_FOUND when ODS returns NOT_FOUND response" in {
      server.stubFor(
        WireMock
          .get(urlEqualTo(odsUrlList))
          .willReturn(aResponse().withStatus(NOT_FOUND))
      )

      val result = route(appn, getRequest(apiUrlHasSummary))
      result.map(status) mustBe Some(NOT_FOUND)
    }

    "return an exception when ODS returns an empty ok" in {
      server.stubFor(
        WireMock
          .get(urlEqualTo(odsUrlList))
          .willReturn(ok())
      )

      val result = route(appn, getRequest(apiUrlHasSummary))

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
            .get(urlEqualTo(odsUrlList))
            .willReturn(aResponse().withStatus(httpResponse))
        )

        val result = route(appn, getRequest(apiUrlHasSummary))
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
            .get(urlEqualTo(odsUrlList))
            .willReturn(aResponse().withStatus(httpResponse))
        )

        val result = route(appn, getRequest(apiUrlHasSummary))
        result.map(status) mustBe Some(BAD_GATEWAY)
      }
    }

    s"return an 502 when ODS is timing out" in {
      server.stubFor(
        WireMock
          .get(urlEqualTo(odsUrlList))
          .willReturn(ok(FileHelper.loadFile("sa/saFullJourneyAtsListData.json")).withFixedDelay(10000))
      )

      val result = route(appn, getRequest(apiUrlHasSummary))
      result.map(status) mustBe Some(BAD_GATEWAY)
    }
  }
}
