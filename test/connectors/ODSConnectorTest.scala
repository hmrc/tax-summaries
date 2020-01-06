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

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import play.api.libs.json.JsValue
import uk.gov.hmrc.play.test.UnitSpec
import org.mockito.Mockito._
import org.mockito.Matchers.{eq => eqTo, _}
import utils.TestConstants._

import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.http.{HeaderCarrier, HttpGet, HttpReads}
import uk.gov.hmrc.play.config.ServicesConfig

class ODSConnectorTest extends UnitSpec with MockitoSugar with ScalaFutures {

  class TestConnector extends ODSConnector with ServicesConfig {
    override lazy val serviceUrl = ""
    override lazy val http = mock[HttpGet]
  }

  "connectToSelfAssessment" should {

    "return successful future" in new TestConnector {

      when(
        http.GET[JsValue](eqTo(url("/self-assessment/individuals/" + testUtr + "/annual-tax-summaries/2014")))(
          any[HttpReads[JsValue]],
          any[HeaderCarrier],
          any[ExecutionContext]))
        .thenReturn(Future.successful(mock[JsValue]))

      val result = connectToSelfAssessment(testUtr, 2014)(mock[HeaderCarrier])

      whenReady(result) {
        _ shouldBe a[JsValue]
      }
    }

    "return failed future" in new TestConnector {

      when(
        http.GET[JsValue](eqTo(url("/self-assessment/individuals/" + testUtr + "/annual-tax-summaries/2014")))(
          any[HttpReads[JsValue]],
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

    "return a successful future" in new TestConnector {

      when(
        http.GET[JsValue](eqTo(url("/self-assessment/individuals/" + testUtr + "/annual-tax-summaries")))(
          any[HttpReads[JsValue]],
          any[HeaderCarrier],
          any[ExecutionContext]))
        .thenReturn(Future.successful(mock[JsValue]))

      val result = connectToSelfAssessmentList(testUtr)(mock[HeaderCarrier])

      whenReady(result) {
        _ shouldBe a[JsValue]
      }
    }

    "return failed future" in new TestConnector {

      when(
        http.GET[JsValue](eqTo(url("/self-assessment/individuals/" + testUtr + "/annual-tax-summaries")))(
          any[HttpReads[JsValue]],
          any[HeaderCarrier],
          any[ExecutionContext]))
        .thenReturn(Future.failed(new Exception()))

      val result = connectToSelfAssessmentList(testUtr)(mock[HeaderCarrier])

      whenReady(result.failed) { exception =>
        exception shouldBe a[Exception]
      }
    }

  }

  "connectToSATaxpayerDetails" should {

    "return successful future" in new TestConnector {

      when(
        http.GET[JsValue](eqTo(url("/self-assessment/individual/" + testUtr + "/designatory-details/taxpayer")))(
          any[HttpReads[JsValue]],
          any[HeaderCarrier],
          any[ExecutionContext]))
        .thenReturn(Future.successful(mock[JsValue]))

      val result = connectToSATaxpayerDetails(testUtr)(mock[HeaderCarrier])

      whenReady(result) {
        _ shouldBe a[JsValue]
      }
    }

    "return failed future" in new TestConnector {

      when(
        http.GET[JsValue](eqTo(url("/self-assessment/individual/" + testUtr + "/designatory-details/taxpayer")))(
          any[HttpReads[JsValue]],
          any[HeaderCarrier],
          any[ExecutionContext]))
        .thenReturn(Future.failed(new Exception))

      val result = connectToSATaxpayerDetails(testUtr)(mock[HeaderCarrier])

      whenReady(result.failed) { exception =>
        exception shouldBe a[Exception]
      }

    }

  }

}
