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
import config.ApplicationConfig
import play.api.Logger
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, NOT_FOUND}
import uk.gov.hmrc.http.{HeaderCarrier, _}
import uk.gov.hmrc.http.HttpClient

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class NpsConnector @Inject()(http: HttpClient, applicationConfig: ApplicationConfig)(implicit ec: ExecutionContext) {

  private val logger = Logger(getClass.getName)

  def serviceUrl: String = applicationConfig.npsServiceUrl

  def url(path: String) = s"$serviceUrl$path"

  private def header(implicit hc: HeaderCarrier): Seq[(String, String)] = Seq(
    "Authorization" -> applicationConfig.authorization,
    "Environment"   -> applicationConfig.environment,
    "OriginatorId"  -> applicationConfig.originatorId,
    "SessionId"     -> HeaderNames.xSessionId,
    "RequestId"     -> HeaderNames.xRequestId,
    "CorrelationId" -> UUID.randomUUID().toString
  )

  def connectToPayeTaxSummary(NINO: String, TAX_YEAR: Int)(implicit hc: HeaderCarrier): Future[HttpResponse] = {
    val ninoWithoutSuffix = NINO.take(8)

    http.GET[HttpResponse](
      url("/individuals/annual-tax-summary/" + ninoWithoutSuffix + "/" + TAX_YEAR),
      headers = header) recover {
      case e: BadRequestException =>
        HttpResponse(BAD_REQUEST, s"Bad request response in connector for $NINO with message ${e.message}")
      case e: NotFoundException =>
        HttpResponse(NOT_FOUND, s"Not found response in connector for $NINO with message ${e.message}")
      case e: UpstreamErrorResponse =>
        logger.error(
          s"UpstreamErrorResponse in connector for $NINO with status ${e.statusCode} and message: ${e.getMessage()}")
        HttpResponse(INTERNAL_SERVER_ERROR, s"Nino: $NINO Status: ${e.statusCode} Message: ${e.getMessage()}")
      case e => {
        logger.error(s"Exception in NPSConnector: $e", e)
        HttpResponse(INTERNAL_SERVER_ERROR, s"Exception in connector for $NINO with message ${e.getMessage}")
      }
    }
  }
}
