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

package common.services

import common.services.GoodsAndServices._
import common.utils.BaseSpec

class GovSpendServiceTest extends BaseSpec {

  "govSpending" must {
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

    "return correct amounts for 2023" in {
      val result: Map[GoodsAndServices, Double] =
        new GovSpendService(applicationConfig).govSpending(2023)

      result.get(Health) mustBe Some(19.80)
      result.get(Welfare) mustBe Some(19.60)
      result.get(NationalDebtInterest) mustBe Some(12.00)
      result.get(StatePensions) mustBe Some(10.30)
      result.get(Education) mustBe Some(9.90)
      result.get(BusinessAndIndustry) mustBe Some(7.60)
      result.get(Defence) mustBe Some(5.20)
      result.get(PublicOrderAndSafety) mustBe Some(4.10)
      result.get(Transport) mustBe Some(4.10)
      result.get(GovernmentAdministration) mustBe Some(2.00)
      result.get(HousingAndUtilities) mustBe Some(1.70)
      result.get(Culture) mustBe Some(1.30)
      result.get(Environment) mustBe Some(1.30)
      result.get(OutstandingPaymentsToTheEU) mustBe Some(0.60)
      result.get(OverseasAid) mustBe Some(0.50)
    }

    "return correct amounts for 2024" in {
      val result: Map[GoodsAndServices, Double] =
        new GovSpendService(applicationConfig).govSpending(2024)

      result.get(Health) mustBe Some(20.20)
      result.get(Welfare) mustBe Some(21.60)
      result.get(NationalDebtInterest) mustBe Some(11.10)
      result.get(StatePensions) mustBe Some(11.40)
      result.get(Education) mustBe Some(10.20)
      result.get(BusinessAndIndustry) mustBe Some(4.20)
      result.get(Defence) mustBe Some(5.20)
      result.get(PublicOrderAndSafety) mustBe Some(4.40)
      result.get(Transport) mustBe Some(4.20)
      result.get(GovernmentAdministration) mustBe Some(2.10)
      result.get(HousingAndUtilities) mustBe Some(1.80)
      result.get(Culture) mustBe Some(1.20)
      result.get(Environment) mustBe Some(1.40)
      result.get(OutstandingPaymentsToTheEU) mustBe Some(0.60)
      result.get(OverseasAid) mustBe Some(0.70)
    }
  }
}
