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

package controller

import connectors.ODSConnector
import controllers.ATSDataController
import models.SpendData
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.libs.json.{JsValue, Json}
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout}
import play.api.test.FakeRequest
import play.test.WithApplication
import services.OdsService
import uk.gov.hmrc.play.test.UnitSpec
import utils.TaxsJsonHelper
import utils.TestConstants._

import scala.concurrent.Future
import uk.gov.hmrc.http.{HeaderCarrier, HttpGet}

class GovSpendingControllerTest extends UnitSpec with GuiceOneAppPerTest {

  val request = FakeRequest()

  val summaryJson = "/utr_2014.json"
  val capitalGainsOnlyJson = "/test_gov_spend_capital_gains_only.json"
  val allTaxJson = "/test_gov_spend_all_tax.json"
  val govSpendPath = "/test_gov_spend_ref_data_year_2014.json"
  val taxPayerDataPath = "/taxpayerData/test_individual_utr.json"

  def makeController(inputJson: String) = {
    val odsc = new ODSConnector {
      override def connectToSelfAssessment(UTR: String, TAX_YEAR: Int)(implicit hc: HeaderCarrier): Future[JsValue] =
        MockConnections.connectToMockPayloadService(inputJson)
      override def connectToSATaxpayerDetails(UTR: String)(implicit hc: HeaderCarrier): Future[JsValue] =
        MockConnections.connectToMockPayloadService(taxPayerDataPath)
      override def http: HttpGet = null
      override def serviceUrl: String = null
    }

    val odsServiceObject = new OdsService {
      override val odsConnector = odsc
      override val jsonHelper = new TaxsJsonHelper {}
    }

    new ATSDataController {
      override val odsService = odsServiceObject
    }
  }

  "Calling Government Spend with no session" should {
    "return a 200 response" in new WithApplication() {

      val controllerUnderTest = makeController(summaryJson)
      val result2014 = Future.successful(controllerUnderTest.getATSData("user", 2014)(request))
      val result2015 = Future.successful(controllerUnderTest.getATSData("user", 2015)(request))

      status(result2014) shouldBe 200
      status(result2015) shouldBe 200
    }
    "have the right data in the output Json" in new WithApplication() {

      val controllerUnderTest = makeController(summaryJson)
      val result2014 = Future.successful(controllerUnderTest.getATSData(testUtr, 2014)(request))
      val result2015 = Future.successful(controllerUnderTest.getATSData(testUtr, 2015)(request))

      val rawJsonString2014 = contentAsString(result2014)
      val rawJson2014 = Json.parse(rawJsonString2014)

      val rawJsonString2015 = contentAsString(result2015)
      val rawJson2015 = Json.parse(rawJsonString2015)

      (rawJson2014 \ "gov_spending" \ "govSpendAmountData" \ "Health").as[SpendData].amount.amount shouldBe BigDecimal(
        "70.20")
      (rawJson2014 \ "gov_spending" \ "govSpendAmountData" \ "Education").as[SpendData].percentage shouldBe BigDecimal(
        "13.15")
      (rawJson2014 \ "gov_spending" \ "govSpendAmountData" \ "BusinessAndIndustry")
        .as[SpendData]
        .amount
        .amount shouldBe BigDecimal("10.19")
      (rawJson2014 \ "gov_spending" \ "govSpendAmountData" \ "Culture").as[SpendData].amount.amount shouldBe BigDecimal(
        "6.29")
      (rawJson2014 \ "gov_spending" \ "govSpendAmountData" \ "OverseasAid")
        .as[SpendData]
        .percentage shouldBe BigDecimal("1.15")

      (rawJson2015 \ "gov_spending" \ "govSpendAmountData" \ "Health").as[SpendData].amount.amount shouldBe BigDecimal(
        "74.03")
      (rawJson2015 \ "gov_spending" \ "govSpendAmountData" \ "Education").as[SpendData].percentage shouldBe BigDecimal(
        "12.5")
      (rawJson2015 \ "gov_spending" \ "govSpendAmountData" \ "BusinessAndIndustry")
        .as[SpendData]
        .amount
        .amount shouldBe BigDecimal("10.04")
      (rawJson2015 \ "gov_spending" \ "govSpendAmountData" \ "Culture").as[SpendData].amount.amount shouldBe BigDecimal(
        "6.70")
      (rawJson2015 \ "gov_spending" \ "govSpendAmountData" \ "OverseasAid")
        .as[SpendData]
        .percentage shouldBe BigDecimal("1.3")
    }
  }
  "Calling Government Spend with only capital gains tax to pay" should {
    "show the correct figures in the government spend screen" in new WithApplication() {

      val controllerUnderTest = makeController(capitalGainsOnlyJson)
      val result2014 = Future.successful(controllerUnderTest.getATSData("user", 2014)(request))
      val result2015 = Future.successful(controllerUnderTest.getATSData("user", 2015)(request))

      val rawJsonString2014 = contentAsString(result2014)
      val rawJson2014 = Json.parse(rawJsonString2014)

      val rawJsonString2015 = contentAsString(result2015)
      val rawJson2015 = Json.parse(rawJsonString2015)

      (rawJson2014 \ "gov_spending" \ "govSpendAmountData" \ "Health").as[SpendData].amount.amount shouldBe BigDecimal(
        "33.97")
      (rawJson2014 \ "gov_spending" \ "govSpendAmountData" \ "Education").as[SpendData].percentage shouldBe BigDecimal(
        "13.15")
      (rawJson2014 \ "gov_spending" \ "govSpendAmountData" \ "BusinessAndIndustry")
        .as[SpendData]
        .amount
        .amount shouldBe BigDecimal("4.93")
      (rawJson2014 \ "gov_spending" \ "govSpendAmountData" \ "Culture").as[SpendData].amount.amount shouldBe BigDecimal(
        "3.04")
      (rawJson2014 \ "gov_spending" \ "govSpendAmountData" \ "OverseasAid")
        .as[SpendData]
        .percentage shouldBe BigDecimal("1.15")

      (rawJson2015 \ "gov_spending" \ "govSpendAmountData" \ "Health").as[SpendData].amount.amount shouldBe BigDecimal(
        "35.82")
      (rawJson2015 \ "gov_spending" \ "govSpendAmountData" \ "Education").as[SpendData].percentage shouldBe BigDecimal(
        "12.5")
      (rawJson2015 \ "gov_spending" \ "govSpendAmountData" \ "BusinessAndIndustry")
        .as[SpendData]
        .amount
        .amount shouldBe BigDecimal("4.86")
      (rawJson2015 \ "gov_spending" \ "govSpendAmountData" \ "Culture").as[SpendData].amount.amount shouldBe BigDecimal(
        "3.24")
      (rawJson2015 \ "gov_spending" \ "govSpendAmountData" \ "OverseasAid")
        .as[SpendData]
        .percentage shouldBe BigDecimal("1.3")
    }
  }
  "Calling Government Spend with income, NICs and Capital Gains tax to pay" should {
    "show the correct figures in the government spend screen" in new WithApplication() {

      val controllerUnderTest = makeController(allTaxJson)
      val result2014 = Future.successful(controllerUnderTest.getATSData("user", 2014)(request))
      val result2015 = Future.successful(controllerUnderTest.getATSData("user", 2015)(request))

      val rawJsonString2014 = contentAsString(result2014)
      val rawJson2014 = Json.parse(rawJsonString2014)

      val rawJsonString2015 = contentAsString(result2015)
      val rawJson2015 = Json.parse(rawJsonString2015)

      (rawJson2014 \ "gov_spending" \ "govSpendAmountData" \ "Health").as[SpendData].amount.amount shouldBe BigDecimal(
        "104.16")
      (rawJson2014 \ "gov_spending" \ "govSpendAmountData" \ "BusinessAndIndustry")
        .as[SpendData]
        .amount
        .amount shouldBe BigDecimal("15.12")
      (rawJson2014 \ "gov_spending" \ "govSpendAmountData" \ "Education").as[SpendData].percentage shouldBe BigDecimal(
        "13.15")
      (rawJson2014 \ "gov_spending" \ "govSpendAmountData" \ "Culture").as[SpendData].amount.amount shouldBe BigDecimal(
        "9.33")
      (rawJson2014 \ "gov_spending" \ "govSpendAmountData" \ "OverseasAid")
        .as[SpendData]
        .percentage shouldBe BigDecimal("1.15")

      (rawJson2015 \ "gov_spending" \ "govSpendAmountData" \ "Health").as[SpendData].amount.amount shouldBe BigDecimal(
        "109.85")
      (rawJson2015 \ "gov_spending" \ "govSpendAmountData" \ "BusinessAndIndustry")
        .as[SpendData]
        .amount
        .amount shouldBe BigDecimal("14.9")
      (rawJson2015 \ "gov_spending" \ "govSpendAmountData" \ "Education").as[SpendData].percentage shouldBe BigDecimal(
        "12.5")
      (rawJson2015 \ "gov_spending" \ "govSpendAmountData" \ "Culture").as[SpendData].amount.amount shouldBe BigDecimal(
        "9.94")
      (rawJson2015 \ "gov_spending" \ "govSpendAmountData" \ "OverseasAid")
        .as[SpendData]
        .percentage shouldBe BigDecimal("1.3")
    }
  }
}
