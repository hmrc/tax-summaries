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

import java.time._

class TaxRateServiceTest extends BaseSpec {

  val ratePercentages: Int => Map[String, Double] = applicationConfig.ratePercentages

  val startOfTaxYear: MonthDay = MonthDay.of(4, 6)
  val now: LocalDate = LocalDate.now()

  val lastCompletedTaxYear: Int = if (MonthDay.of(now.getMonth, now.getDayOfMonth).isBefore(startOfTaxYear)) {
    now.getYear - 1
  } else {
    now.getYear
  }

  "taxRateService" must {

    Seq(2014, 2015, 2016).foreach { year =>
      s"return correct amounts for Dividends Ordinary Rate for $year" in {
        val taxRate = new TaxRateService(year, ratePercentages)
        val result = taxRate.dividendsOrdinaryRate()
        result mustBe Rate(10)
      }
    }

    (2017 to lastCompletedTaxYear).foreach { year =>
      s"return correct amounts for Dividends Ordinary Rate for $year" in {
        val taxRate = new TaxRateService(year, ratePercentages)
        val result = taxRate.dividendsOrdinaryRate()
        result mustBe Rate(7.5)
      }
    }

    (2014 to lastCompletedTaxYear).foreach { year =>
      s"return correct amounts for Dividends Upper Rate for $year" in {
        val taxRate = new TaxRateService(year, ratePercentages)
        val result = taxRate.dividendUpperRateRate()
        result mustBe Rate(32.5)
      }
    }

    (2017 to lastCompletedTaxYear).foreach { year =>
      s"return correct amounts for Dividends Additional Rate for $year" in {
        val taxRate = new TaxRateService(year, ratePercentages)
        val result = taxRate.dividendAdditionalRate()
        result mustBe Rate(38.1)
      }
    }

    Seq(2012, 2013, 2014, 2015, 2016).foreach { year =>
      s"return correct percentage rate for Capital Gains ordinary rate for $year" in {
        val taxRate = new TaxRateService(year, ratePercentages)
        val result = taxRate.cgOrdinaryRate()
        result mustBe Rate(18)
      }
    }

    (2017 to lastCompletedTaxYear).foreach { year =>
      s"return correct percentage rate for Capital Gains ordinary rate for $year" in {
        val taxRate = new TaxRateService(year, ratePercentages)
        val result = taxRate.cgOrdinaryRate()
        result mustBe Rate(10)
      }
    }

    Seq(2012, 2013, 2014, 2015, 2016).foreach { year =>
      s"return correct percentage rate for Capital Gains upper rate for $year" in {
        val taxRate = new TaxRateService(year, ratePercentages)
        val result = taxRate.cgUpperRate()
        result mustBe Rate(28)
      }
    }

    (2017 to lastCompletedTaxYear).foreach { year =>
      s"return correct percentage rate for Capital Gains upper rate for $year" in {
        val taxRate = new TaxRateService(year, ratePercentages)
        val result = taxRate.cgUpperRate()
        result mustBe Rate(20)
      }
    }

    (2017 to lastCompletedTaxYear).foreach { year =>
      s"property tax and carried interest lower rate for $year" in {
        val taxRate = new TaxRateService(year, ratePercentages)
        val result = taxRate.individualsForResidentialPropertyAndCarriedInterestLowerRate()
        result mustBe Rate(18)
      }
    }

    Seq(2014, 2015, 2016).foreach { year =>
      s"property tax and carried interest lower rate for $year" in {
        val taxRate = new TaxRateService(year, ratePercentages)
        val result = taxRate.individualsForResidentialPropertyAndCarriedInterestLowerRate()
        result mustBe Rate(0)
      }
    }

    (2017 to lastCompletedTaxYear).foreach { year =>
      s"property tax and carried interest higher rate for $year" in {
        val taxRate = new TaxRateService(year, ratePercentages)
        val result = taxRate.individualsForResidentialPropertyAndCarriedInterestHigherRate()
        result mustBe Rate(28)
      }
    }

    Seq(2014, 2015, 2016).foreach { year =>
      s"property tax and carried interest higher rate for $year" in {
        val taxRate = new TaxRateService(year, ratePercentages)
        val result = taxRate.individualsForResidentialPropertyAndCarriedInterestHigherRate()
        result mustBe Rate(0)
      }
    }

  }
}
