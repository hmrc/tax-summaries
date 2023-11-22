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
import connectors.SelfAssessmentODSConnector
import play.api.http.Status.NOT_FOUND
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Request
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, UpstreamErrorResponse}
import uk.gov.hmrc.time.TaxYear
import utils.TaxsJsonHelper

import scala.concurrent.{ExecutionContext, Future}

class OdsService @Inject() (
  jsonHelper: TaxsJsonHelper,
  selfAssessmentOdsConnector: SelfAssessmentODSConnector
)(implicit ec: ExecutionContext) {

  /* Copy of getAtsList from removed OdsIndividualYearsService:-
  Use this to add the retry code into below
  
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
   */

  def getPayload(utr: String, TAX_YEAR: Int)(implicit
    hc: HeaderCarrier,
    request: Request[_]
  ): EitherT[Future, UpstreamErrorResponse, JsValue] =
    for {
      taxpayer     <- selfAssessmentOdsConnector
                        .connectToSATaxpayerDetails(utr)
                        .transform {
                          case Right(response) if response.status == NOT_FOUND =>
                            Left(UpstreamErrorResponse("NOT_FOUND", NOT_FOUND))
                          case Right(response)                                 => Right(response.json.as[JsValue])
                          case Left(error)                                     => Left(error)
                        }
      taxSummaries <- selfAssessmentOdsConnector.connectToSelfAssessment(utr, TAX_YEAR).transform {
                        case Right(response) if response.status == NOT_FOUND =>
                          Left(UpstreamErrorResponse("NOT_FOUND", NOT_FOUND))
                        case Right(response)                                 => Right(response.json.as[JsValue])
                        case Left(error)                                     => Left(error)
                      }
    } yield jsonHelper.getAllATSData(taxpayer, taxSummaries, utr, TAX_YEAR)

  def getATSList(utr: String, startYear: Int, endYear: Int)(implicit
    hc: HeaderCarrier,
    request: Request[_]
  ): EitherT[Future, UpstreamErrorResponse, Seq[Int]] =
    EitherT(
      (startYear to endYear).foldLeft[Future[Either[UpstreamErrorResponse, Seq[Int]]]](Future(Right(Nil))) {
        (futureAcc, taxYear) =>
          futureAcc.flatMap {
            case a @ Left(_)             => Future.successful(a)
            case Right(previousTaxYears) =>
              val futureResponseForTaxYear =
                selfAssessmentOdsConnector.connectToSelfAssessment(utr, taxYear).value map {
                  case Right(HttpResponse(NOT_FOUND, _, _)) => Right(Nil)
                  case Left(errorResponse)                  => Left(errorResponse)
                  case Right(response)                      =>
                    Right(
                      if (jsonHelper.getATSCalculations(taxYear, response.json).hasLiability) {
                        Seq(taxYear)
                      } else {
                        Nil
                      }
                    )
                }
              futureResponseForTaxYear.map {
                case errorResponse @ Left(_) =>
                  errorResponse
                case Right(currentTaxYear)   => Right(previousTaxYears ++ currentTaxYear)
              }
          }
      }
    )

  def hasATS(
    utr: String
  )(implicit hc: HeaderCarrier, request: Request[_]): EitherT[Future, UpstreamErrorResponse, JsValue] =
    getATSList(utr, TaxYear.current.startYear - 4, TaxYear.current.startYear).map { taxYears =>
      Json.obj("has_ats" -> taxYears.nonEmpty)
    }

  def connectToSATaxpayerDetails(
    utr: String
  )(implicit hc: HeaderCarrier, request: Request[_]): EitherT[Future, UpstreamErrorResponse, JsValue] =
    selfAssessmentOdsConnector
      .connectToSATaxpayerDetails(utr)
      .transform {
        case Right(response) if response.status == NOT_FOUND =>
          Left(UpstreamErrorResponse("NOT_FOUND", NOT_FOUND))
        case Right(response)                                 =>
          Right(response.json.as[JsValue])
        case Left(error)                                     => Left(error)
      }

}
