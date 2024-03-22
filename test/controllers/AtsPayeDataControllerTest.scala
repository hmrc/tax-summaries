/*
 * Copyright 2023 HM Revenue & Customs
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
import akka.stream.Materializer
import cats.data.EitherT
import controllers.auth.{AuthJourney, FakeAuthAction, FakeAuthJourney, PayeAuthAction}
import models.paye.PayeAtsMiddleTier
import org.mockito.ArgumentMatchers.{eq => eqTo, _}
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, LOCKED, NOT_FOUND}
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, ControllerComponents, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsJson, contentAsString, defaultAwaitTimeout, status, stubControllerComponents}
import services.NpsService
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import utils.TestConstants._
import utils.{ATSErrorHandler, BaseSpec}

import scala.concurrent.{ExecutionContext, Future}

class AtsPayeDataControllerTest extends BaseSpec {

  val cc: ControllerComponents                     = stubControllerComponents()
  implicit val ec: ExecutionContext                = cc.executionContext
  implicit val actorSystem: ActorSystem            = ActorSystem()
  implicit val mat: Materializer                   = app.materializer
  val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

  val npsService: NpsService                = mock[NpsService]
  val payeAuthAction: PayeAuthAction        = FakeAuthAction
  lazy val atsErrorHandler: ATSErrorHandler = inject[ATSErrorHandler]
  val mockAuthJourney: AuthJourney          = FakeAuthJourney

  class TestController extends AtsPayeDataController(npsService, mockAuthJourney, atsErrorHandler, cc)

  val cy      = 2022
  val cyPlus1 = 2023

  val expectedResponseCY: PayeAtsMiddleTier      = PayeAtsMiddleTier(cy, testNino, None, None, None, None, None)
  val expectedResponseCYPlus1: PayeAtsMiddleTier = PayeAtsMiddleTier(cyPlus1, testNino, None, None, None, None, None)

  val notFoundError: UpstreamErrorResponse         = UpstreamErrorResponse("Not found", NOT_FOUND, INTERNAL_SERVER_ERROR)
  val badRequestError: UpstreamErrorResponse       = UpstreamErrorResponse("Bad request", BAD_REQUEST, INTERNAL_SERVER_ERROR)
  val downstreamClientError: UpstreamErrorResponse = UpstreamErrorResponse("", LOCKED, INTERNAL_SERVER_ERROR)
  val downstreamServerError: UpstreamErrorResponse =
    UpstreamErrorResponse("", INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR)

  override def beforeEach(): Unit = {
    reset(npsService)
    super.beforeEach()
  }

  "getAtsData" must {

    "return success response with ATS data" in new TestController {
      when(npsService.getPayeATSData(eqTo(testNino), eqTo(cy))(any[HeaderCarrier]))
        .thenReturn(EitherT.rightT(expectedResponseCY))
      val result: Future[Result] = getAtsPayeData(testNino, cy)(request)

      status(result) mustBe 200
      contentAsJson(result) mustBe Json.toJson(expectedResponseCY)
    }

    "return a failed future" in new TestController {
      when(npsService.getPayeATSData(eqTo(testNino), eqTo(cy))(any[HeaderCarrier]))
        .thenReturn(EitherT.leftT(badRequestError))
      val result: Future[Result] = getAtsPayeData(testNino, cy)(request)

      status(result) mustBe INTERNAL_SERVER_ERROR
      contentAsString(result) mustBe badRequestError.message
    }
  }

  "getATSDataMultipleYears" must {

    "return a list of data for the given tax years" in new TestController {
      when(npsService.getAtsPayeDataMultipleYears(any(), any())(any()))
        .thenReturn(EitherT.rightT(List(expectedResponseCY, expectedResponseCYPlus1)))

      val result: Future[Result] = getAtsPayeDataMultipleYears(testNino, cy, cyPlus1)(request)

      status(result) mustBe OK
      contentAsJson(result) mustBe Json.toJson(Seq(expectedResponseCY, expectedResponseCYPlus1))
    }

    "return NOT_FOUND" when {

      "the service returns no data" in new TestController {
        when(npsService.getAtsPayeDataMultipleYears(eqTo(testNino), any())(any[HeaderCarrier]))
          .thenReturn(EitherT.rightT(List.empty))

        val result: Future[Result] = getAtsPayeDataMultipleYears(testNino, cy, cyPlus1)(request)

        status(result) mustBe NOT_FOUND
      }
    }

    "return BAD_REQUEST" when {

      "the service returns bad request" in new TestController {
        when(npsService.getAtsPayeDataMultipleYears(eqTo(testNino), any())(any[HeaderCarrier]))
          .thenReturn(EitherT.leftT(badRequestError))

        val result: Future[Result] = getAtsPayeDataMultipleYears(testNino, cy, cyPlus1)(request)

        status(result) mustBe INTERNAL_SERVER_ERROR
      }
    }

    "return INTERNAL_SERVER_ERROR" when {

      "the service returns internal server error" in new TestController {
        when(npsService.getAtsPayeDataMultipleYears(eqTo(testNino), any())(any[HeaderCarrier]))
          .thenReturn(EitherT.leftT(downstreamServerError))

        val result: Future[Result] = getAtsPayeDataMultipleYears(testNino, cy, cyPlus1)(request)

        status(result) mustBe BAD_GATEWAY
      }
    }
  }
}
