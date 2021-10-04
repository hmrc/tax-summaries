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

package services

import cats.data.EitherT
import com.fasterxml.jackson.core.JsonParseException
import com.google.inject.Inject
import connectors.ODSConnector
import models._
import play.api.Logger
import play.api.http.Status.NOT_FOUND
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import utils.TaxsJsonHelper

import scala.concurrent.{ExecutionContext, Future}

class OdsService @Inject()(
  jsonHelper: TaxsJsonHelper,
  odsConnector: ODSConnector
)(implicit ec: ExecutionContext) {

  def getPayload(UTR: String, TAX_YEAR: Int)(implicit hc: HeaderCarrier): Future[Either[ServiceError, JsValue]] =
    withErrorHandling {
      (for {
        taxpayer     <- EitherT(odsConnector.connectToSATaxpayerDetails(UTR))
        taxSummaries <- EitherT(odsConnector.connectToSelfAssessment(UTR, TAX_YEAR))
      } yield {
        jsonHelper.getAllATSData(taxpayer, taxSummaries, UTR, TAX_YEAR)
      }).value.map {
        case Right(value)                                                        => Right(value)
        case Left(error: UpstreamErrorResponse) if error.statusCode == NOT_FOUND => Left(NotFoundError(error.message))
      }
    }

  def getList(UTR: String)(implicit hc: HeaderCarrier): Future[Either[ServiceError, JsValue]] =
    withErrorHandling {
      odsConnector.connectToSelfAssessmentList(UTR) map {
        case Right(value) =>
          Right(Json.toJson(AtsCheck(jsonHelper.hasAtsForPreviousPeriod(value))))
        case Left(error: UpstreamErrorResponse) if error.statusCode == NOT_FOUND => Left(NotFoundError(error.message))
      }
    }

  def getATSList(UTR: String)(implicit hc: HeaderCarrier): Future[Either[ServiceError, JsValue]] =
    withErrorHandling {
      (for {
        taxSummaries <- EitherT(odsConnector.connectToSelfAssessmentList(UTR))
        taxpayer     <- EitherT(odsConnector.connectToSATaxpayerDetails(UTR))
      } yield {
        jsonHelper.createTaxYearJson(taxSummaries, UTR, taxpayer)
      }).value.map {
        case Right(value)                                                        => Right(value)
        case Left(error: UpstreamErrorResponse) if error.statusCode == NOT_FOUND => Left(NotFoundError(error.message))
      }
    }

  private def withErrorHandling(block: Future[Either[ServiceError, JsValue]]): Future[Either[ServiceError, JsValue]] = {
    val logger = Logger(getClass.getName)
    block recover {
      case error: JsonParseException =>
        logger.error("Malformed JSON", error)
        Left(JsonParseError(error.getMessage))
      case error =>
        logger.error("Generic error", error)
        Left(GenericError(error.getMessage))
    }
  }
}
