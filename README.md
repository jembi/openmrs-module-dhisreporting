<img src="https://s3.amazonaws.com/uploads.hipchat.com/20562/3117993/yatUWvRRArvRZkI/omrs-button%20%282%29.png" alt="OpenMRS" align="left"/> <img src="https://s3.amazonaws.com/uploads.hipchat.com/20562/3117993/pmvz9920GZ81cMT/dhis2.png" alt="OpenMRS" align="right"/>

## DHIS OpenMRS Reporting module

openmrs-module-dhisreporting is a generic path to submit data from OpenMRS into DHIS2, the module uses [DHIS Connector](http://github.com/jembi/openmrs-module-dhisconnector) to post reporting data into DHIS2
____________________________________


# Mapping

* Mapping DHIS2 datasets (containing dataelements & category option combinations/disaggregations) to OpenMRS period indincator reports (containing indicators & dimensions) happens through a [locally stored CSV file](https://github.com/jembi/openmrs-module-dhisreporting/blob/master/api/src/main/resources/pepfar-meta-datim.csv) stored within DHISReporting folder that can be found within the OpenMRS Data folder.
* On installation of this module, reports configured within the mapping will be looked for within the System which when non existant will be auto generated basing on the configurations within the same mapping file.

> Let us consider the mapping file

```
dataelementName|dataelementCode|dataelementId|categoryoptioncomboName|categoryoptioncomboCode|categoryoptioncomboUid|dataset|disaggregationCategory|activeString|openmrsReportUuid|openmrsNumeratorCohortUuid|openmrsDenominatorCohortUuid|inherentDisaggOrder|reportingPeriodType
```

* `disaggregationCategory` is a basic categorisation of the varios possible (e.g. MER 2.0) indicator disaggregations, forexample; `AGE, GENDER, DEFAULT` (the default such as HIV positive status for ON ART report), `inherent` (disaggregations broken down further into others such as gender within a given age range), `CODED` (any other complex patient observational disaggregation) and `OTHER` and `NULL` (any other disaggregations not fiting the above categories).
* `activeString` (set to either `TRUE/FALSE`) is used to set whether a given indicator should be loaded within the reports setup or generation at OpenMRS level as well as when posting/submitting the data to DHIS.
* `inherentDisaggOrder` is used when `disaggregationCategory` is set to `INHERENT` just to specify the order used such as (Age,Gender), the order must be seperated with comma(,) and the key only supported keywords are `Age,Gender,Coded`.
* `reportingPeriodType` is a DHIS period type to be used to define starting and ending date when running the OpenMRS report and then used raw while posting data to DHIS, current supported values are; `Quarterly, Monthly, Yearly, Weekly and Daily`.
* The rest of the fiels should be self eplanatory by their names
* 


# Module Status

Jira Issues
  - [DHISReporting Module's issues](https://jembiprojects.jira.com/projects/RODI/issues)

Implemented
  - [x] Create OpenMRS to DHIS2 mapping terminplogy
  - [x] Support Inherent, age and gender dimension categorizations
  - [x] Write ON ART report and support sample test indicators whose DHIS metadata can be created
  - [x] 

TODO
  - [ ] Support UI indicator mapping managment (edit, create new, delete) export/import etc
  - [ ] Write background tasks to run reports and post data depending on the reporting periods set in the mapping
  - [ ] Report setup must always be triggered whenever changes are made to the mapping
  - [ ] Create inbuit support for all the four default reports
  - [ ] Setup DHIS2 MER 2.0 indicators instance and test with it all default reports


## License

[![CC0](https://licensebuttons.net/p/zero/1.0/88x31.png)](https://creativecommons.org/publicdomain/zero/1.0/)
[MPL 2.0 w/ HD](http://openmrs.org/license/) © [OpenMRS Inc.](http://www.openmrs.org/)
