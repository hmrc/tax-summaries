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

package connectors

import cats.data.EitherT
import cats.implicits._
import org.mockito.ArgumentMatchers.any
import play.api.Application
import play.api.inject.bind
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import repositories.SessionCacheRepository
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, UpstreamErrorResponse}
import uk.gov.hmrc.mongo.cache.DataKey
import utils.{BaseSpec, WireMockHelper}

import scala.concurrent.{ExecutionContext, Future}

class CachingSelfAssessmentODSConnectorSpec extends BaseSpec with ConnectorSpec with WireMockHelper {

  val mockSelfAssessmentODSConnector: SelfAssessmentODSConnector = mock[SelfAssessmentODSConnector]
  val mockSessionCacheRepository: SessionCacheRepository         = mock[SessionCacheRepository]

  override implicit val hc: HeaderCarrier         = HeaderCarrier()
  override implicit lazy val ec: ExecutionContext = inject[ExecutionContext]

  override implicit lazy val app: Application = app(
    Map("microservice.services.agent-client-authorisation.port" -> server.port()),
    bind(classOf[SelfAssessmentODSConnector])
      .qualifiedWith("default")
      .toInstance(mockSelfAssessmentODSConnector),
    bind[SessionCacheRepository].toInstance(mockSessionCacheRepository)
  )

  override def beforeEach(): Unit = {
    reset(mockSelfAssessmentODSConnector)
    reset(mockSessionCacheRepository)
  }

  def connector: CachingSelfAssessmentODSConnector = inject[CachingSelfAssessmentODSConnector]

  val url = "/agent-client-authorisation/status"

  implicit val userRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest()

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
    }

    "return a Left UpstreamErrorResponse object" ignore {
      stubGet(url, INTERNAL_SERVER_ERROR, None)

      val result = connector.connectToSelfAssessment("utr", 2022).value.futureValue
      result mustBe a[Left[_, _]]
      result.swap.getOrElse(UpstreamErrorResponse("", OK)) mustBe an[UpstreamErrorResponse]
    }
  }
}
