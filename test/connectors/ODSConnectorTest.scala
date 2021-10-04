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

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, _}
import org.scalatest.Inside.inside
import play.api.Application
import play.api.http.Status.{BAD_GATEWAY, INTERNAL_SERVER_ERROR, NOT_FOUND, SERVICE_UNAVAILABLE}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, JsString}
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames, RequestId, SessionId, UpstreamErrorResponse}
import utils.TestConstants._
import utils.{BaseSpec, WireMockHelper}

class ODSConnectorTest extends BaseSpec with WireMockHelper {

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .configure(
        "microservice.services.tax-summaries-hod.port" -> server.port(),
        "microservice.services.tax-summaries-hod.host" -> "127.0.0.1",
        "play.ws.timeout.request"                      -> "1000ms",
        "play.ws.timeout.connection"                   -> "500ms"
      )
      .build()

  lazy val sut: ODSConnector = inject[ODSConnector]

  val json = JsObject(Map("foo" -> JsString("bar")))

  val sessionId = "testSessionId"
  val requestId = "testRequestId"

  implicit val hc: HeaderCarrier = HeaderCarrier(
    sessionId = Some(SessionId(sessionId)),
    requestId = Some(RequestId(requestId))
  )

  "connectToSelfAssessment" must {

    val url = s"/self-assessment/individuals/$testUtr/annual-tax-summaries/2014"

    "return json" when {

      "200 is returned" in {

        server.stubFor(
          get(url).willReturn(ok(json.toString()))
        )

        val result = sut.connectToSelfAssessment(testUtr, 2014)

        whenReady(result) {
          _ mustBe Right(json)
        }

        server.verify(
          getRequestedFor(urlEqualTo(url))
            .withHeader(HeaderNames.xSessionId, equalTo(sessionId))
            .withHeader(HeaderNames.xRequestId, equalTo(requestId))
            .withHeader(
              "CorrelationId",
              matching("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}"))
        )
      }
    }

    "return UpstreamErrorResponse" when {

      "500 is returned when response retrieves 404" in {

        server.stubFor(
          get(url).willReturn(notFound())
        )

        val result = sut.connectToSelfAssessment(testUtr, 2014).futureValue

        inside(result.left.get) {
          case UpstreamErrorResponse(_, status, reportedAs, _) =>
            status mustBe NOT_FOUND
            reportedAs mustBe INTERNAL_SERVER_ERROR
        }
      }

      List(
        INTERNAL_SERVER_ERROR,
        SERVICE_UNAVAILABLE,
        BAD_GATEWAY
      ).foreach { status: Int =>
        s"502 is returned when response retrieves $status" in {

          server.stubFor(
            get(urlEqualTo(url)).willReturn(
              aResponse()
                .withStatus(status)
                .withBody(""))
          )

          val result = sut.connectToSelfAssessment(testUtr, 2014).futureValue

          inside(result.left.get) {
            case UpstreamErrorResponse(_, status, reportedAs, _) =>
              status mustBe status
              reportedAs mustBe BAD_GATEWAY
          }
        }
      }
    }
  }

  "connectToSelfAssessmentList" must {

    val url = s"/self-assessment/individuals/$testUtr/annual-tax-summaries"

    "return json" when {

      "200 is returned" in {

        server.stubFor(
          get(url).willReturn(ok(json.toString()))
        )

        val result = sut.connectToSelfAssessmentList(testUtr)

        whenReady(result) {
          _ mustBe Right(json)
        }
      }
    }

    "return UpstreamErrorResponse" when {

      "500 is returned when response retrieves 404" in {
        server.stubFor(
          get(url).willReturn(notFound())
        )

        val result = sut.connectToSelfAssessmentList(testUtr).futureValue

        inside(result.left.get) {
          case UpstreamErrorResponse(_, status, reportedAs, _) =>
            status mustBe NOT_FOUND
            reportedAs mustBe INTERNAL_SERVER_ERROR
        }
      }

      List(
        INTERNAL_SERVER_ERROR,
        SERVICE_UNAVAILABLE,
        BAD_GATEWAY
      ).foreach { status: Int =>
        s"502 is returned when response retrieves $status" in {

          server.stubFor(
            get(urlEqualTo(url)).willReturn(
              aResponse()
                .withStatus(status)
                .withBody(""))
          )

          val result = sut.connectToSelfAssessmentList(testUtr).futureValue

          inside(result.left.get) {
            case UpstreamErrorResponse(_, status, reportedAs, _) =>
              status mustBe status
              reportedAs mustBe BAD_GATEWAY
          }
        }
      }
    }
  }

  "connectToSATaxpayerDetails" must {

    val url = s"/self-assessment/individual/$testUtr/designatory-details/taxpayer"

    "return json" when {

      "200 is returned" in {

        server.stubFor(
          get(url).willReturn(ok(json.toString()))
        )

        val result = sut.connectToSATaxpayerDetails(testUtr)

        whenReady(result) {
          _ mustBe Right(json)
        }
      }
    }

    "return UpstreamErrorResponse" when {

      "500 is returned when response retrieves 404" in {

        server.stubFor(
          get(url).willReturn(notFound())
        )

        val result = sut.connectToSATaxpayerDetails(testUtr).futureValue

        inside(result.left.get) {
          case UpstreamErrorResponse(_, status, reportedAs, _) =>
            status mustBe NOT_FOUND
            reportedAs mustBe INTERNAL_SERVER_ERROR
        }
      }

      List(
        INTERNAL_SERVER_ERROR,
        SERVICE_UNAVAILABLE,
        BAD_GATEWAY
      ).foreach { status: Int =>
        s"502 is returned when response retrieves $status" in {

          server.stubFor(
            get(urlEqualTo(url)).willReturn(
              aResponse()
                .withStatus(status)
                .withBody(""))
          )

          val result = sut.connectToSATaxpayerDetails(testUtr).futureValue

          inside(result.left.get) {
            case UpstreamErrorResponse(_, status, reportedAs, _) =>
              status mustBe status
              reportedAs mustBe BAD_GATEWAY
          }
        }
      }
    }
  }
}
