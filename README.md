
Annual Tax Summary - Backend Microservice
====================================================================

Annual Tax Summary is an online service that allows individuals and agents to view the annual summary of an individual's personal tax and National Insurance contributions (NICs) and how they've been spent.

Running the service using service manager
------------
sm2 --start TAXS

Running the app locally
------------
sbt "run -Dapplication.router=testOnlyDoNotUseInAppConf.Routes"


Testing
------------
Please run Unit tests by running `sbt test` and `sbt it:test`

## Endpoints

- [Get Government Spend](api-docs/getGovernmentSpend.md): `GET /government-spend/:TAX_YEAR`
- [Get PAYE details for nino and tax year](api-docs/getPAYEDetailsForNinoTaxYear.md): `GET /:NINO/:TAX_YEAR/paye-ats-data`
- [Get PAYE details for nino and tax year range](api-docs/getPAYEDetailsForNinoTaxYearRange.md): `GET /:NINO/:YEAR_FROM/:YEAR_TO/paye-ats-data`
- [Get SA details for UTR and tax year](api-docs/getSADetailsForUTRTaxYear.md): `GET /:UTR/:TAX_YEAR/ats-data`
- [Get tax years for UTR and tax year range](api-docs/getSATaxYearsForUTRTaxYearRange.md): `GET /:UTR/:ENDYEAR/:NUMBEROFYEARS/ats-list`
- [Has summary for previous period](api-docs/hasSummaryForPreviousPeriod.md): `GET /:UTR/has_summary_for_previous_period`
 
### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").


[Scala]: http://www.scala-lang.org/
[Play]: http://playframework.com/
[JRE]: http://www.oracle.com/technetwork/java/javase/overview/index.html
[Government Gateway]: http://www.gateway.gov.uk/
    
