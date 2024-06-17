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
import org.scalatest.Assertions
import play.api.libs.json.{JsObject, JsValue, Json}
import transformers.ATSRawDataTransformer

trait AtsRawDataTransformerTestFixture extends BaseSpec with Assertions {
  protected val taxYear: Int
  protected val incomeTaxStatus: String

  // scalastyle:off method.length
  protected def tliSlpAtsData: Map[String, BigDecimal]

  protected def saPayeNicDetails: Map[String, BigDecimal]

  def expectedResultIncomeTax: Map[LiabilityKey, Amount]
  def expectedResultIncomeData: Map[LiabilityKey, Amount]
  def expectedResultCapitalGainsData: Map[LiabilityKey, Amount]
  def expectedResultAllowanceData: Map[LiabilityKey, Amount]
  def expectedResultSummaryData: Map[LiabilityKey, Amount]
  def transformedData: AtsMiddleTierData = doTest(buildJsonPayload())

  protected def parsedTaxpayerDetailsJson: JsValue = Json.parse(JsonUtil.load("/taxpayer/sa_taxpayer-valid.json"))

  protected def doTest(jsonPayload: JsObject): AtsMiddleTierData = {
    val atsRawDataTransformer: ATSRawDataTransformer = inject[ATSRawDataTransformer]
    atsRawDataTransformer.atsDataDTO(jsonPayload, parsedTaxpayerDetailsJson, "", taxYear)
  }
//  protected def atsRawDataTransformerWithCalculationsTwo(): Unit =
//    Seq(
//      ("income tax", transformedData.income_tax, expectedResultIncomeTax),
//      ("income data", transformedData.income_data, expectedResultIncomeData),
//      ("cap gains data", transformedData.capital_gains_data, expectedResultCapitalGainsData),
//      ("allowance data", transformedData.allowance_data, expectedResultAllowanceData),
//      ("summary data", transformedData.summary_data, expectedResultSummaryData)
//    ).foreach { case (section, actualOptDataHolder, exp) =>
//      val act = actualOptDataHolder.flatMap(_.payload).getOrElse(Map.empty)
//      if (act.exists(a => exp.exists(_._1 == a._1))) {
//        act.foreach { item =>
//          exp.find(_._1 == item._1).map { actItem =>
//            assert(
//              item._2 == actItem._2,
//              s"(for $section section) field ${item._1} calculated (act ${actItem._2.amount}, exp ${item._2.amount})"
//            )
//          }
//        }
//
////          "check for missing keys made" in {
////            exp.keys.toSeq.diff(act.keys.toSeq) mustBe Nil
////          }
//
//      }
//    }

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
