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

package models.ODSModels

import play.api.libs.json.{Json, OFormat}

case class SelfAssessmentList(annualTaxSummaries: List[AnnualTaxSummary], links: List[Link])

object SelfAssessmentList {
  implicit val formats: OFormat[SelfAssessmentList] = Json.format[SelfAssessmentList]
}

case class AnnualTaxSummary(taxYearEnd: Option[Int], links: List[Link])

object AnnualTaxSummary {
  implicit val formats: OFormat[AnnualTaxSummary] = Json.format[AnnualTaxSummary]
}

case class Link(rel: String, href: String)

object Link {
  implicit val formats: OFormat[Link] = Json.format[Link]
}
