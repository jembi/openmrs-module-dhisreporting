package org.openmrs.module.dhisreporting;

import javax.annotation.Generated;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.report.definition.PeriodIndicatorReportDefinition;

/**
 * As part of its monitoring, evaluation, and reporting (MER) guidance, the U.S.
 * President’s Emergency Plan for AIDS Relief (PEPFAR) launched a set of
 * essential indicators for orphans and vulnerable children (OVC) programs.
 * 
 * @author k-joseph
 *
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({ "indicatorName", "indicatorDescription", "indicatorCode", "numerator", "denominator",
		"aggregation", "disaggregation" })
@JsonIgnoreProperties(ignoreUnknown = true)

public class MerIndicator {

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

	public MerIndicator(CohortIndicator indicator) {
	}

	public String getIndicatorName() {
		return indicatorName;
	}

	public void setIndicatorName(String indicatorName) {
		this.indicatorName = indicatorName;
	}

	public String getIndicatorDescription() {
		return indicatorDescription;
	}

	public void setIndicatorDescription(String indicatorDescription) {
		this.indicatorDescription = indicatorDescription;
	}

	public String getIndicatorCode() {
		return indicatorCode;
	}

	public void setIndicatorCode(String indicatorCode) {
		this.indicatorCode = indicatorCode;
	}

	public String getNumerator() {
		return numerator;
	}

	public void setNumerator(String numerator) {
		this.numerator = numerator;
	}

	public String getDenominator() {
		return denominator;
	}

	public void setDenominator(String denominator) {
		this.denominator = denominator;
	}

	public String getAggregation() {
		return aggregation;
	}

	public void setAggregation(String aggregation) {
		this.aggregation = aggregation;
	}

	public String getDisaggregation() {
		return disaggregation;
	}

	public void setDisaggregation(String disaggregation) {
		this.disaggregation = disaggregation;
	}

	public CohortIndicator getIndicator() {
		return indicator;
	}

	public void setIndicator(CohortIndicator indicator) {
		this.indicator = indicator;
	}

	public PeriodIndicatorReportDefinition getReport() {
		return report;
	}

	public void setReport(PeriodIndicatorReportDefinition report) {
		this.report = report;
	}

}
