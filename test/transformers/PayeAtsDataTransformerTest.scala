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

import models.LiabilityKey.{MarriageAllowanceTransferredAmount, OtherAllowancesAmount, PersonalTaxFreeAmount, TotalTaxFreeAmount, UpperRate}
import models.{Amount, DataHolder, LiabilityKey}
import models.paye.{PayeAtsData, PayeAtsMiddeTier}
import org.scalatestplus.play.OneAppPerSuite
import uk.gov.hmrc.play.test.UnitSpec
import utils.{PayeAtsDataUtil, TestConstants}

class PayeAtsDataTransformerTest extends UnitSpec with OneAppPerSuite {

  implicit def toGbpAmount(i: Double): Amount = Amount.gbp(i)

  val atsData: PayeAtsData = PayeAtsDataUtil.atsData
  val nino: String = TestConstants.testNino
  //TODO bring in taxyear domain model
  val taxYear = 2020
  lazy val transformedData: PayeAtsMiddeTier =
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
        TotalTaxFreeAmount                 -> 25500.00
      )

      payload should contain theSameElementsAs expectedValues
      allowanceData.incomeTaxStatus shouldBe None
      allowanceData.rates shouldBe None
    }
  }

}
