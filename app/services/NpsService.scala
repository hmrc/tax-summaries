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

import com.google.inject.Inject
import config.ApplicationConfig
import connectors.NpsConnector
import models.paye._
import play.api.Logger
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import play.api.libs.json.JsResultException
import repositories.Repository
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class NpsService @Inject()(repository: Repository, innerService: DirectNpsService) {

  def getPayeATSData(nino: String, taxYear: Int)(
    implicit hc: HeaderCarrier): Future[Either[HttpResponse, PayeAtsMiddleTier]] =
    repository
      .get(nino, taxYear)
      .flatMap {
        case Some(data) => Future.successful(Right(data))
        case None       => refreshCache(nino, taxYear)
      }
      .recover {
        case ex =>
          Logger.error("Failed to fetch data from cache", ex)
          Left(HttpResponse(INTERNAL_SERVER_ERROR))
      }

  private def refreshCache(nino: String, taxYear: Int)(
    implicit hc: HeaderCarrier): Future[Either[HttpResponse, PayeAtsMiddleTier]] =
    innerService
      .getPayeATSData(nino, taxYear)
      .flatMap {
        case Left(response) => Future.successful(Left(response))
        case Right(data) =>
          repository
            .set(nino, taxYear, data)
            .map(_ => Right(data))
            .recover { case _ => Left(HttpResponse(INTERNAL_SERVER_ERROR)) }
      }
}

class DirectNpsService @Inject()(applicationConfig: ApplicationConfig, npsConnector: NpsConnector) {
  def getPayeATSData(nino: String, taxYear: Int)(
    implicit hc: HeaderCarrier): Future[Either[HttpResponse, PayeAtsMiddleTier]] =
    npsConnector.connectToPayeTaxSummary(nino, taxYear - 1, hc) map { response =>
      response status match {
        case OK => Right(response.json.as[PayeAtsData].transformToPayeMiddleTier(applicationConfig, nino, taxYear))
        case _  => Left(response)
      }
    } recover {
      case e: JsResultException => {
        Logger.error(s"Exception in NpsService parsing Json: $e", e)
        Left(HttpResponse(INTERNAL_SERVER_ERROR))
      }
    }
}
