
Annual Tax Summary - Individual application 
====================================================================

A [gov.uk](https://www.gov.uk/) online application service that allows individuals to view the annual summary of their personal tax and National Insurance contributions (NICs) and how they're spent.


Summary
-----------

This service is designed to collect Individual personal tax and how they're spent.


Requirements
------------

This service is written in [Scala] and [Play], so needs the latest [JRE] to run.


Annual SA uprating
------------------
Each year we have to update the tax rules used for ATS for SA users. The process for doing this is described below.

<ol>
<li>Copy the previous year's test folder (in test/ transformers/ATSyyyy) to a new folder for new tax year.</li>
<li>Amend the tax year within the class ATSRawDataTransformerTestFixtureBase in the new folder.</li>
<li>Copy the previous year's code folder (in app/ transformers/ATSyyyy) to a new folder for new tax year.</li>
<li>Rename the classes to the new tax year (e.g. ATSCalculations2023 -> ATSCalculations2024).</li>
<li>Copy the previous years's tax rates to the current year in application.conf.</li>
<li>Add new tax year items to the tax years map at the bottom of ATSCalculations.</li>
<li>Run the unit tests for the new tax year (testOnly test.transformers.ATSyyyy.AtsRawDataTransformerSpec) - they should all pass.</li>
<li>Now follow the usual TDD process to update tests and code. 
<li>Update the tax year in app config staging, raise a PR and get it merged. 
<li>Edit the app/controllers/testOnly/AtsSaFieldListController.getFieldList method and add a new case for the new tax year, adding any new ODS fields and removing any no longer valid. This needs to be up-to-date with the correct ODS field list for the new tax year otherwise the frontend validation on the SME test tool will fail for that tax year.</li>
<li>Raise a PR for the code changes and get it merged into staging.</li>
<li>Test that the calculations appear using the SME test tool (https://www.staging.tax.service.gov.uk/annual-tax-summary/test-only/enterSearch).
<li>Hand over to SME to test the different test scenarios in matrix spreadsheet.
</ol>

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").


[Scala]: http://www.scala-lang.org/
[Play]: http://playframework.com/
[JRE]: http://www.oracle.com/technetwork/java/javase/overview/index.html
[Government Gateway]: http://www.gateway.gov.uk/
    
