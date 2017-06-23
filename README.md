<img src="https://s3.amazonaws.com/uploads.hipchat.com/20562/3117993/pmvz9920GZ81cMT/dhis2.png" alt="OpenMRS" align="right"/>

## DHIS OpenMRS Reporting module

openmrs-module-dhisreporting is a generic path to submit data (by default supports PEPFAR MER 2.0 Indicators) from OpenMRS into DHIS2, the module uses [DHIS Connector](http://github.com/jembi/openmrs-module-dhisconnector) to post reporting data into DHIS2
____________________________________


# Mapping

* Mapping DHIS2 datasets (containing dataelements & category option combinations/disaggregations) to OpenMRS period indincator reports (containing indicators & dimensions) happens through a [locally stored CSV file](https://github.com/jembi/openmrs-module-dhisreporting/blob/master/api/src/main/resources/pepfar-meta-datim.csv) within DHISReporting folder that can be found within the OpenMRS Data folder.
* On installation of this module, reports configured within the mapping will be looked for within the System which when non existant will be auto generated basing on the configurations within the same mapping file.

> Let us consider the mapping file

```
dataelementName|dataelementCode|dataelementId|categoryoptioncomboName|categoryoptioncomboCode|categoryoptioncomboUid|dataset|disaggregationCategory|activeString|openmrsReportUuid|openmrsNumeratorCohortUuid|openmrsDenominatorCohortUuid|inherentDisaggOrder|reportingPeriodType
```

* `disaggregationCategory` is a basic categorisation of the varios possible (e.g. MER 2.0) indicator disaggregations, forexample; `AGE` (Valid sample values inlude;  25+, 20-24, <15, <=30, >45, >=13. Keep the characters and indentation), `GENDER`, `DEFAULT` (the default such as HIV positive status for ON ART report), `inherent` (disaggregations broken down further into others such as gender within a given age range), `CODED` (any other complex patient observational disaggregation) and `OTHER` and `NULL` (any other disaggregations not fiting the above categories).
* `activeString` (set to either `TRUE`/`FALSE`) is used to set whether a given indicator should be loaded within the reports setup or generation at OpenMRS level as well as when posting/submitting the data to DHIS.
* `inherentDisaggOrder` is used when `disaggregationCategory` is set to `INHERENT` just to specify the order used such as (Age,Gender), the order must be seperated with comma(,) and the key only supported keywords are `Age,Gender,Coded`.
* `reportingPeriodType` is a DHIS period type to be used to define starting and ending date when running the OpenMRS report and then used raw while posting data to DHIS, current supported values are; `Quarterly`, `Monthly`, `Yearly`, `Weekly` and `Daily`.
* `codedDisaggQuestion` is the concept id of the question being answered by the value(concept name) set for `categoryoptioncomboName`
* `codedDisaggAnswer` is the concept id if any that answers `codedDisaggQuestion` instead of concept name
* `category` is the category for a given indicatpr mapping, currently supports either `INBUILT` or `DYNAMIC`, for `DYNAMIC` the period report indicator codes must match the `dataelementCode`__DisaggregationName format e.g; `TX_CURR_N_DSD_Age_Sex__ONETOFOUROFAGE__FEMALE`, `TX_CURR_N_TA_Age_Sex_TARGET__FIFTEENANDABOVEOFAGE__FEMALE`, `TX_CURR_N_TA_NARRATIVE`, it is recommended that each mapping entry have a unique `dataelementCode`
* `baseCohort` is a categorisation of the cohort; currently can be; `ANC, ONART, HIVSTATUS, PREVENTION, OTHERS`, its a way for the `INBUILT` indicators to be assigned at-least one ('base') cohort definition 
* The rest of the fields should be self eplanatory by their names

> Generating mappings from DHIS metadata

* You can export a CSV version of an SQLView of the Postgress SQL below;
```
select coalesce(de.name, '') as dataelementName, coalesce(de.code, '') as dataelementCode, coalesce(de.uid, '') as dataelementId, coalesce(coc.name, '') as categoryoptioncomboName, coalesce(coc.code, '') as categoryoptioncomboCode, coalesce(coc.uid, '') as categoryoptioncomboUid, coalesce(ds.uid, '') as dataset, '' as disaggregationCategory, 'FALSE' as activeString, '' as openmrsReportUuid, '' as openmrsNumeratorCohortUuid, '' as openmrsDenominatorCohortUuid, '' as inherentDisaggOrder, 'Quarterly' as reportingPeriodType, '' as codedDisaggQuestion, '' as codedDisaggAnswer, 'INBUILT' as category
from categoryoptioncombo coc
inner join categorycombos_optioncombos ccoc on ccoc.categoryoptioncomboid = coc.categoryoptioncomboid
inner join dataelement de on de.categorycomboid = ccoc.categorycomboid
inner join dataset ds on ds.categorycomboid = ccoc.categorycomboid
order by dataset, dataelementId
```
* Fill in the remaining details and make any corrections/editions such as excluding some datasets et-cetera
* Here is a sample RHMIS ANC mappings output generator

```
select coalesce(de.name, '') as dataelementName, coalesce(de.code, '') as dataelementCode, coalesce(de.uid, '') as dataelementId, coalesce(coc.name, '') as categoryoptioncomboName, coalesce(coc.code, '') as categoryoptioncomboCode, coalesce(coc.uid, '') as categoryoptioncomboUid, coalesce(ds.uid, '') as dataset, '' as disaggregationCategory, 'FALSE' as activeString, '' as openmrsReportUuid, '' as openmrsNumeratorCohortUuid, '' as openmrsDenominatorCohortUuid, '' as inherentDisaggOrder, 'Monthly' as reportingPeriodType, '' as codedDisaggQuestion, '' as codedDisaggAnswer, 'INBUILT' as category
from dataelement de
inner join categorycombo cc on cc.categorycomboid = de.categorycomboid
inner join dataset ds on cc.categorycomboid = ds.categorycomboid
inner join categorycombos_optioncombos ccoc on cc.categorycomboid = ccoc.categorycomboid
inner join categoryoptioncombo coc on ccoc.categoryoptioncomboid = coc.categoryoptioncomboid
inner join dataelementgroupmembers degm on de.dataelementid = degm.dataelementid
inner join dataelementgroup deg on degm.dataelementgroupid = deg.dataelementgroupid
where ds.uid = 'ygTEbJWQhqf' and deg.uid = 'AlzCrr1AvUe'
order by dataset, dataelementId
```

# Module Status

Jira Issues
  - [DHISReporting Module's issues](https://jembiprojects.jira.com/projects/RODI/issues)

Implemented
  - [x] Create OpenMRS to DHIS2 mapping design
  - [x] Support Inherent, age and gender dimension/disaggregation categorisations
  - [x] Write ON ART report and support sample test indicators whose DHIS metadata can be created (add sample test metadata to prove the concept)
  - [x] Write background tasks to run reports and post data depending on the reporting periods set in the mapping
  - [x] Setup DHIS2 MER 2.0 indicators instance and test with it all default reports; [here](http://146.185.151.152:8080)

TODO
  > In progress
  - [ ] Support UI indicator mapping managment (edit, create new, delete) export/import etc
  - [ ] Report setup must always be triggered whenever changes are made to the mapping
  - [ ] Create inbuilt support for all the four default reports
  

# References
 - [First presentation](http://rebrand.ly/drPresentation)

# License

[![CC0](https://licensebuttons.net/p/zero/1.0/88x31.png)](http://jembi.org)
[MPL 2.0 w/ HD](http://openmrs.org/license/) © [OpenMRS Inc.](http://www.openmrs.org/)
