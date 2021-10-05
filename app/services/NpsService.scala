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
import models.{BadRequestError, DownstreamClientError, DownstreamServerError, NotFoundError, ServiceError}
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, NOT_FOUND}
import repositories.Repository
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class NpsService @Inject()(repository: Repository, innerService: DirectNpsService) {

  def getPayeATSData(nino: String, taxYear: Int)(
    implicit hc: HeaderCarrier): Future[Either[ServiceError, PayeAtsMiddleTier]] =
    repository
      .get(nino, taxYear)
      .flatMap {
        case Some(data) => Future.successful(Right(data))
        case None       => refreshCache(nino, taxYear)
      }

  private def refreshCache(nino: String, taxYear: Int)(
    implicit hc: HeaderCarrier): Future[Either[ServiceError, PayeAtsMiddleTier]] =
    innerService
      .getPayeATSData(nino, taxYear)
      .flatMap {
        case Left(response) => Future.successful(Left(response))
        case Right(data) =>
          repository
            .set(nino, taxYear, data)
            .map(_ => Right(data))
      }
}

class DirectNpsService @Inject()(applicationConfig: ApplicationConfig, npsConnector: NpsConnector) {
  def getPayeATSData(nino: String, taxYear: Int)(
    implicit hc: HeaderCarrier): Future[Either[ServiceError, PayeAtsMiddleTier]] =
    npsConnector.connectToPayeTaxSummary(nino, taxYear - 1) map {
      case Right(value) =>
        Right(value.json.as[PayeAtsData].transformToPayeMiddleTier(applicationConfig, nino, taxYear))
      case Left(error) if error.statusCode == NOT_FOUND             => Left(NotFoundError(error.message))
      case Left(error) if error.statusCode == BAD_REQUEST           => Left(BadRequestError(error.message))
      case Left(error) if error.statusCode >= INTERNAL_SERVER_ERROR => Left(DownstreamServerError(error.message))
      case Left(error)                                              => Left(DownstreamClientError(error.message, error))
    }
}
