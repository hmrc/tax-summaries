/*
 * Copyright 2021 HM Revenue & Customs
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
import controllers.auth.AuthAction
import models.ServiceError
import play.api.libs.json.JsValue
import play.api.mvc.{Action, AnyContent, ControllerComponents, Result}
import services.OdsService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import utils.ATSErrorHandler

import scala.concurrent.{ExecutionContext, Future}

class ATSDataController @Inject()(
  odsService: OdsService,
  atsErrorHandler: ATSErrorHandler,
  authAction: AuthAction,
  cc: ControllerComponents)(implicit val ec: ExecutionContext)
    extends BackendController(cc) {

  def hasAts(utr: String): Action[AnyContent] = authAction.async { implicit request =>
    handleResponse(odsService.getList(utr))
  }

  def getATSData(utr: String, tax_year: Int): Action[AnyContent] = authAction.async { implicit request =>
    handleResponse(odsService.getPayload(utr, tax_year))
  }

  def getATSList(utr: String): Action[AnyContent] = authAction.async { implicit request =>
    handleResponse(odsService.getATSList(utr))
  }

  private def handleResponse(call: Future[Either[ServiceError, JsValue]]): Future[Result] = call map {
    case Left(error)  => atsErrorHandler.errorToResponse(error)
    case Right(value) => Ok(value)
  }

}
