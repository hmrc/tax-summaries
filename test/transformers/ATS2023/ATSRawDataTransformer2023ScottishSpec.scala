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
import play.api.libs.json.Json
import transformers.ATSRawDataTransformer
import utils.{AtsJsonDataUpdate, BaseSpec, JsonUtil}

class ATSRawDataTransformer2023ScottishSpec extends BaseSpec with AtsJsonDataUpdate {
  import ATSRawDataTransformer2023ScottishSpec._
  private val taxpayerDetailsJson       = JsonUtil.load("/taxpayer/sa_taxpayer-valid.json")
  private val parsedTaxpayerDetailsJson = Json.parse(taxpayerDetailsJson)

  private val atsRawDataTransformer: ATSRawDataTransformer = inject[ATSRawDataTransformer]

  private lazy val parsedJson                     = Json.parse(sampleJson)
  private lazy val returnValue: AtsMiddleTierData =
    atsRawDataTransformer.atsDataDTO(parsedJson, parsedTaxpayerDetailsJson, "", taxYear)

  "atsDataDTO for country Scotland and tax year 2023" must {
    "have the correct tax year from json" in {
      returnValue.taxYear mustBe taxYear
    }

    "use the correct tax rates" in {
      returnValue.income_tax.flatMap(_.rates).map(_.toSet) mustBe Some(
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

      //      val parsedPayload: Option[Set[(RateKey, ApiRate)]] =
      //        returnValue.income_tax.flatMap(_.rates).map(_.toSet)
      //      parsedPayload.map { x =>
      //        x.map { y =>
      //          println(s"""\n${y._1} -> ApiRate("${y._2.percent}"),""")
      //        }
      //      }
    }

    Set(
      ("income tax", returnValue.income_tax, expectedResultIncomeTax),
      ("income data", returnValue.income_data, expectedResultIncomeData),
      ("cap gains data", returnValue.capital_gains_data, expectedResultCGData),
      ("allowance data", returnValue.allowance_data, expectedResultAllowanceData),
      ("summary data", returnValue.summary_data, expectedResultSummaryData)
    ).foreach { case (descr, actualOptDataHolder, exp) =>
      s"calculate field values correctly for $descr" when {
        val act = actualOptDataHolder.flatMap(_.payload).getOrElse(Map.empty)

//        act.foreach { y =>
//          println(s"""${y._1} -> amt(BigDecimal(${y._2.amount}), "${y._2.calculus.get}"),""")
//        }

        act.foreach { item =>
          exp.find(_._1 == item._1).map { actItem =>
            s"field ${item._1} calculated" in {
              item._2 mustBe actItem._2
            }
          }
        }

        "check for missing keys made" in {
          exp.keys.toSeq.diff(act.keys.toSeq) mustBe Nil
        }
      }
    }
  }
}

object ATSRawDataTransformer2023ScottishSpec {
  private val taxYear: Int                                     = 2023
  private val sampleJson                                       =
    s"""{
      |  "taxYear":$taxYear,
      |  "saPayeNicDetails": {
      |    "employeeClass1Nic": {
      |      "amount": 100.00,
      |      "currency": "GBP"
      |    },
      |    "employeeClass2Nic": {
      |      "amount": 200.00,
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
  private def amt(value: BigDecimal, calculus: String): Amount = Amount(value, "GBP", Some(calculus))

  private val expectedResultIncomeTax: Map[LiabilityKey, Amount] = Map(
    StartingRateForSavingsAmount    -> amt(BigDecimal(0), "null (savingsRateAmountScottish2023)"),
    OtherAdjustmentsReducing        -> amt(
      BigDecimal(510),
      "0.00(ctnDeficiencyRelief) + 0.00(topSlicingRelief) + 0.00(ctnVctSharesReliefAmt) + 0.00(ctnEisReliefAmt) + 0.00(ctnSeedEisReliefAmt) + 0.00(ctnCommInvTrustRelAmt) + 0.00(ctnSocialInvTaxRelAmt) + 0.00(atsSurplusMcaAlimonyRel) + 0.00(alimony) + 0.00(ctnNotionalTaxCegs) + 0.00(ctnNotlTaxOthrSrceAmo) + 0.00(ctnFtcrRestricted) + 500.00(reliefForFinanceCosts) + 0.00(lfiRelief) + 10.00(ctnRelTaxAcctFor)"
    ),
    UpperRate                       -> amt(BigDecimal(0.00), "0.00(ctnDividendChgbleHighRate)"),
    SavingsLowerIncome              -> amt(BigDecimal(2678.00), "2678.00(ctnSavingsChgbleLowerRate)"),
    SavingsLowerRateTax             -> amt(BigDecimal(535.60), "535.60(ctnSavingsTaxLowerRate)"),
    ScottishIncomeTax               -> amt(BigDecimal(0), "null (scottishIncomeTaxScottish2023)"),
    ScottishIntermediateRateTax     -> amt(
      BigDecimal(1438.50),
      "1438.50(taxOnPaySIR) + 0.00(ctnTaxOnRedundancySir) + null (ctnPensionLsumTaxDueAmt)"
    ),
    MarriageAllowanceReceivedAmount -> amt(BigDecimal(0.00), "0.00(ctnMarriageAllceInAmt)"),
    OrdinaryRateAmount              -> amt(BigDecimal(806.25), "806.25(ctnDividendTaxLowRate)"),
    ScottishHigherIncome            -> amt(
      BigDecimal(0.00),
      "0.00(ctnIncomeChgbleHigherRate) + 0.00(ctnTaxableRedundancyHr) + null (itfStatePensionLsGrossAmt)"
    ),
    ScottishStarterRateTax          -> amt(
      BigDecimal(398.43),
      "398.43(taxOnPaySSR) + 0.00(ctnTaxOnRedundancySsr) + null (ctnPensionLsumTaxDueAmt)"
    ),
    AdditionalRate                  -> amt(BigDecimal(0.00), "0.00(ctnDividendChgbleAddHRate)"),
    StartingRateForSavings          -> amt(BigDecimal(0), "null (savingsRateScottish2023)"),
    AdditionalRateIncomeTax         -> amt(BigDecimal(0), "null (additionalRateIncomeTaxScottish2023)"),
    SavingsAdditionalIncome         -> amt(BigDecimal(0.00), "0.00(ctnSavingsChgbleAddHRate)"),
    SavingsHigherIncome             -> amt(BigDecimal(0.00), "0.00(ctnSavingsChgbleHigherRate)"),
    ScottishAdditionalRateTax       -> amt(
      BigDecimal(0.00),
      "0.00(ctnIncomeTaxAddHighRate) + 0.00(ctnTaxOnRedundancyAhr) + null (ctnPensionLsumTaxDueAmt)"
    ),
    OtherAdjustmentsIncreasing      -> amt(
      BigDecimal(0.00),
      "0.00(nonDomChargeAmount) + 0.00(giftAidTaxReduced) + 0.00(netAnnuityPaytsTaxDue) + 0.00(ctnChildBenefitChrgAmt) + 0.00(ctnPensionSavingChrgbleAmt)"
    ),
    HigherRateIncomeTax             -> amt(BigDecimal(0), "null (higherRateIncomeTaxScottish2023)"),
    ScottishBasicRateTax            -> amt(
      BigDecimal(3483.80),
      "3483.80(ctnIncomeTaxBasicRate) + 0.00(ctnTaxOnRedundancyBr) + null (ctnPensionLsumTaxDueAmt)"
    ),
    BasicRateIncomeTaxAmount        -> amt(BigDecimal(0), "null (basicRateIncomeTaxAmountScottish2023)"),
    AdditionalRateAmount            -> amt(BigDecimal(0.00), "0.00(ctnDividendTaxAddHighRate)"),
    WelshIncomeTax                  -> amt(BigDecimal(0), "null (welshIncomeTax)"),
    ScottishAdditionalIncome        -> amt(
      BigDecimal(0.00),
      "0.00(ctnIncomeChgbleAddHRate) + 0.00(ctnTaxableRedundancyAhr) + null (itfStatePensionLsGrossAmt)"
    ),
    ScottishIntermediateIncome      -> amt(
      BigDecimal(6850.00),
      "6850.00(taxablePaySIR) + 0.00(ctnTaxableRedundancySir) + null (itfStatePensionLsGrossAmt)"
    ),
    UpperRateAmount                 -> amt(BigDecimal(0.00), "0.00(ctnDividendTaxHighRate)"),
    AdditionalRateIncomeTaxAmount   -> amt(BigDecimal(0), "null (additionalRateIncomeTaxAmountScottish2023)"),
    ScottishBasicIncome             -> amt(
      BigDecimal(17419.00),
      "17419.00(ctnIncomeChgbleBasicRate) + 0.00(ctnTaxableRedundancyBr) + null (itfStatePensionLsGrossAmt)"
    ),
    ScottishTotalTax                -> amt(
      BigDecimal(5320.73),
      "398.43(taxOnPaySSR) + 0.00(ctnTaxOnRedundancySsr) + null (ctnPensionLsumTaxDueAmt) + 3483.80(ctnIncomeTaxBasicRate) + 0.00(ctnTaxOnRedundancyBr) + null (ctnPensionLsumTaxDueAmt) + 1438.50(taxOnPaySIR) + 0.00(ctnTaxOnRedundancySir) + null (ctnPensionLsumTaxDueAmt) + 0.00(ctnIncomeTaxHigherRate) + 0.00(ctnTaxOnRedundancyHr) + null (ctnPensionLsumTaxDueAmt) + 0.00(ctnIncomeTaxAddHighRate) + 0.00(ctnTaxOnRedundancyAhr) + null (ctnPensionLsumTaxDueAmt)"
    ),
    BasicRateIncomeTax              -> amt(BigDecimal(0), "null (basicRateIncomeTaxScottish2023)"),
    SavingsAdditionalRateTax        -> amt(BigDecimal(0.00), "0.00(ctnSavingsTaxAddHighRate) + 0.00(ctnTaxOnCegAhr)"),
    HigherRateIncomeTaxAmount       -> amt(BigDecimal(0), "null (higherRateIncomeTaxAmountScottish2023)"),
    TotalIncomeTax                  -> amt(
      BigDecimal(6152.58),
      "null (savingsRateAmountScottish2023) + null (basicRateIncomeTaxAmountScottish2023) + null (higherRateIncomeTaxAmountScottish2023) + null (additionalRateIncomeTaxAmountScottish2023) + 806.25(ctnDividendTaxLowRate) + 0.00(ctnDividendTaxHighRate) + 0.00(ctnDividendTaxAddHighRate) + 0.00(nonDomChargeAmount) + 0.00(giftAidTaxReduced) + 0.00(netAnnuityPaytsTaxDue) + 0.00(ctnChildBenefitChrgAmt) + 0.00(ctnPensionSavingChrgbleAmt) - 0.00(ctnDeficiencyRelief) + 0.00(topSlicingRelief) + 0.00(ctnVctSharesReliefAmt) + 0.00(ctnEisReliefAmt) + 0.00(ctnSeedEisReliefAmt) + 0.00(ctnCommInvTrustRelAmt) + 0.00(ctnSocialInvTaxRelAmt) + 0.00(atsSurplusMcaAlimonyRel) + 0.00(alimony) + 0.00(ctnNotionalTaxCegs) + 0.00(ctnNotlTaxOthrSrceAmo) + 0.00(ctnFtcrRestricted) + 500.00(reliefForFinanceCosts) + 0.00(lfiRelief) + 10.00(ctnRelTaxAcctFor) - 0.00(ctnMarriageAllceInAmt) + 398.43(taxOnPaySSR) + 0.00(ctnTaxOnRedundancySsr) + null (ctnPensionLsumTaxDueAmt) + 3483.80(ctnIncomeTaxBasicRate) + 0.00(ctnTaxOnRedundancyBr) + null (ctnPensionLsumTaxDueAmt) + 1438.50(taxOnPaySIR) + 0.00(ctnTaxOnRedundancySir) + null (ctnPensionLsumTaxDueAmt) + 0.00(ctnIncomeTaxHigherRate) + 0.00(ctnTaxOnRedundancyHr) + null (ctnPensionLsumTaxDueAmt) + 0.00(ctnIncomeTaxAddHighRate) + 0.00(ctnTaxOnRedundancyAhr) + null (ctnPensionLsumTaxDueAmt) + 535.60(ctnSavingsTaxLowerRate) + 0.00(ctnSavingsTaxHigherRate) + 0.00(ctnSavingsTaxAddHighRate) + 0.00(ctnTaxOnCegAhr)"
    ),
    SavingsHigherRateTax            -> amt(BigDecimal(0.00), "0.00(ctnSavingsTaxHigherRate)"),
    OrdinaryRate                    -> amt(BigDecimal(10750.00), "10750.00(ctnDividendChgbleLowRate)"),
    ScottishHigherRateTax           -> amt(
      BigDecimal(0.00),
      "0.00(ctnIncomeTaxHigherRate) + 0.00(ctnTaxOnRedundancyHr) + null (ctnPensionLsumTaxDueAmt)"
    ),
    ScottishStarterIncome           -> amt(
      BigDecimal(2097.00),
      "2097.00(taxablePaySSR) + 0.00(ctnTaxableRedundancySsr) + null (itfStatePensionLsGrossAmt)"
    )
  )

  private val expectedResultIncomeData: Map[LiabilityKey, Amount] = Map(
    SelfEmploymentIncome   -> amt(
      BigDecimal(0.00),
      "0.00(ctnSummaryTotalScheduleD) + 0.00(ctnSummaryTotalPartnership) + 0.00(ctnSavingsPartnership) + 0.00(ctnDividendsPartnership)"
    ),
    IncomeFromEmployment   -> amt(BigDecimal(23678.00), "23678.00(ctnSummaryTotalEmployment)"),
    StatePension           -> amt(BigDecimal(9783.00), "9783.00(atsStatePensionAmt)"),
    OtherPensionIncome     -> amt(BigDecimal(0.00), "0.00(atsOtherPensionAmt) + 0.00(itfStatePensionLsGrossAmt)"),
    TotalIncomeBeforeTax   -> amt(
      BigDecimal(55364.00),
      "0.00(ctnSummaryTotalScheduleD) + 0.00(ctnSummaryTotalPartnership) + 0.00(ctnSavingsPartnership) + 0.00(ctnDividendsPartnership) + 23678.00(ctnSummaryTotalEmployment) + 9783.00(atsStatePensionAmt) + 0.00(atsOtherPensionAmt) + 0.00(itfStatePensionLsGrossAmt) + 0.00(atsIncBenefitSuppAllowAmt) + 0.00(atsJobSeekersAllowanceAmt) + 0.00(atsOthStatePenBenefitsAmt) + 0.00(ctnSummaryTotShareOptions) + 5475.00(ctnSummaryTotalUklProperty) + 0.00(ctnSummaryTotForeignIncome) + 0.00(ctnSummaryTotTrustEstates) + 0.00(ctnSummaryTotalOtherIncome) + 3678.00(ctnSummaryTotalUkInterest) + 0.00(ctnSummaryTotForeignDiv) + 12750.00(ctnSummaryTotalUkIntDivs) + 0.00(ctn4SumTotLifePolicyGains) + 0.00(ctnSummaryTotForeignSav) + 0.00(ctnForeignCegDedn) + 0.00(itfCegReceivedAfterTax) + 0.00(ctnEmploymentBenefitsAmt)"
    ),
    OtherIncome            -> amt(
      BigDecimal(21903.00),
      "0.00(ctnSummaryTotShareOptions) + 5475.00(ctnSummaryTotalUklProperty) + 0.00(ctnSummaryTotForeignIncome) + 0.00(ctnSummaryTotTrustEstates) + 0.00(ctnSummaryTotalOtherIncome) + 3678.00(ctnSummaryTotalUkInterest) + 0.00(ctnSummaryTotForeignDiv) + 12750.00(ctnSummaryTotalUkIntDivs) + 0.00(ctn4SumTotLifePolicyGains) + 0.00(ctnSummaryTotForeignSav) + 0.00(ctnForeignCegDedn) + 0.00(itfCegReceivedAfterTax)"
    ),
    BenefitsFromEmployment -> amt(BigDecimal(0.00), "0.00(ctnEmploymentBenefitsAmt)"),
    TaxableStateBenefits   -> amt(
      BigDecimal(0.00),
      "0.00(atsIncBenefitSuppAllowAmt) + 0.00(atsJobSeekersAllowanceAmt) + 0.00(atsOthStatePenBenefitsAmt)"
    )
  )

  private val expectedResultAllowanceData: Map[LiabilityKey, Amount] = Map(
    PersonalTaxFreeAmount              -> amt(BigDecimal(12570.00), "12570.00(ctnPersonalAllowance)"),
    MarriageAllowanceTransferredAmount -> amt(BigDecimal(0.00), "0.00(ctnMarriageAllceOutAmt)"),
    OtherAllowancesAmount              -> amt(
      BigDecimal(0),
      "0.00(ctnEmploymentExpensesAmt) + 0.00(ctnSummaryTotalDedPpr) + 0.00(ctnSumTotForeignTaxRelief) + 0.00(ctnSumTotLossRestricted) + 0.00(grossAnnuityPayts) + 0.00(itf4GiftsInvCharitiesAmo) + 0.00(ctnBpaAllowanceAmt) + 0.00(itfBpaAmount)"
    ),
    TotalTaxFreeAmount                 -> amt(
      BigDecimal(12570.00),
      "0.00(ctnEmploymentExpensesAmt) + 0.00(ctnSummaryTotalDedPpr) + 0.00(ctnSumTotForeignTaxRelief) + 0.00(ctnSumTotLossRestricted) + 0.00(grossAnnuityPayts) + 0.00(itf4GiftsInvCharitiesAmo) + 0.00(ctnBpaAllowanceAmt) + 0.00(itfBpaAmount) + 12570.00(ctnPersonalAllowance) - 0.00(ctnMarriageAllceOutAmt)"
    )
  )

  private val expectedResultCGData: Map[LiabilityKey, Amount] = Map(
    AmountDueRPCILowerRate       -> amt(BigDecimal(0.00), "0.00(ctnLowerRateCgtRPCI)"),
    AmountAtHigherRate           -> amt(BigDecimal(0.00), "0.00(ctnCgAtHigherRate)"),
    Adjustments                  -> amt(BigDecimal(0.00), "0.00(capAdjustmentAmt)"),
    AmountAtOrdinaryRate         -> amt(BigDecimal(0.00), "0.00(ctnCgAtLowerRate)"),
    AmountAtRPCIHigheRate        -> amt(BigDecimal(0.00), "0.00(ctnCGAtHigherRateRPCI)"),
    AmountDueAtEntrepreneursRate -> amt(BigDecimal(0.00), "0.00(ctnCgDueEntrepreneursRate)"),
    CgTaxPerCurrencyUnit         -> amt(
      BigDecimal(0.00),
      "0.00(atsCgTotGainsAfterLosses) + 0.00(atsCgGainsAfterLossesAmt)"
    ),
    TaxableGains                 -> amt(BigDecimal(0.00), "0.00(atsCgTotGainsAfterLosses) + 0.00(atsCgGainsAfterLossesAmt)"),
    AmountDueAtOrdinaryRate      -> amt(BigDecimal(0.00), "0.00(ctnCgDueLowerRate)"),
    PayCgTaxOn                   -> amt(BigDecimal(0), "null (taxableGains() < get(CgAnnualExempt))"),
    TotalCgTax                   -> amt(
      BigDecimal(0.00),
      "max(0, Some(0.00(ctnLowerRateCgtRPCI) + 0.00(ctnHigherRateCgtRPCI) + 0.00(ctnCgDueEntrepreneursRate) + 0.00(ctnCgDueLowerRate) + 0.00(ctnCgDueHigherRate) + 0.00(capAdjustmentAmt)))"
    ),
    AmountAtEntrepreneursRate    -> amt(BigDecimal(0.00), "0.00(ctnCgAtEntrepreneursRate)"),
    LessTaxFreeAmount            -> amt(BigDecimal(12300.00), "12300.00(atsCgAnnualExemptAmt)"),
    AmountDueRPCIHigherRate      -> amt(BigDecimal(0.00), "0.00(ctnHigherRateCgtRPCI)"),
    AmountDueAtHigherRate        -> amt(BigDecimal(0.00), "0.00(ctnCgDueHigherRate)"),
    AmountAtRPCILowerRate        -> amt(BigDecimal(0.00), "0.00(ctnCGAtLowerRateRPCI)")
  )

  private val expectedResultSummaryData: Map[LiabilityKey, Amount] = Map(
    TotalIncomeTaxAndNics     -> amt(
      BigDecimal(6252.58),
      "100.00(employeeClass1Nic) + 0.00(ctnClass2NicAmt) + 0.00(class4Nic) + null (savingsRateAmountScottish2023) + null (basicRateIncomeTaxAmountScottish2023) + null (higherRateIncomeTaxAmountScottish2023) + null (additionalRateIncomeTaxAmountScottish2023) + 806.25(ctnDividendTaxLowRate) + 0.00(ctnDividendTaxHighRate) + 0.00(ctnDividendTaxAddHighRate) + 0.00(nonDomChargeAmount) + 0.00(giftAidTaxReduced) + 0.00(netAnnuityPaytsTaxDue) + 0.00(ctnChildBenefitChrgAmt) + 0.00(ctnPensionSavingChrgbleAmt) - 0.00(ctnDeficiencyRelief) + 0.00(topSlicingRelief) + 0.00(ctnVctSharesReliefAmt) + 0.00(ctnEisReliefAmt) + 0.00(ctnSeedEisReliefAmt) + 0.00(ctnCommInvTrustRelAmt) + 0.00(ctnSocialInvTaxRelAmt) + 0.00(atsSurplusMcaAlimonyRel) + 0.00(alimony) + 0.00(ctnNotionalTaxCegs) + 0.00(ctnNotlTaxOthrSrceAmo) + 0.00(ctnFtcrRestricted) + 500.00(reliefForFinanceCosts) + 0.00(lfiRelief) + 10.00(ctnRelTaxAcctFor) - 0.00(ctnMarriageAllceInAmt) + 398.43(taxOnPaySSR) + 0.00(ctnTaxOnRedundancySsr) + null (ctnPensionLsumTaxDueAmt) + 3483.80(ctnIncomeTaxBasicRate) + 0.00(ctnTaxOnRedundancyBr) + null (ctnPensionLsumTaxDueAmt) + 1438.50(taxOnPaySIR) + 0.00(ctnTaxOnRedundancySir) + null (ctnPensionLsumTaxDueAmt) + 0.00(ctnIncomeTaxHigherRate) + 0.00(ctnTaxOnRedundancyHr) + null (ctnPensionLsumTaxDueAmt) + 0.00(ctnIncomeTaxAddHighRate) + 0.00(ctnTaxOnRedundancyAhr) + null (ctnPensionLsumTaxDueAmt) + 535.60(ctnSavingsTaxLowerRate) + 0.00(ctnSavingsTaxHigherRate) + 0.00(ctnSavingsTaxAddHighRate) + 0.00(ctnTaxOnCegAhr)"
    ),
    NicsAndTaxPerCurrencyUnit -> amt(
      BigDecimal(0.1129),
      "100.00(employeeClass1Nic) + 0.00(ctnClass2NicAmt) + 0.00(class4Nic) + null (savingsRateAmountScottish2023) + null (basicRateIncomeTaxAmountScottish2023) + null (higherRateIncomeTaxAmountScottish2023) + null (additionalRateIncomeTaxAmountScottish2023) + 806.25(ctnDividendTaxLowRate) + 0.00(ctnDividendTaxHighRate) + 0.00(ctnDividendTaxAddHighRate) + 0.00(nonDomChargeAmount) + 0.00(giftAidTaxReduced) + 0.00(netAnnuityPaytsTaxDue) + 0.00(ctnChildBenefitChrgAmt) + 0.00(ctnPensionSavingChrgbleAmt) - 0.00(ctnDeficiencyRelief) + 0.00(topSlicingRelief) + 0.00(ctnVctSharesReliefAmt) + 0.00(ctnEisReliefAmt) + 0.00(ctnSeedEisReliefAmt) + 0.00(ctnCommInvTrustRelAmt) + 0.00(ctnSocialInvTaxRelAmt) + 0.00(atsSurplusMcaAlimonyRel) + 0.00(alimony) + 0.00(ctnNotionalTaxCegs) + 0.00(ctnNotlTaxOthrSrceAmo) + 0.00(ctnFtcrRestricted) + 500.00(reliefForFinanceCosts) + 0.00(lfiRelief) + 10.00(ctnRelTaxAcctFor) - 0.00(ctnMarriageAllceInAmt) + 398.43(taxOnPaySSR) + 0.00(ctnTaxOnRedundancySsr) + null (ctnPensionLsumTaxDueAmt) + 3483.80(ctnIncomeTaxBasicRate) + 0.00(ctnTaxOnRedundancyBr) + null (ctnPensionLsumTaxDueAmt) + 1438.50(taxOnPaySIR) + 0.00(ctnTaxOnRedundancySir) + null (ctnPensionLsumTaxDueAmt) + 0.00(ctnIncomeTaxHigherRate) + 0.00(ctnTaxOnRedundancyHr) + null (ctnPensionLsumTaxDueAmt) + 0.00(ctnIncomeTaxAddHighRate) + 0.00(ctnTaxOnRedundancyAhr) + null (ctnPensionLsumTaxDueAmt) + 535.60(ctnSavingsTaxLowerRate) + 0.00(ctnSavingsTaxHigherRate) + 0.00(ctnSavingsTaxAddHighRate) + 0.00(ctnTaxOnCegAhr)"
    ),
    CgTaxPerCurrencyUnit      -> amt(BigDecimal(0.00), "0.00(atsCgTotGainsAfterLosses) + 0.00(atsCgGainsAfterLossesAmt)"),
    TotalIncomeBeforeTax      -> amt(
      BigDecimal(55364.00),
      "0.00(ctnSummaryTotalScheduleD) + 0.00(ctnSummaryTotalPartnership) + 0.00(ctnSavingsPartnership) + 0.00(ctnDividendsPartnership) + 23678.00(ctnSummaryTotalEmployment) + 9783.00(atsStatePensionAmt) + 0.00(atsOtherPensionAmt) + 0.00(itfStatePensionLsGrossAmt) + 0.00(atsIncBenefitSuppAllowAmt) + 0.00(atsJobSeekersAllowanceAmt) + 0.00(atsOthStatePenBenefitsAmt) + 0.00(ctnSummaryTotShareOptions) + 5475.00(ctnSummaryTotalUklProperty) + 0.00(ctnSummaryTotForeignIncome) + 0.00(ctnSummaryTotTrustEstates) + 0.00(ctnSummaryTotalOtherIncome) + 3678.00(ctnSummaryTotalUkInterest) + 0.00(ctnSummaryTotForeignDiv) + 12750.00(ctnSummaryTotalUkIntDivs) + 0.00(ctn4SumTotLifePolicyGains) + 0.00(ctnSummaryTotForeignSav) + 0.00(ctnForeignCegDedn) + 0.00(itfCegReceivedAfterTax) + 0.00(ctnEmploymentBenefitsAmt)"
    ),
    TotalCgTax                -> amt(
      BigDecimal(0.00),
      "max(0, Some(0.00(ctnLowerRateCgtRPCI) + 0.00(ctnHigherRateCgtRPCI) + 0.00(ctnCgDueEntrepreneursRate) + 0.00(ctnCgDueLowerRate) + 0.00(ctnCgDueHigherRate) + 0.00(capAdjustmentAmt)))"
    ),
    YourTotalTax              -> amt(
      BigDecimal(6252.58),
      "100.00(employeeClass1Nic) + 0.00(ctnClass2NicAmt) + 0.00(class4Nic) + null (savingsRateAmountScottish2023) + null (basicRateIncomeTaxAmountScottish2023) + null (higherRateIncomeTaxAmountScottish2023) + null (additionalRateIncomeTaxAmountScottish2023) + 806.25(ctnDividendTaxLowRate) + 0.00(ctnDividendTaxHighRate) + 0.00(ctnDividendTaxAddHighRate) + 0.00(nonDomChargeAmount) + 0.00(giftAidTaxReduced) + 0.00(netAnnuityPaytsTaxDue) + 0.00(ctnChildBenefitChrgAmt) + 0.00(ctnPensionSavingChrgbleAmt) - 0.00(ctnDeficiencyRelief) + 0.00(topSlicingRelief) + 0.00(ctnVctSharesReliefAmt) + 0.00(ctnEisReliefAmt) + 0.00(ctnSeedEisReliefAmt) + 0.00(ctnCommInvTrustRelAmt) + 0.00(ctnSocialInvTaxRelAmt) + 0.00(atsSurplusMcaAlimonyRel) + 0.00(alimony) + 0.00(ctnNotionalTaxCegs) + 0.00(ctnNotlTaxOthrSrceAmo) + 0.00(ctnFtcrRestricted) + 500.00(reliefForFinanceCosts) + 0.00(lfiRelief) + 10.00(ctnRelTaxAcctFor) - 0.00(ctnMarriageAllceInAmt) + 398.43(taxOnPaySSR) + 0.00(ctnTaxOnRedundancySsr) + null (ctnPensionLsumTaxDueAmt) + 3483.80(ctnIncomeTaxBasicRate) + 0.00(ctnTaxOnRedundancyBr) + null (ctnPensionLsumTaxDueAmt) + 1438.50(taxOnPaySIR) + 0.00(ctnTaxOnRedundancySir) + null (ctnPensionLsumTaxDueAmt) + 0.00(ctnIncomeTaxHigherRate) + 0.00(ctnTaxOnRedundancyHr) + null (ctnPensionLsumTaxDueAmt) + 0.00(ctnIncomeTaxAddHighRate) + 0.00(ctnTaxOnRedundancyAhr) + null (ctnPensionLsumTaxDueAmt) + 535.60(ctnSavingsTaxLowerRate) + 0.00(ctnSavingsTaxHigherRate) + 0.00(ctnSavingsTaxAddHighRate) + 0.00(ctnTaxOnCegAhr) + max(0, Some(0.00(ctnLowerRateCgtRPCI) + 0.00(ctnHigherRateCgtRPCI) + 0.00(ctnCgDueEntrepreneursRate) + 0.00(ctnCgDueLowerRate) + 0.00(ctnCgDueHigherRate) + 0.00(capAdjustmentAmt)))"
    ),
    TotalTaxFreeAmount        -> amt(
      BigDecimal(12570.00),
      "0.00(ctnEmploymentExpensesAmt) + 0.00(ctnSummaryTotalDedPpr) + 0.00(ctnSumTotForeignTaxRelief) + 0.00(ctnSumTotLossRestricted) + 0.00(grossAnnuityPayts) + 0.00(itf4GiftsInvCharitiesAmo) + 0.00(ctnBpaAllowanceAmt) + 0.00(itfBpaAmount) + 12570.00(ctnPersonalAllowance) - 0.00(ctnMarriageAllceOutAmt)"
    ),
    TotalIncomeTax            -> amt(
      BigDecimal(6152.58),
      "null (savingsRateAmountScottish2023) + null (basicRateIncomeTaxAmountScottish2023) + null (higherRateIncomeTaxAmountScottish2023) + null (additionalRateIncomeTaxAmountScottish2023) + 806.25(ctnDividendTaxLowRate) + 0.00(ctnDividendTaxHighRate) + 0.00(ctnDividendTaxAddHighRate) + 0.00(nonDomChargeAmount) + 0.00(giftAidTaxReduced) + 0.00(netAnnuityPaytsTaxDue) + 0.00(ctnChildBenefitChrgAmt) + 0.00(ctnPensionSavingChrgbleAmt) - 0.00(ctnDeficiencyRelief) + 0.00(topSlicingRelief) + 0.00(ctnVctSharesReliefAmt) + 0.00(ctnEisReliefAmt) + 0.00(ctnSeedEisReliefAmt) + 0.00(ctnCommInvTrustRelAmt) + 0.00(ctnSocialInvTaxRelAmt) + 0.00(atsSurplusMcaAlimonyRel) + 0.00(alimony) + 0.00(ctnNotionalTaxCegs) + 0.00(ctnNotlTaxOthrSrceAmo) + 0.00(ctnFtcrRestricted) + 500.00(reliefForFinanceCosts) + 0.00(lfiRelief) + 10.00(ctnRelTaxAcctFor) - 0.00(ctnMarriageAllceInAmt) + 398.43(taxOnPaySSR) + 0.00(ctnTaxOnRedundancySsr) + null (ctnPensionLsumTaxDueAmt) + 3483.80(ctnIncomeTaxBasicRate) + 0.00(ctnTaxOnRedundancyBr) + null (ctnPensionLsumTaxDueAmt) + 1438.50(taxOnPaySIR) + 0.00(ctnTaxOnRedundancySir) + null (ctnPensionLsumTaxDueAmt) + 0.00(ctnIncomeTaxHigherRate) + 0.00(ctnTaxOnRedundancyHr) + null (ctnPensionLsumTaxDueAmt) + 0.00(ctnIncomeTaxAddHighRate) + 0.00(ctnTaxOnRedundancyAhr) + null (ctnPensionLsumTaxDueAmt) + 535.60(ctnSavingsTaxLowerRate) + 0.00(ctnSavingsTaxHigherRate) + 0.00(ctnSavingsTaxAddHighRate) + 0.00(ctnTaxOnCegAhr)"
    ),
    PersonalTaxFreeAmount     -> amt(BigDecimal(12570.00), "12570.00(ctnPersonalAllowance)"),
    EmployeeNicAmount         -> amt(BigDecimal(100.00), "100.00(employeeClass1Nic) + 0.00(ctnClass2NicAmt) + 0.00(class4Nic)"),
    TaxableGains              -> amt(BigDecimal(0.00), "0.00(atsCgTotGainsAfterLosses) + 0.00(atsCgGainsAfterLossesAmt)")
  )
}
