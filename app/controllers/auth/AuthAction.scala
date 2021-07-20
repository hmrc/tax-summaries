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

package controllers.auth

import com.google.inject.{ImplementedBy, Inject}
import play.api.Logger
import play.api.mvc.Results.{BadRequest, Unauthorized}
import play.api.mvc._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions, ConfidenceLevel, Enrolments}
import uk.gov.hmrc.domain.Uar
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

class AuthActionImpl @Inject()(val authConnector: AuthConnector, cc: ControllerComponents)(
  implicit ec: ExecutionContext)
    extends AuthAction with AuthorisedFunctions {

  private val logger = Logger(getClass.getName)

  override protected def filter[A](request: Request[A]): Future[Option[Result]] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromHeadersAndSession(request.headers, None)

    val matchUtrInUriPattern = "/([\\d-]+).*".r

    val matches = matchUtrInUriPattern.findAllIn(request.uri)

    if (matches.isEmpty) {
      Future.successful(Some(BadRequest))
    } else {
      authorised(ConfidenceLevel.L50)
        .retrieve(Retrievals.allEnrolments and Retrievals.saUtr) {
          case Enrolments(enrolments) ~ saUtr =>
            val agentRef: Option[Uar] = enrolments.find(_.key == "IR-SA-AGENT").flatMap { enrolment =>
              enrolment.identifiers
                .find(id => id.key == "IRAgentReference")
                .map(key => Uar(key.value))
            }

            val isAgentActive: Boolean = enrolments.find(_.key == "IR-SA-AGENT").map(_.isActivated).getOrElse(false)

            if (saUtr.isDefined || isAgentActive) {
              Future.successful(None)
            } else {
              Future.successful(Some(Unauthorized))
            }
        }
        .recover {
          case t: Throwable =>
            logger.debug(s"Debug info - ${t.getMessage}", t)
            Some(Unauthorized)
        }
    }
  }

  override val parser: BodyParser[AnyContent] = cc.parsers.defaultBodyParser
  override protected def executionContext: ExecutionContext = ec
}

@ImplementedBy(classOf[AuthActionImpl])
trait AuthAction extends ActionBuilder[Request, AnyContent] with ActionFilter[Request]
