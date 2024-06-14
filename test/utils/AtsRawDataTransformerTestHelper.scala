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

package utils

import models.{Amount, AtsMiddleTierData, LiabilityKey}
import play.api.libs.json.{JsObject, JsValue, Json}
import transformers.ATSRawDataTransformer

trait AtsRawDataTransformerTestHelper extends BaseSpec {
  protected val taxYear: Int
  protected val incomeTaxStatus: String

  protected def tliSlpAtsData: Map[String, BigDecimal]    = Map(
    "ctnEmploymentBenefitsAmt"   -> BigDecimal(10.00),
    "ctnSummaryTotalScheduleD"   -> BigDecimal(20.00),
    "ctnSummaryTotalPartnership" -> BigDecimal(30.00),
    "ctnSummaryTotalEmployment"  -> BigDecimal(23678.00),
    "atsStatePensionAmt"         -> BigDecimal(9783.00),
    "atsOtherPensionAmt"         -> BigDecimal(40.00),
    "itfStatePensionLsGrossAmt"  -> BigDecimal(50.00),
    "atsIncBenefitSuppAllowAmt"  -> BigDecimal(60.00),
    "atsJobSeekersAllowanceAmt"  -> BigDecimal(70.00),
    "atsOthStatePenBenefitsAmt"  -> BigDecimal(80.00),
    "ctnSummaryTotShareOptions"  -> BigDecimal(100.00),
    "ctnSummaryTotalUklProperty" -> BigDecimal(5475.00),
    "ctnSummaryTotForeignIncome" -> BigDecimal(110.00),
    "ctnSummaryTotTrustEstates"  -> BigDecimal(120.00),
    "ctnSummaryTotalOtherIncome" -> BigDecimal(130.00),
    "ctnSummaryTotForeignSav"    -> BigDecimal(140.00),
    "ctnForeignCegDedn"          -> BigDecimal(150.00),
    "ctnSummaryTotalUkInterest"  -> BigDecimal(3678.00),
    "itfCegReceivedAfterTax"     -> BigDecimal(160.00),
    "ctnSummaryTotForeignDiv"    -> BigDecimal(170.00),
    "ctnSummaryTotalUkIntDivs"   -> BigDecimal(12750.00),
    "ctn4SumTotLifePolicyGains"  -> BigDecimal(180.00),
    "ctnPersonalAllowance"       -> BigDecimal(12570.00),
    "ctnEmploymentExpensesAmt"   -> BigDecimal(1900.00),
    "ctnSummaryTotalDedPpr"      -> BigDecimal(200.00),
    "ctnSumTotForeignTaxRelief"  -> BigDecimal(210.00),
    "ctnSumTotLoanRestricted"    -> BigDecimal(220.00),
    "ctnSumTotLossRestricted"    -> BigDecimal(230.00),
    "grossAnnuityPayts"          -> BigDecimal(240.00),
    "itf4GiftsInvCharitiesAmo"   -> BigDecimal(250.00),
    "itfTradeUnionDeathBenefits" -> BigDecimal(260.00),
    "ctnBpaAllowanceAmt"         -> BigDecimal(270.00),
    "itfBpaAmount"               -> BigDecimal(280.00),
    "grossExcludedIncome"        -> BigDecimal(290.00),
    "class4Nic"                  -> BigDecimal(300.00),
    "ctnClass2NicAmt"            -> BigDecimal(310.00),
    "ctnSavingsChgbleStartRate"  -> BigDecimal(320.00),
    "ctnSavingsTaxStartingRate"  -> BigDecimal(330.00),
    "ctnIncomeChgbleBasicRate"   -> BigDecimal(17419.00),
    "ctnSavingsChgbleLowerRate"  -> BigDecimal(2678.00),
    "ctnIncomeTaxBasicRate"      -> BigDecimal(3483.80),
    "ctnSavingsTaxLowerRate"     -> BigDecimal(535.60),
    "ctnIncomeChgbleHigherRate"  -> BigDecimal(340.00),
    "ctnSavingsChgbleHigherRate" -> BigDecimal(350.00),
    "ctnIncomeTaxHigherRate"     -> BigDecimal(360.00),
    "ctnSavingsTaxHigherRate"    -> BigDecimal(370.00),
    "ctnIncomeChgbleAddHRate"    -> BigDecimal(380.00),
    "ctnSavingsChgbleAddHRate"   -> BigDecimal(390.00),
    "ctnIncomeTaxAddHighRate"    -> BigDecimal(400.00),
    "ctnSavingsTaxAddHighRate"   -> BigDecimal(410.00),
    "taxablePaySSR"              -> BigDecimal(2097.00),
    "taxOnPaySSR"                -> BigDecimal(398.43),
    "taxablePaySIR"              -> BigDecimal(6850.00),
    "taxOnPaySIR"                -> BigDecimal(1438.50),
    "ctnDividendChgbleLowRate"   -> BigDecimal(10750.00),
    "ctnDividendTaxLowRate"      -> BigDecimal(806.25),
    "ctnDividendChgbleHighRate"  -> BigDecimal(420.00),
    "ctnDividendTaxHighRate"     -> BigDecimal(430.00),
    "ctnDividendChgbleAddHRate"  -> BigDecimal(440.00),
    "ctnDividendTaxAddHighRate"  -> BigDecimal(450.00),
    "ctnTaxableRedundancySSR"    -> BigDecimal(460.00),
    "ctnTaxOnRedundancySsr"      -> BigDecimal(470.00),
    "ctnTaxableRedundancyBr"     -> BigDecimal(480.00),
    "ctnTaxOnRedundancyBr"       -> BigDecimal(490.00),
    "ctnTaxableRedundancySir"    -> BigDecimal(500.00),
    "ctnTaxOnRedundancySir"      -> BigDecimal(510.00),
    "ctnTaxableRedundancyHr"     -> BigDecimal(520.00),
    "ctnTaxOnRedundancyHr"       -> BigDecimal(530.00),
    "ctnTaxableRedundancyAhr"    -> BigDecimal(540.00),
    "ctnTaxOnRedundancyAhr"      -> BigDecimal(550.00),
    "ctnTaxableCegBr"            -> BigDecimal(560.00),
    "ctnTaxOnCegBr"              -> BigDecimal(570.00),
    "ctnTaxableCegHr"            -> BigDecimal(580.00),
    "ctnTaxOnCegHr"              -> BigDecimal(590.00),
    "ctnTaxableCegAhr"           -> BigDecimal(600.00),
    "ctnTaxOnCegAhr"             -> BigDecimal(610.00),
    "nonDomChargeAmount"         -> BigDecimal(620.00),
    "taxExcluded"                -> BigDecimal(40000.00),
    "taxOnNonExcludedInc"        -> BigDecimal(60000.00),
    "incomeTaxDue"               -> BigDecimal(6162.58),
    "ctn4TaxDueAfterAllceRlf"    -> BigDecimal(6162.58),
    "netAnnuityPaytsTaxDue"      -> BigDecimal(650.00),
    "ctnChildBenefitChrgAmt"     -> BigDecimal(660.00),
    "ctnPensionSavingChrgbleAmt" -> BigDecimal(670.00),
    "atsTaxCharged"              -> BigDecimal(6662.58),
    "ctnDeficiencyRelief"        -> BigDecimal(680.00),
    "topSlicingRelief"           -> BigDecimal(690.00),
    "ctnVctSharesReliefAmt"      -> BigDecimal(700.00),
    "ctnEisReliefAmt"            -> BigDecimal(710.00),
    "ctnSeedEisReliefAmt"        -> BigDecimal(720.00),
    "ctnCommInvTrustRelAmt"      -> BigDecimal(730.00),
    "atsSurplusMcaAlimonyRel"    -> BigDecimal(740.00),
    "alimony"                    -> BigDecimal(750.00),
    "ctnNotionalTaxCegs"         -> BigDecimal(760.00),
    "ctnNotlTaxOthrSrceAmo"      -> BigDecimal(770.00),
    "ctnFtcrRestricted"          -> BigDecimal(780.00),
    "reliefForFinanceCosts"      -> BigDecimal(500.00),
    "lfiRelief"                  -> BigDecimal(790.00),
    "ctnRelTaxAcctFor"           -> BigDecimal(10.00),
    "ctnTaxCredForDivs"          -> BigDecimal(800.00),
    "ctnQualDistnReliefAmt"      -> BigDecimal(810.00),
    "figTotalTaxCreditRelief"    -> BigDecimal(820.00),
    "ctnNonPayableTaxCredits"    -> BigDecimal(830.00),
    "atsCgTotGainsAfterLosses"   -> BigDecimal(840.00),
    "atsCgGainsAfterLossesAmt"   -> BigDecimal(850.00),
    "cap3AssessableChgeableGain" -> BigDecimal(860.00),
    "atsCgAnnualExemptAmt"       -> BigDecimal(12300.00),
    "ctnCgAtEntrepreneursRate"   -> BigDecimal(870.00),
    "ctnCgDueEntrepreneursRate"  -> BigDecimal(880.00),
    "ctnCgAtLowerRate"           -> BigDecimal(890.00),
    "ctnCgDueLowerRate"          -> BigDecimal(900.00),
    "ctnCgAtHigherRate"          -> BigDecimal(910.00),
    "ctnCgDueHigherRate"         -> BigDecimal(920.00),
    "ctnCGAtLowerRateRPCI"       -> BigDecimal(930.00),
    "ctnLowerRateCgtRPCI"        -> BigDecimal(940.00),
    "ctnCGAtHigherRateRPCI"      -> BigDecimal(950.00),
    "ctnHigherRateCgtRPCI"       -> BigDecimal(960.00),
    "capAdjustmentAmt"           -> BigDecimal(970.00),
    "ctnPensionLsumTaxDueAmt"    -> BigDecimal(980.00),
    "ctnMarriageAllceInAmt"      -> BigDecimal(990.00),
    "ctnMarriageAllceOutAmt"     -> BigDecimal(1000.00),
    "ctnSocialInvTaxRelAmt"      -> BigDecimal(1010.00),
    "ctnSavingsPartnership"      -> BigDecimal(1020.00),
    "ctnDividendsPartnership"    -> BigDecimal(1030.00),
    "giftAidTaxReduced"          -> BigDecimal(1040.00),
    "ctnTaxableCegSr"            -> BigDecimal(1050.00),
    "ctnTaxOnCegSr"              -> BigDecimal(1060.00),
    "ctnTaxableRedundancySsr"    -> BigDecimal(1070.00)
  ).map(item => item._1 -> item._2.setScale(2))

  protected def saPayeNicDetails: Map[String, BigDecimal] = Map(
    "employeeClass1Nic" -> BigDecimal(1080.00),
    "employeeClass2Nic" -> BigDecimal(200.00),
    "employerNic"       -> BigDecimal(0.00)
  ).map(item => item._1 -> item._2.setScale(2))

  protected def parsedTaxpayerDetailsJson: JsValue        = Json.parse(JsonUtil.load("/taxpayer/sa_taxpayer-valid.json"))

  protected def doTest(jsonPayload: JsObject): AtsMiddleTierData = {
    val atsRawDataTransformer: ATSRawDataTransformer = inject[ATSRawDataTransformer]
    atsRawDataTransformer.atsDataDTO(jsonPayload, parsedTaxpayerDetailsJson, "", taxYear)
  }

  protected def transformedData: AtsMiddleTierData = doTest(buildJsonPayload())

  protected def atsRawDataTransformerWithCalculations(
    description: String,
    transformedData: AtsMiddleTierData,
    expResultIncomeTax: Map[LiabilityKey, Amount] = Map.empty,
    expResultIncomeData: Map[LiabilityKey, Amount] = Map.empty,
    expResultCapitalGainsData: Map[LiabilityKey, Amount] = Map.empty,
    expResultAllowanceData: Map[LiabilityKey, Amount] = Map.empty,
    expResultSummaryData: Map[LiabilityKey, Amount] = Map.empty
  ): Unit =
    Seq(
      ("income tax", transformedData.income_tax, expResultIncomeTax),
      ("income data", transformedData.income_data, expResultIncomeData),
      ("cap gains data", transformedData.capital_gains_data, expResultCapitalGainsData),
      ("allowance data", transformedData.allowance_data, expResultAllowanceData),
      ("summary data", transformedData.summary_data, expResultSummaryData)
    ).foreach { case (section, actualOptDataHolder, exp) =>
      val act = actualOptDataHolder.flatMap(_.payload).getOrElse(Map.empty)
      if (act.exists(a => exp.exists(_._1 == a._1))) {
        s"calculate $description field values correctly for $section" when {
          act.foreach { item =>
            exp.find(_._1 == item._1).map { actItem =>
              s"field ${item._1} calculated (act ${actItem._2.amount}, exp ${item._2.amount})" in {
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

  protected def calcExp(fieldNames: String*): Amount = {
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
        val bdValue = if (tliSlpAtsData.isDefinedAt(name)) {
          tliSlpAtsData(name)
        } else if (saPayeNicDetails.isDefinedAt(name)) {
          saPayeNicDetails(name)
        } else {
          throw new NoSuchElementException("key not found in either tliSlpAtsData or saPayeNicDetails: " + name)
        }
        Amount(bdValue, "GBP", Some(s"$bdValue($name)"))
      }
    }

    val initialValue = retrieveAmount(fieldNames.head)
    fieldNames.tail.foldLeft[Amount](initialValue) { (c, i) =>
      c + retrieveAmount(i)
    }
  }

  protected def buildJsonPayload(
    tliSlpAtsData: Map[String, BigDecimal] = tliSlpAtsData,
    saPayeNicDetails: Map[String, BigDecimal] = saPayeNicDetails
  ): JsObject = {
    val tliSlpAtsDataAsJsObject    = tliSlpAtsData.foldLeft[JsObject](
      Json.obj(
        "incomeTaxStatus"          -> incomeTaxStatus,
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
    val saPayeNicDetailsAsJsObject = saPayeNicDetails.foldLeft[JsObject](Json.obj()) { (c, i) =>
      c ++ Json.obj(
        i._1 -> Json.obj(
          "amount"   -> i._2.setScale(2),
          "currency" -> "GBP"
        )
      )
    }

    Json.obj(
      "taxYear"          -> taxYear,
      "saPayeNicDetails" -> saPayeNicDetailsAsJsObject,
      "tliSlpAtsData"    -> tliSlpAtsDataAsJsObject
    )
  }

}
