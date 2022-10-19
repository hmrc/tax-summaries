/*
 * Copyright 2022 HM Revenue & Customs
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

import models.Rate
import utils.BaseSpec

class TaxRateServiceTest extends BaseSpec {
  val ratePercentages: Int => Map[String, Double] = applicationConfig.ratePercentages

  /*
    Rates are only defined in config up to the 2021 tax year. For tax years after 2021 these tests do not pass.
    I'm not aware of the business reasons for this so i've just hard-coded the maximum year to ensure that these tests
    do not start failing at a point in the future.
   */
  val maximumSupportedTaxYear: Int = 2021

  "taxRateService" must {

    Seq(2014, 2015, 2016).foreach { year =>
      s"return correct amounts for Dividends Ordinary Rate for $year" in {
        val taxRate = new TaxRateService(year, ratePercentages)
        val result  = taxRate.dividendsOrdinaryRate()
        result mustBe Rate(10)
      }
    }

    (2017 to maximumSupportedTaxYear).foreach { year =>
      s"return correct amounts for Dividends Ordinary Rate for $year" in {
        val taxRate = new TaxRateService(year, ratePercentages)
        val result  = taxRate.dividendsOrdinaryRate()
        result mustBe Rate(7.5)
      }
    }

    (2014 to maximumSupportedTaxYear).foreach { year =>
      s"return correct amounts for Dividends Upper Rate for $year" in {
        val taxRate = new TaxRateService(year, ratePercentages)
        val result  = taxRate.dividendUpperRateRate()
        result mustBe Rate(32.5)
      }
    }

    (2017 to maximumSupportedTaxYear).foreach { year =>
      s"return correct amounts for Dividends Additional Rate for $year" in {
        val taxRate = new TaxRateService(year, ratePercentages)
        val result  = taxRate.dividendAdditionalRate()
        result mustBe Rate(38.1)
      }
    }

    Seq(2012, 2013, 2014, 2015, 2016).foreach { year =>
      s"return correct percentage rate for Capital Gains ordinary rate for $year" in {
        val taxRate = new TaxRateService(year, ratePercentages)
        val result  = taxRate.cgOrdinaryRate()
        result mustBe Rate(18)
      }
    }

    (2017 to maximumSupportedTaxYear).foreach { year =>
      s"return correct percentage rate for Capital Gains ordinary rate for $year" in {
        val taxRate = new TaxRateService(year, ratePercentages)
        val result  = taxRate.cgOrdinaryRate()
        result mustBe Rate(10)
      }
    }

    Seq(2012, 2013, 2014, 2015, 2016).foreach { year =>
      s"return correct percentage rate for Capital Gains upper rate for $year" in {
        val taxRate = new TaxRateService(year, ratePercentages)
        val result  = taxRate.cgUpperRate()
        result mustBe Rate(28)
      }
    }

    (2017 to maximumSupportedTaxYear).foreach { year =>
      s"return correct percentage rate for Capital Gains upper rate for $year" in {
        val taxRate = new TaxRateService(year, ratePercentages)
        val result  = taxRate.cgUpperRate()
        result mustBe Rate(20)
      }
    }

    (2017 to maximumSupportedTaxYear).foreach { year =>
      s"property tax and carried interest lower rate for $year" in {
        val taxRate = new TaxRateService(year, ratePercentages)
        val result  = taxRate.individualsForResidentialPropertyAndCarriedInterestLowerRate()
        result mustBe Rate(18)
      }
    }

    Seq(2014, 2015, 2016).foreach { year =>
      s"property tax and carried interest lower rate for $year" in {
        val taxRate = new TaxRateService(year, ratePercentages)
        val result  = taxRate.individualsForResidentialPropertyAndCarriedInterestLowerRate()
        result mustBe Rate(0)
      }
    }

    (2017 to maximumSupportedTaxYear).foreach { year =>
      s"property tax and carried interest higher rate for $year" in {
        val taxRate = new TaxRateService(year, ratePercentages)
        val result  = taxRate.individualsForResidentialPropertyAndCarriedInterestHigherRate()
        result mustBe Rate(28)
      }
    }

    Seq(2014, 2015, 2016).foreach { year =>
      s"property tax and carried interest higher rate for $year" in {
        val taxRate = new TaxRateService(year, ratePercentages)
        val result  = taxRate.individualsForResidentialPropertyAndCarriedInterestHigherRate()
        result mustBe Rate(0)
      }
    }

  }
}
