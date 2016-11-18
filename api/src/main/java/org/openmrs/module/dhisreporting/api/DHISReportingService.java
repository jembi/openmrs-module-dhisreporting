/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.dhisreporting.api;

import java.io.File;
import java.util.Date;
import java.util.List;

import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.dhisconnector.api.model.DHISImportSummary;
import org.openmrs.module.dhisreporting.OpenMRSToDHISMapping;
import org.openmrs.module.dhisreporting.OpenMRSToDHISMapping.DHISMappingType;
import org.openmrs.module.reporting.cohort.definition.CodedObsCohortDefinition;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.report.Report;
import org.openmrs.module.reporting.report.definition.PeriodIndicatorReportDefinition;
import org.springframework.transaction.annotation.Transactional;

/**
 * This service exposes module's core functionality. It is a Spring managed bean
 * which is configured in moduleApplicationContext.xml.
 * <p>
 * It can be accessed only via Context:<br>
 * <code>
 * Context.getService(DHISReportingService.class).someMethod();
 * </code>
 * 
 * @see org.openmrs.api.context.Context
 */
@Transactional
public interface DHISReportingService extends OpenmrsService {

	CodedObsCohortDefinition createDHISObsCountCohortQuery(String name, Concept concept);

	CohortIndicator saveNewDHISCohortIndicator(String indicatorName, String indicatorDescription,
			CodedObsCohortDefinition obsCohort);

	PeriodIndicatorReportDefinition createNewDHISPeriodReportAndItsDHISConnectorMapping(String reportName,
			String reportDrescription, List<CohortIndicator> indicators, String uuid, String dataSetCode,
			String dataSetPeriod);

	void createCohortQueriesIndicatorsAndLabReport();

	void transferDHISMappingsToDataDirectory();

	Report runPeriodIndicatorReport(PeriodIndicatorReportDefinition reportDef, Date startDate, Date endDate,
			Location location);

	DHISImportSummary sendReportDataToDHIS(Report report, String dataSetId, String period, String orgUnitId);

	DHISImportSummary runAndSendReportDataForTheCurrentMonth();

	OpenMRSToDHISMapping getMapping(String openmrsIdOrCode, DHISMappingType mappingType);

	List<OpenMRSToDHISMapping> getAllMappings();

	OpenMRSToDHISMapping getMappingFromMappings(String code, DHISMappingType type);

	OpenMRSToDHISMapping saveOrUpdateMapping(OpenMRSToDHISMapping mapping);

	void deleteMapping(OpenMRSToDHISMapping mapping);

	void writeContentToFile(String content, File file);
}