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
import models.RateKey._
import models._

class ATSRawDataTransformer2023WelshSpec extends ATSRawDataTransformer2023Spec {

  override protected val incomeTaxStatus: String = "0003"

  private def welshRate: Double = 0.1d

  override protected def expectedResultIncomeTax: Map[LiabilityKey, Amount] = super.expectedResultIncomeTax ++
    Map(
      ScottishIncomeTax -> calcExp("scottishIncomeTaxWelsh2023:null"),
      WelshIncomeTax    -> calcExp(
        "ctnIncomeChgbleBasicRate",
        "ctnTaxableRedundancyBr",
        "ctnIncomeChgbleHigherRate",
        "ctnTaxableRedundancyHr",
        "ctnIncomeChgbleAddHRate",
        "ctnTaxableRedundancyAhr"
      ) * welshRate
    )

  s"atsDataDTO for incomeTaxStatus (i.e. country) $incomeTaxStatus and tax year $taxYear" must {
    "have the correct tax year from json" in {
      transformedData.taxYear mustBe taxYear
    }

    "use the correct tax rates" in {
      transformedData.income_tax.flatMap(_.rates).map(_.toSet) mustBe Some(
        Set(
          Additional               -> ApiRate("39.35%"),
          Ordinary                 -> ApiRate("8.75%"),
          ScottishBasicRate        -> ApiRate("20%"),
          SavingsLowerRate         -> ApiRate("20%"),
          SavingsHigherRate        -> ApiRate("40%"),
          ScottishAdditionalRate   -> ApiRate("46%"),
          IncomeHigher             -> ApiRate("40%"),
          ScottishIntermediateRate -> ApiRate("21%"),
          SavingsAdditionalRate    -> ApiRate("45%"),
          IncomeAdditional         -> ApiRate("45%"),
          ScottishHigherRate       -> ApiRate("41%"),
          ScottishStarterRate      -> ApiRate("19%"),
          Savings                  -> ApiRate("0%"),
          Upper                    -> ApiRate("33.75%"),
          IncomeBasic              -> ApiRate("20%")
        )
      )
    }

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

  override protected def expTotalAmountTaxAndNics: Amount = expEmployeeNicAmount + calcExp(
    "taxExcluded",
    "taxOnNonExcludedInc"
  )
}
