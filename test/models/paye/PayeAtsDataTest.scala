/*
 * Copyright 2020 HM Revenue & Customs
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

/*
 * Copyright 2020 HM Revenue & Customs
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

package models.paye

import config.ApplicationConfig
import models.LiabilityKey._
import models.RateKey._
import models._
import play.api.Configuration
import services.GoodsAndServices
import services.GoodsAndServices._
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import utils.{BaseSpec, PayeAtsDataUtil, TestConstants}

class PayeAtsDataTest extends BaseSpec {

  val atsData: PayeAtsData = PayeAtsDataUtil.atsData
  val nino: String = TestConstants.testNino
  val taxYear = "2018"
  lazy val servicesConfig = app.injector.instanceOf[ServicesConfig]
  lazy val configuration = app.injector.instanceOf[Configuration]

  lazy val transformedData: PayeAtsMiddleTier =
    atsData.transformToPayeMiddleTier(applicationConfig, nino, taxYear.toInt)

  "transformToPayeMiddleTier" should {
    "populate the nino and tax year" in {
      transformedData.nino shouldBe nino
      transformedData.taxYear shouldBe taxYear.toInt
    }

    "create allowance data" in {
      val allowanceData: DataHolder =
        transformedData.allowance_data.getOrElse(fail("No allowance data"))

      val expectedValues: Map[LiabilityKey, Amount] = Map(
        PersonalTaxFreeAmount              -> Amount.gbp(12500.00),
        MarriageAllowanceTransferredAmount -> Amount.gbp(1250.00),
        OtherAllowancesAmount              -> Amount.gbp(6000.00),
        TotalTaxFreeAmount                 -> Amount.gbp(25500.00),
        TotalIncomeBeforeTax               -> Amount.gbp(28000.00)
      )

      allowanceData shouldBe DataHolder(Some(expectedValues), None, None)

    }

    "create income data with ScottishIncomeTax as correct value when writ changes are enabled" in {

      val incomeData: DataHolder =
        transformedData.income_data.getOrElse(fail("No income data"))

      val expectedValues: Map[LiabilityKey, Amount] = Map(
        IncomeFromEmployment   -> Amount.gbp(25000.00),
        StatePension           -> Amount.gbp(1000.00),
        OtherPensionIncome     -> Amount.gbp(500.00),
        OtherIncome            -> Amount.gbp(3000.00),
        TotalIncomeBeforeTax   -> Amount.gbp(28000.00),
        ScottishIncomeTax      -> Amount.gbp(2550.00),
        BenefitsFromEmployment -> Amount.gbp(200.00),
        TaxableStateBenefits   -> Amount.gbp(500.00)
      )
      incomeData shouldBe DataHolder(Some(expectedValues), None, None)
    }

    "create income data with ScottishIncomeTax as zero when writ changes are disabled" in {

      class ApplicationConfigStub extends ApplicationConfig(servicesConfig, configuration) {
        override val writEnabled = false
      }

      lazy val transformedData: PayeAtsMiddleTier =
        atsData.transformToPayeMiddleTier(new ApplicationConfigStub, nino, taxYear.toInt)

      val incomeData: DataHolder =
        transformedData.income_data.getOrElse(fail("No income data"))

      val expectedValues: Map[LiabilityKey, Amount] = Map(
        IncomeFromEmployment   -> Amount.gbp(25000.00),
        StatePension           -> Amount.gbp(1000.00),
        OtherPensionIncome     -> Amount.gbp(500.00),
        OtherIncome            -> Amount.gbp(3000.00),
        TotalIncomeBeforeTax   -> Amount.gbp(28000.00),
        ScottishIncomeTax      -> Amount.gbp(0),
        BenefitsFromEmployment -> Amount.gbp(200.00),
        TaxableStateBenefits   -> Amount.gbp(500.00)
      )
      incomeData shouldBe DataHolder(Some(expectedValues), None, None)
    }

    "create summary data" in {
      val summaryData: DataHolder =
        transformedData.summary_data.getOrElse(fail("No summary data"))

      val expectedValues: Map[LiabilityKey, Amount] = Map(
        TotalIncomeBeforeTax  -> Amount.gbp(28000.00),
        TotalTaxFreeAmount    -> Amount.gbp(25500.00),
        TotalIncomeTaxAndNics -> Amount.gbp(4200.00),
        TotalIncomeTax2Nics   -> Amount.gbp(2500.00),
        IncomeAfterTaxAndNics -> Amount.gbp(5000.00),
        TotalIncomeTax        -> Amount.gbp(4010.00),
        EmployeeNicAmount     -> Amount.gbp(200.00),
        EmployerNicAmount     -> Amount.gbp(100.00),
        LiableTaxAmount       -> Amount.gbp(15000.00)
      )

      summaryData shouldBe DataHolder(Some(expectedValues), Some(Map(NICS -> ApiRate("25%"))), None)
    }

    "create income tax" in {
      val incomeTax: DataHolder =
        transformedData.income_tax.getOrElse(fail("No income tax data"))

      val expectedPayloadValues: Map[LiabilityKey, Amount] = Map(
        BasicRateIncomeTaxAmount                -> Amount.gbp(2000.00),
        BasicRateIncomeTax                      -> Amount.gbp(10000.00),
        TotalUKIncomeTax                        -> Amount.gbp(2000.0),
        HigherRateIncomeTaxAmount               -> Amount.gbp(2000.00),
        HigherRateIncomeTax                     -> Amount.gbp(10000.00),
        OrdinaryRateAmount                      -> Amount.gbp(200.00),
        OrdinaryRate                            -> Amount.gbp(2000.00),
        UpperRateAmount                         -> Amount.gbp(200.00),
        UpperRate                               -> Amount.gbp(2000.00),
        MarriedCouplesAllowance                 -> Amount.gbp(500.00),
        MarriageAllowanceReceivedAmount         -> Amount.gbp(1250.00),
        LessTaxAdjustmentPrevYear               -> Amount.gbp(200.00),
        TaxUnderpaidPrevYear                    -> Amount.gbp(200.00),
        TotalIncomeTax                          -> Amount.gbp(4000.00),
        ScottishTotalTax                        -> Amount.gbp(2000.00),
        ScottishStarterRateIncomeTaxAmount      -> Amount.gbp(380.00),
        ScottishStarterRateIncomeTax            -> Amount.gbp(2000.00),
        ScottishBasicRateIncomeTaxAmount        -> Amount.gbp(2030.0),
        ScottishBasicRateIncomeTax              -> Amount.gbp(10150.0),
        TotalIncomeTax2                         -> Amount.gbp(4010.0),
        ScottishIntermediateRateIncomeTaxAmount -> Amount.gbp(4080.3),
        ScottishIntermediateRateIncomeTax       -> Amount.gbp(19430.0),
        ScottishHigherRateIncomeTaxAmount       -> Amount.gbp(12943.7),
        ScottishHigherRateIncomeTax             -> Amount.gbp(31570.0)
      )

      val expectedRatesValues: Map[RateKey, ApiRate] = Map(
        PayeDividendOrdinaryRate     -> ApiRate("7.5%"),
        PayeHigherRateIncomeTax      -> ApiRate("40%"),
        PayeBasicRateIncomeTax       -> ApiRate("20%"),
        PayeDividendUpperRate        -> ApiRate("32.5%"),
        PayeScottishStarterRate      -> ApiRate("19%"),
        PayeScottishBasicRate        -> ApiRate("20%"),
        PayeScottishIntermediateRate -> ApiRate("21%"),
        PayeScottishHigherRate       -> ApiRate("41%")
      )

      incomeTax shouldBe DataHolder(Some(expectedPayloadValues), Some(expectedRatesValues), None)
    }

    "create gov spend data" should {

      "gov spend data contains correct amount with percentages" in {

        val expectedValues: Map[GoodsAndServices, SpendData] = Map(
          PublicOrderAndSafety     -> SpendData(Amount(180.60, "GBP"), 4.3),
          Environment              -> SpendData(Amount(63.00, "GBP"), 1.5),
          OverseasAid              -> SpendData(Amount(50.40, "GBP"), 1.2),
          BusinessAndIndustry      -> SpendData(Amount(151.20, "GBP"), 3.6),
          NationalDebtInterest     -> SpendData(Amount(214.20, "GBP"), 5.1),
          Defence                  -> SpendData(Amount(222.60, "GBP"), 5.3),
          Health                   -> SpendData(Amount(848.40, "GBP"), 20.2),
          Culture                  -> SpendData(Amount(63.00, "GBP"), 1.5),
          UkContributionToEuBudget -> SpendData(Amount(42.00, "GBP"), 1.0),
          HousingAndUtilities      -> SpendData(Amount(67.20, "GBP"), 1.6),
          Transport                -> SpendData(Amount(180.60, "GBP"), 4.3),
          Welfare                  -> SpendData(Amount(987.00, "GBP"), 23.5),
          GovernmentAdministration -> SpendData(Amount(88.20, "GBP"), 2.1),
          Education                -> SpendData(Amount(495.60, "GBP"), 11.8),
          StatePensions            -> SpendData(Amount(537.60, "GBP"), 12.8)
        )
        val spendData = transformedData.gov_spending.getOrElse(fail("No gov spend data"))
        spendData.govSpendAmountData shouldBe expectedValues
      }

      "with nics included if employer contributions are present" in {
        val spendData = transformedData.gov_spending.getOrElse(fail("No gov spend data"))
        spendData.totalAmount shouldBe Amount.gbp(4200.00)
      }

      "without nics included if employer contributions are not present" in {
        val atsDataWithoutNics = atsData.copy(nationalInsurance = Some(NationalInsurance(Some(100.00), None)))
        val transformedData: PayeAtsMiddleTier =
          atsDataWithoutNics.transformToPayeMiddleTier(applicationConfig, nino, taxYear.toInt)

        val spendData = transformedData.gov_spending.getOrElse(fail("No gov spend data"))
        spendData.totalAmount shouldBe Amount.gbp(4000.00)
      }

      "without nics included if national insurance section is not present" in {
        val atsDataWithoutNics = atsData.copy(nationalInsurance = None)
        val transformedData: PayeAtsMiddleTier =
          atsDataWithoutNics.transformToPayeMiddleTier(applicationConfig, nino, taxYear.toInt)

        val spendData = transformedData.gov_spending.getOrElse(fail("No gov spend data"))
        spendData.totalAmount shouldBe Amount.gbp(4000.00)
      }

    }
  }
}
