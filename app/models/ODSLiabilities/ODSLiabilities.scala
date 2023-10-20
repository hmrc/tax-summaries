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

package models.ODSLiabilities

import models.ApiValue
import play.api.libs.json.Reads

// scalastyle:off number.of.methods

sealed class ODSLiabilities(apiValue: String) extends ApiValue(apiValue)

object ODSLiabilities {
  case object AnnuityPay extends ODSLiabilities("grossAnnuityPayts")

  case object BPA extends ODSLiabilities("itfBpaAmount")

  case object BpaAllowance extends ODSLiabilities("ctnBpaAllowanceAmt")

  case object CGAtHigherRateRPCI extends ODSLiabilities("ctnCGAtHigherRateRPCI")

  case object CGAtLowerRateRPCI extends ODSLiabilities("ctnCGAtLowerRateRPCI")

  case object CGOtherGainsAfterLoss extends ODSLiabilities("atsCGOtherGainsAfterLoss")

  case object CapAdjustment extends ODSLiabilities("capAdjustmentAmt")

  case object CgAnnualExempt extends ODSLiabilities("atsCgAnnualExemptAmt")

  case object CgAtEntrepreneursRate extends ODSLiabilities("ctnCgAtEntrepreneursRate")

  case object CgAtHigherRate extends ODSLiabilities("ctnCgAtHigherRate")

  case object CgAtLowerRate extends ODSLiabilities("ctnCgAtLowerRate")

  case object CgDueEntrepreneursRate extends ODSLiabilities("ctnCgDueEntrepreneursRate")

  case object CgDueHigherRate extends ODSLiabilities("ctnCgDueHigherRate")

  case object CgDueLowerRate extends ODSLiabilities("ctnCgDueLowerRate")

  case object CgGainsAfterLosses extends ODSLiabilities("atsCgGainsAfterLossesAmt")

  case object CgTotGainsAfterLosses extends ODSLiabilities("atsCgTotGainsAfterLosses")

  case object ChildBenefitCharge extends ODSLiabilities("ctnChildBenefitChrgAmt")

  case object Class4Nic extends ODSLiabilities("class4Nic")

  case object CommInvTrustRel extends ODSLiabilities("ctnCommInvTrustRelAmt")

  case object DeficiencyRelief extends ODSLiabilities("ctnDeficiencyRelief")

  case object Alimony extends ODSLiabilities("alimony")

  case object DividendChargeableAddHRate extends ODSLiabilities("ctnDividendChgbleAddHRate")

  case object DividendChargeableHighRate extends ODSLiabilities("ctnDividendChgbleHighRate")

  case object DividendChargeableLowRate extends ODSLiabilities("ctnDividendChgbleLowRate")

  case object DividendTaxAddHighRate extends ODSLiabilities("ctnDividendTaxAddHighRate")

  case object DividendTaxHighRate extends ODSLiabilities("ctnDividendTaxHighRate")

  case object DividendTaxLowRate extends ODSLiabilities("ctnDividendTaxLowRate")

  case object EisRelief extends ODSLiabilities("ctnEisReliefAmt")

  case object EmploymentBenefits extends ODSLiabilities("ctnEmploymentBenefitsAmt")

  case object EmploymentExpenses extends ODSLiabilities("ctnEmploymentExpensesAmt")

  case object ExcludedIncome extends ODSLiabilities("grossExcludedIncome")

  case object GiftsInvCharities extends ODSLiabilities("itf4GiftsInvCharitiesAmo")

  case object HigherRateCgtRPCI extends ODSLiabilities("ctnHigherRateCgtRPCI")

  case object IncBenefitSuppAllow extends ODSLiabilities("atsIncBenefitSuppAllowAmt")

  case object IncomeChargeableAddHRate extends ODSLiabilities("ctnIncomeChgbleAddHRate")

  case object IncomeChargeableBasicRate extends ODSLiabilities("ctnIncomeChgbleBasicRate")

  case object IncomeChargeableHigherRate extends ODSLiabilities("ctnIncomeChgbleHigherRate")

  case object IncomeTaxAddHighRate extends ODSLiabilities("ctnIncomeTaxAddHighRate")

  case object IncomeTaxBasicRate extends ODSLiabilities("ctnIncomeTaxBasicRate")

  case object IncomeTaxDue extends ODSLiabilities("incomeTaxDue")

  case object IncomeTaxHigherRate extends ODSLiabilities("ctnIncomeTaxHigherRate")

  case object JobSeekersAllowance extends ODSLiabilities("atsJobSeekersAllowanceAmt")

  case object LFIRelief extends ODSLiabilities("lfiRelief")

  case object LowerRateCgtRPCI extends ODSLiabilities("ctnLowerRateCgtRPCI")

  case object MarriageAllceIn extends ODSLiabilities("ctnMarriageAllceInAmt")

  case object MarriageAllceOut extends ODSLiabilities("ctnMarriageAllceOutAmt")

  case object NRGTGainsAfterLoss extends ODSLiabilities("atsNRGTGainsAfterLoss")

  case object NetAnnuityPaytsTaxDue extends ODSLiabilities("netAnnuityPaytsTaxDue")

  case object NonDomCharge extends ODSLiabilities("nonDomChargeAmount")

  case object NonPayableTaxCredits extends ODSLiabilities("ctnNonPayableTaxCredits")

  case object NotionalTaxCegs extends ODSLiabilities("ctnNotionalTaxCegs")

  case object NotlTaxOtherSource extends ODSLiabilities("ctnNotlTaxOthrSrceAmo")

  case object OthStatePenBenefits extends ODSLiabilities("atsOthStatePenBenefitsAmt")

  case object OtherPension extends ODSLiabilities("atsOtherPensionAmt")

  case object PensionLsumTaxDue extends ODSLiabilities("ctnPensionLsumTaxDueAmt")

  case object PensionSavingChargeable extends ODSLiabilities("ctnPensionSavingChrgbleAmt")

  case object PersonalAllowance extends ODSLiabilities("ctnPersonalAllowance")

  case object QualDistnRelief extends ODSLiabilities("ctnQualDistnReliefAmt")

  case object ReliefForFinanceCosts extends ODSLiabilities("reliefForFinanceCosts")

  case object SavingsChargeableAddHRate extends ODSLiabilities("ctnSavingsChgbleAddHRate")

  case object SavingsChargeableHigherRate extends ODSLiabilities("ctnSavingsChgbleHigherRate")

  case object SavingsChargeableLowerRate extends ODSLiabilities("ctnSavingsChgbleLowerRate")

  case object SavingsChargeableStartRate extends ODSLiabilities("ctnSavingsChgbleStartRate")

  case object SavingsTaxAddHighRate extends ODSLiabilities("ctnSavingsTaxAddHighRate")

  case object SavingsTaxHigherRate extends ODSLiabilities("ctnSavingsTaxHigherRate")

  case object SavingsTaxLowerRate extends ODSLiabilities("ctnSavingsTaxLowerRate")

  case object SavingsTaxStartingRate extends ODSLiabilities("ctnSavingsTaxStartingRate")

  case object SeedEisRelief extends ODSLiabilities("ctnSeedEisReliefAmt")

  case object SocialInvTaxRel extends ODSLiabilities("ctnSocialInvTaxRelAmt")

  case object StatePension extends ODSLiabilities("atsStatePensionAmt")

  case object StatePensionGross extends ODSLiabilities("itfStatePensionLsGrossAmt")

  case object SumTotForeignTaxRelief extends ODSLiabilities("ctnSumTotForeignTaxRelief")

  case object SumTotLifePolicyGains extends ODSLiabilities("ctn4SumTotLifePolicyGains")

  case object SumTotLoanRestricted extends ODSLiabilities("ctnSumTotLoanRestricted")

  case object SumTotLossRestricted extends ODSLiabilities("ctnSumTotLossRestricted")

  case object SummaryTotForeignDiv extends ODSLiabilities("ctnSummaryTotForeignDiv")

  case object SummaryTotForeignIncome extends ODSLiabilities("ctnSummaryTotForeignIncome")

  case object SummaryTotShareOptions extends ODSLiabilities("ctnSummaryTotShareOptions")

  case object SummaryTotTrustEstates extends ODSLiabilities("ctnSummaryTotTrustEstates")

  case object SummaryTotalDedPpr extends ODSLiabilities("ctnSummaryTotalDedPpr")

  case object SummaryTotalEmployment extends ODSLiabilities("ctnSummaryTotalEmployment")

  case object SummaryTotalOtherIncome extends ODSLiabilities("ctnSummaryTotalOtherIncome")

  case object SummaryTotalPartnership extends ODSLiabilities("ctnSummaryTotalPartnership")

  case object SummaryTotalSchedule extends ODSLiabilities("ctnSummaryTotalScheduleD")

  case object SummaryTotalUkIntDivs extends ODSLiabilities("ctnSummaryTotalUkIntDivs")

  case object SummaryTotalUkInterest extends ODSLiabilities("ctnSummaryTotalUkInterest")

  case object SummaryTotalUklProperty extends ODSLiabilities("ctnSummaryTotalUklProperty")

  case object SurplusMcaAlimonyRel extends ODSLiabilities("atsSurplusMcaAlimonyRel")

  case object TaxablePayScottishIntermediateRate extends ODSLiabilities("taxablePaySIR")

  case object TaxablePayScottishStarterRate extends ODSLiabilities("taxablePaySSR")

  case object TaxCharged extends ODSLiabilities("atsTaxCharged")

  case object TaxCreditsForDivs extends ODSLiabilities("ctnTaxCredForDivs")

  case object TaxDueAfterAllceRlf extends ODSLiabilities("ctn4TaxDueAfterAllceRlf")

  case object TaxExcluded extends ODSLiabilities("taxExcluded")

  case object TaxOnPayScottishIntermediateRate extends ODSLiabilities("taxOnPaySIR")

  case object TaxOnPayScottishStarterRate extends ODSLiabilities("taxOnPaySSR")

  case object TopSlicingRelief extends ODSLiabilities("topSlicingRelief")

  case object TotalTaxCreditRelief extends ODSLiabilities("figTotalTaxCreditRelief")

  case object TradeUnionDeathBenefits extends ODSLiabilities("itfTradeUnionDeathBenefits")

  case object VctSharesRelief extends ODSLiabilities("ctnVctSharesReliefAmt")

  case object EmployeeClass1NI extends ODSLiabilities("employeeClass1Nic")

  case object EmployeeClass2NI extends ODSLiabilities("employeeClass2Nic")

  case object EmployerNI extends ODSLiabilities("employerNic")

  // Added in 2021
  case object DividendsPartnership extends ODSLiabilities("ctnDividendsPartnership")

  case object SavingsPartnership extends ODSLiabilities("ctnSavingsPartnership")

  case object TaxOnNonExcludedIncome extends ODSLiabilities("taxOnNonExcludedInc")

  case object SummaryTotForeignSav extends ODSLiabilities("ctnSummaryTotForeignSav")

  case object GiftAidTaxReduced extends ODSLiabilities("giftAidTaxReduced") // bug should be used before

  // Added in 2022
  case object ForeignCegDedn extends ODSLiabilities("ctnForeignCegDedn")

  case object ItfCegReceivedAfterTax extends ODSLiabilities("itfCegReceivedAfterTax")

  case object FtcrRestricted extends ODSLiabilities("ctnFtcrRestricted")

  case object Class2NicAmt extends ODSLiabilities("ctnClass2NicAmt")

  case object TaxableRedundancyHr extends ODSLiabilities("ctnTaxableRedundancyHr")

  case object TaxableCegHr extends ODSLiabilities("ctnTaxableCegHr")

  case object PensionLumpSumTaxRate extends ODSLiabilities("ctnPensionLumpSumTaxRate")

  case object TaxOnRedundancyHr extends ODSLiabilities("ctnTaxOnRedundancyHr")

  case object TaxOnRedundancySsr extends ODSLiabilities("ctnTaxOnRedundancySsr")

  case object TaxOnRedundancySir extends ODSLiabilities("ctnTaxOnRedundancySir")

  case object TaxOnCegHr extends ODSLiabilities("ctnTaxOnCegHr")

  case object TaxOnRedundancyBr extends ODSLiabilities("ctnTaxOnRedundancyBr")

  case object TaxOnCegBr extends ODSLiabilities("ctnTaxOnCegBr")

  case object TaxOnRedundancyAhr extends ODSLiabilities("ctnTaxOnRedundancyAhr")

  case object TaxOnCegAhr extends ODSLiabilities("ctnTaxOnCegAhr")

  case object TaxableRedundancyBr extends ODSLiabilities("ctnTaxableRedundancyBr")

  case object TaxableCegBr extends ODSLiabilities("ctnTaxableCegBr")

  case object TaxableRedundancyAhr extends ODSLiabilities("ctnTaxableRedundancyAhr")

  case object TaxableCegAhr extends ODSLiabilities("ctnTaxableCegAhr")

  case object TaxableCegSr extends ODSLiabilities("ctnTaxableCegSr")

  case object TaxOnCegSr extends ODSLiabilities("ctnTaxOnCegSr")

  case object TaxableRedundancySsr extends ODSLiabilities("ctnTaxableRedundancySsr")

  case object TaxableRedundancySir extends ODSLiabilities("ctnTaxableRedundancySir")

  // format: off
  private val allLiabilities: List[ODSLiabilities with ApiValue] =
    List(AnnuityPay, BPA, BpaAllowance, CGAtHigherRateRPCI, CGAtLowerRateRPCI, CGOtherGainsAfterLoss, CapAdjustment,
      CgAnnualExempt, CgAtEntrepreneursRate, CgAtHigherRate, CgAtLowerRate, CgDueEntrepreneursRate, CgDueHigherRate,
      CgDueLowerRate, CgGainsAfterLosses, CgTotGainsAfterLosses, ChildBenefitCharge, Class4Nic, CommInvTrustRel,
      DeficiencyRelief, Alimony, DividendChargeableAddHRate, DividendChargeableHighRate, DividendChargeableLowRate,
      DividendTaxAddHighRate, DividendTaxHighRate, DividendTaxLowRate, EisRelief, EmploymentBenefits, EmploymentExpenses,
      ExcludedIncome, GiftsInvCharities, HigherRateCgtRPCI, IncBenefitSuppAllow, IncomeChargeableAddHRate,
      IncomeChargeableBasicRate, IncomeChargeableHigherRate, IncomeTaxAddHighRate, IncomeTaxBasicRate, IncomeTaxDue,
      IncomeTaxHigherRate, JobSeekersAllowance, LowerRateCgtRPCI, MarriageAllceIn, MarriageAllceOut, NRGTGainsAfterLoss,
      NetAnnuityPaytsTaxDue, NonDomCharge, NonPayableTaxCredits, NotionalTaxCegs, NotlTaxOtherSource, OthStatePenBenefits,
      OtherPension, PensionLsumTaxDue, PensionSavingChargeable, PersonalAllowance, QualDistnRelief, ReliefForFinanceCosts,
      SavingsChargeableAddHRate, SavingsChargeableHigherRate, SavingsChargeableLowerRate, SavingsChargeableStartRate,
      SavingsTaxAddHighRate, SavingsTaxHigherRate, SavingsTaxLowerRate, SavingsTaxStartingRate, SeedEisRelief,
      SocialInvTaxRel, StatePension, StatePensionGross, SumTotForeignTaxRelief, SumTotLifePolicyGains, SumTotLoanRestricted,
      SumTotLossRestricted, SummaryTotForeignDiv, SummaryTotForeignIncome, SummaryTotShareOptions, SummaryTotTrustEstates,
      SummaryTotalDedPpr, SummaryTotalEmployment, SummaryTotalOtherIncome, SummaryTotalPartnership, SummaryTotalSchedule,
      SummaryTotalUkIntDivs, SummaryTotalUkInterest, SummaryTotalUklProperty, SurplusMcaAlimonyRel, TaxablePayScottishIntermediateRate,
      TaxablePayScottishStarterRate, TaxCharged, TaxCreditsForDivs, TaxDueAfterAllceRlf, TaxExcluded, TaxOnPayScottishIntermediateRate,
      TaxOnPayScottishStarterRate, TopSlicingRelief, TotalTaxCreditRelief, TradeUnionDeathBenefits, VctSharesRelief,
      EmployeeClass1NI, EmployeeClass2NI, EmployerNI, LFIRelief, SavingsPartnership, DividendsPartnership,
      TaxOnNonExcludedIncome, SummaryTotForeignSav, GiftAidTaxReduced)

  private val mapLiabilities: Map[Int, List[ODSLiabilities with ApiValue]] =
    Map(
      2022 -> (allLiabilities ++ List(
        ForeignCegDedn, ItfCegReceivedAfterTax, FtcrRestricted, Class2NicAmt, TaxableRedundancyHr, TaxableCegHr,
        PensionLumpSumTaxRate, TaxOnRedundancyHr, TaxOnCegHr, TaxOnRedundancyBr, TaxOnCegBr, TaxOnRedundancyAhr,
        TaxOnRedundancySir, TaxOnRedundancySsr, TaxOnCegAhr, TaxableRedundancyBr, TaxableCegBr, TaxableRedundancyAhr,
        TaxableCegAhr, TaxableCegSr, TaxOnCegSr, TaxableRedundancySsr, TaxableRedundancySir
      ))
    )
  // format: on

  def readsLiabilities(taxYear: Int): Reads[ODSLiabilities] =
    ApiValue.readFromList[ODSLiabilities](
      mapLiabilities.get(taxYear) match {
        case Some(liabilities) => liabilities
        case _                 =>
          val latestTaxYearForLiabilities = mapLiabilities.keys.toSeq.max
          if (taxYear > latestTaxYearForLiabilities) {
            mapLiabilities(latestTaxYearForLiabilities)
          } else {
            allLiabilities
          }
      }
    )
}
