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

package sa.services

import common.models.Rate
import common.utils.BaseSpec

class TaxRateServiceTest extends BaseSpec {
  "taxRateService" must {

    "return a value for StartingRateForSavingsRate" in {
      val expectedStartingRateForSavingsRate = Rate(12.3)

      val taxRateService =
        new TaxRateService(Map("startingRateForSavingsRate" -> expectedStartingRateForSavingsRate))

      val actualStartingRateForSavingsRate = taxRateService.startingRateForSavingsRate

      actualStartingRateForSavingsRate mustBe expectedStartingRateForSavingsRate
    }

    "return a value for BasicRateIncomeTaxRate" in {
      val expectedBasicRateIncomeTaxRate = Rate(20)

      val taxRateService =
        new TaxRateService(Map("basicRateIncomeTaxRate" -> expectedBasicRateIncomeTaxRate))

      val actualBasicRateIncomeTaxRate = taxRateService.basicRateIncomeTaxRate

      actualBasicRateIncomeTaxRate mustBe expectedBasicRateIncomeTaxRate
    }

    "return a value for HigherRateIncomeTaxRate" in {
      val expectedHigherRateIncomeTaxRate = Rate(21)

      val taxRateService =
        new TaxRateService(Map("higherRateIncomeTaxRate" -> expectedHigherRateIncomeTaxRate))

      val actualHigherRateIncomeTaxRate = taxRateService.higherRateIncomeTaxRate

      actualHigherRateIncomeTaxRate mustBe expectedHigherRateIncomeTaxRate
    }

    "return a value for AdditionalRateIncomeTaxRate" in {
      val expectedAdditionalRateIncomeTaxRate = Rate(22)

      val taxRateService =
        new TaxRateService(Map("additionalRateIncomeTaxRate" -> expectedAdditionalRateIncomeTaxRate))

      val actualAdditionalRateIncomeTaxRate = taxRateService.additionalRateIncomeTaxRate

      actualAdditionalRateIncomeTaxRate mustBe expectedAdditionalRateIncomeTaxRate
    }

    "return a value for dividendsOrdinaryRate" in {
      val expectedDividendsOrdinaryRate = Rate(26)

      val taxRateService = new TaxRateService(Map("dividendsOrdinaryRate" -> expectedDividendsOrdinaryRate))

      val actualDividendsOrdinaryRate = taxRateService.dividendsOrdinaryRate

      actualDividendsOrdinaryRate mustBe expectedDividendsOrdinaryRate
    }

    "return a value for dividendUpperRateRate" in {
      val expectedDividendUpperRateRate = Rate(27)

      val taxRateService = new TaxRateService(Map("dividendUpperRateRate" -> expectedDividendUpperRateRate))

      val actualDividendUpperRateRate = taxRateService.dividendUpperRateRate

      actualDividendUpperRateRate mustBe expectedDividendUpperRateRate
    }

    "return a value for dividendAdditionalRate" in {
      val expectedDividendAdditionalRate = Rate(28)

      val taxRateService =
        new TaxRateService(Map("dividendAdditionalRate" -> expectedDividendAdditionalRate))

      val actualDividendAdditionalRate = taxRateService.dividendAdditionalRate

      actualDividendAdditionalRate mustBe expectedDividendAdditionalRate
    }

    "return a value for cgEntrepreneursRate" in {
      val expectedCgEntrepreneursRate = Rate(29)

      val taxRateService = new TaxRateService(Map("cgEntrepreneursRate" -> expectedCgEntrepreneursRate))

      val actualCgEntrepreneursRate = taxRateService.cgEntrepreneursRate

      actualCgEntrepreneursRate mustBe expectedCgEntrepreneursRate
    }

    "return a value for cgOrdinaryRate" in {
      val expectedCgOrdinaryRate = Rate(30)

      val taxRateService = new TaxRateService(Map("cgOrdinaryRate" -> expectedCgOrdinaryRate))

      val actualCgOrdinaryRate = taxRateService.cgOrdinaryRate

      actualCgOrdinaryRate mustBe expectedCgOrdinaryRate
    }

    "return a value for CgUpperRate" in {
      val expectedCgUpperRate = Rate(31)

      val taxRateService = new TaxRateService(Map("cgUpperRate" -> expectedCgUpperRate))

      val actualCgUpperRate = taxRateService.cgUpperRate

      actualCgUpperRate mustBe expectedCgUpperRate
    }

    "return a value for individualsForResidentialPropertyAndCarriedInterestLowerRate" in {
      val expectedIndividualsForResidentialPropertyAndCarriedInterestLowerRate = Rate(32)

      val taxRateService = new TaxRateService(
        Map(
          "RPCILowerRate" -> expectedIndividualsForResidentialPropertyAndCarriedInterestLowerRate
        )
      )

      val actualIndividualsForResidentialPropertyAndCarriedInterestLowerRate =
        taxRateService.individualsForResidentialPropertyAndCarriedInterestLowerRate

      actualIndividualsForResidentialPropertyAndCarriedInterestLowerRate mustBe expectedIndividualsForResidentialPropertyAndCarriedInterestLowerRate
    }

    "return a value for individualsForResidentialPropertyAndCarriedInterestHigherRate" in {
      val expectedIndividualsForResidentialPropertyAndCarriedInterestHigherRate = Rate(33)

      val taxRateService = new TaxRateService(
        Map(
          "RPCIHigherRate" -> expectedIndividualsForResidentialPropertyAndCarriedInterestHigherRate
        )
      )

      val actualIndividualsForResidentialPropertyAndCarriedInterestHigherRate =
        taxRateService.individualsForResidentialPropertyAndCarriedInterestHigherRate

      actualIndividualsForResidentialPropertyAndCarriedInterestHigherRate mustBe expectedIndividualsForResidentialPropertyAndCarriedInterestHigherRate

    }

    "return empty for all values" in {
      val taxRateService = new TaxRateService(Map.empty)

      val actualStartingRateForSavingsRate                                    = taxRateService.startingRateForSavingsRate
      val actualBasicRateIncomeTaxRate                                        = taxRateService.basicRateIncomeTaxRate
      val actualHigherRateIncomeTaxRate                                       = taxRateService.higherRateIncomeTaxRate
      val actualAdditionalRateIncomeTaxRate                                   = taxRateService.additionalRateIncomeTaxRate
      val actualDividendsOrdinaryRate                                         = taxRateService.dividendsOrdinaryRate
      val actualDividendUpperRateRate                                         = taxRateService.dividendUpperRateRate
      val actualDividendAdditionalRate                                        = taxRateService.dividendAdditionalRate
      val actualCgEntrepreneursRate                                           = taxRateService.cgEntrepreneursRate
      val actualCgOrdinaryRate                                                = taxRateService.cgOrdinaryRate
      val actualCgUpperRate                                                   = taxRateService.cgUpperRate
      val actualIndividualsForResidentialPropertyAndCarriedInterestLowerRate  =
        taxRateService.individualsForResidentialPropertyAndCarriedInterestLowerRate
      val actualIndividualsForResidentialPropertyAndCarriedInterestHigherRate =
        taxRateService.individualsForResidentialPropertyAndCarriedInterestHigherRate

      actualStartingRateForSavingsRate mustBe Rate.empty
      actualBasicRateIncomeTaxRate mustBe Rate.empty
      actualHigherRateIncomeTaxRate mustBe Rate.empty
      actualAdditionalRateIncomeTaxRate mustBe Rate.empty
      actualDividendsOrdinaryRate mustBe Rate.empty
      actualDividendUpperRateRate mustBe Rate.empty
      actualDividendAdditionalRate mustBe Rate.empty
      actualCgEntrepreneursRate mustBe Rate.empty
      actualCgOrdinaryRate mustBe Rate.empty
      actualCgUpperRate mustBe Rate.empty
      actualIndividualsForResidentialPropertyAndCarriedInterestLowerRate mustBe Rate.empty
      actualIndividualsForResidentialPropertyAndCarriedInterestHigherRate mustBe Rate.empty

    }
  }
}
