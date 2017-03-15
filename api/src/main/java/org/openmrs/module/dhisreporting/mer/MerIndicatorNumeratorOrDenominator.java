package org.openmrs.module.dhisreporting.mer;

import javax.annotation.Generated;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonIgnoreProperties(ignoreUnknown = true)
public class MerIndicatorNumeratorOrDenominator {
	private String name;

	private String cohortUuid;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCohortUuid() {
		return cohortUuid;
	}

	public void setCohortUuid(String cohortUuid) {
		this.cohortUuid = cohortUuid;
	}

}
