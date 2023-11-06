/*
 * Copyright 2023 HM Revenue & Customs
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
import controllers.auth.FakeAuthJourney
import org.mockito.ArgumentMatchers.{eq => meq}
import play.api.http.Status.OK
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout, status, stubControllerComponents}
import services.GoodsAndServices.Environment
import services.{GoodsAndServices, GovSpendService}
import utils.BaseSpec

class GovernmentSpendControllerSpec extends BaseSpec {

  val mockGovSpendService: GovSpendService = mock[GovSpendService]

  val taxYear = 2020

  implicit val mat: Materializer = app.injector.instanceOf[Materializer]

  val expectedBody = """{"Environment":5.5}"""

  "GovernmentSpendController" must {
    def sut: GovernmentSpendController = new GovernmentSpendController(
      mockGovSpendService,
      FakeAuthJourney,
      stubControllerComponents()
    )

    when(mockGovSpendService.govSpending(meq(taxYear))) thenReturn Map[GoodsAndServices, Double](Environment -> 5.5)

    "return government spend figures" in {

      val result = sut.getGovernmentSpend(taxYear)(FakeRequest("GET", "/"))
      status(result) mustBe OK
      contentAsString(result) mustBe expectedBody

    }

  }
}
