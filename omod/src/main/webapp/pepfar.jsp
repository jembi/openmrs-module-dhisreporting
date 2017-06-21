<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<%@ include file="template/localHeader.jsp"%>

<style type="text/css">
	.resp_msg {
		background-color: lightyellow;
		border: 1px dashed lightgrey;
	}
</style>

<form method="post">
	<input type="submit" value="Generate & Post or Delete All DHISReporting reports">
</form>

<br />
<c:forEach items="${response}" var="resp">
	<div class="resp_msg">${resp}</div><br />
</c:forEach>

<%@ include file="/WEB-INF/template/footer.jsp"%>