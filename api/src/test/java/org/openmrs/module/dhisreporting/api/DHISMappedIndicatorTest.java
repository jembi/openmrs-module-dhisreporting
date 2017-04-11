package org.openmrs.module.dhisreporting.api;

import java.util.Properties;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.report.definition.PeriodIndicatorReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.test.BaseModuleContextSensitiveTest;

@Ignore
public class DHISMappedIndicatorTest extends BaseModuleContextSensitiveTest {
	@Override
	public Boolean useInMemoryDatabase() {
		return false;
	}

	@Override
	public Properties getRuntimeProperties() {
		Properties p = super.getRuntimeProperties();
		p.setProperty("connection.url",
				"jdbc:mysql://localhost:3316/openmrs?autoReconnect=true&useUnicode=true&characterEncoding=UTF-8");
		p.setProperty("connection.username", "openmrs");
		p.setProperty("connection.password", "N78qID0A|ZgM");
		p.setProperty("junit.username", "test");
		p.setProperty("junit.password", "Password123");
		return p;
	}

	@Override
	public void deleteAllData() throws Exception {
	}

	@Before
	public void setup() throws Exception {
		if (!Context.isSessionOpen()) {
			Context.openSession();
		}
		Context.clearSession();
		authenticate();
	}

	@Test
	public void test() {
		String openmrsReportUuid = "7e162556-ff30-11e6-bc64-92361f002671";
		PeriodIndicatorReportDefinition r = (PeriodIndicatorReportDefinition) Context
				.getService(ReportDefinitionService.class).getDefinitionByUuid(openmrsReportUuid);

		Assert.assertNotNull(r);
	}

	@After
	public void closeSetup() throws Exception {
		if (Context.isSessionOpen()) {
			Context.closeSession();
		}
	}
}
