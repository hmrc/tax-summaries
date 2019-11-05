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

package models

import play.api.libs.json._


sealed trait LiabilityTransformer {
  val apiValue: String
}

object LiabilityTransformer extends DefaultReads with DefaultWrites with DefaultFormat {

  case object AdditionalRate extends ApiValue("additional_rate") with LiabilityTransformer
  case object AdditionalRateAmount extends ApiValue("additional_rate_amount") with LiabilityTransformer
  case object AdditionalRateIncomeTax extends ApiValue("additional_rate_income_tax") with LiabilityTransformer
  case object AdditionalRateIncomeTaxAmount extends ApiValue("additional_rate_income_tax_amount") with LiabilityTransformer
  case object Adjustments extends ApiValue("adjustments") with LiabilityTransformer
  case object AmountAtEntrepreneursRate extends ApiValue("amount_at_entrepreneurs_rate") with LiabilityTransformer
  case object AmountAtHigherRate extends ApiValue("amount_at_higher_rate") with LiabilityTransformer
  case object AmountAtOrdinaryRate extends ApiValue("amount_at_ordinary_rate") with LiabilityTransformer
  case object AmountAtRPCIHigheRate extends ApiValue("amount_at_rpci_higher_rate") with LiabilityTransformer
  case object AmountAtRPCILowerRate extends ApiValue("amount_at_rpci_lower_rate") with LiabilityTransformer
  case object AmountDueAtEntrepreneursRate extends ApiValue("amount_due_at_entrepreneurs_rate") with LiabilityTransformer
  case object AmountDueAtHigherRate extends ApiValue("amount_due_at_higher_rate") with LiabilityTransformer
  case object AmountDueAtOrdinaryRate extends ApiValue("amount_due_at_ordinary_rate") with LiabilityTransformer
  case object AmountDueRPCIHigherRate extends ApiValue("amount_due_rpci_higher_rate") with LiabilityTransformer
  case object AmountDueRPCILowerRate extends ApiValue("amount_due_rpci_lower_rate") with LiabilityTransformer
  case object BasicRateIncomeTax extends ApiValue("basic_rate_income_tax") with LiabilityTransformer
  case object BasicRateIncomeTaxAmount extends ApiValue("basic_rate_income_tax_amount") with LiabilityTransformer
  case object BenefitsFromEmployment extends ApiValue("benefits_from_employment") with LiabilityTransformer
  case object CgTaxPerCurrencyUnit extends ApiValue("cg_tax_per_currency_unit") with LiabilityTransformer
  case object EmployeeNicAmount extends ApiValue("employee_nic_amount") with LiabilityTransformer
  case object HigherRateIncomeTax extends ApiValue("higher_rate_income_tax") with LiabilityTransformer
  case object HigherRateIncomeTaxAmount extends ApiValue("higher_rate_income_tax_amount") with LiabilityTransformer
  case object IncomeFromEmployment extends ApiValue("income_from_employment") with LiabilityTransformer
  case object LessTaxFreeAmount extends ApiValue("less_tax_free_amount") with LiabilityTransformer
  case object MarriageAllowanceReceivedAmount extends ApiValue("marriage_allowance_received_amount") with LiabilityTransformer
  case object MarriageAllowanceTransferredAmount extends ApiValue("marriage_allowance_transferred_amount") with LiabilityTransformer
  case object NicsAndTaxPerCurrencyUnit extends ApiValue("nics_and_tax_per_currency_unit") with LiabilityTransformer
  case object NicsAndTaxRate extends ApiValue("nics_and_tax_rate") with LiabilityTransformer
  case object OrdinaryRate extends ApiValue("ordinary_rate") with LiabilityTransformer
  case object OrdinaryRateAmount extends ApiValue("ordinary_rate_amount") with LiabilityTransformer
  case object OtherAdjustmentsIncreasing extends ApiValue("other_adjustments_increasing") with LiabilityTransformer
  case object OtherAdjustmentsReducing extends ApiValue("other_adjustments_reducing") with LiabilityTransformer
  case object OtherAllowancesAmount extends ApiValue("other_allowances_amount") with LiabilityTransformer
  case object OtherIncome extends ApiValue("other_income") with LiabilityTransformer
  case object OtherPensionIncome extends ApiValue("other_pension_income") with LiabilityTransformer
  case object PayCgTaxOn extends ApiValue("pay_cg_tax_on") with LiabilityTransformer
  case object PersonalTaxFreeAmount extends ApiValue("personal_tax_free_amount") with LiabilityTransformer
  case object ScottishIncomeTax extends ApiValue("scottish_income_tax") with LiabilityTransformer
  case object SelfEmploymentIncome extends ApiValue("self_employment_income") with LiabilityTransformer
  case object StartingRateForSavings extends ApiValue("starting_rate_for_savings") with LiabilityTransformer
  case object StartingRateForSavingsAmount extends ApiValue("starting_rate_for_savings_amount") with LiabilityTransformer
  case object StatePension extends ApiValue("state_pension") with LiabilityTransformer
  case object TaxableGains extends ApiValue("taxable_gains") with LiabilityTransformer
  case object TaxableStateBenefits extends ApiValue("taxable_state_benefits") with LiabilityTransformer
  case object TotalCgTax extends ApiValue("total_cg_tax") with LiabilityTransformer
  case object TotalCgTaxRate extends ApiValue("total_cg_tax_rate") with LiabilityTransformer
  case object TotalIncomeBeforeTax extends ApiValue("total_income_before_tax") with LiabilityTransformer
  case object TotalIncomeTax extends ApiValue("total_income_tax") with LiabilityTransformer
  case object TotalIncomeTaxAndNics extends ApiValue("total_income_tax_and_nics") with LiabilityTransformer
  case object TotalTaxFreeAmount extends ApiValue("total_tax_free_amount") with LiabilityTransformer
  case object UpperRate extends ApiValue("upper_rate") with LiabilityTransformer
  case object UpperRateAmount extends ApiValue("upper_rate_amount") with LiabilityTransformer
  case object YourTotalTax extends ApiValue("your_total_tax") with LiabilityTransformer

  val allItems: List[LiabilityTransformer] =
    List(TaxableGains, LessTaxFreeAmount, PayCgTaxOn, AmountAtEntrepreneursRate, AmountDueAtEntrepreneursRate, AmountAtOrdinaryRate, AmountDueAtOrdinaryRate,
      AmountAtHigherRate, AmountDueAtHigherRate, Adjustments, TotalCgTax, CgTaxPerCurrencyUnit, AmountAtRPCILowerRate, AmountDueRPCILowerRate, AmountAtRPCIHigheRate,
      AmountDueRPCIHigherRate, SelfEmploymentIncome, IncomeFromEmployment, StatePension, OtherPensionIncome, TaxableStateBenefits, OtherIncome, BenefitsFromEmployment,
      TotalIncomeBeforeTax, PersonalTaxFreeAmount, MarriageAllowanceTransferredAmount, OtherAllowancesAmount, TotalTaxFreeAmount, EmployeeNicAmount, TotalIncomeTaxAndNics,
      YourTotalTax, TotalIncomeTax, NicsAndTaxPerCurrencyUnit, TotalCgTaxRate, NicsAndTaxRate, StartingRateForSavings, StartingRateForSavingsAmount, BasicRateIncomeTax,
      BasicRateIncomeTaxAmount, HigherRateIncomeTax, HigherRateIncomeTaxAmount, AdditionalRateIncomeTax, AdditionalRateIncomeTaxAmount, OrdinaryRate, OrdinaryRateAmount,
      UpperRate, UpperRateAmount, AdditionalRate, AdditionalRateAmount, OtherAdjustmentsIncreasing, MarriageAllowanceReceivedAmount, OtherAdjustmentsReducing,
      ScottishIncomeTax)

  implicit def format[V: Format]: Format[Map[LiabilityTransformer, V]] =
    Format(
      mapReads(s => allItems.find(_.apiValue == s).fold[JsResult[LiabilityTransformer]](JsError(""))(JsSuccess(_))),
      new Writes[Map[LiabilityTransformer, V]] {
        override def writes(o: Map[LiabilityTransformer, V]): JsValue =
          JsObject(o.map { case (k, v) =>
            k.apiValue -> Json.toJson(v)
          })
      }
    )

  implicit val formats: Format[LiabilityTransformer] = new Format[LiabilityTransformer] {
    override def reads(json: JsValue): JsResult[LiabilityTransformer] = json match {
      case JsString(value) =>
        allItems.find(_.apiValue == value).fold[JsResult[LiabilityTransformer]](JsError("Unable to parse unknown api value"))(JsSuccess(_))
      case _ =>
        JsError(s"Unable to parse Liability Transformer value: expected String, got: $json")
    }


    override def writes(o: LiabilityTransformer): JsValue =
      JsString(o.apiValue)
  }

}
