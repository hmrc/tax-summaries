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

import com.google.inject.Inject
import config.ApplicationConfig
import play.api.Mode.Mode
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, NOT_FOUND}
import play.api.{Configuration, Environment, Logger, Play}
import uk.gov.hmrc.http.logging.Authorization
import uk.gov.hmrc.http.{HeaderCarrier, _}
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import uk.gov.hmrc.play.config.ServicesConfig

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class NpsConnector @Inject()(
  http: HttpClient,
  override val runModeConfiguration: Configuration,
  environment: Environment)
    extends ServicesConfig {

  override def mode: Mode = environment.mode

  def serviceUrl: String = ApplicationConfig.npsServiceUrl
  def url(path: String) = s"$serviceUrl$path"

  def header(hc: HeaderCarrier): HeaderCarrier =
    hc.copy(authorization = Some(Authorization(ApplicationConfig.authorization)))
      .withExtraHeaders(
        "Environment"  -> ApplicationConfig.environment,
        "OriginatorId" -> ApplicationConfig.originatorId
      )

  def connectToPayeTaxSummary(NINO: String, TAX_YEAR: Int)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    val ninoWithoutSuffix = NINO.take(8)

    implicit val desHeaderCarrier: HeaderCarrier = header(hc)

    http.GET[HttpResponse](url("/individuals/annual-tax-summary/" + ninoWithoutSuffix + "/" + TAX_YEAR))(
      RawReads.readRaw,
      desHeaderCarrier,
      ec = global) recover {
      case _: BadRequestException => HttpResponse(BAD_REQUEST)
      case _: NotFoundException   => HttpResponse(NOT_FOUND)
      case e => {
        Logger.error(s"Exception in NPSConnector: $e", e)
        HttpResponse(INTERNAL_SERVER_ERROR)
      }
    }
  }
}
