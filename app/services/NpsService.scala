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

import connectors.NpsConnector
import models.paye._
import play.api.Logger
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json.JsResultException
import services.DirectNpsService.npsConnector
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait NpsService {
  def getPayeATSData(nino: String, taxYear: Int)(
    implicit hc: HeaderCarrier): Future[Either[HttpResponse, PayeAtsMiddleTier]]
}

object CachingNpsService extends NpsService {
  override def getPayeATSData(nino: String, taxYear: Int)(
    implicit hc: HeaderCarrier): Future[Either[HttpResponse, PayeAtsMiddleTier]] = ???
}

trait DirectNpsService extends NpsService {
  def npsConnector: NpsConnector

  def getPayeATSData(nino: String, taxYear: Int)(
    implicit hc: HeaderCarrier): Future[Either[HttpResponse, PayeAtsMiddleTier]] =
    npsConnector.connectToPayeTaxSummary(nino, taxYear) map { response =>
      response status match {
        case OK => Right(response.json.as[PayeAtsData].transformToPayeMiddleTier(nino, taxYear))
        case _  => Left(response)
      }
    } recover {
      case e: JsResultException => {
        Logger.error(s"Exception in NpsService parsing Json: $e", e)
        Left(HttpResponse(INTERNAL_SERVER_ERROR))
      }
    }
}

object DirectNpsService extends DirectNpsService {
  override val npsConnector = NpsConnector
}
