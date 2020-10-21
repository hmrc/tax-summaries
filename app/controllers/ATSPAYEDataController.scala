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
import com.typesafe.scalalogging.LazyLogging
import controllers.auth.PayeAuthAction
import models.paye.PayeAtsMiddleTier
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import services.NpsService
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.collection.immutable.Seq
import scala.concurrent.{ExecutionContext, Future}

class ATSPAYEDataController @Inject()(npsService: NpsService, payeAuthAction: PayeAuthAction, cc: ControllerComponents)(
  implicit ec: ExecutionContext)
    extends BackendController(cc) with LazyLogging {

  def getATSData(nino: String, taxYear: Int): Action[AnyContent] = payeAuthAction.async { implicit request =>
    callConnector(nino, taxYear) map {
      case Right(response)     => Ok(Json.toJson(response))
      case Left(errorResponse) => new Status(errorResponse.status).apply(errorResponse.json)
    }
  }

  def getATSDataMultipleYears(nino: String, yearFrom: Int, yearTo: Int): Action[AnyContent] = payeAuthAction.async {
    implicit request =>
      def dataList: Seq[Future[Option[JsValue]]] = (yearFrom to yearTo).toList map { year =>
        callConnector(nino, year) map {
          case Right(response) => Some(Json.toJson(response))
          case Left(error) =>
            logger.error(s"Fetching $year data for $nino returned ${error.status}")
            None
        }
      }
      Future.sequence(dataList) map { data =>
        val flattenedData = data.flatten
        if (flattenedData.isEmpty) NotFound(s"No data found for $nino") else Ok(Json.toJson(flattenedData))
      } recover {
        case e =>
          logger.error(e.getMessage)
          InternalServerError(e.getMessage)
      }
  }

  private def callConnector(nino: String, taxYear: Int)(
    implicit hc: HeaderCarrier): Future[Either[HttpResponse, PayeAtsMiddleTier]] =
    npsService.getPayeATSData(nino, taxYear)
}
