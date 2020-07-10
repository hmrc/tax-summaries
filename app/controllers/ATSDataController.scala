/*
 * Copyright 2020 HM Revenue & Customs
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
import play.api.mvc.{Action, AnyContent}
import services.OdsService
import uk.gov.hmrc.play.microservice.controller.BaseController

import scala.concurrent.ExecutionContext.Implicits.global

class ATSDataController @Inject()(odsService: OdsService, authAction: AuthAction) extends BaseController {

  def hasAts(utr: String): Action[AnyContent] = authAction.async { implicit request =>
    odsService.getList(utr) map (Ok(_)) recover {
      case _ => NotFound
    }
  }

  def getATSData(utr: String, tax_year: Int): Action[AnyContent] = authAction.async { implicit request =>
    odsService.getPayload(utr, tax_year) map { Ok(_) }
  }

  def getATSList(utr: String): Action[AnyContent] = authAction.async { implicit request =>
    odsService.getATSList(utr) map { Ok(_) }
  }
}
