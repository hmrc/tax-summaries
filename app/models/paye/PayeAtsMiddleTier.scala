/*
 * Copyright 2022 HM Revenue & Customs
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

package models.paye

import models.{DataHolder, GovernmentSpendingOutputWrapper}
import play.api.libs.functional.syntax._
import play.api.libs.json.{Format, JsPath, JsResult, JsValue, Json, Reads}

case class PayeAtsMiddleTier(
  taxYear: Int,
  nino: String,
  income_tax: Option[DataHolder],
  summary_data: Option[DataHolder],
  income_data: Option[DataHolder],
  allowance_data: Option[DataHolder],
  gov_spending: Option[GovernmentSpendingOutputWrapper]
)

object PayeAtsMiddleTier {

  implicit val format: Format[PayeAtsMiddleTier] = Json.format[PayeAtsMiddleTier]

//  implicit val format: Format[PayeAtsMiddleTier] = new Format[PayeAtsMiddleTier] {
//    override def writes(o: PayeAtsMiddleTier): JsValue = ???
//
//    override def reads(json: JsValue): JsResult[PayeAtsMiddleTier] = (
//      (JsPath \ "data" \ "taxYear").read[Int] ,
//        (JsPath \ "data" \ "nino").read[String] ,
//        (JsPath \ "data" \ "income_tax").readNullable[DataHolder] ,
//        (JsPath \ "data" \ "summary_data").readNullable[DataHolder] ,
//        (JsPath \ "data" \ "income_data").readNullable[DataHolder] ,
//        (JsPath \ "data" \ "allowance_data").readNullable[DataHolder] ,
//        (JsPath \ "data" \ "gov_spending").readNullable[GovernmentSpendingOutputWrapper]
//      )
//  }
//
//  implicit lazy val UserReads: Reads[User] = (
//    (__ \ 'id).read[Long] and
//      (__ \ 'name).read[String] and
//      (__ \ 'friend).lazyRead(UserReads)
//    )(User.apply _)

  //val r: Reads[Product] =
//
//  implicit val payeAtsMiddleTierReads: Reads[PayeAtsMiddleTier] = (
//    (JsPath \ "data" \ "taxYear").read[Int] and
//      (JsPath \ "data" \ "nino").read[String] and
//      (JsPath \ "data" \ "income_tax").readNullable[DataHolder] and
//      (JsPath \ "data" \ "summary_data").readNullable[DataHolder] and
//      (JsPath \ "data" \ "income_data").readNullable[DataHolder] and
//      (JsPath \ "data" \ "allowance_data").readNullable[DataHolder] and
//      (JsPath \ "data" \ "gov_spending").readNullable[GovernmentSpendingOutputWrapper]
//  )(PayeAtsMiddleTier.apply _)

}
