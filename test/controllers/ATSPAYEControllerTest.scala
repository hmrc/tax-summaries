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

import controllers.ATSPAYEDataController
import org.mockito.Matchers.{eq => eqTo, _}
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.time.{Millis, Seconds, Span}
import play.api.libs.json.Json
import play.api.test.FakeRequest
import services.NpsService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import utils.TestConstants._

import scala.concurrent.Future

class ATSPAYEDataControllerTest extends UnitSpec with MockitoSugar with WithFakeApplication with ScalaFutures {

  implicit val defaultPatience =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(500, Millis))

  class TestController extends ATSPAYEDataController {
    val request = FakeRequest()
    override lazy val npsService: NpsService = mock[NpsService]
  }

  "getRawAtsData" should {
    "return ok" in new TestController {
      when(npsService.getRawPayload(eqTo(testNino), eqTo(2018))(any[HeaderCarrier]))
        .thenReturn(Future.successful(Json.obj()))
      val result = getRawATSData(testNino, 2018)(request)

      status(result) shouldBe 200
    }

    "return a failed future" in new TestController {
      when(npsService.getRawPayload(eqTo(testNino), eqTo(2018))(any[HeaderCarrier]))
        .thenReturn(Future.failed(new Exception("failed")))
      val result = getRawATSData(testNino, 2018)(request)

      whenReady(result.failed) { exception =>
        exception shouldBe a[Exception]
      }
    }
  }

  "getAtsData" should {
    "return ok" in new TestController {
      when(npsService.getPayload(eqTo(testNino), eqTo(2018))(any[HeaderCarrier]))
        .thenReturn(Future.successful(Json.obj()))
      val result = getATSData(testNino, 2018)(request)

      status(result) shouldBe 200
    }

    "return a failed future" in new TestController {
      when(npsService.getPayload(eqTo(testNino), eqTo(2018))(any[HeaderCarrier]))
        .thenReturn(Future.failed(new Exception("failed")))
      val result = getATSData(testNino, 2018)(request)

      whenReady(result.failed) { exception =>
        exception shouldBe a[Exception]
      }
    }
  }
}
