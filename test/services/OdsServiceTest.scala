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
import models.Liability.IncomeTaxDue
import models.{Amount, AtsCheck, PensionTaxRate, TaxSummaryLiability}
import models.ODSModels.{SaTaxpayerDetails, SelfAssessmentList}
import org.mockito.Matchers.{eq => eqTo, _}
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.exceptions.TestFailedException
import org.scalatest.mockito.MockitoSugar
import org.scalatest.time.{Millis, Seconds, Span}
import play.api.libs.json.JsValue
import uk.gov.hmrc.http.{HeaderCarrier, Upstream5xxResponse}
import uk.gov.hmrc.play.test.UnitSpec
import utils.TaxsJsonHelper
import utils.TestConstants._

import scala.concurrent.Future

class OdsServiceTest extends UnitSpec with MockitoSugar with ScalaFutures {

  implicit val defaultPatience =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(500, Millis))

  trait TestService extends OdsService {
    override lazy val odsConnector: ODSConnector = mock[ODSConnector]
    override lazy val jsonHelper: TaxsJsonHelper = mock[TaxsJsonHelper]
  }

  "getPayload" should {

    "return a successful future" in new TestService {

      val fakeTaxSummaryLiability =
        TaxSummaryLiability(
          2014,
          PensionTaxRate(5.0),
          None,
          Map(IncomeTaxDue -> Amount(BigDecimal(12.0), "gbp")),
          Map(IncomeTaxDue -> Amount(BigDecimal(12.0), "gbp"))
        )

      when(odsConnector.connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier]))
        .thenReturn(Future.successful(Some(mock[SaTaxpayerDetails])))
      when(odsConnector.connectToSelfAssessment(eqTo(testUtr), eqTo(2014))(any[HeaderCarrier]))
        .thenReturn(Future.successful(Some(fakeTaxSummaryLiability)))
      when(jsonHelper.getAllATSData(any[SaTaxpayerDetails], any[TaxSummaryLiability], eqTo(testUtr), eqTo(2014)))
        .thenReturn(mock[JsValue])

      val result = getPayload(testUtr, 2014)(mock[HeaderCarrier])

      whenReady(result) { result =>
        result.get shouldBe a[JsValue]

        verify(jsonHelper, times(1))
          .getAllATSData(any[SaTaxpayerDetails], any[TaxSummaryLiability], eqTo(testUtr), eqTo(2014))
      }
    }

    "throw the same exception if the connector throws" when {
      "SaTaxpayerDetails connector call fails" in new TestService {

        when(odsConnector.connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier]))
          .thenReturn(Future.failed(mock[JsonParseException]))

        val result = getPayload(testUtr, 2014)(mock[HeaderCarrier])

        assertThrows[JsonParseException] {
          await(result)
        }

        verify(odsConnector, times(1)).connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier])
        verify(odsConnector, never()).connectToSelfAssessment(eqTo(testUtr), eqTo(2014))(any[HeaderCarrier])
        verify(jsonHelper, never())
          .getAllATSData(any[SaTaxpayerDetails], any[TaxSummaryLiability], eqTo(testUtr), eqTo(2014))
      }

      "SelfAssessment connector call fails" in new TestService {

        val exceptionMessage = "Something went wrong"

        when(odsConnector.connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier]))
          .thenReturn(Future.successful(Some(mock[SaTaxpayerDetails])))
        when(odsConnector.connectToSelfAssessment(eqTo(testUtr), eqTo(2014))(any[HeaderCarrier]))
          .thenReturn(Future.failed(new Exception(exceptionMessage)))

        val result = getPayload(testUtr, 2014)(mock[HeaderCarrier])

        the[Exception] thrownBy {
          await(result)
        } should have message exceptionMessage

        verify(odsConnector, times(1)).connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier])
        verify(odsConnector, times(1)).connectToSelfAssessment(eqTo(testUtr), eqTo(2014))(any[HeaderCarrier])
        verify(jsonHelper, never())
          .getAllATSData(any[SaTaxpayerDetails], any[TaxSummaryLiability], eqTo(testUtr), eqTo(2014))
      }
    }
  }

  "getList" should {

    "return a successful future" in new TestService {

      when(odsConnector.connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier]))
        .thenReturn(Future.successful(Some(mock[SelfAssessmentList])))
      when(jsonHelper.hasAtsForPreviousPeriod(any[SelfAssessmentList]))
        .thenReturn(true)

      val result = getList(testUtr)(mock[HeaderCarrier])

      whenReady(result) { result =>
        result.get shouldBe a[JsValue]

        result.getOrElse(throw new TestFailedException("Option should not be empty", 0)).asOpt[AtsCheck] shouldBe Some(
          AtsCheck(true))
      }
    }

    "return a failed future" in new TestService {

      when(odsConnector.connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier]))
        .thenReturn(Future.failed(new Exception()))

      val result = getList(testUtr)(mock[HeaderCarrier])

      assertThrows[Exception] {
        await(result)
      }

      verify(jsonHelper, never()).hasAtsForPreviousPeriod(any[SelfAssessmentList])
    }
  }

  "getATSList" should {

    "return a JsValue containing the ATS data" in new TestService {

      when(odsConnector.connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier]))
        .thenReturn(Future.successful(Some(mock[SelfAssessmentList])))
      when(odsConnector.connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier]))
        .thenReturn(Future.successful(Some(mock[SaTaxpayerDetails])))
      when(jsonHelper.createTaxYearJson(any[SelfAssessmentList], eqTo(testUtr), any[SaTaxpayerDetails]))
        .thenReturn(Future.successful(mock[JsValue]))

      val result = getATSList(testUtr)(mock[HeaderCarrier])

      whenReady(result) { result =>
        result.get shouldBe a[JsValue]

        verify(odsConnector, times(1)).connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier])
        verify(odsConnector, times(1)).connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier])
        verify(jsonHelper, times(1)).createTaxYearJson(any[SelfAssessmentList], eqTo(testUtr), any[SaTaxpayerDetails])
      }
    }

    "return none" when {
      "No Ats Data is found" in new TestService {

        when(odsConnector.connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier]))
          .thenReturn(Future.successful(Some(mock[SelfAssessmentList])))
        when(odsConnector.connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier]))
          .thenReturn(Future.successful(None))

        val result = getATSList(testUtr)(mock[HeaderCarrier])

        whenReady(result) { result =>
          result shouldBe None

          verify(odsConnector, times(1)).connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier])
          verify(odsConnector, times(1)).connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier])
          verify(jsonHelper, never()).createTaxYearJson(any[SelfAssessmentList], eqTo(testUtr), any[SaTaxpayerDetails])
        }
      }
    }

    "throw the same exception as the connector" when {
      "parsing json throws an exception" in new TestService {

        when(odsConnector.connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier]))
          .thenReturn(Future.failed(mock[JsonParseException]))

        val result = getATSList(testUtr)(mock[HeaderCarrier])

        assertThrows[JsonParseException] {
          await(result)
        }

        verify(odsConnector, times(1)).connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier])
        verify(odsConnector, never()).connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier])
        verify(jsonHelper, never()).createTaxYearJson(any[SelfAssessmentList], eqTo(testUtr), any[SaTaxpayerDetails])

      }

      "an upstream 5xx response is received" in new TestService {

        when(odsConnector.connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier]))
          .thenReturn(Future.failed(Upstream5xxResponse("raw exception", 500, 500)))

        val result = getATSList(testUtr)(mock[HeaderCarrier])

        assertThrows[Upstream5xxResponse] {
          await(result)
        }
        verify(odsConnector, times(1)).connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier])
        verify(odsConnector, never()).connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier])
        verify(jsonHelper, never()).createTaxYearJson(any[SelfAssessmentList], eqTo(testUtr), any[SaTaxpayerDetails])
      }

      "an unknown error occurs" in new TestService {

        when(odsConnector.connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier]))
          .thenReturn(Future.failed(new Exception("raw exception")))

        val result = getATSList(testUtr)(mock[HeaderCarrier])

        assertThrows[Exception] {
          await(result)
        }

        verify(odsConnector, times(1)).connectToSelfAssessmentList(eqTo(testUtr))(any[HeaderCarrier])
        verify(odsConnector, never()).connectToSATaxpayerDetails(eqTo(testUtr))(any[HeaderCarrier])
        verify(jsonHelper, never()).createTaxYearJson(any[SelfAssessmentList], eqTo(testUtr), any[SaTaxpayerDetails])
      }
    }
  }
}
