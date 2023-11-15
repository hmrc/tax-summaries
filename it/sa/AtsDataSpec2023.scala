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

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.{ok, urlEqualTo}
import models.LiabilityKey._
import models._
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.GoodsAndServices._
import utils.FileHelper

class AtsDataSpec2023 extends SaTestHelper {

  val taxPayerFile = "taxPayerDetails.json"

  trait Test {
    val taxYear = 2023

    def odsUrl(taxYear: Int): String = s"/self-assessment/individuals/" + utr + s"/annual-tax-summaries/$taxYear"

    def apiUrl(taxYear: Int): String = s"/taxs/$utr/$taxYear/ats-data"

    def request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, apiUrl(taxYear))
      .withHeaders((AUTHORIZATION, "Bearer 123"))
  }

  "HasSummary (SIT001) - Test Case 1 (TC52)" must {
    val expected = Map(
      SelfEmploymentIncome               -> 942.00, // LS1a e
      IncomeFromEmployment               -> 122500.00, // LS1 e
      StatePension                       -> 3770.00, //LS2 e
      OtherPensionIncome                 -> 3121.00, //LS3 e Excel
      TaxableStateBenefits               -> 0.00, //LS4 e
      OtherIncome                        -> 300663.00, //LS5 e
      BenefitsFromEmployment             -> 9600.00, //LS6 e
      TotalIncomeBeforeTax               -> 440596.00, //LS7 total income received e Excel
      PersonalTaxFreeAmount              -> 0.00, //LS8.1 e
      MarriageAllowanceTransferredAmount -> 0.00, //LS8.2 e
      OtherAllowancesAmount              -> 15451.00, //LS9 e
      TotalTaxFreeAmount                 -> 15451.00, //LS10 e
      StartingRateForSavings             -> 0.00, //LS12.1
      StartingRateForSavingsAmount       -> 0.00, //LS12.1 e
      BasicRateIncomeTax                 -> 45218.00, //lS12.2 e
      BasicRateIncomeTaxAmount           -> 9043.60, // LS12.2 (tax amount - right column)
      HigherRateIncomeTax                -> 112300.00, //LS12.3 e
      HigherRateIncomeTaxAmount          -> 44920.0, // LS12.3 (tax amount - right column)
      AdditionalRateIncomeTax            -> 206995.00, //LS12.4 e
      AdditionalRateIncomeTaxAmount      -> 93147.75, // LS12.4 (tax amount - right column)
      OrdinaryRate                       -> 0.00, //LS13.1 e
      OrdinaryRateAmount                 -> 0.0, // LS13.1 (tax amount - right column)
      UpperRate                          -> 0.00, //LS13.2 e
      UpperRateAmount                    -> 0.0, // LS13.2 (tax amount - right column)
      AdditionalRate                     -> 58632.00, //LS13.3 e,
      AdditionalRateAmount               -> 23071.69, // LS13.3 (tax amount - right column)
      OtherAdjustmentsIncreasing         -> 0.00, //LS15a e
      OtherAdjustmentsReducing           -> 5163.00, //LS15b e
      WelshIncomeTax                     -> 0.00, //LS20a
      TotalIncomeTax                     -> 165020.74, //LS20 e
      TotalIncomeTaxAndNics              -> 165184.54, //LS16 e
      EmployeeNicAmount                  -> 163.80, //LS14 e
      PayCgTaxOn                         -> 28700.00, //LS19.8 e // We are calculating by totalling up atsCgTotGainsAfterLosses & atsCgGainsAfterLossesAmt but value in data sheet is in cap3AssessableChgeableGain
      TaxableGains                       -> 41000.00, //LS19.6 e
      AmountDueAtEntrepreneursRate       -> 1200.00, //LS19.1 e
      AmountAtEntrepreneursRate          -> 12000.00, //LS19.1 e
      AmountDueAtOrdinaryRate            -> 0.00, //LS19.2 e
      AmountDueRPCIHigherRate            -> 3340.00, //LS19.3 e Excel
      AmountDueRPCILowerRate             -> 0.00, //LS19.3a e
      Adjustments                        -> 0.00, //LS19.4 e
      TotalCgTax                         -> 4540.00, //e
      YourTotalTax                       -> 169724.54, //RS7 e
      ScottishIncomeTax                  -> 0.0,
      ScottishStarterRateTax             -> 0.0, // LS12.5	Scottish Starter rate
      ScottishBasicRateTax               -> 0.0, // LS12.6	Scottish Basic rate
      ScottishIntermediateRateTax        -> 0.0, // LS12.7	Intermediate rate
      ScottishHigherRateTax              -> 0.0, // LS12.8	Scottish Higher rate ?
      ScottishAdditionalRateTax          -> 0.0, // LS12.9	Scottish Top rate ?
      ScottishTotalTax                   -> 0.0, // LS12a	Total Scottish Income Tax ?
      ScottishStarterIncome              -> 0.0, // Starter rate	£12,571 to £14,667	19%
      ScottishBasicIncome                -> 0.0,
      ScottishIntermediateIncome         -> 0.0,
      ScottishHigherIncome               -> 0.0,
      ScottishAdditionalIncome           -> 0.0,
      SavingsLowerRateTax                -> 0.0, // LS12b.1	Basic rate Income Tax
      SavingsHigherRateTax               -> 0.0, // LS12b.2	Higher rate Income Tax
      SavingsAdditionalRateTax           -> 0.0 // LS12b.3 Additional rate Income Tax
    )

    expected foreach { case (key, expectedValue) =>
      s"return the correct key $key" in new Test {
        server.stubFor(
          WireMock
            .get(urlEqualTo(odsUrl(taxYear)))
            .willReturn(ok(FileHelper.loadFile("2022-23/TestCase1.json")))
        )

        val result: AtsMiddleTierData = resultToAtsData(route(app, request))
        checkResult(result, key, expectedValue)
      }
    }
  }

  "HasSummary (SIT002) - Test Case 2 (TC53)" must {
    val expected = Map(
      SelfEmploymentIncome               -> 5570.00, // LS1a e
      IncomeFromEmployment               -> 8000.00, // LS1 e
      StatePension                       -> 0.00, //LS2 e
      OtherPensionIncome                 -> 0.00, //LS3 e
      TaxableStateBenefits               -> 0.00, //LS4 e
      OtherIncome                        -> 38728.00, //LS5 e
      BenefitsFromEmployment             -> 0.00, //LS6 e
      TotalIncomeBeforeTax               -> 52298.00, //LS7 total income received e
      PersonalTaxFreeAmount              -> 12570.00, //LS8.1 e
      MarriageAllowanceTransferredAmount -> 0.00, //LS8.2 e
      OtherAllowancesAmount              -> 0.00, //LS9 e
      TotalTaxFreeAmount                 -> 12570.00, //LS10 e
      StartingRateForSavings             -> 5000.00, //LS12.1
      StartingRateForSavingsAmount       -> 0.00, //LS12.1 e
      BasicRateIncomeTax                 -> 61.00, //lS12.2 e
      BasicRateIncomeTaxAmount           -> 12.20, // LS12.2 (tax amount - right column)
      HigherRateIncomeTax                -> 2028.00, //LS12.3 e
      HigherRateIncomeTaxAmount          -> 811.20, // LS12.3 (tax amount - right column)
      AdditionalRateIncomeTax            -> 0.00, //LS12.4 e
      AdditionalRateIncomeTaxAmount      -> 0.00, // LS12.4 (tax amount - right column)
      OrdinaryRate                       -> 30139.00, //LS13.1 e
      OrdinaryRateAmount                 -> 2637.16, // LS13.1 (tax amount - right column)
      UpperRate                          -> 0.00, //LS13.2 e
      UpperRateAmount                    -> 0.0, // LS13.2 (tax amount - right column)
      AdditionalRate                     -> 0.00, //LS13.3 e,
      AdditionalRateAmount               -> 0.00, // LS13.3 (tax amount - right column)
      OtherAdjustmentsIncreasing         -> 0.00, //LS15a e
      OtherAdjustmentsReducing           -> 0.00, //LS15b e
      WelshIncomeTax                     -> 0.00, //LS20a
      TotalIncomeTax                     -> 3460.56, //LS20 e
      TotalIncomeTaxAndNics              -> 3460.56, //LS16 e
      EmployeeNicAmount                  -> 0.00, //LS14 e
      PayCgTaxOn                         -> 0.00, //LS19.8 e
      TaxableGains                       -> 0.00, //LS19.6 e
      AmountDueAtEntrepreneursRate       -> 0.00, //LS19.1 e
      AmountAtEntrepreneursRate          -> 0.00, //LS19.1 e
      AmountDueAtOrdinaryRate            -> 0.00, //LS19.2 e
      AmountDueRPCIHigherRate            -> 0.00, //LS19.3 e
      AmountDueRPCILowerRate             -> 0.00, //LS19.3a e
      Adjustments                        -> 0.00, //LS19.4 e
      TotalCgTax                         -> 0.00, //e
      YourTotalTax                       -> 3460.56, //RS7 e
      ScottishIncomeTax                  -> 0.0,
      ScottishStarterRateTax             -> 0.0, // LS12.5	Scottish Starter rate
      ScottishBasicRateTax               -> 0.0, // LS12.6	Scottish Basic rate
      ScottishIntermediateRateTax        -> 0.0, // LS12.7	Intermediate rate
      ScottishHigherRateTax              -> 0.0, // LS12.8	Scottish Higher rate ?
      ScottishAdditionalRateTax          -> 0.0, // LS12.9	Scottish Top rate ?
      ScottishTotalTax                   -> 0.0, // LS12a	Total Scottish Income Tax ?
      ScottishStarterIncome              -> 0.0, // Starter rate	£12,571 to £14,667	19%
      ScottishBasicIncome                -> 0.0,
      ScottishIntermediateIncome         -> 0.0,
      ScottishHigherIncome               -> 0.0,
      ScottishAdditionalIncome           -> 0.0,
      SavingsLowerRateTax                -> 0.0, // LS12b.1	Basic rate Income Tax
      SavingsHigherRateTax               -> 0.0, // LS12b.2	Higher rate Income Tax
      SavingsAdditionalRateTax           -> 0.0 // LS12b.3 Additional rate Income Tax
    )

    expected foreach { case (key, expectedValue) =>
      s"return the correct key $key" in new Test {
        server.stubFor(
          WireMock
            .get(urlEqualTo(odsUrl(taxYear)))
            .willReturn(ok(FileHelper.loadFile("2022-23/TestCase2.json")))
        )

        val result: AtsMiddleTierData = resultToAtsData(route(app, request))
        checkResult(result, key, expectedValue)
      }
    }
  }

  "HasSummary (SIT003) - Test Case 3 (TC45)" must {
    val expected = Map(
      SelfEmploymentIncome               -> 0.00, // LS1a e
      IncomeFromEmployment               -> 0.00, // LS1 e
      StatePension                       -> 0.00, //LS2 e
      OtherPensionIncome                 -> 0.00, //LS3 e
      TaxableStateBenefits               -> 0.00, //LS4 e
      OtherIncome                        -> 102811.00, //LS5 e
      BenefitsFromEmployment             -> 0.00, //LS6 e
      TotalIncomeBeforeTax               -> 102811.00, //LS7 total income received e
      PersonalTaxFreeAmount              -> 12510.00, //LS8.1 e
      MarriageAllowanceTransferredAmount -> 0.00, //LS8.2 e
      OtherAllowancesAmount              -> 2691.00, //LS9 e
      TotalTaxFreeAmount                 -> 15201.00, //LS10 e
      StartingRateForSavings             -> 0.00, //LS12.1
      StartingRateForSavingsAmount       -> 0.00, //LS12.1 e
      BasicRateIncomeTax                 -> 35925.00, //lS12.2 e
      BasicRateIncomeTaxAmount           -> 7185.00, // LS12.2 (tax amount - right column)
      HigherRateIncomeTax                -> 49910.00, //LS12.3 e
      HigherRateIncomeTaxAmount          -> 19964.00, // LS12.3 (tax amount - right column)
      AdditionalRateIncomeTax            -> 0.00, //LS12.4 e
      AdditionalRateIncomeTaxAmount      -> 0.00, // LS12.4 (tax amount - right column)
      OrdinaryRate                       -> 0.00, //LS13.1 e
      OrdinaryRateAmount                 -> 0.00, // LS13.1 (tax amount - right column)
      UpperRate                          -> 0.00, //LS13.2 e
      UpperRateAmount                    -> 0.0, // LS13.2 (tax amount - right column)
      AdditionalRate                     -> 0.00, //LS13.3 e,
      AdditionalRateAmount               -> 0.00, // LS13.3 (tax amount - right column)
      OtherAdjustmentsIncreasing         -> 0.00, //LS15a e
      OtherAdjustmentsReducing           -> 23473.00, //LS15b e <-- actually giving 23468
      WelshIncomeTax                     -> 0.00, //LS20a
      TotalIncomeTax                     -> 3676.00, //LS20 e
      TotalIncomeTaxAndNics              -> 3676.00, //LS16 e
      EmployeeNicAmount                  -> 0.00, //LS14 e
      PayCgTaxOn                         -> 0.00, //LS19.8 e
      TaxableGains                       -> 0.00, //LS19.6 e
      AmountDueAtEntrepreneursRate       -> 0.00, //LS19.1 e
      AmountAtEntrepreneursRate          -> 0.00, //LS19.1 e
      AmountDueAtOrdinaryRate            -> 0.00, //LS19.2 e
      AmountDueRPCIHigherRate            -> 0.00, //LS19.3 e
      AmountDueRPCILowerRate             -> 0.00, //LS19.3a e
      Adjustments                        -> 0.00, //LS19.4 e
      TotalCgTax                         -> 0.00, //e
      YourTotalTax                       -> 3676.00, //RS7 e
      ScottishIncomeTax                  -> 0.0,
      ScottishStarterRateTax             -> 0.0, // LS12.5	Scottish Starter rate
      ScottishBasicRateTax               -> 0.0, // LS12.6	Scottish Basic rate
      ScottishIntermediateRateTax        -> 0.0, // LS12.7	Intermediate rate
      ScottishHigherRateTax              -> 0.0, // LS12.8	Scottish Higher rate ?
      ScottishAdditionalRateTax          -> 0.0, // LS12.9	Scottish Top rate ?
      ScottishTotalTax                   -> 0.0, // LS12a	Total Scottish Income Tax ?
      ScottishStarterIncome              -> 0.0, // Starter rate	£12,571 to £14,667	19%
      ScottishBasicIncome                -> 0.0,
      ScottishIntermediateIncome         -> 0.0,
      ScottishHigherIncome               -> 0.0,
      ScottishAdditionalIncome           -> 0.0,
      SavingsLowerRateTax                -> 0.0, // LS12b.1	Basic rate Income Tax
      SavingsHigherRateTax               -> 0.0, // LS12b.2	Higher rate Income Tax
      SavingsAdditionalRateTax           -> 0.0 // LS12b.3 Additional rate Income Tax
    )

    expected foreach { case (key, expectedValue) =>
      s"return the correct key $key" in new Test {
        server.stubFor(
          WireMock
            .get(urlEqualTo(odsUrl(taxYear)))
            .willReturn(ok(FileHelper.loadFile("2022-23/TestCase3.json")))
        )

        val result: AtsMiddleTierData = resultToAtsData(route(app, request))
        checkResult(result, key, expectedValue)
      }
    }
  }

  "HasSummary (SIT004) - Test Case 4 (TC153)" must {
    val expected: Map[LiabilityKey, Double] = Map(
      SelfEmploymentIncome               -> 157719.00, // LS1a e
      IncomeFromEmployment               -> 15000.00, // LS1 e
      StatePension                       -> 0.00, //LS2 e
      OtherPensionIncome                 -> 0.00, //LS3 e
      TaxableStateBenefits               -> 0.00, //LS4 e
      OtherIncome                        -> 16963.00, //LS5 e
      BenefitsFromEmployment             -> 0.00, //LS6 e
      TotalIncomeBeforeTax               -> 189682.00, //LS7 total income received e
      PersonalTaxFreeAmount              -> 0.00, //LS8.1 e
      MarriageAllowanceTransferredAmount -> 0.00, //LS8.2 e
      OtherAllowancesAmount              -> 33151.00, //LS9 e
      TotalTaxFreeAmount                 -> 33151.00, //LS10 e
      StartingRateForSavings             -> 0.00, //LS12.1
      StartingRateForSavingsAmount       -> 0.00, //LS12.1 e
      OrdinaryRate                       -> 0.00, //LS13.1 e
      OrdinaryRateAmount                 -> 0.00, // LS13.1 (tax amount - right column)
      UpperRate                          -> 3233.00, //LS13.2 e
      UpperRateAmount                    -> 1091.13, // LS13.2 (tax amount - right column)
      AdditionalRate                     -> 0.00, //LS13.3 e,
      AdditionalRateAmount               -> 0.00, // LS13.3 (tax amount - right column)
      OtherAdjustmentsIncreasing         -> 0.00, //LS15a e
      OtherAdjustmentsReducing           -> 0.00, //LS15b e
      WelshIncomeTax                     -> 0.00, //LS20a
      TotalIncomeTax                     -> 56528.70, //LS20 e excel
      TotalIncomeTaxAndNics              -> 62297.72, //LS16 e excel
      EmployeeNicAmount                  -> 5769.02, //LS14 e
      PayCgTaxOn                         -> 34000.00, //LS19.8 e
      TaxableGains                       -> 46300.00, //LS19.6 e
      AmountDueAtEntrepreneursRate       -> 0.00, //LS19.1 e
      AmountAtEntrepreneursRate          -> 0.00, //LS19.1 e
      AmountDueAtOrdinaryRate            -> 0.00, //LS19.2 e
      AmountDueAtHigherRate              -> 9520, //LS19.3 e
      AmountDueRPCILowerRate             -> 0.00, //LS19.3a e
      Adjustments                        -> 0.00, //LS19.4 e
      TotalCgTax                         -> 9520.00, //e
      YourTotalTax                       -> 71817.72, //RS7 e Excel
      ScottishIncomeTax                  -> 0.0,
      ScottishStarterRateTax             -> 410.78, // LS12.5	Scottish Starter rate
      ScottishBasicRateTax               -> 2551.20, // LS12.6	Scottish Basic rate
      ScottishIntermediateRateTax        -> 3774.54, // LS12.7	Intermediate rate
      ScottishHigherRateTax              -> 43246.39, // LS12.8	Scottish Higher rate ?
      ScottishAdditionalRateTax          -> 2176.26, // LS12.9	Scottish Top rate ?
      ScottishTotalTax                   -> 52159.17, // LS12a	Total Scottish Income Tax ?
      ScottishHigherIncome               -> 43246.39 / 0.41,
      ScottishAdditionalIncome           -> 2176.26 / 0.46,
      SavingsLowerRateTax                -> 0.0, // LS12b.1	Basic rate Income Tax
      SavingsHigherRateTax               -> 3278.40, // LS12b.2	Higher rate Income Tax
      SavingsAdditionalRateTax           -> 0.0
    )

    expected foreach { case (key, expectedValue) =>
      s"return the correct key $key" in new Test {
        server.stubFor(
          WireMock
            .get(urlEqualTo(odsUrl(taxYear)))
            .willReturn(ok(FileHelper.loadFile("2022-23/TestCase4.json")))
        )

        val result: AtsMiddleTierData = resultToAtsData(route(app, request))
        checkResult(result, key, expectedValue)
      }

    }
  }

  "HasSummary (SIT005) - Test Case 5 (TC222)" must {
    val expected = Map(
      SelfEmploymentIncome               -> 0.00, // LS1a e
      IncomeFromEmployment               -> 149243.00, // LS1 e
      StatePension                       -> 0.00, //LS2 e
      OtherPensionIncome                 -> 0.00, //LS3 e
      TaxableStateBenefits               -> 0.00, //LS4 e
      OtherIncome                        -> 12000.00, //LS5 e
      BenefitsFromEmployment             -> 0.00, //LS6 e
      TotalIncomeBeforeTax               -> 161243.00, //LS7 total income received e
      PersonalTaxFreeAmount              -> 0.00, //LS8.1 e
      MarriageAllowanceTransferredAmount -> 0.00, //LS8.2 e
      OtherAllowancesAmount              -> 0.00, //LS9 e
      TotalTaxFreeAmount                 -> 0.00, //LS10 e
      StartingRateForSavings             -> 0.00, //LS12.1
      StartingRateForSavingsAmount       -> 0.00, //LS12.1 e
      BasicRateIncomeTax                 -> 25700.00, //lS12.2 e
      BasicRateIncomeTaxAmount           -> 5140.00, // LS12.2 (tax amount - right column)
      HigherRateIncomeTax                -> 112300.00, //LS12.3 e
      HigherRateIncomeTaxAmount          -> 44920.00, // LS12.3 (tax amount - right column)
      AdditionalRateIncomeTax            -> 11243.00, //LS12.4 e
      AdditionalRateIncomeTaxAmount      -> 5059.35, // LS12.4 (tax amount - right column)
      OrdinaryRate                       -> 10000.00, //LS13.1 e
      OrdinaryRateAmount                 -> 875.00, // LS13.1 (tax amount - right column)
      UpperRate                          -> 0.00, //LS13.2 e
      UpperRateAmount                    -> 0.0, // LS13.2 (tax amount - right column)
      AdditionalRate                     -> 0.00, //LS13.3 e,
      AdditionalRateAmount               -> 0.00, // LS13.3 (tax amount - right column)
      OtherAdjustmentsIncreasing         -> 0.00, //LS15a e
      OtherAdjustmentsReducing           -> 0.00, //LS15b e
      TotalIncomeTax                     -> 55994.35, //LS20 e
      TotalIncomeTaxAndNics              -> 55994.35, //LS16 e
      EmployeeNicAmount                  -> 0.00, //LS14 e
      PayCgTaxOn                         -> 0.00, //LS19.8 e
      TaxableGains                       -> 0.00, //LS19.6 e
      AmountDueAtEntrepreneursRate       -> 0.00, //LS19.1 e
      AmountAtEntrepreneursRate          -> 0.00, //LS19.1 e
      AmountDueAtOrdinaryRate            -> 0.00, //LS19.2 e
      AmountDueRPCIHigherRate            -> 0.00, //LS19.3 e
      Adjustments                        -> 0.00, //LS19.4 e
      TotalCgTax                         -> 0.00, //e
      YourTotalTax                       -> 55994.35, //RS7 e
      ScottishIncomeTax                  -> 0.0,
      WelshIncomeTax                     -> 14924.3,
      ScottishStarterRateTax             -> 0.0, // LS12.5	Scottish Starter rate
      ScottishBasicRateTax               -> 0.0, // LS12.6	Scottish Basic rate
      ScottishIntermediateRateTax        -> 0.0, // LS12.7	Intermediate rate
      ScottishHigherRateTax              -> 0.0, // LS12.8	Scottish Higher rate ?
      ScottishAdditionalRateTax          -> 0.0, // LS12.9	Scottish Top rate ?
      ScottishTotalTax                   -> 0.0, // LS12a	Total Scottish Income Tax ?
      ScottishStarterIncome              -> 0.0, // Starter rate	£12,571 to £14,667	19%
      ScottishBasicIncome                -> 0.0,
      ScottishIntermediateIncome         -> 0.0,
      ScottishHigherIncome               -> 0.0,
      ScottishAdditionalIncome           -> 0.0,
      SavingsLowerRateTax                -> 0.0, // LS12b.1	Basic rate Income Tax
      SavingsHigherRateTax               -> 0.0, // LS12b.2	Higher rate Income Tax
      SavingsAdditionalRateTax           -> 0.0 // LS12b.3 Additional rate Income Tax
    )

    expected foreach { case (key, expectedValue) =>
      s"return the correct key $key" in new Test {
        server.stubFor(
          WireMock
            .get(urlEqualTo(odsUrl(taxYear)))
            .willReturn(ok(FileHelper.loadFile("2022-23/TestCase5.json")))
        )

        val result: AtsMiddleTierData = resultToAtsData(route(app, request))
        checkResult(result, key, expectedValue)
      }
    }
  }

  "HasSummary (SIT006) - Test Case 6 (TC3)" must {
    val expected = Map(
      SelfEmploymentIncome               -> 14190.00, // LS1a e
      IncomeFromEmployment               -> 31717.00, // LS1 e
      StatePension                       -> 0.00, //LS2 e
      OtherPensionIncome                 -> 0.00, //LS3 e
      TaxableStateBenefits               -> 0.00, //LS4 e
      OtherIncome                        -> 6178.00, //LS5 e
      BenefitsFromEmployment             -> 0.00, //LS6 e
      TotalIncomeBeforeTax               -> 52085.00, //LS7 total income received e
      PersonalTaxFreeAmount              -> 12570.00, //LS8.1 e
      MarriageAllowanceTransferredAmount -> 0.00, //LS8.2 e
      OtherAllowancesAmount              -> 0.00, //LS9 e
      TotalTaxFreeAmount                 -> 12570.00, //LS10 e
      StartingRateForSavings             -> 0.00, //LS12.1
      StartingRateForSavingsAmount       -> 0.00, //LS12.1 e
      BasicRateIncomeTax                 -> 0.00, //lS12.2 e
      BasicRateIncomeTaxAmount           -> 0.00, // LS12.2 (tax amount - right column)
      HigherRateIncomeTax                -> 0.00, //LS12.3 e
      HigherRateIncomeTaxAmount          -> 0.00, // LS12.3 (tax amount - right column)
      AdditionalRateIncomeTax            -> 0.00, //LS12.4 e
      AdditionalRateIncomeTaxAmount      -> 0.00, // LS12.4 (tax amount - right column)
      OrdinaryRate                       -> 3310.00, //LS13.1 e
      OrdinaryRateAmount                 -> 289.62, // LS13.1 (tax amount - right column)
      UpperRate                          -> 190.00, //LS13.2 e
      UpperRateAmount                    -> 64.12, // LS13.2 (tax amount - right column)
      AdditionalRate                     -> 0.00, //LS13.3 e,
      AdditionalRateAmount               -> 0.00, // LS13.3 (tax amount - right column)
      OtherAdjustmentsIncreasing         -> 0.00, //LS15a e
      OtherAdjustmentsReducing           -> 0.00, //LS15b e
      TotalIncomeTax                     -> 7345.06, //LS20 e
      TotalIncomeTaxAndNics              -> 7730.89, //LS16 e
      EmployeeNicAmount                  -> 385.83, //LS14 e
      PayCgTaxOn                         -> 0.00, //LS19.8 e
      TaxableGains                       -> 0.00, //LS19.6 e
      AmountDueAtEntrepreneursRate       -> 0.00, //LS19.1 e
      AmountAtEntrepreneursRate          -> 0.00, //LS19.1 e
      AmountDueAtOrdinaryRate            -> 0.00, //LS19.2 e
      AmountDueRPCIHigherRate            -> 0.00, //LS19.3 e
      Adjustments                        -> 0.00, //LS19.4 e
      TotalCgTax                         -> 0.00, //e
      YourTotalTax                       -> 7730.89, //RS7 e
      ScottishIncomeTax                  -> 0.0,
      WelshIncomeTax                     -> 0.0,
      ScottishStarterRateTax             -> 410.78, // LS12.5	Scottish Starter rate
      ScottishBasicRateTax               -> 2516.20, // LS12.6	Scottish Basic rate
      ScottishIntermediateRateTax        -> 3774.54, // LS12.7	Intermediate rate <<< LEFT OFF HERE!!!
      ScottishHigherRateTax              -> 254.20, // LS12.8	Scottish Higher rate ?
      ScottishAdditionalRateTax          -> 0.0, // LS12.9	Scottish Top rate ?
      ScottishTotalTax                   -> 6955.72, // LS12a	Total Scottish Income Tax ?
      ScottishStarterIncome              -> 2162.00, // Starter rate	£12,571 to £14,667	19%
      ScottishBasicIncome                -> 12581.00,
      ScottishIntermediateIncome         -> 17974.00,
      ScottishHigherIncome               -> 620.0,
      ScottishAdditionalIncome           -> 0.0,
      SavingsLowerRateTax                -> 35.60, // LS12b.1	Basic rate Income Tax
      SavingsHigherRateTax               -> 0.0, // LS12b.2	Higher rate Income Tax
      SavingsAdditionalRateTax           -> 0.0 // LS12b.3 Additional rate Income Tax
    )

    expected foreach { case (key, expectedValue) =>
      s"return the correct key $key" in new Test {
        server.stubFor(
          WireMock
            .get(urlEqualTo(odsUrl(taxYear)))
            .willReturn(ok(FileHelper.loadFile("2022-23/TestCase6.json")))
        )

        val result: AtsMiddleTierData = resultToAtsData(route(app, request))
        checkResult(result, key, expectedValue)
      }
    }
  }

  "HasSummary (SIT007) - Test Case 7 (TC6)" must {

    val expected = Map(
      SelfEmploymentIncome               -> 0.00, // LS1a e
      IncomeFromEmployment               -> 0.00, // LS1 e
      StatePension                       -> 6198.00, //LS2 e Wrong in excel
      OtherPensionIncome                 -> 12302.00, //LS3 e Wrong in excel
      TaxableStateBenefits               -> 0.00, //LS4 e
      OtherIncome                        -> 36505.00, //LS5 e
      BenefitsFromEmployment             -> 0.00, //LS6 e
      TotalIncomeBeforeTax               -> 55005.00, //LS7 total income received e Excel
      PersonalTaxFreeAmount              -> 12570.00, //LS8.1 e
      MarriageAllowanceTransferredAmount -> 0.00, //LS8.2 e
      OtherAllowancesAmount              -> 0.00, //LS9 e
      TotalTaxFreeAmount                 -> 12570.00, //LS10 e
      StartingRateForSavings             -> 0.00, //LS12.1
      StartingRateForSavingsAmount       -> 0.00, //LS12.1 e
      BasicRateIncomeTax                 -> 37200.00, //lS12.2 e
      BasicRateIncomeTaxAmount           -> 7440.00, // LS12.2 (tax amount - right column)
      HigherRateIncomeTax                -> 0.00, //LS12.3 e
      HigherRateIncomeTaxAmount          -> 0.00, // LS12.3 (tax amount - right column)
      AdditionalRateIncomeTax            -> 0.00, //LS12.4 e
      AdditionalRateIncomeTaxAmount      -> 0.00, // LS12.4 (tax amount - right column)
      OrdinaryRate                       -> 0.00, //LS13.1 e
      OrdinaryRateAmount                 -> 0.00, // LS13.1 (tax amount - right column)
      UpperRate                          -> 2735.00, //LS13.2 e
      UpperRateAmount                    -> 923.06, // LS13.2 (tax amount - right column)
      AdditionalRate                     -> 0.00, //LS13.3 e,
      AdditionalRateAmount               -> 0.00, // LS13.3 (tax amount - right column)
      OtherAdjustmentsIncreasing         -> 0.00, //LS15a e
      OtherAdjustmentsReducing           -> 0.00, //LS15b e
      WelshIncomeTax                     -> 755.00, //LS20a
      TotalIncomeTax                     -> 8363.06, //LS20 e
      TotalIncomeTaxAndNics              -> 8363.06, //LS16 e
      EmployeeNicAmount                  -> 0.00, //LS14 e
      PayCgTaxOn                         -> 0.00, //LS19.8 e
      TaxableGains                       -> 0.00, //LS19.6 e
      AmountDueAtEntrepreneursRate       -> 0.00, //LS19.1 e
      AmountAtEntrepreneursRate          -> 0.00, //LS19.1 e
      AmountDueAtOrdinaryRate            -> 0.00, //LS19.2 e
      AmountDueRPCIHigherRate            -> 0.00, //LS19.3 e
      Adjustments                        -> 0.00, //LS19.4 e
      TotalCgTax                         -> 0.00, //e
      YourTotalTax                       -> 8363.06, //RS7 e
      ScottishIncomeTax                  -> 0.0,
      ScottishStarterRateTax             -> 0.0, // LS12.5	Scottish Starter rate
      ScottishBasicRateTax               -> 0.0, // LS12.6	Scottish Basic rate
      ScottishIntermediateRateTax        -> 0.0, // LS12.7	Intermediate rate
      ScottishHigherRateTax              -> 0.0, // LS12.8	Scottish Higher rate ?
      ScottishAdditionalRateTax          -> 0.0, // LS12.9	Scottish Top rate ?
      ScottishTotalTax                   -> 0.0, // LS12a	Total Scottish Income Tax ?
      ScottishStarterIncome              -> 0.0, // Starter rate	£12,571 to £14,667	19%
      ScottishBasicIncome                -> 0.0,
      ScottishIntermediateIncome         -> 0.0,
      ScottishHigherIncome               -> 0.0,
      ScottishAdditionalIncome           -> 0.0,
      SavingsLowerRateTax                -> 0.0, // LS12b.1	Basic rate Income Tax
      SavingsHigherRateTax               -> 0.0, // LS12b.2	Higher rate Income Tax
      SavingsAdditionalRateTax           -> 0.0 // LS12b.3 Additional rate Income Tax
    )

    expected foreach { case (key, expectedValue) =>
      s"return the correct key $key" in new Test {
        server.stubFor(
          WireMock
            .get(urlEqualTo(odsUrl(taxYear)))
            .willReturn(ok(FileHelper.loadFile("2022-23/TestCase7.json")))
        )

        val result: AtsMiddleTierData = resultToAtsData(route(app, request))
        checkResult(result, key, expectedValue)
      }
    }
  }

  "HasSummary (SIT008) - Test Case 8 (TC51)" must {
    val expected = Map(
      // Your Taxable income
      SelfEmploymentIncome   -> 64284.00, // LS1a Self Employment Income
      IncomeFromEmployment   -> 0.00, // LS1 Total income from employment
      StatePension           -> 0.00, //LS2 State pension
      OtherPensionIncome     -> 0.00, //LS3 Other pension income
      TaxableStateBenefits   -> 0.00, //LS4 Taxable state benefits
      OtherIncome            -> 80305.00, //LS5 Other income
      BenefitsFromEmployment -> 0.00, //LS6 Benefits from employment
      TotalIncomeBeforeTax   -> 144589.00, //LS7 Your income before tax

      // Tax Free Amount
      PersonalTaxFreeAmount              -> 0.0, //LS8.1 tax free amount
      MarriageAllowanceTransferredAmount -> 0.00, //LS8.2 Marriage Allowance transferred
      OtherAllowancesAmount              -> 13186.00, //LS9 Other allowances, deducations and expenses
      TotalTaxFreeAmount                 -> 13186.0, //LS10 Less your total tax free amount
      // LS11 You pay tax on

      // Income Tax - UK
      StartingRateForSavings             -> 0.00, //LS12.1 Starting rate for savings (income)
      StartingRateForSavingsAmount       -> 0.00, //LS12.1 Starting rate for savings (tax amount - right column)
      BasicRateIncomeTax                 -> 37700.00, //lS12.2 Basic rate Income Tax (income)
      BasicRateIncomeTaxAmount           -> 7540.00, // LS12.2 Basic rate Income Tax (tax amount - right column)
      HigherRateIncomeTax                -> 69635.00, //LS12.3 Higher rate Income Tax (income)
      HigherRateIncomeTaxAmount          -> 27854.00, // LS12.3 Higher rate Income Tax (tax amount - right column)
      AdditionalRateIncomeTax            -> 0.00, //LS12.4 Additional rate Income Tax (income)
      AdditionalRateIncomeTaxAmount      -> 0.00, // LS12.4 Additional rate Income Tax (tax amount - right column)

      // Dividends
      OrdinaryRate       -> 0.00, //LS13.1 Ordinary Rate (income)
      OrdinaryRateAmount -> 0.00, // LS13.1 Ordinary Rate (tax amount - right column)
      UpperRate          -> 21568.00, //LS13.2 Upper Rate (income)
      UpperRateAmount    -> 7279.20, // LS13.2 Upper Rate (tax amount - right column)
      AdditionalRate     -> 0.00, //LS13.3 Additional Rate (income)

      // Adjustments
      OtherAdjustmentsIncreasing -> 117.40, //LS15a Other adjustments that increase your Income Tax
      OtherAdjustmentsReducing   -> 5332.00, //LS15b Less other adjustments that reduce your Income Tax

      // Income Tax - Welsh
      WelshIncomeTax        -> 0.00, //LS20a
      TotalIncomeTax        -> 37458.60, //LS20 Total Income Tax
      TotalIncomeTaxAndNics -> 41641.90, //LS16 Total Income Tax and NICs
      EmployeeNicAmount     -> 4183.30, //LS14	National Insurance Contributions (NICs)

      // Capital Gains
      PayCgTaxOn                   -> 0.00, //LS19.8 You pay tax on
      TaxableGains                 -> 0.00, //LS19.6 Your Taxable Gains
      AmountDueAtEntrepreneursRate -> 0.00, //LS19.1 Entrepreneurs' relief rate ??
      AmountAtEntrepreneursRate    -> 0.00, //LS19.1 Entrepreneurs' relief rate ??
      AmountDueAtOrdinaryRate      -> 0.00, //LS19.2 Ordinary Rate
      AmountDueRPCIHigherRate      -> 0.00, //LS19.3 Upper Rate
      Adjustments                  -> 0.00, //LS19.4 Adjustment to Capital Gains Tax
      TotalCgTax                   -> 0.00, // Total Capital Gains Tax
      YourTotalTax                 -> 41641.90, //RS7 Your Total Income Tax, Capital Gains Tax and NICs
      SavingsAdditionalRateTax     -> 0.0 // LS12b.3 Additional rate Income Tax
    )

    expected foreach { case (key, expectedValue) =>
      s"return the correct key $key" in new Test {
        server.stubFor(
          WireMock
            .get(urlEqualTo(odsUrl(taxYear)))
            .willReturn(ok(FileHelper.loadFile("2022-23/TestCase8.json")))
        )

        val result: AtsMiddleTierData = resultToAtsData(route(app, request))
        checkResult(result, key, expectedValue)
      }
    }
  }

  "HasSummary (TCWithNulls)" must {
    val expected = Map(
      // Your Taxable income
      SelfEmploymentIncome   -> 0.00, // LS1a Self Employment Income
      IncomeFromEmployment   -> 149243.00, // LS1 Total income from employment
      StatePension           -> 0.00, //LS2 State pension
      OtherPensionIncome     -> 0.00, //LS3 Other pension income
      TaxableStateBenefits   -> 0.00, //LS4 Taxable state benefits
      OtherIncome            -> 12000.00, //LS5 Other income
      BenefitsFromEmployment -> 0.00, //LS6 Benefits from employment
      TotalIncomeBeforeTax   -> 161243.00, //LS7 Your income before tax

      // Tax Free Amount
      PersonalTaxFreeAmount              -> 0.0, //LS8.1 tax free amount
      MarriageAllowanceTransferredAmount -> 0.00, //LS8.2 Marriage Allowance transferred
      OtherAllowancesAmount              -> 0.00, //LS9 Other allowances, deducations and expenses
      TotalTaxFreeAmount                 -> 0.0, //LS10 Less your total tax free amount
      // LS11 You pay tax on

      // Income Tax - UK
      StartingRateForSavings             -> 0.00, //LS12.1 Starting rate for savings (income)
      StartingRateForSavingsAmount       -> 0.00, //LS12.1 Starting rate for savings (tax amount - right column)
      BasicRateIncomeTax                 -> 20493.0, //lS12.2 Basic rate Income Tax (income)
      BasicRateIncomeTaxAmount           -> 4098.6, // LS12.2 Basic rate Income Tax (tax amount - right column)
      HigherRateIncomeTax                -> 0.00, //LS12.3 Higher rate Income Tax (income)
      HigherRateIncomeTaxAmount          -> 0.00, // LS12.3 Higher rate Income Tax (tax amount - right column)
      AdditionalRateIncomeTax            -> 0.00, //LS12.4 Additional rate Income Tax (income)
      AdditionalRateIncomeTaxAmount      -> 0.00, // LS12.4 Additional rate Income Tax (tax amount - right column)

      // Dividends
      OrdinaryRate                 -> 10000.00, //LS13.1 Ordinary Rate (income)
      OrdinaryRateAmount           -> 750.0, // LS13.1 Ordinary Rate (tax amount - right column)
      UpperRate                    -> 0.00, //LS13.2 Upper Rate (income)
      UpperRateAmount              -> 0.00, // LS13.2 Upper Rate (tax amount - right column)
      AdditionalRate               -> 0.00, //LS13.3 Additional Rate (income)
      AdditionalRateAmount         -> 0.00, // LS13.3 Additional Rate (tax amount - right column)
      // LS13a Total UK Income Tax

      // Adjustments
      OtherAdjustmentsIncreasing   -> 0.00, //LS15a Other adjustments that increase your Income Tax
      OtherAdjustmentsReducing     -> 0.00, //LS15b Less other adjustments that reduce your Income Tax
      // LS15aa	Marriage Allowance received that reduces your income tax

      // Income Tax - Welsh
      WelshIncomeTax               -> 0.00, //LS20a
      TotalIncomeTax               -> 4848.6, //LS20 Total Income Tax
      TotalIncomeTaxAndNics        -> 4848.6, //LS16 Total Income Tax and NICs
      EmployeeNicAmount            -> 0.00, //LS14	National Insurance Contributions (NICs)
      // LS17/RS5		Your income after tax and NICs
      //LS18	National Insurance Contributions

      // Capital Gains
      PayCgTaxOn                   -> 0.00, //LS19.8 You pay tax on
      TaxableGains                 -> 0.00, //LS19.6 Your Taxable Gains
      AmountDueAtEntrepreneursRate -> 0.00, //LS19.1 Entrepreneurs' relief rate ??
      AmountAtEntrepreneursRate    -> 0.00, //LS19.1 Entrepreneurs' relief rate ??
      AmountDueAtOrdinaryRate      -> 0.00, //LS19.2 Ordinary Rate
      AmountDueRPCIHigherRate      -> 0.00, //LS19.3 Upper Rate
      Adjustments                  -> 0.00, //LS19.4 Adjustment to Capital Gains Tax
      TotalCgTax                   -> 0.00, // Total Capital Gains Tax

      //RS7 Your Total Income Tax, Capital Gains Tax and NICs
      YourTotalTax                -> 4848.6,
      // Income Tax - Scottish
      ScottishIncomeTax           -> 0.0,
      ScottishStarterRateTax      -> 0.0, // LS12.5	Scottish Starter rate
      ScottishBasicRateTax        -> 0.0, // LS12.6	Scottish Basic rate
      ScottishIntermediateRateTax -> 0.0, // LS12.7	Intermediate rate
      ScottishHigherRateTax       -> 0.0, // LS12.8	Scottish Higher rate
      ScottishAdditionalRateTax   -> 0.0, // LS12.9	Scottish Top rate
      ScottishTotalTax            -> 0.00, // LS12a	Total Scottish Income Tax
      ScottishStarterIncome       -> 0.0, // LS12.5 Starter rate income
      ScottishBasicIncome         -> 0.0, // LS12.6	Scottish Basic rate income
      ScottishIntermediateIncome  -> 0.0, // LS12.7	Intermediate rate income
      ScottishHigherIncome        -> 0.0, // LS12.8	Scottish Higher rate income
      ScottishAdditionalIncome    -> 0.0, // LS12.9	Scottish Top rate income
      SavingsLowerRateTax         -> 0.0, // LS12b.1	Basic rate Income Tax
      SavingsHigherRateTax        -> 0.0, // LS12b.2	Higher rate Income Tax
      SavingsAdditionalRateTax    -> 0.0 // LS12b.3 Additional rate Income Tax
    )

    expected foreach { case (key, expectedValue) =>
      s"return the correct key $key" in new Test {
        server.stubFor(
          WireMock
            .get(urlEqualTo(odsUrl(taxYear)))
            .willReturn(ok(FileHelper.loadFile("2022-23/TestCaseWithNulls.json")))
        )

        val result: AtsMiddleTierData = resultToAtsData(route(app, request))
        checkResult(result, key, expectedValue)
      }
    }
  }

  "return correct government spending" in new Test {

    val expectedValue: GovernmentSpendingOutputWrapper =
      GovernmentSpendingOutputWrapper(
        2023,
        Map(
          PublicOrderAndSafety       -> SpendData(Amount(261.46, "GBP", None), 4.1),
          BusinessAndIndustry        -> SpendData(Amount(484.65, "GBP", None), 7.6),
          OutstandingPaymentsToTheEU -> SpendData(Amount(38.26, "GBP", None), 0.6),
          NationalDebtInterest       -> SpendData(Amount(765.24, "GBP", None), 12),
          Defence                    -> SpendData(Amount(331.6, "GBP", None), 5.2),
          Health                     -> SpendData(Amount(1262.65, "GBP", None), 19.8),
          Culture                    -> SpendData(Amount(82.9, "GBP", None), 1.3),
          HousingAndUtilities        -> SpendData(Amount(108.41, "GBP", None), 1.7),
          GovernmentAdministration   -> SpendData(Amount(127.54, "GBP", None), 2),
          Environment                -> SpendData(Amount(82.9, "GBP", None), 1.3),
          OverseasAid                -> SpendData(Amount(31.89, "GBP", None), 0.5),
          Transport                  -> SpendData(Amount(261.46, "GBP", None), 4.1),
          Welfare                    -> SpendData(Amount(1249.89, "GBP", None), 19.6),
          Education                  -> SpendData(Amount(631.32, "GBP", None), 9.9),
          StatePensions              -> SpendData(Amount(656.83, "GBP", None), 10.3)
        ),
        Amount(6377, "GBP", None),
        None
      )

    override val taxYear = 2023

    server.stubFor(
      WireMock
        .get(urlEqualTo(odsUrl(taxYear)))
        .willReturn(
          ok(FileHelper.loadFile("2022-23/TestCaseGovernmentSpend.json"))
        )
    )

    val result: AtsMiddleTierData = resultToAtsData(route(app, request))
    result.gov_spending.get mustBe expectedValue
  }

}
