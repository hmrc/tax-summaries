/*
 * Copyright 2020 HM Revenue & Customs
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
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.libs.json.{JsResultException, JsValue, Json}
import uk.gov.hmrc.http.{BadGatewayException, HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.test.UnitSpec
import utils.TestConstants._
import utils.{JsonUtil, PayeAtsDataUtil}

import scala.collection.Seq
import scala.concurrent.Future

class DirectNpsServiceTest extends UnitSpec with MockitoSugar with JsonUtil with GuiceOneAppPerTest with ScalaFutures {

  implicit val hc = HeaderCarrier()
  val expectedNpsResponse: JsValue = Json.parse(load("/paye_annual_tax_summary.json"))
  val atsData: PayeAtsData = PayeAtsDataUtil.atsData
  lazy val transformedData: PayeAtsMiddleTier =
    atsData.transformToPayeMiddleTier(testNino, currentYear)

  class TestService extends DirectNpsService {

    override lazy val npsConnector: NpsConnector = mock[NpsConnector]
  }

  private val currentYear = 2018

  "getPayeATSData" should {

    "return a successful response after transforming NPS data to PAYE model" in new TestService {

      when(npsConnector.connectToPayeTaxSummary(eqTo(testNino), eqTo(currentYear))(any()))
        .thenReturn(Future.successful(
          HttpResponse(responseStatus = 200, responseJson = Some(expectedNpsResponse), responseHeaders = Map.empty)))

      val result = getPayeATSData(testNino, currentYear).futureValue

      result shouldBe Right(transformedData)
    }

    "return a Bad Gateway Response in case of Bad Gateway from Connector" in new TestService {

      val response = HttpResponse(responseStatus = 502)

      when(npsConnector.connectToPayeTaxSummary(eqTo(testNino), eqTo(currentYear))(any()))
        .thenReturn(Future.successful(response))

      val result = getPayeATSData(testNino, currentYear).futureValue

      result shouldBe Left(response)

    }

    "return INTERNAL_SERVER_ERROR response in case of Exception from NPS" in new TestService {

      when(npsConnector.connectToPayeTaxSummary(eqTo(testNino), eqTo(currentYear))(any()))
        .thenReturn(Future.failed(new JsResultException(List())))

      val result = getPayeATSData(testNino, currentYear).futureValue

      result.left.get.status shouldBe 500
    }
  }
}