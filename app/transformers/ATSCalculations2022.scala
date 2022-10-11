/*
 * Copyright 2022 HM Revenue & Customs
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

package transformers

import models.Liability._
import models._
import services._

trait ATSCalculations2022 extends ATSCalculations2021 {

  val summaryData: TaxSummaryLiability
  val taxRates: TaxRateService

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

  override def savingsRateAmount: Amount = get(SavingsTaxStartingRate) + get(TaxOnCegSr)

  override def savingsRate: Amount = get(SavingsChargeableStartRate) + get(TaxableCegSr)
}
