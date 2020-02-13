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
import org.mockito.Mockito.{verify, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import utils.{JsonUtil, PayeAtsDataUtil}
import utils.TestConstants._

import scala.concurrent.Future

class NpsServiceTest extends UnitSpec with MockitoSugar with JsonUtil with GuiceOneAppPerTest {

  implicit val hc = HeaderCarrier()

  val expectedNpsResponse: String = load("/paye_annual_tax_summary.json")

  val atsData: PayeAtsData = PayeAtsDataUtil.atsData

  lazy val transformedData: PayeAtsMiddleTier =
    atsData.transformToPayeMiddleTier(testNino, currentYear)

  class TestService extends NpsService {

    override lazy val npsConnector: NpsConnector = mock[NpsConnector]
  }

  private val currentYear = 2018

  "getPayeATSData" should {
    "return a successful future" in new TestService {

      when(npsConnector.connectToPayeTaxSummary(eqTo(testNino), eqTo(currentYear))(any[HeaderCarrier]))
        .thenReturn(Future.successful(Json.parse(expectedNpsResponse)))

      val result = await(getPayeATSData(testNino, currentYear))

      result shouldBe transformedData
    }
  }
}
