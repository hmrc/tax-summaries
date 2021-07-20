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

package controllers

import controllers.auth.FakeAuthAction
import models.{GenericError, JsonParseError, NotFoundError}
import org.mockito.Matchers.{eq => eqTo, _}
import org.mockito.Mockito._
import org.scalatest.time.{Millis, Seconds, Span}
import play.api.http.Status.{INTERNAL_SERVER_ERROR, NOT_FOUND, OK}
import play.api.libs.json.JsString
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsJson, contentAsString, defaultAwaitTimeout, status, stubControllerComponents}
import services.OdsService
import uk.gov.hmrc.http.HeaderCarrier
import utils.BaseSpec
import utils.TestConstants._

import scala.concurrent.{ExecutionContext, Future}

class ATSDataControllerSpec extends BaseSpec {

  lazy val cc = stubControllerComponents()

  implicit lazy val ec = inject[ExecutionContext]

  implicit val defaultPatience =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(500, Millis))

  val request = FakeRequest()

  val odsService: OdsService = mock[OdsService]
  val controller = new ATSDataController(odsService, FakeAuthAction, cc)

  val taxYear = 2021
  val json: JsString = JsString("success")

  "getAtsData" should {

    "return 200" when {

      "the service returns a right" in {

        when(odsService.getPayload(eqTo(testUtr), eqTo(taxYear))(any[HeaderCarrier])) thenReturn Future.successful(
          Right(json))
        val result = controller.getATSData(testUtr, taxYear)(request)

        status(result) shouldBe OK
        contentAsJson(result) shouldBe json
      }
    }

    "return 404" when {

      "connector returns a left with NotFoundError" in {

        val msg = "Record not found"

        when(odsService.getPayload(eqTo(testUtr), eqTo(taxYear))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(NotFoundError(msg)))

        val result = controller.getATSData(testUtr, taxYear)(request)

        status(result) shouldBe NOT_FOUND
        contentAsString(result) shouldBe msg
      }
    }

    "return 500" when {

      "connector returns a left with JsonParseError" in {

        val msg = "Could not parse JSON"

        when(odsService.getPayload(eqTo(testUtr), eqTo(taxYear))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(JsonParseError(msg)))

        val result = controller.getATSData(testUtr, taxYear)(request)

        status(result) shouldBe INTERNAL_SERVER_ERROR
        contentAsString(result) shouldBe msg
      }

      "connector returns a left with GenericError" in {

        val msg = "Something went wrong"

        when(odsService.getPayload(eqTo(testUtr), eqTo(taxYear))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(GenericError(msg)))

        val result = controller.getATSData(testUtr, taxYear)(request)

        status(result) shouldBe INTERNAL_SERVER_ERROR
        contentAsString(result) shouldBe msg
      }
    }
  }

  "hasAts" should {

    "return 200" when {

      "the service returns a right" in {

        when(odsService.getList(eqTo(testUtr))(any[HeaderCarrier])) thenReturn Future.successful(Right(json))

        val result = controller.hasAts(testUtr)(request)

        status(result) shouldBe OK
        contentAsJson(result) shouldBe json
      }
    }

    "return 404" when {

      "connector returns a left with NotFoundError" in {

        val msg = "Record not found"

        when(odsService.getList(eqTo(testUtr))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(NotFoundError(msg)))

        val result = controller.hasAts(testUtr)(request)

        status(result) shouldBe NOT_FOUND
        contentAsString(result) shouldBe msg
      }
    }

    "return 500" when {

      "connector returns a left with JsonParseError" in {

        val msg = "Could not parse JSON"

        when(odsService.getList(eqTo(testUtr))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(JsonParseError(msg)))

        val result = controller.hasAts(testUtr)(request)

        status(result) shouldBe INTERNAL_SERVER_ERROR
        contentAsString(result) shouldBe msg
      }

      "connector returns a left with GenericError" in {

        val msg = "Something went wrong"

        when(odsService.getList(eqTo(testUtr))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(GenericError(msg)))

        val result = controller.hasAts(testUtr)(request)

        status(result) shouldBe INTERNAL_SERVER_ERROR
        contentAsString(result) shouldBe msg
      }
    }
  }

  "getATSList" should {

    "return 200" when {

      "connector returns a right" in {

        when(odsService.getATSList(eqTo(testUtr))(any[HeaderCarrier])) thenReturn Future.successful(Right(json))

        val result = controller.getATSList(testUtr)(request)

        status(result) shouldBe OK
        contentAsJson(result) shouldBe json
      }
    }

    "return 404" when {

      "connector returns a left with NotFoundError" in {

        val errorMessage = "NoAtsData"

        when(odsService.getATSList(eqTo(testUtr))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(NotFoundError(errorMessage)))

        val result = controller.getATSList(testUtr)(request)

        status(result) shouldBe NOT_FOUND
        contentAsString(result) shouldBe errorMessage
      }
    }

    "return 500" when {

      "connector returns a left with JsonParseError" in {

        val errorMessage = "Error"

        when(odsService.getATSList(eqTo(testUtr))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(JsonParseError(errorMessage)))

        val result = controller.getATSList(testUtr)(request)

        status(result) shouldBe INTERNAL_SERVER_ERROR
        contentAsString(result) shouldBe errorMessage
      }

      "connector returns a left with GenericError" in {

        val errorMessage = "Error"

        when(odsService.getATSList(eqTo(testUtr))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(GenericError(errorMessage)))

        val result = controller.getATSList(testUtr)(request)

        status(result) shouldBe INTERNAL_SERVER_ERROR
        contentAsString(result) shouldBe errorMessage
      }
    }

    "return a failed future" in {
      when(odsService.getATSList(eqTo(testUtr))(any[HeaderCarrier])) thenReturn Future.failed(new Exception("failed"))

      val result = controller.getATSList(testUtr)(request)

      whenReady(result.failed) { exception =>
        exception shouldBe a[Exception]
      }
    }
  }
}
