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

class ATSRawDataTransformer2023EnglishSpec extends AtsRawDataTransformerTestHelper with AtsJsonDataUpdate {
  import ATSRawDataTransformer2023EnglishSpec._

  override protected val taxYear: Int            = 2023
  override protected val incomeTaxStatus: String = "0001"

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
    StartingRateForSavingsAmount    -> calcExp("ctnSavingsTaxStartingRate", "ctnTaxOnCegSr"),
    OtherAdjustmentsReducing        -> calcExp(fieldsOtherAdjustmentsReducing: _*),
    UpperRate                       -> calcExp("ctnDividendChgbleHighRate"),
    SavingsLowerIncome              -> calcExp("savingsBasicRateIncome:null"),
    SavingsLowerRateTax             -> calcExp("savingsBasicRateTax:null"),
    ScottishIncomeTax               -> calcExp("scottishIncomeTaxUK2023:null"),
    ScottishIntermediateRateTax     -> calcExp("scottishIntermediateRateTax:null"),
    MarriageAllowanceReceivedAmount -> calcExp("ctnMarriageAllceInAmt"),
    OrdinaryRateAmount              -> calcExp("ctnDividendTaxLowRate"),
    ScottishHigherIncome            -> calcExp("scottishHigherRateIncome:null"),
    ScottishStarterRateTax          -> calcExp("scottishStarterRateTax:null"),
    AdditionalRate                  -> calcExp("ctnDividendChgbleAddHRate"),
    StartingRateForSavings          -> calcExp("ctnSavingsChgbleStartRate", "ctnTaxableCegSr"),
    AdditionalRateIncomeTax         -> calcExp(
      "ctnIncomeChgbleAddHRate",
      "ctnSavingsChgbleAddHRate",
      "ctnTaxableRedundancyAhr",
      "ctnTaxableCegAhr",
      "itfStatePensionLsGrossAmt:null"
    ),
    SavingsAdditionalIncome         -> calcExp("savingsAdditionalRateIncome:null"),
    SavingsHigherIncome             -> calcExp("savingsHigherRateIncome:null"),
    ScottishAdditionalRateTax       -> calcExp(
      "scottishAdditionalRateTax:null"
    ),
    OtherAdjustmentsIncreasing      -> calcExp(fieldsOtherAdjustmentsIncreasing: _*),
    HigherRateIncomeTax             -> calcExp(
      "ctnIncomeChgbleHigherRate",
      "ctnSavingsChgbleHigherRate",
      "ctnTaxableRedundancyHr",
      "ctnTaxableCegHr",
      "itfStatePensionLsGrossAmt:null"
    ),
    ScottishBasicRateTax            -> calcExp("scottishBasicRateTax:null"),
    BasicRateIncomeTaxAmount        -> calcExp(
      "ctnIncomeTaxBasicRate",
      "ctnSavingsTaxLowerRate",
      "ctnTaxOnRedundancyBr",
      "ctnTaxOnCegBr",
      "ctnPensionLsumTaxDueAmt:null"
    ),
    AdditionalRateAmount            -> calcExp("ctnDividendTaxAddHighRate"),
    WelshIncomeTax                  -> calcExp("welshIncomeTax:null"),
    ScottishAdditionalIncome        -> calcExp("scottishAdditionalRateIncome:null"),
    ScottishIntermediateIncome      -> calcExp("scottishIntermediateRateIncome:null"),
    UpperRateAmount                 -> calcExp("ctnDividendTaxHighRate"),
    AdditionalRateIncomeTaxAmount   -> calcExp(
      "ctnIncomeTaxAddHighRate",
      "ctnSavingsTaxAddHighRate",
      "ctnTaxOnRedundancyAhr",
      "ctnTaxOnCegAhr",
      "ctnPensionLsumTaxDueAmt:null"
    ),
    ScottishBasicIncome             -> calcExp("scottishBasicRateIncome:null"),
    ScottishTotalTax                -> calcExp(
      "scottishStarterRateTax:null",
      "scottishBasicRateTax:null",
      "scottishIntermediateRateTax:null",
      "scottishHigherRateTax:null",
      "scottishAdditionalRateTax:null"
    ),
    BasicRateIncomeTax              -> calcExp(
      "ctnIncomeChgbleBasicRate",
      "ctnSavingsChgbleLowerRate",
      "ctnTaxableRedundancyBr",
      "ctnTaxableCegBr",
      "itfStatePensionLsGrossAmt:null"
    ),
    SavingsAdditionalRateTax        -> calcExp("savingsAdditionalRateTax:null"),
    HigherRateIncomeTaxAmount       -> calcExp(
      "ctnIncomeTaxHigherRate",
      "ctnSavingsTaxHigherRate",
      "ctnTaxOnRedundancyHr",
      "ctnTaxOnCegHr",
      "ctnPensionLsumTaxDueAmt:null"
    ),
    TotalIncomeTax                  -> calcExp("taxExcluded", "taxOnNonExcludedInc"),
    SavingsHigherRateTax            -> calcExp("savingsHigherRateTax:null"),
    OrdinaryRate                    -> calcExp("ctnDividendChgbleLowRate"),
    ScottishHigherRateTax           -> calcExp("scottishHigherRateTax:null"),
    ScottishStarterIncome           -> calcExp("scottishStarterRateIncome:null")
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
    CgTaxPerCurrencyUnit         -> taxPerTaxableCurrencyUnit(
      calcExp(fieldsTotalCgTax: _*).max(0),
      calcExp(fieldsTaxableGains: _*)
    ),
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

object ATSRawDataTransformer2023EnglishSpec {
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
