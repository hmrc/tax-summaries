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

import models.Liability._
import models.{Amount, Liability, PensionTaxRate, TaxSummaryLiability}
import org.scalatest.prop.PropertyChecks
import services.{DefaultTaxRateService, TaxRateService}
import uk.gov.hmrc.play.test.UnitSpec

class ATSCalculationsTest extends UnitSpec with PropertyChecks {

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

    class ScottishFixture(newPensionTaxRate: PensionTaxRate, newAtsData: (Liability, Amount)*) extends CalcFixtures {

      override val taxYear: Int = 2018
      override val isScottish: Boolean = true

      override val pensionTaxRate: PensionTaxRate = newPensionTaxRate
      override val atsData: Map[Liability, Amount] = newAtsData.toMap
    }

    object ScottishFixture {

      def apply(newPensionTaxRate: PensionTaxRate, newAtsData: (Liability, Amount)*): ScottishFixture =
        new ScottishFixture(newPensionTaxRate, newAtsData: _*)

      def apply(newAtsData: (Liability, Amount)*): ScottishFixture =
        new ScottishFixture(PensionTaxRate(0), newAtsData: _*)
    }

    val fixture = new ScottishFixture(PensionTaxRate(0)) {}

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

    "scottishStarterRateTaxAmount includes pension tax when pension rate matches starter rate" in {

      forAll { (income: BigDecimal, pension: BigDecimal) =>
        val sut = ScottishFixture(
          PensionTaxRate(0.19),
          TaxOnPayScottishStarterRate -> Amount.gbp(income),
          PensionLsumTaxDue           -> Amount.gbp(pension))

        sut.calculation.scottishStarterRateTax shouldBe Amount.gbp(income + pension)
      }
    }

    "scottishBasicRateTaxAmount includes pension tax when pension rate matches basic rate" in {

      forAll { (income: BigDecimal, pension: BigDecimal) =>
        val sut = ScottishFixture(
          PensionTaxRate(0.20),
          IncomeTaxBasicRate -> Amount.gbp(income),
          PensionLsumTaxDue  -> Amount.gbp(pension))

        sut.calculation.scottishBasicRateTax shouldBe Amount.gbp(income + pension)
      }
    }

    "scottishIntermediateRateTaxAmount includes pension tax when pension rate matches intermediate rate" in {

      forAll { (income: BigDecimal, pension: BigDecimal) =>
        val sut = ScottishFixture(
          PensionTaxRate(0.21),
          TaxOnPayScottishIntermediateRate -> Amount.gbp(income),
          PensionLsumTaxDue                -> Amount.gbp(pension))

        sut.calculation.scottishIntermediateRateTax shouldBe Amount.gbp(income + pension)
      }
    }

    "scottishHigherRateTaxAmount includes pension tax when pension rate matches higher rate" in {

      forAll { (income: BigDecimal, pension: BigDecimal) =>
        val sut = ScottishFixture(
          PensionTaxRate(0.41),
          IncomeTaxHigherRate -> Amount.gbp(income),
          PensionLsumTaxDue   -> Amount.gbp(pension))

        sut.calculation.scottishHigherRateTax shouldBe Amount.gbp(income + pension)
      }
    }

    "scottishAdditionalRateTaxAmount includes pension tax when pension rate matches additional rate" in {

      forAll { (income: BigDecimal, pension: BigDecimal) =>
        val sut = ScottishFixture(
          PensionTaxRate(0.46),
          IncomeTaxAddHighRate -> Amount.gbp(income),
          PensionLsumTaxDue    -> Amount.gbp(pension))

        sut.calculation.scottishAdditionalRateTax shouldBe Amount.gbp(income + pension)
      }
    }

    "scottishStarterRateIncome include pension lump sum amount when matches starter rate" in {

      forAll { (income: BigDecimal, pension: BigDecimal) =>
        val sut = ScottishFixture(
          PensionTaxRate(0.19),
          TaxablePayScottishStarterRate -> Amount.gbp(income),
          StatePensionGross             -> Amount.gbp(pension))

        sut.calculation.scottishStarterRateIncome shouldBe Amount.gbp(income + pension)
      }
    }

    "scottishStarterRateIncome include pension lump sum amount when matches basic rate" in {

      forAll { (income: BigDecimal, pension: BigDecimal) =>
        val sut = ScottishFixture(
          PensionTaxRate(0.20),
          IncomeTaxBasicRate -> Amount.gbp(income),
          StatePensionGross  -> Amount.gbp(pension))

        sut.calculation.scottishBasicRateIncome shouldBe Amount.gbp(income + pension)
      }
    }

    "scottishStarterRateIncome include pension lump sum amount when matches intermediate rate" in {

      forAll { (income: BigDecimal, pension: BigDecimal) =>
        val sut = ScottishFixture(
          PensionTaxRate(0.21),
          TaxablePayScottishIntermediateRate -> Amount.gbp(income),
          StatePensionGross                  -> Amount.gbp(pension))

        sut.calculation.scottishIntermediateRateIncome shouldBe Amount.gbp(income + pension)
      }
    }

    "scottishStarterRateIncome include pension lump sum amount when matches higher rate" in {

      forAll { (income: BigDecimal, pension: BigDecimal) =>
        val sut = ScottishFixture(
          PensionTaxRate(0.41),
          IncomeTaxHigherRate -> Amount.gbp(income),
          StatePensionGross   -> Amount.gbp(pension))

        sut.calculation.scottishHigherRateIncome shouldBe Amount.gbp(income + pension)
      }
    }

    "scottishStarterRateIncome include pension lump sum amount when matches additional rate" in {

      forAll { (income: BigDecimal, pension: BigDecimal) =>
        val sut = new ScottishFixture(
          PensionTaxRate(0.46),
          IncomeTaxAddHighRate -> Amount.gbp(income),
          StatePensionGross    -> Amount.gbp(pension))

        sut.calculation.scottishAdditionalRateIncome shouldBe Amount.gbp(income + pension)
      }
    }

    "savingsBasicRateTax returns tax on savings" in {

      forAll { tax: BigDecimal =>
        val sut = ScottishFixture(SavingsTaxLowerRate -> Amount.gbp(tax))
        sut.calculation.savingsBasicRateTax shouldBe Amount.gbp(tax)
      }
    }

    "savingsHigherRateTax returns tax on savings" in {

      forAll { tax: BigDecimal =>
        val sut = ScottishFixture(SavingsTaxHigherRate -> Amount.gbp(tax))
        sut.calculation.savingsHigherRateTax shouldBe Amount.gbp(tax)
      }
    }

    "savingsAdditionalRateTax returns tax on savings" in {

      forAll { tax: BigDecimal =>
        val sut = ScottishFixture(SavingsTaxAddHighRate -> Amount.gbp(tax))
        sut.calculation.savingsAdditionalRateTax shouldBe Amount.gbp(tax)
      }
    }

    "savingsBasicRateIncome returns income on savings" in {

      forAll { tax: BigDecimal =>
        val sut = ScottishFixture(SavingsChargeableLowerRate -> Amount.gbp(tax))
        sut.calculation.savingsBasicRateIncome shouldBe Amount.gbp(tax)
      }
    }

    "savingsHigherRateIncome returns income on savings" in {

      forAll { tax: BigDecimal =>
        val sut = ScottishFixture(SavingsChargeableHigherRate -> Amount.gbp(tax))
        sut.calculation.savingsHigherRateIncome shouldBe Amount.gbp(tax)
      }
    }

    "savingsAdditionalRateIncome returns income on savings" in {

      forAll { tax: BigDecimal =>
        val sut = ScottishFixture(SavingsChargeableAddHRate -> Amount.gbp(tax))
        sut.calculation.savingsAdditionalRateIncome shouldBe Amount.gbp(tax)
      }
    }
  }
}
