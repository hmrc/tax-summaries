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
import com.github.tomakehurst.wiremock.http.Fault
import org.scalatest.concurrent.IntegrationPatience
import play.api.Application
import play.api.http.Status._
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames, HttpClient, RequestId, SessionId}
import utils.TestConstants.testNino
import utils.{BaseSpec, JsonUtil, WireMockHelper}

import scala.concurrent.ExecutionContext

class NPSConnectorTest extends BaseSpec with WireMockHelper {

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .configure(
        "microservice.services.tax-summaries-hod.port" -> server.port()
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

  "connectToPayeTaxSummary" should {

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

      result.json shouldBe Json.parse(expectedNpsResponse)

      server.verify(
        getRequestedFor(urlEqualTo(url))
          .withHeader("Environment", equalTo("local"))
          .withHeader("Authorization", equalTo("Bearer local"))
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

      result.json shouldBe Json.parse(expectedNpsResponse)
    }

    "return BAD_REQUEST response in case of Bad request from NPS" in new NPSConnectorSetUp {

      val url = s"/individuals/annual-tax-summary/" + testNinoWithoutSuffix + "/" + invalidTaxYear

      server.stubFor(
        get(urlEqualTo(url)).willReturn(
          aResponse()
            .withStatus(400)
            .withBody("Bad Request"))
      )

      val result = connectToPayeTaxSummary(testNino, invalidTaxYear).futureValue

      result.status shouldBe BAD_REQUEST
    }

    "return NOT_FOUND response in case of Not found from NPS" in new NPSConnectorSetUp {

      val url = s"/individuals/annual-tax-summary/" + testNinoWithoutSuffix + "/" + invalidTaxYear

      server.stubFor(
        get(urlEqualTo(url)).willReturn(
          aResponse()
            .withStatus(404)
            .withBody("Not Found"))
      )

      val result = connectToPayeTaxSummary(testNino, invalidTaxYear).futureValue

      result.status shouldBe NOT_FOUND
    }

    "return INTERNAL_SERVER_ERROR response in case of Exception from NPS" in new NPSConnectorSetUp {

      val url = s"/individuals/annual-tax-summary/" + testNinoWithoutSuffix + "/" + invalidTaxYear

      server.stubFor(
        get(urlEqualTo(url)).willReturn(
          aResponse()
            .withFault(Fault.EMPTY_RESPONSE)
            .withBody("File not found"))
      )

      val result = connectToPayeTaxSummary(testNino, invalidTaxYear).futureValue

      result.status shouldBe INTERNAL_SERVER_ERROR
    }

    "return INTERNAL_SERVER_ERROR response in case of 503 from NPS" in new NPSConnectorSetUp {

      val url = s"/individuals/annual-tax-summary/" + testNinoWithoutSuffix + "/" + invalidTaxYear

      server.stubFor(
        get(urlEqualTo(url)).willReturn(
          aResponse()
            .withStatus(503)
            .withBody("SERVICE_UNAVAILABLE"))
      )

      val result = connectToPayeTaxSummary(testNino, invalidTaxYear).futureValue

      result.status shouldBe INTERNAL_SERVER_ERROR
    }
  }
}
