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

package paye.services

import cats.data.EitherT
import cats.instances.future.*
import common.config.ApplicationConfig
import common.utils.BaseSpec
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.mockito.{ArgumentCaptor, Mockito}
import paye.models.{PayeAtsMiddleTier, PayeAtsMiddleTierMongo}
import paye.repositories.Repository
import play.api.http.Status.{BAD_GATEWAY, INTERNAL_SERVER_ERROR, NOT_FOUND}
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}

import java.sql.Timestamp
import java.time.{Instant, LocalDateTime}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.jdk.CollectionConverters.*

class CachingNpsServiceTest extends BaseSpec {
  val IM_A_TEAPOT = 418

  val repository: Repository         = mock[Repository]
  val innerService: DirectNpsService = mock[DirectNpsService]
  val config: ApplicationConfig      = mock[ApplicationConfig]

  class Fixture extends NpsService(repository, innerService, config)

  override def beforeEach(): Unit = {
    Mockito.reset(repository)
    Mockito.reset(innerService)
    Mockito.reset(config)
    super.beforeEach()
  }

  def buildId(nino: String, taxYear: Int): String = s"$nino::$taxYear"

  val ttl: Instant = Timestamp.valueOf(LocalDateTime.now.plusMinutes(15)).toInstant

  "getAtsPayeDataMultipleYears" must {
    "return a successful response" in new Fixture {
      val data                             = new PayeAtsMiddleTier(2627, generatedNino.nino, None, None, None, None, None)
      val eventCaptor: ArgumentCaptor[Int] = ArgumentCaptor.forClass(classOf[Int])
      when(config.calculateExpiryTime()).thenReturn(ttl)
      when(repository.get(any(), any())).thenReturn(Future.successful(None))
      when(innerService.getPayeATSData(any(), any())(any())).thenReturn(EitherT.rightT(data))
      when(repository.set(any())).thenReturn(Future.successful(true))

      whenReady(getAtsPayeDataMultipleYears(generatedNino.nino, List(2000, 2001))(HeaderCarrier()).value) { result =>
        result mustBe Right(List(data, data))
        verify(repository, times(2)).set(any())
        verify(innerService, times(2)).getPayeATSData(any(), eventCaptor.capture())(any())
        val argsYear: List[Int] = eventCaptor.getAllValues.asScala.toList
        argsYear.sorted mustBe List(2000, 2001)
      }
    }

    "return an empty list" in new Fixture {
      val eventCaptor: ArgumentCaptor[Int] = ArgumentCaptor.forClass(classOf[Int])
      when(config.calculateExpiryTime()).thenReturn(ttl)
      when(repository.get(any(), any())).thenReturn(Future.successful(None))
      when(repository.set(any())).thenReturn(Future.successful(true))
      when(innerService.getPayeATSData(any(), any())(any()))
        .thenReturn(EitherT.leftT(UpstreamErrorResponse("", NOT_FOUND)))

      whenReady(getAtsPayeDataMultipleYears(generatedNino.nino, List(2000, 2001))(HeaderCarrier()).value) { result =>
        result mustBe Right(List.empty)
        verify(repository, times(0)).set(any())
        verify(innerService, times(2)).getPayeATSData(any(), eventCaptor.capture())(any())
        val argsYear: List[Int] = eventCaptor.getAllValues.asScala.toList
        argsYear.sorted mustBe List(2000, 2001)
      }
    }

    "return a failure" in new Fixture {
      val data                             = new PayeAtsMiddleTier(2627, generatedNino.nino, None, None, None, None, None)
      val eventCaptor: ArgumentCaptor[Int] = ArgumentCaptor.forClass(classOf[Int])
      when(config.calculateExpiryTime()).thenReturn(ttl)
      when(repository.get(any(), any())).thenReturn(Future.successful(None))
      when(repository.set(any())).thenReturn(Future.successful(true))
      when(innerService.getPayeATSData(any(), any())(any())).thenReturn(
        EitherT.rightT(data),
        EitherT.leftT(UpstreamErrorResponse("", INTERNAL_SERVER_ERROR)),
        EitherT.rightT(data)
      )

      whenReady(getAtsPayeDataMultipleYears(generatedNino.nino, List(2000, 2001, 2002))(HeaderCarrier()).value) {
        result =>
          result mustBe a[Left[UpstreamErrorResponse, _]]
          verify(repository, times(2)).set(any())
          verify(innerService, times(3)).getPayeATSData(any(), eventCaptor.capture())(any())
          val argsYear: List[Int] = eventCaptor.getAllValues.asScala.toList
          argsYear.sorted mustBe List(2000, 2001, 2002)
      }
    }
  }

  "caching getPayeATSData" must {
    "Retrieve data from the cache" in new Fixture {
      val data      = new PayeAtsMiddleTier(2627, generatedNino.nino, None, None, None, None, None)
      val dataMongo = new PayeAtsMiddleTierMongo(buildId(generatedNino.nino, 2627), data, ttl)

      when(config.calculateExpiryTime()).thenReturn(ttl)
      when(repository.get(any(), any())).thenReturn(Future.successful(Some(dataMongo)))

      val result: Future[Either[UpstreamErrorResponse, PayeAtsMiddleTier]] =
        getPayeATSData(generatedNino.nino, 5465)(HeaderCarrier()).value
      result.futureValue mustBe Right(data)
    }

    "Retrieve data from the InnerService when cache empty and refresh the cache" in new Fixture {
      val data = new PayeAtsMiddleTier(2627, generatedNino.nino, None, None, None, None, None)
      when(config.calculateExpiryTime()).thenReturn(ttl)
      when(repository.get(any(), any())).thenReturn(Future.successful(None))
      when(repository.set(any())).thenReturn(Future.successful(true))
      when(innerService.getPayeATSData(any(), any())(any())).thenReturn(EitherT.rightT(data))

      whenReady(getPayeATSData(generatedNino.nino, 5465)(HeaderCarrier()).value) { result =>
        result mustBe Right(data)
        verify(repository).set(any())
      }
    }

    "Return an internal server error when refreshing the cache fails" in new Fixture {
      val data = new PayeAtsMiddleTier(2627, generatedNino.nino, None, None, None, None, None)

      when(repository.get(any(), any())).thenReturn(Future.successful(None))
      when(repository.set(any())).thenReturn(Future.failed(new Exception("Failed")))
      when(innerService.getPayeATSData(any(), any())(any())).thenReturn(EitherT.rightT(data))

      val result: Future[Either[UpstreamErrorResponse, PayeAtsMiddleTier]] =
        getPayeATSData(generatedNino.nino, 5465)(HeaderCarrier()).value

      whenReady(result.failed) { e =>
        e mustBe a[Exception]
        verify(repository).set(any())
      }
    }

    "Return an internal server error when retrieving from the cache fails" in new Fixture {
      when(repository.get(any(), any())).thenReturn(Future.failed(new Exception("Failed")))

      val result: Future[Either[UpstreamErrorResponse, PayeAtsMiddleTier]] =
        getPayeATSData(generatedNino.nino, 5465)(HeaderCarrier()).value

      whenReady(result.failed) { e =>
        e mustBe a[Exception]
      }
    }

    "Pass through the response when Inner service fails" in new Fixture {
      val response: UpstreamErrorResponse = UpstreamErrorResponse("Bad Gateway", BAD_GATEWAY, BAD_GATEWAY)

      when(repository.get(any(), any())).thenReturn(Future.successful(None))
      when(innerService.getPayeATSData(any(), any())(any()))
        .thenReturn(EitherT.leftT(response))

      whenReady(getPayeATSData(generatedNino.nino, 5465)(HeaderCarrier()).value) {
        case Left(response) => response mustBe a[UpstreamErrorResponse]
        case _              => fail("Incorrect response from Caching Service")
      }
    }
  }
}
