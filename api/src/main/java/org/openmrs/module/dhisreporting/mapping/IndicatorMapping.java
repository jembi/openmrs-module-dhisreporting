package org.openmrs.module.dhisreporting.mapping;

import javax.annotation.Generated;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * Represents a general OpenMRS to DHIS 2 Indicator/dataelement mapping starting
 * with PEPFAR Mer 2.0 indicator
 * 
 * @author k-joseph
 *
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonIgnoreProperties(ignoreUnknown = true)
public class IndicatorMapping {
	private String dataelement_name;
	private String dataelement_code;
	private String categoryoptioncombo_name;
	private String categoryoptioncombo_code;
	private String categoryoptioncombo_uid;
	private String dataset;
	private DisaggregationCategory disaggregationCategory;
	private Boolean active;
	private String openmrsReportUuid;

	public enum DisaggregationCategory {
		AGE, DEFAULT, NULL, CODED, INHERENT, GENDER
	}

	public String getDataelement_name() {
		return dataelement_name;
	}

	public void setDataelement_name(String dataelement_name) {
		this.dataelement_name = dataelement_name;
	}

	public String getDataelement_code() {
		return dataelement_code;
	}

	public void setDataelement_code(String dataelement_code) {
		this.dataelement_code = dataelement_code;
	}

	public String getCategoryoptioncombo_name() {
		return categoryoptioncombo_name;
	}

	public void setCategoryoptioncombo_name(String categoryoptioncombo_name) {
		this.categoryoptioncombo_name = categoryoptioncombo_name;
	}

	public String getCategoryoptioncombo_code() {
		return categoryoptioncombo_code;
	}

	public void setCategoryoptioncombo_code(String categoryoptioncombo_code) {
		this.categoryoptioncombo_code = categoryoptioncombo_code;
	}

	public String getCategoryoptioncombo_uid() {
		return categoryoptioncombo_uid;
	}

	public void setCategoryoptioncombo_uid(String categoryoptioncombo_uid) {
		this.categoryoptioncombo_uid = categoryoptioncombo_uid;
	}

	public String getDataset() {
		return dataset;
	}

	public void setDataset(String dataset) {
		this.dataset = dataset;
	}

	public DisaggregationCategory getDisaggregationCategory() {
		return disaggregationCategory;
	}

	public void setDisaggregationCategory(DisaggregationCategory disaggregationCategory) {
		this.disaggregationCategory = disaggregationCategory;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public String getOpenmrsReportUuid() {
		return openmrsReportUuid;
	}

	public void setOpenmrsReportUuid(String openmrsReportUuid) {
		this.openmrsReportUuid = openmrsReportUuid;
	}

}
