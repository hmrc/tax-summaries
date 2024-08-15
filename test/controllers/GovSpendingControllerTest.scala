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

package controllers

import cats.data.EitherT
import connectors.SelfAssessmentODSConnector
import controllers.auth.FakeAuthAction
import models.SpendData
import org.mockito.ArgumentMatchers.any
import play.api.http.Status.OK
import play.api.libs.json.Json
import play.api.mvc.{AnyContentAsEmpty, ControllerComponents}
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout, status, stubControllerComponents}
import services.OdsService
import uk.gov.hmrc.http.HttpResponse
import utils.TestConstants._
import utils.{ATSErrorHandler, BaseSpec, JsonUtil, TaxsJsonHelper}

import scala.concurrent.{ExecutionContext, Future}

class GovSpendingControllerTest extends BaseSpec {

  lazy val cc: ControllerComponents                     = stubControllerComponents()
  lazy val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
  lazy val atsErrorHandler: ATSErrorHandler             = inject[ATSErrorHandler]

  implicit lazy val ec: ExecutionContext                        = inject[ExecutionContext]
  implicit val userRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()
  val jsonHelper: TaxsJsonHelper                                = mock[TaxsJsonHelper]
  private val taxYear                                           = 2024
  private val summaryJson                                       = JsonUtil.load("/sa/sa_ats_valid.json", Map("<taxYear>" -> taxYear.toString))
  private val capitalGainsOnlyJson                              =
    JsonUtil.load("/sa/sa_ats_gov_spend_capital_gains_only.json", Map("<taxYear>" -> taxYear.toString))
  private val allTaxJson                                        =
    JsonUtil.load("/sa/sa_ats_gov_spend_all_tax.json", Map("<taxYear>" -> taxYear.toString))
  private val taxPayerDataPath                                  = JsonUtil.load("/taxpayer/sa_taxpayer-valid.json")

  def makeController(inputJson: String): AtsSaDataController = {
    val odsc = mock[SelfAssessmentODSConnector]
    when(odsc.connectToSelfAssessment(any(), any())(any(), any()))
      .thenReturn(EitherT.rightT(HttpResponse(OK, inputJson)))
    when(odsc.connectToSATaxpayerDetails(any())(any(), any()))
      .thenReturn(EitherT.rightT(HttpResponse(OK, taxPayerDataPath)))

    val odsService = new OdsService(app.injector.instanceOf[TaxsJsonHelper], odsc)
    new AtsSaDataController(
      odsService,
      atsErrorHandler,
      FakeAuthAction,
      cc,
      jsonHelper
    )
  }

  "Calling Government Spend with no session"                                must {
    "return a 200 response" in {
      val controllerUnderTest = makeController(summaryJson)
      val result2022          = Future.successful(controllerUnderTest.getAtsSaData("user", 2023)(request))
      val result2023          = Future.successful(controllerUnderTest.getAtsSaData("user", 2024)(request))

      status(result2022.futureValue) mustBe 200
      status(result2023.futureValue) mustBe 200
    }
    "have the right data in the output Json" in {

      val controllerUnderTest = makeController(summaryJson)
      val result2022          = Future.successful(controllerUnderTest.getAtsSaData(testUtr, 2023)(request))
      val result2023          = Future.successful(controllerUnderTest.getAtsSaData(testUtr, 2024)(request))

      val rawJsonString2022 = contentAsString(result2022.futureValue)
      val rawJson2022       = Json.parse(rawJsonString2022)

      val rawJsonString2023 = contentAsString(result2023.futureValue)
      val rawJson2023       = Json.parse(rawJsonString2023)

      (rawJson2022 \ "gov_spending" \ "govSpendAmountData" \ "Health").as[SpendData].amount.amount mustBe BigDecimal(
        "33605.46"
      )
      (rawJson2022 \ "gov_spending" \ "govSpendAmountData" \ "Education").as[SpendData].percentage mustBe BigDecimal(
        "9.9"
      )
      (rawJson2022 \ "gov_spending" \ "govSpendAmountData" \ "BusinessAndIndustry")
        .as[SpendData]
        .amount
        .amount mustBe BigDecimal("12899.07")

      (rawJson2022 \ "gov_spending" \ "govSpendAmountData" \ "Culture").as[SpendData].amount.amount mustBe BigDecimal(
        "2206.42"
      )
      (rawJson2022 \ "gov_spending" \ "govSpendAmountData" \ "OverseasAid")
        .as[SpendData]
        .percentage mustBe BigDecimal("0.5")

      (rawJson2023 \ "gov_spending" \ "govSpendAmountData" \ "Health").as[SpendData].amount.amount mustBe BigDecimal(
        "34284.36"
      )
      (rawJson2023 \ "gov_spending" \ "govSpendAmountData" \ "Education").as[SpendData].percentage mustBe BigDecimal(
        "10.2"
      )
      (rawJson2023 \ "gov_spending" \ "govSpendAmountData" \ "BusinessAndIndustry")
        .as[SpendData]
        .amount
        .amount mustBe BigDecimal("7128.43")

      (rawJson2023 \ "gov_spending" \ "govSpendAmountData" \ "Culture").as[SpendData].amount.amount mustBe BigDecimal(
        "2036.69"
      )
      (rawJson2023 \ "gov_spending" \ "govSpendAmountData" \ "OverseasAid")
        .as[SpendData]
        .percentage mustBe BigDecimal("0.7")

    }
  }
  "Calling Government Spend with only capital gains tax to pay"             must {
    "show the correct figures in the government spend screen" in {

      val controllerUnderTest = makeController(capitalGainsOnlyJson)
      val result2022          = Future.successful(controllerUnderTest.getAtsSaData("user", 2023)(request))
      val result2023          = Future.successful(controllerUnderTest.getAtsSaData("user", 2024)(request))

      val rawJsonString2022 = contentAsString(result2022.futureValue)
      val rawJson2022       = Json.parse(rawJsonString2022)

      val rawJsonString2023 = contentAsString(result2023.futureValue)
      val rawJson2023       = Json.parse(rawJsonString2023)

      (rawJson2022 \ "gov_spending" \ "govSpendAmountData" \ "Health").as[SpendData].amount.amount mustBe BigDecimal(
        "35.64"
      )
      (rawJson2022 \ "gov_spending" \ "govSpendAmountData" \ "Education").as[SpendData].percentage mustBe BigDecimal(
        "9.9"
      )
      (rawJson2022 \ "gov_spending" \ "govSpendAmountData" \ "BusinessAndIndustry")
        .as[SpendData]
        .amount
        .amount mustBe BigDecimal("13.68")
      (rawJson2022 \ "gov_spending" \ "govSpendAmountData" \ "Culture").as[SpendData].amount.amount mustBe BigDecimal(
        "2.34"
      )
      (rawJson2022 \ "gov_spending" \ "govSpendAmountData" \ "OverseasAid")
        .as[SpendData]
        .percentage mustBe BigDecimal("0.5")

      (rawJson2023 \ "gov_spending" \ "govSpendAmountData" \ "Health").as[SpendData].amount.amount mustBe BigDecimal(
        "36.36"
      )
      (rawJson2023 \ "gov_spending" \ "govSpendAmountData" \ "Education").as[SpendData].percentage mustBe BigDecimal(
        "10.2"
      )
      (rawJson2023 \ "gov_spending" \ "govSpendAmountData" \ "BusinessAndIndustry")
        .as[SpendData]
        .amount
        .amount mustBe BigDecimal("7.56")
      (rawJson2023 \ "gov_spending" \ "govSpendAmountData" \ "Culture").as[SpendData].amount.amount mustBe BigDecimal(
        "2.16"
      )
      (rawJson2023 \ "gov_spending" \ "govSpendAmountData" \ "OverseasAid")
        .as[SpendData]
        .percentage mustBe BigDecimal("0.7")
    }
  }
  "Calling Government Spend with income, NICs and Capital Gains tax to pay" must {
    "show the correct figures in the government spend screen" in {

      val controllerUnderTest = makeController(allTaxJson)
      val resultPrevYear      = Future.successful(controllerUnderTest.getAtsSaData("user", 2023)(request))
      val resultLatestYear    = Future.successful(controllerUnderTest.getAtsSaData("user", 2024)(request))

      val rawJsonStringPrevYear = contentAsString(resultPrevYear.futureValue)
      val rawJsonPrevYear       = Json.parse(rawJsonStringPrevYear)

      val rawJsonStringLatestYear = contentAsString(resultLatestYear.futureValue)
      val rawJsonLatestYear       = Json.parse(rawJsonStringLatestYear)

      (rawJsonPrevYear \ "gov_spending" \ "govSpendAmountData" \ "Health")
        .as[SpendData]
        .amount
        .amount mustBe BigDecimal(
        "75.54"
      )
      (rawJsonPrevYear \ "gov_spending" \ "govSpendAmountData" \ "BusinessAndIndustry")
        .as[SpendData]
        .amount
        .amount mustBe BigDecimal("28.99")
      (rawJsonPrevYear \ "gov_spending" \ "govSpendAmountData" \ "Education")
        .as[SpendData]
        .percentage mustBe BigDecimal(
        "9.9"
      )
      (rawJsonPrevYear \ "gov_spending" \ "govSpendAmountData" \ "Culture")
        .as[SpendData]
        .amount
        .amount mustBe BigDecimal(
        "4.96"
      )
      (rawJsonPrevYear \ "gov_spending" \ "govSpendAmountData" \ "OverseasAid")
        .as[SpendData]
        .percentage mustBe BigDecimal("0.5")

      (rawJsonLatestYear \ "gov_spending" \ "govSpendAmountData" \ "Health")
        .as[SpendData]
        .amount
        .amount mustBe BigDecimal(
        "77.06"
      )
      (rawJsonLatestYear \ "gov_spending" \ "govSpendAmountData" \ "BusinessAndIndustry")
        .as[SpendData]
        .amount
        .amount mustBe BigDecimal("16.02")
      (rawJsonLatestYear \ "gov_spending" \ "govSpendAmountData" \ "Education")
        .as[SpendData]
        .percentage mustBe BigDecimal(
        "10.2"
      )
      (rawJsonLatestYear \ "gov_spending" \ "govSpendAmountData" \ "Culture")
        .as[SpendData]
        .amount
        .amount mustBe BigDecimal(
        "4.58"
      )
      (rawJsonLatestYear \ "gov_spending" \ "govSpendAmountData" \ "OverseasAid")
        .as[SpendData]
        .percentage mustBe BigDecimal("0.7")
    }
  }
}
