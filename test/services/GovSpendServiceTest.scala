/*
 * Copyright 2020 HM Revenue & Customs
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
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import services.GoodsAndServices._
import uk.gov.hmrc.play.test.UnitSpec

class GovSpendServiceTest extends UnitSpec with MockitoSugar with ScalaFutures with GuiceOneAppPerTest {

  "govSpending" should {
    "return correct amounts for 2016" in {
      val result: Map[GoodsAndServices, Double] = GovSpendService.govSpending(2016)

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
  }
}
