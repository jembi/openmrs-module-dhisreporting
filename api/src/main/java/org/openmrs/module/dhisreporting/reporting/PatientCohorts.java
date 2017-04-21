package org.openmrs.module.dhisreporting.reporting;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.Program;
import org.openmrs.api.PatientSetService.TimeModifier;
import org.openmrs.api.context.Context;
import org.openmrs.module.dhisreporting.AgeRange;
import org.openmrs.module.dhisreporting.Configurations;
import org.openmrs.module.dhisreporting.DHISReportingConstants;
import org.openmrs.module.dhisreporting.Gender;
import org.openmrs.module.dhisreporting.NumberToWord;
import org.openmrs.module.dhisreporting.WordToNumber;
import org.openmrs.module.dhisreporting.api.DHISReportingService;
import org.openmrs.module.dhisreporting.mapping.CodedDisaggregation;
import org.openmrs.module.reporting.cohort.definition.AgeCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CodedObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.GenderCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.InverseCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.ProgramEnrollmentCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.common.DurationUnit;
import org.openmrs.module.reporting.common.SetComparator;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.openmrs.module.reporting.indicator.dimension.CohortDefinitionDimension;

public class PatientCohorts {
	private Configurations config = new Configurations();

	public GenderCohortDefinition genderPatients(Gender gender) {
		GenderCohortDefinition cd = new GenderCohortDefinition();

		if (gender.equals(Gender.FEMALE)) {
			cd.setName("Female");
			cd.setDescription("Patients whose gender is F");
			cd.setFemaleIncluded(true);
		} else if (gender.equals(Gender.MALE)) {
			cd.setName("Male");
			cd.setDescription("Patients whose gender is M");
			cd.setMaleIncluded(true);
		} else {
			cd.setName("Unknown");
			cd.setDescription("Patients whose gender is not known");
			cd.setUnknownGenderIncluded(true);
		}

		return cd;
	}

	public SqlCohortDefinition createParameterizedLocationCohort(String name) {
		SqlCohortDefinition location = new SqlCohortDefinition();
		location.setQuery(
				"select p.patient_id from patient p, person_attribute pa, person_attribute_type pat where p.patient_id = pa.person_id and pat.name ='Health Center' and pa.voided = 0 and pat.person_attribute_type_id = pa.person_attribute_type_id and pa.value = :location");
		location.setName(name);
		location.addParameter(new Parameter("location", "location", Location.class));
		return location;
	}

	public AgeCohortDefinition patientsInAgeRange(AgeRange ageRange) {
		AgeCohortDefinition cd = new AgeCohortDefinition();

		if (ageRange != null) {

			try {
				cd.setMinAge(ageRange.getMinAge());
				cd.setMinAgeUnit(ageRange.getMinAgeUnit());
				cd.setMaxAge(ageRange.getMaxAge());
				cd.setMaxAgeUnit(ageRange.getMaxAgeUnit());
				cd.setName(getBetteredAgeRangeString(ageRange));

				cd.setDescription(ageRange.getMinAge() != null && ageRange.getMaxAge() != null
						? ageRange.getMinAge() + " " + ageRange.getMinAgeUnit() + " to " + ageRange.getMaxAgeUnit()
						: null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return cd;
	}

	private String getBetteredAgeRangeString(AgeRange ageRange) throws Exception {
		String name = "";

		if ("zeroToZero".equals(ageRange.toWordString())) {
			name += "belowOne";
		} else {
			if (ageRange.toWordString().startsWith("zeroTo")) {
				name += "below" + capitalizeFirstLetter(NumberToWord
						.convert(Math.round(WordToNumber.convert(ageRange.toWordString().replace("zeroTo", "")) + 1)));
			} else {
				name += ageRange.toWordString();
			}

		}

		name += "OfAge";

		return name;
	}

	private String capitalizeFirstLetter(String input) {
		return input.substring(0, 1).toUpperCase() + input.substring(1);
	}

	private ProgramEnrollmentCohortDefinition inPrograms(String name, List<Program> programs) {
		ProgramEnrollmentCohortDefinition pc = new ProgramEnrollmentCohortDefinition();

		addBasicPeriodIndicatorParameters(pc);
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

	public ProgramEnrollmentCohortDefinition inPMTCTProgram() {
		List<Program> programs = new ArrayList<Program>();

		programs.add(config.getPMTCTProgram());
		return inPrograms("in PMTCT Program", programs);
	}

	/**
	 * An Active patient according to Rwanda is one enrolled within the HIV
	 * program and is not exited from care as well as taking ARV or HIV
	 * medication
	 * 
	 * @return
	 */
	public SqlCohortDefinition hivActiveARTPatients() {
		SqlCohortDefinition sc = new SqlCohortDefinition();
		String sql = "select distinct pp.patient_id from obs o inner join patient_program pp on o.person_id = pp.patient_id inner join orders ord on o.person_id = ord.patient_id where o.concept_id != "
				+ config.getReasonForExitingCareConcept().getConceptId() + " and pp.program_id = "
				+ config.getHIVProgram().getProgramId()
				+ " and ord.concept_id in (select distinct concept_id from concept_set where pp.date_completed is null and concept_set = "
				+ config.getARVDrugsConceptSet().getConceptId()
				+ ") and (o.obs_datetime between :startDate and :endDate)";
		// ^ TODO should this be order startdate comparison or obs datatime

		sc.setName("Active HIV ART Patients");
		addBasicPeriodIndicatorParameters(sc);
		sc.setQuery(sql);

		return sc;
	}

	public SqlCohortDefinition hivNewArtPatients() {
		SqlCohortDefinition sc = new SqlCohortDefinition();
		String sql = "select distinct pp.patient_id from obs o inner join patient_program pp on o.person_id = pp.patient_id inner join orders ord on o.person_id = ord.patient_id where o.concept_id != "
				+ config.getReasonForExitingCareConcept().getConceptId() + " and pp.program_id = "
				+ config.getHIVProgram().getProgramId()
				+ " and ord.concept_id in (select distinct concept_id from concept_set where pp.date_completed is null and concept_set = "
				+ config.getARVDrugsConceptSet().getConceptId()
				+ ") and (o.obs_datetime between :startDate and :endDate)"
				+ (config.monthsConsideredNewOnART() != null ? " and o.obs_datetime >= (CURRENT_DATE() - INTERVAL "
						+ config.monthsConsideredNewOnART() + " MONTH)" : "");
		// ^ TODO should this be order startdate comparison or obs datatime

		sc.setName("New HIV ART Patients");
		addBasicPeriodIndicatorParameters(sc);
		sc.setQuery(sql);

		return sc;
	}

	private void addBasicPeriodIndicatorParameters(CohortDefinition cd) {
		cd.addParameter(new Parameter("startDate", "startDate", Date.class));
		cd.addParameter(new Parameter("endDate", "endDate", Date.class));
		cd.addParameter(new Parameter("location", "location", Location.class));
	}

	public InverseCohortDefinition nonHIVActiveARTPatients() {
		return new InverseCohortDefinition(hivActiveARTPatients());
	}

	public CodedObsCohortDefinition hivPositivePatients() {
		return createCodedObsCohortDefinition(config.getHIVStatusQuestion(), config.getHIVPositiveAnswer(),
				SetComparator.IN, TimeModifier.LAST);
	}

	private CodedObsCohortDefinition createCodedObsCohortDefinition(Concept question, Concept value,
			SetComparator setComparator, TimeModifier timeModifier) {
		if (question != null) {
			CodedObsCohortDefinition obsCohortDefinition = new CodedObsCohortDefinition();
			List<Concept> valueList = new ArrayList<Concept>();

			// TODO is using onOrBefore equal to making use of endDate!!!
			addBasicPeriodIndicatorParameters(obsCohortDefinition);
			if (question.getName() != null && value != null && value.getName() != null)
				obsCohortDefinition.setName(
						value.getName().getName().replace(" ", "") + question.getName().getName().replace(" ", ""));
			else
				obsCohortDefinition.setName(
						"codedCohort#" + question.getConceptId() + (value != null ? "_" + value.getConceptId() : ""));
			obsCohortDefinition.setQuestion(question);
			obsCohortDefinition.setOperator(setComparator);
			obsCohortDefinition.setTimeModifier(timeModifier);

			if (value != null) {
				valueList.add(value);
				obsCohortDefinition.setValueList(valueList);
			}

			return obsCohortDefinition;
		}
		return null;
	}

	/**
	 * 
	 * @param genders
	 * @return
	 */
	public CohortDefinitionDimension createGenderDimension(String gender) {
		if (StringUtils.isNotBlank(gender)) {
			CohortDefinitionDimension genderDimension = new CohortDefinitionDimension();

			if ("Male".equalsIgnoreCase(gender)) {
				genderDimension.setName("Male");
				genderDimension.addCohortDefinition("Male", genderPatients(Gender.MALE), null);
			} else if ("Female".equalsIgnoreCase(gender)) {
				genderDimension.setName("Female");
				genderDimension.addCohortDefinition("Female", genderPatients(Gender.FEMALE), null);
			}
			genderDimension
					.addParameter(new Parameter(genderDimension.getName(), genderDimension.getName(), String.class));
			return genderDimension;
		}
		return null;
	}

	/**
	 * @param ageQuery
	 * @param durationUnit
	 * @return
	 */
	public CohortDefinitionDimension createAgeDimension(String ageQuery, DurationUnit durationUnit) {
		if (StringUtils.isNotBlank(ageQuery)) {
			CohortDefinitionDimension ageDimension = new CohortDefinitionDimension();
			AgeRange ar = new AgeRange(ageQuery, durationUnit, durationUnit);
			AgeCohortDefinition ad = patientsInAgeRange(ar);

			ageDimension.setName(ad.getName());
			ageDimension.addCohortDefinition(ad.getName(), ad, null);
			ageDimension.addParameter(new Parameter(ageDimension.getName(), ageDimension.getName(), AgeRange.class));

			return ageDimension;
		}
		return null;
	}

	public CohortDefinitionDimension createCodedQuestionDimension(Integer question, String value) {
		if (question != null && StringUtils.isNotBlank(value)) {
			Concept q = Context.getConceptService().getConcept(question);
			Concept a = CodedDisaggregation.matchCodedQuestionDisaggregation(question, value);

			if (q != null && a != null) {
				CohortDefinitionDimension cd = new CohortDefinitionDimension();
				CodedObsCohortDefinition qa = createCodedObsCohortDefinition(q, a, SetComparator.IN, TimeModifier.LAST);

				cd.setName(value);
				cd.addCohortDefinition(qa.getName(), qa, startAndEndDatesMappings());

				return cd;
			}
		}
		return null;
	}

	/**
	 * pregnant is basically enrollment in PMTCT
	 * 
	 * @return
	 */
	public CompositionCohortDefinition inPMTCTHIVPostivePatients() {
		CompositionCohortDefinition c = new CompositionCohortDefinition();

		c.addSearch("inPMTCT", inPMTCTProgram(), startAndEndDatesMappings());
		// enrollment in PMTCT means HIV+
		c.addSearch("hivPositive", hivPositivePatients(), startAndEndDatesMappings());

		return c;
	}

	public CodedObsCohortDefinition pregnantPatients() {
		String pregConceptId = Context.getAdministrationService()
				.getGlobalProperty(DHISReportingConstants.PREGNANTPATIENT_CONCEPTID);

		if (StringUtils.isNotBlank(pregConceptId))
			return createCodedObsCohortDefinition(config.getPregnantPatientConcept(), null, SetComparator.IN,
					TimeModifier.LAST);
		return null;
	}

	public CompositionCohortDefinition infantsWhoHadPCRTestInNMonths(Integer numberOfMonths) {
		CompositionCohortDefinition c = new CompositionCohortDefinition();
		String infantAgeQuery = Context.getAdministrationService()
				.getGlobalProperty(DHISReportingConstants.AGEQUERY_INFANT);
		AgeRange infant = Context.getService(DHISReportingService.class).convertAgeQueryToAgeRangeObject(infantAgeQuery,
				DurationUnit.YEARS, DurationUnit.YEARS);
		AgeCohortDefinition infants = patientsInAgeRange(infant);

		//TODO within first n months
		CodedObsCohortDefinition withPCRTest = createCodedObsCohortDefinition(
				config.getPCRConcept(), null, SetComparator.IN, TimeModifier.LAST);

		
		c.addSearch("infants", infants, startAndEndDatesMappings());
		c.addSearch("pcrTest", withPCRTest, startAndEndDatesMappings());

		return c;
	}

	public Map<String, Object> startAndEndDatesMappings() {
		return ParameterizableUtil.createParameterMappings(
				"startDate=${startDate},endDate=${endDate},onOrAfter=${startDate},onOrBefore=${endDate}");
	}
}
