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

///
//   Copyright 2019 HM Revenue & Customs
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
//

package transformers

import models._
import models.Liability._

class ATSCalculations(summaryData: TaxSummaryLiability) {

  def get(liability: Liability): Amount = {
    summaryData.atsData.getOrElse(liability,
      summaryData.nationalInsuranceData.getOrElse(liability,
        throw ATSParsingException(liability.apiValue))
    )
  }

  def getWithDefaultAmount(liability: Liability): Amount ={
    try {
      get(liability)
    } catch {
      case ATSParsingException(_) => Amount(0.0, "GBP")
    }
  }

  def taxableGains(): Amount = {
    get(CgTotGainsAfterLosses) +
    get(CgGainsAfterLosses)
  }

  def payCapitalGainsTaxOn: Amount = {
    if (taxableGains < get(CgAnnualExempt)) Amount.empty
    else taxableGains - get(CgAnnualExempt)
  }

  def totalCapitalGainsTax: Amount = {
    get(CgDueEntrepreneursRate) +
      get(CgDueLowerRate) +
      get(CgDueHigherRate) -
      get(CapAdjustment) +
      getWithDefaultAmount(LowerRateCgtRPCI) +
      getWithDefaultAmount(HigherRateCgtRPCI)
  }

  def selfEmployment: Amount = {
    get(SummaryTotalSchedule) +
      get(SummaryTotalPartnership)
  }

  def otherPension: Amount = {
    get(OtherPension) +
    get(StatePensionGross)
  }

  def taxableStateBenefits: Amount = {
    get(IncBenefitSuppAllow) +
    get(JobSeekersAllowance) +
    get(OthStatePenBenefits)
  }

  def otherIncome: Amount = {
    get(SummaryTotShareOptions) +
    get(SummaryTotalUklProperty) +
    get(SummaryTotForeignIncome) +
    get(SummaryTotTrustEstates) +
    get(SummaryTotalOtherIncome) +
    get(SummaryTotalUkInterest) +
    get(SummaryTotForeignDiv) +
    get(SummaryTotalUkIntDivs) +
    get(SumTotLifePolicyGains)
  }

  def totalIncomeBeforeTax: Amount = {
    selfEmployment +
    get(SummaryTotalEmployment) +
    get(StatePension) +
    otherPension +
    taxableStateBenefits +
    otherIncome +
    get(EmploymentBenefits)
  }

  def otherAllowances: Amount = {
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
  }

  def totalTaxFreeAmount: Amount = {
    otherAllowances +
    get(PersonalAllowance) -
    getWithDefaultAmount(MarriageAllceOut)
  }

  def totalAmountEmployeeNic: Amount = {
    (
      get(EmployeeClass1NI) +
      get(EmployeeClass2NI) +
      get(Class4Nic)
    ).roundAmountUp()
  }

  def basicRateIncomeTaxAmount: Amount = {
    get(IncomeTaxBasicRate) +
    get(SavingsTaxLowerRate) +
    {
      if (summaryData.pensionLumpSumTaxRate.value == 0.20) get(PensionLsumTaxDue) //rates TODO
      else Amount.empty
    }
  }

  def higherRateIncomeTaxAmount: Amount = {
    get(IncomeTaxHigherRate) +
    get(SavingsTaxHigherRate) +
    {
      if (summaryData.pensionLumpSumTaxRate.value == 0.40) get(PensionLsumTaxDue) //rates TODO
      else Amount.empty
    }
  }

  def additionalRateIncomeTaxAmount: Amount = {
    get(IncomeTaxAddHighRate) +
    get(SavingsTaxAddHighRate) +
    {
      if (summaryData.pensionLumpSumTaxRate.value == 0.45) get(PensionLsumTaxDue) //rates TODO
      else Amount.empty
    }
  }

  def otherAdjustmentsIncreasing: Amount = {
    (
      get(NonDomCharge) +
      get(TaxExcluded) +
      get(IncomeTaxDue) +
      get(NetAnnuityPaytsTaxDue) +
      get(ChildBenefitCharge) +
      get(PensionSavingChargeable)
    ) - get(TaxDueAfterAllceRlf)
  }

  def otherAdjustmentsReducing: Amount = {
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
  }

  def totalIncomeTaxAmount: Amount = {

    println("-" * 50)
    println(get(SavingsTaxStartingRate))
    println(  basicRateIncomeTaxAmount)
    println(higherRateIncomeTaxAmount)
    println(  get(DividendTaxLowRate))
    println(get(DividendTaxHighRate))
    println(get(DividendTaxAddHighRate))
    println(otherAdjustmentsIncreasing)
    println(otherAdjustmentsReducing)
    println(getWithDefaultAmount(MarriageAllceIn))


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
  }

  def totalAmountTaxAndNics: Amount = {
    totalAmountEmployeeNic +
    totalIncomeTaxAmount
  }

  def totalTax: Amount = {
    totalAmountTaxAndNics +
    totalCapitalGainsTax
  }

  def basicIncomeRateIncomeTax: Amount = {
    getWithDefaultAmount(IncomeChargeableBasicRate) +
    get(SavingsChargeableLowerRate)
  }

  def higherRateIncomeTax: Amount = {
    getWithDefaultAmount(IncomeChargeableHigherRate) +
    get(SavingsChargeableHigherRate)
  }

  def additionalRateIncomeTax: Amount = {
    getWithDefaultAmount(IncomeChargeableAddHRate) +
    get(SavingsChargeableAddHRate)
  }

  def scottishIncomeTax = {
    val scottishRate = 0.1

    Amount.gbp((
      getWithDefaultAmount(IncomeChargeableBasicRate) +
      getWithDefaultAmount(IncomeChargeableHigherRate) +
      getWithDefaultAmount(IncomeChargeableAddHRate)
    ).amount * scottishRate)
  }

  def hasLiability: Boolean = {
    !(totalCapitalGainsTax + totalIncomeTaxAmount).isZeroOrLess
  }

  def capitalGainsTaxPerCurrency: Amount = {
    taxPerTaxableCurrencyUnit(totalCapitalGainsTax, taxableGains())
  }

  def nicsAndTaxPerCurrency: Amount = {
    taxPerTaxableCurrencyUnit(totalAmountTaxAndNics, totalIncomeBeforeTax)
  }

  private def taxPerTaxableCurrencyUnit(tax: Amount, taxable: Amount): Amount =
    taxable match {
      case value if value.isZero => taxable
      case _ => tax.divideWithPrecision(taxable, 4)
    }

  def totalNicsAndTaxLiabilityAsPercentage: Rate = liabilityAsPercentage(nicsAndTaxPerCurrency)

  def totalCgTaxLiabilityAsPercentage: Rate = liabilityAsPercentage(capitalGainsTaxPerCurrency)

  private def liabilityAsPercentage(amountPerUnit: Amount) =
    Rate.rateFromPerUnitAmount(amountPerUnit)
}