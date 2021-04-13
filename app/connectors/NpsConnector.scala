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
import uk.gov.hmrc.http.logging.Authorization
import uk.gov.hmrc.http.{HeaderCarrier, _}
import uk.gov.hmrc.http.HttpClient

import scala.concurrent.{ExecutionContext, Future}

class NpsConnector @Inject()(http: HttpClient, applicationConfig: ApplicationConfig)(implicit ec: ExecutionContext) {

  def serviceUrl: String = applicationConfig.npsServiceUrl
  def url(path: String) = s"$serviceUrl$path"

  def header(hc: HeaderCarrier): HeaderCarrier =
    hc.copy(authorization = Some(Authorization(applicationConfig.authorization)))
      .withExtraHeaders(
        "Environment"  -> applicationConfig.environment,
        "OriginatorId" -> applicationConfig.originatorId
      )

  def connectToPayeTaxSummary(NINO: String, TAX_YEAR: Int, hc: HeaderCarrier): Future[HttpResponse] = {
    val ninoWithoutSuffix = NINO.take(8)

    implicit val desHeaderCarrier: HeaderCarrier = header(hc)

    http.GET[HttpResponse](url("/individuals/annual-tax-summary/" + ninoWithoutSuffix + "/" + TAX_YEAR)) recover {
      case _: BadRequestException => HttpResponse(BAD_REQUEST)
      case _: NotFoundException   => HttpResponse(NOT_FOUND)
      case _: Upstream5xxResponse => HttpResponse(INTERNAL_SERVER_ERROR)
      case e => {
        Logger.error(s"Exception in NPSConnector: $e", e)
        HttpResponse(INTERNAL_SERVER_ERROR)
      }
    }
  }
}
