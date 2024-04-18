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

import com.google.inject.Inject
import config.ApplicationConfig
import models.AtsMiddleTierData.noAtsResult
import models.{AtsYearList, TaxSummaryLiability}
import play.api.libs.json.{JsNumber, JsValue, Json}
import services.TaxRateService
import transformers.{ATSCalculations, ATSRawDataTransformer, ATSTaxpayerDataTransformer}
import uk.gov.hmrc.http.HeaderCarrier

class TaxsJsonHelper @Inject() (applicationConfig: ApplicationConfig, aTSRawDataTransformer: ATSRawDataTransformer) {

  def getAllATSData(rawTaxpayerJson: JsValue, rawPayloadJson: JsValue, UTR: String, taxYear: Int)(implicit
    hc: HeaderCarrier
  ): JsValue = {
    val taxRate        = new TaxRateService(taxYear, applicationConfig.ratePercentages)
    val middleTierData = ATSCalculations.make(rawPayloadJson.as[TaxSummaryLiability], taxRate) match {
      case Some(calculations) =>
        aTSRawDataTransformer.atsDataDTO(taxRate, calculations, rawTaxpayerJson, UTR, taxYear)
      case _                  => noAtsResult(taxYear)
    }
    Json.toJson(middleTierData)
  }

  def getATSCalculations(taxYear: Int, rawPayloadJson: JsValue): Option[ATSCalculations] = {
    val taxRate = new TaxRateService(taxYear, applicationConfig.ratePercentages)
    ATSCalculations.make(rawPayloadJson.as[TaxSummaryLiability], taxRate)
  }

  def hasAtsForPreviousPeriod(rawJson: JsValue): Boolean = {
    val annualTaxSummaries: List[JsValue] = (rawJson \ "annualTaxSummaries").as[List[JsValue]]
    annualTaxSummaries.flatMap(item => (item \ "taxYearEnd").asOpt[JsNumber]).nonEmpty
  }

  def createTaxYearJson(rawJson: JsValue, utr: String, rawTaxpayerJson: JsValue): JsValue = {
    val annualTaxSummariesList: List[JsValue] = (rawJson \ "annualTaxSummaries").as[List[JsValue]]
    val atsYearList                           = annualTaxSummariesList.map(item => (item \ "taxYearEnd").as[JsNumber])
    val taxPayer                              = ATSTaxpayerDataTransformer(rawTaxpayerJson).atsTaxpayerDataDTO
    Json.toJson(AtsYearList(utr, Some(taxPayer), Some(atsYearList)))
  }
}
