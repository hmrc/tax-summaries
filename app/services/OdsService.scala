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
import services.OdsService._
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, UpstreamErrorResponse}
import uk.gov.hmrc.time.TaxYear
import utils.TaxsJsonHelper

import scala.concurrent.{ExecutionContext, Future}

class OdsService @Inject() (
  jsonHelper: TaxsJsonHelper,
  selfAssessmentOdsConnector: SelfAssessmentODSConnector
)(implicit ec: ExecutionContext) {
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

  private def getTaxYearIfLiable(taxYear: Int, json: JsValue): Seq[Int] =
    if (jsonHelper.getATSCalculations(taxYear, json).hasLiability) {
      Seq(taxYear)
    } else {
      Nil
    }

  // format: off
  private def retrieveSATaxYears(utr: String, yearToStartFrom: Int, yearToEndAt: Int, stopWhenFound: Boolean)(implicit
                                                                                                              hc: HeaderCarrier,
                                                                                                              request: Request[_]
  ): Future[InterimResult] = {
    def retrieveSATaxYear(taxYear: Int): PartialFunction[InterimResult, Future[InterimResult]] = {
      case InterimResult(previousTaxYears, Nil, previousNotFoundCount) =>
        val futureResponseForTaxYear = selfAssessmentOdsConnector.connectToSelfAssessment(utr, taxYear).value map {
          case Right(HttpResponse(NOT_FOUND, _, _)) => NotFoundInterimResult
          case Left(errorResponse) => InterimResult(previousTaxYears, Seq(FailureInfo(errorResponse, taxYear)))
          case Right(response) =>
            InterimResult(processedYears = getTaxYearIfLiable(taxYear, response.json), failureInfo = Nil)
        }
        futureResponseForTaxYear.map {
          case errorResponse@InterimResult(_, failureInfo, _) if failureInfo.nonEmpty => errorResponse
          case InterimResult(currentTaxYear, _, notFoundCount) =>
            InterimResult(previousTaxYears ++ currentTaxYear, Nil, previousNotFoundCount + notFoundCount)
        }
      case ir => Future.successful(ir)
    }

    val taxYearRange = yearToStartFrom to yearToEndAt by -1
    if (stopWhenFound) {
      taxYearRange.foldLeft[Future[InterimResult]](Future(EmptyInterimResult)) { (futureAcc, taxYear) =>
        futureAcc.flatMap {
          case ir@InterimResult(Seq(_), Nil, _) => Future.successful(ir)
          case ir => retrieveSATaxYear(taxYear)(ir)
        }
      }
    } else {
      Future
        .sequence(taxYearRange.map(taxYear => retrieveSATaxYear(taxYear)(EmptyInterimResult)))
        .map { seqInterimResult =>
          seqInterimResult.count(_.failureInfo.nonEmpty) match {
            case 0 => InterimResult(
              processedYears = seqInterimResult.flatMap(_.processedYears),
              failureInfo = Nil,
              notFoundCount = seqInterimResult.map(_.notFoundCount).sum
            )
            case _ => InterimResult(
              processedYears = seqInterimResult.flatMap(_.processedYears),
              failureInfo = seqInterimResult.flatMap(_.failureInfo),
              notFoundCount = seqInterimResult.map(_.notFoundCount).sum
            )
          }
        }
    }
  }

  private def findYearsWithTaxSummaryData(utr: String, startYear: Int, endYear: Int, stopWhenFound: Boolean)(implicit
                                                                                                             hc: HeaderCarrier,
                                                                                                             request: Request[_]
  ): EitherT[Future, UpstreamErrorResponse, Seq[Int]] = {
    val futureResult = {
      retrieveSATaxYears(
        utr = utr,
        yearToStartFrom = endYear,
        yearToEndAt = startYear,
        stopWhenFound = stopWhenFound
      ).flatMap {
        case InterimResult(processedYears, Seq(failureInfo), _) =>
          retrieveSATaxYears(
            utr = utr,
            yearToStartFrom = failureInfo.failedYear,
            yearToEndAt = if (stopWhenFound) startYear else failureInfo.failedYear,
            stopWhenFound = stopWhenFound
          ).map {
            case ir@InterimResult(processedYearsSecondTry, Nil, _) =>
              ir copy (processedYears = processedYears ++ processedYearsSecondTry)
            case ir => ir
          }
        case ir => Future.successful(ir)
      }
    }
    val totalTaxYears = endYear - startYear
    EitherT(
      futureResult.map {
        case InterimResult(_, seqFailureInfo, _) if seqFailureInfo.nonEmpty =>
          Left(seqFailureInfo.head.upstreamErrorResponse)
        case InterimResult(_, _, notFoundCount) if notFoundCount > totalTaxYears =>
          Left(UpstreamErrorResponse("Not_Found", NOT_FOUND))
        case InterimResult(processedYears, _, _) => Right(processedYears)
      }
    )
  }

  // TODO: If there are no liabilities in ANY of the years then return Left(UpstreamErrorResponse("Not_Found", NOT_FOUND))
  // This should fix the one remaining failing journey test (due to UTR being displayed in info section on page) + 
  // the failing unit test

  def getATSList(utr: String, startYear: Int, endYear: Int)(implicit
                                                            hc: HeaderCarrier,
                                                            request: Request[_]
  ): EitherT[Future, UpstreamErrorResponse, Seq[Int]] =
    findYearsWithTaxSummaryData(utr = utr, startYear = startYear, endYear = endYear, stopWhenFound = false).map(
      _.sorted
    )

  def hasATS(
              utr: String
            )(implicit hc: HeaderCarrier, request: Request[_]): EitherT[Future, UpstreamErrorResponse, JsValue] =
    findYearsWithTaxSummaryData(
      utr = utr,
      startYear = TaxYear.current.startYear - 4,
      endYear = TaxYear.current.startYear,
      stopWhenFound = true
    ).map { taxYears =>
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
        case Right(response) =>
          Right(response.json.as[JsValue])
        case Left(error) => Left(error)
      }
  // format: on
}

object OdsService {
  case class FailureInfo(upstreamErrorResponse: UpstreamErrorResponse, failedYear: Int)

  case class InterimResult(processedYears: Seq[Int], failureInfo: Seq[FailureInfo], notFoundCount: Int = 0)

  private final val EmptyInterimResult    = InterimResult(Nil, Nil)
  private final val NotFoundInterimResult = InterimResult(Nil, Nil, 1)
}
