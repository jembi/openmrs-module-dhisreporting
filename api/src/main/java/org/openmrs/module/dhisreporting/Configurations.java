package org.openmrs.module.dhisreporting;

import org.openmrs.Concept;
import org.openmrs.Program;
import org.openmrs.api.context.Context;

/**
 * Configurable global properties and objects
 * 
 * @author k-joseph
 * TODO expose these as Rest web services
 */
public class Configurations {

	private Concept getConceptByGpCode(String globalPropertyCode) {
		String concept = Context.getAdministrationService().getGlobalProperty(globalPropertyCode);
		Integer conceptId = concept != null ? Integer.parseInt(concept) : null;

		return conceptId != null ? Context.getConceptService().getConcept(conceptId) : null;
	}

	public Boolean getDxfToAdxSwitchConcept() {
		return "true".equals(Context.getAdministrationService().getGlobalProperty(DHISReportingConstants.DXF_TO_ADX_SWITCH));
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

	public Program getHIVProgram() {
		return inProgram(DHISReportingConstants.HIV_PROGRAMID);
	}

	public Program getTBProgram() {
		return inProgram(DHISReportingConstants.TB_PROGRAMID);
	}

	private Program inProgram(String programGpCode) {
		String program = Context.getAdministrationService().getGlobalProperty(programGpCode);
		Integer programId = program != null ? Integer.parseInt(program) : null;

		return programId != null ? Context.getProgramWorkflowService().getProgram(programId) : null;
	}
}
