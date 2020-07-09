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
import play.api.http.Status._
import play.api.mvc._
import play.api.test.FakeRequest
import play.api.test.Helpers.status
import uk.gov.hmrc.auth.core.{AuthConnector, InsufficientConfidenceLevel, InternalError, MissingBearerToken}
import utils.TestConstants._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

class PayeAuthActionSpec extends PlaySpec with GuiceOneAppPerSuite with BeforeAndAfterEach with MockitoSugar {

  val mockAuthConnector = mock[AuthConnector]
  val payeAuthAction = new PayeAuthActionImpl(mockAuthConnector)
  val harness = new Harness(payeAuthAction)
  val request = FakeRequest("GET", s"/$testNino/2018/paye-ats-data")

  class Harness(payeAuthAction: PayeAuthAction) extends Controller {
    def onPageLoad(): Action[AnyContent] = payeAuthAction { _ =>
      Ok
    }
  }

  implicit val timeout: Timeout = 5 seconds

  "AuthAction" should {
    "allow a request when authorised is successful" in {

      when(mockAuthConnector.authorise[Unit](any(), any())(any(), any()))
        .thenReturn(Future.successful(()))

      val result = harness.onPageLoad()(request)

      status(result) mustBe OK
    }

    "return UNAUTHORIZED when the user is not logged in" in {

      when(mockAuthConnector.authorise(any(), any())(any(), any()))
        .thenReturn(Future.failed(new MissingBearerToken))

      val result = harness.onPageLoad()(request)

      status(result) mustBe UNAUTHORIZED
    }

    "return UNAUTHORIZED when confidence level is insufficient" in {

      val retrievalResult: Future[Option[String]] = Future.failed(new InsufficientConfidenceLevel)

      when(mockAuthConnector.authorise[Option[String]](any(), any())(any(), any()))
        .thenReturn(retrievalResult)

      val result = harness.onPageLoad()(request)

      status(result) mustBe UNAUTHORIZED
    }

    "return UNAUTHORIZED when NINO is missing or doesn't match URL parameter" in {

      val retrievalResult: Future[Option[String]] = Future.failed(InternalError("IncorrectNino"))

      when(mockAuthConnector.authorise[Option[String]](any(), any())(any(), any()))
        .thenReturn(retrievalResult)

      val result = harness.onPageLoad()(request)

      status(result) mustBe UNAUTHORIZED
    }

    "return INTERNAL_SERVER_ERROR when auth call fails for unexpected reason" in {

      val retrievalResult: Future[Option[String]] = Future.failed(new RuntimeException())

      when(mockAuthConnector.authorise[Option[String]](any(), any())(any(), any()))
        .thenReturn(retrievalResult)

      val result = harness.onPageLoad()(request)

      status(result) mustBe INTERNAL_SERVER_ERROR
    }

  }

}
