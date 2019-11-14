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

package services

import config.ApplicationConfig
import models.Rate

object TaxRateService {

  private def getRate(taxYear: Int, rate: String): Rate = {
    val result = ApplicationConfig.ratePercentages(taxYear)
    Rate(result.getOrElse(rate, Rate.empty))
  }

  def startingRateForSavingsRate(taxYear: Int): Rate = getRate(taxYear,"startingRateForSavingsRate")

  def basicRateIncomeTaxRate(taxYear: Int): Rate = getRate(taxYear, "basicRateIncomeTaxRate")

  def higherRateIncomeTaxRate(taxYear: Int): Rate = getRate(taxYear,"higherRateIncomeTaxRate")

  def additionalRateIncomeTaxRate(taxYear: Int): Rate = getRate(taxYear,"additionalRateIncomeTaxRate")

  def dividendsOrdinaryRate(taxYear: Int): Rate = getRate(taxYear,"dividendsOrdinaryRate")

  def dividendUpperRateRate(taxYear: Int): Rate = getRate(taxYear,"dividendUpperRateRate")

  def dividendAdditionalRate(taxYear: Int): Rate = getRate(taxYear,"dividendAdditionalRate")

  def cgEntrepreneursRate(taxYear: Int): Rate = getRate(taxYear,"cgEntrepreneursRate")

  def cgOrdinaryRate(taxYear: Int): Rate = getRate(taxYear,"cgOrdinaryRate")

  def cgUpperRate(taxYear: Int): Rate = getRate(taxYear,"cgUpperRate")

  def individualsForResidentialPropertyAndCarriedInterestLowerRate(taxYear: Int): Rate = getRate(taxYear,"RCPILowerRate")

  def individualsForResidentialPropertyAndCarriedInterestHigherRate(taxYear: Int): Rate = getRate(taxYear,"RCPIHigherRate")
}
