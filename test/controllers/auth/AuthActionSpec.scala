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

package controllers.auth

import akka.util.Timeout
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status.{BAD_REQUEST, OK, UNAUTHORIZED}
import play.api.mvc.{AbstractController, Action, AnyContent}
import play.api.test.{FakeRequest, Injecting}
import play.api.test.Helpers.{status, stubControllerComponents}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.domain.SaUtrGenerator

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._

class AuthActionSpec
    extends PlaySpec with GuiceOneAppPerSuite with BeforeAndAfterEach with MockitoSugar with Injecting {

  val cc = stubControllerComponents()
  val mockAuthConnector = mock[AuthConnector]
  implicit lazy val ec = inject[ExecutionContext]

  class Harness(authAction: AuthAction) extends AbstractController(cc) {
    def onPageLoad(): Action[AnyContent] = authAction { _ =>
      Ok
    }
  }
  val utr = new SaUtrGenerator().nextSaUtr.utr
  val uar = "SomeUar"

  implicit val timeout: Timeout = 5 seconds

  "AuthAction" should {
    "return the request when the user has an active IR-SA-AGENT enrolment" in {
      val retrievalResults: Future[~[Enrolments, Option[String]]] =
        Future.successful(
          new ~(
            Enrolments(Set(Enrolment("IR-SA-AGENT", Seq(EnrolmentIdentifier("IRAgentReference", uar)), "Activated"))),
            None)
        )

      when(mockAuthConnector.authorise[Enrolments ~ Option[String]](any(), any())(any(), any()))
        .thenReturn(retrievalResults)

      val authAction = new AuthActionImpl(mockAuthConnector, cc)
      val harness = new Harness(authAction)
      val result = harness.onPageLoad()(FakeRequest("GET", "/1111111111/ats-list"))

      status(result) mustBe OK
    }

    "return the request when the user has a UTR enrolment associated with their account" in {
      val retrievalResults: Future[~[Enrolments, Option[String]]] =
        Future.successful(
          new ~(Enrolments(Set.empty), Some(utr))
        )

      when(mockAuthConnector.authorise[Enrolments ~ Option[String]](any(), any())(any(), any()))
        .thenReturn(retrievalResults)

      val authAction = new AuthActionImpl(mockAuthConnector, cc)
      val harness = new Harness(authAction)
      val result = harness.onPageLoad()(FakeRequest("GET", "/1111111111/ats-list"))

      status(result) mustBe OK
    }

    "return the request when the user has a UTR enrolment and an IR-SA-AGENT enrolment" in {
      val retrievalResults: Future[~[Enrolments, Option[String]]] =
        Future.successful(
          new ~(
            Enrolments(Set(Enrolment("IR-SA-AGENT", Seq(EnrolmentIdentifier("IRAgentReference", uar)), ""))),
            Some(utr))
        )

      when(mockAuthConnector.authorise[Enrolments ~ Option[String]](any(), any())(any(), any()))
        .thenReturn(retrievalResults)

      val authAction = new AuthActionImpl(mockAuthConnector, cc)
      val harness = new Harness(authAction)
      val result = harness.onPageLoad()(FakeRequest("GET", "/1111111111/ats-list"))

      status(result) mustBe OK
    }

    "return an UNAUTHORIZED when the user has inactive IR-SA-AGENT enrolment" in {
      val retrievalResults: Future[~[Enrolments, Option[String]]] =
        Future.successful(
          new ~(Enrolments(Set(Enrolment("IR-SA-AGENT", Seq(EnrolmentIdentifier("IRAgentReference", uar)), ""))), None)
        )

      when(mockAuthConnector.authorise[Enrolments ~ Option[String]](any(), any())(any(), any()))
        .thenReturn(retrievalResults)

      val authAction = new AuthActionImpl(mockAuthConnector, cc)
      val harness = new Harness(authAction)
      val result = harness.onPageLoad()(FakeRequest("GET", "/1111111111/ats-list"))

      status(result) mustBe UNAUTHORIZED
    }

    "return an UNAUTHORIZED when a user has neither SA enrolments" in {
      val retrievalResults: Future[~[Enrolments, Option[String]]] =
        Future.successful(
          new ~(Enrolments(Set.empty), None)
        )

      when(mockAuthConnector.authorise[Enrolments ~ Option[String]](any(), any())(any(), any()))
        .thenReturn(retrievalResults)

      val authAction = new AuthActionImpl(mockAuthConnector, cc)
      val harness = new Harness(authAction)
      val result = harness.onPageLoad()(FakeRequest("GET", "/1111111111/ats-list"))

      status(result) mustBe UNAUTHORIZED
    }

    "return UNAUTHORIZED when the user is not logged in" in {

      when(mockAuthConnector.authorise(any(), any())(any(), any()))
        .thenReturn(Future.failed(new MissingBearerToken))

      val authAction = new AuthActionImpl(mockAuthConnector, cc)
      val harness = new Harness(authAction)
      val result = harness.onPageLoad()(FakeRequest("GET", "/1111111111/ats-list"))
      status(result) mustBe UNAUTHORIZED
    }

    "return BAD_REQUEST when the user is authorised and the uri doesn't match our expected format" in {

      val authAction = new AuthActionImpl(mockAuthConnector, cc)
      val harness = new Harness(authAction)
      val result = harness.onPageLoad()(FakeRequest("GET", "/invalid"))

      status(result) mustBe BAD_REQUEST
    }
  }

}
