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
  val nino: String         = TestConstants.testNino
  val taxYear              = "2019"
  lazy val servicesConfig  = app.injector.instanceOf[ServicesConfig]
  lazy val configuration   = app.injector.instanceOf[Configuration]

  lazy val transformedData: PayeAtsMiddleTier =
    atsData.transformToPayeMiddleTier(applicationConfig, nino, taxYear.toInt)

  "transformToPayeMiddleTier" must {
    "populate the nino and tax year" in {
      transformedData.nino mustBe nino
      transformedData.taxYear mustBe taxYear.toInt
    }

    "create allowance data" in {
      val allowanceData: DataHolder =
        transformedData.allowance_data.getOrElse(fail("No allowance data"))

      val expectedValues: Map[LiabilityKey, Amount] = Map(
        PersonalTaxFreeAmount              -> Amount.gbp(12500.00, PersonalTaxFreeAmount.apiValue),
        MarriageAllowanceTransferredAmount -> Amount.gbp(1250.00, MarriageAllowanceTransferredAmount.apiValue),
        OtherAllowancesAmount              -> Amount.gbp(6000.00, OtherAllowancesAmount.apiValue),
        TotalTaxFreeAmount                 -> Amount.gbp(25500.00, TotalTaxFreeAmount.apiValue),
        TotalIncomeBeforeTax               -> Amount.gbp(28000.00, TotalIncomeBeforeTax.apiValue)
      )

      allowanceData mustBe DataHolder(Some(expectedValues), None, None)

    }

    "create income data with ScottishIncomeTax as correct value when writ changes are enabled" in {

      val incomeData: DataHolder =
        transformedData.income_data.getOrElse(fail("No income data"))

      val expectedValues: Map[LiabilityKey, Amount] = Map(
        IncomeFromEmployment   -> Amount.gbp(25000.00, IncomeFromEmployment.apiValue),
        StatePension           -> Amount.gbp(1000.00, StatePension.apiValue),
        OtherPensionIncome     -> Amount.gbp(500.00, OtherPensionIncome.apiValue),
        OtherIncome            -> Amount.gbp(3000.00, OtherIncome.apiValue),
        TotalIncomeBeforeTax   -> Amount.gbp(28000.00, TotalIncomeBeforeTax.apiValue),
        ScottishIncomeTax      -> Amount.gbp(2550.00, ScottishIncomeTax.apiValue),
        BenefitsFromEmployment -> Amount.gbp(200.00, BenefitsFromEmployment.apiValue),
        TaxableStateBenefits   -> Amount.gbp(500.00, TaxableStateBenefits.apiValue)
      )
      incomeData mustBe DataHolder(Some(expectedValues), None, None)
    }

    "create summary data" in {
      val summaryData: DataHolder =
        transformedData.summary_data.getOrElse(fail("No summary data"))

      val expectedValues: Map[LiabilityKey, Amount] = Map(
        TotalIncomeBeforeTax  -> Amount.gbp(28000.00, TotalIncomeBeforeTax.apiValue),
        TotalTaxFreeAmount    -> Amount.gbp(25500.00, TotalTaxFreeAmount.apiValue),
        TotalIncomeTaxAndNics -> Amount.gbp(4200.00, TotalIncomeTaxAndNics.apiValue),
        TotalIncomeTax2Nics   -> Amount.gbp(2500.00, TotalIncomeTax2Nics.apiValue),
        IncomeAfterTaxAndNics -> Amount.gbp(5000.00, IncomeAfterTaxAndNics.apiValue),
        TotalIncomeTax        -> Amount.gbp(4010.00, TotalIncomeTax.apiValue),
        EmployeeNicAmount     -> Amount.gbp(200.00, EmployeeNicAmount.apiValue),
        EmployerNicAmount     -> Amount.gbp(100.00, EmployerNicAmount.apiValue),
        LiableTaxAmount       -> Amount.gbp(15000.00, LiableTaxAmount.apiValue)
      )

      summaryData mustBe DataHolder(Some(expectedValues), Some(Map(NICS -> ApiRate("25%"))), None)
    }

    "create income tax" in {
      val incomeTax: DataHolder =
        transformedData.income_tax.getOrElse(fail("No income tax data"))

      val expectedPayloadValues: Map[LiabilityKey, Amount] = Map(
        BasicRateIncomeTaxAmount                -> Amount.gbp(2000.00, BasicRateIncomeTaxAmount.apiValue),
        BasicRateIncomeTax                      -> Amount.gbp(10000.00, BasicRateIncomeTax.apiValue),
        TotalUKIncomeTax                        -> Amount.gbp(2000.0, TotalUKIncomeTax.apiValue),
        HigherRateIncomeTaxAmount               -> Amount.gbp(2000.00, HigherRateIncomeTaxAmount.apiValue),
        HigherRateIncomeTax                     -> Amount.gbp(10000.00, HigherRateIncomeTax.apiValue),
        OrdinaryRateAmount                      -> Amount.gbp(200.00, OrdinaryRateAmount.apiValue),
        OrdinaryRate                            -> Amount.gbp(2000.00, OrdinaryRate.apiValue),
        UpperRateAmount                         -> Amount.gbp(200.00, UpperRateAmount.apiValue),
        UpperRate                               -> Amount.gbp(2000.00, UpperRate.apiValue),
        MarriedCouplesAllowance                 -> Amount.gbp(500.00, MarriedCouplesAllowance.apiValue),
        MarriageAllowanceReceivedAmount         -> Amount.gbp(1250.00, MarriageAllowanceReceivedAmount.apiValue),
        LessTaxAdjustmentPrevYear               -> Amount.gbp(200.00, LessTaxAdjustmentPrevYear.apiValue),
        TaxUnderpaidPrevYear                    -> Amount.gbp(200.00, TaxUnderpaidPrevYear.apiValue),
        TotalIncomeTax                          -> Amount.gbp(4000.00, TotalIncomeTax.apiValue),
        ScottishTotalTax                        -> Amount.gbp(2000.00, ScottishTotalTax.apiValue),
        ScottishStarterRateIncomeTaxAmount      -> Amount.gbp(380.00, ScottishStarterRateIncomeTaxAmount.apiValue),
        ScottishStarterRateIncomeTax            -> Amount.gbp(2000.00, ScottishStarterRateIncomeTax.apiValue),
        ScottishBasicRateIncomeTaxAmount        -> Amount.gbp(2030.0, ScottishBasicRateIncomeTaxAmount.apiValue),
        ScottishBasicRateIncomeTax              -> Amount.gbp(10150.0, ScottishBasicRateIncomeTax.apiValue),
        TotalIncomeTax2                         -> Amount.gbp(4010.0, TotalIncomeTax2.apiValue),
        ScottishIntermediateRateIncomeTaxAmount -> Amount.gbp(4080.3, ScottishIntermediateRateIncomeTaxAmount.apiValue),
        ScottishIntermediateRateIncomeTax       -> Amount.gbp(19430.0, ScottishIntermediateRateIncomeTax.apiValue),
        ScottishHigherRateIncomeTaxAmount       -> Amount.gbp(12943.7, ScottishHigherRateIncomeTaxAmount.apiValue),
        ScottishHigherRateIncomeTax             -> Amount.gbp(31570.0, ScottishHigherRateIncomeTax.apiValue)
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

      incomeTax mustBe DataHolder(Some(expectedPayloadValues), Some(expectedRatesValues), None)
    }

    "create gov spend data" must {

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
        val spendData                                        = transformedData.gov_spending.getOrElse(fail("No gov spend data"))
        spendData.govSpendAmountData.map(x => x._1 -> x._2.amount.amount) mustBe expectedValues.map(x =>
          x._1 -> x._2.amount.amount
        )
        spendData.govSpendAmountData.map(x => x._1 -> x._2.percentage) mustBe expectedValues.map(x =>
          x._1 -> x._2.percentage
        )
      }

      "with nics included if employer contributions are present" in {
        val spendData = transformedData.gov_spending.getOrElse(fail("No gov spend data"))
        spendData.totalAmount mustBe Amount.gbp(4200.00, "total_income_tax")
      }

      "without nics included if employer contributions are not present" in {
        val atsDataWithoutNics                 = atsData.copy(nationalInsurance = Some(NationalInsurance(Some(100.00), None)))
        val transformedData: PayeAtsMiddleTier =
          atsDataWithoutNics.transformToPayeMiddleTier(applicationConfig, nino, taxYear.toInt)

        val spendData = transformedData.gov_spending.getOrElse(fail("No gov spend data"))
        spendData.totalAmount mustBe Amount.gbp(4000.00, "total_income_tax")
      }

      "without nics included if national insurance section is not present" in {
        val atsDataWithoutNics                 = atsData.copy(nationalInsurance = None)
        val transformedData: PayeAtsMiddleTier =
          atsDataWithoutNics.transformToPayeMiddleTier(applicationConfig, nino, taxYear.toInt)

        val spendData = transformedData.gov_spending.getOrElse(fail("No gov spend data"))
        spendData.totalAmount mustBe Amount.gbp(4000.00, "total_income_tax")
      }

    }
  }
}
