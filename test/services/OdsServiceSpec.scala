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
import models.{Amount, PensionTaxRate, TaxSummaryLiability}
import org.mockito.ArgumentMatchers.{eq => eqTo, _}
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Request
import transformers.ATSCalculations
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, UpstreamErrorResponse}
import utils.TestConstants._
import utils.{BaseSpec, TaxsJsonHelper}

import scala.concurrent.ExecutionContext
import scala.language.implicitConversions

class OdsServiceSpec extends BaseSpec {
  private implicit lazy val ec: ExecutionContext = inject[ExecutionContext]

  private val odsConnector: SelfAssessmentODSConnector = mock[SelfAssessmentODSConnector]
  private val jsonHelper: TaxsJsonHelper               = mock[TaxsJsonHelper]

  private val service = new OdsService(jsonHelper, odsConnector)

  private val currentTaxYear = fakeTaxYear

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

  private implicit def convertIntToSeqInt(i: Int): Seq[Int] = Seq(i)

  private def whenClausesForSA(endTaxYear: Int, responseStatusesToMockForSA: Seq[Seq[Int]]): Unit =
    responseStatusesToMockForSA.reverse.zipWithIndex.foreach { case (seqInt, i) =>
      val seqEither = seqInt.map {
        case OK           => Right(HttpResponse(OK, saResponse(endTaxYear - i), Map.empty))
        case NOT_FOUND    => Right(HttpResponse(NOT_FOUND, saResponse(endTaxYear - i), Map.empty))
        case responseCode => Left(UpstreamErrorResponse("", responseCode))
      }

      val seqEitherT = seqEither.map(EitherT.fromEither(_)(catsStdInstancesForFuture(ec)))

      if (seqEitherT.size > 1) {
        when(odsConnector.connectToSelfAssessment(eqTo(testUtr), eqTo(endTaxYear - i))(any[HeaderCarrier], any()))
          .thenReturn(seqEitherT.head, seqEitherT.tail: _*)
      } else {
        when(odsConnector.connectToSelfAssessment(eqTo(testUtr), eqTo(endTaxYear - i))(any[HeaderCarrier], any()))
          .thenReturn(seqEitherT.head)
      }
    }

  private def verifySA(endTaxYear: Int, expectedNumberOfCalls: Seq[Int]): Unit =
    expectedNumberOfCalls.reverse.zipWithIndex.foreach { case (expNumberOfCalls, i) =>
      verify(odsConnector, times(expNumberOfCalls))
        .connectToSelfAssessment(eqTo(testUtr), eqTo(endTaxYear - i))(any[HeaderCarrier], any())
    }

  private def whenClausesForATSCalculations(endTaxYear: Int, values: Seq[BigDecimal]): Unit =
    ((endTaxYear - (values.size - 1)) to endTaxYear).zipWithIndex.foreach { case (year, i) =>
      when(jsonHelper.getATSCalculations(eqTo(year), eqTo(saResponse(year))))
        .thenReturn(atsCalculations(year, values(i)))
    }

  private def verifyATSCalculations(endTaxYear: Int, expectedNumberOfCalls: Seq[Int]): Unit =
    ((endTaxYear - (expectedNumberOfCalls.size - 1)) to endTaxYear).zipWithIndex.foreach { case (year, i) =>
      verify(jsonHelper, times(expectedNumberOfCalls(i))).getATSCalculations(eqTo(year), eqTo(saResponse(year)))
    }

  override def beforeEach(): Unit = {
    reset(odsConnector, jsonHelper)
    super.beforeEach()
  }

  "getPayload" must {

    "return json" when {
      "the call is successful" in {

        when(odsConnector.connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier], any()))
          .thenReturn(EitherT.rightT(HttpResponse(OK, "{}")))
        when(odsConnector.connectToSelfAssessment(eqTo(testUtr), eqTo(2014))(any[HeaderCarrier], any()))
          .thenReturn(EitherT.rightT(HttpResponse(OK, "{}")))
        when(jsonHelper.getAllATSData(any[JsValue], any[JsValue], eqTo(testUtr), eqTo(2014)))
          .thenReturn(mock[JsValue])

        val result = service.getPayload(testUtr, 2014)(mock[HeaderCarrier], mock[Request[_]]).value

        whenReady(result) { res =>
          res.isRight mustBe true

          verify(jsonHelper, times(1)).getAllATSData(any[JsValue], any[JsValue], eqTo(testUtr), eqTo(2014))
        }
      }
    }

    "return a UpstreamErrorResponse" when {
      "Not found response is received from self assessment" in {
        when(odsConnector.connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier], any()))
          .thenReturn(EitherT.rightT(HttpResponse(OK, "{}")))
        when(odsConnector.connectToSelfAssessment(eqTo(testUtr), eqTo(2014))(any[HeaderCarrier], any()))
          .thenReturn(EitherT.rightT(HttpResponse(NOT_FOUND, "")))

        val result = service.getPayload(testUtr, 2014)(mock[HeaderCarrier], mock[Request[_]]).value

        whenReady(result) { res =>
          res mustBe a[Left[UpstreamErrorResponse, _]]

          verify(odsConnector).connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier], any())
          verify(odsConnector).connectToSelfAssessment(eqTo(testUtr), eqTo(2014))(any[HeaderCarrier], any())
          verify(jsonHelper, never).getAllATSData(any[JsValue], any[JsValue], eqTo(testUtr), eqTo(2014))
        }
      }

      "Not found response is received from tax payer details" in {
        when(odsConnector.connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier], any()))
          .thenReturn(EitherT.rightT(HttpResponse(NOT_FOUND, "")))
        when(odsConnector.connectToSelfAssessment(eqTo(testUtr), eqTo(2014))(any[HeaderCarrier], any()))
          .thenReturn(EitherT.rightT(HttpResponse(OK, "{}")))

        val result = service.getPayload(testUtr, 2014)(mock[HeaderCarrier], mock[Request[_]]).value

        whenReady(result) { res =>
          res mustBe a[Left[UpstreamErrorResponse, _]]

          verify(odsConnector).connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier], any())
          verify(odsConnector, times(0)).connectToSelfAssessment(eqTo(testUtr), eqTo(2014))(any[HeaderCarrier], any())
          verify(jsonHelper, never).getAllATSData(any[JsValue], any[JsValue], eqTo(testUtr), eqTo(2014))
        }
      }

      List(400, 401, 403, 500, 501, 502, 503, 504).foreach { statusCode =>
        s"UpstreamErrorResponse with status $statusCode is received" in {
          val response = UpstreamErrorResponse("Not found", statusCode, INTERNAL_SERVER_ERROR)

          when(odsConnector.connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier], any()))
            .thenReturn(EitherT.rightT(HttpResponse(OK, "{}")))
          when(odsConnector.connectToSelfAssessment(eqTo(testUtr), eqTo(2014))(any[HeaderCarrier], any()))
            .thenReturn(EitherT.leftT(response))

          val result = service.getPayload(testUtr, 2014)(mock[HeaderCarrier], mock[Request[_]]).value

          whenReady(result) { res =>
            res mustBe a[Left[UpstreamErrorResponse, _]]

            verify(odsConnector).connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier], any())
            verify(odsConnector).connectToSelfAssessment(eqTo(testUtr), eqTo(2014))(any[HeaderCarrier], any())
            verify(jsonHelper, never).getAllATSData(any[JsValue], any[JsValue], eqTo(testUtr), eqTo(2014))
          }
        }
      }
    }
  }

  "getList" must {

    "return json" when {

      "connector calls are successful" in {

        when(odsConnector.connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier], any()))
          .thenReturn(EitherT.rightT(HttpResponse(OK, "{}")))
        when(jsonHelper.hasAtsForPreviousPeriod(any[JsValue]))
          .thenReturn(true)

        val result = service.getList(testUtr)(mock[HeaderCarrier], mock[Request[_]]).value

        whenReady(result) { result =>
          result.isRight mustBe true
          val actualResult = result.getOrElse(Json.obj())
          actualResult mustBe Json.obj("has_ats" -> true)
        }
      }
    }

    "return a UpstreamErrorResponse" when {
      "Not found response is received" in {
        when(odsConnector.connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier], any()))
          .thenReturn(EitherT.rightT(HttpResponse(OK, "{}")))
        when(odsConnector.connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier], any()))
          .thenReturn(EitherT.rightT(HttpResponse(NOT_FOUND, "")))

        val result = service.getList(testUtr)(mock[HeaderCarrier], mock[Request[_]]).value

        whenReady(result) { res =>
          res mustBe a[Left[UpstreamErrorResponse, _]]

          verify(odsConnector, times(0)).connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier], any())
          verify(odsConnector).connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier], any())
          verify(jsonHelper, never).getAllATSData(any[JsValue], any[JsValue], eqTo(testUtr), eqTo(2014))
        }
      }

      List(400, 401, 403, 500, 501, 502, 503, 504).foreach { statusCode =>
        s"UpstreamErrorResponse with status $statusCode is received" in {
          val response = UpstreamErrorResponse("Not found", statusCode, INTERNAL_SERVER_ERROR)

          when(odsConnector.connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier], any()))
            .thenReturn(EitherT.leftT(response))

          val result = service.getList(testUtr)(mock[HeaderCarrier], mock[Request[_]]).value

          whenReady(result) { res =>
            res.swap.getOrElse(UpstreamErrorResponse("", IM_A_TEAPOT)) mustBe response

            verify(jsonHelper, never).hasAtsForPreviousPeriod(any[JsValue])
          }
        }
      }
    }
  }

  "getATSList" must {

    "return no years if no liability at all but not ALL tax years return not found" in {
      whenClausesForSA(
        endTaxYear = currentTaxYear,
        responseStatusesToMockForSA = Seq(OK, NOT_FOUND, OK, OK)
      )

      whenClausesForATSCalculations(endTaxYear = currentTaxYear, values = Seq(BigDecimal(0), BigDecimal(0)))
      whenClausesForATSCalculations(endTaxYear = currentTaxYear - 3, values = Seq(BigDecimal(0), BigDecimal(0)))

      whenReady(
        service.getATSList(testUtr, currentTaxYear - 4, currentTaxYear)(mock[HeaderCarrier], mock[Request[_]]).value
      ) { result =>
        result mustBe Right(Nil)

        verifySA(
          endTaxYear = currentTaxYear,
          expectedNumberOfCalls = Seq(1, 1, 1, 1)
        )
        verifyATSCalculations(
          endTaxYear = currentTaxYear,
          expectedNumberOfCalls = Seq(1, 0, 1, 1)
        )
      }
    }

    "return NOT_FOUND upstream error response if ALL tax years return not found" in {
      whenClausesForSA(
        endTaxYear = currentTaxYear,
        responseStatusesToMockForSA = Seq(NOT_FOUND, NOT_FOUND, NOT_FOUND, NOT_FOUND)
      )

      whenReady(
        service.getATSList(testUtr, currentTaxYear - 4, currentTaxYear)(mock[HeaderCarrier], mock[Request[_]]).value
      ) { result =>
        result mustBe Left(UpstreamErrorResponse("Not_Found", NOT_FOUND))
        verifySA(
          endTaxYear = currentTaxYear,
          expectedNumberOfCalls = Seq(1, 1, 1, 1)
        )
        verifyATSCalculations(
          endTaxYear = currentTaxYear,
          expectedNumberOfCalls = Seq(0, 0, 0, 0)
        )
      }
    }

    "return NOT_FOUND upstream error response if ALL tax years return not found after retrying after one 5xx error" in {
      whenClausesForSA(
        endTaxYear = currentTaxYear,
        responseStatusesToMockForSA = Seq(Seq(INTERNAL_SERVER_ERROR, NOT_FOUND), NOT_FOUND, NOT_FOUND, NOT_FOUND)
      )

      whenReady(
        service.getATSList(testUtr, currentTaxYear - 4, currentTaxYear)(mock[HeaderCarrier], mock[Request[_]]).value
      ) { result =>
        result mustBe Left(UpstreamErrorResponse("Not_Found", NOT_FOUND))
        verifySA(
          endTaxYear = currentTaxYear,
          expectedNumberOfCalls = Seq(2, 1, 1, 1)
        )
        verifyATSCalculations(
          endTaxYear = currentTaxYear,
          expectedNumberOfCalls = Seq(0, 0, 0, 0)
        )
      }
    }

    "return years minus any where no tax liability or no tax data" in {
      whenClausesForSA(
        endTaxYear = currentTaxYear,
        responseStatusesToMockForSA = Seq(OK, OK, NOT_FOUND, OK, OK)
      )

      whenClausesForATSCalculations(endTaxYear = currentTaxYear, values = Seq(BigDecimal(0), BigDecimal(1)))
      whenClausesForATSCalculations(endTaxYear = currentTaxYear - 3, values = Seq(BigDecimal(1), BigDecimal(2)))

      whenReady(
        service.getATSList(testUtr, currentTaxYear - 4, currentTaxYear)(mock[HeaderCarrier], mock[Request[_]]).value
      ) { result =>
        result mustBe Right(Seq(currentTaxYear - 3, currentTaxYear))

        verifySA(
          endTaxYear = currentTaxYear,
          expectedNumberOfCalls = Seq(1, 1, 1, 1)
        )
        verifyATSCalculations(
          endTaxYear = currentTaxYear,
          expectedNumberOfCalls = Seq(1, 0, 1, 1)
        )
      }
    }

    "return years minus any where 4xx other than not found response" in {
      whenClausesForSA(
        endTaxYear = currentTaxYear,
        responseStatusesToMockForSA = Seq(OK, BAD_REQUEST, OK, OK)
      )

      whenClausesForATSCalculations(endTaxYear = currentTaxYear, values = Seq(BigDecimal(0), BigDecimal(1)))
      whenClausesForATSCalculations(endTaxYear = currentTaxYear - 3, values = Seq(BigDecimal(1), BigDecimal(2)))

      whenReady(
        service.getATSList(testUtr, currentTaxYear - 4, currentTaxYear)(mock[HeaderCarrier], mock[Request[_]]).value
      ) { result =>
        result mustBe Right(Seq(currentTaxYear - 3, currentTaxYear))

        verifySA(
          endTaxYear = currentTaxYear,
          expectedNumberOfCalls = Seq(1, 1, 1, 1)
        )
        verifyATSCalculations(
          endTaxYear = currentTaxYear,
          expectedNumberOfCalls = Seq(1, 0, 1, 1)
        )
      }
    }

    "return failure response if 1 call to HOD fails with 5xx + retry fails" in {
      whenClausesForSA(
        endTaxYear = currentTaxYear,
        responseStatusesToMockForSA = Seq(OK, OK, BAD_GATEWAY, OK, OK)
      )
      whenClausesForATSCalculations(endTaxYear = currentTaxYear, values = Seq(BigDecimal(1), BigDecimal(2)))
      whenClausesForATSCalculations(endTaxYear = currentTaxYear - 3, values = Seq(BigDecimal(1), BigDecimal(2)))

      whenReady(
        service.getATSList(testUtr, currentTaxYear - 4, currentTaxYear)(mock[HeaderCarrier], mock[Request[_]]).value
      ) { result =>
        result mustBe Left(UpstreamErrorResponse("", BAD_GATEWAY))

        verifySA(
          endTaxYear = currentTaxYear,
          expectedNumberOfCalls = Seq(1, 2, 1, 1)
        )
        verifyATSCalculations(
          endTaxYear = currentTaxYear,
          expectedNumberOfCalls = Seq(1, 0, 1, 1)
        )
      }
    }

    "return all years when 1 HOD call fails with 5xx but retry succeeds" in {
      whenClausesForSA(
        endTaxYear = currentTaxYear,
        responseStatusesToMockForSA = Seq(OK, Seq(INTERNAL_SERVER_ERROR, OK), OK, OK)
      )

      whenClausesForATSCalculations(
        endTaxYear = currentTaxYear,
        values = Seq(BigDecimal(1), BigDecimal(1), BigDecimal(1), BigDecimal(1))
      )

      whenReady(
        service.getATSList(testUtr, currentTaxYear - 4, currentTaxYear)(mock[HeaderCarrier], mock[Request[_]]).value
      ) { result =>
        result mustBe
          Right(Seq(currentTaxYear - 3, currentTaxYear - 2, currentTaxYear - 1, currentTaxYear))

        verifySA(
          endTaxYear = currentTaxYear,
          expectedNumberOfCalls = Seq(1, 2, 1, 1)
        )
        verifyATSCalculations(
          endTaxYear = currentTaxYear,
          expectedNumberOfCalls = Seq(1, 1, 1, 1)
        )
      }
    }

    "return multiple upstream error exception when more than one exception + don't retry" in {
      whenClausesForSA(
        endTaxYear = currentTaxYear,
        responseStatusesToMockForSA = Seq(OK, INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR, OK, OK)
      )

      whenClausesForATSCalculations(
        endTaxYear = currentTaxYear,
        values = Seq(BigDecimal(1), BigDecimal(1))
      )

      whenClausesForATSCalculations(
        endTaxYear = currentTaxYear - 3,
        values = Seq(BigDecimal(1), BigDecimal(1))
      )

      whenReady(
        service.getATSList(testUtr, currentTaxYear - 4, currentTaxYear)(mock[HeaderCarrier], mock[Request[_]]).value
      ) { result =>
        result mustBe
          Left(UpstreamErrorResponse("Multiple upstream failures", INTERNAL_SERVER_ERROR))

        verifySA(
          endTaxYear = currentTaxYear,
          expectedNumberOfCalls = Seq(1, 1, 1, 1)
        )
        verifyATSCalculations(
          endTaxYear = currentTaxYear,
          expectedNumberOfCalls = Seq(0, 0, 1, 1)
        )
      }
    }

  }

  // TODO: Below tests are for hasATS method which is not called at present but it may be called in future by BTA. See comments on DDCNL-7999 for more info
  "hasATS" must {
    "return NOT_FOUND upstream error response if ALL tax years return not found" in {
      whenClausesForSA(
        endTaxYear = currentTaxYear,
        responseStatusesToMockForSA = Seq(NOT_FOUND, NOT_FOUND, NOT_FOUND, NOT_FOUND, NOT_FOUND)
      )

      whenReady(
        service.hasATS(testUtr)(mock[HeaderCarrier], mock[Request[_]]).value
      ) { result =>
        result mustBe Left(UpstreamErrorResponse("Not_Found", NOT_FOUND))
        verifySA(
          endTaxYear = currentTaxYear,
          expectedNumberOfCalls = Seq(1, 1, 1, 1)
        )
        verifyATSCalculations(
          endTaxYear = currentTaxYear,
          expectedNumberOfCalls = Seq(0, 0, 0, 0)
        )
      }
    }

    "return NOT_FOUND upstream error response if ALL tax years return not found after retry" in {
      whenClausesForSA(
        endTaxYear = currentTaxYear,
        responseStatusesToMockForSA =
          Seq(NOT_FOUND, NOT_FOUND, Seq(INTERNAL_SERVER_ERROR, NOT_FOUND), NOT_FOUND, NOT_FOUND)
      )

      whenReady(
        service.hasATS(testUtr)(mock[HeaderCarrier], mock[Request[_]]).value
      ) { result =>
        result mustBe Left(UpstreamErrorResponse("Not_Found", NOT_FOUND))
        verifySA(
          endTaxYear = currentTaxYear,
          expectedNumberOfCalls = Seq(1, 2, 1, 1)
        )
        verifyATSCalculations(
          endTaxYear = currentTaxYear,
          expectedNumberOfCalls = Seq(0, 0, 0, 0)
        )
      }
    }

    "return true where all but 1 year have no tax data or no tax liability found" in {
      whenClausesForSA(
        endTaxYear = currentTaxYear,
        responseStatusesToMockForSA = Seq(OK, OK, NOT_FOUND, OK, OK)
      )
      whenClausesForATSCalculations(endTaxYear = currentTaxYear, values = Seq(BigDecimal(0), BigDecimal(0)))
      whenClausesForATSCalculations(endTaxYear = currentTaxYear - 3, values = Seq(BigDecimal(1), BigDecimal(0)))

      whenReady(
        service.hasATS(testUtr)(mock[HeaderCarrier], mock[Request[_]]).value
      ) { result =>
        result mustBe Right(Json.obj("has_ats" -> true))

        verifySA(
          endTaxYear = currentTaxYear,
          expectedNumberOfCalls = Seq(1, 1, 1, 1)
        )
        verifyATSCalculations(
          endTaxYear = currentTaxYear,
          expectedNumberOfCalls = Seq(1, 0, 1, 1)
        )
      }
    }

    "return json with false value where all years have no tax data (+ must not do tax liability calculations) or no tax liability found" in {
      whenClausesForSA(
        endTaxYear = currentTaxYear,
        responseStatusesToMockForSA = Seq(OK, NOT_FOUND, NOT_FOUND, OK, NOT_FOUND)
      )
      whenClausesForATSCalculations(endTaxYear = currentTaxYear - 1, values = Seq(BigDecimal(0)))
      whenClausesForATSCalculations(endTaxYear = currentTaxYear - 4, values = Seq(BigDecimal(0)))

      whenReady(
        service.hasATS(testUtr)(mock[HeaderCarrier], mock[Request[_]]).value
      ) { result =>
        result mustBe Right(Json.obj("has_ats" -> false))

        verifySA(
          endTaxYear = currentTaxYear,
          expectedNumberOfCalls = Seq(1, 1, 1, 1)
        )
        verifyATSCalculations(
          endTaxYear = currentTaxYear,
          expectedNumberOfCalls = Seq(0, 0, 1, 0)
        )
      }
    }

    "return failed error code if 1 call to HOD fails + retry that call ONCE ONLY (fails again)" in {
      whenClausesForSA(
        endTaxYear = currentTaxYear,
        responseStatusesToMockForSA = Seq(OK, OK, GATEWAY_TIMEOUT, NOT_FOUND, OK)
      )
      whenClausesForATSCalculations(endTaxYear = currentTaxYear, values = Seq(BigDecimal(0)))
      whenClausesForATSCalculations(endTaxYear = currentTaxYear - 3, values = Seq(BigDecimal(0), BigDecimal(0)))

      whenReady(
        service.hasATS(testUtr)(mock[HeaderCarrier], mock[Request[_]]).value
      ) { result =>
        result mustBe Left(UpstreamErrorResponse("", GATEWAY_TIMEOUT))

        verifySA(
          endTaxYear = currentTaxYear,
          expectedNumberOfCalls = Seq(1, 2, 1, 1)
        )
        verifyATSCalculations(
          endTaxYear = currentTaxYear,
          expectedNumberOfCalls = Seq(1, 0, 0, 1)
        )
      }
    }

    "return json with true value when one HOD call fails but doesn't retry because has found 2 tax years" in {
      whenClausesForSA(
        endTaxYear = currentTaxYear,
        responseStatusesToMockForSA = Seq(OK, OK, INTERNAL_SERVER_ERROR, NOT_FOUND, NOT_FOUND)
      )

      whenClausesForATSCalculations(
        endTaxYear = currentTaxYear - 3,
        values = Seq(BigDecimal(1), BigDecimal(1))
      )

      whenReady(
        service.hasATS(testUtr)(mock[HeaderCarrier], mock[Request[_]]).value
      ) { result =>
        result mustBe Right(Json.obj("has_ats" -> true))

        verifySA(
          endTaxYear = currentTaxYear,
          expectedNumberOfCalls = Seq(1, 1, 1, 1)
        )
        verifyATSCalculations(
          endTaxYear = currentTaxYear,
          expectedNumberOfCalls = Seq(1, 0, 0, 0)
        )
      }
    }

    "return json with true value when multiple HOD calls fail but doesn't retry because has found 1 tax year" in {
      whenClausesForSA(
        endTaxYear = currentTaxYear,
        responseStatusesToMockForSA = Seq(OK, INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR, NOT_FOUND, NOT_FOUND)
      )

      whenClausesForATSCalculations(
        endTaxYear = currentTaxYear - 2,
        values = Seq(BigDecimal(1), BigDecimal(1), BigDecimal(1))
      )

      whenReady(
        service.hasATS(testUtr)(mock[HeaderCarrier], mock[Request[_]]).value
      ) { result =>
        result mustBe Right(Json.obj("has_ats" -> true))

        verifySA(
          endTaxYear = currentTaxYear,
          expectedNumberOfCalls = Seq(1, 1, 1, 1)
        )
        verifyATSCalculations(
          endTaxYear = currentTaxYear,
          expectedNumberOfCalls = Seq(0, 0, 0, 0)
        )
      }
    }

    "return json with true value when one HOD call fails then because have found no tax years with liability " +
      "retries + finds a year with liability" in {
        whenClausesForSA(
          endTaxYear = currentTaxYear,
          responseStatusesToMockForSA = Seq(OK, OK, Seq(INTERNAL_SERVER_ERROR, OK), NOT_FOUND, NOT_FOUND)
        )

        whenClausesForATSCalculations(
          endTaxYear = currentTaxYear - 2,
          values = Seq(BigDecimal(0), BigDecimal(0), BigDecimal(1))
        )

        whenReady(
          service.hasATS(testUtr)(mock[HeaderCarrier], mock[Request[_]]).value
        ) { result =>
          result mustBe Right(Json.obj("has_ats" -> true))

          verifySA(
            endTaxYear = currentTaxYear,
            expectedNumberOfCalls = Seq(1, 2, 1, 1)
          )
          verifyATSCalculations(
            endTaxYear = currentTaxYear,
            expectedNumberOfCalls = Seq(1, 1, 0, 0)
          )
        }
      }
  }

  "connectToSATaxpayerDetails" must {
    "returns json as JsValue" in {
      when(odsConnector.connectToSATaxpayerDetails(any())(any(), any()))
        .thenReturn(EitherT.rightT(HttpResponse(OK, "{}")))

      val result = service.connectToSATaxpayerDetails("")(mock[HeaderCarrier], mock[Request[_]]).value.futureValue

      result mustBe a[Right[_, JsValue]]
    }

    "returns A left" when {
      "a not found response is received" in {
        when(odsConnector.connectToSATaxpayerDetails(any())(any(), any()))
          .thenReturn(EitherT.rightT(HttpResponse(NOT_FOUND, "")))

        val result = service.connectToSATaxpayerDetails("")(mock[HeaderCarrier], mock[Request[_]]).value.futureValue

        result mustBe a[Left[UpstreamErrorResponse, _]]
      }

      "a Left is received" in {
        when(odsConnector.connectToSATaxpayerDetails(any())(any(), any()))
          .thenReturn(EitherT.leftT(UpstreamErrorResponse("Server error", INTERNAL_SERVER_ERROR)))

        val result = service.connectToSATaxpayerDetails("")(mock[HeaderCarrier], mock[Request[_]]).value.futureValue

        result mustBe a[Left[UpstreamErrorResponse, _]]
      }
    }
  }
}
