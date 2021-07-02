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
import models.paye.PayeAtsMiddleTier
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents, Result}
import services.NpsService
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import scala.collection.immutable.Seq
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

class ATSPAYEDataController @Inject()(npsService: NpsService, payeAuthAction: PayeAuthAction, cc: ControllerComponents)(
  implicit ec: ExecutionContext)
    extends BackendController(cc) with LazyLogging {

  def getATSData(nino: String, taxYear: Int): Action[AnyContent] = payeAuthAction.async { implicit request =>
    callConnector(nino, taxYear) map {
      case Right(response) => Ok(Json.toJson(response))
      case Left(errorResponse) =>
        new Status(errorResponse.status).apply(errorResponse.body)
    }
  }

  def getATSDataMultipleYears(nino: String, yearFrom: Int, yearTo: Int): Action[AnyContent] = payeAuthAction.async {
    implicit request =>
      def dataList: Seq[Future[Either[HttpResponse, JsValue]]] = (yearFrom to yearTo).toList map { year =>
        callConnector(nino, year) map {
          case Right(response) => Right(Json.toJson(response))
          case Left(error) =>
            logger.error(s"Fetching $year data for $nino returned ${error.status}")
            Left(error)
        }
      }

      Future.sequence(dataList) map { seqEither =>
        val seqRights = seqEither.collect { case Right(value) => value }
        val seqLeftsHttp = seqEither.collect {
          case Left(value) if value.status == INTERNAL_SERVER_ERROR || value.status == BAD_REQUEST => value
        }

        (seqLeftsHttp.isEmpty, seqRights.isEmpty) match {
          case (false, _) =>
            val firstErr = seqLeftsHttp.head
            Status(firstErr.status).apply(firstErr.body)
          case (true, false) => Ok(Json.toJson(seqRights))
          case _             => NotFound(s"No data found for $nino")
        }
      } recover {
        case NonFatal(e) =>
          logger.error(e.getMessage)
          InternalServerError(e.getMessage)
      }

  }

  private def callConnector(nino: String, taxYear: Int)(
    implicit hc: HeaderCarrier): Future[Either[HttpResponse, PayeAtsMiddleTier]] =
    npsService.getPayeATSData(nino, taxYear)
}
