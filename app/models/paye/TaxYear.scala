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

package models.paye

case class TaxYear(ty: String) {
  require(TaxYear.isValidTaxYear(ty), s"$ty is not a valid Tax Year.")

  val startYr = ty.toInt
}

object TaxYear {

  private val validTaxYearFormat = "^(\\d{4})$"

  def isValidTaxYear(taxYear: String) = taxYear != null && taxYear.matches(validTaxYearFormat)

}
