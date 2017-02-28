package org.openmrs.module.dhisreporting;

import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.report.definition.PeriodIndicatorReportDefinition;

/**
 * 
 * @author k-joseph
 *
 */
public class PepfarIndicator {

	private String indicatorName;

	private String indicatorDescription;

	private String indicatorCode;

	/**
	 * numerator, denominator, aggregators are text descriptions of what should
	 * be part of the indicator
	 */
	private String numerator;

	private String denominator;

	private String aggregation;
	
	private String disaggregation;

	/**
	 * Required
	 */
	private CohortIndicator indicator;

	private PeriodIndicatorReportDefinition report;

	enum ReportingPeriod {
		DAILY, MONTHLY, QUARTERY, SEMIANNUALLY, ANNUALLY
	}

	public PepfarIndicator(CohortIndicator indicator) {
	}

}
