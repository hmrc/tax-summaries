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

package sa.utils

import common.models.{Amount, LiabilityKey}
import common.utils.{BaseSpec, JsonUtil}
import org.scalatest.Assertions
import play.api.libs.json.{JsObject, JsValue, Json}
import sa.models.AtsMiddleTierData
import sa.transformers.ATSRawDataTransformer

trait AtsRawDataTransformerTestFixture extends BaseSpec with Assertions {
  protected val taxYear: Int
  protected val incomeTaxStatus: String

  protected def tliSlpAtsData: Map[String, BigDecimal]
  protected def saPayeNicDetails: Map[String, BigDecimal]

  def expectedResultIncomeTax: Map[LiabilityKey, Amount]
  def expectedResultIncomeData: Map[LiabilityKey, Amount]
  def expectedResultCapitalGainsData: Map[LiabilityKey, Amount]
  def expectedResultAllowanceData: Map[LiabilityKey, Amount]
  def expectedResultSummaryData: Map[LiabilityKey, Amount]
  def transformedData: AtsMiddleTierData = doTest(buildJsonPayload())

  protected def parsedTaxpayerDetailsJson: JsValue =
    Json.parse(JsonUtil.load("/common/taxpayer/sa_taxpayer-valid.json"))

  def doTest(jsonPayload: JsObject): AtsMiddleTierData = {
    val atsRawDataTransformer: ATSRawDataTransformer = inject[ATSRawDataTransformer]
    atsRawDataTransformer.atsDataDTO(
      rawPayloadJson = jsonPayload,
      rawTaxPayerJson = parsedTaxpayerDetailsJson,
      UTR = "",
      taxYear = taxYear
    )
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

  def buildJsonPayload(
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
