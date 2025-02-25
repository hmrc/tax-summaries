/*
 * Copyright 2025 HM Revenue & Customs
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

package common.connectors

import cats.data.EitherT
import common.utils.{BaseSpec, WireMockHelper}
import org.scalatest.RecoverMethods
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import play.api.Logging
import play.api.http.Status.*
import uk.gov.hmrc.http.*
import uk.gov.hmrc.play.bootstrap.tools.LogCapturing

import scala.concurrent.{ExecutionContext, Future}

class HttpClientResponseSpec
    extends BaseSpec
    with WireMockHelper
    with ScalaFutures
    with IntegrationPatience
    with RecoverMethods
    with LogCapturing
    with TestLogger {

  private implicit lazy val ec: ExecutionContext = inject[ExecutionContext]

  private lazy val httpClientResponseUsingMockLogger: HttpClientResponse = new HttpClientResponse with Logging {}

  private val dummyContent = "error message"

  "read" must {
    behave like clientResponseLogger(
      httpClientResponseUsingMockLogger.read,
      infoLevel = Set(NOT_FOUND),
      warnLevel = Set(LOCKED),
      errorLevelWithThrowable = Set(UNPROCESSABLE_ENTITY, UNAUTHORIZED, FORBIDDEN, BAD_REQUEST),
      errorLevelWithoutThrowable = Set(TOO_MANY_REQUESTS, INTERNAL_SERVER_ERROR)
    )
  }

  "readSA" must {
    behave like clientResponseLogger(
      httpClientResponseUsingMockLogger.readSA,
      infoLevel = Set(NOT_FOUND, BAD_REQUEST),
      warnLevel = Set(LOCKED),
      errorLevelWithThrowable = Set(UNPROCESSABLE_ENTITY, UNAUTHORIZED, FORBIDDEN),
      errorLevelWithoutThrowable = Set(TOO_MANY_REQUESTS, INTERNAL_SERVER_ERROR)
    )
  }

  private def clientResponseLogger(
    block: Future[Either[UpstreamErrorResponse, HttpResponse]] => EitherT[Future, UpstreamErrorResponse, HttpResponse],
    infoLevel: Set[Int],
    warnLevel: Set[Int],
    errorLevelWithThrowable: Set[Int],
    errorLevelWithoutThrowable: Set[Int]
  ): Unit = {

    infoLevel.foreach { httpResponseCode =>
      s"log message: INFO level only when response code is $httpResponseCode" in {
        withCaptureOfLoggingFrom(getLogger) { capturedLogs =>
          val response: Future[Either[UpstreamErrorResponse, HttpResponse]] =
            Future(Left(UpstreamErrorResponse(dummyContent, httpResponseCode)))
          whenReady(block(response).value) { actual =>
            actual mustBe Left(UpstreamErrorResponse(dummyContent, httpResponseCode))

            val logMessages = capturedLogs.filter(_.getLevel.toString == "INFO").map(_.getMessage)
            logMessages.exists(_.contains(dummyContent)) mustBe true
          }
          ()
        }
      }
    }

    warnLevel.foreach { httpResponseCode =>
      s"log message: WARNING level only when response is $httpResponseCode" in {
        withCaptureOfLoggingFrom(getLogger) { capturedLogs =>
          val response: Future[Either[UpstreamErrorResponse, HttpResponse]] =
            Future(Left(UpstreamErrorResponse(dummyContent, httpResponseCode)))
          whenReady(block(response).value) { actual =>
            actual mustBe Left(UpstreamErrorResponse(dummyContent, httpResponseCode))

            val logMessages = capturedLogs.filter(_.getLevel.toString == "WARN").map(_.getMessage)
            logMessages.exists(_.contains(dummyContent)) mustBe true
          }
          ()
        }
      }
    }

    errorLevelWithThrowable.foreach { httpResponseCode =>
      s"log message: ERROR level only WITH throwable when response code is $httpResponseCode" in {
        withCaptureOfLoggingFrom(getLogger) { capturedLogs =>
          val response: Future[Either[UpstreamErrorResponse, HttpResponse]] =
            Future(Left(UpstreamErrorResponse(dummyContent, httpResponseCode)))
          whenReady(block(response).value) { actual =>
            actual mustBe Left(UpstreamErrorResponse(dummyContent, httpResponseCode))

            val logMessages = capturedLogs.filter(_.getLevel.toString == "ERROR").map(_.getMessage)
            logMessages.exists(_.contains(dummyContent)) mustBe true
          }
          ()
        }
      }
    }

    errorLevelWithoutThrowable.foreach { httpResponseCode =>
      s"log message: ERROR level only WITHOUT throwable when response code is $httpResponseCode" in {
        withCaptureOfLoggingFrom(getLogger) { capturedLogs =>
          val response: Future[Either[UpstreamErrorResponse, HttpResponse]] =
            Future(Left(UpstreamErrorResponse(dummyContent, httpResponseCode)))
          whenReady(block(response).value) { actual =>
            actual mustBe Left(UpstreamErrorResponse(dummyContent, httpResponseCode))

            val logMessages = capturedLogs.filter(_.getLevel.toString == "ERROR").map(_.getMessage)
            logMessages.exists(_.contains(dummyContent)) mustBe true
          }
          ()
        }
      }
    }

    "log message: ERROR level only WITHOUT throwable when future failed with HttpException & recover to BAD GATEWAY" in {
      withCaptureOfLoggingFrom(getLogger) { capturedLogs =>
        val response: Future[Either[UpstreamErrorResponse, HttpResponse]] =
          Future.failed(new HttpException(dummyContent, GATEWAY_TIMEOUT))

        whenReady(block(response).value) { actual =>
          actual mustBe Left(UpstreamErrorResponse(dummyContent, BAD_GATEWAY))

          val logMessages = capturedLogs.filter(_.getLevel.toString == "ERROR").map(_.getMessage)
          logMessages.exists(_.contains(dummyContent)) mustBe true
        }
        ()
      }
    }

    "log nothing at all when future failed with non-HTTPException" in {
      withCaptureOfLoggingFrom(getLogger) { capturedLogs =>
        val response: Future[Either[UpstreamErrorResponse, HttpResponse]] =
          Future.failed(new RuntimeException(dummyContent))

        recoverToSucceededIf[RuntimeException] {
          block(response).value
        }

        capturedLogs.isEmpty mustBe true
        ()
      }
    }
  }
}
