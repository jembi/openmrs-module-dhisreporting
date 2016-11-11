package org.openmrs.module.dhisreporting;

import java.io.File;

import org.openmrs.util.OpenmrsUtil;

public class DHISReportingConstants {

	private static final String DHISREPORTING_DIRECTORY_NAME = "DHISReporting";

	public static final File DHISREPORTING_DIRECTORY = OpenmrsUtil
			.getDirectoryInApplicationDataDirectory(DHISREPORTING_DIRECTORY_NAME);

	public static final String DHISREPORTING_MAPPING_FILENAME = "dhis-mappings.properties";

	public static final File DHISREPORTING_FINAL_MAPPINGFILE = new File(
			DHISREPORTING_DIRECTORY.getAbsolutePath() + File.separator + DHISREPORTING_MAPPING_FILENAME);

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

	public static String HEMOGLOBIN_CONCEPTID = "dhisreporting.hemoglobinConceptId";

	public static String FULLBLOODCOUNT_CONCEPTID = "dhisreporting.fullBloodCountConceptId";

	public static String CREATINE_CONCEPTID = "dhisreporting.creatinineConceptId";

	public static String AMYLASSE_CONCEPTID = "dhisreporting.amylasseConceptId";

	public static String CD4COUNT_CONCEPTID = "dhisreporting.cd4CountConceptId";

	public static String WIDAL_CONCEPTID = "dhisreporting.widalConceptId";

	public static String DEREBROSPINALFLUID_CONCEPTID = "dhisreporting.derebrospinalFluidConceptId";

	public static String LAB_REPORT_UUID = "c06091be-a4c0-11e6-80f5-76304dec7eb7";

	public static String DEFAULT_LOCATION_ID = "dhisreporting.defaultLocationId";

	public static String CONFIGURED_ORGUNIT_CODE = "dhisreporting.configuredDHISOrgUnitFosIdMatchingDefaultLocation";
}
