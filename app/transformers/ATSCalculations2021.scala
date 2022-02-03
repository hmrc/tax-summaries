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

import models.Liability.{DividendsPartnership, SavingsPartnership, TaxOnNonExcludedIncome, _}
import models._
import services._

import scala.util.{Failure, Success, Try}

trait ATSCalculations2021 extends ATSCalculations {

  val summaryData: TaxSummaryLiability
  val taxRates: TaxRateService

  override def selfEmployment: Amount =
    get(SummaryTotalSchedule) +
      get(SummaryTotalPartnership) +
      get(SavingsPartnership) +
      get(DividendsPartnership)

  override def otherIncome: Amount =
    get(SummaryTotShareOptions) +
      get(SummaryTotalUklProperty) +
      get(SummaryTotForeignIncome) +
      get(SummaryTotTrustEstates) +
      get(SummaryTotalOtherIncome) +
      get(SummaryTotalUkInterest) +
      get(SummaryTotForeignDiv) +
      get(SummaryTotalUkIntDivs) +
      get(SumTotLifePolicyGains) +
      get(SummaryTotForeignSav)

  override def otherAllowances: Amount =
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
        get(BPA)
    ).roundAmountUp()

  override def otherAdjustmentsIncreasing: Amount =
    get(NonDomCharge) +
      get(GiftAidTaxReduced) +
      get(NetAnnuityPaytsTaxDue) +
      get(ChildBenefitCharge) +
      get(PensionSavingChargeable)

  override def totalIncomeTaxAmount: Amount = {
    val rateDividendAdjustmentTax = savingsRateAmount + // LS12.1
      basicRateIncomeTaxAmount + // LS12.2
      higherRateIncomeTaxAmount + // LS12.3
      additionalRateIncomeTaxAmount +
      get(DividendTaxLowRate) +
      get(DividendTaxHighRate) + //LS13.2
      get(DividendTaxAddHighRate) +
      otherAdjustmentsIncreasing -
      otherAdjustmentsReducing -
      getWithDefaultAmount(MarriageAllceIn)

    val excludedAndNonExcludedTax = get(TaxExcluded) + getWithDefaultAmount(TaxOnNonExcludedIncome)

    if (excludedAndNonExcludedTax.amount > 0) {
      List(rateDividendAdjustmentTax, excludedAndNonExcludedTax).min
    } else {
      rateDividendAdjustmentTax
    }
  }

}
