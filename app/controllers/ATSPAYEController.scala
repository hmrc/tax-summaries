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

import play.api.mvc.Action
import services.NpsService

import uk.gov.hmrc.play.microservice.controller.BaseController

import scala.concurrent.ExecutionContext.Implicits.global

object ATSPAYEDataController extends ATSPAYEDataController {
  override val npsService = NpsService
}

trait ATSPAYEDataController extends BaseController {

  def npsService: NpsService

  def getRawATSData(nino: String, taxYear: Int) = Action.async { implicit request =>
    {
      npsService.getRawPayload(nino, taxYear) map { Ok(_) }
    }
  }

  def getATSData(utr: String, tax_year: Int) = Action.async { implicit request =>
    {
      npsService.getPayload(utr, tax_year) map { Ok(_) }
    }
  }
}
