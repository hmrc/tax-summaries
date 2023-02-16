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

package models

import play.api.libs.json._

sealed class LiabilityKey(apiValue: String) extends ApiValue(apiValue)

object LiabilityKey extends DefaultReads {

  case object AdditionalRate extends LiabilityKey("additional_rate")

  case object AdditionalRateAmount extends LiabilityKey("additional_rate_amount")

  case object AdditionalRateIncomeTax extends LiabilityKey("additional_rate_income_tax")

  case object AdditionalRateIncomeTaxAmount extends LiabilityKey("additional_rate_income_tax_amount")

  case object Adjustments extends LiabilityKey("adjustments")

  case object AmountAtEntrepreneursRate extends LiabilityKey("amount_at_entrepreneurs_rate")

  case object AmountAtHigherRate extends LiabilityKey("amount_at_higher_rate")

  case object AmountAtOrdinaryRate extends LiabilityKey("amount_at_ordinary_rate")

  case object AmountAtRPCIHigheRate extends LiabilityKey("amount_at_rpci_higher_rate")

  case object AmountAtRPCILowerRate extends LiabilityKey("amount_at_rpci_lower_rate")

  case object AmountDueAtEntrepreneursRate extends LiabilityKey("amount_due_at_entrepreneurs_rate")

  case object AmountDueAtHigherRate extends LiabilityKey("amount_due_at_higher_rate")

  case object AmountDueAtOrdinaryRate extends LiabilityKey("amount_due_at_ordinary_rate")

  case object AmountDueRPCIHigherRate extends LiabilityKey("amount_due_rpci_higher_rate")

  case object AmountDueRPCILowerRate extends LiabilityKey("amount_due_rpci_lower_rate")

  case object BasicRateIncomeTax extends LiabilityKey("basic_rate_income_tax")

  case object BasicRateIncomeTaxAmount extends LiabilityKey("basic_rate_income_tax_amount")

  case object BenefitsFromEmployment extends LiabilityKey("benefits_from_employment")

  case object CgTaxPerCurrencyUnit extends LiabilityKey("cg_tax_per_currency_unit")

  case object EmployeeNicAmount extends LiabilityKey("employee_nic_amount")

  case object HigherRateIncomeTax extends LiabilityKey("higher_rate_income_tax")

  case object HigherRateIncomeTaxAmount extends LiabilityKey("higher_rate_income_tax_amount")

  case object IncomeFromEmployment extends LiabilityKey("income_from_employment")

  case object LessTaxFreeAmount extends LiabilityKey("less_tax_free_amount")

  case object MarriageAllowanceReceivedAmount extends LiabilityKey("marriage_allowance_received_amount")

  case object MarriageAllowanceTransferredAmount extends LiabilityKey("marriage_allowance_transferred_amount")

  case object NicsAndTaxPerCurrencyUnit extends LiabilityKey("nics_and_tax_per_currency_unit")

  case object MarriedCouplesAllowance extends LiabilityKey("married_couples_allowance_adjustment")

  case object LessTaxAdjustmentPrevYear extends LiabilityKey("less_tax_adjustment_previous_year")

  case object TaxUnderpaidPrevYear extends LiabilityKey("tax_underpaid_previous_year")

  case object IncomeAfterTaxAndNics extends LiabilityKey("income_after_tax_and_nics")

  case object EmployerNicAmount extends LiabilityKey("employer_nic_amount")

  case object LiableTaxAmount extends LiabilityKey("liable_tax_amount")

  case object NicsAndTaxRate extends LiabilityKey("nics_and_tax_rate")

  case object OrdinaryRate extends LiabilityKey("ordinary_rate")

  case object OrdinaryRateAmount extends LiabilityKey("ordinary_rate_amount")

  case object OtherAdjustmentsIncreasing extends LiabilityKey("other_adjustments_increasing")

  case object OtherAdjustmentsReducing extends LiabilityKey("other_adjustments_reducing")

  case object OtherAllowancesAmount extends LiabilityKey("other_allowances_amount")

  case object OtherIncome extends LiabilityKey("other_income")

  case object OtherPensionIncome extends LiabilityKey("other_pension_income")

  case object PayCgTaxOn extends LiabilityKey("pay_cg_tax_on")

  case object PersonalTaxFreeAmount extends LiabilityKey("personal_tax_free_amount")

  case object ScottishIncomeTax extends LiabilityKey("scottish_income_tax")

  case object SelfEmploymentIncome extends LiabilityKey("self_employment_income")

  case object StartingRateForSavings extends LiabilityKey("starting_rate_for_savings")

  case object StartingRateForSavingsAmount extends LiabilityKey("starting_rate_for_savings_amount")

  case object StatePension extends LiabilityKey("state_pension")

  case object TaxableGains extends LiabilityKey("taxable_gains")

  case object TaxableStateBenefits extends LiabilityKey("taxable_state_benefits")

  case object TotalCgTax extends LiabilityKey("total_cg_tax")

  case object TotalCgTaxRate extends LiabilityKey("total_cg_tax_rate")

  case object TotalIncomeBeforeTax extends LiabilityKey("total_income_before_tax")

  case object TotalIncomeTax extends LiabilityKey("total_income_tax")

  case object TotalIncomeTax2Nics extends LiabilityKey("total_income_tax_2_nics")

  case object TotalUKIncomeTax extends LiabilityKey("total_UK_income_tax")

  case object TotalIncomeTax2 extends LiabilityKey("total_income_tax_2")

  case object TotalIncomeTaxAndNics extends LiabilityKey("total_income_tax_and_nics")

  case object TotalTaxFreeAmount extends LiabilityKey("total_tax_free_amount")

  case object UpperRate extends LiabilityKey("upper_rate")

  case object UpperRateAmount extends LiabilityKey("upper_rate_amount")

  case object YourTotalTax extends LiabilityKey("your_total_tax")

  case object WelshIncomeTax extends LiabilityKey("welsh_income_tax")

  case object ScottishStarterRateTax extends LiabilityKey("scottish_starter_rate_tax")

  case object ScottishBasicRateTax extends LiabilityKey("scottish_basic_rate_tax")

  case object ScottishIntermediateRateTax extends LiabilityKey("scottish_intermediate_rate_tax")

  case object ScottishHigherRateTax extends LiabilityKey("scottish_higher_rate_tax")

  case object ScottishAdditionalRateTax extends LiabilityKey("scottish_additional_rate_tax")

  case object ScottishTotalTax extends LiabilityKey("scottish_total_tax")

  case object ScottishStarterRateIncomeTaxAmount extends LiabilityKey("scottish_starter_rate_amount")

  case object ScottishStarterRateIncomeTax extends LiabilityKey("scottish_starter_rate")

  case object ScottishBasicRateIncomeTaxAmount extends LiabilityKey("scottish_basic_rate_amount")

  case object ScottishBasicRateIncomeTax extends LiabilityKey("scottish_basic_rate")

  case object ScottishIntermediateRateIncomeTaxAmount extends LiabilityKey("scottish_intermediate_rate_amount")

  case object ScottishIntermediateRateIncomeTax extends LiabilityKey("scottish_intermediate_rate")

  case object ScottishHigherRateIncomeTaxAmount extends LiabilityKey("scottish_higher_rate_amount")

  case object ScottishHigherRateIncomeTax extends LiabilityKey("scottish_higher_rate")

  case object ScottishStarterIncome extends LiabilityKey("scottish_starter_income")

  case object ScottishBasicIncome extends LiabilityKey("scottish_basic_income")

  case object ScottishIntermediateIncome extends LiabilityKey("scottish_intermediate_income")

  case object ScottishHigherIncome extends LiabilityKey("scottish_higher_income")

  case object ScottishAdditionalIncome extends LiabilityKey("scottish_additional_income")

  case object SavingsLowerRateTax extends LiabilityKey("savings_lower_rate_tax")

  case object SavingsHigherRateTax extends LiabilityKey("savings_higher_rate_tax")

  case object SavingsAdditionalRateTax extends LiabilityKey("savings_additional_rate_tax")

  case object SavingsLowerIncome extends LiabilityKey("savings_lower_income")

  case object SavingsHigherIncome extends LiabilityKey("savings_higher_income")

  case object SavingsAdditionalIncome extends LiabilityKey("savings_additional_income")

  // format: off
  val allItems: List[LiabilityKey] = List(
    AdditionalRate, AdditionalRateAmount, AdditionalRateIncomeTax, AdditionalRateIncomeTaxAmount, Adjustments,
    AmountAtEntrepreneursRate, AmountAtHigherRate, AmountAtOrdinaryRate, AmountAtRPCIHigheRate, AmountAtRPCILowerRate,
    AmountDueAtEntrepreneursRate, AmountDueAtHigherRate, AmountDueAtOrdinaryRate, AmountDueRPCIHigherRate,
    AmountDueRPCILowerRate, BasicRateIncomeTax, BasicRateIncomeTaxAmount, BenefitsFromEmployment, CgTaxPerCurrencyUnit,
    EmployeeNicAmount, HigherRateIncomeTax, HigherRateIncomeTaxAmount, IncomeFromEmployment, LessTaxFreeAmount,
    MarriageAllowanceReceivedAmount, MarriageAllowanceTransferredAmount, NicsAndTaxPerCurrencyUnit, MarriedCouplesAllowance,
    LessTaxAdjustmentPrevYear, TaxUnderpaidPrevYear, IncomeAfterTaxAndNics, EmployerNicAmount, LiableTaxAmount, NicsAndTaxRate,
    OrdinaryRate, OrdinaryRateAmount, OtherAdjustmentsIncreasing, OtherAdjustmentsReducing, OtherAllowancesAmount, OtherIncome,
    OtherPensionIncome, PayCgTaxOn, PersonalTaxFreeAmount, ScottishIncomeTax, SelfEmploymentIncome, StartingRateForSavings,
    StartingRateForSavingsAmount, StatePension, TaxableGains, TaxableStateBenefits, TotalCgTax, TotalCgTaxRate,
    TotalIncomeBeforeTax, TotalIncomeTax, TotalIncomeTax2Nics, TotalUKIncomeTax, TotalIncomeTax2, TotalIncomeTaxAndNics,
    TotalTaxFreeAmount, UpperRate, UpperRateAmount, YourTotalTax, ScottishStarterRateTax, ScottishBasicRateTax,
    ScottishIntermediateRateTax, ScottishHigherRateTax, ScottishAdditionalRateTax, ScottishTotalTax,
    ScottishStarterRateIncomeTaxAmount, ScottishStarterRateIncomeTax, ScottishBasicRateIncomeTaxAmount, ScottishBasicRateIncomeTax,
    ScottishIntermediateRateIncomeTaxAmount, ScottishIntermediateRateIncomeTax, ScottishHigherRateIncomeTaxAmount,
    ScottishHigherRateIncomeTax, ScottishStarterIncome, ScottishBasicIncome, ScottishIntermediateIncome,
    ScottishHigherIncome, ScottishAdditionalIncome, SavingsLowerRateTax, SavingsHigherRateTax, SavingsAdditionalRateTax,
    SavingsLowerIncome, SavingsHigherIncome, SavingsAdditionalIncome, WelshIncomeTax
  )
    // format: on

  implicit def mapFormat[V: Format]: Format[Map[LiabilityKey, V]] =
    ApiValue.formatMap[LiabilityKey, V](allItems)

  implicit val formats: Format[LiabilityKey] = Format(
    ApiValue.readFromList(allItems),
    Writes[LiabilityKey](o => JsString(o.apiValue))
  )
}
