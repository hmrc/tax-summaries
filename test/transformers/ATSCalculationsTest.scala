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

package transformers

import config.ApplicationConfig
import models.ODSLiabilities.ODSLiabilities
import models.ODSLiabilities.ODSLiabilities._
import models._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import services.TaxRateService
import transformers.Scottish.ATSCalculationsScottish2019
import transformers.UK.{ATSCalculationsUK2019, ATSCalculationsUK2021}
import transformers.Welsh.{ATSCalculationsWelsh2020, ATSCalculationsWelsh2021, ATSCalculationsWelsh2022}
import utils.{BaseSpec, DoubleUtils}

import scala.util.Random

class ATSCalculationsTest extends BaseSpec with ScalaCheckPropertyChecks with DoubleUtils {

  class CalcFixtures(val taxYear: Int, val origin: Nationality, applicationConfig: ApplicationConfig)(
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

    lazy val calculation: ATSCalculations = ATSCalculations.make(taxSummaryLiability, taxRateService)
  }

  class Fixture(val taxYear: Int, origin: Nationality, applicationConfig: ApplicationConfig = applicationConfig) {

    def apply(): CalcFixtures =
      new CalcFixtures(taxYear, origin, applicationConfig)(PensionTaxRate(0))

    def apply(newPensionTaxRate: PensionTaxRate, newAtsData: (ODSLiabilities, Amount)*): CalcFixtures =
      new CalcFixtures(taxYear, origin, applicationConfig)(newPensionTaxRate, newAtsData: _*)

    def apply(newAtsData: (ODSLiabilities, Amount)*): CalcFixtures =
      new CalcFixtures(taxYear, origin, applicationConfig)(PensionTaxRate(0), newAtsData: _*)

    def apply(newAtsData: List[(ODSLiabilities, Amount)]): CalcFixtures =
      new CalcFixtures(taxYear, origin, applicationConfig)(PensionTaxRate(0), newAtsData: _*)
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
    CapAdjustment           -> Amount.empty(CapAdjustment.apiValue)
  )

  "make" must {

    "return Post2018ScottishATSCalculations" when {

      "tax years is > 2018 and type is scottish" in {

        val calculation = new Fixture(2019, Scottish())().calculation
        calculation mustBe a[ATSCalculationsScottish2019]
      }
    }

    "return Post2018rUKATSCalculations" when {

      "tax year is > 2018" in {
        val calculation = new Fixture(2019, UK())().calculation
        calculation mustBe a[ATSCalculationsUK2019]
      }

      "return WelshATSCalculations" when {
        "tax year is > 2019" in {
          forAll { (taxYear: Int) =>
            val calculation = new Fixture(taxYear, Welsh())().calculation

            if (taxYear == 2020) {
              calculation mustBe a[ATSCalculationsWelsh2020]
            } else if (taxYear == 2021) {
              calculation mustBe a[ATSCalculationsWelsh2021]
            } else if (taxYear > 2021) {
              calculation mustBe a[ATSCalculationsWelsh2022]
            } else {
              calculation mustBe a[DefaultATSCalculations]
            }

          }
        }
      }
    }

    "return DefaultATSCalculations" when {

      "tax year is < 2019 and type is scottish" in {

        val calculation = new Fixture(2018, Scottish())().calculation
        calculation mustBe a[DefaultATSCalculations]
      }

      "tax year is < 2019" in {

        val calculation = new Fixture(2018, UK())().calculation
        calculation mustBe a[DefaultATSCalculations]
      }
    }

    "return ATSCalculationsUK2020" when {
      "tax year is 2020 and type is UK" in {

        val calculation = new Fixture(2021, UK())().calculation
        calculation mustBe a[ATSCalculationsUK2021]
      }
    }
  }

  "DefaultATSCalculations" must {

    val fixture = new Fixture(2016, UK())

    "basicIncomeRateIncomeTax includes pension tax when pension rate matches basic rate" in {

      forAll { (incomeVal: Double, pensionVal: Double, savingsVal: Double) =>
        val (income: BigDecimal, pension: BigDecimal, savings: BigDecimal) =
          (BigDecimal(incomeVal), BigDecimal(pensionVal), BigDecimal(savingsVal))

        val sut = fixture(
          PensionTaxRate(0.20),
          IncomeChargeableBasicRate  -> Amount.gbp(income, IncomeChargeableBasicRate.apiValue),
          StatePensionGross          -> Amount.gbp(pension, StatePensionGross.apiValue),
          SavingsChargeableLowerRate -> Amount.gbp(savings, SavingsChargeableLowerRate.apiValue)
        )

        sut.calculation.basicRateIncomeTax.roundAmount() mustBe Amount
          .gbp(
            income + pension + savings,
            s"$income(ctnIncomeChgbleBasicRate) + $savings(ctnSavingsChgbleLowerRate) + $pension(itfStatePensionLsGrossAmt)"
          )
          .roundAmount()
      }
    }

    "higherRateIncomeTaxAmount includes pension tax when pension rate matches basic rate" in {

      forAll { (incomeVal: Double, pensionVal: Double, savingsVal: Double) =>
        val (income: BigDecimal, pension: BigDecimal, savings: BigDecimal) =
          (BigDecimal(incomeVal), BigDecimal(pensionVal), BigDecimal(savingsVal))

        val sut = fixture(
          PensionTaxRate(0.40),
          IncomeChargeableHigherRate  -> Amount.gbp(income, IncomeChargeableHigherRate.apiValue),
          StatePensionGross           -> Amount.gbp(pension, StatePensionGross.apiValue),
          SavingsChargeableHigherRate -> Amount.gbp(savings, SavingsChargeableHigherRate.apiValue)
        )

        sut.calculation.higherRateIncomeTax
          .roundAmount() mustBe Amount
          .gbp(
            income + pension + savings,
            s"$income(ctnIncomeChgbleHigherRate) + $savings(ctnSavingsChgbleHigherRate) + $pension(itfStatePensionLsGrossAmt)"
          )
          .roundAmount()
      }
    }

    "additionalRateIncomeTaxAmount includes pension tax when pension rate matches basic rate" in {

      forAll { (incomeVal: Double, pensionVal: Double, savingsVal: Double) =>
        val (income: BigDecimal, pension: BigDecimal, savings: BigDecimal) =
          (BigDecimal(incomeVal), BigDecimal(pensionVal), BigDecimal(savingsVal))

        val sut = fixture(
          PensionTaxRate(0.45),
          IncomeChargeableAddHRate  -> Amount.gbp(income, IncomeChargeableAddHRate.apiValue),
          StatePensionGross         -> Amount.gbp(pension, StatePensionGross.apiValue),
          SavingsChargeableAddHRate -> Amount.gbp(savings, SavingsChargeableAddHRate.apiValue)
        )

        sut.calculation.additionalRateIncomeTax
          .roundAmount() mustBe Amount
          .gbp(
            income + pension + savings,
            s"$income(ctnIncomeChgbleAddHRate) + $savings(ctnSavingsChgbleAddHRate) + $pension(itfStatePensionLsGrossAmt)"
          )
          .roundAmount()
      }
    }

    "includePensionIncomeForRate returns StatePensionGross when percentages match" in {

      forAll { (rateI: Int, totalValI: Int) =>
        val rate = Math.abs(rateI).toDouble / 100.0
        val totalVal = Math.abs(totalValI).toDouble / 100.0
        val total: BigDecimal = BigDecimal(totalVal)
        val sum = List.fill(10)(rate).fold(0.0)(_ + _)
        val prod = rate * 10
        val sut = fixture(PensionTaxRate(sum / 100), StatePensionGross -> Amount.gbp(total, StatePensionGross.apiValue))
        sut.calculation.includePensionTaxForRate(Rate(prod)).roundAmount() mustBe Amount
          .gbp(total, s"$total(itfStatePensionLsGrossAmt)")
          .roundAmount()
      }
    }

    "includePensionIncomeForRate returns 0 when rates don't match" in {

      forAll { (rate1: Double, rate2: Double) =>
        whenever(rate1 !== rate2) {
          val sut = fixture(PensionTaxRate(rate1 / 100))
          sut.calculation
            .includePensionIncomeForRate(Rate(rate2)) mustBe Amount.empty("itfStatePensionLsGrossAmt")
        }
      }
    }

    "includePensionTaxForRate returns PensionLsumTaxDue when percentages match" in {
      forAll { (rateI: Int, totalValI: Int) =>
        val rate              = Math.abs(rateI).toDouble / 100.0
        val totalVal          = Math.abs(totalValI).toDouble / 100.0
        val total: BigDecimal = BigDecimal(totalVal)
        val sum               = List.fill(10)(rate).fold(0.0)(_ + _)
        val prod              = rate * 10
        val sut               = fixture(PensionTaxRate(sum / 100), PensionLsumTaxDue -> Amount.gbp(total, PensionLsumTaxDue.apiValue))
        sut.calculation.includePensionTaxForRate(Rate(prod)).roundAmount() mustBe Amount
          .gbp(total, s"$total(ctnPensionLsumTaxDueAmt)")
          .roundAmount()
      }
    }

    "includePensionTaxForRate returns 0 when rates don't match" in {

      forAll { (rate1: Double, rate2: Double) =>
        whenever(rate1 !== rate2) {
          val sut = fixture(PensionTaxRate(rate1 / 100))
          sut.calculation.includePensionTaxForRate(Rate(rate2)) mustBe Amount.empty("ctnPensionLsumTaxDueAmt")
        }
      }
    }

    "totalCapitalGainsTax returns correct calculation" in {

      forAll { (lowerVal: Double, higherVal: Double) =>
        val (lower: BigDecimal, higher: BigDecimal) = (BigDecimal(lowerVal), BigDecimal(higherVal))
        val sut                                     = fixture(
          LowerRateCgtRPCI  -> Amount.gbp(lower, LowerRateCgtRPCI.apiValue),
          HigherRateCgtRPCI -> Amount.gbp(higher, HigherRateCgtRPCI.apiValue)
        )

        sut.calculation.totalCapitalGainsTax.roundAmount() mustBe Amount
          .gbp(
            (lower + higher).max(0),
            s"max(0, Some($lower(ctnLowerRateCgtRPCI) + $higher(ctnHigherRateCgtRPCI) + 0(ctnCgDueEntrepreneursRate) + 0(ctnCgDueLowerRate) + 0(ctnCgDueHigherRate) + 0(capAdjustmentAmt)))"
          )
          .roundAmount()
      }
    }

    "return empty for welshIncomeTax" in {
      fixture().calculation.welshIncomeTax mustBe Amount.empty("welshIncomeTax")
    }
  }

  "Post2018rUKATSCalculations" must {

    val fixture     = new Fixture(2019, UK())()
    val calculation = fixture.calculation

    "return an empty amount for scottishIncomeTax" in {

      calculation.scottishIncomeTax mustBe Amount.empty("scottishIncomeTaxUK2019")
    }

    "return empty for savingsRate" in {

      calculation.savingsRate mustBe Amount.empty("savingsRateUK2019")
    }

    "return empty for savingsRateAmount" in {

      calculation.savingsRateAmount mustBe Amount.empty("savingsRateAmountUK2019")
    }

    "return empty for welshIncomeTax" in {
      calculation.welshIncomeTax mustBe Amount.empty("welshIncomeTax")
    }
  }

  "Post2018ScottishATSCalculations" must {

    val scottishFixture = new Fixture(taxYear = 2019, Scottish())
    val calculation     = scottishFixture().calculation

    "return an empty amount for scottishIncomeTax" in {
      calculation.scottishIncomeTax mustBe Amount.empty("scottishIncomeTaxScottish1019")
    }

    "return an empty amount for basicRateIncomeTaxAmount" in {
      calculation.basicRateIncomeTaxAmount mustBe Amount.empty("basicRateIncomeTaxAmountScottish2019")
    }

    "return an empty amount for higherRateIncomeTaxAmount" in {
      calculation.higherRateIncomeTaxAmount mustBe Amount.empty("higherRateIncomeTaxAmountScottish2019")
    }

    "return an empty amount for additionalRateIncomeTaxAmount" in {
      calculation.additionalRateIncomeTaxAmount mustBe Amount.empty("additionalRateIncomeTaxAmountScottish2019")
    }

    "return an empty amount for basicRateIncomeTax" in {
      calculation.basicRateIncomeTax mustBe Amount.empty("basicRateIncomeTaxScottish2019")
    }

    "return an empty amount for higherRateIncomeTax" in {
      calculation.higherRateIncomeTax mustBe Amount.empty("higherRateIncomeTaxScottish2019")
    }

    "return an empty amount for additionalRateIncomeTax" in {
      calculation.additionalRateIncomeTax mustBe Amount.empty("additionalRateIncomeTaxScottish2019")
    }

    "return empty for savingsRate" in {

      calculation.savingsRate mustBe Amount.empty("savingsRateScottish2019")
    }

    "return empty for savingsRateAmount" in {

      calculation.savingsRateAmount mustBe Amount.empty("savingsRateAmountScottish2019")
    }

    "return empty for welshIncomeTax" in {
      calculation.welshIncomeTax mustBe Amount.empty("welshIncomeTax")
    }

    "scottishStarterRateTaxAmount includes pension tax when pension rate matches starter rate" in {

      forAll { (incomeVal: Double, pensionVal: Double) =>
        val (income: BigDecimal, pension: BigDecimal) = (BigDecimal(incomeVal), BigDecimal(pensionVal))
        val sut                                       = scottishFixture(
          PensionTaxRate(0.19),
          TaxOnPayScottishStarterRate -> Amount.gbp(income, TaxOnPayScottishStarterRate.apiValue),
          PensionLsumTaxDue           -> Amount.gbp(pension, PensionLsumTaxDue.apiValue)
        )

        sut.calculation.scottishStarterRateTax mustBe Amount.gbp(
          income + pension,
          s"$income(taxOnPaySSR) + $pension(ctnPensionLsumTaxDueAmt)"
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

        sut.calculation.scottishBasicRateTax.roundAmount() mustBe Amount
          .gbp(income + pension, s"$income(ctnIncomeTaxBasicRate) + $pension(ctnPensionLsumTaxDueAmt)")
          .roundAmount()
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

        sut.calculation.scottishIntermediateRateTax.roundAmount() mustBe Amount
          .gbp(income + pension, s"$income(taxOnPaySIR) + $pension(ctnPensionLsumTaxDueAmt)")
          .roundAmount()
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

        sut.calculation.scottishHigherRateTax.roundAmount() mustBe Amount
          .gbp(income + pension, s"$income(ctnIncomeTaxHigherRate) + $pension(ctnPensionLsumTaxDueAmt)")
          .roundAmount()
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

        sut.calculation.scottishAdditionalRateTax.roundAmount() mustBe Amount
          .gbp(income + pension, s"$income(ctnIncomeTaxAddHighRate) + $pension(ctnPensionLsumTaxDueAmt)")
          .roundAmount()
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

        sut.calculation.scottishStarterRateIncome.roundAmount() mustBe Amount
          .gbp(income + pension, s"$income(taxablePaySSR) + $pension(itfStatePensionLsGrossAmt)")
          .roundAmount()
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

        sut.calculation.scottishBasicRateIncome.roundAmount() mustBe Amount
          .gbp(income + pension, s"$income(ctnIncomeChgbleBasicRate) + $pension(itfStatePensionLsGrossAmt)")
          .roundAmount()
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

        sut.calculation.scottishIntermediateRateIncome
          .roundAmount() mustBe Amount
          .gbp(income + pension, s"$income(taxablePaySIR) + $pension(itfStatePensionLsGrossAmt)")
          .roundAmount()
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

        sut.calculation.scottishHigherRateIncome.roundAmount() mustBe Amount
          .gbp(income + pension, s"$income(ctnIncomeChgbleHigherRate) + $pension(itfStatePensionLsGrossAmt)")
          .roundAmount()
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

        sut.calculation.scottishAdditionalRateIncome.roundAmount() mustBe Amount
          .gbp(income + pension, s"$income(ctnIncomeChgbleAddHRate) + $pension(itfStatePensionLsGrossAmt)")
          .roundAmount()
      }
    }

    "savingsBasicRateTax returns tax on savings" in {

      forAll { taxVal: Double =>
        val tax: BigDecimal = BigDecimal(taxVal)
        val sut             = scottishFixture(SavingsTaxLowerRate -> Amount.gbp(tax, SavingsTaxLowerRate.apiValue))
        sut.calculation.savingsBasicRateTax
          .roundAmount() mustBe Amount.gbp(tax, s"$tax(ctnSavingsTaxLowerRate)").roundAmount()
      }
    }

    "savingsHigherRateTax returns tax on savings" in {

      forAll { taxVal: Double =>
        val tax: BigDecimal = BigDecimal(taxVal)
        val sut             = scottishFixture(SavingsTaxHigherRate -> Amount.gbp(tax, SavingsTaxHigherRate.apiValue))
        sut.calculation.savingsHigherRateTax
          .roundAmount() mustBe Amount.gbp(tax, s"$tax(ctnSavingsTaxHigherRate)").roundAmount()
      }
    }

    "savingsAdditionalRateTax returns tax on savings" in {

      forAll { taxVal: Double =>
        val tax: BigDecimal = BigDecimal(taxVal)
        val sut             = scottishFixture(SavingsTaxAddHighRate -> Amount.gbp(tax, SavingsTaxAddHighRate.apiValue))
        sut.calculation.savingsAdditionalRateTax
          .roundAmount() mustBe Amount.gbp(tax, s"$tax(ctnSavingsTaxAddHighRate)").roundAmount()
      }
    }

    "savingsBasicRateIncome returns income on savings" in {

      forAll { taxVal: Double =>
        val tax: BigDecimal = BigDecimal(taxVal)
        val sut             = scottishFixture(SavingsChargeableLowerRate -> Amount.gbp(tax, SavingsChargeableLowerRate.apiValue))
        sut.calculation.savingsBasicRateIncome
          .roundAmount() mustBe Amount.gbp(tax, s"$tax(ctnSavingsChgbleLowerRate)").roundAmount()
      }
    }

    "savingsHigherRateIncome returns income on savings" in {

      forAll { taxVal: Double =>
        val tax: BigDecimal = BigDecimal(taxVal)
        val sut             = scottishFixture(SavingsChargeableHigherRate -> Amount.gbp(tax, SavingsChargeableHigherRate.apiValue))
        sut.calculation.savingsHigherRateIncome
          .roundAmount() mustBe Amount.gbp(tax, s"$tax(ctnSavingsChgbleHigherRate)").roundAmount()
      }
    }

    "savingsAdditionalRateIncome returns income on savings" in {

      forAll { taxVal: Double =>
        val tax: BigDecimal = BigDecimal(taxVal)
        val sut             = scottishFixture(SavingsChargeableAddHRate -> Amount.gbp(tax, SavingsChargeableAddHRate.apiValue))
        sut.calculation.savingsAdditionalRateIncome
          .roundAmount() mustBe Amount.gbp(tax, s"$tax(ctnSavingsChgbleAddHRate)").roundAmount()
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

        sut.calculation.scottishTotalTax
          .roundAmount()
          .amount mustBe Amount.gbp(first + second, "").roundAmount().amount //TODO: to be fixed
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

        sut.calculation.totalIncomeTaxAmount
          .roundAmount()
          .amount mustBe Amount.gbp(first + second, "").roundAmount().amount
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

        sut.calculation.welshIncomeTax
          .roundAmount() mustBe Amount
          .gbp(
            (basicRate + higherRate + additionalRate) * 0.1,
            s"0.1 * ($basicRate(ctnIncomeChgbleBasicRate) + $higherRate(ctnIncomeChgbleHigherRate) + $additionalRate(ctnIncomeChgbleAddHRate))"
          )
          .roundAmount()
      //sut.calculation.savingsRate mustBe Amount.gbp(0.0, "") //TODO: to be fixed
      //sut.calculation.savingsRateAmount mustBe Amount.gbp(0.0, SavingsTaxStartingRate.apiValue) //TODO: to be fixed
      //sut.calculation.scottishIncomeTax mustBe Amount.gbp(0.0, "to be fixed") //TODO: to be fixed
      }
    }
  }
}
