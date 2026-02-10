/*
 * Copyright 2026 HM Revenue & Customs
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

package sa.calculations

import common.models.{Amount, Rate}
import play.api.Logging
import sa.models.*
import sa.models.ODSLiabilities.ODSLiabilities
import sa.models.ODSLiabilities.ODSLiabilities.*
import sa.models.TaxRate.{AdditionalRateIncomeTaxRate, HigherRateIncomeTaxRate}
import sa.transformers.ATSParsingException
import sa.utils.DoubleUtils

// scalastyle:off number.of.methods
trait ATSCalculations extends DoubleUtils with Logging {
  protected val selfAssessmentAPIResponse: SelfAssessmentAPIResponse
  val taxRates: Map[String, Rate]
  lazy val incomeTaxStatus: Option[Nationality] = selfAssessmentAPIResponse.incomeTaxStatus

  def get(liability: ODSLiabilities): Amount = {
    val result = selfAssessmentAPIResponse.atsData.getOrElse(
      liability,
      selfAssessmentAPIResponse.nationalInsuranceData.getOrElse(
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
    val result = selfAssessmentAPIResponse.atsData.getOrElse(
      liability,
      selfAssessmentAPIResponse.nationalInsuranceData.getOrElse(liability, Amount.empty(liability.apiValue))
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
    (
      getWithDefaultAmount(LowerRateCgtRPCI) +
        getWithDefaultAmount(HigherRateCgtRPCI) +
        get(CgDueEntrepreneursRate) +
        get(CgDueLowerRate) +
        get(CgDueHigherRate) +
        get(CapAdjustment)
    ).max(0)

  def selfEmployment: Amount

  def otherPension: Amount =
    get(OtherPension) +
      get(StatePensionGross)

  def taxableStateBenefits: Amount =
    get(IncBenefitSuppAllow) +
      get(JobSeekersAllowance) +
      get(OthStatePenBenefits)

  def otherIncome: Amount

  def totalIncomeBeforeTax: Amount =
    selfEmployment +
      get(SummaryTotalEmployment) +
      get(StatePension) +
      otherPension +
      taxableStateBenefits +
      otherIncome +
      get(EmploymentBenefits)

  def otherAllowances: Amount

  def totalTaxFreeAmount: Amount =
    otherAllowances +
      get(PersonalAllowance) -
      getWithDefaultAmount(MarriageAllceOut)

  def totalAmountEmployeeNic: Amount

  def basicRateIncomeTaxAmount: Amount

  def higherRateIncomeTaxAmount: Amount =
    get(IncomeTaxHigherRate) +
      get(SavingsTaxHigherRate) +
      includePensionTaxForRate(taxRates.getOrElse(HigherRateIncomeTaxRate, Rate.empty))

  def additionalRateIncomeTaxAmount: Amount =
    get(IncomeTaxAddHighRate) +
      get(SavingsTaxAddHighRate) +
      includePensionTaxForRate(taxRates.getOrElse(AdditionalRateIncomeTaxRate, Rate.empty))

  def scottishStarterRateTax: Amount = Amount.empty("scottishStarterRateTax")

  def scottishBasicRateTax: Amount = Amount.empty("scottishBasicRateTax")

  def scottishIntermediateRateTax: Amount = Amount.empty("scottishIntermediateRateTax")

  def scottishHigherRateTax: Amount = Amount.empty("scottishHigherRateTax")

  def scottishAdvancedRateTax: Amount = Amount.empty("scottishAdvancedRateTax")

  def scottishAdditionalRateTax: Amount = Amount.empty("scottishAdditionalRateTax")

  def scottishTopRateTax: Amount = Amount.empty("scottishTopRateTax")

  def scottishTotalTax: Amount =
    scottishStarterRateTax + scottishBasicRateTax + scottishIntermediateRateTax + scottishHigherRateTax + scottishAdditionalRateTax

  def scottishStarterRateIncome: Amount = Amount.empty("scottishStarterRateIncome")

  def scottishBasicRateIncome: Amount = Amount.empty("scottishBasicRateIncome")

  def scottishIntermediateRateIncome: Amount = Amount.empty("scottishIntermediateRateIncome")

  def scottishHigherRateIncome: Amount = Amount.empty("scottishHigherRateIncome")

  def scottishAdvancedRateIncome: Amount = Amount.empty("scottishAdvancedRateIncome")

  def brdCharge: Amount = Amount.empty("brdCharge")

  def brdReduction: Amount = Amount.empty("brdReduction")

  def scottishAdditionalRateIncome: Amount = Amount.empty("scottishAdditionalRateIncome")

  def scottishTopRateIncome: Amount = Amount.empty("scottishTopRateIncome")

  def savingsBasicRateTax: Amount = Amount.empty("savingsBasicRateTax")

  def savingsHigherRateTax: Amount = Amount.empty("savingsHigherRateTax")

  def savingsAdditionalRateTax: Amount = Amount.empty("savingsAdditionalRateTax")

  def savingsBasicRateIncome: Amount = Amount.empty("savingsBasicRateIncome")

  def savingsHigherRateIncome: Amount = Amount.empty("savingsHigherRateIncome")

  def savingsAdditionalRateIncome: Amount = Amount.empty("savingsAdditionalRateIncome")

  def welshIncomeTax: Amount = Amount.empty("welshIncomeTax")

  def otherAdjustmentsIncreasing: Amount

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

  def totalIncomeTaxAmount: Amount

  def totalAmountTaxAndNics: Amount =
    totalAmountEmployeeNic +
      totalIncomeTaxAmount

  def totalTax: Amount =
    totalAmountTaxAndNics +
      totalCapitalGainsTax

  def basicRateIncomeTax: Amount

  def higherRateIncomeTax: Amount

  def additionalRateIncomeTax: Amount

  def scottishIncomeTax: Amount

  def taxLiability: Amount = totalCapitalGainsTax + totalIncomeTaxAmount

  def hasLiability: Boolean =
    !taxLiability.isZeroOrLess

  def capitalGainsTaxPerCurrency: Amount =
    taxPerTaxableCurrencyUnit(totalCapitalGainsTax, taxableGains())

  def nicsAndTaxPerCurrency: Amount =
    taxPerTaxableCurrencyUnit(totalAmountTaxAndNics, totalIncomeBeforeTax)

  def savingsRate: Amount

  def savingsRateAmount: Amount = get(SavingsTaxStartingRate)

  private def taxPerTaxableCurrencyUnit(tax: Amount, taxable: Amount): Amount =
    taxable match {
      case value if value.isZero => taxable
      case _                     => tax.divideWithPrecision(taxable, 4)
    }

  def totalNicsAndTaxLiabilityAsPercentage: Rate = liabilityAsPercentage(nicsAndTaxPerCurrency)

  def totalCgTaxLiabilityAsPercentage: Rate = liabilityAsPercentage(capitalGainsTaxPerCurrency)

  def includePensionTaxForRate(taxRate: Rate): Amount =
    if (selfAssessmentAPIResponse.pensionLumpSumTaxRate.percentage === taxRate.percent) get(PensionLsumTaxDue)
    else Amount.empty(PensionLsumTaxDue.apiValue)

  def includePensionIncomeForRate(taxRate: Rate): Amount =
    if (selfAssessmentAPIResponse.pensionLumpSumTaxRate.percentage === taxRate.percent) get(StatePensionGross)
    else Amount.empty(StatePensionGross.apiValue)

  private def liabilityAsPercentage(amountPerUnit: Amount): Rate =
    Rate.rateFromPerUnitAmount(amountPerUnit)

  def adjustmentsToCapitalGains: Amount = get(CapAdjustment)
}
