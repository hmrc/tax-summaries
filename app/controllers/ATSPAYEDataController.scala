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
import com.typesafe.scalalogging.LazyLogging
import controllers.auth.PayeAuthAction
import models.ServiceError
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.NpsService
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import utils.ATSErrorHandler

import scala.collection.immutable.Seq
import scala.concurrent.{ExecutionContext, Future}

class ATSPAYEDataController @Inject()(
  npsService: NpsService,
  payeAuthAction: PayeAuthAction,
  atsErrorHandler: ATSErrorHandler,
  cc: ControllerComponents)(implicit ec: ExecutionContext)
    extends BackendController(cc) with LazyLogging {

  def getATSData(nino: String, taxYear: Int): Action[AnyContent] = payeAuthAction.async { implicit request =>
    npsService.getPayeATSData(nino, taxYear) map {
      case Right(response)     => Ok(Json.toJson(response))
      case Left(errorResponse) => atsErrorHandler.errorToResponse(errorResponse)
    }
  }

  def getATSDataMultipleYears(nino: String, yearFrom: Int, yearTo: Int): Action[AnyContent] = payeAuthAction.async {
    implicit request =>
      val dataList: Seq[Future[Either[ServiceError, JsValue]]] = (yearFrom to yearTo).toList map { year =>
        npsService.getPayeATSData(nino, year) map {
          case Right(response) => Right(Json.toJson(response))
          case Left(error)     => Left(error)
        }
      }

      Future.sequence(dataList).map { dataList =>
        dataList.foldLeft(Right(List.empty): Either[ServiceError, List[JsValue]]) { (resultEither, currentEither) =>
          resultEither match {
            case Left(error) => Left(error)
            case Right(result) => {
              currentEither match {
                case Left(error)    => Left(error)
                case Right(jsValue) => Right(jsValue :: result)
              }
            }
          }
        } match {
          case Left(error)    => atsErrorHandler.errorToResponse(error)
          case Right(atsList) => Ok(Json.toJson(atsList))
        }
      }
  }
}
