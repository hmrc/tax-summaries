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

import models.Amount
import play.api.libs.json._
import play.api.libs.json.JsPath

trait PAYETransformer {
  def middleTierAmountJson(name: String, value: Double, currency: String = "GBP"): JsObject = Json.obj(
    name -> Json.obj(
      "amount"   -> value,
      "currency" -> currency
    )
  )

  def middleTierRateJson(name: String, percent: Double): JsObject = Json.obj(
    name -> Json.obj(
      "percent" -> JsString(percent.toString + "%")
    )
  )

  def appendAttribute(path: JsPath, anObject: JsObject) =
    path.json.update((__).read[JsObject].map { o =>
      o ++ anObject
    })

  def pickAmount(path: JsPath, json: JsValue): Option[Double] =
    json.transform(path.json.pick[JsNumber]).asOpt.map(n => n.as[Double])
}

object PAYETransformer {
  def middleTierJson(nino: String, year: Int): JsObject =
    Json.obj( // add nino
      "taxYear" -> JsNumber(year),
      "nino"    -> JsString(nino),
      "lnks" -> Json.arr(
        Json.obj(
          "rel"  -> "self",
          "href" -> s"https://digital.ws.ibt.hmrc.gov.uk/individuals/annual-tax-summary/$nino/$year"
        )),
      "allowance_data"     -> Json.obj("payload" -> Json.obj()),
      "capital_gains_data" -> Json.obj("payload" -> Json.obj()),
      "income_data"        -> Json.obj("payload" -> Json.obj()),
      "income_tax"         -> Json.obj("payload" -> Json.obj(), "rates" -> Json.obj()),
      "summary_data"       -> Json.obj("payload" -> Json.obj()),
      "gov_spending"       -> Json.obj()
    )

  implicit class RichJsObject(original: JsObject) extends PAYETransformer {
    def omitEmpty: JsObject = original.value.foldLeft(original) {
      case (obj, (key, JsString(st))) if st.isEmpty                                                   => obj - key
      case (obj, (key, JsArray(arr))) if arr.isEmpty                                                  => obj - key
      case (obj, (key, JsObject(child))) if new RichJsObject(JsObject(child)).omitEmpty.value.isEmpty => obj - key
      case (obj, (_, _))                                                                              => obj
    }

    def safeTransform(jsonTransformer: Reads[JsObject]): JsObject =
      original.transform(jsonTransformer).asOpt match {
        case Some(json) => json
        case None       => original
      }

    def transformPaye(source: JsObject): JsObject =
      transformTotalIncome(source)
        .transformSummary(source)
        .transformAllowances(source)
        .transformIncomeTax(source)
        .transformGovSpendingData(source)
        .omitEmpty

    def transformTotalIncome(source: JsObject): JsObject = {
      val income: Option[Double] = pickAmount(__ \ 'income \ 'incomeFromEmployment, source)
      val statePension: Option[Double] = pickAmount(__ \ 'income \ 'statePension, source)
      val otherPensionIncome: Option[Double] = pickAmount(__ \ 'income \ 'otherPensionIncome, source)
      val otherIncome: Option[Double] = pickAmount(__ \ 'income \ 'otherIncome, source)
      val incomeBeforeTax: Option[Double] = pickAmount(__ \ 'income \ 'incomeBeforeTax, source)
      val employmentBenefits: Option[Double] = pickAmount(__ \ 'income \ 'employmentBenefits, source)
      val taxableStateBenefits: Option[Double] = pickAmount(__ \ 'taxableStateBenefits, source)

      val jsonTransformer =
        appendAttribute(
          __ \ 'income_data \ 'payload,
          middleTierAmountJson("income_from_employment", income.getOrElse(0))) andThen
          appendAttribute(
            __ \ 'income_data \ 'payload,
            middleTierAmountJson("state_pension", statePension.getOrElse(0))) andThen
          appendAttribute(
            __ \ 'income_data \ 'payload,
            middleTierAmountJson("other_pension_income", otherPensionIncome.getOrElse(0))) andThen
          appendAttribute(__ \ 'income_data \ 'payload, middleTierAmountJson("other_income", otherIncome.getOrElse(0))) andThen
          appendAttribute(
            __ \ 'income_data \ 'payload,
            middleTierAmountJson("total_income_before_tax", incomeBeforeTax.getOrElse(0))) andThen
          appendAttribute(
            __ \ 'income_data \ 'payload,
            middleTierAmountJson("benefits_from_employment", employmentBenefits.getOrElse(0))) andThen
          appendAttribute(
            __ \ 'income_data \ 'payload,
            middleTierAmountJson("taxable_state_benefits", taxableStateBenefits.getOrElse(0)))

      safeTransform(jsonTransformer)
    }
    def transformSummary(source: JsObject): JsObject = {
      val taxableIncome: Option[Double] = pickAmount(__ \ 'income \ 'incomeBeforeTax, source)
      val taxFreeAmount: Option[Double] = pickAmount(__ \ 'income \ 'taxableIncome, source)
      val incomeTaxAndNics: Option[Double] = pickAmount(__ \ 'calculatedTotals \ 'totalIncomeTaxNics, source)
      val IncomeAfterTaxAndNics: Option[Double] = pickAmount(__ \ 'calculatedTotals \ 'incomeAfterTaxNics, source)
      val incomeTax: Option[Double] = pickAmount(__ \ 'calculatedTotals \ 'totalIncomeTax, source)
      val nationalInsurance: Option[Double] = pickAmount(__ \ 'nationalInsurance \ 'employeeContributions, source)
      val nationalInsuranceEmployer: Option[Double] =
        pickAmount(__ \ 'nationalInsurance \ 'employerContributions, source)
      val averageIncomeTaxRate: Option[Double] = pickAmount(__ \ 'averageRateTax, source)

      val jsonTransformer =
        appendAttribute(
          __ \ 'summary_data \ 'payload,
          middleTierAmountJson("total_income_before_tax", taxableIncome.getOrElse(0))) andThen
          appendAttribute(
            __ \ 'summary_data \ 'payload,
            middleTierAmountJson("total_tax_free_amount", taxFreeAmount.getOrElse(0))) andThen
          appendAttribute(
            __ \ 'summary_data \ 'payload,
            middleTierAmountJson("total_income_tax_and_nics", incomeTaxAndNics.getOrElse(0))) andThen
          appendAttribute(
            __ \ 'summary_data \ 'payload,
            middleTierAmountJson("income_after_tax_and_nics", IncomeAfterTaxAndNics.getOrElse(0))) andThen
          appendAttribute(
            __ \ 'summary_data \ 'payload,
            middleTierAmountJson("total_income_tax", incomeTax.getOrElse(0))) andThen
          appendAttribute(
            __ \ 'summary_data \ 'payload,
            middleTierAmountJson("employee_nic_amount", nationalInsurance.getOrElse(0))) andThen
          appendAttribute(
            __ \ 'summary_data \ 'payload,
            middleTierAmountJson("employer_nic_amount", nationalInsuranceEmployer.getOrElse(0))) andThen
          appendAttribute(
            __ \ 'summary_data \ 'payload,
            middleTierAmountJson("nics_and_tax_rate", averageIncomeTaxRate.getOrElse(0)))

      safeTransform(jsonTransformer)
    }

    def transformAllowances(source: JsObject): JsObject = {
      val personalAllowance: Option[Double] = pickAmount(__ \ 'adjustments \ 'taxFreeAmount, source)
      val marriageAllowanceTransferred: Option[Double] =
        pickAmount(__ \ 'adjustments \ 'marriageAllowanceTransferred, source)
      val otherAllowancees: Option[Double] = pickAmount(__ \ 'income \ 'otherAllowancesDeductionsExpenses, source)
      val youPayTaxOn: Option[Double] = pickAmount(__ \ 'calculatedTotals \ 'liableTaxAmount, source)
      val totalTaxFreeAmount: Option[Double] = pickAmount(__ \ 'income \ 'taxableIncome, source)

      val jsonTransformer =
        appendAttribute(
          __ \ 'allowance_data \ 'payload,
          middleTierAmountJson("personal_tax_free_amount", personalAllowance.getOrElse(0))) andThen
          appendAttribute(
            __ \ 'allowance_data \ 'payload,
            middleTierAmountJson("marriage_allowance_transferred_amount", marriageAllowanceTransferred.getOrElse(0))) andThen
          appendAttribute(
            __ \ 'allowance_data \ 'payload,
            middleTierAmountJson("other_allowances_amount", otherAllowancees.getOrElse(0))) andThen
          appendAttribute(
            __ \ 'allowance_data \ 'payload,
            middleTierAmountJson("you_pay_tax_on", youPayTaxOn.getOrElse(0))) andThen
          appendAttribute(
            __ \ 'allowance_data \ 'payload,
            middleTierAmountJson("total_tax_free_amount", totalTaxFreeAmount.getOrElse(0)))

      safeTransform(jsonTransformer)
    }

    def transformIncomeTax(source: JsObject): JsObject = {
      val basicRateTaxAmount: Option[Double] = pickAmount(__ \ 'basicRateBand \ 'basicRateTax, source)
      val basicRateTax: Option[Double] = pickAmount(__ \ 'basicRateBand \ 'basicRateTaxAmount, source)
      val basicRate: Option[Double] = pickAmount(__ \ 'basicRateBand \ 'basicRate, source)

      val higherRateTaxAmount: Option[Double] = pickAmount(__ \ 'higherRateBand \ 'higherRateTax, source)
      val higherRateTax: Option[Double] = pickAmount(__ \ 'higherRateBand \ 'higherRateTaxAmount, source)
      val higherRate: Option[Double] = pickAmount(__ \ 'higherRateBand \ 'higherRate, source)

      val dividendLowRateTaxAmount: Option[Double] = pickAmount(__ \ 'dividendLowerBand \ 'dividendLowRateTax, source)
      val dividendLowRateTax: Option[Double] = pickAmount(__ \ 'dividendLowerBand \ 'dividendLowRateAmount, source)
      val dividendLowRate: Option[Double] = pickAmount(__ \ 'dividendLowerBand \ 'dividendLowRate, source)

      val dividendHigherRateTaxAmount: Option[Double] =
        pickAmount(__ \ 'dividendHigherBand \ 'dividendHigherRateTax, source)
      val dividendHigherRateTax: Option[Double] =
        pickAmount(__ \ 'dividendHigherBand \ 'dividendHigherRateAmount, source)
      val dividendHigherRate: Option[Double] = pickAmount(__ \ 'dividendHigherBand \ 'dividendHigherRate, source)

      val marriedCouplesAllowanceAdjust: Option[Double] =
        pickAmount(__ \ 'adjustments \ 'marriedCouplesAllowanceAdjustment, source)
      val marriedCouplesAllowanceReceived: Option[Double] =
        pickAmount(__ \ 'adjustments \ 'marriageAllowanceReceived, source)
      val lessTaxAdjustPreviousYear: Option[Double] =
        pickAmount(__ \ 'adjustments \ 'lessTaxAdjustmentPreviousYear, source)
      val taxUnderpaidPreviousYear: Option[Double] = pickAmount(__ \ 'adjustments \ 'taxUnderpaidPreviousYear, source)

      val incomeTax: Option[Double] = pickAmount(__ \ 'calculatedTotals \ 'totalIncomeTax, source)

      val jsonTransformer =
        appendAttribute(
          __ \ 'income_tax \ 'payload,
          middleTierAmountJson("basic_rate_income_tax_amount", basicRateTaxAmount.getOrElse(0))) andThen
          appendAttribute(
            __ \ 'income_tax \ 'payload,
            middleTierAmountJson("basic_rate_income_tax", basicRateTax.getOrElse(0))) andThen
          appendAttribute(
            __ \ 'income_tax \ 'rates,
            middleTierRateJson("basic_rate_income_tax_rate", basicRate.getOrElse(0))) andThen
          appendAttribute(
            __ \ 'income_tax \ 'payload,
            middleTierAmountJson("higher_rate_income_tax_amount", higherRateTaxAmount.getOrElse(0))) andThen
          appendAttribute(
            __ \ 'income_tax \ 'payload,
            middleTierAmountJson("higher_rate_income_tax", higherRateTax.getOrElse(0))) andThen
          appendAttribute(
            __ \ 'income_tax \ 'rates,
            middleTierRateJson("higher_rate_income_tax_rate", higherRate.getOrElse(0))) andThen
          appendAttribute(
            __ \ 'income_tax \ 'payload,
            middleTierAmountJson("ordinary_rate_amount", dividendLowRateTaxAmount.getOrElse(0))) andThen
          appendAttribute(
            __ \ 'income_tax \ 'payload,
            middleTierAmountJson("ordinary_rate", dividendLowRateTax.getOrElse(0))) andThen
          appendAttribute(
            __ \ 'income_tax \ 'rates,
            middleTierRateJson("ordinary_rate_tax_rate", dividendLowRate.getOrElse(0))) andThen
          appendAttribute(
            __ \ 'income_tax \ 'payload,
            middleTierAmountJson("upper_rate_amount", dividendHigherRateTaxAmount.getOrElse(0))) andThen
          appendAttribute(
            __ \ 'income_tax \ 'payload,
            middleTierAmountJson("upper_rate", dividendHigherRateTax.getOrElse(0))) andThen
          appendAttribute(
            __ \ 'income_tax \ 'rates,
            middleTierRateJson("upper_rate_rate", dividendHigherRate.getOrElse(0))) andThen
          appendAttribute(
            __ \ 'income_tax \ 'payload,
            middleTierAmountJson("married_couples_allowance_adjustment", marriedCouplesAllowanceAdjust.getOrElse(0))) andThen
          appendAttribute(
            __ \ 'income_tax \ 'payload,
            middleTierAmountJson("marriage_allowance_received_amount", marriedCouplesAllowanceReceived.getOrElse(0))) andThen
          appendAttribute(
            __ \ 'income_tax \ 'payload,
            middleTierAmountJson("less_tax_adjustment_previous_year", lessTaxAdjustPreviousYear.getOrElse(0))) andThen
          appendAttribute(
            __ \ 'income_tax \ 'payload,
            middleTierAmountJson("tax_underpaid_previous_year", taxUnderpaidPreviousYear.getOrElse(0))) andThen
          appendAttribute(__ \ 'income_tax \ 'payload, middleTierAmountJson("total_income_tax", incomeTax.getOrElse(0)))

      safeTransform(jsonTransformer)
    }

    def transformGovSpendingData(source: JsObject): JsObject = {
      import play.api.libs.json.Reads._

      val totalTaxAmount: Option[Double] = pickAmount(__ \ 'calculatedTotals \ 'totalIncomeTaxNics, source)
      val taxYear = original.transform((__ \ 'taxYear).json.pick[JsNumber]).asOpt.map(n => n.as[Int])
      val govSpendingJson = Json
        .toJson(
          new GovSpendingDataTransformer(Amount(BigDecimal(totalTaxAmount.getOrElse(0.0)), "GBP"), taxYear.get).govSpendReferenceDTO)
        .as[JsObject]

      val jsonTransformer = (__ \ 'gov_spending).json.update((__).read[JsObject].map { o =>
        o ++ govSpendingJson
      })
      safeTransform(jsonTransformer)
    }
  }
}
