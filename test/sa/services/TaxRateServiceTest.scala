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
      val expectedStartingRateForSavingsRate = 12.3

      val taxRate =
        new TaxRateService(2000, _ => Map("startingRateForSavingsRate" -> expectedStartingRateForSavingsRate))

      val actualStartingRateForSavingsRate = taxRate.startingRateForSavingsRate()

      actualStartingRateForSavingsRate mustBe Rate(expectedStartingRateForSavingsRate)
    }

    "return a value for BasicRateIncomeTaxRate" in {
      val expectedBasicRateIncomeTaxRate = 20

      val taxRate = new TaxRateService(2000, _ => Map("basicRateIncomeTaxRate" -> expectedBasicRateIncomeTaxRate))

      val actualBasicRateIncomeTaxRate = taxRate.basicRateIncomeTaxRate()

      actualBasicRateIncomeTaxRate mustBe Rate(expectedBasicRateIncomeTaxRate)
    }

    "return a value for HigherRateIncomeTaxRate" in {
      val expectedHigherRateIncomeTaxRate = 21

      val taxRate = new TaxRateService(2000, _ => Map("higherRateIncomeTaxRate" -> expectedHigherRateIncomeTaxRate))

      val actualHigherRateIncomeTaxRate = taxRate.higherRateIncomeTaxRate()

      actualHigherRateIncomeTaxRate mustBe Rate(expectedHigherRateIncomeTaxRate)
    }

    "return a value for AdditionalRateIncomeTaxRate" in {
      val expectedAdditionalRateIncomeTaxRate = 22

      val taxRate =
        new TaxRateService(2000, _ => Map("additionalRateIncomeTaxRate" -> expectedAdditionalRateIncomeTaxRate))

      val actualAdditionalRateIncomeTaxRate = taxRate.additionalRateIncomeTaxRate()

      actualAdditionalRateIncomeTaxRate mustBe Rate(expectedAdditionalRateIncomeTaxRate)
    }

    "return a value for dividendsOrdinaryRate" in {
      val expectedDividendsOrdinaryRate = 26

      val taxRate = new TaxRateService(2000, _ => Map("dividendsOrdinaryRate" -> expectedDividendsOrdinaryRate))

      val actualDividendsOrdinaryRate = taxRate.dividendsOrdinaryRate()

      actualDividendsOrdinaryRate mustBe Rate(expectedDividendsOrdinaryRate)
    }

    "return a value for dividendUpperRateRate" in {
      val expectedDividendUpperRateRate = 27

      val taxRate = new TaxRateService(2000, _ => Map("dividendUpperRateRate" -> expectedDividendUpperRateRate))

      val actualDividendUpperRateRate = taxRate.dividendUpperRateRate()

      actualDividendUpperRateRate mustBe Rate(expectedDividendUpperRateRate)
    }

    "return a value for dividendAdditionalRate" in {
      val expectedDividendAdditionalRate = 28

      val taxRate = new TaxRateService(2000, _ => Map("dividendAdditionalRate" -> expectedDividendAdditionalRate))

      val actualDividendAdditionalRate = taxRate.dividendAdditionalRate()

      actualDividendAdditionalRate mustBe Rate(expectedDividendAdditionalRate)
    }

    "return a value for cgEntrepreneursRate" in {
      val expectedCgEntrepreneursRate = 29

      val taxRate = new TaxRateService(2000, _ => Map("cgEntrepreneursRate" -> expectedCgEntrepreneursRate))

      val actualCgEntrepreneursRate = taxRate.cgEntrepreneursRate()

      actualCgEntrepreneursRate mustBe Rate(expectedCgEntrepreneursRate)
    }

    "return a value for cgOrdinaryRate" in {
      val expectedCgOrdinaryRate = 30

      val taxRate = new TaxRateService(2000, _ => Map("cgOrdinaryRate" -> expectedCgOrdinaryRate))

      val actualCgOrdinaryRate = taxRate.cgOrdinaryRate()

      actualCgOrdinaryRate mustBe Rate(expectedCgOrdinaryRate)
    }

    "return a value for CgUpperRate" in {
      val expectedCgUpperRate = 31

      val taxRate = new TaxRateService(2000, _ => Map("cgUpperRate" -> expectedCgUpperRate))

      val actualCgUpperRate = taxRate.cgUpperRate()

      actualCgUpperRate mustBe Rate(expectedCgUpperRate)
    }

    "return a value for individualsForResidentialPropertyAndCarriedInterestLowerRate" in {
      val expectedIndividualsForResidentialPropertyAndCarriedInterestLowerRate = 32

      val taxRate = new TaxRateService(
        2000,
        _ =>
          Map(
            "RPCILowerRate" -> expectedIndividualsForResidentialPropertyAndCarriedInterestLowerRate
          )
      )

      val actualIndividualsForResidentialPropertyAndCarriedInterestLowerRate =
        taxRate.individualsForResidentialPropertyAndCarriedInterestLowerRate()

      actualIndividualsForResidentialPropertyAndCarriedInterestLowerRate mustBe Rate(
        expectedIndividualsForResidentialPropertyAndCarriedInterestLowerRate
      )
    }

    "return a value for individualsForResidentialPropertyAndCarriedInterestHigherRate" in {
      val expectedIndividualsForResidentialPropertyAndCarriedInterestHigherRate = 33

      val taxRate = new TaxRateService(
        2000,
        _ =>
          Map(
            "RPCIHigherRate" -> expectedIndividualsForResidentialPropertyAndCarriedInterestHigherRate
          )
      )

      val actualIndividualsForResidentialPropertyAndCarriedInterestHigherRate =
        taxRate.individualsForResidentialPropertyAndCarriedInterestHigherRate()

      actualIndividualsForResidentialPropertyAndCarriedInterestHigherRate mustBe Rate(
        expectedIndividualsForResidentialPropertyAndCarriedInterestHigherRate
      )
    }

    "return empty for all values" in {
      val taxRate = new TaxRateService(2000, _ => Map.empty)

      val actualStartingRateForSavingsRate                                    = taxRate.startingRateForSavingsRate()
      val actualBasicRateIncomeTaxRate                                        = taxRate.basicRateIncomeTaxRate()
      val actualHigherRateIncomeTaxRate                                       = taxRate.higherRateIncomeTaxRate()
      val actualAdditionalRateIncomeTaxRate                                   = taxRate.additionalRateIncomeTaxRate()
      val actualDividendsOrdinaryRate                                         = taxRate.dividendsOrdinaryRate()
      val actualDividendUpperRateRate                                         = taxRate.dividendUpperRateRate()
      val actualDividendAdditionalRate                                        = taxRate.dividendAdditionalRate()
      val actualCgEntrepreneursRate                                           = taxRate.cgEntrepreneursRate()
      val actualCgOrdinaryRate                                                = taxRate.cgOrdinaryRate()
      val actualCgUpperRate                                                   = taxRate.cgUpperRate()
      val actualIndividualsForResidentialPropertyAndCarriedInterestLowerRate  =
        taxRate.individualsForResidentialPropertyAndCarriedInterestLowerRate()
      val actualIndividualsForResidentialPropertyAndCarriedInterestHigherRate =
        taxRate.individualsForResidentialPropertyAndCarriedInterestHigherRate()

      actualStartingRateForSavingsRate mustBe Rate(Rate.empty)
      actualBasicRateIncomeTaxRate mustBe Rate(Rate.empty)
      actualHigherRateIncomeTaxRate mustBe Rate(Rate.empty)
      actualAdditionalRateIncomeTaxRate mustBe Rate(Rate.empty)
      actualDividendsOrdinaryRate mustBe Rate(Rate.empty)
      actualDividendUpperRateRate mustBe Rate(Rate.empty)
      actualDividendAdditionalRate mustBe Rate(Rate.empty)
      actualCgEntrepreneursRate mustBe Rate(Rate.empty)
      actualCgOrdinaryRate mustBe Rate(Rate.empty)
      actualCgUpperRate mustBe Rate(Rate.empty)
      actualIndividualsForResidentialPropertyAndCarriedInterestLowerRate mustBe Rate(Rate.empty)
      actualIndividualsForResidentialPropertyAndCarriedInterestHigherRate mustBe Rate(Rate.empty)

    }
  }
}
