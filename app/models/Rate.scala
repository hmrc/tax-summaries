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

import java.text.NumberFormat
import java.util.Locale

import models.LiabilityKey.allItems
import play.api.libs.json.{Format, Json, Reads}

case class Rate(percent: Double) {

  val apiValue: ApiRate = {
    val formatter = NumberFormat.getNumberInstance(Locale.UK)
    ApiRate(formatter.format(percent) + "%")
  }
}

object Rate {

  def rateFromPerUnitAmount(amountPerUnit: Amount): Rate =
    Rate((amountPerUnit.amount * 100).setScale(2, BigDecimal.RoundingMode.DOWN).doubleValue())

  val empty = 0.0

  implicit val formats = Json.format[Rate]
}

sealed case class ApiRate(percent: String)

object ApiRate {

  implicit val formats = Json.format[ApiRate]
}

sealed trait RateKey extends ApiValue

object RateKey {

  case object Additional extends ApiValue("additional_rate_rate") with RateKey
  case object CapitalGainsEntrepreneur extends ApiValue("cg_entrepreneurs_rate") with RateKey
  case object CapitalGainsOrdinary extends ApiValue("cg_ordinary_rate") with RateKey
  case object CapitalGainsUpper extends ApiValue("cg_upper_rate") with RateKey
  case object IncomeAdditional extends ApiValue("additional_rate_income_tax_rate") with RateKey
  case object IncomeBasic extends ApiValue("basic_rate_income_tax_rate") with RateKey
  case object IncomeHigher extends ApiValue("higher_rate_income_tax_rate") with RateKey
  case object InterestHigher extends ApiValue("prop_interest_rate_higher_rate") with RateKey
  case object InterestLower extends ApiValue("prop_interest_rate_lower_rate") with RateKey
  case object NICS extends ApiValue("nics_and_tax_rate") with RateKey
  case object Ordinary extends ApiValue("ordinary_rate_tax_rate") with RateKey
  case object Savings extends ApiValue("starting_rate_for_savings_rate") with RateKey
  case object TotalCapitalGains extends ApiValue("total_cg_tax_rate") with RateKey
  case object Upper extends ApiValue("upper_rate_rate") with RateKey

  case object ScottishStarterRate extends ApiValue("scottish_starter_rate") with RateKey
  case object ScottishBasicRate extends ApiValue("scottish_basic_rate") with RateKey
  case object ScottishIntermediateRate extends ApiValue("scottish_intermediate_rate") with RateKey
  case object ScottishHigherRate extends ApiValue("scottish_higher_rate") with RateKey
  case object ScottishAdditionalRate extends ApiValue("scottish_additional_rate") with RateKey
  case object SavingsLowerRate extends ApiValue("savings_lower_rate") with RateKey
  case object SavingsHigherRate extends ApiValue("savings_higher_rate") with RateKey
  case object SavingsAdditionalRate extends ApiValue("savings_additional_rate") with RateKey

  // format: off
  val allItems: List[RateKey] =
    List(
      Additional, CapitalGainsEntrepreneur, CapitalGainsOrdinary, CapitalGainsUpper, IncomeAdditional, IncomeBasic,
      IncomeHigher, InterestHigher, InterestLower, NICS, Ordinary, Savings, TotalCapitalGains, Upper, ScottishStarterRate,
      ScottishBasicRate, ScottishIntermediateRate, ScottishHigherRate, ScottishAdditionalRate, SavingsLowerRate,
      SavingsHigherRate, SavingsAdditionalRate
    )
  // format: on

  implicit def mapFormat[V: Format]: Format[Map[RateKey, V]] = ApiValue.formatMap[RateKey, V](allItems)

  implicit val reads: Reads[RateKey] = ApiValue.readFromList(allItems)
}
