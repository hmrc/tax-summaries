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

import play.api.http.Status._
import play.api.mvc.Results.{BadGateway, BadRequest, InternalServerError, NotFound}
import uk.gov.hmrc.http.UpstreamErrorResponse

class ATSErrorHandlerSpec extends BaseSpec {

  lazy val sut: ATSErrorHandler = app.injector.instanceOf[ATSErrorHandler]

  "errorToResponse" must {

    "return notFound response" when {
      "an upstreamErrorResponse with status 404 is received" in {
        val error = UpstreamErrorResponse("Not found", NOT_FOUND, INTERNAL_SERVER_ERROR)

        val result = sut.errorToResponse(error)

        result mustBe NotFound(error.message)
      }
    }

    "return badRequest response" when {
      "an upstreamErrorResponse with status 400 is received" in {
        val error = UpstreamErrorResponse("Not found", BAD_REQUEST, INTERNAL_SERVER_ERROR)

        val result = sut.errorToResponse(error)

        result mustBe BadRequest(error.message)
      }
    }
  }

  "return BadGateway response" when {
    "an upstreamErrorResponse with status 5XX is received" in {
      val error = UpstreamErrorResponse("Error", INTERNAL_SERVER_ERROR, BAD_GATEWAY)

      val result = sut.errorToResponse(error)

      result mustBe BadGateway(error.message)
    }
  }

  "return InternalError response" when {
    "an upstreamErrorResponse with status 4XX and not 400 and 404 is received" in {
      val error = UpstreamErrorResponse("Error", LOCKED, INTERNAL_SERVER_ERROR)

      val result = sut.errorToResponse(error)

      result mustBe InternalServerError(error.message)
    }
  }

}
