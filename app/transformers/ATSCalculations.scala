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
import play.api.Logger
import services._

class ATSCalculations(summaryData: TaxSummaryLiability, taxYear: Int, taxRates: TaxRateService) {

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

  def taxableGains: Amount =
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
    get(EmployeeClass1NI) +
      get(EmployeeClass2NI) +
      get(Class4Nic)

  def basicRateIncomeTaxAmount: Amount =
    get(IncomeTaxBasicRate) +
      get(SavingsTaxLowerRate) + {
      includePensionTaxForRate(taxRates.basicRateIncomeTaxRate())
    }

  def higherRateIncomeTaxAmount: Amount =
    get(IncomeTaxHigherRate) +
      get(SavingsTaxHigherRate) + {
      includePensionTaxForRate(taxRates.higherRateIncomeTaxRate())
    }

  def additionalRateIncomeTaxAmount: Amount =
    get(IncomeTaxAddHighRate) +
      get(SavingsTaxAddHighRate) + {
      includePensionTaxForRate(taxRates.additionalRateIncomeTaxRate())

    }

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
    get(IncomeChargeableBasicRate) +
      get(SavingsChargeableLowerRate)

  def higherRateIncomeTax: Amount =
    get(IncomeChargeableHigherRate) +
      get(SavingsChargeableHigherRate)

  def additionalRateIncomeTax: Amount =
    get(IncomeChargeableAddHRate) +
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
    taxPerTaxableCurrencyUnit(totalCapitalGainsTax, taxableGains)

  def nicsAndTaxPerCurrency: Amount =
    taxPerTaxableCurrencyUnit(totalAmountTaxAndNics, totalIncomeBeforeTax)

  private def taxPerTaxableCurrencyUnit(tax: Amount, taxable: Amount): Amount =
    taxable match {
      case value if value.isZero => taxable
      case _                     => tax.divideWithPrecision(taxable, 4)
    }

  def totalNicsAndTaxLiabilityAsPercentage: Rate = liabilityAsPercentage(nicsAndTaxPerCurrency)

  def totalCgTaxLiabilityAsPercentage: Rate = liabilityAsPercentage(capitalGainsTaxPerCurrency)

  private def includePensionTaxForRate(taxRate: Rate): Amount =
    if (summaryData.pensionLumpSumTaxRate.value * 100 == taxRate.percent) get(PensionLsumTaxDue)
    else Amount.empty

  private def liabilityAsPercentage(amountPerUnit: Amount) =
    Rate.rateFromPerUnitAmount(amountPerUnit)
}
