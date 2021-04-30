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

package controller

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import akka.util.Timeout
import controllers.ATSPAYEDataController
import controllers.auth.{FakeAuthAction, PayeAuthAction}
import models.paye.PayeAtsMiddleTier
import org.mockito.Matchers.{eq => eqTo, _}
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import play.api.libs.json.Json
import play.api.mvc.ControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsJson, stubControllerComponents}
import services.NpsService
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import utils.TestConstants._

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.Duration

class ATSPAYEDataControllerTest extends UnitSpec with MockitoSugar with WithFakeApplication with ScalaFutures {

  val cc: ControllerComponents = stubControllerComponents()
  implicit val timeout: Timeout = new Timeout(Duration.Zero)
  implicit val ec: ExecutionContext = cc.executionContext
  implicit val actorSystem: ActorSystem = ActorSystem()
  implicit val mat: Materializer = ActorMaterializer()
  val request = FakeRequest()

  val npsService: NpsService = mock[NpsService]
  val payeAuthAction: PayeAuthAction = FakeAuthAction

  class TestController extends ATSPAYEDataController(npsService, payeAuthAction, cc)

  val cy = 2018
  val cyPlus1 = 2019

  val expectedResponseCY = PayeAtsMiddleTier(cy, testNino, None, None, None, None, None)
  val expectedResponseCYPlus1 = PayeAtsMiddleTier(cyPlus1, testNino, None, None, None, None, None)

  "getAtsData" should {

    "return success response with ATS data" in new TestController {
      when(npsService.getPayeATSData(eqTo(testNino), eqTo(cy))(any[HeaderCarrier]))
        .thenReturn(Right(expectedResponseCY))
      val result = getATSData(testNino, cy)(request)

      status(result) shouldBe 200
      contentAsJson(result) shouldBe Json.toJson(expectedResponseCY)
    }

    "return a failed future" in new TestController {
      when(npsService.getPayeATSData(eqTo(testNino), eqTo(cy))(any[HeaderCarrier]))
        .thenReturn(Left(HttpResponse(BAD_REQUEST, BAD_REQUEST.toString)))
      val result = getATSData(testNino, cy)(request)

      status(result) shouldBe 400
      contentAsJson(result) shouldBe Json.toJson(BAD_REQUEST)
    }
  }

  "getATSDataMultipleYears" should {

    "return a list of data for the given tax years" in new TestController {
      when(npsService.getPayeATSData(eqTo(testNino), eqTo(cy))(any[HeaderCarrier])) thenReturn Future.successful(
        Right(expectedResponseCY))
      when(npsService.getPayeATSData(eqTo(testNino), eqTo(cyPlus1))(any[HeaderCarrier])) thenReturn Future.successful(
        Right(expectedResponseCYPlus1))

      val result = getATSDataMultipleYears(testNino, cy, cyPlus1)(request)

      status(result) shouldBe OK
      contentAsJson(result) shouldBe Json.toJson(Seq(expectedResponseCY, expectedResponseCYPlus1))
    }

    "return Ok" when {

      "the service returns Ok and not found" in new TestController {

        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cy))(any[HeaderCarrier])) thenReturn Future.successful(
          Right(expectedResponseCY))
        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cyPlus1))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(HttpResponse(NOT_FOUND, "Not found")))

        val result = getATSDataMultipleYears(testNino, cy, cyPlus1)(request)

        status(result) shouldBe OK
        contentAsJson(result) shouldBe Json.toJson(Seq(expectedResponseCY))

      }

      "the service returns not found and ok" in new TestController {

        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cy))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(HttpResponse(NOT_FOUND, "NOT_FOUND")))
        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cyPlus1))(any[HeaderCarrier])) thenReturn Future.successful(
          Right(expectedResponseCYPlus1))

        val result = getATSDataMultipleYears(testNino, cy, cyPlus1)(request)

        status(result) shouldBe OK
        contentAsJson(result) shouldBe Json.toJson(Seq(expectedResponseCYPlus1))

      }
    }

    "return NOT_FOUND" when {

      "the service returns no data" in new TestController {

        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cy))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(HttpResponse(NOT_FOUND, "Not found")))
        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cyPlus1))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(HttpResponse(NOT_FOUND, "Not found")))

        val result = getATSDataMultipleYears(testNino, cy, cyPlus1)(request)

        status(result) shouldBe NOT_FOUND
      }
    }

    "return BAD_REQUEST" when {

      "the service returns bad request" in new TestController {

        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cy))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(HttpResponse(BAD_REQUEST, "Bad request")))
        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cyPlus1))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(HttpResponse(BAD_REQUEST, "Bad request")))

        val result = getATSDataMultipleYears(testNino, cy, cyPlus1)(request)

        status(result) shouldBe BAD_REQUEST
      }

      "the service returns bad request and ok" in new TestController {

        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cy))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(HttpResponse(BAD_REQUEST, "Bad request")))
        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cyPlus1))(any[HeaderCarrier])) thenReturn Future.successful(
          Right(expectedResponseCYPlus1))

        val result = getATSDataMultipleYears(testNino, cy, cyPlus1)(request)

        status(result) shouldBe BAD_REQUEST
      }

      "the service returns ok and bad request" in new TestController {

        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cy))(any[HeaderCarrier])) thenReturn Future.successful(
          Right(expectedResponseCY))
        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cyPlus1))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(HttpResponse(BAD_REQUEST, "Bad request")))

        val result = getATSDataMultipleYears(testNino, cy, cyPlus1)(request)

        status(result) shouldBe BAD_REQUEST
      }

      "the service returns bad request and not found" in new TestController {

        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cy))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(HttpResponse(BAD_REQUEST, "Bad request")))
        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cyPlus1))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(HttpResponse(NOT_FOUND, "NOT_FOUND")))

        val result = getATSDataMultipleYears(testNino, cy, cyPlus1)(request)

        status(result) shouldBe BAD_REQUEST
      }

      "the service returns not found and bad request" in new TestController {

        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cy))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(HttpResponse(NOT_FOUND, "Not found")))
        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cyPlus1))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(HttpResponse(BAD_REQUEST, "Bad request")))

        val result = getATSDataMultipleYears(testNino, cy, cyPlus1)(request)

        status(result) shouldBe BAD_REQUEST
      }

      "the service returns bad request (cy) and internal server error (cy +1)" in new TestController {

        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cy))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(HttpResponse(BAD_REQUEST, "Bad request")))
        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cyPlus1))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(HttpResponse(INTERNAL_SERVER_ERROR, "Internal server error")))

        val result = getATSDataMultipleYears(testNino, cy, cyPlus1)(request)

        status(result) shouldBe BAD_REQUEST
      }
    }

    "return INTERNAL_SERVER_ERROR" when {

      "the service returns internal server error" in new TestController {

        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cy))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(HttpResponse(INTERNAL_SERVER_ERROR, "Internal server error")))
        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cyPlus1))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(HttpResponse(INTERNAL_SERVER_ERROR, "Internal server error")))

        val result = getATSDataMultipleYears(testNino, cy, cyPlus1)(request)

        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

      "an exception occurs when retrieving data" in new TestController {

        val exMessage = "An error occurred"

        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cy))(any[HeaderCarrier])) thenReturn Future.failed(
          new Exception(exMessage))

        val result = getATSDataMultipleYears(testNino, cy, cyPlus1)(request)

        status(result) shouldBe INTERNAL_SERVER_ERROR
        bodyOf(result).futureValue shouldBe exMessage
      }

      "the service returns internal server error and not found" in new TestController {

        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cy))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(HttpResponse(INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR")))
        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cyPlus1))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(HttpResponse(NOT_FOUND, "Not found")))

        val result = getATSDataMultipleYears(testNino, cy, cyPlus1)(request)

        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

      "the service returns not found and internal server error" in new TestController {

        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cy))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(HttpResponse(NOT_FOUND, "Not found")))
        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cyPlus1))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(HttpResponse(INTERNAL_SERVER_ERROR, "Internal server error")))

        val result = getATSDataMultipleYears(testNino, cy, cyPlus1)(request)

        status(result) shouldBe INTERNAL_SERVER_ERROR
      }

      "the service returns internal server error and Ok" in new TestController {

        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cy))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(HttpResponse(INTERNAL_SERVER_ERROR, "Internal server error")))
        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cyPlus1))(any[HeaderCarrier])) thenReturn Future.successful(
          Right(expectedResponseCYPlus1))

        val result = getATSDataMultipleYears(testNino, cy, cyPlus1)(request)

        status(result) shouldBe INTERNAL_SERVER_ERROR

      }

      "the service returns ok and internal server error" in new TestController {

        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cy))(any[HeaderCarrier])) thenReturn Future.successful(
          Right(expectedResponseCY))
        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cyPlus1))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(HttpResponse(INTERNAL_SERVER_ERROR, "Internal server error")))

        val result = getATSDataMultipleYears(testNino, cy, cyPlus1)(request)

        status(result) shouldBe INTERNAL_SERVER_ERROR

      }

      "the service returns internal server error (cy) and bad request (cy +1)" in new TestController {

        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cy))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(HttpResponse(INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR")))
        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cyPlus1))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(HttpResponse(BAD_REQUEST, "Bad request")))

        val result = getATSDataMultipleYears(testNino, cy, cyPlus1)(request)

        status(result) shouldBe INTERNAL_SERVER_ERROR
      }
    }
  }
}
