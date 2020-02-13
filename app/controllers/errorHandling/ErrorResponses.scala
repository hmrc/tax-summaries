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

package controllers.errorHandling

import play.api.libs.json.{JsValue, Json, Writes}
import play.api.mvc.Result
import play.api.mvc.Results.Status

sealed abstract class ErrorResponse(val httpStatusCode: Int, val errorCode: String, val message: String) {

  implicit val errorResponseWrites = new Writes[ErrorResponse] {
    def writes(e: ErrorResponse): JsValue = Json.obj("code" -> e.errorCode, "message" -> e.message)
  }

  def toResult: Result =
    Status(this.httpStatusCode)(Json.toJson(this))
}

case object ErrorNotFound extends ErrorResponse(404, "NOT_FOUND", "Resource was not found")
case object ErrorGenericBadRequest extends ErrorResponse(400, "BAD_REQUEST", "Bad Request")
case object ErrorInternalServerError extends ErrorResponse(500, "INTERNAL_SERVER_ERROR", "Internal server error")
case object ErrorBadGateway extends ErrorResponse(502, "BAD_GATEWAY", "BadGateway")
case object ErrorServiceUnavailable extends ErrorResponse(503, "SERVICE_UNAVAILABLE", "ServiceUnavailable")
case object ErrorGatewayTimeout extends ErrorResponse(504, "GATEWAY_TIMEOUT", "GatewayTimeout")
