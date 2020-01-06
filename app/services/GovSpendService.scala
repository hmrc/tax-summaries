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

package services

import models.ApiValue
import play.api.libs.json.{Format, JsError, JsResult, JsString, JsSuccess, JsValue, Json, Writes}
import config.ApplicationConfig

sealed trait GoodsAndServices extends ApiValue

object GoodsAndServices {

  implicit val formats: Format[GoodsAndServices] = Format(
    ApiValue.readFromList(allItems),
    Writes[GoodsAndServices](o => JsString(o.apiValue))
  )

  case object Welfare extends ApiValue("Welfare") with GoodsAndServices
  case object Health extends ApiValue("Health") with GoodsAndServices
  case object Education extends ApiValue("Education") with GoodsAndServices
  case object StatePensions extends ApiValue("StatePensions") with GoodsAndServices
  case object NationalDebtInterest extends ApiValue("NationalDebtInterest") with GoodsAndServices
  case object Defence extends ApiValue("Defence") with GoodsAndServices
  case object CriminalJustice extends ApiValue("CriminalJustice") with GoodsAndServices
  case object Transport extends ApiValue("Transport") with GoodsAndServices
  case object BusinessAndIndustry extends ApiValue("BusinessAndIndustry") with GoodsAndServices
  case object GovernmentAdministration extends ApiValue("GovernmentAdministration") with GoodsAndServices
  case object Culture extends ApiValue("Culture") with GoodsAndServices
  case object HousingAndUtilities extends ApiValue("HousingAndUtilities") with GoodsAndServices
  case object OverseasAid extends ApiValue("OverseasAid") with GoodsAndServices
  case object UkContributionToEuBudget extends ApiValue("UkContributionToEuBudget") with GoodsAndServices
  case object PublicOrderAndSafety extends ApiValue("PublicOrderAndSafety") with GoodsAndServices
  case object Environment extends ApiValue("Environment") with GoodsAndServices

  val allItems = List[GoodsAndServices](
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
    PublicOrderAndSafety,
    Environment
  )

  implicit def mapFormat[V: Format]: Format[Map[GoodsAndServices, V]] =
    ApiValue.formatMap[GoodsAndServices, V](allItems)
}

object GovSpendService {

  import GoodsAndServices._

  def govSpending(taxYear: Int): Map[GoodsAndServices, Double] =
    ApplicationConfig
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
