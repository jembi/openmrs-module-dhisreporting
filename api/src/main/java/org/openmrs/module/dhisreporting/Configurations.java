package org.openmrs.module.dhisreporting;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.Concept;
import org.openmrs.Program;
import org.openmrs.api.context.Context;

/**
 * Configurable global properties and objects
 * 
 * @author k-joseph TODO expose these as Rest web services
 */
public class Configurations {

	private Concept getConceptByGpCode(String globalPropertyCode) {
		String concept = Context.getAdministrationService().getGlobalProperty(globalPropertyCode);
		Integer conceptId = concept != null ? Integer.parseInt(concept) : null;

		return conceptId != null ? Context.getConceptService().getConcept(conceptId) : null;
	}
	
	/**
	 * TODO rename this GP to dhisconnector.* instead of dhisreporting.*
	 * @return
	 */
	public Boolean dxfToAdxSwitch() {
		return "true"
				.equals(Context.getAdministrationService().getGlobalProperty(DHISReportingConstants.DXF_TO_ADX_SWITCH));
	}

	public Concept getViralLoadConcept() {
		return getConceptByGpCode(DHISReportingConstants.VIRALLOAD_CONCEPTID);
	}

	public Concept getCD4CountConcept() {
		return getConceptByGpCode(DHISReportingConstants.CD4COUNT_CONCEPTID);
	}

	public Concept getReasonForExitingCareConcept() {
		return getConceptByGpCode(DHISReportingConstants.EXITCAREREASON_CONCEPTID);
	}

	public Concept getARVDrugsConceptSet() {
		return getConceptByGpCode(DHISReportingConstants.ARVDRUGS_CONCEPTSETID);
	}

	public Concept getHIVStatusQuestion() {
		return getConceptByGpCode(DHISReportingConstants.HIVSTATUS_CONCEPTID);
	}

	public Concept getHIVPositiveAnswer() {
		return getConceptByGpCode(DHISReportingConstants.POSITIVE_CONCEPTID);
	}
	
	public Concept getPregnantPatientConcept() {
		return getConceptByGpCode(DHISReportingConstants.PREGNANTPATIENT_CONCEPTID);
	}
	
	public Concept getPCRConcept() {
		return getConceptByGpCode(DHISReportingConstants.PCR_CONCEPTID);
	}

	public Program getHIVProgram() {
		return inProgram(DHISReportingConstants.HIV_PROGRAMID);
	}

	public Program getTBProgram() {
		return inProgram(DHISReportingConstants.TB_PROGRAMID);
	}
	
	public Program getPMTCTProgram() {
		return inProgram(DHISReportingConstants.PMTCT_PROGRAMID);
	}

	private Program inProgram(String programGpCode) {
		String program = Context.getAdministrationService().getGlobalProperty(programGpCode);
		Integer programId = program != null ? Integer.parseInt(program) : null;

		return programId != null ? Context.getProgramWorkflowService().getProgram(programId) : null;
	}

	public Boolean madeLocalMappingsChanges() {
		return "true".equals(Context.getAdministrationService()
				.getGlobalProperty(DHISReportingConstants.MADE_LOCAL_MAPPING_CHANGES));
	}

	public Integer monthsConsideredNewOnART() {
		String period = Context.getAdministrationService()
				.getGlobalProperty(DHISReportingConstants.NEW_ON_ART_PERIOD_MONTHS);

		return StringUtils.isNotBlank(period) ? Integer.parseInt(period) : null;
	}
}
