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

package services

import com.google.inject.{Inject, Singleton}
import config.ApplicationConfig
import models.ApiValue
import play.api.libs.json.{Format, JsString, Writes}

import scala.collection.immutable.ListMap
import scala.math.Ordering.DeprecatedDoubleOrdering

sealed trait GoodsAndServices extends ApiValue

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
  case object OutstandingPaymentsToTheEU extends ApiValue("OutstandingPaymentsToTheEU") with GoodsAndServices
  case object PublicOrderAndSafety extends ApiValue("PublicOrderAndSafety") with GoodsAndServices
  case object Environment extends ApiValue("Environment") with GoodsAndServices

  implicit def mapFormat[V: Format]: Format[Map[GoodsAndServices, V]] =
    ApiValue.formatMap[GoodsAndServices, V](allItems)
}

@Singleton
class GovSpendService @Inject() (applicationConfig: ApplicationConfig) {

  import GoodsAndServices._

  def govSpending(taxYear: Int): Map[GoodsAndServices, Double] = {
    val govList = applicationConfig
      .governmentSpend(taxYear)

    val governmentSpendUnsorted = govList
      .map(item => allItems.find(_.apiValue == item.name).map(k => (k, item.value)))
      .collect { case Some(v) =>
        v
      }
    val governmentSpend         =
      taxYear match {
        case 2024 => governmentSpendUnsorted.sortBy(_._2)(DeprecatedDoubleOrdering.reverse)
        case _    => governmentSpendUnsorted
      }

    val listMap = ListMap(governmentSpend: _*)
    listMap
  }
}
