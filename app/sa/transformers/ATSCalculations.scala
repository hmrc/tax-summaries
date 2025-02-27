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

package sa.transformers

import common.models.{Amount, Rate}
import play.api.Logging
import sa.models.ODSLiabilities.ODSLiabilities
import sa.models.ODSLiabilities.ODSLiabilities.{Alimony, AnnuityPay, BPA, BpaAllowance, CapAdjustment, CgAnnualExempt, CgDueEntrepreneursRate, CgDueHigherRate, CgDueLowerRate, CgGainsAfterLosses, CgTotGainsAfterLosses, ChildBenefitCharge, Class4Nic, CommInvTrustRel, DeficiencyRelief, DividendTaxAddHighRate, DividendTaxHighRate, DividendTaxLowRate, EisRelief, EmployeeClass1NI, EmployeeClass2NI, EmploymentBenefits, EmploymentExpenses, ExcludedIncome, GiftsInvCharities, HigherRateCgtRPCI, IncBenefitSuppAllow, IncomeChargeableAddHRate, IncomeChargeableBasicRate, IncomeChargeableHigherRate, IncomeTaxAddHighRate, IncomeTaxBasicRate, IncomeTaxDue, IncomeTaxHigherRate, JobSeekersAllowance, LFIRelief, LowerRateCgtRPCI, MarriageAllceIn, MarriageAllceOut, NetAnnuityPaytsTaxDue, NonDomCharge, NonPayableTaxCredits, NotionalTaxCegs, NotlTaxOtherSource, OthStatePenBenefits, OtherPension, PensionLsumTaxDue, PensionSavingChargeable, PersonalAllowance, QualDistnRelief, ReliefForFinanceCosts, SavingsChargeableAddHRate, SavingsChargeableHigherRate, SavingsChargeableLowerRate, SavingsChargeableStartRate, SavingsTaxAddHighRate, SavingsTaxHigherRate, SavingsTaxLowerRate, SavingsTaxStartingRate, SeedEisRelief, StatePension, StatePensionGross, SumTotForeignTaxRelief, SumTotLifePolicyGains, SumTotLoanRestricted, SumTotLossRestricted, SummaryTotForeignDiv, SummaryTotForeignIncome, SummaryTotShareOptions, SummaryTotTrustEstates, SummaryTotalDedPpr, SummaryTotalEmployment, SummaryTotalOtherIncome, SummaryTotalPartnership, SummaryTotalSchedule, SummaryTotalUkIntDivs, SummaryTotalUkInterest, SummaryTotalUklProperty, SurplusMcaAlimonyRel, TaxCreditsForDivs, TaxDueAfterAllceRlf, TaxExcluded, TopSlicingRelief, TotalTaxCreditRelief, TradeUnionDeathBenefits, VctSharesRelief}
import sa.models.{Nationality, Scottish, TaxSummaryLiability, UK, Welsh}
import sa.services.TaxRateService
import sa.transformers.ATS2020.{ATSCalculationsScottish2020, ATSCalculationsUK2020, ATSCalculationsWelsh2020}
import sa.transformers.ATS2021.{ATSCalculationsScottish2021, ATSCalculationsUK2021, ATSCalculationsWelsh2021}
import sa.transformers.ATS2022.{ATSCalculationsScottish2022, ATSCalculationsUK2022, ATSCalculationsWelsh2022}
import sa.transformers.ATS2023.{ATSCalculationsScottish2023, ATSCalculationsUK2023, ATSCalculationsWelsh2023}
import sa.transformers.ATS2024.{ATSCalculationsScottish2024, ATSCalculationsUK2024, ATSCalculationsWelsh2024}
import sa.utils.DoubleUtils

// scalastyle:off number.of.methods
trait ATSCalculations extends DoubleUtils with Logging {
  protected val summaryData: TaxSummaryLiability
  protected val taxRates: TaxRateService
  lazy val incomeTaxStatus: Option[Nationality] = summaryData.incomeTaxStatus

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

  def scottishTopRateTax: Amount = Amount.empty("scottishTopRateTax")

  def scottishTotalTax: Amount =
    scottishStarterRateTax + scottishBasicRateTax + scottishIntermediateRateTax + scottishHigherRateTax + scottishAdditionalRateTax

  def scottishStarterRateIncome: Amount = Amount.empty("scottishStarterRateIncome")

  def scottishBasicRateIncome: Amount = Amount.empty("scottishBasicRateIncome")

  def scottishIntermediateRateIncome: Amount = Amount.empty("scottishIntermediateRateIncome")

  def scottishHigherRateIncome: Amount = Amount.empty("scottishHigherRateIncome")

  def scottishAdditionalRateIncome: Amount = Amount.empty("scottishAdditionalRateIncome")

  def scottishTopRateIncome: Amount = Amount.empty("scottishTopRateIncome")

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
      get(NotlTaxOtherSource) + // 15.16
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
      get(DividendTaxHighRate) + // LS13.2
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

  private def taxPerTaxableCurrencyUnit(tax: Amount, taxable: Amount): Amount =
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

  private def liabilityAsPercentage(amountPerUnit: Amount): Rate =
    Rate.rateFromPerUnitAmount(amountPerUnit)
}

object ATSCalculations {
  private val calculationsForNationalityAndYear
    : Map[(Nationality, Int), (TaxSummaryLiability, TaxRateService) => ATSCalculations] = {
    val uk       = UK()
    val scotland = Scottish()
    val wales    = Welsh()

    val calc2024UK       = new ATSCalculationsUK2024(_, _)
    val calc2024Scotland = new ATSCalculationsScottish2024(_, _)
    val calc2024Wales    = new ATSCalculationsWelsh2024(_, _)

    val calc2023UK       = new ATSCalculationsUK2023(_, _)
    val calc2023Scotland = new ATSCalculationsScottish2023(_, _)
    val calc2023Wales    = new ATSCalculationsWelsh2023(_, _)
    val calc2022UK       = new ATSCalculationsUK2022(_, _)
    val calc2022Scotland = new ATSCalculationsScottish2022(_, _)
    val calc2022Wales    = new ATSCalculationsWelsh2022(_, _)
    val calc2021UK       = new ATSCalculationsUK2021(_, _)
    val calc2021Scotland = new ATSCalculationsScottish2021(_, _)
    val calc2021Wales    = new ATSCalculationsWelsh2021(_, _)
    val calc2020Wales    = new ATSCalculationsWelsh2020(_, _)
    val calc2020UK       = new ATSCalculationsUK2020(_, _)
    val calc2020Scotland = new ATSCalculationsScottish2020(_, _)

    Map(
      (uk, 2024)       -> calc2024UK,
      (scotland, 2024) -> calc2024Scotland,
      (wales, 2024)    -> calc2024Wales,
      (uk, 2023)       -> calc2023UK,
      (scotland, 2023) -> calc2023Scotland,
      (wales, 2023)    -> calc2023Wales,
      (uk, 2022)       -> calc2022UK,
      (scotland, 2022) -> calc2022Scotland,
      (wales, 2022)    -> calc2022Wales,
      (uk, 2021)       -> calc2021UK,
      (scotland, 2021) -> calc2021Scotland,
      (wales, 2021)    -> calc2021Wales,
      (wales, 2020)    -> calc2020Wales,
      (uk, 2020)       -> calc2020UK,
      (scotland, 2020) -> calc2020Scotland
    )
  }

  def make(summaryData: TaxSummaryLiability, taxRates: TaxRateService): Option[ATSCalculations] =
    calculationsForNationalityAndYear.get((summaryData.nationality, summaryData.taxYear)) match {
      case Some(found) => Some(found(summaryData, taxRates))
      case None        =>
        val maxDefinedYearForCountry = calculationsForNationalityAndYear.keys
          .filter(_._1 == summaryData.nationality)
          .map(_._2)
          .max
        if (summaryData.taxYear > maxDefinedYearForCountry) {
          Some(
            calculationsForNationalityAndYear((summaryData.nationality, maxDefinedYearForCountry))(
              summaryData,
              taxRates
            )
          )
        } else {
          None
        }
    }
}
