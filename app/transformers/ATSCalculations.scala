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

package transformers

import models._
import models.Liability._
import models.LiabilityKey.StartingRateForSavingsAmount
import play.api.Logger
import services._
import utils.DoubleUtils

sealed trait ATSCalculations extends DoubleUtils {

  val summaryData: TaxSummaryLiability
  val taxRates: TaxRateService

  def get(liability: Liability): Amount =
    summaryData.atsData.getOrElse(
      liability,
      summaryData.nationalInsuranceData.getOrElse(liability, {
        Logger.error(s"Unable to retrieve $liability")
        throw ATSParsingException(liability.apiValue)
      })
    )

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
      get(SumTotLifePolicyGains) +
      getWithDefaultAmount(DisguisedRemunerationAmount)

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
    get(EmployeeClass1NI) +
      get(EmployeeClass2NI) +
      get(Class4Nic)

  def basicRateIncomeTaxAmount: Amount =
    get(IncomeTaxBasicRate) +
      get(SavingsTaxLowerRate) +
      includePensionTaxForRate(taxRates.basicRateIncomeTaxRate())

  def higherRateIncomeTaxAmount: Amount =
    get(IncomeTaxHigherRate) +
      get(SavingsTaxHigherRate) +
      includePensionTaxForRate(taxRates.higherRateIncomeTaxRate())

  def additionalRateIncomeTaxAmount: Amount =
    get(IncomeTaxAddHighRate) +
      get(SavingsTaxAddHighRate) +
      includePensionTaxForRate(taxRates.additionalRateIncomeTaxRate())

  def scottishStarterRateTax: Amount = Amount.empty
  def scottishBasicRateTax: Amount = Amount.empty
  def scottishIntermediateRateTax: Amount = Amount.empty
  def scottishHigherRateTax: Amount = Amount.empty
  def scottishAdditionalRateTax: Amount = Amount.empty

  def scottishTotalTax: Amount =
    scottishStarterRateTax + scottishBasicRateTax + scottishIntermediateRateTax + scottishHigherRateTax + scottishAdditionalRateTax

  def scottishStarterRateIncome: Amount = Amount.empty
  def scottishBasicRateIncome: Amount = Amount.empty
  def scottishIntermediateRateIncome: Amount = Amount.empty
  def scottishHigherRateIncome: Amount = Amount.empty
  def scottishAdditionalRateIncome: Amount = Amount.empty

  def savingsBasicRateTax: Amount = Amount.empty
  def savingsHigherRateTax: Amount = Amount.empty
  def savingsAdditionalRateTax: Amount = Amount.empty

  def savingsBasicRateIncome: Amount = Amount.empty
  def savingsHigherRateIncome: Amount = Amount.empty
  def savingsAdditionalRateIncome: Amount = Amount.empty

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
        getWithDefaultAmount(ReliefForFinanceCosts) +
        getWithDefaultAmount(LFIRelief)
    ).roundAmountUp()

  def totalIncomeTaxAmount: Amount =
    savingsRateAmount +
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

  def basicRateIncomeTax: Amount =
    getWithDefaultAmount(IncomeChargeableBasicRate) +
      get(SavingsChargeableLowerRate) +
      includePensionIncomeForRate(taxRates.basicRateIncomeTaxRate())

  def higherRateIncomeTax: Amount =
    getWithDefaultAmount(IncomeChargeableHigherRate) +
      get(SavingsChargeableHigherRate) +
      includePensionIncomeForRate(taxRates.higherRateIncomeTaxRate())

  def additionalRateIncomeTax: Amount =
    getWithDefaultAmount(IncomeChargeableAddHRate) +
      get(SavingsChargeableAddHRate) +
      includePensionIncomeForRate(taxRates.additionalRateIncomeTaxRate())

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

  def savingsRate: Amount = get(SavingsChargeableStartRate)

  def savingsRateAmount: Amount = get(SavingsTaxStartingRate)

  protected def taxPerTaxableCurrencyUnit(tax: Amount, taxable: Amount): Amount =
    taxable match {
      case value if value.isZero => taxable
      case _                     => tax.divideWithPrecision(taxable, 4)
    }

  def totalNicsAndTaxLiabilityAsPercentage: Rate = liabilityAsPercentage(nicsAndTaxPerCurrency)

  def totalCgTaxLiabilityAsPercentage: Rate = liabilityAsPercentage(capitalGainsTaxPerCurrency)

  def includePensionTaxForRate(taxRate: Rate): Amount =
    if (summaryData.pensionLumpSumTaxRate.percentage === taxRate.percent) get(PensionLsumTaxDue)
    else Amount.empty

  def includePensionIncomeForRate(taxRate: Rate): Amount =
    if (summaryData.pensionLumpSumTaxRate.percentage === taxRate.percent) get(StatePensionGross)
    else Amount.empty

  protected def liabilityAsPercentage(amountPerUnit: Amount): Rate =
    Rate.rateFromPerUnitAmount(amountPerUnit)
}

sealed class DefaultATSCalculations(val summaryData: TaxSummaryLiability, val taxRates: TaxRateService)
    extends ATSCalculations

sealed class Post2018ATSCalculations(val summaryData: TaxSummaryLiability, val taxRates: TaxRateService)
    extends ATSCalculations {

  override def scottishIncomeTax: Amount = Amount.empty

  override def savingsRate: Amount = Amount.empty

  override def savingsRateAmount: Amount = Amount.empty
}

sealed class Post2018ScottishATSCalculations(val summaryData: TaxSummaryLiability, val taxRates: TaxRateService)
    extends ATSCalculations {

  override def scottishIncomeTax: Amount = Amount.empty
  override def savingsRate: Amount = Amount.empty
  override def savingsRateAmount: Amount = Amount.empty

  override def basicRateIncomeTaxAmount: Amount = Amount.empty
  override def higherRateIncomeTaxAmount: Amount = Amount.empty
  override def additionalRateIncomeTaxAmount: Amount = Amount.empty
  override def basicRateIncomeTax: Amount = Amount.empty
  override def higherRateIncomeTax: Amount = Amount.empty
  override def additionalRateIncomeTax: Amount = Amount.empty

  override def scottishStarterRateTax: Amount =
    getWithDefaultAmount(TaxOnPayScottishStarterRate) + includePensionTaxForRate(taxRates.scottishStarterRate)

  override def scottishBasicRateTax: Amount =
    getWithDefaultAmount(IncomeTaxBasicRate) + includePensionTaxForRate(taxRates.scottishBasicRate)

  override def scottishIntermediateRateTax: Amount =
    getWithDefaultAmount(TaxOnPayScottishIntermediateRate) + includePensionTaxForRate(taxRates.scottishIntermediateRate)

  override def scottishHigherRateTax: Amount =
    getWithDefaultAmount(IncomeTaxHigherRate) + includePensionTaxForRate(taxRates.scottishHigherRate)

  override def scottishAdditionalRateTax: Amount =
    getWithDefaultAmount(IncomeTaxAddHighRate) + includePensionTaxForRate(taxRates.scottishAdditionalRate)

  override def scottishStarterRateIncome: Amount =
    getWithDefaultAmount(TaxablePayScottishStarterRate) + includePensionIncomeForRate(taxRates.scottishStarterRate)

  override def scottishBasicRateIncome: Amount =
    getWithDefaultAmount(IncomeChargeableBasicRate) + includePensionIncomeForRate(taxRates.scottishBasicRate)

  override def scottishIntermediateRateIncome: Amount =
    getWithDefaultAmount(TaxablePayScottishIntermediateRate) + includePensionIncomeForRate(
      taxRates.scottishIntermediateRate)

  override def scottishHigherRateIncome: Amount =
    getWithDefaultAmount(IncomeChargeableHigherRate) + includePensionIncomeForRate(taxRates.scottishHigherRate)

  override def scottishAdditionalRateIncome: Amount =
    getWithDefaultAmount(IncomeChargeableAddHRate) + includePensionIncomeForRate(taxRates.scottishAdditionalRate)

  override def savingsBasicRateTax: Amount = getWithDefaultAmount(SavingsTaxLowerRate)
  override def savingsHigherRateTax: Amount = getWithDefaultAmount(SavingsTaxHigherRate)
  override def savingsAdditionalRateTax: Amount = getWithDefaultAmount(SavingsTaxAddHighRate)

  override def savingsBasicRateIncome: Amount = getWithDefaultAmount(SavingsChargeableLowerRate)
  override def savingsHigherRateIncome: Amount = getWithDefaultAmount(SavingsChargeableHigherRate)
  override def savingsAdditionalRateIncome: Amount = getWithDefaultAmount(SavingsChargeableAddHRate)

  private val savingsTotalTax = savingsBasicRateTax + savingsHigherRateTax + savingsAdditionalRateTax

  override def totalIncomeTaxAmount: Amount =
    super.totalIncomeTaxAmount + scottishTotalTax + savingsTotalTax
}

object ATSCalculations {

  def make(summaryData: TaxSummaryLiability, taxRates: TaxRateService): ATSCalculations =
    if (summaryData.taxYear > 2018 && summaryData.isScottish) {
      new Post2018ScottishATSCalculations(summaryData, taxRates)
    } else if (summaryData.taxYear > 2018) {
      new Post2018ATSCalculations(summaryData, taxRates)
    } else {
      new DefaultATSCalculations(summaryData, taxRates)
    }
}
