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

package sa.services

import cats.data.EitherT
import cats.instances.future.*
import common.models.{Amount, Rate}
import common.utils.BaseSpec
import common.utils.TestConstants.*
import org.mockito.ArgumentMatchers.{eq as eqTo, *}
import org.mockito.Mockito.*
import play.api.http.Status.*
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Request
import sa.connectors.SelfAssessmentODSConnector
import sa.models.{PensionTaxRate, TaxSummaryLiability}
import sa.transformers.ATSCalculations
import sa.utils.TaxsJsonHelper
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, UpstreamErrorResponse}

import scala.collection.immutable.Range
import scala.concurrent.ExecutionContext
import scala.language.implicitConversions

class OdsServiceSpec extends BaseSpec {
  private val taxYear                            = fakeTaxYear
  private implicit lazy val ec: ExecutionContext = inject[ExecutionContext]

  private val odsConnectorCaching: SelfAssessmentODSConnector = mock[SelfAssessmentODSConnector]
  private val jsonHelper: TaxsJsonHelper                      = mock[TaxsJsonHelper]

  private val service = new OdsService(jsonHelper, odsConnectorCaching)

  private val currentTaxYear = fakeTaxYear

  private def saResponse(taxYear: Int): JsValue = Json.obj("taxYear" -> taxYear)

  private def atsCalculations(taxYear: Int, amount: BigDecimal) =
    Some(new ATSCalculations {
      override protected val summaryData: TaxSummaryLiability = TaxSummaryLiability(
        taxYear,
        PensionTaxRate(0),
        None,
        Map.empty,
        Map.empty
      )
      override val taxRates: Map[String, Rate]                = Map.empty

      override def taxLiability: Amount = Amount(amount, "GBP")

      override def otherIncome: Amount = Amount.empty("Dummy other income amount")

      override def selfEmployment: Amount = Amount.empty("Dummy self employment amount")

      override def otherAllowances: Amount = Amount.empty("Dummy other allowances amount")

      override def otherAdjustmentsIncreasing: Amount = Amount.empty("Dummy other adj increasing amount")

      override def totalIncomeTaxAmount: Amount = Amount.empty("Dummy total income tax amount")

      override def scottishIncomeTax: Amount = Amount.empty("Dummy scottish income tax amount")

    })

  private implicit def convertIntToSeqInt(i: Int): Seq[Int] = Seq(i)

  private def whenClausesForSA(
    endTaxYear: Int,
    responseStatusesToMockForSA: Seq[Seq[Int]]
  ): Unit =
    responseStatusesToMockForSA.reverse.zipWithIndex.foreach { case (seqInt, i) =>
      val seqEither = seqInt.map {
        case OK           => Right(HttpResponse(OK, saResponse(endTaxYear - i), Map.empty))
        case NOT_FOUND    => Right(HttpResponse(NOT_FOUND, saResponse(endTaxYear - i), Map.empty))
        case responseCode => Left(UpstreamErrorResponse("", responseCode))
      }

      val seqEitherT = seqEither.map(EitherT.fromEither(_)(catsStdInstancesForFuture(ec)))

      if (seqEitherT.size > 1) {
        when(
          odsConnectorCaching.connectToSelfAssessment(eqTo(testUtr), eqTo(endTaxYear - i))(any[HeaderCarrier], any())
        )
          .thenReturn(seqEitherT.head, seqEitherT.tail: _*)
      } else {
        when(
          odsConnectorCaching.connectToSelfAssessment(eqTo(testUtr), eqTo(endTaxYear - i))(any[HeaderCarrier], any())
        )
          .thenReturn(seqEitherT.head)
      }
    }

  private def verifySA(endTaxYear: Int, expectedNumberOfCalls: Seq[Int]): Unit =
    expectedNumberOfCalls.reverse.zipWithIndex.foreach { case (expNumberOfCalls, i) =>
      verify(odsConnectorCaching, times(expNumberOfCalls))
        .connectToSelfAssessment(eqTo(testUtr), eqTo(endTaxYear - i))(any[HeaderCarrier], any())
    }

  private def whenClausesForATSCalculations(endTaxYear: Int, values: Seq[BigDecimal]): Unit =
    Range(endTaxYear - (values.size - 1), endTaxYear + 1).zipWithIndex.foreach { case (year, i) =>
      when(jsonHelper.getATSCalculations(eqTo(year), eqTo(saResponse(year))))
        .thenReturn(atsCalculations(year, values(i)))
    }

  private def verifyATSCalculations(endTaxYear: Int, expectedNumberOfCalls: Seq[Int]): Unit =
    Range(endTaxYear - (expectedNumberOfCalls.size - 1), endTaxYear + 1).zipWithIndex.foreach { case (year, i) =>
      verify(jsonHelper, times(expectedNumberOfCalls(i))).getATSCalculations(eqTo(year), eqTo(saResponse(year)))
    }

  override def beforeEach(): Unit = {
    reset(odsConnectorCaching, jsonHelper)
    super.beforeEach()
  }

  "getPayload" must {

    "return json" when {
      "the call is successful and the cache is used" in {
        when(odsConnectorCaching.connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier], any()))
          .thenReturn(EitherT.rightT(HttpResponse(OK, "{}")))
        when(odsConnectorCaching.connectToSelfAssessment(eqTo(testUtr), eqTo(taxYear))(any[HeaderCarrier], any()))
          .thenReturn(EitherT.rightT(HttpResponse(OK, "{}")))
        when(jsonHelper.getAllATSData(any[JsValue], any[JsValue], eqTo(testUtr), eqTo(taxYear))(any()))
          .thenReturn(mock[JsValue])

        val result =
          service.getPayload(testUtr, taxYear)(mock[HeaderCarrier], mock[Request[_]]).value

        whenReady(result) { res =>
          res.isRight mustBe true

          verify(jsonHelper, times(1))
            .getAllATSData(any[JsValue], any[JsValue], eqTo(testUtr), eqTo(taxYear))(any())
        }
      }
    }

    "return a UpstreamErrorResponse" when {
      "Not found response is received from self assessment" in {
        when(odsConnectorCaching.connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier], any()))
          .thenReturn(EitherT.rightT(HttpResponse(OK, "{}")))
        when(odsConnectorCaching.connectToSelfAssessment(eqTo(testUtr), eqTo(taxYear))(any[HeaderCarrier], any()))
          .thenReturn(EitherT.rightT(HttpResponse(NOT_FOUND, "")))

        val result =
          service.getPayload(testUtr, taxYear)(mock[HeaderCarrier], mock[Request[_]]).value

        whenReady(result) { res =>
          res mustBe a[Left[UpstreamErrorResponse, _]]

          verify(odsConnectorCaching).connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier], any())
          verify(odsConnectorCaching).connectToSelfAssessment(eqTo(testUtr), eqTo(taxYear))(any[HeaderCarrier], any())
          verify(jsonHelper, never)
            .getAllATSData(any[JsValue], any[JsValue], eqTo(testUtr), eqTo(taxYear))(any())
        }
      }

      "Not found response is received from tax payer details" in {
        when(odsConnectorCaching.connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier], any()))
          .thenReturn(EitherT.rightT(HttpResponse(NOT_FOUND, "")))
        when(odsConnectorCaching.connectToSelfAssessment(eqTo(testUtr), eqTo(taxYear))(any[HeaderCarrier], any()))
          .thenReturn(EitherT.rightT(HttpResponse(OK, "{}")))

        val result =
          service.getPayload(testUtr, taxYear)(mock[HeaderCarrier], mock[Request[_]]).value

        whenReady(result) { res =>
          res mustBe a[Left[UpstreamErrorResponse, _]]

          verify(odsConnectorCaching).connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier], any())
          verify(odsConnectorCaching, times(0))
            .connectToSelfAssessment(eqTo(testUtr), eqTo(taxYear))(any[HeaderCarrier], any())
          verify(jsonHelper, never)
            .getAllATSData(any[JsValue], any[JsValue], eqTo(testUtr), eqTo(taxYear))(any())
        }
      }

      List(400, 401, 403, 500, 501, 502, 503, 504).foreach { statusCode =>
        s"UpstreamErrorResponse with status $statusCode is received" in {
          val response = UpstreamErrorResponse("Not found", statusCode, INTERNAL_SERVER_ERROR)

          when(odsConnectorCaching.connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier], any()))
            .thenReturn(EitherT.rightT(HttpResponse(OK, "{}")))
          when(odsConnectorCaching.connectToSelfAssessment(eqTo(testUtr), eqTo(taxYear))(any[HeaderCarrier], any()))
            .thenReturn(EitherT.leftT(response))

          val result =
            service.getPayload(testUtr, taxYear)(mock[HeaderCarrier], mock[Request[_]]).value

          whenReady(result) { res =>
            res mustBe a[Left[UpstreamErrorResponse, _]]

            verify(odsConnectorCaching).connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier], any())
            verify(odsConnectorCaching).connectToSelfAssessment(eqTo(testUtr), eqTo(taxYear))(any[HeaderCarrier], any())
            verify(jsonHelper, never)
              .getAllATSData(any[JsValue], any[JsValue], eqTo(testUtr), eqTo(taxYear))(any())
          }
        }
      }
    }
  }

  "getList" must {

    "return json" when {

      "connector calls are successful" in {

        when(odsConnectorCaching.connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier], any()))
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
        when(odsConnectorCaching.connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier], any()))
          .thenReturn(EitherT.rightT(HttpResponse(OK, "{}")))
        when(odsConnectorCaching.connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier], any()))
          .thenReturn(EitherT.rightT(HttpResponse(NOT_FOUND, "")))

        val result = service.getList(testUtr)(mock[HeaderCarrier], mock[Request[_]]).value

        whenReady(result) { res =>
          res mustBe a[Left[UpstreamErrorResponse, _]]

          verify(odsConnectorCaching, times(0)).connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier], any())
          verify(odsConnectorCaching).connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier], any())
          verify(jsonHelper, never)
            .getAllATSData(any[JsValue], any[JsValue], eqTo(testUtr), eqTo(taxYear))(any())
        }
      }

      List(400, 401, 403, 500, 501, 502, 503, 504).foreach { statusCode =>
        s"UpstreamErrorResponse with status $statusCode is received" in {
          val response = UpstreamErrorResponse("Not found", statusCode, INTERNAL_SERVER_ERROR)

          when(odsConnectorCaching.connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier], any()))
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
        service
          .getATSList(testUtr, currentTaxYear - 4, currentTaxYear)(
            mock[HeaderCarrier],
            mock[Request[_]]
          )
          .value
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
        service
          .getATSList(testUtr, currentTaxYear - 4, currentTaxYear)(
            mock[HeaderCarrier],
            mock[Request[_]]
          )
          .value
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
        service
          .getATSList(testUtr, currentTaxYear - 4, currentTaxYear)(
            mock[HeaderCarrier],
            mock[Request[_]]
          )
          .value
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
        service
          .getATSList(testUtr, currentTaxYear - 4, currentTaxYear)(
            mock[HeaderCarrier],
            mock[Request[_]]
          )
          .value
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
        service
          .getATSList(testUtr, currentTaxYear - 4, currentTaxYear)(
            mock[HeaderCarrier],
            mock[Request[_]]
          )
          .value
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
        service
          .getATSList(testUtr, currentTaxYear - 4, currentTaxYear)(
            mock[HeaderCarrier],
            mock[Request[_]]
          )
          .value
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
        service
          .getATSList(testUtr, currentTaxYear - 4, currentTaxYear)(
            mock[HeaderCarrier],
            mock[Request[_]]
          )
          .value
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
        service
          .getATSList(testUtr, currentTaxYear - 4, currentTaxYear)(
            mock[HeaderCarrier],
            mock[Request[_]]
          )
          .value
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

  "connectToSATaxpayerDetails" must {
    "returns json as JsValue" in {
      when(odsConnectorCaching.connectToSATaxpayerDetails(any())(any(), any()))
        .thenReturn(EitherT.rightT(HttpResponse(OK, "{}")))

      val result = service.connectToSATaxpayerDetails("")(mock[HeaderCarrier], mock[Request[_]]).value.futureValue

      result mustBe a[Right[_, JsValue]]
    }

    "returns A left" when {
      "a not found response is received" in {
        when(odsConnectorCaching.connectToSATaxpayerDetails(any())(any(), any()))
          .thenReturn(EitherT.rightT(HttpResponse(NOT_FOUND, "")))

        val result = service.connectToSATaxpayerDetails("")(mock[HeaderCarrier], mock[Request[_]]).value.futureValue

        result mustBe a[Left[UpstreamErrorResponse, _]]
      }

      "a Left is received" in {
        when(odsConnectorCaching.connectToSATaxpayerDetails(any())(any(), any()))
          .thenReturn(EitherT.leftT(UpstreamErrorResponse("Server error", INTERNAL_SERVER_ERROR)))

        val result = service.connectToSATaxpayerDetails("")(mock[HeaderCarrier], mock[Request[_]]).value.futureValue

        result mustBe a[Left[UpstreamErrorResponse, _]]
      }
    }
  }
}
