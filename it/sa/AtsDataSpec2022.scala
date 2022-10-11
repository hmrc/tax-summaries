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
import models.RateKey.Additional
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
      OtherPensionIncome -> 3121.00, //LS3 e
      TaxableStateBenefits -> 0.00, //LS4 e
      OtherIncome -> 298111.00, //LS5 e
      BenefitsFromEmployment -> 9600.00, //LS6 e
      TotalIncomeBeforeTax -> 438044.00, //LS7 total income received e
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
      OtherAdjustmentsReducing -> 5129.60, //LS15b e
      WelshIncomeTax -> 0.00, //LS20a
      TotalIncomeTax -> 163332.65, //LS20 e
      TotalIncomeTaxAndNics -> 163491.25, //LS16 e
      EmployeeNicAmount -> 158.60, //LS14 e
      //IncomeAfterTaxAndNics -> 274552.75, //LS17, RS5 e
      //EmployerNicAmount -> 15685.00, //LS18 e
      PayCgTaxOn -> 28700.00, //LS19.8 e
      TaxableGains -> 41000.00, //LS19.6 e
      TotalTaxFreeAmount -> 12300.00, //LS19.7 e
      AmountDueAtEntrepreneursRate -> 1200.00, //LS19.1 e
      AmountAtEntrepreneursRate -> 12000.00, //LS19.1 e
      AmountDueAtOrdinaryRate -> 0.00, //LS19.2 e
      AmountDueRPCIHigherRate -> 3340.00, //LS19.3 e
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
      SelfEmploymentIncome -> 942.00, // LS1a e
      IncomeFromEmployment -> 122500.00, // LS1 e
      StatePension -> 0.00, //LS2 e
      OtherPensionIncome -> 3121.00, //LS3 e
      TaxableStateBenefits -> 0.00, //LS4 e
      OtherIncome -> 298111.00, //LS5 e
      BenefitsFromEmployment -> 9600.00, //LS6 e
      TotalIncomeBeforeTax -> 438044.00, //LS7 total income received e
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
      OtherAdjustmentsReducing -> 5129.60, //LS15b e
      WelshIncomeTax -> 0.00, //LS20a
      TotalIncomeTax -> 163332.65, //LS20 e
      TotalIncomeTaxAndNics -> 163491.25, //LS16 e
      EmployeeNicAmount -> 158.60, //LS14 e
      //IncomeAfterTaxAndNics -> 274552.75, //LS17, RS5 e
      //EmployerNicAmount -> 15685.00, //LS18 e
      PayCgTaxOn -> 28700.00, //LS19.8 e
      TaxableGains -> 41000.00, //LS19.6 e
      TotalTaxFreeAmount -> 12300.00, //LS19.7 e
      AmountDueAtEntrepreneursRate -> 1200.00, //LS19.1 e
      AmountAtEntrepreneursRate -> 12000.00, //LS19.1 e
      AmountDueAtOrdinaryRate -> 0.00, //LS19.2 e
      AmountDueRPCIHigherRate -> 3340.00, //LS19.3 e
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
}
