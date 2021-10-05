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

import com.google.inject.Inject
import models.{BadRequestError, DownstreamClientError, DownstreamServerError, NotFoundError, ServiceError}
import play.api.Logging
import play.api.mvc.Result
import play.api.mvc.Results.{BadGateway, BadRequest, InternalServerError, NotFound}

class ATSErrorHandler @Inject()() extends Logging {

  def errorToResponse(error: ServiceError): Result =
    error match {
      case NotFoundError(msg)   => NotFound(msg)
      case BadRequestError(msg) => BadRequest(msg)
      case DownstreamClientError(msg, error) => {
        logger.error(msg, error)
        InternalServerError(msg)
      }
      case DownstreamServerError(msg) => {
        logger.error(msg)
        BadGateway(msg)
      }
      case error => throw new RuntimeException(s"Unexpected Error: `${error.getClass.getName}`")
    }

}
