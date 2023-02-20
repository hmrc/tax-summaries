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

package sa.regressions

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.{ok, urlEqualTo}
import errors.AtsError
import models.AtsMiddleTierData
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import sa.SaTestHelper
import utils.FileHelper

import java.time.LocalDate

class Ddcnl7055 extends SaTestHelper {
  val taxPayerFile     = "taxPayerDetails.json"
  val currentYear: Int = LocalDate.now().getYear

  trait Test {
    val taxYear = 2022

    def odsUrl(taxYear: Int): String = s"/self-assessment/individuals/" + utr + s"/annual-tax-summaries/$taxYear"

    def apiUrl(taxYear: Int): String = s"/taxs/$utr/$taxYear/ats-data"

    def request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, apiUrl(taxYear))
      .withHeaders((AUTHORIZATION, "Bearer 123"))

  }

  "DDCNL-7055: Given DES response" must {
    s"return NoAtsError" in new Test {
      server.stubFor(
        WireMock
          .get(urlEqualTo(odsUrl(taxYear)))
          .willReturn(
            ok(FileHelper.loadFile("regressions/DDCNL-7055-1.json"))
          )
      )

      val result: AtsMiddleTierData = resultToAtsData(route(app, request))
      result mustBe AtsMiddleTierData(
        2022,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        None,
        Some(AtsError("NoAtsError"))
      )
    }
  }
}
