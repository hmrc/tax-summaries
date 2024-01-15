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

///*
// * Copyright 2023 HM Revenue & Customs
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package controllers.auth
//
//import cats.data.EitherT
//import connectors.PertaxConnector
//import models.PertaxApiResponse
//import models.admin.PertaxBackendToggle
//import org.mockito.ArgumentMatchers.any
//import play.api.mvc.{Action, AnyContent, AnyContentAsEmpty, ControllerComponents, InjectedController}
//import play.api.test.FakeRequest
//import play.api.test.Helpers.{defaultAwaitTimeout, status, stubControllerComponents, _}
//import uk.gov.hmrc.http.UpstreamErrorResponse
//import uk.gov.hmrc.mongoFeatureToggles.model.FeatureFlag
//import uk.gov.hmrc.play.bootstrap.auth.DefaultAuthConnector
//import utils.TestConstants.testNino
//import utils.{BaseSpec, NinoHelper}
//
//import scala.concurrent.{ExecutionContext, Future}
//
//class PertaxAuthActionSpec extends BaseSpec {
//
//  val mockAuthConnector: DefaultAuthConnector = mock[DefaultAuthConnector]
//
//  val mockPertaxConnector: PertaxConnector = mock[PertaxConnector]
//  val cc: ControllerComponents             = stubControllerComponents()
//
//  implicit val ec: ExecutionContext = cc.executionContext
//
//  val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", s"$testNino")
//
//  class Harness(authJourney: AuthJourney) extends InjectedController {
//    def onPageLoad(): Action[AnyContent] = authJourney.authWithPaye { _ =>
//      Ok(s"Nino: $testNino")
//    }
//  }
//
//  override def beforeEach(): Unit = {
//    reset(mockPertaxConnector, mockFeatureFlagService)
//
//    when(mockFeatureFlagService.get(org.mockito.ArgumentMatchers.eq(PertaxBackendToggle))) thenReturn Future
//      .successful(
//        FeatureFlag(PertaxBackendToggle, isEnabled = false)
//      )
//
//  }
//
//  val payeAuthAction = new PayeAuthActionImpl(
//    mockAuthConnector,
//    app.injector.instanceOf[NinoHelper],
//    cc
//  )
//
//  val pertaxAuthAction = new PertaxAuthActionImpl(
//    cc,
//    mockPertaxConnector,
//    mockFeatureFlagService,
//    app.injector.instanceOf[NinoHelper]
//  )
//
//  val authJourney =
//    new AuthJourneyImpl(payeAuthAction, pertaxAuthAction)
//
//  val controller = new Harness(authJourney)
//
//  "A user with a Nino" must {
//    "create a success if PertaxConnector returns an ACCESS_GRANTED code" in {
//
//      when(mockAuthConnector.authorise[Unit](any(), any())(any(), any()))
//        .thenReturn(Future.successful(()))
//
//      when(mockPertaxConnector.pertaxAuth(any())(any()))
//        .thenReturn(
//          EitherT[Future, UpstreamErrorResponse, PertaxApiResponse](
//            Future.successful(Right(PertaxApiResponse("ACCESS_GRANTED", "", None)))
//          )
//        )
//
//      when(mockFeatureFlagService.get(org.mockito.ArgumentMatchers.eq(PertaxBackendToggle))) thenReturn Future
//        .successful(
//          FeatureFlag(PertaxBackendToggle, isEnabled = true)
//        )
//
//      val result = controller.onPageLoad()(request)
//      status(result) mustBe OK
//    }
//  }
//
//  "create a success if PertaxConnector returns an INVALID_AFFINITY code" in {
//    when(mockAuthConnector.authorise[Unit](any(), any())(any(), any()))
//      .thenReturn(Future.successful(()))
//
//    when(mockFeatureFlagService.get(org.mockito.ArgumentMatchers.eq(PertaxBackendToggle))) thenReturn Future
//      .successful(
//        FeatureFlag(PertaxBackendToggle, isEnabled = true)
//      )
//
//    when(mockPertaxConnector.pertaxAuth(any())(any()))
//      .thenReturn(
//        EitherT[Future, UpstreamErrorResponse, PertaxApiResponse](
//          Future.successful(Right(PertaxApiResponse("INVALID_AFFINITY", "", None)))
//        )
//      )
//
//    val result = controller.onPageLoad()(request)
//    status(result) mustBe OK
//  }
//
//  "create a failure if PertaxConnector returns an NO_HMRC_PT_ENROLMENT code" in {
//    when(mockAuthConnector.authorise[Unit](any(), any())(any(), any()))
//      .thenReturn(Future.successful(()))
//
//    when(mockFeatureFlagService.get(org.mockito.ArgumentMatchers.eq(PertaxBackendToggle))) thenReturn Future
//      .successful(
//        FeatureFlag(PertaxBackendToggle, isEnabled = true)
//      )
//
//    when(mockPertaxConnector.pertaxAuth(any())(any()))
//      .thenReturn(
//        EitherT[Future, UpstreamErrorResponse, PertaxApiResponse](
//          Future.successful(Right(PertaxApiResponse("NO_HMRC_PT_ENROLMENT", "", None)))
//        )
//      )
//
//    val result = controller.onPageLoad()(request)
//    status(result) mustBe UNAUTHORIZED
//  }
//}
