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

package services

import cats.data.EitherT
import connectors.NpsConnector
import models.paye.{PayeAtsData, PayeAtsMiddleTier}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import play.api.http.Status.BAD_GATEWAY
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, UpstreamErrorResponse}
import utils.TestConstants._
import utils.{BaseSpec, JsonUtil, PayeAtsDataUtil}

import scala.concurrent.ExecutionContext.Implicits.global

class DirectNpsServiceTest extends BaseSpec with JsonUtil {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val expectedNpsResponse: JsValue            = Json.parse(load("/payeAtsPayloads/paye_annual_tax_summary.json"))
  val atsData: PayeAtsData                    = PayeAtsDataUtil.atsData
  lazy val transformedData: PayeAtsMiddleTier =
    atsData.transformToPayeMiddleTier(applicationConfig, testNino, currentYear)

  val npsConnector: NpsConnector = mock[NpsConnector]

  class TestService extends DirectNpsService(applicationConfig, npsConnector)

  private val currentYear = 2019

  "getPayeATSData" must {

    "return a successful response after transforming NPS data to PAYE model" in new TestService {

      when(npsConnector.connectToPayeTaxSummary(eqTo(testNino), eqTo(currentYear - 1))(any()))
        .thenReturn(EitherT.rightT(HttpResponse(200, expectedNpsResponse, Map.empty)))

      val result: Either[UpstreamErrorResponse, PayeAtsMiddleTier] =
        getPayeATSData(testNino, currentYear).value.futureValue

      result mustBe Right(transformedData)
    }

    "return a UpstreamErrorResponse in case of UpstreamErrorResponse from Connector" in new TestService {

      val response: UpstreamErrorResponse = UpstreamErrorResponse("Bad Gateway", BAD_GATEWAY, BAD_GATEWAY)

      when(npsConnector.connectToPayeTaxSummary(eqTo(testNino), eqTo(currentYear - 1))(any()))
        .thenReturn(EitherT.leftT(response))

      val result: Either[UpstreamErrorResponse, PayeAtsMiddleTier] =
        getPayeATSData(testNino, currentYear).value.futureValue

      result mustBe Left(response)
    }

  }
}
