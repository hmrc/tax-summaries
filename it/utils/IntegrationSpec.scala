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

import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.domain.{Generator, SaUtrGenerator}
import uk.gov.hmrc.http.HeaderCarrier

import scala.util.Random

/**
 * GET   /:UTR/:TAX_YEAR/ats-data                  controllers.ATSDataController.getATSData(UTR: String, TAX_YEAR: Int)
 * GET   /:NINO/:TAX_YEAR/paye-ats-data            controllers.ATSPAYEDataController.getATSData(NINO: String, TAX_YEAR: Int)
 * GET   /:NINO/:YEAR_FROM/:YEAR_TO/paye-ats-data  controllers.ATSPAYEDataController.getATSDataMultipleYears(NINO: String, YEAR_FROM: Int, YEAR_TO: Int)
 *
 * GET   /:UTR/has_summary_for_previous_period     controllers.ATSDataController.hasAts(UTR: String)
 * GET   /:UTR/ats-list                            controllers.ATSDataController.getATSList(UTR: String)
 */

trait IntegrationSpec
  extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with WireMockHelper with ScalaFutures with IntegrationPatience {
  override def beforeEach() = {
    super.beforeEach()

    val authResponse =
      s"""
         |{
         |    "confidenceLevel": 200,
         |    "nino": "$nino",
         |    "saUtr": "$utr",
         |    "name": {
         |        "name": "John",
         |        "lastName": "Smith"
         |    },
         |    "loginTimes": {
         |        "currentLogin": "2021-06-07T10:52:02.594Z",
         |        "previousLogin": null
         |    },
         |    "optionalCredentials": {
         |        "providerId": "4911434741952698",
         |        "providerType": "GovernmentGateway"
         |    },
         |    "authProviderId": {
         |        "ggCredId": "xyz"
         |    },
         |    "externalId": "testExternalId",
         |    "allEnrolments": []
         |}
         |""".stripMargin

    server.stubFor(
      post(urlEqualTo("/auth/authorise"))
        .willReturn(ok(authResponse)))
  }

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .configure(
        "microservice.services.tax-summaries-hod.port" -> server.port(),
        "microservice.services.tax-summaries-hod.host" -> "127.0.0.1",
        "microservice.services.auth.port" -> server.port(),
        "metrics.enabled" -> false,
        "auditing.enabled" -> false,
        "play.ws.timeout.request" -> "1000ms",
        "play.ws.timeout.connection" -> "500ms"
      )
      .build()

  val nino = new Generator(new Random).nextNino
  val utr = new SaUtrGenerator(new Random).nextSaUtr
  val hc = HeaderCarrier()

  val taxYear = 2047
  val taxYearMinusOne = 2047 - 1
}
