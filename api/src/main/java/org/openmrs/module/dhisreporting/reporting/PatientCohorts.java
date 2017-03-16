package org.openmrs.module.dhisreporting.reporting;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.openmrs.Concept;
import org.openmrs.Program;
import org.openmrs.api.PatientSetService.TimeModifier;
import org.openmrs.module.dhisreporting.AgeRange;
import org.openmrs.module.dhisreporting.Configurations;
import org.openmrs.module.dhisreporting.Gender;
import org.openmrs.module.reporting.cohort.definition.AgeCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CodedObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.GenderCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.InverseCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.ProgramEnrollmentCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.common.DurationUnit;
import org.openmrs.module.reporting.common.SetComparator;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.indicator.dimension.CohortDefinitionDimension;

public class PatientCohorts {
	private Configurations config = new Configurations();

	public GenderCohortDefinition genderPatients(Gender gender) {
		GenderCohortDefinition cd = new GenderCohortDefinition();

		if (gender.equals(Gender.FEMALE)) {
			cd.setName("femaleGender");
			cd.setDescription("Patients whose gender is F");
			cd.setFemaleIncluded(true);
		} else if (gender.equals(Gender.MALE)) {
			cd.setName("maleGender");
			cd.setDescription("Patients whose gender is M");
			cd.setMaleIncluded(true);
		} else {
			cd.setName("unknownGender");
			cd.setDescription("Patients whose gender is not known");
			cd.setUnknownGenderIncluded(true);
		}

		return cd;
	}

	public AgeCohortDefinition patientsInAgeRange(AgeRange ageRange) {
		AgeCohortDefinition cd = new AgeCohortDefinition();

		if (ageRange != null) {
			cd.setMinAge(ageRange.getMinAge());
			cd.setMinAgeUnit(ageRange.getMinAgeUnit());
			cd.setMaxAge(ageRange.getMaxAge());
			cd.setMaxAgeUnit(ageRange.getMaxAgeUnit());
			cd.setName(ageRange.toWordString() + "ofAge");
			cd.setDescription(ageRange.getMinAge() != null && ageRange.getMaxAge() != null
					? ageRange.getMinAge() + " " + ageRange.getMinAgeUnit() + " to " + ageRange.getMaxAgeUnit() : null);
		}

		return cd;
	}

	private ProgramEnrollmentCohortDefinition inPrograms(String name, List<Program> programs) {
		ProgramEnrollmentCohortDefinition pc = new ProgramEnrollmentCohortDefinition();

		pc.setName(name);
		pc.setPrograms(programs);

		return pc;
	}

	public ProgramEnrollmentCohortDefinition inHIVProgram() {
		List<Program> programs = new ArrayList<Program>();

		programs.add(config.getHIVProgram());
		return inPrograms("in HIV Program", programs);
	}

	public ProgramEnrollmentCohortDefinition inTBProgram() {
		List<Program> programs = new ArrayList<Program>();

		programs.add(config.getTBProgram());
		return inPrograms("in TB Program", programs);
	}

	/**
	 * An Active patient according to Rwanda is one enrolled within the HIV
	 * program and is not exited from care as well as taking ARV or HIV
	 * medication
	 * 
	 * @return
	 */
	public SqlCohortDefinition hivActivePatients() {
		SqlCohortDefinition sc = new SqlCohortDefinition();
		String sql = "select distinct pp.patient_id from obs o inner join patient_program pp on o.person_id = pp.patient_id inner join orders ord on o.person_id = ord.patient_id where o.concept_id != "
				+ config.getReasonForExitingCareConcept().getConceptId() + " and program_id = "
				+ config.getHIVProgram().getProgramId()
				+ " and ord.concept_id in (select distinct concept_id from concept_set where pp.date_completed is null and concept_set = "
				+ config.getARVDrugsConceptSet().getConceptId()
				+ ") and (o.obs_datetime between :startDate and :endDate)";

		sc.setName("Active HIV Patients");
		sc.addParameter(new Parameter("startDate", "startDate", Date.class));
		sc.addParameter(new Parameter("endDate", "endDate", Date.class));
		sc.setQuery(sql);

		return sc;
	}

	public InverseCohortDefinition nonHIVActivePatients() {
		return new InverseCohortDefinition(hivActivePatients());
	}

	public CodedObsCohortDefinition hivPositivePatients() {
		return createCodedObsCohortDefinition(config.getHIVStatusQuestion(), config.getHIVPositiveAnswer(),
				SetComparator.IN, TimeModifier.LAST);
	}

	private CodedObsCohortDefinition createCodedObsCohortDefinition(Concept question, Concept value,
			SetComparator setComparator, TimeModifier timeModifier) {
		CodedObsCohortDefinition obsCohortDefinition = new CodedObsCohortDefinition();

		obsCohortDefinition.setQuestion(question);
		obsCohortDefinition.setOperator(setComparator);
		obsCohortDefinition.setTimeModifier(timeModifier);

		List<Concept> valueList = new ArrayList<Concept>();
		if (value != null) {
			valueList.add(value);
			obsCohortDefinition.setValueList(valueList);
		}
		return obsCohortDefinition;
	}

	public CohortDefinitionDimension createGenderDimension(String[] genders) {
		if (genders != null && genders.length > 0) {
			CohortDefinitionDimension genderDimension = new CohortDefinitionDimension();
			genderDimension.setName("gender");
			for (int i = 0; i < genders.length; i++) {
				if ("Male".equalsIgnoreCase(genders[i]))
					genderDimension.addCohortDefinition("male", genderPatients(Gender.MALE), null);
				else if ("Female".equalsIgnoreCase(genders[i]))
					genderDimension.addCohortDefinition("female", genderPatients(Gender.FEMALE), null);
			}
			return genderDimension;
		}
		return null;
	}

	public CohortDefinitionDimension createAgeDimension(String[] ageQueries, DurationUnit durationUnit) {
		if (ageQueries != null && ageQueries.length > 0) {
			CohortDefinitionDimension ageDimension = new CohortDefinitionDimension();

			ageDimension.setName("age");
			for (int i = 0; i < ageQueries.length; i++) {
				AgeRange ar = new AgeRange(ageQueries[i], durationUnit, durationUnit);
				AgeCohortDefinition ad = patientsInAgeRange(ar);

				ageDimension.addCohortDefinition(ad.getName(), ad, null);
			}
			return ageDimension;
		}
		return null;
	}
}
