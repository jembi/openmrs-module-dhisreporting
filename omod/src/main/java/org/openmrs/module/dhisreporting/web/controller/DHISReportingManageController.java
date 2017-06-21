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

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.api.context.Context;
import org.openmrs.module.dhisconnector.api.DHISConnectorService;
import org.openmrs.module.dhisconnector.api.model.DHISImportSummary;
import org.openmrs.module.dhisreporting.DHISReportingConstants;
import org.openmrs.module.dhisreporting.MappedIndicatorReport;
import org.openmrs.module.dhisreporting.api.DHISReportingService;
import org.openmrs.web.WebConstants;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * The main controller.
 */
@Controller
public class DHISReportingManageController {
	public static final String DHISCONNECTOR_TEMP_FOLDER = File.separator + "dhisconnector" + File.separator + "temp";

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
		model.addAttribute("response", new ArrayList<String>());
	}

	@RequestMapping(value = "/module/dhisreporting/pepfar", method = RequestMethod.POST)
	public void submitPepfar(ModelMap model, HttpServletRequest request) {
		model.addAttribute("response", Context.getService(DHISReportingService.class).pepfarPage(request));
	}

	@RequestMapping(value = "/module/dhisreporting/dynamicReports", method = RequestMethod.GET)
	public void renderDynamicReportsPage(ModelMap model) {
		List<MappedIndicatorReport> allMappedIndicatorReports = Context.getService(DHISReportingService.class)
				.getAllMappedIndicatorReports();

		model.addAttribute("mappingsMeta",
				Context.getService(DHISReportingService.class).getMappedIndicatorReportExistingMeta().toJSONString());
		try {
			model.addAttribute("mappedIndicatorReports",
					new ObjectMapper().writeValueAsString(allMappedIndicatorReports));
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@RequestMapping(value = "/module/dhisreporting/exportMapping", method = RequestMethod.GET)
	public void exportMappingRenderer(ModelMap model) {
	}

	@RequestMapping(value = "/module/dhisreporting/exportMapping", method = RequestMethod.POST)
	public void exportMapping(ModelMap model, HttpServletRequest request, HttpServletResponse response) {
		try {
			exportFile(response, DHISReportingConstants.INDICATOR_MAPPING_FILE);
		} catch (FileNotFoundException e) {
			request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "No Mapping is available for export!!!");
			e.printStackTrace();
		} catch (IOException e) {
			request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
					"Something went wrong, check server logs!!!");
			e.printStackTrace();
		}
	}

	private void exportFile(HttpServletResponse response, File downloadFile) throws FileNotFoundException, IOException {
		if (downloadFile != null && downloadFile.exists()) {
			FileInputStream inputStream = new FileInputStream(downloadFile);
			String mimeType = "application/octet-stream";

			System.out.println("MIME type: " + mimeType);
			response.setContentType(mimeType);
			response.setContentLength((int) downloadFile.length());

			String headerKey = "Content-Disposition";
			String headerValue = String.format("attachment; filename=\"%s\"", downloadFile.getName());

			response.setHeader(headerKey, headerValue);

			OutputStream outStream = response.getOutputStream();
			byte[] buffer = new byte[4096];
			int bytesRead = -1;

			while ((bytesRead = inputStream.read(buffer)) != -1) {
				outStream.write(buffer, 0, bytesRead);
			}
			inputStream.close();
			outStream.close();
		}
	}

	@RequestMapping(value = "/module/dhisreporting/importMapping", method = RequestMethod.GET)
	public void importMappingRenderer(ModelMap model) {
	}

	@RequestMapping(value = "/module/dhisreporting/importMapping", method = RequestMethod.POST)
	public void importMapping(ModelMap model, HttpServletRequest request,
			@RequestParam(value = "mapping", required = false) MultipartFile mapping) {
		importMapping(mapping, request);
	}

	private void importMapping(MultipartFile mapping, HttpServletRequest request) {
		try {
			OutputStream out = new FileOutputStream(DHISReportingConstants.INDICATOR_MAPPING_FILE);

			IOUtils.copy(mapping.getInputStream(), out);
			request.getSession().setAttribute(WebConstants.OPENMRS_MSG_ATTR,
					"You have Successfully imported mapping file!");
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
			request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR,
					"Importing mapping file failed: !" + e.getMessage());
		}

	}
}
