package org.openmrs.module.dhisreporting.mer;

import javax.annotation.Generated;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
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
@JsonPropertyOrder({ "code", "name", "active", "description", "numerator", "denominator",
		"aggregation", "disaggregation", "openmrsReport", "dhisMeta" })
@JsonIgnoreProperties(ignoreUnknown = true)
public class MerIndicator {

	@JsonProperty("name")
	private String name;

	@JsonProperty("description")
	private String description;

	@JsonProperty("code")
	private String code;

	/**
	 * numerator, denominator are text descriptions of what should be part of
	 * the indicator
	 */
	@JsonProperty("numerator")
	private MerIndicatorNumeratorOrDenominator numerator;

	@JsonProperty("denominator")
	private MerIndicatorNumeratorOrDenominator denominator;

	@JsonProperty("aggregation")
	private MerIndicatorAggregation aggregation;

	@JsonProperty("disaggregation")
	private MerIndicatorDisaggregation disaggregation;

	@JsonProperty("openmrsReport")
	private MerIndicatorOpenmrsReport openmrsReport;

	@JsonProperty("dhisMeta")
	private MerIndicatorDhisMeta dhisMeta;

	/**
	 * Used to either activate or disable an indicator from being executed (run
	 * OpenMRS local report and create or include in mapping as well as submit
	 * indicator evaluation into configured external DHIS instance)
	 */
	@JsonProperty("active")
	private Boolean active;

	/**
	 * Required
	 */
	private CohortIndicator indicator;

	private PeriodIndicatorReportDefinition report;

	enum ReportingPeriod {
		DAILY, MONTHLY, QUARTERY, SEMIANNUALLY, ANNUALLY
	}

	@JsonProperty("name")
	public String getName() {
		return name;
	}

	@JsonProperty("name")
	public void setName(String indicatorName) {
		this.name = indicatorName;
	}

	@JsonProperty("description")
	public String getDescription() {
		return description;
	}

	@JsonProperty("description")
	public void setDescription(String indicatorDescription) {
		this.description = indicatorDescription;
	}

	@JsonProperty("code")
	public String getCode() {
		return code;
	}

	@JsonProperty("code")
	public void setCode(String indicatorCode) {
		this.code = indicatorCode;
	}

	@JsonProperty("numerator")
	public MerIndicatorNumeratorOrDenominator getNumerator() {
		return numerator;
	}

	@JsonProperty("numerator")
	public void setNumerator(MerIndicatorNumeratorOrDenominator numerator) {
		this.numerator = numerator;
	}

	@JsonProperty("denominator")
	public MerIndicatorNumeratorOrDenominator getDenominator() {
		return denominator;
	}

	@JsonProperty("denominator")
	public void setDenominator(MerIndicatorNumeratorOrDenominator denominator) {
		this.denominator = denominator;
	}

	@JsonProperty("aggregation")
	public MerIndicatorAggregation getAggregation() {
		return aggregation;
	}

	@JsonProperty("aggregation")
	public void setAggregation(MerIndicatorAggregation aggregation) {
		// TODO using this here set report and indicator properties heres
		this.aggregation = aggregation;
	}

	@JsonProperty("disaggregation")
	public MerIndicatorDisaggregation getDisaggregation() {
		return disaggregation;
	}

	@JsonProperty("disaggregation")
	public void setDisaggregation(MerIndicatorDisaggregation disaggregation) {
		// TODO using this here set report and indicator properties heres
		this.disaggregation = disaggregation;
	}

	public CohortIndicator getIndicator() {
		return indicator;
	}

	public PeriodIndicatorReportDefinition getReport() {
		return report;
	}

	public MerIndicatorOpenmrsReport getOpenmrsReport() {
		return openmrsReport;
	}

	public void setOpenmrsReport(MerIndicatorOpenmrsReport openmrsReport) {
		// TODO using this here set report and indicator properties heres
		this.openmrsReport = openmrsReport;
	}

	@JsonProperty("dhisMeta")
	public MerIndicatorDhisMeta getDhisMeta() {
		return dhisMeta;
	}

	@JsonProperty("dhisMeta")
	public void setDhisMeta(MerIndicatorDhisMeta dhisMeta) {
		this.dhisMeta = dhisMeta;
	}

	@JsonProperty("active")
	public boolean isActive() {
		return active;
	}

	@JsonProperty("active")
	public Boolean getActive() {
		return isActive();
	}

	@JsonProperty("active")
	public void setActive(Boolean active) {
		this.active = active;
	}

}
