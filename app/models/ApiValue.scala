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

package models

import play.api.libs.json._

import scala.annotation.tailrec
import scala.collection.{Map => CMap}

abstract class ApiValue(val apiValue: String)

object ApiValue extends DefaultReads {

  private def readKeyValue[K <: ApiValue](key: String, ls: List[K]): JsResult[K] =
    ls.find(_.apiValue == key)
      .fold[JsResult[K]](
        JsError(s"Failed to interpret key $key for key in Map")
      )(k => JsSuccess(k))

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

  private def readsMap[K <: ApiValue, V: Reads](ls: List[K], reads: Reads[V]): Reads[Map[K, V]] = Reads {
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
