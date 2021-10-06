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
import models.{BadRequestError, DownstreamClientError, DownstreamServerError, NotFoundError}
import models.paye.{PayeAtsData, PayeAtsMiddleTier}
import org.mockito.Matchers.{any, eq => eqTo}
import org.mockito.Mockito.when
import play.api.http.Status.{BAD_GATEWAY, BAD_REQUEST, INTERNAL_SERVER_ERROR, NOT_FOUND}
import play.api.libs.json.{JsResultException, JsValue, Json}
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, UpstreamErrorResponse}
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
        .thenReturn(Future.successful(Right(
          HttpResponse(responseStatus = 200, responseJson = Some(expectedNpsResponse), responseHeaders = Map.empty))))

      val result = getPayeATSData(testNino, currentYear).futureValue

      result mustBe Right(transformedData)
    }

    "return a DownstreamServerError in case of Bad Gateway from Connector" in new TestService {

      val response = UpstreamErrorResponse("Bad Gateway", BAD_GATEWAY, BAD_GATEWAY)

      val downstreamServerError: DownstreamServerError =
        DownstreamServerError(response.message)

      when(npsConnector.connectToPayeTaxSummary(eqTo(testNino), eqTo(currentYear - 1))(any()))
        .thenReturn(Future.successful(Left(response)))

      val result = getPayeATSData(testNino, currentYear).futureValue

      result mustBe Left(downstreamServerError)
    }

    "return a BadRequestError in case of bad request from Connector" in new TestService {

      val response = UpstreamErrorResponse("Bad request", BAD_REQUEST, INTERNAL_SERVER_ERROR)

      val badRequestError: BadRequestError = BadRequestError(response.message)

      when(npsConnector.connectToPayeTaxSummary(eqTo(testNino), eqTo(currentYear - 1))(any()))
        .thenReturn(Future.successful(Left(response)))

      val result = getPayeATSData(testNino, currentYear).futureValue

      result mustBe Left(badRequestError)
    }

    "return a NotFoundError in case of not found response from Connector" in new TestService {

      val response = UpstreamErrorResponse("Not found", NOT_FOUND, INTERNAL_SERVER_ERROR)

      val notFoundError: NotFoundError = NotFoundError(response.message)

      when(npsConnector.connectToPayeTaxSummary(eqTo(testNino), eqTo(currentYear - 1))(any()))
        .thenReturn(Future.successful(Left(response)))

      val result = getPayeATSData(testNino, currentYear).futureValue

      result mustBe Left(notFoundError)
    }

    "return a DownstreamClientError in case of a 4XX error from Connector" in new TestService {

      val response = UpstreamErrorResponse("Bad requewst", 412, INTERNAL_SERVER_ERROR)

      val downstreamClientError: DownstreamClientError =
        DownstreamClientError(response.message, response)

      when(npsConnector.connectToPayeTaxSummary(eqTo(testNino), eqTo(currentYear - 1))(any()))
        .thenReturn(Future.successful(Left(response)))

      val result = getPayeATSData(testNino, currentYear).futureValue

      result mustBe Left(downstreamClientError)
    }

    "return INTERNAL_SERVER_ERROR response in case of Exception from NPS" in new TestService {

      when(npsConnector.connectToPayeTaxSummary(eqTo(testNino), eqTo(currentYear - 1))(any()))
        .thenReturn(Future.failed(new JsResultException(List())))

      val result = getPayeATSData(testNino, currentYear)

      whenReady(result.failed) { e =>
        e mustBe a[JsResultException]
      }
    }
  }
}
