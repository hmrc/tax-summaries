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

package transformers

import models.LiabilityKey.{OtherAdjustmentsIncreasing, OtherAdjustmentsReducing}
import models.{Amount, AtsMiddleTierData, TaxSummaryLiability}
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import services.TaxRateService
import utils._

import scala.io.Source

class OtherAdjustmentsTransformerTest extends BaseSpec with AtsJsonDataUpdate {

  val taxpayerDetailsJson        = Source.fromURL(getClass.getResource("/taxpayerData/test_individual_utr.json")).mkString
  val parsedTaxpayerDetailsJson  = Json.parse(taxpayerDetailsJson)
  val taxYear: Int               = 2014
  val taxRate                    = new TaxRateService(taxYear, applicationConfig.ratePercentages)
  val SUT: ATSRawDataTransformer = inject[ATSRawDataTransformer]

  "With base data for utr" must {

    "have the correct adjustment data" in {

      val sampleJson = Source.fromURL(getClass.getResource("/utr_2014.json")).mkString

      val parsedJson   = Json.parse(sampleJson)
      val calculations = ATSCalculations.make(parsedJson.as[TaxSummaryLiability], taxRate)

      val returnValue: AtsMiddleTierData =
        SUT.atsDataDTO(taxRate, calculations, parsedTaxpayerDetailsJson, "", taxYear)

      val parsedYear    = returnValue.taxYear
      val testYear: Int = 2014
      testYear mustEqual parsedYear

      val parsedPayload = returnValue.income_tax.get.payload.get

      parsedPayload(OtherAdjustmentsIncreasing) must equal(
        new Amount(
          0.0,
          "GBP",
          Some(
            "0.00(nonDomChargeAmount) + 0.00(taxExcluded) + 0.00(incomeTaxDue) + 0.00(netAnnuityPaytsTaxDue) + 0.00(ctnChildBenefitChrgAmt) + 0.00(ctnPensionSavingChrgbleAmt) - 0.00(ctn4TaxDueAfterAllceRlf)"
          )
        )
      )
      parsedPayload(OtherAdjustmentsReducing)   must equal(
        new Amount(
          200.0,
          "GBP",
          Some(
            "10.00(ctnDeficiencyRelief) + 10.00(topSlicingRelief) + 10.00(ctnVctSharesReliefAmt) + 10.00(ctnEisReliefAmt) + 10.00(ctnSeedEisReliefAmt) + 10.00(ctnCommInvTrustRelAmt) + 10.00(atsSurplusMcaAlimonyRel) + 10.00(ctnNotionalTaxCegs) + 10.00(ctnNotlTaxOthrSrceAmo) + 10.00(ctnTaxCredForDivs) + 10.00(ctnQualDistnReliefAmt) + 10.00(figTotalTaxCreditRelief) + 80.00(ctnNonPayableTaxCredits) + 0.00(reliefForFinanceCosts) + 0(lfiRelief) + 0(alimony)"
          )
        )
      )
    }

    "have the correct adjustment data with Relief for Financial Costs" in {

      val originalJson = getClass.getResource("/utr_2014.json")

      val update = Json.obj("reliefForFinanceCosts" -> Amount(20.0, "GBP"))

      val transformedJson = transformation(sourceJson = originalJson, tliSlpAtsUpdate = update)
      val calculations    = ATSCalculations.make(transformedJson.as[TaxSummaryLiability], taxRate)

      val returnValue: AtsMiddleTierData =
        SUT.atsDataDTO(taxRate, calculations, parsedTaxpayerDetailsJson, "", taxYear)

      val parsedYear    = returnValue.taxYear
      val testYear: Int = 2014
      testYear mustEqual parsedYear

      val parsedPayload = returnValue.income_tax.get.payload.get

      parsedPayload(OtherAdjustmentsIncreasing) must equal(
        new Amount(
          0.0,
          "GBP",
          Some(
            "0.00(nonDomChargeAmount) + 0.00(taxExcluded) + 0.00(incomeTaxDue) + 0.00(netAnnuityPaytsTaxDue) + 0.00(ctnChildBenefitChrgAmt) + 0.00(ctnPensionSavingChrgbleAmt) - 0.00(ctn4TaxDueAfterAllceRlf)"
          )
        )
      )
      parsedPayload(OtherAdjustmentsReducing)   must equal(
        new Amount(
          220.0,
          "GBP",
          Some(
            "10.00(ctnDeficiencyRelief) + 10.00(topSlicingRelief) + 10.00(ctnVctSharesReliefAmt) + 10.00(ctnEisReliefAmt) + 10.00(ctnSeedEisReliefAmt) + 10.00(ctnCommInvTrustRelAmt) + 10.00(atsSurplusMcaAlimonyRel) + 10.00(ctnNotionalTaxCegs) + 10.00(ctnNotlTaxOthrSrceAmo) + 10.00(ctnTaxCredForDivs) + 10.00(ctnQualDistnReliefAmt) + 10.00(figTotalTaxCreditRelief) + 80.00(ctnNonPayableTaxCredits) + 20.0(reliefForFinanceCosts) + 0(lfiRelief) + 0(alimony)"
          )
        )
      )
    }
    "have a correct 'other_adjustments_reducing' roundup data" in {

      val originalJson = getClass.getResource("/utr_2014.json")

      val update = Json.obj("ctnDeficiencyRelief" -> Amount(9.01, "GBP"))

      val transformedJson = transformation(sourceJson = originalJson, tliSlpAtsUpdate = update)
      val calculations    = ATSCalculations.make(transformedJson.as[TaxSummaryLiability], taxRate)

      val returnValue: AtsMiddleTierData =
        SUT.atsDataDTO(taxRate, calculations, parsedTaxpayerDetailsJson, "", taxYear)

      val parsedYear    = returnValue.taxYear
      val testYear: Int = 2014
      testYear mustEqual parsedYear

      val parsedPayload = returnValue.income_tax.get.payload.get

      parsedPayload(OtherAdjustmentsReducing) must equal(
        new Amount(
          200.0,
          "GBP",
          Some(
            "9.01(ctnDeficiencyRelief) + 10.00(topSlicingRelief) + 10.00(ctnVctSharesReliefAmt) + 10.00(ctnEisReliefAmt) + 10.00(ctnSeedEisReliefAmt) + 10.00(ctnCommInvTrustRelAmt) + 10.00(atsSurplusMcaAlimonyRel) + 10.00(ctnNotionalTaxCegs) + 10.00(ctnNotlTaxOthrSrceAmo) + 10.00(ctnTaxCredForDivs) + 10.00(ctnQualDistnReliefAmt) + 10.00(figTotalTaxCreditRelief) + 80.00(ctnNonPayableTaxCredits) + 0.00(reliefForFinanceCosts) + 0(lfiRelief) + 0(alimony)"
          )
        )
      )
    }

    "have the correct adjustment increase data" in {

      val originalJson = getClass.getResource("/utr_2014.json")

      val update = Json.obj(
        "nonDomChargeAmount"         -> Amount(11.0, "GBP"),
        "taxExcluded"                -> Amount(11.0, "GBP"),
        "incomeTaxDue"               -> Amount(12.0, "GBP"),
        "ctn4TaxDueAfterAllceRlf"    -> Amount(11.0, "GBP"),
        "netAnnuityPaytsTaxDue"      -> Amount(11.0, "GBP"),
        "ctnChildBenefitChrgAmt"     -> Amount(11.0, "GBP"),
        "ctnPensionSavingChrgbleAmt" -> Amount(11.0, "GBP")
      )

      val transformedJson = transformation(sourceJson = originalJson, tliSlpAtsUpdate = update)
      val calculations    = ATSCalculations.make(transformedJson.as[TaxSummaryLiability], taxRate)

      val returnValue: AtsMiddleTierData =
        SUT.atsDataDTO(taxRate, calculations, parsedTaxpayerDetailsJson, "", taxYear)

      val parsedYear    = returnValue.taxYear
      val testYear: Int = 2014
      testYear mustEqual parsedYear

      val parsedPayload = returnValue.income_tax.get.payload.get

      parsedPayload(OtherAdjustmentsIncreasing) must equal(
        new Amount(
          56.0,
          "GBP",
          Some(
            "11.0(nonDomChargeAmount) + 11.0(taxExcluded) + 12.0(incomeTaxDue) + 11.0(netAnnuityPaytsTaxDue) + 11.0(ctnChildBenefitChrgAmt) + 11.0(ctnPensionSavingChrgbleAmt) - 11.0(ctn4TaxDueAfterAllceRlf)"
          )
        )
      )
      parsedPayload(OtherAdjustmentsReducing)   must equal(
        new Amount(
          200.0,
          "GBP",
          Some(
            "10.00(ctnDeficiencyRelief) + 10.00(topSlicingRelief) + 10.00(ctnVctSharesReliefAmt) + 10.00(ctnEisReliefAmt) + 10.00(ctnSeedEisReliefAmt) + 10.00(ctnCommInvTrustRelAmt) + 10.00(atsSurplusMcaAlimonyRel) + 10.00(ctnNotionalTaxCegs) + 10.00(ctnNotlTaxOthrSrceAmo) + 10.00(ctnTaxCredForDivs) + 10.00(ctnQualDistnReliefAmt) + 10.00(figTotalTaxCreditRelief) + 80.00(ctnNonPayableTaxCredits) + 0.00(reliefForFinanceCosts) + 0(lfiRelief) + 0(alimony)"
          )
        )
      )
    }

    "have a correct 'other_adjustments_reducing' roundup data when alimony is not given in payload" in {

      val originalJson = getClass.getResource("/utr_2014.json")

      val parsedJson   = Json.parse(Source.fromURL(originalJson).mkString)
      val calculations = ATSCalculations.make(parsedJson.as[TaxSummaryLiability], taxRate)

      val returnValue: AtsMiddleTierData =
        SUT.atsDataDTO(taxRate, calculations, parsedTaxpayerDetailsJson, "", taxYear)

      returnValue.taxYear mustEqual 2014

      val parsedPayload = returnValue.income_tax.get.payload.get

      parsedPayload(OtherAdjustmentsReducing) must equal(
        new Amount(
          200.0,
          "GBP",
          Some(
            "10.00(ctnDeficiencyRelief) + 10.00(topSlicingRelief) + 10.00(ctnVctSharesReliefAmt) + 10.00(ctnEisReliefAmt) + 10.00(ctnSeedEisReliefAmt) + 10.00(ctnCommInvTrustRelAmt) + 10.00(atsSurplusMcaAlimonyRel) + 10.00(ctnNotionalTaxCegs) + 10.00(ctnNotlTaxOthrSrceAmo) + 10.00(ctnTaxCredForDivs) + 10.00(ctnQualDistnReliefAmt) + 10.00(figTotalTaxCreditRelief) + 80.00(ctnNonPayableTaxCredits) + 0.00(reliefForFinanceCosts) + 0(lfiRelief) + 0(alimony)"
          )
        )
      )
    }

    "have a correct 'other_adjustments_reducing' roundup data when alimony is given" in {

      val originalJson = getClass.getResource("/utr_2014.json")

      val update = Json.obj("alimony" -> Amount(9.01, "GBP"))

      val transformedJson = transformation(sourceJson = originalJson, tliSlpAtsUpdate = update)

      val taxYear2020: Int = 2020
      val taxRate          = new TaxRateService(taxYear2020, applicationConfig.ratePercentages)

      val calculations = ATSCalculations.make(transformedJson.as[TaxSummaryLiability], taxRate)

      val returnValue: AtsMiddleTierData =
        SUT.atsDataDTO(taxRate, calculations, parsedTaxpayerDetailsJson, "", taxYear2020)

      returnValue.taxYear mustEqual 2020

      val parsedPayload = returnValue.income_tax.get.payload.get

      parsedPayload(OtherAdjustmentsReducing) must equal(
        new Amount(
          210.0,
          "GBP",
          Some(
            "10.00(ctnDeficiencyRelief) + 10.00(topSlicingRelief) + 10.00(ctnVctSharesReliefAmt) + 10.00(ctnEisReliefAmt) + 10.00(ctnSeedEisReliefAmt) + 10.00(ctnCommInvTrustRelAmt) + 10.00(atsSurplusMcaAlimonyRel) + 10.00(ctnNotionalTaxCegs) + 10.00(ctnNotlTaxOthrSrceAmo) + 10.00(ctnTaxCredForDivs) + 10.00(ctnQualDistnReliefAmt) + 10.00(figTotalTaxCreditRelief) + 80.00(ctnNonPayableTaxCredits) + 0.00(reliefForFinanceCosts) + 0(lfiRelief) + 9.01(alimony)"
          )
        )
      )
    }
  }
}
