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

package services

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import uk.gov.hmrc.play.test.UnitSpec

class GovSpendServiceTest extends UnitSpec with MockitoSugar with ScalaFutures {

  "govSpending" should {

    "return correct amounts for 2014" in {
      val result: Map[String, BigDecimal] = GovSpendService.govSpending(2014)

      result("Welfare") shouldBe 24.52
      result("Health") shouldBe 18.87
      result("Education") shouldBe 13.15
      result("StatePensions") shouldBe 12.12
      result("NationalDebtInterest") shouldBe 7.0
      result("Defence") shouldBe 5.31
      result("CriminalJustice") shouldBe 4.4
      result("Transport") shouldBe 2.95
      result("BusinessAndIndustry") shouldBe 2.74
      result("GovernmentAdministration") shouldBe 2.05
      result("Culture") shouldBe 1.69
      result("Environment") shouldBe 1.66
      result("HousingAndUtilities") shouldBe 1.64
      result("OverseasAid") shouldBe 1.15
      result("UkContributionToEuBudget") shouldBe 0.75
    }

    "return correct amounts for 2015" in {
      val result: Map[String, BigDecimal] = GovSpendService.govSpending(2015)

      result("Welfare") shouldBe 25.3
      result("Health") shouldBe 19.9
      result("StatePensions") shouldBe 12.8
      result("Education") shouldBe 12.5
      result("Defence") shouldBe 5.4
      result("NationalDebtInterest") shouldBe 5.0
      result("PublicOrderAndSafety") shouldBe 4.4
      result("Transport") shouldBe 3.0
      result("BusinessAndIndustry") shouldBe 2.7
      result("GovernmentAdministration") shouldBe 2.0
      result("Culture") shouldBe 1.8
      result("Environment") shouldBe 1.7
      result("HousingAndUtilities") shouldBe 1.6
      result("OverseasAid") shouldBe 1.3
      result("UkContributionToEuBudget") shouldBe 0.6
    }

    "return correct amounts for 2016" in {
      val result: Map[String, BigDecimal] = GovSpendService.govSpending(2016)

      result("Welfare") shouldBe 25.00
      result("Health") shouldBe 19.90
      result("StatePensions") shouldBe 12.80
      result("Education") shouldBe 12.00
      result("Defence") shouldBe 5.20
      result("NationalDebtInterest") shouldBe 5.30
      result("PublicOrderAndSafety") shouldBe 4.30
      result("Transport") shouldBe 4.00
      result("BusinessAndIndustry") shouldBe 2.40
      result("GovernmentAdministration") shouldBe 2.00
      result("Culture") shouldBe 1.60
      result("Environment") shouldBe 1.70
      result("HousingAndUtilities") shouldBe 1.40
      result("OverseasAid") shouldBe 1.20
      result("UkContributionToEuBudget") shouldBe 1.10
    }

    "return correct amounts for 2017" in {
      val result: Map[String, BigDecimal] = GovSpendService.govSpending(2017)

      result("Welfare") shouldBe 24.30
      result("Health") shouldBe 20.30
      result("StatePensions") shouldBe 12.90
      result("Education") shouldBe 12.30
      result("Defence") shouldBe 5.20
      result("NationalDebtInterest") shouldBe 5.50
      result("PublicOrderAndSafety") shouldBe 4.20
      result("Transport") shouldBe 4.20
      result("BusinessAndIndustry") shouldBe 2.50
      result("GovernmentAdministration") shouldBe 2.10
      result("Culture") shouldBe 1.60
      result("Environment") shouldBe 1.60
      result("HousingAndUtilities") shouldBe 1.50
      result("OverseasAid") shouldBe 1.10
      result("UkContributionToEuBudget") shouldBe 0.70
    }

    "return correct amounts for 2018" in {
      val result: Map[String, BigDecimal] = GovSpendService.govSpending(2018)

      result("Welfare") shouldBe 23.80
      result("Health") shouldBe 19.90
      result("StatePensions") shouldBe 12.80
      result("Education") shouldBe 12.00
      result("Defence") shouldBe 5.30
      result("NationalDebtInterest") shouldBe 6.10
      result("PublicOrderAndSafety") shouldBe 4.30
      result("Transport") shouldBe 4.30
      result("BusinessAndIndustry") shouldBe 2.90
      result("GovernmentAdministration") shouldBe 2.10
      result("Culture") shouldBe 1.60
      result("Environment") shouldBe 1.60
      result("HousingAndUtilities") shouldBe 1.60
      result("OverseasAid") shouldBe 1.20
      result("UkContributionToEuBudget") shouldBe 0.70
    }

  }

}
