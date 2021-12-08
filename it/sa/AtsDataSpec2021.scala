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

  "HasSummary (SIT001)" must {

    val taxYear = 2021
    val expected = Map(
      SelfEmploymentIncome -> 0.0, // LS1a
      IncomeFromEmployment -> 0.0, // LS1
      StatePension -> 14206.0, //LS2
      OtherPensionIncome -> 5300.0, //LS3
      TaxableStateBenefits -> 0.0, //LS4
      OtherIncome -> 3358.0, //LS5
      BenefitsFromEmployment -> 0.0, //LS6
      TotalIncomeBeforeTax -> 22864.0, //LS7
      PersonalTaxFreeAmount -> 0.00, //LS8.1
      MarriageAllowanceTransferredAmount -> 0.0, //LS8.2
      OtherAllowancesAmount -> 17564.0, //LS9
      TotalTaxFreeAmount -> 12500.0, //LS10
      StartingRateForSavingsAmount -> 0.0, //LS12.1
      BasicRateIncomeTax -> 20532.0, //lS12.2
      HigherRateIncomeTax -> 0.0, //LS12.3
      AdditionalRateIncomeTax -> 0.0, //LS12.4
      OrdinaryRate -> 0.0, //LS13.1
      UpperRate -> 0.0, //LS13.2
      AdditionalRate -> 0.0, //LS13.3
      OtherAdjustmentsIncreasing -> 0.0,
      OtherAdjustmentsReducing -> 0.0,
      WelshIncomeTax -> 0.0, //LS20a
      TotalIncomeTax -> 4106.4, //LS20
      TotalIncomeTaxAndNics -> 1281.45, //LS16
      EmployeeNicAmount -> 131.60, //LS14
      PayCgTaxOn -> 0.0, //LS19.8
      TaxableGains -> 0.0, //LS19.6
      TotalTaxFreeAmount -> 17564.0, //LS19.7
      AmountDueAtEntrepreneursRate -> 0.0, //LS19.1
      AmountDueAtOrdinaryRate -> 0.0, //LS19.2
      AmountDueRPCIHigherRate -> 0.0, //LS19.3
      AmountDueRPCILowerRate -> 0.0, //LS19.3b
      Adjustments -> 0.0, //LS19.4
      TotalCgTax -> 0.0,
      YourTotalTax -> 1281.45 //RS7
    )

    expected foreach { case (key, expectedValue) =>
      s"return the correct key $key" in {
        server.stubFor(
          WireMock.get(urlEqualTo(odsUrl(taxYear)))
            .willReturn(ok(FileHelper.loadFile("2020-21/utr_6602556503.json")))
        )
        val request = FakeRequest(GET, apiUrl(taxYear))
        val result = resultToAtsData(route(app, request))

        checkResult(result, key, expectedValue)
      }
    }
  }

  "HasSummary (SIT002)" must {

    val taxYear = 2021
    val expected = Map(
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
      OtherAllowancesAmount -> 1057.0, //LS9
      TotalTaxFreeAmount -> 13557.0, //LS10
      StartingRateForSavingsAmount -> 0.0, //LS12.1
      BasicRateIncomeTax -> 37500.0, //lS12.2
      HigherRateIncomeTax -> 12732.0, //LS12.3
      AdditionalRateIncomeTax -> 0.0, //LS12.4
      OrdinaryRate -> 0.0, //LS13.1
      UpperRate -> 0.0, //LS13.2
      AdditionalRate -> 0.0, //LS13.3
      OtherAdjustmentsIncreasing -> 0.0,
      OtherAdjustmentsReducing -> 0.0,
      WelshIncomeTax -> 0.0, //LS20a
      TotalIncomeTax -> 12592.8, //LS20
      TotalIncomeTaxAndNics -> 12724.4, //LS16
      EmployeeNicAmount -> 131.60, //LS14
      PayCgTaxOn -> 8675.0, //LS19.8
      TaxableGains -> 20975.0, //LS19.6
      TotalTaxFreeAmount -> 13557.0, //LS19.7
      AmountDueAtEntrepreneursRate -> 0.0, //LS19.1
      AmountDueAtOrdinaryRate -> 0.0, //LS19.2
      AmountDueRPCIHigherRate -> 9077.04, //LS19.3
      AmountDueRPCILowerRate -> 0.0, //LS19.3b
      Adjustments -> 0.0, //LS19.4
      TotalCgTax -> 13272.04,
      YourTotalTax -> 25996.44 //RS7
    )

    expected foreach { case (key, expectedValue) =>
      s"return the correct key $key" in {
        server.stubFor(
          WireMock.get(urlEqualTo(odsUrl(taxYear)))
            .willReturn(ok(FileHelper.loadFile("2020-21/utr_2752692244.json")))
        )
        val request = FakeRequest(GET, apiUrl(taxYear))
        val result = resultToAtsData(route(app, request))

        checkResult(result, key, expectedValue)
      }
    }
  }

  //Regression test for 2021 spec
  "HasSummary (SIT013)" must {

    val taxYear = 2020
    val expected = Map(
      SelfEmploymentIncome -> 0.0, // LS1a
      IncomeFromEmployment -> 0.0, // LS1
      StatePension -> 0.0, //LS2
      OtherPensionIncome -> 0.0, //LS3
      TaxableStateBenefits -> 0.0, //LS4
      OtherIncome -> 7005.0, //LS5
      BenefitsFromEmployment -> 0.0, //LS6
      TotalIncomeBeforeTax -> 7005.0, //LS7
      PersonalTaxFreeAmount -> 12500.00, //LS8.1
      MarriageAllowanceTransferredAmount -> 0.0, //LS8.2
      OtherAllowancesAmount -> 0.0, //LS9
      TotalTaxFreeAmount -> 12500.0, //LS10
      StartingRateForSavingsAmount -> 0.0, //LS12.1
      BasicRateIncomeTax -> 0.0, //lS12.2
      HigherRateIncomeTax -> 0.0, //LS12.3
      AdditionalRateIncomeTax -> 0.0, //LS12.4
      OrdinaryRate -> 0.0, //LS13.1
      UpperRate -> 0.0, //LS13.2
      AdditionalRate -> 0.0, //LS13.3
      OtherAdjustmentsIncreasing -> 450.0,
      OtherAdjustmentsReducing -> 0.0,
      WelshIncomeTax -> 0.0, //LS20a
      TotalIncomeTax -> 450.0, //LS20 INCOME_TAX_DUE?
      TotalIncomeTaxAndNics -> 581.60, //LS16
      EmployeeNicAmount -> 131.60, //LS14
      PayCgTaxOn -> 0.0, //LS19.8
      TaxableGains -> 0.0, //LS19.6
      TotalTaxFreeAmount -> 12500.0, //LS19.7
      AmountDueAtEntrepreneursRate -> 0.0, //LS19.1
      AmountDueAtOrdinaryRate -> 0.0, //LS19.2
      AmountDueRPCIHigherRate -> 0.0, //LS19.3
      AmountDueRPCILowerRate -> 0.0, //LS19.3b
      Adjustments -> 0.0, //LS19.4
      TotalCgTax -> 0.0,
      YourTotalTax -> 581.60 //RS7
    )

    expected foreach { case (key, expectedValue) =>
      s"return the correct key $key" in {
        server.stubFor(
          WireMock.get(urlEqualTo(odsUrl(taxYear)))
            .willReturn(ok(FileHelper.loadFile("2020-21/utr_9784036411.json")))
        )
        val request = FakeRequest(GET, apiUrl(taxYear))
        val result = resultToAtsData(route(app, request))

        checkResult(result, key, expectedValue)
      }
    }
  }

    "HasSummary (SIT003)" must {

      val taxYear = 2021
      val expected = Map(
        SelfEmploymentIncome -> 21055.0, // LS1a
        IncomeFromEmployment -> 48484.0, // LS1
        StatePension -> 8609.0, //LS2
        OtherPensionIncome ->  79811.0, //LS3
        TaxableStateBenefits -> 0.0, //LS4
        OtherIncome -> 4338.0, //LS5
        BenefitsFromEmployment -> 0.0, //LS6
        TotalIncomeBeforeTax -> 162297.0, //LS7
        PersonalTaxFreeAmount -> 0.0, //LS8.1
        MarriageAllowanceTransferredAmount -> 0.0, //LS8.2
        OtherAllowancesAmount -> 6424.0, //LS9
        TotalTaxFreeAmount -> 6424.0, //LS10
        StartingRateForSavingsAmount -> 0.0, //LS12.1
        BasicRateIncomeTax -> 40374.0, //lS12.2
        HigherRateIncomeTax -> 112500.0, //LS12.3
        AdditionalRateIncomeTax -> 596.0, //LS12.4
        OrdinaryRate -> 0.0, //LS13.1
        UpperRate -> 0.0, //LS13.2
        AdditionalRate -> 403.0, //LS13.3
        OtherAdjustmentsIncreasing -> 0.0,
        OtherAdjustmentsReducing -> 234.0,
        WelshIncomeTax -> 0.0, //LS20a
        TotalIncomeTax -> 53262.54, //LS20
        TotalIncomeTaxAndNics -> 53394.14, //LS16
        EmployeeNicAmount -> 131.60, //LS14
        PayCgTaxOn -> 0.0, //LS19.8
        TaxableGains -> 0.0, //LS19.6
        AmountDueAtEntrepreneursRate -> 0.0, //LS19.1
        AmountDueAtOrdinaryRate -> 0.0, //LS19.2
        AmountDueRPCIHigherRate -> 0.0, //LS19.3
        AmountDueRPCILowerRate -> 0.0, //LS19.3b
        Adjustments -> 0.0, //LS19.4
        TotalCgTax -> 0.0,
        YourTotalTax -> 53394.14 //RS7
      )

      expected foreach { case (key, expectedValue) =>
        s"return the correct key $key" in {
          server.stubFor(
            WireMock.get(urlEqualTo(odsUrl(taxYear)))
              .willReturn(ok(FileHelper.loadFile("2020-21/utr_2216360398.json")))
          )
          val request = FakeRequest(GET, apiUrl(taxYear))
          val result = resultToAtsData(route(app, request))

          checkResult(result, key, expectedValue)
        }
      }
    }

    "HasSummary (SIT004)" must {

      val taxYear = 2021
      val expected = Map(
        SelfEmploymentIncome -> 0.0, // LS1a
        IncomeFromEmployment -> 49650.0, // LS1
        StatePension -> 0.0, //LS2
        OtherPensionIncome ->  0.0, //LS3
        TaxableStateBenefits -> 0.0, //LS4
        OtherIncome -> 10079.0, //LS5
        BenefitsFromEmployment -> 0.0, //LS6
        TotalIncomeBeforeTax -> 59729.0, //LS7
        PersonalTaxFreeAmount -> 12500.0, //LS8.1
        MarriageAllowanceTransferredAmount -> 0.0, //LS8.2
        OtherAllowancesAmount -> 0.0, //LS9
        TotalTaxFreeAmount -> 12500.0, //LS10
        StartingRateForSavingsAmount -> 0.0, //LS12.1
        BasicRateIncomeTax -> 37500.0, //lS12.2
        HigherRateIncomeTax -> 729.0, //LS12.3
        AdditionalRateIncomeTax -> 0.0, //LS12.4
        OrdinaryRate -> 0.0, //LS13.1
        UpperRate -> 6500.0, //LS13.2
        AdditionalRate -> 0.0, //LS13.3
        OtherAdjustmentsIncreasing -> 0.0,
        OtherAdjustmentsReducing -> 0.0,
        WelshIncomeTax -> 3750.0, //LS20a
        TotalIncomeTax -> 9904.1, //LS20
        TotalIncomeTaxAndNics -> 10035.7, //LS16
        EmployeeNicAmount -> 131.60, //LS14
        PayCgTaxOn -> 0.0, //LS19.8
        TaxableGains -> 0.0, //LS19.6
        AmountDueAtEntrepreneursRate -> 0.0, //LS19.1
        AmountDueAtOrdinaryRate -> 0.0, //LS19.2
        AmountDueRPCIHigherRate -> 0.0, //LS19.3
        AmountDueRPCILowerRate -> 0.0, //LS19.3b
        Adjustments -> 0.0, //LS19.4
        TotalCgTax -> 0.0,
        YourTotalTax -> 10035.7 //RS7
      )

      expected foreach { case (key, expectedValue) =>
        s"return the correct key $key" in {
          server.stubFor(
            WireMock.get(urlEqualTo(odsUrl(taxYear)))
              .willReturn(ok(FileHelper.loadFile("2020-21/utr_8673565454.json")))
          )
          val request = FakeRequest(GET, apiUrl(taxYear))
          val result = resultToAtsData(route(app, request))

          checkResult(result, key, expectedValue)
        }
      }
    }

  "HasSummary (SIT005)" must {

    val taxYear = 2021
    val expected = Map(
      SelfEmploymentIncome -> 0.0, // LS1a
      IncomeFromEmployment -> 50000.0, // LS1
      StatePension -> 0.0, //LS2
      OtherPensionIncome ->  0.0, //LS3
      TaxableStateBenefits -> 0.0, //LS4
      OtherIncome -> 160126.0, //LS5
      BenefitsFromEmployment -> 0.0, //LS6
      TotalIncomeBeforeTax -> 210126.0, //LS7
      PersonalTaxFreeAmount -> 12500.0, //LS8.1
      MarriageAllowanceTransferredAmount -> 0.0, //LS8.2
      OtherAllowancesAmount -> 0.0, //LS9
      TotalTaxFreeAmount -> 12500.0, //LS10
      StartingRateForSavingsAmount -> 0.0, //LS12.1
      BasicRateIncomeTax -> 37500.0, //lS12.2
      HigherRateIncomeTax -> 0.0, //LS12.3
      AdditionalRateIncomeTax -> 0.0, //LS12.4
      OrdinaryRate -> 147874.0, //LS13.1
      UpperRate -> 10126.0, //LS13.2
      AdditionalRate -> 0.0, //LS13.3
      OtherAdjustmentsIncreasing -> 12094.5,
      OtherAdjustmentsReducing -> 0.0,
      WelshIncomeTax -> 3750.0, //LS20a
      TotalIncomeTax -> 31988.0, //LS20
      TotalIncomeTaxAndNics -> 34107.6, //LS16
      EmployeeNicAmount -> 131.60, //LS14
      PayCgTaxOn -> 0.0, //LS19.8
      TaxableGains -> 0.0, //LS19.6
      AmountDueAtEntrepreneursRate -> 0.0, //LS19.1
      AmountDueAtOrdinaryRate -> 0.0, //LS19.2
      AmountDueRPCIHigherRate -> 0.0, //LS19.3
      AmountDueRPCILowerRate -> 0.0, //LS19.3b
      Adjustments -> 0.0, //LS19.4
      TotalCgTax -> 0.0,
      YourTotalTax -> 34107.6 //RS7
    )

    expected foreach { case (key, expectedValue) =>
      s"return the correct key $key" in {
        server.stubFor(
          WireMock.get(urlEqualTo(odsUrl(taxYear)))
            .willReturn(ok(FileHelper.loadFile("2020-21/utr_6309169120.json")))
        )
        val request = FakeRequest(GET, apiUrl(taxYear))
        val result = resultToAtsData(route(app, request))

        checkResult(result, key, expectedValue)
      }
    }
  }

  "HasSummary (SIT006)" must {

    val taxYear = 2021
    val expected = Map(
      SelfEmploymentIncome -> 0.0, // LS1a
      IncomeFromEmployment -> 50000.0, // LS1
      StatePension -> 0.0, //LS2
      OtherPensionIncome ->  0.0, //LS3
      TaxableStateBenefits -> 0.0, //LS4
      OtherIncome -> 160126.0, //LS5
      BenefitsFromEmployment -> 0.0, //LS6
      TotalIncomeBeforeTax -> 210126.0, //LS7
      PersonalTaxFreeAmount -> 12500.0, //LS8.1
      MarriageAllowanceTransferredAmount -> 0.0, //LS8.2
      OtherAllowancesAmount -> 0.0, //LS9
      TotalTaxFreeAmount -> 12500.0, //LS10
      StartingRateForSavingsAmount -> 0.0, //LS12.1
      BasicRateIncomeTax -> 37500.0, //lS12.2
      HigherRateIncomeTax -> 0.0, //LS12.3
      AdditionalRateIncomeTax -> 0.0, //LS12.4
      OrdinaryRate -> 147874.0, //LS13.1
      UpperRate -> 10126.0, //LS13.2
      AdditionalRate -> 0.0, //LS13.3
      OtherAdjustmentsIncreasing -> 12094.5,
      OtherAdjustmentsReducing -> 0.0,
      WelshIncomeTax -> 3750.0, //LS20a
      TotalIncomeTax -> 7328.37, //LS20
      TotalIncomeTaxAndNics -> 34107.6, //LS16
      EmployeeNicAmount -> 131.60, //LS14
      PayCgTaxOn -> 0.0, //LS19.8
      TaxableGains -> 0.0, //LS19.6
      AmountDueAtEntrepreneursRate -> 0.0, //LS19.1
      AmountDueAtOrdinaryRate -> 0.0, //LS19.2
      AmountDueRPCIHigherRate -> 0.0, //LS19.3
      AmountDueRPCILowerRate -> 0.0, //LS19.3b
      Adjustments -> 0.0, //LS19.4
      TotalCgTax -> 0.0,
      YourTotalTax -> 34107.6 //RS7
    )

    expected foreach { case (key, expectedValue) =>
      s"return the correct key $key" in {
        server.stubFor(
          WireMock.get(urlEqualTo(odsUrl(taxYear)))
            .willReturn(ok(FileHelper.loadFile("2020-21/utr_7362435273.json")))
        )
        val request = FakeRequest(GET, apiUrl(taxYear))
        val result = resultToAtsData(route(app, request))

        checkResult(result, key, expectedValue)
      }
    }
  }

  "HasSummary (SIT007)" must {

    val taxYear = 2021
    val expected = Map(
      SelfEmploymentIncome -> 0.0, // LS1a
      IncomeFromEmployment -> 50000.0, // LS1
      StatePension -> 0.0, //LS2
      OtherPensionIncome ->  0.0, //LS3
      TaxableStateBenefits -> 0.0, //LS4
      OtherIncome -> 160126.0, //LS5
      BenefitsFromEmployment -> 0.0, //LS6
      TotalIncomeBeforeTax -> 210126.0, //LS7
      PersonalTaxFreeAmount -> 12500.0, //LS8.1
      MarriageAllowanceTransferredAmount -> 0.0, //LS8.2
      OtherAllowancesAmount -> 0.0, //LS9
      TotalTaxFreeAmount -> 12500.0, //LS10
      StartingRateForSavingsAmount -> 0.0, //LS12.1
      BasicRateIncomeTax -> 37500.0, //lS12.2
      HigherRateIncomeTax -> 0.0, //LS12.3
      AdditionalRateIncomeTax -> 0.0, //LS12.4
      OrdinaryRate -> 147874.0, //LS13.1
      UpperRate -> 10126.0, //LS13.2
      AdditionalRate -> 0.0, //LS13.3
      OtherAdjustmentsIncreasing -> 12094.5,
      OtherAdjustmentsReducing -> 0.0,
      WelshIncomeTax -> 3750.0, //LS20a
      TotalIncomeTax -> 4083.32, //LS20
      TotalIncomeTaxAndNics -> 34107.6, //LS16
      EmployeeNicAmount -> 131.60, //LS14
      PayCgTaxOn -> 0.0, //LS19.8
      TaxableGains -> 0.0, //LS19.6
      AmountDueAtEntrepreneursRate -> 0.0, //LS19.1
      AmountDueAtOrdinaryRate -> 0.0, //LS19.2
      AmountDueRPCIHigherRate -> 0.0, //LS19.3
      AmountDueRPCILowerRate -> 0.0, //LS19.3b
      Adjustments -> 0.0, //LS19.4
      TotalCgTax -> 0.0,
      YourTotalTax -> 34107.6 //RS7
    )

    expected foreach { case (key, expectedValue) =>
      s"return the correct key $key" in {
        server.stubFor(
          WireMock.get(urlEqualTo(odsUrl(taxYear)))
            .willReturn(ok(FileHelper.loadFile("2020-21/utr_6721445140.json")))
        )
        val request = FakeRequest(GET, apiUrl(taxYear))
        val result = resultToAtsData(route(app, request))

        checkResult(result, key, expectedValue)
      }
    }
  }

  "HasSummary (SIT008)" must {

    val taxYear = 2021
    val expected = Map(
      SelfEmploymentIncome -> 0.0, // LS1a
      IncomeFromEmployment -> 50000.0, // LS1
      StatePension -> 0.0, //LS2
      OtherPensionIncome ->  0.0, //LS3
      TaxableStateBenefits -> 0.0, //LS4
      OtherIncome -> 160126.0, //LS5
      BenefitsFromEmployment -> 0.0, //LS6
      TotalIncomeBeforeTax -> 210126.0, //LS7
      PersonalTaxFreeAmount -> 12500.0, //LS8.1
      MarriageAllowanceTransferredAmount -> 0.0, //LS8.2
      OtherAllowancesAmount -> 0.0, //LS9
      TotalTaxFreeAmount -> 12500.0, //LS10
      StartingRateForSavingsAmount -> 0.0, //LS12.1
      BasicRateIncomeTax -> 37500.0, //lS12.2
      HigherRateIncomeTax -> 0.0, //LS12.3
      AdditionalRateIncomeTax -> 0.0, //LS12.4
      OrdinaryRate -> 147874.0, //LS13.1
      UpperRate -> 10126.0, //LS13.2
      AdditionalRate -> 0.0, //LS13.3
      OtherAdjustmentsIncreasing -> 12094.5,
      OtherAdjustmentsReducing -> 0.0,
      WelshIncomeTax -> 3750.0, //LS20a
      TotalIncomeTax -> 11001.87, //LS20
      TotalIncomeTaxAndNics -> 34107.6, //LS16
      EmployeeNicAmount -> 131.60, //LS14
      PayCgTaxOn -> 0.0, //LS19.8
      TaxableGains -> 0.0, //LS19.6
      AmountDueAtEntrepreneursRate -> 0.0, //LS19.1
      AmountDueAtOrdinaryRate -> 0.0, //LS19.2
      AmountDueRPCIHigherRate -> 0.0, //LS19.3
      AmountDueRPCILowerRate -> 0.0, //LS19.3b
      Adjustments -> 0.0, //LS19.4
      TotalCgTax -> 0.0,
      YourTotalTax -> 34107.6 //RS7
    )

    expected foreach { case (key, expectedValue) =>
      s"return the correct key $key" in {
        server.stubFor(
          WireMock.get(urlEqualTo(odsUrl(taxYear)))
            .willReturn(ok(FileHelper.loadFile("2020-21/utr_2839798608.json")))
        )
        val request = FakeRequest(GET, apiUrl(taxYear))
        val result = resultToAtsData(route(app, request))

        checkResult(result, key, expectedValue)
      }
    }
  }

  "HasSummary (SIT009)" must {

    val taxYear = 2021
    val expected = Map(
      SelfEmploymentIncome -> 0.0, // LS1a
      IncomeFromEmployment -> 50000.0, // LS1
      StatePension -> 0.0, //LS2
      OtherPensionIncome ->  0.0, //LS3
      TaxableStateBenefits -> 0.0, //LS4
      OtherIncome -> 160126.0, //LS5
      BenefitsFromEmployment -> 0.0, //LS6
      TotalIncomeBeforeTax -> 210126.0, //LS7
      PersonalTaxFreeAmount -> 12500.0, //LS8.1
      MarriageAllowanceTransferredAmount -> 0.0, //LS8.2
      OtherAllowancesAmount -> 0.0, //LS9
      TotalTaxFreeAmount -> 12500.0, //LS10
      StartingRateForSavingsAmount -> 0.0, //LS12.1
      BasicRateIncomeTax -> 37500.0, //lS12.2
      HigherRateIncomeTax -> 0.0, //LS12.3
      AdditionalRateIncomeTax -> 0.0, //LS12.4
      OrdinaryRate -> 147874.0, //LS13.1
      UpperRate -> 10126.0, //LS13.2
      AdditionalRate -> 0.0, //LS13.3
      OtherAdjustmentsIncreasing -> 12094.5,
      OtherAdjustmentsReducing -> 0.0,
      WelshIncomeTax -> 3750.0, //LS20a
      TotalIncomeTax -> 344009.02, //LS20
      TotalIncomeTaxAndNics -> 34107.6, //LS16
      EmployeeNicAmount -> 131.60, //LS14
      PayCgTaxOn -> 0.0, //LS19.8
      TaxableGains -> 0.0, //LS19.6
      AmountDueAtEntrepreneursRate -> 0.0, //LS19.1
      AmountDueAtOrdinaryRate -> 0.0, //LS19.2
      AmountDueRPCIHigherRate -> 0.0, //LS19.3
      AmountDueRPCILowerRate -> 0.0, //LS19.3b
      Adjustments -> 0.0, //LS19.4
      TotalCgTax -> 0.0,
      YourTotalTax -> 34107.6 //RS7
    )

    expected foreach { case (key, expectedValue) =>
      s"return the correct key $key" in {
        server.stubFor(
          WireMock.get(urlEqualTo(odsUrl(taxYear)))
            .willReturn(ok(FileHelper.loadFile("2020-21/utr_3902670233.json")))
        )
        val request = FakeRequest(GET, apiUrl(taxYear))
        val result = resultToAtsData(route(app, request))

        checkResult(result, key, expectedValue)
      }
    }
  }

  "HasSummary (SIT010)" must {

    val taxYear = 2021
    val expected = Map(
      SelfEmploymentIncome -> 0.0, // LS1a
      IncomeFromEmployment -> 50000.0, // LS1
      StatePension -> 0.0, //LS2
      OtherPensionIncome ->  0.0, //LS3
      TaxableStateBenefits -> 0.0, //LS4
      OtherIncome -> 160126.0, //LS5
      BenefitsFromEmployment -> 0.0, //LS6
      TotalIncomeBeforeTax -> 210126.0, //LS7
      PersonalTaxFreeAmount -> 12500.0, //LS8.1
      MarriageAllowanceTransferredAmount -> 0.0, //LS8.2
      OtherAllowancesAmount -> 0.0, //LS9
      TotalTaxFreeAmount -> 12500.0, //LS10
      StartingRateForSavingsAmount -> 0.0, //LS12.1
      BasicRateIncomeTax -> 37500.0, //lS12.2
      HigherRateIncomeTax -> 0.0, //LS12.3
      AdditionalRateIncomeTax -> 0.0, //LS12.4
      OrdinaryRate -> 147874.0, //LS13.1
      UpperRate -> 10126.0, //LS13.2
      AdditionalRate -> 0.0, //LS13.3
      OtherAdjustmentsIncreasing -> 12094.5,
      OtherAdjustmentsReducing -> 0.0,
      WelshIncomeTax -> 3750.0, //LS20a
      TotalIncomeTax -> 692.0, //LS20
      TotalIncomeTaxAndNics -> 34107.6, //LS16
      EmployeeNicAmount -> 131.60, //LS14
      PayCgTaxOn -> 0.0, //LS19.8
      TaxableGains -> 0.0, //LS19.6
      AmountDueAtEntrepreneursRate -> 0.0, //LS19.1
      AmountDueAtOrdinaryRate -> 0.0, //LS19.2
      AmountDueRPCIHigherRate -> 0.0, //LS19.3
      AmountDueRPCILowerRate -> 0.0, //LS19.3b
      Adjustments -> 0.0, //LS19.4
      TotalCgTax -> 0.0,
      YourTotalTax -> 34107.6 //RS7
    )

    expected foreach { case (key, expectedValue) =>
      s"return the correct key $key" in {
        server.stubFor(
          WireMock.get(urlEqualTo(odsUrl(taxYear)))
            .willReturn(ok(FileHelper.loadFile("2020-21/utr_9716771495.json")))
        )
        val request = FakeRequest(GET, apiUrl(taxYear))
        val result = resultToAtsData(route(app, request))

        checkResult(result, key, expectedValue)
      }
    }
  }

  "HasSummary (SIT011)" must {

    val taxYear = 2021
    val expected = Map(
      SelfEmploymentIncome -> 0.0, // LS1a
      IncomeFromEmployment -> 50000.0, // LS1
      StatePension -> 0.0, //LS2
      OtherPensionIncome ->  0.0, //LS3
      TaxableStateBenefits -> 0.0, //LS4
      OtherIncome -> 160126.0, //LS5
      BenefitsFromEmployment -> 0.0, //LS6
      TotalIncomeBeforeTax -> 210126.0, //LS7
      PersonalTaxFreeAmount -> 12500.0, //LS8.1
      MarriageAllowanceTransferredAmount -> 0.0, //LS8.2
      OtherAllowancesAmount -> 0.0, //LS9
      TotalTaxFreeAmount -> 12500.0, //LS10
      StartingRateForSavingsAmount -> 0.0, //LS12.1
      BasicRateIncomeTax -> 37500.0, //lS12.2
      HigherRateIncomeTax -> 0.0, //LS12.3
      AdditionalRateIncomeTax -> 0.0, //LS12.4
      OrdinaryRate -> 147874.0, //LS13.1
      UpperRate -> 10126.0, //LS13.2
      AdditionalRate -> 0.0, //LS13.3
      OtherAdjustmentsIncreasing -> 12094.5,
      OtherAdjustmentsReducing -> 0.0,
      WelshIncomeTax -> 3750.0, //LS20a
      TotalIncomeTax -> 1557.6, //LS20
      TotalIncomeTaxAndNics -> 34107.6, //LS16
      EmployeeNicAmount -> 131.60, //LS14
      PayCgTaxOn -> 0.0, //LS19.8
      TaxableGains -> 0.0, //LS19.6
      AmountDueAtEntrepreneursRate -> 0.0, //LS19.1
      AmountDueAtOrdinaryRate -> 0.0, //LS19.2
      AmountDueRPCIHigherRate -> 0.0, //LS19.3
      AmountDueRPCILowerRate -> 0.0, //LS19.3b
      Adjustments -> 0.0, //LS19.4
      TotalCgTax -> 0.0,
      YourTotalTax -> 34107.6 //RS7
    )

    expected foreach { case (key, expectedValue) =>
      s"return the correct key $key" in {
        server.stubFor(
          WireMock.get(urlEqualTo(odsUrl(taxYear)))
            .willReturn(ok(FileHelper.loadFile("2020-21/utr_8842271803.json")))
        )
        val request = FakeRequest(GET, apiUrl(taxYear))
        val result = resultToAtsData(route(app, request))

        checkResult(result, key, expectedValue)
      }
    }
  }

  "HasSummary (SIT012)" must {

    val taxYear = 2021
    val expected = Map(
      SelfEmploymentIncome -> 0.0, // LS1a
      IncomeFromEmployment -> 50000.0, // LS1
      StatePension -> 0.0, //LS2
      OtherPensionIncome ->  0.0, //LS3
      TaxableStateBenefits -> 0.0, //LS4
      OtherIncome -> 160126.0, //LS5
      BenefitsFromEmployment -> 0.0, //LS6
      TotalIncomeBeforeTax -> 210126.0, //LS7
      PersonalTaxFreeAmount -> 12500.0, //LS8.1
      MarriageAllowanceTransferredAmount -> 0.0, //LS8.2
      OtherAllowancesAmount -> 0.0, //LS9
      TotalTaxFreeAmount -> 12500.0, //LS10
      StartingRateForSavingsAmount -> 0.0, //LS12.1
      BasicRateIncomeTax -> 37500.0, //lS12.2
      HigherRateIncomeTax -> 0.0, //LS12.3
      AdditionalRateIncomeTax -> 0.0, //LS12.4
      OrdinaryRate -> 147874.0, //LS13.1
      UpperRate -> 10126.0, //LS13.2
      AdditionalRate -> 0.0, //LS13.3
      OtherAdjustmentsIncreasing -> 12094.5,
      OtherAdjustmentsReducing -> 0.0,
      WelshIncomeTax -> 3750.0, //LS20a
      TotalIncomeTax -> 3337.5, //LS20
      TotalIncomeTaxAndNics -> 34107.6, //LS16
      EmployeeNicAmount -> 131.60, //LS14
      PayCgTaxOn -> 0.0, //LS19.8
      TaxableGains -> 0.0, //LS19.6
      AmountDueAtEntrepreneursRate -> 0.0, //LS19.1
      AmountDueAtOrdinaryRate -> 0.0, //LS19.2
      AmountDueRPCIHigherRate -> 0.0, //LS19.3
      AmountDueRPCILowerRate -> 0.0, //LS19.3b
      Adjustments -> 0.0, //LS19.4
      TotalCgTax -> 0.0,
      YourTotalTax -> 34107.6 //RS7
    )

    expected foreach { case (key, expectedValue) =>
      s"return the correct key $key" in {
        server.stubFor(
          WireMock.get(urlEqualTo(odsUrl(taxYear)))
            .willReturn(ok(FileHelper.loadFile("2020-21/utr_7268957390.json")))
        )
        val request = FakeRequest(GET, apiUrl(taxYear))
        val result = resultToAtsData(route(app, request))

        checkResult(result, key, expectedValue)
      }
    }
  }

  "HasSummary (SIT014)" must {

    val taxYear = 2020
    val expected = Map(
      SelfEmploymentIncome -> 0.0, // LS1a
      IncomeFromEmployment -> 50000.0, // LS1
      StatePension -> 0.0, //LS2
      OtherPensionIncome ->  0.0, //LS3
      TaxableStateBenefits -> 0.0, //LS4
      OtherIncome -> 160126.0, //LS5
      BenefitsFromEmployment -> 0.0, //LS6
      TotalIncomeBeforeTax -> 210126.0, //LS7
      PersonalTaxFreeAmount -> 12500.0, //LS8.1
      MarriageAllowanceTransferredAmount -> 0.0, //LS8.2
      OtherAllowancesAmount -> 0.0, //LS9
      TotalTaxFreeAmount -> 12500.0, //LS10
      StartingRateForSavingsAmount -> 0.0, //LS12.1
      BasicRateIncomeTax -> 37500.0, //lS12.2
      HigherRateIncomeTax -> 0.0, //LS12.3
      AdditionalRateIncomeTax -> 0.0, //LS12.4
      OrdinaryRate -> 147874.0, //LS13.1
      UpperRate -> 10126.0, //LS13.2
      AdditionalRate -> 0.0, //LS13.3
      OtherAdjustmentsIncreasing -> 12094.5,
      OtherAdjustmentsReducing -> 0.0,
      WelshIncomeTax -> 3750.0, //LS20a
      TotalIncomeTax -> 600.0, //LS20
      TotalIncomeTaxAndNics -> 34107.6, //LS16
      EmployeeNicAmount -> 131.60, //LS14
      PayCgTaxOn -> 0.0, //LS19.8
      TaxableGains -> 0.0, //LS19.6
      AmountDueAtEntrepreneursRate -> 0.0, //LS19.1
      AmountDueAtOrdinaryRate -> 0.0, //LS19.2
      AmountDueRPCIHigherRate -> 0.0, //LS19.3
      AmountDueRPCILowerRate -> 0.0, //LS19.3b
      Adjustments -> 0.0, //LS19.4
      TotalCgTax -> 0.0,
      YourTotalTax -> 34107.6 //RS7
    )

    expected foreach { case (key, expectedValue) =>
      s"return the correct key $key" in {
        server.stubFor(
          WireMock.get(urlEqualTo(odsUrl(taxYear)))
            .willReturn(ok(FileHelper.loadFile("2020-21/utr_9290899941.json")))
        )
        val request = FakeRequest(GET, apiUrl(taxYear))
        val result = resultToAtsData(route(app, request))

        checkResult(result, key, expectedValue)
      }
    }
  }

  "HasSummary (SIT015)" must {

    val taxYear = 2020
    val expected = Map(
      SelfEmploymentIncome -> 0.0, // LS1a
      IncomeFromEmployment -> 50000.0, // LS1
      StatePension -> 0.0, //LS2
      OtherPensionIncome ->  0.0, //LS3
      TaxableStateBenefits -> 0.0, //LS4
      OtherIncome -> 160126.0, //LS5
      BenefitsFromEmployment -> 0.0, //LS6
      TotalIncomeBeforeTax -> 210126.0, //LS7
      PersonalTaxFreeAmount -> 12500.0, //LS8.1
      MarriageAllowanceTransferredAmount -> 0.0, //LS8.2
      OtherAllowancesAmount -> 0.0, //LS9
      TotalTaxFreeAmount -> 12500.0, //LS10
      StartingRateForSavingsAmount -> 0.0, //LS12.1
      BasicRateIncomeTax -> 37500.0, //lS12.2
      HigherRateIncomeTax -> 0.0, //LS12.3
      AdditionalRateIncomeTax -> 0.0, //LS12.4
      OrdinaryRate -> 147874.0, //LS13.1
      UpperRate -> 10126.0, //LS13.2
      AdditionalRate -> 0.0, //LS13.3
      OtherAdjustmentsIncreasing -> 12094.5,
      OtherAdjustmentsReducing -> 0.0,
      WelshIncomeTax -> 3750.0, //LS20a
      TotalIncomeTax -> 9684.5, //LS20
      TotalIncomeTaxAndNics -> 34107.6, //LS16
      EmployeeNicAmount -> 131.60, //LS14
      PayCgTaxOn -> 0.0, //LS19.8
      TaxableGains -> 0.0, //LS19.6
      AmountDueAtEntrepreneursRate -> 0.0, //LS19.1
      AmountDueAtOrdinaryRate -> 0.0, //LS19.2
      AmountDueRPCIHigherRate -> 0.0, //LS19.3
      AmountDueRPCILowerRate -> 0.0, //LS19.3b
      Adjustments -> 0.0, //LS19.4
      TotalCgTax -> 0.0,
      YourTotalTax -> 34107.6 //RS7
    )

    expected foreach { case (key, expectedValue) =>
      s"return the correct key $key" in {
        server.stubFor(
          WireMock.get(urlEqualTo(odsUrl(taxYear)))
            .willReturn(ok(FileHelper.loadFile("2020-21/utr_9223146705.json")))
        )
        val request = FakeRequest(GET, apiUrl(taxYear))
        val result = resultToAtsData(route(app, request))

        checkResult(result, key, expectedValue)
      }
    }
  }

  "HasSummary (SIT016)" must {

    val taxYear = 2021
    val expected = Map(
      SelfEmploymentIncome -> 0.0, // LS1a
      IncomeFromEmployment -> 50000.0, // LS1
      StatePension -> 0.0, //LS2
      OtherPensionIncome ->  0.0, //LS3
      TaxableStateBenefits -> 0.0, //LS4
      OtherIncome -> 160126.0, //LS5
      BenefitsFromEmployment -> 0.0, //LS6
      TotalIncomeBeforeTax -> 210126.0, //LS7
      PersonalTaxFreeAmount -> 12500.0, //LS8.1
      MarriageAllowanceTransferredAmount -> 0.0, //LS8.2
      OtherAllowancesAmount -> 0.0, //LS9
      TotalTaxFreeAmount -> 12500.0, //LS10
      StartingRateForSavingsAmount -> 0.0, //LS12.1
      BasicRateIncomeTax -> 37500.0, //lS12.2
      HigherRateIncomeTax -> 0.0, //LS12.3
      AdditionalRateIncomeTax -> 0.0, //LS12.4
      OrdinaryRate -> 147874.0, //LS13.1
      UpperRate -> 10126.0, //LS13.2
      AdditionalRate -> 0.0, //LS13.3
      OtherAdjustmentsIncreasing -> 12094.5,
      OtherAdjustmentsReducing -> 0.0,
      WelshIncomeTax -> 3750.0, //LS20a
      TotalIncomeTax -> 28015.94, //LS20
      TotalIncomeTaxAndNics -> 34107.6, //LS16
      EmployeeNicAmount -> 131.60, //LS14
      PayCgTaxOn -> 0.0, //LS19.8
      TaxableGains -> 0.0, //LS19.6
      AmountDueAtEntrepreneursRate -> 0.0, //LS19.1
      AmountDueAtOrdinaryRate -> 0.0, //LS19.2
      AmountDueRPCIHigherRate -> 0.0, //LS19.3
      AmountDueRPCILowerRate -> 0.0, //LS19.3b
      Adjustments -> 0.0, //LS19.4
      TotalCgTax -> 0.0,
      YourTotalTax -> 34107.6 //RS7
    )

    expected foreach { case (key, expectedValue) =>
      s"return the correct key $key" in {
        server.stubFor(
          WireMock.get(urlEqualTo(odsUrl(taxYear)))
            .willReturn(ok(FileHelper.loadFile("2020-21/utr_7957650973.json")))
        )
        val request = FakeRequest(GET, apiUrl(taxYear))
        val result = resultToAtsData(route(app, request))

        checkResult(result, key, expectedValue)
      }
    }
  }

  "HasSummary (SIT017)" must {

    val taxYear = 2021
    val expected = Map(
      SelfEmploymentIncome -> 0.0, // LS1a
      IncomeFromEmployment -> 50000.0, // LS1
      StatePension -> 0.0, //LS2
      OtherPensionIncome ->  0.0, //LS3
      TaxableStateBenefits -> 0.0, //LS4
      OtherIncome -> 160126.0, //LS5
      BenefitsFromEmployment -> 0.0, //LS6
      TotalIncomeBeforeTax -> 210126.0, //LS7
      PersonalTaxFreeAmount -> 12500.0, //LS8.1
      MarriageAllowanceTransferredAmount -> 0.0, //LS8.2
      OtherAllowancesAmount -> 0.0, //LS9
      TotalTaxFreeAmount -> 12500.0, //LS10
      StartingRateForSavingsAmount -> 0.0, //LS12.1
      BasicRateIncomeTax -> 37500.0, //lS12.2
      HigherRateIncomeTax -> 0.0, //LS12.3
      AdditionalRateIncomeTax -> 0.0, //LS12.4
      OrdinaryRate -> 147874.0, //LS13.1
      UpperRate -> 10126.0, //LS13.2
      AdditionalRate -> 0.0, //LS13.3
      OtherAdjustmentsIncreasing -> 12094.5,
      OtherAdjustmentsReducing -> 0.0,
      WelshIncomeTax -> 3750.0, //LS20a
      TotalIncomeTax -> 50744.9, //LS20
      TotalIncomeTaxAndNics -> 34107.6, //LS16
      EmployeeNicAmount -> 131.60, //LS14
      PayCgTaxOn -> 0.0, //LS19.8
      TaxableGains -> 0.0, //LS19.6
      AmountDueAtEntrepreneursRate -> 0.0, //LS19.1
      AmountDueAtOrdinaryRate -> 0.0, //LS19.2
      AmountDueRPCIHigherRate -> 0.0, //LS19.3
      AmountDueRPCILowerRate -> 0.0, //LS19.3b
      Adjustments -> 0.0, //LS19.4
      TotalCgTax -> 0.0,
      YourTotalTax -> 34107.6 //RS7
    )

    expected foreach { case (key, expectedValue) =>
      s"return the correct key $key" in {
        server.stubFor(
          WireMock.get(urlEqualTo(odsUrl(taxYear)))
            .willReturn(ok(FileHelper.loadFile("2020-21/utr_1023584560.json")))
        )
        val request = FakeRequest(GET, apiUrl(taxYear))
        val result = resultToAtsData(route(app, request))

        checkResult(result, key, expectedValue)
      }
    }
  }

  "HasSummary (SIT018)" must {

    val taxYear = 2021
    val expected = Map(
      SelfEmploymentIncome -> 0.0, // LS1a
      IncomeFromEmployment -> 50000.0, // LS1
      StatePension -> 0.0, //LS2
      OtherPensionIncome ->  0.0, //LS3
      TaxableStateBenefits -> 0.0, //LS4
      OtherIncome -> 160126.0, //LS5
      BenefitsFromEmployment -> 0.0, //LS6
      TotalIncomeBeforeTax -> 210126.0, //LS7
      PersonalTaxFreeAmount -> 12500.0, //LS8.1
      MarriageAllowanceTransferredAmount -> 0.0, //LS8.2
      OtherAllowancesAmount -> 0.0, //LS9
      TotalTaxFreeAmount -> 12500.0, //LS10
      StartingRateForSavingsAmount -> 0.0, //LS12.1
      BasicRateIncomeTax -> 37500.0, //lS12.2
      HigherRateIncomeTax -> 0.0, //LS12.3
      AdditionalRateIncomeTax -> 0.0, //LS12.4
      OrdinaryRate -> 147874.0, //LS13.1
      UpperRate -> 10126.0, //LS13.2
      AdditionalRate -> 0.0, //LS13.3
      OtherAdjustmentsIncreasing -> 12094.5,
      OtherAdjustmentsReducing -> 0.0,
      WelshIncomeTax -> 3750.0, //LS20a
      TotalIncomeTax -> 30100.0, //LS20
      TotalIncomeTaxAndNics -> 34107.6, //LS16
      EmployeeNicAmount -> 131.60, //LS14
      PayCgTaxOn -> 0.0, //LS19.8
      TaxableGains -> 0.0, //LS19.6
      AmountDueAtEntrepreneursRate -> 0.0, //LS19.1
      AmountDueAtOrdinaryRate -> 0.0, //LS19.2
      AmountDueRPCIHigherRate -> 0.0, //LS19.3
      AmountDueRPCILowerRate -> 0.0, //LS19.3b
      Adjustments -> 0.0, //LS19.4
      TotalCgTax -> 0.0,
      YourTotalTax -> 34107.6 //RS7
    )

    expected foreach { case (key, expectedValue) =>
      s"return the correct key $key" in {
        server.stubFor(
          WireMock.get(urlEqualTo(odsUrl(taxYear)))
            .willReturn(ok(FileHelper.loadFile("2020-21/utr_7741497270.json")))
        )
        val request = FakeRequest(GET, apiUrl(taxYear))
        val result = resultToAtsData(route(app, request))

        checkResult(result, key, expectedValue)
      }
    }
  }

  "HasSummary (SIT019)" must {

    val taxYear = 2021
    val expected = Map(
      SelfEmploymentIncome -> 0.0, // LS1a
      IncomeFromEmployment -> 50000.0, // LS1
      StatePension -> 0.0, //LS2
      OtherPensionIncome ->  0.0, //LS3
      TaxableStateBenefits -> 0.0, //LS4
      OtherIncome -> 160126.0, //LS5
      BenefitsFromEmployment -> 0.0, //LS6
      TotalIncomeBeforeTax -> 210126.0, //LS7
      PersonalTaxFreeAmount -> 12500.0, //LS8.1
      MarriageAllowanceTransferredAmount -> 0.0, //LS8.2
      OtherAllowancesAmount -> 0.0, //LS9
      TotalTaxFreeAmount -> 12500.0, //LS10
      StartingRateForSavingsAmount -> 0.0, //LS12.1
      BasicRateIncomeTax -> 37500.0, //lS12.2
      HigherRateIncomeTax -> 0.0, //LS12.3
      AdditionalRateIncomeTax -> 0.0, //LS12.4
      OrdinaryRate -> 147874.0, //LS13.1
      UpperRate -> 10126.0, //LS13.2
      AdditionalRate -> 0.0, //LS13.3
      OtherAdjustmentsIncreasing -> 12094.5,
      OtherAdjustmentsReducing -> 0.0,
      WelshIncomeTax -> 3750.0, //LS20a
      TotalIncomeTax -> 5200.0, //LS20
      TotalIncomeTaxAndNics -> 34107.6, //LS16
      EmployeeNicAmount -> 131.60, //LS14
      PayCgTaxOn -> 0.0, //LS19.8
      TaxableGains -> 0.0, //LS19.6
      AmountDueAtEntrepreneursRate -> 0.0, //LS19.1
      AmountDueAtOrdinaryRate -> 0.0, //LS19.2
      AmountDueRPCIHigherRate -> 0.0, //LS19.3
      AmountDueRPCILowerRate -> 0.0, //LS19.3b
      Adjustments -> 0.0, //LS19.4
      TotalCgTax -> 0.0,
      YourTotalTax -> 34107.6 //RS7
    )

    expected foreach { case (key, expectedValue) =>
      s"return the correct key $key" in {
        server.stubFor(
          WireMock.get(urlEqualTo(odsUrl(taxYear)))
            .willReturn(ok(FileHelper.loadFile("2020-21/utr_6180195454.json")))
        )
        val request = FakeRequest(GET, apiUrl(taxYear))
        val result = resultToAtsData(route(app, request))

        checkResult(result, key, expectedValue)
      }
    }
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
