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

package sa.regressions

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.{ok, urlEqualTo}
import common.models.LiabilityKey.{AdditionalRate, AdditionalRateIncomeTax, Adjustments, AmountDueAtEntrepreneursRate, AmountDueAtOrdinaryRate, AmountDueRPCIHigherRate, AmountDueRPCILowerRate, BasicRateIncomeTax, BenefitsFromEmployment, DividendOrdinaryRate, DividendUpperRate, EmployeeNicAmount, HigherRateIncomeTax, IncomeFromEmployment, MarriageAllowanceTransferredAmount, OtherAdjustmentsIncreasing, OtherAdjustmentsReducing, OtherAllowancesAmount, OtherIncome, OtherPensionIncome, PayCgTaxOn, PersonalTaxFreeAmount, SelfEmploymentIncome, StartingRateForSavingsAmount, StatePension, TaxableGains, TaxableStateBenefits, TotalCgTax, TotalIncomeBeforeTax, TotalIncomeTax, TotalIncomeTaxAndNics, TotalTaxFreeAmount, WelshIncomeTax, YourTotalTax}
import common.utils.FileHelper
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import sa.SaTestHelper
import sa.models.AtsMiddleTierData

import java.time.LocalDate

class Ddcnl5893 extends SaTestHelper {
  val taxPayerFile     = "sa/taxPayerDetails.json"
  val currentYear: Int = LocalDate.now().getYear

  trait Test {
    val taxYear = 2021

    def odsUrl(taxYear: Int): String = s"/self-assessment/individuals/" + utr + s"/annual-tax-summaries/$taxYear"

    def apiUrl(taxYear: Int): String = s"/taxs/$utr/$taxYear/ats-data"

    def request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest(GET, apiUrl(taxYear))
      .withHeaders((AUTHORIZATION, "Bearer 123"))

  }

  "DDCNL-5893: Incorrect Capital Gains Tax Calculation" must {
    (currentYear - 5 to currentYear).foreach { taxYear =>
      s"Check for capital gains tax calculations for year $taxYear - test case 1" when {
        val expected = Map(
          SelfEmploymentIncome               -> 0.0, // LS1a
          IncomeFromEmployment               -> 171192.0, // LS1
          StatePension                       -> 0.0, // LS2
          OtherPensionIncome                 -> 0.0, // LS3
          TaxableStateBenefits               -> 0.0, // LS4
          OtherIncome                        -> 1364.0, // LS5
          BenefitsFromEmployment             -> 0.0, // LS6
          TotalIncomeBeforeTax               -> 172556.0, // LS7, RS2 total income received
          PersonalTaxFreeAmount              -> 0.00, // LS8.1
          MarriageAllowanceTransferredAmount -> 0.0, // LS8.2
          OtherAllowancesAmount              -> 0.0, // LS9
          TotalTaxFreeAmount                 -> 0.0, // LS10
          StartingRateForSavingsAmount       -> 0.0, // LS12.1
          BasicRateIncomeTax                 -> 37500.0, // lS12.2
          HigherRateIncomeTax                -> 112500.0, // LS12.3
          AdditionalRateIncomeTax            -> 21192.0, // LS12.4
          DividendOrdinaryRate               -> 0.0, // LS13.1
          DividendUpperRate                  -> 0.0, // LS13.2
          AdditionalRate                     -> 0.0, // LS13.3
          OtherAdjustmentsIncreasing         -> 0.0,
          OtherAdjustmentsReducing           -> 0.0,
          WelshIncomeTax                     -> 0.0, // LS20a
          TotalIncomeTax                     -> 62036.4, // LS20
          TotalIncomeTaxAndNics              -> 62036.4, // LS16
          EmployeeNicAmount                  -> 0.0, // LS14
          PayCgTaxOn                         -> 16160.0, // LS19.8
          TaxableGains                       -> 28160.0, // LS19.6
          AmountDueAtEntrepreneursRate       -> 0.0, // LS19.1
          AmountDueAtOrdinaryRate            -> 0.0, // LS19.2
          AmountDueRPCIHigherRate            -> 3232.0, // LS19.3b
          AmountDueRPCILowerRate             -> 0.0, // LS19.3b
          Adjustments                        -> -8413.0, // LS19.4
          TotalCgTax                         -> 0.0,
          YourTotalTax                       -> 62036.4 // RS7
        )

        expected foreach { case (key, expectedValue) =>
          s"return the correct key $key" in new Test {
            server.stubFor(
              WireMock
                .get(urlEqualTo(odsUrl(this.taxYear)))
                .willReturn(
                  ok(FileHelper.loadFile("sa/regressions/DDCNL-5893-1.json").replace("<year>", this.taxYear.toString))
                )
            )

            val result: AtsMiddleTierData = resultToAtsData(route(app, request))
            checkResult(result, key, expectedValue)
          }
        }
      }
    }

    (currentYear - 5 to currentYear).foreach { taxYear =>
      s"Check for capital gains tax calculations for year $taxYear - test case 2" when {
        val expected = Map(
          SelfEmploymentIncome               -> 0.0, // LS1a
          IncomeFromEmployment               -> 171192.0, // LS1
          StatePension                       -> 0.0, // LS2
          OtherPensionIncome                 -> 0.0, // LS3
          TaxableStateBenefits               -> 0.0, // LS4
          OtherIncome                        -> 1364.0, // LS5
          BenefitsFromEmployment             -> 0.0, // LS6
          TotalIncomeBeforeTax               -> 172556.0, // LS7, RS2 total income received
          PersonalTaxFreeAmount              -> 0.00, // LS8.1
          MarriageAllowanceTransferredAmount -> 0.0, // LS8.2
          OtherAllowancesAmount              -> 0.0, // LS9
          TotalTaxFreeAmount                 -> 0.0, // LS10
          StartingRateForSavingsAmount       -> 0.0, // LS12.1
          BasicRateIncomeTax                 -> 37500.0, // lS12.2
          HigherRateIncomeTax                -> 112500.0, // LS12.3
          AdditionalRateIncomeTax            -> 21192.0, // LS12.4
          DividendOrdinaryRate               -> 0.0, // LS13.1
          DividendUpperRate                  -> 0.0, // LS13.2
          AdditionalRate                     -> 0.0, // LS13.3
          OtherAdjustmentsIncreasing         -> 0.0,
          OtherAdjustmentsReducing           -> 0.0,
          WelshIncomeTax                     -> 0.0, // LS20a
          TotalIncomeTax                     -> 62036.4, // LS20
          TotalIncomeTaxAndNics              -> 62036.4, // LS16
          EmployeeNicAmount                  -> 0.0, // LS14
          PayCgTaxOn                         -> 16160.0, // LS19.8
          TaxableGains                       -> 28160.0, // LS19.6
          AmountDueAtEntrepreneursRate       -> 0.0, // LS19.1
          AmountDueAtOrdinaryRate            -> 0.0, // LS19.2
          AmountDueRPCIHigherRate            -> 3232.0, // LS19.3b
          AmountDueRPCILowerRate             -> 0.0, // LS19.3b
          Adjustments                        -> 8413.0, // LS19.4
          TotalCgTax                         -> 11645.0,
          YourTotalTax                       -> 73681.4 // RS7
        )

        expected foreach { case (key, expectedValue) =>
          s"return the correct key $key" in new Test {
            server.stubFor(
              WireMock
                .get(urlEqualTo(odsUrl(this.taxYear)))
                .willReturn(
                  ok(
                    FileHelper.loadFile("sa/regressions/DDCNL-5893-2.json").replace("<year>", this.taxYear.toString)
                  )
                )
            )

            val result: AtsMiddleTierData = resultToAtsData(route(app, request))

            checkResult(result, key, expectedValue)
          }
        }
      }
    }

    (currentYear - 5 to currentYear).foreach { taxYear =>
      s"Check for capital gains tax calculations for year $taxYear - test case 3" when {
        val expected = Map(
          SelfEmploymentIncome               -> 26700.0, // LS1a
          IncomeFromEmployment               -> 13463.0, // LS1
          StatePension                       -> 0.0, // LS2
          OtherPensionIncome                 -> 0.0, // LS3
          TaxableStateBenefits               -> 0.0, // LS4
          OtherIncome                        -> 4779.0, // LS5
          BenefitsFromEmployment             -> 0.0, // LS6
          TotalIncomeBeforeTax               -> 44942.0, // LS7, RS2 total income received
          PersonalTaxFreeAmount              -> 12500.00, // LS8.1
          MarriageAllowanceTransferredAmount -> 0.0, // LS8.2
          OtherAllowancesAmount              -> 0.0, // LS9
          TotalTaxFreeAmount                 -> 12500.0, // LS10
          StartingRateForSavingsAmount       -> 0.0, // LS12.1
          BasicRateIncomeTax                 -> 27917.0, // lS12.2
          HigherRateIncomeTax                -> 0.0, // LS12.3
          AdditionalRateIncomeTax            -> 0.0, // LS12.4
          DividendOrdinaryRate               -> 1525.0, // LS13.1
          DividendUpperRate                  -> 0.0, // LS13.2
          AdditionalRate                     -> 0.0, // LS13.3
          OtherAdjustmentsIncreasing         -> 0.0,
          OtherAdjustmentsReducing           -> 0.0,
          WelshIncomeTax                     -> 0.0, // LS20a
          TotalIncomeTax                     -> 5697.77, // LS20
          TotalIncomeTaxAndNics              -> 7323.89, // LS16
          EmployeeNicAmount                  -> 1626.12, // LS14
          PayCgTaxOn                         -> 55761.0, // LS19.8
          TaxableGains                       -> 67761.0, // LS19.6
          AmountDueAtEntrepreneursRate       -> 0.0, // LS19.1
          AmountDueAtOrdinaryRate            -> 910.44, // LS19.2
          AmountDueRPCIHigherRate            -> 10101.60, // LS19.3b
          AmountDueRPCILowerRate             -> 0.0, // LS19.3b
          Adjustments                        -> -7564.0, // LS19.4
          TotalCgTax                         -> 3502.64,
          YourTotalTax                       -> 10826.53 // RS7
        )

        expected foreach { case (key, expectedValue) =>
          s"return the correct key $key" in new Test {
            server.stubFor(
              WireMock
                .get(urlEqualTo(odsUrl(this.taxYear)))
                .willReturn(
                  ok(
                    FileHelper.loadFile("sa/regressions/DDCNL-5893-3.json").replace("<year>", this.taxYear.toString)
                  )
                )
            )

            val result: AtsMiddleTierData = resultToAtsData(route(app, request))
            checkResult(result, key, expectedValue)
          }
        }
      }
    }
  }
}
