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

package sa

import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, ok, urlEqualTo}
import play.api.http.HttpEntity
import play.api.libs.json.{JsLookupResult, JsResultException, JsValue, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.{FileHelper, IntegrationSpec}

class AtsDataSpec extends IntegrationSpec {

  val odsUrl = s"/self-assessment/individuals/" + utr + "/annual-tax-summaries/2020"
  val taxPayerUrl = "/self-assessment/individual/" + utr + "/designatory-details/taxpayer"

  val apiUrl = s"/taxs/$utr/2020/ats-data"

  "HasSummary" must {
    "return an OK when data is retrieved from ODS" in {

      server.stubFor(
        WireMock.get(urlEqualTo(odsUrl))
          .willReturn(ok(FileHelper.loadFile("2019-20/TC4.json")))
      )
      server.stubFor(
        WireMock.get(urlEqualTo(taxPayerUrl))
          .willReturn(ok(FileHelper.loadFile("taxPayerDetails.json")))
      )

      val request = FakeRequest(GET, apiUrl)
      val result = route(app, request).get


      status(result) mustBe OK
      val body = Json.parse(contentAsString(result))

      val otherIncome = (body \ "income_data" \ "payload" \ "other_pension_income" \ "amount").as[Double]
      otherIncome mustBe 0.0

    }
  }
}
