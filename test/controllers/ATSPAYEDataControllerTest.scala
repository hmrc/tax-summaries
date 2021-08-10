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
import controllers.ATSPAYEDataController
import controllers.auth.{FakeAuthAction, PayeAuthAction}
import models.paye.PayeAtsMiddleTier
import org.mockito.Matchers.{eq => eqTo, _}
import org.mockito.Mockito._
import play.api.libs.json.Json
import play.api.mvc.ControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsJson, contentAsString, defaultAwaitTimeout, status, stubControllerComponents}
import services.NpsService
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import utils.BaseSpec
import utils.TestConstants._

import scala.concurrent.{ExecutionContext, Future}

class ATSPAYEDataControllerTest extends BaseSpec {

  val cc: ControllerComponents = stubControllerComponents()
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

  "getAtsData" must {

    "return success response with ATS data" in new TestController {
      when(npsService.getPayeATSData(eqTo(testNino), eqTo(cy))(any[HeaderCarrier]))
        .thenReturn(Future.successful(Right(expectedResponseCY)))
      val result = getATSData(testNino, cy)(request)

      status(result) mustBe 200
      contentAsJson(result) mustBe Json.toJson(expectedResponseCY)
    }

    "return a failed future" in new TestController {
      val errorMessage = "An error occurred"
      when(npsService.getPayeATSData(eqTo(testNino), eqTo(cy))(any[HeaderCarrier]))
        .thenReturn(Future.successful(Left(HttpResponse(BAD_REQUEST, errorMessage))))
      val result = getATSData(testNino, cy)(request)

      status(result) mustBe 400
      contentAsString(result) mustBe errorMessage
    }
  }

  "getATSDataMultipleYears" must {

    "return a list of data for the given tax years" in new TestController {
      when(npsService.getPayeATSData(eqTo(testNino), eqTo(cy))(any[HeaderCarrier])) thenReturn Future.successful(
        Right(expectedResponseCY))
      when(npsService.getPayeATSData(eqTo(testNino), eqTo(cyPlus1))(any[HeaderCarrier])) thenReturn Future.successful(
        Right(expectedResponseCYPlus1))

      val result = getATSDataMultipleYears(testNino, cy, cyPlus1)(request)

      status(result) mustBe OK
      contentAsJson(result) mustBe Json.toJson(Seq(expectedResponseCY, expectedResponseCYPlus1))
    }

    "return Ok" when {

      "the service returns Ok and not found" in new TestController {

        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cy))(any[HeaderCarrier])) thenReturn Future.successful(
          Right(expectedResponseCY))
        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cyPlus1))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(HttpResponse(NOT_FOUND, "Not found")))

        val result = getATSDataMultipleYears(testNino, cy, cyPlus1)(request)

        status(result) mustBe OK
        contentAsJson(result) mustBe Json.toJson(Seq(expectedResponseCY))

      }

      "the service returns not found and ok" in new TestController {

        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cy))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(HttpResponse(NOT_FOUND, "NOT_FOUND")))
        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cyPlus1))(any[HeaderCarrier])) thenReturn Future.successful(
          Right(expectedResponseCYPlus1))

        val result = getATSDataMultipleYears(testNino, cy, cyPlus1)(request)

        status(result) mustBe OK
        contentAsJson(result) mustBe Json.toJson(Seq(expectedResponseCYPlus1))

      }
    }

    "return NOT_FOUND" when {

      "the service returns no data" in new TestController {

        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cy))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(HttpResponse(NOT_FOUND, "Not found")))
        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cyPlus1))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(HttpResponse(NOT_FOUND, "Not found")))

        val result = getATSDataMultipleYears(testNino, cy, cyPlus1)(request)

        status(result) mustBe NOT_FOUND
      }
    }

    "return BAD_REQUEST" when {

      "the service returns bad request" in new TestController {

        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cy))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(HttpResponse(BAD_REQUEST, "Bad request")))
        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cyPlus1))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(HttpResponse(BAD_REQUEST, "Bad request")))

        val result = getATSDataMultipleYears(testNino, cy, cyPlus1)(request)

        status(result) mustBe BAD_REQUEST
      }

      "the service returns bad request and ok" in new TestController {

        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cy))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(HttpResponse(BAD_REQUEST, "Bad request")))
        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cyPlus1))(any[HeaderCarrier])) thenReturn Future.successful(
          Right(expectedResponseCYPlus1))

        val result = getATSDataMultipleYears(testNino, cy, cyPlus1)(request)

        status(result) mustBe BAD_REQUEST
      }

      "the service returns ok and bad request" in new TestController {

        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cy))(any[HeaderCarrier])) thenReturn Future.successful(
          Right(expectedResponseCY))
        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cyPlus1))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(HttpResponse(BAD_REQUEST, "Bad request")))

        val result = getATSDataMultipleYears(testNino, cy, cyPlus1)(request)

        status(result) mustBe BAD_REQUEST
      }

      "the service returns bad request and not found" in new TestController {

        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cy))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(HttpResponse(BAD_REQUEST, "Bad request")))
        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cyPlus1))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(HttpResponse(NOT_FOUND, "NOT_FOUND")))

        val result = getATSDataMultipleYears(testNino, cy, cyPlus1)(request)

        status(result) mustBe BAD_REQUEST
      }

      "the service returns not found and bad request" in new TestController {

        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cy))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(HttpResponse(NOT_FOUND, "Not found")))
        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cyPlus1))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(HttpResponse(BAD_REQUEST, "Bad request")))

        val result = getATSDataMultipleYears(testNino, cy, cyPlus1)(request)

        status(result) mustBe BAD_REQUEST
      }

      "the service returns bad request (cy) and internal server error (cy +1)" in new TestController {

        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cy))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(HttpResponse(BAD_REQUEST, "Bad request")))
        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cyPlus1))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(HttpResponse(INTERNAL_SERVER_ERROR, "Internal server error")))

        val result = getATSDataMultipleYears(testNino, cy, cyPlus1)(request)

        status(result) mustBe BAD_REQUEST
      }
    }

    "return INTERNAL_SERVER_ERROR" when {

      "the service returns internal server error" in new TestController {

        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cy))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(HttpResponse(INTERNAL_SERVER_ERROR, "Internal server error")))
        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cyPlus1))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(HttpResponse(INTERNAL_SERVER_ERROR, "Internal server error")))

        val result = getATSDataMultipleYears(testNino, cy, cyPlus1)(request)

        status(result) mustBe INTERNAL_SERVER_ERROR
      }

      "an exception occurs when retrieving data" in new TestController {

        val exMessage = "An error occurred"

        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cy))(any[HeaderCarrier])) thenReturn Future.failed(
          new Exception(exMessage))

        val result = getATSDataMultipleYears(testNino, cy, cyPlus1)(request)

        status(result) mustBe INTERNAL_SERVER_ERROR
        contentAsString(result) mustBe exMessage
      }

      "the service returns internal server error and not found" in new TestController {

        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cy))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(HttpResponse(INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR")))
        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cyPlus1))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(HttpResponse(NOT_FOUND, "Not found")))

        val result = getATSDataMultipleYears(testNino, cy, cyPlus1)(request)

        status(result) mustBe INTERNAL_SERVER_ERROR
      }

      "the service returns not found and internal server error" in new TestController {

        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cy))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(HttpResponse(NOT_FOUND, "Not found")))
        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cyPlus1))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(HttpResponse(INTERNAL_SERVER_ERROR, "Internal server error")))

        val result = getATSDataMultipleYears(testNino, cy, cyPlus1)(request)

        status(result) mustBe INTERNAL_SERVER_ERROR
      }

      "the service returns internal server error and Ok" in new TestController {

        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cy))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(HttpResponse(INTERNAL_SERVER_ERROR, "Internal server error")))
        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cyPlus1))(any[HeaderCarrier])) thenReturn Future.successful(
          Right(expectedResponseCYPlus1))

        val result = getATSDataMultipleYears(testNino, cy, cyPlus1)(request)

        status(result) mustBe INTERNAL_SERVER_ERROR

      }

      "the service returns ok and internal server error" in new TestController {

        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cy))(any[HeaderCarrier])) thenReturn Future.successful(
          Right(expectedResponseCY))
        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cyPlus1))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(HttpResponse(INTERNAL_SERVER_ERROR, "Internal server error")))

        val result = getATSDataMultipleYears(testNino, cy, cyPlus1)(request)

        status(result) mustBe INTERNAL_SERVER_ERROR

      }

      "the service returns internal server error (cy) and bad request (cy +1)" in new TestController {

        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cy))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(HttpResponse(INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR")))
        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cyPlus1))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(HttpResponse(BAD_REQUEST, "Bad request")))

        val result = getATSDataMultipleYears(testNino, cy, cyPlus1)(request)

        status(result) mustBe INTERNAL_SERVER_ERROR
      }
    }
  }
}
