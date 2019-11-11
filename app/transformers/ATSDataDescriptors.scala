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

  //createTaxableGains
  def get(liability: Liability): Amount = {
    summaryData.atsData.getOrElse(liability,
      summaryData.nationalInsuranceData.getOrElse(liability,
        throw ATSParsingException(liability.apiValue))
    )
  }

  def getWithDefaultAmount(liability: Liability): Amount ={
    try {
      get(liability)
    } catch {
      case e : ATSParsingException => Amount(0.0, "GBP")
      case e => throw new Exception(e)
    }
  }

  def taxableGains(): Amount = {
    get(CgTotGainsAfterLosses) +
      get(CgGainsAfterLosses)
  }

  //createPayCapitalGainsTaxOn
  def payCapitalGainsTaxOn: Amount = {

    if (taxableGains < get(CgAnnualExempt)) Amount.empty
    else taxableGains - get(CgAnnualExempt)

  }

  //createTotalCapitalGainsTax
  def totalCapitalGainsTax: Amount = { //brackets

    get(CgDueEntrepreneursRate) +
      get(CgDueLowerRate) +
      get(CgDueHigherRate) -
      get(CapAdjustment) +
      get(LowerRateCgtRPCI) +
      get(HigherRateCgtRPCI)



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


    get(SummaryTotalSchedule) +
      get(SummaryTotalPartnership)

  }

  //createOtherPension
  def otherPension: Amount = {
    //    sum(
    //      OtherPension,
    //      StatePensionGross
    //    )
    get(OtherPension) +
      get(StatePensionGross)

  }

  //createTaxableStateBenefits
  def taxableStateBenefits: Amount = {
    /*   sum(
         IncBenefitSuppAllow,
         JobSeekersAllowance,
         OthStatePenBenefits
       )*/


    get(IncBenefitSuppAllow) +
      get(JobSeekersAllowance) +
      get(OthStatePenBenefits)

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

    get(SummaryTotShareOptions) +
      get(SummaryTotalUklProperty) +
      get(SummaryTotForeignIncome) +
      get(SummaryTotTrustEstates) +
      get(SummaryTotalOtherIncome) +
      get(SummaryTotalUkInterest) +
      get(SummaryTotForeignDiv) +
      get(SummaryTotalUkIntDivs) +
      get(SumTotLifePolicyGains)

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
      get(SummaryTotalEmployment) +
      get(StatePension) +
      otherPension +
      taxableStateBenefits +
      otherIncome +
      get(EmploymentBenefits)


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
      get(EmploymentExpenses) +
        get(SummaryTotalDedPpr) +
        get(SumTotForeignTaxRelief) +
        get(SumTotLoanRestricted) +
        get(SumTotLossRestricted) +
        get(AnnuityPay) +
        get(GiftsInvCharities) +
        get(TradeUnionDeathBenefits) +
        get(BpaAllowance) +
        get(BPA) +
        get(ExcludedIncome)
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
      get(PersonalAllowance) -
      get(MarriageAllceOut)

  }


  //createTotalAmountEmployeeNic =
  def totalAmountEmployeeNic: Amount = {
    /*    sum(
          EmployeeClass1NI,
          EmployeeClass2NI,
          Class4Nic
        )*/


    (
      get(EmployeeClass1NI) +
        get(EmployeeClass2NI) +
        get(Class4Nic)
      ).roundAmountUp()

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


    get(IncomeTaxBasicRate) + get(SavingsTaxLowerRate) + {
      if (summaryData.pensionLumpSumTaxRate.value == 0.20) get(PensionLsumTaxDue) //rates TODO
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

    get(IncomeTaxHigherRate) +
      get(SavingsTaxHigherRate) + {
      if (summaryData.pensionLumpSumTaxRate.value == 0.40) get(PensionLsumTaxDue) //rates TODO
      else Amount.empty
    }
  }

  //createAdditionalRateIncomeTaxAmount
  def additionalRateIncomeTaxAmount: Amount = { //requires predicates TODO


    get(IncomeTaxAddHighRate) +
      get(SavingsTaxAddHighRate) + {
      if (summaryData.pensionLumpSumTaxRate.value == 0.45) get(PensionLsumTaxDue) //rates TODO
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
      get(NonDomCharge) +
        get(TaxExcluded) +
        get(IncomeTaxDue) +
        get(NetAnnuityPaytsTaxDue) +
        get(ChildBenefitCharge) +
        get(PensionSavingChargeable)

      ) - get(TaxDueAfterAllceRlf)

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
      get(DeficiencyRelief) +
        get(TopSlicingRelief) +
        get(VctSharesRelief) +
        get(EisRelief) +
        get(SeedEisRelief) +
        get(CommInvTrustRel) +
        get(SurplusMcaAlimonyRel) +
        get(NotionalTaxCegs) +
        get(NotlTaxOtherSource) +
        get(TaxCreditsForDivs) +
        get(QualDistnRelief) +
        get(TotalTaxCreditRelief) +
        get(NonPayableTaxCredits) +
        getWithDefaultAmount(ReliefForFinanceCosts)
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


    get(SavingsTaxStartingRate) +
      basicRateIncomeTaxAmount +
      higherRateIncomeTaxAmount +
      get(DividendTaxLowRate) +
      get(DividendTaxHighRate) +
      get(DividendTaxAddHighRate) +
      otherAdjustmentsIncreasing -
      otherAdjustmentsReducing -
      get(MarriageAllceIn)

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


    get(IncomeChargeableBasicRate) +
      get(SavingsChargeableLowerRate)

  }

  //createHigherRateIncomeTax
  def higherRateIncomeTax: Amount = {
    //    sum(
    //      IncomeChargeableHigherRate,
    //      SavingsChargeableHigherRate
    //    )

    get(IncomeChargeableHigherRate) +
      get(SavingsChargeableHigherRate)
  }

  //createAdditionalRateIncomeTax
  def additionalRateIncomeTax: Amount = {
    //    sum(
    //      IncomeChargeableAddHRate +
    //      SavingsChargeableAddHRate
    //    )


    get(IncomeChargeableAddHRate) +
      get(SavingsChargeableAddHRate)
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
      get(IncomeChargeableBasicRate) +
        get(IncomeChargeableHigherRate) +
        get(IncomeChargeableAddHRate)
      ).amount * scottishRate, "GBP")

  }

  def hasLiability: Boolean = {
    !totalTax.isZeroOrLess
  }

  //createCgTaxPerCurrencyUnit
  def capitalGainsTaxPerCurrency: Amount = {
    taxPerTaxableCurrencyUnit(totalCapitalGainsTax, taxableGains)
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
  def totalNicsAndTaxLiabilityAsPercentage: Rate = liabilityAsPercentage(nicsAndTaxPerCurrency)

  //createTotalCgTaxRate
  def totalCgTaxLiabilityAsPercentage: Rate = liabilityAsPercentage(capitalGainsTaxPerCurrency)

  //rateFromPerUnitAmount
  private def liabilityAsPercentage(amountPerUnit: Amount) = {
    Rate(formatter.format((amountPerUnit.amount * 100).setScale(2, BigDecimal.RoundingMode.DOWN)) + "%")
  }
}