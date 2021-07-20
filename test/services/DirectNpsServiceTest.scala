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

package services

import connectors.NpsConnector
import models.paye.{PayeAtsData, PayeAtsMiddleTier}
import org.mockito.Matchers.{any, eq => eqTo}
import org.mockito.Mockito.when
import org.scalatest.concurrent.IntegrationPatience
import play.api.libs.json.{JsResultException, JsValue, Json}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import utils.TestConstants._
import utils.{BaseSpec, JsonUtil, PayeAtsDataUtil}

import scala.concurrent.Future

class DirectNpsServiceTest extends BaseSpec with JsonUtil {

  implicit val hc = HeaderCarrier()

  val expectedNpsResponse: JsValue = Json.parse(load("/paye_annual_tax_summary.json"))
  val atsData: PayeAtsData = PayeAtsDataUtil.atsData
  lazy val transformedData: PayeAtsMiddleTier =
    atsData.transformToPayeMiddleTier(applicationConfig, testNino, currentYear)

  val npsConnector = mock[NpsConnector]

  class TestService extends DirectNpsService(applicationConfig, npsConnector)

  private val currentYear = 2019

  "getPayeATSData" must {

    "return a successful response after transforming NPS data to PAYE model" in new TestService {

      when(npsConnector.connectToPayeTaxSummary(eqTo(testNino), eqTo(currentYear - 1))(any()))
        .thenReturn(Future.successful(
          HttpResponse(responseStatus = 200, responseJson = Some(expectedNpsResponse), responseHeaders = Map.empty)))

      val result = getPayeATSData(testNino, currentYear).futureValue

      result mustBe Right(transformedData)
    }

    "return a Bad Gateway Response in case of Bad Gateway from Connector" in new TestService {

      val response = HttpResponse(responseStatus = 502)

      when(npsConnector.connectToPayeTaxSummary(eqTo(testNino), eqTo(currentYear - 1))(any()))
        .thenReturn(Future.successful(response))

      val result = getPayeATSData(testNino, currentYear).futureValue

      result mustBe Left(response)

    }

    "return INTERNAL_SERVER_ERROR response in case of Exception from NPS" in new TestService {

      when(npsConnector.connectToPayeTaxSummary(eqTo(testNino), eqTo(currentYear - 1))(any()))
        .thenReturn(Future.failed(new JsResultException(List())))

      val result = getPayeATSData(testNino, currentYear).futureValue

      result.left.get.status mustBe 500
    }
  }
}
