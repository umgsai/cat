<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="contextPath" value="${empty contextPath ? pageContext.request.contextPath : contextPath}" />
<!doctype html>
<style type="text/css">
.graph {
	width: 550px;
	height: 350px;
	margin: 4px auto;
}
</style>
<table>
	<tr>
		<td><div id="hitTrend" class="graph"></div></td>
		<td><div id="failureTrend" class="graph"></div></td>
	</tr>
	<tr>
		<td style="display:none">
			<div id="hitTrendMeta"><c:out value="${hitTrend}" /></div>
			<div id="failureTrendMeta"><c:out value="${failureTrend}" /></div>
		</td>
	</tr>
</table>
<script type="text/javascript">
	var hitTrendData = ${empty hitTrend ? "{}" : hitTrend};
	var failureTrendData = ${empty failureTrend ? "{}" : failureTrend};

	graphLineChart(document.getElementById('hitTrend'), hitTrendData);
	graphLineChart(document.getElementById('failureTrend'), failureTrendData);
</script>
<c:if test="${ipAddress eq 'All' && distributionDetails != null}">
<table class="table table-hover table-striped table-condensed" style="width:100%;">
	<tr><td colspan="8"><h5 style="text-align:center" class="text-center text-info">鍒嗗竷缁熻</h5></td></tr>
	<tr>
		<th class="right">Ip</th>
		<th class="right">Total</th>
		<th class="right">Failure</th>
		<th class="right">Failure%</th>
	</tr>
	<c:forEach var="item" items="${distributionDetails}">
	<tr class="right">
		<td><c:out value="${item.ip}" /></td>
		<td><fmt:formatNumber value="${item.totalCount}" pattern="#,###,###,###,##0" /></td>
		<td><fmt:formatNumber value="${item.failCount}" pattern="#,###,###,###,##0" /></td>
		<td><fmt:formatNumber value="${item.failPercent / 100}" pattern="0.0000%" /></td>
	</tr>
	</c:forEach>
</table>
<br>
<div id="distributionChart" class="pieChart"></div>
<div id="distributionChartMeta" style="display:none"><c:out value="${distributionChart}" /></div>
<script type="text/javascript">
	var distributionChartMeta = ${empty distributionChart ? "null" : distributionChart};

	if (distributionChartMeta != null) {
		graphPieChart(document.getElementById('distributionChart'), distributionChartMeta);
	}
</script>
</c:if>
<br>
