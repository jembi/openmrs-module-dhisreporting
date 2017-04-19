package org.openmrs.module.dhisreporting.scheduler;

import java.util.Calendar;

import org.openmrs.api.context.Context;
import org.openmrs.module.dhisreporting.MappedIndicatorReport;
import org.openmrs.module.dhisreporting.ReportingPeriodType;
import org.openmrs.module.dhisreporting.api.DHISReportingService;
import org.openmrs.scheduler.tasks.AbstractTask;

public class RunDynamicReportsTask extends AbstractTask {

	@Override
	public void execute() {
		Calendar cal = Calendar.getInstance();

		for (MappedIndicatorReport m : Context.getService(DHISReportingService.class).getAllMappedIndicatorReports()) {
			if (ReportingPeriodType.Daily.name().equalsIgnoreCase(m.getPeriodType())) {
				Context.getService(DHISReportingService.class).runAndPostNewDynamicReportFromIndicatorMappings(m);
			} else if (ReportingPeriodType.Monthly.name().equalsIgnoreCase(m.getPeriodType())) {
				// first 3 days of month
				if (cal.get(Calendar.DAY_OF_MONTH) <= 3) {
					Context.getService(DHISReportingService.class).runAndPostNewDynamicReportFromIndicatorMappings(m);
				}
			} else if (ReportingPeriodType.Quarterly.name().equalsIgnoreCase(m.getPeriodType())) {
				if (cal.get(Calendar.MONTH) == 0 || cal.get(Calendar.MONTH) == 3 || cal.get(Calendar.MONTH) == 6
						|| cal.get(Calendar.MONTH) == 9) {
					if (cal.get(Calendar.DAY_OF_MONTH) <= 3)
						Context.getService(DHISReportingService.class)
								.runAndPostNewDynamicReportFromIndicatorMappings(m);
				}
			} else if (ReportingPeriodType.Yearly.name().equalsIgnoreCase(m.getPeriodType())) {
				if (cal.get(Calendar.MONTH) == 0) {
					if (cal.get(Calendar.DAY_OF_MONTH) <= 3)
						Context.getService(DHISReportingService.class)
								.runAndPostNewDynamicReportFromIndicatorMappings(m);
				}
			} else if (ReportingPeriodType.SixMonthly.name().equalsIgnoreCase(m.getPeriodType())) {
				if (cal.get(Calendar.MONTH) == 6) {
					if (cal.get(Calendar.DAY_OF_MONTH) <= 3)
						Context.getService(DHISReportingService.class)
								.runAndPostNewDynamicReportFromIndicatorMappings(m);
				}
			} else if (ReportingPeriodType.Weekly.name().equalsIgnoreCase(m.getPeriodType())) {
				if (cal.get(Calendar.DAY_OF_WEEK) == 5) {
					Context.getService(DHISReportingService.class).runAndPostNewDynamicReportFromIndicatorMappings(m);
				}
			}
		}
	}

}
