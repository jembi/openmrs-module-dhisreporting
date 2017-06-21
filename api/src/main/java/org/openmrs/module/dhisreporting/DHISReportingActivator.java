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

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.GlobalProperty;
import org.openmrs.api.context.Context;
import org.openmrs.module.ModuleActivator;
import org.openmrs.module.dhisconnector.api.DHISConnectorService;
import org.openmrs.module.dhisreporting.api.DHISReportingService;

import java.io.File;
import java.io.IOException;

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
		Context.getService(DHISReportingService.class).transferDHISReportingFilesToDataDirectory();
		Context.getService(DHISReportingService.class).createCohortQueriesIndicatorsAndReports();
		GlobalProperty postDHISMetaTrigger = Context.getAdministrationService()
				.getGlobalPropertyObject(DHISReportingConstants.POST_SAMPLE_DHIS_METADATA);
		GlobalProperty infantAgeQuery = Context.getAdministrationService()
				.getGlobalPropertyObject(DHISReportingConstants.AGEQUERY_INFANT);

		if (postDHISMetaTrigger != null && postDHISMetaTrigger.getPropertyValue().equals("true")) {
			File mappingsFile = new File(getClass().getClassLoader().getResource("metadata.json").getFile());
			String meta;
			try {
				meta = FileUtils.readFileToString(mappingsFile);

				Context.getService(DHISConnectorService.class).postDataToDHISEndpoint("/api/metaData", meta);
				// Context.getService(DHISReportingService.class).postIndicatorMappingDHISMetaData(null);
				postDHISMetaTrigger.setPropertyValue("false");
				Context.getAdministrationService().saveGlobalProperty(postDHISMetaTrigger);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (infantAgeQuery == null) {
			infantAgeQuery = new GlobalProperty(DHISReportingConstants.AGEQUERY_INFANT, "<1",
					"age query (e.g; 25+, 20-24, <15, <=30, >45, >=13) for an infant");
			Context.getAdministrationService().saveGlobalProperty(infantAgeQuery);
		}

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
		Context.getService(DHISReportingService.class).deleteAllDHISReportingReports();
	}

}
