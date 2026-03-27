/*
 * Copyright 2026 HM Revenue & Customs
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

package paye.connectors

import cats.data.EitherT
import com.google.inject.{Inject, Singleton}
import com.google.inject.name.Named
import common.config.ApplicationConfig
import common.connectors.HttpClientResponse
import paye.models
import paye.repositories.NpsCacheRepository
import play.api.Logging
import play.api.libs.json.JsObject
import uk.gov.hmrc.http.*
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

trait NpsConnector {
  def connectToPayeTaxSummary(nino: String, taxYear: Int)(implicit
    hc: HeaderCarrier
  ): EitherT[Future, UpstreamErrorResponse, HttpResponse]
}

class CachingNpsConnector @Inject() (
  @Named("default") underlying: NpsConnector,
  sessionCacheRepository: NpsCacheRepository,
  config: ApplicationConfig
)(implicit ec: ExecutionContext)
    extends NpsConnector {

  override def connectToPayeTaxSummary(nino: String, taxYear: Int)(implicit
    hc: HeaderCarrier
  ): EitherT[Future, UpstreamErrorResponse, HttpResponse] =
    EitherT(
      sessionCacheRepository
        .get(s"$nino::$taxYear")
        .map[Future[Either[UpstreamErrorResponse, HttpResponse]]] {
          case Some(dataMongo) => Future(Right(HttpResponse(200, dataMongo.data.toString)))
          case None            => refreshCache(nino, taxYear).value
        }
        .flatten
    )

  private def refreshCache(nino: String, taxYear: Int)(implicit
    hc: HeaderCarrier
  ): EitherT[Future, UpstreamErrorResponse, HttpResponse] =
    for {
      apiData <- underlying.connectToPayeTaxSummary(nino, taxYear)
      _       <-
        EitherT[Future, UpstreamErrorResponse, Boolean](
          sessionCacheRepository
            .set(
              models.PayeAtsMiddleTierMongo(s"$nino::$taxYear", apiData.json.as[JsObject], config.calculateExpiryTime())
            )
            .map(Right(_))
        )
    } yield apiData
}

@Singleton
class DefaultNpsConnector @Inject() (
  http: HttpClientV2,
  applicationConfig: ApplicationConfig,
  httpClientResponse: HttpClientResponse
)(implicit ec: ExecutionContext)
    extends NpsConnector,
      Logging {

  private def url(ninoWithoutSuffix: String, taxYear: Int): String =
    s"${applicationConfig.hipBaseURL}/paye/individual/$ninoWithoutSuffix/tax-account/$taxYear/annual-tax-summary"

  private def createHeader(implicit hc: HeaderCarrier): Seq[(String, String)] =
    Seq(
      "Environment"             -> applicationConfig.hipEnvironment,
      HeaderNames.xSessionId    -> hc.sessionId.fold("-")(_.value),
      HeaderNames.xRequestId    -> hc.requestId.fold("-")(_.value),
      "CorrelationId"           -> UUID.randomUUID().toString,
      "Gov-Uk-Originator-Id"    -> applicationConfig.hipOriginatorId,
      HeaderNames.authorisation -> s"Basic ${applicationConfig.token}"
    )

  def connectToPayeTaxSummary(nino: String, taxYear: Int)(implicit
    hc: HeaderCarrier
  ): EitherT[Future, UpstreamErrorResponse, HttpResponse] = {
    val ninoWithoutSuffix = nino.take(8)
    httpClientResponse.readPaye(
      http
        .get(url"${url(ninoWithoutSuffix, taxYear)}")
        .setHeader(createHeader: _*)
        .execute[Either[UpstreamErrorResponse, HttpResponse]]
    )
  }
}
