/*
 * Copyright 2022 HM Revenue & Customs
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

import connectors.ODSConnector
import controllers.auth.FakeAuthAction
import models.SpendData
import org.mockito.ArgumentMatchers.any
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, ControllerComponents}
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout, status, stubControllerComponents}
import services.OdsService
import utils.TestConstants._
import utils.{ATSErrorHandler, BaseSpec, TaxsJsonHelper}

import scala.concurrent.{ExecutionContext, Future}

class GovSpendingControllerTest extends BaseSpec {

  lazy val cc: ControllerComponents                = stubControllerComponents()
  val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
  lazy val atsErrorHandler: ATSErrorHandler        = inject[ATSErrorHandler]

  implicit lazy val ec: ExecutionContext = inject[ExecutionContext]

  val summaryJson          = "/utr_2014.json"
  val capitalGainsOnlyJson = "/test_gov_spend_capital_gains_only.json"
  val allTaxJson           = "/test_gov_spend_all_tax.json"
  val govSpendPath         = "/test_gov_spend_ref_data_year_2014.json"
  val taxPayerDataPath     = "/taxpayerData/test_individual_utr.json"

  def makeController(inputJson: String): AtsSaDataController = {

    val odsc = mock[ODSConnector]
    when(odsc.connectToSelfAssessment(any(), any())(any()))
      .thenReturn(MockConnections.connectToMockPayloadService(inputJson))
    when(odsc.connectToSATaxpayerDetails(any())(any()))
      .thenReturn(MockConnections.connectToMockPayloadService(taxPayerDataPath))

    val odsService = new OdsService(app.injector.instanceOf[TaxsJsonHelper], odsc)
    new AtsSaDataController(odsService, atsErrorHandler, FakeAuthAction, cc)
  }

  "Calling Government Spend with no session"                                must {
    "return a 200 response" in {

      val controllerUnderTest = makeController(summaryJson)
      val result2014          = Future.successful(controllerUnderTest.getAtsSaData("user", 2014)(request))
      val result2015          = Future.successful(controllerUnderTest.getAtsSaData("user", 2015)(request))

      status(result2014.futureValue) mustBe 200
      status(result2015.futureValue) mustBe 200
    }
    "have the right data in the output Json" in {

      val controllerUnderTest = makeController(summaryJson)
      val result2014          = Future.successful(controllerUnderTest.getAtsSaData(testUtr, 2014)(request))
      val result2015          = Future.successful(controllerUnderTest.getAtsSaData(testUtr, 2015)(request))

      val rawJsonString2014 = contentAsString(result2014.futureValue)
      val rawJson2014       = Json.parse(rawJsonString2014)

      val rawJsonString2015 = contentAsString(result2015.futureValue)
      val rawJson2015       = Json.parse(rawJsonString2015)

      (rawJson2014 \ "gov_spending" \ "govSpendAmountData" \ "Health").as[SpendData].amount.amount mustBe BigDecimal(
        "70.20"
      )
      (rawJson2014 \ "gov_spending" \ "govSpendAmountData" \ "Education").as[SpendData].percentage mustBe BigDecimal(
        "13.15"
      )
      (rawJson2014 \ "gov_spending" \ "govSpendAmountData" \ "BusinessAndIndustry")
        .as[SpendData]
        .amount
        .amount mustBe BigDecimal("10.19")
      (rawJson2014 \ "gov_spending" \ "govSpendAmountData" \ "Culture").as[SpendData].amount.amount mustBe BigDecimal(
        "6.29"
      )
      (rawJson2014 \ "gov_spending" \ "govSpendAmountData" \ "OverseasAid")
        .as[SpendData]
        .percentage mustBe BigDecimal("1.15")

      (rawJson2015 \ "gov_spending" \ "govSpendAmountData" \ "Health").as[SpendData].amount.amount mustBe BigDecimal(
        "74.03"
      )
      (rawJson2015 \ "gov_spending" \ "govSpendAmountData" \ "Education").as[SpendData].percentage mustBe BigDecimal(
        "12.5"
      )
      (rawJson2015 \ "gov_spending" \ "govSpendAmountData" \ "BusinessAndIndustry")
        .as[SpendData]
        .amount
        .amount mustBe BigDecimal("10.04")
      (rawJson2015 \ "gov_spending" \ "govSpendAmountData" \ "Culture").as[SpendData].amount.amount mustBe BigDecimal(
        "6.70"
      )
      (rawJson2015 \ "gov_spending" \ "govSpendAmountData" \ "OverseasAid")
        .as[SpendData]
        .percentage mustBe BigDecimal("1.3")
    }
  }
  "Calling Government Spend with only capital gains tax to pay"             must {
    "show the correct figures in the government spend screen" in {

      val controllerUnderTest = makeController(capitalGainsOnlyJson)
      val result2014          = Future.successful(controllerUnderTest.getAtsSaData("user", 2014)(request))
      val result2015          = Future.successful(controllerUnderTest.getAtsSaData("user", 2015)(request))

      val rawJsonString2014 = contentAsString(result2014.futureValue)
      val rawJson2014       = Json.parse(rawJsonString2014)

      val rawJsonString2015 = contentAsString(result2015.futureValue)
      val rawJson2015       = Json.parse(rawJsonString2015)

      (rawJson2014 \ "gov_spending" \ "govSpendAmountData" \ "Health").as[SpendData].amount.amount mustBe BigDecimal(
        "33.97"
      )
      (rawJson2014 \ "gov_spending" \ "govSpendAmountData" \ "Education").as[SpendData].percentage mustBe BigDecimal(
        "13.15"
      )
      (rawJson2014 \ "gov_spending" \ "govSpendAmountData" \ "BusinessAndIndustry")
        .as[SpendData]
        .amount
        .amount mustBe BigDecimal("4.93")
      (rawJson2014 \ "gov_spending" \ "govSpendAmountData" \ "Culture").as[SpendData].amount.amount mustBe BigDecimal(
        "3.04"
      )
      (rawJson2014 \ "gov_spending" \ "govSpendAmountData" \ "OverseasAid")
        .as[SpendData]
        .percentage mustBe BigDecimal("1.15")

      (rawJson2015 \ "gov_spending" \ "govSpendAmountData" \ "Health").as[SpendData].amount.amount mustBe BigDecimal(
        "35.82"
      )
      (rawJson2015 \ "gov_spending" \ "govSpendAmountData" \ "Education").as[SpendData].percentage mustBe BigDecimal(
        "12.5"
      )
      (rawJson2015 \ "gov_spending" \ "govSpendAmountData" \ "BusinessAndIndustry")
        .as[SpendData]
        .amount
        .amount mustBe BigDecimal("4.86")
      (rawJson2015 \ "gov_spending" \ "govSpendAmountData" \ "Culture").as[SpendData].amount.amount mustBe BigDecimal(
        "3.24"
      )
      (rawJson2015 \ "gov_spending" \ "govSpendAmountData" \ "OverseasAid")
        .as[SpendData]
        .percentage mustBe BigDecimal("1.3")
    }
  }
  "Calling Government Spend with income, NICs and Capital Gains tax to pay" must {
    "show the correct figures in the government spend screen" in {

      val controllerUnderTest = makeController(allTaxJson)
      val result2014          = Future.successful(controllerUnderTest.getAtsSaData("user", 2014)(request))
      val result2015          = Future.successful(controllerUnderTest.getAtsSaData("user", 2015)(request))

      val rawJsonString2014 = contentAsString(result2014.futureValue)
      val rawJson2014       = Json.parse(rawJsonString2014)

      val rawJsonString2015 = contentAsString(result2015.futureValue)
      val rawJson2015       = Json.parse(rawJsonString2015)

      (rawJson2014 \ "gov_spending" \ "govSpendAmountData" \ "Health").as[SpendData].amount.amount mustBe BigDecimal(
        "104.16"
      )
      (rawJson2014 \ "gov_spending" \ "govSpendAmountData" \ "BusinessAndIndustry")
        .as[SpendData]
        .amount
        .amount mustBe BigDecimal("15.12")
      (rawJson2014 \ "gov_spending" \ "govSpendAmountData" \ "Education").as[SpendData].percentage mustBe BigDecimal(
        "13.15"
      )
      (rawJson2014 \ "gov_spending" \ "govSpendAmountData" \ "Culture").as[SpendData].amount.amount mustBe BigDecimal(
        "9.33"
      )
      (rawJson2014 \ "gov_spending" \ "govSpendAmountData" \ "OverseasAid")
        .as[SpendData]
        .percentage mustBe BigDecimal("1.15")

      (rawJson2015 \ "gov_spending" \ "govSpendAmountData" \ "Health").as[SpendData].amount.amount mustBe BigDecimal(
        "109.85"
      )
      (rawJson2015 \ "gov_spending" \ "govSpendAmountData" \ "BusinessAndIndustry")
        .as[SpendData]
        .amount
        .amount mustBe BigDecimal("14.9")
      (rawJson2015 \ "gov_spending" \ "govSpendAmountData" \ "Education").as[SpendData].percentage mustBe BigDecimal(
        "12.5"
      )
      (rawJson2015 \ "gov_spending" \ "govSpendAmountData" \ "Culture").as[SpendData].amount.amount mustBe BigDecimal(
        "9.94"
      )
      (rawJson2015 \ "gov_spending" \ "govSpendAmountData" \ "OverseasAid")
        .as[SpendData]
        .percentage mustBe BigDecimal("1.3")
    }
  }
}
