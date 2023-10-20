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

import models.ODSLiabilities.ODSLiabilities._
import models.{Amount, TaxSummaryLiability}
import services.TaxRateService
import transformers.ATSCalculations2023

class ATSCalculationsScottish2023(val summaryData: TaxSummaryLiability, val taxRates: TaxRateService)
    extends ATSCalculations2023 {

  override def scottishIncomeTax: Amount = Amount.empty("scottishIncomeTaxScottish2023")

  override def savingsRate: Amount = Amount.empty("savingsRateScottish2023")

  override def savingsRateAmount: Amount = Amount.empty("savingsRateAmountScottish2023")

  override def basicRateIncomeTaxAmount: Amount = Amount.empty("basicRateIncomeTaxAmountScottish2023")

  override def higherRateIncomeTaxAmount: Amount = Amount.empty("higherRateIncomeTaxAmountScottish2023")

  override def additionalRateIncomeTaxAmount: Amount = Amount.empty("additionalRateIncomeTaxAmountScottish2023")

  override def basicRateIncomeTax: Amount = Amount.empty("basicRateIncomeTaxScottish2023")

  override def higherRateIncomeTax: Amount = Amount.empty("higherRateIncomeTaxScottish2023")

  override def additionalRateIncomeTax: Amount = Amount.empty("additionalRateIncomeTaxScottish2023")

  override def scottishStarterRateTax: Amount =
    getWithDefaultAmount(TaxOnPayScottishStarterRate) + get(TaxOnRedundancySsr) + includePensionTaxForRate(
      taxRates.scottishStarterRate
    )

  override def scottishBasicRateTax: Amount =
    getWithDefaultAmount(IncomeTaxBasicRate) + get(TaxOnRedundancyBr) + includePensionTaxForRate(
      taxRates.scottishBasicRate
    )

  override def scottishIntermediateRateTax: Amount =
    getWithDefaultAmount(TaxOnPayScottishIntermediateRate) + get(TaxOnRedundancySir) + includePensionTaxForRate(
      taxRates.scottishIntermediateRate
    )

  override def scottishHigherRateTax: Amount =
    getWithDefaultAmount(IncomeTaxHigherRate) + get(TaxOnRedundancyHr) + includePensionTaxForRate(
      taxRates.scottishHigherRate
    )

  override def scottishAdditionalRateTax: Amount =
    getWithDefaultAmount(IncomeTaxAddHighRate) + get(TaxOnRedundancyAhr) + includePensionTaxForRate(
      taxRates.scottishAdditionalRate
    )

  override def scottishStarterRateIncome: Amount =
    getWithDefaultAmount(TaxablePayScottishStarterRate) + get(TaxableRedundancySsr) + includePensionIncomeForRate(
      taxRates.scottishStarterRate
    )

  override def scottishBasicRateIncome: Amount = // LS12.6: Scottish Basic rate Income tax x 20%
    getWithDefaultAmount(IncomeChargeableBasicRate) + get(TaxableRedundancyBr) + includePensionIncomeForRate(
      taxRates.scottishBasicRate
    )

  override def scottishIntermediateRateIncome: Amount =
    getWithDefaultAmount(TaxablePayScottishIntermediateRate) + get(TaxableRedundancySir) + includePensionIncomeForRate(
      taxRates.scottishIntermediateRate
    )

  override def scottishHigherRateIncome: Amount =
    getWithDefaultAmount(IncomeChargeableHigherRate) + get(TaxableRedundancyHr) + includePensionIncomeForRate(
      taxRates.scottishHigherRate
    )

  override def scottishAdditionalRateIncome: Amount = // LS12.9: Top rate
    getWithDefaultAmount(IncomeChargeableAddHRate) + get(TaxableRedundancyAhr) + includePensionIncomeForRate(
      taxRates.scottishAdditionalRate
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
