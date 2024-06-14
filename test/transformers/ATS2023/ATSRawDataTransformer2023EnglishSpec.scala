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

    behave like atsRawDataTransformerWithCalculations(
      description = "main",
      transformedData = transformedData,
      expResultIncomeTax = expectedResultIncomeTax,
      expResultIncomeData = expectedResultIncomeData,
      expResultCapitalGainsData = expectedResultCGData,
      expResultAllowanceData = expectedResultAllowanceData,
      expResultSummaryData = expectedResultSummaryData
    )

    behave like atsRawDataTransformerWithCalculations(
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

class ATSRawDataTransformer2023EnglishDefaultAmountsSpec extends ATSRawDataTransformer2023Spec {

  override protected val incomeTaxStatus: String = "0001"

  override protected def tliSlpAtsData: Map[String, BigDecimal] =
    super.tliSlpAtsData -- Seq(
      "ctnLowerRateCgtRPCI",
      "ctnHigherRateCgtRPCI",
      "ctnMarriageAllceOutAmt",
      "reliefForFinanceCosts",
      "lfiRelief",
      "alimony",
      "ctnMarriageAllceInAmt",
      "ctnIncomeChgbleBasicRate",
      "ctnIncomeChgbleHigherRate",
      "ctnIncomeChgbleAddHRate",
      "taxOnNonExcludedInc",
      "ctnRelTaxAcctFor"
    )

  override protected def saPayeNicDetails: Map[String, BigDecimal] = super.saPayeNicDetails ++ Map(
    "ctnIncomeChgbleBasicRate"  -> BigDecimal(17420.00),
    "ctnIncomeChgbleHigherRate" -> BigDecimal(342.00),
    "ctnIncomeChgbleAddHRate"   -> BigDecimal(381.00),
    "taxOnNonExcludedInc"       -> BigDecimal(641.00),
    "alimony"                   -> BigDecimal(751.00),
    "reliefForFinanceCosts"     -> BigDecimal(501.00),
    "lfiRelief"                 -> BigDecimal(791.00),
    "ctnRelTaxAcctFor"          -> BigDecimal(11.00),
    "ctnLowerRateCgtRPCI"       -> BigDecimal(941.00),
    "ctnHigherRateCgtRPCI"      -> BigDecimal(961.00),
    "ctnMarriageAllceInAmt"     -> BigDecimal(991.00),
    "ctnMarriageAllceOutAmt"    -> BigDecimal(1001.00)
  ).map(item => item._1 -> item._2.setScale(2))

  s"atsDataDTO for incomeTaxStatus (i.e. country) $incomeTaxStatus and tax year $taxYear" must {
    behave like atsRawDataTransformerWithCalculations(
      description = "default amounts",
      transformedData = transformedData,
      expResultIncomeTax = expectedResultIncomeTax,
      expResultIncomeData = expectedResultIncomeData,
      expResultCapitalGainsData = expectedResultCGData,
      expResultAllowanceData = expectedResultAllowanceData,
      expResultSummaryData = expectedResultSummaryData
    )
  }
}
