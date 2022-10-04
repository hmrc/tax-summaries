/*
 * Copyright 2022 HM Revenue & Customs
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

package services

import com.google.inject.{Inject, Singleton}
import config.ApplicationConfig
import models.ApiValue
import play.api.libs.json.{Format, JsString, Writes}

sealed class GoodsAndServices(apiValue: String) extends ApiValue(apiValue)

object GoodsAndServices {

  val allItems: List[GoodsAndServices] = List[GoodsAndServices](
    Welfare,
    Health,
    Education,
    StatePensions,
    NationalDebtInterest,
    Defence,
    CriminalJustice,
    Transport,
    BusinessAndIndustry,
    GovernmentAdministration,
    Culture,
    HousingAndUtilities,
    OverseasAid,
    UkContributionToEuBudget,
    OutstandingPaymentsToTheEU,
    PublicOrderAndSafety,
    Environment
  )

  implicit val formats: Format[GoodsAndServices] = Format(
    ApiValue.readFromList(allItems),
    Writes[GoodsAndServices](o => JsString(o.apiValue))
  )

  case object Welfare extends GoodsAndServices("Welfare")

  case object Health extends GoodsAndServices("Health")

  case object Education extends GoodsAndServices("Education")

  case object StatePensions extends GoodsAndServices("StatePensions")

  case object NationalDebtInterest extends GoodsAndServices("NationalDebtInterest")

  case object Defence extends GoodsAndServices("Defence")

  case object CriminalJustice extends GoodsAndServices("CriminalJustice")

  case object Transport extends GoodsAndServices("Transport")

  case object BusinessAndIndustry extends GoodsAndServices("BusinessAndIndustry")

  case object GovernmentAdministration extends GoodsAndServices("GovernmentAdministration")

  case object Culture extends GoodsAndServices("Culture")

  case object HousingAndUtilities extends GoodsAndServices("HousingAndUtilities")

  case object OverseasAid extends GoodsAndServices("OverseasAid")

  case object UkContributionToEuBudget extends GoodsAndServices("UkContributionToEuBudget")

  case object OutstandingPaymentsToTheEU extends GoodsAndServices("OutstandingPaymentsToTheEU")

  case object PublicOrderAndSafety extends GoodsAndServices("PublicOrderAndSafety")

  case object Environment extends GoodsAndServices("Environment")

  implicit def mapFormat[V: Format]: Format[Map[GoodsAndServices, V]] =
    ApiValue.formatMap[GoodsAndServices, V](allItems)
}

@Singleton
class GovSpendService @Inject()(applicationConfig: ApplicationConfig) {

  import GoodsAndServices._

  def govSpending(taxYear: Int): Map[GoodsAndServices, Double] =
    applicationConfig
      .governmentSpend(taxYear)
      .toList
      .map {
        case (k, v) => allItems.find(_.apiValue == k).map(k => (k, v))
      }
      .collect {
        case Some(v) => v
      }
      .toMap
}
