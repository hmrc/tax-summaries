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

package paye.connectors

import cats.data.EitherT
import com.google.inject.Inject
import common.config.ApplicationConfig
import common.connectors.HttpClientResponse
import common.models.admin.PayeDetailsFromHipToggle
import play.api.Logging
import uk.gov.hmrc.http.*
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.mongoFeatureToggles.services.FeatureFlagService

import java.nio.charset.StandardCharsets
import java.util.{Base64, UUID}
import scala.concurrent.{ExecutionContext, Future}

class NpsConnector @Inject() (
  http: HttpClientV2,
  applicationConfig: ApplicationConfig,
  httpClientResponse: HttpClientResponse,
  featureFlagService: FeatureFlagService
)(implicit ec: ExecutionContext)
    extends Logging {

  private val hipAuth = {
    val clientId: String     = applicationConfig.hipClientId
    val clientSecret: String = applicationConfig.hipClientSecret
    val token                = Base64.getEncoder.encodeToString(s"$clientId:$clientSecret".getBytes(StandardCharsets.UTF_8))
    Seq(
      HeaderNames.authorisation -> s"Basic $token"
    )
  }

  def serviceUrl: String = applicationConfig.npsServiceUrl

  def url(path: String): String = s"$serviceUrl$path"

  private def hipUrl(ninoWithoutSuffix: String, taxYear: Int): String =
    s"${applicationConfig.hipBaseURL}/individual/$ninoWithoutSuffix/tax-account/$taxYear/annual-tax-summary"

  private def ifUrl(ninoWithoutSuffix: String, taxYear: Int): String =
    s"${applicationConfig.ifBaseURL}/individuals/annual-tax-summary/$ninoWithoutSuffix/$taxYear"

  private def createHeader(hipToggle: Boolean)(implicit hc: HeaderCarrier): Seq[(String, String)] =
    if (hipToggle)
      Seq(
        "Environment"          -> applicationConfig.hipEnvironment,
        HeaderNames.xSessionId -> hc.sessionId.fold("-")(_.value),
        HeaderNames.xRequestId -> hc.requestId.fold("-")(_.value),
        "CorrelationId"        -> UUID.randomUUID().toString,
        "Gov-Uk-Originator-Id" -> applicationConfig.hipOriginatorId
      ) ++ hipAuth
    else
      Seq(
        "Environment"          -> applicationConfig.ifEnvironment,
        "Authorization"        -> applicationConfig.ifAuthorization,
        HeaderNames.xSessionId -> hc.sessionId.fold("-")(_.value),
        HeaderNames.xRequestId -> hc.requestId.fold("-")(_.value),
        "CorrelationId"        -> UUID.randomUUID().toString,
        "OriginatorId"         -> applicationConfig.ifOriginatorId
      )

  def connectToPayeTaxSummary(nino: String, taxYear: Int)(implicit
    hc: HeaderCarrier
  ): EitherT[Future, UpstreamErrorResponse, HttpResponse] = {
    val ninoWithoutSuffix = nino.take(8)
    featureFlagService.getAsEitherT(PayeDetailsFromHipToggle).flatMap { toggle =>

      val url =
        if (toggle.isEnabled) hipUrl(ninoWithoutSuffix, taxYear)
        else ifUrl(ninoWithoutSuffix, taxYear)

      httpClientResponse.readPaye(
        http
          .get(url"$url")
          .setHeader(createHeader(toggle.isEnabled): _*)
          .execute[Either[UpstreamErrorResponse, HttpResponse]]
      )
    }
  }
}
