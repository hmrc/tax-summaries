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
import models.paye.{PayeAtsData, PayeAtsMiddeTier}
import org.mockito.Matchers.{any, eq => eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.libs.json.{JsObject, JsValue, Json}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.test.UnitSpec
import utils.TestConstants._

import scala.concurrent.Future

class NpsServiceTest extends UnitSpec with MockitoSugar with ScalaFutures with GuiceOneAppPerTest {

  implicit val defaultPatience =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(500, Millis))

  class TestService extends NpsService {
    val functionMock = mock[Function3[String, Int, PayeAtsData, PayeAtsMiddeTier]]

    override lazy val npsConnector: NpsConnector = mock[NpsConnector]

    override def convertData: (String, Int, PayeAtsData) => PayeAtsMiddeTier =
      functionMock
  }

  private val currentYear = 2018

  "getPayload" should {
    "return a successful future" in new TestService {
      val mockPayload: JsValue = mock[JsValue]
      when(npsConnector.connectToPayeTaxSummary(eqTo(testNino), eqTo(currentYear))(any[HeaderCarrier]))
        .thenReturn(Future.successful(mockPayload))
      when(mockPayload.as[PayeAtsData])
        .thenReturn(mock[PayeAtsData])
      when(functionMock.apply(eqTo(testNino), eqTo(currentYear), any()))
        .thenReturn(mock[PayeAtsMiddeTier])

      val result: Future[PayeAtsMiddeTier] = getPayload(testNino, currentYear)(mock[HeaderCarrier])
      verify(functionMock, times(1)).apply(any(), any(), any())
    }
  }
}
