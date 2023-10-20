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

package transformers

import models.ODSLiabilities.ODSLiabilities
import models.ODSLiabilities.ODSLiabilities._
import models._
import play.api.Logging
import services._
import transformers.ATS2019.{ATSCalculationsScottish2019, ATSCalculationsUK2019}
import transformers.ATS2020.ATSCalculationsWelsh2020
import transformers.ATS2021.{ATSCalculationsScottish2021, ATSCalculationsUK2021, ATSCalculationsWelsh2021}
import transformers.ATS2022.{ATSCalculationsScottish2022, ATSCalculationsUK2022, ATSCalculationsWelsh2022}
import utils.DoubleUtils

// scalastyle:off number.of.methods
trait ATSCalculations extends DoubleUtils with Logging {
  protected val summaryData: TaxSummaryLiability
  protected val taxRates: TaxRateService
  val incomeTaxStatus: Option[Nationality] = summaryData.incomeTaxStatus

  def get(liability: ODSLiabilities): Amount = {
    val result = summaryData.atsData.getOrElse(
      liability,
      summaryData.nationalInsuranceData.getOrElse(
        liability, {
          val ex = ATSParsingException(liability.apiValue)
          logger.error(s"Unable to retrieve $liability", ex)
          throw ex
        }
      )
    )
    result.copy(calculus = Some(s"${result.amount}(${liability.apiValue})"))
  }

  def getWithDefaultAmount(liability: ODSLiabilities): Amount = {
    val result = summaryData.atsData.getOrElse(
      liability,
      summaryData.nationalInsuranceData.getOrElse(liability, Amount.empty(liability.apiValue))
    )
    result.copy(calculus = Some(s"${result.amount}(${liability.apiValue})"))
  }

  def taxableGains(): Amount =
    get(CgTotGainsAfterLosses) +
      get(CgGainsAfterLosses)

  def payCapitalGainsTaxOn: Amount =
    if (taxableGains() < get(CgAnnualExempt)) Amount.empty("taxableGains() < get(CgAnnualExempt)")
    else taxableGains() - get(CgAnnualExempt)

  def totalCapitalGainsTax: Amount =
    (getWithDefaultAmount(LowerRateCgtRPCI) +
      getWithDefaultAmount(HigherRateCgtRPCI) +
      get(CgDueEntrepreneursRate) +
      get(CgDueLowerRate) +
      get(CgDueHigherRate) +
      get(CapAdjustment)).max(0)

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

  def scottishStarterRateTax: Amount = Amount.empty("scottishStarterRateTax")

  def scottishBasicRateTax: Amount = Amount.empty("scottishBasicRateTax")

  def scottishIntermediateRateTax: Amount = Amount.empty("scottishIntermediateRateTax")

  def scottishHigherRateTax: Amount = Amount.empty("scottishHigherRateTax")

  def scottishAdditionalRateTax: Amount = Amount.empty("scottishAdditionalRateTax")

  def scottishTotalTax: Amount =
    scottishStarterRateTax + scottishBasicRateTax + scottishIntermediateRateTax + scottishHigherRateTax + scottishAdditionalRateTax

  def scottishStarterRateIncome: Amount = Amount.empty("scottishStarterRateIncome")

  def scottishBasicRateIncome: Amount = Amount.empty("scottishBasicRateIncome")

  def scottishIntermediateRateIncome: Amount = Amount.empty("scottishIntermediateRateIncome")

  def scottishHigherRateIncome: Amount = Amount.empty("scottishHigherRateIncome")

  def scottishAdditionalRateIncome: Amount = Amount.empty("scottishAdditionalRateIncome")

  def savingsBasicRateTax: Amount = Amount.empty("savingsBasicRateTax")

  def savingsHigherRateTax: Amount = Amount.empty("savingsHigherRateTax")

  def savingsAdditionalRateTax: Amount = Amount.empty("savingsAdditionalRateTax")

  def savingsBasicRateIncome: Amount = Amount.empty("savingsBasicRateIncome")

  def savingsHigherRateIncome: Amount = Amount.empty("savingsHigherRateIncome")

  def savingsAdditionalRateIncome: Amount = Amount.empty("savingsAdditionalRateIncome")

  def welshIncomeTax: Amount = Amount.empty("welshIncomeTax")

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
      getWithDefaultAmount(LFIRelief) +
      getWithDefaultAmount(Alimony)

  def totalIncomeTaxAmount: Amount =
    savingsRateAmount + // LS12.1
      basicRateIncomeTaxAmount + // LS12.2
      higherRateIncomeTaxAmount + // LS12.3
      additionalRateIncomeTaxAmount +
      get(DividendTaxLowRate) +
      get(DividendTaxHighRate) + //LS13.2
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
    (getWithDefaultAmount(IncomeChargeableBasicRate) +
      getWithDefaultAmount(IncomeChargeableHigherRate) +
      getWithDefaultAmount(IncomeChargeableAddHRate)) * scottishRate
  }

  def taxLiability: Amount = totalCapitalGainsTax + totalIncomeTaxAmount

  def hasLiability: Boolean =
    !taxLiability.isZeroOrLess

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
    else Amount.empty(PensionLsumTaxDue.apiValue)

  def includePensionIncomeForRate(taxRate: Rate): Amount =
    if (summaryData.pensionLumpSumTaxRate.percentage === taxRate.percent) get(StatePensionGross)
    else Amount.empty(StatePensionGross.apiValue)

  protected def liabilityAsPercentage(amountPerUnit: Amount): Rate =
    Rate.rateFromPerUnitAmount(amountPerUnit)
}

sealed class DefaultATSCalculations(val summaryData: TaxSummaryLiability, val taxRates: TaxRateService)
    extends ATSCalculations

object ATSCalculations {

  def make(summaryData: TaxSummaryLiability, taxRates: TaxRateService): ATSCalculations =
    (summaryData.nationality, summaryData.taxYear) match {
      case (_: UK, year) if year > 2021       => new ATSCalculationsUK2022(summaryData, taxRates)
      case (_: Scottish, year) if year > 2021 => new ATSCalculationsScottish2022(summaryData, taxRates)
      case (_: Welsh, year) if year > 2021    => new ATSCalculationsWelsh2022(summaryData, taxRates)
      case (_: UK, year) if year > 2020       => new ATSCalculationsUK2021(summaryData, taxRates)
      case (_: Scottish, year) if year > 2020 => new ATSCalculationsScottish2021(summaryData, taxRates)
      case (_: Welsh, year) if year > 2020    => new ATSCalculationsWelsh2021(summaryData, taxRates)
      case (_: Scottish, year) if year > 2018 => new ATSCalculationsScottish2019(summaryData, taxRates)
      case (_: Welsh, year) if year > 2019    => new ATSCalculationsWelsh2020(summaryData, taxRates)
      case (_: UK, year) if year > 2018       => new ATSCalculationsUK2019(summaryData, taxRates)
      case _                                  => new DefaultATSCalculations(summaryData, taxRates)
    }
}
