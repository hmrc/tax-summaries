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
import services.GoodsAndServices._
import uk.gov.hmrc.play.test.UnitSpec

class GovSpendServiceTest extends UnitSpec with MockitoSugar with ScalaFutures {

  "govSpending" should {

    "return correct amounts for 2014" in {
      val result: Map[GoodsAndServices, BigDecimal] = GovSpendService.govSpending(2014)

      result.get(Welfare) shouldBe Some(24.52)
      result.get(Health) shouldBe Some(18.87)
      result.get(Education) shouldBe Some(13.15)
      result.get(StatePensions) shouldBe Some(12.12)
      result.get(NationalDebtInterest) shouldBe Some(7.0)
      result.get(Defence) shouldBe Some(5.31)
      result.get(CriminalJustice) shouldBe Some(4.4)
      result.get(Transport) shouldBe Some(2.95)
      result.get(BusinessAndIndustry) shouldBe Some(2.74)
      result.get(GovernmentAdministration) shouldBe Some(2.05)
      result.get(Culture) shouldBe Some(1.69)
      result.get(Environment) shouldBe Some(1.66)
      result.get(HousingAndUtilities) shouldBe Some(1.64)
      result.get(OverseasAid) shouldBe Some(1.15)
      result.get(UkContributionToEuBudget) shouldBe Some(0.75)
    }

    "return correct amounts for 2015" in {
      val result: Map[GoodsAndServices, BigDecimal] = GovSpendService.govSpending(2015)

      result.get(Welfare) shouldBe Some(25.3)
      result.get(Health) shouldBe Some(19.9)
      result.get(StatePensions) shouldBe Some(12.8)
      result.get(Education) shouldBe Some(12.5)
      result.get(Defence) shouldBe Some(5.4)
      result.get(NationalDebtInterest) shouldBe Some(5.0)
      result.get(PublicOrderAndSafety) shouldBe Some(4.4)
      result.get(Transport) shouldBe Some(3.0)
      result.get(BusinessAndIndustry) shouldBe Some(2.7)
      result.get(GovernmentAdministration) shouldBe Some(2.0)
      result.get(Culture) shouldBe Some(1.8)
      result.get(Environment) shouldBe Some(1.7)
      result.get(HousingAndUtilities) shouldBe Some(1.6)
      result.get(OverseasAid) shouldBe Some(1.3)
      result.get(UkContributionToEuBudget) shouldBe Some(0.6)
    }

    "return correct amounts for 2016" in {
      val result: Map[GoodsAndServices, BigDecimal] = GovSpendService.govSpending(2016)

      result.get(Welfare) shouldBe Some(25.00)
      result.get(Health) shouldBe Some(19.90)
      result.get(StatePensions) shouldBe Some(12.80)
      result.get(Education) shouldBe Some(12.00)
      result.get(Defence) shouldBe Some(5.20)
      result.get(NationalDebtInterest) shouldBe Some(5.30)
      result.get(PublicOrderAndSafety) shouldBe Some(4.30)
      result.get(Transport) shouldBe Some(4.00)
      result.get(BusinessAndIndustry) shouldBe Some(2.40)
      result.get(GovernmentAdministration) shouldBe Some(2.00)
      result.get(Culture) shouldBe Some(1.60)
      result.get(Environment) shouldBe Some(1.70)
      result.get(HousingAndUtilities) shouldBe Some(1.40)
      result.get(OverseasAid) shouldBe Some(1.20)
      result.get(UkContributionToEuBudget) shouldBe Some(1.10)
    }

    "return correct amounts for 2017" in {
      val result: Map[GoodsAndServices, BigDecimal] = GovSpendService.govSpending(2017)

      result.get(Welfare) shouldBe Some(24.30)
      result.get(Health) shouldBe Some(20.30)
      result.get(StatePensions) shouldBe Some(12.90)
      result.get(Education) shouldBe Some(12.30)
      result.get(Defence) shouldBe Some(5.20)
      result.get(NationalDebtInterest) shouldBe Some(5.50)
      result.get(PublicOrderAndSafety) shouldBe Some(4.20)
      result.get(Transport) shouldBe Some(4.20)
      result.get(BusinessAndIndustry) shouldBe Some(2.50)
      result.get(GovernmentAdministration) shouldBe Some(2.10)
      result.get(Culture) shouldBe Some(1.60)
      result.get(Environment) shouldBe Some(1.60)
      result.get(HousingAndUtilities) shouldBe Some(1.50)
      result.get(OverseasAid) shouldBe Some(1.10)
      result.get(UkContributionToEuBudget) shouldBe Some(0.70)
    }

    "return correct amounts for 2018" in {
      val result: Map[GoodsAndServices, BigDecimal] = GovSpendService.govSpending(2018)

      result.get(Welfare) shouldBe Some(23.80)
      result.get(Health) shouldBe Some(19.90)
      result.get(StatePensions) shouldBe Some(12.80)
      result.get(Education) shouldBe Some(12.00)
      result.get(Defence) shouldBe Some(5.30)
      result.get(NationalDebtInterest) shouldBe Some(6.10)
      result.get(PublicOrderAndSafety) shouldBe Some(4.30)
      result.get(Transport) shouldBe Some(4.30)
      result.get(BusinessAndIndustry) shouldBe Some(2.90)
      result.get(GovernmentAdministration) shouldBe Some(2.10)
      result.get(Culture) shouldBe Some(1.60)
      result.get(Environment) shouldBe Some(1.60)
      result.get(HousingAndUtilities) shouldBe Some(1.60)
      result.get(OverseasAid) shouldBe Some(1.20)
      result.get(UkContributionToEuBudget) shouldBe Some(0.70)
    }

    "return correct amounts for 2019" in {
      val result: Map[GoodsAndServices, BigDecimal] = GovSpendService.govSpending(2019)

      result.get(Welfare) shouldBe Some(23.50)
      result.get(Health) shouldBe Some(20.20)
      result.get(StatePensions) shouldBe Some(12.80)
      result.get(Education) shouldBe Some(11.80)
      result.get(Defence) shouldBe Some(5.30)
      result.get(NationalDebtInterest) shouldBe Some(5.10)
      result.get(Transport) shouldBe Some(4.30)
      result.get(PublicOrderAndSafety) shouldBe Some(4.30)
      result.get(BusinessAndIndustry) shouldBe Some(3.6)
      result.get(GovernmentAdministration) shouldBe Some(2.10)
      result.get(HousingAndUtilities) shouldBe Some(1.60)
      result.get(Environment) shouldBe Some(1.50)
      result.get(Culture) shouldBe Some(1.50)
      result.get(OverseasAid) shouldBe Some(1.20)
      result.get(UkContributionToEuBudget) shouldBe Some(1.00)
    }
  }

}
