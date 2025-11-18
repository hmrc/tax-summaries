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

package sa.services

import com.google.inject.Inject
import common.models.Rate

class TaxRateService @Inject() (val taxYear: Int, configRate: Int => Map[String, Double]) {

  private def getRate(rate: String): Rate = {
    val result = configRate(taxYear)
    Rate(result.getOrElse(rate, Rate.empty))
  }

  def startingRateForSavingsRate: Rate = getRate("startingRateForSavingsRate")

  def basicRateIncomeTaxRate: Rate = getRate("basicRateIncomeTaxRate")

  def higherRateIncomeTaxRate: Rate = getRate("higherRateIncomeTaxRate")

  def additionalRateIncomeTaxRate: Rate = getRate("additionalRateIncomeTaxRate")

  def dividendsOrdinaryRate: Rate = getRate("dividendsOrdinaryRate")

  def dividendUpperRateRate: Rate = getRate("dividendUpperRateRate")

  def dividendAdditionalRate: Rate = getRate("dividendAdditionalRate")

  def cgEntrepreneursRate: Rate = getRate("cgEntrepreneursRate")

  def cgOrdinaryRate: Rate = getRate("cgOrdinaryRate")

  def cgUpperRate: Rate = getRate("cgUpperRate")

  def individualsForResidentialPropertyAndCarriedInterestLowerRate: Rate  = getRate("RPCILowerRate")
  def individualsForResidentialPropertyAndCarriedInterestHigherRate: Rate = getRate("RPCIHigherRate")

  def individualsForCIHigherRate: Rate = getRate("ciHigherRate")
  def individualsForCILowerRate: Rate  = getRate("ciLowerRate")

  def individualsForRPHigherRate: Rate = getRate("rpHigherRate")
  def individualsForRPLowerRate: Rate  = getRate("rpLowerRate")

  def scottishStarterRate: Rate      = getRate("scottishStarterRate")
  def scottishBasicRate: Rate        = getRate("scottishBasicRate")
  def scottishIntermediateRate: Rate = getRate("scottishIntermediateRate")
  def scottishHigherRate: Rate       = getRate("scottishHigherRate")
  def scottishAdvancedRate: Rate     = getRate("scottishAdvancedRate")
  def scottishAdditionalRate: Rate   = getRate("scottishAdditionalRate")
}
