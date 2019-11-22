/*
 * Copyright 2019 HM Revenue & Customs
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

import models._
import models.Liability._
import services._

sealed trait ATSCalculations {

  val summaryData: TaxSummaryLiability
  val taxRates: TaxRateService

  val taxYear: Int = summaryData.taxYear

  def get(liability: Liability): Amount =
    summaryData.atsData.getOrElse(
      liability,
      summaryData.nationalInsuranceData.getOrElse(liability, throw ATSParsingException(liability.apiValue)))

  def getWithDefaultAmount(liability: Liability): Amount =
    try {
      get(liability)
    } catch {
      case ATSParsingException(_) => Amount.empty
    }

  def taxableGains(): Amount =
    get(CgTotGainsAfterLosses) +
      get(CgGainsAfterLosses)

  def payCapitalGainsTaxOn: Amount =
    if (taxableGains < get(CgAnnualExempt)) Amount.empty
    else taxableGains - get(CgAnnualExempt)

  def totalCapitalGainsTax: Amount =
    get(CgDueEntrepreneursRate) +
      get(CgDueLowerRate) +
      get(CgDueHigherRate) -
      get(CapAdjustment)

  def selfEmployment: Amount =
    get(SummaryTotalSchedule) +
      get(SummaryTotalPartnership)

  def otherPension: Amount =
    get(OtherPension) +
      get(StatePensionGross)

  def taxableStateBenefits: Amount =
    get(IncBenefitSuppAllow) +
      get(JobSeekersAllowance) +
      get(OthStatePenBenefits)

  def otherIncome: Amount =
    get(SummaryTotShareOptions) +
      get(SummaryTotalUklProperty) +
      get(SummaryTotForeignIncome) +
      get(SummaryTotTrustEstates) +
      get(SummaryTotalOtherIncome) +
      get(SummaryTotalUkInterest) +
      get(SummaryTotForeignDiv) +
      get(SummaryTotalUkIntDivs) +
      get(SumTotLifePolicyGains)

  def totalIncomeBeforeTax: Amount =
    selfEmployment +
      get(SummaryTotalEmployment) +
      get(StatePension) +
      otherPension +
      taxableStateBenefits +
      otherIncome +
      get(EmploymentBenefits)

  def otherAllowances: Amount =
    (
      get(EmploymentExpenses) +
        get(SummaryTotalDedPpr) +
        get(SumTotForeignTaxRelief) +
        get(SumTotLoanRestricted) +
        get(SumTotLossRestricted) +
        get(AnnuityPay) +
        get(GiftsInvCharities) +
        get(TradeUnionDeathBenefits) +
        get(BpaAllowance) +
        get(BPA) +
        get(ExcludedIncome)
    ).roundAmountUp()

  def totalTaxFreeAmount: Amount =
    otherAllowances +
      get(PersonalAllowance) -
      getWithDefaultAmount(MarriageAllceOut)

  def totalAmountEmployeeNic: Amount =
    (
      get(EmployeeClass1NI) +
        get(EmployeeClass2NI) +
        get(Class4Nic)
    ).roundAmountUp()

  def basicRateIncomeTaxAmount: Amount =
    get(IncomeTaxBasicRate) +
      get(SavingsTaxLowerRate) +
      includePensionForRate(taxRates.basicRateIncomeTaxRate())

  def higherRateIncomeTaxAmount: Amount =
    get(IncomeTaxHigherRate) +
      get(SavingsTaxHigherRate) +
      includePensionForRate(taxRates.higherRateIncomeTaxRate())

  def additionalRateIncomeTaxAmount: Amount =
    get(IncomeTaxAddHighRate) +
      get(SavingsTaxAddHighRate) +
      includePensionForRate(taxRates.additionalRateIncomeTaxRate())

  def scottishStarterRateTaxAmount: Amount = Amount.empty
  def scottishBasicRateTaxAmount: Amount = Amount.empty
  def scottishIntermediateRateTaxAmount: Amount = Amount.empty
  def scottishHigherRateTaxAmount: Amount = Amount.empty
  def scottishAdditionalRateTaxAmount: Amount = Amount.empty

  def otherAdjustmentsIncreasing: Amount =
    (
      get(NonDomCharge) +
        get(TaxExcluded) +
        get(IncomeTaxDue) +
        get(NetAnnuityPaytsTaxDue) +
        get(ChildBenefitCharge) +
        get(PensionSavingChargeable)
    ) - get(TaxDueAfterAllceRlf)

  def otherAdjustmentsReducing: Amount =
    (
      get(DeficiencyRelief) +
        get(TopSlicingRelief) +
        get(VctSharesRelief) +
        get(EisRelief) +
        get(SeedEisRelief) +
        get(CommInvTrustRel) +
        get(SurplusMcaAlimonyRel) +
        get(NotionalTaxCegs) +
        get(NotlTaxOtherSource) +
        get(TaxCreditsForDivs) +
        get(QualDistnRelief) +
        get(TotalTaxCreditRelief) +
        get(NonPayableTaxCredits) +
        getWithDefaultAmount(ReliefForFinanceCosts)
    ).roundAmountUp()

  def totalIncomeTaxAmount: Amount =
    get(SavingsTaxStartingRate) +
      basicRateIncomeTaxAmount +
      higherRateIncomeTaxAmount +
      additionalRateIncomeTaxAmount +
      get(DividendTaxLowRate) +
      get(DividendTaxHighRate) +
      get(DividendTaxAddHighRate) +
      otherAdjustmentsIncreasing -
      otherAdjustmentsReducing -
      getWithDefaultAmount(MarriageAllceIn)

  def totalAmountTaxAndNics: Amount =
    totalAmountEmployeeNic +
      totalIncomeTaxAmount

  def totalTax: Amount =
    totalAmountTaxAndNics +
      totalCapitalGainsTax

  def basicIncomeRateIncomeTax: Amount =
    getWithDefaultAmount(IncomeChargeableBasicRate) +
      get(SavingsChargeableLowerRate)

  def higherRateIncomeTax: Amount =
    getWithDefaultAmount(IncomeChargeableHigherRate) +
      get(SavingsChargeableHigherRate)

  def additionalRateIncomeTax: Amount =
    getWithDefaultAmount(IncomeChargeableAddHRate) +
      get(SavingsChargeableAddHRate)

  def scottishIncomeTax: Amount = {
    val scottishRate = 0.1

    Amount.gbp(
      (
        getWithDefaultAmount(IncomeChargeableBasicRate) +
          getWithDefaultAmount(IncomeChargeableHigherRate) +
          getWithDefaultAmount(IncomeChargeableAddHRate)
      ).amount * scottishRate)
  }

  def hasLiability: Boolean =
    !(totalCapitalGainsTax + totalIncomeTaxAmount).isZeroOrLess

  def capitalGainsTaxPerCurrency: Amount =
    taxPerTaxableCurrencyUnit(totalCapitalGainsTax, taxableGains())

  def nicsAndTaxPerCurrency: Amount =
    taxPerTaxableCurrencyUnit(totalAmountTaxAndNics, totalIncomeBeforeTax)

  protected def taxPerTaxableCurrencyUnit(tax: Amount, taxable: Amount): Amount =
    taxable match {
      case value if value.isZero => taxable
      case _                     => tax.divideWithPrecision(taxable, 4)
    }

  def totalNicsAndTaxLiabilityAsPercentage: Rate = liabilityAsPercentage(nicsAndTaxPerCurrency)

  def totalCgTaxLiabilityAsPercentage: Rate = liabilityAsPercentage(capitalGainsTaxPerCurrency)

  protected def includePensionForRate(taxRate: Rate): Amount =
    if (summaryData.pensionLumpSumTaxRate.percentage == taxRate.percent) get(PensionLsumTaxDue)
    else Amount.empty

  protected def liabilityAsPercentage(amountPerUnit: Amount) =
    Rate.rateFromPerUnitAmount(amountPerUnit)
}

sealed class DefaultATSCalculations(val summaryData: TaxSummaryLiability, val taxRates: TaxRateService)
    extends ATSCalculations

sealed class Post2017ATSCalculations(val summaryData: TaxSummaryLiability, val taxRates: TaxRateService)
    extends ATSCalculations {

  override def scottishIncomeTax: Amount = Amount.empty
}

sealed class Post2017ScottishATSCalculations(val summaryData: TaxSummaryLiability, val taxRates: TaxRateService)
    extends ATSCalculations {

  override def scottishIncomeTax: Amount = Amount.empty

  override def basicRateIncomeTaxAmount: Amount = Amount.empty
  override def higherRateIncomeTaxAmount: Amount = Amount.empty
  override def additionalRateIncomeTaxAmount: Amount = Amount.empty

  override def scottishStarterRateTaxAmount: Amount =
    get(TaxOnPayScottishStarterRate) + includePensionForRate(taxRates.scottishStarterRate)

  override def scottishBasicRateTaxAmount: Amount =
    get(IncomeTaxBasicRate) + includePensionForRate(taxRates.scottishBasicRate)

  override def scottishIntermediateRateTaxAmount: Amount =
    get(TaxOnPayScottishIntermediateRate) + includePensionForRate(taxRates.scottishIntermediateRate)

  override def scottishHigherRateTaxAmount: Amount =
    get(IncomeTaxHigherRate) + includePensionForRate(taxRates.scottishHigherRate)

  override def scottishAdditionalRateTaxAmount: Amount =
    get(IncomeTaxAddHighRate) + includePensionForRate(taxRates.scottishAdditionalRate)
}

object ATSCalculations {

  def make(summaryData: TaxSummaryLiability, taxRates: TaxRateService): ATSCalculations =
    if (summaryData.taxYear > 2017 && summaryData.isScottish) {
      new Post2017ScottishATSCalculations(summaryData, taxRates)
    } else if (summaryData.taxYear > 2017) {
      new Post2017ATSCalculations(summaryData, taxRates)
    } else {
      new DefaultATSCalculations(summaryData, taxRates)
    }
}
