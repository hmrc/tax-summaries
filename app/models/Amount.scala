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

import play.api.Logging
import play.api.libs.functional.syntax._
import play.api.libs.json._

case class Amount(amount: BigDecimal, currency: String, calculus: Option[String] = None)
    extends Ordered[Amount]
    with Logging {

  def isZero: Boolean =
    amount.equals(BigDecimal(0))

  def isZeroOrLess: Boolean =
    amount <= 0

  def max(value: BigDecimal): Amount =
    copy(amount = this.amount.max(value), calculus = Some(s"max($value, ${this.calculus})"))

  def +(that: Amount): Amount = {
    require(this.currency equals that.currency)
    val calculus = (this.calculus, that.calculus) match {
      case (None, None)                             =>
        val error = new RuntimeException("A None calculus was found in both values")
        logger.error(error.getMessage, error)
        None
      case (Some(thisCalculus), None)               =>
        val error = new RuntimeException("A None calculus was found for argument that")
        logger.error(error.getMessage, error)
        Some(s"$thisCalculus + None")
      case (None, Some(thatCalculus))               =>
        val error = new RuntimeException("A None calculus was found for argument this")
        logger.error(error.getMessage, error)
        Some(s"None + $thatCalculus")
      case (Some(thisCalculus), Some(thatCalculus)) => Some(s"$thisCalculus + $thatCalculus")
    }
    copy(amount = this.amount + that.amount, calculus = calculus)
  }

  def -(that: Amount): Amount = {
    require(this.currency equals that.currency)
    val calculus = (this.calculus, that.calculus) match {
      case (None, None)                             =>
        val error = new RuntimeException("A None calculus was found in both values")
        logger.error(error.getMessage, error)
        None
      case (Some(thisCalculus), None)               =>
        val error = new RuntimeException("A None calculus was found for argument that")
        logger.error(error.getMessage, error)
        Some(s"$thisCalculus - None")
      case (None, Some(thatCalculus))               =>
        val error = new RuntimeException("A None calculus was found for argument this")
        logger.error(error.getMessage, error)
        Some(s"None - $thatCalculus")
      case (Some(thisCalculus), Some(thatCalculus)) => Some(s"$thisCalculus - $thatCalculus")
    }
    copy(amount = this.amount - that.amount, calculus = calculus)
  }

  def *(that: Double): Amount =
    copy(amount = this.amount * that, calculus = this.calculus.map(calc => s"$that * ($calc)"))

  def compare(that: Amount): Int = {
    require(this.currency equals that.currency)
    this.amount compare that.amount
  }

  def divideWithPrecision(that: Amount, scale: Int): Amount = {
    require(this.currency equals that.currency)
    copy(amount = (this.amount / that.amount).setScale(scale, BigDecimal.RoundingMode.DOWN))
  }

  def roundAmountUp(): Amount =
    copy(amount = this.amount.setScale(0, BigDecimal.RoundingMode.UP))

  def roundAmount(): Amount =
    copy(amount = this.amount.setScale(2, BigDecimal.RoundingMode.HALF_EVEN))

}

object Amount {
  implicit val writes: Writes[Amount] = (o: Amount) =>
    Json.obj(
      "amount"   -> o.amount,
      "currency" -> o.currency
    )

  implicit val reads: Reads[Amount] =
    (
      (JsPath \ "amount").read[BigDecimal] and
        (JsPath \ "currency").read[String]
    )((amount, currency) => Amount.apply(amount, currency))

  def empty(calculus: String): Amount = Amount(0, "GBP", Some(s"null ($calculus)"))

  def gbp(amount: BigDecimal, calculus: String): Amount = Amount(amount, "GBP", Some(s"$calculus"))
}
