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
import config.ApplicationConfig
import play.api.libs.json.JsValue
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReads, HttpResponse}
import uk.gov.hmrc.play.http.ws.WSHttpResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ODSConnector @Inject()(http: HttpClient, applicationConfig: ApplicationConfig) {

  val serviceUrl = applicationConfig.npsServiceUrl

  def url(path: String) = s"$serviceUrl$path"

  def connectToSelfAssessment(UTR: String, TAX_YEAR: Int)(implicit hc: HeaderCarrier): Future[Option[JsValue]] =
    http.GET[Option[JsValue]](url("/self-assessment/individuals/" + UTR + "/annual-tax-summaries/" + TAX_YEAR))

  def connectToSelfAssessmentList(UTR: String)(implicit hc: HeaderCarrier): Future[Option[JsValue]] =
    http.GET[Option[JsValue]](url("/self-assessment/individuals/" + UTR + "/annual-tax-summaries"))

  def connectToSATaxpayerDetails(UTR: String)(implicit hc: HeaderCarrier): Future[Option[JsValue]] =
    http.GET[Option[JsValue]](url("/self-assessment/individual/" + UTR + "/designatory-details/taxpayer"))
}
