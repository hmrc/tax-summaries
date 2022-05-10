/*
 * Copyright 2022 HM Revenue & Customs
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

import audit.AtsAudit
import models.LiabilityKey.{OtherAdjustmentsIncreasing, OtherAdjustmentsReducing}
import models.{Amount, AtsMiddleTierData, TaxSummaryLiability}
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import services.TaxRateService
import uk.gov.hmrc.http.HeaderCarrier
import utils._
import scala.concurrent.ExecutionContext.Implicits.global

import scala.io.Source

class OtherAdjustmentsTransformerTest extends BaseSpec with AtsJsonDataUpdate {

  val taxpayerDetailsJson = Source.fromURL(getClass.getResource("/taxpayerData/test_individual_utr.json")).mkString
  val parsedTaxpayerDetailsJson = Json.parse(taxpayerDetailsJson)
  val taxYear: Int = 2014
  val taxRate = new TaxRateService(taxYear, applicationConfig.ratePercentages)
  val SUT: ATSRawDataTransformer = inject[ATSRawDataTransformer]

  implicit val hc = HeaderCarrier()

  val atsAudit = mock[AtsAudit]

  "With base data for utr" must {

    "have the correct adjustment data" in {

      val sampleJson = Source.fromURL(getClass.getResource("/utr_2014.json")).mkString

      val parsedJson = Json.parse(sampleJson)
      val calculations = ATSCalculations.make(parsedJson.as[TaxSummaryLiability], taxRate)

      val returnValue: AtsMiddleTierData =
        SUT.atsDataDTO(taxRate, calculations, parsedTaxpayerDetailsJson, atsAudit, "", taxYear)

      val parsedYear = returnValue.taxYear
      val testYear: Int = 2014
      testYear mustEqual parsedYear

      val parsedPayload = returnValue.income_tax.get.payload.get

      parsedPayload(OtherAdjustmentsIncreasing) must equal(new Amount(0.0, "GBP"))
      parsedPayload(OtherAdjustmentsReducing) must equal(new Amount(200.0, "GBP"))
    }

    "have the correct adjustment data with Relief for Financial Costs" in {

      val originalJson = getClass.getResource("/utr_2014.json")

      val update = Json.obj("reliefForFinanceCosts" -> Amount(20.0, "GBP"))

      val transformedJson = transformation(sourceJson = originalJson, tliSlpAtsUpdate = update)
      val calculations = ATSCalculations.make(transformedJson.as[TaxSummaryLiability], taxRate)

      val returnValue: AtsMiddleTierData =
        SUT.atsDataDTO(taxRate, calculations, parsedTaxpayerDetailsJson, atsAudit, "", taxYear)

      val parsedYear = returnValue.taxYear
      val testYear: Int = 2014
      testYear mustEqual parsedYear

      val parsedPayload = returnValue.income_tax.get.payload.get

      parsedPayload(OtherAdjustmentsIncreasing) must equal(new Amount(0.0, "GBP"))
      parsedPayload(OtherAdjustmentsReducing) must equal(new Amount(220.0, "GBP"))
    }
    "have a correct 'other_adjustments_reducing' roundup data" in {

      val originalJson = getClass.getResource("/utr_2014.json")

      val update = Json.obj("ctnDeficiencyRelief" -> Amount(9.01, "GBP"))

      val transformedJson = transformation(sourceJson = originalJson, tliSlpAtsUpdate = update)
      val calculations = ATSCalculations.make(transformedJson.as[TaxSummaryLiability], taxRate)

      val returnValue: AtsMiddleTierData =
        SUT.atsDataDTO(taxRate, calculations, parsedTaxpayerDetailsJson, atsAudit, "", taxYear)

      val parsedYear = returnValue.taxYear
      val testYear: Int = 2014
      testYear mustEqual parsedYear

      val parsedPayload = returnValue.income_tax.get.payload.get

      parsedPayload(OtherAdjustmentsReducing) must equal(new Amount(200.0, "GBP"))
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
      val calculations = ATSCalculations.make(transformedJson.as[TaxSummaryLiability], taxRate)

      val returnValue: AtsMiddleTierData =
        SUT.atsDataDTO(taxRate, calculations, parsedTaxpayerDetailsJson, atsAudit, "", taxYear)

      val parsedYear = returnValue.taxYear
      val testYear: Int = 2014
      testYear mustEqual parsedYear

      val parsedPayload = returnValue.income_tax.get.payload.get

      parsedPayload(OtherAdjustmentsIncreasing) must equal(new Amount(56.0, "GBP"))
      parsedPayload(OtherAdjustmentsReducing) must equal(new Amount(200.0, "GBP"))
    }

    "have a correct 'other_adjustments_reducing' roundup data when alimony is not given in payload" in {

      val originalJson = getClass.getResource("/utr_2014.json")

      val parsedJson = Json.parse(Source.fromURL(originalJson).mkString)
      val calculations = ATSCalculations.make(parsedJson.as[TaxSummaryLiability], taxRate)

      val returnValue: AtsMiddleTierData =
        SUT.atsDataDTO(taxRate, calculations, parsedTaxpayerDetailsJson, atsAudit, "", taxYear)

      returnValue.taxYear mustEqual 2014

      val parsedPayload = returnValue.income_tax.get.payload.get

      parsedPayload(OtherAdjustmentsReducing) must equal(new Amount(200.0, "GBP"))
    }

    "have a correct 'other_adjustments_reducing' roundup data when alimony is given" in {

      val originalJson = getClass.getResource("/utr_2014.json")

      val update = Json.obj("alimony" -> Amount(9.01, "GBP"))

      val transformedJson = transformation(sourceJson = originalJson, tliSlpAtsUpdate = update)

      val taxYear2020: Int = 2020
      val taxRate = new TaxRateService(taxYear2020, applicationConfig.ratePercentages)

      val calculations = ATSCalculations.make(transformedJson.as[TaxSummaryLiability], taxRate)

      val returnValue: AtsMiddleTierData =
        SUT.atsDataDTO(taxRate, calculations, parsedTaxpayerDetailsJson, atsAudit, "", taxYear2020)

      returnValue.taxYear mustEqual 2020

      val parsedPayload = returnValue.income_tax.get.payload.get

      parsedPayload(OtherAdjustmentsReducing) must equal(new Amount(210.0, "GBP"))
    }
  }
}
