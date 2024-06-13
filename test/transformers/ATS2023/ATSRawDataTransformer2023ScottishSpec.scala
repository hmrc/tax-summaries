/*
 * Copyright 2024 HM Revenue & Customs
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
import models._

class ATSRawDataTransformer2023ScottishSpec extends ATSRawDataTransformer2023Spec {
  override protected val incomeTaxStatus: String = "0002"

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
      expResultSummaryData = expectedResultSummaryDataNonExcluded ++ Map(
        NicsAndTaxPerCurrencyUnit -> expNicsAndTaxPerCurrencyUnitExclNonExclMin
      )
    )
  }

  override protected def expectedResultIncomeTax: Map[LiabilityKey, Amount] = super.expectedResultIncomeTax ++ Map(
    StartingRateForSavingsAmount  -> calcExp("savingsRateAmountScottish2023:null"),
    SavingsLowerIncome            -> calcExp("ctnSavingsChgbleLowerRate"),
    SavingsLowerRateTax           -> calcExp("ctnSavingsTaxLowerRate"),
    ScottishIncomeTax             -> calcExp("scottishIncomeTaxScottish2023:null"),
    ScottishIntermediateRateTax   -> calcExp("taxOnPaySIR", "ctnTaxOnRedundancySir", "ctnPensionLsumTaxDueAmt:null"),
    ScottishHigherIncome          -> calcExp(
      "ctnIncomeChgbleHigherRate",
      "ctnTaxableRedundancyHr",
      "itfStatePensionLsGrossAmt:null"
    ),
    ScottishStarterRateTax        -> calcExp("taxOnPaySSR", "ctnTaxOnRedundancySsr", "ctnPensionLsumTaxDueAmt:null"),
    StartingRateForSavings        -> calcExp("savingsRateScottish2023:null"),
    AdditionalRateIncomeTax       -> calcExp("additionalRateIncomeTaxScottish2023:null"),
    SavingsAdditionalIncome       -> calcExp("ctnSavingsChgbleAddHRate"),
    SavingsHigherIncome           -> calcExp("ctnSavingsChgbleHigherRate"),
    ScottishAdditionalRateTax     -> calcExp(
      "ctnIncomeTaxAddHighRate",
      "ctnTaxOnRedundancyAhr",
      "ctnPensionLsumTaxDueAmt:null"
    ),
    HigherRateIncomeTax           -> calcExp("higherRateIncomeTaxScottish2023:null"),
    ScottishBasicRateTax          -> calcExp("ctnIncomeTaxBasicRate", "ctnTaxOnRedundancyBr", "ctnPensionLsumTaxDueAmt:null"),
    BasicRateIncomeTaxAmount      -> calcExp("basicRateIncomeTaxAmountScottish2023:null"),
    ScottishAdditionalIncome      -> calcExp(
      "ctnIncomeChgbleAddHRate",
      "ctnTaxableRedundancyAhr",
      "itfStatePensionLsGrossAmt:null"
    ),
    ScottishIntermediateIncome    -> calcExp("taxablePaySIR", "ctnTaxableRedundancySir", "itfStatePensionLsGrossAmt:null"),
    AdditionalRateIncomeTaxAmount -> calcExp("additionalRateIncomeTaxAmountScottish2023:null"),
    ScottishBasicIncome           -> calcExp(
      "ctnIncomeChgbleBasicRate",
      "ctnTaxableRedundancyBr",
      "itfStatePensionLsGrossAmt:null"
    ),
    ScottishTotalTax              -> expScottishTotalTax,
    BasicRateIncomeTax            -> calcExp("basicRateIncomeTaxScottish2023:null"),
    SavingsAdditionalRateTax      -> calcExp("ctnSavingsTaxAddHighRate", "ctnTaxOnCegAhr"),
    HigherRateIncomeTaxAmount     -> calcExp("higherRateIncomeTaxAmountScottish2023:null"),
    TotalIncomeTax                -> expTotalIncomeTax,
    SavingsHigherRateTax          -> calcExp("ctnSavingsTaxHigherRate"),
    ScottishHigherRateTax         -> calcExp("ctnIncomeTaxHigherRate", "ctnTaxOnRedundancyHr", "ctnPensionLsumTaxDueAmt:null"),
    ScottishStarterIncome         -> calcExp("taxablePaySSR", "ctnTaxableRedundancySsr", "itfStatePensionLsGrossAmt:null")
  )

  override protected def expectedResultSummaryData: Map[LiabilityKey, Amount] = super.expectedResultSummaryData ++ Map(
    TotalIncomeTaxAndNics -> expTotalIncomeTaxAndNics,
    TotalIncomeTax        -> expTotalIncomeTax
  )

  override protected def expTotalIncomeTaxAndNics: Amount =
    expEmployeeNicAmount + expSavingsIncomeTaxDivs + expOtherAdjustmentsIncreasing - expOtherAdjustmentsReducing -
      calcExp("ctnMarriageAllceInAmt") +
      expScottishTotalTax + expSavingsTotalTax

  override protected def expSavingsIncomeTaxDivs: Amount = calcExp(
    "savingsRateAmountScottish2023:null",
    "basicRateIncomeTaxAmountScottish2023:null",
    "higherRateIncomeTaxAmountScottish2023:null",
    "additionalRateIncomeTaxAmountScottish2023:null",
    "ctnDividendTaxLowRate",
    "ctnDividendTaxHighRate",
    "ctnDividendTaxAddHighRate"
  )

  override protected def expYourTotalTax: Amount =
    expEmployeeNicAmount + expSavingsIncomeTaxDivs + expOtherAdjustmentsIncreasing - expOtherAdjustmentsReducing - calcExp(
      "ctnMarriageAllceInAmt"
    ) + expScottishTotalTax + expSavingsTotalTax + expTotalCgTax.max(0)

  override protected def expTotalIncomeTax: Amount =
    ((calcExp(
      "savingsRateAmountScottish2023:null",
      "basicRateIncomeTaxAmountScottish2023:null",
      "higherRateIncomeTaxAmountScottish2023:null",
      "additionalRateIncomeTaxAmountScottish2023:null",
      "ctnDividendTaxLowRate",
      "ctnDividendTaxHighRate",
      "ctnDividendTaxAddHighRate"
    ) + expOtherAdjustmentsIncreasing - expOtherAdjustmentsReducing) - calcExp(
      "ctnMarriageAllceInAmt"
    )) + expScottishTotalTax + expSavingsTotalTax

  override protected def expScottishTotalTax: Amount = calcExp(
    "taxOnPaySSR",
    "ctnTaxOnRedundancySsr",
    "ctnPensionLsumTaxDueAmt:null",
    "ctnIncomeTaxBasicRate",
    "ctnTaxOnRedundancyBr",
    "ctnPensionLsumTaxDueAmt:null",
    "taxOnPaySIR",
    "ctnTaxOnRedundancySir",
    "ctnPensionLsumTaxDueAmt:null",
    "ctnIncomeTaxHigherRate",
    "ctnTaxOnRedundancyHr",
    "ctnPensionLsumTaxDueAmt:null",
    "ctnIncomeTaxAddHighRate",
    "ctnTaxOnRedundancyAhr",
    "ctnPensionLsumTaxDueAmt:null"
  )

  private def expSavingsTotalTax: Amount = calcExp(
    "ctnSavingsTaxLowerRate",
    "ctnSavingsTaxHigherRate",
    "ctnSavingsTaxAddHighRate",
    "ctnTaxOnCegAhr"
  )

  override protected def expTotalAmountTaxAndNics: Amount = expEmployeeNicAmount + expTotalIncomeTax
}
