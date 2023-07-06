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

package utils

import cats.data.EitherT
import com.google.inject.Inject
import play.api.Logging
import play.api.http.Status.{INTERNAL_SERVER_ERROR, NOT_FOUND}
import play.api.libs.json.JsValue
import play.api.mvc.Request
import services.OdsService
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}

import scala.concurrent.{ExecutionContext, Future}

class OdsIndividualYearsService @Inject() (odsService: OdsService) extends Logging {

  def getAtsList(utr: String, endYear: Int, numberOfYears: Int)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext,
    request: Request[_]
  ): EitherT[Future, UpstreamErrorResponse, List[Int]] = {
    def individualYearResponse(
      utr: String,
      taxYear: Int
    ): EitherT[Future, Map[Int, UpstreamErrorResponse], Map[Int, Option[JsValue]]] =
      odsService.getPayload(utr, taxYear).transform {
        case Right(value)                                            => Right(Map(taxYear -> Some(value)))
        case Left(UpstreamErrorResponse(_, NOT_FOUND, _, _))         => Right(Map(taxYear -> None))
        case Left(error) if error.statusCode < INTERNAL_SERVER_ERROR =>
          logger.error(error.getMessage(), error)
          Right(Map(taxYear -> None))
        case Left(error)                                             => Left(Map(taxYear -> UpstreamErrorResponse("", error.statusCode)))
      }

    EitherT(
      Future
        .sequence((endYear - numberOfYears to endYear).map { year =>
          individualYearResponse(utr, year).value
        })
        .flatMap { results =>
          val lefts: Map[Int, UpstreamErrorResponse] = results.collect { case Left(x) => x }.flatten.toMap
          val rights: Map[Int, Option[JsValue]]      = results.collect { case Right(x) => x }.flatten.toMap
          (lefts.size match {
            case 0 =>
              EitherT.rightT[Future, Map[Int, UpstreamErrorResponse]](rights)
            case 1 =>
              val failedYear = lefts.head._1
              individualYearResponse(utr: String, failedYear: Int).map { newItem =>
                newItem ++ rights
              }
            case _ =>
              EitherT
                .leftT[Future, Map[Int, Option[JsValue]]](Map(0 -> UpstreamErrorResponse("", INTERNAL_SERVER_ERROR)))
          }).value
        }
        .map {
          case Left(error)     => Left(error.head._2)
          case Right(response) => Right(response.collect { case (key, Some(value)) => (key, value) }.keys.toList.sorted)
        }
    )
  }
}
