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
import controllers.auth.AuthJourney
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.NpsService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import utils.ATSErrorHandler

import scala.concurrent.ExecutionContext

class AtsPayeDataController @Inject() (
  npsService: NpsService,
  authJourney: AuthJourney,
  atsErrorHandler: ATSErrorHandler,
  cc: ControllerComponents
)(implicit ec: ExecutionContext)
    extends BackendController(cc) {

  def getAtsPayeData(nino: String, taxYear: Int): Action[AnyContent] = authJourney.authWithPaye.async {
    implicit request =>
      npsService
        .getPayeATSData(nino, taxYear)
        .fold(
          error => atsErrorHandler.payeErrorToResponse(error),
          result => Ok(Json.toJson(result))
        )
  }

  def getAtsPayeDataMultipleYears(nino: String, yearFrom: Int, yearTo: Int): Action[AnyContent] =
    authJourney.authWithPaye.async { implicit request =>
      npsService
        .getAtsPayeDataMultipleYears(nino, (yearFrom to yearTo).toList)
        .fold(
          error => atsErrorHandler.payeErrorToResponse(error),
          result => if (result.isEmpty) NotFound("") else Ok(Json.toJson(result))
        )
    }
}
