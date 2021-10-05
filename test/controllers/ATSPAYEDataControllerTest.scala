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

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import controllers.auth.{FakeAuthAction, PayeAuthAction}
import models.paye.PayeAtsMiddleTier
import models.{BadRequestError, DownstreamClientError, DownstreamServerError, NotFoundError}
import org.mockito.Matchers.{eq => eqTo, _}
import org.mockito.Mockito._
import play.api.http.Status.BAD_REQUEST
import play.api.libs.json.Json
import play.api.mvc.ControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsJson, contentAsString, defaultAwaitTimeout, status, stubControllerComponents}
import services.NpsService
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import utils.TestConstants._
import utils.{ATSErrorHandler, BaseSpec}

import scala.concurrent.{ExecutionContext, Future}

class ATSPAYEDataControllerTest extends BaseSpec {

  val cc: ControllerComponents = stubControllerComponents()
  implicit val ec: ExecutionContext = cc.executionContext
  implicit val actorSystem: ActorSystem = ActorSystem()
  implicit val mat: Materializer = ActorMaterializer()
  val request = FakeRequest()

  val npsService: NpsService = mock[NpsService]
  val payeAuthAction: PayeAuthAction = FakeAuthAction
  lazy val atsErrorHandler = inject[ATSErrorHandler]

  class TestController extends ATSPAYEDataController(npsService, payeAuthAction, atsErrorHandler, cc)

  val cy = 2018
  val cyPlus1 = 2019

  val expectedResponseCY = PayeAtsMiddleTier(cy, testNino, None, None, None, None, None)
  val expectedResponseCYPlus1 = PayeAtsMiddleTier(cyPlus1, testNino, None, None, None, None, None)

  val downstreamClientError: DownstreamClientError =
    DownstreamClientError("", UpstreamErrorResponse("", BAD_REQUEST, BAD_REQUEST))

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
        .thenReturn(Future.successful(Left(BadRequestError(errorMessage))))
      val result = getATSData(testNino, cy)(request)

      status(result) mustBe BAD_REQUEST
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
          Left(NotFoundError("")))

        val result = getATSDataMultipleYears(testNino, cy, cyPlus1)(request)

        status(result) mustBe OK
        contentAsJson(result) mustBe Json.toJson(Seq(expectedResponseCY))

      }

      "the service returns not found and ok" in new TestController {

        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cy))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(NotFoundError("")))
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
          Left(NotFoundError("")))
        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cyPlus1))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(NotFoundError("")))

        val result = getATSDataMultipleYears(testNino, cy, cyPlus1)(request)

        status(result) mustBe NOT_FOUND
      }
    }

    "return BAD_REQUEST" when {

      "the service returns bad request" in new TestController {

        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cy))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(BadRequestError("")))
        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cyPlus1))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(BadRequestError("")))

        val result = getATSDataMultipleYears(testNino, cy, cyPlus1)(request)

        status(result) mustBe BAD_REQUEST
      }

      "the service returns bad request and ok" in new TestController {

        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cy))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(BadRequestError("")))
        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cyPlus1))(any[HeaderCarrier])) thenReturn Future.successful(
          Right(expectedResponseCYPlus1))

        val result = getATSDataMultipleYears(testNino, cy, cyPlus1)(request)

        status(result) mustBe BAD_REQUEST
      }

      "the service returns ok and bad request" in new TestController {

        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cy))(any[HeaderCarrier])) thenReturn Future.successful(
          Right(expectedResponseCY))
        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cyPlus1))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(BadRequestError("")))

        val result = getATSDataMultipleYears(testNino, cy, cyPlus1)(request)

        status(result) mustBe BAD_REQUEST
      }

      "the service returns bad request and not found" in new TestController {

        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cy))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(BadRequestError("")))
        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cyPlus1))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(NotFoundError("")))

        val result = getATSDataMultipleYears(testNino, cy, cyPlus1)(request)

        status(result) mustBe BAD_REQUEST
      }

      "the service returns not found and bad request" in new TestController {

        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cy))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(NotFoundError("")))
        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cyPlus1))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(BadRequestError("")))

        val result = getATSDataMultipleYears(testNino, cy, cyPlus1)(request)

        status(result) mustBe BAD_REQUEST
      }

      "the service returns bad request (cy) and internal server error (cy +1)" in new TestController {

        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cy))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(BadRequestError("")))
        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cyPlus1))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(BadRequestError("")))

        val result = getATSDataMultipleYears(testNino, cy, cyPlus1)(request)

        status(result) mustBe BAD_REQUEST
      }
    }

    "return INTERNAL_SERVER_ERROR" when {

      "the service returns internal server error" in new TestController {

        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cy))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(DownstreamServerError("")))
        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cyPlus1))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(DownstreamServerError("")))

        val result = getATSDataMultipleYears(testNino, cy, cyPlus1)(request)

        status(result) mustBe BAD_GATEWAY
      }

      "the service returns internal server error and not found" in new TestController {

        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cy))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(DownstreamServerError("")))
        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cyPlus1))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(NotFoundError("")))

        val result = getATSDataMultipleYears(testNino, cy, cyPlus1)(request)

        status(result) mustBe BAD_GATEWAY
      }

      "the service returns not found and internal server error" in new TestController {

        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cy))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(NotFoundError("")))
        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cyPlus1))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(DownstreamServerError("")))

        val result = getATSDataMultipleYears(testNino, cy, cyPlus1)(request)

        status(result) mustBe BAD_GATEWAY
      }

      "the service returns internal server error and Ok" in new TestController {

        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cy))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(DownstreamServerError("")))
        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cyPlus1))(any[HeaderCarrier])) thenReturn Future.successful(
          Right(expectedResponseCYPlus1))

        val result = getATSDataMultipleYears(testNino, cy, cyPlus1)(request)

        status(result) mustBe BAD_GATEWAY

      }

      "the service returns ok and internal server error" in new TestController {

        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cy))(any[HeaderCarrier])) thenReturn Future.successful(
          Right(expectedResponseCY))
        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cyPlus1))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(DownstreamServerError("")))

        val result = getATSDataMultipleYears(testNino, cy, cyPlus1)(request)

        status(result) mustBe BAD_GATEWAY

      }

      "the service returns internal server error (cy) and bad request (cy +1)" in new TestController {

        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cy))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(DownstreamServerError("")))
        when(npsService.getPayeATSData(eqTo(testNino), eqTo(cyPlus1))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(BadRequestError("")))

        val result = getATSDataMultipleYears(testNino, cy, cyPlus1)(request)

        status(result) mustBe BAD_GATEWAY
      }
    }
  }
}
