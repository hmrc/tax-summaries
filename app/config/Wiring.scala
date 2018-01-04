/*
 * Copyright 2018 HM Revenue & Customs
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

import com.typesafe.config.Config
import play.api.Play
import uk.gov.hmrc.play.audit.http.config.AuditingConfig
import uk.gov.hmrc.play.audit.http.connector.AuditConnector
import uk.gov.hmrc.play.auth.controllers.AuthParamsControllerConfig
import uk.gov.hmrc.play.auth.microservice.connectors.AuthConnector
import uk.gov.hmrc.play.auth.microservice.filters.AuthorisationFilter
import uk.gov.hmrc.play.config.{AppName, ControllerConfig, RunMode, ServicesConfig}
import uk.gov.hmrc.play.http.ws._
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.hooks.HttpHook
import uk.gov.hmrc.play.microservice.config.LoadAuditingConfig
import uk.gov.hmrc.play.microservice.filters.{AuditFilter, LoggingFilter, MicroserviceFilterSupport}


trait WSHttp extends HttpGet with WSGet with HttpPut with WSPut with HttpPost with WSPost with HttpDelete with WSDelete
object WSHttp extends WSHttp with AppName with RunMode {
  override val hooks: Seq[HttpHook] = NoneRequired
}

object TAXSControllerConfig extends ControllerConfig {
  override lazy val controllerConfigs: Config = Play.current.configuration.underlying.getConfig("controllers")
}

object TAXSAuthControllerConfig extends AuthParamsControllerConfig {
  override lazy val controllerConfigs: Config = TAXSControllerConfig.controllerConfigs
}

object TAXSAuditConnector extends AuditConnector with RunMode {
  override lazy val auditingConfig: AuditingConfig = LoadAuditingConfig(s"auditing")
}

object TAXSAuthConnector extends AuthConnector with ServicesConfig with WSHttp {
  override def authBaseUrl: String = baseUrl("auth")
  override val hooks: Seq[HttpHook] = NoneRequired
}

object TAXSLoggingFilter extends LoggingFilter with MicroserviceFilterSupport {
  override def controllerNeedsLogging(controllerName: String): Boolean =
    TAXSControllerConfig.paramsForController(controllerName).needsLogging
}

object TAXSAuditFilter extends AuditFilter with AppName with MicroserviceFilterSupport  {
  override def auditConnector: AuditConnector = TAXSAuditConnector
  // Overriding this globally as no controllers should implicitly audit.
  override def controllerNeedsAuditing(controllerName: String): Boolean = false
}

object TAXSAuthFilter extends AuthorisationFilter with MicroserviceFilterSupport  {
  override def authConnector: AuthConnector = TAXSAuthConnector
  override def authParamsConfig: AuthParamsControllerConfig = TAXSAuthControllerConfig
  override def controllerNeedsAuth(controllerName: String): Boolean =
    TAXSControllerConfig.paramsForController(controllerName).needsAuth
}
