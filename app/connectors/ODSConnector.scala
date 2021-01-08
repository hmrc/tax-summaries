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

package connectors

import com.google.inject.Inject
import com.kenshoo.play.metrics.Metrics
import config.ApplicationConfig
import metrics.uk.gov.hmrc.tai.metrics.metrics.HasMetrics
import play.api.libs.json.JsValue
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ODSConnector @Inject()(http: HttpClient, applicationConfig: ApplicationConfig, val metrics: Metrics)
    extends HasMetrics with ExtraHeaders {

  val serviceUrl = applicationConfig.npsServiceUrl
  def url(path: String): String = s"$serviceUrl$path"

  def connectToSelfAssessment(UTR: String, TAX_YEAR: Int)(implicit hc: HeaderCarrier): Future[JsValue] =
    withMetricsTimerAsync("self-assessment-for-tax-year") { _ =>
      http.GET[JsValue](url("/self-assessment/individuals/" + UTR + "/annual-tax-summaries/" + TAX_YEAR))(
        implicitly,
        extraHeaders,
        implicitly)
    }

  def connectToSelfAssessmentList(UTR: String)(implicit hc: HeaderCarrier): Future[JsValue] =
    withMetricsTimerAsync("self-assessment-list") { _ =>
      http.GET[JsValue](url("/self-assessment/individuals/" + UTR + "/annual-tax-summaries"))(
        implicitly,
        extraHeaders,
        implicitly)
    }

  def connectToSATaxpayerDetails(UTR: String)(implicit hc: HeaderCarrier): Future[JsValue] =
    withMetricsTimerAsync("self-assessment-tax-payers-details") { _ =>
      http.GET[JsValue](url("/self-assessment/individual/" + UTR + "/designatory-details/taxpayer"))(
        implicitly,
        extraHeaders,
        implicitly)
    }
}
