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
import play.api.{Application, Mode}
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.play.test.UnitSpec

class ApplicationConfigSpec extends UnitSpec with GuiceOneAppPerTest {
  //abstract class WithSetup(additionalConfiguration: Map[String, _] = Map.empty)

  override def newAppForTest(testData: TestData): Application = {
    val builder = new GuiceApplicationBuilder().in(Mode.Test)

    def appWithConfig(config: Map[String, _]): Application = builder.configure(config).build()

    appWithConfig(Map("taxRates.default.whitelist" -> Seq("TaxField1","TaxField2"),"taxRates.2017.whitelist" -> Seq("TaxField3")))
  }

  "calling rate TaxFields" should {
    "return list taxFields" in {
      ApplicationConfig.taxFields(2017) shouldBe Seq("TaxField1","TaxField2","TaxField3")
    }
  }
}
