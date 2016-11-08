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

import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.api.context.Context;
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

	@Test
	public void shouldSetupContext() {
		assertNotNull(Context.getService(DHISReportingService.class));
	}

	@Test
	public void test_periodIndicatorCreationByThisModule() {
		Concept concept = Context.getConceptService().getConcept(5089);
		CodedObsCohortDefinition cohort = Context.getService(DHISReportingService.class)
				.createDHISObsCountCohortQuery("test", concept);
		CohortIndicator cohortIndicator = Context.getService(DHISReportingService.class)
				.saveNewDHISCohortIndicator("test", null, cohort);
		ReportDefinition report = Context.getService(DHISReportingService.class)
				.createNewDHISPeriodReportAndItsDHISConnectorMapping("test", null,
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
}
