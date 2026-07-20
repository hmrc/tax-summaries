# Get SA tax years for UTR and tax year range

Returns a list of tax years for a UTR and tax year where there is SA tax liability for the tax year.

Calls to this API must be made by an authenticated and authorised user (at least confidence level 50) with the matching id.

**URL**: `/:UTR/:ENDYEAR/:NUMBEROFYEARS/ats-list`

**Method**: `GET`

**URL Params**:

| Parameter Name | Type   | Description                                | Notes                       |
|----------------|--------|--------------------------------------------|-----------------------------|
| UTR            | String | The UTR                                    |                             |
| ENDYEAR        | String | The final tax year                         |                             |
| NUMBEROFYEARS  | String | How many years to go back from the ENDYEAR |                             |

**Required Request Headers**:

| Header Name   | Header Value   | Description                                |
|---------------|----------------|--------------------------------------------|
| Authorization | Bearer {TOKEN} | A valid bearer token from the auth service |

***Example request:***

/1097172563/2025/4/ats-list

## Responses

### Success response

**Code**: `200 OK` if data found

**Response Body**

The response body returns the SA tax and NI details and the government spend figures.

The fields included will depend on the tax year and country where the tax has been paid (England, Scotland or Wales). All the sections below though should be present.

| Field Name  | Description                                           | Data Type | Mandatory/Optional | Notes                          |
|-------------|-------------------------------------------------------|-----------|--------------------|--------------------------------|
| utr     | The UTR                                               | String    | Mandatory          |                                |
| taxPayer   | Tax payer name information                            | Object    | Mandatory          | |
| atsYearList   | An array of tax years for which there is SA liability | Array     | Mandatory          | |


**Response Body Examples**

***An example response.***

```json
{
  "utr":"1130492359",
  "taxPayer":{
    "title":"Miss",
    "forename":"Xxxxx",
    "surname":"Yyyyy"
  },
  "atsYearList":[
    2022,
    2023,
    2024,
    2025
  ]
}
```

### Responses
**Code**: `401 UNAUTHORIZED`
This response can occur when a call is made by any user without an authorized session or id matching that of the request

**Code**: `500 INTERNAL_SERVER_ERROR`
An exception occurred when trying to process the request

**Code**: `502 BAD_GATEWAY`
Timeout occurred when trying to verify the user