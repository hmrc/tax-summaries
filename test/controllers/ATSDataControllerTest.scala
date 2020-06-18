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

package controllers

import controllers.auth.{AuthAction, FakeAuthAction}
import models.{AtsMiddleTierTaxpayerData, AtsYearList}
import org.mockito.Matchers.{eq => eqTo, _}
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.time.{Millis, Seconds, Span}
import play.api.libs.json.{JsNumber, JsString, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.OdsService
import uk.gov.hmrc.http.{HeaderCarrier, Upstream5xxResponse}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import utils.TestConstants._

import scala.concurrent.Future

class ATSDataControllerTest extends UnitSpec with MockitoSugar with WithFakeApplication with ScalaFutures {

  implicit val defaultPatience =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(500, Millis))

  class TestController extends ATSDataController {
    val request = FakeRequest()
    override lazy val odsService: OdsService = mock[OdsService]
    override val authAction: AuthAction = FakeAuthAction
  }

  "getAtsData" should {

    "return OK (200) if json is present" in new TestController {
      when(odsService.getPayload(eqTo(testUtr), eqTo(2014))(any[HeaderCarrier]))
        .thenReturn(Future.successful(Some(JsString("""{"test": "json"}"""))))
      val result = getATSData(testUtr, 2014)(request)
      status(result) shouldBe OK
    }

    "return NOT_FOUND (404) if no Json is returned" in new TestController {
      when(odsService.getPayload(eqTo(testUtr), eqTo(2014))(any[HeaderCarrier]))
        .thenReturn(Future.successful(None))
      val result = getATSData(testUtr, 2014)(request)
      status(result) shouldBe NOT_FOUND
    }

    "throw any exception encountered" in new TestController {
      when(odsService.getPayload(eqTo(testUtr), eqTo(2014))(any[HeaderCarrier]))
        .thenReturn(Future.failed(new Exception("failed")))
      val result = getATSData(testUtr, 2014)(request)

      whenReady(result.failed) { exception =>
        exception shouldBe a[Exception]
      }
    }
  }

  "hasAts" should {

    "return OK (200) if json is present" in new TestController {
      when(odsService.getList(eqTo(testUtr))(any[HeaderCarrier]))
        .thenReturn(Future.successful(Some(JsString("""{"test": "json"}"""))))
      val result = hasAts(testUtr)(request)
      status(result) shouldBe OK
    }

    "return Not Found (404) if there is no json" in new TestController {
      when(odsService.getList(eqTo(testUtr))(any[HeaderCarrier])).thenReturn(Future.successful(None))
      val result = hasAts(testUtr)(request)
      status(result) shouldBe NOT_FOUND
    }

    "return an exception if one is thrown" in new TestController {

      val exceptionMessage = "Something went wrong"

      when(odsService.getList(eqTo(testUtr))(any[HeaderCarrier]))
        .thenReturn(Future.failed(new Exception(exceptionMessage)))
      val result = hasAts(testUtr)(request)
      the[Exception] thrownBy {
        await(result)
      } should have message exceptionMessage
    }
  }

  "getATSList" should {

    "return OK" when {
      "ats data is retrieved and parsed correctly" in new TestController {

        val dataList = AtsYearList(
          testUtr,
          Some(AtsMiddleTierTaxpayerData(Some(Map("title" -> "Mr", "forename" -> "John", "surname" -> "Doe")), None)),
          Some(List(JsNumber(2014), JsNumber(2015)))
        )

        when(odsService.getATSList(eqTo(testUtr))(any[HeaderCarrier])) thenReturn Future.successful(
          Some(Json.toJson(dataList)))

        val result = getATSList(testUtr)(request)

        status(result) shouldBe OK
        contentAsString(result) should include("Doe")
      }
    }

    "return NOT_FOUND" when {
      "no ats data is found" in new TestController {
        val errorMessage = "No Json could be retrieved"
        when(odsService.getATSList(eqTo(testUtr))(any[HeaderCarrier])) thenReturn Future.successful(None)

        val result = getATSList(testUtr)(request)

        status(result) shouldBe NOT_FOUND
        contentAsString(result) shouldBe errorMessage
      }
    }

    "throw any exception returned by services" when {
      "there is an Upstream5xxResponse" in new TestController {
        when(odsService.getATSList(eqTo(testUtr))(any[HeaderCarrier])) thenReturn Future.failed(
          Upstream5xxResponse("Something went wrong", 500, 500))

        val result = getATSList(testUtr)(request)

        assertThrows[Upstream5xxResponse] {
          await(result)
        }
      }

      "any other exception occurs" in new TestController {
        when(odsService.getATSList(eqTo(testUtr))(any[HeaderCarrier])) thenReturn Future.failed(
          new Exception("An exception was thrown"))

        val result = getATSList(testUtr)(request)

        assertThrows[Exception] {
          await(result)
        }
      }
    }
  }
}
