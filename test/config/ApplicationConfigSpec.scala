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

package config

import org.scalatest.TestData
import org.scalatestplus.play.guice.GuiceOneAppPerTest
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.play.test.UnitSpec

class ApplicationConfigSpec extends UnitSpec with GuiceOneAppPerTest {
  //abstract class WithSetup(additionalConfiguration: Map[String, _] = Map.empty)

  override def newAppForTest(testData: TestData): Application = {

    def appWithConfig(config: Map[String, Any]): Application =
      new GuiceApplicationBuilder().configure(Map("taxRates" -> "")).configure(config).build()

    val taxRates =
      Map("taxRates.default.whitelist" -> Seq("TaxField1", "TaxField2"), "taxRates.2017.whitelist" -> Seq("TaxField3"))

    val percentageRates = Map(
      "taxRates.default.percentages" -> Map("percentageRate1" -> 10, "percentageRate2" -> 20, "percentageRate3" -> 30),
      "taxRates.2017.percentages"    -> Map("percentageRate2" -> 60)
    )

    appWithConfig(taxRates ++ percentageRates)
  }

  "calling rate TaxFields" should {
    "return default taxFields when no overriding year" in {
      ApplicationConfig.taxFields(2016) shouldBe Seq("TaxField1", "TaxField2")
    }

    "return default taxFields with extra fields  for specific year" in {
      ApplicationConfig.taxFields(2017) shouldBe Seq("TaxField1", "TaxField2", "TaxField3")
    }

  }

  "calling ratePercentages" should {
    "return default percentage rates when no overriding year" in {
      ApplicationConfig.ratePercentages(2016) shouldBe Map(
        "percentageRate1" -> 10,
        "percentageRate2" -> 20,
        "percentageRate3" -> 30)
    }

    "return default percentage but override where later year uprates" in {
      ApplicationConfig.ratePercentages(2017) shouldBe Map(
        "percentageRate1" -> 10,
        "percentageRate2" -> 60,
        "percentageRate3" -> 30)
    }
  }
}
