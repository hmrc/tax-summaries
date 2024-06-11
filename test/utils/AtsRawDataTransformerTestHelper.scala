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

//      val parsedPayload: Option[Set[(RateKey, ApiRate)]] =
//        doTest.income_tax.flatMap(_.rates).map(_.toSet)
//      parsedPayload.map { x =>
//        x.map { y =>
//          println(s"""\n${y._1} -> ApiRate("${y._2.percent}"),""")
//        }
//      }

trait AtsRawDataTransformerTestHelper extends BaseSpec {
  protected val taxYear: Int
  protected val incomeTaxStatus: String
  protected val tliSlpAtsData: Map[String, BigDecimal]

  protected def parsedTaxpayerDetailsJson: JsValue = Json.parse(JsonUtil.load("/taxpayer/sa_taxpayer-valid.json"))

  protected def doTest(jsonPayload: JsObject): AtsMiddleTierData = {
    val atsRawDataTransformer: ATSRawDataTransformer = inject[ATSRawDataTransformer]
    atsRawDataTransformer.atsDataDTO(jsonPayload, parsedTaxpayerDetailsJson, "", taxYear)
  }

  protected def transformedData: AtsMiddleTierData = doTest(buildJsonPayload)

  protected def atsRawDataTransformer(
    transformedData: AtsMiddleTierData,
    expResultIncomeTax: Map[LiabilityKey, Amount],
    expResultIncomeData: Map[LiabilityKey, Amount],
    expResultCapitalGainsData: Map[LiabilityKey, Amount],
    expResultAllowanceData: Map[LiabilityKey, Amount],
    expResultSummaryData: Map[LiabilityKey, Amount]
  ): Unit =
    Set(
      ("income tax", transformedData.income_tax, expResultIncomeTax),
      ("income data", transformedData.income_data, expResultIncomeData),
      ("cap gains data", transformedData.capital_gains_data, expResultCapitalGainsData),
      ("allowance data", transformedData.allowance_data, expResultAllowanceData)
      //     ("summary data", doTest.summary_data, expResultSummaryData)
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

  protected def amt(value: BigDecimal, calculus: String): Amount = Amount(value, "GBP", Some(calculus))

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
        val bdValue = tliSlpAtsData(name)
        Amount(bdValue, "GBP", Some(s"$bdValue($name)"))
      }
    }

    val initialValue = retrieveAmount(fieldNames.head)
    fieldNames.tail.foldLeft[Amount](initialValue) { (c, i) =>
      c + retrieveAmount(i)
    }
  }

  protected def buildJsonPayload: JsObject = {
    val tliSlpAtsDataAsJsObject = tliSlpAtsData.foldLeft[JsObject](
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
