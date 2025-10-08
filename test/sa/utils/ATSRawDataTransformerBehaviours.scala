/*
 * Copyright 2024 HM Revenue & Customs
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

package sa.utils

import common.utils.BaseSpec
import sa.models.AtsMiddleTierData

trait ATSRawDataTransformerBehaviours extends BaseSpec {
  protected def atsRawDataTransformerWithCalculations(
    description: String,
    testFixture: AtsRawDataTransformerTestFixture
  ): Unit =
    Seq(
      ("income tax", testFixture.transformedData.income_tax, testFixture.expectedResultIncomeTax),
      ("income data", testFixture.transformedData.income_data, testFixture.expectedResultIncomeData),
      ("cap gains data", testFixture.transformedData.capital_gains_data, testFixture.expectedResultCapitalGainsData),
      ("allowance data", testFixture.transformedData.allowance_data, testFixture.expectedResultAllowanceData),
      ("summary data", testFixture.transformedData.summary_data, testFixture.expectedResultSummaryData)
    ).foreach { case (section, actualOptDataHolder, exp) =>
      val act = actualOptDataHolder.flatMap(_.payload).getOrElse(Map.empty)
      if (act.exists(liabilityKey => exp.exists(_._1 == liabilityKey._1))) {
        s"(for $section section) calculate for $description" when {
          act.foreach { item =>
            exp.find(_._1 == item._1).map { actItem =>
              s"field ${item._1} calculated (act ${actItem._2.amount}, exp ${item._2.amount})" in {
                item._2 mustBe actItem._2
              }
            }
          }

          "check for missing keys made" in {
            exp.keys.toSeq.diff(act.keys.toSeq) mustBe Nil
          }
        }
      }
    }

  protected def atsRawDataTransformerWithTotalTaxLiabilityChecks(
    expTotalTaxLiabilityValue: BigDecimal,
    testFixture: AtsRawDataTransformerTestFixture
  ): Unit =
    "have total tax liability" in {
      val transformedData: AtsMiddleTierData =
        testFixture.doTest(testFixture.buildJsonPayload())
      transformedData.taxLiability.map(_.amount) mustBe Some(expTotalTaxLiabilityValue)
    }
}
