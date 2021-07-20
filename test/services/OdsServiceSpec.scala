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
import org.scalatest.concurrent.IntegrationPatience
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HeaderCarrier
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

  "getPayload" should {

    "return json" when {
      "the call is successful" in {

        when(odsConnector.connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier]))
          .thenReturn(Future.successful(Some(mock[JsValue])))
        when(odsConnector.connectToSelfAssessment(eqTo(testUtr), eqTo(2014))(any[HeaderCarrier]))
          .thenReturn(Future.successful(Some(mock[JsValue])))
        when(jsonHelper.getAllATSData(any[JsValue], any[JsValue], eqTo(testUtr), eqTo(2014)))
          .thenReturn(mock[JsValue])

        val result = service.getPayload(testUtr, 2014)(mock[HeaderCarrier])

        whenReady(result) { res =>
          res.isRight shouldBe true

          verify(jsonHelper, times(1)).getAllATSData(any[JsValue], any[JsValue], eqTo(testUtr), eqTo(2014))
        }
      }
    }

    "return a service error when no ats for that year is found" in {

      when(odsConnector.connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier]))
        .thenReturn(Future.successful(Some(mock[JsValue])))
      when(odsConnector.connectToSelfAssessment(eqTo(testUtr), eqTo(2014))(any[HeaderCarrier]))
        .thenReturn(Future.successful(None))

      val result = service.getPayload(testUtr, 2014)(mock[HeaderCarrier])

      whenReady(result) { res =>
        res.left.get shouldBe a[NotFoundError]

        verify(odsConnector).connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier])
        verify(odsConnector).connectToSelfAssessment(eqTo(testUtr), eqTo(2014))(any[HeaderCarrier])
        verify(jsonHelper, never()).getAllATSData(any[JsValue], any[JsValue], eqTo(testUtr), eqTo(2014))
      }
    }

    "return a service error when no taxpayer details are found" in {

      when(odsConnector.connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier]))
        .thenReturn(Future.successful(None))
      when(odsConnector.connectToSelfAssessment(eqTo(testUtr), eqTo(2014))(any[HeaderCarrier]))
        .thenReturn(Future.successful(Some(mock[JsValue])))

      val result = service.getPayload(testUtr, 2014)(mock[HeaderCarrier])

      whenReady(result) { res =>
        res.left.get shouldBe a[NotFoundError]

        verify(odsConnector).connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier])
        verify(odsConnector).connectToSelfAssessment(eqTo(testUtr), eqTo(2014))(any[HeaderCarrier])
        verify(jsonHelper, never()).getAllATSData(any[JsValue], any[JsValue], eqTo(testUtr), eqTo(2014))
      }
    }

    "return a service error when invalid json is returned" in {

      when(odsConnector.connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier]))
        .thenReturn(Future.failed(mock[JsonParseException]))

      val result = service.getPayload(testUtr, 2014)(mock[HeaderCarrier])

      whenReady(result) { res =>
        res.left.get shouldBe a[JsonParseError]

        verify(odsConnector, times(1)).connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier])
        verify(odsConnector, never()).connectToSelfAssessment(eqTo(testUtr), eqTo(2014))(any[HeaderCarrier])
        verify(jsonHelper, never()).getAllATSData(any[JsValue], any[JsValue], eqTo(testUtr), eqTo(2014))
      }
    }

    "return a service error when any other exception is thrown by the connector" in {

      when(odsConnector.connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier]))
        .thenReturn(Future.successful(Some(mock[JsValue])))
      when(odsConnector.connectToSelfAssessment(eqTo(testUtr), eqTo(2014))(any[HeaderCarrier]))
        .thenReturn(Future.failed(new Exception("raw exception")))

      val result = service.getPayload(testUtr, 2014)(mock[HeaderCarrier])

      whenReady(result) { res =>
        res.left.get shouldBe a[GenericError]

        verify(odsConnector, times(1)).connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier])
        verify(odsConnector, times(1)).connectToSelfAssessment(eqTo(testUtr), eqTo(2014))(any[HeaderCarrier])
        verify(jsonHelper, never()).getAllATSData(any[JsValue], any[JsValue], eqTo(testUtr), eqTo(2014))
      }
    }
  }

  "getList" should {

    "return json" when {

      "connector calls are successful" in {

        when(odsConnector.connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier]))
          .thenReturn(Future.successful(Some(mock[JsValue])))
        when(jsonHelper.hasAtsForPreviousPeriod(any[JsValue]))
          .thenReturn(true)

        val result = service.getList(testUtr)(mock[HeaderCarrier])

        whenReady(result) { result =>
          result.isRight shouldBe true

          Json.fromJson[AtsCheck](result.right.get).asOpt shouldBe Some(AtsCheck(true))
        }
      }
    }

    "return a service error" when {

      "the connector returns invalid json" in {

        when(odsConnector.connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier]))
          .thenReturn(Future.failed(mock[JsonParseException]))

        val result = service.getList(testUtr)(mock[HeaderCarrier])

        whenReady(result) { res =>
          res.left.get shouldBe a[JsonParseError]
          verify(jsonHelper, never()).hasAtsForPreviousPeriod(any[JsValue])
        }
      }

      "the connector returns not found" in {

        when(odsConnector.connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier]))
          .thenReturn(Future.successful(None))

        val result = service.getList(testUtr)(mock[HeaderCarrier])

        whenReady(result) { res =>
          res.left.get shouldBe a[NotFoundError]
          verify(jsonHelper, never()).hasAtsForPreviousPeriod(any[JsValue])
        }
      }

      "any other exception is thrown by the connector" in {

        when(odsConnector.connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier]))
          .thenReturn(Future.failed(new Exception()))

        val result = service.getList(testUtr)(mock[HeaderCarrier])

        whenReady(result) { res =>
          res.left.get shouldBe a[GenericError]
          verify(jsonHelper, never()).hasAtsForPreviousPeriod(any[JsValue])
        }
      }
    }
  }

  "getATSList" should {

    "return a right" in {

      val json = mock[JsValue]

      when(odsConnector.connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier]))
        .thenReturn(Future.successful(Some(json)))
      when(odsConnector.connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier]))
        .thenReturn(Future.successful(Some(json)))
      when(jsonHelper.createTaxYearJson(any[JsValue], eqTo(testUtr), any[JsValue]))
        .thenReturn(json)

      val result = service.getATSList(testUtr)(mock[HeaderCarrier])

      whenReady(result) { result =>
        result shouldBe Right(json)

        verify(odsConnector, times(1)).connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier])
        verify(odsConnector, times(1)).connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier])
        verify(jsonHelper, times(1)).createTaxYearJson(any[JsValue], eqTo(testUtr), any[JsValue])
      }
    }

    "return a left" when {
      "a JsonParseException is caught" in {

        when(odsConnector.connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier]))
          .thenReturn(Future.failed(mock[JsonParseException]))

        val result = service.getATSList(testUtr)(mock[HeaderCarrier])

        whenReady(result) { result =>
          result.left.get shouldBe a[JsonParseError]

          verify(odsConnector, times(1)).connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier])
          verify(odsConnector, never()).connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier])
          verify(jsonHelper, never()).createTaxYearJson(any[JsValue], eqTo(testUtr), any[JsValue])
        }
      }

      "a not found is returned when getting SA json" in {

        when(odsConnector.connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier]))
          .thenReturn(Future.successful(None))
        when(odsConnector.connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier]))
          .thenReturn(Future.successful(Some(mock[JsValue])))

        val result = service.getATSList(testUtr)(mock[HeaderCarrier])

        whenReady(result) { result =>
          result.left.get shouldBe a[NotFoundError]

          verify(odsConnector).connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier])
          verify(odsConnector).connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier])
          verify(jsonHelper, never()).createTaxYearJson(any[JsValue], eqTo(testUtr), any[JsValue])
        }
      }

      "a Not Found is returned when getting taxpayer details" in {

        when(odsConnector.connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier]))
          .thenReturn(Future.successful(Some(mock[JsValue])))
        when(odsConnector.connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier]))
          .thenReturn(Future.successful(None))

        val result = service.getATSList(testUtr)(mock[HeaderCarrier])

        whenReady(result) { result =>
          result.left.get shouldBe a[NotFoundError]

          verify(odsConnector, times(1)).connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier])
          verify(odsConnector, times(1)).connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier])
          verify(jsonHelper, never()).createTaxYearJson(any[JsValue], eqTo(testUtr), any[JsValue])
        }
      }

      "any other exception is thrown" in {

        val exceptionMessage = "raw exception"

        when(odsConnector.connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier]))
          .thenReturn(Future.failed(new Exception(exceptionMessage)))

        val result = service.getATSList(testUtr)(mock[HeaderCarrier])

        whenReady(result) { result =>
          result shouldBe Left(GenericError(exceptionMessage))

          verify(odsConnector, times(1)).connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier])
          verify(odsConnector, never()).connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier])
          verify(jsonHelper, never()).createTaxYearJson(any[JsValue], eqTo(testUtr), any[JsValue])
        }
      }
    }
  }
}
