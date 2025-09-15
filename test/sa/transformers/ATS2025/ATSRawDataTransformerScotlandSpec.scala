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

package sa.transformers.ATS2025

import common.models.LiabilityKey.{AdditionalRateIncomeTax, AdditionalRateIncomeTaxAmount, BasicRateIncomeTax, BasicRateIncomeTaxAmount, HigherRateIncomeTax, HigherRateIncomeTaxAmount, LessTaxFreeAmount, NicsAndTaxPerCurrencyUnit, PayCgTaxOn, SavingsAdditionalIncome, SavingsAdditionalRateTax, SavingsHigherIncome, SavingsHigherRateTax, SavingsLowerIncome, SavingsLowerRateTax, ScottishAdditionalIncome, ScottishAdditionalRateTax, ScottishAdvancedIncome, ScottishBasicIncome, ScottishBasicRateTax, ScottishHigherIncome, ScottishHigherRateTax, ScottishIncomeTax, ScottishIntermediateIncome, ScottishIntermediateRateTax, ScottishStarterIncome, ScottishStarterRateTax, ScottishTotalTax, StartingRateForSavings, StartingRateForSavingsAmount, TotalIncomeTax, TotalIncomeTaxAndNics, YourTotalTax}
import common.models.RateKey.{Additional, IncomeAdditional, IncomeBasic, IncomeHigher, Ordinary, Savings, SavingsAdditionalRate, SavingsHigherRate, SavingsLowerRate, ScottishAdditionalRate, ScottishAdvancedRate, ScottishBasicRate, ScottishHigherRate, ScottishIntermediateRate, ScottishStarterRate, Upper}
import common.models.{Amount, ApiRate, LiabilityKey}
import common.utils.BaseSpec
import sa.utils.ATSRawDataTransformerBehaviours

class ATSRawDataTransformerScotlandSpec extends BaseSpec with ATSRawDataTransformerBehaviours {
  s"atsDataDTO for Scotland 2025" must {
    "use the correct tax rates" in new ATSRawDataTransformerTestFixtureScotland {
      transformedData.income_tax.flatMap(_.rates).map(_.toSet) mustBe Some(
        Set(
          Additional               -> ApiRate("39.35%"),
          Ordinary                 -> ApiRate("8.75%"),
          ScottishBasicRate        -> ApiRate("20%"),
          SavingsLowerRate         -> ApiRate("20%"),
          SavingsHigherRate        -> ApiRate("40%"),
          ScottishAdvancedRate     -> ApiRate("45%"),
          ScottishAdditionalRate   -> ApiRate("48%"),
          IncomeHigher             -> ApiRate("40%"),
          ScottishIntermediateRate -> ApiRate("21%"),
          SavingsAdditionalRate    -> ApiRate("45%"),
          IncomeAdditional         -> ApiRate("45%"),
          ScottishHigherRate       -> ApiRate("42%"),
          ScottishStarterRate      -> ApiRate("19%"),
          Savings                  -> ApiRate("0%"),
          Upper                    -> ApiRate("33.75%"),
          IncomeBasic              -> ApiRate("20%")
        )
      )
    }

    behave like atsRawDataTransformerWithTotalTaxLiabilityChecks(
      expTotalTaxLiabilityValue = BigDecimal(12322.58),
      testFixture = new ATSRawDataTransformerTestFixtureScotland {}
    )

    behave like atsRawDataTransformerWithCalculations(
      description = "NOT using tax excluded/ tax on non-excluded income when > amount",
      testFixture = new ATSRawDataTransformerTestFixtureScotland {}
    )

    behave like atsRawDataTransformerWithCalculations(
      description = "using tax excluded/ tax on non-excluded income when < amount",
      testFixture = new ATSRawDataTransformerTestFixtureScotland {
        override protected def tliSlpAtsData: Map[String, BigDecimal] = super.tliSlpAtsData ++ Map(
          "taxExcluded"           -> BigDecimal(630.00),
          "taxOnNonExcludedInc"   -> BigDecimal(640.00),
          "ctnDividendTaxLowRate" -> BigDecimal(9000.00)
        ).map(item => item._1 -> item._2.setScale(2))

        override def expectedResultIncomeTax: Map[LiabilityKey, Amount] = super.expectedResultIncomeTax ++ Map(
          TotalIncomeTax -> expTotalIncomeTax
        )

        override protected def expTotalIncomeTax: Amount =
          calcExp(
            "taxExcluded",
            "taxOnNonExcludedInc"
          ) + expScottishTotalTax + expSavingsTotalTax

        override def expectedResultSummaryData: Map[LiabilityKey, Amount] =
          super.expectedResultSummaryData ++ Map(
            TotalIncomeTaxAndNics     -> (expEmployeeNicAmount + expTotalIncomeTax),
            YourTotalTax              -> (expEmployeeNicAmount + expTotalIncomeTax + expTotalCgTax.max(0)),
            TotalIncomeTax            -> expTotalIncomeTax,
            NicsAndTaxPerCurrencyUnit -> (expEmployeeNicAmount + expTotalIncomeTax)
              .divideWithPrecision(expTotalIncomeBeforeTax, 4)
          )
      }
    )

    behave like atsRawDataTransformerWithCalculations(
      description = "subtracting capital gains exempt amount when < taxable gains",
      testFixture = new ATSRawDataTransformerTestFixtureScotland {
        override protected def tliSlpAtsData: Map[String, BigDecimal] = super.tliSlpAtsData ++ Map(
          "atsCgAnnualExemptAmt" -> BigDecimal(100.0)
        ).map(item => item._1 -> item._2.setScale(2))

        override def expectedResultCapitalGainsData: Map[LiabilityKey, Amount] =
          super.expectedResultCapitalGainsData ++ Map(
            PayCgTaxOn        -> (expTaxableGains - calcExp("atsCgAnnualExemptAmt")),
            LessTaxFreeAmount -> calcExp("atsCgAnnualExemptAmt")
          )
      }
    )

    atsRawDataTransformerWithCalculations(
      description = "using default amounts where applicable",
      testFixture = new ATSRawDataTransformerTestFixtureScotland {
        override def tliSlpAtsData: Map[String, BigDecimal] =
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
            "ctnRelTaxAcctFor",
            "taxOnPaySSR",
            "ctnIncomeTaxBasicRate",
            "taxOnPaySIR",
            "ctnIncomeTaxHigherRate",
            "ctnIncomeTaxAddHighRate",
            "taxablePaySSR",
            "taxablePaySIR",
            "ctnSavingsTaxLowerRate",
            "ctnSavingsTaxHigherRate",
            "ctnSavingsChgbleLowerRate",
            "ctnSavingsChgbleHigherRate",
            "ctnSavingsChgbleAddHRate"
          )

        override def saPayeNicDetails: Map[String, BigDecimal] = super.saPayeNicDetails ++ Map(
          "ctnIncomeChgbleBasicRate"   -> BigDecimal(17421.00),
          "ctnIncomeChgbleHigherRate"  -> BigDecimal(343.00),
          "ctnIncomeChgbleAddHRate"    -> BigDecimal(382.00),
          "taxOnNonExcludedInc"        -> BigDecimal(642.00),
          "alimony"                    -> BigDecimal(752.00),
          "reliefForFinanceCosts"      -> BigDecimal(502.00),
          "lfiRelief"                  -> BigDecimal(792.00),
          "ctnRelTaxAcctFor"           -> BigDecimal(12.00),
          "ctnLowerRateCgtRPCI"        -> BigDecimal(942.00),
          "ctnHigherRateCgtRPCI"       -> BigDecimal(962.00),
          "ctnMarriageAllceInAmt"      -> BigDecimal(992.00),
          "ctnMarriageAllceOutAmt"     -> BigDecimal(1002.00),
          "taxOnPaySSR"                -> BigDecimal(399.43),
          "ctnIncomeTaxBasicRate"      -> BigDecimal(3484.80),
          "taxOnPaySIR"                -> BigDecimal(1439.50),
          "ctnIncomeTaxHigherRate"     -> BigDecimal(361.00),
          "ctnIncomeTaxAddHighRate"    -> BigDecimal(401.00),
          "taxablePaySSR"              -> BigDecimal(2098.00),
          "taxablePaySIR"              -> BigDecimal(6851.00),
          "ctnSavingsTaxLowerRate"     -> BigDecimal(536.60),
          "ctnSavingsTaxHigherRate"    -> BigDecimal(371.00),
          "ctnSavingsChgbleLowerRate"  -> BigDecimal(2679.00),
          "ctnSavingsChgbleHigherRate" -> BigDecimal(351.00),
          "ctnSavingsChgbleAddHRate"   -> BigDecimal(391.00)
        ).map(item => item._1 -> item._2.setScale(2))
      }
    )
  }
}

protected trait ATSRawDataTransformerTestFixtureScotland extends ATSRawDataTransformerTestFixtureBase {
  override protected val incomeTaxStatus: String                  = "0002"
  override def expectedResultIncomeTax: Map[LiabilityKey, Amount] = super.expectedResultIncomeTax ++ Map(
    StartingRateForSavingsAmount  -> calcExp("savingsRateAmountScottish2025:null"),
    SavingsLowerIncome            -> calcExp("ctnSavingsChgbleLowerRate"),
    SavingsLowerRateTax           -> calcExp("ctnSavingsTaxLowerRate"),
    ScottishIncomeTax             -> calcExp("scottishIncomeTaxScottish2025:null"),
    ScottishIntermediateRateTax   -> calcExp("taxOnPaySIR", "ctnTaxOnRedundancySir", "ctnPensionLsumTaxDueAmt:null"),
    ScottishHigherIncome          -> calcExp(
      "ctnIncomeChgbleHigherRate",
      "ctnTaxableRedundancyHr",
      "itfStatePensionLsGrossAmt:null"
    ),
    ScottishStarterRateTax        -> calcExp("taxOnPaySSR", "ctnTaxOnRedundancySsr", "ctnPensionLsumTaxDueAmt:null"),
    StartingRateForSavings        -> calcExp("savingsRateScottish2025:null"),
    AdditionalRateIncomeTax       -> calcExp("additionalRateIncomeTaxScottish2025:null"),
    SavingsAdditionalIncome       -> calcExp("ctnSavingsChgbleAddHRate"),
    SavingsHigherIncome           -> calcExp("ctnSavingsChgbleHigherRate"),
    ScottishAdditionalRateTax     -> calcExp(
      "ctnIncomeTaxAddHighRate",
      "ctnTaxOnRedundancyAhr",
      "ctnPensionLsumTaxDueAmt:null"
    ),
    HigherRateIncomeTax           -> calcExp("higherRateIncomeTaxScottish2025:null"),
    ScottishBasicRateTax          -> calcExp("ctnIncomeTaxBasicRate", "ctnTaxOnRedundancyBr", "ctnPensionLsumTaxDueAmt:null"),
    BasicRateIncomeTaxAmount      -> calcExp("basicRateIncomeTaxAmountScottish2025:null"),
    ScottishAdditionalIncome      -> calcExp(
      "ctnIncomeChgbleAddHRate",
      "ctnTaxableRedundancyAhr",
      "itfStatePensionLsGrossAmt:null"
    ),
    ScottishIntermediateIncome    -> calcExp("taxablePaySIR", "ctnTaxableRedundancySir", "itfStatePensionLsGrossAmt:null"),
    AdditionalRateIncomeTaxAmount -> calcExp("additionalRateIncomeTaxAmountScottish2025:null"),
    ScottishBasicIncome           -> calcExp(
      "ctnIncomeChgbleBasicRate",
      "ctnTaxableRedundancyBr",
      "itfStatePensionLsGrossAmt:null"
    ),
    ScottishTotalTax              -> expScottishTotalTax,
    BasicRateIncomeTax            -> calcExp("basicRateIncomeTaxScottish2025:null"),
    SavingsAdditionalRateTax      -> calcExp("ctnSavingsTaxAddHighRate", "ctnTaxOnCegAhr"),
    HigherRateIncomeTaxAmount     -> calcExp("higherRateIncomeTaxAmountScottish2025:null"),
    TotalIncomeTax                -> expTotalIncomeTax,
    SavingsHigherRateTax          -> calcExp("ctnSavingsTaxHigherRate"),
    ScottishHigherRateTax         -> calcExp("ctnIncomeTaxHigherRate", "ctnTaxOnRedundancyHr", "ctnPensionLsumTaxDueAmt:null"),
    ScottishStarterIncome         -> calcExp("taxablePaySSR", "ctnTaxableRedundancySsr", "itfStatePensionLsGrossAmt:null")
  )

  override def expectedResultSummaryData: Map[LiabilityKey, Amount] = super.expectedResultSummaryData ++ Map(
    TotalIncomeTaxAndNics     -> expTotalIncomeTaxAndNics,
    TotalIncomeTax            -> expTotalIncomeTax,
    NicsAndTaxPerCurrencyUnit -> expTotalAmountTaxAndNics.divideWithPrecision(expTotalIncomeBeforeTax, 4)
  )

  override protected def expTotalIncomeTaxAndNics: Amount =
    expEmployeeNicAmount + expSavingsIncomeTaxDivs + expOtherAdjustmentsIncreasing - expOtherAdjustmentsReducing -
      calcExp("ctnMarriageAllceInAmt") +
      expScottishTotalTax + expSavingsTotalTax

  override protected def expSavingsIncomeTaxDivs: Amount = calcExp(
    "savingsRateAmountScottish2025:null",
    "basicRateIncomeTaxAmountScottish2025:null",
    "higherRateIncomeTaxAmountScottish2025:null",
    "additionalRateIncomeTaxAmountScottish2025:null",
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
      "savingsRateAmountScottish2025:null",
      "basicRateIncomeTaxAmountScottish2025:null",
      "higherRateIncomeTaxAmountScottish2025:null",
      "additionalRateIncomeTaxAmountScottish2025:null",
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
    "taxOnPaySar",
    "ctnIncomeTaxAddHighRate",
    "ctnTaxOnRedundancyAhr",
    "ctnPensionLsumTaxDueAmt:null"
  )

  protected def expSavingsTotalTax: Amount = calcExp(
    "ctnSavingsTaxLowerRate",
    "ctnSavingsTaxHigherRate",
    "ctnSavingsTaxAddHighRate",
    "ctnTaxOnCegAhr"
  )

  override protected def expTotalAmountTaxAndNics: Amount = expEmployeeNicAmount + expTotalIncomeTax
}
