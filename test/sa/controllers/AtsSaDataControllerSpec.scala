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

package sa.controllers

import cats.data.EitherT
import common.controllers.auth.FakeAuthAction
import common.utils.{ATSErrorHandler, BaseSpec}
import common.utils.TestConstants._
import org.mockito.ArgumentMatchers.{eq => eqTo, _}
import org.scalatest.time.{Millis, Seconds, Span}
import play.api.http.Status._
import play.api.libs.json.{JsString, Json}
import play.api.mvc.{AnyContentAsEmpty, ControllerComponents, Request}
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsJson, contentAsString, defaultAwaitTimeout, status, stubControllerComponents}
import sa.services.OdsService
import sa.utils.TaxsJsonHelper
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}

import scala.concurrent.ExecutionContext
import scala.io.Source

class AtsSaDataControllerSpec extends BaseSpec {

  lazy val cc: ControllerComponents = stubControllerComponents()

  implicit lazy val ec: ExecutionContext = inject[ExecutionContext]

  lazy val atsErrorHandler: ATSErrorHandler = inject[ATSErrorHandler]

  implicit val defaultPatience: PatienceConfig =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(500, Millis))

  val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

  val odsService: OdsService     = mock[OdsService]
  val jsonHelper: TaxsJsonHelper = inject[TaxsJsonHelper]

  val controller = new AtsSaDataController(
    odsService,
    atsErrorHandler,
    FakeAuthAction,
    cc,
    jsonHelper
  )

  val taxYear        = 2021
  val json: JsString = JsString("success")

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(odsService)
  }

  "getAtsData" must {

    "return 200" when {
      "service calls ods service" in {
        when(odsService.getPayload(eqTo(testUtr), eqTo(taxYear))(any[HeaderCarrier], any[Request[_]]))
          .thenReturn(EitherT.rightT(json))
        val result = controller.getAtsSaData(testUtr, taxYear)(request)

        status(result) mustBe OK
        contentAsJson(result) mustBe json
      }
    }

    "return 404" when {

      "connector returns a left with NotFoundError" in {

        val msg = "Record not found"

        when(odsService.getPayload(eqTo(testUtr), eqTo(taxYear))(any[HeaderCarrier], any[Request[_]]))
          .thenReturn(EitherT.leftT(UpstreamErrorResponse(msg, NOT_FOUND, INTERNAL_SERVER_ERROR)))

        val result = controller.getAtsSaData(testUtr, taxYear)(request)

        status(result) mustBe NOT_FOUND
        contentAsString(result) mustBe msg
      }
    }

    "return 400" when {

      "connector returns a left with BadRequestError" in {

        val msg = "Record not found"

        when(odsService.getPayload(eqTo(testUtr), eqTo(taxYear))(any[HeaderCarrier], any[Request[_]]))
          .thenReturn(EitherT.leftT(UpstreamErrorResponse(msg, BAD_REQUEST, INTERNAL_SERVER_ERROR)))

        val result = controller.getAtsSaData(testUtr, taxYear)(request)

        status(result) mustBe INTERNAL_SERVER_ERROR
        contentAsString(result) mustBe msg
      }
    }

    "return 500" when {
      List(401, 403, 409, 412).foreach { statusCode =>
        s"connector returns a $statusCode response" in {

          val upstreamError = UpstreamErrorResponse("Something went wrong", statusCode, INTERNAL_SERVER_ERROR)

          when(odsService.getPayload(eqTo(testUtr), eqTo(taxYear))(any[HeaderCarrier], any[Request[_]]))
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

          when(odsService.getPayload(eqTo(testUtr), eqTo(taxYear))(any[HeaderCarrier], any[Request[_]]))
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

        when(odsService.getList(eqTo(testUtr))(any[HeaderCarrier], any())).thenReturn(EitherT.rightT(json))

        val result = controller.hasAts(testUtr)(request)

        status(result) mustBe OK
        contentAsJson(result) mustBe json
      }
    }

    "return 404" when {

      "connector returns a left with NotFoundError" in {

        val upstreamError = UpstreamErrorResponse("Record not found", NOT_FOUND, INTERNAL_SERVER_ERROR)

        when(odsService.getList(eqTo(testUtr))(any[HeaderCarrier], any())).thenReturn(EitherT.leftT(upstreamError))

        val result = controller.hasAts(testUtr)(request)

        status(result) mustBe NOT_FOUND
        contentAsString(result) mustBe upstreamError.getMessage
      }
    }

    "return 400" when {

      "connector returns a left with BadRequestError" in {

        val upstreamError = UpstreamErrorResponse("Bad request", BAD_REQUEST, INTERNAL_SERVER_ERROR)

        when(odsService.getList(eqTo(testUtr))(any[HeaderCarrier], any())).thenReturn(EitherT.leftT(upstreamError))

        val result = controller.hasAts(testUtr)(request)

        status(result) mustBe INTERNAL_SERVER_ERROR
        contentAsString(result) mustBe upstreamError.getMessage
      }
    }

    "return 500" when {
      List(401, 403, 409, 412).foreach { statusCode =>
        s"connector returns a status $statusCode" in {

          val upstreamError = UpstreamErrorResponse("Something went wrong", statusCode, INTERNAL_SERVER_ERROR)

          when(odsService.getList(eqTo(testUtr))(any[HeaderCarrier], any())).thenReturn(EitherT.leftT(upstreamError))

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

          when(odsService.getList(eqTo(testUtr))(any[HeaderCarrier], any())).thenReturn(EitherT.leftT(upstreamError))

          val result = controller.hasAts(testUtr)(request)

          status(result) mustBe BAD_GATEWAY
          contentAsString(result) mustBe upstreamError.getMessage
        }
      }
    }
  }

  "getATSSAList" must {

    "return 200" when {

      "service returns a right" in {
        val taxPayerSource = Source.fromURL(getClass.getResource("/common/taxpayer/sa_taxpayer-valid.json"))
        val taxPayer       = taxPayerSource.mkString
        taxPayerSource.close()
        when(odsService.connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier], any()))
          .thenReturn(EitherT.rightT(Json.parse(taxPayer)))
        when(odsService.getATSList(eqTo(testUtr), any(), any())(any[HeaderCarrier], any()))
          .thenReturn(EitherT.rightT(Seq(2020)))

        val result = controller.getAtsSaList(testUtr, 2021, 5)(request)

        status(result) mustBe OK
        contentAsString(
          result
        ) mustBe s"""{"utr":"$testUtr","taxPayer":{"title":"Miss","forename":"Jane","surname":"Fisher"},"atsYearList":[2020]}"""
      }
    }

    "return 404" when {

      "connector returns a left with NotFoundError" in {

        val upstreamError = UpstreamErrorResponse("NoAtaData", NOT_FOUND, INTERNAL_SERVER_ERROR)

        when(odsService.getATSList(eqTo(testUtr), any(), any())(any[HeaderCarrier], any()))
          .thenReturn(EitherT.leftT(upstreamError))

        val result = controller.getAtsSaList(testUtr, 2021, 5)(request)

        status(result) mustBe NOT_FOUND
        contentAsString(result) mustBe upstreamError.getMessage
      }
    }

    "return 400" when {

      "connector returns a left with BadRequestError" in {

        val upstreamError = UpstreamErrorResponse("Bad request", BAD_REQUEST, INTERNAL_SERVER_ERROR)

        when(odsService.getATSList(eqTo(testUtr), any(), any())(any[HeaderCarrier], any()))
          .thenReturn(EitherT.leftT(upstreamError))

        val result = controller.getAtsSaList(testUtr, 2021, 5)(request)

        status(result) mustBe INTERNAL_SERVER_ERROR
        contentAsString(result) mustBe upstreamError.getMessage
      }
    }

    "return 500" when {
      List(401, 403, 409, 412).foreach { statusCode =>
        s"connector returns a status $statusCode" in {
          val upstreamError = UpstreamErrorResponse("Error", statusCode, INTERNAL_SERVER_ERROR)

          when(odsService.getATSList(eqTo(testUtr), any(), any())(any[HeaderCarrier], any()))
            .thenReturn(EitherT.leftT(upstreamError))

          val result = controller.getAtsSaList(testUtr, 2021, 5)(request)

          status(result) mustBe INTERNAL_SERVER_ERROR
          contentAsString(result) mustBe upstreamError.getMessage
        }
      }
    }

    "return 502" when {
      List(500, 501, 502, 503, 504).foreach { statusCode =>
        s"connector returns a status $statusCode" in {

          val upstreamError = UpstreamErrorResponse("", statusCode, statusCode)

          when(odsService.getATSList(eqTo(testUtr), any(), any())(any[HeaderCarrier], any()))
            .thenReturn(EitherT.leftT(upstreamError))

          val result = controller.getAtsSaList(testUtr, 2021, 5)(request)

          status(result) mustBe BAD_GATEWAY
          contentAsString(result) mustBe ""
        }
      }
    }
  }
}
