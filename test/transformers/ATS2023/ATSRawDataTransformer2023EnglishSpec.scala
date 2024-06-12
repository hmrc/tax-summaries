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
    BasicRateIncomeTaxAmount        -> expBasicRateIncomeTaxAmount,
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
    TotalTaxFreeAmount                 -> expTotalTaxFreeAmount
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

  private def expTotalAmountEmployeeNic = calcExp("employeeClass1Nic", "employeeClass2Nic", "class4Nic")

  private def expBasicRateIncomeTaxAmount = calcExp(
    "ctnIncomeTaxBasicRate",
    "ctnSavingsTaxLowerRate",
    "ctnTaxOnRedundancyBr",
    "ctnTaxOnCegBr",
    "ctnPensionLsumTaxDueAmt:null"
  )

  private def expectedResultSummaryData: Map[LiabilityKey, Amount] = Map(
    TotalIncomeTaxAndNics -> calcExp(
      "employeeClass1Nic",
      "ctnClass2NicAmt",
      "class4Nic",
      "taxExcluded",
      "taxOnNonExcludedInc"
    ),
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
