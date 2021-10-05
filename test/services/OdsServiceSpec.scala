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

import com.fasterxml.jackson.core.JsonParseException
import connectors.ODSConnector
import models._
import org.mockito.Matchers.{eq => eqTo, _}
import org.mockito.Mockito
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, LOCKED, NOT_FOUND}
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import utils.TestConstants._
import utils.{BaseSpec, TaxsJsonHelper}

import scala.concurrent.{ExecutionContext, Future}

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

    "return a NotFoundError when no ats for that year is found" in {

      when(odsConnector.connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier]))
        .thenReturn(Future.successful(Right(mock[JsValue])))
      when(odsConnector.connectToSelfAssessment(eqTo(testUtr), eqTo(2014))(any[HeaderCarrier]))
        .thenReturn(Future.successful(Left(UpstreamErrorResponse("Not found", NOT_FOUND, INTERNAL_SERVER_ERROR))))

      val result = service.getPayload(testUtr, 2014)(mock[HeaderCarrier])

      whenReady(result) { res =>
        res.left.get mustBe a[NotFoundError]

        verify(odsConnector).connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier])
        verify(odsConnector).connectToSelfAssessment(eqTo(testUtr), eqTo(2014))(any[HeaderCarrier])
        verify(jsonHelper, never()).getAllATSData(any[JsValue], any[JsValue], eqTo(testUtr), eqTo(2014))
      }
    }

    "return a NotFoundError when no taxpayer details are found" in {

      when(odsConnector.connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier]))
        .thenReturn(Future.successful(Left(UpstreamErrorResponse("Not found", NOT_FOUND, INTERNAL_SERVER_ERROR))))
      when(odsConnector.connectToSelfAssessment(eqTo(testUtr), eqTo(2014))(any[HeaderCarrier]))
        .thenReturn(Future.successful(Right(mock[JsValue])))

      val result = service.getPayload(testUtr, 2014)(mock[HeaderCarrier])

      whenReady(result) { res =>
        res.left.get mustBe a[NotFoundError]

        verify(odsConnector).connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier])
        verify(jsonHelper, never()).getAllATSData(any[JsValue], any[JsValue], eqTo(testUtr), eqTo(2014))
      }
    }

    "return a DownStreamServerError when taxpayer details returns a server error" in {
      when(odsConnector.connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier]))
        .thenReturn(
          Future.successful(Left(UpstreamErrorResponse("Server Error", INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR))))
      when(odsConnector.connectToSelfAssessment(eqTo(testUtr), eqTo(2014))(any[HeaderCarrier]))
        .thenReturn(Future.successful(Right(mock[JsValue])))

      val result = service.getPayload(testUtr, 2014)(mock[HeaderCarrier])

      whenReady(result) { res =>
        res.left.get mustBe a[DownstreamServerError]

        verify(odsConnector).connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier])
        verify(jsonHelper, never()).getAllATSData(any[JsValue], any[JsValue], eqTo(testUtr), eqTo(2014))
      }
    }

    "return a BadRequestError when taxpayer details returns a bad request" in {
      when(odsConnector.connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier]))
        .thenReturn(Future.successful(Left(UpstreamErrorResponse("Bad request", BAD_REQUEST, INTERNAL_SERVER_ERROR))))
      when(odsConnector.connectToSelfAssessment(eqTo(testUtr), eqTo(2014))(any[HeaderCarrier]))
        .thenReturn(Future.successful(Right(mock[JsValue])))

      val result = service.getPayload(testUtr, 2014)(mock[HeaderCarrier])

      whenReady(result) { res =>
        res.left.get mustBe a[BadRequestError]

        verify(odsConnector).connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier])
        verify(jsonHelper, never()).getAllATSData(any[JsValue], any[JsValue], eqTo(testUtr), eqTo(2014))
      }
    }

    "return an exception when invalid json is returned" in {

      when(odsConnector.connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier]))
        .thenReturn(Future.failed(mock[JsonParseException]))

      val result = service.getPayload(testUtr, 2014)(mock[HeaderCarrier])

      whenReady(result) { res =>
        res.left.get mustBe a[JsonParseException]
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
          .thenReturn(Future.failed(mock[JsonParseException]))

        val result = service.getList(testUtr)(mock[HeaderCarrier])

        whenReady(result) { res =>
          res.left.get mustBe a[JsonParseException]
        }
      }
    }

    "return a NotFoundError" when {
      "the connector returns not found" in {

        when(odsConnector.connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier]))
          .thenReturn(Future.successful(Left(UpstreamErrorResponse("Not found", NOT_FOUND, INTERNAL_SERVER_ERROR))))

        val result = service.getList(testUtr)(mock[HeaderCarrier])

        whenReady(result) { res =>
          res.left.get mustBe a[NotFoundError]
          verify(jsonHelper, never()).hasAtsForPreviousPeriod(any[JsValue])
        }
      }
    }

    "return a BadRequestError" when {
      "the connector returns Bad request" in {

        when(odsConnector.connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier]))
          .thenReturn(Future.successful(Left(UpstreamErrorResponse("Bad request", BAD_REQUEST, INTERNAL_SERVER_ERROR))))

        val result = service.getList(testUtr)(mock[HeaderCarrier])

        whenReady(result) { res =>
          res.left.get mustBe a[BadRequestError]
          verify(jsonHelper, never()).hasAtsForPreviousPeriod(any[JsValue])
        }
      }
    }

    "return a DownStreamClientError" when {
      "the connector returns a client error" in {

        when(odsConnector.connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier]))
          .thenReturn(Future.successful(Left(UpstreamErrorResponse("Client error", LOCKED, INTERNAL_SERVER_ERROR))))

        val result = service.getList(testUtr)(mock[HeaderCarrier])

        whenReady(result) { res =>
          res.left.get mustBe a[DownstreamClientError]
          verify(jsonHelper, never()).hasAtsForPreviousPeriod(any[JsValue])
        }
      }
    }

    "return a DownStreamServerError" when {
      "the connector returns a server error" in {

        when(odsConnector.connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier]))
          .thenReturn(Future.successful(
            Left(UpstreamErrorResponse("Server error", INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR))))

        val result = service.getList(testUtr)(mock[HeaderCarrier])

        whenReady(result) { res =>
          res.left.get mustBe a[DownstreamServerError]
          verify(jsonHelper, never()).hasAtsForPreviousPeriod(any[JsValue])
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
      "a JsonParseException is caught" in {

        when(odsConnector.connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier]))
          .thenReturn(Future.failed(mock[JsonParseException]))

        val result = service.getATSList(testUtr)(mock[HeaderCarrier])

        whenReady(result) { result =>
          result.left.get mustBe a[JsonParseException]

          verify(odsConnector, times(1)).connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier])
          verify(odsConnector, never()).connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier])
          verify(jsonHelper, never()).createTaxYearJson(any[JsValue], eqTo(testUtr), any[JsValue])
        }
      }
    }

    "return a Left" when {
      "a not found is returned when getting SA json" in {

        when(odsConnector.connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier]))
          .thenReturn(Future.successful(Left(UpstreamErrorResponse("Not found", NOT_FOUND, INTERNAL_SERVER_ERROR))))
        when(odsConnector.connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier]))
          .thenReturn(Future.successful(Right(mock[JsValue])))

        val result = service.getATSList(testUtr)(mock[HeaderCarrier])

        whenReady(result) { result =>
          result.left.get mustBe a[NotFoundError]

          verify(odsConnector).connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier])
          verify(jsonHelper, never()).createTaxYearJson(any[JsValue], eqTo(testUtr), any[JsValue])
        }
      }

      "a Not Found is returned when getting taxpayer details" in {

        when(odsConnector.connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier]))
          .thenReturn(Future.successful(Right(mock[JsValue])))
        when(odsConnector.connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier]))
          .thenReturn(Future.successful(Left(UpstreamErrorResponse("Not found", NOT_FOUND, INTERNAL_SERVER_ERROR))))

        val result = service.getATSList(testUtr)(mock[HeaderCarrier])

        whenReady(result) { result =>
          result.left.get mustBe a[NotFoundError]

          verify(odsConnector, times(1)).connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier])
          verify(jsonHelper, never()).createTaxYearJson(any[JsValue], eqTo(testUtr), any[JsValue])
        }
      }

      "a Not Found is returned when getting SA json and taxpayer details" in {

        when(odsConnector.connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier]))
          .thenReturn(Future.successful(Left(UpstreamErrorResponse("Not found", NOT_FOUND, INTERNAL_SERVER_ERROR))))
        when(odsConnector.connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier]))
          .thenReturn(Future.successful(Left(UpstreamErrorResponse("Not found", NOT_FOUND, INTERNAL_SERVER_ERROR))))

        val result = service.getATSList(testUtr)(mock[HeaderCarrier])

        whenReady(result) { result =>
          result.left.get mustBe a[NotFoundError]

          verify(odsConnector, times(1)).connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier])
          verify(jsonHelper, never()).createTaxYearJson(any[JsValue], eqTo(testUtr), any[JsValue])
        }
      }

      "a bad request is returned when getting SA json" in {

        when(odsConnector.connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier]))
          .thenReturn(Future.successful(Left(UpstreamErrorResponse("Not found", BAD_REQUEST, INTERNAL_SERVER_ERROR))))
        when(odsConnector.connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier]))
          .thenReturn(Future.successful(Right(mock[JsValue])))

        val result = service.getATSList(testUtr)(mock[HeaderCarrier])

        whenReady(result) { result =>
          result.left.get mustBe a[BadRequestError]

          verify(odsConnector).connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier])
          verify(jsonHelper, never()).createTaxYearJson(any[JsValue], eqTo(testUtr), any[JsValue])
        }
      }

      "a bad request is returned when getting taxpayer details" in {

        when(odsConnector.connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier]))
          .thenReturn(Future.successful(Right(mock[JsValue])))
        when(odsConnector.connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier]))
          .thenReturn(Future.successful(Left(UpstreamErrorResponse("Not found", BAD_REQUEST, INTERNAL_SERVER_ERROR))))

        val result = service.getATSList(testUtr)(mock[HeaderCarrier])

        whenReady(result) { result =>
          result.left.get mustBe a[BadRequestError]

          verify(odsConnector, times(1)).connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier])
          verify(jsonHelper, never()).createTaxYearJson(any[JsValue], eqTo(testUtr), any[JsValue])
        }
      }

      "a bad request is returned when getting SA json and taxpayer details" in {

        when(odsConnector.connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier]))
          .thenReturn(Future.successful(Left(UpstreamErrorResponse("Not found", BAD_REQUEST, INTERNAL_SERVER_ERROR))))
        when(odsConnector.connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier]))
          .thenReturn(Future.successful(Left(UpstreamErrorResponse("Not found", BAD_REQUEST, INTERNAL_SERVER_ERROR))))

        val result = service.getATSList(testUtr)(mock[HeaderCarrier])

        whenReady(result) { result =>
          result.left.get mustBe a[BadRequestError]

          verify(odsConnector, times(1)).connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier])
          verify(jsonHelper, never()).createTaxYearJson(any[JsValue], eqTo(testUtr), any[JsValue])
        }
      }

      "a server error is returned when getting taxpayer details" in {

        when(odsConnector.connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier]))
          .thenReturn(Future.successful(Right(mock[JsValue])))
        when(odsConnector.connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier]))
          .thenReturn(Future.successful(
            Left(UpstreamErrorResponse("Server error", INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR))))

        val result = service.getATSList(testUtr)(mock[HeaderCarrier])

        whenReady(result) { result =>
          result.left.get mustBe a[DownstreamServerError]

          verify(odsConnector, times(1)).connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier])
          verify(jsonHelper, never()).createTaxYearJson(any[JsValue], eqTo(testUtr), any[JsValue])
        }
      }

      "a client error is returned when getting taxpayer details" in {

        when(odsConnector.connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier]))
          .thenReturn(Future.successful(Right(mock[JsValue])))
        when(odsConnector.connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier]))
          .thenReturn(Future.successful(Left(UpstreamErrorResponse("client error", LOCKED, INTERNAL_SERVER_ERROR))))

        val result = service.getATSList(testUtr)(mock[HeaderCarrier])

        whenReady(result) { result =>
          result.left.get mustBe a[DownstreamClientError]

          verify(odsConnector, times(1)).connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier])
          verify(jsonHelper, never()).createTaxYearJson(any[JsValue], eqTo(testUtr), any[JsValue])
        }
      }

    }
  }
}
