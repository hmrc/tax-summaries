/*
 * Copyright 2017 HM Revenue & Customs
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

package services

object GovSpendService {

  def govSpending(taxYear: Int): Map[String, BigDecimal] = taxYear match {
    case 2014 => taxYear2014
    case 2015 => taxYear2015
    case 2016 => taxYear2016
    case 2017 => taxYear2017
  }

  val taxYear2014: Map[String, BigDecimal] = Map(
    "Welfare" -> 24.52,
    "Health" -> 18.87,
    "Education" -> 13.15,
    "StatePensions" -> 12.12,
    "NationalDebtInterest" -> 7.00,
    "Defence" -> 5.31,
    "CriminalJustice" -> 4.40,
    "Transport" -> 2.95,
    "BusinessAndIndustry" -> 2.74,
    "GovernmentAdministration" -> 2.05,
    "Culture" -> 1.69,
    "Environment" -> 1.66,
    "HousingAndUtilities" -> 1.64,
    "OverseasAid" -> 1.15,
    "UkContributionToEuBudget" -> 0.75
  )

  val taxYear2015: Map[String, BigDecimal] = Map(
    "Welfare" -> 25.30,
    "Health" -> 19.90,
    "StatePensions" -> 12.80,
    "Education" -> 12.50,
    "Defence" -> 5.40,
    "NationalDebtInterest" -> 5.00,
    "PublicOrderAndSafety" -> 4.40,
    "Transport" -> 3.00,
    "BusinessAndIndustry" -> 2.70,
    "GovernmentAdministration" -> 2.00,
    "Culture" -> 1.80,
    "Environment" -> 1.70,
    "HousingAndUtilities" -> 1.60,
    "OverseasAid" -> 1.30,
    "UkContributionToEuBudget" -> 0.60
  )

  val taxYear2016: Map[String, BigDecimal] = Map(
    "Welfare" -> 25.00,
    "Health" -> 19.90,
    "StatePensions" -> 12.80,
    "Education" -> 12.00,
    "Defence" -> 5.20,
    "NationalDebtInterest" -> 5.30,
    "PublicOrderAndSafety" -> 4.30,
    "Transport" -> 4.00,
    "BusinessAndIndustry" -> 2.40,
    "GovernmentAdministration" -> 2.00,
    "Culture" -> 1.60,
    "Environment" -> 1.70,
    "HousingAndUtilities" -> 1.40,
    "OverseasAid" -> 1.20,
    "UkContributionToEuBudget" -> 1.10
  )

  val taxYear2017: Map[String, BigDecimal] = Map(
    "Welfare" -> 24.30,
    "Health" -> 20.30,
    "StatePensions" -> 12.90,
    "Education" -> 12.30,
    "Defence" -> 5.20,
    "NationalDebtInterest" -> 5.50,
    "PublicOrderAndSafety" -> 4.20,
    "Transport" -> 4.20,
    "BusinessAndIndustry" -> 2.50,
    "GovernmentAdministration" -> 2.10,
    "Culture" -> 1.60,
    "Environment" -> 1.60,
    "HousingAndUtilities" -> 1.50,
    "OverseasAid" -> 1.10,
    "UkContributionToEuBudget" -> 0.70
  )

}
