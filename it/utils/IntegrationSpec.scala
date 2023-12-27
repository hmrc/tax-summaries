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

import akka.Done
import com.github.tomakehurst.wiremock.client.WireMock._
import connectors.{DefaultSelfAssessmentODSConnector, SelfAssessmentODSConnector}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.cache.AsyncCacheApi
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.domain.{Generator, Nino, SaUtr, SaUtrGenerator}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future
import scala.concurrent.duration.Duration
import scala.reflect.ClassTag
import scala.util.Random

/**
  * GET   /:UTR/:TAX_YEAR/ats-data                  controllers.AtsSaDataController.getATSData(UTR: String, TAX_YEAR: Int)
  * GET   /:NINO/:TAX_YEAR/paye-ats-data            controllers.ATSPAYEDataController.getATSData(NINO: String, TAX_YEAR: Int)
  * GET   /:NINO/:YEAR_FROM/:YEAR_TO/paye-ats-data  controllers.ATSPAYEDataController.getATSDataMultipleYears(NINO: String, YEAR_FROM: Int, YEAR_TO: Int)
  *
  * GET   /:UTR/has_summary_for_previous_period     controllers.AtsSaDataController.hasAts(UTR: String)
  * GET   /:UTR/ats-list                            controllers.AtsSaDataController.getATSList(UTR: String)
  */

trait IntegrationSpec
    extends AnyWordSpec
    with Matchers
    with GuiceOneAppPerSuite
    with IntegrationWireMockHelper
    with ScalaFutures
    with IntegrationPatience {

  val mockCacheApi: AsyncCacheApi = new AsyncCacheApi {
    override def set(key: String, value: Any, expiration: Duration): Future[Done] = Future.successful(Done)

    override def remove(key: String): Future[Done] = Future.successful(Done)

    override def getOrElseUpdate[A](key: String, expiration: Duration)(orElse: => Future[A])(implicit
      evidence$1: ClassTag[A]
    ): Future[A] = orElse

    override def get[T](key: String)(implicit evidence$2: ClassTag[T]): Future[Option[T]] = Future.successful(None)

    override def removeAll(): Future[Done] = Future.successful(Done)
  }
  override def beforeEach(): Unit = {
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
        .willReturn(ok(authResponse))
    )
  }

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .disable[config.ATSModule]
      .overrides(
        bind[SelfAssessmentODSConnector].to[DefaultSelfAssessmentODSConnector],
        bind[AsyncCacheApi].toInstance(mockCacheApi)
      )
      .configure(
        "microservice.services.tax-summaries-hod.port" -> server.port(),
        "microservice.services.tax-summaries-hod.host" -> "127.0.0.1",
        "microservice.services.auth.host"              -> "localhost",
        "microservice.services.auth.port"              -> server.port(),
        "play.ws.timeout.request"                      -> "1000ms",
        "play.ws.timeout.connection"                   -> "500ms",
        "mongodb.uri"                                  -> s"mongodb://127.0.0.1:27017/test-${this.getClass.getSimpleName}"
      )
      .build()

  val nino: Nino        = new Generator(new Random).nextNino
  val utr: SaUtr        = new SaUtrGenerator(new Random).nextSaUtr
  val hc: HeaderCarrier = HeaderCarrier()

  val taxYear: Int         = 2047
  val taxYearMinusOne: Int = 2047 - 1
}
