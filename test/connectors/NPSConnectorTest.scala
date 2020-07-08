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
import config.{ApplicationConfig, WSHttp}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, NOT_FOUND, OK}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, HttpGet, Upstream4xxResponse}
import uk.gov.hmrc.play.test.UnitSpec
import utils.TestConstants.testNino
import utils.{JsonUtil, WireMockHelper}

import scala.concurrent.ExecutionContext.Implicits.global

class NPSConnectorTest
    extends UnitSpec with GuiceOneAppPerSuite with WireMockHelper with ScalaFutures with IntegrationPatience {

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .configure(
        "microservice.services.tax-summaries-hod.port" -> server.port()
      )
      .build()

  implicit val hc = HeaderCarrier()
  private val currentYear = 2018
  private val invalidTaxYear = 201899

  private val testNinoWithoutSuffix = testNino.take(8)

  class NPSConnectorSetUp extends NpsConnector(WSHttp) with JsonUtil

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
  }
}
