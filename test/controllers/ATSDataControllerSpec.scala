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

package controllers

import akka.stream.Materializer
import controllers.auth.FakeAuthAction
import org.mockito.Matchers.{eq => eqTo, _}
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.time.{Millis, Seconds, Span}
import play.api.http.Status.{INTERNAL_SERVER_ERROR, NOT_FOUND, OK}
import play.api.libs.json.{JsString, JsValue}
import play.api.test.FakeRequest
import play.api.test.Helpers.stubControllerComponents
import services.OdsService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import utils.TestConstants._

import scala.concurrent.Future

class ATSDataControllerSpec extends UnitSpec with MockitoSugar with WithFakeApplication with ScalaFutures {

  val cc = stubControllerComponents()

  implicit lazy val mat = fakeApplication.injector.instanceOf[Materializer]

  implicit val defaultPatience =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(500, Millis))

  val request = FakeRequest()

  val odsService: OdsService = mock[OdsService]
  val controller = new ATSDataController(odsService, FakeAuthAction, cc)

  "getAtsData" should {

    "return a failed future" in {
      when(odsService.getPayload(eqTo(testUtr), eqTo(2014))(any[HeaderCarrier]))
        .thenReturn(Future.failed(new Exception("failed")))
      val result = controller.getATSData(testUtr, 2014)(request)

      whenReady(result.failed) { exception =>
        exception shouldBe a[Exception]
      }
    }
  }

  "hasAts" should {

    "return Not Found (404) on error" in {
      when(odsService.getList(eqTo(testUtr))(any[HeaderCarrier])).thenReturn(Future.failed(new Exception("failed")))
      val result = controller.hasAts(testUtr)(request)
      status(result) shouldBe 404
    }
  }

  "getATSList" should {

    "return 200" when {

      "connector returns a right" in {

        val expectedBody: JsValue = JsString("body")

        when(odsService.getATSList(eqTo(testUtr))(any[HeaderCarrier])) thenReturn Future.successful(Right(expectedBody))

        val result = controller.getATSList(testUtr)(request)

        status(result) shouldBe OK
        jsonBodyOf(result).futureValue shouldBe expectedBody
      }
    }

    "return 404" when {

      "connector returns a left with the message 'NoAtsData'" in {

        val errorMessage = "NoAtsData"

        when(odsService.getATSList(eqTo(testUtr))(any[HeaderCarrier])) thenReturn Future.successful(Left(errorMessage))

        val result = controller.getATSList(testUtr)(request)

        status(result) shouldBe NOT_FOUND
        bodyOf(result).futureValue shouldBe errorMessage
      }
    }

    "return 500" when {

      "connector returns a left with any other message" in {

        val errorMessage = "Error"

        when(odsService.getATSList(eqTo(testUtr))(any[HeaderCarrier])) thenReturn Future.successful(Left(errorMessage))

        val result = controller.getATSList(testUtr)(request)

        status(result) shouldBe INTERNAL_SERVER_ERROR
        bodyOf(result).futureValue shouldBe errorMessage
      }
    }

    "return a failed future" in {
      when(odsService.getATSList(eqTo(testUtr))(any[HeaderCarrier])).thenReturn(Future.failed(new Exception("failed")))
      val result = controller.getATSList(testUtr)(request)

      whenReady(result.failed) { exception =>
        exception shouldBe a[Exception]
      }
    }
  }
}
