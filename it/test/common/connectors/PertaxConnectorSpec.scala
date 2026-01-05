/*
 * Copyright 2024 HM Revenue & Customs
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

package common.connectors

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, ok, post, urlEqualTo}
import common.models.PertaxApiResponse
import common.utils.IntegrationSpec
import play.api.Application
import play.api.http.Status.*
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import uk.gov.hmrc.http.UpstreamErrorResponse

class PertaxConnectorSpec extends IntegrationSpec {

  override def beforeEach(): Unit =
    super.beforeEach()

  override implicit lazy val app: Application =
    new GuiceApplicationBuilder()
      .configure(
        "microservice.services.pertax.port" -> server.port()
      )
      .build()

  lazy val pertaxConnector: PertaxConnector =
    app.injector.instanceOf[PertaxConnector]

  private val authoriseUrl: String =
    s"/pertax/authorise"

  "PertaxAuthConnector" should {
    "return a PertaxApiResponse with ACCESS_GRANTED code" in {
      server.stubFor(
        post(urlEqualTo(authoriseUrl)).willReturn(
          ok(
            Json.prettyPrint(
              Json.obj(
                "code"    -> "ACCESS_GRANTED",
                "message" -> "Access granted"
              )
            )
          )
        )
      )

      val result =
        pertaxConnector
          .pertaxAuth(hc)
          .value
          .futureValue
          .getOrElse(PertaxApiResponse("INCORRECT", "INCORRECT"))

      result mustBe PertaxApiResponse("ACCESS_GRANTED", "Access granted")
    }

    "return a PertaxApiResponse with NO_HMRC_PT_ENROLMENT code with a redirect link" in {
      server.stubFor(
        post(urlEqualTo(authoriseUrl)).willReturn(
          ok(
            Json.prettyPrint(
              Json.obj(
                "code"    -> "NO_HMRC_PT_ENROLMENT",
                "message" -> "There is no valid HMRC PT enrolment"
              )
            )
          )
        )
      )

      val result =
        pertaxConnector
          .pertaxAuth(hc)
          .value
          .futureValue
          .getOrElse(PertaxApiResponse("INCORRECT", "INCORRECT"))

      result mustBe PertaxApiResponse("NO_HMRC_PT_ENROLMENT", "There is no valid HMRC PT enrolment")
    }

    "return a PertaxApiResponse with INVALID_AFFINITY code and an errorView" in {
      server.stubFor(
        post(urlEqualTo(authoriseUrl)).willReturn(
          ok(
            Json.prettyPrint(
              Json.obj(
                "code"    -> "INVALID_AFFINITY",
                "message" -> "The user is neither an individual or an organisation"
              )
            )
          )
        )
      )

      val result =
        pertaxConnector
          .pertaxAuth(hc)
          .value
          .futureValue
          .getOrElse(PertaxApiResponse("INCORRECT", "INCORRECT"))

      result mustBe PertaxApiResponse("INVALID_AFFINITY", "The user is neither an individual or an organisation")
    }

    "return a UpstreamErrorResponse with the correct error code" in {
      List(
        BAD_REQUEST,
        NOT_FOUND,
        FORBIDDEN,
        INTERNAL_SERVER_ERROR
      ).foreach { error =>
        server.stubFor(
          post(urlEqualTo(authoriseUrl)).willReturn(
            aResponse().withStatus(error)
          )
        )

        val result =
          pertaxConnector
            .pertaxAuth(hc)
            .value
            .futureValue
            .swap
            .getOrElse(UpstreamErrorResponse("INCORRECT", UNPROCESSABLE_ENTITY))

        result.statusCode mustBe error
      }
    }
  }
}
