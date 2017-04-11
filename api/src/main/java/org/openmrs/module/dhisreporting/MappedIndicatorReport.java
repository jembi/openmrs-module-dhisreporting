package org.openmrs.module.dhisreporting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.Location;
import org.openmrs.module.dhisreporting.mapping.IndicatorMapping.DisaggregationCategory;
import org.openmrs.module.reporting.report.definition.PeriodIndicatorReportDefinition;

/**
 * Contains a reference to the OpenMRS period indicator report and its mapped
 * indicators (through filterable properties) which includes the report uuid as
 * well
 * 
 * TODO api & ui to manage this object
 * 
 * @author k-joseph
 *
 */
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

	@ManyToOne
	@Column(name = "location_id", nullable = false)
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

	@ManyToOne
	@JoinColumn(name = "report_uuid", nullable = false, referencedColumnName = "uuid")
	private PeriodIndicatorReportDefinition report;

	public MappedIndicatorReport() {
	}

	public MappedIndicatorReport(String dataElementPrefixes, String disaggregationCategories,
			PeriodIndicatorReportDefinition report) {
		setDataElementPrefixes(dataElementPrefixes);
		setDisaggregationCategories(disaggregationCategories);
		setReport(report);
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

	public PeriodIndicatorReportDefinition getReport() {
		return report;
	}

	public void setReport(PeriodIndicatorReportDefinition report) {
		this.report = report;
	}

	public List<String> getDataElementPrefixesAsList() {
		if (StringUtils.isNotBlank(getDataElementPrefixes()))
			return Arrays.asList(getDataElementPrefixes().replaceAll(" ", "").split(","));
		return null;
	}

	public List<DisaggregationCategory> getDisaggregationCategoriesAsList() {
		if (StringUtils.isNotBlank(getDisaggregationCategories())) {
			List<String> dissaggs = Arrays.asList(getDisaggregationCategories().replaceAll(" ", "").split(","));
			List<DisaggregationCategory> ds = new ArrayList<DisaggregationCategory>();

			for (String d : dissaggs) {
				if (StringUtils.isNotBlank(d) && ArrayUtils.contains(DisaggregationCategory.values(), d))
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

	public void setPeriodType(String periodType) {
		this.periodType = periodType;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

}
