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

package sa.transformers.ATS2024

import common.models.{Amount, Rate}
import sa.models.ODSLiabilities.ODSLiabilities.{IncomeChargeableAddHRate, IncomeChargeableBasicRate, IncomeChargeableHigherRate, IncomeTaxAddHighRate, IncomeTaxBasicRate, IncomeTaxHigherRate, SavingsChargeableAddHRate, SavingsChargeableHigherRate, SavingsChargeableLowerRate, SavingsTaxAddHighRate, SavingsTaxHigherRate, SavingsTaxLowerRate, TaxOnCegAhr, TaxOnPayScottishIntermediateRate, TaxOnPayScottishStarterRate, TaxOnRedundancyAhr, TaxOnRedundancyBr, TaxOnRedundancyHr, TaxOnRedundancySir, TaxOnRedundancySsr, TaxablePayScottishIntermediateRate, TaxablePayScottishStarterRate, TaxableRedundancyAhr, TaxableRedundancyBr, TaxableRedundancyHr, TaxableRedundancySir, TaxableRedundancySsr}
import sa.models.TaxRate._
import sa.models.TaxSummaryLiability
import sa.services.TaxRateService

class ATSCalculationsScottish2024(val summaryData: TaxSummaryLiability, val taxRateService: TaxRateService)
    extends ATSCalculations2024 {

  override def scottishIncomeTax: Amount = Amount.empty("scottishIncomeTaxScottish2024")

  override def savingsRate: Amount = Amount.empty("savingsRateScottish2024")

  override def savingsRateAmount: Amount = Amount.empty("savingsRateAmountScottish2024")

  override def basicRateIncomeTaxAmount: Amount = Amount.empty("basicRateIncomeTaxAmountScottish2024")

  override def higherRateIncomeTaxAmount: Amount = Amount.empty("higherRateIncomeTaxAmountScottish2024")

  override def additionalRateIncomeTaxAmount: Amount = Amount.empty("additionalRateIncomeTaxAmountScottish2024")

  override def basicRateIncomeTax: Amount = Amount.empty("basicRateIncomeTaxScottish2024")

  override def higherRateIncomeTax: Amount = Amount.empty("higherRateIncomeTaxScottish2024")

  override def additionalRateIncomeTax: Amount = Amount.empty("additionalRateIncomeTaxScottish2024")

  override def scottishStarterRateTax: Amount =
    getWithDefaultAmount(TaxOnPayScottishStarterRate) + get(TaxOnRedundancySsr) + includePensionTaxForRate(
      taxRateService.taxRates.getOrElse(ScottishStarterRate, Rate.empty)
    )

  override def scottishBasicRateTax: Amount =
    getWithDefaultAmount(IncomeTaxBasicRate) + get(TaxOnRedundancyBr) + includePensionTaxForRate(
      taxRateService.taxRates.getOrElse(ScottishBasicRate, Rate.empty)
    )

  override def scottishIntermediateRateTax: Amount =
    getWithDefaultAmount(TaxOnPayScottishIntermediateRate) + get(TaxOnRedundancySir) + includePensionTaxForRate(
      taxRateService.taxRates.getOrElse(ScottishIntermediateRate, Rate.empty)
    )

  override def scottishHigherRateTax: Amount =
    getWithDefaultAmount(IncomeTaxHigherRate) + get(TaxOnRedundancyHr) + includePensionTaxForRate(
      taxRateService.taxRates.getOrElse(ScottishHigherRate, Rate.empty)
    )

  override def scottishAdditionalRateTax: Amount =
    getWithDefaultAmount(IncomeTaxAddHighRate) + get(TaxOnRedundancyAhr) + includePensionTaxForRate(
      taxRateService.taxRates.getOrElse(ScottishAdditionalRate, Rate.empty)
    )

  override def scottishStarterRateIncome: Amount =
    getWithDefaultAmount(TaxablePayScottishStarterRate) + get(TaxableRedundancySsr) + includePensionIncomeForRate(
      taxRateService.taxRates.getOrElse(ScottishStarterRate, Rate.empty)
    )

  override def scottishBasicRateIncome: Amount = // LS12.6: Scottish Basic rate Income tax x 20%
    getWithDefaultAmount(IncomeChargeableBasicRate) + get(TaxableRedundancyBr) + includePensionIncomeForRate(
      taxRateService.taxRates.getOrElse(ScottishBasicRate, Rate.empty)
    )

  override def scottishIntermediateRateIncome: Amount =
    getWithDefaultAmount(TaxablePayScottishIntermediateRate) + get(TaxableRedundancySir) + includePensionIncomeForRate(
      taxRateService.taxRates.getOrElse(ScottishIntermediateRate, Rate.empty)
    )

  override def scottishHigherRateIncome: Amount =
    getWithDefaultAmount(IncomeChargeableHigherRate) + get(TaxableRedundancyHr) + includePensionIncomeForRate(
      taxRateService.taxRates.getOrElse(ScottishHigherRate, Rate.empty)
    )

  override def scottishAdditionalRateIncome: Amount = // LS12.9: Top rate
    getWithDefaultAmount(IncomeChargeableAddHRate) + get(TaxableRedundancyAhr) + includePensionIncomeForRate(
      taxRateService.taxRates.getOrElse(ScottishAdditionalRate, Rate.empty)
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
