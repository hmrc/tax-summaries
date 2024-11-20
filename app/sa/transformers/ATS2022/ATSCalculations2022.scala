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

package sa.transformers.ATS2022

import common.models.Amount
import sa.models.ODSLiabilities.ODSLiabilities.{Alimony, AnnuityPay, BPA, BpaAllowance, ChildBenefitCharge, Class2NicAmt, Class4Nic, CommInvTrustRel, DeficiencyRelief, DividendTaxAddHighRate, DividendTaxHighRate, DividendTaxLowRate, DividendsPartnership, EisRelief, EmployeeClass1NI, EmploymentExpenses, ForeignCegDedn, FtcrRestricted, GiftAidTaxReduced, GiftsInvCharities, IncomeChargeableAddHRate, IncomeChargeableBasicRate, IncomeChargeableHigherRate, IncomeTaxAddHighRate, IncomeTaxBasicRate, IncomeTaxHigherRate, ItfCegReceivedAfterTax, LFIRelief, MarriageAllceIn, NetAnnuityPaytsTaxDue, NonDomCharge, NotionalTaxCegs, NotlTaxOtherSource, PensionSavingChargeable, ReliefForFinanceCosts, SavingsChargeableAddHRate, SavingsChargeableHigherRate, SavingsChargeableLowerRate, SavingsChargeableStartRate, SavingsPartnership, SavingsTaxAddHighRate, SavingsTaxHigherRate, SavingsTaxLowerRate, SavingsTaxStartingRate, SeedEisRelief, SocialInvTaxRel, SumTotForeignTaxRelief, SumTotLifePolicyGains, SumTotLossRestricted, SummaryTotForeignDiv, SummaryTotForeignIncome, SummaryTotForeignSav, SummaryTotShareOptions, SummaryTotTrustEstates, SummaryTotalDedPpr, SummaryTotalOtherIncome, SummaryTotalPartnership, SummaryTotalSchedule, SummaryTotalUkIntDivs, SummaryTotalUkInterest, SummaryTotalUklProperty, SurplusMcaAlimonyRel, TaxExcluded, TaxOnCegAhr, TaxOnCegBr, TaxOnCegHr, TaxOnCegSr, TaxOnNonExcludedIncome, TaxOnRedundancyAhr, TaxOnRedundancyBr, TaxOnRedundancyHr, TaxableCegAhr, TaxableCegBr, TaxableCegHr, TaxableCegSr, TaxableRedundancyAhr, TaxableRedundancyBr, TaxableRedundancyHr, TopSlicingRelief, VctSharesRelief}
import sa.models.TaxSummaryLiability
import sa.services.TaxRateService
import sa.transformers.ATSCalculations

trait ATSCalculations2022 extends ATSCalculations {
  protected val summaryData: TaxSummaryLiability
  protected val taxRates: TaxRateService

  override def selfEmployment: Amount =
    get(SummaryTotalSchedule) +
      get(SummaryTotalPartnership) +
      get(SavingsPartnership) +
      get(DividendsPartnership)

  override def otherIncome: Amount =
    get(SummaryTotShareOptions) +
      get(SummaryTotalUklProperty) +
      get(SummaryTotForeignIncome) +
      get(SummaryTotTrustEstates) +
      get(SummaryTotalOtherIncome) +
      get(SummaryTotalUkInterest) +
      get(SummaryTotForeignDiv) +
      get(SummaryTotalUkIntDivs) +
      get(SumTotLifePolicyGains) +
      get(SummaryTotForeignSav) +
      get(ForeignCegDedn) +
      get(ItfCegReceivedAfterTax)

  override def otherAllowances: Amount =
    (
      get(EmploymentExpenses) +
        get(SummaryTotalDedPpr) +
        get(SumTotForeignTaxRelief) +
        get(SumTotLossRestricted) +
        get(AnnuityPay) +
        get(GiftsInvCharities) +
        get(BpaAllowance) +
        get(BPA)
    ).roundAmountUp()

  override def otherAdjustmentsIncreasing: Amount =
    get(NonDomCharge) +
      get(GiftAidTaxReduced) +
      get(NetAnnuityPaytsTaxDue) +
      get(ChildBenefitCharge) +
      get(PensionSavingChargeable)

  override def totalIncomeTaxAmount: Amount = {
    val rateDividendAdjustmentTax = savingsRateAmount + // LS12.1
      basicRateIncomeTaxAmount + // LS12.2
      higherRateIncomeTaxAmount + // LS12.3
      additionalRateIncomeTaxAmount + //LS12.4
      get(DividendTaxLowRate) + //LS13.1
      get(DividendTaxHighRate) + //LS13.2
      get(DividendTaxAddHighRate) +
      otherAdjustmentsIncreasing -
      otherAdjustmentsReducing -
      getWithDefaultAmount(MarriageAllceIn)

    val excludedAndNonExcludedTax = get(TaxExcluded) + getWithDefaultAmount(TaxOnNonExcludedIncome)

    if (excludedAndNonExcludedTax.amount > 0) {
      List(rateDividendAdjustmentTax, excludedAndNonExcludedTax).min
    } else {
      rateDividendAdjustmentTax
    }
  }

  override def otherAdjustmentsReducing: Amount =
    get(DeficiencyRelief) +
      get(TopSlicingRelief) +
      get(VctSharesRelief) +
      get(EisRelief) +
      get(SeedEisRelief) +
      get(CommInvTrustRel) +
      get(SocialInvTaxRel) +
      get(SurplusMcaAlimonyRel) +
      getWithDefaultAmount(Alimony) +
      get(NotionalTaxCegs) +
      get(NotlTaxOtherSource) +
      get(FtcrRestricted) +
      getWithDefaultAmount(ReliefForFinanceCosts) +
      getWithDefaultAmount(LFIRelief)

  override def totalAmountEmployeeNic: Amount =
    get(EmployeeClass1NI) +
      get(Class2NicAmt) +
      get(Class4Nic)

  override def basicRateIncomeTax: Amount =
    getWithDefaultAmount(IncomeChargeableBasicRate) +
      get(SavingsChargeableLowerRate) +
      get(TaxableRedundancyBr) +
      get(TaxableCegBr) +
      includePensionIncomeForRate(taxRates.basicRateIncomeTaxRate())

  override def basicRateIncomeTaxAmount: Amount =
    get(IncomeTaxBasicRate) +
      get(SavingsTaxLowerRate) +
      get(TaxOnRedundancyBr) +
      get(TaxOnCegBr) +
      includePensionTaxForRate(taxRates.basicRateIncomeTaxRate())

  override def higherRateIncomeTax: Amount =
    getWithDefaultAmount(IncomeChargeableHigherRate) +
      get(SavingsChargeableHigherRate) +
      get(TaxableRedundancyHr) +
      get(TaxableCegHr) +
      includePensionIncomeForRate(taxRates.higherRateIncomeTaxRate())

  override def higherRateIncomeTaxAmount: Amount =
    get(IncomeTaxHigherRate) +
      get(SavingsTaxHigherRate) +
      get(TaxOnRedundancyHr) +
      get(TaxOnCegHr) +
      includePensionTaxForRate(taxRates.higherRateIncomeTaxRate())

  override def additionalRateIncomeTaxAmount: Amount =
    get(IncomeTaxAddHighRate) +
      get(SavingsTaxAddHighRate) +
      get(TaxOnRedundancyAhr) +
      get(TaxOnCegAhr) +
      includePensionTaxForRate(taxRates.additionalRateIncomeTaxRate())

  override def additionalRateIncomeTax: Amount =
    getWithDefaultAmount(IncomeChargeableAddHRate) +
      get(SavingsChargeableAddHRate) +
      get(TaxableRedundancyAhr) +
      get(TaxableCegAhr) +
      includePensionIncomeForRate(taxRates.additionalRateIncomeTaxRate())

  override def savingsRateAmount: Amount = get(SavingsTaxStartingRate) + get(TaxOnCegSr)

  override def savingsRate: Amount = get(SavingsChargeableStartRate) + get(TaxableCegSr)
}
