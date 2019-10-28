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

import com.sun.xml.internal.bind.v2.TODO
import models.{ApiValue, Liability}
import models.Liability._
import transformers.Operation.{PositiveDifference, sum}

//sealed trait Operation[A]
//case class Term[A](value: A) extends Operation[A]
//case class Sum[A](first: Operation[A], second: Operation[A], others: Operation[A]*) extends Operation[A]
//case class Difference[A](first: Operation[A], second: Operation[A], others: Operation[A]*) extends Operation[A]


sealed trait Operation[A, +B]
case class Term[A](value: A) extends Operation[A, Nothing]
case class Sum[A](first: Operation[A, Nothing], second: Operation[A, Nothing], others: List[Operation[A, Nothing]]) extends Operation[A, Nothing] {

  def filter[B](predicate: (A, B) => Boolean): Operation[A, B] = Filter(this, predicate)
}
case class Filter[A, B](op: Operation[A, B], predicate: (A, B) => Boolean) extends Operation[A, B]
case class Difference[A](first: Operation[A, Nothing], second: Operation[A, Nothing], others: List[Operation[A, Nothing]]) extends Operation[A, Nothing]
case class Empty[A]() extends Operation[A, Nothing]



object Operation {

  def sum[A](first: A, second: A, others: A*): Sum[A] =
    Sum(Term(first), Term(second), others.map(Term(_)).toList)

  case class Difference[A](first: Operation[A, Nothing], second: Operation[A, Nothing], others: List[Operation[A, Nothing]]) extends Operation[A, Nothing] {

    def positive: PositiveDifference[A] = PositiveDifference(first, second, others)
  }
  case class PositiveDifference[A](first: Operation[A, Nothing], second: Operation[A, Nothing], others: List[Operation[A, Nothing]]=Nil) extends Operation[A, Nothing]




}

object Descripters {


  val taxableGains = {
    sum(
      CgTotGainsAfterLosses,
      CgGainsAfterLosses
    )
  }

  val payCgTaxOn: Operation[Liability, Nothing] = {
    PositiveDifference(
      taxableGains,
      Term(CgAnnualExempt)
    )
  }

  val totalCapitalGainsTax: Operation[Liability, Nothing] = {
    sum(
      CgDueEntrepreneursRate,
      CgDueLowerRate,
      CgDueHigherRate,
      CapAdjustment,
      LowerRateCgtRPCI,
      HigherRateCgtRPCI
    )
  }

  val captialGainsAsPercentage: Operation[Liability, Nothing] = ???
  /* percentageOf(
  totalCapitalGainsTax
  taxableGains
  */

  val selfEmploymentIncome: Operation[Liability, Nothing] ={
    sum(
      SummaryTotalSchedule,
      SummaryTotalPartnership
    )
  }

  val otherPension: Operation[Liability, Nothing]={
    sum(
      OtherPension,
      StatePensionGross
    )
  }

  val taxableStateBenefits: Operation[Liability, Nothing]={
    sum(
      IncBenefitSuppAllow,
      JobSeekersAllowance,
      OthStatePenBenefits
    )
  }

  val otherIncome: Operation[Liability, Nothing]={
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

  val selfEmployment:  Operation[Liability, Nothing]={
    sum(
      SummaryTotalSchedule,
      SummaryTotalPartnership
    )
  }

  val totalIncomeBeforeTax:  Operation[Liability, Nothing] = {
    Sum(
      selfEmploymentIncome,
      Term(SummaryTotalEmployment),
      List(Term(StatePension),
      otherPension,
      taxableStateBenefits,
      otherIncome,
      Term(EmploymentBenefits)
      )
    )
  }


val otherAllowances: Operation[Liability, Nothing] = {
  sum(
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
  ) //TODO needs a round up
}

  val totalTaxFreeAmount : Operation[Liability, Nothing] ={
    Sum(
      otherAllowances,
      Term(PersonalAllowance),
      List(Term(MarriageAllceOut))
    )
  }
//
//  val totalAmountEmployeeNic : Operation[Liability, Nothing] = {
//    sum(
//      EmployeeClass1NI,
//      EmployeeClass2NI
//    )
//  }


  val otherAdjustments: Operation[Liability, Nothing] =
    sum(
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
      NonPayableTaxCredits
    )

  val basicIncomeRateIncomeTax:Operation[Liability, Nothing] = {
    sum(
      IncomeChargeableBasicRate,
      SavingsChargeableLowerRate
    )
  }

  val basicRateIncomeTaxAmount:Operation[Liability, Nothing] = {
    sum(
      IncomeTaxBasicRate,
      SavingsTaxLowerRate,
      PensionLsumTaxDue
    )
  }

}