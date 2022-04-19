package metrics

import com.codahale.metrics.Timer
import org.mockito.Matchers.anyString
import org.mockito.Mockito
import org.mockito.Mockito.{times, verifyNoMoreInteractions, when}
import org.scalatest.{Assertion, BeforeAndAfterAll, OptionValues}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AsyncWordSpecLike
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.{BaseController, ControllerComponents}
import play.api.test.{FakeRequest, Helpers}

class MetricsSpec extends AsyncWordSpecLike with Matchers with OptionValues with MockitoSugar with BeforeAndAfterAll {

  trait MockMetrics { self: Metrics =>
    val timer = mock[Timer.Context]
    val metrics = mock[Metrics]
    override val localMetrics: LocalMetrics = mock[LocalMetrics]
    when(localMetrics.startTimer(anyString())) thenReturn timer
  }

//  class TestHasMetrics extends Metrics with MockMetrics

  class TestActionMetrics extends BaseController with MockMetrics {
    override protected def controllerComponents: ControllerComponents = Helpers.stubControllerComponents()
  }

  def withTestActionMetrics[A](test: TestActionMetrics => A): A =
    test(new TestActionMetrics)

  def verifyCompletedWithSuccess(metricName: String, metrics: MockMetrics): Assertion = {
    val inOrder = Mockito.inOrder(metrics.localMetrics, metrics.timer)
    inOrder.verify(metrics.localMetrics, times(1)).startTimer(metricName)
    inOrder.verify(metrics.localMetrics, times(1)).stopTimer(metrics.timer)
    inOrder.verify(metrics.localMetrics, times(1)).incrementSuccessCounter(metricName)
    verifyNoMoreInteractions(metrics.localMetrics)
    verifyNoMoreInteractions(metrics.timer)
    succeed
  }

  def verifyCompletedWithFailure(metricName: String, metrics: MockMetrics): Assertion = {
    val inOrder = Mockito.inOrder(metrics.localMetrics, metrics.timer)
    inOrder.verify(metrics.localMetrics, times(1)).startTimer(metricName)
    inOrder.verify(metrics.localMetrics, times(1)).stopTimer(metrics.timer)
    inOrder.verify(metrics.localMetrics, times(1)).incrementFailedCounter(metricName)
    verifyNoMoreInteractions(metrics.localMetrics)
    verifyNoMoreInteractions(metrics.timer)
    succeed
  }

  val TestMetric = "test-metric"

  "Metrics" when {

    "withMetricsTimerAction" should {
      def fakeRequest = FakeRequest()

      "increment success counter for an informational Result" in withTestActionMetrics { metrics =>
        metrics
          .withMetricsTimerAction(TestMetric) {
            metrics.Action(Results.SwitchingProtocols)
          }
          .apply(fakeRequest)
          .map(_ => verifyCompletedWithSuccess(TestMetric, metrics))
      }

      "increment success counter for a successful Result" in withTestActionMetrics { metrics =>
        metrics
          .withMetricsTimerAction(TestMetric) {
            metrics.Action(Results.Ok)
          }
          .apply(fakeRequest)
          .map(_ => verifyCompletedWithSuccess(TestMetric, metrics))
      }

      "increment success counter for a redirect Result" in withTestActionMetrics { metrics =>
        metrics
          .withMetricsTimerAction(TestMetric) {
            metrics.Action(Results.Found("https://wikipedia.org"))
          }
          .apply(fakeRequest)
          .map(_ => verifyCompletedWithSuccess(TestMetric, metrics))
      }

      "increment failure counter for a client error Result" in withTestActionMetrics { metrics =>
        metrics
          .withMetricsTimerAction(TestMetric) {
            metrics.Action(Results.Conflict)
          }
          .apply(fakeRequest)
          .map(_ => verifyCompletedWithFailure(TestMetric, metrics))
      }

      "increment failure counter for a server error Result" in withTestActionMetrics { metrics =>
        metrics
          .withMetricsTimerAction(TestMetric) {
            metrics.Action(Results.ServiceUnavailable)
          }
          .apply(fakeRequest)
          .map(_ => verifyCompletedWithFailure(TestMetric, metrics))
      }
    }
  }

}