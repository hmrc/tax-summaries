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

import play.api.libs.json.Json

case class Amount(amount: BigDecimal, currency: String) extends Ordered[Amount] {

  def isZero: Boolean =
    amount.equals(BigDecimal(0))

  def isZeroOrLess: Boolean =
    amount <= 0

  def +(that: Amount): Amount = {
    require(this.currency equals that.currency)
    copy(amount = this.amount + that.amount)
  }

  def -(that: Amount): Amount = {
    require(this.currency equals that.currency)
    copy(amount = this.amount - that.amount)
  }

  def compare(that: Amount): Int = {
    require(this.currency equals that.currency)
    this.amount compare that.amount
  }

  def divideWithPrecision(that: Amount, scale: Int): Amount = {
    require(this.currency equals that.currency)
    copy(amount = (this.amount / that.amount).setScale(scale, BigDecimal.RoundingMode.DOWN))
  }

  def multiplyWithPrecision(that: Amount, scale: BigDecimal): Amount = {
    require(this.currency equals that.currency)
    copy(amount = (this.amount * that.amount))
  }

  def roundAmountUp(): Amount =
    copy(amount = this.amount.setScale(0, BigDecimal.RoundingMode.UP))

}

object Amount {
  implicit val formats = Json.format[Amount]

  val empty = Amount(0, "GBP")

  def gbp(amount: BigDecimal) = Amount(amount, "GBP")
}
