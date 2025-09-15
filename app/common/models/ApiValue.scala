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

package common.models

import play.api.libs.json._

import scala.annotation.tailrec
import scala.collection.{Map => CMap}

abstract class ApiValue(val apiValue: String)

object ApiValue extends DefaultReads {
  /*
KKK:List(additional_rate_rate, cg_entrepreneurs_rate, cg_ordinary_rate, cg_upper_rate, additional_rate_income_tax_rate, basic_rate_income_tax_rate,
higher_rate_income_tax_rate, prop_interest_rate_higher_rate, prop_interest_rate_lower_rate, nics_and_tax_rate, ordinary_rate_tax_rate,
starting_rate_for_savings_rate, total_cg_tax_rate, upper_rate_rate, paye_ordinary_rate, paye_dividend_additional_rate, paye_higher_rate_income_tax,
paye_additional_rate_income_tax, paye_basic_rate_income_tax, paye_upper_rate, paye_scottish_starter_rate, paye_scottish_basic_rate,
paye_scottish_intermediate_rate, paye_scottish_higher_rate, paye_scottish_top_rate, scottish_starter_rate, scottish_basic_rate,
scottish_intermediate_rate, scottish_higher_rate, scottish_additional_rate, savings_lower_rate, savings_higher_rate,
savings_additional_rate):scottish_advanced_rate


   */
  private def readKeyValue[K <: ApiValue](key: String, ls: List[K]): JsResult[K] =
    ls.find(_.apiValue == key)
      .fold[JsResult[K]] {
        JsError(s"Failed to interpret key $key for key in Map")
      }(k => JsSuccess(k))

  private def readPair[K <: ApiValue, V](pair: (String, JsValue), ls: List[K], reads: Reads[V]): JsResult[(K, V)] =
    for {
      k <- readKeyValue(pair._1, ls)
      v <- pair._2.validate[V](reads)
    } yield (k, v)

  @tailrec
  private def readMapRecursive[K <: ApiValue, V](
    m: CMap[String, JsValue],
    ls: List[K],
    reads: Reads[V],
    acc: List[(K, V)],
    errors: Seq[(JsPath, Seq[JsonValidationError])]
  ): JsResult[Map[K, V]] =
    if (m.isEmpty) {
      if (errors.isEmpty) JsSuccess(Map(acc: _*)) else JsError(errors)
    } else {
      readPair(m.head, ls, reads) match {
        case JsSuccess(p, _) => readMapRecursive(m.tail, ls, reads, acc :+ p, errors)
        case JsError(er)     => readMapRecursive(m.tail, ls, reads, acc, errors ++ er.map(item => (item._1, item._2.toSeq)))
      }
    }

  private def readsMap[K <: ApiValue, V](ls: List[K], reads: Reads[V]): Reads[Map[K, V]] = Reads {
    case JsObject(m) => readMapRecursive(m, ls, reads, Nil, Nil)
    case _           => JsError("error.expected.jsobject")
  }

  def formatMap[A <: ApiValue, V: Format](ls: List[A]): Format[Map[A, V]] = Format(
    readsMap(ls, implicitly[Format[V]]),
    Writes[Map[A, V]] { o =>
      JsObject(o.map { case (k, v) =>
        k.apiValue -> Json.toJson(v)
      })
    }
  )

  def readFromList[A <: ApiValue](ls: List[A]): Reads[A] = Reads[A] {
    case JsString(s) =>
      ls.find(l => l.apiValue == s)
        .fold[JsResult[A]](JsError("error.expected.apivalue"))(JsSuccess(_))

    case _ => JsError("error.expected.jsstring")
  }
}
