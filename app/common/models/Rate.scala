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

package common.models

import play.api.libs.json.{Format, Json, OFormat, Reads}

import java.text.NumberFormat
import java.util.Locale

case class Rate(percent: Double) {

  val apiValue: ApiRate = {
    val formatter = NumberFormat.getNumberInstance(Locale.UK)
    ApiRate(formatter.format(percent) + "%")
  }
}

object Rate {

  def rateFromPerUnitAmount(amountPerUnit: Amount): Rate =
    Rate((amountPerUnit.amount * 100).setScale(2, BigDecimal.RoundingMode.DOWN).doubleValue)

  val empty = 0.0

  implicit val formats: OFormat[Rate] = Json.format[Rate]
}

sealed case class ApiRate(percent: String)

object ApiRate {

  implicit val formats: OFormat[ApiRate] = Json.format[ApiRate]
}

sealed class RateKey(apiValue: String) extends ApiValue(apiValue)

object RateKey {

  case object Additional extends RateKey(apiValue = "additional_rate_rate")

  case object CapitalGainsEntrepreneur extends RateKey(apiValue = "cg_entrepreneurs_rate")

  case object CapitalGainsOrdinary extends RateKey(apiValue = "cg_ordinary_rate")

  case object CapitalGainsUpper extends RateKey(apiValue = "cg_upper_rate")

  case object IncomeAdditional extends RateKey(apiValue = "additional_rate_income_tax_rate")

  case object IncomeBasic extends RateKey(apiValue = "basic_rate_income_tax_rate")

  case object IncomeHigher extends RateKey(apiValue = "higher_rate_income_tax_rate")

  case object InterestHigher extends RateKey(apiValue = "prop_interest_rate_higher_rate")

  case object InterestLower extends RateKey(apiValue = "prop_interest_rate_lower_rate")

  case object InterestCIHigher extends RateKey(apiValue = "ci_interest_rate_higher_rate")

  case object InterestCILower extends RateKey(apiValue = "ci_interest_rate_lower_rate")

  case object InterestRPHigher extends RateKey(apiValue = "rp_interest_rate_higher_rate")

  case object InterestRPLower extends RateKey(apiValue = "rp_interest_rate_lower_rate")

  case object NICS extends RateKey(apiValue = "nics_and_tax_rate")

  case object Ordinary extends RateKey(apiValue = "ordinary_rate_tax_rate")

  case object Savings extends RateKey("starting_rate_for_savings_rate")

  case object TotalCapitalGains extends RateKey("total_cg_tax_rate")

  case object Upper extends RateKey("upper_rate_rate")

  case object PayeDividendOrdinaryRate extends RateKey("paye_ordinary_rate")

  case object PayeHigherRateIncomeTax extends RateKey("paye_higher_rate_income_tax")

  case object PayeAdditionalRateIncomeTax extends RateKey("paye_additional_rate_income_tax")

  case object PayeBasicRateIncomeTax extends RateKey("paye_basic_rate_income_tax")

  case object PayeDividendUpperRate extends RateKey("paye_upper_rate")

  case object PayeDividendAdditionalRate extends RateKey("paye_dividend_additional_rate")

  case object PayeScottishStarterRate extends RateKey("paye_scottish_starter_rate")

  case object PayeScottishBasicRate extends RateKey("paye_scottish_basic_rate")

  case object PayeScottishIntermediateRate extends RateKey("paye_scottish_intermediate_rate")

  case object PayeScottishHigherRate extends RateKey("paye_scottish_higher_rate")

  case object PayeScottishAdvancedRate extends RateKey("paye_scottish_advanced_rate")

  case object PayeScottishTopRate extends RateKey("paye_scottish_top_rate")

  case object ScottishStarterRate extends RateKey("scottish_starter_rate")

  case object ScottishBasicRate extends RateKey("scottish_basic_rate")

  case object ScottishIntermediateRate extends RateKey("scottish_intermediate_rate")

  case object ScottishHigherRate extends RateKey("scottish_higher_rate")

  case object ScottishAdvancedRate extends RateKey("scottish_advanced_rate")

  case object ScottishAdditionalRate extends RateKey("scottish_additional_rate")

  case object SavingsLowerRate extends RateKey(apiValue = "savings_lower_rate")

  case object SavingsHigherRate extends RateKey(apiValue = "savings_higher_rate")

  case object SavingsAdditionalRate extends RateKey(apiValue = "savings_additional_rate")

  // format: off
  val allItems: List[RateKey] =
    List(
      Additional, CapitalGainsEntrepreneur, CapitalGainsOrdinary, CapitalGainsUpper, IncomeAdditional, IncomeBasic,
      IncomeHigher, InterestHigher, InterestCILower, InterestCIHigher, InterestRPLower, InterestRPHigher, InterestLower, NICS, Ordinary, Savings, TotalCapitalGains, Upper,
      PayeDividendOrdinaryRate, PayeDividendAdditionalRate, PayeHigherRateIncomeTax, PayeAdditionalRateIncomeTax, PayeBasicRateIncomeTax, PayeDividendUpperRate,
      PayeScottishStarterRate, PayeScottishBasicRate, PayeScottishIntermediateRate, PayeScottishHigherRate, PayeScottishAdvancedRate, PayeScottishTopRate,
      ScottishStarterRate, ScottishBasicRate, ScottishAdvancedRate, ScottishIntermediateRate, ScottishHigherRate, ScottishAdditionalRate,
      SavingsLowerRate, SavingsHigherRate, SavingsAdditionalRate
    )
  // format: on

  implicit def mapFormat[V: Format]: Format[Map[RateKey, V]] = ApiValue.formatMap[RateKey, V](allItems)

  implicit val reads: Reads[RateKey] = ApiValue.readFromList(allItems)
}
