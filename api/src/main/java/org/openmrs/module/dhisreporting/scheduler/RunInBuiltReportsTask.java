package org.openmrs.module.dhisreporting.scheduler;

import java.util.Calendar;

import org.openmrs.api.context.Context;
import org.openmrs.module.dhisreporting.api.DHISReportingService;
import org.openmrs.scheduler.tasks.AbstractTask;

public class RunInBuiltReportsTask extends AbstractTask {

	@Override
	public void execute() {
		Calendar cal = Calendar.getInstance();

		if (cal.get(Calendar.MONTH) == 0 || cal.get(Calendar.MONTH) == 3 || cal.get(Calendar.MONTH) == 6
				|| cal.get(Calendar.MONTH) == 9)
			if (cal.get(Calendar.DAY_OF_MONTH) <= 3) {
				Context.getService(DHISReportingService.class).runAndPostOnARTReportToDHIS();
				Context.getService(DHISReportingService.class).runAndPostHIVStatusReportToDHIS();
			}
	}

}
