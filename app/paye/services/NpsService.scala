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

package paye.services

import cats.data.EitherT
import cats.implicits._
import com.google.inject.Inject
import common.config.ApplicationConfig
import paye.connectors.NpsConnector
import paye.models
import paye.models.{PayeAtsData, PayeAtsMiddleTier}
import paye.repositories.Repository
import play.api.http.Status.NOT_FOUND
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}

import scala.concurrent.{ExecutionContext, Future}

class NpsService @Inject() (repository: Repository, innerService: DirectNpsService, config: ApplicationConfig)(implicit
  ec: ExecutionContext
) {

  def getAtsPayeDataMultipleYears(nino: String, taxYears: List[Int])(implicit
    hc: HeaderCarrier
  ): EitherT[Future, UpstreamErrorResponse, List[PayeAtsMiddleTier]] =
    taxYears
      .map { year =>
        getPayeATSData(nino, year).transform {
          case Left(UpstreamErrorResponse(_, NOT_FOUND, _, _)) => Right(None)
          case Right(data)                                     => Right(Some(data))
          case Left(error)                                     => Left(error)
        }
      }
      .sequence
      .map(_.flatten)

  def getPayeATSData(nino: String, taxYear: Int)(implicit
    hc: HeaderCarrier
  ): EitherT[Future, UpstreamErrorResponse, PayeAtsMiddleTier] =
    EitherT(
      repository
        .get(nino, taxYear)
        .flatMap {
          case Some(dataMongo) => Future.successful(Right(dataMongo.data))
          case None            => refreshCache(nino, taxYear).value
        }
    )

  private def refreshCache(nino: String, taxYear: Int)(implicit
    hc: HeaderCarrier
  ): EitherT[Future, UpstreamErrorResponse, PayeAtsMiddleTier] =
    for {
      data <- innerService.getPayeATSData(nino, taxYear)
      _    <-
        EitherT[Future, UpstreamErrorResponse, Boolean](
          repository
            .set(models.PayeAtsMiddleTierMongo(s"$nino::$taxYear", data, config.calculateExpiryTime()))
            .map(Right(_))
        )
    } yield data
}

class DirectNpsService @Inject() (applicationConfig: ApplicationConfig, npsConnector: NpsConnector)(implicit
  ec: ExecutionContext
) {
  def getPayeATSData(nino: String, taxYear: Int)(implicit
    hc: HeaderCarrier
  ): EitherT[Future, UpstreamErrorResponse, PayeAtsMiddleTier] =
    npsConnector
      .connectToPayeTaxSummary(nino, taxYear - 1)
      .map(_.json.as[PayeAtsData].transformToPayeMiddleTier(applicationConfig, nino, taxYear))
}
