<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="contextPath" value="${empty contextPath ? pageContext.request.contextPath : contextPath}" />
<!doctype html>
<script type="text/javascript" src="${contextPath}/js/jquery-1.7.1.js"></script>
<script type="text/javascript" src="${contextPath}/js/highcharts.js"></script>
<script type="text/javascript" src="${contextPath}/js/baseGraph.js"></script>
<script type="text/javascript" src="${contextPath}/js/transaction.js"></script>

<style type="text/css">
.graph {
	width: 500px;
	height: 300px;
	margin: 4px auto;
}
</style>
<svg version="1.1" width="980" height="380" xmlns="http://www.w3.org/2000/svg">
  <c:out value="${graph1}" escapeXml="false" />
  <c:out value="${graph2}" escapeXml="false" />
  <c:out value="${graph3}" escapeXml="false" />
  <c:out value="${graph4}" escapeXml="false" />
</svg>
<c:if test="${ipAddress eq 'All' && distributionDetails != null}">
<table class="table table-hover table-striped table-condensed" style="width:100%;">
	<tr><td colspan="8"><h5 style="text-align:center" class="text-center text-info">分布统计</h5></td></tr>
	<tr>
		<th class="right">Ip</th>
		<th class="right">Total</th>
		<th class="right">Failure</th>
		<th class="right">Failure%</th>
		<th class="right">Min(ms)</th>
		<th class="right">Max(ms)</th>
		<th class="right">Avg(ms)</th>
		<th class="right">Std(ms)</th>
	</tr>
	<c:forEach var="item" items="${distributionDetails}">
	<tr class="right">
		<td><c:out value="${item.ip}" /></td>
		<td><fmt:formatNumber value="${item.totalCount}" pattern="#,###,###,###,##0" /></td>
		<td><fmt:formatNumber value="${item.failCount}" pattern="#,###,###,###,##0" /></td>
		<td><fmt:formatNumber value="${item.failPercent / 100}" pattern="0.0000%" /></td>
		<td><fmt:formatNumber value="${item.min}" pattern="###,##0.#" /></td>
		<td><fmt:formatNumber value="${item.max}" pattern="###,##0.#" /></td>
		<td><fmt:formatNumber value="${item.avg}" pattern="###,##0.0" /></td>
		<td><fmt:formatNumber value="${item.std}" pattern="###,##0.0" /></td>
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

<br/>
