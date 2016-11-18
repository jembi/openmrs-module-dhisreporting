package org.openmrs.module.dhisreporting.web.resource;

import org.openmrs.api.context.Context;
import org.openmrs.module.dhisreporting.OpenMRSToDHISMapping;
import org.openmrs.module.dhisreporting.OpenMRSToDHISMapping.DHISMappingType;
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
		+ "/dhisreporting/openmrstodhismappings", supportedClass = OpenMRSToDHISMapping.class, supportedOpenmrsVersions = {
				"1.8.*", "1.9.*, 1.10.*, 1.11.*", "1.12.*", "2.0.*" })
public class OpenMRSToDHISMappingResource extends DataDelegatingCrudResource implements Retrievable {

	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation arg0) {
		DelegatingResourceDescription description = new DelegatingResourceDescription();
		description.addProperty("openmrsId", Representation.REF);
		description.addProperty("dhisId", Representation.REF);
		description.addProperty("type", Representation.REF);

		return description;
	}

	@Override
	public Object newDelegate() {
		return null;
	}

	@Override
	public Object save(Object arg0) {
		return null;
	}

	@Override
	protected void delete(Object arg0, String arg1, RequestContext arg2) throws ResponseException {
	}

	@Override
	public Object getByUniqueId(String openmrsIdOrCode) {
		return Context.getService(DHISReportingService.class).getMapping(openmrsIdOrCode, DHISMappingType.INDICATOR_);
	}

	@Override
	public void purge(Object arg0, RequestContext arg1) throws ResponseException {
	}

}
