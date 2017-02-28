package org.openmrs.module.dhisreporting;

import org.openmrs.Concept;
import org.openmrs.Program;
import org.openmrs.api.context.Context;

/**
 * Configurable global properties and objects
 * 
 * @author k-joseph
 *
 */
public class Configurations {

	public static String VIRALLOAD_CONCEPTID = "dhisreporting.concept.viralLoad";

	public static String CD4COUNT_CONCEPTID = "dhisreporting.cd4CountConceptId";

	public static String EXITCAREREASON_CONCEPTID = "dhisreporting.concept.exitCareReason";

	public static String ARVDRUGS_CONCEPTSETID = "dhisreporting.concept.ARVDrugs";

	public static String HIV_PROGRAMID = "dhisreporting.program.HIV";

	public static String TB_PROGRAMID = "dhisreporting.program.TB";

	public static String HIVSTATUS_CONCEPTID = "dhisreporting.concept.hivStatus";

	public static String POSITIVE_CONCEPTID = "dhisreporting.concept.positive";

	private Concept getConceptByGpCode(String globalPropertyCode) {
		String concept = Context.getAdministrationService().getGlobalProperty(globalPropertyCode);
		Integer conceptId = concept != null ? Integer.parseInt(concept) : null;

		return conceptId != null ? Context.getConceptService().getConcept(conceptId) : null;
	}

	public Concept getViralLoadsConcept() {
		return getConceptByGpCode(VIRALLOAD_CONCEPTID);
	}

	public Concept getCD4CountConcept() {
		return getConceptByGpCode(CD4COUNT_CONCEPTID);
	}

	public Concept getReasonForExitingCareConcept() {
		return getConceptByGpCode(EXITCAREREASON_CONCEPTID);
	}

	public Concept getARVDrugsConceptSet() {
		return getConceptByGpCode(ARVDRUGS_CONCEPTSETID);
	}

	public Concept getHIVStatusQuestion() {
		return getConceptByGpCode(HIVSTATUS_CONCEPTID);
	}

	public Concept getHIVPositiveAnswer() {
		return getConceptByGpCode(POSITIVE_CONCEPTID);
	}

	public Program getHIVProgram() {
		return inProgram(HIV_PROGRAMID);
	}

	public Program getTBProgram() {
		return inProgram(TB_PROGRAMID);
	}

	private Program inProgram(String programGpCode) {
		String program = Context.getAdministrationService().getGlobalProperty(programGpCode);
		Integer programId = program != null ? Integer.parseInt(program) : null;

		return programId != null ? Context.getProgramWorkflowService().getProgram(programId) : null;
	}
}
