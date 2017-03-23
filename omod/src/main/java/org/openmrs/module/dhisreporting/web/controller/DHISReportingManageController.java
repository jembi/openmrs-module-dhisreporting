/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.dhisreporting.web.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.api.context.Context;
import org.openmrs.module.dhisconnector.api.DHISConnectorService;
import org.openmrs.module.dhisconnector.api.model.DHISImportSummary;
import org.openmrs.module.dhisreporting.api.DHISReportingService;
import org.openmrs.web.WebConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * The main controller.
 */
@Controller
public class DHISReportingManageController {

	protected final Log log = LogFactory.getLog(getClass());

	@RequestMapping(value = "/module/dhisreporting/manage", method = RequestMethod.GET)
	public void manage(ModelMap model) {
		model.addAttribute("user", Context.getAuthenticatedUser());
	}

	@RequestMapping(value = "/module/dhisreporting/manage", method = RequestMethod.POST)
	public void postManage(ModelMap model, HttpServletRequest request) {
		DHISImportSummary feedback = (DHISImportSummary) Context.getService(DHISReportingService.class)
				.runAndSendReportDataForTheCurrentMonth();

		try {
			request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR,
					new ObjectMapper().writeValueAsString(feedback));
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@RequestMapping(value = "/module/dhisreporting/mappings", method = RequestMethod.GET)
	public void mappings(ModelMap model) {
		model.addAttribute("mappings", Context.getService(DHISReportingService.class).getAllMappings());
	}

	@RequestMapping(value = "/module/dhisreporting/mappings", method = RequestMethod.POST)
	public void postMappings(ModelMap model, HttpServletRequest request) {
		model.addAttribute("mappings", Context.getService(DHISReportingService.class).getAllMappings());
		model.addAttribute("dhisOrgUnits", Context.getService(DHISConnectorService.class).getDHIS2APIBackupPath());
		// TODO get datasets using rest web services api from dhisconnector
		// module
		model.addAttribute("openmrsLocations", Context.getLocationService().getAllLocations());
	}

	@RequestMapping(value = "/module/dhisreporting/pepfar", method = RequestMethod.GET)
	public void pepfar(ModelMap model) {
	}

	@RequestMapping(value = "/module/dhisreporting/pepfar", method = RequestMethod.POST)
	public void submitPepfar(ModelMap model, HttpServletRequest request) {
		Context.getService(DHISReportingService.class).pepfarPage(request);
	}
}
