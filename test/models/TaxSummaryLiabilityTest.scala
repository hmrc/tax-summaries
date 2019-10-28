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

package models

import play.api.libs.json.Json
import uk.gov.hmrc.play.test.UnitSpec
import utils.JsonUtil

class TaxSummaryLiabilityTest extends UnitSpec {

  "TaxSummaryLiability Reads" should {
    "correctly parse the data" in {
      val json = JsonUtil.load("/test_case_5.json")

      val result = Json.parse(json).as[TaxSummaryLiability]
   println(result)
      result.taxYear shouldBe 2014
      result.pensionLumpSumTaxRate shouldBe 0.0
      result.incomeTaxStatus shouldBe "0002"
      result.nationalInsuranceData.size shouldBe 3
      result.atsData.size shouldBe 94

    }
  }
}
