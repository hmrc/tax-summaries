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

import com.fasterxml.jackson.core.JsonParseException
import connectors.ODSConnector
import errors.AtsError
import models.{AtsCheck, AtsMiddleTierData, AtsYearList}
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.{JsValue, Json}
import utils.TaxsJsonHelper

import scala.concurrent.Future
import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException}

trait OdsService {

  def jsonHelper: TaxsJsonHelper
  def odsConnector: ODSConnector

  def getPayload(UTR: String, TAX_YEAR: Int)(implicit hc: HeaderCarrier): Future[JsValue] = {
    for {
      taxpayer       <- odsConnector.connectToSATaxpayerDetails(UTR)
      taxSummariesIn <- odsConnector.connectToSelfAssessment(UTR, TAX_YEAR)
    } yield jsonHelper.getAllATSData(taxpayer, taxSummariesIn, UTR, TAX_YEAR)
  } recover {
    case parsingError: JsonParseException =>
      Logger.error("Malformed JSON for tax year: " + TAX_YEAR, parsingError)
      Json.toJson(
        AtsMiddleTierData(2014, None, None, None, None, None, None, None, None, Option(AtsError("JsonParsingError"))))
    case throwable: Throwable =>
      Logger.error("Generic error for tax year: " + TAX_YEAR, throwable)
      Json.toJson(
        AtsMiddleTierData(2014, None, None, None, None, None, None, None, None, Option(AtsError("GenericError"))))
  }

  def getList(UTR: String)(implicit hc: HeaderCarrier): Future[JsValue] =
    for (taxSummariesIn <- odsConnector.connectToSelfAssessmentList(UTR))
      yield Json.toJson(AtsCheck(jsonHelper.hasAtsForPreviousPeriod(taxSummariesIn)))

  def getATSList(UTR: String)(implicit hc: HeaderCarrier): Future[JsValue] = {
    for {
      taxSummariesIn <- odsConnector.connectToSelfAssessmentList(UTR)
      taxpayer       <- odsConnector.connectToSATaxpayerDetails(UTR)
    } yield jsonHelper.createTaxYearJson(taxSummariesIn, UTR, taxpayer)
  } recover {
    case error: JsonParseException =>
      Logger.error("Malformed JSON", error)
      Json.toJson(AtsYearList(UTR, None, None, Some(AtsError("JsonParsingError"))))
    case error: NotFoundException =>
      Logger.error("No ATS error", error)
      Json.toJson(AtsYearList(UTR, None, None, Some(AtsError("NoAtsData"))))
    case error: Throwable =>
      Logger.error("Generic error", error)
      Json.toJson(AtsYearList(UTR, None, None, Some(AtsError(error.getMessage()))))
  }
}

object OdsService extends OdsService {
  override val odsConnector = ODSConnector
  override val jsonHelper: TaxsJsonHelper = new TaxsJsonHelper {}
}
