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

package utils

import models.ODSModels.{SaTaxpayerDetails, SelfAssessmentList}
import models.{AtsYearList, TaxSummaryLiability}
import play.api.libs.json.{JsNumber, JsValue, Json}
import transformers.ATSRawDataTransformer

trait TaxsJsonHelper {

  def getAllATSData(
    taxpayerDetails: SaTaxpayerDetails,
    taxSummaryLiability: TaxSummaryLiability,
    UTR: String,
    taxYear: Int): JsValue =
    Json.toJson(ATSRawDataTransformer(taxSummaryLiability, taxpayerDetails, UTR, taxYear).atsDataDTO)

  def hasAtsForPreviousPeriod(saList: SelfAssessmentList): Boolean =
    saList.annualTaxSummaries.map(summary => summary.taxYearEnd.nonEmpty).reduce(_ && _)

  def createTaxYearJson(saList: SelfAssessmentList, utr: String, taxpayerDetails: SaTaxpayerDetails): JsValue = {
    val atsYearList = saList.annualTaxSummaries.flatMap(_.taxYearEnd.map(JsNumber(_)))
    val taxPayer = taxpayerDetails.atsTaxpayerDataDTO

    Json.toJson(AtsYearList(utr, Some(taxPayer), Some(atsYearList)))
  }
}
