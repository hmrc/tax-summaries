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

package services

import cats.data.EitherT
import com.google.inject.Inject
import connectors.ODSConnector
import models._
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import utils.TaxsJsonHelper

import scala.concurrent.{ExecutionContext, Future}

class OdsService @Inject() (
  jsonHelper: TaxsJsonHelper,
  odsConnector: ODSConnector
)(implicit ec: ExecutionContext) {

  def getPayload(UTR: String, TAX_YEAR: Int)(implicit
    hc: HeaderCarrier
  ): Future[Either[UpstreamErrorResponse, JsValue]] =
    (for {
      taxpayer     <- EitherT(odsConnector.connectToSATaxpayerDetails(UTR))
      taxSummaries <- EitherT(odsConnector.connectToSelfAssessment(UTR, TAX_YEAR))
    } yield
    //println(taxSummaries.as[TaxSummaryLiability].nationality)
    jsonHelper.getAllATSData(taxpayer, taxSummaries, UTR, TAX_YEAR)).value

  def getList(UTR: String)(implicit hc: HeaderCarrier): Future[Either[UpstreamErrorResponse, JsValue]] =
    odsConnector.connectToSelfAssessmentList(UTR) map {
      case Right(value) => Right(Json.toJson(AtsCheck(jsonHelper.hasAtsForPreviousPeriod(value))))
      case Left(error)  => Left(error)
    }

  def getATSList(UTR: String)(implicit hc: HeaderCarrier): Future[Either[UpstreamErrorResponse, JsValue]] =
    (for {
      taxSummaries <- EitherT(odsConnector.connectToSelfAssessmentList(UTR))
      taxpayer     <- EitherT(odsConnector.connectToSATaxpayerDetails(UTR))
    } yield jsonHelper.createTaxYearJson(taxSummaries, UTR, taxpayer)).value

}
