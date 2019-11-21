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

package services

import models.ApiValue
import play.api.libs.json.{Format, JsError, JsResult, JsString, JsSuccess, JsValue, Json}
import config.ApplicationConfig

sealed trait GoodsAndServices extends ApiValue

object GoodsAndServices {

  implicit val formats = new Format[GoodsAndServices] {
    override def writes(o: GoodsAndServices): JsValue = o match {
      case Welfare                  => JsString("Welfare")
      case Health                   => JsString("Health")
      case Education                => JsString("Education")
      case StatePensions            => JsString("StatePensions")
      case NationalDebtInterest     => JsString("NationalDebtInterest")
      case Defence                  => JsString("Defence")
      case CriminalJustice          => JsString("CriminalJustice")
      case Transport                => JsString("Transport")
      case BusinessAndIndustry      => JsString("BusinessAndIndustry")
      case GovernmentAdministration => JsString("GovernmentAdministration")
      case Culture                  => JsString("Culture")
      case HousingAndUtilities      => JsString("HousingAndUtilities")
      case OverseasAid              => JsString("OverseasAid")
      case UkContributionToEuBudget => JsString("UkContributionToEuBudget")
      case PublicOrderAndSafety     => JsString("PublicOrderAndSafety")
      case Environment              => JsString("Environment")

    }

    override def reads(json: JsValue): JsResult[GoodsAndServices] =
      json match {
        case JsString("Welfare")                  => JsSuccess(Welfare)
        case JsString("Health")                   => JsSuccess(Health)
        case JsString("Education")                => JsSuccess(Education)
        case JsString("StatePensions")            => JsSuccess(StatePensions)
        case JsString("NationalDebtInterest")     => JsSuccess(NationalDebtInterest)
        case JsString("Defence")                  => JsSuccess(Defence)
        case JsString("CriminalJustice")          => JsSuccess(CriminalJustice)
        case JsString("Transport")                => JsSuccess(Transport)
        case JsString("BusinessAndIndustry")      => JsSuccess(BusinessAndIndustry)
        case JsString("GovernmentAdministration") => JsSuccess(GovernmentAdministration)
        case JsString("Culture")                  => JsSuccess(Culture)
        case JsString("HousingAndUtilities")      => JsSuccess(HousingAndUtilities)
        case JsString("OverseasAid")              => JsSuccess(OverseasAid)
        case JsString("UkContributionToEuBudget") => JsSuccess(UkContributionToEuBudget)
        case JsString("PublicOrderAndSafety")     => JsSuccess(PublicOrderAndSafety)
        case JsString("Environment")              => JsSuccess(Environment)

        case _ => JsError(s"Unable to parse object $json as GoodsAndServices")
      }
  }

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
