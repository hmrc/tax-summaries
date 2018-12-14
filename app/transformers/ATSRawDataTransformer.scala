/*
 * Copyright 2018 HM Revenue & Customs
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

package transformers

import java.text.NumberFormat
import java.util.Locale

import errors.AtsError
import models._
import play.api.Logger
import play.api.libs.json._
import services.TaxRateService

case class ATSParsingException(s: String) extends Exception(s)

case class ATSRawDataTransformer(rawJsonFromStub: JsValue, rawTaxPayerJson: JsValue, UTR: String, taxYear: Int) {
  
  val formatter = NumberFormat.getNumberInstance(Locale.UK)

  private def noAtsResult: AtsMiddleTierData = AtsMiddleTierData(taxYear, None, None, None, None, None, None, None, None, Option(AtsError("NoAtsError")))
  
  def atsDataDTO = createATSDataDTO

  private def createATSDataDTO = {
    try {
      hasIncomeAndCapitalGainsLiability match {
        case true => AtsMiddleTierData(taxYear, Some(UTR), createIncomeTaxData, createSummaryData, createIncomeData, createAllowanceData, createCapitalGainsData, createGovSpendData, createTaxPayerData, None)
        case false => noAtsResult
      }
    }
    catch {
      case ATSParsingException(message) => AtsMiddleTierData(taxYear, None, None, None, None, None, None, None, None, Option(AtsError(message)))
      case otherError: Throwable =>
        Logger.error("Unexpected error has occurred", otherError)
        AtsMiddleTierData(taxYear, None, None, None, None, None, None, None, None, Option(AtsError("Other Error")))
    }
  }

  private def hasIncomeAndCapitalGainsLiability = !(createTotalCapitalGainsTax + createTotalIncomeTaxAmount).isZeroOrLess

  private def createGovSpendData = {
    val transform = new GovSpendingDataTransformer(createYourTotalTax, taxYear)
    Option(transform.govSpendReferenceDTO)
  }

  private def createSummaryData = Option(DataHolder(createSummaryPageBreakdown, createSummaryPageRates, None))

  private def createIncomeData = Option(DataHolder(createYourIncomeBeforeTaxBreakdown, None, None))

  private def createIncomeTaxData = Option(DataHolder(createTotalIncomeTaxPageBreakdown, createTotalIncomeTaxPageRates, createIncomeTaxStatus))

  private def createAllowanceData = Option(DataHolder(createYourTaxFreeAmountBreakdown, None, None))

  private def createCapitalGainsData = Option(DataHolder(createCapitalGainsTaxBreakdown, createCapitalGainsTaxRates, None))

  private def createTaxPayerData = Option(ATSTaxpayerDataTransformer(rawTaxPayerJson).atsTaxpayerDataDTO)

  private def createCapitalGainsTaxBreakdown =
    Option(Map("taxable_gains" -> createTaxableGains,
      "less_tax_free_amount" -> createLessTaxFreeAmount,
      "pay_cg_tax_on" -> createPayCapitalGainsTaxOn,
      "amount_at_entrepreneurs_rate" -> createCtnCgAtEntrepreneursRate,
      "amount_due_at_entrepreneurs_rate" -> createCtnCgDueEntrepreneursRate,
      "amount_at_ordinary_rate" -> createCtnCgAtLowerRate,
      "amount_due_at_ordinary_rate" -> createCtnCgDueLowerRate,
      "amount_at_higher_rate" -> createCtnCgAtHigherRate,
      "amount_due_at_higher_rate" -> createCtnCgDueHigherRate,
      "adjustments" -> createCapAdjustmentAmt,
      "total_cg_tax" -> createTotalCapitalGainsTax,
      "cg_tax_per_currency_unit" -> createCgTaxPerCurrencyUnit))

  private def createCapitalGainsTaxRates =
    Option(Map("cg_entrepreneurs_rate" -> TaxRateService.cgEntrepreneursRate(taxYear),
          "cg_ordinary_rate" -> TaxRateService.cgOrdinaryRate(taxYear),
          "cg_upper_rate" -> TaxRateService.cgUpperRate(taxYear),
          "total_cg_tax_rate" -> createTotalCgTaxRate))

  private def createYourIncomeBeforeTaxBreakdown =
    Option(Map("self_employment_income" -> createSelfEmployment,
      "income_from_employment" -> createIncomeFromEmployment,
      "state_pension" -> createStatePension,
      "other_pension_income" -> createOtherPension,
      "taxable_state_benefits" -> createTaxableStateBenefits,
      "other_income" -> createOtherIncome,
      "benefits_from_employment" -> createBenefitsFromEmployment,
      "total_income_before_tax" -> createTotalIncomeBeforeTax))

  private def createYourTaxFreeAmountBreakdown =
    Option(Map("personal_tax_free_amount" -> createPersonalTaxFreeAmount,
      "marriage_allowance_transferred_amount" -> createMarriageAllowanceTransferredAmount,
      "other_allowances_amount" -> createOtherAllowancesAmount,
      "total_tax_free_amount" -> createTotalTaxFreeAmount))

  private def createSummaryPageBreakdown =
    Option(Map("employee_nic_amount" -> createTotalAmountEmployeeNic,
      "total_income_tax_and_nics" -> createTotalAmountTaxAndNics,
      "your_total_tax" -> createYourTotalTax,
      "personal_tax_free_amount" -> createPersonalTaxFreeAmount,
      "total_tax_free_amount" -> createTotalTaxFreeAmount,
      "total_income_before_tax" -> createTotalIncomeBeforeTax,
      "total_income_tax" -> createTotalIncomeTaxAmount,
      "total_cg_tax" -> createTotalCapitalGainsTax,
      "taxable_gains" -> createTaxableGains,
      "cg_tax_per_currency_unit" -> createCgTaxPerCurrencyUnit,
      "nics_and_tax_per_currency_unit" -> createNicsAndTaxPerCurrencyUnit))

  private def createSummaryPageRates =
    Option(Map("total_cg_tax_rate" -> createTotalCgTaxRate,
      "nics_and_tax_rate" -> createNicsAndTaxTaxRate))
  
  private def createTotalIncomeTaxPageBreakdown =
    Option(Map("starting_rate_for_savings" -> createStartingRateForSavings,
      "starting_rate_for_savings_amount" -> createStartingRateForSavingsAmount,
      "basic_rate_income_tax" -> createBasicRateIncomeTax,
      "basic_rate_income_tax_amount" -> basicRateIncomeTaxAmount,
      "higher_rate_income_tax" -> createHigherRateIncomeTax,
      "higher_rate_income_tax_amount" -> createHigherRateIncomeTaxAmount,
      "additional_rate_income_tax" -> createAdditionalRateIncomeTax,
      "additional_rate_income_tax_amount" -> createAdditionalRateIncomeTaxAmount,
      "ordinary_rate" -> createOrdinaryRateDividends,
      "ordinary_rate_amount" -> createOrdinaryRateDividendsAmount,
      "upper_rate" -> createUpperRateDividends,
      "upper_rate_amount" -> createUpperRateDividendsAmount,
      "additional_rate" -> createAdditionalRateDividends,
      "additional_rate_amount" -> createAdditionalRateDividendsAmount,
      "other_adjustments_increasing" -> createOtherAdjustmentsIncreasing,
      "marriage_allowance_received_amount" -> createMarriageAllowanceReceivedAmount,
      "other_adjustments_reducing" -> createOtherAdjustmentsReducing,
      "total_income_tax" -> createTotalIncomeTaxAmount,
      "scottish_income_tax" -> createScottishIncomeTax))

  private def createStartingRateForSavings = getTliSlpAmountVal("ctnSavingsChgbleStartRate")

  private def createStartingRateForSavingsAmount = getTliSlpAmountVal("ctnSavingsTaxStartingRate")

  private def pensionLumpSumRate = getTliSlpBigDecimalVal("ctnPensionLumpSumTaxRate")
  private def hasPensionLumpSumAtBasicRate = pensionLumpSumRate.equals(BigDecimal(0.20))
  private def hasPensionLumpSumAtHigherRate = pensionLumpSumRate.equals(BigDecimal(0.40))
  private def hasPensionLumpSumAtAdditionalRate = pensionLumpSumRate.equals(BigDecimal(0.45))

  private def createBasicRateIncomeTax = getAmountSum("ctnIncomeChgbleBasicRate", "ctnSavingsChgbleLowerRate")

  private def basicRateIncomeTaxAmount = {
    if (hasPensionLumpSumAtBasicRate)
      getAmountSum("ctnIncomeTaxBasicRate", "ctnSavingsTaxLowerRate", "ctnPensionLsumTaxDueAmt")
    else
      getAmountSum("ctnIncomeTaxBasicRate", "ctnSavingsTaxLowerRate")
  }

  private def createHigherRateIncomeTax = getAmountSum("ctnIncomeChgbleHigherRate", "ctnSavingsChgbleHigherRate")

  private def createHigherRateIncomeTaxAmount = {
    if (hasPensionLumpSumAtHigherRate)
      getAmountSum("ctnIncomeTaxHigherRate", "ctnSavingsTaxHigherRate", "ctnPensionLsumTaxDueAmt")
    else
      getAmountSum("ctnIncomeTaxHigherRate", "ctnSavingsTaxHigherRate")
  }

  private def createAdditionalRateIncomeTax = getAmountSum("ctnIncomeChgbleAddHRate", "ctnSavingsChgbleAddHRate")

  private def createAdditionalRateIncomeTaxAmount = {
    if (hasPensionLumpSumAtAdditionalRate)
      getAmountSum("ctnIncomeTaxAddHighRate", "ctnSavingsTaxAddHighRate", "ctnPensionLsumTaxDueAmt")
    else
      getAmountSum("ctnIncomeTaxAddHighRate", "ctnSavingsTaxAddHighRate")
  }

  private def createOrdinaryRateDividends = getTliSlpAmountVal("ctnDividendChgbleLowRate")

  private def createOrdinaryRateDividendsAmount = getTliSlpAmountVal("ctnDividendTaxLowRate")

  private def createUpperRateDividends = getTliSlpAmountVal("ctnDividendChgbleHighRate")

  private def createUpperRateDividendsAmount = getTliSlpAmountVal("ctnDividendTaxHighRate")

  private def createAdditionalRateDividends = getTliSlpAmountVal("ctnDividendChgbleAddHRate")

  private def createAdditionalRateDividendsAmount = getTliSlpAmountVal("ctnDividendTaxAddHighRate")

  private def createOtherAdjustmentsIncreasing = getAmountSum(
    "nonDomChargeAmount",
    "taxExcluded",
    "incomeTaxDue",
    "netAnnuityPaytsTaxDue",
    "ctnChildBenefitChrgAmt",
    "ctnPensionSavingChrgbleAmt") - getTliSlpAmountVal("ctn4TaxDueAfterAllceRlf")

  private def createOtherAdjustmentsReducing = (getAmountSum(
    "ctnDeficiencyRelief",
    "topSlicingRelief",
    "ctnVctSharesReliefAmt",
    "ctnEisReliefAmt",
    "ctnSeedEisReliefAmt",
    "ctnCommInvTrustRelAmt",
    "atsSurplusMcaAlimonyRel",
    "ctnNotionalTaxCegs",
    "ctnNotlTaxOthrSrceAmo",
    "ctnTaxCredForDivs",
    "ctnQualDistnReliefAmt",
    "figTotalTaxCreditRelief",
    "ctnNonPayableTaxCredits") + createReliefForFinanceCosts).roundAmountUp

  private def createReliefForFinanceCosts =  getTliSlpAmountOptVal("reliefForFinanceCosts")

  private def createTotalIncomeTaxAmount = createStartingRateForSavingsAmount +
    basicRateIncomeTaxAmount +
    createHigherRateIncomeTaxAmount +
    createAdditionalRateIncomeTaxAmount +
    createOrdinaryRateDividendsAmount +
    createUpperRateDividendsAmount +
    createAdditionalRateDividendsAmount +
    createOtherAdjustmentsIncreasing -
    createOtherAdjustmentsReducing -
    createMarriageAllowanceReceivedAmount

  private def createTotalIncomeTaxPageRates =
    Option(Map(
      "starting_rate_for_savings_rate" -> TaxRateService.startingRateForSavingsRate(taxYear),
      "basic_rate_income_tax_rate" -> TaxRateService.basicRateIncomeTaxRate(taxYear),
      "higher_rate_income_tax_rate" -> TaxRateService.higherRateIncomeTaxRate(taxYear),
      "additional_rate_income_tax_rate" -> TaxRateService.additionalRateIncomeTaxRate(taxYear),
      "ordinary_rate_tax_rate" -> TaxRateService.dividendsOrdinaryRate(taxYear),
      "upper_rate_rate" -> TaxRateService.dividendUpperRateRate(taxYear),
      "additional_rate_rate" -> TaxRateService.dividendAdditionalRate(taxYear)))

  private def createSelfEmployment = getAmountSum(
    "ctnSummaryTotalScheduleD",
    "ctnSummaryTotalPartnership")

  private def createIncomeFromEmployment = getTliSlpAmountVal("ctnSummaryTotalEmployment")

  private def createStatePension = getTliSlpAmountVal("atsStatePensionAmt")

  private def createOtherPension = getAmountSum(
    "atsOtherPensionAmt",
    "itfStatePensionLsGrossAmt")

  private def createTaxableStateBenefits = getAmountSum("atsIncBenefitSuppAllowAmt",
    "atsJobSeekersAllowanceAmt",
    "atsOthStatePenBenefitsAmt")

  private def createOtherIncome = getAmountSum(
    "ctnSummaryTotShareOptions",
    "ctnSummaryTotalUklProperty",
    "ctnSummaryTotForeignIncome",
    "ctnSummaryTotTrustEstates",
    "ctnSummaryTotalOtherIncome",
    "ctnSummaryTotalUkInterest",
    "ctnSummaryTotForeignDiv",
    "ctnSummaryTotalUkIntDivs",
    "ctn4SumTotLifePolicyGains")

  private def createBenefitsFromEmployment = getTliSlpAmountVal("ctnEmploymentBenefitsAmt")

  private def createTotalIncomeBeforeTax =
    createSelfEmployment +
    createIncomeFromEmployment +
    createStatePension +
    createOtherPension +
    createTaxableStateBenefits +
    createOtherIncome +
    createBenefitsFromEmployment

   private def createTaxableGains = getAmountSum(
    "atsCgTotGainsAfterLosses",
    "atsCgGainsAfterLossesAmt")
  
  private def createCtnCgAtEntrepreneursRate = getTliSlpAmountVal("ctnCgAtEntrepreneursRate")
  
  private def createCtnCgDueEntrepreneursRate = getTliSlpAmountVal("ctnCgDueEntrepreneursRate")
  
  private def createCtnCgAtLowerRate = getTliSlpAmountVal("ctnCgAtLowerRate")
  
  private def createCtnCgDueLowerRate = getTliSlpAmountVal("ctnCgDueLowerRate")
  
  private def createCtnCgAtHigherRate = getTliSlpAmountVal("ctnCgAtHigherRate")
  
  private def createCtnCgDueHigherRate = getTliSlpAmountVal("ctnCgDueHigherRate")

  private def createLessTaxFreeAmount = getTliSlpAmountVal("atsCgAnnualExemptAmt")
  
  private def createCapAdjustmentAmt = getTliSlpAmountVal("capAdjustmentAmt")
  
  private def createTotalCapitalGainsTax = createCtnCgDueEntrepreneursRate + createCtnCgDueLowerRate + createCtnCgDueHigherRate - createCapAdjustmentAmt
  
  private def createPayCapitalGainsTaxOn = if (createTaxableGains < createLessTaxFreeAmount) Amount(0.00,"GBP") else createTaxableGains - createLessTaxFreeAmount

  private def createPersonalTaxFreeAmount = getTliSlpAmountVal("ctnPersonalAllowance")

  private def createMarriageAllowanceTransferredAmount = getTliSlpAmountOptVal("ctnMarriageAllceOutAmt")

  private def createMarriageAllowanceReceivedAmount = getTliSlpAmountOptVal("ctnMarriageAllceInAmt")

  private def createIncomeTaxStatus = Option(getTliSlpString("incomeTaxStatus"))

  private def createCtnIncomeChgbleBasicRate = getTliSlpAmountOptVal("ctnIncomeChgbleBasicRate")

  private def createCtnIncomeChgbleHigherRate = getTliSlpAmountOptVal("ctnIncomeChgbleHigherRate")

  private def createCtnIncomeChgbleAddHRate = getTliSlpAmountOptVal("ctnIncomeChgbleAddHRate")

  private def createScottishIncomeTax = Amount((createCtnIncomeChgbleBasicRate + createCtnIncomeChgbleHigherRate + createCtnIncomeChgbleAddHRate).amount * 0.1,"GBP")


  private def createOtherAllowancesAmount = getAmountSum(
    "ctnEmploymentExpensesAmt",
    "ctnSummaryTotalDedPpr",
    "ctnSumTotForeignTaxRelief",
    "ctnSumTotLoanRestricted",
    "ctnSumTotLossRestricted",
    "grossAnnuityPayts",
    "itf4GiftsInvCharitiesAmo",
    "itfTradeUnionDeathBenefits",
    "ctnBpaAllowanceAmt",
    "itfBpaAmount",
    "grossExcludedIncome").roundAmountUp

  private def createTotalTaxFreeAmount =
    createOtherAllowancesAmount +
      createPersonalTaxFreeAmount -
      createMarriageAllowanceTransferredAmount

  private def createTotalAmountEmployeeNic =
    getSaPayeAmountVal("employeeClass1Nic") +
      getSaPayeAmountVal("employeeClass2Nic") +
      getTliSlpAmountVal("class4Nic")

  private def createTotalAmountTaxAndNics = createTotalAmountEmployeeNic + createTotalIncomeTaxAmount

  private def createYourTotalTax = createTotalAmountTaxAndNics + createTotalCapitalGainsTax
  
  private def createCgTaxPerCurrencyUnit = taxPerTaxableCurrencyUnit(createTotalCapitalGainsTax, createTaxableGains)
  private def createTotalCgTaxRate = rateFromPerUnitAmount(createCgTaxPerCurrencyUnit)

  private def createNicsAndTaxPerCurrencyUnit = taxPerTaxableCurrencyUnit(createTotalAmountTaxAndNics, createTotalIncomeBeforeTax)
  private def createNicsAndTaxTaxRate = rateFromPerUnitAmount(createNicsAndTaxPerCurrencyUnit)

  private def taxPerTaxableCurrencyUnit(tax: Amount, taxable: Amount) =
    taxable match {
    	case value if value.isZero => taxable
    	case _ => tax.divideWithPrecision(taxable,4)
  	}

  private def rateFromPerUnitAmount(amountPerUnit:Amount) = {
    Rate(formatter.format((amountPerUnit.amount * 100).setScale(2, BigDecimal.RoundingMode.DOWN)) + "%")
  }

  private def getTliSlpString(key: String):String = {
    val res = jsonValLookupWithErrorHandlingWithOpt[String](key, "tliSlpAtsData")
    res.getOrElse("")
  }

  private def getTliSlpAmountVal(key: String):Amount = {
    jsonValLookupWithErrorHandling[Amount](key, "tliSlpAtsData")
  }

  private def getTliSlpAmountOptVal(key: String):Amount = {
    val res = jsonValLookupWithErrorHandlingWithOpt[Amount](key, "tliSlpAtsData")
    res.getOrElse(Amount(0,"GBP"))
  }

  private def getSaPayeAmountVal(key: String): Amount = {
    jsonValLookupWithErrorHandling[Amount](key, "saPayeNicDetails")
  }

  private def getTliSlpBigDecimalVal(key: String):BigDecimal = {
    jsonValLookupWithErrorHandling[BigDecimal](key, "tliSlpAtsData")
  }

  private def jsonValLookupWithErrorHandlingWithOpt[T: Reads](key: String, topLevelContainer: String):Option[T] = {

    val theOption = (rawJsonFromStub \ topLevelContainer \ key).validate[T]

    theOption match {
      case s: JsSuccess[T] => Some(s.get)
      case e: JsError => None
    }
  }

  private def jsonValLookupWithErrorHandling[T: Reads](key: String, topLevelContainer: String):T = {

    val theOption = (rawJsonFromStub \ topLevelContainer \ key).validate[T]

    theOption match {
      case s: JsSuccess[T] => s.get
      case e: JsError =>
        Logger.error("Errors: " + JsError.toJson(e).toString() + " we were looking for " + key + " in " + topLevelContainer)
        throw new ATSParsingException(key)
    }
  }

  private def getAmountSum(keys: String*) = {
    (keys map (key => getTliSlpAmountVal(key))).reduceLeft[Amount](_ + _)
  }
}
