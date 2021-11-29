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

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.{ok, urlEqualTo}
import models.{Amount, AtsMiddleTierData, DataHolder, LiabilityKey}
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout}
import utils.{FileHelper, IntegrationSpec}

import scala.concurrent.Future

trait SaTestHelper extends IntegrationSpec {

  override def beforeEach() = {
    super.beforeEach()

    val taxPayerUrl = "/self-assessment/individual/" + utr + "/designatory-details/taxpayer"

    server.stubFor(
      WireMock.get(urlEqualTo(taxPayerUrl))
        .willReturn(ok(FileHelper.loadFile("taxPayerDetails.json")))
    )

  }

  def resultToAtsData(resultOption: Option[Future[Result]]): AtsMiddleTierData = {

    resultOption match {
      case Some(result) => Json.parse(contentAsString(result)).as[AtsMiddleTierData]
      case None => throw new NoSuchElementException
    }
  }

  def checkResult(data: AtsMiddleTierData, checkLiability: Map[LiabilityKey, Double]) = {

    def dataToFind(data: AtsMiddleTierData, key: LiabilityKey) = {
      val incomeData = data.income_data
      val summaryData = data.summary_data
      val allowanceData = data.allowance_data
      val capitalGainsData = data.capital_gains_data

      val dataList: List[DataHolder] = List(incomeData, summaryData, allowanceData, capitalGainsData).flatten

      val mappedList = dataList.flatMap(_.payload.get.get(key))

      if (mappedList.size == 1) {
        mappedList.head
      } else if (mappedList.isEmpty) {
        throw new RuntimeException(s"$key: No keys")
      } else {
        throw new RuntimeException(s"$key: Too many keys")
      }
    }

    checkLiability.foreach {
      case (key, value) =>
        s"$key is calculated" in {
          dataToFind(data, key) mustBe Amount(value, "GBP")
        }
    }

  }



}
