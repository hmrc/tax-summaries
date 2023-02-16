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

package services

import services.GoodsAndServices._
import utils.BaseSpec

class GovSpendServiceTest extends BaseSpec {

  "govSpending" must {
    "return correct amounts for 2016" in {
      val result: Map[GoodsAndServices, Double] =
        new GovSpendService(applicationConfig).govSpending(2016)

      result.get(Welfare) mustBe Some(25.00)
      result.get(Health) mustBe Some(19.90)
      result.get(StatePensions) mustBe Some(12.80)
      result.get(Education) mustBe Some(12.00)
      result.get(Defence) mustBe Some(5.20)
      result.get(NationalDebtInterest) mustBe Some(5.30)
      result.get(PublicOrderAndSafety) mustBe Some(4.30)
      result.get(Transport) mustBe Some(4.00)
      result.get(BusinessAndIndustry) mustBe Some(2.40)
      result.get(GovernmentAdministration) mustBe Some(2.00)
      result.get(Culture) mustBe Some(1.60)
      result.get(Environment) mustBe Some(1.70)
      result.get(HousingAndUtilities) mustBe Some(1.40)
      result.get(OverseasAid) mustBe Some(1.20)
      result.get(UkContributionToEuBudget) mustBe Some(1.10)
    }

    "return correct amounts for 2022" in {
      val result: Map[GoodsAndServices, Double] =
        new GovSpendService(applicationConfig).govSpending(2022)

      result.get(Health) mustBe Some(22.80)
      result.get(Welfare) mustBe Some(20.40)
      result.get(StatePensions) mustBe Some(11.00)
      result.get(Education) mustBe Some(10.50)
      result.get(NationalDebtInterest) mustBe Some(7.60)
      result.get(BusinessAndIndustry) mustBe Some(5.40)
      result.get(Defence) mustBe Some(5.10)
      result.get(Transport) mustBe Some(4.70)
      result.get(PublicOrderAndSafety) mustBe Some(4.40)
      result.get(GovernmentAdministration) mustBe Some(2.30)
      result.get(HousingAndUtilities) mustBe Some(1.60)
      result.get(Environment) mustBe Some(1.50)
      result.get(Culture) mustBe Some(1.30)
      result.get(OutstandingPaymentsToTheEU) mustBe Some(0.70)
      result.get(OverseasAid) mustBe Some(0.60)
    }
  }
}
