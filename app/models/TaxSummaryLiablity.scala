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

package models

import play.api.libs.functional.syntax._
import play.api.libs.json._

sealed abstract class Liability(val apiValue: String)

object Liability {

  case object AnnuityPay extends Liability("grossAnnuityPayts")
  case object BPA extends Liability("itfBpaAmount")
  case object BpaAllowance extends Liability("ctnBpaAllowanceAmt")
  case object CGAtHigherRateRPCI extends Liability("ctnCGAtHigherRateRPCI")
  case object CGAtLowerRateRPCI extends Liability("ctnCGAtLowerRateRPCI")
  case object CGOtherGainsAfterLoss extends Liability("atsCGOtherGainsAfterLoss")
  case object CapAdjustment extends Liability("capAdjustmentAmt")
  case object CgAnnualExempt extends Liability("atsCgAnnualExemptAmt")
  case object CgAtEntrepreneursRate extends Liability("ctnCgAtEntrepreneursRate")
  case object CgAtHigherRate extends Liability("ctnCgAtHigherRate")
  case object CgAtLowerRate extends Liability("ctnCgAtLowerRate")
  case object CgDueEntrepreneursRate extends Liability("ctnCgDueEntrepreneursRate")
  case object CgDueHigherRate extends Liability("ctnCgDueHigherRate")
  case object CgDueLowerRate extends Liability("ctnCgDueLowerRate")
  case object CgGainsAfterLosses extends Liability("atsCgGainsAfterLossesAmt")
  case object CgTotGainsAfterLosses extends Liability("atsCgTotGainsAfterLosses")
  case object ChildBenefitCharge extends Liability("ctnChildBenefitChrgAmt")
  case object Class4Nic extends Liability("class4Nic")
  case object CommInvTrustRel extends Liability("ctnCommInvTrustRelAmt")
  case object DeficiencyRelief extends Liability("ctnDeficiencyRelief")
  case object DividendChargeableAddHRate extends Liability("ctnDividendChgbleAddHRate")
  case object DividendChargeableHighRate extends Liability("ctnDividendChgbleHighRate")
  case object DividendChargeableLowRate extends Liability("ctnDividendChgbleLowRate")
  case object DividendTaxAddHighRate extends Liability("ctnDividendTaxAddHighRate")
  case object DividendTaxHighRate extends Liability("ctnDividendTaxHighRate")
  case object DividendTaxLowRate extends Liability("ctnDividendTaxLowRate")
  case object EisRelief extends Liability("ctnEisReliefAmt")
  case object EmploymentBenefits extends Liability("ctnEmploymentBenefitsAmt")
  case object EmploymentExpenses extends Liability("ctnEmploymentExpensesAmt")
  case object ExcludedIncome extends Liability("grossExcludedIncome")
  case object GiftsInvCharities extends Liability("itf4GiftsInvCharitiesAmo")
  case object HigherRateCgtRPCI extends Liability("ctnHigherRateCgtRPCI")
  case object IncBenefitSuppAllow extends Liability("atsIncBenefitSuppAllowAmt")
  case object IncomeChargeableAddHRate extends Liability("ctnIncomeChgbleAddHRate")
  case object IncomeChargeableBasicRate extends Liability("ctnIncomeChgbleBasicRate")
  case object IncomeChargeableHigherRate extends Liability("ctnIncomeChgbleHigherRate")
  case object IncomeTaxAddHighRate extends Liability("ctnIncomeTaxAddHighRate")
  case object IncomeTaxBasicRate extends Liability("ctnIncomeTaxBasicRate")
  case object IncomeTaxDue extends Liability("incomeTaxDue")
  case object IncomeTaxHigherRate extends Liability("ctnIncomeTaxHigherRate")
  case object JobSeekersAllowance extends Liability("atsJobSeekersAllowanceAmt")
  case object LowerRateCgtRPCI extends Liability("ctnLowerRateCgtRPCI")
  case object MarriageAllceIn extends Liability("ctnMarriageAllceInAmt")
  case object MarriageAllceOut extends Liability("ctnMarriageAllceOutAmt")
  case object NRGTGainsAfterLoss extends Liability("atsNRGTGainsAfterLoss")
  case object NetAnnuityPaytsTaxDue extends Liability("netAnnuityPaytsTaxDue")
  case object NonDomCharge extends Liability("nonDomChargeAmount")
  case object NonPayableTaxCredits extends Liability("ctnNonPayableTaxCredits")
  case object NotionalTaxCegs extends Liability("ctnNotionalTaxCegs")
  case object NotlTaxOtherSource extends Liability("ctnNotlTaxOthrSrceAmo")
  case object OthStatePenBenefits extends Liability("atsOthStatePenBenefitsAmt")
  case object OtherPension extends Liability( "atsOtherPensionAmt")
  case object PensionLsumTaxDue extends Liability("ctnPensionLsumTaxDueAmt")
  case object PensionSavingChargeable extends Liability("ctnPensionSavingChrgbleAmt")
  case object PersonalAllowance extends Liability("ctnPersonalAllowance")
  case object QualDistnRelief extends Liability("ctnQualDistnReliefAmt")
  case object ReliefForFinanceCosts extends Liability("reliefForFinanceCosts")
  case object SavingsChargeableAddHRate extends Liability("ctnSavingsChgbleAddHRate")
  case object SavingsChargeableHigherRate extends Liability("ctnSavingsChgbleHigherRate")
  case object SavingsChargeableLowerRate extends Liability("ctnSavingsChgbleLowerRate")
  case object SavingsChargeableStartRate extends Liability("ctnSavingsChgbleStartRate")
  case object SavingsTaxAddHighRate extends Liability("ctnSavingsTaxAddHighRate")
  case object SavingsTaxHigherRate extends Liability("ctnSavingsTaxHigherRate")
  case object SavingsTaxLowerRate extends Liability("ctnSavingsTaxLowerRate")
  case object SavingsTaxStartingRate extends Liability("ctnSavingsTaxStartingRate")
  case object SeedEisRelief extends Liability("ctnSeedEisReliefAmt")
  case object SocialInvTaxRel extends Liability("ctnSocialInvTaxRelAmt")
  case object StatePension extends Liability( "atsStatePensionAmt")
  case object StatePensionGross extends Liability( "itfStatePensionLsGrossAmt")
  case object SumTotForeignTaxRelief extends Liability("ctnSumTotForeignTaxRelief")
  case object SumTotLifePolicyGains extends Liability("ctn4SumTotLifePolicyGains")
  case object SumTotLoanRestricted extends Liability("ctnSumTotLoanRestricted")
  case object SumTotLossRestricted extends Liability("ctnSumTotLossRestricted")
  case object SummaryTotForeignDiv extends Liability("ctnSummaryTotForeignDiv")
  case object SummaryTotForeignIncome extends Liability("ctnSummaryTotForeignIncome")
  case object SummaryTotShareOptions extends Liability("ctnSummaryTotShareOptions")
  case object SummaryTotTrustEstates extends Liability("ctnSummaryTotTrustEstates")
  case object SummaryTotalDedPpr extends Liability("ctnSummaryTotalDedPpr")
  case object SummaryTotalEmployment extends Liability( "ctnSummaryTotalEmployment")
  case object SummaryTotalOtherIncome extends Liability("ctnSummaryTotalOtherIncome")
  case object SummaryTotalPartnership extends Liability("ctnSummaryTotalPartnership")
  case object SummaryTotalSchedule extends Liability("ctnSummaryTotalScheduleD")
  case object SummaryTotalUkIntDivs extends Liability("ctnSummaryTotalUkIntDivs")
  case object SummaryTotalUkInterest extends Liability("ctnSummaryTotalUkInterest")
  case object SummaryTotalUklProperty extends Liability("ctnSummaryTotalUklProperty")
  case object SurplusMcaAlimonyRel extends Liability("atsSurplusMcaAlimonyRel")
  case object TaxCharged extends Liability("atsTaxCharged")
  case object TaxCreditsForDivs extends Liability("ctnTaxCredForDivs")
  case object TaxDueAfterAllceRlf extends Liability("ctn4TaxDueAfterAllceRlf")
  case object TaxExcluded extends Liability("taxExcluded")
  case object TopSlicingRelief extends Liability("topSlicingRelief")
  case object TotalTaxCreditRelief extends Liability("figTotalTaxCreditRelief")
  case object TradeUnionDeathBenefits extends Liability("itfTradeUnionDeathBenefits")
  case object VctSharesRelief extends Liability("ctnVctSharesReliefAmt")

  val allLiabilities: List[Liability] =
    List(AnnuityPay, BPA, BpaAllowance, CGAtHigherRateRPCI, CGAtLowerRateRPCI, CGOtherGainsAfterLoss, CapAdjustment,
      CgAnnualExempt, CgAtEntrepreneursRate, CgAtHigherRate, CgAtLowerRate, CgDueEntrepreneursRate, CgDueHigherRate,
      CgDueLowerRate, CgGainsAfterLosses, CgTotGainsAfterLosses, ChildBenefitCharge, Class4Nic, CommInvTrustRel,
      DeficiencyRelief, DividendChargeableAddHRate, DividendChargeableHighRate, DividendChargeableLowRate,
      DividendTaxAddHighRate, DividendTaxHighRate, DividendTaxLowRate, EisRelief, EmploymentBenefits, EmploymentExpenses,
      ExcludedIncome, GiftsInvCharities, HigherRateCgtRPCI, IncBenefitSuppAllow, IncomeChargeableAddHRate,
      IncomeChargeableBasicRate, IncomeChargeableHigherRate, IncomeTaxAddHighRate, IncomeTaxBasicRate, IncomeTaxDue,
      IncomeTaxHigherRate, JobSeekersAllowance, LowerRateCgtRPCI, MarriageAllceIn, MarriageAllceOut, NRGTGainsAfterLoss,
      NetAnnuityPaytsTaxDue, NonDomCharge, NonPayableTaxCredits, NotionalTaxCegs, NotlTaxOtherSource, OthStatePenBenefits,
      OtherPension, PensionLsumTaxDue, PensionSavingChargeable, PersonalAllowance, QualDistnRelief, ReliefForFinanceCosts,
      SavingsChargeableAddHRate, SavingsChargeableHigherRate, SavingsChargeableLowerRate, SavingsChargeableStartRate,
      SavingsTaxAddHighRate, SavingsTaxHigherRate, SavingsTaxLowerRate, SavingsTaxStartingRate, SeedEisRelief,
      SocialInvTaxRel, StatePension, StatePensionGross, SumTotForeignTaxRelief, SumTotLifePolicyGains, SumTotLoanRestricted,
      SumTotLossRestricted, SummaryTotForeignDiv, SummaryTotForeignIncome, SummaryTotShareOptions, SummaryTotTrustEstates,
      SummaryTotalDedPpr, SummaryTotalEmployment, SummaryTotalOtherIncome, SummaryTotalPartnership, SummaryTotalSchedule,
      SummaryTotalUkIntDivs, SummaryTotalUkInterest, SummaryTotalUklProperty, SurplusMcaAlimonyRel, TaxCharged, TaxCreditsForDivs,
      TaxDueAfterAllceRlf, TaxExcluded, TopSlicingRelief, TotalTaxCreditRelief, TradeUnionDeathBenefits, VctSharesRelief)

  implicit val reads: Reads[Liability] = Reads[Liability] {
    case JsString(s) =>
      allLiabilities
        .find(l => l.apiValue == s)
        .fold[JsResult[Liability]](JsError("error.expected.liability"))(JsSuccess(_))

    case _ => JsError("error.expected.jsstring")
  }

}


final case class TaxSummaryLiablity(taxYear: Int, ctnPensionLumpSumTaxRate: Double, incomeTaxStatus: String ,
                                    saPayeNicDetails: Map[String, Amount], tliSlpAtsData: Map[Liability, Amount])

  object TaxSummaryLiablity {

    implicit def mapReads[K: Reads, V: Reads]: Reads[Map[K, V]] =
      Reads[Map[K, V]] {
        case JsObject(m) => {

          JsSuccess(m.foldLeft(Map.empty[K, V]) {
            case (acc, (key, value)) =>
              val result = for {
                rv <- value.validate[V]
                rk <- JsString(key).validate[K]
              } yield rk -> rv

              result match {
                case JsSuccess(v, _) => acc + v
                case _               => acc
              }
          })
        }

        case _ => JsError("error.expected.jsobject")

      }

    implicit  val reads: Reads[TaxSummaryLiablity] =
      (
        (JsPath \ "taxYear").read[Int] and
        (JsPath \ "tliSlpAtsData" \ "ctnPensionLumpSumTaxRate").read[Double] and
        (JsPath \ "tliSlpAtsData" \ "incomeTaxStatus").read[String] and
        (JsPath \ "saPayeNicDetails").read(mapReads[String, Amount]) and
        (JsPath \ "tliSlpAtsData").read(mapReads[Liability, Amount])
        )(TaxSummaryLiablity.apply _)
    }





