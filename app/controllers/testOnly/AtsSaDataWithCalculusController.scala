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

package controllers.testOnly

import com.google.inject.Inject
import play.api.Logging
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.OdsService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import utils.ATSErrorHandler

import scala.concurrent.ExecutionContext

class AtsSaDataWithCalculusController @Inject() (
  odsService: OdsService,
  atsErrorHandler: ATSErrorHandler,
  cc: ControllerComponents
)(implicit val ec: ExecutionContext)
    extends BackendController(cc)
    with Logging {

  def getAtsSaDataWithCalculus(utr: String, tax_year: Int): Action[AnyContent] = Action.async { implicit request =>
    odsService
      .getPayload(utr, tax_year, withCalculus = true)
      .fold(
        error => atsErrorHandler.errorToResponse(error),
        result => Ok(result)
      )
  }
}
