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

import com.codahale.metrics.Timer
import com.google.inject.Inject
import config.ApplicationConfig
import metrics.MetricsEnumeration.MetricsEnumeration
import metrics.{Metrics, MetricsEnumeration}
import models.Audit
import play.api.Logging
import play.api.http.Status.BAD_GATEWAY
import play.api.libs.json.JsValue
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames, HttpClient, HttpException, UpstreamErrorResponse}

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class ODSConnector @Inject()(
  http: HttpClient,
  metrics: Metrics,
  applicationConfig: ApplicationConfig
)(implicit ec: ExecutionContext)
    extends Logging {

  val serviceUrl = applicationConfig.npsServiceUrl

  private def header(implicit hc: HeaderCarrier): Seq[(String, String)] = Seq(
    HeaderNames.xSessionId -> hc.sessionId.fold("-")(_.value),
    HeaderNames.xRequestId -> hc.requestId.fold("-")(_.value),
    "CorrelationId"        -> UUID.randomUUID().toString
  )

  def auditDetails(utr: String, taxYear: Option[Int] = None)(implicit hc: HeaderCarrier): Map[String, String] = {
    val taxYearEntry = taxYear.map(year => s"$year-${year + 1}").getOrElse("")

    Map(
      "Authorization" -> hc.authorization.map(_.value).getOrElse(""),
      "deviceID"      -> hc.deviceID.getOrElse(""),
      "endsOn"        -> "",
      "ipAddress"     -> hc.trueClientIp.getOrElse(""),
      "utr"           -> utr,
      "startsOn"      -> "",
      "taxYear"       -> taxYearEntry
    )
  }

  private def handleResponse(
    response: Either[UpstreamErrorResponse, JsValue],
    metricEnum: MetricsEnumeration,
    auditIdentifier: String,
    utr: String,
    taxYear: Option[Int] = None)(implicit hc: HeaderCarrier): Either[UpstreamErrorResponse, JsValue] = {
    val audit =
      Audit("saRequest", auditIdentifier, auditDetails(utr, taxYear))

    response match {
      case response @ Right(_) =>
        metrics.incrementSuccessCounter(metricEnum)
        response
      case Left(error) if error.statusCode >= 500 || error.statusCode == 429 => {
        metrics.incrementFailedCounter(metricEnum)
        logger.error(error.message)
        Left(error)
      }
      case Left(error) if error.statusCode == 404 => {
        metrics.incrementFailedCounter(metricEnum) /// TODO - Should this be fail?
        logger.info(error.message)
        Left(error)
      }
      case Left(error) => {
        metrics.incrementFailedCounter(metricEnum)
        logger.error(error.message, error)
        Left(error)
      }
    }
  }

  def url(path: String) = s"$serviceUrl$path"

  def connectToSelfAssessment(UTR: String, TAX_YEAR: Int)(
    implicit hc: HeaderCarrier): Future[Either[UpstreamErrorResponse, JsValue]] = {
    val metricEnum = MetricsEnumeration.GET_SA
    val timerContext: Timer.Context =
      metrics.startTimer(metricEnum)

    http
      .GET[Either[UpstreamErrorResponse, JsValue]](
        url = url("/self-assessment/individuals/" + UTR + "/annual-tax-summaries/" + TAX_YEAR),
        headers = header
      )
      .map { response =>
        timerContext.stop()
        response
      }
      .map(response => handleResponse(response, metricEnum, "ats_getSaSelfAssessment", UTR, Some(TAX_YEAR))) recover {
      case error: HttpException => {
        metrics.incrementFailedCounter(metricEnum)
        logger.error(error.message)
        Left(UpstreamErrorResponse(error.message, BAD_GATEWAY, BAD_GATEWAY))
      }
    }
  }

  def connectToSelfAssessmentList(UTR: String)(
    implicit hc: HeaderCarrier): Future[Either[UpstreamErrorResponse, JsValue]] = {
    val metricEnum = MetricsEnumeration.GET_SA_LIST
    val timerContext: Timer.Context =
      metrics.startTimer(metricEnum)

    http
      .GET[Either[UpstreamErrorResponse, JsValue]](
        url = url("/self-assessment/individuals/" + UTR + "/annual-tax-summaries"),
        headers = header
      )
      .map { response =>
        timerContext.stop()
        response
      }
      .map(response => handleResponse(response, metricEnum, "ats_getSaSelfAssessmentList", UTR)) recover {
      case error: HttpException => {
        metrics.incrementFailedCounter(metricEnum)
        logger.error(error.message)
        Left(UpstreamErrorResponse(error.message, BAD_GATEWAY, BAD_GATEWAY))
      }
    }
  }

  def connectToSATaxpayerDetails(UTR: String)(
    implicit hc: HeaderCarrier): Future[Either[UpstreamErrorResponse, JsValue]] = {
    val metricEnum = MetricsEnumeration.GET_SA_TAX_PAYER_DETAILS
    val timerContext: Timer.Context =
      metrics.startTimer(metricEnum)

    http
      .GET[Either[UpstreamErrorResponse, JsValue]](
        url("/self-assessment/individual/" + UTR + "/designatory-details/taxpayer"),
        headers = header
      )
      .map { response =>
        timerContext.stop()
        response
      }
      .map(response => handleResponse(response, metricEnum, "ats_getSaTaxPayerDetails", UTR)) recover {
      case error: HttpException => {
        metrics.incrementFailedCounter(metricEnum)
        logger.error(error.message)
        Left(UpstreamErrorResponse(error.message, BAD_GATEWAY, BAD_GATEWAY))
      }
    }
  }
}
