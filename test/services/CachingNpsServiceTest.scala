/*
 * Copyright 2022 HM Revenue & Customs
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

package services

import cats.data.EitherT
import config.ApplicationConfig
import models.paye.{PayeAtsMiddleTier, PayeAtsMiddleTierMongo}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import play.api.http.Status.BAD_GATEWAY
import repositories.Repository
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import utils.BaseSpec

import java.sql.Timestamp
import java.time.{Instant, LocalDateTime}
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class CachingNpsServiceTest extends BaseSpec {
  val IM_A_TEAPOT = 418

  val repository: Repository         = mock[Repository]
  val innerService: DirectNpsService = mock[DirectNpsService]
  val config: ApplicationConfig      = mock[ApplicationConfig]

  class Fixture extends NpsService(repository, innerService, config)

  override def beforeEach(): Unit = {
    Mockito.reset(repository)
    Mockito.reset(innerService)
    super.beforeEach()
  }

  def buildId(nino: String, taxYear: Int): String = s"$nino::$taxYear"

  val ttl: Instant = Timestamp.valueOf(LocalDateTime.now.plusMinutes(15)).toInstant

  "CachingNpsService" must {
    "Retrieve data from the cache" in new Fixture {
      val data      = new PayeAtsMiddleTier(2627, "NINONINO", None, None, None, None, None)
      val dataMongo = new PayeAtsMiddleTierMongo(buildId("NINONINO", 2627), data, ttl)

      when(config.calculateExpiryTime()).thenReturn(ttl)
      when(repository.get(any(), any())).thenReturn(Future.successful(Some(dataMongo)))

      val result: Future[Either[UpstreamErrorResponse, PayeAtsMiddleTier]] =
        getPayeATSData("NONONONO", 5465)(HeaderCarrier()).value
      result.futureValue mustBe Right(data)
    }

    "Retrieve data from the InnerService when cache empty and refresh the cache" in new Fixture {
      val data = new PayeAtsMiddleTier(2627, "NINONINO", None, None, None, None, None)
      when(config.calculateExpiryTime()).thenReturn(ttl)
      when(repository.get(any(), any())).thenReturn(Future.successful(None))
      when(repository.set(any())).thenReturn(Future.successful(true))
      when(innerService.getPayeATSData(any(), any())(any())).thenReturn(EitherT.rightT(data))

      whenReady(getPayeATSData("NONONONO", 5465)(HeaderCarrier()).value) { result =>
        result mustBe Right(data)
        verify(repository).set(any())
      }
    }

    "Return an internal server error when refreshing the cache fails" in new Fixture {
      val data = new PayeAtsMiddleTier(2627, "NINONINO", None, None, None, None, None)

      when(repository.get(any(), any())).thenReturn(Future.successful(None))
      when(repository.set(any())).thenReturn(Future.failed(new Exception("Failed")))
      when(innerService.getPayeATSData(any(), any())(any())).thenReturn(EitherT.rightT(data))

      val result: Future[Either[UpstreamErrorResponse, PayeAtsMiddleTier]] =
        getPayeATSData("NONONONO", 5465)(HeaderCarrier()).value

      whenReady(result.failed) { e =>
        e mustBe a[Exception]
        verify(repository).set(any())
      }
    }

    "Return an internal server error when retrieving from the cache fails" in new Fixture {
      when(repository.get(any(), any())).thenReturn(Future.failed(new Exception("Failed")))

      val result: Future[Either[UpstreamErrorResponse, PayeAtsMiddleTier]] =
        getPayeATSData("NONONONO", 5465)(HeaderCarrier()).value

      whenReady(result.failed) { e =>
        e mustBe a[Exception]
      }
    }

    "Pass through the response when Inner service fails" in new Fixture {
      val response: UpstreamErrorResponse = UpstreamErrorResponse("Bad Gateway", BAD_GATEWAY, BAD_GATEWAY)

      when(repository.get(any(), any())).thenReturn(Future.successful(None))
      when(innerService.getPayeATSData(any(), any())(any()))
        .thenReturn(EitherT.leftT(response))

      whenReady(getPayeATSData("NONONONO", 5465)(HeaderCarrier()).value) {
        case Left(response) => response mustBe a[UpstreamErrorResponse]
        case _              => fail("Incorrect reponse from Caching Service")
      }
    }
  }
}
