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

package sa.controllers.testOnly

import com.google.inject.Inject
import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.concurrent.ExecutionContext

class AtsSaFieldListController @Inject() (
  cc: ControllerComponents
)(implicit val ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  private val fields2023: Seq[String] = Seq(
    "ctnPensionLumpSumTaxRate",
    "ctnEmploymentBenefitsAmt",
    "ctnSummaryTotalScheduleD",
    "ctnSummaryTotalPartnership",
    "ctnSummaryTotalEmployment",
    "atsStatePensionAmt",
    "atsOtherPensionAmt",
    "itfStatePensionLsGrossAmt",
    "atsIncBenefitSuppAllowAmt",
    "atsJobSeekersAllowanceAmt",
    "atsOthStatePenBenefitsAmt",
    "ctnSummaryTotShareOptions",
    "ctnSummaryTotalUklProperty",
    "ctnSummaryTotForeignIncome",
    "ctnSummaryTotTrustEstates",
    "ctnSummaryTotalOtherIncome",
    "ctnSummaryTotForeignSav",
    "ctnForeignCegDedn",
    "ctnSummaryTotalUkInterest",
    "itfCegReceivedAfterTax",
    "ctnSummaryTotForeignDiv",
    "ctnSummaryTotalUkIntDivs",
    "ctn4SumTotLifePolicyGains",
    "ctnPersonalAllowance",
    "ctnEmploymentExpensesAmt",
    "ctnSummaryTotalDedPpr",
    "ctnSumTotForeignTaxRelief",
    "ctnSumTotLoanRestricted",
    "ctnSumTotLossRestricted",
    "grossAnnuityPayts",
    "itf4GiftsInvCharitiesAmo",
    "itfTradeUnionDeathBenefits",
    "ctnBpaAllowanceAmt",
    "itfBpaAmount",
    "grossExcludedIncome",
    "class4Nic",
    "ctnClass2NicAmt",
    "ctnSavingsChgbleStartRate",
    "ctnSavingsTaxStartingRate",
    "ctnIncomeChgbleBasicRate",
    "ctnSavingsChgbleLowerRate",
    "ctnIncomeTaxBasicRate",
    "ctnSavingsTaxLowerRate",
    "ctnIncomeChgbleHigherRate",
    "ctnSavingsChgbleHigherRate",
    "ctnIncomeTaxHigherRate",
    "ctnSavingsTaxHigherRate",
    "ctnIncomeChgbleAddHRate",
    "ctnSavingsChgbleAddHRate",
    "ctnIncomeTaxAddHighRate",
    "ctnSavingsTaxAddHighRate",
    "taxablePaySSR",
    "taxOnPaySSR",
    "taxablePaySIR",
    "taxOnPaySIR",
    "ctnDividendChgbleLowRate",
    "ctnDividendTaxLowRate",
    "ctnDividendChgbleHighRate",
    "ctnDividendTaxHighRate",
    "ctnDividendChgbleAddHRate",
    "ctnDividendTaxAddHighRate",
    "ctnTaxableRedundancySSR",
    "ctnTaxOnRedundancySsr",
    "ctnTaxableRedundancyBr",
    "ctnTaxOnRedundancyBr",
    "ctnTaxableRedundancySir",
    "ctnTaxOnRedundancySir",
    "ctnTaxableRedundancyHr",
    "ctnTaxOnRedundancyHr",
    "ctnTaxableRedundancyAhr",
    "ctnTaxOnRedundancyAhr",
    "ctnTaxableCegBr",
    "ctnTaxOnCegBr",
    "ctnTaxableCegHr",
    "ctnTaxOnCegHr",
    "ctnTaxableCegAhr",
    "ctnTaxOnCegAhr",
    "nonDomChargeAmount",
    "taxExcluded",
    "taxOnNonExcludedInc",
    "incomeTaxDue",
    "ctn4TaxDueAfterAllceRlf",
    "netAnnuityPaytsTaxDue",
    "ctnChildBenefitChrgAmt",
    "ctnPensionSavingChrgbleAmt",
    "atsTaxCharged",
    "ctnDeficiencyRelief",
    "topSlicingRelief",
    "ctnVctSharesReliefAmt",
    "ctnEisReliefAmt",
    "ctnSeedEisReliefAmt",
    "ctnCommInvTrustRelAmt",
    "atsSurplusMcaAlimonyRel",
    "alimony",
    "ctnNotionalTaxCegs",
    "ctnNotlTaxOthrSrceAmo",
    "ctnFtcrRestricted",
    "reliefForFinanceCosts",
    "lfiRelief",
    "ctnRelTaxAcctFor",
    "ctnTaxCredForDivs",
    "ctnQualDistnReliefAmt",
    "figTotalTaxCreditRelief",
    "ctnNonPayableTaxCredits",
    "atsCgTotGainsAfterLosses",
    "atsCgGainsAfterLossesAmt",
    "cap3AssessableChgeableGain",
    "atsCgAnnualExemptAmt",
    "ctnCgAtEntrepreneursRate",
    "ctnCgDueEntrepreneursRate",
    "ctnCgAtLowerRate",
    "ctnCgDueLowerRate",
    "ctnCgAtHigherRate",
    "ctnCgDueHigherRate",
    "ctnCGAtLowerRateRPCI",
    "ctnLowerRateCgtRPCI",
    "ctnCGAtHigherRateRPCI",
    "ctnHigherRateCgtRPCI",
    "capAdjustmentAmt",
    "ctnPensionLsumTaxDueAmt",
    "ctnMarriageAllceInAmt",
    "ctnMarriageAllceOutAmt",
    "ctnSocialInvTaxRelAmt",
    "ctnSavingsPartnership",
    "ctnDividendsPartnership",
    "giftAidTaxReduced",
    "ctnTaxableCegSr",
    "ctnTaxOnCegSr",
    "ctnTaxableRedundancySsr",
    "employeeClass1Nic",
    "employeeClass2Nic",
    "employerNic"
  )

  private val fields2024: Seq[String] = fields2023 :+ "ctnTaxOnTransitionPrft"

  def getFieldList(tax_year: Int): Action[AnyContent] = Action {
    val items = tax_year match {
      case 2021 => fields2023
      case 2022 => fields2023
      case 2023 => fields2023
      case 2024 => fields2024
      case _    => fields2024
    }
    Ok(
      Json.obj(
        "items" -> items
      )
    )
  }
}
