/*
 * Copyright 2020 HM Revenue & Customs
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

import models.LiabilityKey._
import models.RateKey.{IncomeBasic, IncomeHigher, NICS, Ordinary, Upper}
import models._
import models.paye.{PayeAtsData, PayeAtsMiddleTier}
import org.scalatestplus.play.OneAppPerSuite
import uk.gov.hmrc.play.test.UnitSpec
import utils.{PayeAtsDataUtil, TestConstants}

class PayeAtsDataTransformerTest extends UnitSpec with OneAppPerSuite {

  implicit def toGbpAmount(i: Double): Amount = Amount.gbp(i)

  val atsData: PayeAtsData = PayeAtsDataUtil.atsData
  val nino: String = TestConstants.testNino
  //TODO bring in taxyear domain model
  val taxYear = 2020
  lazy val transformedData: PayeAtsMiddleTier =
    new PayeAtsDataTransformer(nino, taxYear, atsData).transformToPayeMiddleTier

  "transformToPayeMiddleTier" should {
    "populate the nino and tax year" in {
      transformedData.nino shouldBe nino
      transformedData.taxYear shouldBe taxYear
    }

    "create allowance data" in {
      val allowanceData: DataHolder =
        transformedData.allowance_data.getOrElse(fail("No allowance data"))
      val payload: Map[LiabilityKey, Amount] =
        allowanceData.payload.getOrElse(fail("No payload for allowance data"))

      val expectedValues: Map[LiabilityKey, Amount] = Map(
        PersonalTaxFreeAmount              -> 12500.00,
        MarriageAllowanceTransferredAmount -> 1250.00,
        OtherAllowancesAmount              -> 6000.00,
        TotalTaxFreeAmount                 -> 25500.00,
        TotalIncomeBeforeTax               -> 28000.00
      )

      payload should contain theSameElementsAs expectedValues
      allowanceData.incomeTaxStatus shouldBe None
      allowanceData.rates shouldBe None

    }

    "create income data" in {
      val incomeData: DataHolder =
        transformedData.income_data.getOrElse(fail("No income data"))
      val payload: Map[LiabilityKey, Amount] =
        incomeData.payload.getOrElse(fail("No payload for income data"))

      val expectedValues: Map[LiabilityKey, Amount] = Map(
        IncomeFromEmployment   -> 25000.00,
        StatePension           -> 1000.00,
        OtherPensionIncome     -> 500.00,
        OtherIncome            -> 3000.00,
        TotalIncomeBeforeTax   -> 28000.00,
        BenefitsFromEmployment -> 200.00,
        TaxableStateBenefits   -> 500.00
      )

      payload should contain theSameElementsAs expectedValues
      incomeData.incomeTaxStatus shouldBe None
      incomeData.rates shouldBe None
    }

    "create summary data" in {
      val summaryData: DataHolder =
        transformedData.summary_data.getOrElse(fail("No summary data"))
      val payload: Map[LiabilityKey, Amount] =
        summaryData.payload.getOrElse(fail("No payload for summary data"))
      val rates: Map[RateKey, ApiRate] =
        summaryData.rates.getOrElse(fail("No rates for summary data"))

      val expectedValues: Map[LiabilityKey, Amount] = Map(
        TotalIncomeBeforeTax  -> 28000.00,
        TotalTaxFreeAmount    -> 25500.00,
        TotalIncomeTaxAndNics -> 4200.00,
        IncomeAfterTaxAndNics -> 5000.00,
        TotalIncomeTax        -> 4010.00,
        EmployeeNicAmount     -> 200.00,
        EmployerNicAmount     -> 100.00
      )
      payload should contain theSameElementsAs expectedValues
      summaryData.incomeTaxStatus shouldBe None
      rates shouldBe Map(NICS -> ApiRate("25%"))
    }

    "create income tax" in {
      val incomeTax: DataHolder =
        transformedData.income_tax.getOrElse(fail("No income tax data"))
      val payload: Map[LiabilityKey, Amount] =
        incomeTax.payload.getOrElse(fail("No payload for income tax data"))
      val rates: Map[RateKey, ApiRate] =
        incomeTax.rates.getOrElse(fail("No rates for income tax data"))

      val expectedPayloadValues: Map[LiabilityKey, Amount] = Map(
        BasicRateIncomeTaxAmount        -> 2000.00,
        BasicRateIncomeTax              -> 10000.00,
        HigherRateIncomeTaxAmount       -> 2000.00,
        HigherRateIncomeTax             -> 10000.00,
        OrdinaryRateAmount              -> 200.00,
        OrdinaryRate                    -> 2000.00,
        UpperRateAmount                 -> 200.00,
        UpperRate                       -> 2000.00,
        MarriedCouplesAllowance         -> 500.00,
        MarriageAllowanceReceivedAmount -> 1250.00,
        LessTaxAdjustmentPrevYear       -> 200.00,
        TaxUnderpaidPrevYear            -> 200.00,
        TotalIncomeTax                  -> 4000.00
      )

      val expectedRatesValues: Map[RateKey, ApiRate] = Map(
        Ordinary     -> ApiRate("7.5%"),
        IncomeHigher -> ApiRate("40%"),
        IncomeBasic  -> ApiRate("20%"),
        Upper        -> ApiRate("32.5%")
      )

      payload should contain theSameElementsAs expectedPayloadValues
      rates should contain theSameElementsAs expectedRatesValues
    }
  }
}
