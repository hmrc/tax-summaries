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

package sa.transformers.ATS2022

import common.models.Amount
import sa.models.TaxSummaryLiability
import sa.services.TaxRateService

class ATSCalculationsUK2022(val summaryData: TaxSummaryLiability, val taxRateService: TaxRateService)
    extends ATSCalculations2022 {
  override def scottishIncomeTax: Amount = Amount.empty("scottishIncomeTaxUK2022")
}
