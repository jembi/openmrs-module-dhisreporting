<%@ include file="/WEB-INF/template/include.jsp" %>
<%@ include file="/WEB-INF/template/header.jsp" %>
<%@ include file="template/localHeader.jsp" %>

<h3>Import Mapping</h3>

	<form method="POST" enctype="multipart/form-data">
        Select CSV Mapping file <input type="file" name="mapping"><br /><br />
        <input type="submit" value='Import'>
    </form>

<%@ include file="/WEB-INF/template/footer.jsp" %>