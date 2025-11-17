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

package sa.transformers.ATS2025

import common.models.Amount
import sa.models.ODSLiabilities.ODSLiabilities.*
import sa.models.TaxSummaryLiability
import sa.services.TaxRateService

class ATSCalculationsScottish2025(val summaryData: TaxSummaryLiability, val taxRatesService: TaxRateService)
    extends ATSCalculations2025 {

  override def scottishIncomeTax: Amount = Amount.empty("scottishIncomeTaxScottish2025")

  override def savingsRate: Amount = Amount.empty("savingsRateScottish2025")

  override def savingsRateAmount: Amount = Amount.empty("savingsRateAmountScottish2025")

  override def basicRateIncomeTaxAmount: Amount = Amount.empty("basicRateIncomeTaxAmountScottish2025")

  override def higherRateIncomeTaxAmount: Amount = Amount.empty("higherRateIncomeTaxAmountScottish2025")

  override def additionalRateIncomeTaxAmount: Amount = Amount.empty("additionalRateIncomeTaxAmountScottish2025")

  override def basicRateIncomeTax: Amount = Amount.empty("basicRateIncomeTaxScottish2025")

  override def higherRateIncomeTax: Amount = Amount.empty("higherRateIncomeTaxScottish2025")

  override def additionalRateIncomeTax: Amount = Amount.empty("additionalRateIncomeTaxScottish2025")

  override def scottishStarterRateTax: Amount =
    getWithDefaultAmount(TaxOnPayScottishStarterRate) + get(TaxOnRedundancySsr) + includePensionTaxForRate(
      taxRatesService.scottishStarterRate
    )

  override def scottishBasicRateTax: Amount =
    getWithDefaultAmount(IncomeTaxBasicRate) + get(TaxOnRedundancyBr) + includePensionTaxForRate(
      taxRatesService.scottishBasicRate
    )

  override def scottishIntermediateRateTax: Amount =
    getWithDefaultAmount(TaxOnPayScottishIntermediateRate) + get(TaxOnRedundancySir) + includePensionTaxForRate(
      taxRatesService.scottishIntermediateRate
    )

  override def scottishHigherRateTax: Amount =
    getWithDefaultAmount(IncomeTaxHigherRate) + get(TaxOnRedundancyHr) + includePensionTaxForRate(
      taxRatesService.scottishHigherRate
    )

  override def scottishAdvancedRateTax: Amount =
    getWithDefaultAmount(TaxOnPayScottishAdvancedRate) + get(TaxOnRedundancySar) + includePensionTaxForRate(
      taxRatesService.scottishAdvancedRate
    )

  override def scottishAdditionalRateTax: Amount =
    getWithDefaultAmount(IncomeTaxAddHighRate) + get(TaxOnRedundancyAhr) + includePensionTaxForRate(
      taxRatesService.scottishAdditionalRate
    )

  override def scottishStarterRateIncome: Amount =
    getWithDefaultAmount(TaxablePayScottishStarterRate) + get(TaxableRedundancySsr) + includePensionIncomeForRate(
      taxRatesService.scottishStarterRate
    )

  override def scottishBasicRateIncome: Amount = // LS12.6: Scottish Basic rate Income tax x 20%
    getWithDefaultAmount(IncomeChargeableBasicRate) + get(TaxableRedundancyBr) + includePensionIncomeForRate(
      taxRatesService.scottishBasicRate
    )

  override def scottishIntermediateRateIncome: Amount =
    getWithDefaultAmount(TaxablePayScottishIntermediateRate) + get(TaxableRedundancySir) + includePensionIncomeForRate(
      taxRatesService.scottishIntermediateRate
    )

  override def scottishHigherRateIncome: Amount =
    getWithDefaultAmount(IncomeChargeableHigherRate) + get(TaxableRedundancyHr) + includePensionIncomeForRate(
      taxRatesService.scottishHigherRate
    )

  override def scottishAdvancedRateIncome: Amount =
    getWithDefaultAmount(TaxablePayScottishAdvancedRate) + get(TaxableRedundancySar) + includePensionIncomeForRate(
      taxRatesService.scottishAdvancedRate
    )

  override def scottishAdditionalRateIncome: Amount = // LS12.9: Top rate
    getWithDefaultAmount(IncomeChargeableAddHRate) + get(TaxableRedundancyAhr) + includePensionIncomeForRate(
      taxRatesService.scottishAdditionalRate
    )

  override def savingsBasicRateTax: Amount = getWithDefaultAmount(SavingsTaxLowerRate)

  override def savingsHigherRateTax: Amount = getWithDefaultAmount(SavingsTaxHigherRate)

  override def savingsAdditionalRateTax: Amount = getWithDefaultAmount(SavingsTaxAddHighRate) + get(TaxOnCegAhr)

  override def savingsBasicRateIncome: Amount = getWithDefaultAmount(SavingsChargeableLowerRate)

  override def savingsHigherRateIncome: Amount = getWithDefaultAmount(SavingsChargeableHigherRate)

  override def savingsAdditionalRateIncome: Amount = getWithDefaultAmount(SavingsChargeableAddHRate)

  private val savingsTotalTax = savingsBasicRateTax + savingsHigherRateTax + savingsAdditionalRateTax

  override def totalIncomeTaxAmount: Amount =
    super.totalIncomeTaxAmount + scottishTotalTax + savingsTotalTax

}
