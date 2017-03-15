package org.openmrs.module.dhisreporting.mer;

import javax.annotation.Generated;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonIgnoreProperties(ignoreUnknown = true)
public class MerIndicatorOpenmrsReport {
	private String reportUuid;
	private String category;
	private String period;
	private String cohortUuid;

	public String getReportUuid() {
		return reportUuid;
	}

	public void setReportUuid(String reportUuid) {
		this.reportUuid = reportUuid;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getPeriod() {
		return period;
	}

	public void setPeriod(String period) {
		this.period = period;
	}

	public String getCohortUuid() {
		return cohortUuid;
	}

	public void setCohortUuid(String cohortUuid) {
		this.cohortUuid = cohortUuid;
	}

}
