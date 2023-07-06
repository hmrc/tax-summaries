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

import cats.data.EitherT
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.{eq => eqMatch}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import services.OdsService
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import utils.TestConstants.testUtr
import play.api.inject.bind
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext
import scala.jdk.CollectionConverters._
import scala.util.Random

class OdsIndividualYearsServiceSpec extends BaseSpec {

  val mockOdsService: OdsService = mock[OdsService]

  override lazy val app = GuiceApplicationBuilder()
    .overrides(
      bind[OdsService].toInstance(mockOdsService)
    )
    .build()

  lazy val sut         = app.injector.instanceOf[OdsIndividualYearsService]
  implicit lazy val ec = app.injector.instanceOf[ExecutionContext]
  implicit lazy val hc = HeaderCarrier()

  implicit val request = FakeRequest()

  override def beforeEach() = reset(mockOdsService)

  "getAtsList build list of years from individual years" when {
    "all calls are successful" in {
      val listOfYears = (2016 to 2020).toList
      val eventCaptor = ArgumentCaptor.forClass(classOf[Int])

      when(mockOdsService.getPayload(any(), any())(any(), any())).thenReturn(
        EitherT.rightT(Json.parse("{}"))
      )
      val result = sut.getAtsList(testUtr, listOfYears.reverse.head, listOfYears.size - 1).value.futureValue

      result mustBe Right(listOfYears)
      verify(mockOdsService, times(listOfYears.size)).getPayload(any(), eventCaptor.capture())(any(), any())
      val years: List[Int] = eventCaptor.getAllValues.asScala.toList
      years.sorted mustBe listOfYears
    }

    "with not found responses excluded" in {
      val listOfYears = (2016 to 2020).toList
      val eventCaptor = ArgumentCaptor.forClass(classOf[Int])

      when(mockOdsService.getPayload(any(), any())(any(), any())).thenReturn(
        EitherT.leftT(UpstreamErrorResponse("Not Found", 404))
      )
      val result = sut.getAtsList(testUtr, listOfYears.reverse.head, listOfYears.size - 1).value.futureValue

      result mustBe Right(List.empty)
      verify(mockOdsService, times(listOfYears.size)).getPayload(any(), eventCaptor.capture())(any(), any())
      val years: List[Int] = eventCaptor.getAllValues.asScala.toList
      years.sorted mustBe listOfYears
    }

    "with any 4XX responses ignored" in {
      val listOfYears = (2016 to 2020).toList
      val eventCaptor = ArgumentCaptor.forClass(classOf[Int])

      listOfYears.foreach { year =>
        val random = Random.between(400, 499)
        when(mockOdsService.getPayload(any(), eqMatch(year))(any(), any())).thenReturn(
          EitherT.leftT(UpstreamErrorResponse("random client error", random))
        )
      }
      val result = sut.getAtsList(testUtr, listOfYears.reverse.head, listOfYears.size - 1).value.futureValue

      result mustBe Right(List.empty)
      verify(mockOdsService, times(listOfYears.size)).getPayload(any(), eventCaptor.capture())(any(), any())
      val years: List[Int] = eventCaptor.getAllValues.asScala.toList
      years.sorted mustBe listOfYears
    }

    "one call is retried successfully" in {
      val listOfYears = (2016 to 2020).toList
      val eventCaptor = ArgumentCaptor.forClass(classOf[Int])

      listOfYears.tail.foreach { year =>
        when(mockOdsService.getPayload(any(), eqMatch(year))(any(), any())).thenReturn(
          EitherT.rightT(Json.parse("{}"))
        )
      }

      when(mockOdsService.getPayload(any(), eqMatch(listOfYears.head))(any(), any())).thenReturn(
        EitherT.leftT(UpstreamErrorResponse("Server error", 500)),
        EitherT.rightT(Json.parse("{}"))
      )

      val result = sut.getAtsList(testUtr, listOfYears.reverse.head, listOfYears.size - 1).value.futureValue

      result mustBe Right(listOfYears)
      verify(mockOdsService, times(listOfYears.size + 1)).getPayload(any(), eventCaptor.capture())(any(), any())
      val years: List[Int] = eventCaptor.getAllValues.asScala.toList
      years.sorted mustBe listOfYears.head +: listOfYears
    }

  }

  "getAtsList returns an error" when {
    "retry is not successful" in {
      val listOfYears = (2016 to 2020).toList
      val eventCaptor = ArgumentCaptor.forClass(classOf[Int])

      listOfYears.tail.foreach { year =>
        when(mockOdsService.getPayload(any(), eqMatch(year))(any(), any())).thenReturn(
          EitherT.rightT(Json.parse("{}"))
        )
      }

      when(mockOdsService.getPayload(any(), eqMatch(listOfYears.head))(any(), any())).thenReturn(
        EitherT.leftT(UpstreamErrorResponse("Server error", 500))
      )

      val result = sut.getAtsList(testUtr, listOfYears.reverse.head, listOfYears.size - 1).value.futureValue

      result mustBe a[Left[_, UpstreamErrorResponse]]
      verify(mockOdsService, times(listOfYears.size + 1)).getPayload(any(), eventCaptor.capture())(any(), any())
      val years: List[Int] = eventCaptor.getAllValues.asScala.toList
      years.sorted mustBe listOfYears.head +: listOfYears
    }
  }

  "two or more calls are failing" in {
    val listOfYears = (2016 to 2020).toList
    val eventCaptor = ArgumentCaptor.forClass(classOf[Int])

    val failures  = listOfYears.take(2)
    val successes = listOfYears.drop(2)

    failures.foreach { year =>
      when(mockOdsService.getPayload(any(), eqMatch(year))(any(), any())).thenReturn(
        EitherT.leftT(UpstreamErrorResponse("Server error", 500))
      )
    }

    successes.foreach { year =>
      when(mockOdsService.getPayload(any(), eqMatch(year))(any(), any())).thenReturn(
        EitherT.rightT(Json.parse("{}"))
      )
    }

    val result = sut.getAtsList(testUtr, listOfYears.reverse.head, listOfYears.size - 1).value.futureValue

    result mustBe a[Left[_, UpstreamErrorResponse]]
    verify(mockOdsService, times(listOfYears.size)).getPayload(any(), eventCaptor.capture())(any(), any())
    val years: List[Int] = eventCaptor.getAllValues.asScala.toList
    years.sorted mustBe listOfYears
  }

}
