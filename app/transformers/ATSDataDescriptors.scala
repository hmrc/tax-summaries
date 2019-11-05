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


import java.text.NumberFormat
import java.util.Locale

import models._
import models.Liability._
import transformers.Operation.{difference, sum}

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

case class RateFromAmount[A, B](op: Operation[A, B]) extends Operation[A, B]

case class TaxPerCurrency[A, B](first: Operation[A, B], second: Operation[A, B]) extends Operation[A, B]

case class Multiply[A, B](first: Operation[A, B], second: Double) extends Operation[A, B]


object Operation {

  def sum[A, B](first: A, second: A, others: A*): Sum[A, B] =
    Sum(Term(first), Term(second), others.map(Term(_)).toList)

  def difference[A, B](first: A, second: A, others: A*): Difference[A, B] =
    Difference(Term(first), Term(second), others.map(Term(_)).toList)


}

case class Descriptors(summaryData: TaxSummaryLiability) {

  val formatter = NumberFormat.getNumberInstance(Locale.UK)

  //  //createTaxableGains
  //  def taxableGains: Amount = {
  //
  //      CgTotGainsAfterLosses +
  //      CgGainsAfterLosses
  ////
  ////    sum(
  ////      CgTotGainsAfterLosses +
  ////      CgGainsAfterLosses
  ////    )
  //  }
  //
  //  //createPayCapitalGainsTaxOn // TODO
  //  def payCapitalGainsTaxOn: Amount = {
  //    Positive(Difference(
  //      taxableGains,
  //      Term(CgAnnualExempt)
  //    ))
  //  }
  //
  //  //createTotalCapitalGainsTax
  //  def totalCapitalGainsTax: Amount = { //brackets
  //
  //          CgDueEntrepreneursRate +
  //          CgDueLowerRate +
  //          CgDueHigherRate -
  //          CapAdjustment +
  //          LowerRateCgtRPCI +
  //          HigherRateCgtRPCI
  //
  //
  //
  ////   Sum(
  ////    Difference(
  ////     Sum(
  ////      Term(CgDueEntrepreneursRate),
  ////      Term(CgDueLowerRate),
  ////      List(
  ////        Term(CgDueHigherRate)
  ////      )
  ////     ),
  ////      Term(CapAdjustment)
  ////    ),
  ////      Term(LowerRateCgtRPCI),
  ////      List(Term(HigherRateCgtRPCI)
  ////    )
  ////   )
  //
  //  }
  //
  //  //createSelfEmployment
  //  def selfEmployment: Amount= {
  //   /* sum(
  //      SummaryTotalSchedule,
  //      SummaryTotalPartnership
  //    )*/
  //
  //
  //      SummaryTotalSchedule +
  //      SummaryTotalPartnership
  //
  //  }
  //
  //  //createOtherPension
  //  def otherPension: Amount = {
  ////    sum(
  ////      OtherPension,
  ////      StatePensionGross
  ////    )
  //    OtherPension +
  //    StatePensionGross
  //
  //  }
  //
  //  //createTaxableStateBenefits
  //  def taxableStateBenefits: Amount = {
  // /*   sum(
  //      IncBenefitSuppAllow,
  //      JobSeekersAllowance,
  //      OthStatePenBenefits
  //    )*/
  //
  //
  //      IncBenefitSuppAllow +
  //      JobSeekersAllowance +
  //      OthStatePenBenefits
  //
  //  }
  //
  //  //createOtherIncome
  //  def otherIncome: Amount = {
  ////    sum(
  ////      SummaryTotShareOptions,
  ////      SummaryTotalUklProperty,
  ////      SummaryTotForeignIncome,
  ////      SummaryTotTrustEstates,
  ////      SummaryTotalOtherIncome,
  ////      SummaryTotalUkInterest,
  ////      SummaryTotForeignDiv,
  ////      SummaryTotalUkIntDivs,
  ////      SumTotLifePolicyGains
  ////    )
  //
  //      SummaryTotShareOptions +
  //      SummaryTotalUklProperty +
  //      SummaryTotForeignIncome +
  //      SummaryTotTrustEstates +
  //      SummaryTotalOtherIncome +
  //      SummaryTotalUkInterest +
  //      SummaryTotForeignDiv +
  //      SummaryTotalUkIntDivs +
  //      SumTotLifePolicyGains
  //
  //  }
  //
  //  //createTotalIncomeBeforeTax
  //  def totalIncomeBeforeTax: Amount = {
  // /*   Sum(
  //      selfEmployment,
  //      Term(SummaryTotalEmployment),
  //      List(Term(StatePension),
  //        otherPension,
  //        taxableStateBenefits,
  //        otherIncome,
  //        Term(EmploymentBenefits)
  //      )
  //    )*/
  //
  //
  //      selfEmployment +
  //    SummaryTotalEmployment +
  //      StatePension +
  //        otherPension +
  //        taxableStateBenefits +
  //        otherIncome +
  //        EmploymentBenefits
  //
  //
  //  }
  //
  //  //createOtherAllowancesAmount
  //  def otherAllowances: Amount = {
  ////    sum[Liability, Nothing](
  ////      EmploymentExpenses,
  ////      SummaryTotalDedPpr,
  ////      SumTotForeignTaxRelief,
  ////      SumTotLoanRestricted,
  ////      SumTotLossRestricted,
  ////      AnnuityPay,
  ////      GiftsInvCharities,
  ////      TradeUnionDeathBenefits,
  ////      BpaAllowance,
  ////      BPA,
  ////      ExcludedIncome
  ////    ).roundedUp
  //
  //    (
  //      EmploymentExpenses +
  //      SummaryTotalDedPpr +
  //      SumTotForeignTaxRelief +
  //      SumTotLoanRestricted +
  //      SumTotLossRestricted +
  //      AnnuityPay +
  //      GiftsInvCharities +
  //      TradeUnionDeathBenefits +
  //      BpaAllowance +
  //      BPA +
  //      ExcludedIncome
  //      )
  //    .roundedUp
  //  }
  //
  //  //createTotalTaxFreeAmount
  //  def totalTaxFreeAmount: Amount = { //brackets
  //   /* Sum(
  //      otherAllowances,
  //      difference(
  //        PersonalAllowance,
  //        MarriageAllceOut
  //      )
  //    )*/
  //
  //
  //      otherAllowances +
  //      PersonalAllowance -
  //      MarriageAllceOut
  //
  //  }
  //
  //
  //  //createTotalAmountEmployeeNic =
  //  def totalAmountEmployeeNic: Amount = {
  ///*    sum(
  //      EmployeeClass1NI,
  //      EmployeeClass2NI,
  //      Class4Nic
  //    )*/
  //
  //
  //      EmployeeClass1NI +
  //      EmployeeClass2NI +
  //      Class4Nic
  //
  //  }
  //
  //  //basicRateIncomeTaxAmount
  //  def basicRateIncomeTaxAmount: Amount = { //requires predicates TODO
  ///*    sum[Liability,BigDecimal](
  //      IncomeTaxBasicRate,
  //      SavingsTaxLowerRate,
  //      PensionLsumTaxDue
  //    ).filter {
  //        case (PensionLsumTaxDue, pensionLumpSumRate) if pensionLumpSumRate == 1 => true
  //        case _ => false
  //      }*/
  //
  //    sum[Liability,BigDecimal](
  //      IncomeTaxBasicRate +
  //      SavingsTaxLowerRate +
  //      PensionLsumTaxDue
  //    ).filter {
  //      case (PensionLsumTaxDue, pensionLumpSumRate) if pensionLumpSumRate == 1 => true
  //      case _ => false
  //    }
  //  }
  //
  //  //createHigherRateIncomeTaxAmount
  //  def higherRateIncomeTaxAmount: Amount = { //requires predicates TODO
  ////    sum[Liability,BigDecimal](
  ////      IncomeTaxHigherRate,
  ////      SavingsTaxHigherRate,
  ////      PensionLsumTaxDue
  ////    ).filter {
  ////      case (PensionLsumTaxDue, pensionLumpSumRate) if pensionLumpSumRate == 2 => true
  ////      case _ => false
  ////    }
  //
  //    sum[Liability,BigDecimal](
  //      IncomeTaxHigherRate +
  //      SavingsTaxHigherRate +
  //      PensionLsumTaxDue
  //    ).filter {
  //      case (PensionLsumTaxDue, pensionLumpSumRate) if pensionLumpSumRate == 2 => true
  //      case _ => false
  //    }
  //  }
  //
  //  //createAdditionalRateIncomeTaxAmount
  //  def additionalRateIncomeTaxAmount: Amount = { //requires predicates TODO
  //
  //    sum[Liability,BigDecimal](
  //      IncomeTaxAddHighRate +
  //      SavingsTaxAddHighRate +
  //      PensionLsumTaxDue
  //    ).filter{
  //      case (PensionLsumTaxDue, pensionLumpSumRate) if pensionLumpSumRate == 3 => true
  //      case _ => false
  //    }
  //  /*  sum[Liability,BigDecimal](
  //      IncomeTaxAddHighRate,
  //      SavingsTaxAddHighRate,
  //      PensionLsumTaxDue
  //    ).filter{
  //      case (PensionLsumTaxDue, pensionLumpSumRate) if pensionLumpSumRate == 3 => true
  //      case _ => false
  //    }*/
  //  }
  //
  //  //createOtherAdjustmentsIncreasing
  //  def otherAdjustmentsIncreasing: Amount = { //brackets & cannot use helpers.
  ////    Difference(
  ////      Sum(
  ////        Term(NonDomCharge),
  ////        Term(TaxExcluded),
  ////        List(
  ////          Term(IncomeTaxDue),
  ////          Term(NetAnnuityPaytsTaxDue),
  ////          Term(ChildBenefitCharge),
  ////          Term(PensionSavingChargeable)
  ////        )
  ////      ),
  ////      Term(TaxDueAfterAllceRlf)
  ////    )
  //
  //   (
  //        NonDomCharge +
  //        TaxExcluded +
  //          IncomeTaxDue +
  //          NetAnnuityPaytsTaxDue +
  //          ChildBenefitCharge +
  //          PensionSavingChargeable
  //
  //      ) - TaxDueAfterAllceRlf
  //    )
  //  }
  //
  //  //createOtherAdjustmentsReducing
  //  def otherAdjustmentsReducing: Amount = {
  ///*    sum[Liability, Nothing](
  //      DeficiencyRelief,
  //      TopSlicingRelief,
  //      VctSharesRelief,
  //      EisRelief,
  //      SeedEisRelief,
  //      CommInvTrustRel,
  //      SurplusMcaAlimonyRel,
  //      NotionalTaxCegs,
  //      NotlTaxOtherSource,
  //      TaxCreditsForDivs,
  //      QualDistnRelief,
  //      TotalTaxCreditRelief,
  //      NonPayableTaxCredits,
  //      ReliefForFinanceCosts
  //    ).roundedUp*/
  //
  //(
  //      DeficiencyRelief +
  //      TopSlicingRelief +
  //      VctSharesRelief +
  //      EisRelief +
  //      SeedEisRelief +
  //      CommInvTrustRel +
  //      SurplusMcaAlimonyRel +
  //      NotionalTaxCegs +
  //      NotlTaxOtherSource +
  //      TaxCreditsForDivs +
  //      QualDistnRelief +
  //      TotalTaxCreditRelief +
  //      NonPayableTaxCredits +
  //      ReliefForFinanceCosts
  //    ).roundedUp
  //  }
  //
  //  // createTotalIncomeTaxAmount
  //  def totalIncomeTaxAmount: Amount = {
  // /*   Difference(
  //      Sum(
  //        Term(SavingsTaxStartingRate),
  //        basicRateIncomeTaxAmount,
  //        List(higherRateIncomeTaxAmount,
  //          Term(DividendTaxLowRate),
  //          Term(DividendTaxHighRate),
  //          Term(DividendTaxAddHighRate),
  //          otherAdjustmentsIncreasing
  //        )
  //      ),
  //      otherAdjustmentsReducing,
  //      List(Term(MarriageAllceIn))
  //    )*/
  //
  //
  //        SavingsTaxStartingRate +
  //        basicRateIncomeTaxAmount +
  //        higherRateIncomeTaxAmount +
  //        DividendTaxLowRate +
  //        DividendTaxHighRate +
  //        DividendTaxAddHighRate +
  //        otherAdjustmentsIncreasing -
  //        otherAdjustmentsReducing -
  //        MarriageAllceIn
  //    )
  //  }
  //
  //  //createTotalAmountTaxAndNics
  //  def totalAmountTaxAndNics: Amount= {
  //
  ////
  ////    Sum(
  ////      totalAmountEmployeeNic,
  ////      totalIncomeTaxAmount
  ////    )
  //
  //      totalAmountEmployeeNic +
  //      totalIncomeTaxAmount
  //
  //  }
  //
  //  //createYourTotalTax
  //  def totalTax: Amount = {
  ////    Sum(
  ////      totalAmountTaxAndNics,
  ////      totalCapitalGainsTax
  ////    )
  //
  //      totalAmountTaxAndNics +
  //      totalCapitalGainsTax
  //
  //  }
  //
  //  //createBasicRateIncomeTax
  //  def basicIncomeRateIncomeTax:Amount = {
  ////    sum(
  ////      IncomeChargeableBasicRate,
  ////      SavingsChargeableLowerRate
  ////    )
  //
  //
  //      IncomeChargeableBasicRate +
  //      SavingsChargeableLowerRate
  //
  //  }
  //
  //  //createHigherRateIncomeTax
  //  def higherRateIncomeTax: Amount = {
  ////    sum(
  ////      IncomeChargeableHigherRate,
  ////      SavingsChargeableHigherRate
  ////    )
  //
  //      IncomeChargeableHigherRate +
  //      SavingsChargeableHigherRate
  //
  //  }
  //
  //  //createAdditionalRateIncomeTax
  //  def additionalRateIncomeTax: Amount= {
  ////    sum(
  ////      IncomeChargeableAddHRate +
  ////      SavingsChargeableAddHRate
  ////    )
  //
  //
  //      IncomeChargeableAddHRate +
  //        SavingsChargeableAddHRate
  //  }
  //
  //  //createScottishIncomeTax
  //  def scottishIncomeTax: Amount = {
  ////      Multiply(
  //      //    sum(
  //      //      IncomeChargeableBasicRate,
  //      //      IncomeChargeableHigherRate,
  //      //      IncomeChargeableAddHRate
  //      //    ),
  //      //      0.1
  //      //    )
  //    val scottishRate=0.1
  //
  //    (
  //      IncomeChargeableBasicRate +
  //        IncomeChargeableHigherRate +
  //        IncomeChargeableAddHRate
  //      ) * scottishRate
  //
  //
  //
  //  }
  //
  //  //createCgTaxPerCurrencyUnit
  //  def capitalGainsTaxPerCurrency: Amount = {
  //    TaxPerCurrency(totalCapitalGainsTax,taxableGains)
  //  }
  //
  //  //createNicsAndTaxPerCurrencyUnit
  //  def nicsAndTaxPerCurrency: Amount = {
  //    TaxPerCurrency(totalAmountTaxAndNics, totalIncomeBeforeTax)
  //  }
  //
  //

  //createTaxableGains
  implicit def get(liability: Liability) = {
    summaryData.atsData.getOrElse(liability, Amount.empty)
  }

  def taxableGains(): Amount = {
    CgTotGainsAfterLosses +
      CgGainsAfterLosses
  }

  //createPayCapitalGainsTaxOn
  def payCapitalGainsTaxOn: Amount = {

    if (taxableGains < CgAnnualExempt) Amount.empty
    else taxableGains - CgAnnualExempt

  }

  //createTotalCapitalGainsTax
  def totalCapitalGainsTax: Amount = { //brackets

    CgDueEntrepreneursRate +
      CgDueLowerRate +
      CgDueHigherRate -
      CapAdjustment +
      LowerRateCgtRPCI +
      HigherRateCgtRPCI



    //   Sum(
    //    Difference(
    //     Sum(
    //      Term(CgDueEntrepreneursRate),
    //      Term(CgDueLowerRate),
    //      List(
    //        Term(CgDueHigherRate)
    //      )
    //     ),
    //      Term(CapAdjustment)
    //    ),
    //      Term(LowerRateCgtRPCI),
    //      List(Term(HigherRateCgtRPCI)
    //    )
    //   )

  }

  //createSelfEmployment
  def selfEmployment: Amount = {
    /* sum(
       SummaryTotalSchedule,
       SummaryTotalPartnership
     )*/


    SummaryTotalSchedule +
      SummaryTotalPartnership

  }

  //createOtherPension
  def otherPension: Amount = {
    //    sum(
    //      OtherPension,
    //      StatePensionGross
    //    )
    OtherPension +
      StatePensionGross

  }

  //createTaxableStateBenefits
  def taxableStateBenefits: Amount = {
    /*   sum(
         IncBenefitSuppAllow,
         JobSeekersAllowance,
         OthStatePenBenefits
       )*/


    IncBenefitSuppAllow +
      JobSeekersAllowance +
      OthStatePenBenefits

  }

  //createOtherIncome
  def otherIncome: Amount = {
    //    sum(
    //      SummaryTotShareOptions,
    //      SummaryTotalUklProperty,
    //      SummaryTotForeignIncome,
    //      SummaryTotTrustEstates,
    //      SummaryTotalOtherIncome,
    //      SummaryTotalUkInterest,
    //      SummaryTotForeignDiv,
    //      SummaryTotalUkIntDivs,
    //      SumTotLifePolicyGains
    //    )

    SummaryTotShareOptions +
      SummaryTotalUklProperty +
      SummaryTotForeignIncome +
      SummaryTotTrustEstates +
      SummaryTotalOtherIncome +
      SummaryTotalUkInterest +
      SummaryTotForeignDiv +
      SummaryTotalUkIntDivs +
      SumTotLifePolicyGains

  }

  //createTotalIncomeBeforeTax
  def totalIncomeBeforeTax: Amount = {
    /*   Sum(
         selfEmployment,
         Term(SummaryTotalEmployment),
         List(Term(StatePension),
           otherPension,
           taxableStateBenefits,
           otherIncome,
           Term(EmploymentBenefits)
         )
       )*/


    selfEmployment +
      SummaryTotalEmployment +
      StatePension +
      otherPension +
      taxableStateBenefits +
      otherIncome +
      EmploymentBenefits


  }

  //createOtherAllowancesAmount
  def otherAllowances: Amount = {
    //    sum[Liability, Nothing](
    //      EmploymentExpenses,
    //      SummaryTotalDedPpr,
    //      SumTotForeignTaxRelief,
    //      SumTotLoanRestricted,
    //      SumTotLossRestricted,
    //      AnnuityPay,
    //      GiftsInvCharities,
    //      TradeUnionDeathBenefits,
    //      BpaAllowance,
    //      BPA,
    //      ExcludedIncome
    //    ).roundedUp

    (
      EmploymentExpenses +
        SummaryTotalDedPpr +
        SumTotForeignTaxRelief +
        SumTotLoanRestricted +
        SumTotLossRestricted +
        AnnuityPay +
        GiftsInvCharities +
        TradeUnionDeathBenefits +
        BpaAllowance +
        BPA +
        ExcludedIncome
      ).roundAmountUp()
  }

  //createTotalTaxFreeAmount
  def totalTaxFreeAmount: Amount = { //brackets
    /* Sum(
       otherAllowances,
       difference(
         PersonalAllowance,
         MarriageAllceOut
       )
     )*/


    otherAllowances +
      PersonalAllowance -
      MarriageAllceOut

  }


  //createTotalAmountEmployeeNic =
  def totalAmountEmployeeNic: Amount = {
    /*    sum(
          EmployeeClass1NI,
          EmployeeClass2NI,
          Class4Nic
        )*/


    (EmployeeClass1NI +
      EmployeeClass2NI +
      Class4Nic).roundAmountUp()

  }

  //basicRateIncomeTaxAmount
  def basicRateIncomeTaxAmount: Amount = { //requires predicates TODO
    /*    sum[Liability,BigDecimal](
          IncomeTaxBasicRate,
          SavingsTaxLowerRate,
          PensionLsumTaxDue
        ).filter {
            case (PensionLsumTaxDue, pensionLumpSumRate) if pensionLumpSumRate == 1 => true
            case _ => false
          }*/


    IncomeTaxBasicRate + SavingsTaxLowerRate + {
      if (summaryData.pensionLumpSumTaxRate.value == 0.20) PensionLsumTaxDue //rates TODO
      else Amount.empty
    }
  }

  //createHigherRateIncomeTaxAmount
  def higherRateIncomeTaxAmount: Amount = { //requires predicates TODO
    //    sum[Liability,BigDecimal](
    //      IncomeTaxHigherRate,
    //      SavingsTaxHigherRate,
    //      PensionLsumTaxDue
    //    ).filter {
    //      case (PensionLsumTaxDue, pensionLumpSumRate) if pensionLumpSumRate == 2 => true
    //      case _ => false
    //    }

    IncomeTaxHigherRate +
      SavingsTaxHigherRate + {
      if (summaryData.pensionLumpSumTaxRate.value == 0.40) PensionLsumTaxDue //rates TODO
      else Amount.empty
    }
  }

  //createAdditionalRateIncomeTaxAmount
  def additionalRateIncomeTaxAmount: Amount = { //requires predicates TODO


    IncomeTaxAddHighRate +
      SavingsTaxAddHighRate + {
      if (summaryData.pensionLumpSumTaxRate.value == 0.45) PensionLsumTaxDue //rates TODO
      else Amount.empty
    }
    /*  sum[Liability,BigDecimal](
        IncomeTaxAddHighRate,
        SavingsTaxAddHighRate,
        PensionLsumTaxDue
      ).filter{
        case (PensionLsumTaxDue, pensionLumpSumRate) if pensionLumpSumRate == 3 => true
        case _ => false
      }*/
  }

  //createOtherAdjustmentsIncreasing
  def otherAdjustmentsIncreasing: Amount = { //brackets & cannot use helpers.
    //    Difference(
    //      Sum(
    //        Term(NonDomCharge),
    //        Term(TaxExcluded),
    //        List(
    //          Term(IncomeTaxDue),
    //          Term(NetAnnuityPaytsTaxDue),
    //          Term(ChildBenefitCharge),
    //          Term(PensionSavingChargeable)
    //        )
    //      ),
    //      Term(TaxDueAfterAllceRlf)
    //    )

    (
      NonDomCharge +
        TaxExcluded +
        IncomeTaxDue +
        NetAnnuityPaytsTaxDue +
        ChildBenefitCharge +
        PensionSavingChargeable

      ) - TaxDueAfterAllceRlf

  }

  //createOtherAdjustmentsReducing
  def otherAdjustmentsReducing: Amount = {
    /*    sum[Liability, Nothing](
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
        ).roundedUp*/

    (
      DeficiencyRelief +
        TopSlicingRelief +
        VctSharesRelief +
        EisRelief +
        SeedEisRelief +
        CommInvTrustRel +
        SurplusMcaAlimonyRel +
        NotionalTaxCegs +
        NotlTaxOtherSource +
        TaxCreditsForDivs +
        QualDistnRelief +
        TotalTaxCreditRelief +
        NonPayableTaxCredits +
        ReliefForFinanceCosts
      ).roundAmountUp()
  }

  // createTotalIncomeTaxAmount
  def totalIncomeTaxAmount: Amount = {
    /*   Difference(
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
       )*/


    SavingsTaxStartingRate +
      basicRateIncomeTaxAmount +
      higherRateIncomeTaxAmount +
      DividendTaxLowRate +
      DividendTaxHighRate +
      DividendTaxAddHighRate +
      otherAdjustmentsIncreasing -
      otherAdjustmentsReducing -
      MarriageAllceIn

  }

  //createTotalAmountTaxAndNics
  def totalAmountTaxAndNics: Amount = {

    //
    //    Sum(
    //      totalAmountEmployeeNic,
    //      totalIncomeTaxAmount
    //    )

    totalAmountEmployeeNic +
      totalIncomeTaxAmount

  }

  //createYourTotalTax
  def totalTax: Amount = {
    //    Sum(
    //      totalAmountTaxAndNics,
    //      totalCapitalGainsTax
    //    )

    totalAmountTaxAndNics +
      totalCapitalGainsTax

  }

  //createBasicRateIncomeTax
  def basicIncomeRateIncomeTax: Amount = {
    //    sum(
    //      IncomeChargeableBasicRate,
    //      SavingsChargeableLowerRate
    //    )


    IncomeChargeableBasicRate +
      SavingsChargeableLowerRate

  }

  //createHigherRateIncomeTax
  def higherRateIncomeTax: Amount = {
    //    sum(
    //      IncomeChargeableHigherRate,
    //      SavingsChargeableHigherRate
    //    )

    IncomeChargeableHigherRate +
      SavingsChargeableHigherRate
  }

  //createAdditionalRateIncomeTax
  def additionalRateIncomeTax: Amount = {
    //    sum(
    //      IncomeChargeableAddHRate +
    //      SavingsChargeableAddHRate
    //    )


    IncomeChargeableAddHRate +
      SavingsChargeableAddHRate
  }

  //createScottishIncomeTax
  def scottishIncomeTax = {
    //      Multiply(
    //    sum(
    //      IncomeChargeableBasicRate,
    //      IncomeChargeableHigherRate,
    //      IncomeChargeableAddHRate
    //    ),
    //      0.1
    //    )
    val scottishRate = 0.1

   Amount((
      IncomeChargeableBasicRate +
        IncomeChargeableHigherRate +
        IncomeChargeableAddHRate
      ).amount * scottishRate,"GBP")

  }

  def hasLiability:Boolean={
    !totalTax.isZeroOrLess
  }
  //createCgTaxPerCurrencyUnit
  def capitalGainsTaxPerCurrency: Amount = {
    taxPerTaxableCurrencyUnit(totalCapitalGainsTax,taxableGains)
  }

  //createNicsAndTaxPerCurrencyUnit
  def nicsAndTaxPerCurrency: Amount = {
    taxPerTaxableCurrencyUnit(totalAmountTaxAndNics, totalIncomeBeforeTax)
  }

  private def taxPerTaxableCurrencyUnit(tax: Amount, taxable: Amount) =
    taxable match {
      case value if value.isZero => taxable
      case _ => tax.divideWithPrecision(taxable, 4)
    }

  //createNicsAndTaxTaxRate
  def totalNicsAndTaxLiabilityAsPercentage:Rate = liabilityAsPercentage(nicsAndTaxPerCurrency)

  //createTotalCgTaxRate
  def totalCgTaxLiabilityAsPercentage:Rate = liabilityAsPercentage(capitalGainsTaxPerCurrency)

  //rateFromPerUnitAmount
  private def liabilityAsPercentage(amountPerUnit: Amount) = {
    Rate(formatter.format((amountPerUnit.amount * 100).setScale(2, BigDecimal.RoundingMode.DOWN)) + "%")
  }
}