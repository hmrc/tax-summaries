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

import audit.AtsAudit
import com.google.inject.Inject
import config.ApplicationConfig
import models.Audit
import play.api.Logging
import play.api.http.Status.BAD_GATEWAY
import play.api.libs.json.JsValue
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HeaderNames, HttpClient, HttpException, UpstreamErrorResponse}

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ODSConnector @Inject()(
  http: HttpClient,
  atsAudit: AtsAudit,
  applicationConfig: ApplicationConfig
) extends Logging {

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
    auditIdentifier: String,
    utr: String,
    taxYear: Option[Int] = None)(implicit hc: HeaderCarrier): Either[UpstreamErrorResponse, JsValue] = {
    val audit =
      Audit("saRequest", auditIdentifier, auditDetails(utr, taxYear))

    response match {
      case response @ Right(_) =>
        atsAudit.doAudit(audit.copy(eventTypelMessage = audit.eventTypelMessage + "Success"))
        response
      case Left(error) if error.statusCode >= 500 || error.statusCode == 429 => {
        atsAudit.doAudit(audit.copy(eventTypelMessage = audit.eventTypelMessage + "ServiceUnavailable"))
        logger.error(error.message)
        Left(error)
      }
      case Left(error) if error.statusCode == 404 => {
        atsAudit.doAudit(audit.copy(eventTypelMessage = audit.eventTypelMessage + "Failed"))
        logger.info(error.message)
        Left(error)
      }
      case Left(error) => {
        atsAudit.doAudit(audit.copy(eventTypelMessage = audit.eventTypelMessage + "Failed"))
        logger.error(error.message, error)
        Left(error)
      }
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
      .map(response => handleResponse(response, "ats_getSaSelfAssessment", UTR, Some(TAX_YEAR))) recover {
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
      .map(response => handleResponse(response, "ats_getSaSelfAssessmentList", UTR)) recover {
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
      .map(response => handleResponse(response, "ats_getSaTaxPayerDetails", UTR)) recover {
      case error: HttpException => {
        logger.error(error.message)
        Left(UpstreamErrorResponse(error.message, BAD_GATEWAY, BAD_GATEWAY))
      }
    }
}
