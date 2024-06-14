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

package transformers.ATS2023

import models.LiabilityKey._
import models.RateKey.{Additional, IncomeAdditional, IncomeBasic, IncomeHigher, Ordinary, Savings, SavingsAdditionalRate, SavingsHigherRate, SavingsLowerRate, ScottishAdditionalRate, ScottishBasicRate, ScottishHigherRate, ScottishIntermediateRate, ScottishStarterRate, Upper}
import models._
import utils.{AtsJsonDataUpdate, AtsRawDataTransformerBaseSpec, AtsRawDataTransformerTestHelper}

trait ATSRawDataTransformer2023Spec
    extends AtsRawDataTransformerTestHelper
    with AtsRawDataTransformerBaseSpec
    with AtsJsonDataUpdate {
  override protected val taxYear: Int = 2023

  protected def atsRawDataTransformerWithTaxRatesAndYear(): Unit = {
    "have the correct tax year from json" in {
      transformedData.taxYear mustBe taxYear
    }
    "use the correct tax rates" in {
      transformedData.income_tax.flatMap(_.rates).map(_.toSet) mustBe Some(
        Set(
          Additional               -> ApiRate("39.35%"),
          Ordinary                 -> ApiRate("8.75%"),
          ScottishBasicRate        -> ApiRate("20%"),
          SavingsLowerRate         -> ApiRate("20%"),
          SavingsHigherRate        -> ApiRate("40%"),
          ScottishAdditionalRate   -> ApiRate("46%"),
          IncomeHigher             -> ApiRate("40%"),
          ScottishIntermediateRate -> ApiRate("21%"),
          SavingsAdditionalRate    -> ApiRate("45%"),
          IncomeAdditional         -> ApiRate("45%"),
          ScottishHigherRate       -> ApiRate("41%"),
          ScottishStarterRate      -> ApiRate("19%"),
          Savings                  -> ApiRate("0%"),
          Upper                    -> ApiRate("33.75%"),
          IncomeBasic              -> ApiRate("20%")
        )
      )
    }
  }

  protected def expectedResultIncomeTax: Map[LiabilityKey, Amount] = Map(
    StartingRateForSavings          -> calcExp("ctnSavingsChgbleStartRate", "ctnTaxableCegSr"),
    StartingRateForSavingsAmount    -> calcExp("ctnSavingsTaxStartingRate", "ctnTaxOnCegSr"),
    BasicRateIncomeTax              -> expBasicRateIncomeTax,
    BasicRateIncomeTaxAmount        -> expBasicRateIncomeTaxAmount,
    HigherRateIncomeTax             -> expHigherRateIncomeTax,
    HigherRateIncomeTaxAmount       -> expHigherRateIncomeTaxAmount,
    AdditionalRateIncomeTax         -> expAdditionalRateIncomeTax,
    AdditionalRateIncomeTaxAmount   -> expAditionalRateIncomeTaxAmount,
    OrdinaryRate                    -> calcExp("ctnDividendChgbleLowRate"),
    OrdinaryRateAmount              -> calcExp("ctnDividendTaxLowRate"),
    UpperRate                       -> calcExp("ctnDividendChgbleHighRate"),
    UpperRateAmount                 -> calcExp("ctnDividendTaxHighRate"),
    AdditionalRate                  -> calcExp("ctnDividendChgbleAddHRate"),
    AdditionalRateAmount            -> calcExp("ctnDividendTaxAddHighRate"),
    OtherAdjustmentsIncreasing      -> expOtherAdjustmentsIncreasing,
    MarriageAllowanceReceivedAmount -> calcExp("ctnMarriageAllceInAmt"),
    OtherAdjustmentsReducing        -> expOtherAdjustmentsReducing,
    TotalIncomeTax                  -> expTotalIncomeTax,
    ScottishIncomeTax               -> calcExp("scottishIncomeTaxUK2023:null"),
    WelshIncomeTax                  -> calcExp("welshIncomeTax:null"),
    ScottishStarterRateTax          -> calcExp("scottishStarterRateTax:null"),
    ScottishBasicRateTax            -> calcExp("scottishBasicRateTax:null"),
    ScottishIntermediateRateTax     -> calcExp("scottishIntermediateRateTax:null"),
    ScottishHigherRateTax           -> calcExp("scottishHigherRateTax:null"),
    ScottishAdditionalRateTax       -> calcExp("scottishAdditionalRateTax:null"),
    ScottishTotalTax                -> expScottishTotalTax,
    ScottishStarterIncome           -> calcExp("scottishStarterRateIncome:null"),
    ScottishBasicIncome             -> calcExp("scottishBasicRateIncome:null"),
    ScottishIntermediateIncome      -> calcExp("scottishIntermediateRateIncome:null"),
    ScottishHigherIncome            -> calcExp("scottishHigherRateIncome:null"),
    ScottishAdditionalIncome        -> calcExp("scottishAdditionalRateIncome:null"),
    SavingsLowerRateTax             -> calcExp("savingsBasicRateTax:null"),
    SavingsHigherRateTax            -> calcExp("savingsHigherRateTax:null"),
    SavingsAdditionalRateTax        -> calcExp("savingsAdditionalRateTax:null"),
    SavingsLowerIncome              -> calcExp("savingsBasicRateIncome:null"),
    SavingsHigherIncome             -> calcExp("savingsHigherRateIncome:null"),
    SavingsAdditionalIncome         -> calcExp("savingsAdditionalRateIncome:null")
  )

  protected def expectedResultIncomeData: Map[LiabilityKey, Amount] = Map(
    SelfEmploymentIncome   -> expSelfEmployment,
    IncomeFromEmployment   -> calcExp("ctnSummaryTotalEmployment"),
    StatePension           -> calcExp("atsStatePensionAmt"),
    OtherPensionIncome     -> calcExp("atsOtherPensionAmt", "itfStatePensionLsGrossAmt"),
    TaxableStateBenefits   -> expTaxableStateBenefits,
    OtherIncome            -> expOtherIncome,
    BenefitsFromEmployment -> calcExp("ctnEmploymentBenefitsAmt"),
    TotalIncomeBeforeTax   -> expTotalIncomeBeforeTax
  )

  protected def expectedResultAllowanceData: Map[LiabilityKey, Amount] = Map(
    PersonalTaxFreeAmount              -> calcExp("ctnPersonalAllowance"),
    MarriageAllowanceTransferredAmount -> calcExp("ctnMarriageAllceOutAmt"),
    OtherAllowancesAmount              -> expOtherAllowancesAmount,
    TotalTaxFreeAmount                 -> expTotalTaxFreeAmount
  )

  protected def expectedResultCGData: Map[LiabilityKey, Amount] = Map(
    TaxableGains                 -> expTaxableGains,
    LessTaxFreeAmount            -> calcExp("atsCgAnnualExemptAmt"),
    PayCgTaxOn                   -> Amount.empty("taxableGains() < get(CgAnnualExempt)"),
    AmountAtEntrepreneursRate    -> calcExp("ctnCgAtEntrepreneursRate"),
    AmountDueAtEntrepreneursRate -> calcExp("ctnCgDueEntrepreneursRate"),
    AmountAtOrdinaryRate         -> calcExp("ctnCgAtLowerRate"),
    AmountDueAtOrdinaryRate      -> calcExp("ctnCgDueLowerRate"),
    AmountAtHigherRate           -> calcExp("ctnCgAtHigherRate"),
    AmountDueAtHigherRate        -> calcExp("ctnCgDueHigherRate"),
    Adjustments                  -> calcExp("capAdjustmentAmt"),
    TotalCgTax                   -> expTotalCgTax.max(0),
    CgTaxPerCurrencyUnit         -> taxPerTaxableCurrencyUnit(
      expTotalCgTax.max(0),
      expTaxableGains
    ),
    AmountAtRPCILowerRate        -> calcExp("ctnCGAtLowerRateRPCI"),
    AmountDueRPCILowerRate       -> calcExp("ctnLowerRateCgtRPCI"),
    AmountAtRPCIHigheRate        -> calcExp("ctnCGAtHigherRateRPCI"),
    AmountDueRPCIHigherRate      -> calcExp("ctnHigherRateCgtRPCI")
  )

  protected def expectedResultSummaryData: Map[LiabilityKey, Amount] = Map(
    EmployeeNicAmount         -> expEmployeeNicAmount,
    TotalIncomeTaxAndNics     -> expTotalIncomeTaxAndNics,
    YourTotalTax              -> (expTotalIncomeTaxAndNics + expTotalCgTax.max(0)),
    PersonalTaxFreeAmount     -> calcExp("ctnPersonalAllowance"),
    TotalTaxFreeAmount        -> expTotalTaxFreeAmount,
    TotalIncomeBeforeTax      -> expTotalIncomeBeforeTax,
    TotalIncomeTax            -> expTotalIncomeTax,
    TotalCgTax                -> expTotalCgTax.max(0),
    TaxableGains              -> calcExp("atsCgTotGainsAfterLosses", "atsCgGainsAfterLossesAmt"),
    CgTaxPerCurrencyUnit      -> taxPerTaxableCurrencyUnit(
      expTotalCgTax.max(0),
      expTaxableGains
    ),
    NicsAndTaxPerCurrencyUnit -> expNicsAndTaxPerCurrencyUnitExclNonExclMax
  )

  protected def expectedResultSummaryDataNonExcluded: Map[LiabilityKey, Amount] = Map(
    TotalIncomeTaxAndNics     -> (expEmployeeNicAmount + calcExp(
      "taxExcluded",
      "taxOnNonExcludedInc"
    )),
    YourTotalTax              -> (expTotalIncomeTaxAndNics + expTotalCgTax.max(0)),
    TotalIncomeTax            -> calcExp("taxExcluded", "taxOnNonExcludedInc"),
    NicsAndTaxPerCurrencyUnit -> expTotalAmountTaxAndNics.divideWithPrecision(expTotalIncomeBeforeTax, 4)
  )
}
