<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.text.DecimalFormat" %>
<%@ page import="java.util.List" %>
<%@ page import="com.dianping.cat.report.page.event.transform.DistributionDetailVisitor.DistributionDetail" %>
<%
	String contextPath = (String) request.getAttribute("contextPath");
	String ipAddress = (String) request.getAttribute("ipAddress");
	String graph1 = (String) request.getAttribute("graph1");
	String graph2 = (String) request.getAttribute("graph2");
	String distributionChart = (String) request.getAttribute("distributionChart");
	List<DistributionDetail> distributionDetails = (List<DistributionDetail>) request.getAttribute("distributionDetails");
	DecimalFormat integerFormat = new DecimalFormat("#,###,###,###,##0");
	DecimalFormat percentFormat = new DecimalFormat("0.0000%");

	if (contextPath == null) {
		contextPath = request.getContextPath();
	}
%>
<!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd">
<script type="text/javascript" src="<%=contextPath%>/js/jquery-1.7.1.js"></script>
<script type="text/javascript" src="<%=contextPath%>/js/highcharts.js"></script>
<script type="text/javascript" src="<%=contextPath%>/js/baseGraph.js"></script>
<script type="text/javascript" src="<%=contextPath%>/js/event.js"></script>
<svg version="1.1" width="980" height="190" xmlns="http://www.w3.org/2000/svg">
  <%=graph1 == null ? "" : graph1%>
  <%=graph2 == null ? "" : graph2%>
</svg>
<style type="text/css">
.graph {
	width: 600px;
	height: 400px;
	margin: 4px auto;
}
</style>
<% if ("All".equals(ipAddress) && distributionDetails != null) { %>
<table class="table table-hover table-striped table-condensed" style="width:100%;">
	<tr><td colspan="8"><h5 style="text-align:center" class="text-center text-info">分布统计</h5></td></tr>
	<tr>
		<th class="right">Ip</th>
		<th class="right">Total</th>
		<th class="right">Failure</th>
		<th class="right">Failure%</th>
	</tr>
	<% for (DistributionDetail item : distributionDetails) { %>
	<tr class="right">
		<td><%=html(item.getIp())%></td>
		<td><%=integerFormat.format(item.getTotalCount())%></td>
		<td><%=integerFormat.format(item.getFailCount())%></td>
		<td><%=percentFormat.format(item.getFailPercent() / 100)%></td>
	</tr>
	<% } %>
</table>
<br>

<div id="distributionChart" class="pieChart"></div>
<div id="distributionChartMeta" style="display:none"><%=distributionChart%></div>
<script type="text/javascript">
	var distributionChartMeta = <%=distributionChart == null ? "null" : distributionChart%>;

	if (distributionChartMeta != null) {
		graphPieChart(document.getElementById('distributionChart'), distributionChartMeta);
	}
</script>
<% } %>

<br>
<%!
	private String html(String value) {
		if (value == null) {
			return "";
		}
		return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
	}
%>
