/*
 * Copyright 2024 HM Revenue & Customs
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

package utils

import models.Amount

trait AtsRawDataTransformerBaseSpec  {
  self: AtsRawDataTransformerTestHelper =>
  protected def expBasicRateIncomeTax: Amount =
    calcExp(
      "ctnIncomeChgbleBasicRate",
      "ctnSavingsChgbleLowerRate",
      "ctnTaxableRedundancyBr",
      "ctnTaxableCegBr",
      "itfStatePensionLsGrossAmt:null"
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

  protected def expTotalIncomeTaxAndNics: Amount = expEmployeeNicAmount + expTotalIncomeTax

  protected def expTotalAmountTaxAndNics: Amount = expEmployeeNicAmount + expTotalIncomeTax

  protected def expNicsAndTaxPerCurrencyUnitExclNonExclMin: Amount =
    expTotalAmountTaxAndNics.divideWithPrecision(expTotalIncomeBeforeTax, 4)

  protected def expNicsAndTaxPerCurrencyUnitExclNonExclMax: Amount = {
    val totalAmountTaxAndNics = expEmployeeNicAmount +
      expSavingsIncomeTaxDivs + expOtherAdjustmentsIncreasing - expOtherAdjustmentsReducing - calcExp(
      "ctnMarriageAllceInAmt"
    )
    totalAmountTaxAndNics.divideWithPrecision(expTotalIncomeBeforeTax, 4)
  }

  protected def expHigherRateIncomeTax: Amount = calcExp(
    "ctnIncomeChgbleHigherRate",
    "ctnSavingsChgbleHigherRate",
    "ctnTaxableRedundancyHr",
    "ctnTaxableCegHr",
    "itfStatePensionLsGrossAmt:null"
  )

  protected def expHigherRateIncomeTaxAmount: Amount =
    calcExp(
      "ctnIncomeTaxHigherRate",
      "ctnSavingsTaxHigherRate",
      "ctnTaxOnRedundancyHr",
      "ctnTaxOnCegHr",
      "ctnPensionLsumTaxDueAmt:null"
    )

  protected def expAdditionalRateIncomeTax: Amount = calcExp(
    "ctnIncomeChgbleAddHRate",
    "ctnSavingsChgbleAddHRate",
    "ctnTaxableRedundancyAhr",
    "ctnTaxableCegAhr",
    "itfStatePensionLsGrossAmt:null"
  )

  protected def expAditionalRateIncomeTaxAmount: Amount = calcExp(
    "ctnIncomeTaxAddHighRate",
    "ctnSavingsTaxAddHighRate",
    "ctnTaxOnRedundancyAhr",
    "ctnTaxOnCegAhr",
    "ctnPensionLsumTaxDueAmt:null"
  )

  protected def expScottishTotalTax: Amount = calcExp(
    "scottishStarterRateTax:null",
    "scottishBasicRateTax:null",
    "scottishIntermediateRateTax:null",
    "scottishHigherRateTax:null",
    "scottishAdditionalRateTax:null"
  )

  protected def expSelfEmployment: Amount = calcExp(
    "ctnSummaryTotalScheduleD",
    "ctnSummaryTotalPartnership",
    "ctnSavingsPartnership",
    "ctnDividendsPartnership"
  )

  protected def expTaxableStateBenefits: Amount = calcExp(
    "atsIncBenefitSuppAllowAmt",
    "atsJobSeekersAllowanceAmt",
    "atsOthStatePenBenefitsAmt"
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
