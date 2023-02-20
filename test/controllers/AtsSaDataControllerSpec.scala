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

import cats.data.EitherT
import controllers.auth.FakeAuthAction
import org.mockito.ArgumentMatchers.{eq => eqTo, _}
import org.scalatest.time.{Millis, Seconds, Span}
import play.api.http.Status._
import play.api.libs.json.JsString
import play.api.mvc.{AnyContentAsEmpty, ControllerComponents}
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsJson, contentAsString, defaultAwaitTimeout, status, stubControllerComponents}
import services.OdsService
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import utils.TestConstants._
import utils.{ATSErrorHandler, BaseSpec}

import scala.concurrent.ExecutionContext

class AtsSaDataControllerSpec extends BaseSpec {

  lazy val cc: ControllerComponents = stubControllerComponents()

  implicit lazy val ec: ExecutionContext = inject[ExecutionContext]

  lazy val atsErrorHandler: ATSErrorHandler = inject[ATSErrorHandler]

  implicit val defaultPatience: PatienceConfig =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(500, Millis))

  val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

  val odsService: OdsService = mock[OdsService]
  val controller             = new AtsSaDataController(odsService, atsErrorHandler, FakeAuthAction, cc)

  val taxYear        = 2021
  val json: JsString = JsString("success")

  "getAtsData" must {

    "return 200" when {

      "the service returns a right" in {

        when(odsService.getPayload(eqTo(testUtr), eqTo(taxYear))(any[HeaderCarrier])).thenReturn(EitherT.rightT(json))
        val result = controller.getAtsSaData(testUtr, taxYear)(request)

        status(result) mustBe OK
        contentAsJson(result) mustBe json
      }
    }

    "return 404" when {

      "connector returns a left with NotFoundError" in {

        val msg = "Record not found"

        when(odsService.getPayload(eqTo(testUtr), eqTo(taxYear))(any[HeaderCarrier]))
          .thenReturn(EitherT.leftT(UpstreamErrorResponse(msg, NOT_FOUND, INTERNAL_SERVER_ERROR)))

        val result = controller.getAtsSaData(testUtr, taxYear)(request)

        status(result) mustBe NOT_FOUND
        contentAsString(result) mustBe msg
      }
    }

    "return 400" when {

      "connector returns a left with BadRequestError" in {

        val msg = "Record not found"

        when(odsService.getPayload(eqTo(testUtr), eqTo(taxYear))(any[HeaderCarrier]))
          .thenReturn(EitherT.leftT(UpstreamErrorResponse(msg, BAD_REQUEST, INTERNAL_SERVER_ERROR)))

        val result = controller.getAtsSaData(testUtr, taxYear)(request)

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe msg
      }
    }

    "return 500" when {
      List(401, 403, 409, 412).foreach { statusCode =>
        s"connector returns a $statusCode response" in {

          val upstreamError = UpstreamErrorResponse("Something went wrong", statusCode, INTERNAL_SERVER_ERROR)

          when(odsService.getPayload(eqTo(testUtr), eqTo(taxYear))(any[HeaderCarrier]))
            .thenReturn(EitherT.leftT(upstreamError))

          val result = controller.getAtsSaData(testUtr, taxYear)(request)

          status(result) mustBe INTERNAL_SERVER_ERROR
          contentAsString(result) mustBe upstreamError.getMessage
        }
      }
    }

    "return 502" when {

      List(500, 502, 503, 504).foreach { statusCode =>
        s"connector returns a $statusCode response" in {

          val upstreamError = UpstreamErrorResponse("Something went wrong", statusCode, BAD_GATEWAY)

          when(odsService.getPayload(eqTo(testUtr), eqTo(taxYear))(any[HeaderCarrier]))
            .thenReturn(EitherT.leftT(upstreamError))

          val result = controller.getAtsSaData(testUtr, taxYear)(request)

          status(result) mustBe BAD_GATEWAY
          contentAsString(result) mustBe upstreamError.getMessage
        }
      }
    }
  }

  "hasAts" must {

    "return 200" when {

      "the service returns a right" in {

        when(odsService.getList(eqTo(testUtr))(any[HeaderCarrier])).thenReturn(EitherT.rightT(json))

        val result = controller.hasAts(testUtr)(request)

        status(result) mustBe OK
        contentAsJson(result) mustBe json
      }
    }

    "return 404" when {

      "connector returns a left with NotFoundError" in {

        val upstreamError = UpstreamErrorResponse("Record not found", NOT_FOUND, INTERNAL_SERVER_ERROR)

        when(odsService.getList(eqTo(testUtr))(any[HeaderCarrier])).thenReturn(EitherT.leftT(upstreamError))

        val result = controller.hasAts(testUtr)(request)

        status(result) mustBe NOT_FOUND
        contentAsString(result) mustBe upstreamError.getMessage
      }
    }

    "return 400" when {

      "connector returns a left with BadRequestError" in {

        val upstreamError = UpstreamErrorResponse("Bad request", BAD_REQUEST, INTERNAL_SERVER_ERROR)

        when(odsService.getList(eqTo(testUtr))(any[HeaderCarrier])).thenReturn(EitherT.leftT(upstreamError))

        val result = controller.hasAts(testUtr)(request)

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe upstreamError.getMessage
      }
    }

    "return 500" when {
      List(401, 403, 409, 412).foreach { statusCode =>
        s"connector returns a status $statusCode" in {

          val upstreamError = UpstreamErrorResponse("Something went wrong", statusCode, INTERNAL_SERVER_ERROR)

          when(odsService.getList(eqTo(testUtr))(any[HeaderCarrier])).thenReturn(EitherT.leftT(upstreamError))

          val result = controller.hasAts(testUtr)(request)

          status(result) mustBe INTERNAL_SERVER_ERROR
          contentAsString(result) mustBe upstreamError.getMessage()
        }
      }
    }

    "return 502" when {

      List(500, 502, 503, 504).foreach { statusCode =>
        s"connector returns status $statusCode" in {

          val upstreamError = UpstreamErrorResponse("Something went wrong", statusCode, INTERNAL_SERVER_ERROR)

          when(odsService.getList(eqTo(testUtr))(any[HeaderCarrier])).thenReturn(EitherT.leftT(upstreamError))

          val result = controller.hasAts(testUtr)(request)

          status(result) mustBe BAD_GATEWAY
          contentAsString(result) mustBe upstreamError.getMessage
        }
      }
    }
  }

  "getATSList" must {

    "return 200" when {

      "connector returns a right" in {

        when(odsService.getATSList(eqTo(testUtr))(any[HeaderCarrier])).thenReturn(EitherT.rightT(json))

        val result = controller.getAtsSaList(testUtr)(request)

        status(result) mustBe OK
        contentAsJson(result) mustBe json
      }
    }

    "return 404" when {

      "connector returns a left with NotFoundError" in {

        val upstreamError = UpstreamErrorResponse("NoAtaData", NOT_FOUND, INTERNAL_SERVER_ERROR)

        when(odsService.getATSList(eqTo(testUtr))(any[HeaderCarrier])).thenReturn(EitherT.leftT(upstreamError))

        val result = controller.getAtsSaList(testUtr)(request)

        status(result) mustBe NOT_FOUND
        contentAsString(result) mustBe upstreamError.getMessage
      }
    }

    "return 400" when {

      "connector returns a left with BadRequestError" in {

        val upstreamError = UpstreamErrorResponse("Bad request", BAD_REQUEST, INTERNAL_SERVER_ERROR)

        when(odsService.getATSList(eqTo(testUtr))(any[HeaderCarrier])).thenReturn(EitherT.leftT(upstreamError))

        val result = controller.getAtsSaList(testUtr)(request)

        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustBe upstreamError.getMessage
      }
    }

    "return 500" when {
      List(401, 403, 409, 412).foreach { statusCode =>
        s"connector returns a status $statusCode" in {

          val upstreamError = UpstreamErrorResponse("Error", statusCode, INTERNAL_SERVER_ERROR)

          when(odsService.getATSList(eqTo(testUtr))(any[HeaderCarrier])).thenReturn(EitherT.leftT(upstreamError))

          val result = controller.getAtsSaList(testUtr)(request)

          status(result) mustBe INTERNAL_SERVER_ERROR
          contentAsString(result) mustBe upstreamError.getMessage
        }
      }
    }

    "return 502" when {
      List(500, 501, 502, 503, 504).foreach { statusCode =>
        s"connector returns a status $statusCode" in {

          val upstreamError = UpstreamErrorResponse("Error", statusCode, BAD_GATEWAY)

          when(odsService.getATSList(eqTo(testUtr))(any[HeaderCarrier])).thenReturn(EitherT.leftT(upstreamError))

          val result = controller.getAtsSaList(testUtr)(request)

          status(result) mustBe BAD_GATEWAY
          contentAsString(result) mustBe upstreamError.getMessage
        }
      }
    }
  }
}