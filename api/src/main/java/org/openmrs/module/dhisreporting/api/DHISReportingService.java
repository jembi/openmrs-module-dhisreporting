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

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.api.OpenmrsService;
import org.openmrs.module.dhisreporting.AgeRange;
import org.openmrs.module.dhisreporting.MappedIndicatorReport;
import org.openmrs.module.dhisreporting.OpenMRSToDHISMapping;
import org.openmrs.module.dhisreporting.OpenMRSToDHISMapping.DHISMappingType;
import org.openmrs.module.dhisreporting.api.impl.DHISReportingServiceImpl;
import org.openmrs.module.dhisreporting.mapping.IndicatorMapping;
import org.openmrs.module.dhisreporting.mapping.IndicatorMapping.DisaggregationCategory;
import org.openmrs.module.dhisreporting.mapping.IndicatorMapping.IndicatorMappingCategory;
import org.openmrs.module.dhisreporting.mer.MerIndicator;
import org.openmrs.module.reporting.cohort.definition.CodedObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.common.DurationUnit;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.indicator.CohortIndicator.IndicatorType;
import org.openmrs.module.reporting.report.Report;
import org.openmrs.module.reporting.report.definition.PeriodIndicatorReportDefinition;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.Date;
import java.util.List;

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
			CohortDefinition obsCohort, IndicatorType indicatorType, String indicatorUuid);

	PeriodIndicatorReportDefinition createNewDHISPeriodReportAndItsDHISConnectorMappingOrUseExisting(String reportName,
			String reportDrescription, List<CohortIndicator> indicators, String uuid, String dataSetCode,
			String dataSetPeriod);

	void createCohortQueriesIndicatorsAndReports();

	void transferDHISReportingFilesToDataDirectory();

	Report runPeriodIndicatorReport(PeriodIndicatorReportDefinition reportDef, Date startDate, Date endDate,
			Location location);

	Object sendReportDataToDHIS(Report report, String dataSetId, String period, String orgUnitId, boolean useTestMapper,
			List<IndicatorMapping> indicatorMappings, IndicatorMappingCategory mappingCategory);

	Object runAndSendReportDataForTheCurrentMonth();

	OpenMRSToDHISMapping getMapping(String openmrsIdOrCode, DHISMappingType mappingType);

	List<OpenMRSToDHISMapping> getAllMappings();

	OpenMRSToDHISMapping getMappingFromMappings(String code, DHISMappingType type);

	OpenMRSToDHISMapping saveOrUpdateMapping(OpenMRSToDHISMapping mapping);

	void deleteMapping(OpenMRSToDHISMapping mapping);

	void writeContentToFile(String content, File file);

	/**
	 * @should rightly handle an ageQuery and create an ageRange object from it
	 * @param ageQuery
	 * @return
	 * @see DHISReportingServiceImpl#convertAgeQueryToAgeRangeObject(String,
	 *      DurationUnit, DurationUnit)
	 */
	AgeRange convertAgeQueryToAgeRangeObject(String ageQuery, DurationUnit minAgeUnit, DurationUnit maxAgeUnit);

	/**
	 * @should parse json file well into json array
	 * @param fileLocation
	 * @return
	 */
	JSONArray readJSONArrayFromFile(String fileLocation);

	/**
	 * @should Parse JSONArray From FileSystem into List of MerIndicator objects
	 * @param merIndicatorsFileLocation
	 * @return
	 */
	List<MerIndicator> getMerIndicators(String merIndicatorsFileLocation, Integer startingFrom, Integer endingAt);

	MerIndicator getMerIndicator(String code);

	List<IndicatorMapping> getAllIndicatorMappings(String mappingFileLocation);

	/**
	 * @should should filter using activity, disaggregations, openmrs report and
	 *         dhis data elements
	 * @param mappings
	 * @param mappingFileLocation
	 * @param disaggs
	 * @param openmrsReportUuid
	 * @param dataElementPrefixs
	 * @return filtered indicatorMappings
	 */
	List<IndicatorMapping> getIndicatorMappings(List<IndicatorMapping> mappings, String mappingFileLocation,
			List<DisaggregationCategory> disaggs, String openmrsReportUuid, List<String> dataElementPrefixs, String dataset);

	void createNewPeriodIndicatorONARTReportFromInBuiltIndicatorMappings();

	List<String> pepfarPage(HttpServletRequest request);

	IndicatorMapping getIndicatorMapping(List<IndicatorMapping> indicatorMappings, String mappingFileLocation,
			String dataelementCode, String categoryoptioncomboName);

	String revertOneDisaggToItsName(String categoryComboName);

	JSONObject postIndicatorMappingDHISMetaData(String mappingLocation);

	Object runAndPostNewDynamicReportFromIndicatorMappings(MappedIndicatorReport report);

	List<MappedIndicatorReport> getAllMappedIndicatorReports();

	MappedIndicatorReport getMappedIndicatorReportByUuid(String uuid);

	MappedIndicatorReport getMappedIndicatorReport(Integer id);

	void deleteMappedIndicatorReport(MappedIndicatorReport mappedIndicatorReport);

	void saveMappedIndicatorReport(MappedIndicatorReport mappedIndicatorReport);

	JSONArray getMappedIndicatorReportExistingMeta();

	List<Object> runAndPostDynamicReports();

	Object runAndPostOnARTReportToDHIS();

	Object runAndPostHIVStatusReportToDHIS();

	public void deleteAllDHISReportingReports();

	public Object runAndPostANCReportToDHIS();
}