/*
 * Copyright 2022 HM Revenue & Customs
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
import play.api.Logging
import play.api.http.Status.BAD_GATEWAY
import play.api.libs.json.JsValue
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames, HttpClient, HttpException, UpstreamErrorResponse}

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ODSConnector @Inject()(http: HttpClient, applicationConfig: ApplicationConfig) extends Logging {

  val serviceUrl = applicationConfig.npsServiceUrl

  private def header(implicit hc: HeaderCarrier): Seq[(String, String)] = Seq(
    HeaderNames.xSessionId -> hc.sessionId.fold("-")(_.value),
    HeaderNames.xRequestId -> hc.requestId.fold("-")(_.value),
    "CorrelationId"        -> UUID.randomUUID().toString
  )

  private def handleResponse(response: Either[UpstreamErrorResponse, JsValue]): Either[UpstreamErrorResponse, JsValue] =
    response match {
      case response @ Right(_) => response
      case Left(error) if error.statusCode >= 500 || error.statusCode == 429 => {
        logger.error(error.message)
        Left(error)
      }
      case Left(error) if error.statusCode == 404 => {
        logger.info(error.message)
        Left(error)
      }
      case Left(error) => {
        logger.error(error.message, error)
        Left(error)
      }
    }

  def url(path: String) = s"$serviceUrl$path"

  def connectToSelfAssessment(UTR: String, TAX_YEAR: Int)(
    implicit hc: HeaderCarrier): Future[Either[UpstreamErrorResponse, JsValue]] =
    http
      .GET[Either[UpstreamErrorResponse, JsValue]](
        url = url("/self-assessment/individuals/" + UTR + "/annual-tax-summaries/" + TAX_YEAR),
        headers = header
      )
      .map(handleResponse) recover {
      case error: HttpException => {
        logger.error(error.message)
        Left(UpstreamErrorResponse(error.message, BAD_GATEWAY, BAD_GATEWAY))
      }
    }

  def connectToSelfAssessmentList(UTR: String)(
    implicit hc: HeaderCarrier): Future[Either[UpstreamErrorResponse, JsValue]] =
    http
      .GET[Either[UpstreamErrorResponse, JsValue]](
        url = url("/self-assessment/individuals/" + UTR + "/annual-tax-summaries"),
        headers = header)
      .map(handleResponse) recover {
      case error: HttpException => {
        logger.error(error.message)
        Left(UpstreamErrorResponse(error.message, BAD_GATEWAY, BAD_GATEWAY))
      }
    }

  def connectToSATaxpayerDetails(UTR: String)(
    implicit hc: HeaderCarrier): Future[Either[UpstreamErrorResponse, JsValue]] =
    http
      .GET[Either[UpstreamErrorResponse, JsValue]](
        url("/self-assessment/individual/" + UTR + "/designatory-details/taxpayer"),
        headers = header
      )
      .map(handleResponse) recover {
      case error: HttpException => {
        logger.error(error.message)
        Left(UpstreamErrorResponse(error.message, BAD_GATEWAY, BAD_GATEWAY))
      }
    }
}
