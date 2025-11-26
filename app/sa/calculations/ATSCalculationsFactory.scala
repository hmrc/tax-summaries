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

package sa.calculations

import common.config.ApplicationConfig
import common.models.Rate
import sa.calculations.ATS2021.{ATSCalculationsScottish2021, ATSCalculationsUK2021, ATSCalculationsWelsh2021}
import sa.calculations.ATS2022.{ATSCalculationsScottish2022, ATSCalculationsUK2022, ATSCalculationsWelsh2022}
import sa.calculations.ATS2023.{ATSCalculationsScottish2023, ATSCalculationsUK2023, ATSCalculationsWelsh2023}
import sa.calculations.ATS2024.{ATSCalculationsScottish2024, ATSCalculationsUK2024, ATSCalculationsWelsh2024}
import sa.calculations.ATS2025.{ATSCalculationsScottish2025, ATSCalculationsUK2025, ATSCalculationsWelsh2025}
import sa.models.*

import javax.inject.{Inject, Singleton}

@Singleton
class ATSCalculationsFactory @Inject() (applicationConfig: ApplicationConfig) {
  private val calculationsForNationalityAndYear
    : Map[(Nationality, Int), (SelfAssessmentAPIResponse, Map[String, Rate]) => ATSCalculations] = {
    val uk       = UK()
    val scotland = Scottish()
    val wales    = Welsh()

    val factoryFor2025UK       = new ATSCalculationsUK2025(_, _)
    val factoryFor2025Scotland = new ATSCalculationsScottish2025(_, _)
    val factoryFor2025Wales    = new ATSCalculationsWelsh2025(_, _)

    val factoryFor2024UK       = new ATSCalculationsUK2024(_, _)
    val factoryFor2024Scotland = new ATSCalculationsScottish2024(_, _)
    val factoryFor2024Wales    = new ATSCalculationsWelsh2024(_, _)

    val factoryFor2023UK       = new ATSCalculationsUK2023(_, _)
    val factoryFor2023Scotland = new ATSCalculationsScottish2023(_, _)
    val factoryFor2023Wales    = new ATSCalculationsWelsh2023(_, _)
    val factoryFor2022UK       = new ATSCalculationsUK2022(_, _)
    val factoryFor2022Scotland = new ATSCalculationsScottish2022(_, _)
    val factoryFor2022Wales    = new ATSCalculationsWelsh2022(_, _)
    val factoryFor2021UK       = new ATSCalculationsUK2021(_, _)
    val factoryFor2021Scotland = new ATSCalculationsScottish2021(_, _)
    val factoryFor2021Wales    = new ATSCalculationsWelsh2021(_, _)

    Map(
      (uk, 2025)       -> factoryFor2025UK,
      (scotland, 2025) -> factoryFor2025Scotland,
      (wales, 2025)    -> factoryFor2025Wales,
      (uk, 2024)       -> factoryFor2024UK,
      (scotland, 2024) -> factoryFor2024Scotland,
      (wales, 2024)    -> factoryFor2024Wales,
      (uk, 2023)       -> factoryFor2023UK,
      (scotland, 2023) -> factoryFor2023Scotland,
      (wales, 2023)    -> factoryFor2023Wales,
      (uk, 2022)       -> factoryFor2022UK,
      (scotland, 2022) -> factoryFor2022Scotland,
      (wales, 2022)    -> factoryFor2022Wales,
      (uk, 2021)       -> factoryFor2021UK,
      (scotland, 2021) -> factoryFor2021Scotland,
      (wales, 2021)    -> factoryFor2021Wales
    )
  }

  // Create (if available) an ATSCalculations instance appropriate for the tax year and country found in the API response.
  def apply(selfAssessmentAPIResponse: SelfAssessmentAPIResponse): Option[ATSCalculations] = {
    val taxYear: Int                          = selfAssessmentAPIResponse.taxYear
    val nationality: Nationality              = selfAssessmentAPIResponse.nationality
    def taxRatesForTaxYear: Map[String, Rate] = applicationConfig.taxRates(taxYear)
    calculationsForNationalityAndYear.get((nationality, taxYear)) match {
      case Some(factoryForNationalityAndYear) =>
        Some(factoryForNationalityAndYear(selfAssessmentAPIResponse, taxRatesForTaxYear))
      case None                               =>
        val maxDefinedYearForCountry = calculationsForNationalityAndYear.keys
          .filter(_._1 == nationality)
          .map(_._2)
          .max
        if (selfAssessmentAPIResponse.taxYear > maxDefinedYearForCountry) {
          Some(
            calculationsForNationalityAndYear((nationality, maxDefinedYearForCountry))(
              selfAssessmentAPIResponse,
              taxRatesForTaxYear
            )
          )
        } else {
          None
        }
    }
  }
}
