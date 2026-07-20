# Get PAYE details for nino and tax year

Returns the paye tax and NI details for a nino and tax year as well as the government spend figures.

Calls to this API must be made by an authenticated and authorised user (at least confidence level 50) with the matching id.

**URL**: `/:NINO/:TAX_YEAR/paye-ats-data`

**Method**: `GET`

**URL Params**:

| Parameter Name | Type   | Description  | Notes                       |
|----------------|--------|--------------|-----------------------------|
| NINO           | String | The NINO     |                             |
| TAX_YEAR       | String | The tax year |                             |

**Required Request Headers**:

| Header Name   | Header Value   | Description                                |
|---------------|----------------|--------------------------------------------|
| Authorization | Bearer {TOKEN} | A valid bearer token from the auth service |

***Example request:***

/OO123456B/2025/paye-ats-data

## Responses

### Success response

**Code**: `200 OK` if data found

**Response Body**

The response body returns the paye tax and NI details and the government spend figures.

**Response Body**

The fields included will depend on the tax year and country where the tax has been paid (England, Scotland or Wales). All the sections below though should be present.

| Field Name  | Description                     | Data Type | Mandatory/Optional | Notes                          |
|-------------|---------------------------------|-----------|--------------------|--------------------------------|
| taxYear     | The tax year                    | Int       | Mandatory          |                                |
| nino        | The nino                        | String    | Mandatory          |                                |
| income_tax | The income tax details          | Object    | Mandatory          |  |
| summary_data   | The summary details             | Object    | Mandatory          | |
| income_data   | The income details              | Object    | Mandatory          | |
| allowance_data   | The allowance details           | Object    | Mandatory          | |
| gov_spending   | The government spending details | Object    | Mandatory          | |
| includeBRDMessage   |  Indicates if the Basic Rate Divergence message should be included in an individuals ATS            | Boolean   | Mandatory          | |

**Response Body Examples**

***An example response. Fields returned will vary depending on the tax year and country.***

```json
{
  "taxYear":2025,
  "nino":"OO123456B",
  "income_tax":{
    "payload":{
      "scottish_higher_rate_amount":{
        "amount":7203.7,
        "currency":"GBP",
        "calculus":"scottish_higher_rate_amount"
      },
      "scottish_advanced_rate_amount":{
        "amount":9000,
        "currency":"GBP",
        "calculus":"scottish_advanced_rate_amount"
      },
      "scottish_higher_rate":{
        "amount":17570,
        "currency":"GBP",
        "calculus":"scottish_higher_rate"
      },
      "scottish_top_rate":{
        "amount":0,
        "currency":"GBP",
        "calculus":"null (scottish_top_rate)"
      },
      "dividend_additional_rate_amount":{
        "amount":0,
        "currency":"GBP",
        "calculus":"null (dividend_additional_rate_amount)"
      },
      "dividend_additional_rate":{
        "amount":0,
        "currency":"GBP",
        "calculus":"null (dividend_additional_rate)"
      },
      "scottish_basic_rate_amount":{
        "amount":2030,
        "currency":"GBP",
        "calculus":"scottish_basic_rate_amount"
      },
      "tax_underpaid_previous_year":{
        "amount":0,
        "currency":"GBP",
        "calculus":"null (tax_underpaid_previous_year)"
      },
      "scottish_starter_rate":{
        "amount":2000,
        "currency":"GBP",
        "calculus":"scottish_starter_rate"
      },
      "higher_rate_income_tax_amount":{
        "amount":1000,
        "currency":"GBP",
        "calculus":"higher_rate_income_tax_amount"
      },
      "additional_rate_income_tax":{
        "amount":0,
        "currency":"GBP",
        "calculus":"null (additional_rate_income_tax)"
      },
      "ordinary_rate":{
        "amount":0,
        "currency":"GBP",
        "calculus":"null (ordinary_rate)"
      },
      "scottish_basic_rate":{
        "amount":10150,
        "currency":"GBP",
        "calculus":"scottish_basic_rate"
      },
      "scottish_starter_rate_amount":{
        "amount":380,
        "currency":"GBP",
        "calculus":"scottish_starter_rate_amount"
      },
      "total_income_tax_2":{
        "amount":0,
        "currency":"GBP",
        "calculus":"null (total_income_tax_2)"
      },
      "married_couples_allowance_adjustment":{
        "amount":0,
        "currency":"GBP",
        "calculus":"null (married_couples_allowance_adjustment)"
      },
      "less_tax_adjustment_previous_year":{
        "amount":9030.83,
        "currency":"GBP",
        "calculus":"less_tax_adjustment_previous_year"
      },
      "scottish_top_rate_amount":{
        "amount":0,
        "currency":"GBP",
        "calculus":"null (scottish_top_rate_amount)"
      },
      "total_UK_income_tax":{
        "amount":1325,
        "currency":"GBP",
        "calculus":"total_UK_income_tax"
      },
      "scottish_intermediate_rate":{
        "amount":19430,
        "currency":"GBP",
        "calculus":"scottish_intermediate_rate"
      },
      "basic_rate_income_tax":{
        "amount":0,
        "currency":"GBP",
        "calculus":"null (basic_rate_income_tax)"
      },
      "upper_rate":{
        "amount":1000,
        "currency":"GBP",
        "calculus":"upper_rate"
      },
      "ordinary_rate_amount":{
        "amount":0,
        "currency":"GBP",
        "calculus":"null (ordinary_rate_amount)"
      },
      "additional_rate_income_tax_amount":{
        "amount":0,
        "currency":"GBP",
        "calculus":"null (additional_rate_income_tax_amount)"
      },
      "total_income_tax":{
        "amount":14700,
        "currency":"GBP",
        "calculus":"total_income_tax"
      },
      "marriage_allowance_received_amount":{
        "amount":0,
        "currency":"GBP",
        "calculus":"null (marriage_allowance_received_amount)"
      },
      "scottish_intermediate_rate_amount":{
        "amount":4080.3,
        "currency":"GBP",
        "calculus":"scottish_intermediate_rate_amount"
      },
      "scottish_advanced_rate":{
        "amount":20000,
        "currency":"GBP",
        "calculus":"scottish_advanced_rate"
      },
      "upper_rate_amount":{
        "amount":325,
        "currency":"GBP",
        "calculus":"upper_rate_amount"
      },
      "higher_rate_income_tax":{
        "amount":2500,
        "currency":"GBP",
        "calculus":"higher_rate_income_tax"
      },
      "basic_rate_income_tax_amount":{
        "amount":0,
        "currency":"GBP",
        "calculus":"null (basic_rate_income_tax_amount)"
      },
      "scottish_total_tax":{
        "amount":22694,
        "currency":"GBP",
        "calculus":"scottish_total_tax"
      }
    },
    "rates":{
      "paye_scottish_basic_rate":{
        "percent":"20%"
      },
      "paye_scottish_starter_rate":{
        "percent":"19%"
      },
      "paye_additional_rate_income_tax":{
        "percent":"0%"
      },
      "paye_ordinary_rate":{
        "percent":"0%"
      },
      "paye_dividend_additional_rate":{
        "percent":"0%"
      },
      "paye_basic_rate_income_tax":{
        "percent":"0%"
      },
      "paye_scottish_higher_rate":{
        "percent":"41%"
      },
      "paye_scottish_advanced_rate":{
        "percent":"45%"
      },
      "paye_higher_rate_income_tax":{
        "percent":"40%"
      },
      "paye_upper_rate":{
        "percent":"32.5%"
      },
      "paye_scottish_intermediate_rate":{
        "percent":"21%"
      },
      "paye_scottish_top_rate":{
        "percent":"0%"
      }
    }
  },
  "summary_data":{
    "payload":{
      "total_income_tax_and_nics":{
        "amount":14787.6,
        "currency":"GBP",
        "calculus":"total_income_tax_and_nics"
      },
      "income_after_tax_and_nics":{
        "amount":52212.4,
        "currency":"GBP",
        "calculus":"income_after_tax_and_nics"
      },
      "liable_tax_amount":{
        "amount":55150,
        "currency":"GBP",
        "calculus":"liable_tax_amount"
      },
      "total_income_tax_2_nics":{
        "amount":6075.77,
        "currency":"GBP",
        "calculus":"total_income_tax_2_nics"
      },
      "employer_nic_amount":{
        "amount":13.14,
        "currency":"GBP",
        "calculus":"employer_nic_amount"
      },
      "total_income_before_tax":{
        "amount":67000,
        "currency":"GBP",
        "calculus":"total_income_before_tax"
      },
      "total_tax_free_amount":{
        "amount":0,
        "currency":"GBP",
        "calculus":"null (total_tax_free_amount)"
      },
      "total_income_tax":{
        "amount":0,
        "currency":"GBP",
        "calculus":"null (total_income_tax)"
      },
      "employee_nic_amount":{
        "amount":87.6,
        "currency":"GBP",
        "calculus":"employee_nic_amount"
      }
    },
    "rates":{
      "nics_and_tax_rate":{
        "percent":"22%"
      }
    }
  },
  "income_data":{
    "payload":{
      "benefits_from_employment":{
        "amount":0,
        "currency":"GBP",
        "calculus":"null (benefits_from_employment)"
      },
      "other_pension_income":{
        "amount":0,
        "currency":"GBP",
        "calculus":"null (other_pension_income)"
      },
      "state_pension":{
        "amount":1000,
        "currency":"GBP",
        "calculus":"state_pension"
      },
      "taxable_state_benefits":{
        "amount":0,
        "currency":"GBP",
        "calculus":"null (taxable_state_benefits)"
      },
      "income_from_employment":{
        "amount":60000,
        "currency":"GBP",
        "calculus":"income_from_employment"
      },
      "other_income":{
        "amount":6000,
        "currency":"GBP",
        "calculus":"other_income"
      },
      "welsh_income_tax":{
        "amount":0,
        "currency":"GBP",
        "calculus":"null (scottish_income_tax)"
      },
      "total_income_before_tax":{
        "amount":67000,
        "currency":"GBP",
        "calculus":"total_income_before_tax"
      }
    }
  },
  "allowance_data":{
    "payload":{
      "other_allowances_amount":{
        "amount":0,
        "currency":"GBP",
        "calculus":"null (other_allowances_amount)"
      },
      "personal_tax_free_amount":{
        "amount":11850,
        "currency":"GBP",
        "calculus":"personal_tax_free_amount"
      },
      "marriage_allowance_transferred_amount":{
        "amount":0,
        "currency":"GBP",
        "calculus":"null (marriage_allowance_transferred_amount)"
      },
      "total_income_before_tax":{
        "amount":67000,
        "currency":"GBP",
        "calculus":"total_income_before_tax"
      },
      "total_tax_free_amount":{
        "amount":0,
        "currency":"GBP",
        "calculus":"null (total_tax_free_amount)"
      }
    }
  },
  "gov_spending":{
    "taxYear":2025,
    "govSpendAmountData":{
      "Welfare":{
        "amount":{
          "amount":3149.76,
          "currency":"GBP",
          "calculus":"Welfare"
        },
        "percentage":21.3
      },
      "Health":{
        "amount":{
          "amount":3090.61,
          "currency":"GBP",
          "calculus":"Health"
        },
        "percentage":20.9
      },
      "StatePensions":{
        "amount":{
          "amount":1759.72,
          "currency":"GBP",
          "calculus":"StatePensions"
        },
        "percentage":11.9
      },
      "NationalDebtInterest":{
        "amount":{
          "amount":1597.06,
          "currency":"GBP",
          "calculus":"NationalDebtInterest"
        },
        "percentage":10.8
      },
      "Education":{
        "amount":{
          "amount":1523.12,
          "currency":"GBP",
          "calculus":"Education"
        },
        "percentage":10.3
      },
      "Defence":{
        "amount":{
          "amount":813.32,
          "currency":"GBP",
          "calculus":"Defence"
        },
        "percentage":5.5
      },
      "PublicOrderAndSafety":{
        "amount":{
          "amount":650.65,
          "currency":"GBP",
          "calculus":"PublicOrderAndSafety"
        },
        "percentage":4.4
      },
      "Transport":{
        "amount":{
          "amount":591.5,
          "currency":"GBP",
          "calculus":"Transport"
        },
        "percentage":4
      },
      "BusinessAndIndustry":{
        "amount":{
          "amount":517.57,
          "currency":"GBP",
          "calculus":"BusinessAndIndustry"
        },
        "percentage":3.5
      },
      "GovernmentAdministration":{
        "amount":{
          "amount":295.75,
          "currency":"GBP",
          "calculus":"GovernmentAdministration"
        },
        "percentage":2
      },
      "HousingAndUtilities":{
        "amount":{
          "amount":280.96,
          "currency":"GBP",
          "calculus":"HousingAndUtilities"
        },
        "percentage":1.9
      },
      "Environment":{
        "amount":{
          "amount":221.81,
          "currency":"GBP",
          "calculus":"Environment"
        },
        "percentage":1.5
      },
      "Culture":{
        "amount":{
          "amount":192.24,
          "currency":"GBP",
          "calculus":"Culture"
        },
        "percentage":1.3
      },
      "OverseasAid":{
        "amount":{
          "amount":103.51,
          "currency":"GBP",
          "calculus":"OverseasAid"
        },
        "percentage":0.7
      },
      "OutstandingPaymentsToTheEU":{
        "amount":{
          "amount":14.79,
          "currency":"GBP",
          "calculus":"OutstandingPaymentsToTheEU"
        },
        "percentage":0.1
      }
    },
    "totalAmount":{
      "amount":14787.6,
      "currency":"GBP",
      "calculus":"total_income_tax"
    }
  },
  "includeBRDMessage":true
}
```

### Responses
**Code**: `401 UNAUTHORIZED`
This response can occur when a call is made by any user without an authorized session or id matching that of the request

**Code**: `500 INTERNAL_SERVER_ERROR`
An exception occurred when trying to process the request

**Code**: `502 BAD_GATEWAY`
Timeout occurred when trying to verify the user