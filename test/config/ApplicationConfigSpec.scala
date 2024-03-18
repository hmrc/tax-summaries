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

package config

import models.Item
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import utils.BaseSpec

class ApplicationConfigSpec extends BaseSpec {

  override implicit lazy val app: Application = {
    def appWithConfig(config: Map[String, Any]): Application =
      new GuiceApplicationBuilder().configure(Map("taxRates" -> "", "governmentSpend" -> "")).configure(config).build()

    val percentageRates                                      = Map(
      "taxRates.default.percentages" -> Map("percentageRate1" -> 10, "percentageRate2" -> 20, "percentageRate3" -> 30),
      "taxRates.2017.percentages"    -> Map("percentageRate2" -> 60)
    )

    val governmentSpend =
      Map(
        "governmentSpend.2023.percentages" ->
          Map(
            "1"  -> Map("Welfare" -> 24.52),
            "2"  -> Map("Health" -> 18.87),
            "3"  -> Map("Education" -> 13.15),
            "4"  -> Map("StatePensions" -> 12.12),
            "5"  -> Map("NationalDebtInterest" -> 7.00),
            "6"  -> Map("Defence" -> 5.31),
            "7"  -> Map("CriminalJustice" -> 4.40),
            "8"  -> Map("Transport" -> 2.95),
            "9"  -> Map("BusinessAndIndustry" -> 2.74),
            "10" -> Map("GovernmentAdministration" -> 2.05),
            "12" -> Map("Environment" -> 1.66),
            "11" -> Map("Culture" -> 1.69),
            "13" -> Map("HousingAndUtilities" -> 1.64),
            "14" -> Map("OverseasAid" -> 1.15),
            "15" -> Map("UkContributionToEuBudget" -> 0.75)
          )
      )

    appWithConfig(percentageRates ++ governmentSpend)
  }

  "calling ratePercentages" must {
    "return default percentage rates when no overriding year" in {
      applicationConfig
        .ratePercentages(2016) mustBe Map("percentageRate1" -> 10, "percentageRate2" -> 20, "percentageRate3" -> 30)
    }

    "return default percentage but override where later year uprates" in {
      applicationConfig
        .ratePercentages(2017) mustBe Map("percentageRate1" -> 10, "percentageRate2" -> 60, "percentageRate3" -> 30)
    }
  }

  "calling governmentSpend" must {
    "return the with Seq of Tuples in Item class ordered with the index number" in {
      applicationConfig
        .governmentSpend(2023) mustBe
        Seq(
          Item("Welfare", 24.52),
          Item("Health", 18.87),
          Item("Education", 13.15),
          Item("StatePensions", 12.12),
          Item("NationalDebtInterest", 7.00),
          Item("Defence", 5.31),
          Item("CriminalJustice", 4.40),
          Item("Transport", 2.95),
          Item("BusinessAndIndustry", 2.74),
          Item("GovernmentAdministration", 2.05),
          Item("Culture", 1.69),
          Item("Environment", 1.66),
          Item("HousingAndUtilities", 1.64),
          Item("OverseasAid", 1.15),
          Item("UkContributionToEuBudget", 0.75)
        )
    }

    "return the empty Seq when the year does not exist and not error out" in {
      applicationConfig
        .governmentSpend(2024) mustBe
        Seq()
    }
  }

}
