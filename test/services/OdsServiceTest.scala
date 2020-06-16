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

import com.fasterxml.jackson.core.JsonParseException
import connectors.ODSConnector
import errors.AtsError
import models.{AtsCheck, AtsMiddleTierData, AtsYearList, GenericError, JsonParseError, NotFoundError, ServiceUnavailableError}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.time.{Millis, Seconds, Span}
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.play.test.UnitSpec
import org.mockito.Mockito._
import org.mockito.Matchers.{eq => eqTo, _}
import org.scalatest.EitherValues
import utils.TaxsJsonHelper
import utils.TestConstants._

import scala.concurrent.Future
import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException, Upstream5xxResponse}

class OdsServiceTest extends UnitSpec with MockitoSugar with ScalaFutures with EitherValues {

  implicit val defaultPatience =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(500, Millis))

  trait TestService extends OdsService {
    override lazy val odsConnector: ODSConnector = mock[ODSConnector]
    override lazy val jsonHelper: TaxsJsonHelper = mock[TaxsJsonHelper]
  }

  "getPayload" should {

    "return a successful future" in new TestService {

      when(odsConnector.connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier]))
        .thenReturn(Future.successful(mock[JsValue]))
      when(odsConnector.connectToSelfAssessment(eqTo(testUtr), eqTo(2014))(any[HeaderCarrier]))
        .thenReturn(Future.successful(mock[JsValue]))
      when(jsonHelper.getAllATSData(any[JsValue], any[JsValue], eqTo(testUtr), eqTo(2014)))
        .thenReturn(mock[JsValue])

      val result = getPayload(testUtr, 2014)(mock[HeaderCarrier])

      whenReady(result) { result =>
        result shouldBe a[JsValue]

        verify(jsonHelper, times(1)).getAllATSData(any[JsValue], any[JsValue], eqTo(testUtr), eqTo(2014))
      }
    }

    "return a successful future when the connection fails (SaTaxpayerDetails)" in new TestService {

      when(odsConnector.connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier]))
        .thenReturn(Future.failed(mock[JsonParseException]))

      val result = getPayload(testUtr, 2014)(mock[HeaderCarrier])

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

    "return a successful future when the connection fails (SelfAssessment)" in new TestService {

      when(odsConnector.connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier]))
        .thenReturn(Future.successful(mock[JsValue]))
      when(odsConnector.connectToSelfAssessment(eqTo(testUtr), eqTo(2014))(any[HeaderCarrier]))
        .thenReturn(Future.failed(new Exception("raw exception")))

      val result = getPayload(testUtr, 2014)(mock[HeaderCarrier])

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

    "return a successful future" in new TestService {

      when(odsConnector.connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier]))
        .thenReturn(Future.successful(mock[JsValue]))
      when(jsonHelper.hasAtsForPreviousPeriod(any[JsValue]))
        .thenReturn(true)

      val result = getList(testUtr)(mock[HeaderCarrier])

      whenReady(result) { result =>
        result shouldBe a[JsValue]

        Json.fromJson[AtsCheck](result).asOpt shouldBe Some(AtsCheck(true))
      }
    }

    "return a failed future" in new TestService {

      when(odsConnector.connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier]))
        .thenReturn(Future.failed(new Exception()))

      val result = getList(testUtr)(mock[HeaderCarrier])

      whenReady(result.failed) { exception =>
        exception shouldBe a[Exception]
        verify(jsonHelper, never()).hasAtsForPreviousPeriod(any[JsValue])
      }
    }
  }

  "getATSList" should {

    "return a right containing the ATS data" in new TestService {

      when(odsConnector.connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier]))
        .thenReturn(Future.successful(mock[JsValue]))
      when(odsConnector.connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier]))
        .thenReturn(Future.successful(mock[JsValue]))
      when(jsonHelper.createTaxYearJson(any[JsValue], eqTo(testUtr), any[JsValue]))
        .thenReturn(Future.successful(mock[JsValue]))

      val result = getATSList(testUtr)(mock[HeaderCarrier])

      whenReady(result) { result =>
        result.isRight shouldBe true

        verify(odsConnector, times(1)).connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier])
        verify(odsConnector, times(1)).connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier])
        verify(jsonHelper, times(1)).createTaxYearJson(any[JsValue], eqTo(testUtr), any[JsValue])
      }
    }

    "return a left" when {
      "parsing json throws an exception" in new TestService {

        when(odsConnector.connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier]))
          .thenReturn(Future.failed(mock[JsonParseException]))

        val result = getATSList(testUtr)(mock[HeaderCarrier])

        whenReady(result) { result =>
          result shouldBe Left(JsonParseError("Failed to parse Json for ATS List"))

          verify(odsConnector, times(1)).connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier])
          verify(odsConnector, never()).connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier])
          verify(jsonHelper, never()).createTaxYearJson(any[JsValue], eqTo(testUtr), any[JsValue])

        }
      }

      "No Ats Data is found" in new TestService {

        when(odsConnector.connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier]))
          .thenReturn(Future.successful(mock[JsValue]))
        when(odsConnector.connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier]))
          .thenReturn(Future.failed(mock[NotFoundException]))

        val result = getATSList(testUtr)(mock[HeaderCarrier])

        whenReady(result) { result =>
          result shouldBe Left(NotFoundError("No ATS found"))

          verify(odsConnector, times(1)).connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier])
          verify(odsConnector, times(1)).connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier])
          verify(jsonHelper, never()).createTaxYearJson(any[JsValue], eqTo(testUtr), any[JsValue])
        }
      }

      "an upstream 5xx response is received" in new TestService {

        when(odsConnector.connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier]))
          .thenReturn(Future.failed(Upstream5xxResponse("raw exception", 500, 500)))

        val result = getATSList(testUtr)(mock[HeaderCarrier])

        whenReady(result) { result =>
          result shouldBe Left(ServiceUnavailableError("odsConnector received a 5xx response"))

          verify(odsConnector, times(1)).connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier])
          verify(odsConnector, never()).connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier])
          verify(jsonHelper, never()).createTaxYearJson(any[JsValue], eqTo(testUtr), any[JsValue])
        }
      }

      "an unknown error occurs" in new TestService {

        when(odsConnector.connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier]))
          .thenReturn(Future.failed(new Exception("raw exception")))

        val result = getATSList(testUtr)(mock[HeaderCarrier])

        whenReady(result) { result =>
          result shouldBe Left(GenericError("Failed to get ATS List"))

          verify(odsConnector, times(1)).connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier])
          verify(odsConnector, never()).connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier])
          verify(jsonHelper, never()).createTaxYearJson(any[JsValue], eqTo(testUtr), any[JsValue])
        }
      }
    }
  }
}
