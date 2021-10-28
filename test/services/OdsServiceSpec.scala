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

package services

import connectors.ODSConnector
import models.AtsCheck
import org.mockito.Matchers.{eq => eqTo, _}
import org.mockito.Mockito
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.libs.json.{JsResultException, JsValue, Json}
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import utils.TestConstants._

import scala.concurrent.duration._
import utils.{BaseSpec, TaxsJsonHelper}

import scala.concurrent.{Await, ExecutionContext, Future}

class OdsServiceSpec extends BaseSpec with BeforeAndAfterEach {

  implicit lazy val ec: ExecutionContext = inject[ExecutionContext]

  val odsConnector = mock[ODSConnector]
  val jsonHelper = mock[TaxsJsonHelper]

  val service = new OdsService(jsonHelper, odsConnector)

  override def beforeEach(): Unit = {
    Mockito.reset(odsConnector)
    Mockito.reset(jsonHelper)
    super.beforeEach()
  }

  "getPayload" must {

    "return json" when {
      "the call is successful" in {

        when(odsConnector.connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier]))
          .thenReturn(Future.successful(Right(mock[JsValue])))
        when(odsConnector.connectToSelfAssessment(eqTo(testUtr), eqTo(2014))(any[HeaderCarrier]))
          .thenReturn(Future.successful(Right(mock[JsValue])))
        when(jsonHelper.getAllATSData(any[JsValue], any[JsValue], eqTo(testUtr), eqTo(2014)))
          .thenReturn(mock[JsValue])

        val result = service.getPayload(testUtr, 2014)(mock[HeaderCarrier])

        whenReady(result) { res =>
          res.isRight mustBe true

          verify(jsonHelper, times(1)).getAllATSData(any[JsValue], any[JsValue], eqTo(testUtr), eqTo(2014))
        }
      }
    }

    "return a UpstreamErrorResponse" when {
      List(400, 401, 403, 404, 500, 501, 502, 503, 504).foreach { statusCode =>
        s"UpstreamErrorResponse with status $statusCode is received" in {
          val response = UpstreamErrorResponse("Not found", statusCode, INTERNAL_SERVER_ERROR)

          when(odsConnector.connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier]))
            .thenReturn(Future.successful(Right(mock[JsValue])))
          when(odsConnector.connectToSelfAssessment(eqTo(testUtr), eqTo(2014))(any[HeaderCarrier]))
            .thenReturn(Future.successful(Left(response)))

          val result = service.getPayload(testUtr, 2014)(mock[HeaderCarrier])

          whenReady(result) { res =>
            res.left.get mustBe response

            verify(odsConnector).connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier])
            verify(odsConnector).connectToSelfAssessment(eqTo(testUtr), eqTo(2014))(any[HeaderCarrier])
            verify(jsonHelper, never()).getAllATSData(any[JsValue], any[JsValue], eqTo(testUtr), eqTo(2014))
          }
        }
      }

      "return an exception when invalid json is returned" in {

        when(odsConnector.connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier]))
          .thenReturn(Future.failed(JsResultException(List.empty)))

        intercept[JsResultException](Await.result(service.getPayload(testUtr, 2014)(mock[HeaderCarrier]), 1 seconds))
      }
    }
  }

  "getList" must {

    "return json" when {

      "connector calls are successful" in {

        when(odsConnector.connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier]))
          .thenReturn(Future.successful(Right(mock[JsValue])))
        when(jsonHelper.hasAtsForPreviousPeriod(any[JsValue]))
          .thenReturn(true)

        val result = service.getList(testUtr)(mock[HeaderCarrier])

        whenReady(result) { result =>
          result.isRight mustBe true

          Json.fromJson[AtsCheck](result.right.get).asOpt mustBe Some(AtsCheck(true))
        }
      }
    }

    "return an exception" when {

      "the connector returns invalid json" in {

        when(odsConnector.connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier]))
          .thenReturn(Future.failed(JsResultException(List.empty)))

        intercept[JsResultException](Await.result(service.getList(testUtr)(mock[HeaderCarrier]), 1 seconds))
      }
    }

    "return a UpstreamErrorResponse" when {
      List(400, 401, 403, 404, 500, 501, 502, 503, 504).foreach { statusCode =>
        s"UpstreamErrorResponse with status $statusCode is received" in {
          val response = UpstreamErrorResponse("Not found", statusCode, INTERNAL_SERVER_ERROR)

          when(odsConnector.connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier]))
            .thenReturn(Future.successful(Left(response)))

          val result = service.getList(testUtr)(mock[HeaderCarrier])

          whenReady(result) { res =>
            res.left.get mustBe response

            verify(jsonHelper, never()).hasAtsForPreviousPeriod(any[JsValue])
          }
        }
      }
    }
  }

  "getATSList" must {

    "return a right" in {

      val json = mock[JsValue]

      when(odsConnector.connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier]))
        .thenReturn(Future.successful(Right(json)))
      when(odsConnector.connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier]))
        .thenReturn(Future.successful(Right(json)))
      when(jsonHelper.createTaxYearJson(any[JsValue], eqTo(testUtr), any[JsValue]))
        .thenReturn(json)

      val result = service.getATSList(testUtr)(mock[HeaderCarrier])

      whenReady(result) { result =>
        result mustBe Right(json)

        verify(odsConnector, times(1)).connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier])
        verify(odsConnector, times(1)).connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier])
        verify(jsonHelper, times(1)).createTaxYearJson(any[JsValue], eqTo(testUtr), any[JsValue])
      }
    }

    "return an exception" when {
      "a JsResultException is caught" in {

        when(odsConnector.connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier]))
          .thenReturn(Future.failed(JsResultException(List.empty)))

        intercept[JsResultException](Await.result(service.getATSList(testUtr)(mock[HeaderCarrier]), 1 seconds))

        verify(odsConnector, times(1)).connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier])
        verify(odsConnector, never()).connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier])
        verify(jsonHelper, never()).createTaxYearJson(any[JsValue], eqTo(testUtr), any[JsValue])
      }
    }

    "return a UpstreamErrorResponse" when {
      List(400, 401, 403, 404, 500, 501, 502, 503, 504).foreach { statusCode =>
        s"UpstreamErrorResponse with status $statusCode is received when getting SA json" in {
          val response = UpstreamErrorResponse("Not found", statusCode, INTERNAL_SERVER_ERROR)

          when(odsConnector.connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier]))
            .thenReturn(Future.successful(Left(response)))
          when(odsConnector.connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier]))
            .thenReturn(Future.successful(Right(mock[JsValue])))

          val result = service.getATSList(testUtr)(mock[HeaderCarrier])

          whenReady(result) { res =>
            res.left.get mustBe response

            verify(odsConnector).connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier])
            verify(jsonHelper, never()).createTaxYearJson(any[JsValue], eqTo(testUtr), any[JsValue])
          }
        }
      }
    }
  }

  "return a UpstreamErrorResponse" when {
    List(400, 401, 403, 404, 500, 501, 502, 503, 504).foreach { statusCode =>
      s"UpstreamErrorResponse with status $statusCode is received when getting taxpayer details" in {
        val response = UpstreamErrorResponse("Not found", statusCode, INTERNAL_SERVER_ERROR)

        when(odsConnector.connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier]))
          .thenReturn(Future.successful(Right(mock[JsValue])))
        when(odsConnector.connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier]))
          .thenReturn(Future.successful(Left(response)))

        val result = service.getATSList(testUtr)(mock[HeaderCarrier])

        whenReady(result) { res =>
          res.left.get mustBe response

          verify(odsConnector, times(1)).connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier])
          verify(jsonHelper, never()).createTaxYearJson(any[JsValue], eqTo(testUtr), any[JsValue])
        }
      }
    }
  }

  "return a UpstreamErrorResponse" when {
    List(400, 401, 403, 404, 500, 501, 502, 503, 504).foreach { statusCode =>
      s"UpstreamErrorResponse with status $statusCode is received when getting taxpayer details ANS SA json" in {
        val response = UpstreamErrorResponse("Not found", statusCode, INTERNAL_SERVER_ERROR)

        when(odsConnector.connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier]))
          .thenReturn(Future.successful(Left(response)))
        when(odsConnector.connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier]))
          .thenReturn(Future.successful(Left(response)))

        val result = service.getATSList(testUtr)(mock[HeaderCarrier])

        whenReady(result) { res =>
          res.left.get mustBe response

          verify(odsConnector, times(1)).connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier])
          verify(jsonHelper, never()).createTaxYearJson(any[JsValue], eqTo(testUtr), any[JsValue])
        }
      }
    }
  }
}
