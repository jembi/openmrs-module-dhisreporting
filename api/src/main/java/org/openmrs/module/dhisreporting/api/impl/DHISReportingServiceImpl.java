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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.TypeFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.OpenmrsMetadata;
import org.openmrs.api.PatientSetService.TimeModifier;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.dhisconnector.api.DHISConnectorService;
import org.openmrs.module.dhisconnector.api.model.DHISCategoryCombo;
import org.openmrs.module.dhisconnector.api.model.DHISCategoryOptionCombo;
import org.openmrs.module.dhisconnector.api.model.DHISDataElement;
import org.openmrs.module.dhisconnector.api.model.DHISDataSet;
import org.openmrs.module.dhisconnector.api.model.DHISDataValue;
import org.openmrs.module.dhisconnector.api.model.DHISDataValueSet;
import org.openmrs.module.dhisconnector.api.model.DHISMapping;
import org.openmrs.module.dhisconnector.api.model.DHISMappingElement;
import org.openmrs.module.dhisreporting.AgeRange;
import org.openmrs.module.dhisreporting.Configurations;
import org.openmrs.module.dhisreporting.DHISReportingConstants;
import org.openmrs.module.dhisreporting.MappedIndicatorReport;
import org.openmrs.module.dhisreporting.OpenMRSReportConcepts;
import org.openmrs.module.dhisreporting.OpenMRSToDHISMapping;
import org.openmrs.module.dhisreporting.OpenMRSToDHISMapping.DHISMappingType;
import org.openmrs.module.dhisreporting.ReportingPeriodType;
import org.openmrs.module.dhisreporting.WordToNumber;
import org.openmrs.module.dhisreporting.api.DHISReportingService;
import org.openmrs.module.dhisreporting.api.db.DHISReportingDAO;
import org.openmrs.module.dhisreporting.mapping.CodedDisaggregation;
import org.openmrs.module.dhisreporting.mapping.IndicatorMapping;
import org.openmrs.module.dhisreporting.mapping.IndicatorMapping.BaseCohort;
import org.openmrs.module.dhisreporting.mapping.IndicatorMapping.DisaggregationCategory;
import org.openmrs.module.dhisreporting.mapping.IndicatorMapping.IndicatorMappingCategory;
import org.openmrs.module.dhisreporting.mer.MerIndicator;
import org.openmrs.module.dhisreporting.reporting.MappedCohortIndicator;
import org.openmrs.module.dhisreporting.reporting.PatientCohorts;
import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.cohort.definition.CodedObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.service.CohortDefinitionService;
import org.openmrs.module.reporting.common.DurationUnit;
import org.openmrs.module.reporting.common.SetComparator;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetColumn;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition.CohortIndicatorAndDimensionColumn;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.indicator.CohortIndicator.IndicatorType;
import org.openmrs.module.reporting.indicator.Indicator;
import org.openmrs.module.reporting.indicator.dimension.CohortDefinitionDimension;
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
import org.openmrs.web.WebConstants;

import liquibase.util.csv.opencsv.CSVReader;

/**
 * It is a default implementation of {@link DHISReportingService}.
 */
public class DHISReportingServiceImpl extends BaseOpenmrsService implements DHISReportingService {

	protected final Log log = LogFactory.getLog(this.getClass());

	private DHISReportingDAO dao;

	private Configurations configs = new Configurations();

	private PatientCohorts cohorts = new PatientCohorts();

	public static final String DATASETS_PATH = "/api/dataSets/";

	private String DATA_ELEMETS_PATH = "/api/dataElements/";

	private String CATEGORY_COMBOS_PATH = "/api/categoryCombos/";

	private String OPTION_CATEGORY_COMBOS_PATH = "/api/categoryOptionCombos/";

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

		return Context.getService(CohortDefinitionService.class).saveDefinition(cd);
	}

	@Override
	public CohortIndicator saveNewDHISCohortIndicator(String indicatorName, String indicatorDescription,
			CohortDefinition cohort, IndicatorType indicatorType, String indicatorUuid) {
		CohortIndicator indicator = new CohortIndicator();
		Map<String, Object> mappings = new HashMap<String, Object>();

		setBasicOpenMRSObjectProps(indicator);
		indicator.getParameters().clear();
		indicator.addParameter(ReportingConstants.START_DATE_PARAMETER);
		indicator.addParameter(ReportingConstants.END_DATE_PARAMETER);
		indicator.addParameter(ReportingConstants.LOCATION_PARAMETER);
		indicator.setName(indicatorName);
		if (StringUtils.isNotBlank(indicatorUuid))
			indicator.setUuid(indicatorUuid);
		indicator.setDescription(indicatorDescription);
		indicator.setType(indicatorType);
		mappings.put("startDate", "${startDate}");
		mappings.put("endDate", "${endDate}");
		mappings.put("location", "${location}");

		indicator.setCohortDefinition(cohort, mappings);

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
					if (c != null && c.getQuestion() != null) {
						String mappedCode = Integer.toString(c.getQuestion().getConceptId());
						map.setIndicator(code);
						map.setDataElement(getValueFromMappings(DHISMappingType.CONCEPTDATAELEMENT + "_" + mappedCode));
						mappings.add(map);
						report.addIndicator(mappedCode, ind.getName(), ind);
					}
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
	public void createCohortQueriesIndicatorsAndReports() {
		/*
		 * mappings trying to use the same mappings list to save memory of
		 * loading it from csv every time while setting up another report
		 */

		/*
		 * if (Context.getService(ReportDefinitionService.class)
		 * .getDefinitionByUuid(DHISReportingConstants.LAB_REPORT_UUID) == null)
		 * { createNewLabPeriodReportAndItsDHISConnectorMapping(); }
		 */
		if (Context.getService(ReportDefinitionService.class).getDefinitionByUuid(Context.getAdministrationService()
				.getGlobalProperty(DHISReportingConstants.REPORT_UUID_ONART)) == null) {
			createNewPeriodIndicatorONARTReportFromInBuiltIndicatorMappings();

		}
		if (Context.getService(ReportDefinitionService.class).getDefinitionByUuid(Context.getAdministrationService()
				.getGlobalProperty(DHISReportingConstants.REPORT_UUID_HIVSTATUS)) == null) {
			createNewPeriodIndicatorHIVStatusReportFromInBuiltIndicatorMappings();

		}
	}

	@SuppressWarnings("unused")
	private void createNewLabPeriodReportAndItsDHISConnectorMapping() {
		CohortIndicator bloodSmear = saveNewDHISCohortIndicator("BLOOD SMEAR", "Auto generated BLOOD SMEAR",
				createDHISObsCountCohortQuery("BLOOD SMEAR", OpenMRSReportConcepts.BLOODSMEAR), IndicatorType.COUNT,
				null);
		CohortIndicator microFilaria = saveNewDHISCohortIndicator("MICRO-FILARIA", "Auto generated MICRO-FILARIA",
				createDHISObsCountCohortQuery("MICRO-FILARIA", OpenMRSReportConcepts.MICROFILARIA), IndicatorType.COUNT,
				null);
		CohortIndicator trypanosoma = saveNewDHISCohortIndicator("TRYPANOSOMA", "Auto generated TRYPANOSOMA",
				createDHISObsCountCohortQuery("TRYPANOSOMA", OpenMRSReportConcepts.TRYPANOSOMA), IndicatorType.COUNT,
				null);
		CohortIndicator giardia = saveNewDHISCohortIndicator("GIARDIA", "Auto generated GIARDIA",
				createDHISObsCountCohortQuery("GIARDIA", OpenMRSReportConcepts.GIARDIA), IndicatorType.COUNT, null);
		CohortIndicator ascariasis = saveNewDHISCohortIndicator("ASCARIASIS", "Auto generated ASCARIASIS",
				createDHISObsCountCohortQuery("ASCARIASIS", OpenMRSReportConcepts.ASCARIASIS), IndicatorType.COUNT,
				null);
		CohortIndicator anklyostiasis = saveNewDHISCohortIndicator("ANKLYOSTIASIS", "Auto generated ANKLYOSTIASIS",
				createDHISObsCountCohortQuery("ANKLYOSTIASIS", OpenMRSReportConcepts.ANKLYOSTIASIS),
				IndicatorType.COUNT, null);
		CohortIndicator taenia = saveNewDHISCohortIndicator("TAENIA", "Auto generated TAENIA",
				createDHISObsCountCohortQuery("TAENIA", OpenMRSReportConcepts.TAENIA), IndicatorType.COUNT, null);
		CohortIndicator otherParasites = saveNewDHISCohortIndicator("OTHER PARASITES", "Auto generated OTHER PARASITES",
				createDHISObsCountCohortQuery("OTHER PARASITES", OpenMRSReportConcepts.OTHERPARASITES),
				IndicatorType.COUNT, null);
		CohortIndicator pregnantTest = saveNewDHISCohortIndicator("PREGNANCY TEST", "Auto generated PREGNANCY TEST",
				createDHISObsCountCohortQuery("PREGNANCY TEST", OpenMRSReportConcepts.PREGNANCYTEST),
				IndicatorType.COUNT, null);
		CohortIndicator hemoglobin = saveNewDHISCohortIndicator("HEMOGLOBIN", "Auto generated HEMOGLOBIN",
				createDHISObsCountCohortQuery("HEMOGLOBIN", OpenMRSReportConcepts.RPR), IndicatorType.COUNT, null);
		CohortIndicator rpr = saveNewDHISCohortIndicator("RAPID PLASMA REAGENT", "Auto generated RPR",
				createDHISObsCountCohortQuery("RAPID PLASMA REAGENT", OpenMRSReportConcepts.RPR), IndicatorType.COUNT,
				null);
		CohortIndicator fullBloodCount = saveNewDHISCohortIndicator("FULL BLOOD COUNT",
				"Auto generated FULL BLOOD COUNT",
				createDHISObsCountCohortQuery("FULL BLOOD COUNT", OpenMRSReportConcepts.FULLBLOODCOUNT),
				IndicatorType.COUNT, null);
		CohortIndicator creatine = saveNewDHISCohortIndicator("CREATINE", "Auto generated CREATINE",
				createDHISObsCountCohortQuery("CREATINE", OpenMRSReportConcepts.CREATINE), IndicatorType.COUNT, null);
		CohortIndicator amylasse = saveNewDHISCohortIndicator("AMYLASSE", "Auto generated AMYLASSE",
				createDHISObsCountCohortQuery("AMYLASSE", OpenMRSReportConcepts.AMYLASSE), IndicatorType.COUNT, null);
		CohortIndicator cd4Count = saveNewDHISCohortIndicator("CD4 COUNT", "Auto generated CD4 COUNT",
				createDHISObsCountCohortQuery("CD4 COUNT", OpenMRSReportConcepts.CD4COUNT), IndicatorType.COUNT, null);
		CohortIndicator widal = saveNewDHISCohortIndicator("WIDAL", "Auto generated WIDAL",
				createDHISObsCountCohortQuery("WIDAL", OpenMRSReportConcepts.WIDAL), IndicatorType.COUNT, null);
		CohortIndicator derebroSpinalFluid = saveNewDHISCohortIndicator("DEREBRO SPINAL FLUID",
				"Auto generated DEREBRO SPINAL FLUID",
				createDHISObsCountCohortQuery("DEREBRO SPINAL FLUID", OpenMRSReportConcepts.DEREBROSPINALFLUID),
				IndicatorType.COUNT, null);
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
				"HMIS Auto-generated Lab Request", indicators,
				Context.getAdministrationService().getGlobalProperty(DHISReportingConstants.LAB_REPORT_UUID),
				DHISMappingType.DATASET + "_" + "HMIS_LAB_REQUEST", "Monthly");
	}

	private List<CohortDefinitionDimension> createInherentDisaggregation(String disaggs, String inherentDisaggOrder) {
		List<CohortDefinitionDimension> dim = new ArrayList<CohortDefinitionDimension>();

		if (StringUtils.isNotBlank(disaggs) && StringUtils.isNotBlank(inherentDisaggOrder)) {
			String[] disO = inherentDisaggOrder.split(",");
			String[] ds = disaggs.split(",");

			if (disO.length == ds.length) {
				for (int i = 0; i < disO.length; i++) {
					if (StringUtils.isNotBlank(disO[i])) {
						// TODO support any other disaggregations categories
						if (disO[i].trim().equals("Gender")) {
							dim.add(cohorts.createGenderDimension(ds[i].trim()));
						} else if (disO[i].trim().equals("Age")) {
							dim.add(cohorts.createAgeDimension(ds[i].trim(), DurationUnit.YEARS));
						}
					}
				}
			}
		}

		return dim;
	}

	/**
	 * TODO add all reports as they get supported
	 *
	 * @param request
	 */
	@Override
	public void pepfarPage(HttpServletRequest request) {
		ReportDefinitionService rDService = Context.getService(ReportDefinitionService.class);
		PeriodIndicatorReportDefinition onART = (PeriodIndicatorReportDefinition) rDService.getDefinitionByUuid(
				Context.getAdministrationService().getGlobalProperty(DHISReportingConstants.REPORT_UUID_ONART));
		PeriodIndicatorReportDefinition hivStatus = (PeriodIndicatorReportDefinition) rDService.getDefinitionByUuid(
				Context.getAdministrationService().getGlobalProperty(DHISReportingConstants.REPORT_UUID_HIVSTATUS));
		String disableWebReports = Context.getAdministrationService()
				.getGlobalProperty(DHISReportingConstants.DISABLE_WEB_REPORTS_DELETION);

		if (onART == null) {
			createNewPeriodIndicatorONARTReportFromInBuiltIndicatorMappings();
			runAndPostOnARTReportToDHIS();
			if (request != null)
				request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR,
						"You have Successfully Created ON ART Report Definition!");
		} else {
			if ("true".equals(disableWebReports)) {
				runAndPostOnARTReportToDHIS();
				if (request != null)
					request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR,
							"You have Successfully Run and posted Reports");
			} else {
				for (CohortIndicatorAndDimensionColumn c : onART.getIndicatorDataSetDefinition().getColumns()) {
					Mapped<? extends CohortIndicator> cInd = c.getIndicator();
					Indicator cIndDef = cInd != null && StringUtils.isNotBlank(cInd.getUuidOfMappedOpenmrsObject())
							? Context.getService(IndicatorService.class)
									.getDefinitionByUuid(cInd.getUuidOfMappedOpenmrsObject())
							: null;

					if (cIndDef != null)
						Context.getService(IndicatorService.class).purgeDefinition(cIndDef);
				}
				rDService.purgeDefinition(onART);
				if (request != null)
					request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
							"You have Deleted ON ART Report Definition!");
			}
		}
		if (hivStatus == null) {
			createNewPeriodIndicatorHIVStatusReportFromInBuiltIndicatorMappings();
			runAndPostHIVStatusReportToDHIS();
		} else {
			if ("true".equals(disableWebReports))
				runAndPostHIVStatusReportToDHIS();
			else
				rDService.purgeDefinition(hivStatus);
		}
		runAndPostDynamicReports();
	}

	@Override
	public void runAndPostDynamicReports() {
		for (MappedIndicatorReport m : getAllMappedIndicatorReports())
			runAndPostNewDynamicReportFromIndicatorMappings(m);
	}

	@Override
	public Object runAndPostNewDynamicReportFromIndicatorMappings(MappedIndicatorReport report) {
		if (report != null) {
			List<IndicatorMapping> filteredMappings = getIndicatorMappings(null, null,
					report.getDisaggregationCategoriesAsList(), report.getReportUuid(),
					report.getDataElementPrefixesAsList());

			if (filteredMappings != null && !filteredMappings.isEmpty()) {
				return runAndPushReportToDHIS(report.getReportUuid(), report.getDataSetId(), report.getOrgUnitId(),
						report.getPeriodType(), report.getLocationId(), filteredMappings,
						IndicatorMappingCategory.DYNAMIC);
			}
		}
		return null;
	}

	/**
	 * TODO refactor/mimic this same approach for the rest of the reports clear
	 * also including generation of any user's added reports
	 */
	@Override
	public void createNewPeriodIndicatorONARTReportFromInBuiltIndicatorMappings() {
		String openmrsReportUuid = Context.getAdministrationService()
				.getGlobalProperty(DHISReportingConstants.REPORT_UUID_ONART);
		// creating cohort indicators
		PeriodIndicatorReportDefinition report = (PeriodIndicatorReportDefinition) Context
				.getService(ReportDefinitionService.class).getDefinitionByUuid(openmrsReportUuid);
		String reportName = "On ART";
		String reportDescription = "90-90: On ART";

		if (report == null) {
			report = initPeriodIndicatorReport(openmrsReportUuid, reportName, reportDescription);

			List<IndicatorMapping> filteredMappings = filterONARTIndicatorMappings();
			List<MappedCohortIndicator> indicators = new ArrayList<MappedCohortIndicator>();
			SqlCohortDefinition activeOnART = cohorts.hivActiveARTPatients();
			SqlCohortDefinition newOnART = cohorts.hivNewArtPatients();

			// creating cohort queries
			for (IndicatorMapping mapping : filteredMappings) {
				CohortIndicator indicator = null;
				MappedCohortIndicator mIndicator = null;

				if (mapping.getCategory() != null && mapping.getCategory().equals(IndicatorMappingCategory.INBUILT)) {
					if (mapping.getDataelementCode().startsWith("TX_NEW")) {
						indicator = saveNewDHISCohortIndicator(mapping.getDataelementCode(),
								mapping.getDataelementName(), newOnART, IndicatorType.COUNT,
								mapping.getOpenmrsNumeratorCohortUuid());
					} else if (mapping.getDataelementCode().startsWith("TX_CURR")) {
						indicator = saveNewDHISCohortIndicator(mapping.getDataelementCode(),
								mapping.getDataelementName(), activeOnART, IndicatorType.COUNT,
								mapping.getOpenmrsNumeratorCohortUuid());
					}
					if (indicator != null && openmrsReportUuid.equals(mapping.getOpenmrsReportUuid())) {
						mIndicator = new MappedCohortIndicator(indicator, mapping);
						indicators.add(mIndicator);
					}
				}
			}

			createReportDimensions(report, indicators);
			Context.getService(ReportDefinitionService.class).saveDefinition(report);
		}
	}

	private PeriodIndicatorReportDefinition initPeriodIndicatorReport(String openmrsReportUuid, String reportName,
			String reportDescription) {
		PeriodIndicatorReportDefinition report;
		report = new PeriodIndicatorReportDefinition();

		setBasicOpenMRSObjectProps(report);
		report.setDescription(reportDescription);
		report.setupDataSetDefinition();
		report.setName(reportName);
		if (StringUtils.isNotBlank(openmrsReportUuid))
			report.setUuid(openmrsReportUuid);
		return report;
	}

	private void createReportDimensions(PeriodIndicatorReportDefinition report,
			List<MappedCohortIndicator> indicators) {
		if (indicators != null) {
			for (MappedCohortIndicator ind : indicators) {
				IndicatorMapping mapping = ind.getMapping();
				CohortIndicator indicator = ind.getCohortIndicator();
				String code = mapping.getDataelementCode();
				String description = mapping.getDataelementName();
				List<CohortDefinitionDimension> disaggregations = new ArrayList<CohortDefinitionDimension>();
				Map<String, String> dimensions = new HashMap<String, String>();

				if (DisaggregationCategory.AGE.equals(mapping.getDisaggregationCategory())) {
					disaggregations
							.add(cohorts.createAgeDimension(mapping.getCategoryoptioncomboName(), DurationUnit.YEARS));
				} else if (DisaggregationCategory.GENDER.equals(mapping.getDisaggregationCategory())) {
					disaggregations.add(cohorts.createGenderDimension(mapping.getCategoryoptioncomboName()));
				} else if (DisaggregationCategory.INHERENT.equals(mapping.getDisaggregationCategory())) {
					disaggregations = createInherentDisaggregation(mapping.getCategoryoptioncomboName(),
							mapping.getInherentDisaggOrder());
				} else if (DisaggregationCategory.CODED.equals(mapping.getDisaggregationCategory())) {
					if (mapping.getCodedDisaggQuestion() != null) {
						CohortDefinitionDimension cd = cohorts.createCodedQuestionDimension(
								mapping.getCodedDisaggQuestion(), mapping.getCategoryoptioncomboName(),
								mapping.getCodedDisaggAnswer());
						if (cd != null)
							disaggregations.add(cd);
					}
				} else if (DisaggregationCategory.NULL.equals(mapping.getDisaggregationCategory()))
					disaggregations.clear();
				if (!disaggregations.isEmpty()) {
					for (CohortDefinitionDimension dim : disaggregations) {
						if (dim != null && StringUtils.isNotBlank(dim.getName())) {
							report.addDimension(dim.getName(), dim);
							dimensions.put(dim.getName(), dim.getName());
						}
					}
					code += convertDimensionMappingsStringToCode(dimensions.toString());
					report.addIndicator(code, description, indicator, dimensions);
				} else {
					report.addIndicator(code, description, indicator);
				}
			}
		}
	}

	private List<IndicatorMapping> filterONARTIndicatorMappings() {
		String openmrsReportUuid = Context.getAdministrationService()
				.getGlobalProperty(DHISReportingConstants.REPORT_UUID_ONART);
		List<DisaggregationCategory> disaggs = new ArrayList<DisaggregationCategory>();
		List<String> dataElementPrefixs = new ArrayList<String>();

		addSupportedDisaggregations(disaggs);
		dataElementPrefixs.add("TX_NEW");
		dataElementPrefixs.add("TX_CURR");

		/*
		 * First piloting being limited to a few indicators with easy
		 * disaggregations and existing openmrs Rwanda MoH data
		 */
		List<IndicatorMapping> filteredMappings = getIndicatorMappings(null, null, disaggs, openmrsReportUuid,
				dataElementPrefixs);
		return filteredMappings;
	}

	private String convertDimensionMappingsStringToCode(String dimensions) {
		String code = "";
		if (dimensions != null) {
			// {oneToFourteenOfAge=oneToFourteenOfAge, Female=Female}
			for (String dim : dimensions.replace("{", "").replace("}", "").split(", ")) {
				code += DHISReportingConstants.DATAELEMENT_DISAGG_SEPARATOR + dim.split("=")[0].toUpperCase();
			}

		}
		return code;
	}

	@Override
	public void transferDHISReportingFilesToDataDirectory() {
		if (!DHISReportingConstants.DHISREPORTING_DIRECTORY.exists()) {
			DHISReportingConstants.DHISREPORTING_DIRECTORY.mkdirs();
		}

		File mappingsFile = new File(getClass().getClassLoader()
				.getResource(DHISReportingConstants.DHISREPORTING_MAPPING_FILENAME).getFile());
		File merIndicatorsFile = new File(getClass().getClassLoader()
				.getResource(DHISReportingConstants.DHISREPORTING_MER_INDICATORS_FILENAME).getFile());
		File indicatorMappingsFile = new File(
				getClass().getClassLoader().getResource(DHISReportingConstants.INDICATOR_MAPPING_FILE_NAME).getFile());

		try {
			try {
				if (DHISReportingConstants.DHISREPORTING_FINAL_MAPPINGFILE.exists())
					DHISReportingConstants.DHISREPORTING_FINAL_MAPPINGFILE.delete();
				if (DHISReportingConstants.DHISREPORTING_MER_INDICATORS_FILE.exists())
					DHISReportingConstants.DHISREPORTING_MER_INDICATORS_FILE.delete();
				if (DHISReportingConstants.INDICATOR_MAPPING_FILE.exists() && !configs.madeLocalMappingsChanges())
					DHISReportingConstants.INDICATOR_MAPPING_FILE.delete();

				FileUtils.copyFile(merIndicatorsFile, DHISReportingConstants.DHISREPORTING_MER_INDICATORS_FILE);
				FileUtils.copyFile(mappingsFile, DHISReportingConstants.DHISREPORTING_FINAL_MAPPINGFILE);
				if (!configs.madeLocalMappingsChanges())
					FileUtils.copyFile(indicatorMappingsFile, DHISReportingConstants.INDICATOR_MAPPING_FILE);
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
	public Object sendReportDataToDHIS(Report report, String dataSetId, String period, String orgUnitId,
			boolean useTestMapper, List<IndicatorMapping> indicatorMappings, IndicatorMappingCategory mappingCategory) {
		if (report != null && report.getReportData() != null) {
			Map<String, DataSet> dataSets = report.getReportData().getDataSets();
			List<DHISDataValue> dataValues = new ArrayList<DHISDataValue>();
			if (dataSets != null && dataSets.size() > 0 && StringUtils.isNotBlank(period)
					&& StringUtils.isNotBlank(orgUnitId)) {
				DHISDataValueSet dataValueSet = new DHISDataValueSet();
				DataSet ds = dataSets.get("defaultDataSet");
				List<DataSetColumn> columns = ds.getMetaData().getColumns();
				DataSetRow row = ds.iterator().next();
				if (indicatorMappings == null)
					indicatorMappings = getAllIndicatorMappings(null);

				for (int i = 0; i < columns.size(); i++) {
					DHISDataValue dv = new DHISDataValue();
					String column = columns.get(i).getName();
					IndicatorMapping m = getSpecificMappingFromIndicatorMappings(indicatorMappings, column);

					dv.setValue(row.getColumnValue(column).toString());
					dv.setComment(column);
					if (useTestMapper) {
						dv.setDataElement(getValueFromMappings(DHISMappingType.CONCEPTDATAELEMENT + "_" + column));
					} else {
						if (m != null && m.getCategory() != null && m.getCategory().equals(mappingCategory)) {
							dv.setDataElement(StringUtils.isBlank(m.getDataelementId()) ? m.getDataelementCode()
									: m.getDataelementId());
							dv.setCategoryOptionCombo(StringUtils.isBlank(m.getCategoryoptioncomboUid())
									? m.getCategoryoptioncomboCode() : m.getCategoryoptioncomboUid());
						} else
							dv = null;
					}
					if (dv != null)
						dataValues.add(dv);
				}
				dataValueSet.setDataValues(dataValues);
				dataValueSet.setOrgUnit(orgUnitId);
				dataValueSet.setPeriod(period);
				dataValueSet.setDataSet(dataSetId);

				if (!dataValueSet.getDataValues().isEmpty())
					return Context.getService(DHISConnectorService.class).postDataValueSet(dataValueSet);
			}
		}
		return null;
	}

	/*
	 * TODO currently no dataelement ids in mapping, we could rather use them
	 * when added, otherwise dhisreporting.config.dxfToAdxSwitch GP must keep
	 * set to true
	 */
	private IndicatorMapping getSpecificMappingFromIndicatorMappings(List<IndicatorMapping> indicatorMappings,
			String indicatorCode) {
		if (StringUtils.isNotBlank(indicatorCode)) {
			String dataElementCode = indicatorCode.indexOf(DHISReportingConstants.DATAELEMENT_DISAGG_SEPARATOR) >= 0
					? indicatorCode.split(DHISReportingConstants.DATAELEMENT_DISAGG_SEPARATOR)[0] : indicatorCode;
			// TODO default remodeling
			String categoryComboName = indicatorCode.indexOf(DHISReportingConstants.DATAELEMENT_DISAGG_SEPARATOR) >= 0
					? indicatorCode.split(DHISReportingConstants.DATAELEMENT_DISAGG_SEPARATOR, 2)[1] : "default";
			IndicatorMapping mapping = getIndicatorMapping(indicatorMappings, null, dataElementCode,
					revertDimensionCodeSummaryToDisaggregationName(categoryComboName));

			if (mapping != null) {
				return mapping;
			}
		}
		return null;
	}

	private String revertDimensionCodeSummaryToDisaggregationName(String categoryComboName) {
		if (StringUtils.isNotBlank(categoryComboName)) {
			if ("default".equals(categoryComboName))
				return "default";
			else if (categoryComboName.indexOf(DHISReportingConstants.DATAELEMENT_DISAGG_SEPARATOR) >= 0) {
				String finalName = "";
				String[] disaggs = categoryComboName.split(DHISReportingConstants.DATAELEMENT_DISAGG_SEPARATOR);

				for (int i = 0; i < disaggs.length; i++) {
					if (i == 0)
						finalName += revertOneDisaggToItsName(disaggs[i]);
					else
						finalName += ", " + revertOneDisaggToItsName(disaggs[i]);
				}
				return finalName;
			} else
				return revertOneDisaggToItsName(categoryComboName);

		}

		return null;
	}

	@Override
	public String revertOneDisaggToItsName(String categoryComboName) {
		if ("default".equals(categoryComboName))
			return "default";
		if (categoryComboName.equalsIgnoreCase("Female"))
			return "Female";
		else if (categoryComboName.equalsIgnoreCase("Male"))
			return "Male";
		else if (categoryComboName.endsWith("OfAge".toUpperCase())) {
			categoryComboName = categoryComboName.replace("OfAge".toUpperCase(), "");

			try {
				if (categoryComboName.equalsIgnoreCase("belowOne")) {
					return "<1";
				} else if (categoryComboName.startsWith("below".toUpperCase())) {
					return "<" + Math.round(WordToNumber.convert(categoryComboName.replace("below".toUpperCase(), "")));
				} else if (categoryComboName.endsWith("AndAbove".toUpperCase())) {
					return Integer
							.toString(Math.round(
									WordToNumber.convert(categoryComboName.replace("AndAbove".toUpperCase(), ""))))
							+ "+";
				} else if (categoryComboName.indexOf("To".toUpperCase()) > 0) {
					return Integer
							.toString(Math.round(WordToNumber.convert(categoryComboName.split("To".toUpperCase())[0])))
							+ "-" + Integer.toString(
									Math.round(WordToNumber.convert(categoryComboName.split("To".toUpperCase())[1])));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public Object runAndSendReportDataForTheCurrentMonth() {
		Calendar startDate = Calendar.getInstance(Context.getLocale());
		Date endDate = new Date();
		Location defaultLocation = Context.getLocationService().getLocation(Integer.parseInt(
				Context.getAdministrationService().getGlobalProperty(DHISReportingConstants.DEFAULT_LOCATION_ID)));
		PeriodIndicatorReportDefinition labReportDef = (PeriodIndicatorReportDefinition) Context
				.getService(ReportDefinitionService.class).getDefinitionByUuid(DHISReportingConstants.LAB_REPORT_UUID);
		Report labReport = runPeriodIndicatorReport(labReportDef, startDate.getTime(), endDate, defaultLocation);
		String dataSetId = getValueFromMappings(DHISMappingType.DATASET + "_" + "HMIS_LAB_REQUEST");
		String orgUnitId = getValueFromMappings(DHISMappingType.LOCATION + "_"
				+ Context.getAdministrationService().getGlobalProperty(DHISReportingConstants.CONFIGURED_ORGUNIT_UID));
		String period = new SimpleDateFormat("yyyyMM").format(new Date());

		startDate.set(Calendar.DAY_OF_MONTH, 1);
		if (defaultLocation != null && labReportDef != null) {
			return sendReportDataToDHIS(labReport, dataSetId, period, orgUnitId, true, null, null);
		}
		return null;
	}

	@Override
	public Object runAndPostOnARTReportToDHIS() {
		// TODO fix and invoke respectively
		String reportUuid = Context.getAdministrationService()
				.getGlobalProperty(DHISReportingConstants.REPORT_UUID_ONART);
		String dataSetId = Context.getAdministrationService()
				.getGlobalProperty(DHISReportingConstants.DHIS_DATASET_ONART_UID);
		String orgUnitId = Context.getAdministrationService()
				.getGlobalProperty(DHISReportingConstants.CONFIGURED_ORGUNIT_UID);
		String periodType = Context.getAdministrationService()
				.getGlobalProperty(DHISReportingConstants.DHIS_DATASET_ONART_PERIODTYPE);
		Integer locationId = Integer.parseInt(
				Context.getAdministrationService().getGlobalProperty(DHISReportingConstants.DEFAULT_LOCATION_ID));
		List<IndicatorMapping> indicatorMappings = filterONARTIndicatorMappings();

		return runAndPushReportToDHIS(reportUuid, dataSetId, orgUnitId, periodType, locationId, indicatorMappings,
				IndicatorMappingCategory.INBUILT);
	}

	@Override
	public Object runAndPostHIVStatusReportToDHIS() {
		String reportUuid = Context.getAdministrationService()
				.getGlobalProperty(DHISReportingConstants.REPORT_UUID_HIVSTATUS);
		String dataSetId = Context.getAdministrationService()
				.getGlobalProperty(DHISReportingConstants.DHIS_DATASET_ONART_UID);
		String orgUnitId = Context.getAdministrationService()
				.getGlobalProperty(DHISReportingConstants.CONFIGURED_ORGUNIT_UID);
		String periodType = Context.getAdministrationService()
				.getGlobalProperty(DHISReportingConstants.DHIS_DATASET_ONART_PERIODTYPE);
		Integer locationId = Integer.parseInt(
				Context.getAdministrationService().getGlobalProperty(DHISReportingConstants.DEFAULT_LOCATION_ID));
		List<IndicatorMapping> indicatorMappings = filterHIVStatusIndicatorMappings();

		return runAndPushReportToDHIS(reportUuid, dataSetId, orgUnitId, periodType, locationId, indicatorMappings,
				IndicatorMappingCategory.INBUILT);
	}

	public Object runAndPushReportToDHIS(String reportUuid, String dataSetId, String orgUnitId, String periodType,
			Integer locationId, List<IndicatorMapping> indicatorMappings, IndicatorMappingCategory mappingCategory) {
		// TODO pull DHISDataSet object from configuration into DHISConnector
		// local storage using its id passed into this method
		Calendar startDate = Calendar.getInstance(Context.getLocale());
		Calendar endDate = Calendar.getInstance(Context.getLocale());
		Location defaultLocation = Context.getLocationService().getLocation(locationId);
		PeriodIndicatorReportDefinition labReportDef = (PeriodIndicatorReportDefinition) Context
				.getService(ReportDefinitionService.class).getDefinitionByUuid(reportUuid);
		String period = "";

		// TODO support more period types
		if (ReportingPeriodType.Quarterly.name().equals(periodType)) {
			startDate.add(Calendar.MONTH, -3);// Quarterly period
			endDate.setTime(startDate.getTime());
			endDate.add(Calendar.MONTH, 3);
			period += startDate.get(Calendar.YEAR) + "Q" + ((startDate.get(Calendar.MONTH) / 3) + 1);
		} else if (ReportingPeriodType.Monthly.name().equals(periodType)) {
			startDate.add(Calendar.MONTH, -1);
			endDate.setTime(startDate.getTime());
			endDate.add(Calendar.MONTH, 1);
			period += new SimpleDateFormat("yyyyMM").format(startDate.getTime());
		} else if (ReportingPeriodType.Yearly.name().equals(periodType)) {
			startDate.add(Calendar.YEAR, -1);
			endDate.setTime(startDate.getTime());
			endDate.add(Calendar.YEAR, 1);
			period += startDate.get(Calendar.YEAR);
		} else if (ReportingPeriodType.Weekly.name().equals(periodType)) {
			startDate.add(Calendar.WEEK_OF_YEAR, -1);
			endDate.setTime(startDate.getTime());
			endDate.add(Calendar.WEEK_OF_YEAR, 1);
			period += startDate.get(Calendar.YEAR) + "W" + startDate.get(Calendar.WEEK_OF_YEAR);
		} else if (ReportingPeriodType.Daily.name().equals(periodType)) {
			startDate.add(Calendar.DAY_OF_YEAR, -1);
			endDate.setTime(startDate.getTime());
			endDate.add(Calendar.DAY_OF_YEAR, 1);
			period += new SimpleDateFormat("yyyyMMdd").format(startDate.getTime());
		}

		Report labReport = runPeriodIndicatorReport(labReportDef, startDate.getTime(), endDate.getTime(),
				defaultLocation);

		if (defaultLocation != null && labReportDef != null) {
			return sendReportDataToDHIS(labReport, dataSetId, period, orgUnitId, false, indicatorMappings,
					mappingCategory);
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

	/**
	 * @deprecated
	 */
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
	 *            examples include;15-19, 20-24, 25-49, >=50, <1
	 * @return
	 */
	@Override
	public AgeRange convertAgeQueryToAgeRangeObject(String ageQuery, DurationUnit minAgeUnit, DurationUnit maxAgeUnit) {
		return new AgeRange(ageQuery, minAgeUnit, maxAgeUnit);
	}

	@Override
	public JSONArray readJSONArrayFromFile(String fileLocation) {
		JSONParser parser = new JSONParser();

		if (new File(fileLocation).exists()) {
			try {
				Object obj = parser.parse(new FileReader(fileLocation));
				return (JSONArray) obj;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		} else
			return null;
	}

	/**
	 * Acts as the runtime MER indicators database;<br />
	 * Should be Invoked when adding, editing, deleting or doing any mer
	 * indicators manipulations
	 *
	 * @param startingFrom,
	 *            item number based on size to start from just as when paging
	 * @param endindAt,
	 *            item number based on size to end at just as when paging
	 * @return
	 */
	@SuppressWarnings("deprecation")
	@Override
	public List<MerIndicator> getMerIndicators(String merIndicatorsFileLocation, Integer startingFrom,
			Integer endindAt) {
		List<MerIndicator> merIndicators = new ArrayList<MerIndicator>();
		JSONArray indicators = readJSONArrayFromFile(
				StringUtils.isNotBlank(merIndicatorsFileLocation) ? merIndicatorsFileLocation
						: DHISReportingConstants.DHISREPORTING_MER_INDICATORS_FILE.getAbsolutePath());
		ObjectMapper mapper = new ObjectMapper();

		try {
			merIndicators = mapper.readValue(indicators.toJSONString(),
					TypeFactory.collectionType(List.class, MerIndicator.class));
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return merIndicators;
	}

	/**
	 * TODO is the indicator code the best unique property,
	 * this method would return the first found indicator with the reqeusted code incase,
	 * probably dataelement id is better being used
	 */
	@Override
	public MerIndicator getMerIndicator(String code) {
		if (StringUtils.isNotBlank(code)) {
			List<MerIndicator> allIndicators = getMerIndicators(null, null, null);

			for (int i = 0; i < allIndicators.size(); i++) {
				if (code.equals((String) allIndicators.get(i).getCode()))
					return allIndicators.get(i);
			}
		}
		return null;
	}

	/**
	 * Reads mappings from pepfar-meta-datim csv file, parses them to jackson to
	 * map them to IndicatorMapping list
	 *
	 * @param mappingFileLocation,
	 *            excell mappings file
	 * @return
	 */
	@Override
	@SuppressWarnings({ "unchecked", "deprecation" })
	public List<IndicatorMapping> getAllIndicatorMappings(String mappingFileLocation) {
		try {
			ObjectMapper mapper = new ObjectMapper();
			CSVReader reader = new CSVReader(new FileReader(StringUtils.isNotBlank(mappingFileLocation)
					? mappingFileLocation : DHISReportingConstants.INDICATOR_MAPPING_FILE.getAbsolutePath()), ',');
			String[] nextLine;
			String[] firstLine = null;
			JSONArray indicators = new JSONArray();

			while ((nextLine = reader.readNext()) != null) {
				if (firstLine == null)
					firstLine = nextLine;
				else {
					JSONObject obj = new JSONObject();

					for (int i = 0; i < nextLine.length; i++) {
						obj.put(firstLine[i], StringUtils.isNotBlank(nextLine[i]) ? nextLine[i] : null);
					}
					indicators.add(obj);
				}
			}
			reader.close();
			return mapper.readValue(indicators.toJSONString(),
					TypeFactory.collectionType(List.class, IndicatorMapping.class));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ArrayList<IndicatorMapping>();
	}

	/**
	 * indicatorMappings is provided to use less memory which would be used to
	 * re-load the csv mapping file
	 *
	 * filters out all mappings with no data elements ids
	 */
	@Override
	public List<IndicatorMapping> getIndicatorMappings(List<IndicatorMapping> indicatorMappings,
			String mappingFileLocation, List<DisaggregationCategory> disaggs, String openmrsReportUuid,
			List<String> dataElementPrefixs) {
		List<IndicatorMapping> filteredMappings = new ArrayList<IndicatorMapping>();

		if (disaggs != null && StringUtils.isNotBlank(openmrsReportUuid) && dataElementPrefixs != null) {
			List<IndicatorMapping> mList = indicatorMappings != null ? indicatorMappings
					: getAllIndicatorMappings(mappingFileLocation);

			for (IndicatorMapping mapping : mList) {
				if (mapping.isActive() && disaggs.contains(mapping.getDisaggregationCategory())
						&& openmrsReportUuid.equals(mapping.getOpenmrsReportUuid())) {
					boolean rightDataElements = false;

					for (String pref : dataElementPrefixs) {
						if (mapping.getDataelementName().startsWith(pref)
								&& StringUtils.isNotBlank(mapping.getDataelementId())) {
							rightDataElements = true;
							break;
						}
					}
					if (rightDataElements) {
						filteredMappings.add(mapping);
					}
				}
			}
		}
		return filteredMappings;
	}

	/**
	 *
	 * @param indicatorMappings
	 * @param mappingFileLocation
	 * @param dataelementCode
	 * @param categoryoptioncomboName
	 * @return
	 */
	@Override
	public IndicatorMapping getIndicatorMapping(List<IndicatorMapping> indicatorMappings, String mappingFileLocation,
			String dataelementCode, String categoryoptioncomboName) {
		if (StringUtils.isNotBlank(dataelementCode) && StringUtils.isNotBlank(categoryoptioncomboName)) {
			List<IndicatorMapping> mappings = indicatorMappings != null ? indicatorMappings
					: getAllIndicatorMappings(mappingFileLocation);
			for (IndicatorMapping m : mappings) {
				if (dataelementCode.equals(m.getDataelementCode())
						&& matchDisaggregationNames(categoryoptioncomboName, m.getCategoryoptioncomboName()))
					return m;
			}
		}
		return null;
	}

	private boolean matchDisaggregationNames(String n1, String n2) {
		if (StringUtils.isNotBlank(n1) && StringUtils.isNotBlank(n2)) {
			String[] n1a = n1.split(", ");
			String[] n2a = n2.split(", ");

			if (n1.equals(n2) || (n1a.length == n2a.length
					&& new HashSet<String>(Arrays.asList(n1a)).equals(new HashSet<String>(Arrays.asList(n2a))))) {
				return true;
			}
		}
		return false;
	}

	// generates and returns only 11 characters from a uuid
	private String generateDHISUid() {
		String uuid = UUID.randomUUID().toString();

		return uuid.substring(uuid.length() - 12, uuid.length() - 1);
	}

	/**
	 * TODO rewrite posting metadata compressed resource
	 *
	 */
	@SuppressWarnings("unchecked")
	@Override
	public JSONObject postIndicatorMappingDHISMetaData(String mappingLocation) {
		List<IndicatorMapping> mappings = getAllIndicatorMappings(mappingLocation);
		List<DHISCategoryCombo> combos = new ArrayList<DHISCategoryCombo>();
		List<DHISDataElement> dataElements = new ArrayList<DHISDataElement>();
		DHISDataSet dataSet = new DHISDataSet();
		DHISCategoryCombo gender = new DHISCategoryCombo();
		DHISCategoryCombo age = new DHISCategoryCombo();
		DHISCategoryCombo genderAge = new DHISCategoryCombo();
		DHISCategoryCombo others = new DHISCategoryCombo();
		DHISCategoryCombo def = new DHISCategoryCombo();
		ObjectMapper mapper = new ObjectMapper();
		DHISConnectorService service = Context.getService(DHISConnectorService.class);
		List<String> comboNames = new ArrayList<String>();
		List<DHISCategoryOptionCombo> optionCombos = new ArrayList<DHISCategoryOptionCombo>();
		List<String> optionComboIds = new ArrayList<String>();
		JSONParser parser = new JSONParser();

		gender.setName("GENDER");
		gender.setCode("GENDER");
		gender.setId(generateDHISUid());
		age.setName("AGE");
		age.setCode("AGE");
		age.setId(generateDHISUid());
		genderAge.setName("GENDER_AGE");
		genderAge.setCode("GENDER_AGE");
		genderAge.setId(generateDHISUid());
		def.setName("DEFAULT");
		def.setCode("DEFAULT");
		def.setId(generateDHISUid());
		dataSet.setName("ON ART");
		dataSet.setCode("ON_ART");
		dataSet.setId(generateDHISUid());
		dataSet.setPeriodType(ReportingPeriodType.Quarterly.name());

		for (IndicatorMapping mapping : mappings) {
			DHISCategoryOptionCombo optionCombo = new DHISCategoryOptionCombo();
			DHISDataElement dataElement = new DHISDataElement();

			if (StringUtils.isNotBlank(mapping.getCategoryoptioncomboName())) {
				optionCombo.setCode(mapping.getCategoryoptioncomboCode());
				optionCombo.setName(mapping.getCategoryoptioncomboName());
				optionCombo.setId(mapping.getCategoryoptioncomboUid());
				dataElement.setName(mapping.getDataelementName());
				dataElement.setCode(mapping.getDataelementCode());
				dataElement.setId(mapping.getDataelementId());
				dataElement.setNumberType("INTEGER");
				dataElement.setType("INTEGER");
				dataElement.setAggregationType("NONE");
				dataElement.setDomainType("AGGREGATE");

				if (!comboNames.contains((mapping.getCategoryoptioncomboName()))) {
					DHISCategoryCombo cc = new DHISCategoryCombo();

					if (DisaggregationCategory.DEFAULT.equals(mapping.getDisaggregationCategory())) {
						def.getCategoryOptionCombos().add(optionCombo);
						cc.setId(def.getId());
						optionCombo.setCategoryCombo(cc);
						dataElement.setCategoryCombo(def);
					} else if (DisaggregationCategory.AGE.equals(mapping.getDisaggregationCategory())) {
						age.getCategoryOptionCombos().add(optionCombo);
						cc.setId(age.getId());
						optionCombo.setCategoryCombo(cc);
						dataElement.setCategoryCombo(age);
					} else if (DisaggregationCategory.GENDER.equals(mapping.getDisaggregationCategory())) {
						gender.getCategoryOptionCombos().add(optionCombo);
						cc.setId(gender.getId());
						optionCombo.setCategoryCombo(cc);
						dataElement.setCategoryCombo(gender);
					} else if (DisaggregationCategory.INHERENT.equals(mapping.getDisaggregationCategory())) {
						genderAge.getCategoryOptionCombos().add(optionCombo);
						cc.setId(genderAge.getId());
						optionCombo.setCategoryCombo(cc);
						dataElement.setCategoryCombo(genderAge);
					} else
						others.getCategoryOptionCombos().add(optionCombo);

					comboNames.add(mapping.getCategoryoptioncomboName());
					if (!dataElements.contains(dataElements)) {
						dataElements.add(dataElement);
					}

					if (!optionComboIds.contains(optionCombo.getId()))
						optionCombos.add(optionCombo);
				}
			}
		}
		combos.add(def);
		combos.add(age);
		combos.add(gender);
		combos.add(genderAge);

		dataSet.setDataElements(dataElements);

		try {
			JSONObject oC = new JSONObject();
			JSONObject json = new JSONObject();
			JSONObject c = new JSONObject();
			JSONObject de = new JSONObject();
			JSONObject ds = new JSONObject();

			String optionCombosString = mapper.writeValueAsString(optionCombos);
			String comboString = mapper.writeValueAsString(combos);
			String dataElementsString = mapper.writeValueAsString(dataElements);
			String dataSetString = mapper.writeValueAsString(dataSet);

			oC.put("categoryOptionCombos", parser.parse(optionCombosString));
			c.put("categoryCombos", parser.parse(comboString));
			de.put("dataElements", parser.parse(dataElementsString));
			ds.put("dataSets", parser.parse(dataSetString));
			json.put("categoryOptionCombos", parser.parse(optionCombosString));
			json.put("categoryCombos", parser.parse(comboString));
			json.put("dataElements", parser.parse(dataElementsString));
			json.put("dataSets", parser.parse(dataSetString));

			String optionComboResponseString = service.postDataToDHISEndpoint(OPTION_CATEGORY_COMBOS_PATH,
					oC.toJSONString());
			String comboResponseString = service.postDataToDHISEndpoint(CATEGORY_COMBOS_PATH, c.toJSONString());
			String dataElementsResponseString = service.postDataToDHISEndpoint(DATA_ELEMETS_PATH, de.toJSONString());
			String datasetsResponseString = service.postDataToDHISEndpoint(DATASETS_PATH, ds.toJSONString());

			System.out.println("\n\n");
			System.out.println("\n\n" + optionComboResponseString);
			System.out.println("\n\n" + comboResponseString);
			System.out.println("\n\n" + dataElementsResponseString);
			System.out.println("\n\n" + datasetsResponseString);
			return json;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public List<MappedIndicatorReport> getAllMappedIndicatorReports() {
		return getDao().getAllMappedIndicatorReports();
	}

	@Override
	public MappedIndicatorReport getMappedIndicatorReportByUuid(String uuid) {
		return getDao().getMappedIndicatorReportByUuid(uuid);
	}

	@Override
	public MappedIndicatorReport getMappedIndicatorReport(Integer id) {
		return getDao().getMappedIndicatorReport(id);
	}

	@Override
	public void deleteMappedIndicatorReport(MappedIndicatorReport mappedIndicatorReport) {
		getDao().deleteMappedIndicatorReport(mappedIndicatorReport);
	}

	@Override
	public void saveMappedIndicatorReport(MappedIndicatorReport mappedIndicatorReport) {
		getDao().saveMappedIndicatorReport(mappedIndicatorReport);
	}

	/**
	 * TODO do further restrictions and checking here, the reports must exist
	 * and their indicator codes match the indicatormappings dataelementcodes
	 *
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public JSONArray getMappedIndicatorReportExistingMeta() {
		List<IndicatorMapping> mappings = getAllIndicatorMappings(null);
		JSONArray meta = new JSONArray();
		String lastReport = null;
		JSONObject metaData = new JSONObject();
		JSONArray disaggCats = new JSONArray();
		JSONArray dataElementPrefixes = new JSONArray();

		// sort indicatorMappings list by report
		Collections.sort(mappings, new Comparator<IndicatorMapping>() {
			@Override
			public int compare(final IndicatorMapping object1, final IndicatorMapping object2) {
				return StringUtils.isNotBlank(object1.getOpenmrsReportUuid())
						&& StringUtils.isNotBlank(object2.getOpenmrsReportUuid())
								? object1.getOpenmrsReportUuid().compareTo(object2.getOpenmrsReportUuid()) : -1;
			}
		});

		for (int i = 0; i < mappings.size(); i++) {
			IndicatorMapping m = mappings.get(i);

			if (IndicatorMappingCategory.DYNAMIC.equals(m.getCategory())
					&& StringUtils.isNotBlank(m.getOpenmrsReportUuid()) && m.isActive()
					&& StringUtils.isNotBlank(m.getDataelementCode())) {
				if (m.getOpenmrsReportUuid().equals(lastReport)) {
					setUpMeta(disaggCats, dataElementPrefixes, m);
				} else {
					disaggCats = new JSONArray();
					dataElementPrefixes = new JSONArray();

					setUpMeta(disaggCats, dataElementPrefixes, m);
				}
				if (i == mappings.size() - 1 || !mappings.get(i + 1).getOpenmrsReportUuid().equals(lastReport)) {
					metaData.put("report", m.getOpenmrsReportUuid());
					metaData.put("disaggCategories", disaggCats);
					metaData.put("dataElementPrefixes", dataElementPrefixes);
					meta.add(metaData);
				}

				lastReport = m.getOpenmrsReportUuid();
			}
		}

		return meta;
	}

	@SuppressWarnings("unchecked")
	private void setUpMeta(JSONArray disaggCats, JSONArray dataElementPrefixes, IndicatorMapping m) {
		if (!disaggCats.contains(m.getDisaggregationCategory().name()))
			disaggCats.add(m.getDisaggregationCategory().name());
		if (!dataElementPrefixes.contains(m.getDataelementCode())) {
			dataElementPrefixes.add(m.getDataelementCode().split("_")[0]);
		}
	}

	public void createNewPeriodIndicatorHIVStatusReportFromInBuiltIndicatorMappings() {
		String openmrsReportUuid = Context.getAdministrationService()
				.getGlobalProperty(DHISReportingConstants.REPORT_UUID_HIVSTATUS);
		// creating cohort indicators
		PeriodIndicatorReportDefinition report = (PeriodIndicatorReportDefinition) Context
				.getService(ReportDefinitionService.class).getDefinitionByUuid(openmrsReportUuid);
		String reportName = "HIV Status";
		String reportDescription = "90: Knowing your HIV status";

		if (report == null) {
			report = initPeriodIndicatorReport(openmrsReportUuid, reportName, reportDescription);

			List<IndicatorMapping> filteredMappings = filterHIVStatusIndicatorMappings();
			List<MappedCohortIndicator> indicators = new ArrayList<MappedCohortIndicator>();
			CompositionCohortDefinition inPMTCTPositive = cohorts.inPMTCTHIVPostivePatients();
			CompositionCohortDefinition pctTestInFirst12Months = cohorts.infantsWhoHadPCRTestInNMonths(12);

			for (IndicatorMapping mapping : filteredMappings) {
				CohortIndicator indicator = null;
				MappedCohortIndicator mIndicator = null;

				if (mapping.getCategory() != null && mapping.getCategory().equals(IndicatorMappingCategory.INBUILT)) {
					if (mapping.getDataelementCode().startsWith("PMTCT_STAT")) {
						CodedObsCohortDefinition preg = cohorts.pregnantPatients();

						indicator = saveNewDHISCohortIndicator(mapping.getDataelementCode(),
								mapping.getDataelementName(), inPMTCTPositive, IndicatorType.COUNT,
								mapping.getOpenmrsNumeratorCohortUuid());
						// denominator is meant to be all pregnant patients but
						// seems not tracked in Rwanda EMR
						if (preg != null) {
							indicator.setType(IndicatorType.FRACTION);
							indicator.setDenominator(preg, cohorts.defaultParameterMappings());
						}
					} else if (mapping.getDataelementCode().startsWith("PMTCT_EID")) {
						indicator = saveNewDHISCohortIndicator(mapping.getDataelementCode(),
								mapping.getDataelementName(), pctTestInFirst12Months, IndicatorType.COUNT,
								mapping.getOpenmrsNumeratorCohortUuid());
						indicator.setType(IndicatorType.FRACTION);
						indicator.setDenominator(inPMTCTPositive, cohorts.defaultParameterMappings());
					}
					if (indicator != null && openmrsReportUuid.equals(mapping.getOpenmrsReportUuid())) {
						mIndicator = new MappedCohortIndicator(indicator, mapping);
						indicators.add(mIndicator);
					}
				}
			}
			createReportDimensions(report, indicators);
			Context.getService(ReportDefinitionService.class).saveDefinition(report);

		}
	}

	private List<IndicatorMapping> filterHIVStatusIndicatorMappings() {
		String openmrsReportUuid = Context.getAdministrationService()
				.getGlobalProperty(DHISReportingConstants.REPORT_UUID_HIVSTATUS);
		List<DisaggregationCategory> disaggs = new ArrayList<DisaggregationCategory>();
		List<String> dataElementPrefixs = new ArrayList<String>();

		addSupportedDisaggregations(disaggs);
		dataElementPrefixs.add("PMTCT_STAT");
		dataElementPrefixs.add("PMTCT_EID");

		List<IndicatorMapping> filteredMappings = getIndicatorMappings(null, null, disaggs, openmrsReportUuid,
				dataElementPrefixs);
		return filteredMappings;
	}

	public void createNewPeriodIndicatorANCReportFromInBuiltIndicatorMappings() {
		String openmrsReportUuid = Context.getAdministrationService()
				.getGlobalProperty(DHISReportingConstants.ANC_REPORT_UUID);
		// creating cohort indicators
		PeriodIndicatorReportDefinition report = (PeriodIndicatorReportDefinition) Context
				.getService(ReportDefinitionService.class).getDefinitionByUuid(openmrsReportUuid);
		String reportName = "RHMIS ANC";
		String reportDescription = "RHMIS ANC report";

		if (report == null) {
			report = initPeriodIndicatorReport(openmrsReportUuid, reportName, reportDescription);

			List<IndicatorMapping> filteredMappings = filterANCIndicatorMappings();
			List<MappedCohortIndicator> indicators = new ArrayList<MappedCohortIndicator>();

			for (IndicatorMapping mapping : filteredMappings) {
				CohortIndicator indicator = null;
				MappedCohortIndicator mIndicator = null;

				if (mapping.getCategory() != null && mapping.getCategory().equals(IndicatorMappingCategory.INBUILT)) {
					if (mapping.getDataelementCode().startsWith("ANC")) {
						CohortDefinition baseCohortDefinition = mapping.getBaseCohort().equals(BaseCohort.ANC)
								? cohorts.inANC() : null;
						CompositionCohortDefinition cd = new CompositionCohortDefinition();
						String cdQuery = "";

						if (baseCohortDefinition != null) {
							cd.addSearch("inANC", baseCohortDefinition, cohorts.defaultParameterMappings());
							cdQuery += "inANC";
						}
						if (mapping.getCodedDisaggQuestion() != null) {
							CohortDefinition cd1 = cohorts.createCodedObsCohortDefinition(
									Context.getConceptService().getConcept(mapping.getCodedDisaggQuestion()),
									mapping.getCodedDisaggAnswer() != null
											? Context.getConceptService().getConcept(mapping.getCodedDisaggAnswer())
											: CodedDisaggregation.matchCodedQuestionDisaggregation(
													mapping.getCodedDisaggQuestion(),
													mapping.getCategoryoptioncomboName()),
									SetComparator.IN, TimeModifier.LAST);
							cd.addSearch(cd1.getName(), cd1, cohorts.defaultParameterMappings());
							cdQuery += StringUtils.isBlank(cdQuery) ? cd1.getName() : " and " + cd1.getName();
						}

						cd.setCompositionString(cdQuery);
						indicator = saveNewDHISCohortIndicator(mapping.getDataelementCode(),
								mapping.getDataelementName(), cd, IndicatorType.COUNT,
								mapping.getOpenmrsNumeratorCohortUuid());
						//TODO proceed
					}
					if (indicator != null && openmrsReportUuid.equals(mapping.getOpenmrsReportUuid())) {
						mIndicator = new MappedCohortIndicator(indicator, mapping);
						indicators.add(mIndicator);
					}
				}
			}
			createReportDimensions(report, indicators);
			Context.getService(ReportDefinitionService.class).saveDefinition(report);
		}
	}

	private List<IndicatorMapping> filterANCIndicatorMappings() {
		String ancReportUuid = Context.getAdministrationService()
				.getGlobalProperty(DHISReportingConstants.ANC_REPORT_UUID);
		List<DisaggregationCategory> disaggs = new ArrayList<DisaggregationCategory>();
		List<String> dataElementPrefixs = new ArrayList<String>();

		addSupportedDisaggregations(disaggs);
		dataElementPrefixs.add("ANC");

		List<IndicatorMapping> filteredMappings = getIndicatorMappings(null, null, disaggs, ancReportUuid,
				dataElementPrefixs);
		return filteredMappings;
	}

	/**
	 * TODO only question-answer coded are supported, support the rest
	 *
	 * @param disaggs
	 */
	private void addSupportedDisaggregations(List<DisaggregationCategory> disaggs) {
		disaggs.add(DisaggregationCategory.AGE);
		disaggs.add(DisaggregationCategory.DEFAULT);
		disaggs.add(DisaggregationCategory.GENDER);
		disaggs.add(DisaggregationCategory.INHERENT);
		disaggs.add(DisaggregationCategory.CODED);
		disaggs.add(DisaggregationCategory.NULL);
	}
}