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

import play.api.libs.functional.syntax._
import play.api.libs.json._

final case class TaxSummaryLiability(
  taxYear: Int,
  pensionLumpSumTaxRate: PensionTaxRate,
  incomeTaxStatus: Option[String],
  nationalInsuranceData: Map[Liability, Amount],
  atsData: Map[Liability, Amount]
) {

  val isScottish: Boolean = incomeTaxStatus.contains("0002")
}

object TaxSummaryLiability {

  implicit def alwaysSuccessfulMapReads[K: Reads, V: Reads]: Reads[Map[K, V]] =
    Reads[Map[K, V]] {
      case JsObject(m) =>
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

      case _ => JsError("error.expected.jsobject")

    }

  implicit val reads: Reads[TaxSummaryLiability] =
    (
      (JsPath \ "taxYear").read[Int] and
        (JsPath \ "tliSlpAtsData" \ "ctnPensionLumpSumTaxRate").read[PensionTaxRate] and
        (JsPath \ "tliSlpAtsData" \ "incomeTaxStatus").readNullable[String].map(x => Some(x.getOrElse(""))) and
        (JsPath \ "saPayeNicDetails").read(alwaysSuccessfulMapReads[Liability, Amount]) and
        (JsPath \ "tliSlpAtsData").read(alwaysSuccessfulMapReads[Liability, Amount])
    )(TaxSummaryLiability.apply _)

}
