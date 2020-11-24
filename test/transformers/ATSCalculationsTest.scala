/*
 * Copyright 2020 HM Revenue & Customs
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
import org.scalatest.prop.PropertyChecks
import play.api.Configuration
import services.TaxRateService
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import utils.{BaseSpec, DoubleUtils}

import scala.util.Random

sealed trait Origin
final case object Scottish extends Origin
final case object Welsh extends Origin
final case object RestUK extends Origin

class ATSCalculationsTest extends BaseSpec with PropertyChecks with DoubleUtils {

  class CalcFixtures(val taxYear: Int, val origin: Origin, applicationConfig: ApplicationConfig)(
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
      case Scottish => Some("0002")
      case Welsh    => Some("0003")
      case _        => None
    }

    lazy val taxSummaryLiability =
      TaxSummaryLiability(taxYear, pensionTaxRate, incomeTaxStatus, niData, atsData)

    lazy val taxRateService = new TaxRateService(self.taxYear, _ => configRates)

    lazy val calculation: ATSCalculations = ATSCalculations.make(taxSummaryLiability, taxRateService, applicationConfig)
  }

  class Fixture(val taxYear: Int, val origin: Origin, applicationConfig: ApplicationConfig = applicationConfig) {

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

  "make" should {

    "return Post2018ScottishATSCalculations" when {

      "tax years is > 2018 and type is scottish" in {

        val calculation = new Fixture(2019, Scottish)().calculation
        calculation shouldBe a[Post2018ScottishATSCalculations]
      }
    }

    "return Post2018rUKATSCalculations" when {

      "tax year is > 2018" in {
        val calculation = new Fixture(2019, RestUK)().calculation
        calculation shouldBe a[Post2018rUKATSCalculations]
      }

      "return WelshATSCalculations" when {
        "tax year is >= 2019" in {
          forAll { (taxYear: Int) =>
            val calculation = new Fixture(taxYear, Welsh)().calculation

            if (taxYear >= 2019) {
              calculation shouldBe a[WelshATSCalculations]
            } else {
              calculation shouldBe a[DefaultATSCalculations]
            }

          }
        }
      }
    }

    "return DefaultATSCalculations" when {

      "tax year is < 2019 and type is scottish" in {

        val calculation = new Fixture(2018, Scottish)().calculation
        calculation shouldBe a[DefaultATSCalculations]
      }

      "tax year is < 2019" in {

        val calculation = new Fixture(2018, RestUK)().calculation
        calculation shouldBe a[DefaultATSCalculations]
      }
    }
  }

  "DefaultATSCalculations" should {

    val fixture = new Fixture(2016, RestUK)

    "basicIncomeRateIncomeTax includes pension tax when pension rate matches basic rate" in {

      forAll { (income: BigDecimal, pension: BigDecimal, savings: BigDecimal) =>
        val sut = fixture(
          PensionTaxRate(0.20),
          IncomeChargeableBasicRate  -> Amount.gbp(income),
          StatePensionGross          -> Amount.gbp(pension),
          SavingsChargeableLowerRate -> Amount.gbp(savings)
        )

        sut.calculation.basicRateIncomeTax shouldBe Amount.gbp(income + pension + savings)
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

        sut.calculation.higherRateIncomeTax shouldBe Amount.gbp(income + pension + savings)
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

        sut.calculation.additionalRateIncomeTax shouldBe Amount.gbp(income + pension + savings)
      }
    }

    "includePensionIncomeForRate returns StatePensionGross when percentages match" in {

      forAll { (rate: Double, total: BigDecimal) =>
        val sum = List.fill(10)(rate).fold(0.0)(_ + _)
        val prod = rate * 10

        val sut = fixture(PensionTaxRate(sum / 100), StatePensionGross -> Amount.gbp(total))
        sut.calculation.includePensionIncomeForRate(Rate(prod)) shouldBe Amount.gbp(total)
      }
    }

    "includePensionIncomeForRate returns 0 when rates don't match" in {

      forAll { (rate1: Double, rate2: Double) =>
        whenever(rate1 !== rate2) {
          val sut = fixture(PensionTaxRate(rate1 / 100))
          sut.calculation.includePensionIncomeForRate(Rate(rate2)) shouldBe Amount.empty
        }
      }
    }

    "includePensionTaxForRate returns PensionLsumTaxDue when percentages match" in {

      forAll { (rate: Double, total: BigDecimal) =>
        val sum = List.fill(10)(rate).fold(0.0)(_ + _)
        val prod = rate * 10

        val sut = fixture(PensionTaxRate(sum / 100), PensionLsumTaxDue -> Amount.gbp(total))
        sut.calculation.includePensionTaxForRate(Rate(prod)) shouldBe Amount.gbp(total)
      }
    }

    "includePensionTaxForRate returns 0 when rates don't match" in {

      forAll { (rate1: Double, rate2: Double) =>
        whenever(rate1 !== rate2) {
          val sut = fixture(PensionTaxRate(rate1 / 100))
          sut.calculation.includePensionTaxForRate(Rate(rate2)) shouldBe Amount.empty
        }
      }
    }

    "totalCapitalGainsTax returns correct calculation" in {

      forAll { (lower: BigDecimal, higher: BigDecimal) =>
        val sut = fixture(
          LowerRateCgtRPCI  -> Amount.gbp(lower),
          HigherRateCgtRPCI -> Amount.gbp(higher)
        )

        sut.calculation.totalCapitalGainsTax shouldBe Amount.gbp(lower + higher)
      }
    }

    "return empty for welshIncomeTax" in {
      fixture().calculation.welshIncomeTax shouldBe Amount.empty
    }
  }

  "Post2018rUKATSCalculations" should {

    val fixture = new Fixture(2019, RestUK)()
    val calculation = fixture.calculation

    "return an empty amount for scottishIncomeTax" in {

      calculation.scottishIncomeTax shouldBe Amount.empty
    }

    "return empty for savingsRate" in {

      calculation.savingsRate shouldBe Amount.empty
    }

    "return empty for savingsRateAmount" in {

      calculation.savingsRateAmount shouldBe Amount.empty
    }

    "return empty for welshIncomeTax" in {
      calculation.welshIncomeTax shouldBe Amount.empty
    }
  }

  "Post2018ScottishATSCalculations" should {

    val scottishFixture = new Fixture(taxYear = 2019, Scottish)
    val calculation = scottishFixture().calculation

    "return an empty amount for scottishIncomeTax" in {
      calculation.scottishIncomeTax shouldBe Amount.empty
    }

    "return an empty amount for basicRateIncomeTaxAmount" in {
      calculation.basicRateIncomeTaxAmount shouldBe Amount.empty
    }

    "return an empty amount for higherRateIncomeTaxAmount" in {
      calculation.higherRateIncomeTaxAmount shouldBe Amount.empty
    }

    "return an empty amount for additionalRateIncomeTaxAmount" in {
      calculation.additionalRateIncomeTaxAmount shouldBe Amount.empty
    }

    "return an empty amount for basicRateIncomeTax" in {
      calculation.basicRateIncomeTax shouldBe Amount.empty
    }

    "return an empty amount for higherRateIncomeTax" in {
      calculation.higherRateIncomeTax shouldBe Amount.empty
    }

    "return an empty amount for additionalRateIncomeTax" in {
      calculation.additionalRateIncomeTax shouldBe Amount.empty
    }

    "return empty for savingsRate" in {

      calculation.savingsRate shouldBe Amount.empty
    }

    "return empty for savingsRateAmount" in {

      calculation.savingsRateAmount shouldBe Amount.empty
    }

    "return empty for welshIncomeTax" in {
      calculation.welshIncomeTax shouldBe Amount.empty
    }

    "scottishStarterRateTaxAmount includes pension tax when pension rate matches starter rate" in {

      forAll { (income: BigDecimal, pension: BigDecimal) =>
        val sut = scottishFixture(
          PensionTaxRate(0.19),
          TaxOnPayScottishStarterRate -> Amount.gbp(income),
          PensionLsumTaxDue           -> Amount.gbp(pension))

        sut.calculation.scottishStarterRateTax shouldBe Amount.gbp(income + pension)
      }
    }

    "scottishBasicRateTaxAmount includes pension tax when pension rate matches basic rate" in {

      forAll { (income: BigDecimal, pension: BigDecimal) =>
        val sut = scottishFixture(
          PensionTaxRate(0.20),
          IncomeTaxBasicRate -> Amount.gbp(income),
          PensionLsumTaxDue  -> Amount.gbp(pension))

        sut.calculation.scottishBasicRateTax shouldBe Amount.gbp(income + pension)
      }
    }

    "scottishIntermediateRateTaxAmount includes pension tax when pension rate matches intermediate rate" in {

      forAll { (income: BigDecimal, pension: BigDecimal) =>
        val sut = scottishFixture(
          PensionTaxRate(0.21),
          TaxOnPayScottishIntermediateRate -> Amount.gbp(income),
          PensionLsumTaxDue                -> Amount.gbp(pension))

        sut.calculation.scottishIntermediateRateTax shouldBe Amount.gbp(income + pension)
      }
    }

    "scottishHigherRateTaxAmount includes pension tax when pension rate matches higher rate" in {

      forAll { (income: BigDecimal, pension: BigDecimal) =>
        val sut = scottishFixture(
          PensionTaxRate(0.41),
          IncomeTaxHigherRate -> Amount.gbp(income),
          PensionLsumTaxDue   -> Amount.gbp(pension))

        sut.calculation.scottishHigherRateTax shouldBe Amount.gbp(income + pension)
      }
    }

    "scottishAdditionalRateTaxAmount includes pension tax when pension rate matches additional rate" in {

      forAll { (income: BigDecimal, pension: BigDecimal) =>
        val sut = scottishFixture(
          PensionTaxRate(0.46),
          IncomeTaxAddHighRate -> Amount.gbp(income),
          PensionLsumTaxDue    -> Amount.gbp(pension))

        sut.calculation.scottishAdditionalRateTax shouldBe Amount.gbp(income + pension)
      }
    }

    "scottishStarterRateIncome include pension lump sum amount when matches starter rate" in {

      forAll { (income: BigDecimal, pension: BigDecimal) =>
        val sut = scottishFixture(
          PensionTaxRate(0.19),
          TaxablePayScottishStarterRate -> Amount.gbp(income),
          StatePensionGross             -> Amount.gbp(pension))

        sut.calculation.scottishStarterRateIncome shouldBe Amount.gbp(income + pension)
      }
    }

    "scottishStarterRateIncome include pension lump sum amount when matches basic rate" in {

      forAll { (income: BigDecimal, pension: BigDecimal) =>
        val sut = scottishFixture(
          PensionTaxRate(0.20),
          IncomeChargeableBasicRate -> Amount.gbp(income),
          StatePensionGross         -> Amount.gbp(pension))

        sut.calculation.scottishBasicRateIncome shouldBe Amount.gbp(income + pension)
      }
    }

    "scottishStarterRateIncome include pension lump sum amount when matches intermediate rate" in {

      forAll { (income: BigDecimal, pension: BigDecimal) =>
        val sut = scottishFixture(
          PensionTaxRate(0.21),
          TaxablePayScottishIntermediateRate -> Amount.gbp(income),
          StatePensionGross                  -> Amount.gbp(pension))

        sut.calculation.scottishIntermediateRateIncome shouldBe Amount.gbp(income + pension)
      }
    }

    "scottishStarterRateIncome include pension lump sum amount when matches higher rate" in {

      forAll { (income: BigDecimal, pension: BigDecimal) =>
        val sut = scottishFixture(
          PensionTaxRate(0.41),
          IncomeChargeableHigherRate -> Amount.gbp(income),
          StatePensionGross          -> Amount.gbp(pension))

        sut.calculation.scottishHigherRateIncome shouldBe Amount.gbp(income + pension)
      }
    }

    "scottishStarterRateIncome include pension lump sum amount when matches additional rate" in {

      forAll { (income: BigDecimal, pension: BigDecimal) =>
        val sut = scottishFixture(
          PensionTaxRate(0.46),
          IncomeChargeableAddHRate -> Amount.gbp(income),
          StatePensionGross        -> Amount.gbp(pension))

        sut.calculation.scottishAdditionalRateIncome shouldBe Amount.gbp(income + pension)
      }
    }

    "savingsBasicRateTax returns tax on savings" in {

      forAll { tax: BigDecimal =>
        val sut = scottishFixture(SavingsTaxLowerRate -> Amount.gbp(tax))
        sut.calculation.savingsBasicRateTax shouldBe Amount.gbp(tax)
      }
    }

    "savingsHigherRateTax returns tax on savings" in {

      forAll { tax: BigDecimal =>
        val sut = scottishFixture(SavingsTaxHigherRate -> Amount.gbp(tax))
        sut.calculation.savingsHigherRateTax shouldBe Amount.gbp(tax)
      }
    }

    "savingsAdditionalRateTax returns tax on savings" in {

      forAll { tax: BigDecimal =>
        val sut = scottishFixture(SavingsTaxAddHighRate -> Amount.gbp(tax))
        sut.calculation.savingsAdditionalRateTax shouldBe Amount.gbp(tax)
      }
    }

    "savingsBasicRateIncome returns income on savings" in {

      forAll { tax: BigDecimal =>
        val sut = scottishFixture(SavingsChargeableLowerRate -> Amount.gbp(tax))
        sut.calculation.savingsBasicRateIncome shouldBe Amount.gbp(tax)
      }
    }

    "savingsHigherRateIncome returns income on savings" in {

      forAll { tax: BigDecimal =>
        val sut = scottishFixture(SavingsChargeableHigherRate -> Amount.gbp(tax))
        sut.calculation.savingsHigherRateIncome shouldBe Amount.gbp(tax)
      }
    }

    "savingsAdditionalRateIncome returns income on savings" in {

      forAll { tax: BigDecimal =>
        val sut = scottishFixture(SavingsChargeableAddHRate -> Amount.gbp(tax))
        sut.calculation.savingsAdditionalRateIncome shouldBe Amount.gbp(tax)
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

        sut.calculation.scottishTotalTax shouldBe Amount.gbp(first + second)
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

        sut.calculation.totalIncomeTaxAmount shouldBe Amount.gbp(first + second)
      }
    }
  }

  "WelshATSCalculations" should {
    "writ is enabled" should {
      "calculate the welshIncomeTax" in {
        val welshFixture = new Fixture(taxYear = 2020, Welsh)

        forAll { (basicRate: BigDecimal, higherRate: BigDecimal, additionalRate: BigDecimal) =>
          val sut = welshFixture(
            IncomeChargeableBasicRate  -> Amount.gbp(basicRate),
            IncomeChargeableHigherRate -> Amount.gbp(higherRate),
            IncomeChargeableAddHRate   -> Amount.gbp(additionalRate)
          )

          sut.calculation.welshIncomeTax shouldBe Amount.gbp((basicRate + higherRate + additionalRate) * 0.1)
          sut.calculation.savingsRate shouldBe Amount.empty
          sut.calculation.savingsRateAmount shouldBe Amount.empty
          sut.calculation.scottishIncomeTax shouldBe Amount.empty
        }
      }
    }

    "writ is disabled" should {
      "return empty for welshIncomeTax" in {
        val servicesConfig = app.injector.instanceOf[ServicesConfig]
        val configuration = app.injector.instanceOf[Configuration]

        class ApplicationConfigStub extends ApplicationConfig(servicesConfig, configuration) {
          override lazy val isSAWritEnabled = false
        }

        val welshFixture = new Fixture(taxYear = 2020, Welsh, new ApplicationConfigStub)

        forAll { (basicRate: BigDecimal, higherRate: BigDecimal, additionalRate: BigDecimal) =>
          val sut = welshFixture(
            IncomeChargeableBasicRate  -> Amount.gbp(basicRate),
            IncomeChargeableHigherRate -> Amount.gbp(higherRate),
            IncomeChargeableAddHRate   -> Amount.gbp(additionalRate)
          )

          sut.calculation.welshIncomeTax shouldBe Amount.empty
          sut.calculation.savingsRate shouldBe Amount.empty
          sut.calculation.savingsRateAmount shouldBe Amount.empty
          sut.calculation.scottishIncomeTax shouldBe Amount.empty
        }
      }
    }
  }
}
