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

import play.api.libs.functional.syntax._
import play.api.libs.json._

sealed abstract class Liability(val apiValue: String)

object Liability {

  case object EmploymentBenefitsAmount extends Liability("ctnEmploymentBenefitsAmt")
  case object SummaryTotalScheduleD extends Liability("ctnSummaryTotalScheduleD")
  case object SummaryTotalPartnership extends Liability("ctnSummaryTotalPartnership")
  case object SummaryTotalEmployment extends Liability(  "ctnSummaryTotalEmployment")
  case object StatePensionAmount extends Liability(  "atsStatePensionAmt")
  case object OtherPensionAmount extends Liability(  "atsOtherPensionAmt")
  case object StatePensionLsGrossAmount extends Liability("itfStatePensionLsGrossAmt")




  val allLiabilities: List[Liability] =
    List(EmploymentBenefitsAmount)

  implicit val reads: Reads[Liability] = Reads[Liability] {
    case JsString(s) =>
      allLiabilities
        .find(l => l.apiValue == s)
        .fold[JsResult[Liability]](JsError("error.expected.liability"))(JsSuccess(_))

    case _ => JsError("error.expected.jsstring")
  }

}


final case class TaxSummaryLiablity(taxYear: Int, ctnPensionLumpSumTaxRate: Double, incomeTaxStatus: String ,
                                    saPayeNicDetails: Map[String, Amount], tliSlpAtsData: Map[Liability, Amount])

  object TaxSummaryLiablity {

    implicit def mapReads[K: Reads, V: Reads]: Reads[Map[K, V]] =
      Reads[Map[K, V]] {
        case JsObject(m) => {

          JsSuccess(m.foldLeft(Map.empty[K, V]) {
            case (acc, (key, value)) =>
              val result = for {
                rv <- value.validate[V]
                rk <- JsString(key).validate[K]
              } yield rk -> rv

              result match {
                case JsSuccess(v, _) => acc + v
                case _               => acc
              }
          })
        }

        case _ => JsError("error.expected.jsobject")

      }

    implicit  val reads: Reads[TaxSummaryLiablity] =
      (
        (JsPath \ "taxYear").read[Int] and
        (JsPath \ "tliSlpAtsData" \ "ctnPensionLumpSumTaxRate").read[Double] and
        (JsPath \ "tliSlpAtsData" \ "incomeTaxStatus").read[String] and
        (JsPath \ "saPayeNicDetails").read(mapReads[String, Amount]) and
        (JsPath \ "tliSlpAtsData").read(mapReads[Liability, Amount])
        )(TaxSummaryLiablity.apply _)
    }





