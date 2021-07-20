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

import com.fasterxml.jackson.core.JsonParseException
import com.google.inject.Inject
import connectors.ODSConnector
import models._
import play.api.Logger
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HeaderCarrier
import utils.TaxsJsonHelper

import scala.concurrent.{ExecutionContext, Future}

class OdsService @Inject()(
  jsonHelper: TaxsJsonHelper,
  odsConnector: ODSConnector
)(implicit ec: ExecutionContext) {

  def getPayload(UTR: String, TAX_YEAR: Int)(implicit hc: HeaderCarrier): Future[Either[ServiceError, JsValue]] =
    withErrorHandling {
      for {
        taxpayerOpt       <- odsConnector.connectToSATaxpayerDetails(UTR)
        taxSummariesInOpt <- odsConnector.connectToSelfAssessment(UTR, TAX_YEAR)
      } yield {
        (taxpayerOpt, taxSummariesInOpt) match {
          case (Some(taxpayer), Some(annualSummary)) =>
            Right(jsonHelper.getAllATSData(taxpayer, annualSummary, UTR, TAX_YEAR))
          case (None, _) => Left(NotFoundError("Could not find Taxpayer details"))
          case (_, None) => Left(NotFoundError("Could not find annual summary"))
        }
      }
    }

  def getList(UTR: String)(implicit hc: HeaderCarrier): Future[Either[ServiceError, JsValue]] =
    withErrorHandling {
      odsConnector.connectToSelfAssessmentList(UTR) map {
        case Some(taxSummariesIn) =>
          Right(Json.toJson(AtsCheck(jsonHelper.hasAtsForPreviousPeriod(taxSummariesIn))))
        case None => Left(NotFoundError("Could not find annual summary"))
      }
    }

  def getATSList(UTR: String)(implicit hc: HeaderCarrier): Future[Either[ServiceError, JsValue]] =
    withErrorHandling {
      for {
        taxSummariesInOpt <- odsConnector.connectToSelfAssessmentList(UTR)
        taxpayerOpt       <- odsConnector.connectToSATaxpayerDetails(UTR)
      } yield {
        (taxpayerOpt, taxSummariesInOpt) match {
          case (Some(taxpayer), Some(annualSummary)) =>
            Right(jsonHelper.createTaxYearJson(annualSummary, UTR, taxpayer))
          case (None, _) => Left(NotFoundError("Could not find Taxpayer details"))
          case (_, None) => Left(NotFoundError("Could not find annual summaries"))
        }
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
