package org.openmrs.module.dhisreporting;

import org.openmrs.Concept;
import org.openmrs.api.context.Context;

public class OpenMRSReportConcepts {
	public static Concept BLOODSMEAR = getConceptFromGPcode(DHISReportingGPConstants.BLOODSMEAR_CONCEPTID);

	public static Concept MICROFILARIA = getConceptFromGPcode(DHISReportingGPConstants.MICROFILARIA_CONCEPTID);

	public static Concept TRYPANOSOMA = getConceptFromGPcode(DHISReportingGPConstants.TRYPANOSOMA_CONCEPTID);

	public static Concept GIARDIA = getConceptFromGPcode(DHISReportingGPConstants.GIARDIA_CONCEPTID);

	public static Concept ASCARIASIS = getConceptFromGPcode(DHISReportingGPConstants.ASCARIASIS_CONCEPTID);

	public static Concept ANKLYOSTIASIS = getConceptFromGPcode(DHISReportingGPConstants.ANKLYOSTIASIS_CONCEPTID);

	public static Concept TAENIA = getConceptFromGPcode(DHISReportingGPConstants.TAENIA_CONCEPTID);

	public static Concept OTHERPARASITES = getConceptFromGPcode(DHISReportingGPConstants.OTHERPARASITES_CONCEPTID);

	public static Concept PREGNANCYTEST = getConceptFromGPcode(DHISReportingGPConstants.PREGNANCYTEST_CONCEPTID);

	public static Concept HEMOGLOBIN = getConceptFromGPcode(DHISReportingGPConstants.HEMOGLOBIN_CONCEPTID);

	public static Concept FULLBLOODCOUNT = getConceptFromGPcode(DHISReportingGPConstants.FULLBLOODCOUNT_CONCEPTID);

	public static Concept CREATINE = getConceptFromGPcode(DHISReportingGPConstants.CREATINE_CONCEPTID);

	public static Concept AMYLASSE = getConceptFromGPcode(DHISReportingGPConstants.AMYLASSE_CONCEPTID);

	public static Concept CD4COUNT = getConceptFromGPcode(DHISReportingGPConstants.CD4COUNT_CONCEPTID);

	public static Concept WIDAL = getConceptFromGPcode(DHISReportingGPConstants.WIDAL_CONCEPTID);

	public static Concept DEREBROSPINALFLUID = getConceptFromGPcode(
			DHISReportingGPConstants.DEREBROSPINALFLUID_CONCEPTID);

	private static Concept getConceptFromGPcode(String code) {
		return Context.getConceptService()
				.getConcept(Integer.parseInt(Context.getAdministrationService().getGlobalProperty(code)));
	}
}
