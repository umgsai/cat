<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="contextPath" value="${empty contextPath ? pageContext.request.contextPath : contextPath}" />
<!doctype html>
<script type="text/javascript" src="${contextPath}/js/jquery-1.7.1.js"></script>
<script type="text/javascript" src="${contextPath}/js/highcharts.js"></script>
<script type="text/javascript" src="${contextPath}/js/baseGraph.js"></script>
<style type="text/css">
.graph {
	width: 450px;
	height: 300px;
	margin: 4px auto;
}
</style>

<table>
	<tr>
		<td>
			<h5 style="text-align:center" class="text-center text-info">错误量</h5>
			<div id="errorTrend" class="graph"></div>
		</td>
		<c:if test="${ipAddress eq 'All'}">
			<td>
				<h5 style="text-align:center" class="text-center text-info">错误分布</h5>
				<div id="distributionChart" class="graph"></div>
			</td>
		</c:if>
	</tr>
	<tr>
		<td style="display:none">
			<div id="errorTrendMeta"><c:out value="${errorsTrend}" /></div>
		</td>
		<td style="display:none">
			<div id="distributionChartMeta"><c:out value="${distributionChart}" /></div>
		</td>
	</tr>
</table>
<script type="text/javascript">
	var errorData = ${empty errorsTrend ? "null" : errorsTrend};
	if (errorData != null) {
		graphLineChart(document.getElementById('errorTrend'), errorData);
	}
</script>
<c:if test="${ipAddress eq 'All'}">
	<script type="text/javascript">
	var distributionChart = ${empty distributionChart ? "null" : distributionChart};

	if (distributionChart != null) {
		graphPieChart(document.getElementById('distributionChart'), distributionChart);
	}
	</script>
</c:if>
