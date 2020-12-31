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

package connectors

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, get, urlEqualTo}
import com.github.tomakehurst.wiremock.http.Fault
import com.kenshoo.play.metrics.Metrics
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReads}
import org.mockito.Mockito._
import org.mockito.{ArgumentCaptor, Matchers, Mockito}
import uk.gov.hmrc.http.logging.Authorization
import utils.TestConstants.testNino
import utils.{ConnectorBaseSpec, JsonUtil, TestMetrics}

import scala.concurrent.{ExecutionContext, Future}

class NPSConnectorTest extends ConnectorBaseSpec with JsonUtil {

  implicit val hc = HeaderCarrier()
  private val currentYear = 2018
  private val invalidTaxYear = 201899

  private val testNinoWithoutSuffix = testNino.take(8)

  lazy val connector = inject[NpsConnector]

  "connectToPayeTaxSummary" should {

    "return successful response when provided suffix" in {
      val expectedNpsResponse: String = load("/paye_annual_tax_summary.json")
      val url = s"/individuals/annual-tax-summary/" + testNinoWithoutSuffix + "/" + currentYear

      server.stubFor(
        get(urlEqualTo(url)).willReturn(
          aResponse()
            .withStatus(OK)
            .withBody(expectedNpsResponse))
      )

      val result = connector.connectToPayeTaxSummary(testNino, currentYear).futureValue

      result.json shouldBe Json.parse(expectedNpsResponse)
    }

    "return successful response when NOT provided suffix" in {
      val expectedNpsResponse: String = load("/paye_annual_tax_summary.json")
      val url = s"/individuals/annual-tax-summary/" + testNinoWithoutSuffix + "/" + currentYear

      server.stubFor(
        get(urlEqualTo(url)).willReturn(
          aResponse()
            .withStatus(OK)
            .withBody(expectedNpsResponse))
      )

      val result = connector.connectToPayeTaxSummary(testNinoWithoutSuffix, currentYear).futureValue

      result.json shouldBe Json.parse(expectedNpsResponse)
    }

    "return BAD_REQUEST response in case of Bad request from NPS" in {
      val url = s"/individuals/annual-tax-summary/" + testNinoWithoutSuffix + "/" + invalidTaxYear

      server.stubFor(
        get(urlEqualTo(url)).willReturn(
          aResponse()
            .withStatus(400)
            .withBody("Bad Request"))
      )

      val result = connector.connectToPayeTaxSummary(testNino, invalidTaxYear).futureValue

      result.status shouldBe BAD_REQUEST
    }

    "return NOT_FOUND response in case of Not found from NPS" in {
      val url = s"/individuals/annual-tax-summary/" + testNinoWithoutSuffix + "/" + invalidTaxYear

      server.stubFor(
        get(urlEqualTo(url)).willReturn(
          aResponse()
            .withStatus(404)
            .withBody("Not Found"))
      )

      val result = connector.connectToPayeTaxSummary(testNino, invalidTaxYear).futureValue

      result.status shouldBe NOT_FOUND
    }

    "return INTERNAL_SERVER_ERROR response in case of Exception from NPS" in {
      val url = s"/individuals/annual-tax-summary/" + testNinoWithoutSuffix + "/" + invalidTaxYear

      server.stubFor(
        get(urlEqualTo(url)).willReturn(
          aResponse()
            .withFault(Fault.EMPTY_RESPONSE)
            .withBody("File not found"))
      )

      val result = connector.connectToPayeTaxSummary(testNino, invalidTaxYear).futureValue

      result.status shouldBe INTERNAL_SERVER_ERROR
    }
  }

  "headers" should {
    "contain additional header tracking measures" in {
      val metrics: Metrics = new TestMetrics
      val http = mock[HttpClient]
      val connector = new NpsConnector(http, applicationConfig, metrics)

      when(
        http.GET[JsValue](Matchers.any[String])(
          Matchers.any[HttpReads[JsValue]],
          Matchers.any[HeaderCarrier],
          Matchers.any[ExecutionContext]))
        .thenReturn(Future.successful(mock[JsValue]))

      val eventCaptor = ArgumentCaptor.forClass(classOf[HeaderCarrier])

      connector.connectToPayeTaxSummary(testNino, currentYear).futureValue

      Mockito.verify(http).GET(Matchers.any())(Matchers.any(), eventCaptor.capture(), Matchers.any())

      val newHeaders = eventCaptor.getValue

      newHeaders.headers.exists(_._1 == "CorrelationId") shouldBe true
      newHeaders.headers.exists(_._1 == "X-Session-ID") shouldBe true
      newHeaders.headers.exists(_._1 == "X-Request-ID") shouldBe true
      newHeaders.headers.contains("Environment"  -> applicationConfig.environment) shouldBe true
      newHeaders.headers.contains("OriginatorId" -> applicationConfig.originatorId) shouldBe true
      newHeaders.authorization.contains(Authorization(applicationConfig.authorization)) shouldBe true
    }
  }
}
