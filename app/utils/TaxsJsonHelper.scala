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
import models.{AtsMiddleTierDataWithCalculus, AtsYearList, DataHolder, DataHolderWithCalculus, OdsValues, TaxSummaryLiability}
import play.api.libs.json.{JsNumber, JsValue, Json}
import services.TaxRateService
import transformers.{ATSCalculations, ATSRawDataTransformer, ATSTaxpayerDataTransformer}
import uk.gov.hmrc.http.HeaderCarrier

class TaxsJsonHelper @Inject() (applicationConfig: ApplicationConfig, aTSRawDataTransformer: ATSRawDataTransformer) {
  def getAllATSData(
    rawTaxpayerJson: JsValue,
    rawPayloadJson: JsValue,
    UTR: String,
    taxYear: Int,
    withCalculus: Boolean = false
  )(implicit
    hc: HeaderCarrier
  ): JsValue = {
    val middleTierData = aTSRawDataTransformer.atsDataDTO(rawPayloadJson, rawTaxpayerJson, UTR, taxYear)
    if (withCalculus) {
      val incomeTaxDataPoint: Option[List[DataHolderWithCalculus]]    = if (middleTierData.income_tax.nonEmpty) {
        Some(createDataPointList(middleTierData.income_tax.get))
      } else {
        None
      }
      val incomeDataPoint: Option[List[DataHolderWithCalculus]]       = if (middleTierData.income_data.nonEmpty) {
        Some(createDataPointList(middleTierData.income_data.get))
      } else {
        None
      }
      val summaryDataPoint: Option[List[DataHolderWithCalculus]]      = if (middleTierData.summary_data.nonEmpty) {
        Some(createDataPointList(middleTierData.summary_data.get))
      } else {
        None
      }
      val allowanceDataPoint: Option[List[DataHolderWithCalculus]]    = if (middleTierData.allowance_data.nonEmpty) {
        Some(createDataPointList(middleTierData.allowance_data.get))
      } else {
        None
      }
      val capitalGainsDataPoint: Option[List[DataHolderWithCalculus]] =
        if (middleTierData.capital_gains_data.nonEmpty) {
          Some(createDataPointList(middleTierData.capital_gains_data.get))
        } else {
          None
        }
      val odsValues                                                   =
        OdsValues(incomeTaxDataPoint, summaryDataPoint, incomeDataPoint, allowanceDataPoint, capitalGainsDataPoint)
      val dataWithCalculus                                            = new AtsMiddleTierDataWithCalculus(middleTierData.taxYear, middleTierData.utr, odsValues)
      Json.toJson(dataWithCalculus)
    } else {
      Json.toJson(middleTierData)
    }
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

  private def createDataPointList(dataPoint: DataHolder): List[DataHolderWithCalculus] = {
    val dataHolderWithCalculusList =
      dataPoint.payload match {
        case Some(dataInDataHolder) =>
          dataInDataHolder.map(liabilityAmountMap =>
            new DataHolderWithCalculus(
              Some(liabilityAmountMap._1.apiValue),
              Some(liabilityAmountMap._2.amount),
              liabilityAmountMap._2.calculus
            )
          )

        case _ => List.empty
      }
    dataHolderWithCalculusList.toList
  }
}
