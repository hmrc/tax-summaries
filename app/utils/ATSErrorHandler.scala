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

package utils

import com.google.inject.Inject
import play.api.Logging
import play.api.http.Status.{BAD_REQUEST, NOT_FOUND, TOO_MANY_REQUESTS}
import play.api.mvc.Result
import play.api.mvc.Results.{BadGateway, InternalServerError, NotFound, TooManyRequests}
import uk.gov.hmrc.http.UpstreamErrorResponse

class ATSErrorHandler @Inject() () extends Logging {

  def payeErrorToResponse(error: UpstreamErrorResponse): Result =
    error match {
      case error if error.statusCode == NOT_FOUND         => NotFound(error.message)
      case error if error.statusCode == TOO_MANY_REQUESTS => TooManyRequests
      case error if error.statusCode < 498                => InternalServerError(error.message)
      case error                                          => BadGateway(error.message)
    }

  def saErrorToResponse(error: UpstreamErrorResponse): Result =
    error match {
      case error if error.statusCode == NOT_FOUND         => NotFound(error.message)
      case error if error.statusCode == BAD_REQUEST       =>
        // If tax year < min tax year then API will return BAD REQUEST
        NotFound(error.message)
      case error if error.statusCode == TOO_MANY_REQUESTS => TooManyRequests
      case error if error.statusCode < 498                => InternalServerError(error.message)
      case error                                          => BadGateway(error.message)
    }
}
