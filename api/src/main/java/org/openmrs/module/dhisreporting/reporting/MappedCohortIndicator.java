package org.openmrs.module.dhisreporting.reporting;

import org.openmrs.module.dhisreporting.mapping.IndicatorMapping;
import org.openmrs.module.reporting.indicator.CohortIndicator;

public class MappedCohortIndicator {
	private CohortIndicator cohortIndicator;
	private IndicatorMapping mapping;

	public MappedCohortIndicator(CohortIndicator cohortIndicator, IndicatorMapping mapping) {
		setCohortIndicator(cohortIndicator);
		setMapping(mapping);
	}

	public CohortIndicator getCohortIndicator() {
		return cohortIndicator;
	}

	public void setCohortIndicator(CohortIndicator cohortIndicator) {
		this.cohortIndicator = cohortIndicator;
	}

	public IndicatorMapping getMapping() {
		return mapping;
	}

	public void setMapping(IndicatorMapping mapping) {
		this.mapping = mapping;
	}

}
