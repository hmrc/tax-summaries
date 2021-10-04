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

package connectors

import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.Inside.inside
import play.api.Application
import play.api.http.Status.{NOT_FOUND, _}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames, HttpClient, RequestId, SessionId, UpstreamErrorResponse}
import utils.TestConstants.testNino
import utils.{BaseSpec, JsonUtil, WireMockHelper}

import scala.concurrent.ExecutionContext

class NPSConnectorTest extends BaseSpec with WireMockHelper {

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .configure(
        "microservice.services.tax-summaries-hod.port" -> server.port(),
        "microservice.services.tax-summaries-hod.host" -> "127.0.0.1",
        "play.ws.timeout.request"                      -> "1000ms",
        "play.ws.timeout.connection"                   -> "500ms"
      )
      .build()

  private val currentYear = 2018
  private val invalidTaxYear = 201899

  val sessionId = "testSessionId"
  val requestId = "testRequestId"

  implicit val hc: HeaderCarrier = HeaderCarrier(
    sessionId = Some(SessionId(sessionId)),
    requestId = Some(RequestId(requestId))
  )

  private val testNinoWithoutSuffix = testNino.take(8)

  class NPSConnectorSetUp
      extends NpsConnector(app.injector.instanceOf[HttpClient], applicationConfig)(
        app.injector.instanceOf[ExecutionContext]) with JsonUtil

  "connectToPayeTaxSummary" must {

    "return successful response when provided suffix" in new NPSConnectorSetUp {

      val expectedNpsResponse: String = load("/paye_annual_tax_summary.json")
      val url = s"/individuals/annual-tax-summary/" + testNinoWithoutSuffix + "/" + currentYear

      server.stubFor(
        get(urlEqualTo(url)).willReturn(
          aResponse()
            .withStatus(OK)
            .withBody(expectedNpsResponse))
      )

      val result = connectToPayeTaxSummary(testNino, currentYear).futureValue

      result.right.get.json mustBe Json.parse(expectedNpsResponse)

      server.verify(
        getRequestedFor(urlEqualTo(url))
          .withHeader("Environment", equalTo("local"))
          .withHeader(HeaderNames.authorisation, equalTo("Bearer local"))
          .withHeader(HeaderNames.xSessionId, equalTo(sessionId))
          .withHeader(HeaderNames.xRequestId, equalTo(requestId))
          .withHeader(
            "CorrelationId",
            matching("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}"))
      )
    }

    "return successful response when NOT provided suffix" in new NPSConnectorSetUp {

      val expectedNpsResponse: String = load("/paye_annual_tax_summary.json")
      val url = s"/individuals/annual-tax-summary/" + testNinoWithoutSuffix + "/" + currentYear

      server.stubFor(
        get(urlEqualTo(url)).willReturn(
          aResponse()
            .withStatus(OK)
            .withBody(expectedNpsResponse))
      )

      val result = connectToPayeTaxSummary(testNinoWithoutSuffix, currentYear).futureValue

      result.right.get.json mustBe Json.parse(expectedNpsResponse)
    }

    List(
      NOT_FOUND   -> INTERNAL_SERVER_ERROR -> "Not found from NPS",
      BAD_REQUEST -> INTERNAL_SERVER_ERROR -> "Bad request from NPS"
    ).foreach {
      case ((statusCode, reportAs), description) =>
        s"return Left[UpstreamErrorResponse] with reportAs $reportAs in case of $description" in new NPSConnectorSetUp {

          val url = s"/individuals/annual-tax-summary/" + testNinoWithoutSuffix + "/" + invalidTaxYear

          server.stubFor(
            get(urlEqualTo(url)).willReturn(
              aResponse()
                .withStatus(statusCode)
                .withBody(""))
          )

          val result = connectToPayeTaxSummary(testNino, invalidTaxYear).futureValue

          inside(result.left.get) {
            case UpstreamErrorResponse(_, status, reportedAs, _) =>
              status mustBe statusCode
              reportedAs mustBe reportAs
          }
        }
    }

    "return INTERNAL_SERVER_ERROR response in case of a timeout exception from http verbs" in new NPSConnectorSetUp {

      val url = s"/individuals/annual-tax-summary/" + testNinoWithoutSuffix + "/" + invalidTaxYear
      val expectedNpsResponse: String = load("/paye_annual_tax_summary.json")

      server.stubFor(
        get(urlEqualTo(url)).willReturn(
          aResponse()
            .withStatus(OK)
            .withBody(expectedNpsResponse)
            .withFixedDelay(30000))
      )

      val result = connectToPayeTaxSummary(testNino, invalidTaxYear).futureValue

      inside(result.left.get) {
        case UpstreamErrorResponse(_, status, reportedAs, _) =>
          status mustBe BAD_GATEWAY
          reportedAs mustBe BAD_GATEWAY
      }
    }

    "return INTERNAL_SERVER_ERROR response in case of 503 from NPS" in new NPSConnectorSetUp {

      val url = s"/individuals/annual-tax-summary/" + testNinoWithoutSuffix + "/" + invalidTaxYear
      val serviceUnavailable = SERVICE_UNAVAILABLE

      server.stubFor(
        get(urlEqualTo(url)).willReturn(
          aResponse()
            .withStatus(serviceUnavailable)
            .withBody("SERVICE_UNAVAILABLE"))
      )

      val result = connectToPayeTaxSummary(testNino, invalidTaxYear).futureValue

      inside(result.left.get) {
        case UpstreamErrorResponse(_, status, reportedAs, _) =>
          status mustBe serviceUnavailable
          reportedAs mustBe BAD_GATEWAY
      }
    }
  }
}
