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

package transformers

import config.ApplicationConfig
import models.Liability._
import models._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import services.TaxRateService
import transformers.Scottish.ATSCalculationsScottish2019
import transformers.UK.{ATSCalculationsUK2019, ATSCalculationsUK2021}
import transformers.Welsh.{ATSCalculationsWelsh2020, ATSCalculationsWelsh2021}
import utils.{BaseSpec, DoubleUtils}

import scala.util.Random

class ATSCalculationsTest extends BaseSpec with ScalaCheckPropertyChecks with DoubleUtils {

  class CalcFixtures(val taxYear: Int, val origin: Nationality, applicationConfig: ApplicationConfig)(
    pensionTaxRate: PensionTaxRate,
    newAtsData: (Liability, Amount)*) { self =>

    val atsData: Map[Liability, Amount] = (newAtsData ++ emptyValues).toMap
    val niData: Map[Liability, Amount] = Map()
    val configRates: Map[String, Double] = Map(
      "basicRateIncomeTaxRate"      -> 20,
      "higherRateIncomeTaxRate"     -> 40,
      "additionalRateIncomeTaxRate" -> 45,
      "scottishStarterRate"         -> 19,
      "scottishBasicRate"           -> 20,
      "scottishIntermediateRate"    -> 21,
      "scottishHigherRate"          -> 41,
      "scottishAdditionalRate"      -> 46
    )

    val incomeTaxStatus = origin match {
      case _: Scottish => Some("0002")
      case _: Welsh    => Some("0003")
      case _           => None
    }

    lazy val taxSummaryLiability =
      TaxSummaryLiability(taxYear, pensionTaxRate, incomeTaxStatus, niData, atsData)

    lazy val taxRateService = new TaxRateService(self.taxYear, _ => configRates)

    lazy val calculation: ATSCalculations = ATSCalculations.make(taxSummaryLiability, taxRateService)
  }

  class Fixture(val taxYear: Int, origin: Nationality, applicationConfig: ApplicationConfig = applicationConfig) {

    def apply(): CalcFixtures =
      new CalcFixtures(taxYear, origin, applicationConfig)(PensionTaxRate(0))

    def apply(newPensionTaxRate: PensionTaxRate, newAtsData: (Liability, Amount)*): CalcFixtures =
      new CalcFixtures(taxYear, origin, applicationConfig)(newPensionTaxRate, newAtsData: _*)

    def apply(newAtsData: (Liability, Amount)*): CalcFixtures =
      new CalcFixtures(taxYear, origin, applicationConfig)(PensionTaxRate(0), newAtsData: _*)

    def apply(newAtsData: List[(Liability, Amount)]): CalcFixtures =
      new CalcFixtures(taxYear, origin, applicationConfig)(PensionTaxRate(0), newAtsData: _*)
  }

  val emptyValues = List(
    SavingsTaxStartingRate  -> Amount.empty,
    DividendTaxLowRate      -> Amount.empty,
    DividendTaxHighRate     -> Amount.empty,
    DividendTaxAddHighRate  -> Amount.empty,
    NonDomCharge            -> Amount.empty,
    TaxExcluded             -> Amount.empty,
    IncomeTaxDue            -> Amount.empty,
    NetAnnuityPaytsTaxDue   -> Amount.empty,
    ChildBenefitCharge      -> Amount.empty,
    PensionSavingChargeable -> Amount.empty,
    TaxDueAfterAllceRlf     -> Amount.empty,
    DeficiencyRelief        -> Amount.empty,
    TopSlicingRelief        -> Amount.empty,
    VctSharesRelief         -> Amount.empty,
    EisRelief               -> Amount.empty,
    SeedEisRelief           -> Amount.empty,
    CommInvTrustRel         -> Amount.empty,
    SurplusMcaAlimonyRel    -> Amount.empty,
    NotionalTaxCegs         -> Amount.empty,
    NotlTaxOtherSource      -> Amount.empty,
    TaxCreditsForDivs       -> Amount.empty,
    QualDistnRelief         -> Amount.empty,
    TotalTaxCreditRelief    -> Amount.empty,
    NonPayableTaxCredits    -> Amount.empty,
    CgDueEntrepreneursRate  -> Amount.empty,
    CgDueLowerRate          -> Amount.empty,
    CgDueHigherRate         -> Amount.empty,
    CapAdjustment           -> Amount.empty
  )

  "make" must {

    "return Post2018ScottishATSCalculations" when {

      "tax years is > 2018 and type is scottish" in {

        val calculation = new Fixture(2019, new Scottish())().calculation
        calculation mustBe a[ATSCalculationsScottish2019]
      }
    }

    "return Post2018rUKATSCalculations" when {

      "tax year is > 2018" in {
        val calculation = new Fixture(2019, new UK())().calculation
        calculation mustBe a[ATSCalculationsUK2019]
      }

      "return WelshATSCalculations" when {
        "tax year is > 2019" in {
          forAll { (taxYear: Int) =>
            val calculation = new Fixture(taxYear, new Welsh())().calculation

            if (taxYear == 2020) {
              calculation mustBe a[ATSCalculationsWelsh2020]
            } else if (taxYear > 2021) {
              calculation mustBe a[ATSCalculationsWelsh2021]
            } else {
              calculation mustBe a[DefaultATSCalculations]
            }

          }
        }
      }
    }

    "return DefaultATSCalculations" when {

      "tax year is < 2019 and type is scottish" in {

        val calculation = new Fixture(2018, new Scottish())().calculation
        calculation mustBe a[DefaultATSCalculations]
      }

      "tax year is < 2019" in {

        val calculation = new Fixture(2018, new UK())().calculation
        calculation mustBe a[DefaultATSCalculations]
      }
    }

    "return ATSCalculationsUK2020" when {
      "tax year is 2020 and type is UK" in {

        val calculation = new Fixture(2021, new UK())().calculation
        calculation mustBe a[ATSCalculationsUK2021]
      }
    }
  }

  "DefaultATSCalculations" must {

    val fixture = new Fixture(2016, new UK())

    "basicIncomeRateIncomeTax includes pension tax when pension rate matches basic rate" in {

      forAll { (income: BigDecimal, pension: BigDecimal, savings: BigDecimal) =>
        val sut = fixture(
          PensionTaxRate(0.20),
          IncomeChargeableBasicRate  -> Amount.gbp(income),
          StatePensionGross          -> Amount.gbp(pension),
          SavingsChargeableLowerRate -> Amount.gbp(savings)
        )

        sut.calculation.basicRateIncomeTax mustBe Amount.gbp(income + pension + savings)
      }
    }

    "higherRateIncomeTaxAmount includes pension tax when pension rate matches basic rate" in {

      forAll { (income: BigDecimal, pension: BigDecimal, savings: BigDecimal) =>
        val sut = fixture(
          PensionTaxRate(0.40),
          IncomeChargeableHigherRate  -> Amount.gbp(income),
          StatePensionGross           -> Amount.gbp(pension),
          SavingsChargeableHigherRate -> Amount.gbp(savings)
        )

        sut.calculation.higherRateIncomeTax mustBe Amount.gbp(income + pension + savings)
      }
    }

    "additionalRateIncomeTaxAmount includes pension tax when pension rate matches basic rate" in {

      forAll { (income: BigDecimal, pension: BigDecimal, savings: BigDecimal) =>
        val sut = fixture(
          PensionTaxRate(0.45),
          IncomeChargeableAddHRate  -> Amount.gbp(income),
          StatePensionGross         -> Amount.gbp(pension),
          SavingsChargeableAddHRate -> Amount.gbp(savings)
        )

        sut.calculation.additionalRateIncomeTax mustBe Amount.gbp(income + pension + savings)
      }
    }

    "includePensionIncomeForRate returns StatePensionGross when percentages match" in {

      forAll { (rate: Double, total: BigDecimal) =>
        val sum = List.fill(10)(rate).fold(0.0)(_ + _)
        val prod = rate * 10

        val sut = fixture(PensionTaxRate(sum / 100), StatePensionGross -> Amount.gbp(total))
        sut.calculation.includePensionIncomeForRate(Rate(prod)) mustBe Amount.gbp(total)
      }
    }

    "includePensionIncomeForRate returns 0 when rates don't match" in {

      forAll { (rate1: Double, rate2: Double) =>
        whenever(rate1 !== rate2) {
          val sut = fixture(PensionTaxRate(rate1 / 100))
          sut.calculation.includePensionIncomeForRate(Rate(rate2)) mustBe Amount.empty
        }
      }
    }

    "includePensionTaxForRate returns PensionLsumTaxDue when percentages match" in {

      forAll { (rate: Double, total: BigDecimal) =>
        val sum = List.fill(10)(rate).fold(0.0)(_ + _)
        val prod = rate * 10

        val sut = fixture(PensionTaxRate(sum / 100), PensionLsumTaxDue -> Amount.gbp(total))
        sut.calculation.includePensionTaxForRate(Rate(prod)).roundAmount() mustBe Amount.gbp(total).roundAmount()
      }
    }

    "includePensionTaxForRate returns 0 when rates don't match" in {

      forAll { (rate1: Double, rate2: Double) =>
        whenever(rate1 !== rate2) {
          val sut = fixture(PensionTaxRate(rate1 / 100))
          sut.calculation.includePensionTaxForRate(Rate(rate2)) mustBe Amount.empty
        }
      }
    }

    "totalCapitalGainsTax returns correct calculation" in {

      forAll { (lower: BigDecimal, higher: BigDecimal) =>
        val sut = fixture(
          LowerRateCgtRPCI  -> Amount.gbp(lower),
          HigherRateCgtRPCI -> Amount.gbp(higher)
        )

        sut.calculation.totalCapitalGainsTax mustBe Amount.gbp((lower + higher).max(0))
      }
    }

    "return empty for welshIncomeTax" in {
      fixture().calculation.welshIncomeTax mustBe Amount.empty
    }
  }

  "Post2018rUKATSCalculations" must {

    val fixture = new Fixture(2019, new UK())()
    val calculation = fixture.calculation

    "return an empty amount for scottishIncomeTax" in {

      calculation.scottishIncomeTax mustBe Amount.empty
    }

    "return empty for savingsRate" in {

      calculation.savingsRate mustBe Amount.empty
    }

    "return empty for savingsRateAmount" in {

      calculation.savingsRateAmount mustBe Amount.empty
    }

    "return empty for welshIncomeTax" in {
      calculation.welshIncomeTax mustBe Amount.empty
    }
  }

  "Post2018ScottishATSCalculations" must {

    val scottishFixture = new Fixture(taxYear = 2019, new Scottish())
    val calculation = scottishFixture().calculation

    "return an empty amount for scottishIncomeTax" in {
      calculation.scottishIncomeTax mustBe Amount.empty
    }

    "return an empty amount for basicRateIncomeTaxAmount" in {
      calculation.basicRateIncomeTaxAmount mustBe Amount.empty
    }

    "return an empty amount for higherRateIncomeTaxAmount" in {
      calculation.higherRateIncomeTaxAmount mustBe Amount.empty
    }

    "return an empty amount for additionalRateIncomeTaxAmount" in {
      calculation.additionalRateIncomeTaxAmount mustBe Amount.empty
    }

    "return an empty amount for basicRateIncomeTax" in {
      calculation.basicRateIncomeTax mustBe Amount.empty
    }

    "return an empty amount for higherRateIncomeTax" in {
      calculation.higherRateIncomeTax mustBe Amount.empty
    }

    "return an empty amount for additionalRateIncomeTax" in {
      calculation.additionalRateIncomeTax mustBe Amount.empty
    }

    "return empty for savingsRate" in {

      calculation.savingsRate mustBe Amount.empty
    }

    "return empty for savingsRateAmount" in {

      calculation.savingsRateAmount mustBe Amount.empty
    }

    "return empty for welshIncomeTax" in {
      calculation.welshIncomeTax mustBe Amount.empty
    }

    "scottishStarterRateTaxAmount includes pension tax when pension rate matches starter rate" in {

      forAll { (income: BigDecimal, pension: BigDecimal) =>
        val sut = scottishFixture(
          PensionTaxRate(0.19),
          TaxOnPayScottishStarterRate -> Amount.gbp(income),
          PensionLsumTaxDue           -> Amount.gbp(pension))

        sut.calculation.scottishStarterRateTax mustBe Amount.gbp(income + pension)
      }
    }

    "scottishBasicRateTaxAmount includes pension tax when pension rate matches basic rate" in {

      forAll { (income: BigDecimal, pension: BigDecimal) =>
        val sut = scottishFixture(
          PensionTaxRate(0.20),
          IncomeTaxBasicRate -> Amount.gbp(income),
          PensionLsumTaxDue  -> Amount.gbp(pension))

        sut.calculation.scottishBasicRateTax.roundAmount() mustBe Amount.gbp(income + pension).roundAmount()
      }
    }

    "scottishIntermediateRateTaxAmount includes pension tax when pension rate matches intermediate rate" in {

      forAll { (income: BigDecimal, pension: BigDecimal) =>
        val sut = scottishFixture(
          PensionTaxRate(0.21),
          TaxOnPayScottishIntermediateRate -> Amount.gbp(income),
          PensionLsumTaxDue                -> Amount.gbp(pension))

        sut.calculation.scottishIntermediateRateTax.roundAmount() mustBe Amount.gbp(income + pension).roundAmount()
      }
    }

    "scottishHigherRateTaxAmount includes pension tax when pension rate matches higher rate" in {

      forAll { (income: BigDecimal, pension: BigDecimal) =>
        val sut = scottishFixture(
          PensionTaxRate(0.41),
          IncomeTaxHigherRate -> Amount.gbp(income),
          PensionLsumTaxDue   -> Amount.gbp(pension))

        sut.calculation.scottishHigherRateTax.roundAmount() mustBe Amount.gbp(income + pension).roundAmount()
      }
    }

    "scottishAdditionalRateTaxAmount includes pension tax when pension rate matches additional rate" in {

      forAll { (income: BigDecimal, pension: BigDecimal) =>
        val sut = scottishFixture(
          PensionTaxRate(0.46),
          IncomeTaxAddHighRate -> Amount.gbp(income),
          PensionLsumTaxDue    -> Amount.gbp(pension))

        sut.calculation.scottishAdditionalRateTax.roundAmount() mustBe Amount.gbp(income + pension).roundAmount()
      }
    }

    "scottishStarterRateIncome include pension lump sum amount when matches starter rate" in {

      forAll { (income: BigDecimal, pension: BigDecimal) =>
        val sut = scottishFixture(
          PensionTaxRate(0.19),
          TaxablePayScottishStarterRate -> Amount.gbp(income),
          StatePensionGross             -> Amount.gbp(pension))

        sut.calculation.scottishStarterRateIncome.roundAmount() mustBe Amount.gbp(income + pension).roundAmount()
      }
    }

    "scottishStarterRateIncome include pension lump sum amount when matches basic rate" in {

      forAll { (income: BigDecimal, pension: BigDecimal) =>
        val sut = scottishFixture(
          PensionTaxRate(0.20),
          IncomeChargeableBasicRate -> Amount.gbp(income),
          StatePensionGross         -> Amount.gbp(pension))

        sut.calculation.scottishBasicRateIncome.roundAmount() mustBe Amount.gbp(income + pension).roundAmount()
      }
    }

    "scottishStarterRateIncome include pension lump sum amount when matches intermediate rate" in {

      forAll { (income: BigDecimal, pension: BigDecimal) =>
        val sut = scottishFixture(
          PensionTaxRate(0.21),
          TaxablePayScottishIntermediateRate -> Amount.gbp(income),
          StatePensionGross                  -> Amount.gbp(pension))

        sut.calculation.scottishIntermediateRateIncome.roundAmount() mustBe Amount.gbp(income + pension).roundAmount()
      }
    }

    "scottishStarterRateIncome include pension lump sum amount when matches higher rate" in {

      forAll { (income: BigDecimal, pension: BigDecimal) =>
        val sut = scottishFixture(
          PensionTaxRate(0.41),
          IncomeChargeableHigherRate -> Amount.gbp(income),
          StatePensionGross          -> Amount.gbp(pension))

        sut.calculation.scottishHigherRateIncome.roundAmount() mustBe Amount.gbp(income + pension).roundAmount()
      }
    }

    "scottishStarterRateIncome include pension lump sum amount when matches additional rate" in {

      forAll { (income: BigDecimal, pension: BigDecimal) =>
        val sut = scottishFixture(
          PensionTaxRate(0.46),
          IncomeChargeableAddHRate -> Amount.gbp(income),
          StatePensionGross        -> Amount.gbp(pension))

        sut.calculation.scottishAdditionalRateIncome.roundAmount() mustBe Amount.gbp(income + pension).roundAmount()
      }
    }

    "savingsBasicRateTax returns tax on savings" in {

      forAll { tax: BigDecimal =>
        val sut = scottishFixture(SavingsTaxLowerRate -> Amount.gbp(tax))
        sut.calculation.savingsBasicRateTax mustBe Amount.gbp(tax)
      }
    }

    "savingsHigherRateTax returns tax on savings" in {

      forAll { tax: BigDecimal =>
        val sut = scottishFixture(SavingsTaxHigherRate -> Amount.gbp(tax))
        sut.calculation.savingsHigherRateTax mustBe Amount.gbp(tax)
      }
    }

    "savingsAdditionalRateTax returns tax on savings" in {

      forAll { tax: BigDecimal =>
        val sut = scottishFixture(SavingsTaxAddHighRate -> Amount.gbp(tax))
        sut.calculation.savingsAdditionalRateTax mustBe Amount.gbp(tax)
      }
    }

    "savingsBasicRateIncome returns income on savings" in {

      forAll { tax: BigDecimal =>
        val sut = scottishFixture(SavingsChargeableLowerRate -> Amount.gbp(tax))
        sut.calculation.savingsBasicRateIncome mustBe Amount.gbp(tax)
      }
    }

    "savingsHigherRateIncome returns income on savings" in {

      forAll { tax: BigDecimal =>
        val sut = scottishFixture(SavingsChargeableHigherRate -> Amount.gbp(tax))
        sut.calculation.savingsHigherRateIncome mustBe Amount.gbp(tax)
      }
    }

    "savingsAdditionalRateIncome returns income on savings" in {

      forAll { tax: BigDecimal =>
        val sut = scottishFixture(SavingsChargeableAddHRate -> Amount.gbp(tax))
        sut.calculation.savingsAdditionalRateIncome mustBe Amount.gbp(tax)
      }
    }

    "scottishTotalTax includes any 2 random scottish taxes" in {

      forAll { (first: BigDecimal, second: BigDecimal) =>
        val keys =
          Random.shuffle(
            List(
              TaxOnPayScottishStarterRate,
              IncomeTaxBasicRate,
              TaxOnPayScottishIntermediateRate,
              IncomeTaxHigherRate,
              IncomeTaxAddHighRate))

        val sut = scottishFixture(
          keys.head -> Amount.gbp(first),
          keys(1)   -> Amount.gbp(second)
        )

        sut.calculation.scottishTotalTax.roundAmount() mustBe Amount.gbp(first + second).roundAmount()
      }
    }

    "totalIncomeTaxAmount includes any 2 random scottish taxes or savings taxes" in {

      forAll { (first: BigDecimal, second: BigDecimal) =>
        val keys =
          Random.shuffle(
            List(
              TaxOnPayScottishStarterRate,
              IncomeTaxBasicRate,
              TaxOnPayScottishIntermediateRate,
              IncomeTaxHigherRate,
              IncomeTaxAddHighRate,
              SavingsTaxLowerRate,
              SavingsTaxHigherRate,
              SavingsTaxAddHighRate
            ))

        val sut = scottishFixture(
          List(
            keys.head -> Amount.gbp(first),
            keys(1)   -> Amount.gbp(second)
          )
        )

        sut.calculation.totalIncomeTaxAmount.roundAmount() mustBe Amount.gbp(first + second).roundAmount()
      }
    }
  }

  "WelshATSCalculations" must {
    "calculate the welshIncomeTax" in {
      val welshFixture = new Fixture(taxYear = 2020, new Welsh())

      forAll { (basicRate: BigDecimal, higherRate: BigDecimal, additionalRate: BigDecimal) =>
        val sut = welshFixture(
          IncomeChargeableBasicRate  -> Amount.gbp(basicRate),
          IncomeChargeableHigherRate -> Amount.gbp(higherRate),
          IncomeChargeableAddHRate   -> Amount.gbp(additionalRate)
        )

        sut.calculation.welshIncomeTax
          .roundAmount() mustBe Amount.gbp((basicRate + higherRate + additionalRate) * 0.1).roundAmount()
        sut.calculation.savingsRate mustBe Amount.empty
        sut.calculation.savingsRateAmount mustBe Amount.empty
        sut.calculation.scottishIncomeTax mustBe Amount.empty
      }
    }
  }
}
