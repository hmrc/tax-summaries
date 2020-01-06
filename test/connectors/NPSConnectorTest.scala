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

import org.mockito.Matchers.{eq => eqTo, _}
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import play.api.libs.json.JsValue
import uk.gov.hmrc.http.{HeaderCarrier, HttpGet, HttpReads}
import uk.gov.hmrc.play.config.ServicesConfig
import uk.gov.hmrc.play.test.UnitSpec
import utils.TestConstants.{testNino, _}

import scala.concurrent.{ExecutionContext, Future}

class NPSConnectorTest extends UnitSpec with MockitoSugar with ScalaFutures {

  class TestConnector extends NPSConnector with ServicesConfig {
    override lazy val serviceUrl = ""
    override lazy val http = mock[HttpGet]
  }

  private val currentYear = 2018

  "connectToPayeTaxSummary" should {

    "return successful future" in new TestConnector {

      when(
        http.GET[JsValue](eqTo(url("/individuals/annual-tax-summary/" + testNino + "/" + currentYear)))(
          any[HttpReads[JsValue]],
          any[HeaderCarrier],
          any[ExecutionContext]))
        .thenReturn(Future.successful(mock[JsValue]))

      val result: Future[JsValue] = connectToPayeTaxSummary(testNino, currentYear)(mock[HeaderCarrier])

      whenReady(result) {
        _ shouldBe a[JsValue]
      }
    }

    "return failed future" in new TestConnector {

      when(
        http.GET[JsValue](eqTo(url("/individuals/annual-tax-summary/" + testNino + "/" + currentYear)))(
          any[HttpReads[JsValue]],
          any[HeaderCarrier],
          any[ExecutionContext]))
        .thenReturn(Future.failed(new Exception))

      val result: Future[JsValue] = connectToPayeTaxSummary(testNino, currentYear)(mock[HeaderCarrier])

      whenReady(result.failed) { exception =>
        exception shouldBe a[Exception]
      }
    }
  }
}
