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

package paye.connectors

import cats.data.EitherT
import com.github.tomakehurst.wiremock.client.WireMock.*
import common.connectors.HttpClientResponse
import common.models.admin.PayeDetailsFromIfToggle
import common.utils.TestConstants.testNino
import common.utils.{BaseSpec, JsonUtil, WireMockHelper}
import org.mockito.Mockito.{reset, when}
import play.api.Application
import play.api.http.Status.{BAD_GATEWAY, IM_A_TEAPOT, OK, SERVICE_UNAVAILABLE}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames, HttpResponse, RequestId, SessionId, UpstreamErrorResponse}
import uk.gov.hmrc.mongoFeatureToggles.model.FeatureFlag

import scala.concurrent.{ExecutionContext, Future}

class NPSConnectorTest extends BaseSpec with WireMockHelper {
  implicit lazy val ec: ExecutionContext      =
    scala.concurrent.ExecutionContext.global // TODO: remove lazy keyword when Caching spec is done.
  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .configure(
        "microservice.services.if-hod.port"               -> server.port(),
        "microservice.services.if-hod.env"                -> "if-env",
        "microservice.services.if-hod.originatorId"       -> "if-origin",
        "microservice.services.if-hod.authorizationToken" -> "if-bearer",
        "microservice.services.tax-summaries-hod.host"    -> "127.0.0.1"
      )
      .build()

  private val currentYear = 2020

  val sessionId = "testSessionId"
  val requestId = "testRequestId"

  override implicit lazy val hc: HeaderCarrier = HeaderCarrier(
    sessionId = Some(SessionId(sessionId)),
    requestId = Some(RequestId(requestId))
  )

  private val testNinoWithoutSuffix = testNino.take(8)

  lazy val httpClientResponse: HttpClientResponse = inject[HttpClientResponse]

  class NPSConnectorSetUp
      extends NpsConnector(
        app.injector.instanceOf[HttpClientV2],
        applicationConfig,
        httpClientResponse,
        mockFeatureFlagService
      )(
        app.injector.instanceOf[ExecutionContext]
      )
      with JsonUtil

  override def beforeEach(): Unit = {
    super.beforeEach()
    server.resetAll()
    reset(mockFeatureFlagService)

    val _ = when(
      mockFeatureFlagService.getAsEitherT[UpstreamErrorResponse](
        org.mockito.ArgumentMatchers.eq(PayeDetailsFromIfToggle)
      )
    ) thenReturn EitherT.pure[Future, UpstreamErrorResponse](
      FeatureFlag(PayeDetailsFromIfToggle, isEnabled = true)
    )
  }

  "connectToPayeTaxSummary" must {

    "return successful response when provided suffix" in new NPSConnectorSetUp {

      val expectedNpsResponse: String = load("/paye/paye_annual_tax_summary.json")
      val url: String                 = s"/individuals/annual-tax-summary/" + testNinoWithoutSuffix + "/" + currentYear

      server.stubFor(
        get(urlEqualTo(url)).willReturn(
          aResponse()
            .withStatus(OK)
            .withBody(expectedNpsResponse)
        )
      )

      val result: Either[UpstreamErrorResponse, HttpResponse] =
        connectToPayeTaxSummary(testNino, currentYear).value.futureValue

      result mustBe a[Right[_, _]]
      result.getOrElse(HttpResponse(IM_A_TEAPOT, "")).json mustBe Json.parse(expectedNpsResponse)

      server.verify(
        getRequestedFor(urlEqualTo(url))
          .withHeader("Environment", equalTo("if-env"))
          .withHeader(HeaderNames.authorisation, equalTo("Bearer if-bearer"))
          .withHeader(HeaderNames.xSessionId, equalTo(sessionId))
          .withHeader(HeaderNames.xRequestId, equalTo(requestId))
          .withHeader("OriginatorId", equalTo("if-origin"))
          .withHeader(
            "CorrelationId",
            matching("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}")
          )
      )
    }

    "return successful response when NOT provided suffix" in new NPSConnectorSetUp {

      val expectedNpsResponse: String = load("/paye/paye_annual_tax_summary.json")
      val url: String                 = s"/individuals/annual-tax-summary/" + testNinoWithoutSuffix + "/" + currentYear

      server.stubFor(
        get(urlEqualTo(url)).willReturn(
          aResponse()
            .withStatus(OK)
            .withBody(expectedNpsResponse)
        )
      )

      val result: Either[UpstreamErrorResponse, HttpResponse] = connectToPayeTaxSummary(
        NINO = testNinoWithoutSuffix,
        TAX_YEAR = currentYear
      ).value.futureValue

      result mustBe a[Right[_, _]]
      result.getOrElse(HttpResponse(IM_A_TEAPOT, "")).json mustBe Json.parse(expectedNpsResponse)
    }

    "return UpstreamErrorResponse" when {
      List(400, 401, 403, 404, 409, 412, 429, 500, 501, 502, 503, 504).foreach { status =>
        s"a response with status $status is received" in new NPSConnectorSetUp {
          val url: String = s"/individuals/annual-tax-summary/" + testNinoWithoutSuffix + "/" + currentYear

          server.stubFor(
            get(urlEqualTo(url)).willReturn(
              aResponse()
                .withStatus(status)
                .withBody("")
            )
          )

          val result: Future[Either[UpstreamErrorResponse, HttpResponse]] = connectToPayeTaxSummary(
            NINO = testNino,
            TAX_YEAR = currentYear
          ).value

          whenReady(result) { res =>
            res mustBe a[Left[_, _]]
          }
        }
      }
    }

    "return INTERNAL_SERVER_ERROR response in case of a timeout exception from http verbs" in new NPSConnectorSetUp {

      val url: String                 = s"/individuals/annual-tax-summary/" + testNinoWithoutSuffix + "/" + currentYear
      val expectedNpsResponse: String = load("/paye/paye_annual_tax_summary.json")

      server.stubFor(
        get(urlEqualTo(url)).willReturn(
          aResponse()
            .withStatus(OK)
            .withBody(expectedNpsResponse)
            .withFixedDelay(10000)
        )
      )

      val result: Either[UpstreamErrorResponse, HttpResponse] =
        connectToPayeTaxSummary(testNino, currentYear).value.futureValue
      result mustBe a[Left[_, _]]

      val error: UpstreamErrorResponse = result.swap.getOrElse(UpstreamErrorResponse("", IM_A_TEAPOT))
      error.statusCode mustBe BAD_GATEWAY
      error.reportAs mustBe BAD_GATEWAY
    }

    "return INTERNAL_SERVER_ERROR response in case of 503 from NPS" in new NPSConnectorSetUp {

      val url: String = s"/individuals/annual-tax-summary/" + testNinoWithoutSuffix + "/" + currentYear

      server.stubFor(
        get(urlEqualTo(url)).willReturn(
          aResponse()
            .withStatus(SERVICE_UNAVAILABLE)
            .withBody("SERVICE_UNAVAILABLE")
        )
      )

      val result: Either[UpstreamErrorResponse, HttpResponse] =
        connectToPayeTaxSummary(testNino, currentYear).value.futureValue
      result mustBe a[Left[_, _]]

      val error: UpstreamErrorResponse = result.swap.getOrElse(UpstreamErrorResponse("", IM_A_TEAPOT))
      error.statusCode mustBe SERVICE_UNAVAILABLE
      error.reportAs mustBe BAD_GATEWAY
    }
  }
}
