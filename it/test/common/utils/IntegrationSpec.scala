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

package common.utils

import cats.data.EitherT
import cats.instances.future.*
import com.github.tomakehurst.wiremock.client.WireMock.*
import common.config.ATSModule
import common.models.admin.{PayeDetailsFromHipToggle, SaDetailsFromHipToggle}
import org.apache.pekko.Done
import org.mockito.Mockito.when
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.cache.AsyncCacheApi
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import sa.connectors.{DefaultSelfAssessmentODSConnector, SelfAssessmentODSConnector}
import uk.gov.hmrc.domain.{Nino, NinoGenerator, SaUtr, SaUtrGenerator}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.mongoFeatureToggles.model.FeatureFlag
import uk.gov.hmrc.mongoFeatureToggles.services.FeatureFlagService

import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

/** GET /:UTR/:TAX_YEAR/ats-data controllers.AtsSaDataController.getATSData(UTR: String, TAX_YEAR: Int) GET
  * /:NINO/:TAX_YEAR/paye-ats-data controllers.ATSPAYEDataController.getATSData(NINO: String, TAX_YEAR: Int) GET
  * /:NINO/:YEAR_FROM/:YEAR_TO/paye-ats-data controllers.ATSPAYEDataController.getATSDataMultipleYears(NINO: String,
  * YEAR_FROM: Int, YEAR_TO: Int)
  *
  * GET /:UTR/has_summary_for_previous_period controllers.AtsSaDataController.hasAts(UTR: String) GET /:UTR/ats-list
  * controllers.AtsSaDataController.getATSList(UTR: String)
  */

trait IntegrationSpec
    extends AnyWordSpec
    with Matchers
    with GuiceOneAppPerSuite
    with IntegrationWireMockHelper
    with ScalaFutures
    with IntegrationPatience {

  protected val mockCacheApi: AsyncCacheApi = new AsyncCacheApi {
    override def set(key: String, value: Any, expiration: Duration): Future[Done] = Future.successful(Done)

    override def remove(key: String): Future[Done] = Future.successful(Done)

    override def getOrElseUpdate[A](key: String, expiration: Duration)(orElse: => Future[A])(implicit
      evidence$1: ClassTag[A]
    ): Future[A] = orElse

    override def get[T](key: String)(implicit evidence$2: ClassTag[T]): Future[Option[T]] = Future.successful(None)

    override def removeAll(): Future[Done] = Future.successful(Done)
  }

  protected implicit lazy val ec: ExecutionContext = app.injector.instanceOf[ExecutionContext]

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

    server.stubFor(
      post(urlEqualTo("/pertax/authorise"))
        .willReturn(ok("{\"code\": \"ACCESS_GRANTED\", \"message\": \"Access granted\"}"))
    )
    when(mockFeatureFlagService.getAsEitherT(org.mockito.ArgumentMatchers.eq(PayeDetailsFromHipToggle))) thenReturn
      EitherT.rightT(FeatureFlag(PayeDetailsFromHipToggle, isEnabled = true))
    when(mockFeatureFlagService.getAsEitherT(org.mockito.ArgumentMatchers.eq(SaDetailsFromHipToggle))) thenReturn
      EitherT.rightT(FeatureFlag(SaDetailsFromHipToggle, isEnabled = true))
    ()
  }

  protected lazy val mockFeatureFlagService: FeatureFlagService = mock[FeatureFlagService]

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .disable[ATSModule]
      .overrides(
        bind[SelfAssessmentODSConnector].to[DefaultSelfAssessmentODSConnector],
        bind[SelfAssessmentODSConnector].qualifiedWith("default").to[DefaultSelfAssessmentODSConnector],
        bind[AsyncCacheApi].toInstance(mockCacheApi),
        bind[FeatureFlagService].toInstance(mockFeatureFlagService)
      )
      .configure(
        "microservice.services.tax-summaries-hod.port" -> server.port(),
        "microservice.services.tax-summaries-hod.host" -> "127.0.0.1",
        "microservice.services.auth.host"              -> "localhost",
        "microservice.services.auth.port"              -> server.port(),
        "microservice.services.pertax.host"            -> "localhost",
        "microservice.services.pertax.port"            -> server.port(),
        "microservice.services.hip-hod.port"           -> server.port(),
        "microservice.services.hip-hod.host"           -> "127.0.0.1",
        "microservice.services.hip-hod-sa.port"        -> server.port(),
        "microservice.services.hip-hod-sa.host"        -> "127.0.0.1",
        "microservice.services.if-hod.port"            -> server.port(),
        "microservice.services.if-hod.host"            -> "127.0.0.1",
        "play.ws.timeout.request"                      -> "1000ms",
        "play.ws.timeout.connection"                   -> "500ms",
        "mongodb.uri"                                  -> s"mongodb://127.0.0.1:27017/test-${this.getClass.getSimpleName}"
      )
      .build()

  protected val nino: Nino        = new NinoGenerator().nextNino
  protected val utr: SaUtr        = new SaUtrGenerator().nextSaUtr
  protected val hc: HeaderCarrier = HeaderCarrier()

  protected val taxYear: Int         = 2047
  protected val taxYearMinusOne: Int = 2047 - 1
}
