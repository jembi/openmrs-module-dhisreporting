package org.openmrs.module.dhisreporting;

import org.openmrs.util.OpenmrsUtil;

import java.io.File;

public class DHISReportingConstants {

	/**
	 * TODO maybe rename to be used for the rest of the reports
	 */
	public static String DHIS_DATASET_ONART_UID = "dhisreporting.dataset.onARTUid";

	public static String DHIS_DATASET_HIVSTATUS_UID = "dhisreporting.dataset.hivStatusUid";

	public static String DHIS_DATASET_RHMISANC_UID = "dhisreporting.dataset.rhmisANCUid";

	public static String DHIS_DATASET_QUARTERY_PERIODTYPE = "dhisreporting.dataset.onQuarteryPeriodType";

	public static String DHIS_DATASET_MONTHLY_PERIODTYPE = "dhisreporting.dataset.monthlyPeriodType";

	public static String DXF_TO_ADX_SWITCH = "dhisreporting.config.dxfToAdxSwitch";

	public static String AGEQUERY_INFANT = "dhisreporting.ageQuery.infant";

	public static String ENCOUNTERTYPE_ANC_REPORT_UUID = "pmtct.encounterType.anc";

	public static String DISABLE_BASE_COHORTS = "dhisreporting.baseCohort.disable";

	public static String DISABLE_WEB_REPORTS_DELETION = "dhisreporting.reporting.disableWebReportsDeletion";

	public static String POST_SAMPLE_DHIS_METADATA = "dhisreporting.dhis.postSampleMetaFromMapping";


	private static final String DHISREPORTING_DIRECTORY_NAME = "DHISReporting";

	public static final File DHISREPORTING_DIRECTORY = OpenmrsUtil
			.getDirectoryInApplicationDataDirectory(DHISREPORTING_DIRECTORY_NAME);

	public static final String DHISREPORTING_MAPPING_FILENAME = "dhis-mappings.properties";

	public static final File DHISREPORTING_FINAL_MAPPINGFILE = new File(
			DHISREPORTING_DIRECTORY.getAbsolutePath() + File.separator + DHISREPORTING_MAPPING_FILENAME);

	public static final File DHISREPORTING_TEMP_MAPPINGFILE = new File(
			DHISREPORTING_DIRECTORY.getAbsolutePath() + File.separator + "tmp-" + DHISREPORTING_MAPPING_FILENAME);

	public static final String RPR_CONCEPTID = "dhisreporting.rprConceptId";

	public static String BLOODSMEAR_CONCEPTID = "dhisreporting.bloodSmearConceptId";

	public static String MICROFILARIA_CONCEPTID = "dhisreporting.microFilariaConceptId";

	public static String TRYPANOSOMA_CONCEPTID = "dhisreporting.trypanosomaConceptId";

	public static String GIARDIA_CONCEPTID = "dhisreporting.giardiaConceptId";

	public static String ASCARIASIS_CONCEPTID = "dhisreporting.ascariasisConceptId";

	public static String ANKLYOSTIASIS_CONCEPTID = "dhisreporting.anklyostiasisConceptId";

	public static String TAENIA_CONCEPTID = "dhisreporting.taeniaConceptId";

	public static String OTHERPARASITES_CONCEPTID = "dhisreporting.otherParasitesConceptId";

	public static String PREGNANCYTEST_CONCEPTID = "dhisreporting.pregnancyTestConceptId";

	public static String PCR_CONCEPTID = "dhisreporting.hiv.PCR";

	public static String HEMOGLOBIN_CONCEPTID = "dhisreporting.hemoglobinConceptId";

	public static String FULLBLOODCOUNT_CONCEPTID = "dhisreporting.fullBloodCountConceptId";

	public static String CREATINE_CONCEPTID = "dhisreporting.creatinineConceptId";

	public static String AMYLASSE_CONCEPTID = "dhisreporting.amylasseConceptId";

	public static String CD4COUNT_CONCEPTID = "dhisreporting.cd4CountConceptId";

	public static String WIDAL_CONCEPTID = "dhisreporting.widalConceptId";

	public static String DEREBROSPINALFLUID_CONCEPTID = "dhisreporting.derebrospinalFluidConceptId";

	public static String LAB_REPORT_UUID = "c06091be-a4c0-11e6-80f5-76304dec7eb7";

	public static String DEFAULT_LOCATION_ID = "dhisreporting.defaultLocationId";

	public static String CONFIGURED_ORGUNIT_UID = "dhisreporting.configuredDHISOrgUnitUIdMatchingDefaultLocation";

	public static String VIRALLOAD_CONCEPTID = "dhisreporting.concept.viralLoad";

	public static String EXITCAREREASON_CONCEPTID = "dhisreporting.concept.exitCareReason";

	public static String ARVDRUGS_CONCEPTSETID = "dhisreporting.concept.ARVDrugs";

	public static String HIV_PROGRAMID = "dhisreporting.program.HIV";

	public static String TB_PROGRAMID = "dhisreporting.program.TB";

	public static String PMTCT_PROGRAMID = "dhisreporting.program.PMTCT";

	public static String PREGNANTPATIENT_CONCEPTID = "dhisreporting.patients.pregnant";

	public static String HIVSTATUS_CONCEPTID = "dhisreporting.concept.hivStatus";

	public static String POSITIVE_CONCEPTID = "dhisreporting.concept.positive";

	public static String DHISREPORTING_MER_INDICATORS_FILENAME = "mer-indicators.json";

	public static File DHISREPORTING_MER_INDICATORS_FILE = new File(
			DHISREPORTING_DIRECTORY.getAbsolutePath() + File.separator + DHISREPORTING_MER_INDICATORS_FILENAME);

	public static String REPORT_UUID_PREVENTION = "dhisreporting.reporting.preventionUuid";

	public static String REPORT_UUID_HIVSTATUS = "dhisreporting.reporting.hivStatus";

	public static String REPORT_UUID_ONART = "dhisreporting.reporting.onARTUuid";

	public static String COHORT_UUID_ONART_TB_ART_DENO = "dhisreporting.reporting.cohort.tbARTdeno";

	public static String REPORT_UUID_OTHER = "dhisreporting.reporting.othersUuid";

	public static String REPORT_UUID_ANC = "dhisreporting.reporting.ancUuid";

	public static String INDICATOR_MAPPING_FILE_NAME = "pepfar-meta-datim.csv";

	public static File INDICATOR_MAPPING_FILE = new File(
			DHISREPORTING_DIRECTORY.getAbsolutePath() + File.separator + INDICATOR_MAPPING_FILE_NAME);

	public static String MADE_LOCAL_MAPPING_CHANGES = "dhisreporting.mappings.madeChanges";

	public static String NEW_ON_ART_PERIOD_MONTHS = "dhisreporting.reporting.newOnARTPeriodInMonths";

	public static String DATAELEMENT_DISAGG_SEPARATOR = "__";

	public static String PERIODTYPE_DAILY_HOUR = "dhisreporting.periodType.dailyHour";

	public static String PERIODTYPE_WEEKLY_DAY = "dhisreporting.periodType.weeklyDay";

	public static String PERIODTYPE_MONTHLY_DAY = "dhisreporting.periodType.monthlyDay";

}
