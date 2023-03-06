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

package transformers.Welsh

import models.ODSLiabilities.ODSLiabilities._
import models.{Amount, TaxSummaryLiability}
import services.TaxRateService
import transformers.ATSCalculations2022

class ATSCalculationsWelsh2022(val summaryData: TaxSummaryLiability, val taxRates: TaxRateService)
    extends ATSCalculations2022 {
  override def welshIncomeTax: Amount = {
    val welshRate = 0.1
    (
      getWithDefaultAmount(IncomeChargeableBasicRate) +
        get(TaxableRedundancyBr) +
        getWithDefaultAmount(IncomeChargeableHigherRate) +
        get(TaxableRedundancyHr) +
        getWithDefaultAmount(IncomeChargeableAddHRate)
        + get(TaxableRedundancyAhr)
    ) * welshRate
  }

  override def scottishIncomeTax: Amount = Amount.empty("scottishIncomeTaxWelsh2022")
}
