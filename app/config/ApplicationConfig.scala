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

package config

import com.google.inject.Inject
import com.typesafe.config.ConfigObject
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.sql.Timestamp
import java.time.{Instant, LocalDateTime}
import scala.jdk.CollectionConverters._

class ApplicationConfig @Inject() (servicesConfig: ServicesConfig, configuration: Configuration) {

  private def defaultRatePercentages: Map[String, Double] =
    configuration
      .getOptional[ConfigObject]("taxRates.default.percentages")
      .map(_.unwrapped().asScala.view.mapValues(_.toString.toDouble))
      .getOrElse(Map())
      .toMap

  private def ratePercentagesByYear(year: Int): Map[String, Double] =
    configuration
      .getOptional[ConfigObject](s"taxRates.$year.percentages")
      .map(_.unwrapped().asScala.view.mapValues(_.toString.toDouble))
      .getOrElse(Map())
      .toMap

  private def governmentSpendByYear(year: Int): Map[String, Double] =
    configuration
      .getOptional[ConfigObject](s"governmentSpend.$year.percentages")
      .map(_.unwrapped().asScala.view.mapValues(_.toString.toDouble))
      .getOrElse(Map())
      .toMap

  def ratePercentages(year: Int): Map[String, Double] = defaultRatePercentages ++ ratePercentagesByYear(year)

  def governmentSpend(year: Int): Map[String, Double] = governmentSpendByYear(year)

  def npsServiceUrl: String = servicesConfig.baseUrl("tax-summaries-hod")

  lazy val mongoTTL: Long = configuration.getOptional[Int]("mongodb.timeToLiveInMinutes").getOrElse(15).toLong

  def calculateExpiryTime(): Instant = Timestamp.valueOf(LocalDateTime.now.plusMinutes(mongoTTL)).toInstant

  lazy val environment: String = servicesConfig.getConfString("tax-summaries-hod.env", "local")

  lazy val authorization: String = "Bearer " + servicesConfig
    .getConfString("tax-summaries-hod.authorizationToken", "local")

  lazy val originatorId: String = servicesConfig.getConfString("tax-summaries-hod.originatorId", "local")

  lazy val appName: String = servicesConfig.getString("appName")
}
