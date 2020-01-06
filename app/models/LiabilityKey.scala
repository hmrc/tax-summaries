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

package models

import play.api.libs.json._

sealed trait LiabilityKey extends ApiValue

object LiabilityKey extends DefaultReads {

  case object AdditionalRate extends ApiValue("additional_rate") with LiabilityKey
  case object AdditionalRateAmount extends ApiValue("additional_rate_amount") with LiabilityKey
  case object AdditionalRateIncomeTax extends ApiValue("additional_rate_income_tax") with LiabilityKey
  case object AdditionalRateIncomeTaxAmount extends ApiValue("additional_rate_income_tax_amount") with LiabilityKey
  case object Adjustments extends ApiValue("adjustments") with LiabilityKey
  case object AmountAtEntrepreneursRate extends ApiValue("amount_at_entrepreneurs_rate") with LiabilityKey
  case object AmountAtHigherRate extends ApiValue("amount_at_higher_rate") with LiabilityKey
  case object AmountAtOrdinaryRate extends ApiValue("amount_at_ordinary_rate") with LiabilityKey
  case object AmountAtRPCIHigheRate extends ApiValue("amount_at_rpci_higher_rate") with LiabilityKey
  case object AmountAtRPCILowerRate extends ApiValue("amount_at_rpci_lower_rate") with LiabilityKey
  case object AmountDueAtEntrepreneursRate extends ApiValue("amount_due_at_entrepreneurs_rate") with LiabilityKey
  case object AmountDueAtHigherRate extends ApiValue("amount_due_at_higher_rate") with LiabilityKey
  case object AmountDueAtOrdinaryRate extends ApiValue("amount_due_at_ordinary_rate") with LiabilityKey
  case object AmountDueRPCIHigherRate extends ApiValue("amount_due_rpci_higher_rate") with LiabilityKey
  case object AmountDueRPCILowerRate extends ApiValue("amount_due_rpci_lower_rate") with LiabilityKey
  case object BasicRateIncomeTax extends ApiValue("basic_rate_income_tax") with LiabilityKey
  case object BasicRateIncomeTaxAmount extends ApiValue("basic_rate_income_tax_amount") with LiabilityKey
  case object BenefitsFromEmployment extends ApiValue("benefits_from_employment") with LiabilityKey
  case object CgTaxPerCurrencyUnit extends ApiValue("cg_tax_per_currency_unit") with LiabilityKey
  case object EmployeeNicAmount extends ApiValue("employee_nic_amount") with LiabilityKey
  case object HigherRateIncomeTax extends ApiValue("higher_rate_income_tax") with LiabilityKey
  case object HigherRateIncomeTaxAmount extends ApiValue("higher_rate_income_tax_amount") with LiabilityKey
  case object IncomeFromEmployment extends ApiValue("income_from_employment") with LiabilityKey
  case object LessTaxFreeAmount extends ApiValue("less_tax_free_amount") with LiabilityKey
  case object MarriageAllowanceReceivedAmount extends ApiValue("marriage_allowance_received_amount") with LiabilityKey
  case object MarriageAllowanceTransferredAmount
      extends ApiValue("marriage_allowance_transferred_amount") with LiabilityKey
  case object NicsAndTaxPerCurrencyUnit extends ApiValue("nics_and_tax_per_currency_unit") with LiabilityKey
  case object NicsAndTaxRate extends ApiValue("nics_and_tax_rate") with LiabilityKey
  case object OrdinaryRate extends ApiValue("ordinary_rate") with LiabilityKey
  case object OrdinaryRateAmount extends ApiValue("ordinary_rate_amount") with LiabilityKey
  case object OtherAdjustmentsIncreasing extends ApiValue("other_adjustments_increasing") with LiabilityKey
  case object OtherAdjustmentsReducing extends ApiValue("other_adjustments_reducing") with LiabilityKey
  case object OtherAllowancesAmount extends ApiValue("other_allowances_amount") with LiabilityKey
  case object OtherIncome extends ApiValue("other_income") with LiabilityKey
  case object OtherPensionIncome extends ApiValue("other_pension_income") with LiabilityKey
  case object PayCgTaxOn extends ApiValue("pay_cg_tax_on") with LiabilityKey
  case object PersonalTaxFreeAmount extends ApiValue("personal_tax_free_amount") with LiabilityKey
  case object ScottishIncomeTax extends ApiValue("scottish_income_tax") with LiabilityKey
  case object SelfEmploymentIncome extends ApiValue("self_employment_income") with LiabilityKey
  case object StartingRateForSavings extends ApiValue("starting_rate_for_savings") with LiabilityKey
  case object StartingRateForSavingsAmount extends ApiValue("starting_rate_for_savings_amount") with LiabilityKey
  case object StatePension extends ApiValue("state_pension") with LiabilityKey
  case object TaxableGains extends ApiValue("taxable_gains") with LiabilityKey
  case object TaxableStateBenefits extends ApiValue("taxable_state_benefits") with LiabilityKey
  case object TotalCgTax extends ApiValue("total_cg_tax") with LiabilityKey
  case object TotalCgTaxRate extends ApiValue("total_cg_tax_rate") with LiabilityKey
  case object TotalIncomeBeforeTax extends ApiValue("total_income_before_tax") with LiabilityKey
  case object TotalIncomeTax extends ApiValue("total_income_tax") with LiabilityKey
  case object TotalIncomeTaxAndNics extends ApiValue("total_income_tax_and_nics") with LiabilityKey
  case object TotalTaxFreeAmount extends ApiValue("total_tax_free_amount") with LiabilityKey
  case object UpperRate extends ApiValue("upper_rate") with LiabilityKey
  case object UpperRateAmount extends ApiValue("upper_rate_amount") with LiabilityKey
  case object YourTotalTax extends ApiValue("your_total_tax") with LiabilityKey

  case object ScottishStarterRateTax extends ApiValue("scottish_starter_rate_tax") with LiabilityKey
  case object ScottishBasicRateTax extends ApiValue("scottish_basic_rate_tax") with LiabilityKey
  case object ScottishIntermediateRateTax extends ApiValue("scottish_intermediate_rate_tax") with LiabilityKey
  case object ScottishHigherRateTax extends ApiValue("scottish_higher_rate_tax") with LiabilityKey
  case object ScottishAdditionalRateTax extends ApiValue("scottish_additional_rate_tax") with LiabilityKey

  case object ScottishTotalTax extends ApiValue("scottish_total_tax") with LiabilityKey

  case object ScottishStarterIncome extends ApiValue("scottish_starter_income") with LiabilityKey
  case object ScottishBasicIncome extends ApiValue("scottish_basic_income") with LiabilityKey
  case object ScottishIntermediateIncome extends ApiValue("scottish_intermediate_income") with LiabilityKey
  case object ScottishHigherIncome extends ApiValue("scottish_higher_income") with LiabilityKey
  case object ScottishAdditionalIncome extends ApiValue("scottish_additional_income") with LiabilityKey

  case object SavingsLowerRateTax extends ApiValue("savings_lower_rate_tax") with LiabilityKey
  case object SavingsHigherRateTax extends ApiValue("savings_higher_rate_tax") with LiabilityKey
  case object SavingsAdditionalRateTax extends ApiValue("savings_additional_rate_tax") with LiabilityKey

  case object SavingsLowerIncome extends ApiValue("savings_lower_income") with LiabilityKey
  case object SavingsHigherIncome extends ApiValue("savings_higher_income") with LiabilityKey
  case object SavingsAdditionalIncome extends ApiValue("savings_additional_income") with LiabilityKey

  // format: off
  val allItems: List[LiabilityKey] =
    List(TaxableGains, LessTaxFreeAmount, PayCgTaxOn, AmountAtEntrepreneursRate, AmountDueAtEntrepreneursRate,
      AmountAtOrdinaryRate, AmountDueAtOrdinaryRate, AmountAtHigherRate, AmountDueAtHigherRate, Adjustments, TotalCgTax,
      CgTaxPerCurrencyUnit, AmountAtRPCILowerRate, AmountDueRPCILowerRate, AmountAtRPCIHigheRate, AmountDueRPCIHigherRate,
      SelfEmploymentIncome, IncomeFromEmployment, StatePension, OtherPensionIncome, TaxableStateBenefits, OtherIncome,
      BenefitsFromEmployment, TotalIncomeBeforeTax, PersonalTaxFreeAmount, MarriageAllowanceTransferredAmount,
      OtherAllowancesAmount, TotalTaxFreeAmount, EmployeeNicAmount, TotalIncomeTaxAndNics, YourTotalTax, TotalIncomeTax,
      NicsAndTaxPerCurrencyUnit, TotalCgTaxRate, NicsAndTaxRate, StartingRateForSavings, StartingRateForSavingsAmount,
      BasicRateIncomeTax, BasicRateIncomeTaxAmount, HigherRateIncomeTax, HigherRateIncomeTaxAmount, AdditionalRateIncomeTax,
      AdditionalRateIncomeTaxAmount, OrdinaryRate, OrdinaryRateAmount, UpperRate, UpperRateAmount, AdditionalRate,
      AdditionalRateAmount, OtherAdjustmentsIncreasing, MarriageAllowanceReceivedAmount, OtherAdjustmentsReducing,
      ScottishIncomeTax, ScottishStarterRateTax, ScottishBasicRateTax, ScottishIntermediateRateTax, ScottishHigherRateTax,
      ScottishAdditionalRateTax, ScottishTotalTax, ScottishStarterIncome, ScottishBasicIncome, ScottishIntermediateIncome,
      ScottishHigherIncome, ScottishAdditionalIncome, SavingsLowerRateTax, SavingsHigherRateTax, SavingsAdditionalRateTax,
      SavingsLowerIncome, SavingsHigherIncome, SavingsAdditionalIncome)
  // format: on

  implicit def mapFormat[V: Format]: Format[Map[LiabilityKey, V]] =
    ApiValue.formatMap[LiabilityKey, V](allItems)

  implicit val formats: Format[LiabilityKey] = Format(
    ApiValue.readFromList(allItems),
    Writes[LiabilityKey](o => JsString(o.apiValue))
  )
}
