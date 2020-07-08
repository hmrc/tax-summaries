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

package connectors

import com.google.inject.Inject
import config.WSHttp
import play.api.Mode.Mode
import play.api.libs.json.JsValue
import play.api.{Configuration, Play}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.config.ServicesConfig

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ODSConnector @Inject()(http: WSHttp) extends ServicesConfig {

  protected def mode: Mode = Play.current.mode
  protected def runModeConfiguration: Configuration = Play.current.configuration

  val serviceUrl = baseUrl("tax-summaries-hod")

  def url(path: String) = s"$serviceUrl$path"

  def connectToSelfAssessment(UTR: String, TAX_YEAR: Int)(implicit hc: HeaderCarrier): Future[JsValue] =
    http.GET[JsValue](url("/self-assessment/individuals/" + UTR + "/annual-tax-summaries/" + TAX_YEAR))

  def connectToSelfAssessmentList(UTR: String)(implicit hc: HeaderCarrier): Future[JsValue] =
    http.GET[JsValue](url("/self-assessment/individuals/" + UTR + "/annual-tax-summaries"))

  def connectToSATaxpayerDetails(UTR: String)(implicit hc: HeaderCarrier): Future[JsValue] =
    http.GET[JsValue](url("/self-assessment/individual/" + UTR + "/designatory-details/taxpayer"))
}
