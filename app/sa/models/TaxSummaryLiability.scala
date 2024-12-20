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

package sa.models

import common.models.Amount
import play.api.Logging
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json._
import sa.models.ODSLiabilities.ODSLiabilities
import sa.models.ODSLiabilities.ODSLiabilities.readsLiabilities

final case class TaxSummaryLiability(
  taxYear: Int,
  pensionLumpSumTaxRate: PensionTaxRate,
  incomeTaxStatus: Option[Nationality],
  nationalInsuranceData: Map[ODSLiabilities, Amount],
  atsData: Map[ODSLiabilities, Amount]
) {
  val nationality: Nationality = incomeTaxStatus.getOrElse(UK())
}

object TaxSummaryLiability extends Logging {

  private def alwaysSuccessfulMapReads[K: Reads, V: Reads]: Reads[Map[K, Option[V]]] =
    Reads[Map[K, Option[V]]] {
      case JsObject(m) =>
        m.foldLeft(JsSuccess(Map.empty[K, Option[V]]): JsResult[Map[K, Option[V]]]) {
          case (accJsResult, ("ctnPensionLumpSumTaxRate", _)) =>
            // This field is read separately. See Reads[TaxSummaryLiability] below
            accJsResult
          case (accJsResult, ("incomeTaxStatus", _))          =>
            // This field is read separately. See Reads[TaxSummaryLiability] below
            accJsResult
          case (JsSuccess(acc, _), (key, value))              =>
            (JsString(key).validate[K], value.validate[V]) match {
              case (JsSuccess(liability, _), JsSuccess(amount, _))          =>
                // Happy path. The liability exists and the amount is valid
                JsSuccess(acc ++ Map[K, Option[V]](liability -> Some(amount)))
              case (JsSuccess(liability, _), JsError(_)) if value == JsNull =>
                // The liability key exists but no value
                JsSuccess(acc ++ Map[K, Option[V]](liability -> None))
              case (JsSuccess(_, _), JsError(_))                            =>
                // The liability key exists but the amount is invalid
                JsError(s"Error while parsing $key:${value.toString}")
              case _                                                        =>
                // The liability key should not be used, ignoring.
                JsSuccess(acc)
            }
          case (JsError(error), _)                            => JsError(error)
        }
      case _           => JsError("error.expected.jsobject")
    }

  implicit val reads: Reads[TaxSummaryLiability] = new Reads[TaxSummaryLiability] {
    override def reads(json: JsValue): JsResult[TaxSummaryLiability] = {
      val href           = (json \ "links" \\ "href").flatMap(_.asOpt[String]).headOption.getOrElse("")
      val taxYear        = (json \ "taxYear").as[Int]
      val nationality    = (json \ "tliSlpAtsData" \ "incomeTaxStatus").asOpt[Nationality].getOrElse(UK())
      val pensionTaxRate = (json \ "tliSlpAtsData" \ "ctnPensionLumpSumTaxRate").as[PensionTaxRate]

      val saPayeNicDetails = (json \ "saPayeNicDetails").as[JsValue]
      val tliSlpAtsData    = (json \ "tliSlpAtsData").as[JsValue]

      val pattern = ".*/self-assessment/individuals/([0-9]+)/annual-tax-summaries/.*".r

      val utr = href match {
        case pattern(utr) => utr
        case _            => ""
      }

      val nationalInsuranceData = saPayeNicDetails
        .as[Map[ODSLiabilities, Option[Amount]]](
          alwaysSuccessfulMapReads[ODSLiabilities, Amount](readsLiabilities(taxYear), implicitly)
        )
        .map {
          case (liability, None)                                              =>
            logger.warn(s"id: $utr, TaxYear: $taxYear, field: $liability, value: null")
            liability -> Amount(0, "GBP")
          case (liability, Some(amount)) if amount.amount == BigDecimal(0.00) =>
            logger.info(s"id: $utr, TaxYear: $taxYear, field: $liability, value: zero")
            liability -> amount
          case (liability, Some(amount))                                      =>
            liability -> amount
        }

      val atsData = tliSlpAtsData
        .as[Map[ODSLiabilities, Option[Amount]]](
          alwaysSuccessfulMapReads[ODSLiabilities, Amount](readsLiabilities(taxYear), implicitly)
        )
        .map {
          case (liability, None)                                              =>
            logger.warn(s"id: $utr, TaxYear: $taxYear, field: $liability, value: null")
            liability -> Amount(0, "GBP")
          case (liability, Some(amount)) if amount.amount == BigDecimal(0.00) =>
            logger.info(s"id: $utr, TaxYear: $taxYear, field: $liability, value: zero")
            liability -> amount
          case (liability, Some(amount))                                      =>
            liability -> amount
        }

      JsSuccess(
        TaxSummaryLiability.apply(
          taxYear,
          pensionTaxRate,
          Some(nationality),
          nationalInsuranceData,
          atsData
        )
      )
    }
  }

}
