package org.openmrs.module.dhisreporting.mapping;

import javax.annotation.Generated;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

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
	private String dataelementName;
	private String dataelementId;
	private String dataelementCode;
	private String categoryoptioncomboName;
	private String categoryoptioncomboCode;
	private String categoryoptioncomboUid;
	private String dataset;
	private DisaggregationCategory disaggregationCategory;
	private Boolean active;
	private String activeString;

	/**
	 * USED and should be mapped through GP/Settings and match what's in the csv
	 * mapping while
	 */
	private String openmrsReportUuid;
	/**
	 * NOT USED yet
	 */
	private String openmrsNumeratorCohortUuid;
	/**
	 * NOT USED yet
	 */
	private String openmrsDenominatorCohortUuid;
	private String inherentDisaggOrder;
	private String reportingPeriodType;

	/**
	 * question concept id for the coded disaggregation category
	 */
	@JsonSerialize(include = Inclusion.NON_NULL)
	private Integer codedDisaggQuestion;

	private IndicatorMappingCategory category;

	public enum IndicatorMappingCategory {
		INBUILT, DYNAMIC
	}

	/**
	 * All blank disaggregation categories or categoryoptioncomboName should
	 * rather be marked as NULL
	 * 
	 * @author k-joseph
	 *
	 */
	public enum DisaggregationCategory {
		AGE, DEFAULT, NULL, CODED, INHERENT, GENDER, OTHER;
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

	public String getCategoryoptioncomboName() {
		return categoryoptioncomboName;
	}

	public void setCategoryoptioncomboName(String categoryoptioncomboName) {
		this.categoryoptioncomboName = categoryoptioncomboName;
	}

	public String getDataelementName() {
		return dataelementName;
	}

	public void setDataelementName(String dataelementName) {
		this.dataelementName = dataelementName;
	}

	public String getCategoryoptioncomboCode() {
		return categoryoptioncomboCode;
	}

	public void setCategoryoptioncomboCode(String categoryoptioncomboCode) {
		this.categoryoptioncomboCode = categoryoptioncomboCode;
	}

	public String getCategoryoptioncomboUid() {
		return categoryoptioncomboUid;
	}

	public void setCategoryoptioncomboUid(String categoryoptioncomboUid) {
		this.categoryoptioncomboUid = categoryoptioncomboUid;
	}

	public String getDataelementCode() {
		return dataelementCode;
	}

	public void setDataelementCode(String dataelementCode) {
		this.dataelementCode = dataelementCode;
	}

	public String getActiveString() {
		return activeString;
	}

	public void setActiveString(String activeString) {
		this.activeString = activeString;
		if (StringUtils.isNotBlank(activeString))
			setActive("TRUE".equalsIgnoreCase(activeString));
	}

	public Boolean isActive() {
		return getActive() != null ? getActive() : false;
	}

	public String getOpenmrsNumeratorCohortUuid() {
		return openmrsNumeratorCohortUuid;
	}

	public void setOpenmrsNumeratorCohortUuid(String openmrsNumeratorCohortUuid) {
		this.openmrsNumeratorCohortUuid = openmrsNumeratorCohortUuid;
	}

	public String getOpenmrsDenominatorCohortUuid() {
		return openmrsDenominatorCohortUuid;
	}

	public void setOpenmrsDenominatorCohortUuid(String openmrsDenominatorCohortUuid) {
		this.openmrsDenominatorCohortUuid = openmrsDenominatorCohortUuid;
	}

	public String getInherentDisaggOrder() {
		return inherentDisaggOrder;
	}

	public String getReportingPeriodType() {
		return reportingPeriodType;
	}

	public void setReportingPeriodType(String reportingPeriodType) {
		this.reportingPeriodType = reportingPeriodType;
	}

	public void setInherentDisaggOrder(String inherentDisaggOrder) {
		this.inherentDisaggOrder = inherentDisaggOrder;
	}

	public String getDataelementId() {
		return dataelementId;
	}

	public void setDataelementId(String dataelementId) {
		this.dataelementId = dataelementId;
	}

	public Integer getCodedDisaggQuestion() {
		return codedDisaggQuestion;
	}

	public void setCodedDisaggQuestion(Integer codedDisaggQuestion) {
		this.codedDisaggQuestion = codedDisaggQuestion;
	}

	public IndicatorMappingCategory getCategory() {
		return category;
	}

	public void setCategory(IndicatorMappingCategory category) {
		this.category = category;
	}

}
