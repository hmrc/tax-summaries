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

package transformers.ATS2023

import models.LiabilityKey._

class ATSRawDataTransformer2023EnglishSpec extends ATSRawDataTransformer2023Spec {

  override protected val incomeTaxStatus: String = "0001"

  s"atsDataDTO for incomeTaxStatus (i.e. country) $incomeTaxStatus and tax year $taxYear" must {

    behave like atsRawDataTransformerWithTaxRatesAndYear()

    behave like atsRawDataTransformer(
      description = "main",
      transformedData = transformedData,
      expResultIncomeTax = expectedResultIncomeTax,
      expResultIncomeData = expectedResultIncomeData,
      expResultCapitalGainsData = expectedResultCGData,
      expResultAllowanceData = expectedResultAllowanceData,
      expResultSummaryData = expectedResultSummaryData
    )

    behave like atsRawDataTransformer(
      description = "tax excluded/ tax on non-excluded income/gains>cg exempt amount",
      transformedData = doTest(
        buildJsonPayload(tliSlpAtsData = tliSlpAtsDataAlternative)
      ),
      expResultCapitalGainsData = expectedResultCGData ++ Map(
        PayCgTaxOn        -> (expTaxableGains - calcExp(tliSlpAtsDataAlternative, "atsCgAnnualExemptAmt")),
        LessTaxFreeAmount -> calcExp(tliSlpAtsDataAlternative, "atsCgAnnualExemptAmt")
      ),
      expResultSummaryData = expectedResultSummaryDataNonExcluded
    )
  }
}
