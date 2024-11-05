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

package controllers.auth

import cats.data.EitherT
import connectors.PertaxConnector
import models.PertaxApiResponse
import org.mockito.ArgumentMatchers.any
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers.{status, _}
import uk.gov.hmrc.http.UpstreamErrorResponse
import utils.BaseSpec
import utils.TestConstants._

import scala.concurrent.{ExecutionContext, Future}

class PayeAuthActionSpec extends BaseSpec {

  implicit lazy val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]

  lazy val cc: ControllerComponents = stubControllerComponents()

  private val mockPertaxConnector: PertaxConnector = mock[PertaxConnector]

  val payeAuthAction                               = new PayeAuthActionImpl(
    stubControllerComponents(),
    mockPertaxConnector
  )
  val harness                                      = new Harness(payeAuthAction)
  val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", s"/$testNino/2020/paye-ats-data")

  class Harness(payeAuthAction: PayeAuthAction) extends AbstractController(cc) {
    def onPageLoad(): Action[AnyContent] = payeAuthAction { _ =>
      Ok
    }
  }

  override def beforeEach(): Unit =
    reset(mockPertaxConnector)

  "PayeAuthAction with PertaxBackendToggleOn" must {
    "allow a request when the pertax API returns an ACCESS_GRANTED response" in {

      when(mockPertaxConnector.pertaxAuth(any()))
        .thenReturn(
          EitherT[Future, UpstreamErrorResponse, PertaxApiResponse](
            Future.successful(Right(PertaxApiResponse("ACCESS_GRANTED", "")))
          )
        )

      val result = harness.onPageLoad()(request)

      status(result) mustBe OK
      verify(mockPertaxConnector, times(1)).pertaxAuth(any())
    }

    "allow a request when authorised is successful invalid affinity" in {

      when(mockPertaxConnector.pertaxAuth(any()))
        .thenReturn(
          EitherT[Future, UpstreamErrorResponse, PertaxApiResponse](
            Future.successful(Right(PertaxApiResponse("INVALID_AFFINITY", "")))
          )
        )

      val result = harness.onPageLoad()(request)

      status(result) mustBe OK
      verify(mockPertaxConnector, times(1)).pertaxAuth(any())
    }

    "return UNAUTHORISED when no PT enrolment" in {

      when(mockPertaxConnector.pertaxAuth(any()))
        .thenReturn(
          EitherT[Future, UpstreamErrorResponse, PertaxApiResponse](
            Future.successful(Right(PertaxApiResponse("NO_HMRC_PT_ENROLMENT", "")))
          )
        )

      val result = harness.onPageLoad()(request)

      status(result) mustBe UNAUTHORIZED
      verify(mockPertaxConnector, times(1)).pertaxAuth(any())
    }

    "return INTERNAL_SERVER_ERROR when unexpected error returned from pertax" in {
      when(mockPertaxConnector.pertaxAuth(any()))
        .thenReturn(
          EitherT[Future, UpstreamErrorResponse, PertaxApiResponse](
            Future.successful(Left(UpstreamErrorResponse("", IM_A_TEAPOT)))
          )
        )

      val result = harness.onPageLoad()(request)

      status(result) mustBe INTERNAL_SERVER_ERROR
      verify(mockPertaxConnector, times(1)).pertaxAuth(any())
    }

    "return TOO_MANY_REQUESTS when this Upstream error returned from pertax" in {
      when(mockPertaxConnector.pertaxAuth(any()))
        .thenReturn(
          EitherT[Future, UpstreamErrorResponse, PertaxApiResponse](
            Future.successful(Left(UpstreamErrorResponse("", TOO_MANY_REQUESTS)))
          )
        )

      val result = harness.onPageLoad()(request)

      status(result) mustBe TOO_MANY_REQUESTS
      verify(mockPertaxConnector, times(1)).pertaxAuth(any())
    }

    "return UNAUTHORIZED when this Upstream error returned from pertax" in {
      when(mockPertaxConnector.pertaxAuth(any()))
        .thenReturn(
          EitherT[Future, UpstreamErrorResponse, PertaxApiResponse](
            Future.successful(Left(UpstreamErrorResponse("", UNAUTHORIZED)))
          )
        )

      val result = harness.onPageLoad()(request)

      status(result) mustBe UNAUTHORIZED
      verify(mockPertaxConnector, times(1)).pertaxAuth(any())
    }
  }

}
