package org.openmrs.module.dhisreporting.web.resource;

import org.openmrs.api.context.Context;
import org.openmrs.module.dhisreporting.MerIndicator;
import org.openmrs.module.dhisreporting.api.DHISReportingService;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.Retrievable;
import org.openmrs.module.webservices.rest.web.resource.impl.DataDelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.response.ResponseException;

@Resource(name = RestConstants.VERSION_1
		+ "/dhisreporting/merindicators", supportedClass = MerIndicator.class, supportedOpenmrsVersions = { "1.8.*",
				"1.9.*, 1.10.*, 1.11.*", "1.12.*", "2.0.*" })

public class MerIndicatorResource extends DataDelegatingCrudResource implements Retrievable {

	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation arg0) {
		DelegatingResourceDescription description = new DelegatingResourceDescription();

		description.addProperty("indicatorName", Representation.REF);
		description.addProperty("indicatorDescription", Representation.REF);
		description.addProperty("indicatorCode", Representation.REF);
		description.addProperty("numerator", Representation.REF);
		description.addProperty("denominator", Representation.REF);
		description.addProperty("aggregation", Representation.REF);
		description.addProperty("disaggregation", Representation.REF);
		description.addProperty("openmrsReportRefs", Representation.REF);

		return description;
	}

	@Override
	public Object newDelegate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object save(Object arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void delete(Object arg0, String arg1, RequestContext arg2) throws ResponseException {
		// TODO Auto-generated method stub

	}

	@Override
	public Object getByUniqueId(String merIndicatorCode) {
		return Context.getService(DHISReportingService.class).getMerIndicator(merIndicatorCode);
	}

	@Override
	public void purge(Object arg0, RequestContext arg1) throws ResponseException {
		// TODO Auto-generated method stub

	}

}
