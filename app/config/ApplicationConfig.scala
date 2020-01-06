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

import com.typesafe.config.ConfigObject
import play.api.{Configuration, Play}
import play.api.Mode.Mode
import play.api.Play.{configuration, current}
import uk.gov.hmrc.play.config.ServicesConfig
import collection.JavaConverters._

trait ApplicationConfig {

  def ratePercentages(year: Int): Map[String, Double]
  def governmentSpend(year: Int): Map[String, Double]
}

object ApplicationConfig extends ApplicationConfig with ServicesConfig {

  override protected def mode: Mode = Play.current.mode
  override protected def runModeConfiguration: Configuration = Play.current.configuration
  private def taxFieldsDefault = configuration.getStringSeq("taxRates.default.whitelist").getOrElse(Seq())
  private def taxFieldsByYear(year: Int) = configuration.getStringSeq(s"taxRates.$year.whitelist").getOrElse(Seq())

  private def defaultRatePercentages: Map[String, Double] =
    configuration
      .getObject("taxRates.default.percentages")
      .map(_.unwrapped().asScala.mapValues(_.toString.toDouble))
      .getOrElse(Map())
      .toMap

  private def ratePercentagesByYear(year: Int): Map[String, Double] =
    configuration
      .getObject(s"taxRates.$year.percentages")
      .map(_.unwrapped().asScala.mapValues(_.toString.toDouble))
      .getOrElse(Map())
      .toMap

  private def governmentSpendByYear(year: Int): Map[String, Double] =
    configuration
      .getObject(s"governmentSpend.$year.percentages")
      .map(_.unwrapped().asScala.mapValues(_.toString.toDouble))
      .getOrElse(Map())
      .toMap

  override def ratePercentages(year: Int) = defaultRatePercentages ++ ratePercentagesByYear(year)

  override def governmentSpend(year: Int) = governmentSpendByYear(year)
}
