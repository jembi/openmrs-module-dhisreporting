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
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.OpenmrsMetadata;
import org.openmrs.api.PatientSetService.TimeModifier;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.dhisconnector.api.DHISConnectorService;
import org.openmrs.module.dhisconnector.api.model.DHISDataValue;
import org.openmrs.module.dhisconnector.api.model.DHISDataValueSet;
import org.openmrs.module.dhisconnector.api.model.DHISMapping;
import org.openmrs.module.dhisconnector.api.model.DHISMappingElement;
import org.openmrs.module.dhisreporting.AgeRange;
import org.openmrs.module.dhisreporting.Configurations;
import org.openmrs.module.dhisreporting.DHISReportingConstants;
import org.openmrs.module.dhisreporting.OpenMRSReportConcepts;
import org.openmrs.module.dhisreporting.OpenMRSToDHISMapping;
import org.openmrs.module.dhisreporting.OpenMRSToDHISMapping.DHISMappingType;
import org.openmrs.module.dhisreporting.api.DHISReportingService;
import org.openmrs.module.dhisreporting.api.db.DHISReportingDAO;
import org.openmrs.module.dhisreporting.mapping.IndicatorMapping;
import org.openmrs.module.dhisreporting.mapping.IndicatorMapping.DisaggregationCategory;
import org.openmrs.module.dhisreporting.mer.MerIndicator;
import org.openmrs.module.dhisreporting.reporting.MappedCohortIndicator;
import org.openmrs.module.dhisreporting.reporting.PatientCohorts;
import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.cohort.definition.CodedObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.service.CohortDefinitionService;
import org.openmrs.module.reporting.common.DurationUnit;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetColumn;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition.CohortIndicatorAndDimensionColumn;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.indicator.CohortIndicator.IndicatorType;
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
	public void createCohortQueriesIndicatorsAndReports() {
		/*
		 * mappings trying to use the same mappings list to save memory of
		 * loading it from csv every time while setting up another report
		 */

		if (Context.getService(ReportDefinitionService.class)
				.getDefinitionByUuid(DHISReportingConstants.LAB_REPORT_UUID) == null) {
			createNewLabPeriodReportAndItsDHISConnectorMapping();
		}
		if (Context.getService(ReportDefinitionService.class).getDefinitionByUuid(Context.getAdministrationService()
				.getGlobalProperty(DHISReportingConstants.REPORT_UUID_ONART)) == null) {
			createNewPeriodIndicatorONARTReportAndItsDHISConnectorMapping();

		}
		// testPeriodIndicatorReportRendereing();
	}

	private void testPeriodIndicatorReportRendereing() {
		String uuid = "uuuuuuuuuuuuuuuuuuuuuuuuid";

		if (Context.getService(ReportDefinitionService.class).getDefinitionByUuid(uuid) == null) {
			CohortIndicator newOnART = saveNewDHISCohortIndicator("ART_NEW", "New On ART", cohorts.hivNewArtPatients(),
					IndicatorType.COUNT, null);
			CohortIndicator currOnART = saveNewDHISCohortIndicator("ART_CURR", "Current On ART",
					cohorts.hivNewArtPatients(), IndicatorType.COUNT, null);
			CohortIndicator inHIV = saveNewDHISCohortIndicator("IN_HIV", "In HIV Program", cohorts.inHIVProgram(),
					IndicatorType.COUNT, null);
			List<CohortIndicator> indicators = new ArrayList<CohortIndicator>();
			PeriodIndicatorReportDefinition report = new PeriodIndicatorReportDefinition();
			CohortDefinitionDimension above5AgeDisagg = cohorts.createAgeDimension(">=5", DurationUnit.YEARS);
			CohortDefinitionDimension maleGenderDisagg = cohorts.createGenderDimension("Male");
			Map<String, Object> mappings = new HashMap<String, Object>();
			Map<String, String> onlyAgeDimension = new HashMap<String, String>();
			Map<String, String> onlyGenderDimension = new HashMap<String, String>();
			Map<String, String> bothGenderAndAgeDimension = new HashMap<String, String>();
			Map<String, String> bothAgeAndGenderDimension = new HashMap<String, String>();

			mappings.put("startDate", "${startDate}");
			mappings.put("endDate", "${endDate}");
			mappings.put("location", "${location}");
			onlyGenderDimension.put(maleGenderDisagg.getName(), maleGenderDisagg.getName());
			onlyAgeDimension.put(above5AgeDisagg.getName(), above5AgeDisagg.getName());
			bothGenderAndAgeDimension.put(maleGenderDisagg.getName(), maleGenderDisagg.getName());
			bothGenderAndAgeDimension.put(above5AgeDisagg.getName(), above5AgeDisagg.getName());
			bothAgeAndGenderDimension.put(above5AgeDisagg.getName(), above5AgeDisagg.getName());
			bothAgeAndGenderDimension.put(maleGenderDisagg.getName(), maleGenderDisagg.getName());
			// noDimension.put(above5AgeDisagg.getName(), "All");
			// noDimension.put(maleGenderDisagg.getName(), "All");

			report.addParameter(new Parameter("startDate", "Start Date", Date.class));
			report.addParameter(new Parameter("endDate", "End Date", Date.class));
			report.addParameter(new Parameter("location", "Health Center", Location.class));
			/*
			 * report.setBaseCohortDefinition(cohorts.
			 * createParameterizedLocationCohort("At Location"),
			 * ParameterizableUtil.createParameterMappings(
			 * "location=${location}"));
			 */
			report.setName("ART");
			report.setDescription("ART Piloting");

			indicators.add(newOnART);
			indicators.add(currOnART);
			indicators.add(inHIV);
			setBasicOpenMRSObjectProps(report);
			report.setupDataSetDefinition();
			report.addDimension(above5AgeDisagg.getName(), above5AgeDisagg);
			report.addDimension(maleGenderDisagg.getName(), maleGenderDisagg);
			if (StringUtils.isNotBlank(uuid))
				report.setUuid(uuid);

			if (indicators != null) {
				for (CohortIndicator ind : indicators) {
					report.addIndicator(ind.getName(), ind.getDescription(), ind);
					report.addIndicator(ind.getName() + "_G", ind.getDescription(), ind, onlyGenderDimension);
					report.addIndicator(ind.getName() + "_A", ind.getDescription(), ind, onlyAgeDimension);
					report.addIndicator(ind.getName() + "_GA", ind.getDescription(), ind, bothGenderAndAgeDimension);
					report.addIndicator(ind.getName() + "_AG", ind.getDescription(), ind, bothAgeAndGenderDimension);
				}
			}

			Context.getService(ReportDefinitionService.class).saveDefinition(report);
		}
	}

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

		if (onART == null) {
			Context.getService(DHISReportingService.class)
					.createNewPeriodIndicatorONARTReportAndItsDHISConnectorMapping();
			if (request != null)
				request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR,
						"You have Successfully Created ON ART Report Definition!");
		} else {
			for (CohortIndicatorAndDimensionColumn c : onART.getIndicatorDataSetDefinition().getColumns()) {
				Context.getService(IndicatorService.class).purgeDefinition(Context.getService(IndicatorService.class)
						.getDefinitionByUuid(c.getIndicator().getUuidOfMappedOpenmrsObject()));
			}
			rDService.purgeDefinition(onART);
			if (request != null)
				request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
						"You have Deleted ON ART Report Definition!");
		}
	}

	/**
	 * @param reportDefinitionUuid
	 * @param mappings
	 */
	@Override
	public void createNewPeriodIndicatorONARTReportAndItsDHISConnectorMapping() {
		String openmrsReportUuid = Context.getAdministrationService()
				.getGlobalProperty(DHISReportingConstants.REPORT_UUID_ONART);
		// creating cohort indicators
		PeriodIndicatorReportDefinition report = (PeriodIndicatorReportDefinition) Context
				.getService(ReportDefinitionService.class).getDefinitionByUuid(openmrsReportUuid);
		String reportName = "On ART";

		if (report == null) {
			List<DisaggregationCategory> disaggs = new ArrayList<DisaggregationCategory>();
			List<String> dataElementPrefixs = new ArrayList<String>();

			disaggs.add(DisaggregationCategory.AGE);
			disaggs.add(DisaggregationCategory.DEFAULT);
			disaggs.add(DisaggregationCategory.GENDER);
			disaggs.add(DisaggregationCategory.INHERENT);
			dataElementPrefixs.add("TX_NEW");
			dataElementPrefixs.add("TX_CURR");

			/*
			 * First piloting being limited to a few indicators with easy
			 * disaggregations and existing openmrs Rwanda MoH data
			 */
			List<IndicatorMapping> filteredMappings = getIndicatorMappings(null, null, true, disaggs, openmrsReportUuid,
					dataElementPrefixs);
			List<MappedCohortIndicator> indicators = new ArrayList<MappedCohortIndicator>();
			SqlCohortDefinition activeOnART = cohorts.hivActiveARTPatients();
			SqlCohortDefinition newOnART = cohorts.hivNewArtPatients();

			// creating cohort queries
			for (IndicatorMapping mapping : filteredMappings) {
				CohortIndicator indicator = null;
				MappedCohortIndicator mIndicator = null;

				if (mapping.getDataelementCode().startsWith("TX_NEW")) {
					indicator = saveNewDHISCohortIndicator(mapping.getDataelementCode(), mapping.getDataelementName(),
							newOnART, IndicatorType.COUNT, mapping.getOpenmrsNumeratorCohortUuid());
				} else if (mapping.getDataelementCode().startsWith("TX_CURR")) {
					indicator = saveNewDHISCohortIndicator(mapping.getDataelementCode(), mapping.getDataelementName(),
							activeOnART, IndicatorType.COUNT, mapping.getOpenmrsNumeratorCohortUuid());
				}
				if (indicator != null && openmrsReportUuid.equals(mapping.getOpenmrsReportUuid())) {
					mIndicator = new MappedCohortIndicator(indicator, mapping);
					indicators.add(mIndicator);
				}
			}
			report = new PeriodIndicatorReportDefinition();

			setBasicOpenMRSObjectProps(report);
			report.setDescription("90-90: On ART");
			report.setupDataSetDefinition();
			report.setName(reportName);
			if (StringUtils.isNotBlank(openmrsReportUuid))
				report.setUuid(openmrsReportUuid);

			if (indicators != null) {
				for (MappedCohortIndicator ind : indicators) {
					IndicatorMapping mapping = ind.getMapping();
					CohortIndicator indicator = ind.getCohortIndicator();
					String code = mapping.getDataelementCode();
					String description = mapping.getDataelementName();
					List<CohortDefinitionDimension> disaggregations = new ArrayList<CohortDefinitionDimension>();
					Map<String, String> dimensions = new HashMap<String, String>();

					if (DisaggregationCategory.AGE.equals(mapping.getDisaggregationCategory())) {
						disaggregations.add(
								cohorts.createAgeDimension(mapping.getCategoryoptioncomboName(), DurationUnit.YEARS));
					} else if (DisaggregationCategory.GENDER.equals(mapping.getDisaggregationCategory())) {
						disaggregations.add(cohorts.createGenderDimension(mapping.getCategoryoptioncomboName()));
					} else if (DisaggregationCategory.INHERENT.equals(mapping.getDisaggregationCategory())) {
						disaggregations = createInherentDisaggregation(mapping.getCategoryoptioncomboName(),
								mapping.getInherentDisaggOrder());
					}
					if (!disaggregations.isEmpty()) {
						for (CohortDefinitionDimension dim : disaggregations) {
							if (dim != null && StringUtils.isNotBlank(dim.getName())) {
								report.addDimension(dim.getName(), dim);
								dimensions.put(dim.getName(), dim.getName());
							}
						}
					}
					report.addIndicator(code, description, indicator, dimensions);
				}
			}
			Context.getService(ReportDefinitionService.class).saveDefinition(report);

		}
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
	// TODO migrating this to DHISConnector module to support scheduling this
	// call in it
	// TODO may either be dxf or adx summary
	public Object sendReportDataToDHIS(Report report, String dataSetId, String period, String orgUnitId) {
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
	public Object runAndSendReportDataForTheCurrentMonth() {
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
	@SuppressWarnings("unchecked")
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
						obj.put(firstLine[i], nextLine[i]);
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
	 */
	@Override
	public List<IndicatorMapping> getIndicatorMappings(List<IndicatorMapping> indicatorMappings,
			String mappingFileLocation, Boolean active, List<DisaggregationCategory> disaggs, String openmrsReportUuid,
			List<String> dataElementPrefixs) {
		List<IndicatorMapping> filteredMappings = new ArrayList<IndicatorMapping>();

		if (active != null && disaggs != null && StringUtils.isNotBlank(openmrsReportUuid)
				&& dataElementPrefixs != null) {
			List<IndicatorMapping> mList = indicatorMappings != null ? indicatorMappings
					: getAllIndicatorMappings(mappingFileLocation);

			for (IndicatorMapping mapping : mList) {
				if (active.equals(mapping.isActive()) && disaggs.contains(mapping.getDisaggregationCategory())
						&& openmrsReportUuid.equals(mapping.getOpenmrsReportUuid())) {
					boolean rightDataElements = false;

					for (String pref : dataElementPrefixs) {
						if (mapping.getDataelementName().startsWith(pref)) {
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
}