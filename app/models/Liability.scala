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

package models

import play.api.libs.json.Reads

sealed trait Liability extends ApiValue

object Liability {

  case object AnnuityPay extends ApiValue("grossAnnuityPayts") with Liability
  case object BPA extends ApiValue("itfBpaAmount") with Liability
  case object BpaAllowance extends ApiValue("ctnBpaAllowanceAmt") with Liability
  case object CGAtHigherRateRPCI extends ApiValue("ctnCGAtHigherRateRPCI") with Liability
  case object CGAtLowerRateRPCI extends ApiValue("ctnCGAtLowerRateRPCI") with Liability
  case object CGOtherGainsAfterLoss extends ApiValue("atsCGOtherGainsAfterLoss") with Liability
  case object CapAdjustment extends ApiValue("capAdjustmentAmt") with Liability
  case object CgAnnualExempt extends ApiValue("atsCgAnnualExemptAmt") with Liability
  case object CgAtEntrepreneursRate extends ApiValue("ctnCgAtEntrepreneursRate") with Liability
  case object CgAtHigherRate extends ApiValue("ctnCgAtHigherRate") with Liability
  case object CgAtLowerRate extends ApiValue("ctnCgAtLowerRate") with Liability
  case object CgDueEntrepreneursRate extends ApiValue("ctnCgDueEntrepreneursRate") with Liability
  case object CgDueHigherRate extends ApiValue("ctnCgDueHigherRate") with Liability
  case object CgDueLowerRate extends ApiValue("ctnCgDueLowerRate") with Liability
  case object CgGainsAfterLosses extends ApiValue("atsCgGainsAfterLossesAmt") with Liability
  case object CgTotGainsAfterLosses extends ApiValue("atsCgTotGainsAfterLosses") with Liability
  case object ChildBenefitCharge extends ApiValue("ctnChildBenefitChrgAmt") with Liability
  case object Class4Nic extends ApiValue("class4Nic") with Liability
  case object CommInvTrustRel extends ApiValue("ctnCommInvTrustRelAmt") with Liability
  case object DeficiencyRelief extends ApiValue("ctnDeficiencyRelief") with Liability
  case object DisguisedRemunerationAmount extends ApiValue("disguisedRemunerationAmount") with Liability
  case object DividendChargeableAddHRate extends ApiValue("ctnDividendChgbleAddHRate") with Liability
  case object DividendChargeableHighRate extends ApiValue("ctnDividendChgbleHighRate") with Liability
  case object DividendChargeableLowRate extends ApiValue("ctnDividendChgbleLowRate") with Liability
  case object DividendTaxAddHighRate extends ApiValue("ctnDividendTaxAddHighRate") with Liability
  case object DividendTaxHighRate extends ApiValue("ctnDividendTaxHighRate") with Liability
  case object DividendTaxLowRate extends ApiValue("ctnDividendTaxLowRate") with Liability
  case object EisRelief extends ApiValue("ctnEisReliefAmt") with Liability
  case object EmploymentBenefits extends ApiValue("ctnEmploymentBenefitsAmt") with Liability
  case object EmploymentExpenses extends ApiValue("ctnEmploymentExpensesAmt") with Liability
  case object ExcludedIncome extends ApiValue("grossExcludedIncome") with Liability
  case object GiftsInvCharities extends ApiValue("itf4GiftsInvCharitiesAmo") with Liability
  case object HigherRateCgtRPCI extends ApiValue("ctnHigherRateCgtRPCI") with Liability
  case object IncBenefitSuppAllow extends ApiValue("atsIncBenefitSuppAllowAmt") with Liability
  case object IncomeChargeableAddHRate extends ApiValue("ctnIncomeChgbleAddHRate") with Liability
  case object IncomeChargeableBasicRate extends ApiValue("ctnIncomeChgbleBasicRate") with Liability
  case object IncomeChargeableHigherRate extends ApiValue("ctnIncomeChgbleHigherRate") with Liability
  case object IncomeTaxAddHighRate extends ApiValue("ctnIncomeTaxAddHighRate") with Liability
  case object IncomeTaxBasicRate extends ApiValue("ctnIncomeTaxBasicRate") with Liability
  case object IncomeTaxDue extends ApiValue("incomeTaxDue") with Liability
  case object IncomeTaxHigherRate extends ApiValue("ctnIncomeTaxHigherRate") with Liability
  case object JobSeekersAllowance extends ApiValue("atsJobSeekersAllowanceAmt") with Liability
  case object LFIRelief extends ApiValue("lfiRelief") with Liability
  case object LowerRateCgtRPCI extends ApiValue("ctnLowerRateCgtRPCI") with Liability
  case object MarriageAllceIn extends ApiValue("ctnMarriageAllceInAmt") with Liability
  case object MarriageAllceOut extends ApiValue("ctnMarriageAllceOutAmt") with Liability
  case object NRGTGainsAfterLoss extends ApiValue("atsNRGTGainsAfterLoss") with Liability
  case object NetAnnuityPaytsTaxDue extends ApiValue("netAnnuityPaytsTaxDue") with Liability
  case object NonDomCharge extends ApiValue("nonDomChargeAmount") with Liability
  case object NonPayableTaxCredits extends ApiValue("ctnNonPayableTaxCredits") with Liability
  case object NotionalTaxCegs extends ApiValue("ctnNotionalTaxCegs") with Liability
  case object NotlTaxOtherSource extends ApiValue("ctnNotlTaxOthrSrceAmo") with Liability
  case object OthStatePenBenefits extends ApiValue("atsOthStatePenBenefitsAmt") with Liability
  case object OtherPension extends ApiValue("atsOtherPensionAmt") with Liability
  case object PensionLsumTaxDue extends ApiValue("ctnPensionLsumTaxDueAmt") with Liability
  case object PensionSavingChargeable extends ApiValue("ctnPensionSavingChrgbleAmt") with Liability
  case object PersonalAllowance extends ApiValue("ctnPersonalAllowance") with Liability
  case object QualDistnRelief extends ApiValue("ctnQualDistnReliefAmt") with Liability
  case object ReliefForFinanceCosts extends ApiValue("reliefForFinanceCosts") with Liability
  case object SavingsChargeableAddHRate extends ApiValue("ctnSavingsChgbleAddHRate") with Liability
  case object SavingsChargeableHigherRate extends ApiValue("ctnSavingsChgbleHigherRate") with Liability
  case object SavingsChargeableLowerRate extends ApiValue("ctnSavingsChgbleLowerRate") with Liability
  case object SavingsChargeableStartRate extends ApiValue("ctnSavingsChgbleStartRate") with Liability
  case object SavingsTaxAddHighRate extends ApiValue("ctnSavingsTaxAddHighRate") with Liability
  case object SavingsTaxHigherRate extends ApiValue("ctnSavingsTaxHigherRate") with Liability
  case object SavingsTaxLowerRate extends ApiValue("ctnSavingsTaxLowerRate") with Liability
  case object SavingsTaxStartingRate extends ApiValue("ctnSavingsTaxStartingRate") with Liability
  case object SeedEisRelief extends ApiValue("ctnSeedEisReliefAmt") with Liability
  case object SocialInvTaxRel extends ApiValue("ctnSocialInvTaxRelAmt") with Liability
  case object StatePension extends ApiValue("atsStatePensionAmt") with Liability
  case object StatePensionGross extends ApiValue("itfStatePensionLsGrossAmt") with Liability
  case object SumTotForeignTaxRelief extends ApiValue("ctnSumTotForeignTaxRelief") with Liability
  case object SumTotLifePolicyGains extends ApiValue("ctn4SumTotLifePolicyGains") with Liability
  case object SumTotLoanRestricted extends ApiValue("ctnSumTotLoanRestricted") with Liability
  case object SumTotLossRestricted extends ApiValue("ctnSumTotLossRestricted") with Liability
  case object SummaryTotForeignDiv extends ApiValue("ctnSummaryTotForeignDiv") with Liability
  case object SummaryTotForeignIncome extends ApiValue("ctnSummaryTotForeignIncome") with Liability
  case object SummaryTotShareOptions extends ApiValue("ctnSummaryTotShareOptions") with Liability
  case object SummaryTotTrustEstates extends ApiValue("ctnSummaryTotTrustEstates") with Liability
  case object SummaryTotalDedPpr extends ApiValue("ctnSummaryTotalDedPpr") with Liability
  case object SummaryTotalEmployment extends ApiValue("ctnSummaryTotalEmployment") with Liability
  case object SummaryTotalOtherIncome extends ApiValue("ctnSummaryTotalOtherIncome") with Liability
  case object SummaryTotalPartnership extends ApiValue("ctnSummaryTotalPartnership") with Liability
  case object SummaryTotalSchedule extends ApiValue("ctnSummaryTotalScheduleD") with Liability
  case object SummaryTotalUkIntDivs extends ApiValue("ctnSummaryTotalUkIntDivs") with Liability
  case object SummaryTotalUkInterest extends ApiValue("ctnSummaryTotalUkInterest") with Liability
  case object SummaryTotalUklProperty extends ApiValue("ctnSummaryTotalUklProperty") with Liability
  case object SurplusMcaAlimonyRel extends ApiValue("atsSurplusMcaAlimonyRel") with Liability
  case object TaxablePayScottishIntermediateRate extends ApiValue("taxablePaySIR") with Liability
  case object TaxablePayScottishStarterRate extends ApiValue("taxablePaySSR") with Liability
  case object TaxCharged extends ApiValue("atsTaxCharged") with Liability
  case object TaxCreditsForDivs extends ApiValue("ctnTaxCredForDivs") with Liability
  case object TaxDueAfterAllceRlf extends ApiValue("ctn4TaxDueAfterAllceRlf") with Liability
  case object TaxExcluded extends ApiValue("taxExcluded") with Liability
  case object TaxOnPayScottishIntermediateRate extends ApiValue("taxOnPaySIR") with Liability
  case object TaxOnPayScottishStarterRate extends ApiValue("taxOnPaySSR") with Liability
  case object TopSlicingRelief extends ApiValue("topSlicingRelief") with Liability
  case object TotalTaxCreditRelief extends ApiValue("figTotalTaxCreditRelief") with Liability
  case object TradeUnionDeathBenefits extends ApiValue("itfTradeUnionDeathBenefits") with Liability
  case object VctSharesRelief extends ApiValue("ctnVctSharesReliefAmt") with Liability
  case object EmployeeClass1NI extends ApiValue("employeeClass1Nic") with Liability
  case object EmployeeClass2NI extends ApiValue("employeeClass2Nic") with Liability
  case object EmployerNI extends ApiValue("employerNic") with Liability

  // format: off
  val allLiabilities: List[Liability with ApiValue] =
    List(AnnuityPay, BPA, BpaAllowance, CGAtHigherRateRPCI, CGAtLowerRateRPCI, CGOtherGainsAfterLoss, CapAdjustment,
      CgAnnualExempt, CgAtEntrepreneursRate, CgAtHigherRate, CgAtLowerRate, CgDueEntrepreneursRate, CgDueHigherRate,
      CgDueLowerRate, CgGainsAfterLosses, CgTotGainsAfterLosses, ChildBenefitCharge, Class4Nic, CommInvTrustRel,
      DeficiencyRelief, DividendChargeableAddHRate, DividendChargeableHighRate, DividendChargeableLowRate,
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
      EmployeeClass1NI, EmployeeClass2NI, EmployerNI, LFIRelief, DisguisedRemunerationAmount)
  // format: on

  implicit val reads: Reads[Liability] = ApiValue.readFromList(allLiabilities)

}
