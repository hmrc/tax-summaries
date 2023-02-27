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

package services

import cats.data.EitherT
import com.google.inject.Inject
import connectors.{ODSConnector, SelfAssessmentODSConnector}
import models._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Request
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import utils.TaxsJsonHelper

import scala.concurrent.{ExecutionContext, Future}

class OdsService @Inject() (
  jsonHelper: TaxsJsonHelper,
  odsConnector: ODSConnector,
  selfAssessmentOdsConnector: SelfAssessmentODSConnector
)(implicit ec: ExecutionContext) {

  def getPayload(UTR: String, TAX_YEAR: Int)(implicit
    hc: HeaderCarrier,
    request: Request[_]
  ): EitherT[Future, UpstreamErrorResponse, JsValue] =
    for {
      taxpayer     <- odsConnector.connectToSATaxpayerDetails(UTR).map(_.json.as[JsValue])
      taxSummaries <- selfAssessmentOdsConnector.connectToSelfAssessment(UTR, TAX_YEAR).map(_.json.as[JsValue])
    } yield jsonHelper.getAllATSData(taxpayer, taxSummaries, UTR, TAX_YEAR)

  def getList(UTR: String)(implicit hc: HeaderCarrier): EitherT[Future, UpstreamErrorResponse, JsValue] =
    odsConnector
      .connectToSelfAssessmentList(UTR)
      .map(response => Json.toJson(AtsCheck(jsonHelper.hasAtsForPreviousPeriod(response.json.as[JsValue]))))

  def getATSList(UTR: String)(implicit hc: HeaderCarrier): EitherT[Future, UpstreamErrorResponse, JsValue] =
    for {
      taxSummaries <- odsConnector.connectToSelfAssessmentList(UTR).map(_.json.as[JsValue])
      taxpayer     <- odsConnector.connectToSATaxpayerDetails(UTR).map(_.json.as[JsValue])
    } yield jsonHelper.createTaxYearJson(taxSummaries, UTR, taxpayer)

}
