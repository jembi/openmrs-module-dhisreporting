package org.openmrs.module.dhisreporting;

import javax.annotation.Generated;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({ "dhisId", "openmrsId" })
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenMRSToDHISMapping {

	@JsonProperty("openmrsId")
	private String openmrsId;

	@JsonProperty("dhisId")
	private String dhisId;

	public enum DHISMappingType {
		INDICATOR_, DATASET_, LOCATION_
	}

	@JsonProperty("openmrsId")
	public String getOpenmrsId() {
		return openmrsId;
	}

	@JsonProperty("openmrsId")
	public void setOpenmrsId(String openmrsId) {
		this.openmrsId = openmrsId;
	}

	@JsonProperty("dhisId")
	public String getDhisId() {
		return dhisId;
	}

	@JsonProperty("dhisId")
	public void setDhisId(String dhisId) {
		this.dhisId = dhisId;
	}
}
