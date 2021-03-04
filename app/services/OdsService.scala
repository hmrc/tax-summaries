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
import utils.TaxsJsonHelper

import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException}

class OdsService @Inject()(
  jsonHelper: TaxsJsonHelper,
  odsConnector: ODSConnector
)(implicit ec: ExecutionContext) {

  def getPayload(UTR: String, TAX_YEAR: Int)(implicit hc: HeaderCarrier): Future[Either[ServiceError, JsValue]] =
    withErrorHandling {
      for {
        taxpayer       <- odsConnector.connectToSATaxpayerDetails(UTR)
        taxSummariesIn <- odsConnector.connectToSelfAssessment(UTR, TAX_YEAR)
      } yield {
        Right(jsonHelper.getAllATSData(taxpayer, taxSummariesIn, UTR, TAX_YEAR))
      }
    }

  def getList(UTR: String)(implicit hc: HeaderCarrier): Future[Either[ServiceError, JsValue]] =
    withErrorHandling {
      odsConnector.connectToSelfAssessmentList(UTR) map { taxSummariesIn =>
        Right(Json.toJson(AtsCheck(jsonHelper.hasAtsForPreviousPeriod(taxSummariesIn))))
      }
    }

  def getATSList(UTR: String)(implicit hc: HeaderCarrier): Future[Either[ServiceError, JsValue]] =
    withErrorHandling {
      for {
        taxSummariesIn <- odsConnector.connectToSelfAssessmentList(UTR)
        taxpayer       <- odsConnector.connectToSATaxpayerDetails(UTR)
      } yield Right(jsonHelper.createTaxYearJson(taxSummariesIn, UTR, taxpayer))
    }

  private def withErrorHandling(block: Future[Either[ServiceError, JsValue]]): Future[Either[ServiceError, JsValue]] =
    block recover {
      case error: JsonParseException =>
        Logger.error("Malformed JSON", error)
        Left(JsonParseError(error.getMessage))
      case error: NotFoundException =>
        Logger.error("No ATS error", error)
        Left(NotFoundError(error.getMessage))
      case error =>
        Logger.error("Generic error", error)
        Left(GenericError(error.getMessage))
    }
}
