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

package transformers.ATS2019

import models._
import services._
import transformers.ATSCalculations

class ATSCalculationsUK2019(val summaryData: TaxSummaryLiability, val taxRates: TaxRateService)
    extends ATSCalculations {
  override def scottishIncomeTax: Amount = Amount.empty("scottishIncomeTaxUK2019")
  override def savingsRate: Amount       = Amount.empty("savingsRateUK2019")
  override def savingsRateAmount: Amount = Amount.empty("savingsRateAmountUK2019")
}
