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

package paye

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, get, urlEqualTo}
import common.utils.IntegrationSpec
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.{status => getStatus, _}

class GetPayeAtsDataErrorSpec extends IntegrationSpec {
  val npsAtsDataUrl = s"/individuals/annual-tax-summary/${nino.withoutSuffix}/$taxYearMinusOne"

  val apiUrl                                       = s"/taxs/$nino/$taxYear/paye-ats-data"
  def request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, apiUrl).withHeaders((AUTHORIZATION, "Bearer 123"))

  "Get Paye Ats Data" must {
    "return an internal server error with an error message when NPS returns a BAD_REQUEST" in {
      server.stubFor(
        get(urlEqualTo(npsAtsDataUrl)).willReturn(
          aResponse()
            .withStatus(BAD_REQUEST)
            .withBody("Bad Request")
        )
      )

      val result = route(fakeApplication(), request)
      result.map(getStatus) mustBe Some(INTERNAL_SERVER_ERROR)
      result.map(contentAsString).map { message =>
        message must include("Bad Request")
      }
    }

    "return a NOT_FOUND with an error message when NPS returns a NOT_FOUND" in {
      server.stubFor(
        get(urlEqualTo(npsAtsDataUrl)).willReturn(
          aResponse()
            .withStatus(NOT_FOUND)
            .withBody("No data")
        )
      )

      val result = route(fakeApplication(), request)
      result.map(getStatus) mustBe Some(NOT_FOUND)
      result.map(contentAsString).map { message =>
        message must include("No data")
      }
    }

    "return a IM_A_TEAPOT with an error message when NPS returns a INTERNAL_SERVER_ERROR" in {
      server.stubFor(
        get(urlEqualTo(npsAtsDataUrl)).willReturn(
          aResponse()
            .withStatus(IM_A_TEAPOT)
            .withBody("teapot body")
        )
      )

      val result = route(fakeApplication(), request)
      result.map(getStatus) mustBe Some(INTERNAL_SERVER_ERROR)
      result.map(contentAsString).map { message =>
        message must include("teapot body")
      }
    }
  }
}
