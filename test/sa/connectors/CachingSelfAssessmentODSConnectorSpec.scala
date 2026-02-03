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

package sa.connectors

import cats.data.EitherT
import cats.instances.future.*
import common.config.ApplicationConfig
import common.utils.BaseSpec
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import play.api
import play.api.Application
import play.api.http.Status.OK
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import sa.repositories.TaxSummariesSessionCacheRepository
import uk.gov.hmrc.http.{HttpResponse, UpstreamErrorResponse}
import uk.gov.hmrc.mongo.cache.DataKey

import scala.concurrent.{ExecutionContext, Future}

class CachingSelfAssessmentODSConnectorSpec extends BaseSpec {

  val mockSelfAssessmentODSConnector: SelfAssessmentODSConnector     = mock[SelfAssessmentODSConnector]
  val mockSessionCacheRepository: TaxSummariesSessionCacheRepository = mock[TaxSummariesSessionCacheRepository]
  private val mockAppConfig                                          = mock[ApplicationConfig]

  private implicit lazy val ec: ExecutionContext = inject[ExecutionContext]

  private lazy val appn: Application = new GuiceApplicationBuilder()
    .overrides(
      api.inject
        .bind(classOf[SelfAssessmentODSConnector])
        .qualifiedWith("default")
        .toInstance(mockSelfAssessmentODSConnector),
      api.inject.bind[TaxSummariesSessionCacheRepository].toInstance(mockSessionCacheRepository),
      api.inject.bind[ApplicationConfig].toInstance(mockAppConfig)
    )
    .build()

  override def beforeEach(): Unit = {
    reset(mockSelfAssessmentODSConnector)
    reset(mockSessionCacheRepository)
    reset(mockAppConfig)
    when(mockAppConfig.environment).thenReturn("tax-summaries-hod.env")
    ()
  }

  private def connector: CachingSelfAssessmentODSConnector = appn.injector.instanceOf[CachingSelfAssessmentODSConnector]

  private implicit val userRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

  "Calling CachingSelfAssessmentODSConnectorSpec.connectToSelfAssessment" must {
    "return a Right response" when {
      "no value is cached" in {

        when(mockSessionCacheRepository.getFromSession[HttpResponse](DataKey(any[String]()))(any(), any()))
          .thenReturn(Future.successful(None))

        when(
          mockSessionCacheRepository.putSession[HttpResponse](DataKey(any[String]()), any())(any(), any(), any())
        )
          .thenReturn(Future.successful(("", "")))

        when(mockSelfAssessmentODSConnector.connectToSelfAssessment("utr", 2022))
          .thenReturn(EitherT.rightT[Future, UpstreamErrorResponse](HttpResponse(OK, "")))

        val saResponse = connector.connectToSelfAssessment("utr", 2022).value.futureValue

        verify(mockSessionCacheRepository, times(1))
          .getFromSession[HttpResponse](DataKey(any[String]()))(any(), any())

        verify(mockSessionCacheRepository, times(1))
          .putSession[HttpResponse](DataKey(any[String]()), any())(any(), any(), any())

        verify(mockSelfAssessmentODSConnector, times(1)).connectToSelfAssessment("utr", 2022)

        saResponse mustBe a[Right[_, _]]
      }

      "a value is cached" in {
        when(mockSessionCacheRepository.getFromSession[HttpResponse](DataKey(any[String]()))(any(), any()))
          .thenReturn(Future.successful(Some(HttpResponse(OK, ""))))

        when(mockSelfAssessmentODSConnector.connectToSelfAssessment("utr", 2022))
          .thenReturn(null)

        when(
          mockSessionCacheRepository.putSession[HttpResponse](DataKey(any[String]()), any())(any(), any(), any())
        )
          .thenReturn(null)

        val saResponse = connector.connectToSelfAssessment("utr", 2022).value.futureValue

        verify(mockSessionCacheRepository, times(1))
          .getFromSession[HttpResponse](DataKey(any[String]()))(any(), any())

        verify(mockSessionCacheRepository, times(0))
          .putSession[HttpResponse](DataKey(any[String]()), any())(any(), any(), any())

        verify(mockSelfAssessmentODSConnector, times(0)).connectToSelfAssessment("utr", 2022)

        saResponse mustBe a[Right[_, _]]

      }

      "a cached value is NOT used when in a 'local' (stubbed) env (i.e. local or staging) and one of test utrs" in {
        when(mockAppConfig.environment).thenReturn("local")
        when(mockSelfAssessmentODSConnector.connectToSelfAssessment(any(), any())(any(), any()))
          .thenReturn(EitherT.rightT[Future, UpstreamErrorResponse](HttpResponse(OK, "")))
        val saResponse = connector.connectToSelfAssessment("0000000010", 2022).value.futureValue
        verify(mockSessionCacheRepository, times(0))
          .getFromSession[HttpResponse](DataKey(any[String]()))(any(), any())
        verify(mockSelfAssessmentODSConnector, times(1))
          .connectToSelfAssessment(ArgumentMatchers.eq("0000000010"), ArgumentMatchers.eq(2022))(any(), any())
        saResponse mustBe a[Right[_, _]]
      }

      "a cached value is used when in a 'local' (stubbed) env (i.e. local or staging) and NOT one of test utrs" in {
        when(mockAppConfig.environment).thenReturn("local")
        when(mockSelfAssessmentODSConnector.connectToSelfAssessment(any(), any())(any(), any()))
          .thenReturn(EitherT.rightT[Future, UpstreamErrorResponse](HttpResponse(OK, "")))
        when(mockSessionCacheRepository.getFromSession[HttpResponse](DataKey(any[String]()))(any(), any()))
          .thenReturn(Future.successful(Some(HttpResponse(OK, ""))))

        val saResponse = connector.connectToSelfAssessment("0000000030", 2022).value.futureValue
        verify(mockSessionCacheRepository, times(1))
          .getFromSession[HttpResponse](DataKey(any[String]()))(any(), any())
        verify(mockSelfAssessmentODSConnector, times(0))
          .connectToSelfAssessment(ArgumentMatchers.eq("0000000030"), ArgumentMatchers.eq(2022))(any(), any())
        saResponse mustBe a[Right[_, _]]
      }

      "a cached value is used when in 'live' (non-stubbed) env and one of test utrs" in {
        when(mockAppConfig.environment).thenReturn("live")
        when(mockSelfAssessmentODSConnector.connectToSelfAssessment(any(), any())(any(), any()))
          .thenReturn(EitherT.rightT[Future, UpstreamErrorResponse](HttpResponse(OK, "")))
        when(mockSessionCacheRepository.getFromSession[HttpResponse](DataKey(any[String]()))(any(), any()))
          .thenReturn(Future.successful(Some(HttpResponse(OK, ""))))

        val saResponse = connector.connectToSelfAssessment("0000000010", 2022).value.futureValue
        verify(mockSessionCacheRepository, times(1))
          .getFromSession[HttpResponse](DataKey(any[String]()))(any(), any())
        verify(mockSelfAssessmentODSConnector, times(0))
          .connectToSelfAssessment(ArgumentMatchers.eq("0000000010"), ArgumentMatchers.eq(2022))(any(), any())
        saResponse mustBe a[Right[_, _]]
      }

      "a cached value is used when in 'qa' (non-stubbed) env and one of test utrs" in {
        when(mockAppConfig.environment).thenReturn("ist0")
        when(mockSelfAssessmentODSConnector.connectToSelfAssessment(any(), any())(any(), any()))
          .thenReturn(EitherT.rightT[Future, UpstreamErrorResponse](HttpResponse(OK, "")))
        when(mockSessionCacheRepository.getFromSession[HttpResponse](DataKey(any[String]()))(any(), any()))
          .thenReturn(Future.successful(Some(HttpResponse(OK, ""))))

        val saResponse = connector.connectToSelfAssessment("0000000010", 2022).value.futureValue
        verify(mockSessionCacheRepository, times(1))
          .getFromSession[HttpResponse](DataKey(any[String]()))(any(), any())
        verify(mockSelfAssessmentODSConnector, times(0))
          .connectToSelfAssessment(ArgumentMatchers.eq("0000000010"), ArgumentMatchers.eq(2022))(any(), any())
        saResponse mustBe a[Right[_, _]]
      }
    }

    "return a Left UpstreamErrorResponse object" in {
      when(mockSessionCacheRepository.getFromSession[HttpResponse](DataKey(any[String]()))(any(), any()))
        .thenReturn(Future.successful(None))

      when(mockSelfAssessmentODSConnector.connectToSelfAssessment("utr", 2022))
        .thenReturn(EitherT.leftT(UpstreamErrorResponse("Server error", 500)))

      when(
        mockSessionCacheRepository.putSession[HttpResponse](DataKey(any[String]()), any())(any(), any(), any())
      )
        .thenReturn(null)

      val saResponse = connector.connectToSelfAssessment("utr", 2022).value.futureValue

      verify(mockSessionCacheRepository, times(1))
        .getFromSession[HttpResponse](DataKey(any[String]()))(any(), any())

      verify(mockSessionCacheRepository, times(0))
        .putSession[HttpResponse](DataKey(any[String]()), any())(any(), any(), any())

      verify(mockSelfAssessmentODSConnector, times(1)).connectToSelfAssessment("utr", 2022)

      saResponse mustBe a[Left[_, _]]
    }
  }

  "Calling CachingSelfAssessmentODSConnectorSpec.connectToSelfAssessmentList" must {
    "return a Right response" when {
      "no value is cached" in {
        when(mockSessionCacheRepository.getFromSession[HttpResponse](DataKey(any[String]()))(any(), any()))
          .thenReturn(Future.successful(None))

        when(
          mockSessionCacheRepository.putSession[HttpResponse](DataKey(any[String]()), any())(any(), any(), any())
        ).thenReturn(Future.successful(("", "")))

        when(mockSelfAssessmentODSConnector.connectToSelfAssessmentList("utr"))
          .thenReturn(EitherT.rightT[Future, UpstreamErrorResponse](HttpResponse(OK, "")))

        val saResponse = connector.connectToSelfAssessmentList("utr").value.futureValue

        verify(mockSessionCacheRepository, times(1))
          .getFromSession[HttpResponse](DataKey(any[String]()))(any(), any())

        verify(mockSessionCacheRepository, times(1))
          .putSession[HttpResponse](DataKey(any[String]()), any())(any(), any(), any())

        verify(mockSelfAssessmentODSConnector, times(1)).connectToSelfAssessmentList("utr")

        saResponse mustBe a[Right[_, _]]
      }
    }
  }

  "Calling CachingSelfAssessmentODSConnectorSpec.connectToSATaxpayerDetails" must {
    "return a Right response" when {
      "no value is cached" in {
        when(mockSessionCacheRepository.getFromSession[HttpResponse](DataKey(any[String]()))(any(), any()))
          .thenReturn(Future.successful(None))

        when(
          mockSessionCacheRepository.putSession[HttpResponse](DataKey(any[String]()), any())(any(), any(), any())
        ).thenReturn(Future.successful(("", "")))

        when(mockSelfAssessmentODSConnector.connectToSATaxpayerDetails("utr"))
          .thenReturn(EitherT.rightT[Future, UpstreamErrorResponse](HttpResponse(OK, "")))

        val saResponse = connector.connectToSATaxpayerDetails("utr").value.futureValue

        verify(mockSessionCacheRepository, times(1))
          .getFromSession[HttpResponse](DataKey(any[String]()))(any(), any())

        verify(mockSessionCacheRepository, times(1))
          .putSession[HttpResponse](DataKey(any[String]()), any())(any(), any(), any())

        verify(mockSelfAssessmentODSConnector, times(1)).connectToSATaxpayerDetails("utr")

        saResponse mustBe a[Right[_, _]]
      }
    }
  }
}
