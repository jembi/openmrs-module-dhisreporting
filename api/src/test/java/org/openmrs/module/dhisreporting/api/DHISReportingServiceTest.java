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
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.report.ReportData;
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
		Location location = Context.getLocationService().getLocation(1);
		Date startDate = DateUtil.getDateTime(2008, 1, 1);
		Date endDate = DateUtil.getDateTime(2016, 11, 1);
		Cohort cohort = Context.getService(DHISReportingService.class).evaluateDHISObsCountCohortQuery(concept,
				location, startDate, endDate);
		CohortIndicator cohortIndicator = Context.getService(DHISReportingService.class)
				.saveNewDHISCohortIndicator("test", null, cohort);
		ReportData reportData = Context.getService(DHISReportingService.class).evaluateNewDHISPeriodReport("test", null,
				startDate, endDate, location, Collections.singletonList(cohortIndicator));

		Assert.assertTrue(cohort.size() > 0);
		Assert.assertNotNull(cohortIndicator);
		Assert.assertNotNull(reportData);
	}
}
