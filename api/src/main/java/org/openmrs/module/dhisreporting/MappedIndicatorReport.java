package org.openmrs.module.dhisreporting;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.SerializedObject;
import org.openmrs.module.dhisreporting.mapping.IndicatorMapping;
import org.openmrs.module.dhisreporting.mapping.IndicatorMapping.DisaggregationCategory;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;

import javax.annotation.Generated;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Contains a reference to the OpenMRS period indicator report and its mapped
 * indicators (through filterable properties) which includes the report uuid as
 * well.
 * 
 * Only used for DYNAMIC {@link IndicatorMapping}
 * 
 * TODO api & ui to manage this object
 * 
 * @author k-joseph
 *
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonIgnoreProperties(ignoreUnknown = true)
@Entity
@Table(name = "dhisreporting_mapped_indicator_report")
public class MappedIndicatorReport {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id", nullable = false)
	private Integer id;

	@Column(name = "uuid", unique = true, nullable = false, length = 38)
	private String uuid = UUID.randomUUID().toString();

	@Column(name = "dataset_id", nullable = false)
	private String dataSetId;

	@Column(name = "org_unit_id", nullable = false)
	private String orgUnitId;

	@Column(name = "period_type", nullable = false)
	private String periodType;

	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "location_id", nullable = false)
	private Location location;
	/**
	 * comma separated dataElementPrefixes
	 */
	@Column(name = "data_element_prefixes")
	private String dataElementPrefixes;

	/**
	 * comma separated disaggregationCategories
	 */
	@Column(name = "disaggregation_categories")
	private String disaggregationCategories;

	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "report_id", nullable = false)
	private SerializedObject report;

	@Transient
	private String reportUuid;

	@Transient
	private Integer locationId;

	public Integer getLocationId() {
		return getLocation() != null ? getLocation().getLocationId() : locationId;
	}

	public String getReportUuid() {
		return getReport() != null ? getReport().getUuid() : reportUuid;
	}

	public MappedIndicatorReport() {
	}

	public MappedIndicatorReport(String dataElementPrefixes, String disaggregationCategories, SerializedObject report) {
		setDataElementPrefixes(dataElementPrefixes);
		setDisaggregationCategories(disaggregationCategories);
		setReport(report);
	}

	public MappedIndicatorReport(String dataElementPrefixes, String disaggregationCategories, SerializedObject report,
								 Location location, String periodType, String orgUnitId, String dataSetId) {
		setDataElementPrefixes(dataElementPrefixes);
		setDisaggregationCategories(disaggregationCategories);
		setReport(report);
		setLocation(location);
		setPeriodType(periodType);
		setOrgUnitId(orgUnitId);
		setDataSetId(dataSetId);
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getDataElementPrefixes() {
		return dataElementPrefixes;
	}

	public void setDataElementPrefixes(String dataElementPrefixes) {
		this.dataElementPrefixes = dataElementPrefixes;
	}

	public String getDisaggregationCategories() {
		return disaggregationCategories;
	}

	public void setDisaggregationCategories(String disaggregationCategories) {
		this.disaggregationCategories = disaggregationCategories;
	}

	@JsonIgnore
	public SerializedObject getReport() {
		return report;
	}

	@JsonIgnore
	public ReportDefinition getPeriodIndicatorReport() {
		return getReport() != null ? Context.getService(ReportDefinitionService.class)
				.getDefinitionByUuid(report.getUuid()) : null;
	}

	@JsonIgnore
	public void setReport(SerializedObject report) {
		this.report = report;
	}

	@JsonIgnore
	public List<String> getDataElementPrefixesAsList() {
		if (StringUtils.isNotBlank(getDataElementPrefixes()))
			return Arrays.asList(getDataElementPrefixes().replaceAll(" ", "").split(","));
		return null;
	}

	@JsonIgnore
	public List<DisaggregationCategory> getDisaggregationCategoriesAsList() {
		if (StringUtils.isNotBlank(getDisaggregationCategories())) {
			List<String> dissaggs = Arrays.asList(getDisaggregationCategories().replaceAll(" ", "").split(","));
			List<DisaggregationCategory> ds = new ArrayList<DisaggregationCategory>();

			for (String d : dissaggs) {
				if (StringUtils.isNotBlank(d)
						&& ArrayUtils.contains(DisaggregationCategory.values(), DisaggregationCategory.valueOf(d)))
					ds.add(DisaggregationCategory.valueOf(d));
			}
			return ds;
		}
		return null;
	}

	public String getDataSetId() {
		return dataSetId;
	}

	public void setDataSetId(String dataSetId) {
		this.dataSetId = dataSetId;
	}

	public String getOrgUnitId() {
		return orgUnitId;
	}

	public void setOrgUnitId(String orgUnitId) {
		this.orgUnitId = orgUnitId;
	}

	public String getPeriodType() {
		return periodType;
	}

	private String[] getNames(Class<? extends Enum<?>> e) {
		return Arrays.toString(e.getEnumConstants()).replaceAll("^.|.$", "").split(", ");
	}

	public void setPeriodType(String periodType) {
		this.periodType = periodType;
	}

	@JsonIgnore
	public Location getLocation() {
		return location;
	}

	@JsonIgnore
	public void setLocation(Location location) {
		this.location = location;
	}

}
