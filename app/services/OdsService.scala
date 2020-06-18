/*
 * Copyright 2020 HM Revenue & Customs
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

import connectors.ODSConnector
import models.AtsCheck
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HeaderCarrier
import utils.TaxsJsonHelper

import scala.concurrent.Future

trait OdsService {

  def jsonHelper: TaxsJsonHelper
  def odsConnector: ODSConnector

  def getPayload(UTR: String, TAX_YEAR: Int)(implicit hc: HeaderCarrier): Future[Option[JsValue]] =
    for {
      taxpayer       <- odsConnector.connectToSATaxpayerDetails(UTR)
      taxSummariesIn <- odsConnector.connectToSelfAssessment(UTR, TAX_YEAR)
    } yield {
      (taxpayer, taxSummariesIn) match {
        case (Some(tp), Some(summaries)) =>
          Some(jsonHelper.getAllATSData(tp, summaries, UTR, TAX_YEAR))
        case _ => None
      }
    }

  def getList(UTR: String)(implicit hc: HeaderCarrier): Future[Option[JsValue]] =
    for (taxSummariesIn <- odsConnector.connectToSelfAssessmentList(UTR))
      yield
        taxSummariesIn.map { summary =>
          Json.toJson(AtsCheck(jsonHelper.hasAtsForPreviousPeriod(summary)))
        }

  def getATSList(UTR: String)(implicit hc: HeaderCarrier): Future[Option[JsValue]] =
    for {
      taxSummariesIn <- odsConnector.connectToSelfAssessmentList(UTR)
      taxpayer       <- odsConnector.connectToSATaxpayerDetails(UTR)
    } yield {
      (taxSummariesIn, taxpayer) match {
        case (Some(summary), Some(tp)) =>
          Some(jsonHelper.createTaxYearJson(summary, UTR, tp))
        case _ => None
      }
    }
}

object OdsService extends OdsService {
  override val odsConnector: ODSConnector = ODSConnector
  override val jsonHelper: TaxsJsonHelper = new TaxsJsonHelper {}
}
