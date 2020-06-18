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

package connectors

import models.Liability.IncomeTaxDue
import models.ODSModels.{SaTaxpayerDetails, SelfAssessmentList}
import models.{Amount, PensionTaxRate, TaxSummaryLiability}
import org.mockito.Matchers.{eq => eqTo, _}
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import uk.gov.hmrc.http.{HeaderCarrier, HttpGet, HttpReads}
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.test.UnitSpec
import utils.TestConstants._

import scala.concurrent.{ExecutionContext, Future}

class ODSConnectorTest extends UnitSpec with MockitoSugar with ScalaFutures {

  class TestConnector extends ODSConnector with ServicesConfig {
    override lazy val serviceUrl = ""
    override lazy val http = mock[HttpGet]
  }

  "connectToSelfAssessment" should {

    "return an instance of TaxSummaryLiability" in new TestConnector {

      val fakeTaxSummaryLiability =
        TaxSummaryLiability(
          2014,
          PensionTaxRate(5.0),
          None,
          Map(IncomeTaxDue -> Amount(BigDecimal(12.0), "gbp")),
          Map(IncomeTaxDue -> Amount(BigDecimal(12.0), "gbp"))
        )

      when(
        http.GET[Option[TaxSummaryLiability]](
          eqTo(url("/self-assessment/individuals/" + testUtr + "/annual-tax-summaries/2014")))(
          any[HttpReads[Option[TaxSummaryLiability]]],
          any[HeaderCarrier],
          any[ExecutionContext]))
        .thenReturn(Future.successful(Some(fakeTaxSummaryLiability)))

      val result = connectToSelfAssessment(testUtr, 2014)(mock[HeaderCarrier])

      await(result).get shouldBe a[TaxSummaryLiability]
    }

    "return None" when {
      "http call returns None" in new TestConnector {
        when(
          http.GET[Option[TaxSummaryLiability]](
            eqTo(url("/self-assessment/individuals/" + testUtr + "/annual-tax-summaries/2014")))(
            any[HttpReads[Option[TaxSummaryLiability]]],
            any[HeaderCarrier],
            any[ExecutionContext]))
          .thenReturn(Future.successful(None))

        val result = connectToSelfAssessment(testUtr, 2014)(mock[HeaderCarrier])

        await(result) shouldBe None
      }
    }

    "throw exceptions when they occur" in new TestConnector {

      when(
        http.GET[Option[TaxSummaryLiability]](
          eqTo(url("/self-assessment/individuals/" + testUtr + "/annual-tax-summaries/2014")))(
          any[HttpReads[Option[TaxSummaryLiability]]],
          any[HeaderCarrier],
          any[ExecutionContext]))
        .thenReturn(Future.failed(new Exception))

      val result = connectToSelfAssessment(testUtr, 2014)(mock[HeaderCarrier])

      whenReady(result.failed) { exception =>
        exception shouldBe a[Exception]
      }
    }

  }

  "connectToSelfAssessmentList" should {

    "return an instance of SelfAssessmentList" in new TestConnector {

      when(
        http.GET[Option[SelfAssessmentList]](
          eqTo(url("/self-assessment/individuals/" + testUtr + "/annual-tax-summaries")))(
          any[HttpReads[Option[SelfAssessmentList]]],
          any[HeaderCarrier],
          any[ExecutionContext]))
        .thenReturn(Future.successful(Some(mock[SelfAssessmentList])))

      val result = connectToSelfAssessmentList(testUtr)(mock[HeaderCarrier])

      await(result).get shouldBe a[SelfAssessmentList]
    }

    "return None" when {
      "http call returns None" in new TestConnector {
        when(
          http.GET[Option[SelfAssessmentList]](
            eqTo(url("/self-assessment/individuals/" + testUtr + "/annual-tax-summaries")))(
            any[HttpReads[Option[SelfAssessmentList]]],
            any[HeaderCarrier],
            any[ExecutionContext]))
          .thenReturn(Future.successful(None))

        val result = connectToSelfAssessmentList(testUtr)(mock[HeaderCarrier])

        await(result) shouldBe None
      }
    }

    "throw exceptions when they occur" in new TestConnector {

      when(
        http.GET[Option[SelfAssessmentList]](
          eqTo(url("/self-assessment/individuals/" + testUtr + "/annual-tax-summaries")))(
          any[HttpReads[Option[SelfAssessmentList]]],
          any[HeaderCarrier],
          any[ExecutionContext]))
        .thenReturn(Future.failed(new Exception()))

      val result = connectToSelfAssessmentList(testUtr)(mock[HeaderCarrier])

      assertThrows[Exception] {
        await(result)
      }
    }

  }

  "connectToSATaxpayerDetails" should {

    "return an instance of SaTaxpayerDetails" in new TestConnector {

      when(
        http.GET[Option[SaTaxpayerDetails]](
          eqTo(url("/self-assessment/individual/" + testUtr + "/designatory-details/taxpayer")))(
          any[HttpReads[Option[SaTaxpayerDetails]]],
          any[HeaderCarrier],
          any[ExecutionContext]))
        .thenReturn(Future.successful(Some(mock[SaTaxpayerDetails])))

      val result = connectToSATaxpayerDetails(testUtr)(mock[HeaderCarrier])

      await(result).get shouldBe a[SaTaxpayerDetails]
    }

    "return None" when {
      "http call returns None" in new TestConnector {
        when(
          http.GET[Option[SaTaxpayerDetails]](
            eqTo(url("/self-assessment/individual/" + testUtr + "/designatory-details/taxpayer")))(
            any[HttpReads[Option[SaTaxpayerDetails]]],
            any[HeaderCarrier],
            any[ExecutionContext]))
          .thenReturn(Future.successful(None))

        val result = connectToSATaxpayerDetails(testUtr)(mock[HeaderCarrier])

        await(result) shouldBe None
      }
    }

    "throw exceptions when they occur" in new TestConnector {

      when(
        http.GET[Option[SaTaxpayerDetails]](
          eqTo(url("/self-assessment/individual/" + testUtr + "/designatory-details/taxpayer")))(
          any[HttpReads[Option[SaTaxpayerDetails]]],
          any[HeaderCarrier],
          any[ExecutionContext]))
        .thenReturn(Future.failed(new Exception))

      val result = connectToSATaxpayerDetails(testUtr)(mock[HeaderCarrier])

      assertThrows[Exception] {
        await(result)
      }
    }
  }
}
