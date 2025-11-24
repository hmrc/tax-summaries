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

package sa.models

import common.utils.{BaseSpec, JsonUtil}
import play.api.libs.json.*

class SelfAssessmentAPIResponseSpec extends BaseSpec {
  private val taxYear = 2024

  private val convertFieldNamesToLower: Reads[JsObject] = { jsValue =>
    val caseSensitiveFields = Seq("incomeTaxStatus", "ctnPensionLumpSumTaxRate")
    val convertedFields     = jsValue
      .as[JsObject]
      .fields
      .map { fieldTuple =>
        if (caseSensitiveFields.contains(fieldTuple._1)) { fieldTuple }
        else {
          fieldTuple._1.toLowerCase -> fieldTuple._2
        }
      }
      .toSeq
    JsSuccess(JsObject(convertedFields))
  }

  private def parseJsonAndConvertFieldNamesToLowerCase(jsonAsString: String): JsObject = {
    val fields = Json.parse(jsonAsString).as[JsObject].fields.map { fieldTuple =>
      val fieldValue = if (fieldTuple._1 == "tliSlpAtsData") {
        fieldTuple._2.as[JsObject](convertFieldNamesToLower)
      } else {
        fieldTuple._2
      }
      fieldTuple._1 -> fieldValue
    }
    JsObject(fields)
  }

  "TaxSummaryLiability Reads" must {
    "correctly parse the data, regardless of case of API field names" in {
      val jsonAsString = JsonUtil.load("/sa/sa_ats_valid.json", Map("<taxYear>" -> taxYear.toString))
      val parsedJson   = parseJsonAndConvertFieldNamesToLowerCase(jsonAsString)

      val result = parsedJson.as[SelfAssessmentAPIResponse]
      result.taxYear mustBe taxYear
      result.pensionLumpSumTaxRate mustBe PensionTaxRate(0.0)
      result.incomeTaxStatus mustBe Some(UK())
      result.nationalInsuranceData.size mustBe 3
      result.atsData.size mustBe 127
    }

    "correctly parse the data where incomeTaxStatus is Null" in {
      val json   = JsonUtil.load("/sa/sa_ats_invalid_null.json", Map("<taxYear>" -> taxYear.toString))
      val result = Json.parse(json).as[SelfAssessmentAPIResponse]
      result.taxYear mustBe taxYear
      result.pensionLumpSumTaxRate mustBe PensionTaxRate(0.0)
      result.incomeTaxStatus mustBe Some(UK())
      result.nationalInsuranceData.size mustBe 3
      result.atsData.size mustBe 95
    }

    "correctly parse the data where fields are missing" in {
      val json   = JsonUtil.load(
        "/sa/sa_ats_income_status_and_fields_missing.json",
        Map("<taxYear>" -> taxYear.toString)
      )
      val result = Json.parse(json).as[SelfAssessmentAPIResponse]
      result.taxYear mustBe taxYear
      result.pensionLumpSumTaxRate mustBe PensionTaxRate(0.0)
      result.incomeTaxStatus mustBe Some(UK())
      result.nationalInsuranceData.size mustBe 3
      result.atsData.size mustBe 90
    }
  }
}
