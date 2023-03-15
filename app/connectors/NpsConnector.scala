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

package connectors

import cats.data.EitherT
import com.google.inject.Inject
import config.ApplicationConfig
import play.api.Logging
import uk.gov.hmrc.http._
import uk.gov.hmrc.http.HttpReads.Implicits._

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class NpsConnector @Inject() (
  http: HttpClient,
  applicationConfig: ApplicationConfig,
  httpClientResponse: HttpClientResponse
)(implicit ec: ExecutionContext)
    extends Logging {

  def serviceUrl: String = applicationConfig.npsServiceUrl

  def url(path: String): String = s"$serviceUrl$path"

  private def header(implicit hc: HeaderCarrier): Seq[(String, String)] = Seq(
    HeaderNames.authorisation -> applicationConfig.authorization,
    "Environment"             -> applicationConfig.environment,
    "OriginatorId"            -> applicationConfig.originatorId,
    HeaderNames.xSessionId    -> hc.sessionId.fold("-")(_.value).split(",").head,
    HeaderNames.xRequestId    -> hc.requestId.fold("-")(_.value).split(",").head,
    "CorrelationId"           -> UUID.randomUUID().toString
  ).distinct

  def connectToPayeTaxSummary(NINO: String, TAX_YEAR: Int)(implicit
    hc: HeaderCarrier
  ): EitherT[Future, UpstreamErrorResponse, HttpResponse] = {
    val ninoWithoutSuffix = NINO.take(8)

    httpClientResponse.read(
      http
        .GET[Either[UpstreamErrorResponse, HttpResponse]](
          url("/individuals/annual-tax-summary/" + ninoWithoutSuffix + "/" + TAX_YEAR),
          headers = header
        )
    )
  }
}
