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

import play.api.libs.json.{Format, JsError, JsResult, JsString, JsSuccess, JsValue, Json}

sealed trait GoodsAndServices

object GoodsAndServices {

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

        case _                                    => JsError(s"Unable to parse object $json as GoodsAndServices")
      }
  }

  case object Welfare extends GoodsAndServices
  case object Health extends GoodsAndServices
  case object Education extends GoodsAndServices
  case object StatePensions extends GoodsAndServices
  case object NationalDebtInterest extends GoodsAndServices
  case object Defence extends GoodsAndServices
  case object CriminalJustice extends GoodsAndServices
  case object Transport extends GoodsAndServices
  case object BusinessAndIndustry extends GoodsAndServices
  case object GovernmentAdministration extends GoodsAndServices
  case object Culture extends GoodsAndServices
  case object HousingAndUtilities extends GoodsAndServices
  case object OverseasAid extends GoodsAndServices
  case object UkContributionToEuBudget extends GoodsAndServices
  case object PublicOrderAndSafety extends GoodsAndServices
  case object Environment extends GoodsAndServices
}

object GovSpendService {

  import GoodsAndServices._

  def govSpending(taxYear: Int): Map[GoodsAndServices, BigDecimal] = taxYear match {
    case 2014 => taxYear2014
    case 2015 => taxYear2015
    case 2016 => taxYear2016
    case 2017 => taxYear2017
    case 2018 => taxYear2018
    case 2019 => taxYear2019
  }

  val taxYear2014: Map[GoodsAndServices, BigDecimal] = Map(
     Welfare -> 24.52,
    Health -> 18.87,
    Education -> 13.15,
    StatePensions -> 12.12,
    NationalDebtInterest -> 7.00,
    Defence -> 5.31,
    CriminalJustice -> 4.40,
    Transport -> 2.95,
    BusinessAndIndustry -> 2.74,
    GovernmentAdministration-> 2.05,
    Culture -> 1.69,
    Environment -> 1.66,
    HousingAndUtilities -> 1.64,
    OverseasAid -> 1.15,
    UkContributionToEuBudget -> 0.75
  )

  val taxYear2015: Map[GoodsAndServices, BigDecimal] = Map(
    Welfare -> 25.30,
    Health -> 19.90,
    StatePensions -> 12.80,
    Education -> 12.50,
    Defence -> 5.40,
    NationalDebtInterest -> 5.00,
    PublicOrderAndSafety -> 4.40,
    Transport -> 3.00,
    BusinessAndIndustry -> 2.70,
    GovernmentAdministration -> 2.00,
    Culture -> 1.80,
    Environment -> 1.70,
    HousingAndUtilities -> 1.60,
    OverseasAid -> 1.30,
    UkContributionToEuBudget -> 0.60
  )

  val taxYear2016: Map[GoodsAndServices, BigDecimal] = Map(
    Welfare -> 25.00,
    Health -> 19.90,
    StatePensions -> 12.80,
    Education -> 12.00,
    Defence -> 5.20,
    NationalDebtInterest -> 5.30,
    PublicOrderAndSafety -> 4.30,
    Transport -> 4.00,
    BusinessAndIndustry -> 2.40,
    GovernmentAdministration -> 2.00,
    Culture -> 1.60,
    Environment -> 1.70,
    HousingAndUtilities -> 1.40,
    OverseasAid -> 1.20,
    UkContributionToEuBudget -> 1.10
  )

  val taxYear2017: Map[GoodsAndServices, BigDecimal] = Map(
    Welfare -> 24.30,
    Health -> 20.30,
    StatePensions -> 12.90,
    Education -> 12.30,
    Defence -> 5.20,
    NationalDebtInterest -> 5.50,
    PublicOrderAndSafety -> 4.20,
    Transport -> 4.20,
    BusinessAndIndustry -> 2.50,
    GovernmentAdministration -> 2.10,
    Culture -> 1.60,
    Environment -> 1.60,
    HousingAndUtilities -> 1.50,
    OverseasAid -> 1.10,
    UkContributionToEuBudget -> 0.70
  )

  val taxYear2018: Map[GoodsAndServices, BigDecimal] = Map(
    Welfare -> 23.80,
    Health -> 19.90,
    StatePensions -> 12.80,
    Education -> 12.00,
    Defence -> 5.30,
    NationalDebtInterest -> 6.10,
    PublicOrderAndSafety -> 4.30,
    Transport -> 4.30,
    BusinessAndIndustry -> 2.90,
    GovernmentAdministration -> 2.10,
    Culture -> 1.60,
    Environment -> 1.60,
    HousingAndUtilities -> 1.60,
    OverseasAid -> 1.20,
    UkContributionToEuBudget -> 0.70
  )

  val taxYear2019: Map[GoodsAndServices, BigDecimal] = Map(
    Welfare -> 23.50,
    Health -> 20.20,
    StatePensions -> 12.80,
    Education -> 11.80,
    Defence -> 5.30,
    NationalDebtInterest -> 5.10,
    Transport -> 4.30,
    PublicOrderAndSafety -> 4.30,
    BusinessAndIndustry -> 3.60,
    GovernmentAdministration -> 2.10,
    HousingAndUtilities -> 1.60,
    Environment -> 1.50,
    Culture -> 1.50,
    OverseasAid -> 1.20,
    UkContributionToEuBudget -> 1.00
  )
}
