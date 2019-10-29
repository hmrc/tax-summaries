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

import com.sun.xml.internal.bind.v2.TODO
import models.{Amount, ApiValue, Liability}
import models.Liability._
import transformers.Operation.sum

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



object Operation {

  def sum[A, B](first: A, second: A, others: A* ): Sum[A, B] =
    Sum(Term(first), Term(second), others.map(Term(_)).toList)

  def difference[A, B](first: A, second: A, others: A* ): Operation[A, B] =
    Difference(Term(first), Term(second), others.map(Term(_)).toList)

}

object Descripters {


  val taxableGains: Operation[Liability, Nothing] = {
    sum(
      CgTotGainsAfterLosses,
      CgGainsAfterLosses
    )
  }

  val payCgTaxOn: Operation[Liability, Nothing] = {
    Positive(Difference(
      taxableGains,
      Term(CgAnnualExempt)
    ))
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
  /*  percentageOf(
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



  val totalAmountEmployeeNic : Operation[Liability, Nothing] = {
    sum(
      EmployeeClass1NI,
      EmployeeClass2NI
    )
  }


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
  case class PensionLumpSumRate(value: Int)

  val totalTaxFreeAmount : Operation[Liability, PensionLumpSumRate] ={
    Sum(
      otherAllowances,
      Term(PersonalAllowance),
      List(Term(MarriageAllceOut))
    )
  }

}
