/*
 * Copyright 2019 HM Revenue & Customs
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

import models.LiabilityTransformer._
import models.{Amount, AtsMiddleTierData, TaxSummaryLiability}
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import uk.gov.hmrc.play.test.UnitSpec
import utils._

import scala.io.Source

class OtherAllowancesTransformerTest extends UnitSpec with AtsJsonDataUpdate with GuiceOneAppPerTest {

  val taxpayerDetailsJson = Source.fromURL(getClass.getResource("/taxpayerData/test_individual_utr.json")).mkString
  val parsedTaxpayerDetailsJson = Json.parse(taxpayerDetailsJson)
  val taxYear: Int = 2014

  "The tax free amount" should {
    "parse the allowance data" in {
      val sampleJson = Source.fromURL(getClass.getResource("/test_case_3.json")).mkString

      val parsedJson = Json.parse(sampleJson)
      val returnValue: AtsMiddleTierData =
        ATSRawDataTransformer(parsedJson.as[TaxSummaryLiability], parsedTaxpayerDetailsJson, "", taxYear).atsDataDTO

      val parsedYear = returnValue.taxYear
      val testYear: Int = 2014

      testYear shouldEqual parsedYear

      val parsedPayload = returnValue.allowance_data.get.payload.get
      val testPayload =
        Map(
          PersonalTaxFreeAmount              -> Amount(9440.0, "GBP"),
          MarriageAllowanceTransferredAmount -> Amount(0.00, "GBP"),
          OtherAllowancesAmount              -> Amount(300.0, "GBP"),
          TotalTaxFreeAmount                 -> Amount(9740.0, "GBP")
        )
      testPayload shouldEqual parsedPayload
    }
  }

  "With base data for utr" should {

    "have the correct other allowances data" in {

      val sampleJson = Source.fromURL(getClass.getResource("/utr_2014.json")).mkString

      val parsedJson = Json.parse(sampleJson)
      val returnValue =
        ATSRawDataTransformer(parsedJson.as[TaxSummaryLiability], parsedTaxpayerDetailsJson, "", taxYear).atsDataDTO

      val parsedYear = returnValue.taxYear
      val testYear: Int = 2014
      testYear shouldEqual parsedYear

      val parsedPayload = returnValue.allowance_data.get.payload.get

      parsedPayload(OtherAllowancesAmount) should equal(new Amount(300.0, "GBP"))
    }

    "have the correct summed other allowances data" in {

      val originalJson = getClass.getResource("/utr_2014.json")

      val update = Json.obj(
        "ctnEmploymentExpensesAmt"   -> Amount(10.0, "GBP"),
        "ctnSummaryTotalDedPpr"      -> Amount(20.0, "GBP"),
        "ctnSumTotForeignTaxRelief"  -> Amount(30.0, "GBP"),
        "ctnSumTotLoanRestricted"    -> Amount(40.0, "GBP"),
        "ctnSumTotLossRestricted"    -> Amount(50.0, "GBP"),
        "grossAnnuityPayts"          -> Amount(60.0, "GBP"),
        "itf4GiftsInvCharitiesAmo"   -> Amount(70.0, "GBP"),
        "itfTradeUnionDeathBenefits" -> Amount(80.0, "GBP"),
        "ctnBpaAllowanceAmt"         -> Amount(90.0, "GBP"),
        "itfBpaAmount"               -> Amount(100.0, "GBP"),
        "grossExcludedIncome"        -> Amount(110.0, "GBP")
      )

      val transformedJson = transformation(sourceJson = originalJson, tliSlpAtsUpdate = update)

      val returnValue =
        ATSRawDataTransformer(transformedJson.as[TaxSummaryLiability], parsedTaxpayerDetailsJson, "", taxYear).atsDataDTO
      val parsedPayload = returnValue.allowance_data.get.payload.get

      parsedPayload(OtherAllowancesAmount) should equal(new Amount(660.0, "GBP"))
    }

    "have the correct summed other allowances data (with 'other_allowances_amount' roundup)" in {

      val originalJson = getClass.getResource("/utr_2014.json")

      val update = Json.obj(
        "ctnEmploymentExpensesAmt"   -> Amount(10.0, "GBP"),
        "ctnSummaryTotalDedPpr"      -> Amount(20.0, "GBP"),
        "ctnSumTotForeignTaxRelief"  -> Amount(30.0, "GBP"),
        "ctnSumTotLoanRestricted"    -> Amount(40.0, "GBP"),
        "ctnSumTotLossRestricted"    -> Amount(50.0, "GBP"),
        "grossAnnuityPayts"          -> Amount(59.01, "GBP"),
        "itf4GiftsInvCharitiesAmo"   -> Amount(70.0, "GBP"),
        "itfTradeUnionDeathBenefits" -> Amount(80.0, "GBP"),
        "ctnBpaAllowanceAmt"         -> Amount(90.0, "GBP"),
        "itfBpaAmount"               -> Amount(100.0, "GBP"),
        "grossExcludedIncome"        -> Amount(110.0, "GBP")
      )

      val transformedJson = transformation(sourceJson = originalJson, tliSlpAtsUpdate = update)

      val returnValue =
        ATSRawDataTransformer(transformedJson.as[TaxSummaryLiability], parsedTaxpayerDetailsJson, "", taxYear).atsDataDTO
      val parsedPayload = returnValue.allowance_data.get.payload.get

      parsedPayload(OtherAllowancesAmount) should equal(new Amount(660.0, "GBP"))
    }
  }
}
