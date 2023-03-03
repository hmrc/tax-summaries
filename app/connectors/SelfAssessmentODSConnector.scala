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
import cats.implicits._
import com.google.inject.name.Named
import com.google.inject.{Inject, Singleton}
import config.ApplicationConfig
import play.api.Logging
import play.api.libs.json.{Format, OFormat}
import play.api.mvc.Request
import repositories.SessionCacheRepository
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.HttpReadsInstances.readEitherOf
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames, HttpClient, HttpResponse, UpstreamErrorResponse}
import uk.gov.hmrc.mongo.cache.DataKey
import play.api.libs.json.Json
import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

trait SelfAssessmentODSConnector {
  def connectToSelfAssessment(UTR: String, TAX_YEAR: Int)(implicit
    hc: HeaderCarrier,
    request: Request[_]
  ): EitherT[Future, UpstreamErrorResponse, HttpResponse]

  def connectToSelfAssessmentList(UTR: String)(implicit
    hc: HeaderCarrier,
    request: Request[_]
  ): EitherT[Future, UpstreamErrorResponse, HttpResponse]

  def connectToSATaxpayerDetails(UTR: String)(implicit
    hc: HeaderCarrier,
    request: Request[_]
  ): EitherT[Future, UpstreamErrorResponse, HttpResponse]
}

class CachingSelfAssessmentODSConnector @Inject() (
  @Named("default") underlying: SelfAssessmentODSConnector,
  sessionCacheRepository: SessionCacheRepository
)(implicit ec: ExecutionContext)
    extends SelfAssessmentODSConnector {

  private def cache[L, A: Format](
    key: String
  )(f: => EitherT[Future, L, A])(implicit request: Request[_]): EitherT[Future, L, A] = {

    def fetchAndCache: EitherT[Future, L, A] =
      for {
        result <- f
        _      <- EitherT[Future, L, (String, String)](
                    sessionCacheRepository
                      .putSession[A](DataKey[A](key), result)
                      .map(Right(_))
                  )
      } yield result

    EitherT(
      sessionCacheRepository
        .getFromSession[A](DataKey[A](key))
        .map {
          case None        => fetchAndCache
          case Some(value) => EitherT.rightT[Future, L](value)
        }
        .map(_.value)
        .flatten
    ) recoverWith { case NonFatal(_) =>
      fetchAndCache
    }
  }

  override def connectToSelfAssessment(UTR: String, TAX_YEAR: Int)(implicit
    hc: HeaderCarrier,
    request: Request[_]
  ): EitherT[Future, UpstreamErrorResponse, HttpResponse] = {
    implicit val formats: OFormat[HttpResponse] = Json.format[HttpResponse]
    cache(UTR + "/" + TAX_YEAR) {
      underlying.connectToSelfAssessment(UTR, TAX_YEAR)
    }
  }

  override def connectToSelfAssessmentList(UTR: String)(implicit
    hc: HeaderCarrier,
    request: Request[_]
  ): EitherT[Future, UpstreamErrorResponse, HttpResponse] = {
    implicit val formats: OFormat[HttpResponse] = Json.format[HttpResponse]
    cache(UTR) {
      underlying.connectToSelfAssessmentList(UTR)
    }
  }

  override def connectToSATaxpayerDetails(UTR: String)(implicit
    hc: HeaderCarrier,
    request: Request[_]
  ): EitherT[Future, UpstreamErrorResponse, HttpResponse] = {
    implicit val formats: OFormat[HttpResponse] = Json.format[HttpResponse]
    cache("taxpayer/" + UTR) {
      underlying.connectToSATaxpayerDetails(UTR)
    }
  }
}

@Singleton
class DefaultSelfAssessmentODSConnector @Inject() (
  val httpClient: HttpClient,
  applicationConfig: ApplicationConfig,
  httpClientResponse: HttpClientResponse
)(implicit ec: ExecutionContext)
    extends SelfAssessmentODSConnector
    with Logging {

  val serviceUrl: String = applicationConfig.npsServiceUrl

  def url(path: String): String = s"$serviceUrl$path"

  private def header(implicit hc: HeaderCarrier): Seq[(String, String)] = Seq(
    HeaderNames.xSessionId -> hc.sessionId.fold("-")(_.value),
    HeaderNames.xRequestId -> hc.requestId.fold("-")(_.value),
    "CorrelationId"        -> UUID.randomUUID().toString
  )

  def connectToSelfAssessment(UTR: String, TAX_YEAR: Int)(implicit
    hc: HeaderCarrier,
    request: Request[_]
  ): EitherT[Future, UpstreamErrorResponse, HttpResponse] =
    httpClientResponse.read(
      httpClient.GET[Either[UpstreamErrorResponse, HttpResponse]](
        url = url("/self-assessment/individuals/" + UTR + "/annual-tax-summaries/" + TAX_YEAR),
        headers = header
      )
    )

  def connectToSelfAssessmentList(UTR: String)(implicit
    hc: HeaderCarrier,
    request: Request[_]
  ): EitherT[Future, UpstreamErrorResponse, HttpResponse] =
    httpClientResponse.read(
      httpClient
        .GET[Either[UpstreamErrorResponse, HttpResponse]](
          url = url("/self-assessment/individuals/" + UTR + "/annual-tax-summaries"),
          headers = header
        )
    )

  def connectToSATaxpayerDetails(UTR: String)(implicit
    hc: HeaderCarrier,
    request: Request[_]
  ): EitherT[Future, UpstreamErrorResponse, HttpResponse] =
    httpClientResponse.read(
      httpClient
        .GET[Either[UpstreamErrorResponse, HttpResponse]](
          url("/self-assessment/individual/" + UTR + "/designatory-details/taxpayer"),
          headers = header
        )
    )
}
