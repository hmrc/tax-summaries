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

package controllers

import cats.data.EitherT
import play.api.http.Status.OK

import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.{HttpResponse, UpstreamErrorResponse}

import java.io.IOException
import scala.concurrent.Future
import scala.io.Source
import scala.util.{Failure, Success, Try}

object MockConnections {

  def connectToServiceWithBrokenIO(URL: String): Future[JsValue] =
    throw new IOException()

  def connectToMockPayloadService(URL: String): EitherT[Future, UpstreamErrorResponse, HttpResponse] =
    Try {
      Source.fromURL(getClass.getResource(URL)).mkString
    } match {
      case Success(value) => EitherT.rightT(HttpResponse(OK, value))
      case Failure(error) => throw new RuntimeException("Mock file not found: " + error.getMessage)
    }

  def connectToMockGovSpendService(URL: String): JsValue = {

    val source     = Source.fromURL(getClass.getResource(URL)).mkString
    val theJsValue = Json.parse(source)

    theJsValue
  }
}
