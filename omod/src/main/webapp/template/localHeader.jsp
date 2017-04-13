<spring:htmlEscape defaultHtmlEscape="true" />
<ul id="menu">
	<li class="first"><a
		href="${pageContext.request.contextPath}/admin"><spring:message
				code="admin.title.short" /></a></li>

	<li
		<c:if test='<%= request.getRequestURI().contains("/pepfar") %>'>class="active"</c:if>>
		<a
		href="${pageContext.request.contextPath}/module/dhisreporting/pepfar.form">PEPFAR MER 2.0 Indicator Reports</a>
	</li>
	
	<li
		<c:if test='<%= request.getRequestURI().contains("/dynamicReports") %>'>class="active"</c:if>>
		<a
		href="${pageContext.request.contextPath}/module/dhisreporting/dynamicReports.form">Dynamic Period Indicator Reports</a>
	</li>
	<li
		<c:if test='<%= request.getRequestURI().contains("/exportMapping") %>'>class="active"</c:if>>
		<a
		href="${pageContext.request.contextPath}/module/dhisreporting/exportMapping.form">Export Mapping</a>
	</li>
	<li
		<c:if test='<%= request.getRequestURI().contains("/importMapping") %>'>class="active"</c:if>>
		<a
		href="${pageContext.request.contextPath}/module/dhisreporting/importMapping.form">Import Mapping</a>
	</li>
	<!-- Add further links here -->
</ul>
<h2>
	<spring:message code="dhisreporting.title" />
</h2>
