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

package paye.connectors

import cats.data.EitherT
import cats.instances.future.*
import common.config.ApplicationConfig
import common.utils.BaseSpec
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{reset, times, verify, when}
import paye.repositories.NpsCacheRepository
import play.api.http.Status.OK
import play.api.libs.json.JsObject
import play.api
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.http.{HttpResponse, UpstreamErrorResponse}

import scala.concurrent.{ExecutionContext, Future}

class CachingNpsConnectorSpec extends BaseSpec {

  private val mockPAYEConnector: NpsConnector   = mock[NpsConnector]
  private val mockCacheRepo: NpsCacheRepository = mock[NpsCacheRepository]
  private val mockAppConfig                     = mock[ApplicationConfig]

  private implicit lazy val ec: ExecutionContext = inject[ExecutionContext]

  private lazy val application: Application = new GuiceApplicationBuilder()
    .overrides(
      api.inject.bind[NpsConnector].qualifiedWith("default").toInstance(mockPAYEConnector),
      api.inject.bind[NpsCacheRepository].toInstance(mockCacheRepo),
      api.inject.bind[ApplicationConfig].toInstance(mockAppConfig)
    )
    .build()

  override def beforeEach(): Unit = {
    reset(mockPAYEConnector)
    reset(mockCacheRepo)
    reset(mockAppConfig)
    ()
  }

  private def connector: CachingNpsConnector = application.injector.instanceOf[CachingNpsConnector]

  private val nino    = "AA111111C"
  private val taxYear = 2022
  private val fresh   = HttpResponse(OK, """{"taxSummary":11111}""")

  "connectToPayeTaxSummary" must {
    "recover a SecurityException and refetch from NPS" in {
      when(mockCacheRepo.get(nino, taxYear))
        .thenReturn(Future.failed(new SecurityException("Unable to decrypt value")))

      when(mockPAYEConnector.connectToPayeTaxSummary(nino, taxYear))
        .thenReturn(EitherT.rightT[Future, UpstreamErrorResponse](fresh))

      when(mockCacheRepo.set(eqTo(nino), eqTo(taxYear), any[JsObject]))
        .thenReturn(Future.successful(true))

      val result = connector.connectToPayeTaxSummary(nino, taxYear).value.futureValue

      result mustBe a[Right[_, _]]
      verify(mockPAYEConnector, times(1)).connectToPayeTaxSummary(nino, taxYear)
      verify(mockCacheRepo, times(1)).set(eqTo(nino), eqTo(taxYear), any[JsObject])
    }
  }
}
