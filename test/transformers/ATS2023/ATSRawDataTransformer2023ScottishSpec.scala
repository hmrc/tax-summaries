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
import models.RateKey._
import models._
import utils.{AtsJsonDataUpdate, AtsRawDataTransformerTestHelper}

// TODO:-
//   Move code into helper class
//   Copy to England class (or add to existing class?) and get that working, do diff scenarios to cover everything.
//   I don't need to use calcExp for every field - the more complex ones I can just hard coded exp values
//    (as a WIP could over time improve this and reduce the no of hard coded values year on year)

class ATSRawDataTransformer2023ScottishSpec extends AtsRawDataTransformerTestHelper with AtsJsonDataUpdate {
  import ATSRawDataTransformer2023ScottishSpec._

  override protected val taxYear: Int                           = 2023
  override protected val incomeTaxStatus: String                = "0002"
  override protected val tliSlpAtsData: Map[String, BigDecimal] = Map(
    "ctnEmploymentBenefitsAmt"   -> BigDecimal(0.00),
    "ctnSummaryTotalScheduleD"   -> BigDecimal(0.00),
    "ctnSummaryTotalPartnership" -> BigDecimal(0.00),
    "ctnSummaryTotalEmployment"  -> BigDecimal(23678.00),
    "atsStatePensionAmt"         -> BigDecimal(9783.00),
    "atsOtherPensionAmt"         -> BigDecimal(0.00),
    "itfStatePensionLsGrossAmt"  -> BigDecimal(0.00),
    "atsIncBenefitSuppAllowAmt"  -> BigDecimal(0.00),
    "atsJobSeekersAllowanceAmt"  -> BigDecimal(0.00),
    "atsOthStatePenBenefitsAmt"  -> BigDecimal(0.00),
    "ctnSummaryTotShareOptions"  -> BigDecimal(0.00),
    "ctnSummaryTotalUklProperty" -> BigDecimal(5475.00),
    "ctnSummaryTotForeignIncome" -> BigDecimal(0.00),
    "ctnSummaryTotTrustEstates"  -> BigDecimal(0.00),
    "ctnSummaryTotalOtherIncome" -> BigDecimal(0.00),
    "ctnSummaryTotForeignSav"    -> BigDecimal(0.00),
    "ctnForeignCegDedn"          -> BigDecimal(0.00),
    "ctnSummaryTotalUkInterest"  -> BigDecimal(3678.00),
    "itfCegReceivedAfterTax"     -> BigDecimal(0.00),
    "ctnSummaryTotForeignDiv"    -> BigDecimal(0.00),
    "ctnSummaryTotalUkIntDivs"   -> BigDecimal(12750.00),
    "ctn4SumTotLifePolicyGains"  -> BigDecimal(0.00),
    "ctnPersonalAllowance"       -> BigDecimal(12570.00),
    "ctnEmploymentExpensesAmt"   -> BigDecimal(0.00),
    "ctnSummaryTotalDedPpr"      -> BigDecimal(0.00),
    "ctnSumTotForeignTaxRelief"  -> BigDecimal(0.00),
    "ctnSumTotLoanRestricted"    -> BigDecimal(0.00),
    "ctnSumTotLossRestricted"    -> BigDecimal(0.00),
    "grossAnnuityPayts"          -> BigDecimal(0.00),
    "itf4GiftsInvCharitiesAmo"   -> BigDecimal(0.00),
    "itfTradeUnionDeathBenefits" -> BigDecimal(0.00),
    "ctnBpaAllowanceAmt"         -> BigDecimal(0.00),
    "itfBpaAmount"               -> BigDecimal(0.00),
    "grossExcludedIncome"        -> BigDecimal(0.00),
    "class4Nic"                  -> BigDecimal(0.00),
    "ctnClass2NicAmt"            -> BigDecimal(0.00),
    "ctnSavingsChgbleStartRate"  -> BigDecimal(0.00),
    "ctnSavingsTaxStartingRate"  -> BigDecimal(0.00),
    "ctnIncomeChgbleBasicRate"   -> BigDecimal(17419.00),
    "ctnSavingsChgbleLowerRate"  -> BigDecimal(2678.00),
    "ctnIncomeTaxBasicRate"      -> BigDecimal(3483.80),
    "ctnSavingsTaxLowerRate"     -> BigDecimal(535.60),
    "ctnIncomeChgbleHigherRate"  -> BigDecimal(0.00),
    "ctnSavingsChgbleHigherRate" -> BigDecimal(0.00),
    "ctnIncomeTaxHigherRate"     -> BigDecimal(0.00),
    "ctnSavingsTaxHigherRate"    -> BigDecimal(0.00),
    "ctnIncomeChgbleAddHRate"    -> BigDecimal(0.00),
    "ctnSavingsChgbleAddHRate"   -> BigDecimal(0.00),
    "ctnIncomeTaxAddHighRate"    -> BigDecimal(0.00),
    "ctnSavingsTaxAddHighRate"   -> BigDecimal(0.00),
    "taxablePaySSR"              -> BigDecimal(2097.00),
    "taxOnPaySSR"                -> BigDecimal(398.43),
    "taxablePaySIR"              -> BigDecimal(6850.00),
    "taxOnPaySIR"                -> BigDecimal(1438.50),
    "ctnDividendChgbleLowRate"   -> BigDecimal(10750.00),
    "ctnDividendTaxLowRate"      -> BigDecimal(806.25),
    "ctnDividendChgbleHighRate"  -> BigDecimal(0.00),
    "ctnDividendTaxHighRate"     -> BigDecimal(0.00),
    "ctnDividendChgbleAddHRate"  -> BigDecimal(0.00),
    "ctnDividendTaxAddHighRate"  -> BigDecimal(0.00),
    "ctnTaxableRedundancySSR"    -> BigDecimal(0.00),
    "ctnTaxOnRedundancySsr"      -> BigDecimal(0.00),
    "ctnTaxableRedundancyBr"     -> BigDecimal(0.00),
    "ctnTaxOnRedundancyBr"       -> BigDecimal(0.00),
    "ctnTaxableRedundancySir"    -> BigDecimal(0.00),
    "ctnTaxOnRedundancySir"      -> BigDecimal(0.00),
    "ctnTaxableRedundancyHr"     -> BigDecimal(0.00),
    "ctnTaxOnRedundancyHr"       -> BigDecimal(0.00),
    "ctnTaxableRedundancyAhr"    -> BigDecimal(0.00),
    "ctnTaxOnRedundancyAhr"      -> BigDecimal(0.00),
    "ctnTaxableCegBr"            -> BigDecimal(0.00),
    "ctnTaxOnCegBr"              -> BigDecimal(0.00),
    "ctnTaxableCegHr"            -> BigDecimal(0.00),
    "ctnTaxOnCegHr"              -> BigDecimal(0.00),
    "ctnTaxableCegAhr"           -> BigDecimal(0.00),
    "ctnTaxOnCegAhr"             -> BigDecimal(0.00),
    "nonDomChargeAmount"         -> BigDecimal(0.00),
    "taxExcluded"                -> BigDecimal(0.00),
    "taxOnNonExcludedInc"        -> BigDecimal(0.00),
    "incomeTaxDue"               -> BigDecimal(6162.58),
    "ctn4TaxDueAfterAllceRlf"    -> BigDecimal(6162.58),
    "netAnnuityPaytsTaxDue"      -> BigDecimal(0.00),
    "ctnChildBenefitChrgAmt"     -> BigDecimal(0.00),
    "ctnPensionSavingChrgbleAmt" -> BigDecimal(0.00),
    "atsTaxCharged"              -> BigDecimal(6662.58),
    "ctnDeficiencyRelief"        -> BigDecimal(0.00),
    "topSlicingRelief"           -> BigDecimal(0.00),
    "ctnVctSharesReliefAmt"      -> BigDecimal(0.00),
    "ctnEisReliefAmt"            -> BigDecimal(0.00),
    "ctnSeedEisReliefAmt"        -> BigDecimal(0.00),
    "ctnCommInvTrustRelAmt"      -> BigDecimal(0.00),
    "atsSurplusMcaAlimonyRel"    -> BigDecimal(0.00),
    "alimony"                    -> BigDecimal(0.00),
    "ctnNotionalTaxCegs"         -> BigDecimal(0.00),
    "ctnNotlTaxOthrSrceAmo"      -> BigDecimal(0.00),
    "ctnFtcrRestricted"          -> BigDecimal(0.00),
    "reliefForFinanceCosts"      -> BigDecimal(500.00),
    "lfiRelief"                  -> BigDecimal(0.00),
    "ctnRelTaxAcctFor"           -> BigDecimal(10.00),
    "ctnTaxCredForDivs"          -> BigDecimal(0.00),
    "ctnQualDistnReliefAmt"      -> BigDecimal(0.00),
    "figTotalTaxCreditRelief"    -> BigDecimal(0.00),
    "ctnNonPayableTaxCredits"    -> BigDecimal(0.00),
    "atsCgTotGainsAfterLosses"   -> BigDecimal(0.00),
    "atsCgGainsAfterLossesAmt"   -> BigDecimal(0.00),
    "cap3AssessableChgeableGain" -> BigDecimal(0.00),
    "atsCgAnnualExemptAmt"       -> BigDecimal(12300.00),
    "ctnCgAtEntrepreneursRate"   -> BigDecimal(0.00),
    "ctnCgDueEntrepreneursRate"  -> BigDecimal(0.00),
    "ctnCgAtLowerRate"           -> BigDecimal(0.00),
    "ctnCgDueLowerRate"          -> BigDecimal(0.00),
    "ctnCgAtHigherRate"          -> BigDecimal(0.00),
    "ctnCgDueHigherRate"         -> BigDecimal(0.00),
    "ctnCGAtLowerRateRPCI"       -> BigDecimal(0.00),
    "ctnLowerRateCgtRPCI"        -> BigDecimal(0.00),
    "ctnCGAtHigherRateRPCI"      -> BigDecimal(0.00),
    "ctnHigherRateCgtRPCI"       -> BigDecimal(0.00),
    "capAdjustmentAmt"           -> BigDecimal(0.00),
    "ctnPensionLsumTaxDueAmt"    -> BigDecimal(0.00),
    "ctnMarriageAllceInAmt"      -> BigDecimal(0.00),
    "ctnMarriageAllceOutAmt"     -> BigDecimal(0.00),
    "ctnSocialInvTaxRelAmt"      -> BigDecimal(0.00),
    "ctnSavingsPartnership"      -> BigDecimal(0.00),
    "ctnDividendsPartnership"    -> BigDecimal(0.00),
    "giftAidTaxReduced"          -> BigDecimal(0.00),
    "ctnTaxableCegSr"            -> BigDecimal(0.00),
    "ctnTaxOnCegSr"              -> BigDecimal(0.00),
    "ctnTaxableRedundancySsr"    -> BigDecimal(0.00)
  ).map(item => item._1 -> item._2.setScale(2))

  s"atsDataDTO for incomeTaxStatus (i.e. country) $incomeTaxStatus and tax year $taxYear" must {
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

    behave like atsRawDataTransformer(
      transformedData = transformedData,
      expResultIncomeTax = expectedResultIncomeTax,
      expResultIncomeData = expectedResultIncomeData,
      expResultCapitalGainsData = expectedResultCGData,
      expResultAllowanceData = expectedResultAllowanceData,
      expResultSummaryData = expectedResultSummaryData
    )
  }

  private def expectedResultIncomeTax: Map[LiabilityKey, Amount] = Map(
    StartingRateForSavingsAmount    -> calcExp("savingsRateAmountScottish2023:null"),
    OtherAdjustmentsReducing        -> calcExp(fieldsOtherAdjustmentsReducing: _*),
    UpperRate                       -> calcExp("ctnDividendChgbleHighRate"),
    SavingsLowerIncome              -> calcExp("ctnSavingsChgbleLowerRate"),
    SavingsLowerRateTax             -> calcExp("ctnSavingsTaxLowerRate"),
    ScottishIncomeTax               -> calcExp("scottishIncomeTaxScottish2023:null"),
    ScottishIntermediateRateTax     -> calcExp("taxOnPaySIR", "ctnTaxOnRedundancySir", "ctnPensionLsumTaxDueAmt:null"),
    MarriageAllowanceReceivedAmount -> calcExp("ctnMarriageAllceInAmt"),
    OrdinaryRateAmount              -> calcExp("ctnDividendTaxLowRate"),
    ScottishHigherIncome            -> calcExp(
      "ctnIncomeChgbleHigherRate",
      "ctnTaxableRedundancyHr",
      "itfStatePensionLsGrossAmt:null"
    ),
    ScottishStarterRateTax          -> calcExp("taxOnPaySSR", "ctnTaxOnRedundancySsr", "ctnPensionLsumTaxDueAmt:null"),
    AdditionalRate                  -> calcExp("ctnDividendChgbleAddHRate"),
    StartingRateForSavings          -> calcExp("savingsRateScottish2023:null"),
    AdditionalRateIncomeTax         -> calcExp("additionalRateIncomeTaxScottish2023:null"),
    SavingsAdditionalIncome         -> calcExp("ctnSavingsChgbleAddHRate"),
    SavingsHigherIncome             -> calcExp("ctnSavingsChgbleHigherRate"),
    ScottishAdditionalRateTax       -> calcExp(
      "ctnIncomeTaxAddHighRate",
      "ctnTaxOnRedundancyAhr",
      "ctnPensionLsumTaxDueAmt:null"
    ),
    OtherAdjustmentsIncreasing      -> calcExp(fieldsOtherAdjustmentsIncreasing: _*),
    HigherRateIncomeTax             -> calcExp("higherRateIncomeTaxScottish2023:null"),
    ScottishBasicRateTax            -> calcExp("ctnIncomeTaxBasicRate", "ctnTaxOnRedundancyBr", "ctnPensionLsumTaxDueAmt:null"),
    BasicRateIncomeTaxAmount        -> calcExp("basicRateIncomeTaxAmountScottish2023:null"),
    AdditionalRateAmount            -> calcExp("ctnDividendTaxAddHighRate"),
    WelshIncomeTax                  -> calcExp("welshIncomeTax:null"),
    ScottishAdditionalIncome        -> calcExp(
      "ctnIncomeChgbleAddHRate",
      "ctnTaxableRedundancyAhr",
      "itfStatePensionLsGrossAmt:null"
    ),
    ScottishIntermediateIncome      -> calcExp("taxablePaySIR", "ctnTaxableRedundancySir", "itfStatePensionLsGrossAmt:null"),
    UpperRateAmount                 -> calcExp("ctnDividendTaxHighRate"),
    AdditionalRateIncomeTaxAmount   -> calcExp("additionalRateIncomeTaxAmountScottish2023:null"),
    ScottishBasicIncome             -> calcExp(
      "ctnIncomeChgbleBasicRate",
      "ctnTaxableRedundancyBr",
      "itfStatePensionLsGrossAmt:null"
    ),
    ScottishTotalTax                -> calcExp(
      "taxOnPaySSR",
      "ctnTaxOnRedundancySsr",
      "ctnPensionLsumTaxDueAmt:null",
      "ctnIncomeTaxBasicRate",
      "ctnTaxOnRedundancyBr",
      "ctnPensionLsumTaxDueAmt:null",
      "taxOnPaySIR",
      "ctnTaxOnRedundancySir",
      "ctnPensionLsumTaxDueAmt:null",
      "ctnIncomeTaxHigherRate",
      "ctnTaxOnRedundancyHr",
      "ctnPensionLsumTaxDueAmt:null",
      "ctnIncomeTaxAddHighRate",
      "ctnTaxOnRedundancyAhr",
      "ctnPensionLsumTaxDueAmt:null"
    ),
    BasicRateIncomeTax              -> calcExp("basicRateIncomeTaxScottish2023:null"),
    SavingsAdditionalRateTax        -> calcExp("ctnSavingsTaxAddHighRate", "ctnTaxOnCegAhr"),
    HigherRateIncomeTaxAmount       -> calcExp("higherRateIncomeTaxAmountScottish2023:null"),
    TotalIncomeTax                  -> expTotalIncomeTax,
    SavingsHigherRateTax            -> calcExp("ctnSavingsTaxHigherRate"),
    OrdinaryRate                    -> calcExp("ctnDividendChgbleLowRate"),
    ScottishHigherRateTax           -> calcExp("ctnIncomeTaxHigherRate", "ctnTaxOnRedundancyHr", "ctnPensionLsumTaxDueAmt:null"),
    ScottishStarterIncome           -> calcExp("taxablePaySSR", "ctnTaxableRedundancySsr", "itfStatePensionLsGrossAmt:null")
  )

  private def expectedResultIncomeData: Map[LiabilityKey, Amount] = Map(
    SelfEmploymentIncome   -> calcExp(fieldsSelfEmployment: _*),
    IncomeFromEmployment   -> calcExp("ctnSummaryTotalEmployment"),
    StatePension           -> calcExp("atsStatePensionAmt"),
    OtherPensionIncome     -> calcExp("atsOtherPensionAmt", "itfStatePensionLsGrossAmt"),
    TotalIncomeBeforeTax   -> calcExp(fieldsTotalIncomeBeforeTax: _*),
    OtherIncome            -> calcExp(fieldsOtherIncome: _*),
    BenefitsFromEmployment -> calcExp("ctnEmploymentBenefitsAmt"),
    TaxableStateBenefits   -> calcExp(
      "atsIncBenefitSuppAllowAmt",
      "atsJobSeekersAllowanceAmt",
      "atsOthStatePenBenefitsAmt"
    )
  )

  private def expectedResultAllowanceData: Map[LiabilityKey, Amount] = Map(
    PersonalTaxFreeAmount              -> calcExp("ctnPersonalAllowance"),
    MarriageAllowanceTransferredAmount -> calcExp("ctnMarriageAllceOutAmt"),
    OtherAllowancesAmount              -> calcExp(
      "ctnEmploymentExpensesAmt",
      "ctnSummaryTotalDedPpr",
      "ctnSumTotForeignTaxRelief",
      "ctnSumTotLossRestricted",
      "grossAnnuityPayts",
      "itf4GiftsInvCharitiesAmo",
      "ctnBpaAllowanceAmt",
      "itfBpaAmount"
    ),
    TotalTaxFreeAmount                 -> (calcExp(
      "ctnEmploymentExpensesAmt",
      "ctnSummaryTotalDedPpr",
      "ctnSumTotForeignTaxRelief",
      "ctnSumTotLossRestricted",
      "grossAnnuityPayts",
      "itf4GiftsInvCharitiesAmo",
      "ctnBpaAllowanceAmt",
      "itfBpaAmount",
      "ctnPersonalAllowance"
    ) - calcExp("ctnMarriageAllceOutAmt"))
  )

  private def expectedResultCGData: Map[LiabilityKey, Amount] = Map(
    AmountDueRPCILowerRate       -> calcExp("ctnLowerRateCgtRPCI"),
    AmountAtHigherRate           -> calcExp("ctnCgAtHigherRate"),
    Adjustments                  -> calcExp("capAdjustmentAmt"),
    AmountAtOrdinaryRate         -> calcExp("ctnCgAtLowerRate"),
    AmountAtRPCIHigheRate        -> calcExp("ctnCGAtHigherRateRPCI"),
    AmountDueAtEntrepreneursRate -> calcExp("ctnCgDueEntrepreneursRate"),
    CgTaxPerCurrencyUnit         -> calcExp(fieldsTaxableGains: _*),
    TaxableGains                 -> calcExp(fieldsTaxableGains: _*),
    AmountDueAtOrdinaryRate      -> calcExp("ctnCgDueLowerRate"),
    PayCgTaxOn                   -> expPayCapitalGainsTaxOn,
    TotalCgTax                   -> calcExp(fieldsTotalCgTax: _*).max(0),
    AmountAtEntrepreneursRate    -> calcExp("ctnCgAtEntrepreneursRate"),
    LessTaxFreeAmount            -> calcExp("atsCgAnnualExemptAmt"),
    AmountDueRPCIHigherRate      -> calcExp("ctnHigherRateCgtRPCI"),
    AmountDueAtHigherRate        -> calcExp("ctnCgDueHigherRate"),
    AmountAtRPCILowerRate        -> calcExp("ctnCGAtLowerRateRPCI")
  )

  private def expectedResultSummaryData: Map[LiabilityKey, Amount] = Map(
    TotalIncomeTaxAndNics     -> amt(
      BigDecimal(6252.58),
      "100.00(employeeClass1Nic) + 0.00(ctnClass2NicAmt) + 0.00(class4Nic) + null (savingsRateAmountScottish2023) + null (basicRateIncomeTaxAmountScottish2023) + null (higherRateIncomeTaxAmountScottish2023) + null (additionalRateIncomeTaxAmountScottish2023) + 806.25(ctnDividendTaxLowRate) + 0.00(ctnDividendTaxHighRate) + 0.00(ctnDividendTaxAddHighRate) + 0.00(nonDomChargeAmount) + 0.00(giftAidTaxReduced) + 0.00(netAnnuityPaytsTaxDue) + 0.00(ctnChildBenefitChrgAmt) + 0.00(ctnPensionSavingChrgbleAmt) - 0.00(ctnDeficiencyRelief) + 0.00(topSlicingRelief) + 0.00(ctnVctSharesReliefAmt) + 0.00(ctnEisReliefAmt) + 0.00(ctnSeedEisReliefAmt) + 0.00(ctnCommInvTrustRelAmt) + 0.00(ctnSocialInvTaxRelAmt) + 0.00(atsSurplusMcaAlimonyRel) + 0.00(alimony) + 0.00(ctnNotionalTaxCegs) + 0.00(ctnNotlTaxOthrSrceAmo) + 0.00(ctnFtcrRestricted) + 500.00(reliefForFinanceCosts) + 0.00(lfiRelief) + 10.00(ctnRelTaxAcctFor) - 0.00(ctnMarriageAllceInAmt) + 398.43(taxOnPaySSR) + 0.00(ctnTaxOnRedundancySsr) + null (ctnPensionLsumTaxDueAmt) + 3483.80(ctnIncomeTaxBasicRate) + 0.00(ctnTaxOnRedundancyBr) + null (ctnPensionLsumTaxDueAmt) + 1438.50(taxOnPaySIR) + 0.00(ctnTaxOnRedundancySir) + null (ctnPensionLsumTaxDueAmt) + 0.00(ctnIncomeTaxHigherRate) + 0.00(ctnTaxOnRedundancyHr) + null (ctnPensionLsumTaxDueAmt) + 0.00(ctnIncomeTaxAddHighRate) + 0.00(ctnTaxOnRedundancyAhr) + null (ctnPensionLsumTaxDueAmt) + 535.60(ctnSavingsTaxLowerRate) + 0.00(ctnSavingsTaxHigherRate) + 0.00(ctnSavingsTaxAddHighRate) + 0.00(ctnTaxOnCegAhr)"
    ),
    NicsAndTaxPerCurrencyUnit -> amt(
      BigDecimal(0.1129),
      "100.00(employeeClass1Nic) + 0.00(ctnClass2NicAmt) + 0.00(class4Nic) + null (savingsRateAmountScottish2023) + null (basicRateIncomeTaxAmountScottish2023) + null (higherRateIncomeTaxAmountScottish2023) + null (additionalRateIncomeTaxAmountScottish2023) + 806.25(ctnDividendTaxLowRate) + 0.00(ctnDividendTaxHighRate) + 0.00(ctnDividendTaxAddHighRate) + 0.00(nonDomChargeAmount) + 0.00(giftAidTaxReduced) + 0.00(netAnnuityPaytsTaxDue) + 0.00(ctnChildBenefitChrgAmt) + 0.00(ctnPensionSavingChrgbleAmt) - 0.00(ctnDeficiencyRelief) + 0.00(topSlicingRelief) + 0.00(ctnVctSharesReliefAmt) + 0.00(ctnEisReliefAmt) + 0.00(ctnSeedEisReliefAmt) + 0.00(ctnCommInvTrustRelAmt) + 0.00(ctnSocialInvTaxRelAmt) + 0.00(atsSurplusMcaAlimonyRel) + 0.00(alimony) + 0.00(ctnNotionalTaxCegs) + 0.00(ctnNotlTaxOthrSrceAmo) + 0.00(ctnFtcrRestricted) + 500.00(reliefForFinanceCosts) + 0.00(lfiRelief) + 10.00(ctnRelTaxAcctFor) - 0.00(ctnMarriageAllceInAmt) + 398.43(taxOnPaySSR) + 0.00(ctnTaxOnRedundancySsr) + null (ctnPensionLsumTaxDueAmt) + 3483.80(ctnIncomeTaxBasicRate) + 0.00(ctnTaxOnRedundancyBr) + null (ctnPensionLsumTaxDueAmt) + 1438.50(taxOnPaySIR) + 0.00(ctnTaxOnRedundancySir) + null (ctnPensionLsumTaxDueAmt) + 0.00(ctnIncomeTaxHigherRate) + 0.00(ctnTaxOnRedundancyHr) + null (ctnPensionLsumTaxDueAmt) + 0.00(ctnIncomeTaxAddHighRate) + 0.00(ctnTaxOnRedundancyAhr) + null (ctnPensionLsumTaxDueAmt) + 535.60(ctnSavingsTaxLowerRate) + 0.00(ctnSavingsTaxHigherRate) + 0.00(ctnSavingsTaxAddHighRate) + 0.00(ctnTaxOnCegAhr)"
    ),
    CgTaxPerCurrencyUnit      -> amt(BigDecimal(0.00), "0.00(atsCgTotGainsAfterLosses) + 0.00(atsCgGainsAfterLossesAmt)"),
    TotalIncomeBeforeTax      -> amt(
      BigDecimal(55364.00),
      "0.00(ctnSummaryTotalScheduleD) + 0.00(ctnSummaryTotalPartnership) + 0.00(ctnSavingsPartnership) + 0.00(ctnDividendsPartnership) + 23678.00(ctnSummaryTotalEmployment) + 9783.00(atsStatePensionAmt) + 0.00(atsOtherPensionAmt) + 0.00(itfStatePensionLsGrossAmt) + 0.00(atsIncBenefitSuppAllowAmt) + 0.00(atsJobSeekersAllowanceAmt) + 0.00(atsOthStatePenBenefitsAmt) + 0.00(ctnSummaryTotShareOptions) + 5475.00(ctnSummaryTotalUklProperty) + 0.00(ctnSummaryTotForeignIncome) + 0.00(ctnSummaryTotTrustEstates) + 0.00(ctnSummaryTotalOtherIncome) + 3678.00(ctnSummaryTotalUkInterest) + 0.00(ctnSummaryTotForeignDiv) + 12750.00(ctnSummaryTotalUkIntDivs) + 0.00(ctn4SumTotLifePolicyGains) + 0.00(ctnSummaryTotForeignSav) + 0.00(ctnForeignCegDedn) + 0.00(itfCegReceivedAfterTax) + 0.00(ctnEmploymentBenefitsAmt)"
    ),
    TotalCgTax                -> amt(
      BigDecimal(0.00),
      "max(0, Some(0.00(ctnLowerRateCgtRPCI) + 0.00(ctnHigherRateCgtRPCI) + 0.00(ctnCgDueEntrepreneursRate) + 0.00(ctnCgDueLowerRate) + 0.00(ctnCgDueHigherRate) + 0.00(capAdjustmentAmt)))"
    ),
    YourTotalTax              -> amt(
      BigDecimal(6252.58),
      "100.00(employeeClass1Nic) + 0.00(ctnClass2NicAmt) + 0.00(class4Nic) + null (savingsRateAmountScottish2023) + null (basicRateIncomeTaxAmountScottish2023) + null (higherRateIncomeTaxAmountScottish2023) + null (additionalRateIncomeTaxAmountScottish2023) + 806.25(ctnDividendTaxLowRate) + 0.00(ctnDividendTaxHighRate) + 0.00(ctnDividendTaxAddHighRate) + 0.00(nonDomChargeAmount) + 0.00(giftAidTaxReduced) + 0.00(netAnnuityPaytsTaxDue) + 0.00(ctnChildBenefitChrgAmt) + 0.00(ctnPensionSavingChrgbleAmt) - 0.00(ctnDeficiencyRelief) + 0.00(topSlicingRelief) + 0.00(ctnVctSharesReliefAmt) + 0.00(ctnEisReliefAmt) + 0.00(ctnSeedEisReliefAmt) + 0.00(ctnCommInvTrustRelAmt) + 0.00(ctnSocialInvTaxRelAmt) + 0.00(atsSurplusMcaAlimonyRel) + 0.00(alimony) + 0.00(ctnNotionalTaxCegs) + 0.00(ctnNotlTaxOthrSrceAmo) + 0.00(ctnFtcrRestricted) + 500.00(reliefForFinanceCosts) + 0.00(lfiRelief) + 10.00(ctnRelTaxAcctFor) - 0.00(ctnMarriageAllceInAmt) + 398.43(taxOnPaySSR) + 0.00(ctnTaxOnRedundancySsr) + null (ctnPensionLsumTaxDueAmt) + 3483.80(ctnIncomeTaxBasicRate) + 0.00(ctnTaxOnRedundancyBr) + null (ctnPensionLsumTaxDueAmt) + 1438.50(taxOnPaySIR) + 0.00(ctnTaxOnRedundancySir) + null (ctnPensionLsumTaxDueAmt) + 0.00(ctnIncomeTaxHigherRate) + 0.00(ctnTaxOnRedundancyHr) + null (ctnPensionLsumTaxDueAmt) + 0.00(ctnIncomeTaxAddHighRate) + 0.00(ctnTaxOnRedundancyAhr) + null (ctnPensionLsumTaxDueAmt) + 535.60(ctnSavingsTaxLowerRate) + 0.00(ctnSavingsTaxHigherRate) + 0.00(ctnSavingsTaxAddHighRate) + 0.00(ctnTaxOnCegAhr) + max(0, Some(0.00(ctnLowerRateCgtRPCI) + 0.00(ctnHigherRateCgtRPCI) + 0.00(ctnCgDueEntrepreneursRate) + 0.00(ctnCgDueLowerRate) + 0.00(ctnCgDueHigherRate) + 0.00(capAdjustmentAmt)))"
    ),
    TotalTaxFreeAmount        -> amt(
      BigDecimal(12570.00),
      "0.00(ctnEmploymentExpensesAmt) + 0.00(ctnSummaryTotalDedPpr) + 0.00(ctnSumTotForeignTaxRelief) + 0.00(ctnSumTotLossRestricted) + 0.00(grossAnnuityPayts) + 0.00(itf4GiftsInvCharitiesAmo) + 0.00(ctnBpaAllowanceAmt) + 0.00(itfBpaAmount) + 12570.00(ctnPersonalAllowance) - 0.00(ctnMarriageAllceOutAmt)"
    ),
    TotalIncomeTax            -> amt(
      BigDecimal(6152.58),
      "null (savingsRateAmountScottish2023) + null (basicRateIncomeTaxAmountScottish2023) + null (higherRateIncomeTaxAmountScottish2023) + null (additionalRateIncomeTaxAmountScottish2023) + 806.25(ctnDividendTaxLowRate) + 0.00(ctnDividendTaxHighRate) + 0.00(ctnDividendTaxAddHighRate) + 0.00(nonDomChargeAmount) + 0.00(giftAidTaxReduced) + 0.00(netAnnuityPaytsTaxDue) + 0.00(ctnChildBenefitChrgAmt) + 0.00(ctnPensionSavingChrgbleAmt) - 0.00(ctnDeficiencyRelief) + 0.00(topSlicingRelief) + 0.00(ctnVctSharesReliefAmt) + 0.00(ctnEisReliefAmt) + 0.00(ctnSeedEisReliefAmt) + 0.00(ctnCommInvTrustRelAmt) + 0.00(ctnSocialInvTaxRelAmt) + 0.00(atsSurplusMcaAlimonyRel) + 0.00(alimony) + 0.00(ctnNotionalTaxCegs) + 0.00(ctnNotlTaxOthrSrceAmo) + 0.00(ctnFtcrRestricted) + 500.00(reliefForFinanceCosts) + 0.00(lfiRelief) + 10.00(ctnRelTaxAcctFor) - 0.00(ctnMarriageAllceInAmt) + 398.43(taxOnPaySSR) + 0.00(ctnTaxOnRedundancySsr) + null (ctnPensionLsumTaxDueAmt) + 3483.80(ctnIncomeTaxBasicRate) + 0.00(ctnTaxOnRedundancyBr) + null (ctnPensionLsumTaxDueAmt) + 1438.50(taxOnPaySIR) + 0.00(ctnTaxOnRedundancySir) + null (ctnPensionLsumTaxDueAmt) + 0.00(ctnIncomeTaxHigherRate) + 0.00(ctnTaxOnRedundancyHr) + null (ctnPensionLsumTaxDueAmt) + 0.00(ctnIncomeTaxAddHighRate) + 0.00(ctnTaxOnRedundancyAhr) + null (ctnPensionLsumTaxDueAmt) + 535.60(ctnSavingsTaxLowerRate) + 0.00(ctnSavingsTaxHigherRate) + 0.00(ctnSavingsTaxAddHighRate) + 0.00(ctnTaxOnCegAhr)"
    ),
    PersonalTaxFreeAmount     -> amt(BigDecimal(12570.00), "12570.00(ctnPersonalAllowance)"),
    EmployeeNicAmount         -> amt(BigDecimal(100.00), "100.00(employeeClass1Nic) + 0.00(ctnClass2NicAmt) + 0.00(class4Nic)"),
    TaxableGains              -> amt(BigDecimal(0.00), "0.00(atsCgTotGainsAfterLosses) + 0.00(atsCgGainsAfterLossesAmt)")
  )

  private def expTotalIncomeTax: Amount =
    ((calcExp(
      (Seq(
        "savingsRateAmountScottish2023:null",
        "basicRateIncomeTaxAmountScottish2023:null",
        "higherRateIncomeTaxAmountScottish2023:null",
        "additionalRateIncomeTaxAmountScottish2023:null",
        "ctnDividendTaxLowRate",
        "ctnDividendTaxHighRate",
        "ctnDividendTaxAddHighRate"
      ) ++ fieldsOtherAdjustmentsIncreasing): _*
    ) - calcExp(
      fieldsOtherAdjustmentsReducing: _*
    )) - calcExp("ctnMarriageAllceInAmt")) + calcExp(
      "taxOnPaySSR",
      "ctnTaxOnRedundancySsr",
      "ctnPensionLsumTaxDueAmt:null",
      "ctnIncomeTaxBasicRate",
      "ctnTaxOnRedundancyBr",
      "ctnPensionLsumTaxDueAmt:null",
      "taxOnPaySIR",
      "ctnTaxOnRedundancySir",
      "ctnPensionLsumTaxDueAmt:null",
      "ctnIncomeTaxHigherRate",
      "ctnTaxOnRedundancyHr",
      "ctnPensionLsumTaxDueAmt:null",
      "ctnIncomeTaxAddHighRate",
      "ctnTaxOnRedundancyAhr",
      "ctnPensionLsumTaxDueAmt:null",
      "ctnSavingsTaxLowerRate",
      "ctnSavingsTaxHigherRate",
      "ctnSavingsTaxAddHighRate",
      "ctnTaxOnCegAhr"
    )

  private def expPayCapitalGainsTaxOn: Amount = {
    val taxableGains         = calcExp(fieldsTaxableGains: _*)
    val cgAnnualExemptAmount = calcExp("atsCgAnnualExemptAmt")
    if (taxableGains < cgAnnualExemptAmount) {
      Amount.empty("taxableGains() < get(CgAnnualExempt)")
    } else {
      taxableGains - cgAnnualExemptAmount
    }
  }
}

object ATSRawDataTransformer2023ScottishSpec {
  private final val fieldsOtherAdjustmentsReducing = Seq(
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

  private val fieldsOtherAdjustmentsIncreasing = Seq(
    "nonDomChargeAmount",
    "giftAidTaxReduced",
    "netAnnuityPaytsTaxDue",
    "ctnChildBenefitChrgAmt",
    "ctnPensionSavingChrgbleAmt"
  )

  private val fieldsSelfEmployment: Seq[String] = Seq(
    "ctnSummaryTotalScheduleD",
    "ctnSummaryTotalPartnership",
    "ctnSavingsPartnership",
    "ctnDividendsPartnership"
  )

  private val fieldsOtherIncome: Seq[String] = Seq(
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

  private val fieldsTotalIncomeBeforeTax: Seq[String] =
    fieldsSelfEmployment ++ Seq(
      "ctnSummaryTotalEmployment",
      "atsStatePensionAmt",
      "atsOtherPensionAmt",
      "itfStatePensionLsGrossAmt",
      "atsIncBenefitSuppAllowAmt",
      "atsJobSeekersAllowanceAmt",
      "atsOthStatePenBenefitsAmt"
    ) ++ fieldsOtherIncome ++ Seq("ctnEmploymentBenefitsAmt")

  private val fieldsTotalCgTax: Seq[String] = Seq(
    "ctnLowerRateCgtRPCI",
    "ctnHigherRateCgtRPCI",
    "ctnCgDueEntrepreneursRate",
    "ctnCgDueLowerRate",
    "ctnCgDueHigherRate",
    "capAdjustmentAmt"
  )

  private val fieldsTaxableGains: Seq[String] = Seq("atsCgTotGainsAfterLosses", "atsCgGainsAfterLossesAmt")
}
