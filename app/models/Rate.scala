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

import java.text.NumberFormat
import java.util.Locale

import play.api.libs.json.Json

case class Rate(percent: Double) {

  val apiValue: ApiRate = {
    val formatter = NumberFormat.getNumberInstance(Locale.UK)
    ApiRate(formatter.format(percent) + "%")
  }
}


object Rate {

  def rateFromPerUnitAmount(amountPerUnit: Amount): Rate = {
    Rate((amountPerUnit.amount * 100).setScale(2, BigDecimal.RoundingMode.DOWN).doubleValue())
  }

  val empty = 0.0

  implicit val formats = Json.format[Rate]
}

sealed case class ApiRate(percent: String)

object ApiRate {

  implicit val formats = Json.format[ApiRate]
}
