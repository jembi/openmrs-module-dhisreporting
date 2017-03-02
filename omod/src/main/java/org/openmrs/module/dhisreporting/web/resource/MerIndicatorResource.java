package org.openmrs.module.dhisreporting.web.resource;

import java.util.ArrayList;
import java.util.List;

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
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/rest/" + RestConstants.VERSION_1 + "/dhisreporting/merindicators")
@SuppressWarnings({ "unchecked", "rawtypes" })
@Resource(name = RestConstants.VERSION_1
		+ "/dhisreporting/merindicators", supportedClass = MerIndicator.class, supportedOpenmrsVersions = { "1.8.*",
				"1.9.*, 1.10.*, 1.11.*", "1.12.*", "2.0.*" })
public class MerIndicatorResource extends DataDelegatingCrudResource implements Retrievable {

	@Override
	public DelegatingResourceDescription getRepresentationDescription(Representation arg0) {
		DelegatingResourceDescription description = new DelegatingResourceDescription();

		description.addProperty("indicatorName");
		description.addProperty("indicatorDescription");
		description.addProperty("indicatorCode");
		description.addProperty("numerator");
		description.addProperty("denominator");
		// description.addProperty("aggregation");
		// description.addProperty("disaggregation");
		// description.addProperty("openmrsReportRefs");

		return description;
	}

	@Override
	public DelegatingResourceDescription getCreatableProperties() {
		DelegatingResourceDescription description = new DelegatingResourceDescription();

		description.addProperty("indicatorName");
		description.addProperty("indicatorDescription");
		description.addProperty("indicatorCode");
		description.addProperty("numerator");
		description.addProperty("denominator");

		return description;
	}

	@Override
	protected NeedsPaging<MerIndicator> doGetAll(RequestContext context) {
		List<MerIndicator> mappings = Context.getService(DHISReportingService.class).getMerIndicators(null, null, null);

		if (mappings == null)
			mappings = new ArrayList<MerIndicator>();

		return new NeedsPaging<MerIndicator>(mappings, context);
	}

	@Override
	public MerIndicator newDelegate() {
		return null;// TODO new MerIndicator();
	}

	@Override
	public MerIndicator save(Object arg0) {
		// TODO
		return null;
	}

	@Override
	protected void delete(Object arg0, String arg1, RequestContext arg2) throws ResponseException {
		// TODO
	}

	@Override
	public MerIndicator getByUniqueId(String merIndicatorCode) {
		return Context.getService(DHISReportingService.class).getMerIndicator(merIndicatorCode);
	}

	@Override
	public void purge(Object arg0, RequestContext arg1) throws ResponseException {

	}

}
