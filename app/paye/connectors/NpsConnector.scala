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
import common.models.admin.PayeDetailsFromIfToggle
import play.api.Logging
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http._
import uk.gov.hmrc.mongoFeatureToggles.services.FeatureFlagService

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class NpsConnector @Inject() (
  http: HttpClient,
  applicationConfig: ApplicationConfig,
  httpClientResponse: HttpClientResponse,
  featureFlagService: FeatureFlagService
)(implicit ec: ExecutionContext)
    extends Logging {

  def serviceUrl: String = applicationConfig.npsServiceUrl

  def url(path: String): String = s"$serviceUrl$path"

  private def desUrl(path: String): String = s"$serviceUrl$path"

  private def ifUrl(path: String): String = s"${applicationConfig.ifBaseURL}$path"

  private def createHeader(ifToggle: Boolean)(implicit hc: HeaderCarrier): Seq[(String, String)] =
    if (ifToggle)
      Seq(
        "Environment"             -> applicationConfig.ifEnvironment,
        "Authorization"           -> applicationConfig.ifAuthorization,
        HeaderNames.xSessionId    -> hc.sessionId.fold("-")(_.value),
        HeaderNames.xRequestId    -> hc.requestId.fold("-")(_.value),
        "CorrelationId"           -> UUID.randomUUID().toString,
        "OriginatorId"            -> applicationConfig.ifOriginatorId
      )
    else
      Seq(
        HeaderNames.authorisation -> applicationConfig.authorization,
        "Environment"             -> applicationConfig.environment,
        "OriginatorId"            -> applicationConfig.originatorId,
        HeaderNames.xSessionId    -> hc.sessionId.fold("-")(_.value),
        HeaderNames.xRequestId    -> hc.requestId.fold("-")(_.value),
        "CorrelationId"           -> UUID.randomUUID().toString
      )

  def connectToPayeTaxSummary(NINO: String, TAX_YEAR: Int)(implicit
    hc: HeaderCarrier
  ): EitherT[Future, UpstreamErrorResponse, HttpResponse] = {
    val ninoWithoutSuffix = NINO.take(8)
    featureFlagService.getAsEitherT(PayeDetailsFromIfToggle).flatMap { toggle =>
      val url = {
        val path = "/individuals/annual-tax-summary/" + ninoWithoutSuffix + "/" + TAX_YEAR
        if (toggle.isEnabled) ifUrl(path)
        else desUrl(path)
      }

      httpClientResponse.read(
        http
          .GET[Either[UpstreamErrorResponse, HttpResponse]](
            url,
            headers = createHeader(toggle.isEnabled)
          )
      )
    }
  }
}
