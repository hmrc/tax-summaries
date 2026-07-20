# Has summary for previous period

Returns true if there is SA tax or NI information for previous period.

Calls to this API must be made by an authenticated and authorised user (at least confidence level 50) with the matching id.

**URL**: `/:UTR/has_summary_for_previous_period`

**Method**: `GET`

**URL Params**:

| Parameter Name | Type   | Description                                | Notes                       |
|----------------|--------|--------------------------------------------|-----------------------------|
| UTR            | String | The UTR                                    |                             |

**Required Request Headers**:

| Header Name   | Header Value   | Description                                |
|---------------|----------------|--------------------------------------------|
| Authorization | Bearer {TOKEN} | A valid bearer token from the auth service |

***Example request:***

/1097172563/has_summary_for_previous_period

## Responses

### Success response

**Code**: `200 OK` if data found

**Response Body**

The response body returns a JSON object with a boolean field "has_ats" with a value of either true or false.

**Response Body Examples**

***An example response. Fields returned will vary depending on the tax year and country.***

```json
{
  "has_ats": true
}
```

### Responses
**Code**: `401 UNAUTHORIZED`
This response can occur when a call is made by any user without an authorized session or id matching that of the request

**Code**: `500 INTERNAL_SERVER_ERROR`
An exception occurred when trying to process the request

**Code**: `502 BAD_GATEWAY`
Timeout occurred when trying to verify the user