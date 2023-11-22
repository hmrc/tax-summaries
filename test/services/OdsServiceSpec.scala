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

package services

import cats.data.EitherT
import cats.implicits._
import connectors.SelfAssessmentODSConnector
import models.{Amount, AtsCheck, PensionTaxRate, TaxSummaryLiability}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{eq => eqTo, _}
import play.api.http.Status._
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc.Request
import transformers.ATSCalculations
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, UpstreamErrorResponse}
import utils.TestConstants._
import utils.{BaseSpec, TaxsJsonHelper}
import uk.gov.hmrc.time.TaxYear

import java.time.LocalDate
import scala.concurrent.ExecutionContext

class OdsServiceSpec extends BaseSpec {

  implicit lazy val ec: ExecutionContext = inject[ExecutionContext]

  val odsConnector: SelfAssessmentODSConnector = mock[SelfAssessmentODSConnector]
  val jsonHelper: TaxsJsonHelper               = mock[TaxsJsonHelper]

  val service = new OdsService(jsonHelper, odsConnector)

  override def beforeEach(): Unit = {
    reset(odsConnector, jsonHelper)
    super.beforeEach()
  }

  //  "getPayload" must {
  //
  //    "return json" when {
  //      "the call is successful" in {
  //
  //        when(odsConnector.connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier], any()))
  //          .thenReturn(EitherT.rightT(HttpResponse(OK, "{}")))
  //        when(odsConnector.connectToSelfAssessment(eqTo(testUtr), eqTo(2014))(any[HeaderCarrier], any()))
  //          .thenReturn(EitherT.rightT(HttpResponse(OK, "{}")))
  //        when(jsonHelper.getAllATSData(any[JsValue], any[JsValue], eqTo(testUtr), eqTo(2014)))
  //          .thenReturn(mock[JsValue])
  //
  //        val result = service.getPayload(testUtr, 2014)(mock[HeaderCarrier], mock[Request[_]]).value
  //
  //        whenReady(result) { res =>
  //          res.isRight mustBe true
  //
  //          verify(jsonHelper, times(1)).getAllATSData(any[JsValue], any[JsValue], eqTo(testUtr), eqTo(2014))
  //        }
  //      }
  //    }
  //
  //    "return a UpstreamErrorResponse" when {
  //      "Not found response is received from self assessment" in {
  //        when(odsConnector.connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier], any()))
  //          .thenReturn(EitherT.rightT(HttpResponse(OK, "{}")))
  //        when(odsConnector.connectToSelfAssessment(eqTo(testUtr), eqTo(2014))(any[HeaderCarrier], any()))
  //          .thenReturn(EitherT.rightT(HttpResponse(NOT_FOUND, "")))
  //
  //        val result = service.getPayload(testUtr, 2014)(mock[HeaderCarrier], mock[Request[_]]).value
  //
  //        whenReady(result) { res =>
  //          res mustBe a[Left[UpstreamErrorResponse, _]]
  //
  //          verify(odsConnector).connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier], any())
  //          verify(odsConnector).connectToSelfAssessment(eqTo(testUtr), eqTo(2014))(any[HeaderCarrier], any())
  //          verify(jsonHelper, never).getAllATSData(any[JsValue], any[JsValue], eqTo(testUtr), eqTo(2014))
  //        }
  //      }
  //
  //      "Not found response is received from tax payer details" in {
  //        when(odsConnector.connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier], any()))
  //          .thenReturn(EitherT.rightT(HttpResponse(NOT_FOUND, "")))
  //        when(odsConnector.connectToSelfAssessment(eqTo(testUtr), eqTo(2014))(any[HeaderCarrier], any()))
  //          .thenReturn(EitherT.rightT(HttpResponse(OK, "{}")))
  //
  //        val result = service.getPayload(testUtr, 2014)(mock[HeaderCarrier], mock[Request[_]]).value
  //
  //        whenReady(result) { res =>
  //          res mustBe a[Left[UpstreamErrorResponse, _]]
  //
  //          verify(odsConnector).connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier], any())
  //          verify(odsConnector, times(0)).connectToSelfAssessment(eqTo(testUtr), eqTo(2014))(any[HeaderCarrier], any())
  //          verify(jsonHelper, never).getAllATSData(any[JsValue], any[JsValue], eqTo(testUtr), eqTo(2014))
  //        }
  //      }
  //
  //      List(400, 401, 403, 500, 501, 502, 503, 504).foreach { statusCode =>
  //        s"UpstreamErrorResponse with status $statusCode is received" in {
  //          val response = UpstreamErrorResponse("Not found", statusCode, INTERNAL_SERVER_ERROR)
  //
  //          when(odsConnector.connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier], any()))
  //            .thenReturn(EitherT.rightT(HttpResponse(OK, "{}")))
  //          when(odsConnector.connectToSelfAssessment(eqTo(testUtr), eqTo(2014))(any[HeaderCarrier], any()))
  //            .thenReturn(EitherT.leftT(response))
  //
  //          val result = service.getPayload(testUtr, 2014)(mock[HeaderCarrier], mock[Request[_]]).value
  //
  //          whenReady(result) { res =>
  //            res mustBe a[Left[UpstreamErrorResponse, _]]
  //
  //            verify(odsConnector).connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier], any())
  //            verify(odsConnector).connectToSelfAssessment(eqTo(testUtr), eqTo(2014))(any[HeaderCarrier], any())
  //            verify(jsonHelper, never).getAllATSData(any[JsValue], any[JsValue], eqTo(testUtr), eqTo(2014))
  //          }
  //        }
  //      }
  //    }
  //  }

  //  "getList" must {
  //
  //    "return json" when {
  //
  //      "connector calls are successful" in {
  //
  //        when(odsConnector.connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier], any()))
  //          .thenReturn(EitherT.rightT(HttpResponse(OK, "{}")))
  //        when(jsonHelper.hasAtsForPreviousPeriod(any[JsValue]))
  //          .thenReturn(true)
  //
  //        val result = service.getList(testUtr)(mock[HeaderCarrier], mock[Request[_]]).value
  //
  //        whenReady(result) { result =>
  //          result.isRight mustBe true
  //
  //          Json.fromJson[AtsCheck](result.getOrElse(JsObject.empty)).asOpt mustBe Some(AtsCheck(true))
  //        }
  //      }
  //    }
  //
  //    "return a UpstreamErrorResponse" when {
  //      "Not found response is received" in {
  //        when(odsConnector.connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier], any()))
  //          .thenReturn(EitherT.rightT(HttpResponse(OK, "{}")))
  //        when(odsConnector.connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier], any()))
  //          .thenReturn(EitherT.rightT(HttpResponse(NOT_FOUND, "")))
  //
  //        val result = service.getList(testUtr)(mock[HeaderCarrier], mock[Request[_]]).value
  //
  //        whenReady(result) { res =>
  //          res mustBe a[Left[UpstreamErrorResponse, _]]
  //
  //          verify(odsConnector, times(0)).connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier], any())
  //          verify(odsConnector).connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier], any())
  //          verify(jsonHelper, never).getAllATSData(any[JsValue], any[JsValue], eqTo(testUtr), eqTo(2014))
  //        }
  //      }
  //
  //      List(400, 401, 403, 500, 501, 502, 503, 504).foreach { statusCode =>
  //        s"UpstreamErrorResponse with status $statusCode is received" in {
  //          val response = UpstreamErrorResponse("Not found", statusCode, INTERNAL_SERVER_ERROR)
  //
  //          when(odsConnector.connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier], any()))
  //            .thenReturn(EitherT.leftT(response))
  //
  //          val result = service.getList(testUtr)(mock[HeaderCarrier], mock[Request[_]]).value
  //
  //          whenReady(result) { res =>
  //            res.swap.getOrElse(UpstreamErrorResponse("", IM_A_TEAPOT)) mustBe response
  //
  //            verify(jsonHelper, never).hasAtsForPreviousPeriod(any[JsValue])
  //          }
  //        }
  //      }
  //    }
  //  }

  private val currentTaxYear = TaxYear.current.currentYear

  private def saResponse(taxYear: Int): JsValue = Json.obj("taxYear" -> taxYear)

  private val mockTaxRateService = mock[TaxRateService]

  private def atsCalculations(taxYear: Int, amount: BigDecimal) =
    new ATSCalculations {
      override protected val summaryData: TaxSummaryLiability = TaxSummaryLiability(
        taxYear,
        PensionTaxRate(0),
        None,
        Map.empty,
        Map.empty
      )
      override protected val taxRates: TaxRateService         = mockTaxRateService

      override def taxLiability: Amount = Amount(amount, "GBP")
    }

  "getATSList" must {
    "return years list minus any years where no tax data or tax liability found" in {
      when(odsConnector.connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier], any()))
        .thenReturn(EitherT.rightT(HttpResponse(OK, "{}")))

      val taxYearJson = Json.obj("test" -> "test")
      when(jsonHelper.createTaxYearJsonNew(any, eqTo(testUtr), any[JsValue]))
        .thenReturn(taxYearJson)

      when(odsConnector.connectToSelfAssessment(eqTo(testUtr), eqTo(currentTaxYear))(any[HeaderCarrier], any()))
        .thenReturn(EitherT.rightT(HttpResponse(OK, saResponse(currentTaxYear), Map.empty)))
      when(odsConnector.connectToSelfAssessment(eqTo(testUtr), eqTo(currentTaxYear - 1))(any[HeaderCarrier], any()))
        .thenReturn(EitherT.rightT(HttpResponse(OK, saResponse(currentTaxYear - 1), Map.empty)))
      when(odsConnector.connectToSelfAssessment(eqTo(testUtr), eqTo(currentTaxYear - 2))(any[HeaderCarrier], any()))
        .thenReturn(EitherT.leftT(UpstreamErrorResponse("", NOT_FOUND)))
      when(odsConnector.connectToSelfAssessment(eqTo(testUtr), eqTo(currentTaxYear - 3))(any[HeaderCarrier], any()))
        .thenReturn(EitherT.rightT(HttpResponse(OK, saResponse(currentTaxYear - 3), Map.empty)))
      when(odsConnector.connectToSelfAssessment(eqTo(testUtr), eqTo(currentTaxYear - 4))(any[HeaderCarrier], any()))
        .thenReturn(EitherT.rightT(HttpResponse(OK, saResponse(currentTaxYear - 4), Map.empty)))

      when(jsonHelper.getATSCalculations(eqTo(currentTaxYear), eqTo(saResponse(currentTaxYear))))
        .thenReturn(atsCalculations(currentTaxYear, 100))
      when(jsonHelper.getATSCalculations(eqTo(currentTaxYear - 1), eqTo(saResponse(currentTaxYear - 1))))
        .thenReturn(atsCalculations(currentTaxYear, 0))
      when(jsonHelper.getATSCalculations(eqTo(currentTaxYear - 3), eqTo(saResponse(currentTaxYear - 3))))
        .thenReturn(atsCalculations(currentTaxYear, 60))
      when(jsonHelper.getATSCalculations(eqTo(currentTaxYear - 4), eqTo(saResponse(currentTaxYear - 4))))
        .thenReturn(atsCalculations(currentTaxYear, 40))

      val result =
        service.getATSList(testUtr, currentTaxYear - 4, currentTaxYear)(mock[HeaderCarrier], mock[Request[_]]).value

      whenReady(result) { result =>
        result mustBe Right(taxYearJson)
        verify(odsConnector, times(1)).connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier], any())
        val expectedSeqYears = Seq(currentTaxYear - 4, currentTaxYear - 3, currentTaxYear)
        verify(jsonHelper, times(1)).createTaxYearJsonNew(eqTo(expectedSeqYears), eqTo(testUtr), any[JsValue])
      }
    }
  }

  //  "getATSList" must {
  //
  //    "return a right" in {
  //
  //      val json = mock[JsValue]
  //
  //      when(odsConnector.connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier], any()))
  //        .thenReturn(EitherT.rightT(HttpResponse(OK, "{}")))
  //      when(odsConnector.connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier], any()))
  //        .thenReturn(EitherT.rightT(HttpResponse(OK, "{}")))
  //      when(jsonHelper.createTaxYearJson(any[JsValue], eqTo(testUtr), any[JsValue]))
  //        .thenReturn(json)
  //
  //      val result = service.getATSList(testUtr)(mock[HeaderCarrier], mock[Request[_]]).value
  //
  //      whenReady(result) { result =>
  //        result mustBe Right(json)
  //
  //        verify(odsConnector, times(1)).connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier], any())
  //        verify(odsConnector, times(1)).connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier], any())
  //        verify(jsonHelper, times(1)).createTaxYearJson(any[JsValue], eqTo(testUtr), any[JsValue])
  //      }
  //    }
  //
  //    "return a UpstreamErrorResponse" when {
  //      "Not found response is received from self assessment" in {
  //        when(odsConnector.connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier], any()))
  //          .thenReturn(EitherT.rightT(HttpResponse(OK, "{}")))
  //        when(odsConnector.connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier], any()))
  //          .thenReturn(EitherT.rightT(HttpResponse(NOT_FOUND, "")))
  //
  //        val result = service.getATSList(testUtr)(mock[HeaderCarrier], mock[Request[_]]).value
  //
  //        whenReady(result) { res =>
  //          res mustBe a[Left[UpstreamErrorResponse, _]]
  //
  //          verify(odsConnector, times(0)).connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier], any())
  //          verify(odsConnector).connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier], any())
  //          verify(jsonHelper, never).getAllATSData(any[JsValue], any[JsValue], eqTo(testUtr), eqTo(2014))
  //        }
  //      }
  //
  //      "Not found response is received from tax payer details" in {
  //        when(odsConnector.connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier], any()))
  //          .thenReturn(EitherT.rightT(HttpResponse(NOT_FOUND, "")))
  //        when(odsConnector.connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier], any()))
  //          .thenReturn(EitherT.rightT(HttpResponse(OK, "{}")))
  //
  //        val result = service.getATSList(testUtr)(mock[HeaderCarrier], mock[Request[_]]).value
  //
  //        whenReady(result) { res =>
  //          res mustBe a[Left[UpstreamErrorResponse, _]]
  //
  //          verify(odsConnector, times(1)).connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier], any())
  //          verify(odsConnector).connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier], any())
  //          verify(jsonHelper, never).getAllATSData(any[JsValue], any[JsValue], eqTo(testUtr), eqTo(2014))
  //        }
  //      }
  //
  //      List(400, 401, 403, 500, 501, 502, 503, 504).foreach { statusCode =>
  //        s"UpstreamErrorResponse with status $statusCode is received when getting SA json" in {
  //          val response = UpstreamErrorResponse("Not found", statusCode, INTERNAL_SERVER_ERROR)
  //
  //          when(odsConnector.connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier], any()))
  //            .thenReturn(EitherT.leftT(response))
  //          when(odsConnector.connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier], any()))
  //            .thenReturn(EitherT.rightT(HttpResponse(OK, "{}")))
  //
  //          val result = service.getATSList(testUtr)(mock[HeaderCarrier], mock[Request[_]]).value
  //
  //          whenReady(result) { res =>
  //            res.swap.getOrElse(UpstreamErrorResponse("", IM_A_TEAPOT)) mustBe response
  //
  //            verify(odsConnector).connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier], any())
  //            verify(jsonHelper, never).createTaxYearJson(any[JsValue], eqTo(testUtr), any[JsValue])
  //          }
  //        }
  //      }
  //    }
  //
  //    "return a UpstreamErrorResponse" when {
  //      List(400, 401, 403, 500, 501, 502, 503, 504).foreach { statusCode =>
  //        s"UpstreamErrorResponse with status $statusCode is received when getting taxpayer details" in {
  //          val response = UpstreamErrorResponse("Not found", statusCode, INTERNAL_SERVER_ERROR)
  //
  //          when(odsConnector.connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier], any()))
  //            .thenReturn(EitherT.rightT(HttpResponse(OK, "{}")))
  //          when(odsConnector.connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier], any()))
  //            .thenReturn(EitherT.leftT(response))
  //
  //          val result = service.getATSList(testUtr)(mock[HeaderCarrier], mock[Request[_]]).value
  //
  //          whenReady(result) { res =>
  //            res.swap.getOrElse(UpstreamErrorResponse("", IM_A_TEAPOT)) mustBe response
  //
  //            verify(odsConnector, times(1)).connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier], any())
  //            verify(jsonHelper, never).createTaxYearJson(any[JsValue], eqTo(testUtr), any[JsValue])
  //          }
  //        }
  //      }
  //    }
  //
  //    "return a UpstreamErrorResponse" when {
  //      List(400, 401, 403, 500, 501, 502, 503, 504).foreach { statusCode =>
  //        s"UpstreamErrorResponse with status $statusCode is received when getting taxpayer details ANS SA json" in {
  //          val response = UpstreamErrorResponse("Not found", statusCode, INTERNAL_SERVER_ERROR)
  //
  //          when(odsConnector.connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier], any()))
  //            .thenReturn(EitherT.leftT(response))
  //          when(odsConnector.connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier], any()))
  //            .thenReturn(EitherT.leftT(response))
  //
  //          val result = service.getATSList(testUtr)(mock[HeaderCarrier], mock[Request[_]]).value
  //
  //          whenReady(result) { res =>
  //            res.swap.getOrElse(UpstreamErrorResponse("", IM_A_TEAPOT)) mustBe response
  //
  //            verify(odsConnector, times(1)).connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier], any())
  //            verify(jsonHelper, never).createTaxYearJson(any[JsValue], eqTo(testUtr), any[JsValue])
  //          }
  //        }
  //      }
  //    }
  //  }

  //  "connectToSATaxpayerDetails" must {
  //    "returns json as JsValue" in {
  //      when(odsConnector.connectToSATaxpayerDetails(any())(any(), any()))
  //        .thenReturn(EitherT.rightT(HttpResponse(OK, "{}")))
  //
  //      val result = service.connectToSATaxpayerDetails("")(mock[HeaderCarrier], mock[Request[_]]).value.futureValue
  //
  //      result mustBe a[Right[_, JsValue]]
  //    }
  //
  //    "returns A left" when {
  //      "a not found response is received" in {
  //        when(odsConnector.connectToSATaxpayerDetails(any())(any(), any()))
  //          .thenReturn(EitherT.rightT(HttpResponse(NOT_FOUND, "")))
  //
  //        val result = service.connectToSATaxpayerDetails("")(mock[HeaderCarrier], mock[Request[_]]).value.futureValue
  //
  //        result mustBe a[Left[UpstreamErrorResponse, _]]
  //      }
  //
  //      "a Left is received" in {
  //        when(odsConnector.connectToSATaxpayerDetails(any())(any(), any()))
  //          .thenReturn(EitherT.leftT(UpstreamErrorResponse("Server error", INTERNAL_SERVER_ERROR)))
  //
  //        val result = service.connectToSATaxpayerDetails("")(mock[HeaderCarrier], mock[Request[_]]).value.futureValue
  //
  //        result mustBe a[Left[UpstreamErrorResponse, _]]
  //      }
  //    }
  //  }
}
