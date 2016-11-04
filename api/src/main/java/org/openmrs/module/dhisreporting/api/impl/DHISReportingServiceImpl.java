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

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.OpenmrsMetadata;
import org.openmrs.api.PatientSetService.TimeModifier;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.dhisreporting.api.DHISReportingService;
import org.openmrs.module.dhisreporting.api.db.DHISReportingDAO;
import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.cohort.definition.CodedObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.EncounterCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.service.CohortDefinitionService;
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
	 * @param endDate
	 * @return cohort, cohort.size() returns evaluated number of patients
	 *         returned
	 */
	@Override
	public CodedObsCohortDefinition evaluateDHISObsCountCohortQuery(String name, Concept concept, Location location, Date startDate,
			Date endDate) {
		CodedObsCohortDefinition cd = new CodedObsCohortDefinition();

		setBasicOpenMRSObjectProps(cd);
		cd.setTimeModifier(TimeModifier.AVG);
		cd.setName(name);
		cd.setQuestion(concept);
		cd.setOnOrAfter(startDate);
		cd.setOnOrBefore(endDate);
		cd.setLocationList(Collections.singletonList(location));
		
		return Context.getService(CohortDefinitionService.class).saveDefinition(cd);
	}

	@Override
	public CohortIndicator saveNewDHISCohortIndicator(String indicatorName, String indicatorDescription,
			CodedObsCohortDefinition obsCohort) {
		CohortIndicator indicator = new CohortIndicator();
		EncounterCohortDefinition atSite = new EncounterCohortDefinition();

		setBasicOpenMRSObjectProps(indicator);
		atSite.setName("At Site");
		atSite.addParameter(new Parameter("locationList", "List of Locations", Location.class));

		indicator.getParameters().clear();
		indicator.addParameter(ReportingConstants.START_DATE_PARAMETER);
		indicator.addParameter(ReportingConstants.END_DATE_PARAMETER);
		indicator.addParameter(ReportingConstants.LOCATION_PARAMETER);
		indicator.setName(indicatorName);
		indicator.setDescription(indicatorDescription);
		indicator.setType(IndicatorType.COUNT);
		indicator.setLocationFilter(atSite, "locationList=${location}");
		indicator.setCohortDefinition(obsCohort, "");

		return Context.getService(IndicatorService.class).saveDefinition(indicator);
	}

	@Override
	public Report evaluateNewDHISPeriodReport(String reportName, String reportDrescription, Date startDate,
			Date endDate, Location location, List<CohortIndicator> indicators) {
		PeriodIndicatorReportDefinition report = new PeriodIndicatorReportDefinition();

		setBasicOpenMRSObjectProps(report);
		report.setDescription(reportDrescription);
		report.setupDataSetDefinition();
		report.setName(reportName);

		if (indicators != null) {
			for (CohortIndicator ind : indicators) {
				report.addIndicator(ind.getName().replaceAll(" ", ""), ind.getName(), ind);
			}
		}
		ReportRequest request = new ReportRequest(new Mapped<ReportDefinition>(report, null), null,
				new RenderingMode(new DefaultWebRenderer(), "Web", null, 100), Priority.HIGHEST, null);

		request.getReportDefinition().addParameterMapping("startDate", startDate);
		request.getReportDefinition().addParameterMapping("endDate", endDate);
		request.getReportDefinition().addParameterMapping("location", location);
		request.setStatus(Status.PROCESSING);
		request = Context.getService(ReportService.class).saveReportRequest(request);

		Report runReport = Context.getService(ReportService.class).runReport(request);

		return runReport;
	}

	private void setBasicOpenMRSObjectProps(OpenmrsMetadata omrs) {
		if (omrs != null) {
			omrs.setCreator(Context.getAuthenticatedUser());
			omrs.setRetired(false);
			omrs.setDateCreated(Calendar.getInstance(Context.getLocale()).getTime());
		}
	}
}