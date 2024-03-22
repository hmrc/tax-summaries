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

package models

import play.api.libs.json.Json
import utils.{BaseSpec, JsonUtil}

class TaxSummaryLiabilitySpec extends BaseSpec {

  private val taxYear = 2023

  "TaxSummaryLiability Reads" must {
    "correctly parse the data" in {
      val json = JsonUtil.load("/odsSaAtsPayloads/sa_ats_valid.json", Map("<taxYear>" -> taxYear.toString))

      val result = Json.parse(json).as[TaxSummaryLiability]
      result.taxYear mustBe taxYear
      result.pensionLumpSumTaxRate mustBe PensionTaxRate(0.0)
      result.incomeTaxStatus mustBe Some(UK())
      result.nationalInsuranceData.size mustBe 3
      result.atsData.size mustBe 126
    }

    "correctly parse the data where incomeTaxStatus is Null" in {
      val json   = JsonUtil.load("/odsSaAtsPayloads/sa_ats_invalid_null.json", Map("<taxYear>" -> taxYear.toString))
      val result = Json.parse(json).as[TaxSummaryLiability]
      result.taxYear mustBe taxYear
      result.pensionLumpSumTaxRate mustBe PensionTaxRate(0.0)
      result.incomeTaxStatus mustBe Some(UK())
      result.nationalInsuranceData.size mustBe 3
      result.atsData.size mustBe 95
    }

    "correctly parse the data where fields are missing" in {
      val json   = JsonUtil.load(
        "/odsSaAtsPayloads/sa_ats_income_status_and_fields_missing.json",
        Map("<taxYear>" -> taxYear.toString)
      )
      val result = Json.parse(json).as[TaxSummaryLiability]
      result.taxYear mustBe taxYear
      result.pensionLumpSumTaxRate mustBe PensionTaxRate(0.0)
      result.incomeTaxStatus mustBe Some(UK())
      result.nationalInsuranceData.size mustBe 3
      result.atsData.size mustBe 90
    }
  }
}
