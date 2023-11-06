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

import com.google.inject.{Inject, Singleton}
import connectors.PertaxConnector
import models.PertaxResponse
import models.admin.PertaxBackendToggle
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import uk.gov.hmrc.mongoFeatureToggles.services.FeatureFlagService
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PertaxAuthAction @Inject() (
  pertaxConnector: PertaxConnector,
  featureFlagService: FeatureFlagService,
  cc: ControllerComponents
) extends ActionFilter[Request]
    with Results
    with I18nSupport
    with Logging {

  override def messagesApi: MessagesApi = cc.messagesApi

  override def filter[A](request: Request[A]): Future[Option[Result]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequest(request)

    featureFlagService.get(PertaxBackendToggle).flatMap { toggle =>
      if (toggle.isEnabled) {
        pertaxConnector.pertaxPostAuthorise
          .fold(
            { error: UpstreamErrorResponse =>
              Some(BadGateway(error.getMessage()))
            },
            {
              case PertaxResponse("ACCESS_GRANTED", _) => None
              case PertaxResponse(code, message)       =>
                Some(Unauthorized(s"Unauthorised withe error code: `$code` and message:`$message`"))
            }
          )
      } else {
        Future.successful(None)
      }
    }
  }

  override protected implicit val executionContext: ExecutionContext = cc.executionContext

}
