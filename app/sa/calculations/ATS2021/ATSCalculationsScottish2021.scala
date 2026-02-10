/*
 * Copyright 2026 HM Revenue & Customs
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

package sa.calculations.ATS2021

import common.models.{Amount, Rate}
import sa.models.ODSLiabilities.ODSLiabilities.*
import sa.models.TaxRate.*
import sa.models.SelfAssessmentAPIResponse

class ATSCalculationsScottish2021(
  val selfAssessmentAPIResponse: SelfAssessmentAPIResponse,
  val taxRates: Map[String, Rate]
) extends ATSCalculations2021 {

  override def scottishIncomeTax: Amount = Amount.empty("scottishIncomeTaxScottish2021")

  override def savingsRate: Amount = Amount.empty("savingsRateScottish2021")

  override def savingsRateAmount: Amount = Amount.empty("savingsRateAmountScottish2021")

  override def basicRateIncomeTaxAmount: Amount = Amount.empty("basicRateIncomeTaxAmountScottish2021")

  override def higherRateIncomeTaxAmount: Amount = Amount.empty("higherRateIncomeTaxAmountScottish2021")

  override def additionalRateIncomeTaxAmount: Amount = Amount.empty("additionalRateIncomeTaxAmountScottish2021")

  override def basicRateIncomeTax: Amount = Amount.empty("basicRateIncomeTaxScottish2021")

  override def higherRateIncomeTax: Amount = Amount.empty("higherRateIncomeTaxScottish2021")

  override def additionalRateIncomeTax: Amount = Amount.empty("additionalRateIncomeTaxScottish2021")

  override def scottishStarterRateTax: Amount =
    getWithDefaultAmount(TaxOnPayScottishStarterRate) + includePensionTaxForRate(
      taxRates.getOrElse(ScottishStarterRate, Rate.empty)
    )

  override def scottishBasicRateTax: Amount =
    getWithDefaultAmount(IncomeTaxBasicRate) + includePensionTaxForRate(
      taxRates.getOrElse(ScottishBasicRate, Rate.empty)
    )

  override def scottishIntermediateRateTax: Amount =
    getWithDefaultAmount(TaxOnPayScottishIntermediateRate) + includePensionTaxForRate(
      taxRates.getOrElse(ScottishIntermediateRate, Rate.empty)
    )

  override def scottishHigherRateTax: Amount =
    getWithDefaultAmount(IncomeTaxHigherRate) + includePensionTaxForRate(
      taxRates.getOrElse(ScottishHigherRate, Rate.empty)
    )

  override def scottishAdditionalRateTax: Amount =
    getWithDefaultAmount(IncomeTaxAddHighRate) + includePensionTaxForRate(
      taxRates.getOrElse(ScottishAdditionalRate, Rate.empty)
    )

  override def scottishStarterRateIncome: Amount =
    getWithDefaultAmount(TaxablePayScottishStarterRate) + includePensionIncomeForRate(
      taxRates.getOrElse(ScottishStarterRate, Rate.empty)
    )

  override def scottishBasicRateIncome: Amount =
    getWithDefaultAmount(IncomeChargeableBasicRate) + includePensionIncomeForRate(
      taxRates.getOrElse(ScottishBasicRate, Rate.empty)
    )

  override def scottishIntermediateRateIncome: Amount =
    getWithDefaultAmount(TaxablePayScottishIntermediateRate) + includePensionIncomeForRate(
      taxRates.getOrElse(ScottishIntermediateRate, Rate.empty)
    )

  override def scottishHigherRateIncome: Amount =
    getWithDefaultAmount(IncomeChargeableHigherRate) + includePensionIncomeForRate(
      taxRates.getOrElse(ScottishHigherRate, Rate.empty)
    )

  override def scottishAdditionalRateIncome: Amount =
    getWithDefaultAmount(IncomeChargeableAddHRate) + includePensionIncomeForRate(
      taxRates.getOrElse(ScottishAdditionalRate, Rate.empty)
    )

  override def savingsBasicRateTax: Amount = getWithDefaultAmount(SavingsTaxLowerRate)

  override def savingsHigherRateTax: Amount = getWithDefaultAmount(SavingsTaxHigherRate)

  override def savingsAdditionalRateTax: Amount = getWithDefaultAmount(SavingsTaxAddHighRate)

  override def savingsBasicRateIncome: Amount = getWithDefaultAmount(SavingsChargeableLowerRate)

  override def savingsHigherRateIncome: Amount = getWithDefaultAmount(SavingsChargeableHigherRate)

  override def savingsAdditionalRateIncome: Amount = getWithDefaultAmount(SavingsChargeableAddHRate)

  private val savingsTotalTax = savingsBasicRateTax + savingsHigherRateTax + savingsAdditionalRateTax

  override def totalIncomeTaxAmount: Amount =
    super.totalIncomeTaxAmount + scottishTotalTax + savingsTotalTax

}
