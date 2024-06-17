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

package transformers.ATS2023

import models.{Amount, LiabilityKey}
import models.LiabilityKey._
import utils.AtsRawDataTransformerTestFixture

trait ATSRawDataTransformerTestFixtureBase
    extends AtsRawDataTransformerTestFixture
    with AtsRawDataTransformerTestFixtureBaseCalculations {
  override protected val taxYear: Int = 2023

  // scalastyle:off method.length
  override protected def tliSlpAtsData: Map[String, BigDecimal]    = Map(
    "ctnEmploymentBenefitsAmt"   -> BigDecimal(10.00),
    "ctnSummaryTotalScheduleD"   -> BigDecimal(20.00),
    "ctnSummaryTotalPartnership" -> BigDecimal(30.00),
    "ctnSummaryTotalEmployment"  -> BigDecimal(23678.00),
    "atsStatePensionAmt"         -> BigDecimal(9783.00),
    "atsOtherPensionAmt"         -> BigDecimal(40.00),
    "itfStatePensionLsGrossAmt"  -> BigDecimal(50.00),
    "atsIncBenefitSuppAllowAmt"  -> BigDecimal(60.00),
    "atsJobSeekersAllowanceAmt"  -> BigDecimal(70.00),
    "atsOthStatePenBenefitsAmt"  -> BigDecimal(80.00),
    "ctnSummaryTotShareOptions"  -> BigDecimal(100.00),
    "ctnSummaryTotalUklProperty" -> BigDecimal(5475.00),
    "ctnSummaryTotForeignIncome" -> BigDecimal(110.00),
    "ctnSummaryTotTrustEstates"  -> BigDecimal(120.00),
    "ctnSummaryTotalOtherIncome" -> BigDecimal(130.00),
    "ctnSummaryTotForeignSav"    -> BigDecimal(140.00),
    "ctnForeignCegDedn"          -> BigDecimal(150.00),
    "ctnSummaryTotalUkInterest"  -> BigDecimal(3678.00),
    "itfCegReceivedAfterTax"     -> BigDecimal(160.00),
    "ctnSummaryTotForeignDiv"    -> BigDecimal(170.00),
    "ctnSummaryTotalUkIntDivs"   -> BigDecimal(12750.00),
    "ctn4SumTotLifePolicyGains"  -> BigDecimal(180.00),
    "ctnPersonalAllowance"       -> BigDecimal(12570.00),
    "ctnEmploymentExpensesAmt"   -> BigDecimal(1900.00),
    "ctnSummaryTotalDedPpr"      -> BigDecimal(200.00),
    "ctnSumTotForeignTaxRelief"  -> BigDecimal(210.00),
    "ctnSumTotLoanRestricted"    -> BigDecimal(220.00),
    "ctnSumTotLossRestricted"    -> BigDecimal(230.00),
    "grossAnnuityPayts"          -> BigDecimal(240.00),
    "itf4GiftsInvCharitiesAmo"   -> BigDecimal(250.00),
    "itfTradeUnionDeathBenefits" -> BigDecimal(260.00),
    "ctnBpaAllowanceAmt"         -> BigDecimal(270.00),
    "itfBpaAmount"               -> BigDecimal(280.00),
    "grossExcludedIncome"        -> BigDecimal(290.00),
    "class4Nic"                  -> BigDecimal(300.00),
    "ctnClass2NicAmt"            -> BigDecimal(310.00),
    "ctnSavingsChgbleStartRate"  -> BigDecimal(320.00),
    "ctnSavingsTaxStartingRate"  -> BigDecimal(330.00),
    "ctnIncomeChgbleBasicRate"   -> BigDecimal(17419.00),
    "ctnSavingsChgbleLowerRate"  -> BigDecimal(2678.00),
    "ctnIncomeTaxBasicRate"      -> BigDecimal(3483.80),
    "ctnSavingsTaxLowerRate"     -> BigDecimal(535.60),
    "ctnIncomeChgbleHigherRate"  -> BigDecimal(340.00),
    "ctnSavingsChgbleHigherRate" -> BigDecimal(350.00),
    "ctnIncomeTaxHigherRate"     -> BigDecimal(360.00),
    "ctnSavingsTaxHigherRate"    -> BigDecimal(370.00),
    "ctnIncomeChgbleAddHRate"    -> BigDecimal(380.00),
    "ctnSavingsChgbleAddHRate"   -> BigDecimal(390.00),
    "ctnIncomeTaxAddHighRate"    -> BigDecimal(400.00),
    "ctnSavingsTaxAddHighRate"   -> BigDecimal(410.00),
    "taxablePaySSR"              -> BigDecimal(2097.00),
    "taxOnPaySSR"                -> BigDecimal(398.43),
    "taxablePaySIR"              -> BigDecimal(6850.00),
    "taxOnPaySIR"                -> BigDecimal(1438.50),
    "ctnDividendChgbleLowRate"   -> BigDecimal(10750.00),
    "ctnDividendTaxLowRate"      -> BigDecimal(806.25),
    "ctnDividendChgbleHighRate"  -> BigDecimal(420.00),
    "ctnDividendTaxHighRate"     -> BigDecimal(430.00),
    "ctnDividendChgbleAddHRate"  -> BigDecimal(440.00),
    "ctnDividendTaxAddHighRate"  -> BigDecimal(450.00),
    "ctnTaxableRedundancySSR"    -> BigDecimal(460.00),
    "ctnTaxOnRedundancySsr"      -> BigDecimal(470.00),
    "ctnTaxableRedundancyBr"     -> BigDecimal(480.00),
    "ctnTaxOnRedundancyBr"       -> BigDecimal(490.00),
    "ctnTaxableRedundancySir"    -> BigDecimal(500.00),
    "ctnTaxOnRedundancySir"      -> BigDecimal(510.00),
    "ctnTaxableRedundancyHr"     -> BigDecimal(520.00),
    "ctnTaxOnRedundancyHr"       -> BigDecimal(530.00),
    "ctnTaxableRedundancyAhr"    -> BigDecimal(540.00),
    "ctnTaxOnRedundancyAhr"      -> BigDecimal(550.00),
    "ctnTaxableCegBr"            -> BigDecimal(560.00),
    "ctnTaxOnCegBr"              -> BigDecimal(570.00),
    "ctnTaxableCegHr"            -> BigDecimal(580.00),
    "ctnTaxOnCegHr"              -> BigDecimal(590.00),
    "ctnTaxableCegAhr"           -> BigDecimal(600.00),
    "ctnTaxOnCegAhr"             -> BigDecimal(610.00),
    "nonDomChargeAmount"         -> BigDecimal(620.00),
    "taxExcluded"                -> BigDecimal(40000.00),
    "taxOnNonExcludedInc"        -> BigDecimal(60000.00),
    "incomeTaxDue"               -> BigDecimal(6162.58),
    "ctn4TaxDueAfterAllceRlf"    -> BigDecimal(6162.58),
    "netAnnuityPaytsTaxDue"      -> BigDecimal(650.00),
    "ctnChildBenefitChrgAmt"     -> BigDecimal(660.00),
    "ctnPensionSavingChrgbleAmt" -> BigDecimal(670.00),
    "atsTaxCharged"              -> BigDecimal(6662.58),
    "ctnDeficiencyRelief"        -> BigDecimal(680.00),
    "topSlicingRelief"           -> BigDecimal(690.00),
    "ctnVctSharesReliefAmt"      -> BigDecimal(700.00),
    "ctnEisReliefAmt"            -> BigDecimal(710.00),
    "ctnSeedEisReliefAmt"        -> BigDecimal(720.00),
    "ctnCommInvTrustRelAmt"      -> BigDecimal(730.00),
    "atsSurplusMcaAlimonyRel"    -> BigDecimal(740.00),
    "alimony"                    -> BigDecimal(750.00),
    "ctnNotionalTaxCegs"         -> BigDecimal(760.00),
    "ctnNotlTaxOthrSrceAmo"      -> BigDecimal(770.00),
    "ctnFtcrRestricted"          -> BigDecimal(780.00),
    "reliefForFinanceCosts"      -> BigDecimal(500.00),
    "lfiRelief"                  -> BigDecimal(790.00),
    "ctnRelTaxAcctFor"           -> BigDecimal(10.00),
    "ctnTaxCredForDivs"          -> BigDecimal(800.00),
    "ctnQualDistnReliefAmt"      -> BigDecimal(810.00),
    "figTotalTaxCreditRelief"    -> BigDecimal(820.00),
    "ctnNonPayableTaxCredits"    -> BigDecimal(830.00),
    "atsCgTotGainsAfterLosses"   -> BigDecimal(840.00),
    "atsCgGainsAfterLossesAmt"   -> BigDecimal(850.00),
    "cap3AssessableChgeableGain" -> BigDecimal(860.00),
    "atsCgAnnualExemptAmt"       -> BigDecimal(12300.00),
    "ctnCgAtEntrepreneursRate"   -> BigDecimal(870.00),
    "ctnCgDueEntrepreneursRate"  -> BigDecimal(880.00),
    "ctnCgAtLowerRate"           -> BigDecimal(890.00),
    "ctnCgDueLowerRate"          -> BigDecimal(900.00),
    "ctnCgAtHigherRate"          -> BigDecimal(910.00),
    "ctnCgDueHigherRate"         -> BigDecimal(920.00),
    "ctnCGAtLowerRateRPCI"       -> BigDecimal(930.00),
    "ctnLowerRateCgtRPCI"        -> BigDecimal(940.00),
    "ctnCGAtHigherRateRPCI"      -> BigDecimal(950.00),
    "ctnHigherRateCgtRPCI"       -> BigDecimal(960.00),
    "capAdjustmentAmt"           -> BigDecimal(970.00),
    "ctnPensionLsumTaxDueAmt"    -> BigDecimal(980.00),
    "ctnMarriageAllceInAmt"      -> BigDecimal(990.00),
    "ctnMarriageAllceOutAmt"     -> BigDecimal(1000.00),
    "ctnSocialInvTaxRelAmt"      -> BigDecimal(1010.00),
    "ctnSavingsPartnership"      -> BigDecimal(1020.00),
    "ctnDividendsPartnership"    -> BigDecimal(1030.00),
    "giftAidTaxReduced"          -> BigDecimal(1040.00),
    "ctnTaxableCegSr"            -> BigDecimal(1050.00),
    "ctnTaxOnCegSr"              -> BigDecimal(1060.00),
    "ctnTaxableRedundancySsr"    -> BigDecimal(1070.00)
  ).map(item => item._1 -> item._2.setScale(2))

  override protected def saPayeNicDetails: Map[String, BigDecimal] = Map(
    "employeeClass1Nic" -> BigDecimal(1080.00),
    "employeeClass2Nic" -> BigDecimal(200.00),
    "employerNic"       -> BigDecimal(0.00)
  ).map(item => item._1 -> item._2.setScale(2))

  override def expectedResultIncomeTax: Map[LiabilityKey, Amount]  = Map(
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

  override def expectedResultIncomeData: Map[LiabilityKey, Amount] = Map(
    SelfEmploymentIncome   -> expSelfEmployment,
    IncomeFromEmployment   -> calcExp("ctnSummaryTotalEmployment"),
    StatePension           -> calcExp("atsStatePensionAmt"),
    OtherPensionIncome     -> calcExp("atsOtherPensionAmt", "itfStatePensionLsGrossAmt"),
    TaxableStateBenefits   -> expTaxableStateBenefits,
    OtherIncome            -> expOtherIncome,
    BenefitsFromEmployment -> calcExp("ctnEmploymentBenefitsAmt"),
    TotalIncomeBeforeTax   -> expTotalIncomeBeforeTax
  )

  override def expectedResultAllowanceData: Map[LiabilityKey, Amount] = Map(
    PersonalTaxFreeAmount              -> calcExp("ctnPersonalAllowance"),
    MarriageAllowanceTransferredAmount -> calcExp("ctnMarriageAllceOutAmt"),
    OtherAllowancesAmount              -> expOtherAllowancesAmount,
    TotalTaxFreeAmount                 -> expTotalTaxFreeAmount
  )

  override def expectedResultCapitalGainsData: Map[LiabilityKey, Amount] = Map(
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
    CgTaxPerCurrencyUnit         -> expTotalCgTax.max(0).divideWithPrecision(expTaxableGains, 4),
    AmountAtRPCILowerRate        -> calcExp("ctnCGAtLowerRateRPCI"),
    AmountDueRPCILowerRate       -> calcExp("ctnLowerRateCgtRPCI"),
    AmountAtRPCIHigheRate        -> calcExp("ctnCGAtHigherRateRPCI"),
    AmountDueRPCIHigherRate      -> calcExp("ctnHigherRateCgtRPCI")
  )

  override def expectedResultSummaryData: Map[LiabilityKey, Amount] = Map(
    EmployeeNicAmount         -> expEmployeeNicAmount,
    TotalIncomeTaxAndNics     -> expTotalIncomeTaxAndNics,
    YourTotalTax              -> (expTotalIncomeTaxAndNics + expTotalCgTax.max(0)),
    PersonalTaxFreeAmount     -> calcExp("ctnPersonalAllowance"),
    TotalTaxFreeAmount        -> expTotalTaxFreeAmount,
    TotalIncomeBeforeTax      -> expTotalIncomeBeforeTax,
    TotalIncomeTax            -> expTotalIncomeTax,
    TotalCgTax                -> expTotalCgTax.max(0),
    TaxableGains              -> calcExp("atsCgTotGainsAfterLosses", "atsCgGainsAfterLossesAmt"),
    CgTaxPerCurrencyUnit      -> expTotalCgTax.max(0).divideWithPrecision(expTaxableGains, 4),
    NicsAndTaxPerCurrencyUnit -> expNicsAndTaxPerCurrencyUnitExclNonExclMax
  )
}

trait AtsRawDataTransformerTestFixtureBaseCalculations {
  self: AtsRawDataTransformerTestFixture =>
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

  protected def expTotalAmountTaxAndNics: Amount = expEmployeeNicAmount + calcExp(
    "taxExcluded",
    "taxOnNonExcludedInc"
  )

  protected def expTotalIncomeTaxAndNics: Amount = expEmployeeNicAmount + expTotalIncomeTax

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
