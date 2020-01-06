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

package controller

import controllers.ATSDataController
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatest.time.{Millis, Seconds, Span}
import play.api.test.FakeRequest
import services.OdsService
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import org.mockito.Mockito._
import org.mockito.Matchers.{eq => eqTo, _}
import utils.TestConstants._
import scala.concurrent.Future
import uk.gov.hmrc.http.HeaderCarrier

class ATSDataControllerTest extends UnitSpec with MockitoSugar with WithFakeApplication with ScalaFutures {

  implicit val defaultPatience =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(500, Millis))

  class TestController extends ATSDataController {
    val request = FakeRequest()
    override lazy val odsService: OdsService = mock[OdsService]
  }

  "getAtsData" should {

    "return a failed future" in new TestController {
      when(odsService.getPayload(eqTo(testUtr), eqTo(2014))(any[HeaderCarrier]))
        .thenReturn(Future.failed(new Exception("failed")))
      val result = getATSData(testUtr, 2014)(request)

      whenReady(result.failed) { exception =>
        exception shouldBe a[Exception]
      }
    }
  }

  "hasAts" should {

    "return Not Found (404) on error" in new TestController {
      when(odsService.getList(eqTo(testUtr))(any[HeaderCarrier])).thenReturn(Future.failed(new Exception("failed")))
      val result = hasAts(testUtr)(request)
      status(result) shouldBe 404
    }
  }

  "getATSList" should {

    "return a failed future" in new TestController {
      when(odsService.getATSList(eqTo(testUtr))(any[HeaderCarrier])).thenReturn(Future.failed(new Exception("failed")))
      val result = getATSList(testUtr)(request)

      whenReady(result.failed) { exception =>
        exception shouldBe a[Exception]
      }
    }
  }
}
