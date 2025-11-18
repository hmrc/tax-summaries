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
import sa.models.TaxRate.*

class TaxRateService @Inject() (taxRates: Map[String, Rate]) {
  def startingRateForSavingsRate: Rate = taxRates.getOrElse(StartingRateForSavingsRate, Rate.empty)

  def basicRateIncomeTaxRate: Rate = taxRates.getOrElse(BasicRateIncomeTaxRate, Rate.empty)

  def higherRateIncomeTaxRate: Rate = taxRates.getOrElse(HigherRateIncomeTaxRate, Rate.empty)

  def additionalRateIncomeTaxRate: Rate = taxRates.getOrElse(AdditionalRateIncomeTaxRate, Rate.empty)

  def dividendsOrdinaryRate: Rate = taxRates.getOrElse(DividendsOrdinaryRate, Rate.empty)

  def dividendUpperRateRate: Rate = taxRates.getOrElse(DividendUpperRateRate, Rate.empty)

  def dividendAdditionalRate: Rate = taxRates.getOrElse(DividendAdditionalRate, Rate.empty)

  def cgEntrepreneursRate: Rate = taxRates.getOrElse(CgEntrepreneursRate, Rate.empty)

  def cgOrdinaryRate: Rate = taxRates.getOrElse(CgOrdinaryRate, Rate.empty)

  def cgUpperRate: Rate = taxRates.getOrElse(CgUpperRate, Rate.empty)

  def individualsForResidentialPropertyAndCarriedInterestLowerRate: Rate  =
    taxRates.getOrElse(IndividualsForResidentialPropertyAndCarriedInterestLowerRate, Rate.empty)
  def individualsForResidentialPropertyAndCarriedInterestHigherRate: Rate =
    taxRates.getOrElse(IndividualsForResidentialPropertyAndCarriedInterestHigherRate, Rate.empty)

  def individualsForCIHigherRate: Rate = taxRates.getOrElse(IndividualsForCIHigherRate, Rate.empty)
  def individualsForCILowerRate: Rate  = taxRates.getOrElse(IndividualsForCILowerRate, Rate.empty)

  def individualsForRPHigherRate: Rate = taxRates.getOrElse(IndividualsForRPHigherRate, Rate.empty)
  def individualsForRPLowerRate: Rate  = taxRates.getOrElse(IndividualsForRPLowerRate, Rate.empty)

  def scottishStarterRate: Rate      = taxRates.getOrElse(ScottishStarterRate, Rate.empty)
  def scottishBasicRate: Rate        = taxRates.getOrElse(ScottishBasicRate, Rate.empty)
  def scottishIntermediateRate: Rate = taxRates.getOrElse(ScottishIntermediateRate, Rate.empty)
  def scottishHigherRate: Rate       = taxRates.getOrElse(ScottishHigherRate, Rate.empty)
  def scottishAdvancedRate: Rate     = taxRates.getOrElse(ScottishAdvancedRate, Rate.empty)
  def scottishAdditionalRate: Rate   = taxRates.getOrElse(ScottishAdditionalRate, Rate.empty)
}
