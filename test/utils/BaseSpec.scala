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

import config.ApplicationConfig
import org.mockito.MockitoSugar
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.test.Injecting
import uk.gov.hmrc.domain.{Generator, Nino}
import uk.gov.hmrc.mongoFeatureToggles.services.FeatureFlagService

class BaseSpec
    extends AnyWordSpec
    with Matchers
    with GuiceOneAppPerSuite
    with Injecting
    with MockitoSugar
    with ScalaFutures
    with IntegrationPatience
    with BeforeAndAfterEach {
  lazy val applicationConfig: ApplicationConfig = inject[ApplicationConfig]

  val generatedNino: Nino = new Generator().nextNino

  implicit lazy val mockFeatureFlagService: FeatureFlagService = mock[FeatureFlagService]

}
