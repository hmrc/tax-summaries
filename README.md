
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

| Method | Endpoint                           | Note                                                                                                                                                       |
|--------|------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------|
|GET   | /government-spend/:TAX_YEAR| Get government spend figures for the tax year                                                                                                              |
|GET   | /:NINO/:TAX_YEAR/paye-ats-data| Get PAYE tax & NI details for the NINO and tax year                                                                                                        |
|GET   | /:NINO/:YEAR_FROM/:YEAR_TO/paye-ats-data| Get PAYE tax & NI details for the NINO and range of tax years                                                                                              |
|GET   | /:UTR/:TAX_YEAR/ats-data| Get SA tax & NI details for the UTR and tax year                                                                                                           |
|GET   | /:UTR/:ENDYEAR/:NUMBEROFYEARS/ats-list| Get list of tax years for the UTR starting from the specified tax year and going back by the specified number of years where there are SA tax & NI details |
|GET   | /:UTR/has_summary_for_previous_period| Return true if there are any tax & NI summary details for the UTR. Used by BTA only.                                                                       |


 
### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").


[Scala]: http://www.scala-lang.org/
[Play]: http://playframework.com/
[JRE]: http://www.oracle.com/technetwork/java/javase/overview/index.html
[Government Gateway]: http://www.gateway.gov.uk/
    
