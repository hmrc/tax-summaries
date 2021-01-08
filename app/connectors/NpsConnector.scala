/*
 * Copyright 2021 HM Revenue & Customs
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
import com.kenshoo.play.metrics.Metrics
import config.ApplicationConfig
import metrics.uk.gov.hmrc.tai.metrics.metrics.HasMetrics
import play.api.Logger
import play.api.http.Status._
import uk.gov.hmrc.http.logging.Authorization
import uk.gov.hmrc.http.{HeaderCarrier, _}
import uk.gov.hmrc.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

class NpsConnector @Inject()(http: HttpClient, applicationConfig: ApplicationConfig, val metrics: Metrics)(
  implicit ec: ExecutionContext)
    extends HasMetrics with ExtraHeaders {

  private def serviceUrl: String = applicationConfig.npsServiceUrl
  private def url(path: String): String = s"$serviceUrl$path"

  private def header(hc: HeaderCarrier): HeaderCarrier =
    hc.copy(authorization = Some(Authorization(applicationConfig.authorization)))
      .withExtraHeaders(
        "Environment"  -> applicationConfig.environment,
        "OriginatorId" -> applicationConfig.originatorId
      )

  def connectToPayeTaxSummary(NINO: String, TAX_YEAR: Int)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    val ninoWithoutSuffix = NINO.take(8)

    val headerCarrier = header(extraHeaders(hc))

    withMetricsTimerAsync("paye-for-tax-year") { metricsTimer =>
      http
        .GET[HttpResponse](url("/individuals/annual-tax-summary/" + ninoWithoutSuffix + "/" + TAX_YEAR))(
          implicitly,
          headerCarrier,
          implicitly)
        .flatMap { response =>
          metricsTimer.completeWithSuccess()
          Future.successful(response)
        } recover {
        case _: NotFoundException =>
          metricsTimer.completeWithSuccess()
          HttpResponse.apply(NOT_FOUND, "")
        case _: BadRequestException =>
          metricsTimer.completeWithFailure()
          HttpResponse.apply(BAD_REQUEST, "")
        case e => {
          metricsTimer.completeWithFailure()
          val errorMessage = s"Exception in NPSConnector: $e"
          Logger.error(errorMessage, e)
          HttpResponse.apply(INTERNAL_SERVER_ERROR, errorMessage)
        }
      }
    }
  }
}
