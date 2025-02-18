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

package sa

import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, ok, urlEqualTo}
import common.models.LiabilityKey.*
import common.models.{Amount, GovernmentSpendingOutputWrapper}
import common.services.GoodsAndServices.*
import common.utils.FileHelper
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import sa.models.{AtsMiddleTierData, SpendData}

import scala.concurrent.Future

class AtsDataSpec2020 extends SaTestHelper {

  val taxPayerFile = "sa/taxPayerDetailsWelsh.json"

  trait Test {
    val taxYear: Int

    def odsUrl(taxYear: Int): String = s"/self-assessment/individuals/" + utr + s"/annual-tax-summaries/$taxYear"

    def apiUrl(taxYear: Int): String = s"/taxs/$utr/$taxYear/ats-data"

    def request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, apiUrl(taxYear))
      .withHeaders((AUTHORIZATION, "Bearer 123"))
  }

  "HasSummary (TC4)" must {
    val expected = Map(
      SelfEmploymentIncome               -> 0.0, // LS1a
      IncomeFromEmployment               -> 40511.0, // LS1
      StatePension                       -> 0.0, // LS2
      OtherPensionIncome                 -> 0.0, // LS3
      TaxableStateBenefits               -> 0.0, // LS4
      OtherIncome                        -> 10079.0, // LS5
      BenefitsFromEmployment             -> 0.0, // LS6
      TotalIncomeBeforeTax               -> 50590.0, // LS7
      PersonalTaxFreeAmount              -> 12500.00, // LS8.1
      MarriageAllowanceTransferredAmount -> 0.0, // LS8.2
      OtherAllowancesAmount              -> 0.0, // LS9
      TotalTaxFreeAmount                 -> 12500.0, // LS10
      StartingRateForSavingsAmount       -> 0.0, // LS12.1
      BasicRateIncomeTax                 -> 5818.0, // lS12.2
      HigherRateIncomeTax                -> 0.0, // lS12.3
      AdditionalRateIncomeTax            -> 0.0, // LS12.4
      DividendOrdinaryRate               -> 443.25, // LS13.1
      DividendUpperRate                  -> 191.75, // LS13.2
      AdditionalRate                     -> 0.0, // LS13.3
      // WelshIncomeTax -> 2909.0, //LS20a
      OtherAdjustmentsIncreasing         -> 0.0, // LS15a
      OtherAdjustmentsReducing           -> 0.0, // LS15b
      TotalIncomeTax                     -> 6453.0, // LS20
      TotalIncomeTaxAndNics              -> 10278.48, // LS16
      EmployeeNicAmount                  -> 3825.48, // LS14
      PayCgTaxOn                         -> 0.0, // LS19.8
      TaxableGains                       -> 0.0, // LS19.6
      // ??? -> 12000.0, //LS19.7
      AmountDueAtEntrepreneursRate       -> 0.0, // LS19.1
      AmountDueAtOrdinaryRate            -> 0.0, // LS19.2
      AmountDueRPCIHigherRate            -> 0.0, // LS19.3
      AmountDueRPCILowerRate             -> 0.0, // LS19.3b
      Adjustments                        -> 0.0, // LS19.4
      TotalCgTax                         -> 0.0,
      YourTotalTax                       -> 10278.48 // RS7
    )

    expected foreach { case (key, expectedValue) =>
      s"return the correct key $key" in new Test {
        val taxYear = 2020

        server.stubFor(
          WireMock
            .get(urlEqualTo(odsUrl(taxYear)))
            .willReturn(ok(FileHelper.loadFile("sa/2019-20/TC4.json")))
        )

        val result: AtsMiddleTierData = resultToAtsData(route(app, request))
        checkResult(result, key, expectedValue)
      }
    }
  }

  "HasSummary (1097172561)" must {
    val expected = Map(
      SelfEmploymentIncome               -> 1100.0, // LS1a
      IncomeFromEmployment               -> 10500.0, // LS1
      StatePension                       -> 0.0, // LS2
      OtherPensionIncome                 -> 0.0, // LS3
      TaxableStateBenefits               -> 0.0, // LS4
      OtherIncome                        -> 0.0, // LS5
      BenefitsFromEmployment             -> 0.0, // LS6
      TotalIncomeBeforeTax               -> 11600.0, // LS7
      PersonalTaxFreeAmount              -> 9440.00, // LS8.1
      MarriageAllowanceTransferredAmount -> 0.0, // LS8.2
      OtherAllowancesAmount              -> 300.0, // LS9
      TotalTaxFreeAmount                 -> 9740.0, // LS10
      StartingRateForSavingsAmount       -> 0.0, // LS12.1
      BasicRateIncomeTax                 -> 0.0, // lS12.2
      HigherRateIncomeTax                -> 0.0, // lS12.3
      AdditionalRateIncomeTax            -> 0.0, // LS12.4
      DividendOrdinaryRate               -> 27000.0, // LS13.1
      DividendUpperRate                  -> 0.0, // LS13.2
      AdditionalRate                     -> 0.0, // LS13.3
      // WelshIncomeTax -> 2909.0, //LS20a
      OtherAdjustmentsIncreasing         -> 0.0, // LS15a
      OtherAdjustmentsReducing           -> 0.0, // LS15b
      TotalIncomeTax                     -> 4297.00, // LS20
      TotalIncomeTaxAndNics              -> 4497.00, // LS16
      EmployeeNicAmount                  -> 200.00, // LS14
      PayCgTaxOn                         -> 2000.0, // LS19.8
      TaxableGains                       -> 2100.0, // LS19.6
      // ??? -> 12000.0, //LS19.7
      AmountDueAtEntrepreneursRate       -> 100.0, // LS19.1
      AmountDueAtOrdinaryRate            -> 200.0, // LS19.2
      AmountDueRPCIHigherRate            -> 840.0, // LS19.3
      AmountDueRPCILowerRate             -> 540.0, // LS19.3b
      Adjustments                        -> 0.0, // LS19.4
      TotalCgTax                         -> 1880.0,
      YourTotalTax                       -> 6377.00 // RS7
    )

    expected foreach { case (key, expectedValue) =>
      s"return the correct key $key" in new Test {
        val taxYear = 2020

        server.stubFor(
          WireMock
            .get(urlEqualTo(odsUrl(taxYear)))
            .willReturn(ok(FileHelper.loadFile("sa/2019-20/utr_1097172561.json")))
        )

        val result: AtsMiddleTierData = resultToAtsData(route(app, request))
        checkResult(result, key, expectedValue)
      }
    }

    "return correct government spending" in new Test {
      val expectedValue: GovernmentSpendingOutputWrapper = GovernmentSpendingOutputWrapper(
        2020,
        Map(
          PublicOrderAndSafety     -> SpendData(Amount(274.21, "GBP"), 4.3),
          BusinessAndIndustry      -> SpendData(Amount(242.33, "GBP"), 3.8),
          NationalDebtInterest     -> SpendData(Amount(440.01, "GBP"), 6.9),
          Defence                  -> SpendData(Amount(337.98, "GBP"), 5.3),
          Health                   -> SpendData(Amount(1307.29, "GBP"), 20.5),
          HousingAndUtilities      -> SpendData(Amount(114.79, "GBP"), 1.8),
          GovernmentAdministration -> SpendData(Amount(133.92, "GBP"), 2.1),
          Environment              -> SpendData(Amount(95.66, "GBP"), 1.5),
          OverseasAid              -> SpendData(Amount(70.15, "GBP"), 1.1),
          Culture                  -> SpendData(Amount(95.66, "GBP"), 1.5),
          UkContributionToEuBudget -> SpendData(Amount(51.02, "GBP"), 0.8),
          Transport                -> SpendData(Amount(274.21, "GBP"), 4.3),
          Welfare                  -> SpendData(Amount(1409.32, "GBP"), 22.1),
          Education                -> SpendData(Amount(739.73, "GBP"), 11.6),
          StatePensions            -> SpendData(Amount(790.75, "GBP"), 12.4)
        ),
        Amount(6377, "GBP"),
        None
      )

      val taxYear = 2020

      server.stubFor(
        WireMock
          .get(urlEqualTo(odsUrl(taxYear)))
          .willReturn(ok(FileHelper.loadFile("sa/2019-20/utr_1097172561.json")))
      )

      val result: AtsMiddleTierData = resultToAtsData(route(app, request))

      val actualWrapper: GovernmentSpendingOutputWrapper = result.gov_spending.get
      actualWrapper.taxYear mustBe taxYear
      actualWrapper.govSpendAmountData.foreach { goodsAndService =>
        val expSpendData = expectedValue.govSpendAmountData(goodsAndService._1)
        goodsAndService._2.amount.amount mustBe expSpendData.amount.amount
        goodsAndService._2.amount.currency mustBe expSpendData.amount.currency
      }
    }
  }

  "return NOT_FOUND when ODS returns NOT_FOUND response" in new Test {
    val taxYear = 2021

    server.stubFor(
      WireMock
        .get(urlEqualTo(odsUrl(taxYear)))
        .willReturn(aResponse().withStatus(NOT_FOUND))
    )

    val result: Option[Future[Result]] = route(app, request)
    result.map(status) mustBe Some(NOT_FOUND)
  }

  "return an exception when ODS returns an empty ok" in new Test {
    val taxYear = 2021

    server.stubFor(
      WireMock
        .get(urlEqualTo(odsUrl(taxYear)))
        .willReturn(ok())
    )

    val result: Option[Future[Result]] = route(app, request)
    whenReady(result.get.failed) { e =>
      e mustBe a[MismatchedInputException]
    }
  }

  List(
    IM_A_TEAPOT,
    LOCKED
  ).foreach { httpResponse =>
    s"return an $httpResponse when data is retrieved from ODS" in new Test {
      val taxYear = 2021

      server.stubFor(
        WireMock
          .get(urlEqualTo(odsUrl(taxYear)))
          .willReturn(aResponse().withStatus(httpResponse))
      )

      val result: Option[Future[Result]] = route(app, request)
      result.map(status) mustBe Some(INTERNAL_SERVER_ERROR)
    }
  }

  List(
    INTERNAL_SERVER_ERROR,
    BAD_GATEWAY,
    SERVICE_UNAVAILABLE
  ).foreach { httpResponse =>
    s"return an 502 when $httpResponse status is received from ODS" in new Test {
      val taxYear = 2021

      server.stubFor(
        WireMock
          .get(urlEqualTo(odsUrl(taxYear)))
          .willReturn(aResponse().withStatus(httpResponse))
      )

      val result: Option[Future[Result]] = route(app, request)
      result.map(status) mustBe Some(BAD_GATEWAY)
    }
  }
}
