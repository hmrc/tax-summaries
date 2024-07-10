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
import models.admin.SelfAssessmentDetailsFromIfToggle
import play.api.Logging
import play.api.http.Status.NOT_FOUND
import play.api.libs.json.{Format, Json, OFormat}
import play.api.mvc.Request
import repositories.TaxSummariesSessionCacheRepository
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.HttpReadsInstances.readEitherOf
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames, HttpClient, HttpReads, HttpResponse, UpstreamErrorResponse}
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
      implicit val formats: OFormat[HttpResponse] = Json.format[HttpResponse]
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
      implicit val formats: OFormat[HttpResponse] = Json.format[HttpResponse]
      cache(utr) {
        underlying.connectToSelfAssessmentList(utr)
      }
    }

  override def connectToSATaxpayerDetails(utr: String)(implicit
    hc: HeaderCarrier,
    request: Request[_]
  ): EitherT[Future, UpstreamErrorResponse, HttpResponse] = {
    implicit val formats: OFormat[HttpResponse] = Json.format[HttpResponse]
    cache("taxpayer/" + utr) {
      underlying.connectToSATaxpayerDetails(utr)
    }
  }
}

@Singleton
class DefaultSelfAssessmentODSConnector @Inject() (
  val httpClient: HttpClient,
  applicationConfig: ApplicationConfig,
  httpClientResponse: HttpClientResponse,
  featureFlagService: FeatureFlagService
)(implicit ec: ExecutionContext)
    extends SelfAssessmentODSConnector
    with Logging {

  val serviceUrl: String = applicationConfig.npsServiceUrl

  def desUrl(path: String): String = s"$serviceUrl$path"

  def ifUrl(path: String): String = s"${applicationConfig.ifBaseURL}$path"

  private def createHeader(ifToggle: Boolean)(implicit hc: HeaderCarrier): Seq[(String, String)] =
    if (ifToggle) {
      Seq(
        "Environment"          -> applicationConfig.ifEnvironment,
        "Authorization"        -> applicationConfig.ifAuthorization,
        HeaderNames.xSessionId -> hc.sessionId.fold("-")(_.value),
        HeaderNames.xRequestId -> hc.requestId.fold("-")(_.value),
        "CorrelationId"        -> UUID.randomUUID().toString,
        "OriginatorId"         -> applicationConfig.ifOriginatorId
      )
    } else {
      Seq(
        HeaderNames.xSessionId -> hc.sessionId.fold("-")(_.value),
        HeaderNames.xRequestId -> hc.requestId.fold("-")(_.value),
        "CorrelationId"        -> UUID.randomUUID().toString
      )
    }

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
      val path = "/self-assessment/individuals/" + utr + "/annual-tax-summaries/" + taxYear
      val url  =
        if (toggle.isEnabled) {
          ifUrl(path)
        } else {
          desUrl(path)
        }

      httpClientResponse.readSA(
        httpClient.GET[Either[UpstreamErrorResponse, HttpResponse]](
          url = url,
          headers = createHeader(toggle.isEnabled)
        )(readEitherOfWithNotFound, implicitly, implicitly)
      )
    }

  def connectToSelfAssessmentList(utr: String)(implicit
    hc: HeaderCarrier,
    request: Request[_]
  ): EitherT[Future, UpstreamErrorResponse, HttpResponse] =
    httpClientResponse.readSA(
      httpClient
        .GET[Either[UpstreamErrorResponse, HttpResponse]](
          url = desUrl("/self-assessment/individuals/" + utr + "/annual-tax-summaries"),
          headers = createHeader(false)
        )(readEitherOfWithNotFound, implicitly, implicitly)
    )

  def connectToSATaxpayerDetails(utr: String)(implicit
    hc: HeaderCarrier,
    request: Request[_]
  ): EitherT[Future, UpstreamErrorResponse, HttpResponse] =
    httpClientResponse.read(
      httpClient
        .GET[Either[UpstreamErrorResponse, HttpResponse]](
          desUrl("/self-assessment/individual/" + utr + "/designatory-details/taxpayer"),
          headers = createHeader(false)
        )(readEitherOfWithNotFound, implicitly, implicitly)
    )
}
