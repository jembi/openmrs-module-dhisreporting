package org.openmrs.module.dhisreporting.reporting;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.module.dhisreporting.mapping.IndicatorMapping;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.reporting.web.renderers.DefaultWebRenderer;

public class BaseReportSetup {
	PatientCohorts cohorts = new PatientCohorts();

	List<IndicatorMapping> mappings;

	public BaseReportSetup(List<IndicatorMapping> mappings) {
		setMappings(mappings);
	}

	public List<IndicatorMapping> getMappings() {
		return mappings;
	}

	public void setMappings(List<IndicatorMapping> mappings) {
		this.mappings = mappings;
	}

	public void deleteReportDefinition(String name) {
		ReportService rs = Context.getService(ReportService.class);

		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if (name.equals(rd.getReportDefinition().getUuid())) {
				rs.purgeReportDesign(rd);
			}
		}
		purgeReportDefinition(name);
	}

	/**
	 * 
	 */
	public void setupReport(ReportDefinition rd, String name, String excellOutputFileName) throws IOException {
		ReportDesign design = createWebDesign(rd);

		Context.getService(ReportService.class).saveReportDesign(design);

	}

	public ReportDefinition createReportDefinition(String name) {
		ReportDefinition reportDefinition = new ReportDefinition();

		reportDefinition.setName(name);
		reportDefinition.addParameter(new Parameter("startDate", "Start Date", Date.class));
		reportDefinition.addParameter(new Parameter("endDate", "End Date", Date.class));
		reportDefinition.addParameter(new Parameter("location", "Health Center", Location.class));
		// TODO disable location filter, set this base cohort to aggregation
		// cohort
		reportDefinition.setBaseCohortDefinition(cohorts.createParameterizedLocationCohort("At Location"),
				ParameterizableUtil.createParameterMappings("location=${location}"));

		return reportDefinition;
	}

	private void purgeReportDefinition(String name) {
		ReportDefinitionService rds = Context.getService(ReportDefinitionService.class);
		try {
			ReportDefinition findDefinition = findReportDefinition(name);
			if (findDefinition != null) {
				rds.purgeDefinition(findDefinition);
			}
		} catch (RuntimeException e) {
			// intentional empty as the author is too long out of business...
		}
	}

	private ReportDefinition findReportDefinition(String name) {
		ReportDefinitionService s = (ReportDefinitionService) Context.getService(ReportDefinitionService.class);
		List<ReportDefinition> defs = s.getDefinitions(name, true);

		for (ReportDefinition def : defs) {
			return def;
		}
		throw new RuntimeException("Couldn't find Definition " + name);
	}

	private ReportDesign createWebDesign(ReportDefinition reportDefinition) {
		ReportDesign design = new ReportDesign();

		design.setReportDefinition(reportDefinition);
		design.setRendererType(DefaultWebRenderer.class);

		return design;
	}

}
