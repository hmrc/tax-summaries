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
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.http.Status.{BAD_REQUEST, GATEWAY_TIMEOUT, OK}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, HttpGet}
import uk.gov.hmrc.play.test.UnitSpec
import utils.TestConstants.testNino
import utils.{JsonUtil, WireMockHelper}

import scala.concurrent.Await
import scala.concurrent.duration._

class NPSConnectorTest extends UnitSpec with GuiceOneAppPerSuite with WireMockHelper {

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .configure(
        "microservice.services.tax-summaries-hod.port" -> server.port()
      )
      .build()

  implicit val hc = HeaderCarrier()
  private val currentYear = 2018
  private val invalidTaxYear = 201899

  trait NPSConnectorSetUp extends NpsConnector with JsonUtil {

    override def http: HttpGet = WSHttp
    override def serviceUrl: String = ApplicationConfig.npsServiceUrl
  }

  "connectToPayeTaxSummary" should {

    "return successful response" in new NPSConnectorSetUp {

      val expectedNpsResponse: String = load("/paye_annual_tax_summary.json")
      val url = s"/individuals/annual-tax-summary/" + testNino + "/" + currentYear

      server.stubFor(
        get(urlEqualTo(url)).willReturn(
          aResponse()
            .withStatus(OK)
            .withBody(expectedNpsResponse))
      )

      val result = Await.result(connectToPayeTaxSummary(testNino, currentYear), 5.seconds)
      result shouldBe Right(Json.parse(expectedNpsResponse))
    }

    "return Failure response" in new NPSConnectorSetUp {

      val expectedNpsResponse: String = load("/paye_annual_tax_summary.json")
      val url = s"/individuals/annual-tax-summary/" + testNino + "/" + invalidTaxYear

      server.stubFor(
        get(urlEqualTo(url)).willReturn(
          aResponse()
            .withStatus(400)
            .withBody("File not found"))
      )

      val result = Await.result(connectToPayeTaxSummary(testNino, invalidTaxYear), 5.seconds)
      result shouldBe Left(BAD_REQUEST)
    }

    "return Failure response in case of Exception" in new NPSConnectorSetUp {

      val expectedNpsResponse: String = load("/paye_annual_tax_summary.json")
      val url = s"/individuals/annual-tax-summary/" + testNino + "/" + invalidTaxYear

      server.stubFor(
        get(urlEqualTo(url)).willReturn(
          aResponse()
            .withFault(Fault.EMPTY_RESPONSE)
            .withBody("File not found"))
      )

      val result = Await.result(connectToPayeTaxSummary(testNino, invalidTaxYear), 5.seconds)
      result shouldBe Left(GATEWAY_TIMEOUT)
    }
  }
}
