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

package connectors
import org.mockito.Matchers.{eq => eqTo, _}
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import play.api.libs.json.JsValue
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads}
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import utils.BaseSpec
import utils.TestConstants._

import scala.concurrent.{ExecutionContext, Future}

class ODSConnectorTest extends BaseSpec with MockitoSugar with ScalaFutures {

  val http = mock[HttpClient]

  class TestConnector extends ODSConnector(http, applicationConfig)

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
