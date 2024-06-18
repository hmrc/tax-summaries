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

package utils

import play.api.libs.json._
import transformers.{ATSParsingException, ATSRawDataTransformer}
import utils.TestConstants._

class TaxsJsonHelperTest extends BaseSpec {
  private val prevTaxYear = 2022
  private val taxYear     = 2023

  val aTSRawDataTransformer: ATSRawDataTransformer = inject[ATSRawDataTransformer]

  private val summaryJson = JsonUtil.load("/sa/sa_ats_valid.json", Map("<taxYear>" -> taxYear.toString))

  class SetUp extends TaxsJsonHelper(applicationConfig, aTSRawDataTransformer)

  "hasAtsForPreviousPeriod" must {

    "return true when json response has non empty annual tax summaries data" in new SetUp {

      val rawJson: JsValue = Json.parse(s"""
           | {
           |   "annualTaxSummaries" : [
           |   { "taxYearEnd" : $prevTaxYear },
           |   { "taxYearEnd" : $taxYear }
           |   ]
           | }
        """.stripMargin)

      val result: Boolean = hasAtsForPreviousPeriod(rawJson)

      result mustBe true
    }

    "return false when json response has no annual tax summaries data" in new SetUp {

      val rawJson: JsValue = Json.parse("""
          | {
          |   "annualTaxSummaries" : []
          | }
        """.stripMargin)

      val result: Boolean = hasAtsForPreviousPeriod(rawJson)

      result mustBe false
    }

    "return false for badly formed json" in new SetUp {

      val rawJson: JsValue = Json.parse(s"""
           | {
           |   "annualTaxSummaries" : [
           |   { "userName" : "" }
           |   ],
           |   "taxYearEnd" : $prevTaxYear
           | }
        """.stripMargin)

      val result: Boolean = hasAtsForPreviousPeriod(rawJson)

      result mustBe false
    }
  }

  "createTaxYearJson" must {

    "return a jsValue with correct data when passed correct format" in new SetUp {

      val rawJson: JsValue = Json.parse(s"""
           | {
           |   "annualTaxSummaries" : [
           |   { "taxYearEnd" : $prevTaxYear },
           |   { "taxYearEnd" : $taxYear }
           |   ]
           | }
        """.stripMargin)

      val rawTaxpayerJson: JsValue = Json.parse("""
          |{
          |  "name": {
          |    "title": "Mr",
          |    "forename": "forename",
          |    "surname": "surname"
          |  }
          | }
        """.stripMargin)

      val result: JsValue = createTaxYearJson(rawJson, testUtr, rawTaxpayerJson)

      result \ "utr" mustBe JsDefined(JsString(testUtr))
      result \ "taxPayer" mustBe JsDefined(
        Json.parse("""{"taxpayer_name":{"title":"Mr","forename":"forename","surname":"surname"}}""")
      )
      result \ "atsYearList" mustBe JsDefined(Json.parse(s"[$prevTaxYear, $taxYear]"))
    }

    "return an exception when passed badly formed json" in new SetUp {

      val rawJson: JsValue = Json.parse(s"""
           | {
           |   "annualTaxSummaries" : [
           |   { "taxYearEnd" : $taxYear }
           |   ]
           | }
        """.stripMargin)

      val rawTaxpayerJson: JsValue = Json.parse("""
          |{
          |  "name": {
          |    "title": "Mr"
          |  },
          |  "forename": "forename",
          |  "surname": "surname"
          |}
        """.stripMargin)

      intercept[ATSParsingException] {
        createTaxYearJson(rawJson, testUtr, rawTaxpayerJson)
      }
    }

  }

  "getAllATSData" must {

    "return a jsValue with correct data when asked for the value with calculus" in new SetUp {

      val resultJson: JsValue      = Json.parse("""
          |{
          |  "income_tax": [
          |    {
          |      "fieldName": "starting_rate_for_savings_amount",
          |      "amount": 0,
          |      "calculus": "0.00(ctnSavingsTaxStartingRate) + 0.00(ctnTaxOnCegSr)"
          |    },
          |    {
          |      "fieldName": "other_adjustments_reducing",
          |      "amount": 5163,
          |      "calculus": "0.00(ctnDeficiencyRelief) + 0.00(topSlicingRelief) + 0.00(ctnVctSharesReliefAmt) + 0.00(ctnEisReliefAmt) + 0.00(ctnSeedEisReliefAmt) + 0.00(ctnCommInvTrustRelAmt) + 0.00(ctnSocialInvTaxRelAmt) + 333.70(atsSurplusMcaAlimonyRel) + 0.00(alimony) + 2000.00(ctnNotionalTaxCegs) + 0.00(ctnNotlTaxOthrSrceAmo) + 2442.00(ctnFtcrRestricted) + 386.60(reliefForFinanceCosts) + 0.00(lfiRelief) + 0.00(ctnRelTaxAcctFor)"
          |    },
          |    {
          |      "fieldName": "upper_rate",
          |      "amount": 0,
          |      "calculus": "0.00(ctnDividendChgbleHighRate)"
          |    },
          |    {
          |      "fieldName": "savings_lower_income",
          |      "amount": 0,
          |      "calculus": "null (savingsBasicRateIncome)"
          |    },
          |    {
          |      "fieldName": "savings_lower_rate_tax",
          |      "amount": 0,
          |      "calculus": "null (savingsBasicRateTax)"
          |    },
          |    {
          |      "fieldName": "scottish_income_tax",
          |      "amount": 0,
          |      "calculus": "null (scottishIncomeTaxUK2023)"
          |    },
          |    {
          |      "fieldName": "scottish_intermediate_rate_tax",
          |      "amount": 0,
          |      "calculus": "null (scottishIntermediateRateTax)"
          |    },
          |    {
          |      "fieldName": "marriage_allowance_received_amount",
          |      "amount": 0,
          |      "calculus": "0.00(ctnMarriageAllceInAmt)"
          |    },
          |    {
          |      "fieldName": "ordinary_rate_amount",
          |      "amount": 0,
          |      "calculus": "0.00(ctnDividendTaxLowRate)"
          |    },
          |    {
          |      "fieldName": "scottish_higher_income",
          |      "amount": 0,
          |      "calculus": "null (scottishHigherRateIncome)"
          |    },
          |    {
          |      "fieldName": "scottish_starter_rate_tax",
          |      "amount": 0,
          |      "calculus": "null (scottishStarterRateTax)"
          |    },
          |    {
          |      "fieldName": "additional_rate",
          |      "amount": 58632,
          |      "calculus": "58632.00(ctnDividendChgbleAddHRate)"
          |    },
          |    {
          |      "fieldName": "starting_rate_for_savings",
          |      "amount": 0,
          |      "calculus": "0.00(ctnSavingsChgbleStartRate) + 0.00(ctnTaxableCegSr)"
          |    },
          |    {
          |      "fieldName": "additional_rate_income_tax",
          |      "amount": 206995,
          |      "calculus": "145613.00(ctnIncomeChgbleAddHRate) + 36382.00(ctnSavingsChgbleAddHRate) + 15000.00(ctnTaxableRedundancyAhr) + 10000.00(ctnTaxableCegAhr) + null (itfStatePensionLsGrossAmt)"
          |    },
          |    {
          |      "fieldName": "savings_additional_income",
          |      "amount": 0,
          |      "calculus": "null (savingsAdditionalRateIncome)"
          |    },
          |    {
          |      "fieldName": "savings_higher_income",
          |      "amount": 0,
          |      "calculus": "null (savingsHigherRateIncome)"
          |    },
          |    {
          |      "fieldName": "scottish_additional_rate_tax",
          |      "amount": 0,
          |      "calculus": "null (scottishAdditionalRateTax)"
          |    },
          |    {
          |      "fieldName": "other_adjustments_increasing",
          |      "amount": 0,
          |      "calculus": "0.00(nonDomChargeAmount) + 0.00(giftAidTaxReduced) + 0.00(netAnnuityPaytsTaxDue) + 0.00(ctnChildBenefitChrgAmt) + 0.00(ctnPensionSavingChrgbleAmt)"
          |    },
          |    {
          |      "fieldName": "higher_rate_income_tax",
          |      "amount": 112300,
          |      "calculus": "112300.00(ctnIncomeChgbleHigherRate) + 0.00(ctnSavingsChgbleHigherRate) + 0.00(ctnTaxableRedundancyHr) + 0.00(ctnTaxableCegHr) + null (itfStatePensionLsGrossAmt)"
          |    },
          |    {
          |      "fieldName": "scottish_basic_rate_tax",
          |      "amount": 0,
          |      "calculus": "null (scottishBasicRateTax)"
          |    },
          |    {
          |      "fieldName": "basic_rate_income_tax_amount",
          |      "amount": 9043.6,
          |      "calculus": "9043.60(ctnIncomeTaxBasicRate) + 0.00(ctnSavingsTaxLowerRate) + 0.00(ctnTaxOnRedundancyBr) + 0.00(ctnTaxOnCegBr) + null (ctnPensionLsumTaxDueAmt)"
          |    },
          |    {
          |      "fieldName": "additional_rate_amount",
          |      "amount": 23071.69,
          |      "calculus": "23071.69(ctnDividendTaxAddHighRate)"
          |    },
          |    {
          |      "fieldName": "welsh_income_tax",
          |      "amount": 0,
          |      "calculus": "null (welshIncomeTax)"
          |    },
          |    {
          |      "fieldName": "scottish_additional_income",
          |      "amount": 0,
          |      "calculus": "null (scottishAdditionalRateIncome)"
          |    },
          |    {
          |      "fieldName": "scottish_intermediate_income",
          |      "amount": 0,
          |      "calculus": "null (scottishIntermediateRateIncome)"
          |    },
          |    {
          |      "fieldName": "upper_rate_amount",
          |      "amount": 0,
          |      "calculus": "0.00(ctnDividendTaxHighRate)"
          |    },
          |    {
          |      "fieldName": "additional_rate_income_tax_amount",
          |      "amount": 93147.75,
          |      "calculus": "65525.85(ctnIncomeTaxAddHighRate) + 16371.90(ctnSavingsTaxAddHighRate) + 6750.00(ctnTaxOnRedundancyAhr) + 4500.00(ctnTaxOnCegAhr) + null (ctnPensionLsumTaxDueAmt)"
          |    },
          |    {
          |      "fieldName": "scottish_basic_income",
          |      "amount": 0,
          |      "calculus": "null (scottishBasicRateIncome)"
          |    },
          |    {
          |      "fieldName": "scottish_total_tax",
          |      "amount": 0,
          |      "calculus": "null (scottishStarterRateTax) + null (scottishBasicRateTax) + null (scottishIntermediateRateTax) + null (scottishHigherRateTax) + null (scottishAdditionalRateTax)"
          |    },
          |    {
          |      "fieldName": "basic_rate_income_tax",
          |      "amount": 45218,
          |      "calculus": "45218.00(ctnIncomeChgbleBasicRate) + 0.00(ctnSavingsChgbleLowerRate) + 0.00(ctnTaxableRedundancyBr) + 0.00(ctnTaxableCegBr) + null (itfStatePensionLsGrossAmt)"
          |    },
          |    {
          |      "fieldName": "savings_additional_rate_tax",
          |      "amount": 0,
          |      "calculus": "null (savingsAdditionalRateTax)"
          |    },
          |    {
          |      "fieldName": "higher_rate_income_tax_amount",
          |      "amount": 44920,
          |      "calculus": "44920.00(ctnIncomeTaxHigherRate) + 0.00(ctnSavingsTaxHigherRate) + 0.00(ctnTaxOnRedundancyHr) + 0.00(ctnTaxOnCegHr) + null (ctnPensionLsumTaxDueAmt)"
          |    },
          |    {
          |      "fieldName": "total_income_tax",
          |      "amount": 165020.74,
          |      "calculus": "0.00(ctnSavingsTaxStartingRate) + 0.00(ctnTaxOnCegSr) + 9043.60(ctnIncomeTaxBasicRate) + 0.00(ctnSavingsTaxLowerRate) + 0.00(ctnTaxOnRedundancyBr) + 0.00(ctnTaxOnCegBr) + null (ctnPensionLsumTaxDueAmt) + 44920.00(ctnIncomeTaxHigherRate) + 0.00(ctnSavingsTaxHigherRate) + 0.00(ctnTaxOnRedundancyHr) + 0.00(ctnTaxOnCegHr) + null (ctnPensionLsumTaxDueAmt) + 65525.85(ctnIncomeTaxAddHighRate) + 16371.90(ctnSavingsTaxAddHighRate) + 6750.00(ctnTaxOnRedundancyAhr) + 4500.00(ctnTaxOnCegAhr) + null (ctnPensionLsumTaxDueAmt) + 0.00(ctnDividendTaxLowRate) + 0.00(ctnDividendTaxHighRate) + 23071.69(ctnDividendTaxAddHighRate) + 0.00(nonDomChargeAmount) + 0.00(giftAidTaxReduced) + 0.00(netAnnuityPaytsTaxDue) + 0.00(ctnChildBenefitChrgAmt) + 0.00(ctnPensionSavingChrgbleAmt) - 0.00(ctnDeficiencyRelief) + 0.00(topSlicingRelief) + 0.00(ctnVctSharesReliefAmt) + 0.00(ctnEisReliefAmt) + 0.00(ctnSeedEisReliefAmt) + 0.00(ctnCommInvTrustRelAmt) + 0.00(ctnSocialInvTaxRelAmt) + 333.70(atsSurplusMcaAlimonyRel) + 0.00(alimony) + 2000.00(ctnNotionalTaxCegs) + 0.00(ctnNotlTaxOthrSrceAmo) + 2442.00(ctnFtcrRestricted) + 386.60(reliefForFinanceCosts) + 0.00(lfiRelief) + 0.00(ctnRelTaxAcctFor) - 0.00(ctnMarriageAllceInAmt)"
          |    },
          |    {
          |      "fieldName": "savings_higher_rate_tax",
          |      "amount": 0,
          |      "calculus": "null (savingsHigherRateTax)"
          |    },
          |    {
          |      "fieldName": "ordinary_rate",
          |      "amount": 0,
          |      "calculus": "0.00(ctnDividendChgbleLowRate)"
          |    },
          |    {
          |      "fieldName": "scottish_higher_rate_tax",
          |      "amount": 0,
          |      "calculus": "null (scottishHigherRateTax)"
          |    },
          |    {
          |      "fieldName": "scottish_starter_income",
          |      "amount": 0,
          |      "calculus": "null (scottishStarterRateIncome)"
          |    }
          |  ],
          |  "summary_data": [
          |    {
          |      "fieldName": "total_income_tax_and_nics",
          |      "amount": 165184.54,
          |      "calculus": "0.00(employeeClass1Nic) + 163.80(ctnClass2NicAmt) + 0.00(class4Nic) + 0.00(ctnSavingsTaxStartingRate) + 0.00(ctnTaxOnCegSr) + 9043.60(ctnIncomeTaxBasicRate) + 0.00(ctnSavingsTaxLowerRate) + 0.00(ctnTaxOnRedundancyBr) + 0.00(ctnTaxOnCegBr) + null (ctnPensionLsumTaxDueAmt) + 44920.00(ctnIncomeTaxHigherRate) + 0.00(ctnSavingsTaxHigherRate) + 0.00(ctnTaxOnRedundancyHr) + 0.00(ctnTaxOnCegHr) + null (ctnPensionLsumTaxDueAmt) + 65525.85(ctnIncomeTaxAddHighRate) + 16371.90(ctnSavingsTaxAddHighRate) + 6750.00(ctnTaxOnRedundancyAhr) + 4500.00(ctnTaxOnCegAhr) + null (ctnPensionLsumTaxDueAmt) + 0.00(ctnDividendTaxLowRate) + 0.00(ctnDividendTaxHighRate) + 23071.69(ctnDividendTaxAddHighRate) + 0.00(nonDomChargeAmount) + 0.00(giftAidTaxReduced) + 0.00(netAnnuityPaytsTaxDue) + 0.00(ctnChildBenefitChrgAmt) + 0.00(ctnPensionSavingChrgbleAmt) - 0.00(ctnDeficiencyRelief) + 0.00(topSlicingRelief) + 0.00(ctnVctSharesReliefAmt) + 0.00(ctnEisReliefAmt) + 0.00(ctnSeedEisReliefAmt) + 0.00(ctnCommInvTrustRelAmt) + 0.00(ctnSocialInvTaxRelAmt) + 333.70(atsSurplusMcaAlimonyRel) + 0.00(alimony) + 2000.00(ctnNotionalTaxCegs) + 0.00(ctnNotlTaxOthrSrceAmo) + 2442.00(ctnFtcrRestricted) + 386.60(reliefForFinanceCosts) + 0.00(lfiRelief) + 0.00(ctnRelTaxAcctFor) - 0.00(ctnMarriageAllceInAmt)"
          |    },
          |    {
          |      "fieldName": "nics_and_tax_per_currency_unit",
          |      "amount": 0.3749,
          |      "calculus": "0.00(employeeClass1Nic) + 163.80(ctnClass2NicAmt) + 0.00(class4Nic) + 0.00(ctnSavingsTaxStartingRate) + 0.00(ctnTaxOnCegSr) + 9043.60(ctnIncomeTaxBasicRate) + 0.00(ctnSavingsTaxLowerRate) + 0.00(ctnTaxOnRedundancyBr) + 0.00(ctnTaxOnCegBr) + null (ctnPensionLsumTaxDueAmt) + 44920.00(ctnIncomeTaxHigherRate) + 0.00(ctnSavingsTaxHigherRate) + 0.00(ctnTaxOnRedundancyHr) + 0.00(ctnTaxOnCegHr) + null (ctnPensionLsumTaxDueAmt) + 65525.85(ctnIncomeTaxAddHighRate) + 16371.90(ctnSavingsTaxAddHighRate) + 6750.00(ctnTaxOnRedundancyAhr) + 4500.00(ctnTaxOnCegAhr) + null (ctnPensionLsumTaxDueAmt) + 0.00(ctnDividendTaxLowRate) + 0.00(ctnDividendTaxHighRate) + 23071.69(ctnDividendTaxAddHighRate) + 0.00(nonDomChargeAmount) + 0.00(giftAidTaxReduced) + 0.00(netAnnuityPaytsTaxDue) + 0.00(ctnChildBenefitChrgAmt) + 0.00(ctnPensionSavingChrgbleAmt) - 0.00(ctnDeficiencyRelief) + 0.00(topSlicingRelief) + 0.00(ctnVctSharesReliefAmt) + 0.00(ctnEisReliefAmt) + 0.00(ctnSeedEisReliefAmt) + 0.00(ctnCommInvTrustRelAmt) + 0.00(ctnSocialInvTaxRelAmt) + 333.70(atsSurplusMcaAlimonyRel) + 0.00(alimony) + 2000.00(ctnNotionalTaxCegs) + 0.00(ctnNotlTaxOthrSrceAmo) + 2442.00(ctnFtcrRestricted) + 386.60(reliefForFinanceCosts) + 0.00(lfiRelief) + 0.00(ctnRelTaxAcctFor) - 0.00(ctnMarriageAllceInAmt)"
          |    },
          |    {
          |      "fieldName": "cg_tax_per_currency_unit",
          |      "amount": 0.1107,
          |      "calculus": "max(0, Some(0.00(ctnLowerRateCgtRPCI) + 3340.00(ctnHigherRateCgtRPCI) + 1200.00(ctnCgDueEntrepreneursRate) + 0.00(ctnCgDueLowerRate) + 0.00(ctnCgDueHigherRate) + 0.00(capAdjustmentAmt)))"
          |    },
          |    {
          |      "fieldName": "total_income_before_tax",
          |      "amount": 440596,
          |      "calculus": "0.00(ctnSummaryTotalScheduleD) + 942.00(ctnSummaryTotalPartnership) + 0.00(ctnSavingsPartnership) + 0.00(ctnDividendsPartnership) + 122500.00(ctnSummaryTotalEmployment) + 3770.00(atsStatePensionAmt) + 3121.00(atsOtherPensionAmt) + 0.00(itfStatePensionLsGrossAmt) + 0.00(atsIncBenefitSuppAllowAmt) + 0.00(atsJobSeekersAllowanceAmt) + 0.00(atsOthStatePenBenefitsAmt) + 6250.00(ctnSummaryTotShareOptions) + 3600.00(ctnSummaryTotalUklProperty) + 9687.00(ctnSummaryTotForeignIncome) + 248649.00(ctnSummaryTotTrustEstates) + 720.00(ctnSummaryTotalOtherIncome) + 12913.00(ctnSummaryTotalUkInterest) + 2679.00(ctnSummaryTotForeignDiv) + 2603.00(ctnSummaryTotalUkIntDivs) + 0.00(ctn4SumTotLifePolicyGains) + 3562.00(ctnSummaryTotForeignSav) + 0.00(ctnForeignCegDedn) + 10000.00(itfCegReceivedAfterTax) + 9600.00(ctnEmploymentBenefitsAmt)"
          |    },
          |    {
          |      "fieldName": "total_cg_tax",
          |      "amount": 4540,
          |      "calculus": "max(0, Some(0.00(ctnLowerRateCgtRPCI) + 3340.00(ctnHigherRateCgtRPCI) + 1200.00(ctnCgDueEntrepreneursRate) + 0.00(ctnCgDueLowerRate) + 0.00(ctnCgDueHigherRate) + 0.00(capAdjustmentAmt)))"
          |    },
          |    {
          |      "fieldName": "your_total_tax",
          |      "amount": 169724.54,
          |      "calculus": "0.00(employeeClass1Nic) + 163.80(ctnClass2NicAmt) + 0.00(class4Nic) + 0.00(ctnSavingsTaxStartingRate) + 0.00(ctnTaxOnCegSr) + 9043.60(ctnIncomeTaxBasicRate) + 0.00(ctnSavingsTaxLowerRate) + 0.00(ctnTaxOnRedundancyBr) + 0.00(ctnTaxOnCegBr) + null (ctnPensionLsumTaxDueAmt) + 44920.00(ctnIncomeTaxHigherRate) + 0.00(ctnSavingsTaxHigherRate) + 0.00(ctnTaxOnRedundancyHr) + 0.00(ctnTaxOnCegHr) + null (ctnPensionLsumTaxDueAmt) + 65525.85(ctnIncomeTaxAddHighRate) + 16371.90(ctnSavingsTaxAddHighRate) + 6750.00(ctnTaxOnRedundancyAhr) + 4500.00(ctnTaxOnCegAhr) + null (ctnPensionLsumTaxDueAmt) + 0.00(ctnDividendTaxLowRate) + 0.00(ctnDividendTaxHighRate) + 23071.69(ctnDividendTaxAddHighRate) + 0.00(nonDomChargeAmount) + 0.00(giftAidTaxReduced) + 0.00(netAnnuityPaytsTaxDue) + 0.00(ctnChildBenefitChrgAmt) + 0.00(ctnPensionSavingChrgbleAmt) - 0.00(ctnDeficiencyRelief) + 0.00(topSlicingRelief) + 0.00(ctnVctSharesReliefAmt) + 0.00(ctnEisReliefAmt) + 0.00(ctnSeedEisReliefAmt) + 0.00(ctnCommInvTrustRelAmt) + 0.00(ctnSocialInvTaxRelAmt) + 333.70(atsSurplusMcaAlimonyRel) + 0.00(alimony) + 2000.00(ctnNotionalTaxCegs) + 0.00(ctnNotlTaxOthrSrceAmo) + 2442.00(ctnFtcrRestricted) + 386.60(reliefForFinanceCosts) + 0.00(lfiRelief) + 0.00(ctnRelTaxAcctFor) - 0.00(ctnMarriageAllceInAmt) + max(0, Some(0.00(ctnLowerRateCgtRPCI) + 3340.00(ctnHigherRateCgtRPCI) + 1200.00(ctnCgDueEntrepreneursRate) + 0.00(ctnCgDueLowerRate) + 0.00(ctnCgDueHigherRate) + 0.00(capAdjustmentAmt)))"
          |    },
          |    {
          |      "fieldName": "total_tax_free_amount",
          |      "amount": 15451,
          |      "calculus": "2235.00(ctnEmploymentExpensesAmt) + 681.00(ctnSummaryTotalDedPpr) + 0.00(ctnSumTotForeignTaxRelief) + 12535.00(ctnSumTotLossRestricted) + 0.00(grossAnnuityPayts) + 0.00(itf4GiftsInvCharitiesAmo) + 0.00(ctnBpaAllowanceAmt) + 0.00(itfBpaAmount) + 0.00(ctnPersonalAllowance) - 0.00(ctnMarriageAllceOutAmt)"
          |    },
          |    {
          |      "fieldName": "total_income_tax",
          |      "amount": 165020.74,
          |      "calculus": "0.00(ctnSavingsTaxStartingRate) + 0.00(ctnTaxOnCegSr) + 9043.60(ctnIncomeTaxBasicRate) + 0.00(ctnSavingsTaxLowerRate) + 0.00(ctnTaxOnRedundancyBr) + 0.00(ctnTaxOnCegBr) + null (ctnPensionLsumTaxDueAmt) + 44920.00(ctnIncomeTaxHigherRate) + 0.00(ctnSavingsTaxHigherRate) + 0.00(ctnTaxOnRedundancyHr) + 0.00(ctnTaxOnCegHr) + null (ctnPensionLsumTaxDueAmt) + 65525.85(ctnIncomeTaxAddHighRate) + 16371.90(ctnSavingsTaxAddHighRate) + 6750.00(ctnTaxOnRedundancyAhr) + 4500.00(ctnTaxOnCegAhr) + null (ctnPensionLsumTaxDueAmt) + 0.00(ctnDividendTaxLowRate) + 0.00(ctnDividendTaxHighRate) + 23071.69(ctnDividendTaxAddHighRate) + 0.00(nonDomChargeAmount) + 0.00(giftAidTaxReduced) + 0.00(netAnnuityPaytsTaxDue) + 0.00(ctnChildBenefitChrgAmt) + 0.00(ctnPensionSavingChrgbleAmt) - 0.00(ctnDeficiencyRelief) + 0.00(topSlicingRelief) + 0.00(ctnVctSharesReliefAmt) + 0.00(ctnEisReliefAmt) + 0.00(ctnSeedEisReliefAmt) + 0.00(ctnCommInvTrustRelAmt) + 0.00(ctnSocialInvTaxRelAmt) + 333.70(atsSurplusMcaAlimonyRel) + 0.00(alimony) + 2000.00(ctnNotionalTaxCegs) + 0.00(ctnNotlTaxOthrSrceAmo) + 2442.00(ctnFtcrRestricted) + 386.60(reliefForFinanceCosts) + 0.00(lfiRelief) + 0.00(ctnRelTaxAcctFor) - 0.00(ctnMarriageAllceInAmt)"
          |    },
          |    {
          |      "fieldName": "personal_tax_free_amount",
          |      "amount": 0,
          |      "calculus": "0.00(ctnPersonalAllowance)"
          |    },
          |    {
          |      "fieldName": "employee_nic_amount",
          |      "amount": 163.8,
          |      "calculus": "0.00(employeeClass1Nic) + 163.80(ctnClass2NicAmt) + 0.00(class4Nic)"
          |    },
          |    {
          |      "fieldName": "taxable_gains",
          |      "amount": 41000,
          |      "calculus": "2000.00(atsCgTotGainsAfterLosses) + 39000.00(atsCgGainsAfterLossesAmt)"
          |    }
          |  ],
          |  "income_data": [
          |    {
          |      "fieldName": "income_from_employment",
          |      "amount": 122500,
          |      "calculus": "122500.00(ctnSummaryTotalEmployment)"
          |    },
          |    {
          |      "fieldName": "state_pension",
          |      "amount": 3770,
          |      "calculus": "3770.00(atsStatePensionAmt)"
          |    },
          |    {
          |      "fieldName": "taxable_state_benefits",
          |      "amount": 0,
          |      "calculus": "0.00(atsIncBenefitSuppAllowAmt) + 0.00(atsJobSeekersAllowanceAmt) + 0.00(atsOthStatePenBenefitsAmt)"
          |    },
          |    {
          |      "fieldName": "self_employment_income",
          |      "amount": 942,
          |      "calculus": "0.00(ctnSummaryTotalScheduleD) + 942.00(ctnSummaryTotalPartnership) + 0.00(ctnSavingsPartnership) + 0.00(ctnDividendsPartnership)"
          |    },
          |    {
          |      "fieldName": "total_income_before_tax",
          |      "amount": 440596,
          |      "calculus": "0.00(ctnSummaryTotalScheduleD) + 942.00(ctnSummaryTotalPartnership) + 0.00(ctnSavingsPartnership) + 0.00(ctnDividendsPartnership) + 122500.00(ctnSummaryTotalEmployment) + 3770.00(atsStatePensionAmt) + 3121.00(atsOtherPensionAmt) + 0.00(itfStatePensionLsGrossAmt) + 0.00(atsIncBenefitSuppAllowAmt) + 0.00(atsJobSeekersAllowanceAmt) + 0.00(atsOthStatePenBenefitsAmt) + 6250.00(ctnSummaryTotShareOptions) + 3600.00(ctnSummaryTotalUklProperty) + 9687.00(ctnSummaryTotForeignIncome) + 248649.00(ctnSummaryTotTrustEstates) + 720.00(ctnSummaryTotalOtherIncome) + 12913.00(ctnSummaryTotalUkInterest) + 2679.00(ctnSummaryTotForeignDiv) + 2603.00(ctnSummaryTotalUkIntDivs) + 0.00(ctn4SumTotLifePolicyGains) + 3562.00(ctnSummaryTotForeignSav) + 0.00(ctnForeignCegDedn) + 10000.00(itfCegReceivedAfterTax) + 9600.00(ctnEmploymentBenefitsAmt)"
          |    },
          |    {
          |      "fieldName": "benefits_from_employment",
          |      "amount": 9600,
          |      "calculus": "9600.00(ctnEmploymentBenefitsAmt)"
          |    },
          |    {
          |      "fieldName": "other_pension_income",
          |      "amount": 3121,
          |      "calculus": "3121.00(atsOtherPensionAmt) + 0.00(itfStatePensionLsGrossAmt)"
          |    },
          |    {
          |      "fieldName": "other_income",
          |      "amount": 300663,
          |      "calculus": "6250.00(ctnSummaryTotShareOptions) + 3600.00(ctnSummaryTotalUklProperty) + 9687.00(ctnSummaryTotForeignIncome) + 248649.00(ctnSummaryTotTrustEstates) + 720.00(ctnSummaryTotalOtherIncome) + 12913.00(ctnSummaryTotalUkInterest) + 2679.00(ctnSummaryTotForeignDiv) + 2603.00(ctnSummaryTotalUkIntDivs) + 0.00(ctn4SumTotLifePolicyGains) + 3562.00(ctnSummaryTotForeignSav) + 0.00(ctnForeignCegDedn) + 10000.00(itfCegReceivedAfterTax)"
          |    }
          |  ],
          |  "allowance_data": [
          |    {
          |      "fieldName": "personal_tax_free_amount",
          |      "amount": 0,
          |      "calculus": "0.00(ctnPersonalAllowance)"
          |    },
          |    {
          |      "fieldName": "marriage_allowance_transferred_amount",
          |      "amount": 0,
          |      "calculus": "0.00(ctnMarriageAllceOutAmt)"
          |    },
          |    {
          |      "fieldName": "other_allowances_amount",
          |      "amount": 15451,
          |      "calculus": "2235.00(ctnEmploymentExpensesAmt) + 681.00(ctnSummaryTotalDedPpr) + 0.00(ctnSumTotForeignTaxRelief) + 12535.00(ctnSumTotLossRestricted) + 0.00(grossAnnuityPayts) + 0.00(itf4GiftsInvCharitiesAmo) + 0.00(ctnBpaAllowanceAmt) + 0.00(itfBpaAmount)"
          |    },
          |    {
          |      "fieldName": "total_tax_free_amount",
          |      "amount": 15451,
          |      "calculus": "2235.00(ctnEmploymentExpensesAmt) + 681.00(ctnSummaryTotalDedPpr) + 0.00(ctnSumTotForeignTaxRelief) + 12535.00(ctnSumTotLossRestricted) + 0.00(grossAnnuityPayts) + 0.00(itf4GiftsInvCharitiesAmo) + 0.00(ctnBpaAllowanceAmt) + 0.00(itfBpaAmount) + 0.00(ctnPersonalAllowance) - 0.00(ctnMarriageAllceOutAmt)"
          |    }
          |  ],
          |  "capital_gains_data": [
          |    {
          |      "fieldName": "personal_tax_free_amount",
          |      "amount": 0,
          |      "calculus": "0.00(ctnPersonalAllowance)"
          |    },
          |    {
          |      "fieldName": "marriage_allowance_transferred_amount",
          |      "amount": 0,
          |      "calculus": "0.00(ctnMarriageAllceOutAmt)"
          |    },
          |    {
          |      "fieldName": "other_allowances_amount",
          |      "amount": 15451,
          |      "calculus": "2235.00(ctnEmploymentExpensesAmt) + 681.00(ctnSummaryTotalDedPpr) + 0.00(ctnSumTotForeignTaxRelief) + 12535.00(ctnSumTotLossRestricted) + 0.00(grossAnnuityPayts) + 0.00(itf4GiftsInvCharitiesAmo) + 0.00(ctnBpaAllowanceAmt) + 0.00(itfBpaAmount)"
          |    },
          |    {
          |      "fieldName": "total_tax_free_amount",
          |      "amount": 15451,
          |      "calculus": "2235.00(ctnEmploymentExpensesAmt) + 681.00(ctnSummaryTotalDedPpr) + 0.00(ctnSumTotForeignTaxRelief) + 12535.00(ctnSumTotLossRestricted) + 0.00(grossAnnuityPayts) + 0.00(itf4GiftsInvCharitiesAmo) + 0.00(ctnBpaAllowanceAmt) + 0.00(itfBpaAmount) + 0.00(ctnPersonalAllowance) - 0.00(ctnMarriageAllceOutAmt)"
          |    }
          |  ]
          |}
                          """.stripMargin)
      val rawTaxpayerJson: JsValue = Json.parse("""
          |{
          |  "name": {
          |    "title": "Mr",
          |    "forename": "forename",
          |    "surname": "surname"
          |  }
          | }
        """.stripMargin)

      val result: JsValue =
        getAllATSData(rawTaxpayerJson, Json.parse(summaryJson), testUtr, taxYear, withCalculus = true)

      result \ "utr" mustBe JsDefined(JsString(testUtr))
      result \ "odsValues" mustBe JsDefined(
        resultJson
      )
    }
  }

}
