package org.openmrs.module.dhisreporting.mer;

import javax.annotation.Generated;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonIgnoreProperties(ignoreUnknown = true)
public class MerIndicatorDhisMeta {
	private String dataElement;
	private String categoryCombo;
	private String orgUnit;
	private String dataSet;

	public String getDataElement() {
		return dataElement;
	}

	public void setDataElement(String dataElement) {
		this.dataElement = dataElement;
	}

	public String getCategoryCombo() {
		return categoryCombo;
	}

	public void setCategoryCombo(String categoryCombo) {
		this.categoryCombo = categoryCombo;
	}

	public String getOrgUnit() {
		return orgUnit;
	}

	public void setOrgUnit(String orgUnit) {
		this.orgUnit = orgUnit;
	}

	public String getDataSet() {
		return dataSet;
	}

	public void setDataSet(String dataSet) {
		this.dataSet = dataSet;
	}

}
