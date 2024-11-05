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
import play.api.http.Status.{TOO_MANY_REQUESTS, UNAUTHORIZED}
import play.api.mvc.Results._
import play.api.mvc._
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

class PayeAuthActionImpl @Inject() (
  cc: ControllerComponents,
  pertaxConnector: PertaxConnector
)(implicit ec: ExecutionContext)
    extends PayeAuthAction {

  override protected def filter[A](request: Request[A]): Future[Option[Result]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)
    pertaxConnector.pertaxAuth.value.map {
      case Right(PertaxApiResponse("ACCESS_GRANTED", _))   => None
      case Right(PertaxApiResponse("INVALID_AFFINITY", _)) => None
      case Right(PertaxApiResponse(code, message))         =>
        Some(Unauthorized(s"Unauthorised with error code: `$code` and message: `$message`"))
      case Left(error)                                     => handleError(error)
    }
  }

  private def handleError(error: UpstreamErrorResponse): Option[Result] =
    error.statusCode match {
      case UNAUTHORIZED            => Some(Unauthorized(""))
      case TOO_MANY_REQUESTS       => Some(TooManyRequests(""))
      case status if status >= 499 => Some(BadGateway("Dependant services failing"))
      case _                       =>
        Some(
          InternalServerError(
            s"Unexpected response from pertax with status ${error.statusCode} and response ${error.message}"
          )
        )
    }

  override val parser: BodyParser[AnyContent]               = cc.parsers.defaultBodyParser
  override protected def executionContext: ExecutionContext = ec
}

@ImplementedBy(classOf[PayeAuthActionImpl])
trait PayeAuthAction extends ActionBuilder[Request, AnyContent] with ActionFilter[Request]
