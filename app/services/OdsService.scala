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

  private val findAllYears: (Range, Int => Future[InterimResult]) => Future[InterimResult] = (range, connectToSA) =>
    Future.sequence(range.map(taxYear => connectToSA(taxYear))).map { seqInterimResult =>
      InterimResult(
        processedYears = seqInterimResult.flatMap(_.processedYears),
        failureInfo = seqInterimResult.flatMap(_.failureInfo),
        notFoundCount = seqInterimResult.map(_.notFoundCount).sum
      )
    }

  private val findYearThenStop: (Range, Int => Future[InterimResult]) => Future[InterimResult] = (range, connectToSA) =>
    range.foldLeft[Future[InterimResult]](Future(EmptyInterimResult)) { (futureAcc, taxYear) =>
      futureAcc.flatMap {
        case ir @ InterimResult(Seq(_), Nil, _) => Future.successful(ir)
        case ir                                 =>
          ir match {
            case InterimResult(previousTaxYears, Nil, previousNotFoundCount) =>
              connectToSA(taxYear).map {
                case errorResponse @ InterimResult(_, failureInfo, _) if failureInfo.nonEmpty => errorResponse
                case InterimResult(currentTaxYear, _, notFoundCount)                          =>
                  InterimResult(
                    processedYears = previousTaxYears ++ currentTaxYear,
                    failureInfo = Nil,
                    notFoundCount = previousNotFoundCount + notFoundCount
                  )
              }
            case ir                                                          => Future.successful(ir)
          }
      }
    }

  private def findYearsWithTaxLiabilityInclRetryOnce(
    utr: String,
    startYear: Int,
    endYear: Int,
    findYears: (Range, Int => Future[InterimResult]) => Future[InterimResult],
    resumeUntil: (Int, Int) => Int
  )(implicit
    hc: HeaderCarrier,
    request: Request[_]
  ): EitherT[Future, UpstreamErrorResponse, Seq[Int]] = {
    val connectToSA: Int => Future[InterimResult] = taxYear =>
      selfAssessmentOdsConnector.connectToSelfAssessment(utr, taxYear).value map {
        case Right(HttpResponse(NOT_FOUND, _, _)) => NotFoundInterimResult
        case Left(errorResponse)                  =>
          InterimResult(
            processedYears = Nil,
            failureInfo = Seq(FailureInfo(errorResponse, taxYear))
          )
        case Right(response)                      =>
          InterimResult(
            processedYears = getTaxYearIfLiable(taxYear, response.json),
            failureInfo = Nil
          )
      }

    val futureResult = findYears(endYear to startYear by -1, connectToSA).flatMap {
      case InterimResult(processedYears, Seq(failureInfo), notFoundCount) =>
        findYears(failureInfo.failedYear to resumeUntil(failureInfo.failedYear, startYear) by -1, connectToSA)
          .map(ir =>
            ir.copy(
              processedYears = processedYears ++ ir.processedYears,
              notFoundCount = notFoundCount + ir.notFoundCount
            )
          )

      case ir =>
        Future.successful(ir)
    }

    EitherT(
      futureResult.map(toEither(endYear - startYear))
    )
  }

  def getATSList(utr: String, startYear: Int, endYear: Int)(implicit
    hc: HeaderCarrier,
    request: Request[_]
  ): EitherT[Future, UpstreamErrorResponse, Seq[Int]] =
    findYearsWithTaxLiabilityInclRetryOnce(
      utr = utr,
      startYear = startYear,
      endYear = endYear,
      findYears = findAllYears,
      resumeUntil = (failedYear, _) => failedYear
    )
      .map(_.sorted)

  def hasATS(
    utr: String
  )(implicit hc: HeaderCarrier, request: Request[_]): EitherT[Future, UpstreamErrorResponse, JsValue] =
    findYearsWithTaxLiabilityInclRetryOnce(
      utr = utr,
      startYear = TaxYear.current.startYear - 4,
      endYear = TaxYear.current.startYear,
      findYears = findYearThenStop,
      resumeUntil = (_, startYear) => startYear
    ).map(taxYears => Json.obj("has_ats" -> taxYears.nonEmpty))

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
  private case class FailureInfo(upstreamErrorResponse: UpstreamErrorResponse, failedYear: Int)

  private case class InterimResult(processedYears: Seq[Int], failureInfo: Seq[FailureInfo], notFoundCount: Int = 0)

  private def toEither(years: Int): PartialFunction[InterimResult, Either[UpstreamErrorResponse, Seq[Int]]] = {
    case InterimResult(_, seqFailureInfo, _) if seqFailureInfo.nonEmpty =>
      Left(seqFailureInfo.head.upstreamErrorResponse)
    case InterimResult(_, _, notFoundCount) if notFoundCount > years    =>
      Left(UpstreamErrorResponse("Not_Found", NOT_FOUND))
    case InterimResult(processedYears, _, _)                            => Right(processedYears)
  }

  private final val EmptyInterimResult    = InterimResult(Nil, Nil)
  private final val NotFoundInterimResult = InterimResult(Nil, Nil, 1)
}
