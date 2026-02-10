/*
 * Copyright 2026 HM Revenue & Customs
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
import common.utils.BaseSpec
import org.scalatest.RecoverMethods
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import play.api.Logging
import play.api.http.Status.*
import uk.gov.hmrc.http.*
import uk.gov.hmrc.play.bootstrap.tools.LogCapturing

import scala.concurrent.{ExecutionContext, Future}

class HttpClientResponseSpec
    extends BaseSpec
    with ScalaFutures
    with IntegrationPatience
    with RecoverMethods
    with LogCapturing
    with TestLogger {

  private implicit lazy val ec: ExecutionContext                         = inject[ExecutionContext]
  private lazy val httpClientResponseUsingMockLogger: HttpClientResponse = new HttpClientResponse with Logging {}
  private val dummyContent                                               = "error message"

  "read" must {
    behave like clientResponseLogger(
      httpClientResponseUsingMockLogger.readPaye,
      infoLevel = Set(NOT_FOUND, UNPROCESSABLE_ENTITY),
      warnLevel = Set(LOCKED),
      errorLevelWithThrowable = Set(UNAUTHORIZED, FORBIDDEN, BAD_REQUEST),
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

    def testLogLevel(httpResponseCode: Int, logLevel: String): Unit =
      s"log message: $logLevel level only when response code is $httpResponseCode" in {
        withCaptureOfLoggingFrom(getLogger) { capturedLogs =>
          val response = Future(Left(UpstreamErrorResponse(dummyContent, httpResponseCode)))
          whenReady(block(response).value) { actual =>
            actual mustBe Left(UpstreamErrorResponse(dummyContent, httpResponseCode))
            capturedLogs
              .filter(_.getLevel.toString == logLevel)
              .map(_.getMessage)
              .exists(_.contains(dummyContent)) mustBe true
          }
          ()
        }
      }

    infoLevel.foreach(testLogLevel(_, "INFO"))
    warnLevel.foreach(testLogLevel(_, "WARN"))
    errorLevelWithThrowable.foreach(testLogLevel(_, "ERROR"))
    errorLevelWithoutThrowable.foreach(testLogLevel(_, "ERROR"))

    "log message: ERROR level only WITHOUT throwable when future failed with HttpException & recover to BAD GATEWAY" in {
      withCaptureOfLoggingFrom(getLogger) { capturedLogs =>
        val response = Future.failed(new HttpException(dummyContent, GATEWAY_TIMEOUT))
        whenReady(block(response).value) { actual =>
          actual mustBe Left(UpstreamErrorResponse(dummyContent, BAD_GATEWAY))
          capturedLogs.exists(_.getMessage.contains(dummyContent)) mustBe true
        }
        ()
      }
    }

    "log nothing at all when future failed with non-HTTPException" in {
      withCaptureOfLoggingFrom(getLogger) { capturedLogs =>
        recoverToSucceededIf[RuntimeException](
          block(Future.failed(new RuntimeException(dummyContent))).value
        )
        capturedLogs.isEmpty mustBe true
        ()
      }
    }
  }
}
