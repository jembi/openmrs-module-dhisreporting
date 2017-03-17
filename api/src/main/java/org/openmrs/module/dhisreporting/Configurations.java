package org.openmrs.module.dhisreporting;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.Concept;
import org.openmrs.Location;
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

	public Boolean madeLocalMappingsChanges() {
		return "true".equals(Context.getAdministrationService()
				.getGlobalProperty(DHISReportingConstants.MADE_LOCAL_MAPPING_CHANGES));
	}

	public Location getCurrentOpenmrsLocationMatchedWithDHIS2() {
		String mapping = Context.getAdministrationService()
				.getGlobalProperty(DHISReportingConstants.MATCHOPENMRSLOCATION_TO_DHIS2_ORGUNIT);

		return StringUtils.isNotBlank(mapping) && mapping.indexOf(":") > -1
				? Context.getLocationService().getLocationByUuid(mapping.split(":")[0]) : null;
	}

	public String getCurrentDHIS2OrgUnitUidMapped() {
		String mapping = Context.getAdministrationService()
				.getGlobalProperty(DHISReportingConstants.MATCHOPENMRSLOCATION_TO_DHIS2_ORGUNIT);
		return StringUtils.isNotBlank(mapping) && mapping.indexOf(":") > -1 ? mapping.split(":")[1] : null;
	}
}
