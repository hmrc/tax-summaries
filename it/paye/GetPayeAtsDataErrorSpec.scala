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

package paye

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, get, urlEqualTo}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.{status => getStatus, _}
import utils.IntegrationSpec

class GetPayeAtsDataErrorSpec extends IntegrationSpec {
  val npsAtsDataUrl = s"/individuals/annual-tax-summary/${nino.withoutSuffix}/$taxYearMinusOne"

  val apiUrl = s"/taxs/$nino/$taxYear/paye-ats-data"
  def request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, apiUrl)

  "Get Paye Ats Data" must {
    "return a blah for a BAD_REQUEST" in {
      server.stubFor(
        get(urlEqualTo(npsAtsDataUrl)).willReturn(
          aResponse()
            .withStatus(BAD_REQUEST)
            .withBody("Bad Request"))
      )

      val result = route(fakeApplication(), request)
      result.map(getStatus) shouldBe Some(BAD_REQUEST)
      result.map(contentAsString).contains("Bad Request") shouldBe true
    }

  }
}
