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

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.api.PatientSetService.TimeModifier;
import org.openmrs.api.context.Context;
import org.openmrs.api.impl.BaseOpenmrsService;
import org.openmrs.module.dhisreporting.api.DHISReportingService;
import org.openmrs.module.dhisreporting.api.db.DHISReportingDAO;
import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.cohort.definition.CodedObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.service.CohortDefinitionService;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.indicator.CohortIndicator.IndicatorType;
import org.openmrs.module.reporting.indicator.service.IndicatorService;
import org.openmrs.module.reporting.report.ReportData;
import org.openmrs.module.reporting.report.definition.PeriodIndicatorReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;

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
	public Cohort evaluateDHISObsCountCohortQuery(Concept concept, Location location, Date startDate, Date endDate) {
		CodedObsCohortDefinition cd = new CodedObsCohortDefinition();

		cd.setTimeModifier(TimeModifier.AVG);
		cd.setQuestion(concept);
		cd.setOnOrAfter(startDate);
		cd.setOnOrBefore(endDate);
		cd.setLocationList(Collections.singletonList(location));
		try {
			return Context.getService(CohortDefinitionService.class).evaluate(cd, null);
		} catch (EvaluationException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	@SuppressWarnings("deprecation")
	public CohortIndicator saveNewDHISCohortIndicator(String indicatorName, String indicatorDescription,
			Cohort obsCohort) {
		CohortIndicator indicator = new CohortIndicator();
		CodedObsCohortDefinition obsCohortDef = (CodedObsCohortDefinition) obsCohort.getCohortDefinition();

		indicator.getParameters().clear();
		indicator.addParameter(ReportingConstants.START_DATE_PARAMETER);
		indicator.addParameter(ReportingConstants.END_DATE_PARAMETER);
		indicator.addParameter(ReportingConstants.LOCATION_PARAMETER);
		indicator.setName(indicatorName);
		indicator.setDescription(indicatorDescription);
		indicator.setType(IndicatorType.COUNT);
		indicator.setCohortDefinition(obsCohortDef, ParameterizableUtil
				.createParameterMappings("startDate=${startDate},endDate=${endDate},locationList=${location}"));

		return Context.getService(IndicatorService.class).saveDefinition(indicator);
	}

	@Override
	public ReportData evaluateNewDHISPeriodReport(String reportName, String reportDrescription, Date startDate,
			Date endDate, Location location, List<CohortIndicator> indicators) {
		PeriodIndicatorReportDefinition report = new PeriodIndicatorReportDefinition();
		ReportDefinitionService rs = Context.getService(ReportDefinitionService.class);
		EvaluationContext context = new EvaluationContext();

		report.setupDataSetDefinition();
		report.setName(reportName);
		report.setDescription(reportDrescription);

		if (indicators != null)
			for (CohortIndicator ind : indicators)
				report.addIndicator(ind);
		context.addParameterValue("startDate", startDate);
		context.addParameterValue("endDate", endDate);
		context.addParameterValue("location", location);

		try {
			return rs.evaluate(report, context);
		} catch (EvaluationException e) {
			e.printStackTrace();

			return null;
		}
	}

}