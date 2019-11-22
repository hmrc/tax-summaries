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

package transformers

import models.Liability.{IncomeTaxAddHighRate, IncomeTaxBasicRate, IncomeTaxHigherRate, PensionLsumTaxDue, TaxOnPayScottishIntermediateRate, TaxOnPayScottishStarterRate}
import models.{Amount, Liability, PensionTaxRate, TaxSummaryLiability}
import services.{DefaultTaxRateService, TaxRateService}
import uk.gov.hmrc.play.test.UnitSpec

class ATSCalculationsTest extends UnitSpec {

  trait CalcFixtures { self =>

    val taxYear: Int
    val isScottish: Boolean
    val pensionTaxRate: PensionTaxRate = PensionTaxRate(0)
    val niData: Map[Liability, Amount] = Map()
    val atsData: Map[Liability, Amount] = Map()
    val configRates: Map[String, Double] = Map(
      "scottishStarterRate"      -> 19,
      "scottishBasicRate"        -> 20,
      "scottishIntermediateRate" -> 21,
      "scottishHigherRate"       -> 41,
      "scottishAdditionalRate"   -> 46
    )

    lazy val taxSummaryLiability =
      TaxSummaryLiability(taxYear, pensionTaxRate, if (isScottish) Some("0002") else None, niData, atsData)
    lazy val taxRateService = new TaxRateService {
      override val taxYear: Int = self.taxYear
      override val configRate: Int => Map[String, Double] = _ => configRates
    }

    lazy val calculation: ATSCalculations = ATSCalculations.make(taxSummaryLiability, taxRateService)
  }

  "make" should {

    "return Post2017ScottishATSCalculations" when {

      "tax years is > 2017 and type is scottish" in new CalcFixtures {

        override val taxYear = 2018
        override val isScottish: Boolean = true

        calculation shouldBe a[Post2017ScottishATSCalculations]
      }
    }

    "return Post2017ATSCalculations" when {

      "tax year is > 2017" in new CalcFixtures {

        override val taxYear = 2018
        override val isScottish: Boolean = false

        calculation shouldBe a[Post2017ATSCalculations]
      }
    }

    "return DefaultATSCalculations" when {

      "tax year is < 2018 and type is scottish" in new CalcFixtures {

        override val taxYear = 2017
        override val isScottish: Boolean = true

        calculation shouldBe a[DefaultATSCalculations]
      }

      "tax year is < 2018" in new CalcFixtures {

        override val taxYear = 2017
        override val isScottish: Boolean = false

        calculation shouldBe a[DefaultATSCalculations]
      }
    }
  }

  "Post2017ATSCalculations" should {

    val fixture = new CalcFixtures {
      override val taxYear: Int = 2018
      override val isScottish: Boolean = false
    }

    "return an empty amount for scottishIncomeTax" in {
      import fixture._

      calculation.scottishIncomeTax shouldBe Amount.empty
    }
  }

  "Post2017ScottishATSCalculations" should {

    trait ScottishFixture extends CalcFixtures {
      override val taxYear: Int = 2018
      override val isScottish: Boolean = true
    }

    val fixture = new ScottishFixture {}

    "return an empty amount for scottishIncomeTax" in {
      import fixture._
      calculation.scottishIncomeTax shouldBe Amount.empty
    }

    "return an empty amount for basicRateIncomeTaxAmount" in {
      import fixture._
      calculation.basicRateIncomeTaxAmount shouldBe Amount.empty
    }

    "return an empty amount for higherRateIncomeTaxAmount" in {
      import fixture._
      calculation.higherRateIncomeTaxAmount shouldBe Amount.empty
    }

    "return an empty amount for additionalRateIncomeTaxAmount" in {
      import fixture._
      calculation.additionalRateIncomeTaxAmount shouldBe Amount.empty
    }

    "scottishStarterRateTaxAmount is starter rate amount" in new ScottishFixture {

      override val atsData: Map[Liability, Amount] = Map(TaxOnPayScottishStarterRate -> Amount.gbp(200))
      calculation.scottishStarterRateTaxAmount shouldBe Amount.gbp(200)
    }

    "scottishStarterRateTaxAmount includes pension tax when pension rate matches starter rate" in new ScottishFixture {

      override val pensionTaxRate: PensionTaxRate = PensionTaxRate(0.19)
      override val atsData: Map[Liability, Amount] =
        Map(TaxOnPayScottishStarterRate -> Amount.gbp(200), PensionLsumTaxDue -> Amount.gbp(300))

      calculation.scottishStarterRateTaxAmount shouldBe Amount.gbp(500)
    }

    "scottishBasicRateTaxAmount is basic rate amount" in new ScottishFixture {

      override val atsData: Map[Liability, Amount] = Map(IncomeTaxBasicRate -> Amount.gbp(200))
      calculation.scottishBasicRateTaxAmount shouldBe Amount.gbp(200)
    }

    "scottishBasicRateTaxAmount includes pension tax when pension rate matches basic rate" in new ScottishFixture {

      override val pensionTaxRate: PensionTaxRate = PensionTaxRate(0.2)
      override val atsData: Map[Liability, Amount] =
        Map(IncomeTaxBasicRate -> Amount.gbp(200), PensionLsumTaxDue -> Amount.gbp(300))

      calculation.scottishBasicRateTaxAmount shouldBe Amount.gbp(500)
    }

    "scottishIntermediateRateTaxAmount is intermediate rate amount" in new ScottishFixture {

      override val atsData: Map[Liability, Amount] = Map(TaxOnPayScottishIntermediateRate -> Amount.gbp(200))
      calculation.scottishIntermediateRateTaxAmount shouldBe Amount.gbp(200)
    }

    "scottishIntermediateRateTaxAmount includes pension tax when pension rate matches intermediate rate" in new ScottishFixture {

      override val pensionTaxRate: PensionTaxRate = PensionTaxRate(0.21)
      override val atsData: Map[Liability, Amount] =
        Map(TaxOnPayScottishIntermediateRate -> Amount.gbp(200), PensionLsumTaxDue -> Amount.gbp(300))

      calculation.scottishIntermediateRateTaxAmount shouldBe Amount.gbp(500)
    }

    "scottishHigherRateTaxAmount is higher rate amount" in new ScottishFixture {

      override val atsData: Map[Liability, Amount] = Map(IncomeTaxHigherRate -> Amount.gbp(200))
      calculation.scottishHigherRateTaxAmount shouldBe Amount.gbp(200)
    }

    "scottishHigherRateTaxAmount includes pension tax when pension rate matches higher rate" in new ScottishFixture {

      override val pensionTaxRate: PensionTaxRate = PensionTaxRate(0.41)
      override val atsData: Map[Liability, Amount] =
        Map(IncomeTaxHigherRate -> Amount.gbp(200), PensionLsumTaxDue -> Amount.gbp(300))

      calculation.scottishHigherRateTaxAmount shouldBe Amount.gbp(500)
    }

    "scottishAdditionalRateTaxAmount is additional rate amount" in new ScottishFixture {

      override val atsData: Map[Liability, Amount] = Map(IncomeTaxAddHighRate -> Amount.gbp(200))
      calculation.scottishAdditionalRateTaxAmount shouldBe Amount.gbp(200)
    }

    "scottishAdditionalRateTaxAmount includes pension tax when pension rate matches additional rate" in new ScottishFixture {

      override val pensionTaxRate: PensionTaxRate = PensionTaxRate(0.46)
      override val atsData: Map[Liability, Amount] =
        Map(IncomeTaxAddHighRate -> Amount.gbp(200), PensionLsumTaxDue -> Amount.gbp(300))

      calculation.scottishAdditionalRateTaxAmount shouldBe Amount.gbp(500)
    }
  }
}
