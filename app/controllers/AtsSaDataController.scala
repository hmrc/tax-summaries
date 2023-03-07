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

import cats.data.EitherT
import com.google.inject.Inject
import controllers.auth.AuthAction
import play.api.libs.json.{JsArray, JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.OdsService
import uk.gov.hmrc.http.UpstreamErrorResponse
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import utils.{ATSErrorHandler, OdsFakeListOfYears}
import play.api.Logging

import scala.concurrent.{ExecutionContext, Future}

class AtsSaDataController @Inject() (
                                      odsService: OdsService,
                                      odsFakeListOfYears: OdsFakeListOfYears,
                                      atsErrorHandler: ATSErrorHandler,
                                      authAction: AuthAction,
                                      cc: ControllerComponents
)(implicit val ec: ExecutionContext)
    extends BackendController(cc) with Logging {

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
      val oldListF: EitherT[Future, UpstreamErrorResponse, List[Int]] = odsService
        .getATSList(utr).map { json =>
        (json \ "annualTaxSummaries").as[JsArray].value.map { summary =>
          (summary \ "taxYearEnd").as[Int]
        }.toList
      }

      val newListF = odsFakeListOfYears.getAtsList(utr: String, endYear: Int, numberOfYears: Int)

      for {
        oldList <- oldListF
        newList <- newListF.bimap(
          _ => UpstreamErrorResponse("", 500),
          json => json.keys.toList
        )
      } yield {
        oldList.diff(newList)
        logger.warn("")
      }


      newListF.fold(
        error => atsErrorHandler.errorToResponse(error.values.headOption.getOrElse(UpstreamErrorResponse("", INTERNAL_SERVER_ERROR))),
        result =>
          Ok(
            Json.obj(
              "annualTaxSummaries" ->
                result.keys.map { year =>
                  Json.obj(
                    "taxYearEnd" -> year,
                    "links"      -> List(
                      Json.obj(
                        "rel"  -> "details",
                        "href" -> s"https://digital.ws.hmrc.gov.uk/self-assessment/individuals/$utr/annual-tax-summaries/$year"
                      )
                    )
                  )
                }
            )
          )
      )
  }
}
