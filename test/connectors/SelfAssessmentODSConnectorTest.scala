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

package connectors

import com.github.tomakehurst.wiremock.client.WireMock._
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, JsString}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames, HttpResponse, RequestId, SessionId, UpstreamErrorResponse}
import utils.TestConstants._
import utils.{BaseSpec, WireMockHelper}

class SelfAssessmentODSConnectorTest extends BaseSpec with ConnectorSpec with WireMockHelper {

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .configure(
        "microservice.services.tax-summaries-hod.port" -> server.port(),
        "microservice.services.tax-summaries-hod.host" -> "127.0.0.1"
      )
      .build()

  lazy val sut: SelfAssessmentODSConnector = inject[SelfAssessmentODSConnector]

  val json: JsObject = JsObject(Map("foo" -> JsString("bar")))

  implicit val userRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

  val sessionId = "testSessionId"
  val requestId = "testRequestId"

  override implicit val hc: HeaderCarrier = HeaderCarrier(
    sessionId = Some(SessionId(sessionId)),
    requestId = Some(RequestId(requestId))
  )

  "connectToSelfAssessment" must {

    val url = s"/self-assessment/individuals/$testUtr/annual-tax-summaries/2014"

    "return json" when {

      "200 is returned" in {

        stubGet(url, OK, Some(json.toString()))

        val result = sut.connectToSelfAssessment(testUtr, 2014).value

        whenReady(result) {
          _.map(_.json) mustBe Right(json)
        }

        server.verify(
          getRequestedFor(urlEqualTo(url))
            .withHeader(HeaderNames.xSessionId, equalTo(sessionId))
            .withHeader(HeaderNames.xRequestId, equalTo(requestId))
            .withHeader(
              "CorrelationId",
              matching("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}")
            )
        )
      }
    }

    "return UpstreamErrorResponse" when {
      List(400, 401, 403, 409, 412, 429, 500, 501, 502, 503, 504).foreach { status =>
        s"a response with status $status is received" in {

          stubGet(url, status, Some(""))

          val result = sut.connectToSelfAssessment(testUtr, 2014).value

          whenReady(result) { res =>
            res mustBe a[Left[UpstreamErrorResponse, _]]
          }
        }
      }
    }

    "return UpstreamErrorResponse" when {
      s"a response with status 404 is received" in {

        stubGet(url, NOT_FOUND, Some(""))

        val result = sut.connectToSelfAssessment(testUtr, 2014).value

        whenReady(result) { res =>
          res mustBe a[Right[_, HttpResponse]]
        }
      }

    }
  }
}
