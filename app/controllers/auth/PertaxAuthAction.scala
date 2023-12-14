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

package controllers.auth

import com.google.inject.{ImplementedBy, Inject}
import connectors.PertaxConnector
import models.PertaxApiResponse
import models.admin.PertaxBackendToggle
import play.api.mvc.Results.{InternalServerError, Unauthorized}
import play.api.mvc.{ActionBuilder, ActionFilter, AnyContent, BodyParser, ControllerComponents, Request, Result}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.mongoFeatureToggles.services.FeatureFlagService
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.NinoHelper

import scala.concurrent.{ExecutionContext, Future}

class PertaxAuthActionImpl @Inject()(
                                      cc: ControllerComponents,
                                      pertaxConnector: PertaxConnector,
                                      featureFlagService: FeatureFlagService,
                                      ninoRegexHelper: NinoHelper
                                    ) extends PertaxAuthAction {

  override protected def filter[A](request: Request[A]): Future[Option[Result]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    val nino = ninoRegexHelper.findNinoIn(request.uri)
    featureFlagService.get(PertaxBackendToggle).flatMap { toggle =>
      if (toggle.isEnabled) {
        pertaxConnector
          .pertaxAuth(nino.getOrElse(""))
          .value
          .flatMap {
            case Right(PertaxApiResponse("ACCESS_GRANTED", _, _, _)) =>
              Future.successful(None)
            case Right(PertaxApiResponse("INVALID_AFFINITY", _, _, _)) =>
              Future.successful(None)
            case Right(PertaxApiResponse("NO_HMRC_PT_ENROLMENT", _, _, _)) =>
              Future.successful(Some(Unauthorized))
            case _ =>
              Future.successful(Some(InternalServerError))
          }

      } else {
        Future.successful(None)
      }
    }
  }

  override implicit val executionContext: ExecutionContext = cc.executionContext
  override val parser: BodyParser[AnyContent] = cc.parsers.defaultBodyParser
}

@ImplementedBy(classOf[PertaxAuthActionImpl])
trait PertaxAuthAction extends ActionBuilder[Request, AnyContent] with ActionFilter[Request]
