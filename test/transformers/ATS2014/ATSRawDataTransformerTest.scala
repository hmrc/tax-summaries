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

package transformers.ATS2014

import models.LiabilityKey._
import models._
import org.mockito.ArgumentMatchers.any
import org.mockito.{ArgumentCaptor, Mockito}
import play.api.libs.json.Json
import services.TaxRateService
import transformers.{ATSCalculations, ATSRawDataTransformer}
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.audit.model.DataEvent
import utils.{AtsJsonDataUpdate, BaseSpec, JsonUtil}

import scala.concurrent.ExecutionContext

class ATSRawDataTransformerTest extends BaseSpec with AtsJsonDataUpdate {

  val taxpayerDetailsJson       = JsonUtil.load("/taxpayerData/test_individual_utr.json")
  val parsedTaxpayerDetailsJson = Json.parse(taxpayerDetailsJson)
  val taxYear: Int              = 2014
  val taxRate                   = new TaxRateService(taxYear, applicationConfig.ratePercentages)

  val auditConnector: AuditConnector = mock[AuditConnector]
  implicit val ec: ExecutionContext  = inject[ExecutionContext]

  val SUT: ATSRawDataTransformer = new ATSRawDataTransformer(applicationConfig, auditConnector)

  override def beforeEach(): Unit = {
    Mockito.reset(auditConnector)
    super.beforeEach()
  }

  "The income before tax" must {

    "parse the income values for utr year:2014" in {

      val sampleJson = JsonUtil.load("/utr_2014.json")

      val parsedJson   = Json.parse(sampleJson).as[TaxSummaryLiability]
      val calculations = ATSCalculations.make(parsedJson, taxRate)

      val returnValue: AtsMiddleTierData =
        SUT.atsDataDTO(taxRate, calculations, parsedTaxpayerDetailsJson, "", taxYear)

      val parsedYear                                         = returnValue.taxYear
      val testYear: Int                                      = 2014
      testYear mustEqual parsedYear
      val dataEventArgumentCaptor: ArgumentCaptor[DataEvent] = ArgumentCaptor.forClass(classOf[DataEvent])

      val parsedPayload          = returnValue.income_data.get.payload.get
      val testPayload            =
        Map(
          SelfEmploymentIncome   -> Amount(1100.0, "GBP"),
          IncomeFromEmployment   -> Amount(10500.0, "GBP"),
          StatePension           -> Amount(0.0, "GBP"),
          OtherPensionIncome     -> Amount(0.0, "GBP"),
          TaxableStateBenefits   -> Amount(0.0, "GBP"),
          OtherIncome            -> Amount.empty("other_income"),
          BenefitsFromEmployment -> Amount(0.0, "GBP"),
          TotalIncomeBeforeTax   -> Amount(11600.00, "GBP")
        )
      testPayload.map(x => x._1 -> x._2.amount) mustEqual parsedPayload.map(x => x._1 -> x._2.amount)

      verify(auditConnector, times(1)).sendEvent(dataEventArgumentCaptor.capture())(any(), any())
      val valueOfLiabilityAmount = dataEventArgumentCaptor.getAllValues.get(0).detail.find(_._1 == "liabilityAmount")
      assert(valueOfLiabilityAmount.get._2.contains("172.00"))
    }

    "parse the income values for test case 2" in {
      val sampleJson = JsonUtil.load("/test_case_2.json")

      val parsedJson   = Json.parse(sampleJson).as[TaxSummaryLiability]
      val calculations = ATSCalculations.make(parsedJson, taxRate)

      val returnValue: AtsMiddleTierData =
        SUT.atsDataDTO(taxRate, calculations, parsedTaxpayerDetailsJson, "", taxYear)

      val parsedYear    = returnValue.taxYear
      val testYear: Int = 2014
      testYear mustEqual parsedYear

      val parsedPayload = returnValue.income_data.get.payload.get
      val testPayload   =
        Map(
          SelfEmploymentIncome   -> Amount(6.0, "GBP"),
          IncomeFromEmployment   -> Amount(8.0, "GBP"),
          StatePension           -> Amount(16.0, "GBP"),
          OtherPensionIncome     -> Amount(96.0, "GBP"),
          TaxableStateBenefits   -> Amount(896.0, "GBP"),
          OtherIncome            -> Amount(523264.00, "GBP"),
          BenefitsFromEmployment -> Amount(1.0, "GBP"),
          TotalIncomeBeforeTax   -> Amount(524287.00, "GBP")
        )
      testPayload.map(x => x._1 -> x._2.amount) mustEqual parsedPayload.map(x => x._1 -> x._2.amount)
    }

    "auditing event for no liability for the year 2014" in {

      val sampleJson = JsonUtil.load("/utr_2014_no_liability.json")

      val parsedJson   = Json.parse(sampleJson).as[TaxSummaryLiability]
      val calculations = ATSCalculations.make(parsedJson, taxRate)

      SUT.atsDataDTO(taxRate, calculations, parsedTaxpayerDetailsJson, "", taxYear)

      val dataEventArgumentCaptor: ArgumentCaptor[DataEvent] = ArgumentCaptor.forClass(classOf[DataEvent])

      verify(auditConnector, times(1)).sendEvent(dataEventArgumentCaptor.capture())(any(), any())
      val valueOfLiabilityAmount = dataEventArgumentCaptor.getAllValues.get(0).detail.find(_._1 == "liabilityAmount")
      assert(valueOfLiabilityAmount.get._2.contains("0.00"))
    }
  }

  "The summary page data" must {
    "parse the NICs data" in {
      val sampleJson = JsonUtil.load("/test_case_4.json")

      val parsedJson   = Json.parse(sampleJson).as[TaxSummaryLiability]
      val calculations = ATSCalculations.make(parsedJson, taxRate)

      val returnValue: AtsMiddleTierData =
        SUT.atsDataDTO(taxRate, calculations, parsedTaxpayerDetailsJson, "", taxYear)

      val parsedYear    = returnValue.taxYear
      val testYear: Int = 2014
      testYear mustEqual parsedYear

      val parsedPayload = returnValue.summary_data.get.payload.get
      val testPayload   =
        Map(
          EmployeeNicAmount         -> Amount(200.00, "GBP"),
          TotalIncomeTaxAndNics     -> Amount(554.83, "GBP"),
          YourTotalTax              -> Amount(554.83, "GBP"),
          PersonalTaxFreeAmount     -> Amount(9440.00, "GBP"),
          TotalTaxFreeAmount        -> Amount(9740.00, "GBP"),
          TotalIncomeBeforeTax      -> Amount(11600.00, "GBP"),
          TotalIncomeTax            -> Amount(354.83, "GBP"),
          TotalCgTax                -> Amount(0.00, "GBP"),
          TaxableGains              -> Amount(0.00, "GBP"),
          CgTaxPerCurrencyUnit      -> Amount(0.00, "GBP"),
          NicsAndTaxPerCurrencyUnit -> Amount(0.0478, "GBP")
        )
      testPayload.map(x => x._1 -> x._2.amount) mustEqual parsedPayload.map(x => x._1 -> x._2.amount)

      val parsedRates   = returnValue.summary_data.get.rates.get

      val testRates = Map(
        "total_cg_tax_rate" -> ApiRate("0%"),
        "nics_and_tax_rate" -> ApiRate("4.78%")
      )
      testRates mustEqual parsedRates.map { case (k, v) => (k.apiValue, v) }
    }

    "parse the NICs data with 'other_adjustments_reducing'" in {

      val update       = Json.obj("ctnDeficiencyRelief" -> Amount(0.01, "GBP"))
      val amendedJson  = JsonUtil.loadAndReplace("/test_case_4.json", update)
      val calculations = ATSCalculations.make(amendedJson.as[TaxSummaryLiability], taxRate)

      val returnValue: AtsMiddleTierData =
        SUT.atsDataDTO(taxRate, calculations, parsedTaxpayerDetailsJson, "", taxYear)

      val parsedYear    = returnValue.taxYear
      val testYear: Int = 2014
      testYear mustEqual parsedYear

      val parsedPayload = returnValue.summary_data.get.payload.get
      val testPayload   =
        Map(
          EmployeeNicAmount         -> Amount(200.00, "GBP"),
          TotalIncomeTaxAndNics     -> Amount(554.82, "GBP"),
          YourTotalTax              -> Amount(554.82, "GBP"),
          PersonalTaxFreeAmount     -> Amount(9440.00, "GBP"),
          TotalTaxFreeAmount        -> Amount(9740.00, "GBP"),
          TotalIncomeBeforeTax      -> Amount(11600.00, "GBP"),
          TotalIncomeTax            -> Amount(354.82, "GBP"),
          TotalCgTax                -> Amount(0.00, "GBP"),
          TaxableGains              -> Amount(0.00, "GBP"),
          CgTaxPerCurrencyUnit      -> Amount(0.00, "GBP"),
          NicsAndTaxPerCurrencyUnit -> Amount(0.0478, "GBP")
        )
      testPayload.map(x => x._1 -> x._2.amount) mustEqual parsedPayload.map(x => x._1 -> x._2.amount)

      val parsedRates   = returnValue.summary_data.get.rates.get

      val testRates = Map(
        "total_cg_tax_rate" -> ApiRate("0%"),
        "nics_and_tax_rate" -> ApiRate("4.78%")
      )

      testRates mustEqual parsedRates.map { case (k, v) => (k.apiValue, v) }
    }

    "parse the NICs data for utr year:2014" in {

      val sampleJson = JsonUtil.load("/test_case_5.json")

      val parsedJson   = Json.parse(sampleJson)
      val calculations = ATSCalculations.make(parsedJson.as[TaxSummaryLiability], taxRate)

      val returnValue: AtsMiddleTierData =
        SUT.atsDataDTO(taxRate, calculations, parsedTaxpayerDetailsJson, "", taxYear)

      val parsedYear    = returnValue.taxYear
      val testYear: Int = 2014
      testYear mustEqual parsedYear

      val parsedPayload = returnValue.summary_data.get.payload.get
      val testPayload   =
        Map(
          EmployeeNicAmount         -> Amount(200.0, "GBP"),
          TotalIncomeTaxAndNics     -> Amount(544.83, "GBP"),
          YourTotalTax              -> Amount(6099.83, "GBP"),
          PersonalTaxFreeAmount     -> Amount(9440.00, "GBP"),
          TotalTaxFreeAmount        -> Amount(9740.00, "GBP"),
          TotalIncomeBeforeTax      -> Amount(11600.00, "GBP"),
          TotalIncomeTax            -> Amount(344.83, "GBP"),
          TotalCgTax                -> Amount(5555.00, "GBP"),
          TaxableGains              -> Amount(12250.00, "GBP"),
          CgTaxPerCurrencyUnit      -> Amount(0.4534, "GBP"),
          NicsAndTaxPerCurrencyUnit -> Amount(0.0469, "GBP")
        )
      testPayload.map(x => x._1 -> x._2.amount) mustEqual parsedPayload.map(x => x._1 -> x._2.amount)

      val parsedRates   = returnValue.summary_data.get.rates.get

      val testRates = Map(
        "total_cg_tax_rate" -> ApiRate("45.34%"),
        "nics_and_tax_rate" -> ApiRate("4.69%")
      )
      testRates mustEqual parsedRates.map { case (k, v) => (k.apiValue, v) }
    }
  }

  "The total income before tax" must {
    "parse the tax rates transformation (based on utr year:2014 data)" in {

      val sampleJson = JsonUtil.load("/test_case_5.json")

      val parsedJson   = Json.parse(sampleJson)
      val calculations = ATSCalculations.make(parsedJson.as[TaxSummaryLiability], taxRate)

      val returnValue: AtsMiddleTierData =
        SUT.atsDataDTO(taxRate, calculations, parsedTaxpayerDetailsJson, "", taxYear)

      val parsedYear    = returnValue.taxYear
      val testYear: Int = 2014
      testYear mustEqual parsedYear

      val parsedPayload = returnValue.income_tax.get.payload.get
      val testPayload   =
        Map(
          StartingRateForSavings          -> Amount(0.00, "GBP"),
          StartingRateForSavingsAmount    -> Amount(0.00, "GBP"),
          BasicRateIncomeTax              -> Amount(1860.00, "GBP"),
          BasicRateIncomeTaxAmount        -> Amount(372.00, "GBP"),
          HigherRateIncomeTax             -> Amount(0.00, "GBP"),
          HigherRateIncomeTaxAmount       -> Amount(0.00, "GBP"),
          AdditionalRateIncomeTax         -> Amount(0.00, "GBP"),
          AdditionalRateIncomeTaxAmount   -> Amount(0.00, "GBP"),
          OrdinaryRate                    -> Amount(0.00, "GBP"),
          OrdinaryRateAmount              -> Amount(0.00, "GBP"),
          UpperRate                       -> Amount(0.00, "GBP"),
          UpperRateAmount                 -> Amount(0.00, "GBP"),
          AdditionalRate                  -> Amount(0.00, "GBP"),
          AdditionalRateAmount            -> Amount(0.00, "GBP"),
          OtherAdjustmentsIncreasing      -> Amount(0.00, "GBP"),
          MarriageAllowanceReceivedAmount -> Amount(0.00, "GBP"),
          OtherAdjustmentsReducing        -> Amount(28.0, "GBP"),
          ScottishIncomeTax               -> Amount(186.00, "GBP"),
          TotalIncomeTax                  -> Amount(344.83, "GBP")
        )
      parsedPayload.map(x => x._1 -> x._2.amount) must contain allElementsOf testPayload.map(x => x._1 -> x._2.amount)

      val parsedRates   = returnValue.income_tax.get.rates.get
      val testRates     =
        Map(
          "starting_rate_for_savings_rate"  -> ApiRate("10%"),
          "basic_rate_income_tax_rate"      -> ApiRate("20%"),
          "higher_rate_income_tax_rate"     -> ApiRate("40%"),
          "additional_rate_income_tax_rate" -> ApiRate("45%"),
          "ordinary_rate_tax_rate"          -> ApiRate("10%"),
          "upper_rate_rate"                 -> ApiRate("32.5%"),
          "additional_rate_rate"            -> ApiRate("37.5%"),
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

    "Calculate the correct Total Income Tax" in {

      val update = Json.obj(
        "ctnPensionLumpSumTaxRate"   -> 0.45,
        "ctnSavingsTaxStartingRate"  -> Amount(991.0, "GBP"),
        "ctnIncomeTaxBasicRate"      -> Amount(1153.0, "GBP"),
        "ctnSavingsTaxLowerRate"     -> Amount(1174.0, "GBP"),
        "ctnPensionLsumTaxDueAmt"    -> Amount(2458.0, "GBP"),
        "ctnIncomeTaxHigherRate"     -> Amount(1816.0, "GBP"),
        "ctnSavingsTaxHigherRate"    -> Amount(1725.0, "GBP"),
        "ctnIncomeTaxAddHighRate"    -> Amount(1366.0, "GBP"),
        "ctnSavingsTaxAddHighRate"   -> Amount(2061.0, "GBP"),
        "ctnDividendTaxLowRate"      -> Amount(293.0, "GBP"),
        "ctnDividendTaxHighRate"     -> Amount(487.0, "GBP"),
        "ctnDividendTaxAddHighRate"  -> Amount(725.0, "GBP"),
        "nonDomChargeAmount"         -> Amount(64.0, "GBP"),
        "taxExcluded"                -> Amount(88.0, "GBP"),
        "incomeTaxDue"               -> Amount(75.0, "GBP"),
        "netAnnuityPaytsTaxDue"      -> Amount(111.0, "GBP"),
        "ctnChildBenefitChrgAmt"     -> Amount(119.0, "GBP"),
        "ctnPensionSavingChrgbleAmt" -> Amount(127.0, "GBP"),
        "ctn4TaxDueAfterAllceRlf"    -> Amount(100.0, "GBP"),
        "ctnDeficiencyRelief"        -> Amount(612.2, "GBP"),
        "topSlicingRelief"           -> Amount(134.0, "GBP"),
        "ctnVctSharesReliefAmt"      -> Amount(532.0, "GBP"),
        "ctnEisReliefAmt"            -> Amount(762.0, "GBP"),
        "ctnSeedEisReliefAmt"        -> Amount(159.0, "GBP"),
        "ctnCommInvTrustRelAmt"      -> Amount(854.0, "GBP"),
        "atsSurplusMcaAlimonyRel"    -> Amount(137.0, "GBP"),
        "ctnNotionalTaxCegs"         -> Amount(99.0, "GBP"),
        "ctnNotlTaxOthrSrceAmo"      -> Amount(87.0, "GBP"),
        "ctnTaxCredForDivs"          -> Amount(166.0, "GBP"),
        "ctnQualDistnReliefAmt"      -> Amount(258, "GBP"),
        "figTotalTaxCreditRelief"    -> Amount(789.0, "GBP"),
        "ctnNonPayableTaxCredits"    -> Amount(198.0, "GBP"),
        "reliefForFinanceCosts"      -> Amount(469.0, "GBP"),
        "ctnMarriageAllceInAmt"      -> Amount(587.0, "GBP")
      )

      val amendedJson  = JsonUtil.loadAndReplace("/test_case_5.json", update)
      val calculations = ATSCalculations.make(amendedJson.as[TaxSummaryLiability], taxRate)

      val returnValue: AtsMiddleTierData =
        SUT.atsDataDTO(taxRate, calculations, parsedTaxpayerDetailsJson, "", taxYear)
      val parsedPayload                  = returnValue.income_tax.get.payload.get
      parsedPayload(TotalIncomeTax) mustEqual Amount(
        8872.63,
        "GBP",
        Some(
          "991.0(ctnSavingsTaxStartingRate) + 1153.0(ctnIncomeTaxBasicRate) + " +
            "1174.0(ctnSavingsTaxLowerRate) + null (ctnPensionLsumTaxDueAmt) + " +
            "1816.0(ctnIncomeTaxHigherRate) + 1725.0(ctnSavingsTaxHigherRate) + " +
            "null (ctnPensionLsumTaxDueAmt) + 1366.0(ctnIncomeTaxAddHighRate) + " +
            "2061.0(ctnSavingsTaxAddHighRate) + 2458.0(ctnPensionLsumTaxDueAmt) + " +
            "293.0(ctnDividendTaxLowRate) + 487.0(ctnDividendTaxHighRate) + " +
            "725.0(ctnDividendTaxAddHighRate) + 64.0(nonDomChargeAmount) + " +
            "88.0(taxExcluded) + 75.0(incomeTaxDue) + 111.0(netAnnuityPaytsTaxDue) + " +
            "119.0(ctnChildBenefitChrgAmt) + 127.0(ctnPensionSavingChrgbleAmt) - " +
            "100.0(ctn4TaxDueAfterAllceRlf) - 612.2(ctnDeficiencyRelief) + " +
            "134.0(topSlicingRelief) + 532.0(ctnVctSharesReliefAmt) + " +
            "762.0(ctnEisReliefAmt) + 159.0(ctnSeedEisReliefAmt) + " +
            "854.0(ctnCommInvTrustRelAmt) + 137.0(atsSurplusMcaAlimonyRel) + " +
            "99.0(ctnNotionalTaxCegs) + 87.0(ctnNotlTaxOthrSrceAmo) + " +
            "166.0(ctnTaxCredForDivs) + 258(ctnQualDistnReliefAmt) + " +
            "789.0(figTotalTaxCreditRelief) + 198.0(ctnNonPayableTaxCredits) + " +
            "469.0(reliefForFinanceCosts) + 17.17(lfiRelief) + " +
            "0(alimony) - 587.0(ctnMarriageAllceInAmt)"
        )
      )
    }

    "Calculate the Scottish Rate" in {

      val update = Json.obj(
        "ctnIncomeChgbleBasicRate"  -> Amount(469.0, "GBP"),
        "ctnIncomeChgbleHigherRate" -> Amount(267.0, "GBP"),
        "ctnIncomeChgbleAddHRate"   -> Amount(568.0, "GBP")
      )

      val amendedJson  = JsonUtil.loadAndReplace("/test_case_5.json", update)
      val calculations = ATSCalculations.make(amendedJson.as[TaxSummaryLiability], taxRate)

      val returnValue: AtsMiddleTierData =
        SUT.atsDataDTO(taxRate, calculations, parsedTaxpayerDetailsJson, "", taxYear)

      val parsedPayload = returnValue.income_tax.get.payload.get
      parsedPayload(ScottishIncomeTax) mustEqual Amount(
        130.4,
        "GBP",
        Some(
          "0.1 * (469.0(ctnIncomeChgbleBasicRate) + 267.0(ctnIncomeChgbleHigherRate) + 568.0(ctnIncomeChgbleAddHRate))"
        )
      )
    }

    "Calculate the Welsh Income Tax" in {

      val writTaxpayerDetailsJson = Json.parse(JsonUtil.load("/writ_values.json"))
      val calculations            = ATSCalculations.make(writTaxpayerDetailsJson.as[TaxSummaryLiability], taxRate)

      val returnValue: AtsMiddleTierData =
        SUT.atsDataDTO(taxRate, calculations, parsedTaxpayerDetailsJson, "", taxYear)

      val parsedPayload = returnValue.income_tax.get.payload.get
      parsedPayload(WelshIncomeTax) mustEqual Amount(
        186,
        "GBP",
        Some(
          "0.1 * (1860.00(ctnIncomeChgbleBasicRate) + 0.00(ctnIncomeChgbleHigherRate) + 0.00(ctnIncomeChgbleAddHRate))"
        )
      )
    }

    "ATS raw data transformer" must {
      "produce a no ats error if the total income tax is -500 and capital gains tax is 200" in {

        val sampleJson = JsonUtil.load("/test_case_7.json")

        val parsedJson   = Json.parse(sampleJson)
        val calculations = ATSCalculations.make(parsedJson.as[TaxSummaryLiability], taxRate)

        val returnValue: AtsMiddleTierData =
          SUT.atsDataDTO(taxRate, calculations, parsedTaxpayerDetailsJson, "", taxYear)

        returnValue.errors.get.error mustBe "NoAtsError"
      }

      "TotalCgTax is 500 when the total income tax is 200 and capital gains tax is -500" in {

        val sampleJson = JsonUtil.load("/test_case_8.json")

        val parsedJson   = Json.parse(sampleJson)
        val calculations = ATSCalculations.make(parsedJson.as[TaxSummaryLiability], taxRate)

        val returnValue: AtsMiddleTierData =
          SUT.atsDataDTO(taxRate, calculations, parsedTaxpayerDetailsJson, "", taxYear)

        val parsedPayload = returnValue.capital_gains_data.get.payload.get
        parsedPayload(TotalCgTax) mustEqual Amount(
          500,
          "GBP",
          Some(
            "max(0, Some(0.00(ctnLowerRateCgtRPCI) + 0.00(ctnHigherRateCgtRPCI) + 0.00(ctnCgDueEntrepreneursRate) + 0.00(ctnCgDueLowerRate) + 0.00(ctnCgDueHigherRate) + 500.00(capAdjustmentAmt)))"
          )
        )
      }
    }
  }
}
