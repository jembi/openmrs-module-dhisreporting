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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.GlobalProperty;
import org.openmrs.api.context.Context;
import org.openmrs.module.dhisreporting.DHISReportingConstants;
import org.openmrs.module.dhisreporting.OpenMRSToDHISMapping;
import org.openmrs.module.dhisreporting.OpenMRSToDHISMapping.DHISMappingType;
import org.openmrs.module.reporting.cohort.definition.CodedObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.service.CohortDefinitionService;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.test.BaseModuleContextSensitiveTest;

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
		Context.getService(DHISReportingService.class).transferDHISMappingsToDataDirectory();
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
}
