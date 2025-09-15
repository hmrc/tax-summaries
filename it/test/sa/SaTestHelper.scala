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

package sa

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.{ok, urlEqualTo}
import common.models.{DataHolder, LiabilityKey}
import common.utils.{FileHelper, IntegrationSpec}
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout}
import sa.models.AtsMiddleTierData

import scala.concurrent.Future

trait SaTestHelper extends IntegrationSpec {

  val taxPayerFile: String

  override def beforeEach(): Unit = {
    super.beforeEach()

    val taxPayerUrl = "/self-assessment/individual/" + utr + "/designatory-details/taxpayer"

    server.stubFor(
      WireMock
        .get(urlEqualTo(taxPayerUrl))
        .willReturn(ok(FileHelper.loadFile(taxPayerFile)))
    )

  }

  def resultToAtsData(resultOption: Option[Future[Result]]): AtsMiddleTierData =
    resultOption match {
      case Some(result) =>
        Json.parse(contentAsString(result)).as[AtsMiddleTierData]
      case None         => throw new NoSuchElementException
    }

  def checkResult(data: AtsMiddleTierData, key: LiabilityKey, value: Double): Unit = {

    def dataToFind(data: AtsMiddleTierData, key: LiabilityKey) = {
      val incomeData       = data.income_data
      val summaryData      = data.summary_data
      val allowanceData    = data.allowance_data
      val capitalGainsData = data.capital_gains_data
      val incomeTax        = data.income_tax

      val dataList: List[DataHolder] = List(incomeData, summaryData, allowanceData, capitalGainsData, incomeTax).flatten

      val mappedList = dataList.flatMap(_.payload.get.get(key))

      if (mappedList.size == 1) {
        mappedList.head
      } else if (mappedList.isEmpty) {
        throw new RuntimeException(s"$key: the key is missing")
      } else {
        if (mappedList.map(_.amount).distinct.size == 1) {
          mappedList.head
        } else {
          throw new RuntimeException(s"$key: Too many keys")
        }
      }
    }

    if (data.errors.isEmpty) {
      dataToFind(data, key).amount mustBe BigDecimal(value)
    } else {
      throw new RuntimeException(s"error occurred ......." + data.errors.get.error)
    }

  }

}
