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

package common.config

import com.google.inject.Inject
import com.typesafe.config.ConfigObject
import common.models.{Item, Rate}
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.sql.Timestamp
import java.time.{Instant, LocalDateTime}
import scala.jdk.CollectionConverters.*

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

  private def governmentSpendByYear(year: Int): Seq[Item] = {
    val governmentSpendConfig = configuration
      .getOptional[ConfigObject](s"governmentSpend.$year.percentages")
    if (governmentSpendConfig.isDefined) {
      configuration.underlying
        .getObject(s"governmentSpend.$year.percentages")
        .asScala
        .map { case (index, _) =>
          val map: Map[String, Double] = configuration
            .getOptional[ConfigObject](s"governmentSpend.$year.percentages.$index")
            .map(_.unwrapped().asScala.view.mapValues(_.toString.toDouble).toMap)
            .getOrElse(Map.empty[String, Double])
          index.toInt -> Item(map.keys.head, map.values.head)
        }
        .toSeq
        .sortBy(_._1)
        .map(_._2)
    } else {
      Seq.empty
    }
  }

  def taxRates(year: Int): Map[String, Rate] = {
    val ratesInclDefaults = defaultRatePercentages ++ ratePercentagesByYear(year)
    val rates             = ratesInclDefaults.iterator.map { t =>
      t._1 -> Rate(t._2)
    }
    rates.toMap
  }

  def governmentSpend(year: Int): Seq[Item] = governmentSpendByYear(year)

  def npsServiceUrl: String = servicesConfig.baseUrl("tax-summaries-hod")

  private lazy val mongoTTL: Long = configuration.getOptional[Int]("mongodb.timeToLiveInMinutes").getOrElse(15).toLong

  def calculateExpiryTime(): Instant = Timestamp.valueOf(LocalDateTime.now.plusMinutes(mongoTTL)).toInstant

  lazy val environment: String = servicesConfig.getConfString("tax-summaries-hod.env", "local")

  lazy val authorization: String = "Bearer " + servicesConfig
    .getConfString("tax-summaries-hod.authorizationToken", "local")

  lazy val originatorId: String = servicesConfig.getConfString("tax-summaries-hod.originatorId", "local")

  val sessionCacheTtl: Int = configuration.getOptional[Int]("feature.session-cache.ttl").getOrElse(15)

  lazy val pertaxHost: String = servicesConfig.baseUrl("pertax")

  lazy val ifBaseURL: String       = servicesConfig.baseUrl("if-hod")
  lazy val ifEnvironment: String   = servicesConfig.getConfString("if-hod.env", "local")
  lazy val ifAuthorization: String = "Bearer " + servicesConfig.getConfString("if-hod.authorizationToken", "local")
  lazy val ifOriginatorId: String  = servicesConfig.getConfString("if-hod.originatorId", "")

  private lazy val hipPath: String  = servicesConfig.getConfString("hip-hod.path", "")
  lazy val hipBaseURL: String       = s"${servicesConfig.baseUrl("hip-hod")}$hipPath"
  lazy val hipEnvironment: String   = servicesConfig.getConfString("hip-hod.env", "local")
  lazy val hipAuthorization: String = "Bearer " + servicesConfig.getConfString("hip-hod.authorizationToken", "local")
  lazy val hipOriginatorId: String  = servicesConfig.getConfString("hip-hod.originatorId", "")
  val hipClientId: String           = servicesConfig.getConfString("hip-hod.clientId", "local")
  val hipClientSecret: String       = servicesConfig.getConfString("hip-hod.clientSecret", "local")

  val hipClientIdSA: String     = servicesConfig.getConfString("hip-hod-sa.clientId", "local")
  val hipClientSecretSA: String = servicesConfig.getConfString("hip-hod-sa.clientSecret", "local")

  lazy val appName: String = servicesConfig.getString("appName")
}
