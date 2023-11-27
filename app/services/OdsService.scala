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
import play.api.Logger
import play.api.http.Status.{INTERNAL_SERVER_ERROR, NOT_FOUND}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Request
import services.OdsService._
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, UpstreamErrorResponse}
import uk.gov.hmrc.time.TaxYear
import utils.TaxsJsonHelper

import scala.concurrent.{ExecutionContext, Future}

class OdsService @Inject()(
                            jsonHelper: TaxsJsonHelper,
                            selfAssessmentOdsConnector: SelfAssessmentODSConnector
                          )(implicit ec: ExecutionContext) {
  private val logger = Logger(getClass.getName)

  def getPayload(utr: String, TAX_YEAR: Int)(implicit
                                             hc: HeaderCarrier,
                                             request: Request[_]
  ): EitherT[Future, UpstreamErrorResponse, JsValue] =
    for {
      taxpayer <- selfAssessmentOdsConnector
        .connectToSATaxpayerDetails(utr)
        .transform {
          case Right(response) if response.status == NOT_FOUND =>
            Left(UpstreamErrorResponse("NOT_FOUND", NOT_FOUND))
          case Right(response) => Right(response.json.as[JsValue])
          case Left(error) => Left(error)
        }
      taxSummaries <- selfAssessmentOdsConnector.connectToSelfAssessment(utr, TAX_YEAR).transform {
        case Right(response) if response.status == NOT_FOUND =>
          Left(UpstreamErrorResponse("NOT_FOUND", NOT_FOUND))
        case Right(response) => Right(response.json.as[JsValue])
        case Left(error) => Left(error)
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

  private def findYearsWithTaxLiabilityInclRetryOnce(
                                                      utr: String,
                                                      startYear: Int,
                                                      endYear: Int
                                                    )(implicit
                                                      hc: HeaderCarrier,
                                                      request: Request[_]
                                                    ): EitherT[Future, UpstreamErrorResponse, Seq[Int]] = {
    val connectToSA: Int => Future[InterimResult] = taxYear =>
      selfAssessmentOdsConnector.connectToSelfAssessment(utr, taxYear).value map {
        case Right(HttpResponse(NOT_FOUND, _, _)) => NotFoundInterimResult
        case Left(error) if error.statusCode < INTERNAL_SERVER_ERROR =>
          logger.error(error.getMessage(), error)
          EmptyInterimResult
        case Left(errorResponse) =>
          InterimResult(
            processedYears = Nil,
            failureInfo = Seq(FailureInfo(errorResponse, taxYear))
          )
        case Right(response) =>
          InterimResult(
            processedYears = getTaxYearIfLiable(taxYear, response.json),
            failureInfo = Nil
          )
      }

    val futureResult = findAllYears(endYear to startYear by -1, connectToSA).flatMap {
      case InterimResult(processedYears, Seq(failureInfo), notFoundCount) =>
        findAllYears(failureInfo.failedYear to failureInfo.failedYear by -1, connectToSA)
          .map(ir =>
            ir.copy(
              processedYears = processedYears ++ ir.processedYears,
              failureInfo = handleSecondFailure(ir.failureInfo),
              notFoundCount = notFoundCount + ir.notFoundCount
            )
          )
      case ir => Future.successful(ir)
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
      endYear = endYear
    ).map(_.sorted)

  def hasATS(
              utr: String
            )(implicit hc: HeaderCarrier, request: Request[_]): EitherT[Future, UpstreamErrorResponse, JsValue] =
    findYearsWithTaxLiabilityInclRetryOnce(
      utr = utr,
      startYear = TaxYear.current.startYear - 4,
      endYear = TaxYear.current.startYear
    ).map(taxYears => Json.obj("has_ats" -> taxYears.nonEmpty))

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
}

object OdsService {
  private case class FailureInfo(upstreamErrorResponse: UpstreamErrorResponse, failedYear: Int)

  private case class InterimResult(processedYears: Seq[Int], failureInfo: Seq[FailureInfo], notFoundCount: Int = 0)

  private def toEither(years: Int): PartialFunction[InterimResult, Either[UpstreamErrorResponse, Seq[Int]]] = {
    case InterimResult(_, seqFailureInfo, _) if seqFailureInfo.nonEmpty =>
      Left(seqFailureInfo.head.upstreamErrorResponse)
    case InterimResult(_, _, notFoundCount) if notFoundCount > years =>
      Left(UpstreamErrorResponse("Not_Found", NOT_FOUND))
    case InterimResult(processedYears, _, _) => Right(processedYears)
  }

  private def handleSecondFailure(failureInfo: Seq[FailureInfo]): Seq[FailureInfo] =
    failureInfo.headOption match {
      case Some(fi) =>
        Seq(fi.copy(upstreamErrorResponse = UpstreamErrorResponse("Multiple upstream failures", INTERNAL_SERVER_ERROR)))
      case _ => failureInfo
    }

  private final val EmptyInterimResult = InterimResult(Nil, Nil)
  private final val NotFoundInterimResult = InterimResult(Nil, Nil, 1)
}
