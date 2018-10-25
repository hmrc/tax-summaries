/*
 * Copyright 2018 HM Revenue & Customs
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
import models.Rate

class TaxRateServiceTest extends UnitSpec with MockitoSugar with ScalaFutures {

  "taxRateService" should {

    "return correct amounts for dividends ordinary rate 2014" in {
      val result: Rate = TaxRateService.dividendsOrdinaryRate(2014)
      result shouldBe Rate("10%")
    }

    "return correct amounts for dividends ordinary rate 2017" in {
      val result: Rate = TaxRateService.dividendsOrdinaryRate(2017)
      result shouldBe Rate("7.5%")
    }

    Seq(2011,2012,2013,2014,2015).foreach {
      year=>
        s"return correct percentage rate for Capital Gains ordinary rate for $year" in {
          val result: Rate=TaxRateService.cgOrdinaryRate(year)
          result shouldBe Rate("18%")
        }
    }

    Seq(2016,2017).foreach{
      year=>
        s"return correct percentage rate for Capital Gains ordinary rate for $year" in {
          val result: Rate=TaxRateService.cgOrdinaryRate(year)
          result shouldBe Rate("10%")
        }
    }

    Seq(2011,2012,2013,2014,2015).foreach {
      year=>
        s"return correct percentage rate for Capital Gains upper rate for $year" in {
          val result: Rate=TaxRateService.cgUpperRate(year)
          result shouldBe Rate("28%")
        }
    }

    Seq(2016,2017).foreach{
      year=>
        s"return correct percentage rate for Capital Gains upper rate for $year" in {
          val result: Rate=TaxRateService.cgUpperRate(year)
          result shouldBe Rate("20%")
        }
    }



  }

}
