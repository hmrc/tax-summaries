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

import config.ApplicationConfig
import models.{Amount, GovernmentSpendingOutputWrapper, Item}
import org.mockito.ArgumentMatchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.Json
import services.GoodsAndServices
import services.GoodsAndServices._
import utils._

class GovernmentSpendingOutputWrapperSpec extends BaseSpec with AtsJsonDataUpdate with ScalaCheckPropertyChecks {
  private val taxYear                                                              = 2023
  private val previousTaxYear                                                      = 2022
  private val mockAppConfig                                                        = mock[ApplicationConfig]
  private val randomPercentagesForTaxYear: Seq[(GoodsAndServices, Double)]         =
    Seq[(GoodsAndServices, Double)](
      Welfare                    -> 3.15,
      Health                     -> 5.12,
      Education                  -> 8.47,
      StatePensions              -> 3.54,
      NationalDebtInterest       -> 6.12,
      Defence                    -> 0.39,
      CriminalJustice            -> 8.22,
      Transport                  -> 10.04,
      BusinessAndIndustry        -> 2.3,
      GovernmentAdministration   -> 5.4,
      Culture                    -> 6.7,
      HousingAndUtilities        -> 0.45,
      OverseasAid                -> 4.55,
      UkContributionToEuBudget   -> 6.66,
      OutstandingPaymentsToTheEU -> 7.12,
      PublicOrderAndSafety       -> 10.33,
      Environment                -> 11.44
    )
  private val randomPercentagesForPreviousTaxYear: Seq[(GoodsAndServices, Double)] =
    Seq[(GoodsAndServices, Double)](
      Welfare                    -> 3.15,
      Health                     -> 5.12,
      Education                  -> 8.47,
      StatePensions              -> 0.05,
      NationalDebtInterest       -> 3.37,
      Defence                    -> 0.39,
      CriminalJustice            -> 6.45,
      Transport                  -> 12.32,
      BusinessAndIndustry        -> 2.3,
      GovernmentAdministration   -> 9.22,
      Culture                    -> 2.33,
      HousingAndUtilities        -> 8.18,
      OverseasAid                -> 3.09,
      UkContributionToEuBudget   -> 6.66,
      OutstandingPaymentsToTheEU -> 7.13,
      PublicOrderAndSafety       -> 10.33,
      Environment                -> 11.44
    )

  private val appConfigItemsForTaxYear: Seq[Item] = randomPercentagesForTaxYear.map { g =>
    Item(g._1.apiValue, g._2)
  }

  private val appConfigItemsForPreviousTaxYear: Seq[Item] = randomPercentagesForPreviousTaxYear.map { g =>
    Item(g._1.apiValue, g._2)
  }

  private def expectedTotalForTaxYear(amount: Int): BigDecimal =
    BigDecimal(randomPercentagesForTaxYear.map(v => amount * v._2 / 100).sum)
      .setScale(2, BigDecimal.RoundingMode.HALF_UP)

  private def expectedTotalForPreviousTaxYear(amount: Int): BigDecimal =
    BigDecimal(randomPercentagesForPreviousTaxYear.map(v => amount * v._2 / 100).sum)
      .setScale(2, BigDecimal.RoundingMode.HALF_UP)

  private val testAmountUser1: Int = 1400
  private val testAmountUser2: Int = 2900

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockAppConfig)
    when(mockAppConfig.governmentSpend(ArgumentMatchers.eq(previousTaxYear)))
      .thenReturn(appConfigItemsForPreviousTaxYear)
    when(mockAppConfig.governmentSpend(ArgumentMatchers.eq(taxYear))).thenReturn(appConfigItemsForTaxYear)
  }

  private def governmentSpend(
    testAmount: Int,
    testYear: Int,
    pc: Seq[(GoodsAndServices, Double)],
    expTotal: BigDecimal
  ): Unit = {
    pc.foreach { item =>
      s"display correct amount for ${item._1.apiValue} with percentage value of ${item._2} for a user with total amount of $testAmount" in {
        val returnValue: GovernmentSpendingOutputWrapper =
          GovernmentSpendingOutputWrapper(mockAppConfig, new Amount(testAmount, "GBP"), testYear)

        val parsedYear = returnValue.taxYear

        testYear mustEqual parsedYear

        val govSpendData = returnValue.govSpendAmountData

        govSpendData(item._1).amount.amount must equal(
          BigDecimal(testAmount * item._2 / 100).setScale(2, BigDecimal.RoundingMode.HALF_UP)
        )
      }
    }

    s"total up correctly to $expTotal for a user with total amount of $testAmount" in {
      val returnValue: GovernmentSpendingOutputWrapper =
        GovernmentSpendingOutputWrapper(mockAppConfig, new Amount(testAmount, "GBP"), testYear)

      val parsedYear = returnValue.taxYear

      testYear mustEqual parsedYear

      val govSpendData = returnValue.govSpendAmountData

      val actualTotal: BigDecimal = govSpendData.values.foldLeft(BigDecimal(0.0)) {
        _ + _.amount.amount
      }
      actualTotal mustBe expTotal
    }
  }

  "GovernmentSpendingOutputWrapper must round trip through Json " in {
    forAll(Generators.genGovernmentSpending) { data =>
      val json = Json.toJson(data)
      val obj  = json.as[GovernmentSpendingOutputWrapper]
      obj mustBe data
    }
  }

  s"The Gov spending data for $taxYear"         must {
    behave like governmentSpend(
      testAmountUser1,
      taxYear,
      randomPercentagesForTaxYear,
      expectedTotalForTaxYear(testAmountUser1)
    )
    behave like governmentSpend(
      testAmountUser2,
      taxYear,
      randomPercentagesForTaxYear,
      expectedTotalForTaxYear(testAmountUser2)
    )
  }

  s"The Gov spending data for $previousTaxYear" must {
    behave like governmentSpend(
      testAmountUser1,
      previousTaxYear,
      randomPercentagesForPreviousTaxYear,
      expectedTotalForPreviousTaxYear(testAmountUser1)
    )
    behave like governmentSpend(
      testAmountUser2,
      previousTaxYear,
      randomPercentagesForPreviousTaxYear,
      expectedTotalForPreviousTaxYear(testAmountUser2)
    )
  }
}
