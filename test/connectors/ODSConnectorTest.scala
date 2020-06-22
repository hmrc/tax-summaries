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
import models.ODSModels.{SaTaxpayerDetails, SelfAssessmentList}
import models.TaxSummaryLiability
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.http.Status.{BAD_REQUEST, FORBIDDEN, INTERNAL_SERVER_ERROR, NOT_FOUND, OK, SERVICE_UNAVAILABLE, UNAUTHORIZED}
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier, HttpGet, Upstream4xxResponse, Upstream5xxResponse}
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.test.UnitSpec
import utils.TestConstants._
import utils.{JsonUtil, WireMockHelper}

class ODSConnectorTest
    extends UnitSpec with GuiceOneAppPerSuite with WireMockHelper with ScalaFutures with IntegrationPatience
    with JsonUtil {

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .configure(
        "microservice.services.tax-summaries-hod.port" -> server.port()
      )
      .build()

  implicit val hc: HeaderCarrier = HeaderCarrier()

  class TestConnector extends ODSConnector with ServicesConfig {
    override def http: HttpGet = WSHttp
    override def serviceUrl: String = ApplicationConfig.npsServiceUrl
    val taxYear: Int = 2014
    val url: String = s"/self-assessment/individuals/$testUtr/annual-tax-summaries/$taxYear"
  }

  "connectToSelfAssessment" should {

    "return an instance of TaxSummaryLiability" in new TestConnector {

      val expectedResponse = load("/utr_2014.json")

      server.stubFor(
        get(urlEqualTo(url)).willReturn(
          aResponse()
            .withStatus(OK)
            .withBody(expectedResponse))
      )

      val result = connectToSelfAssessment(testUtr, taxYear)

      await(result).get shouldBe a[TaxSummaryLiability]
    }

    "return None" when {
      "http call returns Not Found" in new TestConnector {

        server.stubFor(
          get(urlEqualTo(url)).willReturn(
            aResponse()
              .withStatus(NOT_FOUND)
          ))

        val result = connectToSelfAssessment(testUtr, taxYear)

        await(result) shouldBe None
      }
    }

    "throw a BadRequestException" when {
      "http call returns Bad Request" in new TestConnector {

        server.stubFor(
          get(urlEqualTo(url)).willReturn(
            aResponse()
              .withStatus(BAD_REQUEST)
              .withBody("Bad Request")
          ))

        val result = connectToSelfAssessment(testUtr, taxYear)

        whenReady(result.failed) { exception =>
          exception shouldBe a[BadRequestException]
        }
      }
    }

    "throw an Upstream4xxResponse" when {
      "http call returns other 4xx codes" in new TestConnector {

        server.stubFor(
          get(urlEqualTo(url)).willReturn(
            aResponse()
              .withStatus(FORBIDDEN)
              .withBody("Forbidden")
          ))

        val result = connectToSelfAssessment(testUtr, taxYear)

        whenReady(result.failed) { exception =>
          exception shouldBe a[Upstream4xxResponse]
        }
      }
    }

    "throw an Upstream5xxResponse" when {
      "http call returns Internal Server Error" in new TestConnector {

        server.stubFor(
          get(urlEqualTo(url)).willReturn(
            aResponse()
              .withStatus(INTERNAL_SERVER_ERROR)
              .withBody("An error occurred")
          ))

        val result = connectToSelfAssessment(testUtr, taxYear)

        whenReady(result.failed) { exception =>
          exception shouldBe a[Upstream5xxResponse]
        }
      }

      "http call returns Service Unavailable" in new TestConnector {

        server.stubFor(
          get(urlEqualTo(url)).willReturn(
            aResponse()
              .withStatus(SERVICE_UNAVAILABLE)
              .withBody("Cannot reach HOD")
          ))

        val result = connectToSelfAssessment(testUtr, taxYear)

        whenReady(result.failed) { exception =>
          exception shouldBe a[Upstream5xxResponse]
        }
      }
    }

    "throw exceptions when they occur" in new TestConnector {

      server.stubFor(
        get(urlEqualTo(url)).willReturn(
          aResponse()
            .withFault(Fault.MALFORMED_RESPONSE_CHUNK)
        ))

      val result = connectToSelfAssessment(testUtr, taxYear)

      whenReady(result.failed) { exception =>
        exception shouldBe a[Exception]
      }
    }

  }

  "connectToSelfAssessmentList" should {

    "return an instance of SelfAssessmentList" in new TestConnector {

      override val url = s"/self-assessment/individuals/$testUtr/annual-tax-summaries"

      val expectedResponse = load("/taxYearList/test_list_1097172501.json")

      server.stubFor(
        get(urlEqualTo(url)).willReturn(
          aResponse()
            .withStatus(OK)
            .withBody(expectedResponse))
      )

      val result = connectToSelfAssessmentList(testUtr)

      await(result).get shouldBe a[SelfAssessmentList]
    }

    "return None" when {
      "http call returns Not Found" in new TestConnector {

        override val url = s"/self-assessment/individuals/$testUtr/annual-tax-summaries"

        server.stubFor(
          get(urlEqualTo(url)).willReturn(aResponse()
            .withStatus(NOT_FOUND))
        )

        val result = connectToSelfAssessmentList(testUtr)

        await(result) shouldBe None
      }
    }

    "throw a BadRequestException" when {
      "http call returns Bad Request" in new TestConnector {

        override val url = s"/self-assessment/individuals/$testUtr/annual-tax-summaries"

        server.stubFor(
          get(urlEqualTo(url)).willReturn(
            aResponse()
              .withStatus(BAD_REQUEST)
              .withBody("Bad Request"))
        )

        val result = connectToSelfAssessmentList(testUtr)

        assertThrows[BadRequestException] {
          await(result)
        }
      }
    }

    "throw an Upstream4xxResponse" when {
      "http call returns other 4xx codes" in new TestConnector {

        override val url = s"/self-assessment/individuals/$testUtr/annual-tax-summaries"

        server.stubFor(
          get(urlEqualTo(url)).willReturn(
            aResponse()
              .withStatus(UNAUTHORIZED)
              .withBody("Unauthorized"))
        )

        val result = connectToSelfAssessmentList(testUtr)

        assertThrows[Upstream4xxResponse] {
          await(result)
        }
      }
    }

    "throw an Upstream5xxResponse" when {
      "http call returns Internal Server Error" in new TestConnector {

        override val url = s"/self-assessment/individuals/$testUtr/annual-tax-summaries"

        server.stubFor(
          get(urlEqualTo(url)).willReturn(
            aResponse()
              .withStatus(INTERNAL_SERVER_ERROR)
              .withBody("An error occurred"))
        )

        val result = connectToSelfAssessmentList(testUtr)

        assertThrows[Upstream5xxResponse] {
          await(result)
        }
      }

      "http call returns Service Unavailable" in new TestConnector {

        override val url = s"/self-assessment/individuals/$testUtr/annual-tax-summaries"

        server.stubFor(
          get(urlEqualTo(url)).willReturn(
            aResponse()
              .withStatus(SERVICE_UNAVAILABLE)
              .withBody("Cannot reach HOD"))
        )

        val result = connectToSelfAssessmentList(testUtr)

        assertThrows[Upstream5xxResponse] {
          await(result)
        }
      }
    }

    "throw exceptions when they occur" in new TestConnector {

      override val url = s"/self-assessment/individuals/$testUtr/annual-tax-summaries"

      server.stubFor(
        get(urlEqualTo(url)).willReturn(aResponse()
          .withFault(Fault.EMPTY_RESPONSE))
      )

      val result = connectToSelfAssessmentList(testUtr)

      assertThrows[Exception] {
        await(result)
      }
    }

  }

  "connectToSATaxpayerDetails" should {

    "return an instance of SaTaxpayerDetails" in new TestConnector {

      override val url = s"/self-assessment/individual/$testUtr/designatory-details/taxpayer"

      val expectedResponse = load("/taxpayerData/test_individual_utr.json")

      server.stubFor(
        get(urlEqualTo(url)).willReturn(
          aResponse()
            .withStatus(OK)
            .withBody(expectedResponse))
      )

      val result = connectToSATaxpayerDetails(testUtr)

      await(result).get shouldBe a[SaTaxpayerDetails]
    }

    "return None" when {
      "http call returns Not Found" in new TestConnector {

        override val url = s"/self-assessment/individual/$testUtr/designatory-details/taxpayer"

        server.stubFor(
          get(urlEqualTo(url)).willReturn(aResponse()
            .withStatus(NOT_FOUND))
        )

        val result = connectToSATaxpayerDetails(testUtr)

        await(result) shouldBe None
      }
    }

    "throw a BadRequestException" when {
      "http call returns Bad Request" in new TestConnector {

        override val url = s"/self-assessment/individual/$testUtr/designatory-details/taxpayer"

        server.stubFor(
          get(urlEqualTo(url)).willReturn(
            aResponse()
              .withStatus(BAD_REQUEST)
              .withBody("Bad Request"))
        )

        val result = connectToSATaxpayerDetails(testUtr)

        assertThrows[BadRequestException] {
          await(result)
        }
      }
    }

    "throw an Upstream4xxResponse" when {
      "http call returns other 4xx codes" in new TestConnector {

        override val url = s"/self-assessment/individual/$testUtr/designatory-details/taxpayer"

        server.stubFor(
          get(urlEqualTo(url)).willReturn(
            aResponse()
              .withStatus(FORBIDDEN)
              .withBody("Forbidden"))
        )

        val result = connectToSATaxpayerDetails(testUtr)

        assertThrows[Upstream4xxResponse] {
          await(result)
        }
      }
    }

    "throw an Upstream5xxResponse" when {
      "http call returns Internal Server Error" in new TestConnector {

        override val url = s"/self-assessment/individual/$testUtr/designatory-details/taxpayer"

        server.stubFor(
          get(urlEqualTo(url)).willReturn(
            aResponse()
              .withStatus(INTERNAL_SERVER_ERROR)
              .withBody("An error occurred"))
        )

        val result = connectToSATaxpayerDetails(testUtr)

        assertThrows[Upstream5xxResponse] {
          await(result)
        }
      }

      "http call returns Service Unavailable" in new TestConnector {

        override val url = s"/self-assessment/individual/$testUtr/designatory-details/taxpayer"

        server.stubFor(
          get(urlEqualTo(url)).willReturn(
            aResponse()
              .withStatus(SERVICE_UNAVAILABLE)
              .withBody("Cannot reach HOD"))
        )

        val result = connectToSATaxpayerDetails(testUtr)

        assertThrows[Upstream5xxResponse] {
          await(result)
        }
      }
    }

    "throw exceptions when they occur" in new TestConnector {

      override val url = s"/self-assessment/individual/$testUtr/designatory-details/taxpayer"

      server.stubFor(
        get(urlEqualTo(url)).willReturn(aResponse()
          .withFault(Fault.RANDOM_DATA_THEN_CLOSE))
      )

      val result = connectToSATaxpayerDetails(testUtr)

      assertThrows[Exception] {
        await(result)
      }
    }
  }
}
