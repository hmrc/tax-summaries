/*
 * Copyright 2020 HM Revenue & Customs
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

package metrics

import com.codahale.metrics.Timer
import com.kenshoo.play.metrics.Metrics
import org.mockito.Matchers._
import org.mockito.Mockito
import org.mockito.Mockito._
import org.scalatest._
import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.play.test.UnitSpec

class HasMetricsSpec
    extends UnitSpec with GuiceOneAppPerSuite with PatienceConfiguration with BeforeAndAfterEach with MockitoSugar {

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(
      "metrics.enabled" -> false
    )
    .build()

  trait SetUp {
    class TestHasMetrics extends HasMetrics {
      val timer = mock[Timer.Context]
      val metricId = "test"

      override def metrics: Metrics = mock[Metrics]

      override val localMetrics = mock[LocalMetrics]
      when(localMetrics.startTimer(any())) thenReturn timer

      def testCompleteTimerAndIncrementSuccessCounter(): Unit = withMetricsTimerAsync(metricId) { t =>
        t.completeTimerAndIncrementSuccessCounter()

        val inOrder = Mockito.inOrder(localMetrics, timer)

        inOrder.verify(localMetrics, times(1)).startTimer(metricId)
        inOrder.verify(localMetrics, times(1)).stopTimer(timer)
        inOrder.verify(localMetrics, times(1)).incrementSuccessCounter(metricId)
      }

      def testCompleteTimerAndIncrementFailedCounter(): Unit = withMetricsTimerAsync(metricId) { t =>
        t.completeTimerAndIncrementFailedCounter()

        val inOrder = Mockito.inOrder(localMetrics, timer)

        inOrder.verify(localMetrics, times(1)).startTimer(metricId)
        inOrder.verify(localMetrics, times(1)).stopTimer(timer)
        inOrder.verify(localMetrics, times(1)).incrementFailedCounter(metricId)
      }
    }
  }

  "completeTimerAndIncrementSuccessCounter should start/stop the timer and increment the success counter in a specific order" in new SetUp {
    new TestHasMetrics().testCompleteTimerAndIncrementSuccessCounter
  }

  "completeTimerAndIncrementFailedCounter should start/stop the timer and increment the failed counter in a specific order" in new SetUp {
    new TestHasMetrics().testCompleteTimerAndIncrementFailedCounter
  }
}
