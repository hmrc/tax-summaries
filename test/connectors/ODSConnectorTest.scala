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

import com.github.tomakehurst.wiremock.client.WireMock.{get, notFound, ok, serverError}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, JsString}
import play.api.test.Injecting
import uk.gov.hmrc.http.{HeaderCarrier, Upstream5xxResponse, UpstreamErrorResponse}
import utils.TestConstants._
import utils.{BaseSpec, WireMockHelper}

class ODSConnectorTest extends BaseSpec with WireMockHelper with ScalaFutures with IntegrationPatience with Injecting {

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .configure(
        "microservice.services.tax-summaries-hod.port" -> server.port()
      )
      .build()

  lazy val sut: ODSConnector = inject[ODSConnector]

  val json = JsObject(Map("foo" -> JsString("bar")))

  implicit val hc = HeaderCarrier()

  "connectToSelfAssessment" should {

    val url = s"/self-assessment/individuals/$testUtr/annual-tax-summaries/2014"

    "return json" when {

      "200 is returned" in {

        server.stubFor(
          get(url).willReturn(ok(json.toString()))
        )

        val result = sut.connectToSelfAssessment(testUtr, 2014)

        whenReady(result) {
          _ shouldBe Some(json)
        }
      }
    }

    "return None" when {

      "404 is returned" in {

        server.stubFor(
          get(url).willReturn(notFound())
        )

        val result = sut.connectToSelfAssessment(testUtr, 2014)

        whenReady(result) {
          _ shouldBe None
        }
      }
    }

    "return 500" in {

      server.stubFor(
        get(url).willReturn(serverError())
      )

      val result = sut.connectToSelfAssessment(testUtr, 2014)

      whenReady(result.failed) { exception =>
        exception shouldBe a[UpstreamErrorResponse]
      }
    }

  }

  "connectToSelfAssessmentList" should {

    val url = s"/self-assessment/individuals/$testUtr/annual-tax-summaries"

    "return json" when {

      "200 is returned" in {

        server.stubFor(
          get(url).willReturn(ok(json.toString()))
        )

        val result = sut.connectToSelfAssessmentList(testUtr)

        whenReady(result) {
          _ shouldBe Some(json)
        }
      }
    }

    "return None" when {

      "404 is returned" in {

        server.stubFor(
          get(url).willReturn(notFound())
        )

        val result = sut.connectToSelfAssessmentList(testUtr)

        whenReady(result) {
          _ shouldBe None
        }
      }
    }

    "return an exception" when {

      "500 is returned" in {

        server.stubFor(
          get(url).willReturn(serverError())
        )

        val result = sut.connectToSelfAssessmentList(testUtr)

        whenReady(result.failed) { exception =>
          exception shouldBe a[UpstreamErrorResponse]
        }
      }
    }
  }

  "connectToSATaxpayerDetails" should {

    val url = s"/self-assessment/individual/$testUtr/designatory-details/taxpayer"

    "return json" when {

      "200 is returned" in {

        server.stubFor(
          get(url).willReturn(ok(json.toString()))
        )

        val result = sut.connectToSATaxpayerDetails(testUtr)

        whenReady(result) {
          _ shouldBe Some(json)
        }
      }
    }

    "return None" when {

      "404 is returned" in {

        server.stubFor(
          get(url).willReturn(notFound())
        )

        val result = sut.connectToSATaxpayerDetails(testUtr)

        whenReady(result) {
          _ shouldBe None
        }
      }
    }

    "return an exception" when {

      "500 is returned" in {

        server.stubFor(
          get(url).willReturn(serverError())
        )

        val result = sut.connectToSATaxpayerDetails(testUtr)

        whenReady(result.failed) { exception =>
          exception shouldBe a[UpstreamErrorResponse]
        }
      }
    }
  }
}
