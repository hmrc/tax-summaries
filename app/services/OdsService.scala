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
import models._
import play.api.http.Status.NOT_FOUND
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Request
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
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

  private val tempStubbedListData = Json.parse("""{
      |    "annualTaxSummaries":
      |      [
      |        {
      |          "taxYearEnd":2015,
      |          "links":
      |            [
      |              {"rel":"details", "href":"/self-assessment/individuals/1130492359/annual-tax-summaries/2015"}
      |            ]
      |        },
      |        {
      |          "taxYearEnd":2020,
      |          "links":
      |          [
      |            {"rel":"details", "href":"/self-assessment/individuals/1130492359/annual-tax-summaries/2020"}
      |          ]
      |        },
      |        {
      |          "taxYearEnd":2023,
      |          "links":
      |          [
      |            {"rel":"details", "href":"/self-assessment/individuals/1130492359/annual-tax-summaries/2023"}
      |          ]
      |        }
      |      ],
      |    "links":
      |      [
      |        {"rel":"self", "href":"/self-assessment/individuals/1130492359/annual-tax-summaries"}
      |      ]
      |  }""".stripMargin)

  def getList(
    utr: String
  )(implicit hc: HeaderCarrier, request: Request[_]): EitherT[Future, UpstreamErrorResponse, JsValue] =
//    selfAssessmentOdsConnector
//      .connectToSelfAssessmentList(utr)
//      .transform {
//        case Right(response) if response.status == NOT_FOUND =>
//          Left(UpstreamErrorResponse("NOT_FOUND", NOT_FOUND))
//        case Right(response)                                 =>
    EitherT.rightT(Json.toJson(AtsCheck(jsonHelper.hasAtsForPreviousPeriod(tempStubbedListData))))

  //        case Left(error)                                     => Left(error)
//      }

  def getATSList(
    utr: String
  )(implicit hc: HeaderCarrier, request: Request[_]): EitherT[Future, UpstreamErrorResponse, JsValue] = {
    //EitherT[Future, UpstreamErrorResponse, HttpResponse]
    val taxSummaries = Json.toJson(tempStubbedListData)
    for {
//      taxSummaries <- selfAssessmentOdsConnector.connectToSelfAssessmentList(utr).transform {
//                        case Right(response) if response.status == NOT_FOUND =>
//                          Left(UpstreamErrorResponse("Not_Found", NOT_FOUND))
//                        case Right(response)                                 =>
//                          Right(response.json.as[JsValue])
//                        case Left(error)                                     => Left(error)
//                      }
      taxpayer <- selfAssessmentOdsConnector
                    .connectToSATaxpayerDetails(utr)
                    .transform {
                      case Right(response) if response.status == NOT_FOUND =>
                        Left(UpstreamErrorResponse("Not_Found", NOT_FOUND))
                      case Right(response)                                 =>
                        Right(response.json.as[JsValue])
                      case Left(error)                                     => Left(error)
                    }
    } yield jsonHelper.createTaxYearJson(taxSummaries, utr, taxpayer)
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
