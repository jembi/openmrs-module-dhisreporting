package org.openmrs.module.dhisreporting.mer;

import javax.annotation.Generated;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonIgnoreProperties(ignoreUnknown = true)
public class MerIndicatorDisaggregation {
	private String name;
	private String[] ageQueries;
	private String[] sex;
	private String coded;
	private String inherent;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String[] getAgeQueries() {
		return ageQueries;
	}

	public void setAgeQueries(String[] ageQueries) {
		this.ageQueries = ageQueries;
	}

	public String[] getSex() {
		return sex;
	}

	public void setSex(String[] sex) {
		this.sex = sex;
	}

	public String getCoded() {
		return coded;
	}

	public void setCoded(String coded) {
		this.coded = coded;
	}

	public String getInherent() {
		return inherent;
	}

	public void setInherent(String inherent) {
		this.inherent = inherent;
	}

}
