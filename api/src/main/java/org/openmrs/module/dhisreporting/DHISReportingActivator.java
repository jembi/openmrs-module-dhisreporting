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
package org.openmrs.module.dhisreporting;

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.module.ModuleActivator;
import org.openmrs.module.dhisreporting.api.DHISReportingService;
import org.openmrs.module.reporting.common.DateUtil;

/**
 * This class contains the logic that is run every time this module is either
 * started or stopped.
 */
public class DHISReportingActivator implements ModuleActivator {

	protected Log log = LogFactory.getLog(getClass());

	/**
	 * @see ModuleActivator#willRefreshContext()
	 */
	public void willRefreshContext() {
		log.info("Refreshing DHIS Reporting Module");
	}

	/**
	 * @see ModuleActivator#contextRefreshed()
	 */
	public void contextRefreshed() {
		log.info("DHIS Reporting Module refreshed");
	}

	/**
	 * @see ModuleActivator#willStart()
	 */
	public void willStart() {
		log.info("Starting DHIS Reporting Module");
	}

	/**
	 * @see ModuleActivator#started()
	 */
	public void started() {
		Location defaultLocation = Context.getLocationService().getLocation(Integer
				.parseInt(Context.getAdministrationService().getGlobalProperty("mohtracportal.defaultLocationId")));
		Date startDate = DateUtil.getDateTime(2008, 1, 1);
		Date endDate = new Date();

		log.info("Trying to create lab request HMIS indicator report...\n"
				+ Context.getService(DHISReportingService.class)
						.createCohortQueriesIndicatorsAndLabReport(defaultLocation, startDate, endDate));
		log.info("DHIS Reporting Module started");
	}

	/**
	 * @see ModuleActivator#willStop()
	 */
	public void willStop() {
		log.info("Stopping DHIS Reporting Module");
	}

	/**
	 * @see ModuleActivator#stopped()
	 */
	public void stopped() {
		log.info("DHIS Reporting Module stopped");
	}

}
