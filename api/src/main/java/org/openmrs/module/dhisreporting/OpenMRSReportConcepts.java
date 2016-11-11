package org.openmrs.module.dhisreporting;

import org.openmrs.Concept;
import org.openmrs.api.context.Context;

public class OpenMRSReportConcepts {
	public static Concept BLOODSMEAR = getConceptFromGPcode(DHISReportingConstants.BLOODSMEAR_CONCEPTID);

	public static Concept MICROFILARIA = getConceptFromGPcode(DHISReportingConstants.MICROFILARIA_CONCEPTID);

	public static Concept TRYPANOSOMA = getConceptFromGPcode(DHISReportingConstants.TRYPANOSOMA_CONCEPTID);

	public static Concept GIARDIA = getConceptFromGPcode(DHISReportingConstants.GIARDIA_CONCEPTID);

	public static Concept ASCARIASIS = getConceptFromGPcode(DHISReportingConstants.ASCARIASIS_CONCEPTID);

	public static Concept ANKLYOSTIASIS = getConceptFromGPcode(DHISReportingConstants.ANKLYOSTIASIS_CONCEPTID);

	public static Concept TAENIA = getConceptFromGPcode(DHISReportingConstants.TAENIA_CONCEPTID);

	public static Concept OTHERPARASITES = getConceptFromGPcode(DHISReportingConstants.OTHERPARASITES_CONCEPTID);

	public static Concept PREGNANCYTEST = getConceptFromGPcode(DHISReportingConstants.PREGNANCYTEST_CONCEPTID);

	public static Concept HEMOGLOBIN = getConceptFromGPcode(DHISReportingConstants.HEMOGLOBIN_CONCEPTID);

	public static Concept RPR = getConceptFromGPcode(DHISReportingConstants.RPR_CONCEPTID);

	public static Concept FULLBLOODCOUNT = getConceptFromGPcode(DHISReportingConstants.FULLBLOODCOUNT_CONCEPTID);

	public static Concept CREATINE = getConceptFromGPcode(DHISReportingConstants.CREATINE_CONCEPTID);

	public static Concept AMYLASSE = getConceptFromGPcode(DHISReportingConstants.AMYLASSE_CONCEPTID);

	public static Concept CD4COUNT = getConceptFromGPcode(DHISReportingConstants.CD4COUNT_CONCEPTID);

	public static Concept WIDAL = getConceptFromGPcode(DHISReportingConstants.WIDAL_CONCEPTID);

	public static Concept DEREBROSPINALFLUID = getConceptFromGPcode(
			DHISReportingConstants.DEREBROSPINALFLUID_CONCEPTID);

	private static Concept getConceptFromGPcode(String code) {
		return Context.getConceptService()
				.getConcept(Integer.parseInt(Context.getAdministrationService().getGlobalProperty(code)));
	}
}
