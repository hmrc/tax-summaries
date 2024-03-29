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
import play.api.Logger
import play.api.mvc.Results._
import play.api.mvc._
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisationException, AuthorisedFunctions, ConfidenceLevel}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.mongoFeatureToggles.services.FeatureFlagService
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

class PayeAuthActionImpl @Inject() (
  val authConnector: AuthConnector,
  cc: ControllerComponents,
  featureFlagService: FeatureFlagService,
  pertaxConnector: PertaxConnector
)(implicit ec: ExecutionContext)
    extends PayeAuthAction
    with AuthorisedFunctions {

  private val logger = Logger(getClass.getName)

  override protected def filter[A](request: Request[A]): Future[Option[Result]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)
    authorised(ConfidenceLevel.L50) {
      featureFlagService.get(PertaxBackendToggle).flatMap { toggle =>
        if (toggle.isEnabled) {
          pertaxConnector.pertaxAuth.value.flatMap {
            case Right(PertaxApiResponse("ACCESS_GRANTED", _, _, _))       =>
              Future.successful(None)
            case Right(PertaxApiResponse("INVALID_AFFINITY", _, _, _))     =>
              Future.successful(None)
            case Right(PertaxApiResponse("NO_HMRC_PT_ENROLMENT", _, _, _)) =>
              Future.successful(Some(Unauthorized))
            case Right(r)                                                  =>
              logger.warn("auth action received response: " + r)
              Future.successful(Some(InternalServerError))
            case Left(ex)                                                  =>
              logger.warn("Error received from auth", ex)
              Future.successful(Some(InternalServerError))
          }
        } else {
          Future.successful(None)
        }
      }
    }
  }.recover {
    case ae: AuthorisationException =>
      logger.error(s"Authorisation exception", ae)
      Some(Unauthorized)
    case t: Throwable               =>
      logger.error(s"Authorisation error", t)
      Some(InternalServerError)
  }

  override val parser: BodyParser[AnyContent]               = cc.parsers.defaultBodyParser
  override protected def executionContext: ExecutionContext = ec
}

@ImplementedBy(classOf[PayeAuthActionImpl])
trait PayeAuthAction extends ActionBuilder[Request, AnyContent] with ActionFilter[Request]
