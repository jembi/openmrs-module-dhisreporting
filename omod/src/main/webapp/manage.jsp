<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<%@ include file="template/localHeader.jsp"%>

<p>Hello ${user.systemId}!</p>

<form method="post">
	<input type="submit" value="Test Sending monthly HMIS data">
</form>
<%@ include file="/WEB-INF/template/footer.jsp"%>