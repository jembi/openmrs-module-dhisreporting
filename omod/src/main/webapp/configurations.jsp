<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<%@ include file="template/localHeader.jsp"%>

<form method="post">
    <table>
        <thead>
            <tr class="evenRow">
                <th>Setting</th>
                <th>Description</th>
                <th>Value</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach items="${configurations}" var="config">
                <tr class="evenRow">
                    <td>${config.property}</td>
                    <td>${config.description}</td>
                    <td><input type="text" value="${config.value}" name="${config.property}" size="35"></td>
                </tr>
            </c:forEach>
        </tbody>
    </table>
    <br />
    <input type="submit" value="Save">
</form>

<%@ include file="/WEB-INF/template/footer.jsp"%>