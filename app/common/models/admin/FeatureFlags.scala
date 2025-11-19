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

package common.models.admin

import uk.gov.hmrc.mongoFeatureToggles.model.Environment.Environment
import uk.gov.hmrc.mongoFeatureToggles.model.{Environment, FeatureFlagName}

object AllFeatureFlags {
  val list: List[FeatureFlagName] =
    List(PayeDetailsFromHipToggle)
}

case object PayeDetailsFromHipToggle extends FeatureFlagName {
  override val name: String                         = "paye-details-from-hip-toggle"
  override val description: Option[String]          = Some(
    "Enable/disable calls to HIP for PAYE annual tax summary data using API-1535: `/individuals/annual-tax-summary/<nino>/<taxYear>`"
  )
  override val lockedEnvironments: Seq[Environment] =
    Seq(Environment.Local, Environment.Qa, Environment.Production, Environment.Staging)
}
