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

  import OdsService._
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
  ): EitherT[Future, UpstreamErrorResponse, Seq[Int]] = {
    def retrieveSAYears(startYear: Int)(implicit hc: HeaderCarrier, request: Request[_]): Future[InterimResult] =
      (startYear to endYear).foldLeft[Future[InterimResult]](Future(EmptyInterimResult)) { (futureAcc, taxYear) =>
        futureAcc.flatMap {
          case ir @ InterimResult(_, Some(_))        => Future.successful(ir)
          case InterimResult(previousTaxYears, None) =>
            val futureResponseForTaxYear = selfAssessmentOdsConnector.connectToSelfAssessment(utr, taxYear).value map {
              case Right(HttpResponse(NOT_FOUND, _, _)) => EmptyInterimResult
              case Left(errorResponse)                  => InterimResult(previousTaxYears, Some(errorResponse))
              case Right(response)                      =>
                InterimResult(
                  processedYears = if (jsonHelper.getATSCalculations(taxYear, response.json).hasLiability) {
                    Seq(taxYear)
                  } else {
                    Nil
                  },
                  optionUpstreamErrorResponse = None
                )
            }
            futureResponseForTaxYear.map {
              case errorResponse @ InterimResult(_, Some(_)) => errorResponse
              case InterimResult(currentTaxYear, None)       => InterimResult(previousTaxYears ++ currentTaxYear, None)
            }
        }
      }

    val futureResults = retrieveSAYears(startYear = startYear).flatMap {
      case InterimResult(processedYears, Some(_)) =>
        retrieveSAYears(startYear = processedYears.max + 1).map {
          case ir @ InterimResult(_, Some(_))               => ir
          case InterimResult(processedYearsSecondTry, None) =>
            InterimResult(
              processedYears = processedYears ++ processedYearsSecondTry,
              optionUpstreamErrorResponse = None
            )
        }
      case ir @ InterimResult(_, None)            => Future.successful(ir)
    }
    EitherT(
      futureResults.map {
        case InterimResult(_, Some(errorResponse)) =>
          Left(errorResponse)
        case InterimResult(processedYears, None)   => Right(processedYears)
      }
    )
  }

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

object OdsService {
  case class InterimResult(processedYears: Seq[Int], optionUpstreamErrorResponse: Option[UpstreamErrorResponse])

  private final val EmptyInterimResult = InterimResult(Nil, None)
}
