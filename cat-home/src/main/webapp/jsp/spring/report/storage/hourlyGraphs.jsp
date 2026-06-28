<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="contextPath" value="${empty contextPath ? pageContext.request.contextPath : contextPath}" />
<script type="text/javascript" src="${contextPath}/assets/js/jquery.min.js"></script>
<script type="text/javascript" src="${contextPath}/js/highcharts.js"></script>
<script type="text/javascript" src="${contextPath}/js/baseGraph.js"></script>
<style type="text/css">
	.graph { width: 100%; height: 300px; }
</style>
<table width="100%">
	<tr>
		<td width="50%"><h5 class="text-center text-info">错误量</h5><div id="errorTrend" class="graph"></div></td>
		<td width="50%"><h5 class="text-center text-info">响应时间</h5><div id="avgTrend" class="graph"></div></td>
	</tr>
	<tr>
		<td width="50%"><h5 class="text-center text-info">操作量</h5><div id="countTrend" class="graph"></div></td>
		<td width="50%"><h5 class="text-center text-info">长响应</h5><div id="longTrend" class="graph"></div></td>
	</tr>
	<c:if test="${ipAddress eq 'All' and project eq 'All' and not empty distributionChart}">
		<tr><td colspan="2" width="90%"><h5 class="text-center text-info">错误分布</h5><div id="piechart" class="graph"></div></td></tr>
	</c:if>
</table>
<script type="text/javascript">
	graphLineChart(document.getElementById('countTrend'), ${empty countTrend ? "{}" : countTrend});
	graphLineChart(document.getElementById('avgTrend'), ${empty avgTrend ? "{}" : avgTrend});
	graphLineChart(document.getElementById('errorTrend'), ${empty errorTrend ? "{}" : errorTrend});
	graphLineChart(document.getElementById('longTrend'), ${empty longTrend ? "{}" : longTrend});
	<c:if test="${ipAddress eq 'All' and project eq 'All' and not empty distributionChart}">
		graphPieChart(document.getElementById('piechart'), ${distributionChart});
	</c:if>
</script>
