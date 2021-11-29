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

package sa

import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, ok, urlEqualTo}
import models.LiabilityKey._
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.FileHelper

class AtsDataSpec2021 extends SaTestHelper {

  val taxPayerFile = "taxPayerDetails.json"

  def odsUrl(taxYear: Int) = s"/self-assessment/individuals/" + utr + s"/annual-tax-summaries/$taxYear"

  def apiUrl(taxYear: Int) = s"/taxs/$utr/$taxYear/ats-data"

  "HasSummary" must {

    "return an OK when data for 2021 is retrieved from ODS (TC256)" ignore {

      val taxYear = 2021
      server.stubFor(
        WireMock.get(urlEqualTo(odsUrl(taxYear)))
          .willReturn(ok(FileHelper.loadFile("2020-21/TC256.json")))
      )
      val request = FakeRequest(GET, apiUrl(taxYear))
      val result = resultToAtsData(route(app, request))

      checkResult(result, Map(
        SelfEmploymentIncome -> 0.0, // LS1a
        IncomeFromEmployment -> 0.0, // LS1
        StatePension -> 0.0, //LS2
        OtherPensionIncome -> 62732.0, //LS3
        TaxableStateBenefits -> 0.0, //LS4
        OtherIncome -> 1057.0, //LS5
        BenefitsFromEmployment -> 0.0, //LS6
        TotalIncomeBeforeTax -> 63789.0, //LS7
        PersonalTaxFreeAmount -> 12500.00, //LS8.1
        MarriageAllowanceTransferredAmount -> 0.0, //LS8.2
        OtherAllowancesAmount -> 0.0, //LS9
        TotalTaxFreeAmount -> 12500.0, //LS10
        StartingRateForSavingsAmount -> 0.0, //LS12.1
        BasicRateIncomeTax -> 7500.0, //lS12.2
        HigherRateIncomeTax -> 5092.0, //LS12.3
        AdditionalRateIncomeTax -> 0.0, //LS12.4
        OrdinaryRate -> -1, //LS13.1
        UpperRate -> -1, //LS13.2
        AdditionalRate -> -1, //LS13.3
        //TODO: Should these all be left as -1
        OtherAdjustmentsIncreasing -> -1,
        OtherAdjustmentsReducing -> -1,
        WelshIncomeTax -> -1, //LS20a
        TotalIncomeTax -> -1, //LS20
        TotalIncomeTaxAndNics -> -1, //LS16
        EmployeeNicAmount -> -1, //LS14
        IncomeAfterTaxAndNics -> -1, //LS17, RS5
        EmployerNicAmount -> -1, //LS18
        PayCgTaxOn -> -1, //LS19.8
        TaxableGains -> -1, //LS19.6
        TotalTaxFreeAmount -> -1, //LS19.7
        AmountDueAtEntrepreneursRate -> -1, //LS19.1
        AmountDueAtOrdinaryRate -> -1, //LS19.2
        AmountDueRPCIHigherRate -> -1, //LS19.3
        AmountDueRPCILowerRate -> -1, //LS19.3b
        Adjustments -> -1, //LS19.4
        TotalCgTax -> -1,
        YourTotalTax -> -1 //RS7
      ))
    }

    "return an OK when data for 2021 is retrieved from ODS (TC13)" ignore {

      val taxYear = 2021

      server.stubFor(
        WireMock.get(urlEqualTo(odsUrl(taxYear)))
          .willReturn(ok(FileHelper.loadFile("2020-21/TC13.json")))
      )

      val request = FakeRequest(GET, apiUrl(taxYear))
      val result = resultToAtsData(route(app, request))

      checkResult(result, Map(
        SelfEmploymentIncome -> 21055.0, // LS1a
        IncomeFromEmployment -> 48484.0, // LS1
        StatePension -> 8609.0, //LS2
        OtherPensionIncome -> 79811.0, //LS3
        TaxableStateBenefits -> 0.0, //LS4
        OtherIncome -> 4338.0, //LS5
        BenefitsFromEmployment -> 0.0, //LS6
        TotalIncomeBeforeTax -> 162297.0, //LS7
        PersonalTaxFreeAmount -> 0.00, //LS8.1
        MarriageAllowanceTransferredAmount -> 0.0, //LS8.2
        OtherAllowancesAmount -> 6424.0, //LS9
        TotalTaxFreeAmount -> 6424.0, //LS10
        StartingRateForSavingsAmount -> 0.0, //LS12.1
        BasicRateIncomeTax -> 8074.8, //lS12.2
        HigherRateIncomeTax -> 45000.0, //LS12.3
        AdditionalRateIncomeTax -> 268.20, //LS12.4
        OrdinaryRate -> -1, //LS13.1
        UpperRate -> -1, //LS13.2
        AdditionalRate -> -1, //LS13.3
        //TODO: Should these all be left as -1
        OtherAdjustmentsIncreasing -> -1,
        OtherAdjustmentsReducing -> -1,
        WelshIncomeTax -> -1, //LS20a
        TotalIncomeTax -> -1, //LS20
        TotalIncomeTaxAndNics -> -1, //LS16
        EmployeeNicAmount -> -1, //LS14
        IncomeAfterTaxAndNics -> -1, //LS17, RS5
        EmployerNicAmount -> -1, //LS18
        PayCgTaxOn -> -1, //LS19.8
        TaxableGains -> -1, //LS19.6
        TotalTaxFreeAmount -> -1, //LS19.7
        AmountDueAtEntrepreneursRate -> -1, //LS19.1
        AmountDueAtOrdinaryRate -> -1, //LS19.2
        AmountDueRPCIHigherRate -> -1, //LS19.3
        AmountDueRPCILowerRate -> -1, //LS19.3b
        Adjustments -> -1, //LS19.4
        TotalCgTax -> -1,
        YourTotalTax -> -1 //RS7
      ))
    }

    "return NOT_FOUND when ODS returns NOT_FOUND response" in {

      val taxYear = 2021

      server.stubFor(
        WireMock.get(urlEqualTo(odsUrl(taxYear)))
          .willReturn(aResponse().withStatus(NOT_FOUND))
      )

      val request = FakeRequest(GET, apiUrl(taxYear))

      val result = route(app, request)

      result.map(status) mustBe Some(NOT_FOUND)
    }

    "return an exception when ODS returns an empty ok" in {

      val taxYear = 2021

      server.stubFor(
        WireMock.get(urlEqualTo(odsUrl(taxYear)))
          .willReturn(ok())
      )

      val request = FakeRequest(GET, apiUrl(taxYear))

      val result = route(app, request)

      whenReady(result.get.failed) { e =>
        e mustBe a[MismatchedInputException]
      }
    }

    List(
      IM_A_TEAPOT,
      LOCKED
    ).foreach { httpResponse =>
      s"return an $httpResponse when data is retrieved from ODS" in {
        val taxYear = 2021

        server.stubFor(
          WireMock.get(urlEqualTo(odsUrl(taxYear)))
            .willReturn(aResponse().withStatus(httpResponse))
        )

        val request = FakeRequest(GET, apiUrl(taxYear))

        val result = route(app, request)

        result.map(status) mustBe Some(INTERNAL_SERVER_ERROR)
      }
    }

    List(
      INTERNAL_SERVER_ERROR,
      BAD_GATEWAY,
      SERVICE_UNAVAILABLE
    ).foreach { httpResponse =>
      s"return an 502 when $httpResponse status is received from ODS" in {

        val taxYear = 2021

        server.stubFor(
          WireMock.get(urlEqualTo(odsUrl(taxYear)))
            .willReturn(aResponse().withStatus(httpResponse))
        )

        val request = FakeRequest(GET, apiUrl(taxYear))

        val result = route(app, request)

        result.map(status) mustBe Some(BAD_GATEWAY)
      }
    }
  }
}
