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
      SelfEmploymentIncome               -> 942.00, // LS1a e
      IncomeFromEmployment               -> 122500.00, // LS1 e
      StatePension                       -> 3770.00, //LS2 e
      OtherPensionIncome                 -> 3121.00, //LS3 e Excel
      TaxableStateBenefits               -> 0.00, //LS4 e
      OtherIncome                        -> 298111.00, //LS5 e
      BenefitsFromEmployment             -> 9600.00, //LS6 e
      TotalIncomeBeforeTax               -> 438044.00, //LS7 total income received e Excel
      PersonalTaxFreeAmount              -> 0.00, //LS8.1 e
      MarriageAllowanceTransferredAmount -> 0.00, //LS8.2 e
      OtherAllowancesAmount              -> 15209.00, //LS9 e
      TotalTaxFreeAmount                 -> 15209.00, //LS10 e
      StartingRateForSavings             -> 0.00, //LS12.1
      StartingRateForSavingsAmount       -> 0.00, //LS12.1 e
      BasicRateIncomeTax                 -> 45218.00, //lS12.2 e
      BasicRateIncomeTaxAmount           -> 9043.60, // LS12.2 (tax amount - right column)
      HigherRateIncomeTax                -> 112300.00, //LS12.3 e
      HigherRateIncomeTaxAmount          -> 44920.0, // LS12.3 (tax amount - right column)
      AdditionalRateIncomeTax            -> 205433.00, //LS12.4 e
      AdditionalRateIncomeTaxAmount      -> 92444.85, // LS12.4 (tax amount - right column)
      OrdinaryRate                       -> 0.00, //LS13.1 e
      OrdinaryRateAmount                 -> 0.0, // LS13.1 (tax amount - right column)
      UpperRate                          -> 0.00, //LS13.2 e
      UpperRateAmount                    -> 0.0, // LS13.2 (tax amount - right column)
      AdditionalRate                     -> 57884.00, //LS13.3 e,
      AdditionalRateAmount               -> 22053.80, // LS13.3 (tax amount - right column)
      OtherAdjustmentsIncreasing         -> 0.00, //LS15a e
      OtherAdjustmentsReducing           -> 5130.00, //LS15b e
      WelshIncomeTax                     -> 0.00, //LS20a
      TotalIncomeTax                     -> 163332.65, //LS20 e
      TotalIncomeTaxAndNics              -> 163491.25, //LS16 e
      EmployeeNicAmount                  -> 158.60, //LS14 e
      PayCgTaxOn                         -> 28700.00, //LS19.8 e
      TaxableGains                       -> 41000.00, //LS19.6 e
      AmountDueAtEntrepreneursRate       -> 1200.00, //LS19.1 e
      AmountAtEntrepreneursRate          -> 12000.00, //LS19.1 e
      AmountDueAtOrdinaryRate            -> 0.00, //LS19.2 e
      AmountDueRPCIHigherRate            -> 3340.00, //LS19.3 e Excel
      AmountDueRPCILowerRate             -> 0.00, //LS19.3a e
      Adjustments                        -> 0.00, //LS19.4 e
      TotalCgTax                         -> 4540.00, //e
      YourTotalTax                       -> 168031.25, //RS7 e
      ScottishIncomeTax                  -> 0.0,
      WelshIncomeTax                     -> 0.0,
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
            .willReturn(ok(FileHelper.loadFile("2021-22/TC52.json")))
        )

        val result: AtsMiddleTierData = resultToAtsData(route(app, request))
        checkResult(result, key, expectedValue)
      }
    }
  }

  "HasSummary (SIT002)" must {
    val expected = Map(
      SelfEmploymentIncome               -> 6945.00, // LS1a e
      IncomeFromEmployment               -> 8000.00, // LS1 e
      StatePension                       -> 0.00, //LS2 e
      OtherPensionIncome                 -> 0.00, //LS3 e
      TaxableStateBenefits               -> 0.00, //LS4 e
      OtherIncome                        -> 38728.00, //LS5 e
      BenefitsFromEmployment             -> 0.00, //LS6 e
      TotalIncomeBeforeTax               -> 53673.00, //LS7 total income received e
      PersonalTaxFreeAmount              -> 12570.00, //LS8.1 e
      MarriageAllowanceTransferredAmount -> 0.00, //LS8.2 e
      OtherAllowancesAmount              -> 0.00, //LS9 e
      TotalTaxFreeAmount                 -> 12570.00, //LS10 e
      StartingRateForSavings             -> 5000.00, //LS12.1
      StartingRateForSavingsAmount       -> 0.00, //LS12.1 e
      BasicRateIncomeTax                 -> 61.00, //lS12.2 e
      BasicRateIncomeTaxAmount           -> 12.20, // LS12.2 (tax amount - right column)
      HigherRateIncomeTax                -> 3403.00, //LS12.3 e
      HigherRateIncomeTaxAmount          -> 1361.20, // LS12.3 (tax amount - right column)
      AdditionalRateIncomeTax            -> 0.00, //LS12.4 e
      AdditionalRateIncomeTaxAmount      -> 0.00, // LS12.4 (tax amount - right column)
      OrdinaryRate                       -> 30139.00, //LS13.1 e
      OrdinaryRateAmount                 -> 2260.42, // LS13.1 (tax amount - right column)
      UpperRate                          -> 0.00, //LS13.2 e
      UpperRateAmount                    -> 0.0, // LS13.2 (tax amount - right column)
      AdditionalRate                     -> 0.00, //LS13.3 e,
      AdditionalRateAmount               -> 0.00, // LS13.3 (tax amount - right column)
      OtherAdjustmentsIncreasing         -> 0.00, //LS15a e
      OtherAdjustmentsReducing           -> 0.00, //LS15b e
      WelshIncomeTax                     -> 0.00, //LS20a
      TotalIncomeTax                     -> 3633.82, //LS20 e
      TotalIncomeTaxAndNics              -> 3792.42, //LS16 e
      EmployeeNicAmount                  -> 158.60, //LS14 e
      PayCgTaxOn                         -> 0.00, //LS19.8 e
      TaxableGains                       -> 0.00, //LS19.6 e
      AmountDueAtEntrepreneursRate       -> 0.00, //LS19.1 e
      AmountAtEntrepreneursRate          -> 0.00, //LS19.1 e
      AmountDueAtOrdinaryRate            -> 0.00, //LS19.2 e
      AmountDueRPCIHigherRate            -> 0.00, //LS19.3 e
      AmountDueRPCILowerRate             -> 0.00, //LS19.3a e
      Adjustments                        -> 0.00, //LS19.4 e
      TotalCgTax                         -> 0.00, //e
      YourTotalTax                       -> 3792.42, //RS7 e
      ScottishIncomeTax                  -> 0.0,
      WelshIncomeTax                     -> 0.0,
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
            .willReturn(ok(FileHelper.loadFile("2021-22/TC53.json")))
        )

        val result: AtsMiddleTierData = resultToAtsData(route(app, request))
        checkResult(result, key, expectedValue)
      }
    }
  }

  "HasSummary (SIT003)" must {
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
      OtherAdjustmentsReducing           -> 23468.00, //LS15b e
      WelshIncomeTax                     -> 0.00, //LS20a
      TotalIncomeTax                     -> 3681.50, //LS20 e
      TotalIncomeTaxAndNics              -> 3681.50, //LS16 e
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
      YourTotalTax                       -> 3681.50, //RS7 e
      ScottishIncomeTax                  -> 0.0,
      WelshIncomeTax                     -> 0.0,
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
            .willReturn(ok(FileHelper.loadFile("2021-22/TC45.json")))
        )

        val result: AtsMiddleTierData = resultToAtsData(route(app, request))
        checkResult(result, key, expectedValue)
      }
    }
  }

  "HasSummary (SIT004)" must {
    val expected = Map(
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
      UpperRateAmount                    -> 1050.72, // LS13.2 (tax amount - right column)
      AdditionalRate                     -> 0.00, //LS13.3 e,
      AdditionalRateAmount               -> 0.00, // LS13.3 (tax amount - right column)
      OtherAdjustmentsIncreasing         -> 0.00, //LS15a e
      OtherAdjustmentsReducing           -> 0.00, //LS15b e
      WelshIncomeTax                     -> 0.00, //LS20a
      TotalIncomeTax                     -> 56492.86, //LS20 e excel
      TotalIncomeTaxAndNics              -> 61668.70, //LS16 e excel
      EmployeeNicAmount                  -> 5175.84, //LS14 e
      PayCgTaxOn                         -> 34000.00, //LS19.8 e
      TaxableGains                       -> 46300.00, //LS19.6 e
      AmountDueAtEntrepreneursRate       -> 0.00, //LS19.1 e
      AmountAtEntrepreneursRate          -> 0.00, //LS19.1 e
      AmountDueAtOrdinaryRate            -> 0.00, //LS19.2 e
      AmountDueRPCIHigherRate            -> 9520.00, //LS19.3 e
      AmountDueRPCILowerRate             -> 0.00, //LS19.3a e
      Adjustments                        -> 0.00, //LS19.4 e
      TotalCgTax                         -> 9520.00, //e
      YourTotalTax                       -> 71188.70, //RS7 e Excel
      ScottishIncomeTax                  -> 0.0,
      WelshIncomeTax                     -> 0.0,
      ScottishStarterRateTax             -> 398.43, // LS12.5	Scottish Starter rate
      ScottishBasicRateTax               -> 2485.8, // LS12.6	Scottish Basic rate
      ScottishIntermediateRateTax        -> 3856.86, // LS12.7	Intermediate rate
      ScottishHigherRateTax              -> 43246.39, // LS12.8	Scottish Higher rate ?
      ScottishAdditionalRateTax          -> 2176.26, // LS12.9	Scottish Top rate ?
      ScottishTotalTax                   -> 52163.74, // LS12a	Total Scottish Income Tax ?
      ScottishStarterIncome              -> 398.43 / 0.19, // Starter rate	£12,571 to £14,667	19%
      ScottishBasicIncome                -> 2485.8 / 0.2,
      ScottishIntermediateIncome         -> 3856.86 / 0.21,
      ScottishHigherIncome               -> 43246.39 / 0.41,
      ScottishAdditionalIncome           -> 2176.26 / 0.46,
      SavingsLowerRateTax                -> 0.0, // LS12b.1	Basic rate Income Tax
      SavingsHigherRateTax               -> 3278.40, // LS12b.2	Higher rate Income Tax
      SavingsAdditionalRateTax           -> 0.0 // LS12b.3 Additional rate Income Tax
      //SavingsLowerIncome                 -> 0.0,
      //SavingsHigherIncome                -> 0.0,
      //SavingsAdditionalIncome            -> 0.0
    )

    expected foreach { case (key, expectedValue) =>
      s"return the correct key $key" in new Test {
        server.stubFor(
          WireMock
            .get(urlEqualTo(odsUrl(taxYear)))
            .willReturn(ok(FileHelper.loadFile("2021-22/TC153.json")))
        )

        val result: AtsMiddleTierData = resultToAtsData(route(app, request))
        checkResult(result, key, expectedValue)
      }

    }
  }

  "HasSummary (SIT005)" must {
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
      OrdinaryRateAmount                 -> 750.00, // LS13.1 (tax amount - right column)
      UpperRate                          -> 0.00, //LS13.2 e
      UpperRateAmount                    -> 0.0, // LS13.2 (tax amount - right column)
      AdditionalRate                     -> 0.00, //LS13.3 e,
      AdditionalRateAmount               -> 0.00, // LS13.3 (tax amount - right column)
      OtherAdjustmentsIncreasing         -> 0.00, //LS15a e
      OtherAdjustmentsReducing           -> 0.00, //LS15b e
      WelshIncomeTax                     -> 0.00, //LS20a
      TotalIncomeTax                     -> 55869.35, //LS20 e
      TotalIncomeTaxAndNics              -> 55869.35, //LS16 e
      EmployeeNicAmount                  -> 0.00, //LS14 e
      PayCgTaxOn                         -> 0.00, //LS19.8 e
      TaxableGains                       -> 0.00, //LS19.6 e
      AmountDueAtEntrepreneursRate       -> 0.00, //LS19.1 e
      AmountAtEntrepreneursRate          -> 0.00, //LS19.1 e
      AmountDueAtOrdinaryRate            -> 0.00, //LS19.2 e
      AmountDueRPCIHigherRate            -> 0.00, //LS19.3 e
      Adjustments                        -> 0.00, //LS19.4 e
      TotalCgTax                         -> 0.00, //e
      YourTotalTax                       -> 55869.35, //RS7 e
      ScottishIncomeTax                  -> 0.0,
      WelshIncomeTax                     -> 0.0,
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
            .willReturn(ok(FileHelper.loadFile("2021-22/TC222.json")))
        )

        val result: AtsMiddleTierData = resultToAtsData(route(app, request))
        checkResult(result, key, expectedValue)
      }
    }
  }

  "HasSummary (SIT006)" must {
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
      BasicRateIncomeTax                 -> 12432.00, //lS12.2 e
      BasicRateIncomeTaxAmount           -> 2486.40, // LS12.2 (tax amount - right column)
      HigherRateIncomeTax                -> 620.00, //LS12.3 e
      HigherRateIncomeTaxAmount          -> 254.20, // LS12.3 (tax amount - right column)
      AdditionalRateIncomeTax            -> 0.00, //LS12.4 e
      AdditionalRateIncomeTaxAmount      -> 0.00, // LS12.4 (tax amount - right column)
      OrdinaryRate                       -> 3310.00, //LS13.1 e
      OrdinaryRateAmount                 -> 248.25, // LS13.1 (tax amount - right column)
      UpperRate                          -> 190.00, //LS13.2 e
      UpperRateAmount                    -> 61.75, // LS13.2 (tax amount - right column)
      AdditionalRate                     -> 0.00, //LS13.3 e,
      AdditionalRateAmount               -> 0.00, // LS13.3 (tax amount - right column)
      OtherAdjustmentsIncreasing         -> 0.00, //LS15a e
      OtherAdjustmentsReducing           -> 0.00, //LS15b e
      WelshIncomeTax                     -> 0.00, //LS20a
      TotalIncomeTax                     -> 3050.60, //LS20 e
      TotalIncomeTaxAndNics              -> 3585.53, //LS16 e
      EmployeeNicAmount                  -> 534.93, //LS14 e
      PayCgTaxOn                         -> 0.00, //LS19.8 e
      TaxableGains                       -> 0.00, //LS19.6 e
      AmountDueAtEntrepreneursRate       -> 0.00, //LS19.1 e
      AmountAtEntrepreneursRate          -> 0.00, //LS19.1 e
      AmountDueAtOrdinaryRate            -> 0.00, //LS19.2 e
      AmountDueRPCIHigherRate            -> 0.00, //LS19.3 e
      Adjustments                        -> 0.00, //LS19.4 e
      TotalCgTax                         -> 0.00, //e
      YourTotalTax                       -> 3585.53, //RS7 e
      ScottishIncomeTax                  -> 0.0,
      WelshIncomeTax                     -> 0.0,
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
            .willReturn(ok(FileHelper.loadFile("2021-22/TC3.json")))
        )

        val result: AtsMiddleTierData = resultToAtsData(route(app, request))
        checkResult(result, key, expectedValue)
      }
    }
  }

  "HasSummary (SIT007)" must {
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
      UpperRateAmount                    -> 888.87, // LS13.2 (tax amount - right column)
      AdditionalRate                     -> 0.00, //LS13.3 e,
      AdditionalRateAmount               -> 0.00, // LS13.3 (tax amount - right column)
      OtherAdjustmentsIncreasing         -> 0.00, //LS15a e
      OtherAdjustmentsReducing           -> 0.00, //LS15b e
      WelshIncomeTax                     -> 0.00, //LS20a
      TotalIncomeTax                     -> 8328.87, //LS20 e
      TotalIncomeTaxAndNics              -> 8328.87, //LS16 e
      EmployeeNicAmount                  -> 0.00, //LS14 e
      PayCgTaxOn                         -> 0.00, //LS19.8 e
      TaxableGains                       -> 0.00, //LS19.6 e
      AmountDueAtEntrepreneursRate       -> 0.00, //LS19.1 e
      AmountAtEntrepreneursRate          -> 0.00, //LS19.1 e
      AmountDueAtOrdinaryRate            -> 0.00, //LS19.2 e
      AmountDueRPCIHigherRate            -> 0.00, //LS19.3 e
      Adjustments                        -> 0.00, //LS19.4 e
      TotalCgTax                         -> 0.00, //e
      YourTotalTax                       -> 8328.87, //RS7 e
      ScottishIncomeTax                  -> 0.0,
      WelshIncomeTax                     -> 0.0,
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
            .willReturn(ok(FileHelper.loadFile("2021-22/TC6.json")))
        )

        val result: AtsMiddleTierData = resultToAtsData(route(app, request))
        checkResult(result, key, expectedValue)
      }
    }
  }

  "HasSummary (SIT008)" must {
    val expected = Map(
      // Your Taxable income
      SelfEmploymentIncome   -> 0.00, // LS1a Self Employment Income
      IncomeFromEmployment   -> 23678.00, // LS1 Total income from employment
      StatePension           -> 9783.00, //LS2 State pension
      OtherPensionIncome     -> 0.00, //LS3 Other pension income
      TaxableStateBenefits   -> 0.00, //LS4 Taxable state benefits
      OtherIncome            -> 21903.00, //LS5 Other income
      BenefitsFromEmployment -> 0.00, //LS6 Benefits from employment
      TotalIncomeBeforeTax   -> 55364.00, //LS7 Your income before tax

      // Tax Free Amount
      PersonalTaxFreeAmount              -> 12570.0, //LS8.1 tax free amount
      MarriageAllowanceTransferredAmount -> 0.00, //LS8.2 Marriage Allowance transferred
      OtherAllowancesAmount              -> 0.00, //LS9 Other allowances, deducations and expenses
      TotalTaxFreeAmount                 -> 12570.0, //LS10 Less your total tax free amount
      // LS11 You pay tax on

      // Income Tax - UK
      StartingRateForSavings             -> 0.00, //LS12.1 Starting rate for savings (income)
      StartingRateForSavingsAmount       -> 0.00, //LS12.1 Starting rate for savings (tax amount - right column)
      BasicRateIncomeTax                 -> 0.00, //lS12.2 Basic rate Income Tax (income)
      BasicRateIncomeTaxAmount           -> 0.00, // LS12.2 Basic rate Income Tax (tax amount - right column)
      HigherRateIncomeTax                -> 0.00, //LS12.3 Higher rate Income Tax (income)
      HigherRateIncomeTaxAmount          -> 0.00, // LS12.3 Higher rate Income Tax (tax amount - right column)
      AdditionalRateIncomeTax            -> 0.00, //LS12.4 Additional rate Income Tax (income)
      AdditionalRateIncomeTaxAmount      -> 0.00, // LS12.4 Additional rate Income Tax (tax amount - right column)

      // Dividends
      OrdinaryRate                 -> 10750.00, //LS13.1 Ordinary Rate (income)
      OrdinaryRateAmount           -> 806.25, // LS13.1 Ordinary Rate (tax amount - right column)
      UpperRate                    -> 0.00, //LS13.2 Upper Rate (income)
      UpperRateAmount              -> 0.00, // LS13.2 Upper Rate (tax amount - right column)
      AdditionalRate               -> 0.00, //LS13.3 Additional Rate (income)
      AdditionalRateAmount         -> 0.00, // LS13.3 Additional Rate (tax amount - right column)
      // LS13a Total UK Income Tax

      // Adjustments
      OtherAdjustmentsIncreasing   -> 0.00, //LS15a Other adjustments that increase your Income Tax
      OtherAdjustmentsReducing     -> 500.00, //LS15b Less other adjustments that reduce your Income Tax
      // LS15aa	Marriage Allowance received that reduces your income tax

      // Income Tax - Welsh
      WelshIncomeTax               -> 0.00, //LS20a
      TotalIncomeTax               -> 6162.58, //LS20 Total Income Tax
      TotalIncomeTaxAndNics        -> 6162.58, //LS16 Total Income Tax and NICs
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
      YourTotalTax                -> 6162.58,
      // Income Tax - Scottish
      ScottishIncomeTax           -> 0.0,
      ScottishStarterRateTax      -> 398.43, // LS12.5	Scottish Starter rate
      ScottishBasicRateTax        -> 3483.80, // LS12.6	Scottish Basic rate
      ScottishIntermediateRateTax -> 1438.50, // LS12.7	Intermediate rate
      ScottishHigherRateTax       -> 0.0, // LS12.8	Scottish Higher rate
      ScottishAdditionalRateTax   -> 0.0, // LS12.9	Scottish Top rate
      ScottishTotalTax            -> 5320.73, // LS12a	Total Scottish Income Tax
      ScottishStarterIncome       -> 398.43 / 0.19, // LS12.5 Starter rate income
      ScottishBasicIncome         -> 3483.80 / 0.2, // LS12.6	Scottish Basic rate income
      ScottishIntermediateIncome  -> 1438.50 / 0.21, // LS12.7	Intermediate rate income
      ScottishHigherIncome        -> 0.0, // LS12.8	Scottish Higher rate income
      ScottishAdditionalIncome    -> 0.0, // LS12.9	Scottish Top rate income
      SavingsLowerRateTax         -> 535.60, // LS12b.1	Basic rate Income Tax
      SavingsHigherRateTax        -> 0.0, // LS12b.2	Higher rate Income Tax
      SavingsAdditionalRateTax    -> 0.0 // LS12b.3 Additional rate Income Tax
    )

    expected foreach { case (key, expectedValue) =>
      s"return the correct key $key" in new Test {
        server.stubFor(
          WireMock
            .get(urlEqualTo(odsUrl(taxYear)))
            .willReturn(ok(FileHelper.loadFile("2021-22/TC11.json")))
        )

        val result: AtsMiddleTierData = resultToAtsData(route(app, request))
        checkResult(result, key, expectedValue)
      }
    }
  }

  "return correct government spending" in new Test {

    val expectedValue: GovernmentSpendingOutputWrapper = GovernmentSpendingOutputWrapper(
      2022,
      Map(
        PublicOrderAndSafety       -> SpendData(Amount(280.59, "GBP"), 4.4),
        BusinessAndIndustry        -> SpendData(Amount(344.36, "GBP"), 5.4),
        NationalDebtInterest       -> SpendData(Amount(484.65, "GBP"), 7.6),
        Defence                    -> SpendData(Amount(325.23, "GBP"), 5.1),
        Health                     -> SpendData(Amount(1453.96, "GBP"), 22.80),
        HousingAndUtilities        -> SpendData(Amount(102.03, "GBP"), 1.60),
        GovernmentAdministration   -> SpendData(Amount(146.67, "GBP"), 2.3),
        Environment                -> SpendData(Amount(95.66, "GBP"), 1.5),
        OverseasAid                -> SpendData(Amount(38.26, "GBP"), 0.60),
        Culture                    -> SpendData(Amount(82.90, "GBP"), 1.3),
        OutstandingPaymentsToTheEU -> SpendData(Amount(44.64, "GBP"), 0.7),
        Transport                  -> SpendData(Amount(299.72, "GBP"), 4.7),
        Welfare                    -> SpendData(Amount(1300.91, "GBP"), 20.40),
        Education                  -> SpendData(Amount(669.59, "GBP"), 10.50),
        StatePensions              -> SpendData(Amount(701.47, "GBP"), 11)
      ),
      Amount(6377, "GBP"),
      None
    )

    override val taxYear = 2022

    server.stubFor(
      WireMock
        .get(urlEqualTo(odsUrl(taxYear)))
        .willReturn(ok(FileHelper.loadFile("2019-20/utr_1097172561.json")))
    )

    val result: AtsMiddleTierData = resultToAtsData(route(app, request))
    result.gov_spending.get mustBe expectedValue
  }

}
