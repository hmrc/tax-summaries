/*
 * Copyright 2019 HM Revenue & Customs
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
import config.WSHttp
import play.api.Mode.Mode
import play.api.mvc.Results.{BadRequest, Unauthorized}
import play.api.mvc.{ActionBuilder, ActionFilter, Request, Result}
import play.api.{Configuration, Environment, Logger}
import uk.gov.hmrc.auth.core.{AuthorisedFunctions, ConfidenceLevel, Enrolment, PlayAuthConnector}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter
import uk.gov.hmrc.play.config.ServicesConfig

import scala.concurrent.{ExecutionContext, Future}

class AuthActionImpl @Inject()(val authConnector: AuthConnector)(implicit executionContext: ExecutionContext)
    extends AuthAction with AuthorisedFunctions {

  override protected def filter[A](request: Request[A]): Future[Option[Result]] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers, None)

    val matchUtrInUriPattern = "/([\\d-]+).*".r

    val matches = matchUtrInUriPattern.findAllIn(request.uri)

    if (matches.isEmpty) {
      Future.successful(Some(BadRequest))
    } else {

      val urlUtr: String = matches.group(1)
      authorised(
        ConfidenceLevel.L50 and Enrolment("IR-SA")
          .withIdentifier("UTR", urlUtr)
          .withDelegatedAuthRule("sa-auth")) {
        Future.successful(None)
      }.recover {
        case t: Throwable =>
          Logger.debug(s"Debug info - ${t.getMessage}", t)
          Some(Unauthorized)
      }
    }
  }
}

@ImplementedBy(classOf[AuthActionImpl])
trait AuthAction extends ActionBuilder[Request] with ActionFilter[Request]

class AuthConnector @Inject()(val http: WSHttp, val runModeConfiguration: Configuration, environment: Environment)
    extends PlayAuthConnector with ServicesConfig {
  override val serviceUrl: String = baseUrl("auth")

  override protected def mode: Mode = environment.mode
}
