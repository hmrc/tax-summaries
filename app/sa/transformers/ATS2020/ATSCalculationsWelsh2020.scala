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

package sa.transformers.ATS2020

import common.models.Amount
import sa.models.ODSLiabilities.ODSLiabilities.{IncomeChargeableAddHRate, IncomeChargeableBasicRate, IncomeChargeableHigherRate}
import sa.models.TaxSummaryLiability
import sa.services.TaxRateService
import sa.transformers.ATSCalculations

class ATSCalculationsWelsh2020(val summaryData: TaxSummaryLiability, val taxRates: TaxRateService)
    extends ATSCalculations {
  override def welshIncomeTax: Amount = {
    val welshRate = 0.1
    (
      getWithDefaultAmount(IncomeChargeableBasicRate) +
        getWithDefaultAmount(IncomeChargeableHigherRate) +
        getWithDefaultAmount(IncomeChargeableAddHRate)
    ) * welshRate
  }

  override def scottishIncomeTax: Amount = Amount.empty("scottishIncomeTaxWelsh2020")
  override def savingsRate: Amount       = Amount.empty("savingsRateWelsh2020")
  override def savingsRateAmount: Amount = Amount.empty("savingsRateAmountWelsh2020")
}