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

import akka.actor.ActorSystem
import com.typesafe.config.Config
import play.api.Mode.Mode
import play.api.{Configuration, Play}
import uk.gov.hmrc.play.audit.http.config.AuditingConfig
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.auth.controllers.AuthParamsControllerConfig
import uk.gov.hmrc.play.auth.microservice.connectors.AuthConnector
import uk.gov.hmrc.play.auth.microservice.filters.AuthorisationFilter
import uk.gov.hmrc.play.config.{AppName, ControllerConfig, ServicesConfig}
import uk.gov.hmrc.play.http.ws._
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.hooks.HttpHook
import uk.gov.hmrc.play.audit.http.HttpAuditing
import uk.gov.hmrc.play.microservice.config.LoadAuditingConfig
import uk.gov.hmrc.play.microservice.filters.{AuditFilter, LoggingFilter, MicroserviceFilterSupport}

trait WSHttp
    extends HttpGet with WSGet with HttpPut with WSPut with HttpPost with WSPost with HttpDelete with WSDelete
    with HttpAuditing with AppName {
  protected def appNameConfiguration: Configuration = Play.current.configuration

  override protected def actorSystem: ActorSystem = Play.current.actorSystem

  override protected def configuration: Option[Config] = Some(Play.current.configuration.underlying)

  protected def mode: Mode = Play.current.mode

  protected def runModeConfiguration: Configuration = Play.current.configuration

  def auditConnector: AuditConnector = TAXSAuditConnector

  val hooks = Seq(AuditingHook)

}
object WSHttp extends WSHttp

object TAXSControllerConfig extends ControllerConfig {
  override lazy val controllerConfigs: Config = Play.current.configuration.underlying.getConfig("controllers")
}

object TAXSAuthControllerConfig extends AuthParamsControllerConfig {
  override lazy val controllerConfigs: Config = TAXSControllerConfig.controllerConfigs
}

object TAXSAuditConnector extends AuditConnector {
  override lazy val auditingConfig: AuditingConfig = LoadAuditingConfig(s"auditing")
}

object TAXSAuthConnector extends AuthConnector with ServicesConfig with WSHttp {
  override def authBaseUrl: String = baseUrl("auth")

}

object TAXSLoggingFilter extends LoggingFilter with MicroserviceFilterSupport {
  override def controllerNeedsLogging(controllerName: String): Boolean =
    TAXSControllerConfig.paramsForController(controllerName).needsLogging
}

object TAXSAuditFilter extends AuditFilter with AppName with MicroserviceFilterSupport {
  override def auditConnector: AuditConnector = TAXSAuditConnector
  // Overriding this globally as no controllers should implicitly audit.
  override def controllerNeedsAuditing(controllerName: String): Boolean = false

  protected def appNameConfiguration: Configuration = Play.current.configuration
}

object TAXSAuthFilter extends AuthorisationFilter with MicroserviceFilterSupport {
  override def authConnector: AuthConnector = TAXSAuthConnector
  override def authParamsConfig: AuthParamsControllerConfig = TAXSAuthControllerConfig
  override def controllerNeedsAuth(controllerName: String): Boolean =
    TAXSControllerConfig.paramsForController(controllerName).needsAuth
}
