/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.dhisreporting.api.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.OpenmrsMetadata;
import org.openmrs.api.PatientSetService.TimeModifier;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.dhisconnector.api.DHISConnectorService;
import org.openmrs.module.dhisconnector.api.model.DHISDataValue;
import org.openmrs.module.dhisconnector.api.model.DHISDataValueSet;
import org.openmrs.module.dhisconnector.api.model.DHISImportSummary;
import org.openmrs.module.dhisconnector.api.model.DHISMapping;
import org.openmrs.module.dhisconnector.api.model.DHISMappingElement;
import org.openmrs.module.dhisreporting.AgeRange;
import org.openmrs.module.dhisreporting.DHISReportingConstants;
import org.openmrs.module.dhisreporting.OpenMRSReportConcepts;
import org.openmrs.module.dhisreporting.OpenMRSToDHISMapping;
import org.openmrs.module.dhisreporting.OpenMRSToDHISMapping.DHISMappingType;
import org.openmrs.module.dhisreporting.api.DHISReportingService;
import org.openmrs.module.dhisreporting.api.db.DHISReportingDAO;
import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.cohort.definition.CodedObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.service.CohortDefinitionService;
import org.openmrs.module.reporting.common.DurationUnit;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetColumn;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.indicator.CohortIndicator.IndicatorType;
import org.openmrs.module.reporting.indicator.service.IndicatorService;
import org.openmrs.module.reporting.report.Report;
import org.openmrs.module.reporting.report.ReportRequest;
import org.openmrs.module.reporting.report.ReportRequest.Priority;
import org.openmrs.module.reporting.report.ReportRequest.Status;
import org.openmrs.module.reporting.report.definition.PeriodIndicatorReportDefinition;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.module.reporting.report.renderer.RenderingMode;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.reporting.web.renderers.DefaultWebRenderer;

/**
 * It is a default implementation of {@link DHISReportingService}.
 */
public class DHISReportingServiceImpl extends BaseOpenmrsService implements DHISReportingService {

	protected final Log log = LogFactory.getLog(this.getClass());

	private DHISReportingDAO dao;

	/**
	 * @param dao
	 *            the dao to set
	 */
	public void setDao(DHISReportingDAO dao) {
		this.dao = dao;
	}

	/**
	 * @return the dao
	 */
	public DHISReportingDAO getDao() {
		return dao;
	}

	/**
	 * 
	 * @param concept
	 * @param location
	 * @param startDate
	 * @param
	 * @return cohort, cohort.size() returns evaluated number of patients
	 *         returned
	 */
	@Override
	public CodedObsCohortDefinition createDHISObsCountCohortQuery(String name, Concept concept) {
		CodedObsCohortDefinition cd = new CodedObsCohortDefinition();

		setBasicOpenMRSObjectProps(cd);
		cd.setTimeModifier(TimeModifier.AVG);
		cd.setName(name);
		cd.setQuestion(concept);
		cd.addParameter(ReportingConstants.START_DATE_PARAMETER);
		cd.addParameter(ReportingConstants.END_DATE_PARAMETER);
		cd.addParameter(ReportingConstants.LOCATION_PARAMETER);
		cd.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		cd.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		cd.addParameter(new Parameter("locationList", "Location", Location.class));

		return Context.getService(CohortDefinitionService.class).saveDefinition(cd);
	}

	@Override
	public CohortIndicator saveNewDHISCohortIndicator(String indicatorName, String indicatorDescription,
			CodedObsCohortDefinition obsCohort) {
		CohortIndicator indicator = new CohortIndicator();
		Map<String, Object> mappings = new HashMap<String, Object>();

		setBasicOpenMRSObjectProps(indicator);
		indicator.getParameters().clear();
		indicator.addParameter(ReportingConstants.START_DATE_PARAMETER);
		indicator.addParameter(ReportingConstants.END_DATE_PARAMETER);
		indicator.addParameter(ReportingConstants.LOCATION_PARAMETER);
		indicator.setName(indicatorName);
		indicator.setDescription(indicatorDescription);
		indicator.setType(IndicatorType.COUNT);
		mappings.put("startDate", "${startDate}");
		mappings.put("endDate", "${endDate}");
		mappings.put("location", "${location}");
		mappings.put("onOrAfter", "${startDate}");
		mappings.put("onOrBefore", "${endDate}");
		mappings.put("locationList", "${location}");

		indicator.setCohortDefinition(obsCohort, mappings);

		return Context.getService(IndicatorService.class).saveDefinition(indicator);
	}

	@Override
	public PeriodIndicatorReportDefinition createNewDHISPeriodReportAndItsDHISConnectorMappingOrUseExisting(
			String reportName, String reportDrescription, List<CohortIndicator> indicators, String uuid,
			String dataSetCode, String dataSetPeriod) {
		PeriodIndicatorReportDefinition report = (PeriodIndicatorReportDefinition) Context
				.getService(ReportDefinitionService.class).getDefinitionByUuid(uuid);

		if (report == null) {
			report = new PeriodIndicatorReportDefinition();
			List<DHISMappingElement> mappings = new ArrayList<DHISMappingElement>();
			DHISMapping mapping = new DHISMapping();

			setBasicOpenMRSObjectProps(report);
			report.setDescription(reportDrescription);
			report.setupDataSetDefinition();
			report.setName(reportName);
			if (StringUtils.isNotBlank(uuid))
				report.setUuid(uuid);

			if (indicators != null) {
				for (CohortIndicator ind : indicators) {
					String code = ind.getName().replaceAll(" ", "").replaceAll("-", "");
					DHISMappingElement map = new DHISMappingElement();
					CodedObsCohortDefinition c = (CodedObsCohortDefinition) ind.getCohortDefinition()
							.getParameterizable();
					String mappedCode = Integer.toString(c.getQuestion().getConceptId());

					map.setIndicator(code);
					map.setDataElement(getValueFromMappings(DHISMappingType.CONCEPTDATAELEMENT + "_" + mappedCode));
					mappings.add(map);
					report.addIndicator(mappedCode, ind.getName(), ind);
				}
			}
			mapping.setName(reportName);
			mapping.setCreated(Calendar.getInstance(Context.getLocale()).getTimeInMillis());
			mapping.setDataSetUID(getValueFromMappings(dataSetCode));
			mapping.setPeriodType(dataSetPeriod);
			mapping.setPeriodIndicatorReportGUID(report.getUuid());
			mapping.setElements(mappings);
			Context.getService(DHISConnectorService.class).saveMapping(mapping);

			return Context.getService(ReportDefinitionService.class).saveDefinition(report);
		}
		return report;
	}

	private void setBasicOpenMRSObjectProps(OpenmrsMetadata omrs) {
		if (omrs != null) {
			omrs.setCreator(Context.getAuthenticatedUser());
			omrs.setRetired(false);
			omrs.setDateCreated(Calendar.getInstance(Context.getLocale()).getTime());
		}
	}

	@Override
	public void createCohortQueriesIndicatorsAndLabReport() {
		if (Context.getService(ReportDefinitionService.class)
				.getDefinitionByUuid(DHISReportingConstants.LAB_REPORT_UUID) == null) {
			createNewLabPeriodReportAndItsDHISConnectorMapping();
		}
	}

	private void createNewLabPeriodReportAndItsDHISConnectorMapping() {
		CohortIndicator bloodSmear = saveNewDHISCohortIndicator("BLOOD SMEAR", "Auto generated BLOOD SMEAR",
				createDHISObsCountCohortQuery("BLOOD SMEAR", OpenMRSReportConcepts.BLOODSMEAR));
		CohortIndicator microFilaria = saveNewDHISCohortIndicator("MICRO-FILARIA", "Auto generated MICRO-FILARIA",
				createDHISObsCountCohortQuery("MICRO-FILARIA", OpenMRSReportConcepts.MICROFILARIA));
		CohortIndicator trypanosoma = saveNewDHISCohortIndicator("TRYPANOSOMA", "Auto generated TRYPANOSOMA",
				createDHISObsCountCohortQuery("TRYPANOSOMA", OpenMRSReportConcepts.TRYPANOSOMA));
		CohortIndicator giardia = saveNewDHISCohortIndicator("GIARDIA", "Auto generated GIARDIA",
				createDHISObsCountCohortQuery("GIARDIA", OpenMRSReportConcepts.GIARDIA));
		CohortIndicator ascariasis = saveNewDHISCohortIndicator("ASCARIASIS", "Auto generated ASCARIASIS",
				createDHISObsCountCohortQuery("ASCARIASIS", OpenMRSReportConcepts.ASCARIASIS));
		CohortIndicator anklyostiasis = saveNewDHISCohortIndicator("ANKLYOSTIASIS", "Auto generated ANKLYOSTIASIS",
				createDHISObsCountCohortQuery("ANKLYOSTIASIS", OpenMRSReportConcepts.ANKLYOSTIASIS));
		CohortIndicator taenia = saveNewDHISCohortIndicator("TAENIA", "Auto generated TAENIA",
				createDHISObsCountCohortQuery("TAENIA", OpenMRSReportConcepts.TAENIA));
		CohortIndicator otherParasites = saveNewDHISCohortIndicator("OTHER PARASITES", "Auto generated OTHER PARASITES",
				createDHISObsCountCohortQuery("OTHER PARASITES", OpenMRSReportConcepts.OTHERPARASITES));
		CohortIndicator pregnantTest = saveNewDHISCohortIndicator("PREGNANCY TEST", "Auto generated PREGNANCY TEST",
				createDHISObsCountCohortQuery("PREGNANCY TEST", OpenMRSReportConcepts.PREGNANCYTEST));
		CohortIndicator hemoglobin = saveNewDHISCohortIndicator("HEMOGLOBIN", "Auto generated HEMOGLOBIN",
				createDHISObsCountCohortQuery("HEMOGLOBIN", OpenMRSReportConcepts.RPR));
		CohortIndicator rpr = saveNewDHISCohortIndicator("RAPID PLASMA REAGENT", "Auto generated RPR",
				createDHISObsCountCohortQuery("RAPID PLASMA REAGENT", OpenMRSReportConcepts.RPR));
		CohortIndicator fullBloodCount = saveNewDHISCohortIndicator("FULL BLOOD COUNT",
				"Auto generated FULL BLOOD COUNT",
				createDHISObsCountCohortQuery("FULL BLOOD COUNT", OpenMRSReportConcepts.FULLBLOODCOUNT));
		CohortIndicator creatine = saveNewDHISCohortIndicator("CREATINE", "Auto generated CREATINE",
				createDHISObsCountCohortQuery("CREATINE", OpenMRSReportConcepts.CREATINE));
		CohortIndicator amylasse = saveNewDHISCohortIndicator("AMYLASSE", "Auto generated AMYLASSE",
				createDHISObsCountCohortQuery("AMYLASSE", OpenMRSReportConcepts.AMYLASSE));
		CohortIndicator cd4Count = saveNewDHISCohortIndicator("CD4 COUNT", "Auto generated CD4 COUNT",
				createDHISObsCountCohortQuery("CD4 COUNT", OpenMRSReportConcepts.CD4COUNT));
		CohortIndicator widal = saveNewDHISCohortIndicator("WIDAL", "Auto generated WIDAL",
				createDHISObsCountCohortQuery("WIDAL", OpenMRSReportConcepts.WIDAL));
		CohortIndicator derebroSpinalFluid = saveNewDHISCohortIndicator("DEREBRO SPINAL FLUID",
				"Auto generated DEREBRO SPINAL FLUID",
				createDHISObsCountCohortQuery("DEREBRO SPINAL FLUID", OpenMRSReportConcepts.DEREBROSPINALFLUID));
		List<CohortIndicator> indicators = new ArrayList<CohortIndicator>();

		indicators.add(bloodSmear);
		indicators.add(microFilaria);
		indicators.add(trypanosoma);
		indicators.add(giardia);
		indicators.add(ascariasis);
		indicators.add(anklyostiasis);
		indicators.add(taenia);
		indicators.add(otherParasites);
		indicators.add(pregnantTest);
		indicators.add(hemoglobin);
		indicators.add(rpr);
		indicators.add(fullBloodCount);
		indicators.add(creatine);
		indicators.add(amylasse);
		indicators.add(cd4Count);
		indicators.add(widal);
		indicators.add(derebroSpinalFluid);

		createNewDHISPeriodReportAndItsDHISConnectorMappingOrUseExisting("HMIS Lab Request",
				"HMIS Auto-generated Lab Request", indicators, DHISReportingConstants.LAB_REPORT_UUID,
				DHISMappingType.DATASET + "_" + "HMIS_LAB_REQUEST", "Monthly");
	}

	@Override
	public void transferDHISMappingsToDataDirectory() {
		if (!DHISReportingConstants.DHISREPORTING_DIRECTORY.exists()) {
			DHISReportingConstants.DHISREPORTING_DIRECTORY.mkdirs();
		}

		File mappingsFile = new File(getClass().getClassLoader()
				.getResource(DHISReportingConstants.DHISREPORTING_MAPPING_FILENAME).getFile());

		try {
			try {
				if (!DHISReportingConstants.DHISREPORTING_FINAL_MAPPINGFILE.exists())
					FileUtils.copyFile(mappingsFile, DHISReportingConstants.DHISREPORTING_FINAL_MAPPINGFILE);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String getValueFromMappings(String code) {
		FileInputStream fis;
		BufferedReader br;
		String objectMappedValue = null;

		try {
			fis = new FileInputStream(DHISReportingConstants.DHISREPORTING_FINAL_MAPPINGFILE);
			br = new BufferedReader(new InputStreamReader(fis));
			String line = null;

			while ((line = br.readLine()) != null) {
				if (line.startsWith(code + "=")) {
					objectMappedValue = line.replace(code + "=", "");
				}
			}

			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return objectMappedValue;
	}

	@Override
	public Report runPeriodIndicatorReport(PeriodIndicatorReportDefinition reportDef, Date startDate, Date endDate,
			Location location) {
		ReportRequest request = new ReportRequest(new Mapped<ReportDefinition>(reportDef, null), null,
				new RenderingMode(new DefaultWebRenderer(), "Web", null, 100), Priority.HIGHEST, null);

		request.getReportDefinition().addParameterMapping("startDate", startDate);
		request.getReportDefinition().addParameterMapping("endDate", endDate);
		request.getReportDefinition().addParameterMapping("location", location);
		request.setStatus(Status.PROCESSING);
		request = Context.getService(ReportService.class).saveReportRequest(request);

		return Context.getService(ReportService.class).runReport(request);
	}

	@Override
	public DHISImportSummary sendReportDataToDHIS(Report report, String dataSetId, String period, String orgUnitId) {
		Map<String, DataSet> dataSets = report.getReportData().getDataSets();
		List<DHISDataValue> dataValues = new ArrayList<DHISDataValue>();

		if (dataSets != null && dataSets.size() > 0 && StringUtils.isNotBlank(period)
				&& StringUtils.isNotBlank(orgUnitId)) {
			DHISDataValueSet dataValueSet = new DHISDataValueSet();
			DataSet ds = dataSets.get("defaultDataSet");
			List<DataSetColumn> columns = ds.getMetaData().getColumns();

			DataSetRow row = ds.iterator().next();

			for (int i = 0; i < columns.size(); i++) {
				DHISDataValue dv = new DHISDataValue();
				String column = columns.get(i).getName();

				dv.setValue(row.getColumnValue(column).toString());
				dv.setDataElement(getValueFromMappings(DHISMappingType.CONCEPTDATAELEMENT + "_" + column));
				dataValues.add(dv);
			}
			dataValueSet.setDataValues(dataValues);
			dataValueSet.setOrgUnit(orgUnitId);
			dataValueSet.setPeriod(period);
			dataValueSet.setDataSet(dataSetId);

			return Context.getService(DHISConnectorService.class).postDataValueSet(dataValueSet);
		}
		return null;
	}

	@Override
	public DHISImportSummary runAndSendReportDataForTheCurrentMonth() {
		Location defaultLocation = Context.getLocationService().getLocation(Integer.parseInt(
				Context.getAdministrationService().getGlobalProperty(DHISReportingConstants.DEFAULT_LOCATION_ID)));

		Calendar startDate = Calendar.getInstance(Context.getLocale());
		Date endDate = new Date();
		PeriodIndicatorReportDefinition labReportDef = (PeriodIndicatorReportDefinition) Context
				.getService(ReportDefinitionService.class).getDefinitionByUuid(DHISReportingConstants.LAB_REPORT_UUID);

		startDate.set(Calendar.DAY_OF_MONTH, 1);
		if (defaultLocation != null && labReportDef != null) {
			Report labReport = runPeriodIndicatorReport(labReportDef, startDate.getTime(), endDate, defaultLocation);
			String dataSetId = getValueFromMappings(DHISMappingType.DATASET + "_" + "HMIS_LAB_REQUEST");
			String orgUnitId = getValueFromMappings(DHISMappingType.LOCATION + "_" + Context.getAdministrationService()
					.getGlobalProperty(DHISReportingConstants.CONFIGURED_ORGUNIT_CODE));
			String period = new SimpleDateFormat("yyyyMM").format(new Date());

			return sendReportDataToDHIS(labReport, dataSetId, period, orgUnitId);
		}
		return null;
	}

	@Override
	public OpenMRSToDHISMapping getMapping(String openmrsIdOrCode, DHISMappingType mappingType) {
		OpenMRSToDHISMapping mapping = new OpenMRSToDHISMapping();

		mapping.setOpenmrsId(openmrsIdOrCode);
		mapping.setType(mappingType.name());
		mapping.setDhisId(getValueFromMappings(mappingType + "_" + openmrsIdOrCode));
		if (StringUtils.isNotBlank(mapping.getOpenmrsId()) && StringUtils.isNotBlank(mapping.getDhisId()))
			return mapping;
		return null;
	}

	@Override
	public List<OpenMRSToDHISMapping> getAllMappings() {
		List<OpenMRSToDHISMapping> mappings = new ArrayList<OpenMRSToDHISMapping>();

		FileInputStream fis;
		BufferedReader br;

		try {
			fis = new FileInputStream(DHISReportingConstants.DHISREPORTING_FINAL_MAPPINGFILE);
			br = new BufferedReader(new InputStreamReader(fis));
			String line = null;

			while ((line = br.readLine()) != null) {
				OpenMRSToDHISMapping map = new OpenMRSToDHISMapping();

				if (line.matches("(" + DHISMappingType.DATASET + "|" + DHISMappingType.CONCEPTDATAELEMENT + "|"
						+ DHISMappingType.LOCATION + ").*")) {
					map.setType(line.split("_")[0]);
					map.setOpenmrsId(concateLines(line.split("=")[0].split("_"), "_", 0));
					map.setDhisId(line.split("=")[1]);
					mappings.add(map);
				}
			}

			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return mappings;
	}

	private String concateLines(String[] s, String separator, int skipIndex) {
		String result = "";
		if (s.length > 0) {
			for (int i = 0; i < s.length; i++) {
				if (i != skipIndex) {
					result = result + separator + s[i];
				}
			}
		}
		return result = result.replaceFirst(separator, "");
	}

	/**
	 * @param code,
	 *            includes type and openmrsid
	 * @return
	 */
	@Override
	public OpenMRSToDHISMapping getMappingFromMappings(String code, DHISMappingType type) {
		OpenMRSToDHISMapping map = null;
		FileInputStream fis;
		BufferedReader br;

		try {
			fis = new FileInputStream(DHISReportingConstants.DHISREPORTING_FINAL_MAPPINGFILE);
			br = new BufferedReader(new InputStreamReader(fis));
			String line = null;

			while ((line = br.readLine()) != null) {
				if (line.startsWith(type + code + "=")) {
					map = new OpenMRSToDHISMapping();

					map.setType(line.split("_")[0]);
					map.setOpenmrsId(code);
					map.setDhisId(line.replace(type + code + "=", ""));
				}
			}

			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return map;
	}

	@Override
	public OpenMRSToDHISMapping saveOrUpdateMapping(OpenMRSToDHISMapping mapping) {
		FileInputStream fis;
		BufferedReader br;
		String newContent = "";
		boolean editing = false;

		if (mapping != null && StringUtils.isNotBlank(mapping.getType())
				&& StringUtils.isNotBlank(mapping.getOpenmrsId()) && StringUtils.isNotBlank(mapping.getDhisId())) {
			try {
				fis = new FileInputStream(DHISReportingConstants.DHISREPORTING_FINAL_MAPPINGFILE);
				br = new BufferedReader(new InputStreamReader(fis));
				String line = null;

				// EDITING
				while ((line = br.readLine()) != null) {
					if (line.startsWith(mapping.getType() + "_" + mapping.getOpenmrsId() + "=")) {
						editing = true;
						newContent += mapping.getType() + "_" + mapping.getOpenmrsId() + "=" + mapping.getDhisId()
								+ "\n";
					} else {
						newContent += line + "\n";
					}
				}

				// ADDING NEW MAPPING
				if (editing == false) {
					newContent = "";
					fis = new FileInputStream(DHISReportingConstants.DHISREPORTING_FINAL_MAPPINGFILE);
					br = new BufferedReader(new InputStreamReader(fis));
					while ((line = br.readLine()) != null) {
						String beforeLine = "##ending " + mapping.getType() + " mappings";
						if (line.equals(beforeLine)) {
							newContent += mapping.getType() + "_" + mapping.getOpenmrsId() + "=" + mapping.getDhisId()
									+ "\n" + beforeLine + "\n";
						} else {
							newContent += line + "\n";
						}
					}
				}
				newContent = replaceLast(newContent, "\n", "");
				writeContentToFile(newContent, DHISReportingConstants.DHISREPORTING_FINAL_MAPPINGFILE);
				br.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return mapping;
	}

	private String replaceLast(String string, String toBeReplaced, String replaceWith) {
		if (StringUtils.isNotBlank(string)) {
			StringBuffer sb = new StringBuffer(string);
			sb.replace(string.lastIndexOf(toBeReplaced), string.lastIndexOf(toBeReplaced) + 1, replaceWith);
			return sb.toString();
		} else
			return null;
	}

	@Override
	public void deleteMapping(OpenMRSToDHISMapping mapping) {
		FileInputStream fis;
		BufferedReader br;
		String newContent = "";
		if (mapping != null && StringUtils.isNotBlank(mapping.getType())
				&& StringUtils.isNotBlank(mapping.getOpenmrsId()) && StringUtils.isNotBlank(mapping.getDhisId())) {
			try {
				fis = new FileInputStream(DHISReportingConstants.DHISREPORTING_FINAL_MAPPINGFILE);
				br = new BufferedReader(new InputStreamReader(fis));
				String line = null;

				while ((line = br.readLine()) != null) {
					// SKIP/DELETE existing mapping entry
					if (!line.equals(mapping.getType() + "_" + mapping.getOpenmrsId() + "=" + mapping.getDhisId())) {
						newContent += line + "\n";
					}
				}
				newContent = replaceLast(newContent, "\n", "");
				writeContentToFile(newContent, DHISReportingConstants.DHISREPORTING_TEMP_MAPPINGFILE);
				FileUtils.copyFile(DHISReportingConstants.DHISREPORTING_TEMP_MAPPINGFILE,
						DHISReportingConstants.DHISREPORTING_FINAL_MAPPINGFILE);
				DHISReportingConstants.DHISREPORTING_TEMP_MAPPINGFILE.delete();
				br.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void writeContentToFile(String content, File file) {
		if (StringUtils.isNotBlank(content) && file != null) {
			try {
				if (!file.exists()) {
					file.createNewFile();
				}

				FileWriter fw = new FileWriter(file.getAbsoluteFile());
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(content);
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 
	 * @param ageQuery,
	 *            examples include;15:19, 20:24, 25:49, >=50, <1
	 * @return
	 */
	@Override
	public AgeRange convertAgeQueryToAgeRangeObject(String ageQuery, DurationUnit ageUnit) {
		AgeRange ageRange = null;
		if (StringUtils.isNotBlank(ageQuery)) {
			ageRange = new AgeRange();

			ageRange.setMinAgeUnit(ageUnit);
			ageRange.setMaxAgeUnit(ageUnit);

			if (ageQuery.indexOf("<=") >= 0) {
				ageRange.setMinAge(0);
				ageRange.setMaxAge(Integer.parseInt(ageQuery.split("<=")[1]));
			} else if (ageQuery.indexOf(">=") >= 0) {
				ageRange.setMinAge(Integer.parseInt(ageQuery.split(">=")[1]));
			} else if (ageQuery.indexOf("<") >= 0) {
				ageRange.setMinAge(0);
				ageRange.setMaxAge(Integer.parseInt(ageQuery.split("<")[1]) - 1);
			} else if (ageQuery.indexOf(">") >= 0) {
				ageRange.setMinAge(Integer.parseInt(ageQuery.split(">")[1]) + 1);
			} else if (ageQuery.indexOf(":") >= 0) {
				ageRange.setMinAge(Integer.parseInt(ageQuery.split(":")[0]));
				ageRange.setMaxAge(Integer.parseInt(ageQuery.split(":")[1]));
			} else {
				ageRange = null;
			}
		}

		return ageRange;
	}
}