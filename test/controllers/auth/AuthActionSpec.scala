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

package controllers.auth

import akka.util.Timeout
import org.mockito.Matchers.any
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status.{BAD_REQUEST, OK, UNAUTHORIZED}
import play.api.mvc.{Action, AnyContent, Controller}
import play.api.test.FakeRequest
import play.api.test.Helpers.status
import play.api.test.Helpers.stubControllerComponents
import uk.gov.hmrc.auth.core.{AuthConnector, InsufficientEnrolments, MissingBearerToken}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

class AuthActionSpec extends PlaySpec with GuiceOneAppPerSuite with BeforeAndAfterEach with MockitoSugar {

  val cc = stubControllerComponents()
  val mockAuthConnector = mock[AuthConnector]

  class Harness(authAction: AuthAction) extends Controller {
    def onPageLoad(): Action[AnyContent] = authAction { _ =>
      Ok
    }
  }

  implicit val timeout: Timeout = 5 seconds

  "AuthAction" should {
    "return UNAUTHORIZED when the user is not logged in" in {

      when(mockAuthConnector.authorise(any(), any())(any(), any()))
        .thenReturn(Future.failed(new MissingBearerToken))

      val authAction = new AuthActionImpl(mockAuthConnector, cc)
      val harness = new Harness(authAction)
      val result = harness.onPageLoad()(FakeRequest("GET", "/1111111111/ats-list"))
      status(result) mustBe UNAUTHORIZED
    }

    "return the request when the user is authorised" in {

      when(mockAuthConnector.authorise[Unit](any(), any())(any(), any()))
        .thenReturn(Future.successful(()))

      val authAction = new AuthActionImpl(mockAuthConnector, cc)
      val harness = new Harness(authAction)
      val result = harness.onPageLoad()(FakeRequest("GET", "/1111111111/ats-list"))

      status(result) mustBe OK
    }

    "return BAD_REQUEST when the user is authorised and the uri doesn't match our expected format" in {

      val authAction = new AuthActionImpl(mockAuthConnector, cc)
      val harness = new Harness(authAction)
      val result = harness.onPageLoad()(FakeRequest("GET", "/invalid"))

      status(result) mustBe BAD_REQUEST
    }

    "return UNAUTHORIZED when the IR-SA enrolment is not present" in {

      val retrievalResult: Future[Option[String]] = Future.failed(new InsufficientEnrolments)

      when(mockAuthConnector.authorise[Option[String]](any(), any())(any(), any()))
        .thenReturn(retrievalResult)

      val authAction = new AuthActionImpl(mockAuthConnector, cc)
      val harness = new Harness(authAction)
      val result = harness.onPageLoad()(FakeRequest("GET", "/1111111111/ats-list"))

      status(result) mustBe UNAUTHORIZED
    }
  }

}
