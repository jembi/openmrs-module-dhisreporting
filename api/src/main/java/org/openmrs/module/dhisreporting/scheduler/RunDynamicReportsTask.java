package org.openmrs.module.dhisreporting.scheduler;

import org.openmrs.api.context.Context;
import org.openmrs.module.dhisreporting.DHISReportingConstants;
import org.openmrs.module.dhisreporting.MappedIndicatorReport;
import org.openmrs.module.dhisreporting.ReportingPeriodType;
import org.openmrs.module.dhisreporting.api.DHISReportingService;
import org.openmrs.scheduler.tasks.AbstractTask;

import java.util.Calendar;

public class RunDynamicReportsTask extends AbstractTask {

	@Override
	public void execute() {
		Calendar cal = Calendar.getInstance();
		String gpWeeklyDay = Context.getAdministrationService().getGlobalProperty(DHISReportingConstants.PERIODTYPE_WEEKLY_DAY);
		String gpMonthlyDay = Context.getAdministrationService().getGlobalProperty(DHISReportingConstants.PERIODTYPE_MONTHLY_DAY);

		for (MappedIndicatorReport m : Context.getService(DHISReportingService.class).getAllMappedIndicatorReports()) {
			if (ReportingPeriodType.Daily.name().equalsIgnoreCase(m.getPeriodType())) {
				Context.getService(DHISReportingService.class).runAndPostNewDynamicReportFromIndicatorMappings(m);
			} else if (ReportingPeriodType.Monthly.name().equalsIgnoreCase(m.getPeriodType())) {
				// first 3 days of month
				if (cal.get(Calendar.DAY_OF_MONTH) <= 3 || cal.get(Calendar.DAY_OF_MONTH) == Integer.parseInt(gpMonthlyDay)) {
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
				if (cal.get(Calendar.DAY_OF_WEEK) == Integer.parseInt(gpWeeklyDay)) {
					Context.getService(DHISReportingService.class).runAndPostNewDynamicReportFromIndicatorMappings(m);
				}
			}
		}
	}

}
