package org.openmrs.module.dhisreporting.scheduler;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.api.context.Context;
import org.openmrs.module.dhisreporting.api.DHISReportingService;
import org.openmrs.scheduler.tasks.AbstractTask;

public class RunReportsAndPushToDHISTask extends AbstractTask {

	protected Log log = LogFactory.getLog(getClass());

	@Override
	public void execute() {
		try {
			log.info("Trying to push data into DHIS: Feedback:\n" + new ObjectMapper().writeValueAsString(
					Context.getService(DHISReportingService.class).runAndSendReportDataForTheCurrentMonth() + "\n"));
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
