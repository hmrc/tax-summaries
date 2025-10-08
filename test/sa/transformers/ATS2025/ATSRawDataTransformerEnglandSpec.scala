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

import common.models.LiabilityKey.{LessTaxFreeAmount, NicsAndTaxPerCurrencyUnit, PayCgTaxOn, TotalIncomeTax, TotalIncomeTaxAndNics, YourTotalTax}
import common.models.RateKey.{Additional, IncomeAdditional, IncomeBasic, IncomeHigher, Ordinary, Savings, SavingsAdditionalRate, SavingsHigherRate, SavingsLowerRate, ScottishAdditionalRate, ScottishAdvancedRate, ScottishBasicRate, ScottishHigherRate, ScottishIntermediateRate, ScottishStarterRate, Upper}
import common.models.{Amount, ApiRate, LiabilityKey}
import common.utils.BaseSpec
import sa.utils.ATSRawDataTransformerBehaviours

class ATSRawDataTransformerEnglandSpec extends BaseSpec with ATSRawDataTransformerBehaviours {
  s"atsDataDTO for England 2025" must {
    "use the correct tax rates" in new ATSRawDataTransformerTestFixtureEngland {
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
      expTotalTaxLiabilityValue = BigDecimal(16955.65),
      testFixture = new ATSRawDataTransformerTestFixtureEngland {}
    )

    behave like atsRawDataTransformerWithCalculations(
      description = "NOT using tax excluded/ tax on non-excluded income when > amount",
      testFixture = new ATSRawDataTransformerTestFixtureEngland {}
    )

    behave like atsRawDataTransformerWithCalculations(
      description = "using tax excluded/ tax on non-excluded income when < amount",
      testFixture = new ATSRawDataTransformerTestFixtureEngland {
        override def tliSlpAtsData: Map[String, BigDecimal] = super.tliSlpAtsData ++ Map(
          "taxExcluded"         -> BigDecimal(630.00),
          "taxOnNonExcludedInc" -> BigDecimal(640.00)
        ).map(item => item._1 -> item._2.setScale(2))

        override def expectedResultIncomeTax: Map[LiabilityKey, Amount] = super.expectedResultIncomeTax ++ Map(
          TotalIncomeTax -> calcExp("taxExcluded", "taxOnNonExcludedInc")
        )

        override def expectedResultSummaryData: Map[LiabilityKey, Amount] =
          super.expectedResultSummaryData ++ Map(
            TotalIncomeTaxAndNics     -> (expEmployeeNicAmount + calcExp(
              "taxExcluded",
              "taxOnNonExcludedInc"
            )),
            YourTotalTax              -> (expEmployeeNicAmount +
              calcExp(
                "taxExcluded",
                "taxOnNonExcludedInc"
              ) +
              expTotalCgTax.max(0)),
            TotalIncomeTax            -> calcExp("taxExcluded", "taxOnNonExcludedInc"),
            NicsAndTaxPerCurrencyUnit -> expTotalAmountTaxAndNics.divideWithPrecision(expTotalIncomeBeforeTax, 4)
          )
      }
    )

    behave like atsRawDataTransformerWithCalculations(
      description = "subtracting capital gains exempt amount when < taxable gains",
      testFixture = new ATSRawDataTransformerTestFixtureEngland {
        override def tliSlpAtsData: Map[String, BigDecimal] = super.tliSlpAtsData ++ Map(
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
      testFixture = new ATSRawDataTransformerTestFixtureEngland {
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
            "ctnRelTaxAcctFor"
          )

        override def saPayeNicDetails: Map[String, BigDecimal] = super.saPayeNicDetails ++ Map(
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
      }
    )
  }
}

protected trait ATSRawDataTransformerTestFixtureEngland extends ATSRawDataTransformerTestFixtureBase {
  override protected val incomeTaxStatus: String = "0001"
}
