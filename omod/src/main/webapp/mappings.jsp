<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<%@ include file="template/localHeader.jsp"%>

<table>
	<thead>
		<tr>
			<th>OpenMRS Object Name/ID</th>
			<th>DHIS Object Name/ID</th>
			<th>Mapping Type</th>
			<th>Action</th>
		</tr>
	</thead>
	<tbody>
		<c:forEach items="${mappings}" var="mapping">
			<tr>
				<td>${mapping.openmrsId}</td>
				<td>${mapping.dhisId}</td>
				<td>${mapping.type}</td>
				<td style="float: right;">
					<span><input onClick="editMapping(${mapping.openmrsId}, ${mapping.type})" type="image" src="../../images/edit.gif" title='<spring:message code="general.edit" />'></span>
					<span><input onClick="deleteMapping(${mapping.openmrsId}, ${mapping.type})" type="image" src="../../images/delete.gif" title='<spring:message code="general.delete" />'></span>
				</td>
			</tr>
		</c:forEach>
	</tbody>
</table>
<%@ include file="/WEB-INF/template/footer.jsp"%>