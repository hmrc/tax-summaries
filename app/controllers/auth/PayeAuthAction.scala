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
import play.api.Logger
import play.api.mvc.Results._
import play.api.mvc._
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisationException, AuthorisedFunctions, ConfidenceLevel, Nino => AuthNino}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.NinoHelper

import scala.concurrent.{ExecutionContext, Future}

class PayeAuthActionImpl @Inject() (
  val authConnector: AuthConnector,
  ninoRegexHelper: NinoHelper,
  cc: ControllerComponents
)(implicit ec: ExecutionContext)
    extends PayeAuthAction
    with AuthorisedFunctions {

  private val logger = Logger(getClass.getName)

  override protected def filter[A](request: Request[A]): Future[Option[Result]] = {

    val nino = ninoRegexHelper.findNinoIn(request.uri)

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)

    authorised(ConfidenceLevel.L50 and AuthNino(hasNino = true, nino = nino)) {
      Future.successful(None)
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
