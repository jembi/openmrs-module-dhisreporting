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

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.GlobalProperty;
import org.openmrs.api.context.Context;
import org.openmrs.module.dhisreporting.AgeRange;
import org.openmrs.module.dhisreporting.DHISReportingConstants;
import org.openmrs.module.dhisreporting.OpenMRSToDHISMapping;
import org.openmrs.module.dhisreporting.OpenMRSToDHISMapping.DHISMappingType;
import org.openmrs.module.dhisreporting.mapping.IndicatorMapping;
import org.openmrs.module.dhisreporting.mer.MerIndicator;
import org.openmrs.module.reporting.cohort.definition.CodedObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.service.CohortDefinitionService;
import org.openmrs.module.reporting.common.DurationUnit;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.test.Verifies;

/**
 * Tests {@link ${DHISReportingService}}.
 */
public class DHISReportingServiceTest extends BaseModuleContextSensitiveTest {

	GlobalProperty mappingsLocation = null;

	@Test
	public void shouldSetupContext() {
		assertNotNull(Context.getService(DHISReportingService.class));
	}

	@Before
	public void init() {
		Context.getService(DHISReportingService.class).transferDHISReportingFilesToDataDirectory();
	}

	@Test
	public void test_periodIndicatorCreationByThisModule() {
		Concept concept = Context.getConceptService().getConcept(5089);
		CodedObsCohortDefinition cohort = Context.getService(DHISReportingService.class)
				.createDHISObsCountCohortQuery("test", concept);
		CohortIndicator cohortIndicator = Context.getService(DHISReportingService.class)
				.saveNewDHISCohortIndicator("test", null, cohort);
		ReportDefinition report = Context.getService(DHISReportingService.class)
				.createNewDHISPeriodReportAndItsDHISConnectorMappingOrUseExisting("test", null,
						Collections.singletonList(cohortIndicator), null, "", "");
		try {
			Cohort c = Context.getService(CohortDefinitionService.class).evaluate(cohort, null);

			Assert.assertTrue(c.size() > 0);
		} catch (EvaluationException e) {
			e.printStackTrace();
		}
		Assert.assertNotNull(cohortIndicator);
		Assert.assertNotNull(report);
	}

	@Test
	public void test_getAllMappings() {
		List<OpenMRSToDHISMapping> mappings = Context.getService(DHISReportingService.class).getAllMappings();
		Assert.assertNotNull(mappings);
		Assert.assertTrue(mappings.size() > 0);
	}

	@Test
	public void test_saveOrEditAndDeleteMapping() {
		List<OpenMRSToDHISMapping> mappings = Context.getService(DHISReportingService.class).getAllMappings();
		Integer mappingsOriginalCount = mappings.size();
		OpenMRSToDHISMapping map = Context.getService(DHISReportingService.class).getMapping("JEMBI",
				DHISMappingType.LOCATION);
		OpenMRSToDHISMapping newMap = new OpenMRSToDHISMapping();
		File mappingFile = DHISReportingConstants.DHISREPORTING_FINAL_MAPPINGFILE;

		Assert.assertEquals("vjFcsoL24z5", map.getDhisId());

		map.setDhisId("dhisNewId");
		map = Context.getService(DHISReportingService.class).saveOrUpdateMapping(map);

		Assert.assertEquals("dhisNewId", map.getDhisId());
		Assert.assertEquals(mappingsOriginalCount.intValue(),
				Context.getService(DHISReportingService.class).getAllMappings().size());
		Assert.assertEquals(mappingFile.getAbsolutePath(),
				DHISReportingConstants.DHISREPORTING_FINAL_MAPPINGFILE.getAbsolutePath());

		newMap.setOpenmrsId("testOmrs");
		newMap.setDhisId("testDHIS");
		newMap.setType(DHISMappingType.CONCEPTDATAELEMENT.name());
		newMap = Context.getService(DHISReportingService.class).saveOrUpdateMapping(newMap);

		Assert.assertEquals(mappingFile.getAbsolutePath(),
				DHISReportingConstants.DHISREPORTING_FINAL_MAPPINGFILE.getAbsolutePath());
		Assert.assertEquals("testDHIS", newMap.getDhisId());
		Assert.assertEquals(mappingsOriginalCount.intValue() + 1,
				Context.getService(DHISReportingService.class).getAllMappings().size());

		Context.getService(DHISReportingService.class).deleteMapping(newMap);

		Assert.assertEquals(mappingsOriginalCount.intValue(),
				Context.getService(DHISReportingService.class).getAllMappings().size());
	}

	/**
	 * @see {@link DHISReportingService#convertAgeQueryToAgeRangeObject(String, DurationUnit)}
	 */
	@Test
	@Verifies(value = "should rightly handle an ageQuery and create an ageRange object from it", method = "convertAgeQueryToAgeRangeObject(String, DurationUnit)")
	public void convertAgeQueryToAgeRangeObject_shouldRightlyHandleAnAgeQueryAndCreateAnAgeRangeObjectFromIt() {
		// testing with queries; 15-19, 20-24, 25-49, >=50, <1, >15

		AgeRange infant = Context.getService(DHISReportingService.class).convertAgeQueryToAgeRangeObject("<=1",
				DurationUnit.YEARS, DurationUnit.YEARS);
		AgeRange adult = Context.getService(DHISReportingService.class).convertAgeQueryToAgeRangeObject(">=15",
				DurationUnit.YEARS, DurationUnit.YEARS);
		AgeRange infant1 = Context.getService(DHISReportingService.class).convertAgeQueryToAgeRangeObject("<12",
				DurationUnit.MONTHS, DurationUnit.MONTHS);
		AgeRange adult1 = Context.getService(DHISReportingService.class).convertAgeQueryToAgeRangeObject(">15",
				DurationUnit.YEARS, DurationUnit.YEARS);
		AgeRange m1519 = Context.getService(DHISReportingService.class).convertAgeQueryToAgeRangeObject("15-19",
				DurationUnit.MONTHS, DurationUnit.MONTHS);
		AgeRange y2549 = Context.getService(DHISReportingService.class).convertAgeQueryToAgeRangeObject("25-49",
				DurationUnit.YEARS, DurationUnit.YEARS);

		Assert.assertEquals(DurationUnit.YEARS, infant.getMaxAgeUnit());
		Assert.assertEquals(DurationUnit.YEARS, adult1.getMinAgeUnit());
		Assert.assertEquals(DurationUnit.MONTHS, m1519.getMinAgeUnit());
		Assert.assertEquals(DurationUnit.MONTHS, m1519.getMaxAgeUnit());

		Assert.assertEquals(new Integer(0), infant.getMinAge());
		Assert.assertEquals(new Integer(1), infant.getMaxAge());
		Assert.assertEquals(new Integer(0), infant1.getMinAge());
		Assert.assertEquals(new Integer(11), infant1.getMaxAge());
		Assert.assertEquals(new Integer(15), adult.getMinAge());
		Assert.assertNull(adult.getMaxAge());
		Assert.assertEquals(new Integer(16), adult1.getMinAge());
		Assert.assertNull(adult1.getMaxAge());

		Assert.assertEquals(new Integer(15), m1519.getMinAge());
		Assert.assertEquals(new Integer(19), m1519.getMaxAge());
		Assert.assertEquals(new Integer(25), y2549.getMinAge());
		Assert.assertEquals(new Integer(49), y2549.getMaxAge());
	}

	/**
	 * @see {@link DHISReportingService#readJSONArrayFromFile(String)}
	 */
	@Test
	@Ignore
	@Verifies(value = "should parse json file well into json array", method = "readJSONArrayFromFile(String)")
	public void readJSONArrayFromFile_shouldParseJsonFileWellIntoJSONArray() {
		JSONArray json = Context.getService(DHISReportingService.class).readJSONArrayFromFile(getClass()
				.getClassLoader().getResource(DHISReportingConstants.DHISREPORTING_MER_INDICATORS_FILENAME).getFile());

		Assert.assertEquals("PREP_NEW", ((JSONObject) json.get(0)).get("code"));
		// Assert.assertEquals("Male","Female", ((JSONObject) ((JSONObject)
		// json.get(0)).get("disaggregation")).get("sex"));
		Assert.assertTrue(StringUtils.isBlank((String) ((JSONObject) json.get(1)).get("description")));
	}

	/**
	 * Also tests starting and ending indexes which work as paging supports
	 * 
	 * @see {@link DHISReportingService#getMerIndicators(String, Integer, Integer)}
	 * 
	 */
	@Test
	@Ignore
	@Verifies(value = "should Parse JSONArray From FileSystem into List of MerIndicator objects", method = "getMerIndicators(String, Integer, Integer)")
	public void getMerIndicators_shouldParseJSONArrayFromFileSystemIntoListofMerIndicatorObjects() {
		List<MerIndicator> indicators = Context.getService(DHISReportingService.class)
				.getMerIndicators(
						getClass().getClassLoader()
								.getResource(DHISReportingConstants.DHISREPORTING_MER_INDICATORS_FILENAME).getFile(),
						1, 10);
		Assert.assertEquals(10, indicators.size());
		Assert.assertTrue(StringUtils.isBlank(indicators.get(9).getDescription()));
		Assert.assertEquals("VMMC_CIRC", indicators.get(1).getCode());
		/*
		 * Assert.assertEquals("Received PrEP by:  Age/Sex",
		 * indicators.get(0).getDisaggregation().get("name"));
		 * Assert.assertEquals(
		 * "Number of individuals who (a) received HTS and (b) their test results in the reporting period"
		 * , indicators.get(2).getNumerator().get("name"));
		 */
	}

	@Test
	@Ignore
	public void getMerIndicator() {
		MerIndicator HTS_TST = Context.getService(DHISReportingService.class).getMerIndicator("HTS_TST");

		Assert.assertEquals(
				"Number of individuals who (a) received HTS and (b) their test results in the reporting period",
				HTS_TST.getName());
		Assert.assertTrue(HTS_TST.isActive());
		/*
		 * Assert.assertEquals(
		 * "Received HTS and results according to: - Community service delivery modality - Facillity service delivery modality - Age/Sex/Result received by Service Modaility"
		 * , (String) HTS_TST.getDisaggregation().get("name"));
		 */
	}

	@Test
	public void testConvertingExcellIntoJSON() {
		String mappingFile = getClass().getClassLoader().getResource(DHISReportingConstants.INDICATOR_MAPPING_FILE_NAME)
				.getFile();
		List<IndicatorMapping> mappings = Context.getService(DHISReportingService.class)
				.getIndicatorMappings(mappingFile);

		Assert.assertFalse(mappings.get(0).getActive());
		Assert.assertEquals("PREP_NEW_D_DSD_Age",
				mappings.get(0).getDataelementCode());
	}

}
