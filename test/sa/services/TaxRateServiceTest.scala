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
import sa.models.TaxRate._

class TaxRateServiceTest extends BaseSpec {
  "taxRateService" must {

    "return a value for StartingRateForSavingsRate" in {
      val expectedStartingRateForSavingsRate = Rate(12.3)

      val taxRateService =
        new TaxRateService(Map(StartingRateForSavingsRate -> expectedStartingRateForSavingsRate))

      val actualStartingRateForSavingsRate = taxRateService.taxRates.getOrElse(StartingRateForSavingsRate, Rate.empty)

      actualStartingRateForSavingsRate mustBe expectedStartingRateForSavingsRate
    }

    "return a value for BasicRateIncomeTaxRate" in {
      val expectedBasicRateIncomeTaxRate = Rate(20)

      val taxRateService =
        new TaxRateService(Map(BasicRateIncomeTaxRate -> expectedBasicRateIncomeTaxRate))

      val actualBasicRateIncomeTaxRate = taxRateService.taxRates.getOrElse(BasicRateIncomeTaxRate, Rate.empty)

      actualBasicRateIncomeTaxRate mustBe expectedBasicRateIncomeTaxRate
    }

    "return a value for HigherRateIncomeTaxRate" in {
      val expectedHigherRateIncomeTaxRate = Rate(21)

      val taxRateService =
        new TaxRateService(Map(HigherRateIncomeTaxRate -> expectedHigherRateIncomeTaxRate))

      val actualHigherRateIncomeTaxRate = taxRateService.taxRates.getOrElse(HigherRateIncomeTaxRate, Rate.empty)

      actualHigherRateIncomeTaxRate mustBe expectedHigherRateIncomeTaxRate
    }

    "return a value for AdditionalRateIncomeTaxRate" in {
      val expectedAdditionalRateIncomeTaxRate = Rate(22)

      val taxRateService =
        new TaxRateService(Map(AdditionalRateIncomeTaxRate -> expectedAdditionalRateIncomeTaxRate))

      val actualAdditionalRateIncomeTaxRate = taxRateService.taxRates.getOrElse(AdditionalRateIncomeTaxRate, Rate.empty)

      actualAdditionalRateIncomeTaxRate mustBe expectedAdditionalRateIncomeTaxRate
    }

    "return a value for dividendsOrdinaryRate" in {
      val expectedDividendsOrdinaryRate = Rate(26)

      val taxRateService = new TaxRateService(Map(DividendsOrdinaryRate -> expectedDividendsOrdinaryRate))

      val actualDividendsOrdinaryRate = taxRateService.taxRates.getOrElse(DividendsOrdinaryRate, Rate.empty)

      actualDividendsOrdinaryRate mustBe expectedDividendsOrdinaryRate
    }

    "return a value for dividendUpperRateRate" in {
      val expectedDividendUpperRateRate = Rate(27)

      val taxRateService = new TaxRateService(Map(DividendUpperRateRate -> expectedDividendUpperRateRate))

      val actualDividendUpperRateRate = taxRateService.taxRates.getOrElse(DividendUpperRateRate, Rate.empty)

      actualDividendUpperRateRate mustBe expectedDividendUpperRateRate
    }

    "return a value for dividendAdditionalRate" in {
      val expectedDividendAdditionalRate = Rate(28)

      val taxRateService =
        new TaxRateService(Map(DividendAdditionalRate -> expectedDividendAdditionalRate))

      val actualDividendAdditionalRate = taxRateService.taxRates.getOrElse(DividendAdditionalRate, Rate.empty)

      actualDividendAdditionalRate mustBe expectedDividendAdditionalRate
    }

    "return a value for cgEntrepreneursRate" in {
      val expectedCgEntrepreneursRate = Rate(29)

      val taxRateService = new TaxRateService(Map(CgEntrepreneursRate -> expectedCgEntrepreneursRate))

      val actualCgEntrepreneursRate = taxRateService.taxRates.getOrElse(CgEntrepreneursRate, Rate.empty)

      actualCgEntrepreneursRate mustBe expectedCgEntrepreneursRate
    }

    "return a value for cgOrdinaryRate" in {
      val expectedCgOrdinaryRate = Rate(30)

      val taxRateService = new TaxRateService(Map(CgOrdinaryRate -> expectedCgOrdinaryRate))

      val actualCgOrdinaryRate = taxRateService.taxRates.getOrElse(CgOrdinaryRate, Rate.empty)

      actualCgOrdinaryRate mustBe expectedCgOrdinaryRate
    }

    "return a value for CgUpperRate" in {
      val expectedCgUpperRate = Rate(31)

      val taxRateService = new TaxRateService(Map(CgUpperRate -> expectedCgUpperRate))

      val actualCgUpperRate = taxRateService.taxRates.getOrElse(CgUpperRate, Rate.empty)

      actualCgUpperRate mustBe expectedCgUpperRate
    }

    "return a value for individualsForResidentialPropertyAndCarriedInterestLowerRate" in {
      val expectedIndividualsForResidentialPropertyAndCarriedInterestLowerRate = Rate(32)

      val taxRateService = new TaxRateService(
        Map(
          IndividualsForResidentialPropertyAndCarriedInterestLowerRate -> expectedIndividualsForResidentialPropertyAndCarriedInterestLowerRate
        )
      )

      val actualIndividualsForResidentialPropertyAndCarriedInterestLowerRate =
        taxRateService.taxRates.getOrElse(IndividualsForResidentialPropertyAndCarriedInterestLowerRate, Rate.empty)

      actualIndividualsForResidentialPropertyAndCarriedInterestLowerRate mustBe expectedIndividualsForResidentialPropertyAndCarriedInterestLowerRate
    }

    "return a value for individualsForResidentialPropertyAndCarriedInterestHigherRate" in {
      val expectedIndividualsForResidentialPropertyAndCarriedInterestHigherRate = Rate(33)

      val taxRateService = new TaxRateService(
        Map(
          IndividualsForResidentialPropertyAndCarriedInterestHigherRate -> expectedIndividualsForResidentialPropertyAndCarriedInterestHigherRate
        )
      )

      val actualIndividualsForResidentialPropertyAndCarriedInterestHigherRate =
        taxRateService.taxRates.getOrElse(IndividualsForResidentialPropertyAndCarriedInterestHigherRate, Rate.empty)

      actualIndividualsForResidentialPropertyAndCarriedInterestHigherRate mustBe expectedIndividualsForResidentialPropertyAndCarriedInterestHigherRate

    }

    "return empty for all values" in {
      val taxRateService = new TaxRateService(Map.empty)

      val actualStartingRateForSavingsRate                                    = taxRateService.taxRates.getOrElse(StartingRateForSavingsRate, Rate.empty)
      val actualBasicRateIncomeTaxRate                                        = taxRateService.taxRates.getOrElse(BasicRateIncomeTaxRate, Rate.empty)
      val actualHigherRateIncomeTaxRate                                       = taxRateService.taxRates.getOrElse(HigherRateIncomeTaxRate, Rate.empty)
      val actualAdditionalRateIncomeTaxRate                                   = taxRateService.taxRates.getOrElse(AdditionalRateIncomeTaxRate, Rate.empty)
      val actualDividendsOrdinaryRate                                         = taxRateService.taxRates.getOrElse(DividendsOrdinaryRate, Rate.empty)
      val actualDividendUpperRateRate                                         = taxRateService.taxRates.getOrElse(DividendUpperRateRate, Rate.empty)
      val actualDividendAdditionalRate                                        = taxRateService.taxRates.getOrElse(DividendAdditionalRate, Rate.empty)
      val actualCgEntrepreneursRate                                           = taxRateService.taxRates.getOrElse(CgEntrepreneursRate, Rate.empty)
      val actualCgOrdinaryRate                                                = taxRateService.taxRates.getOrElse(CgOrdinaryRate, Rate.empty)
      val actualCgUpperRate                                                   = taxRateService.taxRates.getOrElse(CgUpperRate, Rate.empty)
      val actualIndividualsForResidentialPropertyAndCarriedInterestLowerRate  =
        taxRateService.taxRates.getOrElse(IndividualsForResidentialPropertyAndCarriedInterestLowerRate, Rate.empty)
      val actualIndividualsForResidentialPropertyAndCarriedInterestHigherRate =
        taxRateService.taxRates.getOrElse(IndividualsForResidentialPropertyAndCarriedInterestHigherRate, Rate.empty)

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
