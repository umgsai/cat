<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.text.DecimalFormat" %>
<%@ page import="java.util.List" %>
<%@ page import="com.dianping.cat.report.page.transaction.transform.DistributionDetailVisitor.DistributionDetail" %>
<%
	String contextPath = (String) request.getAttribute("contextPath");
	String ipAddress = (String) request.getAttribute("ipAddress");
	String graph1 = (String) request.getAttribute("graph1");
	String graph2 = (String) request.getAttribute("graph2");
	String graph3 = (String) request.getAttribute("graph3");
	String graph4 = (String) request.getAttribute("graph4");
	String distributionChart = (String) request.getAttribute("distributionChart");
	List<DistributionDetail> distributionDetails = (List<DistributionDetail>) request.getAttribute("distributionDetails");
	DecimalFormat integerFormat = new DecimalFormat("#,###,###,###,##0");
	DecimalFormat percentFormat = new DecimalFormat("0.0000%");
	DecimalFormat oneDecimalFormat = new DecimalFormat("###,##0.0");
	DecimalFormat optionalDecimalFormat = new DecimalFormat("###,##0.#");

	if (contextPath == null) {
		contextPath = request.getContextPath();
	}
%>
<!DOCTYPE svg PUBLIC "-//W3C//DTD SVG 1.1//EN" "http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd">
<script type="text/javascript" src="<%=contextPath%>/js/jquery-1.7.1.js"></script>
<script type="text/javascript" src="<%=contextPath%>/js/highcharts.js"></script>
<script type="text/javascript" src="<%=contextPath%>/js/baseGraph.js"></script>
<script type="text/javascript" src="<%=contextPath%>/js/transaction.js"></script>

<style type="text/css">
.graph {
	width: 500px;
	height: 300px;
	margin: 4px auto;
}
</style>
<svg version="1.1" width="980" height="380" xmlns="http://www.w3.org/2000/svg">
  <%=graph1 == null ? "" : graph1%>
  <%=graph2 == null ? "" : graph2%>
  <%=graph3 == null ? "" : graph3%>
  <%=graph4 == null ? "" : graph4%>
</svg>
<% if ("All".equals(ipAddress) && distributionDetails != null) { %>
<table class="table table-hover table-striped table-condensed" style="width:100%;">
	<tr><td colspan="8"><h5 style="text-align:center" class="text-center text-info">鍒嗗竷缁熻</h5></td></tr>
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
	<% for (DistributionDetail item : distributionDetails) { %>
	<tr class="right">
		<td><%=item.getIp()%></td>
		<td><%=integerFormat.format(item.getTotalCount())%></td>
		<td><%=integerFormat.format(item.getFailCount())%></td>
		<td><%=percentFormat.format(item.getFailPercent() / 100)%></td>
		<td><%=optionalDecimalFormat.format(item.getMin())%></td>
		<td><%=optionalDecimalFormat.format(item.getMax())%></td>
		<td><%=oneDecimalFormat.format(item.getAvg())%></td>
		<td><%=oneDecimalFormat.format(item.getStd())%></td>
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

<br/>
