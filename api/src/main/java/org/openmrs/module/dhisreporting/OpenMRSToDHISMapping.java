package org.openmrs.module.dhisreporting;

import javax.annotation.Generated;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.openmrs.api.context.Context;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({ "dhisId", "openmrsId", "type" })
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenMRSToDHISMapping {
	@JsonProperty("openmrsId")
	private String openmrsId;

	@JsonProperty("dhisId")
	private String dhisId;

	@JsonProperty("type")
	private String type;

	private String openmrsName;

	private String dhisName;

	public enum DHISMappingType {
		CONCEPTDATAELEMENT, DATASET, LOCATION
	}

	@JsonProperty("openmrsId")
	public String getOpenmrsId() {
		return openmrsId;
	}

	@JsonProperty("type")
	public String getType() {
		return type;
	}

	@JsonProperty("type")
	public void setType(String type) {
		this.type = type;
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

	public String getOpenmrsName() {
		if (StringUtils.isNotBlank(getOpenmrsId()) && DHISMappingType.CONCEPTDATAELEMENT.name().equals(getType())) {
			return Context.getConceptService().getConcept(Integer.parseInt(getOpenmrsId())).getName().getName();
		}
		return openmrsName;
	}

	public String getDhisName() {
		return dhisName;
	}
}
