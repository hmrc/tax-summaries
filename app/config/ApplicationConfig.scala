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

package config

import com.google.inject.Inject
import play.api.Mode.Mode
import play.api.{Configuration, Environment}
import uk.gov.hmrc.play.config.ServicesConfig

import scala.collection.JavaConverters._

class ApplicationConfig @Inject()(override val runModeConfiguration: Configuration, playEnvironment: Environment)
    extends ServicesConfig {

  override protected def mode: Mode = playEnvironment.mode

  private def defaultRatePercentages: Map[String, Double] =
    runModeConfiguration
      .getObject("taxRates.default.percentages")
      .map(_.unwrapped().asScala.mapValues(_.toString.toDouble))
      .getOrElse(Map())
      .toMap

  private def ratePercentagesByYear(year: Int): Map[String, Double] =
    runModeConfiguration
      .getObject(s"taxRates.$year.percentages")
      .map(_.unwrapped().asScala.mapValues(_.toString.toDouble))
      .getOrElse(Map())
      .toMap

  private def governmentSpendByYear(year: Int): Map[String, Double] =
    runModeConfiguration
      .getObject(s"governmentSpend.$year.percentages")
      .map(_.unwrapped().asScala.mapValues(_.toString.toDouble))
      .getOrElse(Map())
      .toMap

  def ratePercentages(year: Int) = defaultRatePercentages ++ ratePercentagesByYear(year)

  def governmentSpend(year: Int) = governmentSpendByYear(year)

  def npsServiceUrl = baseUrl("tax-summaries-hod")

  lazy val environment: String =
    runModeConfiguration.getString(s"$rootServices.tax-summaries-hod.env").getOrElse("local")

  lazy val authorization: String = "Bearer " + runModeConfiguration
    .getString(s"$rootServices.tax-summaries-hod.authorizationToken")
    .getOrElse("local")

  lazy val originatorId: String =
    runModeConfiguration.getString(s"$rootServices.tax-summaries-hod.originatorId").getOrElse("local")

}
