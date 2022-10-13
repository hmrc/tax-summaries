/*
 * Copyright 2022 HM Revenue & Customs
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
import play.api.Logging
import play.api.http.Status.BAD_GATEWAY
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.HttpReads.Implicits._

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class NpsConnector @Inject() (http: HttpClient, applicationConfig: ApplicationConfig)(implicit ec: ExecutionContext)
    extends Logging {

  def serviceUrl: String = applicationConfig.npsServiceUrl

  def url(path: String): String = s"$serviceUrl$path"

  private def header(implicit hc: HeaderCarrier): Seq[(String, String)] = Seq(
    HeaderNames.authorisation -> applicationConfig.authorization,
    "Environment"             -> applicationConfig.environment,
    "OriginatorId"            -> applicationConfig.originatorId,
    HeaderNames.xSessionId    -> hc.sessionId.fold("-")(_.value),
    HeaderNames.xRequestId    -> hc.requestId.fold("-")(_.value),
    "CorrelationId"           -> UUID.randomUUID().toString
  )

  def connectToPayeTaxSummary(NINO: String, TAX_YEAR: Int)(implicit
    hc: HeaderCarrier
  ): Future[Either[UpstreamErrorResponse, HttpResponse]] = {
    val ninoWithoutSuffix = NINO.take(8)

    http
      .GET[Either[UpstreamErrorResponse, HttpResponse]](
        url("/individuals/annual-tax-summary/" + ninoWithoutSuffix + "/" + TAX_YEAR),
        headers = header
      )
      .map {
        case response @ Right(_)                                               => response
        case Left(error) if error.statusCode >= 500 || error.statusCode == 429 =>
          logger.error(error.message)
          Left(error)
        case Left(error) if error.statusCode == 404                            =>
          logger.info(error.message)
          Left(error)
        case Left(error)                                                       =>
          logger.error(error.message, error)
          Left(error)
      } recover { case error: HttpException =>
      logger.error(error.message)
      Left(UpstreamErrorResponse(error.message, BAD_GATEWAY, BAD_GATEWAY))
    }
  }
}
