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

package sa.transformers

import common.models.Amount
import common.utils.BaseSpec
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import sa.models.ODSLiabilities.ODSLiabilities
import sa.models.ODSLiabilities.ODSLiabilities.{CapAdjustment, CgDueEntrepreneursRate, CgDueHigherRate, CgDueLowerRate, ChildBenefitCharge, CommInvTrustRel, DeficiencyRelief, DividendTaxAddHighRate, DividendTaxHighRate, DividendTaxLowRate, EisRelief, IncomeChargeableAddHRate, IncomeChargeableBasicRate, IncomeChargeableHigherRate, IncomeTaxAddHighRate, IncomeTaxBasicRate, IncomeTaxDue, IncomeTaxHigherRate, NetAnnuityPaytsTaxDue, NonDomCharge, NonPayableTaxCredits, NotionalTaxCegs, NotlTaxOtherSource, PensionLsumTaxDue, PensionSavingChargeable, QualDistnRelief, SavingsChargeableAddHRate, SavingsChargeableHigherRate, SavingsChargeableLowerRate, SavingsChargeableStartRate, SavingsTaxAddHighRate, SavingsTaxHigherRate, SavingsTaxLowerRate, SavingsTaxStartingRate, SeedEisRelief, StatePensionGross, SurplusMcaAlimonyRel, TaxCreditsForDivs, TaxDueAfterAllceRlf, TaxExcluded, TaxOnCegAhr, TaxOnPayScottishIntermediateRate, TaxOnPayScottishStarterRate, TaxablePayScottishIntermediateRate, TaxablePayScottishStarterRate, TopSlicingRelief, TotalTaxCreditRelief, VctSharesRelief}
import sa.models.{Nationality, PensionTaxRate, Scottish, TaxSummaryLiability, UK, Welsh}
import sa.services.TaxRateService
import sa.transformers.ATS2021.ATSCalculationsUK2021
import sa.transformers.ATS2024.{ATSCalculationsScottish2024, ATSCalculationsUK2024, ATSCalculationsWelsh2024}
import sa.utils.DoubleUtils

import scala.util.Random

class ATSCalculationsTest extends BaseSpec with ScalaCheckPropertyChecks with DoubleUtils {

  class CalcFixtures(val taxYear: Int, val origin: Nationality)(
    pensionTaxRate: PensionTaxRate,
    newAtsData: (ODSLiabilities, Amount)*
  ) { self =>

    val atsData: Map[ODSLiabilities, Amount] = (newAtsData ++ emptyValues).toMap
    val niData: Map[ODSLiabilities, Amount]  = Map()
    val configRates: Map[String, Double]     = Map(
      "basicRateIncomeTaxRate"      -> 20,
      "higherRateIncomeTaxRate"     -> 40,
      "additionalRateIncomeTaxRate" -> 45,
      "scottishStarterRate"         -> 19,
      "scottishBasicRate"           -> 20,
      "scottishIntermediateRate"    -> 21,
      "scottishHigherRate"          -> 41,
      "scottishAdditionalRate"      -> 46
    )

    val incomeTaxStatus: Option[String] = origin match {
      case _: Scottish => Some("0002")
      case _: Welsh    => Some("0003")
      case _           => None
    }

    lazy val taxSummaryLiability: TaxSummaryLiability = TaxSummaryLiability(
      taxYear,
      pensionTaxRate,
      Some(origin),
      niData,
      atsData
    )

    lazy val taxRateService = new TaxRateService(self.taxYear, _ => configRates)

    lazy val calculation: Option[ATSCalculations] = ATSCalculations.make(taxSummaryLiability, taxRateService)
  }

  class Fixture(val taxYear: Int, origin: Nationality) {

    def apply(): CalcFixtures =
      new CalcFixtures(taxYear, origin)(PensionTaxRate(0))

    def apply(newPensionTaxRate: PensionTaxRate, newAtsData: (ODSLiabilities, Amount)*): CalcFixtures =
      new CalcFixtures(taxYear, origin)(newPensionTaxRate, newAtsData: _*)

    def apply(newAtsData: (ODSLiabilities, Amount)*): CalcFixtures =
      new CalcFixtures(taxYear, origin)(PensionTaxRate(0), newAtsData: _*)

    def apply(newAtsData: List[(ODSLiabilities, Amount)]): CalcFixtures =
      new CalcFixtures(taxYear, origin)(PensionTaxRate(0), newAtsData: _*)
  }

  val emptyValues: List[(ODSLiabilities, Amount)] = List(
    SavingsTaxStartingRate  -> Amount.empty(SavingsTaxStartingRate.apiValue),
    DividendTaxLowRate      -> Amount.empty(DividendTaxLowRate.apiValue),
    DividendTaxHighRate     -> Amount.empty(DividendTaxHighRate.apiValue),
    DividendTaxAddHighRate  -> Amount.empty(DividendTaxAddHighRate.apiValue),
    NonDomCharge            -> Amount.empty(NonDomCharge.apiValue),
    TaxExcluded             -> Amount.empty(TaxExcluded.apiValue),
    IncomeTaxDue            -> Amount.empty(IncomeTaxDue.apiValue),
    NetAnnuityPaytsTaxDue   -> Amount.empty(NetAnnuityPaytsTaxDue.apiValue),
    ChildBenefitCharge      -> Amount.empty(ChildBenefitCharge.apiValue),
    PensionSavingChargeable -> Amount.empty(PensionSavingChargeable.apiValue),
    TaxDueAfterAllceRlf     -> Amount.empty(TaxDueAfterAllceRlf.apiValue),
    DeficiencyRelief        -> Amount.empty(DeficiencyRelief.apiValue),
    TopSlicingRelief        -> Amount.empty(TopSlicingRelief.apiValue),
    VctSharesRelief         -> Amount.empty(VctSharesRelief.apiValue),
    EisRelief               -> Amount.empty(EisRelief.apiValue),
    SeedEisRelief           -> Amount.empty(SeedEisRelief.apiValue),
    CommInvTrustRel         -> Amount.empty(CommInvTrustRel.apiValue),
    SurplusMcaAlimonyRel    -> Amount.empty(SurplusMcaAlimonyRel.apiValue),
    NotionalTaxCegs         -> Amount.empty(NotionalTaxCegs.apiValue),
    NotlTaxOtherSource      -> Amount.empty(NotlTaxOtherSource.apiValue),
    TaxCreditsForDivs       -> Amount.empty(TaxCreditsForDivs.apiValue),
    QualDistnRelief         -> Amount.empty(QualDistnRelief.apiValue),
    TotalTaxCreditRelief    -> Amount.empty(TotalTaxCreditRelief.apiValue),
    NonPayableTaxCredits    -> Amount.empty(NonPayableTaxCredits.apiValue),
    CgDueEntrepreneursRate  -> Amount.empty(CgDueEntrepreneursRate.apiValue),
    CgDueLowerRate          -> Amount.empty(CgDueLowerRate.apiValue),
    CgDueHigherRate         -> Amount.empty(CgDueHigherRate.apiValue),
    CapAdjustment           -> Amount.empty(CapAdjustment.apiValue),
    TaxOnCegAhr             -> Amount.empty(TaxOnCegAhr.apiValue)
  )

  "make" must {
    "return the latest calculations class for nationality when year > latest" when {
      "country is UK" in {
        val calculation = new Fixture(9999, UK())().calculation
        calculation.isDefined mustBe true
        calculation.map(_ mustBe a[ATSCalculationsUK2024])
      }
      "country is Scotland" in {
        val calculation = new Fixture(9999, Scottish())().calculation
        calculation.isDefined mustBe true
        calculation.map(_ mustBe a[ATSCalculationsScottish2024])
      }
      "country is Wales" in {
        val calculation = new Fixture(9999, Welsh())().calculation
        calculation.isDefined mustBe true
        calculation.map(_ mustBe a[ATSCalculationsWelsh2024])
      }
    }

    "return None" when {
      "tax year is < 2020 and type is scottish" in {
        val calculation = new Fixture(2019, Scottish())().calculation
        calculation mustBe None
      }

      "tax year is < 2020 and type is welsh" in {
        val calculation = new Fixture(2019, Welsh())().calculation
        calculation mustBe None
      }

      "tax year is < 2020 and type is UK" in {
        val calculation = new Fixture(2019, UK())().calculation
        calculation mustBe None
      }
    }

    "return ATSCalculationsUK2020" when {
      "tax year is 2020 and type is UK" in {
        val calculation = new Fixture(2021, UK())().calculation
        calculation.isDefined mustBe true
        calculation.map(_ mustBe a[ATSCalculationsUK2021])
      }
    }
  }

  "2020UKATSCalculations" must {

    val fixture     = new Fixture(2020, UK())()
    val calculation = fixture.calculation

    "return an empty amount for scottishIncomeTax" in {

      calculation.map(_.scottishIncomeTax) mustBe Some(Amount.empty("scottishIncomeTaxUK2020"))
    }

    "return empty for savingsRate" in {

      calculation.map(_.savingsRate) mustBe Some(Amount.empty("savingsRateUK2020"))
    }

    "return empty for savingsRateAmount" in {

      calculation.map(_.savingsRateAmount) mustBe Some(Amount.empty("savingsRateAmountUK2020"))
    }

    "return empty for welshIncomeTax" in {
      calculation.map(_.welshIncomeTax) mustBe Some(Amount.empty("welshIncomeTax"))
    }
  }

  "2020ScottishATSCalculations" must {

    val scottishFixture = new Fixture(taxYear = 2020, Scottish())
    val calculation     = scottishFixture().calculation

    "return an empty amount for scottishIncomeTax" in {
      calculation.map(_.scottishIncomeTax) mustBe Some(Amount.empty("scottishIncomeTaxScottish1019"))
    }

    "return an empty amount for basicRateIncomeTaxAmount" in {
      calculation.map(_.basicRateIncomeTaxAmount) mustBe Some(Amount.empty("basicRateIncomeTaxAmountScottish2020"))
    }

    "return an empty amount for higherRateIncomeTaxAmount" in {
      calculation.map(_.higherRateIncomeTaxAmount) mustBe Some(Amount.empty("higherRateIncomeTaxAmountScottish2020"))
    }

    "return an empty amount for additionalRateIncomeTaxAmount" in {
      calculation.map(_.additionalRateIncomeTaxAmount) mustBe Some(
        Amount.empty("additionalRateIncomeTaxAmountScottish2020")
      )
    }

    "return an empty amount for basicRateIncomeTax" in {
      calculation.map(_.basicRateIncomeTax) mustBe Some(Amount.empty("basicRateIncomeTaxScottish2020"))
    }

    "return an empty amount for higherRateIncomeTax" in {
      calculation.map(_.higherRateIncomeTax) mustBe Some(Amount.empty("higherRateIncomeTaxScottish2020"))
    }

    "return an empty amount for additionalRateIncomeTax" in {
      calculation.map(_.additionalRateIncomeTax) mustBe Some(Amount.empty("additionalRateIncomeTaxScottish2020"))
    }

    "return empty for savingsRate" in {

      calculation.map(_.savingsRate) mustBe Some(Amount.empty("savingsRateScottish2020"))
    }

    "return empty for savingsRateAmount" in {

      calculation.map(_.savingsRateAmount) mustBe Some(Amount.empty("savingsRateAmountScottish2020"))
    }

    "return empty for welshIncomeTax" in {
      calculation.map(_.welshIncomeTax) mustBe Some(Amount.empty("welshIncomeTax"))
    }

    "scottishStarterRateTaxAmount includes pension tax when pension rate matches starter rate" in {

      forAll { (incomeVal: Double, pensionVal: Double) =>
        val (income: BigDecimal, pension: BigDecimal) = (BigDecimal(incomeVal), BigDecimal(pensionVal))
        val sut                                       = scottishFixture(
          PensionTaxRate(0.19),
          TaxOnPayScottishStarterRate -> Amount.gbp(income, TaxOnPayScottishStarterRate.apiValue),
          PensionLsumTaxDue           -> Amount.gbp(pension, PensionLsumTaxDue.apiValue)
        )

        sut.calculation.map(_.scottishStarterRateTax) mustBe Some(
          Amount.gbp(
            income + pension,
            s"$income(taxOnPaySSR) + $pension(ctnPensionLsumTaxDueAmt)"
          )
        )
      }
    }

    "scottishBasicRateTaxAmount includes pension tax when pension rate matches basic rate" in {

      forAll { (incomeVal: Double, pensionVal: Double) =>
        val (income: BigDecimal, pension: BigDecimal) = (BigDecimal(incomeVal), BigDecimal(pensionVal))
        val sut                                       = scottishFixture(
          PensionTaxRate(0.20),
          IncomeTaxBasicRate -> Amount.gbp(income, IncomeTaxBasicRate.apiValue),
          PensionLsumTaxDue  -> Amount.gbp(pension, PensionLsumTaxDue.apiValue)
        )

        sut.calculation.map(_.scottishBasicRateTax.roundAmount()) mustBe Some(
          Amount
            .gbp(income + pension, s"$income(ctnIncomeTaxBasicRate) + $pension(ctnPensionLsumTaxDueAmt)")
            .roundAmount()
        )
      }
    }

    "scottishIntermediateRateTaxAmount includes pension tax when pension rate matches intermediate rate" in {

      forAll { (incomeVal: Double, pensionVal: Double) =>
        val (income: BigDecimal, pension: BigDecimal) = (BigDecimal(incomeVal), BigDecimal(pensionVal))
        val sut                                       = scottishFixture(
          PensionTaxRate(0.21),
          TaxOnPayScottishIntermediateRate -> Amount.gbp(income, TaxOnPayScottishIntermediateRate.apiValue),
          PensionLsumTaxDue                -> Amount.gbp(pension, PensionLsumTaxDue.apiValue)
        )

        sut.calculation.map(_.scottishIntermediateRateTax.roundAmount()) mustBe Some(
          Amount
            .gbp(income + pension, s"$income(taxOnPaySIR) + $pension(ctnPensionLsumTaxDueAmt)")
            .roundAmount()
        )
      }
    }

    "scottishHigherRateTaxAmount includes pension tax when pension rate matches higher rate" in {

      forAll { (incomeVal: Double, pensionVal: Double) =>
        val (income: BigDecimal, pension: BigDecimal) = (BigDecimal(incomeVal), BigDecimal(pensionVal))
        val sut                                       = scottishFixture(
          PensionTaxRate(0.41),
          IncomeTaxHigherRate -> Amount.gbp(income, IncomeTaxHigherRate.apiValue),
          PensionLsumTaxDue   -> Amount.gbp(pension, PensionLsumTaxDue.apiValue)
        )

        sut.calculation.map(_.scottishHigherRateTax.roundAmount()) mustBe Some(
          Amount
            .gbp(income + pension, s"$income(ctnIncomeTaxHigherRate) + $pension(ctnPensionLsumTaxDueAmt)")
            .roundAmount()
        )
      }
    }

    "scottishAdditionalRateTaxAmount includes pension tax when pension rate matches additional rate" in {

      forAll { (incomeVal: Double, pensionVal: Double) =>
        val (income: BigDecimal, pension: BigDecimal) = (BigDecimal(incomeVal), BigDecimal(pensionVal))
        val sut                                       = scottishFixture(
          PensionTaxRate(0.46),
          IncomeTaxAddHighRate -> Amount.gbp(income, PensionLsumTaxDue.apiValue),
          PensionLsumTaxDue    -> Amount.gbp(pension, PensionLsumTaxDue.apiValue)
        )

        sut.calculation.map(_.scottishAdditionalRateTax.roundAmount()) mustBe Some(
          Amount
            .gbp(income + pension, s"$income(ctnIncomeTaxAddHighRate) + $pension(ctnPensionLsumTaxDueAmt)")
            .roundAmount()
        )
      }
    }

    "scottishStarterRateIncome include pension lump sum amount when matches starter rate" in {

      forAll { (incomeVal: Double, pensionVal: Double) =>
        val (income: BigDecimal, pension: BigDecimal) = (BigDecimal(incomeVal), BigDecimal(pensionVal))
        val sut                                       = scottishFixture(
          PensionTaxRate(0.19),
          TaxablePayScottishStarterRate -> Amount.gbp(income, TaxablePayScottishStarterRate.apiValue),
          StatePensionGross             -> Amount.gbp(pension, StatePensionGross.apiValue)
        )

        sut.calculation.map(_.scottishStarterRateIncome.roundAmount()) mustBe Some(
          Amount
            .gbp(income + pension, s"$income(taxablePaySSR) + $pension(itfStatePensionLsGrossAmt)")
            .roundAmount()
        )
      }
    }

    "scottishStarterRateIncome include pension lump sum amount when matches basic rate" in {

      forAll { (incomeVal: Double, pensionVal: Double) =>
        val (income: BigDecimal, pension: BigDecimal) = (BigDecimal(incomeVal), BigDecimal(pensionVal))
        val sut                                       = scottishFixture(
          PensionTaxRate(0.20),
          IncomeChargeableBasicRate -> Amount.gbp(income, IncomeChargeableBasicRate.apiValue),
          StatePensionGross         -> Amount.gbp(pension, StatePensionGross.apiValue)
        )

        sut.calculation.map(_.scottishBasicRateIncome.roundAmount()) mustBe Some(
          Amount
            .gbp(income + pension, s"$income(ctnIncomeChgbleBasicRate) + $pension(itfStatePensionLsGrossAmt)")
            .roundAmount()
        )
      }
    }

    "scottishStarterRateIncome include pension lump sum amount when matches intermediate rate" in {

      forAll { (incomeVal: Double, pensionVal: Double) =>
        val (income: BigDecimal, pension: BigDecimal) = (BigDecimal(incomeVal), BigDecimal(pensionVal))
        val sut                                       = scottishFixture(
          PensionTaxRate(0.21),
          TaxablePayScottishIntermediateRate -> Amount.gbp(income, TaxablePayScottishIntermediateRate.apiValue),
          StatePensionGross                  -> Amount.gbp(pension, StatePensionGross.apiValue)
        )

        sut.calculation.map(
          _.scottishIntermediateRateIncome
            .roundAmount()
        ) mustBe Some(
          Amount
            .gbp(income + pension, s"$income(taxablePaySIR) + $pension(itfStatePensionLsGrossAmt)")
            .roundAmount()
        )

      }
    }

    "scottishStarterRateIncome include pension lump sum amount when matches higher rate" in {

      forAll { (incomeVal: Double, pensionVal: Double) =>
        val (income: BigDecimal, pension: BigDecimal) = (BigDecimal(incomeVal), BigDecimal(pensionVal))
        val sut                                       = scottishFixture(
          PensionTaxRate(0.41),
          IncomeChargeableHigherRate -> Amount.gbp(income, IncomeChargeableHigherRate.apiValue),
          StatePensionGross          -> Amount.gbp(pension, StatePensionGross.apiValue)
        )

        sut.calculation.map(_.scottishHigherRateIncome.roundAmount()) mustBe Some(
          Amount
            .gbp(income + pension, s"$income(ctnIncomeChgbleHigherRate) + $pension(itfStatePensionLsGrossAmt)")
            .roundAmount()
        )
      }
    }

    "scottishStarterRateIncome include pension lump sum amount when matches additional rate" in {

      forAll { (incomeVal: Double, pensionVal: Double) =>
        val (income: BigDecimal, pension: BigDecimal) = (BigDecimal(incomeVal), BigDecimal(pensionVal))
        val sut                                       = scottishFixture(
          PensionTaxRate(0.46),
          IncomeChargeableAddHRate -> Amount.gbp(income, IncomeChargeableAddHRate.apiValue),
          StatePensionGross        -> Amount.gbp(pension, StatePensionGross.apiValue)
        )

        sut.calculation.map(_.scottishAdditionalRateIncome.roundAmount()) mustBe Some(
          Amount
            .gbp(income + pension, s"$income(ctnIncomeChgbleAddHRate) + $pension(itfStatePensionLsGrossAmt)")
            .roundAmount()
        )
      }
    }

    "savingsBasicRateTax returns tax on savings" in {

      forAll { (taxVal: Double) =>
        val tax: BigDecimal = BigDecimal(taxVal)
        val sut             = scottishFixture(SavingsTaxLowerRate -> Amount.gbp(tax, SavingsTaxLowerRate.apiValue))
        sut.calculation.map(
          _.savingsBasicRateTax
            .roundAmount()
        ) mustBe Some(Amount.gbp(tax, s"$tax(ctnSavingsTaxLowerRate)").roundAmount())
      }
    }

    "savingsHigherRateTax returns tax on savings" in {

      forAll { (taxVal: Double) =>
        val tax: BigDecimal = BigDecimal(taxVal)
        val sut             = scottishFixture(SavingsTaxHigherRate -> Amount.gbp(tax, SavingsTaxHigherRate.apiValue))
        sut.calculation.map(
          _.savingsHigherRateTax
            .roundAmount()
        ) mustBe Some(Amount.gbp(tax, s"$tax(ctnSavingsTaxHigherRate)").roundAmount())
      }
    }

    "savingsAdditionalRateTax returns tax on savings" in {

      forAll { (taxVal: Double) =>
        val tax: BigDecimal = BigDecimal(taxVal)
        val sut             = scottishFixture(SavingsTaxAddHighRate -> Amount.gbp(tax, SavingsTaxAddHighRate.apiValue))
        sut.calculation.map(
          _.savingsAdditionalRateTax
            .roundAmount()
        ) mustBe Some(Amount.gbp(tax, s"$tax(ctnSavingsTaxAddHighRate)").roundAmount())
      }
    }

    "savingsBasicRateIncome returns income on savings" in {

      forAll { (taxVal: Double) =>
        val tax: BigDecimal = BigDecimal(taxVal)
        val sut             = scottishFixture(SavingsChargeableLowerRate -> Amount.gbp(tax, SavingsChargeableLowerRate.apiValue))
        sut.calculation.map(
          _.savingsBasicRateIncome
            .roundAmount()
        ) mustBe Some(Amount.gbp(tax, s"$tax(ctnSavingsChgbleLowerRate)").roundAmount())
      }
    }

    "savingsHigherRateIncome returns income on savings" in {

      forAll { (taxVal: Double) =>
        val tax: BigDecimal = BigDecimal(taxVal)
        val sut             =
          scottishFixture(SavingsChargeableHigherRate -> Amount.gbp(tax, SavingsChargeableHigherRate.apiValue))
        sut.calculation.map(
          _.savingsHigherRateIncome
            .roundAmount()
        ) mustBe Some(Amount.gbp(tax, s"$tax(ctnSavingsChgbleHigherRate)").roundAmount())
      }
    }

    "savingsAdditionalRateIncome returns income on savings" in {

      forAll { (taxVal: Double) =>
        val tax: BigDecimal = BigDecimal(taxVal)
        val sut             = scottishFixture(SavingsChargeableAddHRate -> Amount.gbp(tax, SavingsChargeableAddHRate.apiValue))
        sut.calculation.map(
          _.savingsAdditionalRateIncome
            .roundAmount()
        ) mustBe Some(Amount.gbp(tax, s"$tax(ctnSavingsChgbleAddHRate)").roundAmount())
      }
    }

    "scottishTotalTax includes any 2 random scottish taxes" in {

      forAll { (firstVal: Double, secondVal: Double) =>
        val (first: BigDecimal, second: BigDecimal) = (BigDecimal(firstVal), BigDecimal(secondVal))

        val keys =
          Random.shuffle(
            List(
              TaxOnPayScottishStarterRate,
              IncomeTaxBasicRate,
              TaxOnPayScottishIntermediateRate,
              IncomeTaxHigherRate,
              IncomeTaxAddHighRate
            )
          )

        val sut = scottishFixture(
          keys.head -> Amount.gbp(first, keys.head.apiValue),
          keys(1)   -> Amount.gbp(second, keys(1).apiValue)
        )

        sut.calculation.map(
          _.scottishTotalTax
            .roundAmount()
            .amount
        ) mustBe Some(Amount.gbp(first + second, "").roundAmount().amount) // TODO: to be fixed
      }
    }

    "totalIncomeTaxAmount includes any 2 random scottish taxes or savings taxes" in {

      forAll { (firstVal: Double, secondVal: Double) =>
        val (first: BigDecimal, second: BigDecimal) = (BigDecimal(firstVal), BigDecimal(secondVal))

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
            )
          )

        val sut = scottishFixture(
          List(
            keys.head -> Amount.gbp(first, keys.head.apiValue),
            keys(1)   -> Amount.gbp(second, keys(1).apiValue)
          )
        )

        sut.calculation.map(
          _.totalIncomeTaxAmount
            .roundAmount()
            .amount
        ) mustBe Some(Amount.gbp(first + second, "").roundAmount().amount)
      }
    }
  }

  "WelshATSCalculations" must {
    "calculate the welshIncomeTax" in {
      val welshFixture = new Fixture(taxYear = 2020, Welsh())

      forAll { (basicRateVal: Double, higherRateVal: Double, additionalRateVal: Double) =>
        val (basicRate: BigDecimal, higherRate: BigDecimal, additionalRate: BigDecimal) =
          (BigDecimal(basicRateVal), BigDecimal(higherRateVal), BigDecimal(additionalRateVal))

        val sut = welshFixture(
          SavingsChargeableStartRate -> Amount.gbp(0.0, SavingsChargeableStartRate.apiValue),
          SavingsTaxStartingRate     -> Amount.gbp(0.0, SavingsTaxStartingRate.apiValue),
          IncomeChargeableBasicRate  -> Amount.gbp(basicRate, IncomeChargeableBasicRate.apiValue),
          IncomeChargeableHigherRate -> Amount.gbp(higherRate, IncomeChargeableHigherRate.apiValue),
          IncomeChargeableAddHRate   -> Amount.gbp(additionalRate, IncomeChargeableAddHRate.apiValue)
        )

        sut.calculation.map(
          _.welshIncomeTax
            .roundAmount()
        ) mustBe Some(
          Amount
            .gbp(
              (basicRate + higherRate + additionalRate) * 0.1,
              s"0.1 * ($basicRate(ctnIncomeChgbleBasicRate) + $higherRate(ctnIncomeChgbleHigherRate) + $additionalRate(ctnIncomeChgbleAddHRate))"
            )
            .roundAmount()
        )
      }
    }
  }
}
