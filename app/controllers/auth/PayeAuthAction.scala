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

package controllers.auth

import com.google.inject.{ImplementedBy, Inject}
import play.api.Logger
import play.api.mvc.Results._
import play.api.mvc._
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisationException, AuthorisedFunctions, ConfidenceLevel, Nino => AuthNino}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

class PayeAuthActionImpl @Inject()(val authConnector: AuthConnector)(implicit executionContext: ExecutionContext)
    extends PayeAuthAction with AuthorisedFunctions {

  override protected def filter[A](request: Request[A]): Future[Option[Result]] = {

    val nino = """(?<=\/)[A-Z]{2}\d{6}[ABCD](?=\/)""".r.findFirstIn(request.uri)

    implicit val hc: HeaderCarrier =
      HeaderCarrierConverter.fromHeadersAndSession(request.headers, None)

    authorised(ConfidenceLevel.L200 and AuthNino(hasNino = true, nino = nino)) {
      Future.successful(None)
    }
  }.recover {
    case ae: AuthorisationException =>
      Logger.debug(s"Authorisation exception", ae)
      Some(Unauthorized)
    case t: Throwable =>
      Logger.error(s"Authorisation error", t)
      Some(InternalServerError)
  }
}

@ImplementedBy(classOf[PayeAuthActionImpl])
trait PayeAuthAction extends ActionBuilder[Request] with ActionFilter[Request]
