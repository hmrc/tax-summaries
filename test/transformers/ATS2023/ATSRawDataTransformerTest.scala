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
import models._
import play.api.libs.json.Json
import services.TaxRateService
import transformers.{ATSCalculations, ATSRawDataTransformer}
import utils.{AtsJsonDataUpdate, BaseSpec, JsonUtil}

class ATSRawDataTransformerTest extends BaseSpec with AtsJsonDataUpdate {
  import ATSRawDataTransformerTest._
  private val taxpayerDetailsJson       = JsonUtil.load("/taxpayer/sa_taxpayer-valid.json")
  private val parsedTaxpayerDetailsJson = Json.parse(taxpayerDetailsJson)
  private val taxYear: Int              = 2023
  private val taxRate                   = new TaxRateService(taxYear, applicationConfig.ratePercentages)

  private val SUT: ATSRawDataTransformer = inject[ATSRawDataTransformer]

  "The total income before tax" must {
    "parse the tax rates transformation (based on utr year:2023 data)" in {
      val parsedJson   = Json.parse(sampleJson)
      val calculations = ATSCalculations.make(parsedJson.as[TaxSummaryLiability], taxRate).get

      val returnValue: AtsMiddleTierData =
        SUT.atsDataDTO(taxRate, calculations, parsedTaxpayerDetailsJson, "", taxYear)

      val parsedYear    = returnValue.taxYear
      val testYear: Int = 2023
      testYear mustEqual parsedYear

      val parsedPayload = returnValue.income_tax.get.payload
      val testPayload   =
        Map(
          StartingRateForSavingsAmount    -> Amount(0.00, "GBP"),
          OtherAdjustmentsReducing        -> Amount(510, "GBP"),
          UpperRate                       -> Amount(0.00, "GBP"),
          SavingsLowerIncome              -> Amount(2678.00, "GBP"),
          SavingsLowerRateTax             -> Amount(535.60, "GBP"),
          ScottishIncomeTax               -> Amount(0.00, "GBP"),
          ScottishIntermediateRateTax     -> Amount(1438.50, "GBP"),
          MarriageAllowanceReceivedAmount -> Amount(0.00, "GBP"),
          OrdinaryRateAmount              -> Amount(806.25, "GBP"),
          ScottishHigherIncome            -> Amount(0.00, "GBP"),
          ScottishStarterRateTax          -> Amount(398.43, "GBP"),
          AdditionalRate                  -> Amount(0.00, "GBP"),
          StartingRateForSavings          -> Amount(0.00, "GBP"),
          AdditionalRateIncomeTax         -> Amount(0.00, "GBP"),
          SavingsAdditionalIncome         -> Amount(0.00, "GBP"),
          SavingsHigherIncome             -> Amount(0.00, "GBP"),
          ScottishAdditionalRateTax       -> Amount(0.00, "GBP"),
          OtherAdjustmentsIncreasing      -> Amount(0.00, "GBP"),
          HigherRateIncomeTax             -> Amount(0.00, "GBP"),
          ScottishBasicRateTax            -> Amount(3483.80, "GBP"),
          BasicRateIncomeTaxAmount        -> Amount(0.00, "GBP"),
          AdditionalRateAmount            -> Amount(0.00, "GBP"),
          WelshIncomeTax                  -> Amount(0.00, "GBP"),
          ScottishAdditionalIncome        -> Amount(0.00, "GBP"),
          ScottishIntermediateIncome      -> Amount(6850.00, "GBP"),
          UpperRateAmount                 -> Amount(0.00, "GBP"),
          AdditionalRateIncomeTaxAmount   -> Amount(0.00, "GBP"),
          ScottishBasicIncome             -> Amount(17419.00, "GBP"),
          ScottishTotalTax                -> Amount(5320.73, "GBP"),
          BasicRateIncomeTax              -> Amount(0.00, "GBP"),
          SavingsAdditionalRateTax        -> Amount(0.00, "GBP"),
          HigherRateIncomeTaxAmount       -> Amount(0.00, "GBP"),
          TotalIncomeTax                  -> Amount(6152.58, "GBP"),
          SavingsHigherRateTax            -> Amount(0.00, "GBP"),
          OrdinaryRate                    -> Amount(10750.00, "GBP"),
          ScottishHigherRateTax           -> Amount(0.00, "GBP"),
          ScottishStarterIncome           -> Amount(2097.00, "GBP")
        )

      parsedPayload
        .map(_.map(x => x._1 -> x._2.amount) must contain allElementsOf testPayload.map(x => x._1 -> x._2.amount))
        .getOrElse(fail("No calculation returned"))

      val parsedRates = returnValue.income_tax.get.rates.get
      val testRates   =
        Map(
          "starting_rate_for_savings_rate"  -> ApiRate("0%"),
          "basic_rate_income_tax_rate"      -> ApiRate("20%"),
          "higher_rate_income_tax_rate"     -> ApiRate("40%"),
          "additional_rate_income_tax_rate" -> ApiRate("45%"),
          "ordinary_rate_tax_rate"          -> ApiRate("8.75%"), // Rate.Ordinary -> dividendsOrdinaryRate
          "upper_rate_rate"                 -> ApiRate("33.75%"), // Rate.Upper -> dividendUpperRateRate
          "additional_rate_rate"            -> ApiRate("39.35%"), // Rate.Additional -> dividendAdditionalRate
          "scottish_starter_rate"           -> ApiRate("19%"),
          "scottish_basic_rate"             -> ApiRate("20%"),
          "scottish_intermediate_rate"      -> ApiRate("21%"),
          "scottish_higher_rate"            -> ApiRate("41%"),
          "scottish_additional_rate"        -> ApiRate("46%"),
          "savings_lower_rate"              -> ApiRate("20%"),
          "savings_higher_rate"             -> ApiRate("40%"),
          "savings_additional_rate"         -> ApiRate("45%")
        )

      testRates mustEqual parsedRates.map { case (k, v) => (k.apiValue, v) }
    }
  }
}

object ATSRawDataTransformerTest {
  private val sampleJson =
    """{
      |  "taxYear":2023,
      |  "saPayeNicDetails": {
      |    "employeeClass1Nic": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "employeeClass2Nic": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "employerNic": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    }
      |  },
      |  "tliSlpAtsData": {
      |    "incomeTaxStatus": "0002",
      |    "tliLastUpdated": "2022-09-01",
      |    "ctnPensionLumpSumTaxRate": 0.00,
      |    "ctnEmploymentBenefitsAmt": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnSummaryTotalScheduleD": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnSummaryTotalPartnership": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnSummaryTotalEmployment": {
      |      "amount": 23678.00,
      |      "currency": "GBP"
      |    },
      |    "atsStatePensionAmt": {
      |      "amount": 9783.00,
      |      "currency": "GBP"
      |    },
      |    "atsOtherPensionAmt": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "itfStatePensionLsGrossAmt": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "atsIncBenefitSuppAllowAmt": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "atsJobSeekersAllowanceAmt": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "atsOthStatePenBenefitsAmt": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnSummaryTotShareOptions": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnSummaryTotalUklProperty": {
      |      "amount": 5475.00,
      |      "currency": "GBP"
      |    },
      |    "ctnSummaryTotForeignIncome": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnSummaryTotTrustEstates": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnSummaryTotalOtherIncome": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnSummaryTotForeignSav": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnForeignCegDedn": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnSummaryTotalUkInterest": {
      |      "amount": 3678.00,
      |      "currency": "GBP"
      |    },
      |    "itfCegReceivedAfterTax": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnSummaryTotForeignDiv": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnSummaryTotalUkIntDivs": {
      |      "amount": 12750.00,
      |      "currency": "GBP"
      |    },
      |    "ctn4SumTotLifePolicyGains": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnPersonalAllowance": {
      |      "amount": 12570.00,
      |      "currency": "GBP"
      |    },
      |    "ctnEmploymentExpensesAmt": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnSummaryTotalDedPpr": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnSumTotForeignTaxRelief": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnSumTotLoanRestricted": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnSumTotLossRestricted": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "grossAnnuityPayts": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "itf4GiftsInvCharitiesAmo": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "itfTradeUnionDeathBenefits": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnBpaAllowanceAmt": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "itfBpaAmount": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "grossExcludedIncome": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "class4Nic": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnClass2NicAmt": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnSavingsChgbleStartRate": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnSavingsTaxStartingRate": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnIncomeChgbleBasicRate": {
      |      "amount": 17419.00,
      |      "currency": "GBP"
      |    },
      |    "ctnSavingsChgbleLowerRate": {
      |      "amount": 2678.00,
      |      "currency": "GBP"
      |    },
      |    "ctnIncomeTaxBasicRate": {
      |      "amount": 3483.80,
      |      "currency": "GBP"
      |    },
      |    "ctnSavingsTaxLowerRate": {
      |      "amount": 535.60,
      |      "currency": "GBP"
      |    },
      |    "ctnIncomeChgbleHigherRate": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnSavingsChgbleHigherRate": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnIncomeTaxHigherRate": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnSavingsTaxHigherRate": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnIncomeChgbleAddHRate": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnSavingsChgbleAddHRate": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnIncomeTaxAddHighRate": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnSavingsTaxAddHighRate": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "taxablePaySSR": {
      |      "amount": 2097.00,
      |      "currency": "GBP"
      |    },
      |    "taxOnPaySSR": {
      |      "amount": 398.43,
      |      "currency": "GBP"
      |    },
      |    "taxablePaySIR": {
      |      "amount": 6850.00,
      |      "currency": "GBP"
      |    },
      |    "taxOnPaySIR": {
      |      "amount": 1438.50,
      |      "currency": "GBP"
      |    },
      |    "ctnDividendChgbleLowRate": {
      |      "amount": 10750.00,
      |      "currency": "GBP"
      |    },
      |    "ctnDividendTaxLowRate": {
      |      "amount": 806.25,
      |      "currency": "GBP"
      |    },
      |    "ctnDividendChgbleHighRate": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnDividendTaxHighRate": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnDividendChgbleAddHRate": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnDividendTaxAddHighRate": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnTaxableRedundancySSR": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnTaxOnRedundancySsr": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnTaxableRedundancyBr": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnTaxOnRedundancyBr": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnTaxableRedundancySir": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnTaxOnRedundancySir": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnTaxableRedundancyHr": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnTaxOnRedundancyHr": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnTaxableRedundancyAhr": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnTaxOnRedundancyAhr": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnTaxableCegBr": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnTaxOnCegBr": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnTaxableCegHr": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnTaxOnCegHr": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnTaxableCegAhr": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnTaxOnCegAhr": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "nonDomChargeAmount": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "taxExcluded": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "taxOnNonExcludedInc": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "incomeTaxDue": {
      |      "amount": 6162.58,
      |      "currency": "GBP"
      |    },
      |    "ctn4TaxDueAfterAllceRlf": {
      |      "amount": 6162.58,
      |      "currency": "GBP"
      |    },
      |    "netAnnuityPaytsTaxDue": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnChildBenefitChrgAmt": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnPensionSavingChrgbleAmt": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "atsTaxCharged": {
      |      "amount": 6662.58,
      |      "currency": "GBP"
      |    },
      |    "ctnDeficiencyRelief": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "topSlicingRelief": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnVctSharesReliefAmt": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnEisReliefAmt": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnSeedEisReliefAmt": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnCommInvTrustRelAmt": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "atsSurplusMcaAlimonyRel": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "alimony": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnNotionalTaxCegs": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnNotlTaxOthrSrceAmo": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnFtcrRestricted": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "reliefForFinanceCosts": {
      |      "amount": 500.00,
      |      "currency": "GBP"
      |    },
      |    "lfiRelief": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnRelTaxAcctFor": {
      |      "amount": 10.00,
      |      "currency": "GBP"
      |    },
      |    "ctnTaxCredForDivs": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnQualDistnReliefAmt": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "figTotalTaxCreditRelief": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnNonPayableTaxCredits": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "atsCgTotGainsAfterLosses": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "atsCgGainsAfterLossesAmt": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "cap3AssessableChgeableGain": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "atsCgAnnualExemptAmt": {
      |      "amount": 12300.00,
      |      "currency": "GBP"
      |    },
      |    "ctnCgAtEntrepreneursRate": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnCgDueEntrepreneursRate": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnCgAtLowerRate": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnCgDueLowerRate": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnCgAtHigherRate": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnCgDueHigherRate": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnCGAtLowerRateRPCI": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnLowerRateCgtRPCI": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnCGAtHigherRateRPCI": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnHigherRateCgtRPCI": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "capAdjustmentAmt": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnPensionLsumTaxDueAmt": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnMarriageAllceInAmt": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnMarriageAllceOutAmt": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnSocialInvTaxRelAmt": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnSavingsPartnership": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnDividendsPartnership": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "giftAidTaxReduced": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnTaxableCegSr": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnTaxOnCegSr": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    },
      |    "ctnTaxableRedundancySsr": {
      |      "amount": 0.00,
      |      "currency": "GBP"
      |    }
      |  }
      |}
      |
      |""".stripMargin
}
