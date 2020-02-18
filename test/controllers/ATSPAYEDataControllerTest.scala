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

import akka.util.Timeout
import controllers.ATSPAYEDataController
import controllers.auth.{AuthAction, FakeAuthAction}
import models.paye.PayeAtsMiddleTier
import org.mockito.Matchers.{eq => eqTo, _}
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.contentAsJson
import services.NpsService
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import utils.TestConstants._

import scala.concurrent.duration.Duration

class ATSPAYEDataControllerTest extends UnitSpec with MockitoSugar with WithFakeApplication with ScalaFutures {

  implicit val timeout = new Timeout(Duration.Zero)

  class TestController extends ATSPAYEDataController {
    val request = FakeRequest()
    override lazy val npsService: NpsService = mock[NpsService]
    override val authAction: AuthAction = FakeAuthAction
  }

  "getAtsData" should {

    val expectedResponse = PayeAtsMiddleTier(2018, testNino, None, None, None, None, None)

    "return success response with ATS data" in new TestController {
      when(npsService.getPayeATSData(eqTo(testNino), eqTo(2018))(any[HeaderCarrier]))
        .thenReturn(Right(expectedResponse))
      val result = getATSData(testNino, 2018)(request)

      status(result) shouldBe 200
      contentAsJson(result) shouldBe Json.toJson(expectedResponse)
    }

    "return a failed future" in new TestController {
      when(npsService.getPayeATSData(eqTo(testNino), eqTo(2018))(any[HeaderCarrier]))
        .thenReturn(Left(HttpResponse(responseStatus = BAD_REQUEST, responseJson = Some(Json.toJson(BAD_REQUEST)))))
      val result = getATSData(testNino, 2018)(request)

      status(result) shouldBe 400
      contentAsJson(result) shouldBe Json.toJson(BAD_REQUEST)
    }
  }
}
