/*
 * Copyright 2020 HM Revenue & Customs
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

import com.fasterxml.jackson.core.{JsonParseException, JsonParser}
import connectors.ODSConnector
import errors.AtsError
import models._
import org.mockito.Matchers.{eq => eqTo, _}
import org.mockito.Mockito
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.mockito.MockitoSugar
import play.api.libs.json.{JsValue, Json}
import play.api.test.Injecting
import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException}
import utils.TestConstants._
import utils.{BaseSpec, TaxsJsonHelper}

import scala.concurrent.{ExecutionContext, Future}

class OdsServiceSpec
    extends BaseSpec with MockitoSugar with ScalaFutures with BeforeAndAfterEach with IntegrationPatience
    with Injecting {

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

    "return a successful future" in {

      when(odsConnector.connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier]))
        .thenReturn(Future.successful(mock[JsValue]))
      when(odsConnector.connectToSelfAssessment(eqTo(testUtr), eqTo(2014))(any[HeaderCarrier]))
        .thenReturn(Future.successful(mock[JsValue]))
      when(jsonHelper.getAllATSData(any[JsValue], any[JsValue], eqTo(testUtr), eqTo(2014)))
        .thenReturn(mock[JsValue])

      val result = service.getPayload(testUtr, 2014)(mock[HeaderCarrier])

      whenReady(result) { result =>
        result shouldBe a[JsValue]

        verify(jsonHelper, times(1)).getAllATSData(any[JsValue], any[JsValue], eqTo(testUtr), eqTo(2014))
      }
    }

    "return a successful future when the connection fails (SaTaxpayerDetails)" in {

      when(odsConnector.connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier]))
        .thenReturn(Future.failed(mock[JsonParseException]))

      val result = service.getPayload(testUtr, 2014)(mock[HeaderCarrier])

      whenReady(result) { result =>
        result shouldBe a[JsValue]

        verify(odsConnector, times(1)).connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier])
        verify(odsConnector, never()).connectToSelfAssessment(eqTo(testUtr), eqTo(2014))(any[HeaderCarrier])
        verify(jsonHelper, never()).getAllATSData(any[JsValue], any[JsValue], eqTo(testUtr), eqTo(2014))

        Json.fromJson[AtsMiddleTierData](result).asOpt shouldBe
          Some(
            AtsMiddleTierData(
              2014,
              None,
              None,
              None,
              None,
              None,
              None,
              None,
              None,
              Option(AtsError("JsonParsingError"))))
      }
    }

    "return a successful future when the connection fails (SelfAssessment)" in {

      when(odsConnector.connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier]))
        .thenReturn(Future.successful(mock[JsValue]))
      when(odsConnector.connectToSelfAssessment(eqTo(testUtr), eqTo(2014))(any[HeaderCarrier]))
        .thenReturn(Future.failed(new Exception("raw exception")))

      val result = service.getPayload(testUtr, 2014)(mock[HeaderCarrier])

      whenReady(result) { result =>
        result shouldBe a[JsValue]

        verify(odsConnector, times(1)).connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier])
        verify(odsConnector, times(1)).connectToSelfAssessment(eqTo(testUtr), eqTo(2014))(any[HeaderCarrier])
        verify(jsonHelper, never()).getAllATSData(any[JsValue], any[JsValue], eqTo(testUtr), eqTo(2014))

        Json.fromJson[AtsMiddleTierData](result).asOpt shouldBe
          Some(
            AtsMiddleTierData(2014, None, None, None, None, None, None, None, None, Option(AtsError("GenericError"))))
      }
    }
  }

  "getList" should {

    "return a successful future" in {

      when(odsConnector.connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier]))
        .thenReturn(Future.successful(mock[JsValue]))
      when(jsonHelper.hasAtsForPreviousPeriod(any[JsValue]))
        .thenReturn(true)

      val result = service.getList(testUtr)(mock[HeaderCarrier])

      whenReady(result) { result =>
        result shouldBe a[JsValue]

        Json.fromJson[AtsCheck](result).asOpt shouldBe Some(AtsCheck(true))
      }
    }

    "return a failed future" in {

      when(odsConnector.connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier]))
        .thenReturn(Future.failed(new Exception()))

      val result = service.getList(testUtr)(mock[HeaderCarrier])

      whenReady(result.failed) { exception =>
        exception shouldBe a[Exception]
        verify(jsonHelper, never()).hasAtsForPreviousPeriod(any[JsValue])
      }
    }
  }

  "getATSList" should {

    "return a right" in {

      val json = mock[JsValue]

      when(odsConnector.connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier]))
        .thenReturn(Future.successful(json))
      when(odsConnector.connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier]))
        .thenReturn(Future.successful(json))
      when(jsonHelper.createTaxYearJson(any[JsValue], eqTo(testUtr), any[JsValue]))
        .thenReturn(Future.successful(json))

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

        val exception = new JsonParseException(mock[JsonParser], "Malformed Json")

        when(odsConnector.connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier]))
          .thenReturn(Future.failed(exception))

        val result = service.getATSList(testUtr)(mock[HeaderCarrier])

        whenReady(result) { result =>
          result shouldBe Left(JsonParseError(exception.getMessage))

          verify(odsConnector, times(1)).connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier])
          verify(odsConnector, never()).connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier])
          verify(jsonHelper, never()).createTaxYearJson(any[JsValue], eqTo(testUtr), any[JsValue])
        }
      }

      "a NotFoundException is caught when getting SA json" in {

        val exceptionMessage = "Connector returned not found"

        when(odsConnector.connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier]))
          .thenReturn(Future.failed(new NotFoundException(exceptionMessage)))

        val result = service.getATSList(testUtr)(mock[HeaderCarrier])

        whenReady(result) { result =>
          result shouldBe Left(NotFoundError(exceptionMessage))

          verify(odsConnector).connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier])
          verify(odsConnector, never()).connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier])
          verify(jsonHelper, never()).createTaxYearJson(any[JsValue], eqTo(testUtr), any[JsValue])
        }
      }

      "a NotFoundException is caught when getting taxpayer details" in {

        val exceptionMessage = "Connector returned not found"

        when(odsConnector.connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier]))
          .thenReturn(Future.successful(mock[JsValue]))
        when(odsConnector.connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier]))
          .thenReturn(Future.failed(new NotFoundException(exceptionMessage)))

        val result = service.getATSList(testUtr)(mock[HeaderCarrier])

        whenReady(result) { result =>
          result shouldBe Left(NotFoundError(exceptionMessage))

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
