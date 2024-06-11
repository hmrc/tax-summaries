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
import play.api.libs.json.{JsObject, Json}
import transformers.ATSRawDataTransformer
import utils.{AtsJsonDataUpdate, BaseSpec, JsonUtil}

class ATSRawDataTransformer2023ScottishSpec extends BaseSpec with AtsJsonDataUpdate {
  import ATSRawDataTransformer2023ScottishSpec._
  private val taxpayerDetailsJson       = JsonUtil.load("/taxpayer/sa_taxpayer-valid.json")
  private val parsedTaxpayerDetailsJson = Json.parse(taxpayerDetailsJson)

  private val atsRawDataTransformer: ATSRawDataTransformer = inject[ATSRawDataTransformer]

  private lazy val parsedJson                     = buildJsonPayload(tliSlpAtsData)
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
      ("allowance data", returnValue.allowance_data, expectedResultAllowanceData)
      //     ("summary data", returnValue.summary_data, expectedResultSummaryData)
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
  private val taxYear: Int = 2023

  private val tliSlpAtsData: Map[String, BigDecimal]           = Map(
    "ctnEmploymentBenefitsAmt"   -> BigDecimal(0.00),
    "ctnSummaryTotalScheduleD"   -> BigDecimal(0.00),
    "ctnSummaryTotalPartnership" -> BigDecimal(0.00),
    "ctnSummaryTotalEmployment"  -> BigDecimal(23678.00),
    "atsStatePensionAmt"         -> BigDecimal(9783.00),
    "atsOtherPensionAmt"         -> BigDecimal(0.00),
    "itfStatePensionLsGrossAmt"  -> BigDecimal(0.00),
    "atsIncBenefitSuppAllowAmt"  -> BigDecimal(0.00),
    "atsJobSeekersAllowanceAmt"  -> BigDecimal(0.00),
    "atsOthStatePenBenefitsAmt"  -> BigDecimal(0.00),
    "ctnSummaryTotShareOptions"  -> BigDecimal(0.00),
    "ctnSummaryTotalUklProperty" -> BigDecimal(5475.00),
    "ctnSummaryTotForeignIncome" -> BigDecimal(0.00),
    "ctnSummaryTotTrustEstates"  -> BigDecimal(0.00),
    "ctnSummaryTotalOtherIncome" -> BigDecimal(0.00),
    "ctnSummaryTotForeignSav"    -> BigDecimal(0.00),
    "ctnForeignCegDedn"          -> BigDecimal(0.00),
    "ctnSummaryTotalUkInterest"  -> BigDecimal(3678.00),
    "itfCegReceivedAfterTax"     -> BigDecimal(0.00),
    "ctnSummaryTotForeignDiv"    -> BigDecimal(0.00),
    "ctnSummaryTotalUkIntDivs"   -> BigDecimal(12750.00),
    "ctn4SumTotLifePolicyGains"  -> BigDecimal(0.00),
    "ctnPersonalAllowance"       -> BigDecimal(12570.00),
    "ctnEmploymentExpensesAmt"   -> BigDecimal(0.00),
    "ctnSummaryTotalDedPpr"      -> BigDecimal(0.00),
    "ctnSumTotForeignTaxRelief"  -> BigDecimal(0.00),
    "ctnSumTotLoanRestricted"    -> BigDecimal(0.00),
    "ctnSumTotLossRestricted"    -> BigDecimal(0.00),
    "grossAnnuityPayts"          -> BigDecimal(0.00),
    "itf4GiftsInvCharitiesAmo"   -> BigDecimal(0.00),
    "itfTradeUnionDeathBenefits" -> BigDecimal(0.00),
    "ctnBpaAllowanceAmt"         -> BigDecimal(0.00),
    "itfBpaAmount"               -> BigDecimal(0.00),
    "grossExcludedIncome"        -> BigDecimal(0.00),
    "class4Nic"                  -> BigDecimal(0.00),
    "ctnClass2NicAmt"            -> BigDecimal(0.00),
    "ctnSavingsChgbleStartRate"  -> BigDecimal(0.00),
    "ctnSavingsTaxStartingRate"  -> BigDecimal(0.00),
    "ctnIncomeChgbleBasicRate"   -> BigDecimal(17419.00),
    "ctnSavingsChgbleLowerRate"  -> BigDecimal(2678.00),
    "ctnIncomeTaxBasicRate"      -> BigDecimal(3483.80),
    "ctnSavingsTaxLowerRate"     -> BigDecimal(535.60),
    "ctnIncomeChgbleHigherRate"  -> BigDecimal(0.00),
    "ctnSavingsChgbleHigherRate" -> BigDecimal(0.00),
    "ctnIncomeTaxHigherRate"     -> BigDecimal(0.00),
    "ctnSavingsTaxHigherRate"    -> BigDecimal(0.00),
    "ctnIncomeChgbleAddHRate"    -> BigDecimal(0.00),
    "ctnSavingsChgbleAddHRate"   -> BigDecimal(0.00),
    "ctnIncomeTaxAddHighRate"    -> BigDecimal(0.00),
    "ctnSavingsTaxAddHighRate"   -> BigDecimal(0.00),
    "taxablePaySSR"              -> BigDecimal(2097.00),
    "taxOnPaySSR"                -> BigDecimal(398.43),
    "taxablePaySIR"              -> BigDecimal(6850.00),
    "taxOnPaySIR"                -> BigDecimal(1438.50),
    "ctnDividendChgbleLowRate"   -> BigDecimal(10750.00),
    "ctnDividendTaxLowRate"      -> BigDecimal(806.25),
    "ctnDividendChgbleHighRate"  -> BigDecimal(0.00),
    "ctnDividendTaxHighRate"     -> BigDecimal(0.00),
    "ctnDividendChgbleAddHRate"  -> BigDecimal(0.00),
    "ctnDividendTaxAddHighRate"  -> BigDecimal(0.00),
    "ctnTaxableRedundancySSR"    -> BigDecimal(0.00),
    "ctnTaxOnRedundancySsr"      -> BigDecimal(0.00),
    "ctnTaxableRedundancyBr"     -> BigDecimal(0.00),
    "ctnTaxOnRedundancyBr"       -> BigDecimal(0.00),
    "ctnTaxableRedundancySir"    -> BigDecimal(0.00),
    "ctnTaxOnRedundancySir"      -> BigDecimal(0.00),
    "ctnTaxableRedundancyHr"     -> BigDecimal(0.00),
    "ctnTaxOnRedundancyHr"       -> BigDecimal(0.00),
    "ctnTaxableRedundancyAhr"    -> BigDecimal(0.00),
    "ctnTaxOnRedundancyAhr"      -> BigDecimal(0.00),
    "ctnTaxableCegBr"            -> BigDecimal(0.00),
    "ctnTaxOnCegBr"              -> BigDecimal(0.00),
    "ctnTaxableCegHr"            -> BigDecimal(0.00),
    "ctnTaxOnCegHr"              -> BigDecimal(0.00),
    "ctnTaxableCegAhr"           -> BigDecimal(0.00),
    "ctnTaxOnCegAhr"             -> BigDecimal(0.00),
    "nonDomChargeAmount"         -> BigDecimal(0.00),
    "taxExcluded"                -> BigDecimal(0.00),
    "taxOnNonExcludedInc"        -> BigDecimal(0.00),
    "incomeTaxDue"               -> BigDecimal(6162.58),
    "ctn4TaxDueAfterAllceRlf"    -> BigDecimal(6162.58),
    "netAnnuityPaytsTaxDue"      -> BigDecimal(0.00),
    "ctnChildBenefitChrgAmt"     -> BigDecimal(0.00),
    "ctnPensionSavingChrgbleAmt" -> BigDecimal(0.00),
    "atsTaxCharged"              -> BigDecimal(6662.58),
    "ctnDeficiencyRelief"        -> BigDecimal(0.00),
    "topSlicingRelief"           -> BigDecimal(0.00),
    "ctnVctSharesReliefAmt"      -> BigDecimal(0.00),
    "ctnEisReliefAmt"            -> BigDecimal(0.00),
    "ctnSeedEisReliefAmt"        -> BigDecimal(0.00),
    "ctnCommInvTrustRelAmt"      -> BigDecimal(0.00),
    "atsSurplusMcaAlimonyRel"    -> BigDecimal(0.00),
    "alimony"                    -> BigDecimal(0.00),
    "ctnNotionalTaxCegs"         -> BigDecimal(0.00),
    "ctnNotlTaxOthrSrceAmo"      -> BigDecimal(0.00),
    "ctnFtcrRestricted"          -> BigDecimal(0.00),
    "reliefForFinanceCosts"      -> BigDecimal(500.00),
    "lfiRelief"                  -> BigDecimal(0.00),
    "ctnRelTaxAcctFor"           -> BigDecimal(10.00),
    "ctnTaxCredForDivs"          -> BigDecimal(0.00),
    "ctnQualDistnReliefAmt"      -> BigDecimal(0.00),
    "figTotalTaxCreditRelief"    -> BigDecimal(0.00),
    "ctnNonPayableTaxCredits"    -> BigDecimal(0.00),
    "atsCgTotGainsAfterLosses"   -> BigDecimal(0.00),
    "atsCgGainsAfterLossesAmt"   -> BigDecimal(0.00),
    "cap3AssessableChgeableGain" -> BigDecimal(0.00),
    "atsCgAnnualExemptAmt"       -> BigDecimal(12300.00),
    "ctnCgAtEntrepreneursRate"   -> BigDecimal(0.00),
    "ctnCgDueEntrepreneursRate"  -> BigDecimal(0.00),
    "ctnCgAtLowerRate"           -> BigDecimal(0.00),
    "ctnCgDueLowerRate"          -> BigDecimal(0.00),
    "ctnCgAtHigherRate"          -> BigDecimal(0.00),
    "ctnCgDueHigherRate"         -> BigDecimal(0.00),
    "ctnCGAtLowerRateRPCI"       -> BigDecimal(0.00),
    "ctnLowerRateCgtRPCI"        -> BigDecimal(0.00),
    "ctnCGAtHigherRateRPCI"      -> BigDecimal(0.00),
    "ctnHigherRateCgtRPCI"       -> BigDecimal(0.00),
    "capAdjustmentAmt"           -> BigDecimal(0.00),
    "ctnPensionLsumTaxDueAmt"    -> BigDecimal(0.00),
    "ctnMarriageAllceInAmt"      -> BigDecimal(0.00),
    "ctnMarriageAllceOutAmt"     -> BigDecimal(0.00),
    "ctnSocialInvTaxRelAmt"      -> BigDecimal(0.00),
    "ctnSavingsPartnership"      -> BigDecimal(0.00),
    "ctnDividendsPartnership"    -> BigDecimal(0.00),
    "giftAidTaxReduced"          -> BigDecimal(0.00),
    "ctnTaxableCegSr"            -> BigDecimal(0.00),
    "ctnTaxOnCegSr"              -> BigDecimal(0.00),
    "ctnTaxableRedundancySsr"    -> BigDecimal(0.00)
  ).map(item => item._1 -> item._2.setScale(2))

  private def amt(value: BigDecimal, calculus: String): Amount = Amount(value, "GBP", Some(calculus))

  private def calcExp(fieldNames: String*): Amount = {
    val retrieveAmount: String => Amount = fieldName => {
      val (name, isNull) = {
        if (fieldName.endsWith(":null")) {
          (fieldName.takeWhile(_ != ':'), true)
        } else {
          (fieldName, false)
        }
      }

      if (isNull) {
        Amount.empty(name)
      } else {
        val bdValue = tliSlpAtsData(name)
        Amount(bdValue, "GBP", Some(s"$bdValue($name)"))
      }
    }

    val initialValue = retrieveAmount(fieldNames.head)
    fieldNames.tail.foldLeft[Amount](initialValue) { (c, i) =>
      if (i.startsWith("-")) {
        c - retrieveAmount(i.substring(1))
      } else {
        c + retrieveAmount(i)
      }
    }
  }

  private val fieldsOtherAdjustmentsReducing = Seq(
    "ctnDeficiencyRelief",
    "topSlicingRelief",
    "ctnVctSharesReliefAmt",
    "ctnEisReliefAmt",
    "ctnSeedEisReliefAmt",
    "ctnCommInvTrustRelAmt",
    "ctnSocialInvTaxRelAmt",
    "atsSurplusMcaAlimonyRel",
    "alimony",
    "ctnNotionalTaxCegs",
    "ctnNotlTaxOthrSrceAmo",
    "ctnFtcrRestricted",
    "reliefForFinanceCosts",
    "lfiRelief",
    "ctnRelTaxAcctFor"
  )

  private val fieldsOtherAdjustmentsIncreasing = Seq(
    "nonDomChargeAmount",
    "giftAidTaxReduced",
    "netAnnuityPaytsTaxDue",
    "ctnChildBenefitChrgAmt",
    "ctnPensionSavingChrgbleAmt"
  )

  private val fieldsXX1 = Seq(
    "savingsRateAmountScottish2023:null",
    "basicRateIncomeTaxAmountScottish2023:null",
    "higherRateIncomeTaxAmountScottish2023:null",
    "additionalRateIncomeTaxAmountScottish2023:null",
    "ctnDividendTaxLowRate",
    "ctnDividendTaxHighRate",
    "ctnDividendTaxAddHighRate"
  )

  private val fieldsXX2 = Seq(
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
    "ctnPensionLsumTaxDueAmt:null",
    "ctnSavingsTaxLowerRate",
    "ctnSavingsTaxHigherRate",
    "ctnSavingsTaxAddHighRate",
    "ctnTaxOnCegAhr"
  )

  private def expTotalIncomeTax: Amount =
    ((calcExp((fieldsXX1 ++ fieldsOtherAdjustmentsIncreasing): _*) - calcExp(
      fieldsOtherAdjustmentsReducing: _*
    )) - calcExp(
      "ctnMarriageAllceInAmt"
    )) + calcExp(
      fieldsXX2: _*
    )

  private def expTotalTaxFreeAmount: Amount = calcExp(
    "ctnEmploymentExpensesAmt",
    "ctnSummaryTotalDedPpr",
    "ctnSumTotForeignTaxRelief",
    "ctnSumTotLossRestricted",
    "grossAnnuityPayts",
    "itf4GiftsInvCharitiesAmo",
    "ctnBpaAllowanceAmt",
    "itfBpaAmount",
    "ctnPersonalAllowance"
  ) - calcExp("ctnMarriageAllceOutAmt")

  private val expectedResultIncomeTax: Map[LiabilityKey, Amount] = Map(
    StartingRateForSavingsAmount    -> calcExp("savingsRateAmountScottish2023:null"),
    OtherAdjustmentsReducing        -> calcExp(fieldsOtherAdjustmentsReducing: _*),
    UpperRate                       -> calcExp("ctnDividendChgbleHighRate"),
    SavingsLowerIncome              -> calcExp("ctnSavingsChgbleLowerRate"),
    SavingsLowerRateTax             -> calcExp("ctnSavingsTaxLowerRate"),
    ScottishIncomeTax               -> calcExp("scottishIncomeTaxScottish2023:null"),
    ScottishIntermediateRateTax     -> calcExp("taxOnPaySIR", "ctnTaxOnRedundancySir", "ctnPensionLsumTaxDueAmt:null"),
    MarriageAllowanceReceivedAmount -> calcExp("ctnMarriageAllceInAmt"),
    OrdinaryRateAmount              -> calcExp("ctnDividendTaxLowRate"),
    ScottishHigherIncome            -> calcExp(
      "ctnIncomeChgbleHigherRate",
      "ctnTaxableRedundancyHr",
      "itfStatePensionLsGrossAmt:null"
    ),
    ScottishStarterRateTax          -> calcExp("taxOnPaySSR", "ctnTaxOnRedundancySsr", "ctnPensionLsumTaxDueAmt:null"),
    AdditionalRate                  -> calcExp("ctnDividendChgbleAddHRate"),
    StartingRateForSavings          -> calcExp("savingsRateScottish2023:null"),
    AdditionalRateIncomeTax         -> calcExp("additionalRateIncomeTaxScottish2023:null"),
    SavingsAdditionalIncome         -> calcExp("ctnSavingsChgbleAddHRate"),
    SavingsHigherIncome             -> calcExp("ctnSavingsChgbleHigherRate"),
    ScottishAdditionalRateTax       -> calcExp(
      "ctnIncomeTaxAddHighRate",
      "ctnTaxOnRedundancyAhr",
      "ctnPensionLsumTaxDueAmt:null"
    ),
    OtherAdjustmentsIncreasing      -> calcExp(
      fieldsOtherAdjustmentsIncreasing: _*
    ),
    HigherRateIncomeTax             -> calcExp("higherRateIncomeTaxScottish2023:null"),
    ScottishBasicRateTax            -> calcExp("ctnIncomeTaxBasicRate", "ctnTaxOnRedundancyBr", "ctnPensionLsumTaxDueAmt:null"),
    BasicRateIncomeTaxAmount        -> calcExp("basicRateIncomeTaxAmountScottish2023:null"),
    AdditionalRateAmount            -> calcExp("ctnDividendTaxAddHighRate"),
    WelshIncomeTax                  -> calcExp("welshIncomeTax:null"),
    ScottishAdditionalIncome        -> calcExp(
      "ctnIncomeChgbleAddHRate",
      "ctnTaxableRedundancyAhr",
      "itfStatePensionLsGrossAmt:null"
    ),
    ScottishIntermediateIncome      -> calcExp("taxablePaySIR", "ctnTaxableRedundancySir", "itfStatePensionLsGrossAmt:null"),
    UpperRateAmount                 -> calcExp("ctnDividendTaxHighRate"),
    AdditionalRateIncomeTaxAmount   -> calcExp("additionalRateIncomeTaxAmountScottish2023:null"),
    ScottishBasicIncome             -> calcExp(
      "ctnIncomeChgbleBasicRate",
      "ctnTaxableRedundancyBr",
      "itfStatePensionLsGrossAmt:null"
    ),
    ScottishTotalTax                -> calcExp(
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
    ),
    BasicRateIncomeTax              -> calcExp("basicRateIncomeTaxScottish2023:null"),
    SavingsAdditionalRateTax        -> calcExp("ctnSavingsTaxAddHighRate", "ctnTaxOnCegAhr"),
    HigherRateIncomeTaxAmount       -> calcExp("higherRateIncomeTaxAmountScottish2023:null"),
    TotalIncomeTax                  -> expTotalIncomeTax,
    SavingsHigherRateTax            -> calcExp("ctnSavingsTaxHigherRate"),
    OrdinaryRate                    -> calcExp("ctnDividendChgbleLowRate"),
    ScottishHigherRateTax           -> calcExp("ctnIncomeTaxHigherRate", "ctnTaxOnRedundancyHr", "ctnPensionLsumTaxDueAmt:null"),
    ScottishStarterIncome           -> calcExp("taxablePaySSR", "ctnTaxableRedundancySsr", "itfStatePensionLsGrossAmt:null")
  )

  private val expectedResultIncomeData: Map[LiabilityKey, Amount] = Map(
    SelfEmploymentIncome   -> calcExp(
      "ctnSummaryTotalScheduleD",
      "ctnSummaryTotalPartnership",
      "ctnSavingsPartnership",
      "ctnDividendsPartnership"
    ),
    IncomeFromEmployment   -> calcExp("ctnSummaryTotalEmployment"),
    StatePension           -> calcExp("atsStatePensionAmt"),
    OtherPensionIncome     -> calcExp("atsOtherPensionAmt", "itfStatePensionLsGrossAmt"),
    TotalIncomeBeforeTax   -> amt(
      BigDecimal(55364.00),
      "0.00(ctnSummaryTotalScheduleD) + 0.00(ctnSummaryTotalPartnership) + 0.00(ctnSavingsPartnership) + 0.00(ctnDividendsPartnership) + 23678.00(ctnSummaryTotalEmployment) + 9783.00(atsStatePensionAmt) + 0.00(atsOtherPensionAmt) + 0.00(itfStatePensionLsGrossAmt) + 0.00(atsIncBenefitSuppAllowAmt) + 0.00(atsJobSeekersAllowanceAmt) + 0.00(atsOthStatePenBenefitsAmt) + 0.00(ctnSummaryTotShareOptions) + 5475.00(ctnSummaryTotalUklProperty) + 0.00(ctnSummaryTotForeignIncome) + 0.00(ctnSummaryTotTrustEstates) + 0.00(ctnSummaryTotalOtherIncome) + 3678.00(ctnSummaryTotalUkInterest) + 0.00(ctnSummaryTotForeignDiv) + 12750.00(ctnSummaryTotalUkIntDivs) + 0.00(ctn4SumTotLifePolicyGains) + 0.00(ctnSummaryTotForeignSav) + 0.00(ctnForeignCegDedn) + 0.00(itfCegReceivedAfterTax) + 0.00(ctnEmploymentBenefitsAmt)"
    ),
    OtherIncome            -> amt(
      BigDecimal(21903.00),
      "0.00(ctnSummaryTotShareOptions) + 5475.00(ctnSummaryTotalUklProperty) + 0.00(ctnSummaryTotForeignIncome) + 0.00(ctnSummaryTotTrustEstates) + 0.00(ctnSummaryTotalOtherIncome) + 3678.00(ctnSummaryTotalUkInterest) + 0.00(ctnSummaryTotForeignDiv) + 12750.00(ctnSummaryTotalUkIntDivs) + 0.00(ctn4SumTotLifePolicyGains) + 0.00(ctnSummaryTotForeignSav) + 0.00(ctnForeignCegDedn) + 0.00(itfCegReceivedAfterTax)"
    ),
    BenefitsFromEmployment -> calcExp("ctnEmploymentBenefitsAmt"),
    TaxableStateBenefits   -> calcExp(
      "atsIncBenefitSuppAllowAmt",
      "atsJobSeekersAllowanceAmt",
      "atsOthStatePenBenefitsAmt"
    )
  )

  private val expectedResultAllowanceData: Map[LiabilityKey, Amount] = Map(
    PersonalTaxFreeAmount              -> calcExp("ctnPersonalAllowance"),
    MarriageAllowanceTransferredAmount -> calcExp("ctnMarriageAllceOutAmt"),
    OtherAllowancesAmount              -> calcExp(
      "ctnEmploymentExpensesAmt",
      "ctnSummaryTotalDedPpr",
      "ctnSumTotForeignTaxRelief",
      "ctnSumTotLossRestricted",
      "grossAnnuityPayts",
      "itf4GiftsInvCharitiesAmo",
      "ctnBpaAllowanceAmt",
      "itfBpaAmount"
    ),
    TotalTaxFreeAmount                 -> expTotalTaxFreeAmount
  )

  private val expectedResultCGData: Map[LiabilityKey, Amount] = Map(
    AmountDueRPCILowerRate       -> calcExp("ctnLowerRateCgtRPCI"),
    AmountAtHigherRate           -> calcExp("ctnCgAtHigherRate"),
    Adjustments                  -> calcExp("capAdjustmentAmt"),
    AmountAtOrdinaryRate         -> calcExp("ctnCgAtLowerRate"),
    AmountAtRPCIHigheRate        -> calcExp("ctnCGAtHigherRateRPCI"),
    AmountDueAtEntrepreneursRate -> calcExp("ctnCgDueEntrepreneursRate"),
    CgTaxPerCurrencyUnit         -> calcExp("atsCgTotGainsAfterLosses", "atsCgGainsAfterLossesAmt"),
    TaxableGains                 -> calcExp("atsCgTotGainsAfterLosses", "atsCgGainsAfterLossesAmt"),
    AmountDueAtOrdinaryRate      -> calcExp("ctnCgDueLowerRate"),
    PayCgTaxOn                   -> amt(BigDecimal(0), "null (taxableGains() < get(CgAnnualExempt))"),
    TotalCgTax                   -> amt(
      BigDecimal(0.00),
      "max(0, Some(0.00(ctnLowerRateCgtRPCI) + 0.00(ctnHigherRateCgtRPCI) + 0.00(ctnCgDueEntrepreneursRate) + 0.00(ctnCgDueLowerRate) + 0.00(ctnCgDueHigherRate) + 0.00(capAdjustmentAmt)))"
    ),
    AmountAtEntrepreneursRate    -> calcExp("ctnCgAtEntrepreneursRate"),
    LessTaxFreeAmount            -> calcExp("atsCgAnnualExemptAmt"),
    AmountDueRPCIHigherRate      -> calcExp("ctnHigherRateCgtRPCI"),
    AmountDueAtHigherRate        -> calcExp("ctnCgDueHigherRate"),
    AmountAtRPCILowerRate        -> calcExp("ctnCGAtLowerRateRPCI")
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

  private def buildJsonPayload(tliSlpAtsData: Map[String, BigDecimal]): JsObject = {
    val tliSlpAtsDataAsJsObject = tliSlpAtsData.foldLeft[JsObject](
      Json.obj(
        "incomeTaxStatus"          -> "0002",
        "tliLastUpdated"           -> "2022-09-01",
        "ctnPensionLumpSumTaxRate" -> 0.00
      )
    ) { (c, i) =>
      c ++ Json.obj(
        i._1 -> Json.obj(
          "amount"   -> i._2.setScale(2),
          "currency" -> "GBP"
        )
      )
    }
    Json.obj(
      "taxYear" -> taxYear,
      "saPayeNicDetails" -> Json.obj(
        "employeeClass1Nic" -> Json.obj(
          "amount"   -> BigDecimal(100.00).setScale(2),
          "currency" -> "GBP"
        ),
        "employeeClass2Nic" -> Json.obj(
          "amount"   -> BigDecimal(200.00).setScale(2),
          "currency" -> "GBP"
        ),
        "employerNic"       -> Json.obj(
          "amount"   -> BigDecimal(0.00).setScale(2),
          "currency" -> "GBP"
        )
      ),
      "tliSlpAtsData"    -> tliSlpAtsDataAsJsObject
    )
  }
}
