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
import sa.models.ODSLiabilities.ODSLiabilities.*
import sa.models.*
import sa.services.TaxRateService
import sa.transformers.ATS2025.{ATSCalculationsScottish2025, ATSCalculationsUK2025, ATSCalculationsWelsh2025}
import sa.utils.DoubleUtils

class ATSCalculationsTest extends BaseSpec with ScalaCheckPropertyChecks with DoubleUtils {

  class CalcFixtures(val taxYear: Int, val origin: Nationality)(
    pensionTaxRate: PensionTaxRate,
    newAtsData: (ODSLiabilities, Amount)*
  ) {
    self =>

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
        calculation.map(_ mustBe a[ATSCalculationsUK2025])
      }
      "country is Scotland" in {
        val calculation = new Fixture(9999, Scottish())().calculation
        calculation.isDefined mustBe true
        calculation.map(_ mustBe a[ATSCalculationsScottish2025])
      }
      "country is Wales" in {
        val calculation = new Fixture(9999, Welsh())().calculation
        calculation.isDefined mustBe true
        calculation.map(_ mustBe a[ATSCalculationsWelsh2025])
      }
    }
  }

}
