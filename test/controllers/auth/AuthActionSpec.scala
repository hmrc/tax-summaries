/*
 * Copyright 2022 HM Revenue & Customs
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
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status.{BAD_REQUEST, OK, UNAUTHORIZED}
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import play.api.test.Helpers.{status, stubControllerComponents}
import play.api.test.{FakeRequest, Injecting}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.domain.SaUtrGenerator

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class AuthActionSpec
    extends PlaySpec with GuiceOneAppPerSuite with BeforeAndAfterEach with MockitoSugar with Injecting {

  val cc: ControllerComponents = stubControllerComponents()
  val mockAuthConnector: AuthConnector = mock[AuthConnector]
  implicit lazy val ec: ExecutionContext = inject[ExecutionContext]
  implicit val timeout: Timeout = 5.seconds

  class Harness(authAction: AuthAction) extends AbstractController(cc) {
    def onPageLoad(): Action[AnyContent] = authAction { _ =>
      Ok
    }
  }
  val utr: String = new SaUtrGenerator().nextSaUtr.utr
  val uar = "SomeUar"
  val authAction = new AuthActionImpl(mockAuthConnector, cc)
  val harness = new Harness(authAction)

  "AuthAction" must {

    "allow a request when authorised is successful" in {
      when(mockAuthConnector.authorise[Unit](any(), any())(any(), any()))
        .thenReturn(Future.successful(()))

      val result = harness.onPageLoad()(FakeRequest("GET", "/1111111111/ats-list"))
      status(result) mustBe OK
    }

    "return UNAUTHORIZED when the user is not logged in" in {
      when(mockAuthConnector.authorise(any(), any())(any(), any()))
        .thenReturn(Future.failed(new MissingBearerToken))

      val result = harness.onPageLoad()(FakeRequest("GET", "/1111111111/ats-list"))
      status(result) mustBe UNAUTHORIZED
    }

    "return BAD_REQUEST when the user is authorised and the uri doesn't match our expected format" in {
      val result = harness.onPageLoad()(FakeRequest("GET", "/invalid"))

      status(result) mustBe BAD_REQUEST
    }
  }

}
