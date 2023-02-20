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

package transformers.Scottish

import models.Liability._
import models.{Amount, TaxSummaryLiability}
import services.TaxRateService
import transformers.ATSCalculations2021

class ATSCalculationsScottish2021(val summaryData: TaxSummaryLiability, val taxRates: TaxRateService)
    extends ATSCalculations2021 {

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
    getWithDefaultAmount(TaxOnPayScottishStarterRate) + includePensionTaxForRate(taxRates.scottishStarterRate)

  override def scottishBasicRateTax: Amount =
    getWithDefaultAmount(IncomeTaxBasicRate) + includePensionTaxForRate(taxRates.scottishBasicRate)

  override def scottishIntermediateRateTax: Amount =
    getWithDefaultAmount(TaxOnPayScottishIntermediateRate) + includePensionTaxForRate(taxRates.scottishIntermediateRate)

  override def scottishHigherRateTax: Amount =
    getWithDefaultAmount(IncomeTaxHigherRate) + includePensionTaxForRate(taxRates.scottishHigherRate)

  override def scottishAdditionalRateTax: Amount =
    getWithDefaultAmount(IncomeTaxAddHighRate) + includePensionTaxForRate(taxRates.scottishAdditionalRate)

  override def scottishStarterRateIncome: Amount =
    getWithDefaultAmount(TaxablePayScottishStarterRate) + includePensionIncomeForRate(taxRates.scottishStarterRate)

  override def scottishBasicRateIncome: Amount =
    getWithDefaultAmount(IncomeChargeableBasicRate) + includePensionIncomeForRate(taxRates.scottishBasicRate)

  override def scottishIntermediateRateIncome: Amount =
    getWithDefaultAmount(TaxablePayScottishIntermediateRate) + includePensionIncomeForRate(
      taxRates.scottishIntermediateRate
    )

  override def scottishHigherRateIncome: Amount =
    getWithDefaultAmount(IncomeChargeableHigherRate) + includePensionIncomeForRate(taxRates.scottishHigherRate)

  override def scottishAdditionalRateIncome: Amount =
    getWithDefaultAmount(IncomeChargeableAddHRate) + includePensionIncomeForRate(taxRates.scottishAdditionalRate)

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
