# Get SA details for UTR and tax year

Returns the paye tax and NI details for a nino and tax year as well as the government spend figures.

Calls to this API must be made by an authenticated and authorised user (at least confidence level 50) with the matching id.

**URL**: `/:UTR/:TAX_YEAR/ats-data`

**Method**: `GET`

**URL Params**:

| Parameter Name | Type   | Description  | Notes                       |
|----------------|--------|--------------|-----------------------------|
| UTR            | String | The UTR      |                             |
| TAX_YEAR       | String | The tax year |                             |

**Required Request Headers**:

| Header Name   | Header Value   | Description                                |
|---------------|----------------|--------------------------------------------|
| Authorization | Bearer {TOKEN} | A valid bearer token from the auth service |

***Example request:***

/1097172563/2025/ats-data

## Responses

### Success response

**Code**: `200 OK` if data found

**Response Body**

The response body returns the SA tax and NI details and the government spend figures.

**Response Body**

The fields included will depend on the tax year and country where the tax has been paid (England, Scotland or Wales). All the sections below though should be present.

| Field Name  | Description                    | Data Type | Mandatory/Optional | Notes                          |
|-------------|--------------------------------|-----------|--------------------|--------------------------------|
| taxYear     | The tax year                   | Int       | Mandatory          |                                |
| nino        | The nino                       | String    | Mandatory          |                                |
| income_tax | The income tax details         | Object    | Mandatory          |  |
| summary_data   | The summary details            | Object    | Mandatory          | |
| income_data   | The income details             | Object    | Mandatory          | |
| allowance_data   | The allowance details          | Object    | Mandatory          | |
| gov_spending   | The government spending details | Object    | Mandatory          | |
| taxPayerData   | Tax payer name information     | Object    | Mandatory          | |
| taxLiability   | The total tax liability        | Object    | Mandatory          | |

The non-rate fields returned as JSON objects in the various sections are in the following form:

| Field Name | Description                                                           | Data Type | Mandatory/Optional | Notes                          |
|------------|-----------------------------------------------------------------------|-----------|--------------------|--------------------------------|
| amount     | The amount                                                            | Number    | Mandatory          |                                |
| currency   | The currency: always GBP                                              | String    | Mandatory          |                                |
| calculus | The value with, in brackets, the formula used to calculate the value. | Object    | Mandatory          |  |

**Response Body Examples**

***An example response. Fields returned will vary depending on the tax year and country.***

```json
{
  "taxYear":2025,
  "utr":"1130492359",
  "income_tax":{
    "payload":{
      "savings_additional_rate_tax":{
        "amount":0,
        "currency":"GBP",
        "calculus":"null (savingsAdditionalRateTax)"
      },
      "scottish_top_rate_tax":{
        "amount":0,
        "currency":"GBP",
        "calculus":"null (scottishTopRateTax)"
      },
      "additional_rate":{
        "amount":5000,
        "currency":"GBP",
        "calculus":"5000(ctnDividendChgbleAddHRate)"
      },
      "other_adjustments_reducing":{
        "amount":30001,
        "currency":"GBP",
        "calculus":"0(ctnDeficiencyRelief) + 0(topSlicingRelief) + 0(ctnVctSharesReliefAmt) + 0(ctnEisReliefAmt) + 0(ctnSeedEisReliefAmt) + 0(ctnCommInvTrustRelAmt) + 0.5(ctnSocialInvTaxRelAmt) + 30000(atsSurplusMcaAlimonyRel) + 0(alimony) + 0(ctnNotionalTaxCegs) + 0(ctnNotlTaxOthrSrceAmo) + 0(ctnFtcrRestricted) + 0(reliefForFinanceCosts) + 0(lfiRelief) + 0(ctnRelTaxAcctFor)"
      },
      "scottish_higher_income":{
        "amount":0,
        "currency":"GBP",
        "calculus":"null (scottishHigherRateIncome)"
      },
      "savings_additional_income":{
        "amount":0,
        "currency":"GBP",
        "calculus":"null (savingsAdditionalRateIncome)"
      },
      "scottish_intermediate_income":{
        "amount":0,
        "currency":"GBP",
        "calculus":"null (scottishIntermediateRateIncome)"
      },
      "other_adjustments_increasing":{
        "amount":500,
        "currency":"GBP",
        "calculus":"0(nonDomChargeAmount) + 0(giftAidTaxReduced) + 500(netAnnuityPaytsTaxDue) + 0(ctnChildBenefitChrgAmt) + 0(ctnPensionSavingChrgbleAmt) + 0(ctnTaxOnTransitionPrft)"
      },
      "brdReduction":{
        "amount":1200.9,
        "currency":"GBP",
        "calculus":"1200.9(brdReduction)"
      },
      "scottish_additional_rate_tax":{
        "amount":0,
        "currency":"GBP",
        "calculus":"null (scottishAdditionalRateTax)"
      },
      "savings_higher_rate_tax":{
        "amount":0,
        "currency":"GBP",
        "calculus":"null (savingsHigherRateTax)"
      },
      "scottish_income_tax":{
        "amount":0,
        "currency":"GBP",
        "calculus":"null (scottishIncomeTax)"
      },
      "savings_lower_rate_tax":{
        "amount":0,
        "currency":"GBP",
        "calculus":"null (savingsBasicRateTax)"
      },
      "basic_rate_income_tax":{
        "amount":39130,
        "currency":"GBP",
        "calculus":"32010(ctnIncomeChgbleBasicRate) + 7120(ctnSavingsChgbleLowerRate) + 0(ctnTaxableRedundancyBr) + 0(ctnTaxableCegBr) + null (itfStatePensionLsGrossAmt)"
      },
      "scottish_basic_rate_tax":{
        "amount":0,
        "currency":"GBP",
        "calculus":"null (scottishBasicRateTax)"
      },
      "upper_rate":{
        "amount":0,
        "currency":"GBP",
        "calculus":"0(ctnDividendChgbleHighRate)"
      },
      "additional_rate_amount":{
        "amount":1875,
        "currency":"GBP",
        "calculus":"1875(ctnDividendTaxAddHighRate)"
      },
      "ordinary_rate_amount":{
        "amount":0,
        "currency":"GBP",
        "calculus":"0(ctnDividendTaxLowRate)"
      },
      "additional_rate_income_tax_amount":{
        "amount":172479,
        "currency":"GBP",
        "calculus":"170229(ctnIncomeTaxAddHighRate) + 0(ctnSavingsTaxAddHighRate) + 0(ctnTaxOnRedundancyAhr) + 0(ctnTaxOnCegAhr) + 2250(ctnPensionLsumTaxDueAmt)"
      },
      "total_income_tax":{
        "amount":200163.5,
        "currency":"GBP",
        "calculus":"288(ctnSavingsTaxStartingRate) + 0(ctnTaxOnCegSr) + 6402(ctnIncomeTaxBasicRate) + 1424(ctnSavingsTaxLowerRate) + 0(ctnTaxOnRedundancyBr) + 0(ctnTaxOnCegBr) + null (ctnPensionLsumTaxDueAmt) + 47196(ctnIncomeTaxHigherRate) + 0(ctnSavingsTaxHigherRate) + 0(ctnTaxOnRedundancyHr) + 0(ctnTaxOnCegHr) + null (ctnPensionLsumTaxDueAmt) + 170229(ctnIncomeTaxAddHighRate) + 0(ctnSavingsTaxAddHighRate) + 0(ctnTaxOnRedundancyAhr) + 0(ctnTaxOnCegAhr) + 2250(ctnPensionLsumTaxDueAmt) + 0(ctnDividendTaxLowRate) + 0(ctnDividendTaxHighRate) + 1875(ctnDividendTaxAddHighRate) + 0(nonDomChargeAmount) + 0(giftAidTaxReduced) + 500(netAnnuityPaytsTaxDue) + 0(ctnChildBenefitChrgAmt) + 0(ctnPensionSavingChrgbleAmt) + 0(ctnTaxOnTransitionPrft) - 0(ctnDeficiencyRelief) + 0(topSlicingRelief) + 0(ctnVctSharesReliefAmt) + 0(ctnEisReliefAmt) + 0(ctnSeedEisReliefAmt) + 0(ctnCommInvTrustRelAmt) + 0.5(ctnSocialInvTaxRelAmt) + 30000(atsSurplusMcaAlimonyRel) + 0(alimony) + 0(ctnNotionalTaxCegs) + 0(ctnNotlTaxOthrSrceAmo) + 0(ctnFtcrRestricted) + 0(reliefForFinanceCosts) + 0(lfiRelief) + 0(ctnRelTaxAcctFor) - 0(ctnMarriageAllceInAmt)"
      },
      "marriage_allowance_received_amount":{
        "amount":0,
        "currency":"GBP",
        "calculus":"0(ctnMarriageAllceInAmt)"
      },
      "savings_higher_income":{
        "amount":0,
        "currency":"GBP",
        "calculus":"null (savingsHigherRateIncome)"
      },
      "scottish_advanced_rate_tax":{
        "amount":0,
        "currency":"GBP",
        "calculus":"null (scottishAdvancedRateTax)"
      },
      "higher_rate_income_tax_amount":{
        "amount":47196,
        "currency":"GBP",
        "calculus":"47196(ctnIncomeTaxHigherRate) + 0(ctnSavingsTaxHigherRate) + 0(ctnTaxOnRedundancyHr) + 0(ctnTaxOnCegHr) + null (ctnPensionLsumTaxDueAmt)"
      },
      "scottish_basic_income":{
        "amount":0,
        "currency":"GBP",
        "calculus":"null (scottishBasicRateIncome)"
      },
      "scottish_starter_rate_tax":{
        "amount":0,
        "currency":"GBP",
        "calculus":"null (scottishStarterRateTax)"
      },
      "scottish_starter_income":{
        "amount":0,
        "currency":"GBP",
        "calculus":"null (scottishStarterRateIncome)"
      },
      "additional_rate_income_tax":{
        "amount":383288,
        "currency":"GBP",
        "calculus":"378288(ctnIncomeChgbleAddHRate) + 0(ctnSavingsChgbleAddHRate) + 0(ctnTaxableRedundancyAhr) + 0(ctnTaxableCegAhr) + 5000(itfStatePensionLsGrossAmt)"
      },
      "brdCharge":{
        "amount":600.5,
        "currency":"GBP",
        "calculus":"600.5(brdCharge)"
      },
      "welsh_income_tax":{
        "amount":0,
        "currency":"GBP",
        "calculus":"null (welshIncomeTax)"
      },
      "ordinary_rate":{
        "amount":0,
        "currency":"GBP",
        "calculus":"0(ctnDividendChgbleLowRate)"
      },
      "starting_rate_for_savings":{
        "amount":10000,
        "currency":"GBP",
        "calculus":"10000(ctnSavingsChgbleStartRate) + 0(ctnTaxableCegSr)"
      },
      "upper_rate_amount":{
        "amount":0,
        "currency":"GBP",
        "calculus":"0(ctnDividendTaxHighRate)"
      },
      "higher_rate_income_tax":{
        "amount":117990,
        "currency":"GBP",
        "calculus":"117990(ctnIncomeChgbleHigherRate) + 0(ctnSavingsChgbleHigherRate) + 0(ctnTaxableRedundancyHr) + 0(ctnTaxableCegHr) + null (itfStatePensionLsGrossAmt)"
      },
      "savings_lower_income":{
        "amount":0,
        "currency":"GBP",
        "calculus":"null (savingsBasicRateIncome)"
      },
      "scottish_additional_income":{
        "amount":0,
        "currency":"GBP",
        "calculus":"null (scottishAdditionalRateIncome)"
      },
      "starting_rate_for_savings_amount":{
        "amount":288,
        "currency":"GBP",
        "calculus":"288(ctnSavingsTaxStartingRate) + 0(ctnTaxOnCegSr)"
      },
      "scottish_higher_rate_tax":{
        "amount":0,
        "currency":"GBP",
        "calculus":"null (scottishHigherRateTax)"
      },
      "scottish_intermediate_rate_tax":{
        "amount":0,
        "currency":"GBP",
        "calculus":"null (scottishIntermediateRateTax)"
      },
      "scottish_top_income":{
        "amount":0,
        "currency":"GBP",
        "calculus":"null (scottishTopRateIncome)"
      },
      "basic_rate_income_tax_amount":{
        "amount":7826,
        "currency":"GBP",
        "calculus":"6402(ctnIncomeTaxBasicRate) + 1424(ctnSavingsTaxLowerRate) + 0(ctnTaxOnRedundancyBr) + 0(ctnTaxOnCegBr) + null (ctnPensionLsumTaxDueAmt)"
      },
      "scottish_advanced_income":{
        "amount":0,
        "currency":"GBP",
        "calculus":"null (scottishAdvancedRateIncome)"
      },
      "scottish_total_tax":{
        "amount":0,
        "currency":"GBP",
        "calculus":"null (scottishStarterRateTax) + null (scottishBasicRateTax) + null (scottishIntermediateRateTax) + null (scottishHigherRateTax) + null (scottishAdvancedRateTax) + null (scottishAdditionalRateTax)"
      }
    },
    "rates":{
      "savings_higher_rate":{
        "percent":"40%"
      },
      "higher_rate_income_tax_rate":{
        "percent":"40%"
      },
      "scottish_intermediate_rate":{
        "percent":"21%"
      },
      "scottish_additional_rate":{
        "percent":"48%"
      },
      "scottish_starter_rate":{
        "percent":"19%"
      },
      "scottish_advanced_rate":{
        "percent":"45%"
      },
      "savings_additional_rate":{
        "percent":"45%"
      },
      "starting_rate_for_savings_rate":{
        "percent":"0%"
      },
      "basic_rate_income_tax_rate":{
        "percent":"20%"
      },
      "scottish_basic_rate":{
        "percent":"20%"
      },
      "additional_rate_income_tax_rate":{
        "percent":"45%"
      },
      "scottish_higher_rate":{
        "percent":"42%"
      },
      "upper_rate_rate":{
        "percent":"33.75%"
      },
      "savings_lower_rate":{
        "percent":"20%"
      },
      "ordinary_rate_tax_rate":{
        "percent":"8.75%"
      },
      "additional_rate_rate":{
        "percent":"39.35%"
      }
    },
    "incomeTaxStatus":"0001"
  },
  "summary_data":{
    "payload":{
      "total_income_tax_and_nics":{
        "amount":208163.5,
        "currency":"GBP",
        "calculus":"8000(employeeClass1Nic) + 0(ctnClass2NicAmt) + 0(class4Nic) + 288(ctnSavingsTaxStartingRate) + 0(ctnTaxOnCegSr) + 6402(ctnIncomeTaxBasicRate) + 1424(ctnSavingsTaxLowerRate) + 0(ctnTaxOnRedundancyBr) + 0(ctnTaxOnCegBr) + null (ctnPensionLsumTaxDueAmt) + 47196(ctnIncomeTaxHigherRate) + 0(ctnSavingsTaxHigherRate) + 0(ctnTaxOnRedundancyHr) + 0(ctnTaxOnCegHr) + null (ctnPensionLsumTaxDueAmt) + 170229(ctnIncomeTaxAddHighRate) + 0(ctnSavingsTaxAddHighRate) + 0(ctnTaxOnRedundancyAhr) + 0(ctnTaxOnCegAhr) + 2250(ctnPensionLsumTaxDueAmt) + 0(ctnDividendTaxLowRate) + 0(ctnDividendTaxHighRate) + 1875(ctnDividendTaxAddHighRate) + 0(nonDomChargeAmount) + 0(giftAidTaxReduced) + 500(netAnnuityPaytsTaxDue) + 0(ctnChildBenefitChrgAmt) + 0(ctnPensionSavingChrgbleAmt) + 0(ctnTaxOnTransitionPrft) - 0(ctnDeficiencyRelief) + 0(topSlicingRelief) + 0(ctnVctSharesReliefAmt) + 0(ctnEisReliefAmt) + 0(ctnSeedEisReliefAmt) + 0(ctnCommInvTrustRelAmt) + 0.5(ctnSocialInvTaxRelAmt) + 30000(atsSurplusMcaAlimonyRel) + 0(alimony) + 0(ctnNotionalTaxCegs) + 0(ctnNotlTaxOthrSrceAmo) + 0(ctnFtcrRestricted) + 0(reliefForFinanceCosts) + 0(lfiRelief) + 0(ctnRelTaxAcctFor) - 0(ctnMarriageAllceInAmt)"
      },
      "cg_tax_per_currency_unit":{
        "amount":0.1388,
        "currency":"GBP",
        "calculus":"max(0, Some(0(ctnLowerRateCgtRPCI) + 0(ctnHigherRateCgtRPCI) + 444(lowerRateCgtCI) + 0(higherRateCgtCI) + 0(lowerRateCgtRP) + 0(higherRateCgtRP) + 500(ctnCgDueEntrepreneursRate) + 0(ctnCgDueLowerRate) + 1232(ctnCgDueHigherRate) + 600(capAdjustmentAmt) + 0(capOffshoreTrustLiability)))"
      },
      "personal_tax_free_amount":{
        "amount":0,
        "currency":"GBP",
        "calculus":"0(ctnPersonalAllowance)"
      },
      "your_total_tax":{
        "amount":210939.5,
        "currency":"GBP",
        "calculus":"8000(employeeClass1Nic) + 0(ctnClass2NicAmt) + 0(class4Nic) + 288(ctnSavingsTaxStartingRate) + 0(ctnTaxOnCegSr) + 6402(ctnIncomeTaxBasicRate) + 1424(ctnSavingsTaxLowerRate) + 0(ctnTaxOnRedundancyBr) + 0(ctnTaxOnCegBr) + null (ctnPensionLsumTaxDueAmt) + 47196(ctnIncomeTaxHigherRate) + 0(ctnSavingsTaxHigherRate) + 0(ctnTaxOnRedundancyHr) + 0(ctnTaxOnCegHr) + null (ctnPensionLsumTaxDueAmt) + 170229(ctnIncomeTaxAddHighRate) + 0(ctnSavingsTaxAddHighRate) + 0(ctnTaxOnRedundancyAhr) + 0(ctnTaxOnCegAhr) + 2250(ctnPensionLsumTaxDueAmt) + 0(ctnDividendTaxLowRate) + 0(ctnDividendTaxHighRate) + 1875(ctnDividendTaxAddHighRate) + 0(nonDomChargeAmount) + 0(giftAidTaxReduced) + 500(netAnnuityPaytsTaxDue) + 0(ctnChildBenefitChrgAmt) + 0(ctnPensionSavingChrgbleAmt) + 0(ctnTaxOnTransitionPrft) - 0(ctnDeficiencyRelief) + 0(topSlicingRelief) + 0(ctnVctSharesReliefAmt) + 0(ctnEisReliefAmt) + 0(ctnSeedEisReliefAmt) + 0(ctnCommInvTrustRelAmt) + 0.5(ctnSocialInvTaxRelAmt) + 30000(atsSurplusMcaAlimonyRel) + 0(alimony) + 0(ctnNotionalTaxCegs) + 0(ctnNotlTaxOthrSrceAmo) + 0(ctnFtcrRestricted) + 0(reliefForFinanceCosts) + 0(lfiRelief) + 0(ctnRelTaxAcctFor) - 0(ctnMarriageAllceInAmt) + max(0, Some(0(ctnLowerRateCgtRPCI) + 0(ctnHigherRateCgtRPCI) + 444(lowerRateCgtCI) + 0(higherRateCgtCI) + 0(lowerRateCgtRP) + 0(higherRateCgtRP) + 500(ctnCgDueEntrepreneursRate) + 0(ctnCgDueLowerRate) + 1232(ctnCgDueHigherRate) + 600(capAdjustmentAmt) + 0(capOffshoreTrustLiability)))"
      },
      "nics_and_tax_per_currency_unit":{
        "amount":0.3835,
        "currency":"GBP",
        "calculus":"8000(employeeClass1Nic) + 0(ctnClass2NicAmt) + 0(class4Nic) + 288(ctnSavingsTaxStartingRate) + 0(ctnTaxOnCegSr) + 6402(ctnIncomeTaxBasicRate) + 1424(ctnSavingsTaxLowerRate) + 0(ctnTaxOnRedundancyBr) + 0(ctnTaxOnCegBr) + null (ctnPensionLsumTaxDueAmt) + 47196(ctnIncomeTaxHigherRate) + 0(ctnSavingsTaxHigherRate) + 0(ctnTaxOnRedundancyHr) + 0(ctnTaxOnCegHr) + null (ctnPensionLsumTaxDueAmt) + 170229(ctnIncomeTaxAddHighRate) + 0(ctnSavingsTaxAddHighRate) + 0(ctnTaxOnRedundancyAhr) + 0(ctnTaxOnCegAhr) + 2250(ctnPensionLsumTaxDueAmt) + 0(ctnDividendTaxLowRate) + 0(ctnDividendTaxHighRate) + 1875(ctnDividendTaxAddHighRate) + 0(nonDomChargeAmount) + 0(giftAidTaxReduced) + 500(netAnnuityPaytsTaxDue) + 0(ctnChildBenefitChrgAmt) + 0(ctnPensionSavingChrgbleAmt) + 0(ctnTaxOnTransitionPrft) - 0(ctnDeficiencyRelief) + 0(topSlicingRelief) + 0(ctnVctSharesReliefAmt) + 0(ctnEisReliefAmt) + 0(ctnSeedEisReliefAmt) + 0(ctnCommInvTrustRelAmt) + 0.5(ctnSocialInvTaxRelAmt) + 30000(atsSurplusMcaAlimonyRel) + 0(alimony) + 0(ctnNotionalTaxCegs) + 0(ctnNotlTaxOthrSrceAmo) + 0(ctnFtcrRestricted) + 0(reliefForFinanceCosts) + 0(lfiRelief) + 0(ctnRelTaxAcctFor) - 0(ctnMarriageAllceInAmt)"
      },
      "taxable_gains":{
        "amount":20000,
        "currency":"GBP",
        "calculus":"20000(atsCgTotGainsAfterLosses) + 0(atsCgGainsAfterLossesAmt)"
      },
      "total_income_before_tax":{
        "amount":542788,
        "currency":"GBP",
        "calculus":"0(ctnSummaryTotalScheduleD) + 0(ctnSummaryTotalPartnership) + 0(ctnSavingsPartnership) + 0(ctnDividendsPartnership) + 500000(ctnSummaryTotalEmployment) + 0(atsStatePensionAmt) + 0(atsOtherPensionAmt) + 5000(itfStatePensionLsGrossAmt) + 2151(atsIncBenefitSuppAllowAmt) + 0(atsJobSeekersAllowanceAmt) + 5237(atsOthStatePenBenefitsAmt) + 0(ctnSummaryTotShareOptions) + 0(ctnSummaryTotalUklProperty) + 4000(ctnSummaryTotForeignIncome) + 3000(ctnSummaryTotTrustEstates) + 2000(ctnSummaryTotalOtherIncome) + 10000(ctnSummaryTotalUkInterest) + 300(ctnSummaryTotForeignDiv) + 5000(ctnSummaryTotalUkIntDivs) + 6000(ctn4SumTotLifePolicyGains) + 0(ctnSummaryTotForeignSav) + 0(ctnForeignCegDedn) + 0(itfCegReceivedAfterTax) + 100(incomeTermination) + 0(ctnEmploymentBenefitsAmt)"
      },
      "total_tax_free_amount":{
        "amount":10200,
        "currency":"GBP",
        "calculus":"0(ctnEmploymentExpensesAmt) + 5000(ctnSummaryTotalDedPpr) + 40(ctnSumTotForeignTaxRelief) + 0(ctnSumTotLossRestricted) + 0(grossAnnuityPayts) + 3000(itf4GiftsInvCharitiesAmo) + 2160(ctnBpaAllowanceAmt) + 0(itfBpaAmount) + 0(ctnPersonalAllowance) - 0(ctnMarriageAllceOutAmt)"
      },
      "total_cg_tax":{
        "amount":2776,
        "currency":"GBP",
        "calculus":"max(0, Some(0(ctnLowerRateCgtRPCI) + 0(ctnHigherRateCgtRPCI) + 444(lowerRateCgtCI) + 0(higherRateCgtCI) + 0(lowerRateCgtRP) + 0(higherRateCgtRP) + 500(ctnCgDueEntrepreneursRate) + 0(ctnCgDueLowerRate) + 1232(ctnCgDueHigherRate) + 600(capAdjustmentAmt) + 0(capOffshoreTrustLiability)))"
      },
      "total_income_tax":{
        "amount":200163.5,
        "currency":"GBP",
        "calculus":"288(ctnSavingsTaxStartingRate) + 0(ctnTaxOnCegSr) + 6402(ctnIncomeTaxBasicRate) + 1424(ctnSavingsTaxLowerRate) + 0(ctnTaxOnRedundancyBr) + 0(ctnTaxOnCegBr) + null (ctnPensionLsumTaxDueAmt) + 47196(ctnIncomeTaxHigherRate) + 0(ctnSavingsTaxHigherRate) + 0(ctnTaxOnRedundancyHr) + 0(ctnTaxOnCegHr) + null (ctnPensionLsumTaxDueAmt) + 170229(ctnIncomeTaxAddHighRate) + 0(ctnSavingsTaxAddHighRate) + 0(ctnTaxOnRedundancyAhr) + 0(ctnTaxOnCegAhr) + 2250(ctnPensionLsumTaxDueAmt) + 0(ctnDividendTaxLowRate) + 0(ctnDividendTaxHighRate) + 1875(ctnDividendTaxAddHighRate) + 0(nonDomChargeAmount) + 0(giftAidTaxReduced) + 500(netAnnuityPaytsTaxDue) + 0(ctnChildBenefitChrgAmt) + 0(ctnPensionSavingChrgbleAmt) + 0(ctnTaxOnTransitionPrft) - 0(ctnDeficiencyRelief) + 0(topSlicingRelief) + 0(ctnVctSharesReliefAmt) + 0(ctnEisReliefAmt) + 0(ctnSeedEisReliefAmt) + 0(ctnCommInvTrustRelAmt) + 0.5(ctnSocialInvTaxRelAmt) + 30000(atsSurplusMcaAlimonyRel) + 0(alimony) + 0(ctnNotionalTaxCegs) + 0(ctnNotlTaxOthrSrceAmo) + 0(ctnFtcrRestricted) + 0(reliefForFinanceCosts) + 0(lfiRelief) + 0(ctnRelTaxAcctFor) - 0(ctnMarriageAllceInAmt)"
      },
      "employee_nic_amount":{
        "amount":8000,
        "currency":"GBP",
        "calculus":"8000(employeeClass1Nic) + 0(ctnClass2NicAmt) + 0(class4Nic)"
      }
    },
    "rates":{
      "total_cg_tax_rate":{
        "percent":"13.88%"
      },
      "nics_and_tax_rate":{
        "percent":"38.35%"
      }
    }
  },
  "income_data":{
    "payload":{
      "self_employment_income":{
        "amount":0,
        "currency":"GBP",
        "calculus":"0(ctnSummaryTotalScheduleD) + 0(ctnSummaryTotalPartnership) + 0(ctnSavingsPartnership) + 0(ctnDividendsPartnership)"
      },
      "benefits_from_employment":{
        "amount":0,
        "currency":"GBP",
        "calculus":"0(ctnEmploymentBenefitsAmt)"
      },
      "other_pension_income":{
        "amount":5000,
        "currency":"GBP",
        "calculus":"0(atsOtherPensionAmt) + 5000(itfStatePensionLsGrossAmt)"
      },
      "state_pension":{
        "amount":0,
        "currency":"GBP",
        "calculus":"0(atsStatePensionAmt)"
      },
      "taxable_state_benefits":{
        "amount":7388,
        "currency":"GBP",
        "calculus":"2151(atsIncBenefitSuppAllowAmt) + 0(atsJobSeekersAllowanceAmt) + 5237(atsOthStatePenBenefitsAmt)"
      },
      "income_from_employment":{
        "amount":500000,
        "currency":"GBP",
        "calculus":"500000(ctnSummaryTotalEmployment)"
      },
      "other_income":{
        "amount":30400,
        "currency":"GBP",
        "calculus":"0(ctnSummaryTotShareOptions) + 0(ctnSummaryTotalUklProperty) + 4000(ctnSummaryTotForeignIncome) + 3000(ctnSummaryTotTrustEstates) + 2000(ctnSummaryTotalOtherIncome) + 10000(ctnSummaryTotalUkInterest) + 300(ctnSummaryTotForeignDiv) + 5000(ctnSummaryTotalUkIntDivs) + 6000(ctn4SumTotLifePolicyGains) + 0(ctnSummaryTotForeignSav) + 0(ctnForeignCegDedn) + 0(itfCegReceivedAfterTax) + 100(incomeTermination)"
      },
      "total_income_before_tax":{
        "amount":542788,
        "currency":"GBP",
        "calculus":"0(ctnSummaryTotalScheduleD) + 0(ctnSummaryTotalPartnership) + 0(ctnSavingsPartnership) + 0(ctnDividendsPartnership) + 500000(ctnSummaryTotalEmployment) + 0(atsStatePensionAmt) + 0(atsOtherPensionAmt) + 5000(itfStatePensionLsGrossAmt) + 2151(atsIncBenefitSuppAllowAmt) + 0(atsJobSeekersAllowanceAmt) + 5237(atsOthStatePenBenefitsAmt) + 0(ctnSummaryTotShareOptions) + 0(ctnSummaryTotalUklProperty) + 4000(ctnSummaryTotForeignIncome) + 3000(ctnSummaryTotTrustEstates) + 2000(ctnSummaryTotalOtherIncome) + 10000(ctnSummaryTotalUkInterest) + 300(ctnSummaryTotForeignDiv) + 5000(ctnSummaryTotalUkIntDivs) + 6000(ctn4SumTotLifePolicyGains) + 0(ctnSummaryTotForeignSav) + 0(ctnForeignCegDedn) + 0(itfCegReceivedAfterTax) + 100(incomeTermination) + 0(ctnEmploymentBenefitsAmt)"
      }
    }
  },
  "allowance_data":{
    "payload":{
      "personal_tax_free_amount":{
        "amount":0,
        "currency":"GBP",
        "calculus":"0(ctnPersonalAllowance)"
      },
      "marriage_allowance_transferred_amount":{
        "amount":0,
        "currency":"GBP",
        "calculus":"0(ctnMarriageAllceOutAmt)"
      },
      "other_allowances_amount":{
        "amount":10200,
        "currency":"GBP",
        "calculus":"0(ctnEmploymentExpensesAmt) + 5000(ctnSummaryTotalDedPpr) + 40(ctnSumTotForeignTaxRelief) + 0(ctnSumTotLossRestricted) + 0(grossAnnuityPayts) + 3000(itf4GiftsInvCharitiesAmo) + 2160(ctnBpaAllowanceAmt) + 0(itfBpaAmount)"
      },
      "total_tax_free_amount":{
        "amount":10200,
        "currency":"GBP",
        "calculus":"0(ctnEmploymentExpensesAmt) + 5000(ctnSummaryTotalDedPpr) + 40(ctnSumTotForeignTaxRelief) + 0(ctnSumTotLossRestricted) + 0(grossAnnuityPayts) + 3000(itf4GiftsInvCharitiesAmo) + 2160(ctnBpaAllowanceAmt) + 0(itfBpaAmount) + 0(ctnPersonalAllowance) - 0(ctnMarriageAllceOutAmt)"
      }
    }
  },
  "capital_gains_data":{
    "payload":{
      "amount_at_rpci_higher_rate":{
        "amount":0,
        "currency":"GBP",
        "calculus":"0(ctnCGAtHigherRateRPCI)"
      },
      "amount_at_higher_rate":{
        "amount":4400,
        "currency":"GBP",
        "calculus":"4400(ctnCgAtHigherRate)"
      },
      "amount_at_rpci_lower_rate":{
        "amount":0,
        "currency":"GBP",
        "calculus":"0(ctnCGAtLowerRateRPCI)"
      },
      "amount_due_at_ordinary_rate":{
        "amount":0,
        "currency":"GBP",
        "calculus":"0(ctnCgDueLowerRate)"
      },
      "amount_due_rpci_lower_rate":{
        "amount":0,
        "currency":"GBP",
        "calculus":"0(ctnLowerRateCgtRPCI)"
      },
      "amount_due_ci_higher_rate":{
        "amount":0,
        "currency":"GBP",
        "calculus":"0(higherRateCgtCI)"
      },
      "amount_at_ordinary_rate":{
        "amount":0,
        "currency":"GBP",
        "calculus":"0(ctnCgAtLowerRate)"
      },
      "total_cg_tax":{
        "amount":2776,
        "currency":"GBP",
        "calculus":"max(0, Some(0(ctnLowerRateCgtRPCI) + 0(ctnHigherRateCgtRPCI) + 444(lowerRateCgtCI) + 0(higherRateCgtCI) + 0(lowerRateCgtRP) + 0(higherRateCgtRP) + 500(ctnCgDueEntrepreneursRate) + 0(ctnCgDueLowerRate) + 1232(ctnCgDueHigherRate) + 600(capAdjustmentAmt) + 0(capOffshoreTrustLiability)))"
      },
      "amount_due_at_entrepreneurs_rate":{
        "amount":500,
        "currency":"GBP",
        "calculus":"500(ctnCgDueEntrepreneursRate)"
      },
      "amount_at_rp_lower_rate":{
        "amount":0,
        "currency":"GBP",
        "calculus":"0(cGAtLowerRateRP)"
      },
      "amount_at_entrepreneurs_rate":{
        "amount":5000,
        "currency":"GBP",
        "calculus":"5000(ctnCgAtEntrepreneursRate)"
      },
      "amount_due_rpci_higher_rate":{
        "amount":0,
        "currency":"GBP",
        "calculus":"0(ctnHigherRateCgtRPCI)"
      },
      "amount_due_rp_lower_rate":{
        "amount":0,
        "currency":"GBP",
        "calculus":"0(lowerRateCgtRP)"
      },
      "cg_tax_per_currency_unit":{
        "amount":0.1388,
        "currency":"GBP",
        "calculus":"max(0, Some(0(ctnLowerRateCgtRPCI) + 0(ctnHigherRateCgtRPCI) + 444(lowerRateCgtCI) + 0(higherRateCgtCI) + 0(lowerRateCgtRP) + 0(higherRateCgtRP) + 500(ctnCgDueEntrepreneursRate) + 0(ctnCgDueLowerRate) + 1232(ctnCgDueHigherRate) + 600(capAdjustmentAmt) + 0(capOffshoreTrustLiability)))"
      },
      "amount_due_at_higher_rate":{
        "amount":1232,
        "currency":"GBP",
        "calculus":"1232(ctnCgDueHigherRate)"
      },
      "amount_at_rp_higher_rate":{
        "amount":0,
        "currency":"GBP",
        "calculus":"0(cGAtHigherRateRP)"
      },
      "less_tax_free_amount":{
        "amount":10600,
        "currency":"GBP",
        "calculus":"10600(atsCgAnnualExemptAmt)"
      },
      "amount_due_rp_higher_rate":{
        "amount":0,
        "currency":"GBP",
        "calculus":"0(higherRateCgtRP)"
      },
      "pay_cg_tax_on":{
        "amount":9400,
        "currency":"GBP",
        "calculus":"20000(atsCgTotGainsAfterLosses) + 0(atsCgGainsAfterLossesAmt) - 10600(atsCgAnnualExemptAmt)"
      },
      "amount_at_ci_higher_rate":{
        "amount":3344,
        "currency":"GBP",
        "calculus":"3344(cGAtHigherRateCI)"
      },
      "amount_due_ci_lower_rate":{
        "amount":444,
        "currency":"GBP",
        "calculus":"444(lowerRateCgtCI)"
      },
      "amount_at_ci_lower_rate":{
        "amount":0,
        "currency":"GBP",
        "calculus":"0(cGAtLowerRateCI)"
      },
      "taxable_gains":{
        "amount":20000,
        "currency":"GBP",
        "calculus":"20000(atsCgTotGainsAfterLosses) + 0(atsCgGainsAfterLossesAmt)"
      },
      "adjustments":{
        "amount":600,
        "currency":"GBP",
        "calculus":"600(capAdjustmentAmt) + 0(capOffshoreTrustLiability)"
      }
    },
    "rates":{
      "cg_upper_rate":{
        "percent":"20%"
      },
      "cg_entrepreneurs_rate":{
        "percent":"10%"
      },
      "prop_interest_rate_lower_rate":{
        "percent":"0%"
      },
      "ci_interest_rate_lower_rate":{
        "percent":"18%"
      },
      "rp_interest_rate_lower_rate":{
        "percent":"18%"
      },
      "rp_interest_rate_higher_rate":{
        "percent":"24%"
      },
      "ci_interest_rate_higher_rate":{
        "percent":"28%"
      },
      "cg_ordinary_rate":{
        "percent":"18%"
      },
      "prop_interest_rate_higher_rate":{
        "percent":"0%"
      },
      "total_cg_tax_rate":{
        "percent":"13.88%"
      }
    }
  },
  "gov_spending":{
    "taxYear":2025,
    "govSpendAmountData":{
      "Welfare":{
        "amount":{
          "amount":44930.11,
          "currency":"GBP",
          "calculus":"Welfare"
        },
        "percentage":21.3
      },
      "Health":{
        "amount":{
          "amount":44086.36,
          "currency":"GBP",
          "calculus":"Health"
        },
        "percentage":20.9
      },
      "StatePensions":{
        "amount":{
          "amount":25101.8,
          "currency":"GBP",
          "calculus":"StatePensions"
        },
        "percentage":11.9
      },
      "NationalDebtInterest":{
        "amount":{
          "amount":22781.47,
          "currency":"GBP",
          "calculus":"NationalDebtInterest"
        },
        "percentage":10.8
      },
      "Education":{
        "amount":{
          "amount":21726.77,
          "currency":"GBP",
          "calculus":"Education"
        },
        "percentage":10.3
      },
      "Defence":{
        "amount":{
          "amount":11601.67,
          "currency":"GBP",
          "calculus":"Defence"
        },
        "percentage":5.5
      },
      "PublicOrderAndSafety":{
        "amount":{
          "amount":9281.34,
          "currency":"GBP",
          "calculus":"PublicOrderAndSafety"
        },
        "percentage":4.4
      },
      "Transport":{
        "amount":{
          "amount":8437.58,
          "currency":"GBP",
          "calculus":"Transport"
        },
        "percentage":4
      },
      "BusinessAndIndustry":{
        "amount":{
          "amount":7382.88,
          "currency":"GBP",
          "calculus":"BusinessAndIndustry"
        },
        "percentage":3.5
      },
      "GovernmentAdministration":{
        "amount":{
          "amount":4218.79,
          "currency":"GBP",
          "calculus":"GovernmentAdministration"
        },
        "percentage":2
      },
      "HousingAndUtilities":{
        "amount":{
          "amount":4007.85,
          "currency":"GBP",
          "calculus":"HousingAndUtilities"
        },
        "percentage":1.9
      },
      "Environment":{
        "amount":{
          "amount":3164.09,
          "currency":"GBP",
          "calculus":"Environment"
        },
        "percentage":1.5
      },
      "Culture":{
        "amount":{
          "amount":2742.21,
          "currency":"GBP",
          "calculus":"Culture"
        },
        "percentage":1.3
      },
      "OverseasAid":{
        "amount":{
          "amount":1476.58,
          "currency":"GBP",
          "calculus":"OverseasAid"
        },
        "percentage":0.7
      },
      "OutstandingPaymentsToTheEU":{
        "amount":{
          "amount":210.94,
          "currency":"GBP",
          "calculus":"OutstandingPaymentsToTheEU"
        },
        "percentage":0.1
      }
    },
    "totalAmount":{
      "amount":210939.5,
      "currency":"GBP",
      "calculus":"8000(employeeClass1Nic) + 0(ctnClass2NicAmt) + 0(class4Nic) + 288(ctnSavingsTaxStartingRate) + 0(ctnTaxOnCegSr) + 6402(ctnIncomeTaxBasicRate) + 1424(ctnSavingsTaxLowerRate) + 0(ctnTaxOnRedundancyBr) + 0(ctnTaxOnCegBr) + null (ctnPensionLsumTaxDueAmt) + 47196(ctnIncomeTaxHigherRate) + 0(ctnSavingsTaxHigherRate) + 0(ctnTaxOnRedundancyHr) + 0(ctnTaxOnCegHr) + null (ctnPensionLsumTaxDueAmt) + 170229(ctnIncomeTaxAddHighRate) + 0(ctnSavingsTaxAddHighRate) + 0(ctnTaxOnRedundancyAhr) + 0(ctnTaxOnCegAhr) + 2250(ctnPensionLsumTaxDueAmt) + 0(ctnDividendTaxLowRate) + 0(ctnDividendTaxHighRate) + 1875(ctnDividendTaxAddHighRate) + 0(nonDomChargeAmount) + 0(giftAidTaxReduced) + 500(netAnnuityPaytsTaxDue) + 0(ctnChildBenefitChrgAmt) + 0(ctnPensionSavingChrgbleAmt) + 0(ctnTaxOnTransitionPrft) - 0(ctnDeficiencyRelief) + 0(topSlicingRelief) + 0(ctnVctSharesReliefAmt) + 0(ctnEisReliefAmt) + 0(ctnSeedEisReliefAmt) + 0(ctnCommInvTrustRelAmt) + 0.5(ctnSocialInvTaxRelAmt) + 30000(atsSurplusMcaAlimonyRel) + 0(alimony) + 0(ctnNotionalTaxCegs) + 0(ctnNotlTaxOthrSrceAmo) + 0(ctnFtcrRestricted) + 0(reliefForFinanceCosts) + 0(lfiRelief) + 0(ctnRelTaxAcctFor) - 0(ctnMarriageAllceInAmt) + max(0, Some(0(ctnLowerRateCgtRPCI) + 0(ctnHigherRateCgtRPCI) + 444(lowerRateCgtCI) + 0(higherRateCgtCI) + 0(lowerRateCgtRP) + 0(higherRateCgtRP) + 500(ctnCgDueEntrepreneursRate) + 0(ctnCgDueLowerRate) + 1232(ctnCgDueHigherRate) + 600(capAdjustmentAmt) + 0(capOffshoreTrustLiability)))"
    }
  },
  "taxPayerData":{
    "title":"Miss",
    "forename":"Jane",
    "surname":"Fisher"
  },
  "taxLiability":{
    "amount":202939.5,
    "currency":"GBP",
    "calculus":"max(0, Some(0(ctnLowerRateCgtRPCI) + 0(ctnHigherRateCgtRPCI) + 444(lowerRateCgtCI) + 0(higherRateCgtCI) + 0(lowerRateCgtRP) + 0(higherRateCgtRP) + 500(ctnCgDueEntrepreneursRate) + 0(ctnCgDueLowerRate) + 1232(ctnCgDueHigherRate) + 600(capAdjustmentAmt) + 0(capOffshoreTrustLiability))) + 288(ctnSavingsTaxStartingRate) + 0(ctnTaxOnCegSr) + 6402(ctnIncomeTaxBasicRate) + 1424(ctnSavingsTaxLowerRate) + 0(ctnTaxOnRedundancyBr) + 0(ctnTaxOnCegBr) + null (ctnPensionLsumTaxDueAmt) + 47196(ctnIncomeTaxHigherRate) + 0(ctnSavingsTaxHigherRate) + 0(ctnTaxOnRedundancyHr) + 0(ctnTaxOnCegHr) + null (ctnPensionLsumTaxDueAmt) + 170229(ctnIncomeTaxAddHighRate) + 0(ctnSavingsTaxAddHighRate) + 0(ctnTaxOnRedundancyAhr) + 0(ctnTaxOnCegAhr) + 2250(ctnPensionLsumTaxDueAmt) + 0(ctnDividendTaxLowRate) + 0(ctnDividendTaxHighRate) + 1875(ctnDividendTaxAddHighRate) + 0(nonDomChargeAmount) + 0(giftAidTaxReduced) + 500(netAnnuityPaytsTaxDue) + 0(ctnChildBenefitChrgAmt) + 0(ctnPensionSavingChrgbleAmt) + 0(ctnTaxOnTransitionPrft) - 0(ctnDeficiencyRelief) + 0(topSlicingRelief) + 0(ctnVctSharesReliefAmt) + 0(ctnEisReliefAmt) + 0(ctnSeedEisReliefAmt) + 0(ctnCommInvTrustRelAmt) + 0.5(ctnSocialInvTaxRelAmt) + 30000(atsSurplusMcaAlimonyRel) + 0(alimony) + 0(ctnNotionalTaxCegs) + 0(ctnNotlTaxOthrSrceAmo) + 0(ctnFtcrRestricted) + 0(reliefForFinanceCosts) + 0(lfiRelief) + 0(ctnRelTaxAcctFor) - 0(ctnMarriageAllceInAmt)"
  }
}
```

### Responses
**Code**: `401 UNAUTHORIZED`
This response can occur when a call is made by any user without an authorized session or id matching that of the request

**Code**: `500 INTERNAL_SERVER_ERROR`
An exception occurred when trying to process the request

**Code**: `502 BAD_GATEWAY`
Timeout occurred when trying to verify the user