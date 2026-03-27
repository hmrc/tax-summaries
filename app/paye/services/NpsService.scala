/*
 * Copyright 2026 HM Revenue & Customs
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
import cats.implicits.*
import com.google.inject.Inject
import common.config.ApplicationConfig
import paye.connectors.NpsConnector
import paye.models
import paye.models.{PayeAtsData, PayeAtsMiddleTier}
import play.api.http.Status.{NOT_FOUND, UNPROCESSABLE_ENTITY}
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}

import scala.concurrent.{ExecutionContext, Future}

class NpsService @Inject() (npsConnector: NpsConnector, config: ApplicationConfig)(implicit
  ec: ExecutionContext
) {

  def getAtsPayeDataMultipleYears(nino: String, taxYears: List[Int])(implicit
    hc: HeaderCarrier
  ): EitherT[Future, UpstreamErrorResponse, List[PayeAtsMiddleTier]] =
    taxYears
      .map { year =>
        getPayeATSData(nino, year).transform {
          case Left(UpstreamErrorResponse(_, NOT_FOUND, _, _))            => Right(None) // Nino not found
          case Left(UpstreamErrorResponse(_, UNPROCESSABLE_ENTITY, _, _)) => Right(None) // No ATS data
          case Right(data)                                                => Right(Some(data))
          case Left(error)                                                => Left(error)
        }
      }
      .sequence
      .map(_.flatten)

  def getPayeATSData(nino: String, taxYear: Int)(implicit
    hc: HeaderCarrier
  ): EitherT[Future, UpstreamErrorResponse, PayeAtsMiddleTier] =
    npsConnector
      .connectToPayeTaxSummary(nino, taxYear - 1)
      .map(_.json.as[PayeAtsData].transformToPayeMiddleTier(config, nino, taxYear))
}
