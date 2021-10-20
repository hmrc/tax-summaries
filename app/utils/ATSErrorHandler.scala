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

package utils

import models.{DownstreamError, NotFoundError, ServiceError}
import play.api.mvc.Result
import play.api.mvc.Results.{BadGateway, InternalServerError, NotFound}
import uk.gov.hmrc.http.{Upstream4xxResponse, Upstream5xxResponse}
import com.google.inject.Inject
import play.api.Logging

class ATSErrorHandler @Inject()() extends Logging {

  def errorToResponse(error: ServiceError): Result =
    error match {
      case NotFoundError(msg) => NotFound(msg)
      case DownstreamError(msg, error: Upstream4xxResponse) => {
        logger.error(msg, error)
        InternalServerError(msg)
      }
      case DownstreamError(msg, Upstream5xxResponse(_, _, _, _)) => {
        logger.error(msg)
        BadGateway(msg)
      }
      case error => throw new RuntimeException(s"Unexpected Error: `${error.getClass.getName}`")
    }

}
