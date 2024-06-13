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

import models.LiabilityKey.{TotalIncomeBeforeTax, _}
import models._
import utils.{AtsJsonDataUpdate, AtsRawDataTransformerTestHelper}

trait ATSRawDataTransformer2023Spec extends AtsRawDataTransformerTestHelper with AtsJsonDataUpdate {

  override protected val taxYear: Int = 2023

  protected def expectedResultIncomeTax: Map[LiabilityKey, Amount] = Map(
    StartingRateForSavings          -> calcExp("ctnSavingsChgbleStartRate", "ctnTaxableCegSr"),
    StartingRateForSavingsAmount    -> calcExp("ctnSavingsTaxStartingRate", "ctnTaxOnCegSr"),
    BasicRateIncomeTax              -> calcExp(
      "ctnIncomeChgbleBasicRate",
      "ctnSavingsChgbleLowerRate",
      "ctnTaxableRedundancyBr",
      "ctnTaxableCegBr",
      "itfStatePensionLsGrossAmt:null"
    ),
    BasicRateIncomeTaxAmount        -> expBasicRateIncomeTaxAmount,
    HigherRateIncomeTax             -> calcExp(
      "ctnIncomeChgbleHigherRate",
      "ctnSavingsChgbleHigherRate",
      "ctnTaxableRedundancyHr",
      "ctnTaxableCegHr",
      "itfStatePensionLsGrossAmt:null"
    ),
    HigherRateIncomeTaxAmount       -> expHigherRateIncomeTaxAmount,
    AdditionalRateIncomeTax         -> calcExp(
      "ctnIncomeChgbleAddHRate",
      "ctnSavingsChgbleAddHRate",
      "ctnTaxableRedundancyAhr",
      "ctnTaxableCegAhr",
      "itfStatePensionLsGrossAmt:null"
    ),
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
    TotalIncomeTax                  -> calcExp("taxExcluded", "taxOnNonExcludedInc"),
    ScottishIncomeTax               -> calcExp("scottishIncomeTaxUK2023:null"),
    WelshIncomeTax                  -> calcExp("welshIncomeTax:null"),
    ScottishStarterRateTax          -> calcExp("scottishStarterRateTax:null"),
    ScottishBasicRateTax            -> calcExp("scottishBasicRateTax:null"),
    ScottishIntermediateRateTax     -> calcExp("scottishIntermediateRateTax:null"),
    ScottishHigherRateTax           -> calcExp("scottishHigherRateTax:null"),
    ScottishAdditionalRateTax       -> calcExp("scottishAdditionalRateTax:null"),
    ScottishTotalTax                -> calcExp(
      "scottishStarterRateTax:null",
      "scottishBasicRateTax:null",
      "scottishIntermediateRateTax:null",
      "scottishHigherRateTax:null",
      "scottishAdditionalRateTax:null"
    ),
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
    TaxableStateBenefits   -> calcExp(
      "atsIncBenefitSuppAllowAmt",
      "atsJobSeekersAllowanceAmt",
      "atsOthStatePenBenefitsAmt"
    ),
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
    TotalIncomeTaxAndNics     -> (expEmployeeNicAmount + calcExp(
      "taxExcluded",
      "taxOnNonExcludedInc"
    )),
    YourTotalTax              -> expYourTotalTax,
    PersonalTaxFreeAmount     -> amt(BigDecimal(12570.00), "12570.00(ctnPersonalAllowance)"),
    TotalTaxFreeAmount        -> expTotalTaxFreeAmount,
    TotalIncomeBeforeTax      -> expTotalIncomeBeforeTax,
    TotalIncomeTax            -> calcExp("taxExcluded", "taxOnNonExcludedInc"),
    TotalCgTax                -> expTotalCgTax.max(0),
    TaxableGains              -> calcExp("atsCgTotGainsAfterLosses", "atsCgGainsAfterLossesAmt"),
    CgTaxPerCurrencyUnit      -> taxPerTaxableCurrencyUnit(
      expTotalCgTax.max(0),
      expTaxableGains
    ),
    NicsAndTaxPerCurrencyUnit -> expNicsAndTaxPerCurrencyUnitExclNonExclMin
  )

  protected def expectedResultSummaryDataNonExcluded: Map[LiabilityKey, Amount] = Map(
    TotalIncomeTaxAndNics     -> expTotalIncomeTaxAndNics,
    NicsAndTaxPerCurrencyUnit -> expNicsAndTaxPerCurrencyUnitExclNonExclMax, // HERE
    CgTaxPerCurrencyUnit      -> taxPerTaxableCurrencyUnit(
      expTotalCgTax.max(0),
      expTaxableGains
    ),
    TotalIncomeBeforeTax      -> expTotalIncomeBeforeTax,
    TotalCgTax                -> expTotalCgTax.max(0),
    YourTotalTax              -> (expTotalIncomeTaxAndNics + expTotalCgTax.max(0)),
    TotalTaxFreeAmount        -> expTotalTaxFreeAmount,
    TotalIncomeTax            -> expTotalIncomeTax,
    PersonalTaxFreeAmount     -> calcExp("ctnPersonalAllowance"),
    EmployeeNicAmount         -> expEmployeeNicAmount,
    TaxableGains              -> calcExp("atsCgTotGainsAfterLosses", "atsCgGainsAfterLossesAmt")
  )

  protected def expBasicRateIncomeTaxAmount: Amount = calcExp(
    "ctnIncomeTaxBasicRate",
    "ctnSavingsTaxLowerRate",
    "ctnTaxOnRedundancyBr",
    "ctnTaxOnCegBr",
    "ctnPensionLsumTaxDueAmt:null"
  )

  protected def expEmployeeNicAmount: Amount = calcExp("employeeClass1Nic", "ctnClass2NicAmt", "class4Nic")

  protected def expOtherAllowancesAmount: Amount = calcExp(
    "ctnEmploymentExpensesAmt",
    "ctnSummaryTotalDedPpr",
    "ctnSumTotForeignTaxRelief",
    "ctnSumTotLossRestricted",
    "grossAnnuityPayts",
    "itf4GiftsInvCharitiesAmo",
    "ctnBpaAllowanceAmt",
    "itfBpaAmount"
  )

  protected def expTotalTaxFreeAmount: Amount = expOtherAllowancesAmount +
    calcExp("ctnPersonalAllowance") - calcExp("ctnMarriageAllceOutAmt")

  protected def expOtherAdjustmentsIncreasing: Amount = calcExp(
    "nonDomChargeAmount",
    "giftAidTaxReduced",
    "netAnnuityPaytsTaxDue",
    "ctnChildBenefitChrgAmt",
    "ctnPensionSavingChrgbleAmt"
  )

  protected def expOtherAdjustmentsReducing: Amount = calcExp(
    "ctnDeficiencyRelief",
    "topSlicingRelief",
    "ctnVctSharesReliefAmt",
    "ctnEisReliefAmt",
    "ctnSeedEisReliefAmt",
    "ctnCommInvTrustRelAmt",
    "ctnSocialInvTaxRelAmt",
    "atsSurplusMcaAlimonyRel",
    "alimony",
    "ctnNotionalTaxCegs",
    "ctnNotlTaxOthrSrceAmo",
    "ctnFtcrRestricted",
    "reliefForFinanceCosts",
    "lfiRelief",
    "ctnRelTaxAcctFor"
  )

  protected def expSavingsIncomeTaxDivs: Amount =
    calcExp(
      "ctnSavingsTaxStartingRate",
      "ctnTaxOnCegSr"
    ) + expBasicRateIncomeTaxAmount + expHigherRateIncomeTaxAmount + expAditionalRateIncomeTaxAmount + calcExp(
      "ctnDividendTaxLowRate",
      "ctnDividendTaxHighRate",
      "ctnDividendTaxAddHighRate"
    )

  protected def expTotalIncomeTax: Amount =
    expSavingsIncomeTaxDivs + expOtherAdjustmentsIncreasing - expOtherAdjustmentsReducing - calcExp(
      "ctnMarriageAllceInAmt"
    )

  protected def expTotalIncomeTaxAndNics: Amount =
    expEmployeeNicAmount + expTotalIncomeTax

  protected def expTotalAmountTaxAndNics: Amount = expEmployeeNicAmount + calcExp(
    "taxExcluded",
    "taxOnNonExcludedInc"
  )

  protected def expNicsAndTaxPerCurrencyUnitExclNonExclMin: Amount =
    expTotalAmountTaxAndNics.divideWithPrecision(expTotalIncomeBeforeTax, 4)

  protected def expNicsAndTaxPerCurrencyUnitExclNonExclMax: Amount = {
    val totalAmountTaxAndNics = expEmployeeNicAmount +
      expSavingsIncomeTaxDivs + expOtherAdjustmentsIncreasing - expOtherAdjustmentsReducing - calcExp(
        "ctnMarriageAllceInAmt"
      )
    totalAmountTaxAndNics.divideWithPrecision(expTotalIncomeBeforeTax, 4)
  }

  protected def expHigherRateIncomeTaxAmount: Amount =
    calcExp(
      "ctnIncomeTaxHigherRate",
      "ctnSavingsTaxHigherRate",
      "ctnTaxOnRedundancyHr",
      "ctnTaxOnCegHr",
      "ctnPensionLsumTaxDueAmt:null"
    )

  protected def expAditionalRateIncomeTaxAmount: Amount = calcExp(
    "ctnIncomeTaxAddHighRate",
    "ctnSavingsTaxAddHighRate",
    "ctnTaxOnRedundancyAhr",
    "ctnTaxOnCegAhr",
    "ctnPensionLsumTaxDueAmt:null"
  )

  protected def expSelfEmployment: Amount = calcExp(
    "ctnSummaryTotalScheduleD",
    "ctnSummaryTotalPartnership",
    "ctnSavingsPartnership",
    "ctnDividendsPartnership"
  )

  protected def expOtherIncome: Amount = calcExp(
    "ctnSummaryTotShareOptions",
    "ctnSummaryTotalUklProperty",
    "ctnSummaryTotForeignIncome",
    "ctnSummaryTotTrustEstates",
    "ctnSummaryTotalOtherIncome",
    "ctnSummaryTotalUkInterest",
    "ctnSummaryTotForeignDiv",
    "ctnSummaryTotalUkIntDivs",
    "ctn4SumTotLifePolicyGains",
    "ctnSummaryTotForeignSav",
    "ctnForeignCegDedn",
    "itfCegReceivedAfterTax"
  )

  protected def expYourTotalTax: Amount =
    expEmployeeNicAmount +
      calcExp(
        "taxExcluded",
        "taxOnNonExcludedInc"
      ) +
      expTotalCgTax.max(0)

  protected def expTotalCgTax: Amount = calcExp(
    "ctnLowerRateCgtRPCI",
    "ctnHigherRateCgtRPCI",
    "ctnCgDueEntrepreneursRate",
    "ctnCgDueLowerRate",
    "ctnCgDueHigherRate",
    "capAdjustmentAmt"
  )

  protected def expTaxableGains: Amount = calcExp("atsCgTotGainsAfterLosses", "atsCgGainsAfterLossesAmt")

  protected def expTotalIncomeBeforeTax: Amount = expSelfEmployment + calcExp(
    "ctnSummaryTotalEmployment",
    "atsStatePensionAmt",
    "atsOtherPensionAmt",
    "itfStatePensionLsGrossAmt",
    "atsIncBenefitSuppAllowAmt",
    "atsJobSeekersAllowanceAmt",
    "atsOthStatePenBenefitsAmt"
  ) + expOtherIncome + calcExp("ctnEmploymentBenefitsAmt")
}
