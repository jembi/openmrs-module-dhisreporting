package org.openmrs.module.dhisreporting;

import javax.annotation.Generated;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.json.simple.JSONObject;
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
@JsonPropertyOrder({ "indicatorCode", "indicatorName", "indicatorDescription", "numerator", "denominator",
		/*"aggregation", "disaggregation", "openmrsReportRefs"*/ })
@JsonIgnoreProperties(ignoreUnknown = true)
public class MerIndicator {

	@JsonProperty("indicatorName")
	private String indicatorName;

	@JsonProperty("indicatorDescription")
	private String indicatorDescription;

	@JsonProperty("indicatorCode")
	private String indicatorCode;

	/**
	 * numerator, denominator are text descriptions of what should be part of
	 * the indicator
	 */
	@JsonProperty("numerator")
	private String numerator;

	@JsonProperty("denominator")
	private String denominator;

	@JsonProperty("aggregation")
	private JSONObject aggregation;

	@JsonProperty("disaggregation")
	private JSONObject disaggregation;

	@JsonProperty("openmrsReportRefs")
	private JSONObject openmrsReportRefs;

	/**
	 * Required
	 */
	private CohortIndicator indicator;

	private PeriodIndicatorReportDefinition report;

	enum ReportingPeriod {
		DAILY, MONTHLY, QUARTERY, SEMIANNUALLY, ANNUALLY
	}

	@JsonProperty("indicatorName")
	public String getIndicatorName() {
		return indicatorName;
	}

	@JsonProperty("indicatorName")
	public void setIndicatorName(String indicatorName) {
		this.indicatorName = indicatorName;
	}

	@JsonProperty("indicatorDescription")
	public String getIndicatorDescription() {
		return indicatorDescription;
	}

	@JsonProperty("indicatorDescription")
	public void setIndicatorDescription(String indicatorDescription) {
		this.indicatorDescription = indicatorDescription;
	}

	@JsonProperty("indicatorCode")
	public String getIndicatorCode() {
		return indicatorCode;
	}

	@JsonProperty("indicatorCode")
	public void setIndicatorCode(String indicatorCode) {
		this.indicatorCode = indicatorCode;
	}

	@JsonProperty("numerator")
	public String getNumerator() {
		return numerator;
	}

	@JsonProperty("numerator")
	public void setNumerator(String numerator) {
		this.numerator = numerator;
	}

	@JsonProperty("denominator")
	public String getDenominator() {
		return denominator;
	}

	@JsonProperty("denominator")
	public void setDenominator(String denominator) {
		this.denominator = denominator;
	}

	@JsonProperty("aggregation")
	public JSONObject getAggregation() {
		return aggregation;
	}

	@JsonProperty("aggregation")
	public void setAggregation(JSONObject aggregation) {
		// TODO using this here set report and indicator properties heres
		this.aggregation = aggregation;
	}

	@JsonProperty("disaggregation")
	public JSONObject getDisaggregation() {
		return disaggregation;
	}

	@JsonProperty("disaggregation")
	public void setDisaggregation(JSONObject disaggregation) {
		// TODO using this here set report and indicator properties heres
		this.disaggregation = disaggregation;
	}

	public CohortIndicator getIndicator() {
		return indicator;
	}

	public PeriodIndicatorReportDefinition getReport() {
		return report;
	}

	public JSONObject getOpenmrsReportRefs() {
		return openmrsReportRefs;
	}

	public void setOpenmrsReportRefs(JSONObject openmrsReportRefs) {
		// TODO using this here set report and indicator properties heres
		this.openmrsReportRefs = openmrsReportRefs;
	}

}
