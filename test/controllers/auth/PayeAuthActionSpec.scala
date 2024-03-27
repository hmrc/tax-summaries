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
import models.admin.PertaxBackendToggle
import org.mockito.ArgumentMatchers.any
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers.{status, _}
import uk.gov.hmrc.auth.core.{AuthConnector, InsufficientConfidenceLevel, InternalError, MissingBearerToken}
import uk.gov.hmrc.http.UpstreamErrorResponse
import uk.gov.hmrc.mongoFeatureToggles.model.FeatureFlag
import utils.BaseSpec
import utils.TestConstants._

import scala.concurrent.{ExecutionContext, Future}

class PayeAuthActionSpec extends BaseSpec {

  implicit lazy val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]

  lazy val cc: ControllerComponents = stubControllerComponents()

  private val mockPertaxConnector: PertaxConnector = mock[PertaxConnector]

  val mockAuthConnector: AuthConnector             = mock[AuthConnector]
  val payeAuthAction                               = new PayeAuthActionImpl(
    mockAuthConnector,
    stubControllerComponents(),
    mockFeatureFlagService,
    mockPertaxConnector
  )
  val harness                                      = new Harness(payeAuthAction)
  val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", s"/$testNino/2020/paye-ats-data")

  class Harness(payeAuthAction: PayeAuthAction) extends AbstractController(cc) {
    def onPageLoad(): Action[AnyContent] = payeAuthAction { _ =>
      Ok
    }
  }

  private def pertaxBackendToggleOn =
    when(mockFeatureFlagService.get(org.mockito.ArgumentMatchers.eq(PertaxBackendToggle))) thenReturn Future
      .successful(
        FeatureFlag(PertaxBackendToggle, isEnabled = true)
      )

  private def pertaxBackendToggleOff =
    when(mockFeatureFlagService.get(org.mockito.ArgumentMatchers.eq(PertaxBackendToggle))) thenReturn Future
      .successful(
        FeatureFlag(PertaxBackendToggle, isEnabled = false)
      )

  override def beforeEach(): Unit =
    reset(mockPertaxConnector, mockFeatureFlagService)

  "AuthAction (feature toggle OFF)" must {
    "allow a request when authorised is successful" in {
      pertaxBackendToggleOff
      when(mockAuthConnector.authorise[Unit](any(), any())(any(), any()))
        .thenReturn(Future.successful((): Unit))

      val result = harness.onPageLoad()(request)

      status(result) mustBe OK
      verify(mockPertaxConnector, times(0)).pertaxAuth(any())
    }

    "return UNAUTHORIZED when the user is not logged in" in {
      pertaxBackendToggleOff
      when(mockAuthConnector.authorise[Unit](any(), any())(any(), any()))
        .thenReturn(Future.failed(new MissingBearerToken))

      val result = harness.onPageLoad()(request)

      status(result) mustBe UNAUTHORIZED
      verify(mockPertaxConnector, times(0)).pertaxAuth(any())
    }

    "return UNAUTHORIZED when confidence level is insufficient" in {
      pertaxBackendToggleOff
      val retrievalResult: Future[Unit] = Future.failed(new InsufficientConfidenceLevel)

      when(mockAuthConnector.authorise[Unit](any(), any())(any(), any()))
        .thenReturn(retrievalResult)

      val result = harness.onPageLoad()(request)

      status(result) mustBe UNAUTHORIZED
      verify(mockPertaxConnector, times(0)).pertaxAuth(any())
    }

    "return UNAUTHORIZED when NINO is missing or doesn't match URL parameter" in {
      pertaxBackendToggleOff
      val retrievalResult: Future[Unit] = Future.failed(InternalError("IncorrectNino"))

      when(mockAuthConnector.authorise[Unit](any(), any())(any(), any()))
        .thenReturn(retrievalResult)

      val result = harness.onPageLoad()(request)

      status(result) mustBe UNAUTHORIZED
      verify(mockPertaxConnector, times(0)).pertaxAuth(any())
    }

    "return INTERNAL_SERVER_ERROR when auth call fails for unexpected reason" in {
      pertaxBackendToggleOff
      val retrievalResult: Future[Unit] = Future.failed(new RuntimeException())

      when(mockAuthConnector.authorise[Unit](any(), any())(any(), any()))
        .thenReturn(retrievalResult)

      val result = harness.onPageLoad()(request)

      status(result) mustBe INTERNAL_SERVER_ERROR
      verify(mockPertaxConnector, times(0)).pertaxAuth(any())
    }

  }

  "AuthAction (feature toggle ON)" must {
    "allow a request when authorised is successful" in {
      val retrievalResult: Future[Unit] = Future.successful(Some(testNino))
      pertaxBackendToggleOn
      when(mockAuthConnector.authorise[Unit](any(), any())(any(), any()))
        .thenReturn(retrievalResult)
      when(mockPertaxConnector.pertaxAuth(any()))
        .thenReturn(
          EitherT[Future, UpstreamErrorResponse, PertaxApiResponse](
            Future.successful(Right(PertaxApiResponse("ACCESS_GRANTED", "", None)))
          )
        )

      val result = harness.onPageLoad()(request)

      status(result) mustBe OK
      verify(mockPertaxConnector, times(1)).pertaxAuth(any())
    }

    "allow a request when authorised is successful invalid affinity" in {
      val retrievalResult: Future[Unit] = Future.successful(Some(testNino))
      pertaxBackendToggleOn
      when(mockAuthConnector.authorise[Unit](any(), any())(any(), any()))
        .thenReturn(retrievalResult)
      when(mockPertaxConnector.pertaxAuth(any()))
        .thenReturn(
          EitherT[Future, UpstreamErrorResponse, PertaxApiResponse](
            Future.successful(Right(PertaxApiResponse("INVALID_AFFINITY", "", None)))
          )
        )

      val result = harness.onPageLoad()(request)

      status(result) mustBe OK
      verify(mockPertaxConnector, times(1)).pertaxAuth(any())
    }

    "return UNAUTHORISED when no PT enrolment" in {
      val retrievalResult: Future[Unit] = Future.successful(Some(testNino))
      pertaxBackendToggleOn
      when(mockAuthConnector.authorise[Unit](any(), any())(any(), any()))
        .thenReturn(retrievalResult)
      when(mockPertaxConnector.pertaxAuth(any()))
        .thenReturn(
          EitherT[Future, UpstreamErrorResponse, PertaxApiResponse](
            Future.successful(Right(PertaxApiResponse("NO_HMRC_PT_ENROLMENT", "", None)))
          )
        )

      val result = harness.onPageLoad()(request)

      status(result) mustBe UNAUTHORIZED
      verify(mockPertaxConnector, times(1)).pertaxAuth(any())
    }

    "return INTERNAL_SERVER_ERROR when the pertax auth call fails for unexpected reason" in {
      val retrievalResult: Future[Unit] = Future.successful(Some(testNino))
      pertaxBackendToggleOn
      when(mockAuthConnector.authorise[Unit](any(), any())(any(), any()))
        .thenReturn(retrievalResult)
      when(mockPertaxConnector.pertaxAuth(any()))
        .thenReturn(
          EitherT[Future, UpstreamErrorResponse, PertaxApiResponse](
            Future.successful(Left(UpstreamErrorResponse.apply("", 500)))
          )
        )

      val result = harness.onPageLoad()(request)

      status(result) mustBe INTERNAL_SERVER_ERROR
      verify(mockPertaxConnector, times(1)).pertaxAuth(any())
    }

    "return UNAUTHORIZED when the user is not logged in" in {
      pertaxBackendToggleOn
      when(mockAuthConnector.authorise[Unit](any(), any())(any(), any()))
        .thenReturn(Future.failed(new MissingBearerToken))

      val result = harness.onPageLoad()(request)

      status(result) mustBe UNAUTHORIZED
      verify(mockPertaxConnector, times(0)).pertaxAuth(any())
    }

    "return UNAUTHORIZED when confidence level is insufficient" in {
      pertaxBackendToggleOn
      val retrievalResult: Future[Unit] = Future.failed(new InsufficientConfidenceLevel)

      when(mockAuthConnector.authorise[Unit](any(), any())(any(), any()))
        .thenReturn(retrievalResult)

      val result = harness.onPageLoad()(request)

      status(result) mustBe UNAUTHORIZED
      verify(mockPertaxConnector, times(0)).pertaxAuth(any())
    }

    "return UNAUTHORIZED when NINO is missing or doesn't match URL parameter" in {
      pertaxBackendToggleOn
      val retrievalResult: Future[Unit] = Future.failed(InternalError("IncorrectNino"))

      when(mockAuthConnector.authorise[Unit](any(), any())(any(), any()))
        .thenReturn(retrievalResult)

      val result = harness.onPageLoad()(request)

      status(result) mustBe UNAUTHORIZED
      verify(mockPertaxConnector, times(0)).pertaxAuth(any())
    }

    "return INTERNAL_SERVER_ERROR when auth call fails for unexpected reason" in {
      pertaxBackendToggleOn
      val retrievalResult: Future[Unit] = Future.failed(new RuntimeException())

      when(mockAuthConnector.authorise[Unit](any(), any())(any(), any()))
        .thenReturn(retrievalResult)

      val result = harness.onPageLoad()(request)

      status(result) mustBe INTERNAL_SERVER_ERROR
      verify(mockPertaxConnector, times(0)).pertaxAuth(any())
    }

  }

}
