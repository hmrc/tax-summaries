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
import com.kenshoo.play.metrics.Metrics
import generators.JsValueGenerator
import org.mockito.Mockito._
import org.mockito.{ArgumentCaptor, Matchers, Mockito}
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import play.api.http.Status._
import play.api.libs.json.JsValue
import uk.gov.hmrc.http._
import utils.TestConstants._
import utils.{ConnectorBaseSpec, TestMetrics}

import scala.concurrent.{ExecutionContext, Future}

class ODSConnectorTest extends ConnectorBaseSpec with GeneratorDrivenPropertyChecks with JsValueGenerator {

  implicit val hc = HeaderCarrier()
  lazy val connector = inject[ODSConnector]

  "connectToSelfAssessment" should {
    val url = "/self-assessment/individuals/" + testUtr + "/annual-tax-summaries/2014"

    "return successful future" in {
      forAll { (jsValue: JsValue) =>
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(aResponse().withStatus(OK).withBody(jsValue.toString))
        )

        val result = connector.connectToSelfAssessment(testUtr, 2014)

        whenReady(result) {
          _ shouldBe a[JsValue]
        }
      }
    }

    "http verbs should return an exception" when {
      "response is a BAD_REQUEST" in {
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(aResponse().withStatus(BAD_REQUEST))
        )

        val result = connector.connectToSelfAssessment(testUtr, 2014)

        whenReady(result.failed) { exception =>
          exception shouldBe a[BadRequestException]
        }
      }

      "response is a NOT_FOUND" in {
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(aResponse().withStatus(NOT_FOUND))
        )

        val result = connector.connectToSelfAssessment(testUtr, 2014)

        whenReady(result.failed) { exception =>
          exception shouldBe a[NotFoundException]
        }
      }
    }
  }

  "connectToSelfAssessmentList" should {
    val url = "/self-assessment/individuals/" + testUtr + "/annual-tax-summaries"

    "return successful future" in {
      forAll { (jsValue: JsValue) =>
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(aResponse().withStatus(OK).withBody(jsValue.toString))
        )

        val result = connector.connectToSelfAssessmentList(testUtr)

        whenReady(result) {
          _ shouldBe a[JsValue]
        }
      }
    }

    "http verbs should return an exception" when {
      "response is a BAD_REQUEST" in {
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(aResponse().withStatus(BAD_REQUEST))
        )

        val result = connector.connectToSelfAssessmentList(testUtr)

        whenReady(result.failed) { exception =>
          exception shouldBe a[BadRequestException]
        }
      }

      "response is a NOT_FOUND" in {
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(aResponse().withStatus(NOT_FOUND))
        )

        val result = connector.connectToSelfAssessmentList(testUtr)

        whenReady(result.failed) { exception =>
          exception shouldBe a[NotFoundException]
        }
      }
    }
  }

  "connectToSATaxpayerDetails" should {
    val url = "/self-assessment/individual/" + testUtr + "/designatory-details/taxpayer"

    "return successful future" in {
      forAll { (jsValue: JsValue) =>
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(aResponse().withStatus(OK).withBody(jsValue.toString))
        )

        val result = connector.connectToSATaxpayerDetails(testUtr)

        whenReady(result) {
          _ shouldBe a[JsValue]
        }
      }
    }

    "http verbs should return an exception" when {
      "response is a BAD_REQUEST" in {
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(aResponse().withStatus(BAD_REQUEST))
        )

        val result = connector.connectToSATaxpayerDetails(testUtr)

        whenReady(result.failed) { exception =>
          exception shouldBe a[BadRequestException]
        }
      }

      "response is a NOT_FOUND" in {
        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(aResponse().withStatus(NOT_FOUND))
        )

        val result = connector.connectToSATaxpayerDetails(testUtr)

        whenReady(result.failed) { exception =>
          exception shouldBe a[NotFoundException]
        }
      }
    }
  }

  "ODSConnector headers" should {
    "contain additional tracking measures" in {
      lazy val metrics: Metrics = new TestMetrics
      val http = mock[HttpClient]
      lazy val connector = new ODSConnector(http, applicationConfig, metrics)

      when(
        http.GET[JsValue](Matchers.any[String])(
          Matchers.any[HttpReads[JsValue]],
          Matchers.any[HeaderCarrier],
          Matchers.any[ExecutionContext]))
        .thenReturn(Future.successful(mock[JsValue]))

      val eventCaptor = ArgumentCaptor.forClass(classOf[HeaderCarrier])

      val result = connector.connectToSelfAssessment(testUtr, 2014)

      whenReady(result) { _ =>
        Mockito.verify(http).GET(Matchers.any())(Matchers.any(), eventCaptor.capture(), Matchers.any())

        val newHeaders = eventCaptor.getValue

        newHeaders.headers.exists(_._1 == "CorrelationId") shouldBe true
        newHeaders.headers.exists(_._1 == "X-Session-ID") shouldBe true
        newHeaders.headers.exists(_._1 == "X-Request-ID") shouldBe true
      }
    }
  }
}
