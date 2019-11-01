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

///
//   Copyright 2019 HM Revenue & Customs
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
//  /

package transformers


import models.{Amount, ApiValue, Liability}
import models.Liability._
import transformers.Operation.{sum, difference, taxPerCurrency}

sealed trait Operation[A, +B] {

  def filter(predicate: (A, B) => Boolean): Operation[A, B] = Filter(this, predicate)
  def roundedUp[B1 >: B]: RoundUp[A, B1] = RoundUp(this)
  lazy val positive: Operation[A, B] = Positive(this)

}

case class Term[A, B](value: A) extends Operation[A, B]
case class Sum[A, B](first: Operation[A, B], second: Operation[A, B], others: List[Operation[A, B]] = Nil) extends Operation[A, B]
case class Difference[A, B](first: Operation[A, B], second: Operation[A, B], others: List[Operation[A, B]] = Nil) extends Operation[A, B]
case class Filter[A, B](op: Operation[A, B], predicate: (A, B) => Boolean) extends Operation[A, B]
case class Positive[A, B](op: Operation[A, B]) extends Operation[A, B]
case class RoundUp[A, B](op: Operation[A, B]) extends Operation[A, B]
case class Empty[A, B]() extends Operation[A, B]
case class RateFromAmount[A,B](op: Operation[A,B]) extends Operation[A, B]
case class TaxPerCurrency[A,B](first: Operation[A,B], second: Operation[A,B]) extends Operation[A, B]
case class Multiply[A,B](first: Operation[A,B], second: Double) extends Operation[A, B]


object Operation {

  def sum[A, B](first: A, second: A, others: A* ): Sum[A, B] =
    Sum(Term(first), Term(second), others.map(Term(_)).toList)

  def difference[A, B](first: A, second: A, others: A* ): Difference[A, B] =
    Difference(Term(first), Term(second), others.map(Term(_)).toList)

  def taxPerCurrency[A, B](first: A, second: A): TaxPerCurrency[A,B] =
    TaxPerCurrency(Term(first), Term(second))


}

object Descripters {


  //createTaxableGains
  val taxableGains: Operation[Liability, Nothing] = {
    sum(
      CgTotGainsAfterLosses,
      CgGainsAfterLosses
    )
  }

  //createPayCapitalGainsTaxOn
  val payCgTaxOn: Operation[Liability, Nothing] = {
    Positive(Difference(
      taxableGains,
      Term(CgAnnualExempt)
    ))
  }

  //createTotalCapitalGainsTax
  val totalCapitalGainsTax: Operation[Liability, Nothing] = { //brackets
    Sum(
      Term(CgDueEntrepreneursRate),
      Term(CgDueLowerRate),
      List(
        Difference(
         Term(CgDueHigherRate),
         Term(CapAdjustment)
        ),
        Term(LowerRateCgtRPCI),
        Term(HigherRateCgtRPCI)
      )
    )
  }

  //createSelfEmployment
  val selfEmployment: Operation[Liability, Nothing] = {
    sum(
      SummaryTotalSchedule,
      SummaryTotalPartnership
    )
  }

  //createOtherPension
  val otherPension: Operation[Liability, Nothing] = {
    sum(
      OtherPension,
      StatePensionGross
    )
  }

  //createTaxableStateBenefits
  val taxableStateBenefits: Operation[Liability, Nothing] = {
    sum(
      IncBenefitSuppAllow,
      JobSeekersAllowance,
      OthStatePenBenefits
    )
  }

  //createOtherIncome
  val otherIncome: Operation[Liability, Nothing] = {
    sum(
      SummaryTotShareOptions,
      SummaryTotalUklProperty,
      SummaryTotForeignIncome,
      SummaryTotTrustEstates,
      SummaryTotalOtherIncome,
      SummaryTotalUkInterest,
      SummaryTotForeignDiv,
      SummaryTotalUkIntDivs,
      SumTotLifePolicyGains
    )
  }

  //createTotalIncomeBeforeTax
  val totalIncomeBeforeTax: Operation[Liability, Nothing] = {
    Sum(
      selfEmployment,
      Term(SummaryTotalEmployment),
      List(Term(StatePension),
        otherPension,
        taxableStateBenefits,
        otherIncome,
        Term(EmploymentBenefits)
      )
    )
  }

  //createOtherAllowancesAmount
  val otherAllowances: Operation[Liability, Nothing] = {
    sum[Liability, Nothing](
      EmploymentExpenses,
      SummaryTotalDedPpr,
      SumTotForeignTaxRelief,
      SumTotLoanRestricted,
      SumTotLossRestricted,
      AnnuityPay,
      GiftsInvCharities,
      TradeUnionDeathBenefits,
      BpaAllowance,
      BPA,
      ExcludedIncome
    ).roundedUp
  }

  //createTotalTaxFreeAmount
  val totalTaxFreeAmount: Operation[Liability, Nothing] = { //brackets
    Sum(
      otherAllowances,
      difference(
        PersonalAllowance,
        MarriageAllceOut
      )
    )
  }


  //createTotalAmountEmployeeNic =
  val totalAmountEmployeeNic: Operation[Liability, Nothing] = {
    sum(
      EmployeeClass1NI,
      EmployeeClass2NI,
      Class4Nic
    )
  }

  //basicRateIncomeTaxAmount
  val basicRateIncomeTaxAmount: Operation[Liability, BigDecimal] = { //requires predicates TODO
    sum[Liability,BigDecimal](
      IncomeTaxBasicRate,
      SavingsTaxLowerRate,
      PensionLsumTaxDue
    ).filter {
        case (PensionLsumTaxDue, pensionLumpSumRate) if pensionLumpSumRate == 1 => true
        case _ => false
      }
  }

  //createHigherRateIncomeTaxAmount
  val higherRateIncomeTaxAmount: Operation[Liability, BigDecimal] = { //requires predicates TODO
    sum[Liability,BigDecimal](
      IncomeTaxHigherRate,
      SavingsTaxHigherRate,
      PensionLsumTaxDue
    ).filter {
      case (PensionLsumTaxDue, pensionLumpSumRate) if pensionLumpSumRate == 2 => true
      case _ => false
    }
  }

  //createAdditionalRateIncomeTaxAmount
  val additionalRateIncomeTaxAmount: Operation[Liability, BigDecimal] = { //requires predicates TODO
    sum[Liability,BigDecimal](
      IncomeTaxAddHighRate,
      SavingsTaxAddHighRate,
      PensionLsumTaxDue
    ).filter{
      case (PensionLsumTaxDue, pensionLumpSumRate) if pensionLumpSumRate == 3 => true
      case _ => false
    }
  }

  //createOtherAdjustmentsIncreasing
  val otherAdjustmentsIncreasing: Operation[Liability, Nothing] = { //brackets & cannot use helpers.
    Difference(
      Sum(
        Term(NonDomCharge),
        Term(TaxExcluded),
        List(
          Term(IncomeTaxDue),
          Term(NetAnnuityPaytsTaxDue),
          Term(ChildBenefitCharge),
          Term(PensionSavingChargeable)
        )
      ),
      Term(TaxDueAfterAllceRlf)
    )
  }

  //createOtherAdjustmentsReducing
  val otherAdjustmentsReducing: Operation[Liability, Nothing] = {
    sum[Liability, Nothing](
      DeficiencyRelief,
      TopSlicingRelief,
      VctSharesRelief,
      EisRelief,
      SeedEisRelief,
      CommInvTrustRel,
      SurplusMcaAlimonyRel,
      NotionalTaxCegs,
      NotlTaxOtherSource,
      TaxCreditsForDivs,
      QualDistnRelief,
      TotalTaxCreditRelief,
      NonPayableTaxCredits,
      ReliefForFinanceCosts
    ).roundedUp

  }

  // createTotalIncomeTaxAmount
  val totalIncomeTaxAmount: Operation[Liability, BigDecimal] = {
    Difference(
      Sum(
        Term(SavingsTaxStartingRate),
        basicRateIncomeTaxAmount,
        List(higherRateIncomeTaxAmount,
          Term(DividendTaxLowRate),
          Term(DividendTaxHighRate),
          Term(DividendTaxAddHighRate),
          otherAdjustmentsIncreasing
        )
      ),
      otherAdjustmentsReducing,
      List(Term(MarriageAllceIn))
    )
  }

  //createTotalAmountTaxAndNics
  val totalAmountTaxAndNics: Operation[Liability, BigDecimal] = {
    Sum(
      totalAmountEmployeeNic,
      totalIncomeTaxAmount
    )
  }

  //createYourTotalTax
  val totalTax: Operation[Liability, BigDecimal] = {
    Sum(
      totalAmountTaxAndNics,
      totalCapitalGainsTax
    )
  }

  //createBasicRateIncomeTax
  val basicIncomeRateIncomeTax: Operation[Liability, Nothing] = {
    sum(
      IncomeChargeableBasicRate,
      SavingsChargeableLowerRate
    )
  }

  //createHigherRateIncomeTax
  val higherRateIncomeTax: Operation[Liability, Nothing] = {
    sum(
      IncomeChargeableHigherRate,
      SavingsChargeableHigherRate
    )
  }

  //createAdditionalRateIncomeTax
  val additionalRateIncomeTax: Operation[Liability, Nothing] = {
    sum(
      IncomeChargeableAddHRate,
      SavingsChargeableAddHRate
    )
  }

  //createScottishIncomeTax
  val scottishIncomeTax: Operation[Liability, Nothing] = {
    Multiply(
    sum(
      IncomeChargeableBasicRate,
      IncomeChargeableHigherRate,
      IncomeChargeableAddHRate
    ),
      0.1
    )
  } //TODO needs to be multiplied by 0.1

  //createCgTaxPerCurrencyUnit
  val capitalGainsTaxPerCurrency: Operation[Liability, Nothing] = {
    TaxPerCurrency(totalCapitalGainsTax,taxableGains)
  }

  //createNicsAndTaxPerCurrencyUnit
  val nicsAndTaxPerCurrency: Operation[Liability, BigDecimal] = {
    TaxPerCurrency(totalAmountTaxAndNics, totalIncomeBeforeTax)
  }

 // case class PensionLumpSumRate(value: Int)

}