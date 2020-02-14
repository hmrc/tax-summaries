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

package connectors

import config.{ApplicationConfig, WSHttp}
import play.api.Mode.Mode
import play.api.http.Status.{BAD_GATEWAY, GATEWAY_TIMEOUT, INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json.JsValue
import play.api.{Configuration, Logger, Play}
import uk.gov.hmrc.http.{HeaderCarrier, HttpGet, HttpReads, _}
import uk.gov.hmrc.play.config.ServicesConfig

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object NpsConnector extends NpsConnector with ServicesConfig {

  override val serviceUrl = ApplicationConfig.npsServiceUrl
  override def http = WSHttp

}

trait NpsConnector {

  protected def mode: Mode = Play.current.mode
  protected def runModeConfiguration: Configuration = Play.current.configuration

  def http: HttpGet
  def serviceUrl: String
  def url(path: String) = s"$serviceUrl$path"

  def connectToPayeTaxSummary(NINO: String, TAX_YEAR: Int)(implicit hc: HeaderCarrier): Future[HttpResponse] =
    http.GET[HttpResponse](url("/individuals/annual-tax-summary/" + NINO + "/" + TAX_YEAR))(
      RawReads.readRaw,
      hc,
      ec = global) recover {
      case e =>
        Logger.error(s"Exception blah in connector: ${e.getMessage}")
        HttpResponse(INTERNAL_SERVER_ERROR)
    }
}
