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

package sa.calculations.ATS2021

import common.models.{Amount, Rate}
import sa.models.SelfAssessmentAPIResponse

class ATSCalculationsUK2021(val selfAssessmentAPIResponse: SelfAssessmentAPIResponse, val taxRates: Map[String, Rate])
    extends ATSCalculations2021 {
  override def scottishIncomeTax: Amount = Amount.empty("scottishIncomeTaxUK2021")
  override def savingsRate: Amount       = Amount.empty("savingsRateUK2021")
  override def savingsRateAmount: Amount = Amount.empty("savingsRateAmountUK2021")
}
