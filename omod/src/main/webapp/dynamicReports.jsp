<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<%@ include file="template/localHeader.jsp"%>

<h3>Mapping Dynamic Period indicator reports</h3>

<form method="post">
    <table>
        <thead>
            <tr>
                <th>Delete</th>
                <th>Org Unit UId</th>
                <th>DataSet UId</th>
                <th>Period Type</th>
                <th>Location</th>
                <th>Report</th>
                <th>DataElement Prefixes</th>
                <th>Disaggregation Categories</th>
            </tr>
        </thead>
        <tbody>
            <tr>
                <td>New</td>
                <td><input type="text" id="new-mapping-orgunit" name="mappingOrgUnit" value="${orgUnit}"></td>
                <td><input type="text" id="new-mapping-dataset" name="mappingDataset"></td>
                <td>
                    <select name="mappingPeriodType">
                        <option></option>
                        <c:forEach items="${periodTypes}" var="periodType">
                            <option value="${periodType}">${periodType}</option>
                        </c:forEach>
                    </select>
                </td>
                <td>
                    <select name="mappingLocation">
                        <option></option>
                        <c:forEach items="${locations}" var="location">
                            <option value="${location.uuid}">${location.name}</option>
                        </c:forEach>
                    </select>
                </td>
                <td>
                    <select name="mappingReport">
                        <option></option>
                        <c:forEach items="${reports}" var="report">
                            <option value="${report.uuid}">${report.name}</option>
                        </c:forEach>
                    </select>
                </td>
                <td><input type="text" id="new-mapping-dataElementPrefixes" name="mappingPrefixes"></td>
                <td><input type="text" id="new-mapping-disaggregationCategories" name="mappingCategories"></td>
            </tr>
            <c:forEach items="${mappedIndicatorReports}" var="mapping">
                <tr>
                    <td><input type="checkbox" class="mapping-indicator-report" name="mappingIds" value="${mapping.id}"/></td>
                    <td>${mapping.orgUnitId}</td>
                    <td>${mapping.dataSetId}</td>
                    <td>${mapping.periodType}</td>
                    <td>${mapping.location.name}</td>
                    <td>${mapping.report.name}</td>
                    <td>${mapping.dataElementPrefixes}</td>
                    <td>${mapping.disaggregationCategories}</td>
                </tr>
            </c:forEach>
        </tbody>
    </table>
    <input type="submit" value="Submit">
</form>
<%@ include file="/WEB-INF/template/footer.jsp"%>