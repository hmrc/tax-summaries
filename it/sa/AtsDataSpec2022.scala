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

package sa

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.{ok, urlEqualTo}
import models.AtsMiddleTierData
import models.LiabilityKey._
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.FileHelper

class AtsDataSpec2022 extends SaTestHelper {

  val taxPayerFile = "taxPayerDetails.json"

  trait Test {
    val taxYear = 2022

    def odsUrl(taxYear: Int): String = s"/self-assessment/individuals/" + utr + s"/annual-tax-summaries/$taxYear"

    def apiUrl(taxYear: Int): String = s"/taxs/$utr/$taxYear/ats-data"

    def request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, apiUrl(taxYear))
      .withHeaders((AUTHORIZATION, "Bearer 123"))
  }


  "HasSummary (SIT001)" must {
    val expected = Map(
      SelfEmploymentIncome -> 942.00, // LS1a e
      IncomeFromEmployment -> 122500.00, // LS1 e
      StatePension -> 0.00, //LS2 e
      //OtherPensionIncome -> 3121.00, //LS3 e Excel
      TaxableStateBenefits -> 0.00, //LS4 e
      OtherIncome -> 298111.00, //LS5 e
      BenefitsFromEmployment -> 9600.00, //LS6 e
      //TotalIncomeBeforeTax -> 438044.00, //LS7 total income received e Excel
      PersonalTaxFreeAmount -> 0.00, //LS8.1 e
      MarriageAllowanceTransferredAmount -> 0.00, //LS8.2 e
      OtherAllowancesAmount -> 15209.00, //LS9 e
      TotalTaxFreeAmount -> 15209.00, //LS10 e
      StartingRateForSavings -> 0.00, //LS12.1
      StartingRateForSavingsAmount -> 0.00, //LS12.1 e
      BasicRateIncomeTax -> 45218.00, //lS12.2 e
      BasicRateIncomeTaxAmount -> 9043.60, // LS12.2 (tax amount - right column)
      HigherRateIncomeTax -> 112300.00, //LS12.3 e
      HigherRateIncomeTaxAmount -> 44920.0, // LS12.3 (tax amount - right column)
      AdditionalRateIncomeTax -> 205433.00, //LS12.4 e
      AdditionalRateIncomeTaxAmount -> 92444.85, // LS12.4 (tax amount - right column)
      OrdinaryRate -> 0.00, //LS13.1 e
      OrdinaryRateAmount -> 0.0, // LS13.1 (tax amount - right column)
      UpperRate -> 0.00, //LS13.2 e
      UpperRateAmount -> 0.0, // LS13.2 (tax amount - right column)
      AdditionalRate -> 57884.00, //LS13.3 e,
      AdditionalRateAmount -> 22053.80, // LS13.3 (tax amount - right column)
      OtherAdjustmentsIncreasing -> 0.00, //LS15a e
      OtherAdjustmentsReducing -> 5130.00, //LS15b e
      WelshIncomeTax -> 0.00, //LS20a
      TotalIncomeTax -> 163332.65, //LS20 e
      TotalIncomeTaxAndNics -> 163491.25, //LS16 e
      EmployeeNicAmount -> 158.60, //LS14 e
      //IncomeAfterTaxAndNics -> 274552.75, //LS17, RS5 e
      //EmployerNicAmount -> 15685.00, //LS18 e
      PayCgTaxOn -> 28700.00, //LS19.8 e
      TaxableGains -> 41000.00, //LS19.6 e
      AmountDueAtEntrepreneursRate -> 1200.00, //LS19.1 e
      AmountAtEntrepreneursRate -> 12000.00, //LS19.1 e
      AmountDueAtOrdinaryRate -> 0.00, //LS19.2 e
      //AmountDueRPCIHigherRate -> 3340.00, //LS19.3 e Excel
      AmountDueRPCILowerRate -> 0.00, //LS19.3a e
      Adjustments -> 0.00, //LS19.4 e
      TotalCgTax -> 4540.00, //e
      YourTotalTax -> 168031.25, //RS7 e
    )

    expected foreach { case (key, expectedValue) =>
      s"return the correct key $key" in new Test {
        server.stubFor(
          WireMock.get(urlEqualTo(odsUrl(taxYear)))
            .willReturn(ok(FileHelper.loadFile("2021-22/TC52.json")))
        )

        val result: AtsMiddleTierData = resultToAtsData(route(app, request))
        checkResult(result, key, expectedValue)
      }
    }
  }

  "HasSummary (SIT002)" must {
    val expected = Map(
      SelfEmploymentIncome -> 6945.00, // LS1a e
      IncomeFromEmployment -> 8000.00, // LS1 e
      StatePension -> 0.00, //LS2 e
      OtherPensionIncome -> 0.00, //LS3 e
      TaxableStateBenefits -> 0.00, //LS4 e
      OtherIncome -> 38728.00, //LS5 e
      BenefitsFromEmployment -> 0.00, //LS6 e
      TotalIncomeBeforeTax -> 53673.00, //LS7 total income received e
      PersonalTaxFreeAmount -> 12570.00, //LS8.1 e
      MarriageAllowanceTransferredAmount -> 0.00, //LS8.2 e
      OtherAllowancesAmount -> 0.00, //LS9 e
      TotalTaxFreeAmount -> 12570.00, //LS10 e
      StartingRateForSavings -> 5000.00, //LS12.1
      StartingRateForSavingsAmount -> 0.00, //LS12.1 e
      BasicRateIncomeTax -> 61.00, //lS12.2 e
      BasicRateIncomeTaxAmount -> 12.20, // LS12.2 (tax amount - right column)
      HigherRateIncomeTax -> 3403.00, //LS12.3 e
      HigherRateIncomeTaxAmount -> 1361.20, // LS12.3 (tax amount - right column)
      AdditionalRateIncomeTax -> 0.00, //LS12.4 e
      AdditionalRateIncomeTaxAmount -> 0.00, // LS12.4 (tax amount - right column)
      OrdinaryRate -> 30139.00, //LS13.1 e
      OrdinaryRateAmount -> 2260.42, // LS13.1 (tax amount - right column)
      UpperRate -> 0.00, //LS13.2 e
      UpperRateAmount -> 0.0, // LS13.2 (tax amount - right column)
      AdditionalRate -> 0.00, //LS13.3 e,
      AdditionalRateAmount -> 0.00, // LS13.3 (tax amount - right column)
      OtherAdjustmentsIncreasing -> 0.00, //LS15a e
      OtherAdjustmentsReducing -> 0.00, //LS15b e
      WelshIncomeTax -> 0.00, //LS20a
      TotalIncomeTax -> 3633.82, //LS20 e
      TotalIncomeTaxAndNics -> 3792.42, //LS16 e
      EmployeeNicAmount -> 158.60, //LS14 e
      //IncomeAfterTaxAndNics -> 274552.75, //LS17, RS5 e
      //EmployerNicAmount -> 15685.00, //LS18 e
      PayCgTaxOn -> 0.00, //LS19.8 e
      TaxableGains -> 0.00, //LS19.6 e
      AmountDueAtEntrepreneursRate -> 0.00, //LS19.1 e
      AmountAtEntrepreneursRate -> 0.00, //LS19.1 e
      AmountDueAtOrdinaryRate -> 0.00, //LS19.2 e
      AmountDueRPCIHigherRate -> 0.00, //LS19.3 e
      AmountDueRPCILowerRate -> 0.00, //LS19.3a e
      Adjustments -> 0.00, //LS19.4 e
      TotalCgTax -> 0.00, //e
      YourTotalTax -> 3792.42, //RS7 e
    )

    expected foreach { case (key, expectedValue) =>
      s"return the correct key $key" in new Test {
        server.stubFor(
          WireMock.get(urlEqualTo(odsUrl(taxYear)))
            .willReturn(ok(FileHelper.loadFile("2021-22/TC53.json")))
        )

        val result: AtsMiddleTierData = resultToAtsData(route(app, request))
        checkResult(result, key, expectedValue)
      }
    }
  }

  "HasSummary (SIT003)" must {
    val expected = Map(
      SelfEmploymentIncome -> 0.00, // LS1a e
      IncomeFromEmployment -> 0.00, // LS1 e
      StatePension -> 0.00, //LS2 e
      OtherPensionIncome -> 0.00, //LS3 e
      TaxableStateBenefits -> 0.00, //LS4 e
      OtherIncome -> 102811.00, //LS5 e
      BenefitsFromEmployment -> 0.00, //LS6 e
      TotalIncomeBeforeTax -> 102811.00, //LS7 total income received e
      PersonalTaxFreeAmount -> 12510.00, //LS8.1 e
      MarriageAllowanceTransferredAmount -> 0.00, //LS8.2 e
      OtherAllowancesAmount -> 2691.00, //LS9 e
      TotalTaxFreeAmount -> 15201.00, //LS10 e
      StartingRateForSavings -> 0.00, //LS12.1
      StartingRateForSavingsAmount -> 0.00, //LS12.1 e
      BasicRateIncomeTax -> 35925.00, //lS12.2 e
      BasicRateIncomeTaxAmount -> 7185.00, // LS12.2 (tax amount - right column)
      HigherRateIncomeTax -> 49910.00, //LS12.3 e
      HigherRateIncomeTaxAmount -> 19964.00, // LS12.3 (tax amount - right column)
      AdditionalRateIncomeTax -> 0.00, //LS12.4 e
      AdditionalRateIncomeTaxAmount -> 0.00, // LS12.4 (tax amount - right column)
      OrdinaryRate -> 0.00, //LS13.1 e
      OrdinaryRateAmount -> 0.00, // LS13.1 (tax amount - right column)
      UpperRate -> 0.00, //LS13.2 e
      UpperRateAmount -> 0.0, // LS13.2 (tax amount - right column)
      AdditionalRate -> 0.00, //LS13.3 e,
      AdditionalRateAmount -> 0.00, // LS13.3 (tax amount - right column)
      OtherAdjustmentsIncreasing -> 0.00, //LS15a e
      OtherAdjustmentsReducing -> 23468.00, //LS15b e
      WelshIncomeTax -> 0.00, //LS20a
      TotalIncomeTax -> 3681.50, //LS20 e
      TotalIncomeTaxAndNics -> 3681.50, //LS16 e
      EmployeeNicAmount -> 0.00, //LS14 e
      //IncomeAfterTaxAndNics -> 274552.75, //LS17, RS5 e
      //EmployerNicAmount -> 15685.00, //LS18 e
      PayCgTaxOn -> 0.00, //LS19.8 e
      TaxableGains -> 0.00, //LS19.6 e
      AmountDueAtEntrepreneursRate -> 0.00, //LS19.1 e
      AmountAtEntrepreneursRate -> 0.00, //LS19.1 e
      AmountDueAtOrdinaryRate -> 0.00, //LS19.2 e
      AmountDueRPCIHigherRate -> 0.00, //LS19.3 e
      AmountDueRPCILowerRate -> 0.00, //LS19.3a e
      Adjustments -> 0.00, //LS19.4 e
      TotalCgTax -> 0.00, //e
      YourTotalTax -> 3681.50, //RS7 e
    )

    expected foreach { case (key, expectedValue) =>
      s"return the correct key $key" in new Test {
        server.stubFor(
          WireMock.get(urlEqualTo(odsUrl(taxYear)))
            .willReturn(ok(FileHelper.loadFile("2021-22/TC45.json")))
        )

        val result: AtsMiddleTierData = resultToAtsData(route(app, request))
        checkResult(result, key, expectedValue)
      }
    }
  }

  "HasSummary (SIT004)" must {
    val expected = Map(
      SelfEmploymentIncome -> 157719.00, // LS1a e
      IncomeFromEmployment -> 15000.00, // LS1 e
      StatePension -> 0.00, //LS2 e
      OtherPensionIncome -> 0.00, //LS3 e
      TaxableStateBenefits -> 0.00, //LS4 e
      OtherIncome -> 16963.00, //LS5 e
      BenefitsFromEmployment -> 0.00, //LS6 e
      TotalIncomeBeforeTax -> 189682.00, //LS7 total income received e
      PersonalTaxFreeAmount -> 0.00, //LS8.1 e
      MarriageAllowanceTransferredAmount -> 0.00, //LS8.2 e
      OtherAllowancesAmount -> 33151.00, //LS9 e
      TotalTaxFreeAmount -> 33151.00, //LS10 e
      StartingRateForSavings -> 0.00, //LS12.1
      StartingRateForSavingsAmount -> 0.00, //LS12.1 e
      OrdinaryRate -> 0.00, //LS13.1 e
      OrdinaryRateAmount -> 0.00, // LS13.1 (tax amount - right column)
      UpperRate -> 3233.00, //LS13.2 e
      UpperRateAmount -> 1050.72, // LS13.2 (tax amount - right column)
      AdditionalRate -> 0.00, //LS13.3 e,
      AdditionalRateAmount -> 0.00, // LS13.3 (tax amount - right column)
      OtherAdjustmentsIncreasing -> 0.00, //LS15a e
      OtherAdjustmentsReducing -> 0.00, //LS15b e
      WelshIncomeTax -> 0.00, //LS20a
      //TotalIncomeTax -> 56492.86, //LS20 e excel
      //TotalIncomeTaxAndNics -> 61668.70, //LS16 e excel
      EmployeeNicAmount -> 5175.84, //LS14 e
      //IncomeAfterTaxAndNics -> 274552.75, //LS17, RS5 e
      //EmployerNicAmount -> 15685.00, //LS18 e
      PayCgTaxOn -> 34000.00, //LS19.8 e
      TaxableGains -> 46300.00, //LS19.6 e
      AmountDueAtEntrepreneursRate -> 0.00, //LS19.1 e
      AmountAtEntrepreneursRate -> 0.00, //LS19.1 e
      AmountDueAtOrdinaryRate -> 0.00, //LS19.2 e
      AmountDueRPCIHigherRate -> 9520.00, //LS19.3 e
      AmountDueRPCILowerRate -> 0.00, //LS19.3a e
      Adjustments -> 0.00, //LS19.4 e
      TotalCgTax -> 9520.00, //e
     // YourTotalTax -> 71188.70, //RS7 e Excel
    )

    expected foreach { case (key, expectedValue) =>
      s"return the correct key $key" in new Test {
        server.stubFor(
          WireMock.get(urlEqualTo(odsUrl(taxYear)))
            .willReturn(ok(FileHelper.loadFile("2021-22/TC153.json")))
        )

        val result: AtsMiddleTierData = resultToAtsData(route(app, request))
        checkResult(result, key, expectedValue)
      }
    }
  }

  "HasSummary (SIT005)" must {
    val expected = Map(
      SelfEmploymentIncome -> 0.00, // LS1a e
      IncomeFromEmployment -> 149243.00, // LS1 e
      StatePension -> 0.00, //LS2 e
      OtherPensionIncome -> 0.00, //LS3 e
      TaxableStateBenefits -> 0.00, //LS4 e
      OtherIncome -> 12000.00, //LS5 e
      BenefitsFromEmployment -> 0.00, //LS6 e
      TotalIncomeBeforeTax -> 161243.00, //LS7 total income received e
      PersonalTaxFreeAmount -> 0.00, //LS8.1 e
      MarriageAllowanceTransferredAmount -> 0.00, //LS8.2 e
      OtherAllowancesAmount -> 0.00, //LS9 e
      TotalTaxFreeAmount -> 0.00, //LS10 e
      StartingRateForSavings -> 0.00, //LS12.1
      StartingRateForSavingsAmount -> 0.00, //LS12.1 e
      BasicRateIncomeTax -> 25700.00, //lS12.2 e
      BasicRateIncomeTaxAmount -> 5140.00, // LS12.2 (tax amount - right column)
      HigherRateIncomeTax -> 112300.00, //LS12.3 e
      HigherRateIncomeTaxAmount -> 44920.00, // LS12.3 (tax amount - right column)
      AdditionalRateIncomeTax -> 11243.00, //LS12.4 e
      AdditionalRateIncomeTaxAmount -> 5059.35, // LS12.4 (tax amount - right column)
      OrdinaryRate -> 10000.00, //LS13.1 e
      OrdinaryRateAmount -> 750.00, // LS13.1 (tax amount - right column)
      UpperRate -> 0.00, //LS13.2 e
      UpperRateAmount -> 0.0, // LS13.2 (tax amount - right column)
      AdditionalRate -> 0.00, //LS13.3 e,
      AdditionalRateAmount -> 0.00, // LS13.3 (tax amount - right column)
      OtherAdjustmentsIncreasing -> 0.00, //LS15a e
      OtherAdjustmentsReducing -> 0.00, //LS15b e
      WelshIncomeTax -> 0.00, //LS20a
      TotalIncomeTax -> 55869.35, //LS20 e
      TotalIncomeTaxAndNics -> 55869.35, //LS16 e
      EmployeeNicAmount -> 0.00, //LS14 e
      //IncomeAfterTaxAndNics -> 274552.75, //LS17, RS5 e
      //EmployerNicAmount -> 15685.00, //LS18 e
      PayCgTaxOn -> 0.00, //LS19.8 e
      TaxableGains -> 0.00, //LS19.6 e
      AmountDueAtEntrepreneursRate -> 0.00, //LS19.1 e
      AmountAtEntrepreneursRate -> 0.00, //LS19.1 e
      AmountDueAtOrdinaryRate -> 0.00, //LS19.2 e
      AmountDueRPCIHigherRate -> 0.00, //LS19.3 e
      Adjustments -> 0.00, //LS19.4 e
      TotalCgTax -> 0.00, //e
      YourTotalTax -> 55869.35, //RS7 e
    )

    expected foreach { case (key, expectedValue) =>
      s"return the correct key $key" in new Test {
        server.stubFor(
          WireMock.get(urlEqualTo(odsUrl(taxYear)))
            .willReturn(ok(FileHelper.loadFile("2021-22/TC222.json")))
        )

        val result: AtsMiddleTierData = resultToAtsData(route(app, request))
        checkResult(result, key, expectedValue)
      }
    }
  }

  "HasSummary (SIT006)" must {
    val expected = Map(
      SelfEmploymentIncome -> 14190.00, // LS1a e
      IncomeFromEmployment -> 31717.00, // LS1 e
      StatePension -> 0.00, //LS2 e
      OtherPensionIncome -> 0.00, //LS3 e
      TaxableStateBenefits -> 0.00, //LS4 e
      OtherIncome -> 6178.00, //LS5 e
      BenefitsFromEmployment -> 0.00, //LS6 e
      TotalIncomeBeforeTax -> 52085.00, //LS7 total income received e
      PersonalTaxFreeAmount -> 12570.00, //LS8.1 e
      MarriageAllowanceTransferredAmount -> 0.00, //LS8.2 e
      OtherAllowancesAmount -> 0.00, //LS9 e
      TotalTaxFreeAmount -> 12570.00, //LS10 e
      StartingRateForSavings -> 0.00, //LS12.1
      StartingRateForSavingsAmount -> 0.00, //LS12.1 e
      BasicRateIncomeTax -> 12432.00, //lS12.2 e
      BasicRateIncomeTaxAmount -> 2486.40, // LS12.2 (tax amount - right column)
      HigherRateIncomeTax -> 620.00, //LS12.3 e
      HigherRateIncomeTaxAmount -> 254.20, // LS12.3 (tax amount - right column)
      AdditionalRateIncomeTax -> 0.00, //LS12.4 e
      AdditionalRateIncomeTaxAmount -> 0.00, // LS12.4 (tax amount - right column)
      OrdinaryRate -> 3310.00, //LS13.1 e
      OrdinaryRateAmount -> 248.25, // LS13.1 (tax amount - right column)
      UpperRate -> 190.00, //LS13.2 e
      UpperRateAmount -> 61.75, // LS13.2 (tax amount - right column)
      AdditionalRate -> 0.00, //LS13.3 e,
      AdditionalRateAmount -> 0.00, // LS13.3 (tax amount - right column)
      OtherAdjustmentsIncreasing -> 0.00, //LS15a e
      OtherAdjustmentsReducing -> 0.00, //LS15b e
      WelshIncomeTax -> 0.00, //LS20a
      TotalIncomeTax -> 3050.60, //LS20 e
      TotalIncomeTaxAndNics -> 3585.53, //LS16 e
      EmployeeNicAmount -> 534.93, //LS14 e
      //IncomeAfterTaxAndNics -> 274552.75, //LS17, RS5 e
      //EmployerNicAmount -> 15685.00, //LS18 e
      PayCgTaxOn -> 0.00, //LS19.8 e
      TaxableGains -> 0.00, //LS19.6 e
      AmountDueAtEntrepreneursRate -> 0.00, //LS19.1 e
      AmountAtEntrepreneursRate -> 0.00, //LS19.1 e
      AmountDueAtOrdinaryRate -> 0.00, //LS19.2 e
      AmountDueRPCIHigherRate -> 0.00, //LS19.3 e
      Adjustments -> 0.00, //LS19.4 e
      TotalCgTax -> 0.00, //e
      YourTotalTax -> 3585.53, //RS7 e
    )

    expected foreach { case (key, expectedValue) =>
      s"return the correct key $key" in new Test {
        server.stubFor(
          WireMock.get(urlEqualTo(odsUrl(taxYear)))
            .willReturn(ok(FileHelper.loadFile("2021-22/TC3.json")))
        )

        val result: AtsMiddleTierData = resultToAtsData(route(app, request))
        checkResult(result, key, expectedValue)
      }
    }
  }

  "HasSummary (SIT007)" must {
    val expected = Map(
      SelfEmploymentIncome -> 0.00, // LS1a e
      IncomeFromEmployment -> 0.00, // LS1 e
      //StatePension -> 6198.00, //LS2 e Wrong in excel
      //OtherPensionIncome -> 12302.00, //LS3 e Wrong in excel
      TaxableStateBenefits -> 0.00, //LS4 e
      OtherIncome -> 36505.00, //LS5 e
      BenefitsFromEmployment -> 0.00, //LS6 e
      //TotalIncomeBeforeTax -> 55005.00, //LS7 total income received e Excel
      PersonalTaxFreeAmount -> 12570.00, //LS8.1 e
      MarriageAllowanceTransferredAmount -> 0.00, //LS8.2 e
      OtherAllowancesAmount -> 0.00, //LS9 e
      TotalTaxFreeAmount -> 12570.00, //LS10 e
      StartingRateForSavings -> 0.00, //LS12.1
      StartingRateForSavingsAmount -> 0.00, //LS12.1 e
      BasicRateIncomeTax -> 37200.00, //lS12.2 e
      BasicRateIncomeTaxAmount -> 7440.00, // LS12.2 (tax amount - right column)
      HigherRateIncomeTax -> 0.00, //LS12.3 e
      HigherRateIncomeTaxAmount -> 0.00, // LS12.3 (tax amount - right column)
      AdditionalRateIncomeTax -> 0.00, //LS12.4 e
      AdditionalRateIncomeTaxAmount -> 0.00, // LS12.4 (tax amount - right column)
      OrdinaryRate -> 0.00, //LS13.1 e
      OrdinaryRateAmount -> 0.00, // LS13.1 (tax amount - right column)
      UpperRate -> 2735.00, //LS13.2 e
      UpperRateAmount -> 888.87, // LS13.2 (tax amount - right column)
      AdditionalRate -> 0.00, //LS13.3 e,
      AdditionalRateAmount -> 0.00, // LS13.3 (tax amount - right column)
      OtherAdjustmentsIncreasing -> 0.00, //LS15a e
      OtherAdjustmentsReducing -> 0.00, //LS15b e
      WelshIncomeTax -> 0.00, //LS20a
      TotalIncomeTax -> 8328.87, //LS20 e
      TotalIncomeTaxAndNics -> 8328.87, //LS16 e
      EmployeeNicAmount -> 0.00, //LS14 e
      //IncomeAfterTaxAndNics -> 274552.75, //LS17, RS5 e
      //EmployerNicAmount -> 15685.00, //LS18 e
      PayCgTaxOn -> 0.00, //LS19.8 e
      TaxableGains -> 0.00, //LS19.6 e
      AmountDueAtEntrepreneursRate -> 0.00, //LS19.1 e
      AmountAtEntrepreneursRate -> 0.00, //LS19.1 e
      AmountDueAtOrdinaryRate -> 0.00, //LS19.2 e
      AmountDueRPCIHigherRate -> 0.00, //LS19.3 e
      Adjustments -> 0.00, //LS19.4 e
      TotalCgTax -> 0.00, //e
      YourTotalTax -> 8328.87, //RS7 e
    )

    expected foreach { case (key, expectedValue) =>
      s"return the correct key $key" in new Test {
        server.stubFor(
          WireMock.get(urlEqualTo(odsUrl(taxYear)))
            .willReturn(ok(FileHelper.loadFile("2021-22/TC6.json")))
        )

        val result: AtsMiddleTierData = resultToAtsData(route(app, request))
        checkResult(result, key, expectedValue)
      }
    }
  }
}
