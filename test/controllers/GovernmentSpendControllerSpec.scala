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

package controllers

import akka.stream.Materializer
import controllers.auth.{AuthAction, FakeAuthAction, PayeAuthAction}
import org.mockito.Matchers.{eq => meq}
import org.mockito.Mockito.when
import play.api.http.Status.{BAD_REQUEST, OK}
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout, status, stubControllerComponents}
import services.GoodsAndServices.Environment
import services.{GoodsAndServices, GovSpendService}
import utils.TestConstants.{testNino, testUtr}
import utils.{BaseSpec, NinoHelper}

class GovernmentSpendControllerSpec extends BaseSpec {

  val mockGovSpendService: GovSpendService = mock[GovSpendService]

  val taxYear = 2019

  implicit val mat = app.injector.instanceOf[Materializer]

  val expectedBody = """{"Environment":5.5}"""

  "GovernmentSpendController" must {

    "return government spend figures" when {

      def sut = new GovernmentSpendController(
        mockGovSpendService,
        app.injector.instanceOf[NinoHelper],
        FakeAuthAction,
        FakeAuthAction,
        stubControllerComponents()
      )

      when(mockGovSpendService.govSpending(meq(taxYear))) thenReturn Map[GoodsAndServices, Double](Environment -> 5.5)

      "the URI contains a valid nino" in {

        val result = sut.getGovernmentSpend(taxYear, testNino)(FakeRequest("GET", "/"))
        status(result) mustBe OK
        contentAsString(result) mustBe expectedBody
      }

      "the URI contains a valid utr" in {
        val result = sut.getGovernmentSpend(taxYear, testUtr)(FakeRequest("GET", "/"))
        status(result) mustBe OK
        contentAsString(result) mustBe expectedBody
      }
    }

    "return BadRequest" when {

      def sut = new GovernmentSpendController(
        app.injector.instanceOf[GovSpendService],
        app.injector.instanceOf[NinoHelper],
        app.injector.instanceOf[AuthAction],
        app.injector.instanceOf[PayeAuthAction],
        stubControllerComponents()
      )

      "an invalid nino is given" in {
        val result = sut.getGovernmentSpend(taxYear, "A123456")(FakeRequest("GET", "/"))
        status(result) mustBe BAD_REQUEST
      }

      "an invalid utr is given" in {
        val result = sut.getGovernmentSpend(taxYear, "foobar123")(FakeRequest("GET", "/"))
        status(result) mustBe BAD_REQUEST
      }
    }
  }
}
