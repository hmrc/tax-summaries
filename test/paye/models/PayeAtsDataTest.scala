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

package paye.models

import common.models.*
import common.models.LiabilityKey.{AdditionalRateIncomeTax, AdditionalRateIncomeTaxAmount, BasicRateIncomeTax, BasicRateIncomeTaxAmount, BenefitsFromEmployment, DividendAdditionalRate, DividendAdditionalRateAmount, DividendOrdinaryRate, DividendOrdinaryRateAmount, DividendUpperRate, DividendUpperRateAmount, EmployeeNicAmount, EmployerNicAmount, HigherRateIncomeTax, HigherRateIncomeTaxAmount, IncomeAfterTaxAndNics, IncomeFromEmployment, LessTaxAdjustmentPrevYear, LiableTaxAmount, MarriageAllowanceReceivedAmount, MarriageAllowanceTransferredAmount, MarriedCouplesAllowance, OtherAllowancesAmount, OtherIncome, OtherPensionIncome, PersonalTaxFreeAmount, ScottishAdvancedRateIncomeTax, ScottishAdvancedRateIncomeTaxAmount, ScottishBasicRateIncomeTax, ScottishBasicRateIncomeTaxAmount, ScottishHigherRateIncomeTax, ScottishHigherRateIncomeTaxAmount, ScottishIncomeTax, ScottishIntermediateRateIncomeTax, ScottishIntermediateRateIncomeTaxAmount, ScottishStarterRateIncomeTax, ScottishStarterRateIncomeTaxAmount, ScottishTopRateIncomeTax, ScottishTopRateIncomeTaxAmount, ScottishTotalTax, StatePension, TaxUnderpaidPrevYear, TaxableStateBenefits, TotalIncomeBeforeTax, TotalIncomeTax, TotalIncomeTax2, TotalIncomeTax2Nics, TotalIncomeTaxAndNics, TotalTaxFreeAmount, TotalUKIncomeTax, WelshIncomeTax}
import common.models.RateKey.{NICS, PayeAdditionalRateIncomeTax, PayeBasicRateIncomeTax, PayeDividendAdditionalRate, PayeDividendOrdinaryRate, PayeDividendUpperRate, PayeHigherRateIncomeTax, PayeScottishAdvancedRate, PayeScottishBasicRate, PayeScottishHigherRate, PayeScottishIntermediateRate, PayeScottishStarterRate, PayeScottishTopRate}
import common.services.GoodsAndServices
import common.services.GoodsAndServices.*
import common.utils.{BaseSpec, TestConstants}
import org.scalatest.AppendedClues.convertToClueful
import paye.utils.PayeAtsDataUtil
import play.api.Configuration
import sa.models.SpendData
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

class PayeAtsDataTest extends BaseSpec {

  val atsData: PayeAtsData                = PayeAtsDataUtil.atsData
  val nino: String                        = TestConstants.testNino
  val taxYear                             = "2024"
  lazy val servicesConfig: ServicesConfig = app.injector.instanceOf[ServicesConfig]
  lazy val configuration: Configuration   = app.injector.instanceOf[Configuration]

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

    "create income data with WelshIncomeTax as correct value (ScottishIncomeTax - see comment in PayeAtsData class)" in {
      val incomeData: DataHolder =
        transformedData.income_data.getOrElse(fail("No income data"))

      val expectedValues: Map[LiabilityKey, Amount] = Map(
        IncomeFromEmployment   -> Amount.gbp(25000.00, IncomeFromEmployment.apiValue),
        StatePension           -> Amount.gbp(1000.00, StatePension.apiValue),
        OtherPensionIncome     -> Amount.gbp(500.00, OtherPensionIncome.apiValue),
        OtherIncome            -> Amount.gbp(3000.00, OtherIncome.apiValue),
        TotalIncomeBeforeTax   -> Amount.gbp(28000.00, TotalIncomeBeforeTax.apiValue),
        WelshIncomeTax         -> Amount.gbp(2550.00, ScottishIncomeTax.apiValue),
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
        AdditionalRateIncomeTaxAmount           -> Amount.gbp(2200.00, AdditionalRateIncomeTaxAmount.apiValue),
        AdditionalRateIncomeTax                 -> Amount.gbp(12000.00, AdditionalRateIncomeTax.apiValue),
        DividendOrdinaryRateAmount              -> Amount.gbp(200.00, DividendOrdinaryRateAmount.apiValue),
        DividendOrdinaryRate                    -> Amount.gbp(2000.00, DividendOrdinaryRate.apiValue),
        DividendUpperRateAmount                 -> Amount.gbp(200.00, DividendUpperRateAmount.apiValue),
        DividendUpperRate                       -> Amount.gbp(2000.00, DividendUpperRate.apiValue),
        DividendAdditionalRateAmount            -> Amount.gbp(110.00, DividendAdditionalRateAmount.apiValue),
        DividendAdditionalRate                  -> Amount.gbp(1100.00, DividendAdditionalRate.apiValue),
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
        ScottishHigherRateIncomeTax             -> Amount.gbp(31570.0, ScottishHigherRateIncomeTax.apiValue),
        ScottishAdvancedRateIncomeTaxAmount     -> Amount.gbp(9000.0, ScottishAdvancedRateIncomeTaxAmount.apiValue),
        ScottishAdvancedRateIncomeTax           -> Amount.gbp(20000.0, ScottishAdvancedRateIncomeTax.apiValue),
        ScottishTopRateIncomeTaxAmount          -> Amount.gbp(5443.7, ScottishTopRateIncomeTaxAmount.apiValue),
        ScottishTopRateIncomeTax                -> Amount.gbp(22570.00, ScottishTopRateIncomeTax.apiValue)
      )

      val expectedRatesValues: Map[RateKey, ApiRate] = Map(
        PayeDividendOrdinaryRate     -> ApiRate("7.5%"),
        PayeHigherRateIncomeTax      -> ApiRate("40%"),
        PayeAdditionalRateIncomeTax  -> ApiRate("45%"),
        PayeBasicRateIncomeTax       -> ApiRate("20%"),
        PayeDividendUpperRate        -> ApiRate("32.5%"),
        PayeDividendAdditionalRate   -> ApiRate("38.2%"),
        PayeScottishStarterRate      -> ApiRate("19%"),
        PayeScottishBasicRate        -> ApiRate("20%"),
        PayeScottishIntermediateRate -> ApiRate("21%"),
        PayeScottishHigherRate       -> ApiRate("41%"),
        PayeScottishAdvancedRate     -> ApiRate("45%"),
        PayeScottishTopRate          -> ApiRate("48%")
      )

      val diff1 = incomeTax.payload.get.keySet.diff(expectedPayloadValues.keySet)
      diff1 mustBe empty

      val diff2 = expectedPayloadValues.keySet.diff(incomeTax.payload.get.keySet)
      diff2 mustBe empty

      incomeTax.payload.get.foreach { case (key: LiabilityKey, amount: Amount) =>
        val expected = expectedPayloadValues.get(key)
        amount mustBe expected.getOrElse(
          Amount(0.0, "missing", Some("expected missing"))
        ) withClue s"clue: `$key must be $expected``"
      }

      incomeTax.rates.get.foreach { case (key: RateKey, rate: ApiRate) =>
        val expected = expectedRatesValues.get(key)
        rate mustBe expected.getOrElse(
          ApiRate("expected missing")
        ) withClue s"clue: `$key must be $expected``"
      }
    }

    "create gov spend data" must {

      "gov spend data contains correct amount with percentages" in {
        val expectedValues: Map[GoodsAndServices, SpendData] = Map(
          Welfare                    -> SpendData(Amount(907.20, "GBP"), 21.6),
          Health                     -> SpendData(Amount(848.40, "GBP"), 20.2),
          StatePensions              -> SpendData(Amount(478.80, "GBP"), 11.4),
          NationalDebtInterest       -> SpendData(Amount(466.20, "GBP"), 11.1),
          Education                  -> SpendData(Amount(428.40, "GBP"), 10.2),
          Defence                    -> SpendData(Amount(218.40, "GBP"), 5.2),
          PublicOrderAndSafety       -> SpendData(Amount(184.80, "GBP"), 4.4),
          Transport                  -> SpendData(Amount(176.40, "GBP"), 4.2),
          BusinessAndIndustry        -> SpendData(Amount(176.40, "GBP"), 4.2),
          GovernmentAdministration   -> SpendData(Amount(88.20, "GBP"), 2.1),
          HousingAndUtilities        -> SpendData(Amount(75.60, "GBP"), 1.8),
          Environment                -> SpendData(Amount(58.80, "GBP"), 1.4),
          Culture                    -> SpendData(Amount(50.40, "GBP"), 1.2),
          OverseasAid                -> SpendData(Amount(29.40, "GBP"), 0.7),
          OutstandingPaymentsToTheEU -> SpendData(Amount(25.200, "GBP"), 0.6)
        )

        val spendData = transformedData.gov_spending.getOrElse(fail("No gov spend data"))
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
