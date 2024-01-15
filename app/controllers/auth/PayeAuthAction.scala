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
import play.api.mvc.Results._
import play.api.mvc._
import play.api.{Logger, mvc}
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
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

  private def retrieveNinoFromAuthClient(implicit hc: HeaderCarrier): Future[Option[String]] =
    authorised(ConfidenceLevel.L50).retrieve(Retrievals.nino) {
      case None        => Future.successful(None)
      case n @ Some(_) => Future.successful(n)
    }

  private def authenticateViaPertax(
    ninoOpt: Option[String]
  )(implicit hc: HeaderCarrier): Future[Option[mvc.Results.Status]] =
    featureFlagService.get(PertaxBackendToggle).flatMap { toggle =>
      if (toggle.isEnabled) {
        ninoOpt match {
          case None       =>
            println("\n\n** NO NINO FOUND IN AUTH CLIENT")
            Future.successful(Some(Unauthorized))
          case Some(nino) =>
            println("\nNINO FOUND IN AUTH CLIENT:" + nino)
            pertaxConnector.pertaxAuth(nino).value.flatMap {
              case Right(PertaxApiResponse("ACCESS_GRANTED", _, _, _))       =>
                println("\nGRANTED")
                Future.successful(None)
              case Right(PertaxApiResponse("INVALID_AFFINITY", _, _, _))     =>
                Future.successful(None)
              case Right(PertaxApiResponse("NO_HMRC_PT_ENROLMENT", _, _, _)) =>
                Future.successful(Some(Unauthorized))
              case e                                                         =>
                Future.successful(Some(InternalServerError))
            }
        }
      } else {
        Future.successful(None)
      }
    }

  override protected def filter[A](request: Request[A]): Future[Option[Result]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)
    for {
      optNino   <- retrieveNinoFromAuthClient
      optStatus <- authenticateViaPertax(optNino)
    } yield optStatus
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
