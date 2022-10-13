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

import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, ok, urlEqualTo}
import models.AtsMiddleTierData
import models.LiabilityKey._
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.FileHelper

import scala.concurrent.Future

class AtsDataSpec2021 extends SaTestHelper {

  val taxPayerFile = "taxPayerDetails.json"

  trait Test {
    val taxYear = 2021

    def odsUrl(taxYear: Int): String = s"/self-assessment/individuals/" + utr + s"/annual-tax-summaries/$taxYear"

    def apiUrl(taxYear: Int): String = s"/taxs/$utr/$taxYear/ats-data"

    def request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, apiUrl(taxYear))
      .withHeaders((AUTHORIZATION, "Bearer 123"))
  }

  "HasSummary (SIT001)" must {
    val expected = Map(
      SelfEmploymentIncome               -> 0.0, // LS1a
      IncomeFromEmployment               -> 0.0, // LS1
      StatePension                       -> 14206.0, //LS2
      OtherPensionIncome                 -> 5300.0, //LS3
      TaxableStateBenefits               -> 0.0, //LS4
      OtherIncome                        -> 3358.0, //LS5
      BenefitsFromEmployment             -> 0.0, //LS6
      TotalIncomeBeforeTax               -> 22864.0, //LS7 total income received
      PersonalTaxFreeAmount              -> 0.00, //LS8.1
      MarriageAllowanceTransferredAmount -> 0.0, //LS8.2
      OtherAllowancesAmount              -> 1204.0, //LS9
      TotalTaxFreeAmount                 -> 12500.0, //LS10
      StartingRateForSavingsAmount       -> 0.0, //LS12.1
      BasicRateIncomeTax                 -> 20532.0, //lS12.2
      HigherRateIncomeTax                -> 0.0, //LS12.3
      AdditionalRateIncomeTax            -> 0.0, //LS12.4
      OrdinaryRate                       -> 0.0, //LS13.1
      UpperRate                          -> 0.0, //LS13.2
      AdditionalRate                     -> 0.0, //LS13.3
      OtherAdjustmentsIncreasing         -> 0.0,
      OtherAdjustmentsReducing           -> 0.0,
      WelshIncomeTax                     -> 0.0, //LS20a
      TotalIncomeTax                     -> 1149.85, //LS20
      TotalIncomeTaxAndNics              -> 1281.45, //LS16
      EmployeeNicAmount                  -> 131.60, //LS14
      PayCgTaxOn                         -> 0.0, //LS19.8
      TaxableGains                       -> 0.0, //LS19.6
      TotalTaxFreeAmount                 -> 1204.0, //LS19.7
      AmountDueAtEntrepreneursRate       -> 0.0, //LS19.1
      AmountDueAtOrdinaryRate            -> 0.0, //LS19.2
      AmountDueRPCIHigherRate            -> 0.0, //LS19.3
      AmountDueRPCILowerRate             -> 0.0, //LS19.3b
      Adjustments                        -> 0.0, //LS19.4
      TotalCgTax                         -> 0.0,
      YourTotalTax                       -> 1281.45 //RS7
    )

    expected foreach { case (key, expectedValue) =>
      s"return the correct key $key" in new Test {
        server.stubFor(
          WireMock
            .get(urlEqualTo(odsUrl(taxYear)))
            .willReturn(ok(FileHelper.loadFile("2020-21/utr_6602556503.json")))
        )

        val result: AtsMiddleTierData = resultToAtsData(route(app, request))
        checkResult(result, key, expectedValue)
      }
    }
  }

  "HasSummary (SIT002)" must {
    val expected = Map(
      SelfEmploymentIncome               -> 0.0, // LS1a
      IncomeFromEmployment               -> 0.0, // LS1
      StatePension                       -> 0.0, //LS2
      OtherPensionIncome                 -> 62732.0, //LS3
      TaxableStateBenefits               -> 0.0, //LS4
      OtherIncome                        -> 1057.0, //LS5
      BenefitsFromEmployment             -> 0.0, //LS6
      TotalIncomeBeforeTax               -> 63789.0, //LS7 total income received
      PersonalTaxFreeAmount              -> 12500.00, //LS8.1
      MarriageAllowanceTransferredAmount -> 0.0, //LS8.2
      OtherAllowancesAmount              -> 0.0, //LS9
      TotalTaxFreeAmount                 -> 12500.0, //LS10
      StartingRateForSavingsAmount       -> 0.0, //LS12.1
      BasicRateIncomeTax                 -> 37500.0, //lS12.2
      HigherRateIncomeTax                -> 12732.0, //LS12.3
      AdditionalRateIncomeTax            -> 0.0, //LS12.4
      OrdinaryRate                       -> 0.0, //LS13.1
      UpperRate                          -> 0.0, //LS13.2
      AdditionalRate                     -> 0.0, //LS13.3
      OtherAdjustmentsIncreasing         -> 0.0,
      OtherAdjustmentsReducing           -> 0.0,
      WelshIncomeTax                     -> 0.0, //LS20a
      TotalIncomeTax                     -> 12592.8, //LS20
      TotalIncomeTaxAndNics              -> 12724.4, //LS16
      EmployeeNicAmount                  -> 131.60, //LS14
      PayCgTaxOn                         -> 8675.0, //LS19.8
      TaxableGains                       -> 20975.0, //LS19.6
      TotalTaxFreeAmount                 -> 12500.0, //LS19.7
      AmountDueAtEntrepreneursRate       -> 0.0, //LS19.1
      AmountDueAtOrdinaryRate            -> 0.0, //LS19.2
      AmountDueRPCIHigherRate            -> 9077.04, //LS19.3
      AmountDueRPCILowerRate             -> 0.0, //LS19.3b
      Adjustments                        -> 0.0, //LS19.4
      TotalCgTax                         -> 13272.04,
      YourTotalTax                       -> 25996.44 //RS7
    )

    expected foreach { case (key, expectedValue) =>
      s"return the correct key $key" in new Test {
        server.stubFor(
          WireMock
            .get(urlEqualTo(odsUrl(taxYear)))
            .willReturn(ok(FileHelper.loadFile("2020-21/utr_2752692244.json")))
        )

        val result: AtsMiddleTierData = resultToAtsData(route(app, request))
        checkResult(result, key, expectedValue)
      }
    }
  }

  //Regression test for 2021 spec
  "HasSummary (SIT013)" must {
    val expected = Map(
      SelfEmploymentIncome               -> 0.0, // LS1a
      IncomeFromEmployment               -> 0.0, // LS1
      StatePension                       -> 0.0, //LS2
      OtherPensionIncome                 -> 0.0, //LS3
      TaxableStateBenefits               -> 0.0, //LS4
      OtherIncome                        -> 7005.0, //LS5
      BenefitsFromEmployment             -> 0.0, //LS6
      TotalIncomeBeforeTax               -> 7005.0, //LS7 total income received
      PersonalTaxFreeAmount              -> 12500.00, //LS8.1
      MarriageAllowanceTransferredAmount -> 0.0, //LS8.2
      OtherAllowancesAmount              -> 0.0, //LS9
      TotalTaxFreeAmount                 -> 12500.0, //LS10
      StartingRateForSavingsAmount       -> 0.0, //LS12.1
      BasicRateIncomeTax                 -> 0.0, //lS12.2
      HigherRateIncomeTax                -> 0.0, //LS12.3
      AdditionalRateIncomeTax            -> 0.0, //LS12.4
      OrdinaryRate                       -> 0.0, //LS13.1
      UpperRate                          -> 0.0, //LS13.2
      AdditionalRate                     -> 0.0, //LS13.3
      OtherAdjustmentsIncreasing         -> 450.0,
      OtherAdjustmentsReducing           -> 0.0,
      WelshIncomeTax                     -> 0.0, //LS20a
      TotalIncomeTax                     -> 450.0, //LS20 INCOME_TAX_DUE?
      TotalIncomeTaxAndNics              -> 581.60, //LS16
      EmployeeNicAmount                  -> 131.60, //LS14
      PayCgTaxOn                         -> 0.0, //LS19.8
      TaxableGains                       -> 0.0, //LS19.6
      TotalTaxFreeAmount                 -> 12500.0, //LS19.7
      AmountDueAtEntrepreneursRate       -> 0.0, //LS19.1
      AmountDueAtOrdinaryRate            -> 0.0, //LS19.2
      AmountDueRPCIHigherRate            -> 0.0, //LS19.3
      AmountDueRPCILowerRate             -> 0.0, //LS19.3b
      Adjustments                        -> 0.0, //LS19.4
      TotalCgTax                         -> 0.0,
      YourTotalTax                       -> 581.60 //RS7
    )

    expected foreach { case (key, expectedValue) =>
      s"return the correct key $key" in new Test {
        server.stubFor(
          WireMock
            .get(urlEqualTo(odsUrl(taxYear)))
            .willReturn(ok(FileHelper.loadFile("2020-21/utr_9784036411.json")))
        )

        val result: AtsMiddleTierData = resultToAtsData(route(app, request))
        checkResult(result, key, expectedValue)
      }
    }
  }

  "HasSummary (SIT003)" must {
    val expected = Map(
      SelfEmploymentIncome               -> 21055.0, // LS1a
      IncomeFromEmployment               -> 48484.0, // LS1
      StatePension                       -> 8609.0, //LS2
      OtherPensionIncome                 -> 79811.0, //LS3
      TaxableStateBenefits               -> 0.0, //LS4
      OtherIncome                        -> 4338.0, //LS5
      BenefitsFromEmployment             -> 0.0, //LS6
      TotalIncomeBeforeTax               -> 162297.0, //LS7 total income received
      PersonalTaxFreeAmount              -> 0.0, //LS8.1
      MarriageAllowanceTransferredAmount -> 0.0, //LS8.2
      OtherAllowancesAmount              -> 6424.0, //LS9
      TotalTaxFreeAmount                 -> 6424.0, //LS10
      StartingRateForSavingsAmount       -> 0.0, //LS12.1
      BasicRateIncomeTax                 -> 40374.0, //lS12.2
      HigherRateIncomeTax                -> 112500.0, //LS12.3
      AdditionalRateIncomeTax            -> 596.0, //LS12.4
      OrdinaryRate                       -> 0.0, //LS13.1
      UpperRate                          -> 0.0, //LS13.2
      AdditionalRate                     -> 403.0, //LS13.3
      OtherAdjustmentsIncreasing         -> 0.0,
      OtherAdjustmentsReducing           -> 234.0,
      WelshIncomeTax                     -> 0.0, //LS20a
      TotalIncomeTax                     -> 53262.54, //LS20
      TotalIncomeTaxAndNics              -> 53394.14, //LS16
      EmployeeNicAmount                  -> 131.60, //LS14
      PayCgTaxOn                         -> 0.0, //LS19.8
      TaxableGains                       -> 0.0, //LS19.6
      AmountDueAtEntrepreneursRate       -> 0.0, //LS19.1
      AmountDueAtOrdinaryRate            -> 0.0, //LS19.2
      AmountDueRPCIHigherRate            -> 0.0, //LS19.3
      AmountDueRPCILowerRate             -> 0.0, //LS19.3b
      Adjustments                        -> 0.0, //LS19.4
      TotalCgTax                         -> 0.0,
      YourTotalTax                       -> 53394.14 //RS7
    )

    expected foreach { case (key, expectedValue) =>
      s"return the correct key $key" in new Test {
        server.stubFor(
          WireMock
            .get(urlEqualTo(odsUrl(taxYear)))
            .willReturn(ok(FileHelper.loadFile("2020-21/utr_2216360398.json")))
        )

        val result: AtsMiddleTierData = resultToAtsData(route(app, request))
        checkResult(result, key, expectedValue)
      }
    }
  }

  "HasSummary (SIT004)" must {
    val expected = Map(
      SelfEmploymentIncome               -> 0.0, // LS1a
      IncomeFromEmployment               -> 49650.0, // LS1
      StatePension                       -> 0.0, //LS2
      OtherPensionIncome                 -> 0.0, //LS3
      TaxableStateBenefits               -> 0.0, //LS4
      OtherIncome                        -> 10079.0, //LS5
      BenefitsFromEmployment             -> 0.0, //LS6
      TotalIncomeBeforeTax               -> 59729.0, //LS7 total income received
      PersonalTaxFreeAmount              -> 12500.0, //LS8.1
      MarriageAllowanceTransferredAmount -> 0.0, //LS8.2
      OtherAllowancesAmount              -> 0.0, //LS9
      TotalTaxFreeAmount                 -> 12500.0, //LS10
      StartingRateForSavingsAmount       -> 0.0, //LS12.1
      BasicRateIncomeTax                 -> 37500.0, //lS12.2
      HigherRateIncomeTax                -> 729.0, //LS12.3
      AdditionalRateIncomeTax            -> 0.0, //LS12.4
      OrdinaryRate                       -> 0.0, //LS13.1
      UpperRate                          -> 6500.0, //LS13.2
      AdditionalRate                     -> 0.0, //LS13.3
      OtherAdjustmentsIncreasing         -> 0.0,
      OtherAdjustmentsReducing           -> 0.0,
      WelshIncomeTax                     -> 3750.0, //LS20a
      TotalIncomeTax                     -> 9904.1, //LS20
      TotalIncomeTaxAndNics              -> 10035.7, //LS16
      EmployeeNicAmount                  -> 131.60, //LS14
      PayCgTaxOn                         -> 0.0, //LS19.8
      TaxableGains                       -> 0.0, //LS19.6
      AmountDueAtEntrepreneursRate       -> 0.0, //LS19.1
      AmountDueAtOrdinaryRate            -> 0.0, //LS19.2
      AmountDueRPCIHigherRate            -> 0.0, //LS19.3
      AmountDueRPCILowerRate             -> 0.0, //LS19.3b
      Adjustments                        -> 0.0, //LS19.4
      TotalCgTax                         -> 0.0,
      YourTotalTax                       -> 10035.7 //RS7
    )

    expected foreach { case (key, expectedValue) =>
      s"return the correct key $key" in new Test {
        server.stubFor(
          WireMock
            .get(urlEqualTo(odsUrl(taxYear)))
            .willReturn(ok(FileHelper.loadFile("2020-21/utr_8673565454.json")))
        )

        val result: AtsMiddleTierData = resultToAtsData(route(app, request))
        checkResult(result, key, expectedValue)
      }
    }
  }

  "HasSummary (SIT005)" must {
    val expected = Map(
      SelfEmploymentIncome               -> 0.0, // LS1a
      IncomeFromEmployment               -> 50000.0, // LS1
      StatePension                       -> 0.0, //LS2
      OtherPensionIncome                 -> 0.0, //LS3
      TaxableStateBenefits               -> 0.0, //LS4
      OtherIncome                        -> 160126.0, //LS5
      BenefitsFromEmployment             -> 0.0, //LS6
      TotalIncomeBeforeTax               -> 210126.0, //LS7 total income received
      PersonalTaxFreeAmount              -> 12500.0, //LS8.1
      MarriageAllowanceTransferredAmount -> 0.0, //LS8.2
      OtherAllowancesAmount              -> 0.0, //LS9
      TotalTaxFreeAmount                 -> 12500.0, //LS10
      StartingRateForSavingsAmount       -> 0.0, //LS12.1
      BasicRateIncomeTax                 -> 37500.0, //lS12.2
      HigherRateIncomeTax                -> 0.0, //LS12.3
      AdditionalRateIncomeTax            -> 0.0, //LS12.4
      OrdinaryRate                       -> 147874.0, //LS13.1
      UpperRate                          -> 10126.0, //LS13.2
      AdditionalRate                     -> 0.0, //LS13.3
      OtherAdjustmentsIncreasing         -> 10106.5,
      OtherAdjustmentsReducing           -> 0.0,
      WelshIncomeTax                     -> 3750.0, //LS20a
      TotalIncomeTax                     -> 31988.0, //LS20
      TotalIncomeTaxAndNics              -> 32119.6, //LS16
      EmployeeNicAmount                  -> 131.60, //LS14
      PayCgTaxOn                         -> 0.0, //LS19.8
      TaxableGains                       -> 0.0, //LS19.6
      AmountDueAtEntrepreneursRate       -> 0.0, //LS19.1
      AmountDueAtOrdinaryRate            -> 0.0, //LS19.2
      AmountDueRPCIHigherRate            -> 0.0, //LS19.3
      AmountDueRPCILowerRate             -> 0.0, //LS19.3b
      Adjustments                        -> 0.0, //LS19.4
      TotalCgTax                         -> 0.0,
      YourTotalTax                       -> 32119.6 //RS7
    )

    expected foreach { case (key, expectedValue) =>
      s"return the correct key $key" in new Test {
        server.stubFor(
          WireMock
            .get(urlEqualTo(odsUrl(taxYear)))
            .willReturn(ok(FileHelper.loadFile("2020-21/utr_6309169120.json")))
        )

        val result: AtsMiddleTierData = resultToAtsData(route(app, request))
        checkResult(result, key, expectedValue)
      }
    }
  }

  "HasSummary (SIT006)" must {
    val expected = Map(
      SelfEmploymentIncome               -> 14190.0, // LS1a
      IncomeFromEmployment               -> 31555.0, // LS1
      StatePension                       -> 0.0, //LS2
      OtherPensionIncome                 -> 0.0, //LS3
      TaxableStateBenefits               -> 0.0, //LS4
      OtherIncome                        -> 6178.0, //LS5
      BenefitsFromEmployment             -> 0.0, //LS6
      TotalIncomeBeforeTax               -> 51923.0, //LS7 total income received
      PersonalTaxFreeAmount              -> 12500.0, //LS8.1
      MarriageAllowanceTransferredAmount -> 0.0, //LS8.2
      OtherAllowancesAmount              -> 0.0, //LS9
      TotalTaxFreeAmount                 -> 12500.0, //LS10
      StartingRateForSavingsAmount       -> 0.0, //LS12.1
      BasicRateIncomeTax                 -> 0.0, //lS12.2
      HigherRateIncomeTax                -> 0.0, //LS12.3
      AdditionalRateIncomeTax            -> 0.0, //LS12.4
      OrdinaryRate                       -> 3202.0, //LS13.1
      UpperRate                          -> 298.0, //LS13.2
      AdditionalRate                     -> 0.0, //LS13.3
      OtherAdjustmentsIncreasing         -> 0.0,
      OtherAdjustmentsReducing           -> 0.0,
      WelshIncomeTax                     -> 0.0, //LS20a
      TotalIncomeTax                     -> 7328.37, //LS20
      TotalIncomeTaxAndNics              -> 7882.07, //LS16
      EmployeeNicAmount                  -> 553.7, //LS14
      PayCgTaxOn                         -> 0.0, //LS19.8
      TaxableGains                       -> 0.0, //LS19.6
      AmountDueAtEntrepreneursRate       -> 0.0, //LS19.1
      AmountDueAtOrdinaryRate            -> 0.0, //LS19.2
      AmountDueRPCIHigherRate            -> 0.0, //LS19.3
      AmountDueRPCILowerRate             -> 0.0, //LS19.3b
      Adjustments                        -> 0.0, //LS19.4
      TotalCgTax                         -> 0.0,
      YourTotalTax                       -> 7882.07 //RS7
    )

    expected foreach { case (key, expectedValue) =>
      s"return the correct key $key" in new Test {
        server.stubFor(
          WireMock
            .get(urlEqualTo(odsUrl(taxYear)))
            .willReturn(ok(FileHelper.loadFile("2020-21/utr_7362435273.json")))
        )

        val result: AtsMiddleTierData = resultToAtsData(route(app, request))
        checkResult(result, key, expectedValue)
      }
    }
  }

  "HasSummary (SIT007)" must {
    val expected = Map(
      SelfEmploymentIncome               -> 38076.0, // LS1a
      IncomeFromEmployment               -> 0.0, // LS1
      StatePension                       -> 0.0, //LS2
      OtherPensionIncome                 -> 0.0, //LS3
      TaxableStateBenefits               -> 0.0, //LS4
      OtherIncome                        -> 6139.0, //LS5
      BenefitsFromEmployment             -> 0.0, //LS6
      TotalIncomeBeforeTax               -> 44215.0, //LS7 total income received
      PersonalTaxFreeAmount              -> 12500.0, //LS8.1
      MarriageAllowanceTransferredAmount -> 0.0, //LS8.2
      OtherAllowancesAmount              -> 7374.0, //LS9
      TotalTaxFreeAmount                 -> 19874.0, //LS10
      StartingRateForSavingsAmount       -> 0.0, //LS12.1
      BasicRateIncomeTax                 -> 19562.0, //lS12.2
      HigherRateIncomeTax                -> 0.0, //LS12.3
      AdditionalRateIncomeTax            -> 0.0, //LS12.4
      OrdinaryRate                       -> 2279.0, //LS13.1
      UpperRate                          -> 0.0, //LS13.2
      AdditionalRate                     -> 0.0, //LS13.3
      OtherAdjustmentsIncreasing         -> 0.0,
      OtherAdjustmentsReducing           -> 394.0,
      WelshIncomeTax                     -> 0.0, //LS20a
      TotalIncomeTax                     -> 3689.82, //LS20
      TotalIncomeTaxAndNics              -> 5729.6, //LS16
      EmployeeNicAmount                  -> 2039.78, //LS14
      PayCgTaxOn                         -> 0.0, //LS19.8
      TaxableGains                       -> 0.0, //LS19.6
      AmountDueAtEntrepreneursRate       -> 0.0, //LS19.1
      AmountDueAtOrdinaryRate            -> 0.0, //LS19.2
      AmountDueRPCIHigherRate            -> 0.0, //LS19.3
      AmountDueRPCILowerRate             -> 0.0, //LS19.3b
      Adjustments                        -> 0.0, //LS19.4
      TotalCgTax                         -> 0.0,
      YourTotalTax                       -> 5729.6 //RS7
    )

    expected foreach { case (key, expectedValue) =>
      s"return the correct key $key" in new Test {
        server.stubFor(
          WireMock
            .get(urlEqualTo(odsUrl(taxYear)))
            .willReturn(ok(FileHelper.loadFile("2020-21/utr_6721445140.json")))
        )

        val result: AtsMiddleTierData = resultToAtsData(route(app, request))
        checkResult(result, key, expectedValue)
      }
    }
  }

  "HasSummary (SIT008)" must {
    val expected = Map(
      SelfEmploymentIncome               -> 0.0, // LS1a
      IncomeFromEmployment               -> 55750.0, // LS1
      StatePension                       -> 0.0, //LS2
      OtherPensionIncome                 -> 0.0, //LS3
      TaxableStateBenefits               -> 0.0, //LS4
      OtherIncome                        -> 0.0, //LS5
      BenefitsFromEmployment             -> 15950.0, //LS6
      TotalIncomeBeforeTax               -> 71700.0, //LS7 total income received
      PersonalTaxFreeAmount              -> 12500.0, //LS8.1
      MarriageAllowanceTransferredAmount -> 0.0, //LS8.2
      OtherAllowancesAmount              -> 5000.0, //LS9
      TotalTaxFreeAmount                 -> 17500.0, //LS10
      StartingRateForSavingsAmount       -> 0.0, //LS12.1
      BasicRateIncomeTax                 -> 0.0, //lS12.2
      HigherRateIncomeTax                -> 0.0, //LS12.3
      AdditionalRateIncomeTax            -> 0.0, //LS12.4
      OrdinaryRate                       -> 0.0, //LS13.1
      UpperRate                          -> 0.0, //LS13.2
      AdditionalRate                     -> 0.0, //LS13.3
      OtherAdjustmentsIncreasing         -> 0.0,
      OtherAdjustmentsReducing           -> 4887.0,
      WelshIncomeTax                     -> 0.0, //LS20a
      TotalIncomeTax                     -> 11001.87, //LS20
      TotalIncomeTaxAndNics              -> 11133.47, //LS16
      EmployeeNicAmount                  -> 131.60, //LS14
      PayCgTaxOn                         -> 77700.0, //LS19.8
      TaxableGains                       -> 90000.0, //LS19.6
      AmountDueAtEntrepreneursRate       -> 0.0, //LS19.1
      AmountDueAtOrdinaryRate            -> 1630.0, //LS19.2
      AmountDueRPCIHigherRate            -> 0.0, //LS19.3
      AmountDueRPCILowerRate             -> 0.0, //LS19.3b
      Adjustments                        -> 0.0, //LS19.4
      TotalCgTax                         -> 13910.0,
      YourTotalTax                       -> 25043.47 //RS7
    )

    expected foreach { case (key, expectedValue) =>
      s"return the correct key $key" in new Test {
        server.stubFor(
          WireMock
            .get(urlEqualTo(odsUrl(taxYear)))
            .willReturn(ok(FileHelper.loadFile("2020-21/utr_2839798608.json")))
        )

        val result: AtsMiddleTierData = resultToAtsData(route(app, request))
        checkResult(result, key, expectedValue)
      }
    }
  }

  "HasSummary (SIT009)" must {
    val expected = Map(
      SelfEmploymentIncome               -> 50949.0, // LS1a
      IncomeFromEmployment               -> 33254.0, // LS1
      StatePension                       -> 6200.0, //LS2
      OtherPensionIncome                 -> 0.0, //LS3
      TaxableStateBenefits               -> 0.0, //LS4
      OtherIncome                        -> 838439.0, //LS5
      BenefitsFromEmployment             -> 0.0, //LS6
      TotalIncomeBeforeTax               -> 928842.0, //LS7 total income received
      PersonalTaxFreeAmount              -> 0.0, //LS8.1
      MarriageAllowanceTransferredAmount -> 0.0, //LS8.2
      OtherAllowancesAmount              -> 1200.0, //LS9
      TotalTaxFreeAmount                 -> 1200.0, //LS10
      StartingRateForSavingsAmount       -> 0.0, //LS12.1
      BasicRateIncomeTax                 -> 37500.0, //lS12.2
      HigherRateIncomeTax                -> 61823.0, //LS12.3
      AdditionalRateIncomeTax            -> 0.0, //LS12.4
      OrdinaryRate                       -> 0.0, //LS13.1
      UpperRate                          -> 48677.0, //LS13.2
      AdditionalRate                     -> 777642.0, //LS13.3
      OtherAdjustmentsIncreasing         -> 0.0,
      OtherAdjustmentsReducing           -> 322.0,
      WelshIncomeTax                     -> 0.0, //LS20a
      TotalIncomeTax                     -> 344009.02, //LS20
      TotalIncomeTaxAndNics              -> 344140.62, //LS16
      EmployeeNicAmount                  -> 131.60, //LS14
      PayCgTaxOn                         -> 0.0, //LS19.8
      TaxableGains                       -> 0.0, //LS19.6
      AmountDueAtEntrepreneursRate       -> 0.0, //LS19.1
      AmountDueAtOrdinaryRate            -> 0.0, //LS19.2
      AmountDueRPCIHigherRate            -> 0.0, //LS19.3
      AmountDueRPCILowerRate             -> 0.0, //LS19.3b
      Adjustments                        -> 0.0, //LS19.4
      TotalCgTax                         -> 0.0,
      YourTotalTax                       -> 344140.62 //RS7
    )

    expected foreach { case (key, expectedValue) =>
      s"return the correct key $key" in new Test {
        server.stubFor(
          WireMock
            .get(urlEqualTo(odsUrl(taxYear)))
            .willReturn(ok(FileHelper.loadFile("2020-21/utr_3902670233.json")))
        )

        val result: AtsMiddleTierData = resultToAtsData(route(app, request))
        checkResult(result, key, expectedValue)
      }
    }
  }

  "HasSummary (SIT010)" must {
    val expected = Map(
      SelfEmploymentIncome               -> 4153.0, // LS1a
      IncomeFromEmployment               -> 0.0, // LS1
      StatePension                       -> 3770.0, //LS2
      OtherPensionIncome                 -> 3121.0, //LS3
      TaxableStateBenefits               -> 0.0, //LS4
      OtherIncome                        -> 12433.0, //LS5
      BenefitsFromEmployment             -> 0.0, //LS6
      TotalIncomeBeforeTax               -> 23477.0, //LS7 total income received
      PersonalTaxFreeAmount              -> 12500.0, //LS8.1
      MarriageAllowanceTransferredAmount -> 0.0, //LS8.2
      OtherAllowancesAmount              -> 0.0, //LS9
      TotalTaxFreeAmount                 -> 12500.0, //LS10
      StartingRateForSavingsAmount       -> 0.0, //LS12.1
      BasicRateIncomeTax                 -> 0.0, //lS12.2
      HigherRateIncomeTax                -> 0.0, //LS12.3
      AdditionalRateIncomeTax            -> 0.0, //LS12.4
      OrdinaryRate                       -> 2977.0, //LS13.1
      UpperRate                          -> 0.0, //LS13.2
      AdditionalRate                     -> 0.0, //LS13.3
      OtherAdjustmentsIncreasing         -> 692.0,
      OtherAdjustmentsReducing           -> 0.0,
      WelshIncomeTax                     -> 0.0, //LS20a
      TotalIncomeTax                     -> 665.27, //LS20
      TotalIncomeTaxAndNics              -> 796.87, //LS16
      EmployeeNicAmount                  -> 131.60, //LS14
      PayCgTaxOn                         -> 700.0, //LS19.8
      TaxableGains                       -> 13000.0, //LS19.6
      AmountDueAtEntrepreneursRate       -> 0.0, //LS19.1
      AmountDueAtOrdinaryRate            -> 70.0, //LS19.2
      AmountDueRPCIHigherRate            -> 0.0, //LS19.3
      AmountDueRPCILowerRate             -> 0.0, //LS19.3b
      Adjustments                        -> 0.0, //LS19.4
      TotalCgTax                         -> 70.0,
      YourTotalTax                       -> 866.87 //RS7
    )

    expected foreach { case (key, expectedValue) =>
      s"return the correct key $key" in new Test {
        server.stubFor(
          WireMock
            .get(urlEqualTo(odsUrl(taxYear)))
            .willReturn(ok(FileHelper.loadFile("2020-21/utr_9716771495.json")))
        )

        val result: AtsMiddleTierData = resultToAtsData(route(app, request))
        checkResult(result, key, expectedValue)
      }
    }
  }

  "HasSummary (SIT011)" must {
    val expected = Map(
      SelfEmploymentIncome               -> 0.0, // LS1a
      IncomeFromEmployment               -> 0.0, // LS1
      StatePension                       -> 0.0, //LS2
      OtherPensionIncome                 -> 14473.97, //LS3
      TaxableStateBenefits               -> 1757.0, //LS4
      OtherIncome                        -> 10278.0, //LS5
      BenefitsFromEmployment             -> 0.0, //LS6
      TotalIncomeBeforeTax               -> 26508.97, //LS7 total income received
      PersonalTaxFreeAmount              -> 12500.0, //LS8.1
      MarriageAllowanceTransferredAmount -> 0.0, //LS8.2
      OtherAllowancesAmount              -> 0.0, //LS9
      TotalTaxFreeAmount                 -> 12500.0, //LS10
      StartingRateForSavingsAmount       -> 0.0, //LS12.1
      BasicRateIncomeTax                 -> 6058.0, //lS12.2
      HigherRateIncomeTax                -> 0.0, //LS12.3
      AdditionalRateIncomeTax            -> 0.0, //LS12.4
      OrdinaryRate                       -> 3680.0, //LS13.1
      UpperRate                          -> 0.0, //LS13.2
      AdditionalRate                     -> 0.0, //LS13.3
      OtherAdjustmentsIncreasing         -> 70.0,
      OtherAdjustmentsReducing           -> 0.0,
      WelshIncomeTax                     -> 0.0, //LS20a
      TotalIncomeTax                     -> 1557.60, //LS20
      TotalIncomeTaxAndNics              -> 1689.2, //LS16
      EmployeeNicAmount                  -> 131.60, //LS14
      PayCgTaxOn                         -> 0.0, //LS19.8
      TaxableGains                       -> 0.0, //LS19.6
      AmountDueAtEntrepreneursRate       -> 0.0, //LS19.1
      AmountDueAtOrdinaryRate            -> 0.0, //LS19.2
      AmountDueRPCIHigherRate            -> 0.0, //LS19.3
      AmountDueRPCILowerRate             -> 0.0, //LS19.3b
      Adjustments                        -> 0.0, //LS19.4
      TotalCgTax                         -> 0.0,
      YourTotalTax                       -> 1689.20 //RS7
    )

    expected foreach { case (key, expectedValue) =>
      s"return the correct key $key" in new Test {
        server.stubFor(
          WireMock
            .get(urlEqualTo(odsUrl(taxYear)))
            .willReturn(ok(FileHelper.loadFile("2020-21/utr_8842271803.json")))
        )

        val result: AtsMiddleTierData = resultToAtsData(route(app, request))
        checkResult(result, key, expectedValue)
      }
    }
  }

  "HasSummary (SIT012)" must {
    val expected = Map(
      SelfEmploymentIncome               -> 50000.0, // LS1a
      IncomeFromEmployment               -> 0.0, // LS1
      StatePension                       -> 0.0, //LS2
      OtherPensionIncome                 -> 0.0, //LS3
      TaxableStateBenefits               -> 0.0, //LS4
      OtherIncome                        -> 0.0, //LS5
      BenefitsFromEmployment             -> 0.0, //LS6
      TotalIncomeBeforeTax               -> 50000.0, //LS7 total income received
      PersonalTaxFreeAmount              -> 12500.0, //LS8.1
      MarriageAllowanceTransferredAmount -> 0.0, //LS8.2
      OtherAllowancesAmount              -> 0.0, //LS9
      TotalTaxFreeAmount                 -> 12500.0, //LS10
      StartingRateForSavingsAmount       -> 0.0, //LS12.1
      BasicRateIncomeTax                 -> 9000.0, //lS12.2
      HigherRateIncomeTax                -> 0.0, //LS12.3
      AdditionalRateIncomeTax            -> 0.0, //LS12.4
      OrdinaryRate                       -> 20500.0, //LS13.1
      UpperRate                          -> 0.0, //LS13.2
      AdditionalRate                     -> 0.0, //LS13.3
      OtherAdjustmentsIncreasing         -> 0.0,
      OtherAdjustmentsReducing           -> 0.0,
      WelshIncomeTax                     -> 0.0, //LS20a
      TotalIncomeTax                     -> 3337.5, //LS20
      TotalIncomeTaxAndNics              -> 3469.1, //LS16
      EmployeeNicAmount                  -> 131.60, //LS14
      PayCgTaxOn                         -> 0.0, //LS19.8
      TaxableGains                       -> 0.0, //LS19.6
      AmountDueAtEntrepreneursRate       -> 0.0, //LS19.1
      AmountDueAtOrdinaryRate            -> 0.0, //LS19.2
      AmountDueRPCIHigherRate            -> 0.0, //LS19.3
      AmountDueRPCILowerRate             -> 0.0, //LS19.3b
      Adjustments                        -> 0.0, //LS19.4
      TotalCgTax                         -> 0.0,
      YourTotalTax                       -> 3469.1 //RS7
    )

    expected foreach { case (key, expectedValue) =>
      s"return the correct key $key" in new Test {
        server.stubFor(
          WireMock
            .get(urlEqualTo(odsUrl(taxYear)))
            .willReturn(ok(FileHelper.loadFile("2020-21/utr_7268957390.json")))
        )

        val result: AtsMiddleTierData = resultToAtsData(route(app, request))
        checkResult(result, key, expectedValue)
      }
    }
  }

  "HasSummary (SIT014)" must {
    val expected = Map(
      SelfEmploymentIncome               -> 4153.0, // LS1a
      IncomeFromEmployment               -> 0.0, // LS1
      StatePension                       -> 0.0, //LS2
      OtherPensionIncome                 -> 0.0, //LS3
      TaxableStateBenefits               -> 0.0, //LS4
      OtherIncome                        -> 14349.0, //LS5
      BenefitsFromEmployment             -> 0.0, //LS6
      TotalIncomeBeforeTax               -> 18502.0, //LS7 total income received
      PersonalTaxFreeAmount              -> 12500.0, //LS8.1
      MarriageAllowanceTransferredAmount -> 0.0, //LS8.2
      OtherAllowancesAmount              -> 0.0, //LS9
      TotalTaxFreeAmount                 -> 12500.0, //LS10
      StartingRateForSavingsAmount       -> 0.0, //LS12.1
      BasicRateIncomeTax                 -> 0.0, //lS12.2
      HigherRateIncomeTax                -> 0.0, //LS12.3
      AdditionalRateIncomeTax            -> 0.0, //LS12.4
      OrdinaryRate                       -> 0.0, //LS13.1
      UpperRate                          -> 0.0, //LS13.2
      AdditionalRate                     -> 0.0, //LS13.3
      OtherAdjustmentsIncreasing         -> 600.0,
      OtherAdjustmentsReducing           -> 0.0,
      WelshIncomeTax                     -> 0.0, //LS20a
      TotalIncomeTax                     -> 600.0, //LS20
      TotalIncomeTaxAndNics              -> 731.6, //LS16
      EmployeeNicAmount                  -> 131.60, //LS14
      PayCgTaxOn                         -> 0.0, //LS19.8
      TaxableGains                       -> 0.0, //LS19.6
      AmountDueAtEntrepreneursRate       -> 0.0, //LS19.1
      AmountDueAtOrdinaryRate            -> 0.0, //LS19.2
      AmountDueRPCIHigherRate            -> 0.0, //LS19.3
      AmountDueRPCILowerRate             -> 0.0, //LS19.3b
      Adjustments                        -> 0.0, //LS19.4
      TotalCgTax                         -> 0.0,
      YourTotalTax                       -> 731.6 //RS7
    )

    expected foreach { case (key, expectedValue) =>
      s"return the correct key $key" in new Test {
        server.stubFor(
          WireMock
            .get(urlEqualTo(odsUrl(taxYear)))
            .willReturn(ok(FileHelper.loadFile("2020-21/utr_9290899941.json")))
        )

        val result: AtsMiddleTierData = resultToAtsData(route(app, request))
        checkResult(result, key, expectedValue)
      }
    }
  }

  "HasSummary (SIT015)" must {
    val expected = Map(
      SelfEmploymentIncome               -> 69500.0, // LS1a
      IncomeFromEmployment               -> 0.0, // LS1
      StatePension                       -> 0.0, //LS2
      OtherPensionIncome                 -> 0.0, //LS3
      TaxableStateBenefits               -> 0.0, //LS4
      OtherIncome                        -> 44000.0, //LS5
      BenefitsFromEmployment             -> 0.0, //LS6
      TotalIncomeBeforeTax               -> 113500.0, //LS7 total income received
      PersonalTaxFreeAmount              -> 12500.0, //LS8.1
      MarriageAllowanceTransferredAmount -> 0.0, //LS8.2
      OtherAllowancesAmount              -> 52370.0, //LS9
      TotalTaxFreeAmount                 -> 64870.0, //LS10
      StartingRateForSavingsAmount       -> 0.0, //LS12.1
      BasicRateIncomeTax                 -> 27760.0, //lS12.2
      HigherRateIncomeTax                -> 0.0, //LS12.3
      AdditionalRateIncomeTax            -> 0.0, //LS12.4
      OrdinaryRate                       -> 6870.0, //LS13.1
      UpperRate                          -> 11130.0, //LS13.2
      AdditionalRate                     -> 0.0, //LS13.3
      OtherAdjustmentsIncreasing         -> 0.0,
      OtherAdjustmentsReducing           -> 0.0,
      WelshIncomeTax                     -> 463.0, //LS20a
      TotalIncomeTax                     -> 9684.5, //LS20
      TotalIncomeTaxAndNics              -> 13929.22, //LS16
      EmployeeNicAmount                  -> 4244.72, //LS14
      PayCgTaxOn                         -> 0.0, //LS19.8
      TaxableGains                       -> 0.0, //LS19.6
      AmountDueAtEntrepreneursRate       -> 0.0, //LS19.1
      AmountDueAtOrdinaryRate            -> 0.0, //LS19.2
      AmountDueRPCIHigherRate            -> 0.0, //LS19.3
      AmountDueRPCILowerRate             -> 0.0, //LS19.3b
      Adjustments                        -> 0.0, //LS19.4
      TotalCgTax                         -> 0.0,
      YourTotalTax                       -> 13929.22 //RS7
    )

    expected foreach { case (key, expectedValue) =>
      s"return the correct key $key" in new Test {
        server.stubFor(
          WireMock
            .get(urlEqualTo(odsUrl(taxYear)))
            .willReturn(ok(FileHelper.loadFile("2020-21/utr_9223146705.json")))
        )

        val result: AtsMiddleTierData = resultToAtsData(route(app, request))
        checkResult(result, key, expectedValue)
      }
    }
  }

  "HasSummary (SIT016)" must {
    val expected = Map(
      SelfEmploymentIncome               -> 0.0, // LS1a
      IncomeFromEmployment               -> 58104.0, // LS1
      StatePension                       -> 0.0, //LS2
      OtherPensionIncome                 -> 0.0, //LS3
      TaxableStateBenefits               -> 0.0, //LS4
      OtherIncome                        -> 32374.0, //LS5
      BenefitsFromEmployment             -> 0.0, //LS6
      TotalIncomeBeforeTax               -> 90478.0, //LS7 total income received
      PersonalTaxFreeAmount              -> 12500.0, //LS8.1
      MarriageAllowanceTransferredAmount -> 0.0, //LS8.2
      OtherAllowancesAmount              -> 4383.0, //LS9
      TotalTaxFreeAmount                 -> 16883.0, //LS10
      StartingRateForSavingsAmount       -> 0.0, //LS12.1
      BasicRateIncomeTax                 -> 37500.0, //lS12.2
      HigherRateIncomeTax                -> 35595.0, //LS12.3
      AdditionalRateIncomeTax            -> 0.0, //LS12.4
      OrdinaryRate                       -> 0.0, //LS13.1
      UpperRate                          -> 0.0, //LS13.2
      AdditionalRate                     -> 0.0, //LS13.3
      OtherAdjustmentsIncreasing         -> 11499.4,
      OtherAdjustmentsReducing           -> 3727.0,
      WelshIncomeTax                     -> 0.0, //LS20a
      TotalIncomeTax                     -> 29511.34, //LS20
      TotalIncomeTaxAndNics              -> 29642.94, //LS16
      EmployeeNicAmount                  -> 131.60, //LS14
      PayCgTaxOn                         -> 0.0, //LS19.8
      TaxableGains                       -> 0.0, //LS19.6
      AmountDueAtEntrepreneursRate       -> 0.0, //LS19.1
      AmountDueAtOrdinaryRate            -> 0.0, //LS19.2
      AmountDueRPCIHigherRate            -> 0.0, //LS19.3
      AmountDueRPCILowerRate             -> 0.0, //LS19.3b
      Adjustments                        -> 0.0, //LS19.4
      TotalCgTax                         -> 0.0,
      YourTotalTax                       -> 29642.94 //RS7
    )

    expected foreach { case (key, expectedValue) =>
      s"return the correct key $key" in new Test {
        server.stubFor(
          WireMock
            .get(urlEqualTo(odsUrl(taxYear)))
            .willReturn(ok(FileHelper.loadFile("2020-21/utr_7957650973.json")))
        )

        val result: AtsMiddleTierData = resultToAtsData(route(app, request))
        checkResult(result, key, expectedValue)
      }
    }
  }

  "HasSummary (SIT017)" must {
    val expected = Map(
      SelfEmploymentIncome               -> 0.0, // LS1a
      IncomeFromEmployment               -> 0.0, // LS1
      StatePension                       -> 0.0, //LS2
      OtherPensionIncome                 -> 0.0, //LS3
      TaxableStateBenefits               -> 0.0, //LS4
      OtherIncome                        -> 172491.0, //LS5
      BenefitsFromEmployment             -> 0.0, //LS6
      TotalIncomeBeforeTax               -> 172491.0, //LS7 total income received
      PersonalTaxFreeAmount              -> 0.0, //LS8.1
      MarriageAllowanceTransferredAmount -> 0.0, //LS8.2
      OtherAllowancesAmount              -> 4541.0, //LS9
      TotalTaxFreeAmount                 -> 4541.0, //LS10
      StartingRateForSavingsAmount       -> 0.0, //LS12.1
      BasicRateIncomeTax                 -> 37500.0, //lS12.2
      HigherRateIncomeTax                -> 112500.0, //LS12.3
      AdditionalRateIncomeTax            -> 17822.0, //LS12.4
      OrdinaryRate                       -> 0.0, //LS13.1
      UpperRate                          -> 0.0, //LS13.2
      AdditionalRate                     -> 0.0, //LS13.3
      OtherAdjustmentsIncreasing         -> 0.0,
      OtherAdjustmentsReducing           -> 9775.0,
      WelshIncomeTax                     -> 0.0, //LS20a
      TotalIncomeTax                     -> 50744.9, //LS20
      TotalIncomeTaxAndNics              -> 50876.5, //LS16
      EmployeeNicAmount                  -> 131.60, //LS14
      PayCgTaxOn                         -> 0.0, //LS19.8
      TaxableGains                       -> 0.0, //LS19.6
      AmountDueAtEntrepreneursRate       -> 0.0, //LS19.1
      AmountDueAtOrdinaryRate            -> 0.0, //LS19.2
      AmountDueRPCIHigherRate            -> 0.0, //LS19.3
      AmountDueRPCILowerRate             -> 0.0, //LS19.3b
      Adjustments                        -> 0.0, //LS19.4
      TotalCgTax                         -> 0.0,
      YourTotalTax                       -> 50876.5 //RS7
    )

    expected foreach { case (key, expectedValue) =>
      s"return the correct key $key" in new Test {
        server.stubFor(
          WireMock
            .get(urlEqualTo(odsUrl(taxYear)))
            .willReturn(ok(FileHelper.loadFile("2020-21/utr_1023584560.json")))
        )

        val result: AtsMiddleTierData = resultToAtsData(route(app, request))
        checkResult(result, key, expectedValue)
      }
    }
  }

  "HasSummary (SIT018)" must {
    val expected = Map(
      SelfEmploymentIncome               -> 0.0, // LS1a
      IncomeFromEmployment               -> 56500.0, // LS1
      StatePension                       -> 0.0, //LS2
      OtherPensionIncome                 -> 0.0, //LS3
      TaxableStateBenefits               -> 0.0, //LS4
      OtherIncome                        -> 0.0, //LS5
      BenefitsFromEmployment             -> 0.0, //LS6
      TotalIncomeBeforeTax               -> 56500.0, //LS7 total income received
      PersonalTaxFreeAmount              -> 12500.0, //LS8.1
      MarriageAllowanceTransferredAmount -> 0.0, //LS8.2
      OtherAllowancesAmount              -> 0.0, //LS9
      TotalTaxFreeAmount                 -> 12500.0, //LS10
      StartingRateForSavingsAmount       -> 0.0, //LS12.1
      BasicRateIncomeTax                 -> 37500.0, //lS12.2
      HigherRateIncomeTax                -> 6500.0, //LS12.3
      AdditionalRateIncomeTax            -> 0.0, //LS12.4
      OrdinaryRate                       -> 0.0, //LS13.1
      UpperRate                          -> 0.0, //LS13.2
      AdditionalRate                     -> 0.0, //LS13.3
      OtherAdjustmentsIncreasing         -> 20000.0,
      OtherAdjustmentsReducing           -> 0.0,
      WelshIncomeTax                     -> 0.0, //LS20a
      TotalIncomeTax                     -> 30100.0, //LS20
      TotalIncomeTaxAndNics              -> 30231.6, //LS16
      EmployeeNicAmount                  -> 131.60, //LS14
      PayCgTaxOn                         -> 0.0, //LS19.8
      TaxableGains                       -> 0.0, //LS19.6
      AmountDueAtEntrepreneursRate       -> 0.0, //LS19.1
      AmountDueAtOrdinaryRate            -> 0.0, //LS19.2
      AmountDueRPCIHigherRate            -> 0.0, //LS19.3
      AmountDueRPCILowerRate             -> 0.0, //LS19.3b
      Adjustments                        -> 0.0, //LS19.4
      TotalCgTax                         -> 0.0,
      YourTotalTax                       -> 30231.6 //RS7
    )

    expected foreach { case (key, expectedValue) =>
      s"return the correct key $key" in new Test {
        server.stubFor(
          WireMock
            .get(urlEqualTo(odsUrl(taxYear)))
            .willReturn(ok(FileHelper.loadFile("2020-21/utr_7741497270.json")))
        )

        val result: AtsMiddleTierData = resultToAtsData(route(app, request))
        checkResult(result, key, expectedValue)
      }
    }
  }

  "HasSummary (SIT019)" must {
    val expected = Map(
      SelfEmploymentIncome               -> 0.0, // LS1a
      IncomeFromEmployment               -> 38500.0, // LS1
      StatePension                       -> 0.0, //LS2
      OtherPensionIncome                 -> 0.0, //LS3
      TaxableStateBenefits               -> 0.0, //LS4
      OtherIncome                        -> 0.0, //LS5
      BenefitsFromEmployment             -> 0.0, //LS6
      TotalIncomeBeforeTax               -> 38500.0, //LS7 total income received
      PersonalTaxFreeAmount              -> 12500.0, //LS8.1
      MarriageAllowanceTransferredAmount -> 0.0, //LS8.2
      OtherAllowancesAmount              -> 0.0, //LS9
      TotalTaxFreeAmount                 -> 12500.0, //LS10
      StartingRateForSavingsAmount       -> 0.0, //LS12.1
      BasicRateIncomeTax                 -> 26000.0, //lS12.2
      HigherRateIncomeTax                -> 0.0, //LS12.3
      AdditionalRateIncomeTax            -> 0.0, //LS12.4
      OrdinaryRate                       -> 0.0, //LS13.1
      UpperRate                          -> 0.0, //LS13.2
      AdditionalRate                     -> 0.0, //LS13.3
      OtherAdjustmentsIncreasing         -> 0.0,
      OtherAdjustmentsReducing           -> 0.0,
      WelshIncomeTax                     -> 0.0, //LS20a
      TotalIncomeTax                     -> 5200.0, //LS20
      TotalIncomeTaxAndNics              -> 5331.6, //LS16
      EmployeeNicAmount                  -> 131.60, //LS14
      PayCgTaxOn                         -> 0.0, //LS19.8
      TaxableGains                       -> 0.0, //LS19.6
      AmountDueAtEntrepreneursRate       -> 0.0, //LS19.1
      AmountDueAtOrdinaryRate            -> 0.0, //LS19.2
      AmountDueRPCIHigherRate            -> 0.0, //LS19.3
      AmountDueRPCILowerRate             -> 0.0, //LS19.3b
      Adjustments                        -> 0.0, //LS19.4
      TotalCgTax                         -> 0.0,
      YourTotalTax                       -> 5331.6 //RS7
    )

    expected foreach { case (key, expectedValue) =>
      s"return the correct key $key" in new Test {
        server.stubFor(
          WireMock
            .get(urlEqualTo(odsUrl(taxYear)))
            .willReturn(ok(FileHelper.loadFile("2020-21/utr_6180195454.json")))
        )

        val result: AtsMiddleTierData = resultToAtsData(route(app, request))
        checkResult(result, key, expectedValue)
      }
    }
  }

  "return NOT_FOUND when ODS returns NOT_FOUND response" in new Test {
    server.stubFor(
      WireMock
        .get(urlEqualTo(odsUrl(taxYear)))
        .willReturn(aResponse().withStatus(NOT_FOUND))
    )

    val result: Option[Future[Result]] = route(app, request)
    result.map(status) mustBe Some(NOT_FOUND)
  }

  "return an exception when ODS returns an empty ok" in new Test {
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
