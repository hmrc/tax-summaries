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

import controllers.auth.FakeAuthAction
import controllers.ATSDataController
import controllers.auth.AuthAction
import models.{AtsMiddleTierTaxpayerData, AtsYearList, GenericError, NotFoundError, ServiceUnavailableError}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.time.{Millis, Seconds, Span}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.OdsService
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import org.mockito.Mockito._
import org.mockito.Matchers.{eq => eqTo, _}
import play.api.libs.json.{JsNumber, Json}
import utils.TestConstants._

import scala.concurrent.Future
import uk.gov.hmrc.http.HeaderCarrier

class ATSDataControllerTest extends UnitSpec with MockitoSugar with WithFakeApplication with ScalaFutures {

  implicit val defaultPatience =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(500, Millis))

  class TestController extends ATSDataController {
    val request = FakeRequest()
    override lazy val odsService: OdsService = mock[OdsService]
    override val authAction: AuthAction = FakeAuthAction
  }

  "getAtsData" should {

    "return a failed future" in new TestController {
      when(odsService.getPayload(eqTo(testUtr), eqTo(2014))(any[HeaderCarrier]))
        .thenReturn(Future.failed(new Exception("failed")))
      val result = getATSData(testUtr, 2014)(request)

      whenReady(result.failed) { exception =>
        exception shouldBe a[Exception]
      }
    }
  }

  "hasAts" should {

    "return Not Found (404) on error" in new TestController {
      when(odsService.getList(eqTo(testUtr))(any[HeaderCarrier])).thenReturn(Future.failed(new Exception("failed")))
      val result = hasAts(testUtr)(request)
      status(result) shouldBe 404
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
          Right(Json.toJson(dataList)))

        val result = getATSList(testUtr)(request)

        status(result) shouldBe OK
        contentAsString(result) should include("Doe")
      }
    }

    "return NOT_FOUND" when {
      "no ats data is found" in new TestController {
        val errorMessage = "No ATS found"
        when(odsService.getATSList(eqTo(testUtr))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(NotFoundError(errorMessage)))

        val result = getATSList(testUtr)(request)

        status(result) shouldBe NOT_FOUND
        contentAsString(result) shouldBe errorMessage
      }
    }

    "return SERVICE_UNAVAILABLE" when {
      "there is an Upstream5xxResponse" in new TestController {
        val errorMessage = "Upstream5xxResponse found"
        when(odsService.getATSList(eqTo(testUtr))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(ServiceUnavailableError(errorMessage)))

        val result = getATSList(testUtr)(request)

        status(result) shouldBe SERVICE_UNAVAILABLE
        contentAsString(result) shouldBe errorMessage
      }
    }

    "return INTERNAL_SERVER_ERROR" when {
      "an unknown error occurs" in new TestController {
        val errorMessage = "Unknown error occurred"
        when(odsService.getATSList(eqTo(testUtr))(any[HeaderCarrier])) thenReturn Future.successful(
          Left(GenericError(errorMessage)))

        val result = getATSList(testUtr)(request)

        status(result) shouldBe INTERNAL_SERVER_ERROR
        contentAsString(result) shouldBe errorMessage
      }

      "an exception occurs" in new TestController {
        val errorMessage = "An exception was thrown"
        when(odsService.getATSList(eqTo(testUtr))(any[HeaderCarrier])) thenReturn Future.failed(
          new Exception(errorMessage))

        val result = getATSList(testUtr)(request)

        status(result) shouldBe INTERNAL_SERVER_ERROR
        contentAsString(result) shouldBe errorMessage
      }
    }
  }
}
