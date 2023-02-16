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

package transformers

import models.{Amount, GovernmentSpendingOutputWrapper}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Json
import services.GoodsAndServices._
import utils._

class GovernmentSpendingOutputWrapperSpec extends BaseSpec with AtsJsonDataUpdate with ScalaCheckPropertyChecks {

  "GovernmentSpendingOutputWrapper must round trip through Json " in {
    forAll(Generators.genGovernmentSpending) { data =>
      val json = Json.toJson(data)
      val obj  = json.as[GovernmentSpendingOutputWrapper]

      obj mustBe data
    }
  }

  "The Gov spending data" must {
    "display correct amounts for a given user in 2014" in {

      val testYear: Int                                = 2014
      val testAmount: Int                              = 1400
      val returnValue: GovernmentSpendingOutputWrapper =
        GovernmentSpendingOutputWrapper(applicationConfig, new Amount(testAmount, "GBP"), testYear)

      val parsedYear = returnValue.taxYear

      testYear mustEqual parsedYear

      val govSpendData = returnValue.govSpendAmountData

      govSpendData(Welfare).amount.amount                  must equal(
        BigDecimal(testAmount * 0.2452).setScale(2, BigDecimal.RoundingMode.HALF_UP)
      )
      govSpendData(Health).amount.amount                   must equal(
        BigDecimal(testAmount * 0.1887).setScale(2, BigDecimal.RoundingMode.HALF_UP)
      )
      govSpendData(Education).amount.amount                must equal(
        BigDecimal(testAmount * 0.1315).setScale(2, BigDecimal.RoundingMode.HALF_UP)
      )
      govSpendData(StatePensions).amount.amount            must equal(
        BigDecimal(testAmount * 0.1212).setScale(2, BigDecimal.RoundingMode.HALF_UP)
      )
      govSpendData(NationalDebtInterest).amount.amount     must equal(
        BigDecimal(testAmount * 0.07).setScale(2, BigDecimal.RoundingMode.HALF_UP)
      )
      govSpendData(Defence).amount.amount                  must equal(
        BigDecimal(testAmount * 0.0531).setScale(2, BigDecimal.RoundingMode.HALF_UP)
      )
      govSpendData(CriminalJustice).amount.amount          must equal(
        BigDecimal(testAmount * 0.044).setScale(2, BigDecimal.RoundingMode.HALF_UP)
      )
      govSpendData(Transport).amount.amount                must equal(
        BigDecimal(testAmount * 0.0295).setScale(2, BigDecimal.RoundingMode.HALF_UP)
      )
      govSpendData(BusinessAndIndustry).amount.amount      must equal(
        BigDecimal(testAmount * 0.0274).setScale(2, BigDecimal.RoundingMode.HALF_UP)
      )
      govSpendData(GovernmentAdministration).amount.amount must equal(
        BigDecimal(testAmount * 0.0205).setScale(2, BigDecimal.RoundingMode.HALF_UP)
      )
      govSpendData(Culture).amount.amount                  must equal(
        BigDecimal(testAmount * 0.0169).setScale(2, BigDecimal.RoundingMode.HALF_UP)
      )
      govSpendData(Environment).amount.amount              must equal(
        BigDecimal(testAmount * 0.0166).setScale(2, BigDecimal.RoundingMode.HALF_UP)
      )
      govSpendData(HousingAndUtilities).amount.amount      must equal(
        BigDecimal(testAmount * 0.0164).setScale(2, BigDecimal.RoundingMode.HALF_UP)
      )
      govSpendData(OverseasAid).amount.amount              must equal(
        BigDecimal(testAmount * 0.0115).setScale(2, BigDecimal.RoundingMode.HALF_UP)
      )
      govSpendData(UkContributionToEuBudget).amount.amount must equal(
        BigDecimal(testAmount * 0.0075).setScale(2, BigDecimal.RoundingMode.HALF_UP)
      )

      govSpendData.values.foldLeft(BigDecimal(0.0)) {
        _ + _.amount.amount
      } must equal(BigDecimal("1400.00"))
      govSpendData.values.foldLeft(BigDecimal(0.0)) {
        _ + _.percentage
      } must equal(BigDecimal("100.00"))
    }

    "display correct amounts for a different user with total tax of £22,000 in 2014" in {

      val testYear: Int                                = 2014
      val testAmount: Int                              = 22000
      val returnValue: GovernmentSpendingOutputWrapper =
        GovernmentSpendingOutputWrapper(applicationConfig, new Amount(testAmount, "GBP"), testYear)

      val parsedYear = returnValue.taxYear

      testYear mustEqual parsedYear

      val govSpendData = returnValue.govSpendAmountData

      govSpendData(Welfare).amount.amount                  must equal(
        BigDecimal(testAmount * 0.2452).setScale(2, BigDecimal.RoundingMode.HALF_UP)
      )
      govSpendData(Health).amount.amount                   must equal(
        BigDecimal(testAmount * 0.1887).setScale(2, BigDecimal.RoundingMode.HALF_UP)
      )
      govSpendData(Education).amount.amount                must equal(
        BigDecimal(testAmount * 0.1315).setScale(2, BigDecimal.RoundingMode.HALF_UP)
      )
      govSpendData(StatePensions).amount.amount            must equal(
        BigDecimal(testAmount * 0.1212).setScale(2, BigDecimal.RoundingMode.HALF_UP)
      )
      govSpendData(NationalDebtInterest).amount.amount     must equal(
        BigDecimal(testAmount * 0.07).setScale(2, BigDecimal.RoundingMode.HALF_UP)
      )
      govSpendData(Defence).amount.amount                  must equal(
        BigDecimal(testAmount * 0.0531).setScale(2, BigDecimal.RoundingMode.HALF_UP)
      )
      govSpendData(CriminalJustice).amount.amount          must equal(
        BigDecimal(testAmount * 0.044).setScale(2, BigDecimal.RoundingMode.HALF_UP)
      )
      govSpendData(Transport).amount.amount                must equal(
        BigDecimal(testAmount * 0.0295).setScale(2, BigDecimal.RoundingMode.HALF_UP)
      )
      govSpendData(BusinessAndIndustry).amount.amount      must equal(
        BigDecimal(testAmount * 0.0274).setScale(2, BigDecimal.RoundingMode.HALF_UP)
      )
      govSpendData(GovernmentAdministration).amount.amount must equal(
        BigDecimal(testAmount * 0.0205).setScale(2, BigDecimal.RoundingMode.HALF_UP)
      )
      govSpendData(Culture).amount.amount                  must equal(
        BigDecimal(testAmount * 0.0169).setScale(2, BigDecimal.RoundingMode.HALF_UP)
      )
      govSpendData(Environment).amount.amount              must equal(
        BigDecimal(testAmount * 0.0166).setScale(2, BigDecimal.RoundingMode.HALF_UP)
      )
      govSpendData(HousingAndUtilities).amount.amount      must equal(
        BigDecimal(testAmount * 0.0164).setScale(2, BigDecimal.RoundingMode.HALF_UP)
      )
      govSpendData(OverseasAid).amount.amount              must equal(
        BigDecimal(testAmount * 0.0115).setScale(2, BigDecimal.RoundingMode.HALF_UP)
      )
      govSpendData(UkContributionToEuBudget).amount.amount must equal(
        BigDecimal(testAmount * 0.0075).setScale(2, BigDecimal.RoundingMode.HALF_UP)
      )

      govSpendData.values.foldLeft(BigDecimal(0.0)) {
        _ + _.amount.amount
      } must equal(BigDecimal("22000.00"))
      govSpendData.values.foldLeft(BigDecimal(0.0)) {
        _ + _.percentage
      } must equal(BigDecimal("100.00"))
    }

    "display correct amounts for a given user in 2015" in {

      val testYear: Int                                = 2015
      val testAmount: Int                              = 1400
      val returnValue: GovernmentSpendingOutputWrapper =
        GovernmentSpendingOutputWrapper(applicationConfig, new Amount(testAmount, "GBP"), testYear)

      val parsedYear = returnValue.taxYear

      testYear mustEqual parsedYear

      val govSpendData = returnValue.govSpendAmountData

      govSpendData(Welfare).amount.amount                  must equal(
        BigDecimal(testAmount * 0.253).setScale(2, BigDecimal.RoundingMode.HALF_UP)
      )
      govSpendData(Health).amount.amount                   must equal(
        BigDecimal(testAmount * 0.199).setScale(2, BigDecimal.RoundingMode.HALF_UP)
      )
      govSpendData(StatePensions).amount.amount            must equal(
        BigDecimal(testAmount * 0.128).setScale(2, BigDecimal.RoundingMode.HALF_UP)
      )
      govSpendData(Education).amount.amount                must equal(
        BigDecimal(testAmount * 0.125).setScale(2, BigDecimal.RoundingMode.HALF_UP)
      )
      govSpendData(Defence).amount.amount                  must equal(
        BigDecimal(testAmount * 0.054).setScale(2, BigDecimal.RoundingMode.HALF_UP)
      )
      govSpendData(NationalDebtInterest).amount.amount     must equal(
        BigDecimal(testAmount * 0.05).setScale(2, BigDecimal.RoundingMode.HALF_UP)
      )
      govSpendData(PublicOrderAndSafety).amount.amount     must equal(
        BigDecimal(testAmount * 0.044).setScale(2, BigDecimal.RoundingMode.HALF_UP)
      )
      govSpendData(Transport).amount.amount                must equal(
        BigDecimal(testAmount * 0.03).setScale(2, BigDecimal.RoundingMode.HALF_UP)
      )
      govSpendData(BusinessAndIndustry).amount.amount      must equal(
        BigDecimal(testAmount * 0.027).setScale(2, BigDecimal.RoundingMode.HALF_UP)
      )
      govSpendData(GovernmentAdministration).amount.amount must equal(
        BigDecimal(testAmount * 0.02).setScale(2, BigDecimal.RoundingMode.HALF_UP)
      )
      govSpendData(Culture).amount.amount                  must equal(
        BigDecimal(testAmount * 0.018).setScale(2, BigDecimal.RoundingMode.HALF_UP)
      )
      govSpendData(Environment).amount.amount              must equal(
        BigDecimal(testAmount * 0.017).setScale(2, BigDecimal.RoundingMode.HALF_UP)
      )
      govSpendData(HousingAndUtilities).amount.amount      must equal(
        BigDecimal(testAmount * 0.016).setScale(2, BigDecimal.RoundingMode.HALF_UP)
      )
      govSpendData(OverseasAid).amount.amount              must equal(
        BigDecimal(testAmount * 0.013).setScale(2, BigDecimal.RoundingMode.HALF_UP)
      )
      govSpendData(UkContributionToEuBudget).amount.amount must equal(
        BigDecimal(testAmount * 0.006).setScale(2, BigDecimal.RoundingMode.HALF_UP)
      )

      govSpendData.values.foldLeft(BigDecimal(0.0)) {
        _ + _.amount.amount
      } must equal(BigDecimal("1400.00"))
      govSpendData.values.foldLeft(BigDecimal(0.0)) {
        _ + _.percentage
      } must equal(BigDecimal("100.00"))
    }

    "display correct amounts for a different user with total tax of £22,000 in 2015" in {

      val testYear: Int                                = 2015
      val testAmount: Int                              = 22000
      val returnValue: GovernmentSpendingOutputWrapper =
        GovernmentSpendingOutputWrapper(applicationConfig, new Amount(testAmount, "GBP"), testYear)

      val parsedYear = returnValue.taxYear

      testYear mustEqual parsedYear

      val govSpendData = returnValue.govSpendAmountData

      govSpendData(Welfare).amount.amount                  must equal(
        BigDecimal(testAmount * 0.253).setScale(2, BigDecimal.RoundingMode.HALF_UP)
      )
      govSpendData(Health).amount.amount                   must equal(
        BigDecimal(testAmount * 0.199).setScale(2, BigDecimal.RoundingMode.HALF_UP)
      )
      govSpendData(StatePensions).amount.amount            must equal(
        BigDecimal(testAmount * 0.128).setScale(2, BigDecimal.RoundingMode.HALF_UP)
      )
      govSpendData(Education).amount.amount                must equal(
        BigDecimal(testAmount * 0.125).setScale(2, BigDecimal.RoundingMode.HALF_UP)
      )
      govSpendData(Defence).amount.amount                  must equal(
        BigDecimal(testAmount * 0.054).setScale(2, BigDecimal.RoundingMode.HALF_UP)
      )
      govSpendData(NationalDebtInterest).amount.amount     must equal(
        BigDecimal(testAmount * 0.05).setScale(2, BigDecimal.RoundingMode.HALF_UP)
      )
      govSpendData(PublicOrderAndSafety).amount.amount     must equal(
        BigDecimal(testAmount * 0.044).setScale(2, BigDecimal.RoundingMode.HALF_UP)
      )
      govSpendData(Transport).amount.amount                must equal(
        BigDecimal(testAmount * 0.03).setScale(2, BigDecimal.RoundingMode.HALF_UP)
      )
      govSpendData(BusinessAndIndustry).amount.amount      must equal(
        BigDecimal(testAmount * 0.027).setScale(2, BigDecimal.RoundingMode.HALF_UP)
      )
      govSpendData(GovernmentAdministration).amount.amount must equal(
        BigDecimal(testAmount * 0.02).setScale(2, BigDecimal.RoundingMode.HALF_UP)
      )
      govSpendData(Culture).amount.amount                  must equal(
        BigDecimal(testAmount * 0.018).setScale(2, BigDecimal.RoundingMode.HALF_UP)
      )
      govSpendData(Environment).amount.amount              must equal(
        BigDecimal(testAmount * 0.017).setScale(2, BigDecimal.RoundingMode.HALF_UP)
      )
      govSpendData(HousingAndUtilities).amount.amount      must equal(
        BigDecimal(testAmount * 0.016).setScale(2, BigDecimal.RoundingMode.HALF_UP)
      )
      govSpendData(OverseasAid).amount.amount              must equal(
        BigDecimal(testAmount * 0.013).setScale(2, BigDecimal.RoundingMode.HALF_UP)
      )
      govSpendData(UkContributionToEuBudget).amount.amount must equal(
        BigDecimal(testAmount * 0.006).setScale(2, BigDecimal.RoundingMode.HALF_UP)
      )

      govSpendData.values.foldLeft(BigDecimal(0.0)) {
        _ + _.amount.amount
      } must equal(BigDecimal("22000.00"))
      govSpendData.values.foldLeft(BigDecimal(0.0)) {
        _ + _.percentage
      } must equal(BigDecimal("100.00"))
    }
    "has the correct spend percentages for 2014" in {

      val testYear: Int                                = 2014
      val returnValue: GovernmentSpendingOutputWrapper =
        GovernmentSpendingOutputWrapper(applicationConfig, new Amount(1400, "GBP"), testYear)

      val govSpendData = returnValue.govSpendAmountData
      govSpendData(Welfare).percentage                  must equal(24.52)
      govSpendData(Health).percentage                   must equal(18.87)
      govSpendData(Education).percentage                must equal(13.15)
      govSpendData(StatePensions).percentage            must equal(12.12)
      govSpendData(NationalDebtInterest).percentage     must equal(7.00)
      govSpendData(Defence).percentage                  must equal(5.31)
      govSpendData(CriminalJustice).percentage          must equal(4.40)
      govSpendData(Transport).percentage                must equal(2.95)
      govSpendData(BusinessAndIndustry).percentage      must equal(2.74)
      govSpendData(GovernmentAdministration).percentage must equal(2.05)
      govSpendData(Culture).percentage                  must equal(1.69)
      govSpendData(Environment).percentage              must equal(1.66)
      govSpendData(HousingAndUtilities).percentage      must equal(1.64)
      govSpendData(OverseasAid).percentage              must equal(1.15)
      govSpendData(UkContributionToEuBudget).percentage must equal(0.75)
    }

    "has the correct spend percentages for 2015" in {

      val testYear: Int                                = 2015
      val returnValue: GovernmentSpendingOutputWrapper =
        GovernmentSpendingOutputWrapper(applicationConfig, new Amount(1400, "GBP"), testYear)

      val govSpendData = returnValue.govSpendAmountData
      govSpendData(Welfare).percentage                  must equal(25.30)
      govSpendData(Health).percentage                   must equal(19.90)
      govSpendData(StatePensions).percentage            must equal(12.80)
      govSpendData(Education).percentage                must equal(12.50)
      govSpendData(Defence).percentage                  must equal(5.40)
      govSpendData(NationalDebtInterest).percentage     must equal(5.00)
      govSpendData(PublicOrderAndSafety).percentage     must equal(4.40)
      govSpendData(Transport).percentage                must equal(3.00)
      govSpendData(BusinessAndIndustry).percentage      must equal(2.70)
      govSpendData(GovernmentAdministration).percentage must equal(2.00)
      govSpendData(Culture).percentage                  must equal(1.80)
      govSpendData(Environment).percentage              must equal(1.70)
      govSpendData(HousingAndUtilities).percentage      must equal(1.60)
      govSpendData(OverseasAid).percentage              must equal(1.30)
      govSpendData(UkContributionToEuBudget).percentage must equal(0.60)
    }
  }
}
