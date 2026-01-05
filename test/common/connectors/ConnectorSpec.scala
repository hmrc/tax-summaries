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

package common.connectors

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.http.{HeaderNames, MimeTypes, Status}
import play.api.inject.guice.{GuiceApplicationBuilder, GuiceableModule}

import scala.concurrent.ExecutionContext

trait ConnectorSpec
    extends AnyWordSpec
    with GuiceOneAppPerSuite
    with Status
    with HeaderNames
    with MimeTypes
    with Matchers
    with ScalaFutures
    with IntegrationPatience {

  implicit lazy val ec: ExecutionContext = scala.concurrent.ExecutionContext.global

  protected val server: WireMockServer

  implicit def app(confStrings: Map[String, Any], overrides: GuiceableModule*): Application =
    new GuiceApplicationBuilder()
      .configure(confStrings)
      .overrides(overrides: _*)
      .build()

  def stubGet(url: String, responseStatus: Int, responseBody: Option[String]): StubMapping = server.stubFor {
    val baseResponse = aResponse().withStatus(responseStatus).withHeader(CONTENT_TYPE, JSON)
    val response     = responseBody.fold(baseResponse)(body => baseResponse.withBody(body))

    get(url).willReturn(response)
  }

}
