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

import controllers.auth.{AuthAction, PayeAuthAction}
import javax.inject.Inject
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.{GoodsAndServices, GovSpendService}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import utils.NinoHelper

class GovernmentSpendController @Inject()(
  govSpendService: GovSpendService,
  ninoHelper: NinoHelper,
  authAction: AuthAction,
  payeAuthAction: PayeAuthAction,
  cc: ControllerComponents
) extends BackendController(cc) {

  def getGovernmentSpend(taxYear: Int, identifier: String): Action[AnyContent] =
    ninoHelper.findNinoIn(identifier) match {
      case Some(_) => payeGovSpend(taxYear)
      case _       => saGovSpend(taxYear)
    }

  private def payeGovSpend(taxYear: Int): Action[AnyContent] = payeAuthAction {
    Ok(getJsonFromConfig(taxYear))
  }

  private def saGovSpend(taxYear: Int): Action[AnyContent] = authAction {
    Ok(getJsonFromConfig(taxYear))
  }

  private def getJsonFromConfig(taxYear: Int): JsValue =
    Json.toJson(govSpendService.govSpending(taxYear))(GoodsAndServices.mapFormat)
}
