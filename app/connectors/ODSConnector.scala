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
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames, HttpClient, HttpResponse, UpstreamErrorResponse}

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class ODSConnector @Inject() (
  http: HttpClient,
  applicationConfig: ApplicationConfig,
  httpClientResponse: HttpClientResponse
)(implicit ec: ExecutionContext)
    extends Logging {

  val serviceUrl: String = applicationConfig.npsServiceUrl

  private def header(implicit hc: HeaderCarrier): Seq[(String, String)] = Seq(
    HeaderNames.xSessionId -> hc.sessionId.fold("-")(_.value),
    HeaderNames.xRequestId -> hc.requestId.fold("-")(_.value),
    "CorrelationId"        -> UUID.randomUUID().toString
  )

  def url(path: String) = s"$serviceUrl$path"

  def connectToSelfAssessmentList(
    UTR: String
  )(implicit hc: HeaderCarrier): EitherT[Future, UpstreamErrorResponse, HttpResponse] =
    httpClientResponse.read(
      http
        .GET[Either[UpstreamErrorResponse, HttpResponse]](
          url = url("/self-assessment/individuals/" + UTR + "/annual-tax-summaries"),
          headers = header
        )
    )

  def connectToSATaxpayerDetails(
    UTR: String
  )(implicit hc: HeaderCarrier): EitherT[Future, UpstreamErrorResponse, HttpResponse] =
    httpClientResponse.read(
      http
        .GET[Either[UpstreamErrorResponse, HttpResponse]](
          url("/self-assessment/individual/" + UTR + "/designatory-details/taxpayer"),
          headers = header
        )
    )
}
