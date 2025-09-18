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

package sa.connectors

import cats.data.EitherT
import cats.implicits.*
import com.google.inject.name.Named
import com.google.inject.{Inject, Singleton}
import common.config.ApplicationConfig
import common.connectors.HttpClientResponse
import common.models.HttpResponseJsonFormat.given
import common.models.admin.SelfAssessmentDetailsFromIfToggle
import play.api.Logging
import play.api.http.Status.NOT_FOUND
import play.api.libs.json.Format
import play.api.mvc.Request
import sa.repositories.TaxSummariesSessionCacheRepository
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames, HttpReads, HttpResponse, StringContextOps, UpstreamErrorResponse}
import uk.gov.hmrc.mongo.cache.DataKey
import uk.gov.hmrc.mongoFeatureToggles.services.FeatureFlagService

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

trait SelfAssessmentODSConnector {
  def connectToSelfAssessment(utr: String, taxYear: Int)(implicit
    hc: HeaderCarrier,
    request: Request[_]
  ): EitherT[Future, UpstreamErrorResponse, HttpResponse]

  def connectToSelfAssessmentList(utr: String)(implicit
    hc: HeaderCarrier,
    request: Request[_]
  ): EitherT[Future, UpstreamErrorResponse, HttpResponse]

  def connectToSATaxpayerDetails(utr: String)(implicit
    hc: HeaderCarrier,
    request: Request[_]
  ): EitherT[Future, UpstreamErrorResponse, HttpResponse]
}

class CachingSelfAssessmentODSConnector @Inject() (
  @Named("default") underlying: SelfAssessmentODSConnector,
  sessionCacheRepository: TaxSummariesSessionCacheRepository,
  applicationConfig: ApplicationConfig
)(implicit ec: ExecutionContext)
    extends SelfAssessmentODSConnector {

  private def cache[L, A: Format](
    key: String
  )(f: => EitherT[Future, L, A])(implicit hc: HeaderCarrier): EitherT[Future, L, A] = {

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
    )
  }

  private def ignoreCacheForSelfAssessment(utr: String): Boolean = {
    val isStubbedEnvironment: Boolean = applicationConfig.environment == "local"
    isStubbedEnvironment && utr >= "0000000010" && utr <= "0000000020"
  }

  override def connectToSelfAssessment(utr: String, taxYear: Int)(implicit
    hc: HeaderCarrier,
    request: Request[_]
  ): EitherT[Future, UpstreamErrorResponse, HttpResponse] =
    if (ignoreCacheForSelfAssessment(utr)) {
      underlying.connectToSelfAssessment(utr, taxYear)
    } else {
      cache(utr + "/" + taxYear) {
        underlying.connectToSelfAssessment(utr, taxYear)
      }
    }

  override def connectToSelfAssessmentList(utr: String)(implicit
    hc: HeaderCarrier,
    request: Request[_]
  ): EitherT[Future, UpstreamErrorResponse, HttpResponse] =
    if (ignoreCacheForSelfAssessment(utr)) {
      underlying.connectToSelfAssessmentList(utr)
    } else {
      cache(utr) {
        underlying.connectToSelfAssessmentList(utr)
      }
    }

  override def connectToSATaxpayerDetails(utr: String)(implicit
    hc: HeaderCarrier,
    request: Request[_]
  ): EitherT[Future, UpstreamErrorResponse, HttpResponse] =
    cache("taxpayer/" + utr) {
      underlying.connectToSATaxpayerDetails(utr)
    }
}

@Singleton
class DefaultSelfAssessmentODSConnector @Inject() (
  http: HttpClientV2,
  applicationConfig: ApplicationConfig,
  httpClientResponse: HttpClientResponse,
  featureFlagService: FeatureFlagService
)(implicit ec: ExecutionContext)
    extends SelfAssessmentODSConnector
    with Logging {

  private def serviceUrl: String = applicationConfig.npsServiceUrl

  private def desUrl(path: String): String = s"$serviceUrl$path"

  private def ifUrl(path: String): String = s"${applicationConfig.ifBaseURL}$path"

  private def createHeader(ifToggle: Boolean)(implicit hc: HeaderCarrier): Seq[(String, String)] =
    if (ifToggle)
      Seq(
        "Environment"          -> applicationConfig.ifEnvironment,
        "Authorization"        -> applicationConfig.ifAuthorization,
        HeaderNames.xSessionId -> hc.sessionId.fold("-")(_.value),
        HeaderNames.xRequestId -> hc.requestId.fold("-")(_.value),
        "CorrelationId"        -> UUID.randomUUID().toString,
        "OriginatorId"         -> applicationConfig.ifOriginatorId
      )
    else
      Seq(
        HeaderNames.xSessionId -> hc.sessionId.fold("-")(_.value),
        HeaderNames.xRequestId -> hc.requestId.fold("-")(_.value),
        "CorrelationId"        -> UUID.randomUUID().toString
      )

  private def readEitherOfWithNotFound[A: HttpReads]: HttpReads[Either[UpstreamErrorResponse, A]] =
    HttpReads.ask.flatMap {
      case (_, _, response) if response.status == NOT_FOUND => HttpReads[A].map(Right.apply)
      case _                                                => HttpReads[Either[UpstreamErrorResponse, A]]
    }

  def connectToSelfAssessment(utr: String, taxYear: Int)(implicit
    hc: HeaderCarrier,
    request: Request[_]
  ): EitherT[Future, UpstreamErrorResponse, HttpResponse] =
    featureFlagService.getAsEitherT(SelfAssessmentDetailsFromIfToggle).flatMap { toggle =>
      val path = s"/self-assessment/individuals/$utr/annual-tax-summaries/$taxYear"
      val url  = if (toggle.isEnabled) ifUrl(path) else desUrl(path)

      httpClientResponse.readSA(
        http
          .get(url"$url")
          .setHeader(createHeader(toggle.isEnabled): _*)
          .execute[Either[UpstreamErrorResponse, HttpResponse]](readEitherOfWithNotFound, implicitly)
      )
    }

  def connectToSelfAssessmentList(utr: String)(implicit
    hc: HeaderCarrier,
    request: Request[_]
  ): EitherT[Future, UpstreamErrorResponse, HttpResponse] =
    httpClientResponse.readSA(
      http
        .get(url"${desUrl(s"/self-assessment/individuals/$utr/annual-tax-summaries")}")
        .setHeader(createHeader(ifToggle = false): _*)
        .execute[Either[UpstreamErrorResponse, HttpResponse]](readEitherOfWithNotFound, implicitly)
    )

  def connectToSATaxpayerDetails(utr: String)(implicit
    hc: HeaderCarrier,
    request: Request[_]
  ): EitherT[Future, UpstreamErrorResponse, HttpResponse] =
    httpClientResponse.readSA(
      http
        .get(url"${desUrl(s"/self-assessment/individual/$utr/designatory-details/taxpayer")}")
        .setHeader(createHeader(ifToggle = false): _*)
        .execute[Either[UpstreamErrorResponse, HttpResponse]](readEitherOfWithNotFound, implicitly)
    )
}
