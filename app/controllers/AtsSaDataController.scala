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

package controllers

import com.google.inject.Inject
import connectors.SelfAssessmentODSConnector
import controllers.auth.AuthAction
import play.api.Logging
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.OdsService
import uk.gov.hmrc.http.UpstreamErrorResponse
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import utils.{ATSErrorHandler, OdsIndividualYearsService, TaxsJsonHelper}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}

class AtsSaDataController @Inject()(
                                     odsService: OdsService,
                                     odsIndividualYearsService: OdsIndividualYearsService,
                                     atsErrorHandler: ATSErrorHandler,
                                     authAction: AuthAction,
                                     cc: ControllerComponents,
                                     jsonHelper: TaxsJsonHelper,
                                     selfAssessmentOdsConnector: SelfAssessmentODSConnector
                                   )(implicit val ec: ExecutionContext)
  extends BackendController(cc)
    with Logging {

  def hasAts(utr: String): Action[AnyContent] = authAction.async { implicit request =>
    odsService
      .getList(utr)
      .fold(
        error => atsErrorHandler.errorToResponse(error),
        result => Ok(result)
      )
  }

  def getAtsSaData(utr: String, tax_year: Int): Action[AnyContent] = authAction.async { implicit request =>
    odsService
      .getPayload(utr, tax_year)
      .fold(
        error => atsErrorHandler.errorToResponse(error),
        result => Ok(result)
      )
  }

  def getAtsSaList(utr: String, endYear: Int, numberOfYears: Int): Action[AnyContent] = authAction.async {
    implicit request =>
      val singleListForAllYears = odsService
        .getATSList(utr)
        .map { json =>
          (json \ "atsYearList").as[List[Int]]
        }

      val yearData = odsIndividualYearsService.getAtsList(utr: String, endYear: Int, numberOfYears: Int)

      for {
        madeupList <- singleListForAllYears
        individualYearsList <- yearData.bimap(
          _ => UpstreamErrorResponse("", 500),
          json => json.collect { case (key, Some(value)) => (key, value) }.keys.toList
        )
      } yield {
        val differentYears = madeupList.filter(_ > endYear - numberOfYears).diff(individualYearsList)
        if (differentYears.nonEmpty) {
          logger.warn(s"Following Years are different  $differentYears")
        }
      }

      yearData.fold(
        error =>
          atsErrorHandler.errorToResponse(
            error.values.headOption.getOrElse(
              UpstreamErrorResponse("", INTERNAL_SERVER_ERROR)
            )
          ),
        result => {
          val resultEmptyYearsRemoved = result.collect { case (key, Some(value)) => (key, value) }

          val taxpayer = Await.result(
            selfAssessmentOdsConnector.connectToSATaxpayerDetails(utr).map(_.json.as[JsValue]).getOrElse(Json.obj()),
            Duration.Inf
          )

          Ok(
            jsonHelper.createTaxYearJson(
              Json.obj(
                "annualTaxSummaries" ->
                  resultEmptyYearsRemoved.keys.map { year =>
                    Json.obj(
                      "taxYearEnd" -> year,
                      "links" -> List(
                        Json.obj(
                          "rel" -> "details",
                          "href" -> s"https://digital.ws.hmrc.gov.uk/self-assessment/individuals/$utr/annual-tax-summaries/$year"
                        )
                      )
                    )
                  }
              ),
              utr,
              taxpayer
            )
          )
        }
      )
  }
}
