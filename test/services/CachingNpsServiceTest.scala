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

package services

import models.paye.PayeAtsMiddleTier
import org.mockito.Matchers.any
import org.mockito.Mockito
import org.mockito.Mockito.{mock, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.IntegrationPatience
import play.api.http.Status.INTERNAL_SERVER_ERROR
import repositories.Repository
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import utils.BaseSpec

import scala.concurrent.Future

class CachingNpsServiceTest extends BaseSpec with BeforeAndAfterEach {
  val IM_A_TEAPOT = 418

  val repository = mock[Repository]
  val innerService = mock[DirectNpsService]

  class Fixture extends NpsService(repository, innerService)

  override def beforeEach(): Unit = {
    Mockito.reset(repository)
    Mockito.reset(innerService)
    super.beforeEach()
  }

  "CachingNpsService" should {
    "Retrieve data from the cache" in new Fixture {
      val data = new PayeAtsMiddleTier(2627, "NINONINO", None, None, None, None, None)
      when(repository.get(any(), any())).thenReturn(Future.successful(Some(data)))

      val result = getPayeATSData("NONONONO", 5465)(HeaderCarrier())
      result.futureValue shouldBe Right(data)
    }

    "Retrieve data from the InnerService when cache empty and refresh the cache" in new Fixture {
      val data = new PayeAtsMiddleTier(2627, "NINONINO", None, None, None, None, None)

      when(repository.get(any(), any())).thenReturn(Future.successful(None))
      when(repository.set(any(), any(), any())).thenReturn(Future.successful(true))
      when(innerService.getPayeATSData(any(), any())(any())).thenReturn(Future.successful(Right(data)))

      whenReady(getPayeATSData("NONONONO", 5465)(HeaderCarrier())) { result =>
        result shouldBe Right(data)
        verify(repository).set("NONONONO", 5465, data)
      }
    }

    "Return an internal server error when refreshing the cache fails" in new Fixture {
      val data = new PayeAtsMiddleTier(2627, "NINONINO", None, None, None, None, None)

      when(repository.get(any(), any())).thenReturn(Future.successful(None))
      when(repository.set(any(), any(), any())).thenReturn(Future.failed(new Exception("Failed")))
      when(innerService.getPayeATSData(any(), any())(any())).thenReturn(Future.successful(Right(data)))

      whenReady(getPayeATSData("NONONONO", 5465)(HeaderCarrier())) { result =>
        result match {
          case Left(response) => response.status shouldBe INTERNAL_SERVER_ERROR
          case _              => fail("Incorrect response from Caching Service")
        }
        verify(repository).set("NONONONO", 5465, data)
      }
    }
    "Return an internal server error when retrieving from the cache fails" in new Fixture {
      val data = new PayeAtsMiddleTier(2627, "NINONINO", None, None, None, None, None)

      when(repository.get(any(), any())).thenReturn(Future.failed(new Exception("Failed")))

      whenReady(getPayeATSData("NONONONO", 5465)(HeaderCarrier())) { result =>
        result match {
          case Left(response) => response.status shouldBe INTERNAL_SERVER_ERROR
          case _              => fail("Incorrect response from Caching Service")
        }
      }
    }

    "Pass through the response when Inner service fails" in new Fixture {
      when(repository.get(any(), any())).thenReturn(Future.successful(None))
      when(innerService.getPayeATSData(any(), any())(any()))
        .thenReturn(Future.successful(Left(HttpResponse(IM_A_TEAPOT))))

      whenReady(getPayeATSData("NONONONO", 5465)(HeaderCarrier())) {
        case Left(response) => response.status shouldBe IM_A_TEAPOT
        case _              => fail("Incorrect reponse from Caching Service")
      }

    }
  }
}
