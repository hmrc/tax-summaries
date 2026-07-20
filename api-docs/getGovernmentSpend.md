# Get Government Spend

Returns the government spend figures (a.k.a. PESA stats) for a tax year.

Calls to this API must be made by an authenticated and authorised user (at least confidence level 50) with the matching id.

**URL**: `/government-spend/:TAX_YEAR`

**Method**: `GET`

**URL Params**:

| Parameter Name | Type   | Description  | Notes                       |
|----------------|--------|--------------|-----------------------------|
| TAX_YEAR       | String | The tax year |                             |

**Required Request Headers**:

| Header Name   | Header Value   | Description                                |
|---------------|----------------|--------------------------------------------|
| Authorization | Bearer {TOKEN} | A valid bearer token from the auth service |

***Example request:***

/government-spend/2025

## Responses

### Success response

**Code**: `200 OK` if data found

**Response Body**

The response body returns the government spend by economic sector as a JSON object with one key-value pair per sector of the economy.

**Response Body Examples**

***An example entry. Sectors will differ depending on tax year:***

```json
{
  "Welfare":21.3,
  "Health":20.9,
  "StatePensions":11.9,
  "NationalDebtInterest":10.8,
  "Education":10.3,
  "Defence":5.5,
  "PublicOrderAndSafety":4.4,
  "Transport":4,
  "BusinessAndIndustry":3.5,
  "GovernmentAdministration":2,
  "HousingAndUtilities":1.9,
  "Environment":1.5,
  "Culture":1.3,
  "OverseasAid":0.7,
  "OutstandingPaymentsToTheEU":0.1
}
```

### Responses
**Code**: `401 UNAUTHORIZED`
This response can occur when a call is made by any user without an authorized session or id matching that of the request

**Code**: `500 INTERNAL_SERVER_ERROR`
An exception occurred when trying to process the request

**Code**: `502 BAD_GATEWAY`
Timeout occurred when trying to verify the user