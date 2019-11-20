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

import errors.AtsError
import play.api.libs.json._
import services.GoodsAndServices
import services.GoodsAndServices._

case class GovernmentSpendingOutputWrapper(
  taxYear: Int,
  govSpendAmountData: Map[GoodsAndServices, SpendData],
  totalAmount: Amount,
  errors: Option[AtsError])

object GovernmentSpendingOutputWrapper {
  implicit val formats = new Format[GoodsAndServices] {
    override def writes(o: GoodsAndServices): JsValue = o match {
      case Welfare => JsString("Welfare")

      case Welfare                  => JsString("Welfare")
      case Health                   => JsString("Health")
      case Education                => JsString("Education")
      case StatePensions            => JsString("StatePensions")
      case NationalDebtInterest     => JsString("NationalDebtInterest")
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
}
