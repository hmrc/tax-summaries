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

class ATSRawDataTransformer2023ScottishSpec extends AtsRawDataTransformerTestHelper with AtsJsonDataUpdate {
  import ATSRawDataTransformer2023ScottishSpec._

  override protected val taxYear: Int            = 2023
  override protected val incomeTaxStatus: String = "0002"
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
      description = "main",
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

//  Amount(6242.58, "GBP", Some("1080.00(employeeClass1Nic) + 310.00(ctnClass2NicAmt) + 300.00(class4Nic) + ((null (savingsRateAmountScottish2023) + null (basicRateIncomeTaxAmountScottish2023) +
//  null (higherRateIncomeTaxAmountScottish2023) + null (additionalRateIncomeTaxAmountScottish2023) + 806.25(ctnDividendTaxLowRate) +
//    430.00(ctnDividendTaxHighRate) + 450.00(ctnDividendTaxAddHighRate) + 620.00(nonDomChargeAmount) + 1040.00(giftAidTaxReduced) +
//    650.00(netAnnuityPaytsTaxDue) + 660.00(ctnChildBenefitChrgAmt) + 670.00(ctnPensionSavingChrgbleAmt)) -
//
//    (680.00(ctnDeficiencyRelief) + 690.00(topSlicingRelief) + 700.00(ctnVctSharesReliefAmt) + 710.00(ctnEisReliefAmt) +
//      720.00(ctnSeedEisReliefAmt) + 730.00(ctnCommInvTrustRelAmt) + 1010.00(ctnSocialInvTaxRelAmt) + 740.00(atsSurplusMcaAlimonyRel) + 750.00(alimony) +
//      760.00(ctnNotionalTaxCegs) + 770.00(ctnNotlTaxOthrSrceAmo) + 780.00(ctnFtcrRestricted) + 500.00(reliefForFinanceCosts) + 790.00(lfiRelief) + 10.00(ctnRelTaxAcctFor))) -
//
//    990.00(ctnMarriageAllceInAmt) + 398.43(taxOnPaySSR) + 470.00(ctnTaxOnRedundancySsr) + null (ctnPensionLsumTaxDueAmt) + 3483.80(ctnIncomeTaxBasicRate) + 490.00(ctnTaxOnRedundancyBr) +
//    null (ctnPensionLsumTaxDueAmt) + 1438.50(taxOnPaySIR) + 510.00(ctnTaxOnRedundancySir) + null (ctnPensionLsumTaxDueAmt) + 360.00(ctnIncomeTaxHigherRate) + 530.00(ctnTaxOnRedundancyHr) +
//    null (ctnPensionLsumTaxDueAmt) + 400.00(ctnIncomeTaxAddHighRate) + 550.00(ctnTaxOnRedundancyAhr) + null (ctnPensionLsumTaxDueAmt) + 535.60(ctnSavingsTaxLowerRate) + 370.00(ctnSavingsTaxHigherRate) +
//    410.00(ctnSavingsTaxAddHighRate) + 610.00(ctnTaxOnCegAhr)"))

  private def expTotalIncomeTaxAndNics =
    (
      calcExp(
        "employeeClass1Nic",
        "ctnClass2NicAmt",
        "class4Nic",
        "savingsRateAmountScottish2023:null",
        "basicRateIncomeTaxAmountScottish2023:null",
        "higherRateIncomeTaxAmountScottish2023:null",
        "additionalRateIncomeTaxAmountScottish2023:null",
        "ctnDividendTaxLowRate",
        "ctnDividendTaxHighRate",
        "ctnDividendTaxAddHighRate",
        "nonDomChargeAmount",
        "giftAidTaxReduced",
        "netAnnuityPaytsTaxDue",
        "ctnChildBenefitChrgAmt",
        "ctnPensionSavingChrgbleAmt"
      ) - calcExp(
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
    ) - calcExp(
      "ctnMarriageAllceInAmt",
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

  private def expectedResultSummaryData: Map[LiabilityKey, Amount] = Map(
    // TODO: The field below is failing - I think due to 2 excluded fields
    TotalIncomeTaxAndNics -> expTotalIncomeTaxAndNics,
    //    NicsAndTaxPerCurrencyUnit -> calcExp(
    //      "employeeClass1Nic",
    //      "ctnClass2NicAmt",
    //      "class4Nic",
    //      "taxExcluded",
    //      "taxOnNonExcludedInc"
    //    ),
    CgTaxPerCurrencyUnit  -> taxPerTaxableCurrencyUnit(
      calcExp(fieldsTotalCgTax: _*).max(0),
      calcExp(fieldsTaxableGains: _*)
    ),
    TotalIncomeBeforeTax  -> calcExp(fieldsTotalIncomeBeforeTax: _*),
    TotalCgTax            -> calcExp(fieldsTotalCgTax: _*).max(0),
    YourTotalTax          -> (calcExp(
      "employeeClass1Nic",
      "ctnClass2NicAmt",
      "class4Nic",
      "taxExcluded",
      "taxOnNonExcludedInc"
    ) + calcExp(
      "ctnLowerRateCgtRPCI",
      "ctnHigherRateCgtRPCI",
      "ctnCgDueEntrepreneursRate",
      "ctnCgDueLowerRate",
      "ctnCgDueHigherRate",
      "capAdjustmentAmt"
    ).max(0)),
    TotalTaxFreeAmount    -> expTotalTaxFreeAmount,
    TotalIncomeTax        -> calcExp("taxExcluded", "taxOnNonExcludedInc"),
    PersonalTaxFreeAmount -> amt(BigDecimal(12570.00), "12570.00(ctnPersonalAllowance)"),
    EmployeeNicAmount     -> calcExp("employeeClass1Nic", "ctnClass2NicAmt", "class4Nic"),
    TaxableGains          -> calcExp("atsCgTotGainsAfterLosses", "atsCgGainsAfterLossesAmt")
  )

  private def expTotalTaxFreeAmount: Amount = calcExp(
    "ctnEmploymentExpensesAmt",
    "ctnSummaryTotalDedPpr",
    "ctnSumTotForeignTaxRelief",
    "ctnSumTotLossRestricted",
    "grossAnnuityPayts",
    "itf4GiftsInvCharitiesAmo",
    "ctnBpaAllowanceAmt",
    "itfBpaAmount",
    "ctnPersonalAllowance"
  ) - calcExp("ctnMarriageAllceOutAmt")

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
