<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.text.DecimalFormat" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.dianping.cat.Constants" %>
<%@ page import="com.dianping.cat.consumer.transaction.model.entity.TransactionName" %>
<%@ page import="com.dianping.cat.consumer.transaction.model.entity.TransactionReport" %>
<%@ page import="com.dianping.cat.consumer.transaction.model.entity.TransactionType" %>
<%@ page import="com.dianping.cat.mvc.UrlNav" %>
<%@ page import="com.dianping.cat.report.page.transaction.DisplayNames" %>
<%@ page import="com.dianping.cat.report.page.transaction.DisplayNames.TransactionNameModel" %>
<%@ page import="com.dianping.cat.report.page.transaction.DisplayTypes" %>
<%@ page import="com.dianping.cat.report.page.transaction.DisplayTypes.TransactionTypeModel" %>
<%@ page import="com.dianping.cat.service.ProjectService.Department" %>
<%@ page import="com.dianping.cat.service.ProjectService.ProjectLine" %>
<%
	String contextPath = (String) request.getAttribute("contextPath");
	String domain = (String) request.getAttribute("domain");
	String displayDomain = (String) request.getAttribute("displayDomain");
	String ipAddress = (String) request.getAttribute("ipAddress");
	String date = (String) request.getAttribute("date");
	String reportType = (String) request.getAttribute("reportType");
	String type = (String) request.getAttribute("type");
	String encodedType = (String) request.getAttribute("encodedType");
	String queryName = (String) request.getAttribute("queryName");
	String reportStart = (String) request.getAttribute("reportStart");
	String reportEnd = (String) request.getAttribute("reportEnd");
	String ipToHostnameStr = (String) request.getAttribute("ipToHostnameStr");
	String pieChart = (String) request.getAttribute("pieChart");
	TransactionReport report = (TransactionReport) request.getAttribute("report");
	DisplayTypes displayTypeReport = (DisplayTypes) request.getAttribute("displayTypeReport");
	DisplayNames displayNameReport = (DisplayNames) request.getAttribute("displayNameReport");
	List<String> ips = (List<String>) request.getAttribute("ips");
	List<String> groups = (List<String>) request.getAttribute("groups");
	Map<String, Department> domainGroups = (Map<String, Department>) request.getAttribute("domainGroups");
	UrlNav[] navs = (UrlNav[]) request.getAttribute("navs");
	Double sample = (Double) request.getAttribute("sample");
	DecimalFormat integerFormat = new DecimalFormat("#,###,###,###,##0");
	DecimalFormat percentFormat = new DecimalFormat("0.0000%");
	DecimalFormat totalPercentFormat = new DecimalFormat("0.00%");
	DecimalFormat oneDecimalFormat = new DecimalFormat("###,##0.0");
	DecimalFormat optionalDecimalFormat = new DecimalFormat("###,##0.#");

	if (contextPath == null) {
		contextPath = request.getContextPath();
	}
	if (displayDomain == null) {
		displayDomain = domain;
	}
	if (queryName == null) {
		queryName = "";
	}
	if (encodedType == null) {
		encodedType = "";
	}
	if (reportType == null || reportType.length() == 0) {
		reportType = "day";
	}
	String navPrefix = "ip=" + ipAddress + "&queryname=" + queryName + "&domain=" + domain
			+ (type == null ? "" : "&type=" + encodedType);
%>
<!doctype html>
<html lang="en">
<head>
	<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
	<meta charset="utf-8">
	<title>CAT</title>
	<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0">
	<link rel="stylesheet" type="text/css" href="<%=contextPath%>/assets/css/bootstrap.min.css">
	<link rel="stylesheet" type="text/css" href="<%=contextPath%>/assets/css/font-awesome.min.css">
	<link rel="stylesheet" type="text/css" href="<%=contextPath%>/assets/css/jquery-ui.min.css">
	<link rel="stylesheet" type="text/css" href="<%=contextPath%>/assets/css/ace-fonts.css">
	<link rel="stylesheet" type="text/css" href="<%=contextPath%>/assets/css/ace.min.css" id="main-ace-style">
	<link rel="stylesheet" type="text/css" href="<%=contextPath%>/assets/css/ace-skins.min.css">
	<link rel="stylesheet" type="text/css" href="<%=contextPath%>/assets/css/ace-rtl.min.css">
	<link rel="stylesheet" type="text/css" href="<%=contextPath%>/css/body.css">
	<script src="<%=contextPath%>/assets/js/jquery.min.js"></script>
	<script src="<%=contextPath%>/assets/js/ace-extra.min.js"></script>
	<script src="<%=contextPath%>/assets/js/bootstrap.min.js"></script>
	<script src="<%=contextPath%>/js/highcharts.js"></script>
	<script src="<%=contextPath%>/js/baseGraph.js"></script>
	<script src="<%=contextPath%>/assets/js/jquery-ui.min.js"></script>
	<script src="<%=contextPath%>/assets/js/jquery.ui.touch-punch.min.js"></script>
	<script src="<%=contextPath%>/assets/js/ace-elements.min.js"></script>
	<script src="<%=contextPath%>/assets/js/ace.min.js"></script>
	<style>
		.right { text-align: right; }
		.left { text-align: left; }
		.center { text-align: center; }
		.current { color: red; font-weight: bold; }
	</style>
</head>
<body class="no-skin">
	<div id="navbar" class="navbar navbar-default">
		<div class="navbar-container" id="navbar-container">
			<button type="button" class="navbar-toggle menu-toggler pull-left" id="menu-toggler">
				<span class="sr-only">Toggle sidebar</span>
				<span class="icon-bar"></span>
				<span class="icon-bar"></span>
				<span class="icon-bar"></span>
			</button>
			<div class="navbar-header pull-left">
				<i class="navbar-brand">
					<span>CAT</span>
					<small style="font-size:65%">（Central Application Tracking）</small>
					<button class="btn btn-success btn-sm disabled" id="nav_application">
						<i class="ace-icon fa fa-signal"></i>Application
					</button>
					<button class="btn btn-inverse btn-sm" id="nav_config">
						<i class="ace-icon fa fa-cogs"></i>Configs
					</button>
					<button class="btn btn-yellow btn-sm" id="nav_document">
						<i class="ace-icon fa fa-cogs"></i>Documents
					</button>
				</i>
			</div>
			<div class="navbar-buttons navbar-header pull-right" role="navigation">
				<ul class="nav ace-nav" style="height:auto;">
					<li class="light-blue">
						<a href="<%=contextPath%>/mvc/r/home?op=view&docName=index">
							<i class="ace-icon glyphicon glyphicon-star"></i>
							<span>Star</span>
						</a>
					</li>
				</ul>
			</div>
		</div>
	</div>
	<div class="main-container" id="main-container">
		<div id="sidebar" class="sidebar responsive">
			<ul class="nav nav-list" style="top: 0px;">
				<li id="Dashboard_report">
					<a href="<%=contextPath%>/mvc/r/top?op=view&domain=<%=domain%>">
						<i class="menu-icon fa fa-tachometer"></i>
						<span class="menu-text">Dashboard</span>
					</a>
				</li>
				<li id="Transaction_report" class="active open">
					<a href="<%=contextPath%>/mvc/r/t?domain=<%=domain%>&ip=<%=ipAddress%>&date=<%=date%>&reportType=<%=reportType%>&op=view">
						<i class="menu-icon glyphicon glyphicon-time"></i>
						<span class="menu-text">Transaction</span>
					</a>
				</li>
				<li id="Event_report">
					<a href="<%=contextPath%>/mvc/r/e?domain=<%=domain%>&ip=<%=ipAddress%>&date=<%=date%>&reportType=<%=reportType%>&op=view">
						<i class="menu-icon fa fa-flag"></i>
						<span class="menu-text">Event</span>
					</a>
				</li>
				<li id="Problem_report">
					<a href="<%=contextPath%>/mvc/r/p?domain=<%=domain%>&ip=<%=ipAddress%>&date=<%=date%>&reportType=<%=reportType%>&op=view">
						<i class="menu-icon fa fa-bug"></i>
						<span class="menu-text">Problem</span>
					</a>
				</li>
				<li id="Heartbeat_report">
					<a href="<%=contextPath%>/mvc/r/h?domain=<%=domain%>&ip=<%=ipAddress%>&date=<%=date%>&reportType=<%=reportType%>&op=view">
						<i class="menu-icon fa fa-heart"></i>
						<span class="menu-text">Heartbeat</span>
					</a>
				</li>
				<li id="Cross_report">
					<a href="<%=contextPath%>/mvc/r/cross?domain=<%=domain%>&ip=<%=ipAddress%>&date=<%=date%>&reportType=<%=reportType%>&op=view">
						<i class="menu-icon glyphicon glyphicon-random"></i>
						<span class="menu-text">Cross</span>
					</a>
				</li>
				<li id="Business_report">
					<a href="<%=contextPath%>/mvc/r/business?name=<%=domain%>&type=domain">
						<i class="menu-icon fa fa-list-alt"></i>
						<span class="menu-text">Business</span>
					</a>
				</li>
				<li id="State_report">
					<a href="<%=contextPath%>/mvc/r/state?domain=<%=domain%>&ip=<%=ipAddress%>&date=<%=date%>&reportType=<%=reportType%>&op=view">
						<i class="menu-icon fa fa-bar-chart-o"></i>
						<span class="menu-text">State</span>
					</a>
				</li>
			</ul>
			<div class="sidebar-toggle sidebar-collapse" id="sidebar-collapse">
				<i class="ace-icon fa fa-angle-double-left" data-icon1="ace-icon fa fa-angle-double-left" data-icon2="ace-icon fa fa-angle-double-right"></i>
			</div>
		</div>
		<div class="main-content">
			<div style="padding-top:2px;padding-left:2px;padding-right:8px;">
				<div class="report">
					<div class="breadcrumbs" id="breadcrumbs">
						<table style="line-height:normal;">
							<tr>
								<td><span class="text-success"><%=reportStart%> to <%=reportEnd%></span></td>
								<td>
									<div id="warp_search_group" class="" style="width:250px;">
										<form id="wrap_search" style="margin-left:10px;margin-bottom:0px;">
											<div class="input-group">
												<span class="input-group-btn"><button class="btn btn-sm btn-default" onclick="showDomain()" type="button" id="switch">全部</button></span>
												<span class="input-group-btn"><button class="btn btn-sm btn-default" onclick="showFrequent()" type="button" id="frequent">常用</button></span>
												<span class="input-icon" style="width:200px;">
													<input id="search" type="text" value="<%=domain%>" class="search-input search-input form-control ui-autocomplete-input" placeholder="input domain for search" autocomplete="off"/>
													<i class="ace-icon fa fa-search nav-search-icon"></i>
												</span>
												<span class="input-group-btn"><button class="btn btn-sm btn-pink" type="button" id="search_go">Go</button></span>
											</div>
										</form>
									</div>
								</td>
								<td>
									<div class="nav-search nav" id="nav-search">
										<span class="text-danger switch">【<a class="switch" href="<%=contextPath%>/mvc/r/t?op=history&domain=<%=domain%>&ip=<%=ipAddress%>"><span class="text-danger">切到历史模式</span></a>】</span>
										<% for (UrlNav nav : navs) { %>
											&nbsp;[ <a href="<%=contextPath%>/mvc/r/t?date=<%=date%>&ip=<%=ipAddress%>&step=<%=nav.getHours()%>&<%=navPrefix%>"><%=nav.getTitle()%></a> ]
										<% } %>
										&nbsp;[ <a href="<%=contextPath%>/mvc/r/t?<%=navPrefix%>">now</a> ]&nbsp;
									</div>
								</td>
							</tr>
						</table>
						<script type="text/javascript">
							try { ace.settings.check('breadcrumbs', 'fixed'); } catch(e) {}
						</script>
					</div>
					<div class="domainNavbar" style="display:none;font-size:small">
						<table border="1" rules="all">
							<% if (domainGroups != null) {
								for (Map.Entry<String, Department> entry : domainGroups.entrySet()) {
									Map<String, ProjectLine> lines = entry.getValue().getProjectLines();
									int row = 0;
							%>
								<tr>
									<td class="department" rowspan="<%=lines.size()%>"><%=html(entry.getKey())%></td>
									<% for (Map.Entry<String, ProjectLine> line : lines.entrySet()) {
										if (row++ > 0) {
									%>
										<tr>
									<% } %>
										<td class="department"><%=html(line.getKey())%></td>
										<td><div class="domain">
											<% for (String itemDomain : line.getValue().getLineDomains()) { %>
												&nbsp;<a class="domainItem" href="<%=contextPath%>/mvc/r/t?op=view&domain=<%=itemDomain%>&date=<%=date%>&reportType=<%=reportType%>">[&nbsp;<%=html(itemDomain)%>&nbsp;]</a>&nbsp;
											<% } %>
										</div></td>
									<% if (row > 1) { %>
										</tr>
									<% } } %>
								</tr>
							<% } } %>
						</table>
					</div>
					<div class="frequentNavbar" style="display:none;font-size:small">
						<table border="1" rules="all"><tr><td class="domain" style="word-break:break-all" id="frequentNavbar"></td></tr></table>
					</div>
					<% if (sample != null && sample.doubleValue() != 1.0) { %>
						<div class="ace-settings-container" id="ace-settings-container">
							<div class="btn btn-app btn-xs btn-warning ace-settings-btn" id="ace-settings-btn">閲囨牱</div>
							<div class="ace-settings-box clearfix" id="ace-settings-box">
								<div class="pull-left width-50">
									<div class="ace-settings-item"><label class="lbl">閲囨牱鐜囷細<strong><span class="text-danger"><%=new DecimalFormat("#0.00").format(sample.doubleValue() * 100)%>%</span></strong></label></div>
									<div class="ace-settings-item"><label class="lbl">采样直接影响的是Transaction、Event的总量和QPS</label></div>
									<div class="ace-settings-item"><label class="lbl">骞朵笉閫傜敤浜嶮etric銆丠eartbeat銆丒xception涓夐」鎸囨爣</label></div>
								</div>
							</div>
						</div>
					<% } %>
					<table class="machines">
						<tr class="left">
							<th>&nbsp;[&nbsp;
								<a href="<%=contextPath%>/mvc/r/t?domain=<%=domain%>&date=<%=date%>&type=<%=encodedType%>&queryname=<%=queryName%>" class="<%=Constants.ALL.equals(ipAddress) ? "current" : ""%>">All</a>
								&nbsp;]&nbsp;
								<% if (ips != null) { for (String ip : ips) { %>
									&nbsp;[&nbsp;<a href="<%=contextPath%>/mvc/r/t?domain=<%=domain%>&ip=<%=ip%>&date=<%=date%>&type=<%=encodedType%>&queryname=<%=queryName%>" class="<%=ip.equals(ipAddress) ? "current" : ""%>"><%=ip%></a>&nbsp;]&nbsp;
								<% } } %>
							</th>
						</tr>
					</table>
					<script type="text/javascript" src="<%=contextPath%>/js/appendHostname.js"></script>
					<script type="text/javascript">
						$(document).ready(function() { appendHostname(<%=ipToHostnameStr%>); });
					</script>
					<table class="groups">
						<tr class="left">
							<th>
								<% if (groups != null) { for (String itemGroup : groups) { %>
									&nbsp;[&nbsp;<a href="<%=contextPath%>/mvc/r/t?op=groupReport&domain=<%=domain%>&date=<%=date%>&group=<%=itemGroup%>&type=<%=encodedType%>"><%=html(itemGroup)%></a>&nbsp;]&nbsp;
								<% } } %>
							</th>
						</tr>
					</table>
					<table class="table table-striped table-condensed table-hover" style="width:100%;">
						<% if (type == null || type.length() == 0) { %>
							<tr>
								<th class="left"><a href="<%=contextPath%>/mvc/r/t?domain=<%=domain%>&date=<%=date%>&ip=<%=ipAddress%>&sort=type">Type</a></th>
								<th class="right"><a href="<%=contextPath%>/mvc/r/t?domain=<%=domain%>&date=<%=date%>&ip=<%=ipAddress%>&sort=total">Total</a></th>
								<th class="right"><a href="<%=contextPath%>/mvc/r/t?domain=<%=domain%>&date=<%=date%>&ip=<%=ipAddress%>&sort=failure">Failure</a></th>
								<th class="right"><a href="<%=contextPath%>/mvc/r/t?domain=<%=domain%>&date=<%=date%>&ip=<%=ipAddress%>&sort=failurePercent">Failure%</a></th>
								<th class="right">Sample Link</th>
								<th class="right"><a href="<%=contextPath%>/mvc/r/t?domain=<%=domain%>&date=<%=date%>&ip=<%=ipAddress%>&sort=min">Min</a>(ms)</th>
								<th class="right"><a href="<%=contextPath%>/mvc/r/t?domain=<%=domain%>&date=<%=date%>&ip=<%=ipAddress%>&sort=max">Max</a>(ms)</th>
								<th class="right"><a href="<%=contextPath%>/mvc/r/t?domain=<%=domain%>&date=<%=date%>&ip=<%=ipAddress%>&sort=avg">Avg</a>(ms)</th>
								<th class="right"><a href="<%=contextPath%>/mvc/r/t?domain=<%=domain%>&date=<%=date%>&ip=<%=ipAddress%>&sort=95line">95Line</a>(ms)</th>
								<th class="right"><a href="<%=contextPath%>/mvc/r/t?domain=<%=domain%>&date=<%=date%>&ip=<%=ipAddress%>&sort=99line">99.9Line</a>(ms)</th>
								<th class="right"><a href="<%=contextPath%>/mvc/r/t?domain=<%=domain%>&date=<%=date%>&ip=<%=ipAddress%>&sort=std">Std</a>(ms)</th>
								<th class="right"><a href="<%=contextPath%>/mvc/r/t?domain=<%=domain%>&date=<%=date%>&ip=<%=ipAddress%>&sort=total">QPS</a></th>
							</tr>
							<% if (displayTypeReport != null) {
								int index = 0;
								for (TransactionTypeModel item : displayTypeReport.getResults()) {
									TransactionType e = item.getDetail();
							%>
								<tr class="right">
									<td class="left"><a href="<%=contextPath%>/mvc/r/t?op=graphs&domain=<%=report.getDomain()%>&date=<%=date%>&ip=<%=ipAddress%>&type=<%=item.getType()%>" class="graph_link" data-status="<%=index%>">[:: show ::]</a>
									&nbsp;&nbsp;<a href="<%=contextPath%>/mvc/r/t?domain=<%=report.getDomain()%>&date=<%=date%>&ip=<%=ipAddress%>&type=<%=item.getType()%>"> <%=html(e.getId())%></a></td>
									<td><%=integerFormat.format(e.getTotalCount())%></td>
									<td><%=integerFormat.format(e.getFailCount())%></td>
									<td>&nbsp;<%=percentFormat.format(e.getFailPercent() / 100)%></td>
									<td><a href="<%=contextPath%>/mvc/r/m/<%=messageUrl(e.getFailMessageUrl(), e.getSuccessMessageUrl())%>?domain=<%=domain%>">Log View</a></td>
									<td><%=optionalDecimalFormat.format(e.getMin())%></td>
									<td><%=optionalDecimalFormat.format(e.getMax())%></td>
									<td><%=oneDecimalFormat.format(e.getAvg())%></td>
									<td><%=oneDecimalFormat.format(e.getLine95Value())%></td>
									<td><%=oneDecimalFormat.format(e.getLine99Value())%></td>
									<td><%=oneDecimalFormat.format(e.getStd())%></td>
									<td><%=oneDecimalFormat.format(e.getTps())%></td>
								</tr>
								<tr class="graphs"><td colspan="13" style="display:none"><div id="<%=index%>" style="display:none"></div></td></tr>
								<tr style="display:none"></tr>
							<% index++; } } %>
						<% } else { %>
							<tr><th class="left" colspan="13"><input type="text" name="queryname" id="queryname" size="40" value="<%=html(queryName)%>">
								<input class="btn btn-primary btn-sm" value="Filter" onclick="selectByName('<%=date%>','<%=domain%>','<%=ipAddress%>','<%=encodedType%>')" type="submit">
								鏀寔澶氫釜瀛楃涓叉煡璇紝渚嬪sql|url|task锛屾煡璇㈢粨鏋滀负鍖呭惈浠讳竴sql銆乽rl銆乼ask鐨勫垪銆?
							</th></tr>
							<tr>
								<th style="text-align:left;"><a href="<%=contextPath%>/mvc/r/t?op=graphs&domain=<%=report.getDomain()%>&date=<%=date%>&ip=<%=ipAddress%>&type=<%=encodedType%>" class="graph_link" data-status="-1">[:: show ::]</a>
								<a href="<%=contextPath%>/mvc/r/t?domain=<%=domain%>&date=<%=date%>&ip=<%=ipAddress%>&type=<%=encodedType%>&sort=type&queryname=<%=queryName%>">Name</a></th>
								<th class="right"><a href="<%=contextPath%>/mvc/r/t?domain=<%=domain%>&date=<%=date%>&ip=<%=ipAddress%>&type=<%=encodedType%>&sort=total&queryname=<%=queryName%>">Total</a></th>
								<th class="right"><a href="<%=contextPath%>/mvc/r/t?domain=<%=domain%>&date=<%=date%>&ip=<%=ipAddress%>&type=<%=encodedType%>&sort=failure&queryname=<%=queryName%>">Failure</a></th>
								<th class="right"><a href="<%=contextPath%>/mvc/r/t?domain=<%=domain%>&date=<%=date%>&ip=<%=ipAddress%>&type=<%=encodedType%>&sort=failurePercent&queryname=<%=queryName%>">Failure%</a></th>
								<th class="right">Sample Link</th>
								<th class="right"><a href="<%=contextPath%>/mvc/r/t?domain=<%=domain%>&date=<%=date%>&ip=<%=ipAddress%>&type=<%=encodedType%>&sort=min&queryname=<%=queryName%>">Min</a>(ms)</th>
								<th class="right"><a href="<%=contextPath%>/mvc/r/t?domain=<%=domain%>&date=<%=date%>&ip=<%=ipAddress%>&type=<%=encodedType%>&sort=max&queryname=<%=queryName%>">Max</a>(ms)</th>
								<th class="right"><a href="<%=contextPath%>/mvc/r/t?domain=<%=domain%>&date=<%=date%>&ip=<%=ipAddress%>&type=<%=encodedType%>&sort=avg&queryname=<%=queryName%>">Avg</a>(ms)</th>
								<th class="right"><a href="<%=contextPath%>/mvc/r/t?domain=<%=domain%>&date=<%=date%>&ip=<%=ipAddress%>&type=<%=encodedType%>&sort=95line&queryname=<%=queryName%>">95Line</a>(ms)</th>
								<th class="right"><a href="<%=contextPath%>/mvc/r/t?domain=<%=domain%>&date=<%=date%>&ip=<%=ipAddress%>&type=<%=encodedType%>&sort=99line&queryname=<%=queryName%>">99.9Line</a>(ms)</th>
								<th class="right"><a href="<%=contextPath%>/mvc/r/t?domain=<%=domain%>&date=<%=date%>&ip=<%=ipAddress%>&type=<%=encodedType%>&sort=std&queryname=<%=queryName%>">Std</a>(ms)</th>
								<th class="right"><a href="<%=contextPath%>/mvc/r/t?domain=<%=domain%>&date=<%=date%>&ip=<%=ipAddress%>&type=<%=encodedType%>&sort=total&queryname=<%=queryName%>">QPS</a></th>
								<th class="right"><a href="<%=contextPath%>/mvc/r/t?domain=<%=domain%>&date=<%=date%>&ip=<%=ipAddress%>&type=<%=encodedType%>&sort=total&queryname=<%=queryName%>">Percent%</a></th>
							</tr>
							<tr class="graphs"><td colspan="13" style="display:none"><div id="-1" style="display:none"></div></td></tr>
							<% if (displayNameReport != null) {
								int index = 0;
								for (TransactionNameModel item : displayNameReport.getResults()) {
									TransactionName e = item.getDetail();
							%>
								<tr class="right">
									<% if (index > 0) { %>
										<td class="left longText" style="white-space:normal">
											<a href="<%=contextPath%>/mvc/r/t?op=graphs&domain=<%=report.getDomain()%>&date=<%=date%>&ip=<%=ipAddress%>&type=<%=encodedType%>&name=<%=item.getName()%>" class="graph_link" data-status="<%=index%>">[:: show ::]</a>
											&nbsp;&nbsp;<%=html(shorten(e.getId(), 120))%>
										</td>
									<% } else { %>
										<td class="center" style="white-space:normal"><%=html(shorten(e.getId(), 120))%></td>
									<% } %>
									<td><%=integerFormat.format(e.getTotalCount())%></td>
									<td><%=integerFormat.format(e.getFailCount())%></td>
									<td>&nbsp;<%=percentFormat.format(e.getFailPercent() / 100)%></td>
									<td class="center"><a href="<%=contextPath%>/mvc/r/m/<%=messageUrl(e.getFailMessageUrl(), e.getSuccessMessageUrl())%>?domain=<%=domain%>">Log View</a></td>
									<td><%=optionalDecimalFormat.format(e.getMin())%></td>
									<td><%=optionalDecimalFormat.format(e.getMax())%></td>
									<td><%=oneDecimalFormat.format(e.getAvg())%></td>
									<% if (index > 0) { %>
										<td><%=oneDecimalFormat.format(e.getLine95Value())%></td>
										<td><%=oneDecimalFormat.format(e.getLine99Value())%></td>
									<% } else { %>
										<td class="center">-</td>
										<td class="center">-</td>
									<% } %>
									<td><%=oneDecimalFormat.format(e.getStd())%></td>
									<td><%=oneDecimalFormat.format(e.getTps())%></td>
									<td><%=totalPercentFormat.format(e.getTotalPercent())%></td>
								</tr>
								<tr class=""><td colspan="13" style="display:none"><div id="<%=index%>" style="display:none"></div></td></tr>
								<tr></tr>
							<% index++; } } %>
						<% } %>
					</table>
					<font color="white"></font>
					<% if (type != null && type.length() > 0) { %>
						<table><tr><td><div id="transactionGraph" class="pieChart"></div></td></tr></table>
						<script type="text/javascript">
							var data = <%=pieChart == null ? "{}" : pieChart%>;
							graphPieChart(document.getElementById('transactionGraph'), data);
						</script>
					<% } %>
				</div>
			</div>
		</div>
	</div>
	<script src="<%=contextPath%>/js/transaction.js"></script>
	<script type="text/javascript">
		function getcookie(objname) {
			var arrstr = document.cookie.split("; ");

			for (var i = 0; i < arrstr.length; i++) {
				var temp = arrstr[i].split("=");

				if (temp[0] == objname) {
					return temp[1];
				}
			}
			return "";
		}
		function showDomain() {
			var b = $('#switch').html();
			if (b == '全部') {
				$('.domainNavbar').slideDown();
				$('#switch').html("收起");
			} else {
				$('.domainNavbar').slideUp();
				$('#switch').html("全部");
			}
		}
		function showFrequent() {
			var b = $('#frequent').html();
			if (b == '常用') {
				$('.frequentNavbar').slideDown();
				$('#frequent').html("收起");
			} else {
				$('.frequentNavbar').slideUp();
				$('#frequent').html("常用");
			}
		}
		function buildHref(domain) {
			return '<a href="<%=contextPath%>/mvc/r/t?op=view&domain=' + domain + '&date=<%=date%>">&nbsp;[&nbsp;' + domain + '&nbsp;]&nbsp;</a>';
		}
		$(document).ready(function() {
			var domains = getcookie('CAT_DOMAINS') || '';
			var domainArray = domains.split("|");
			var html = '';

			for (var i = 0; i < domainArray.length; i++) {
				if (domainArray[i]) {
					html += buildHref(domainArray[i]);
				}
			}
			if (!html) {
				html = buildHref('<%=js(domain)%>');
			}
			$('#frequentNavbar').html(html);
			$("#search_go").bind("click", function() {
				window.location.href = '<%=contextPath%>/mvc/r/t?op=view&domain=' + $("#search").val() + '&date=<%=date%>';
			});
			$('#wrap_search').submit(function() {
				window.location.href = '<%=contextPath%>/mvc/r/t?op=view&domain=' + $("#search").val() + '&date=<%=date%>';
				return false;
			});
			$.widget("custom.catcomplete", $.ui.autocomplete, {
				_renderMenu: function(ul, items) {
					var that = this;
					var currentCategory = "";

					$.each(items, function(index, item) {
						if (item.category != currentCategory) {
							ul.append("<li class='ui-autocomplete-category'>" + item.category + "</li>");
							currentCategory = item.category;
						}
						that._renderItemData(ul, item);
					});
				}
			});

			var data = [];
			<% if (domainGroups != null) {
				for (Map.Entry<String, Department> entry : domainGroups.entrySet()) {
					for (Map.Entry<String, ProjectLine> line : entry.getValue().getProjectLines().entrySet()) {
						for (String itemDomain : line.getValue().getLineDomains()) {
			%>
			data.push({ label: '<%=js(itemDomain)%>', category: '<%=js(line.getKey())%>' });
			<%
						}
					}
				}
			} %>
			$("#search").catcomplete({
				delay: 0,
				source: data
			});
			$('.position').hide();
			$('#Transaction_report').addClass("open active");
			$('#nav_application').click(function() {
				window.location.href = '<%=contextPath%>/mvc/r/t?domain=<%=domain%>&ip=<%=ipAddress%>&date=<%=date%>&reportType=<%=reportType%>&op=view';
			});
			$('#nav_config').click(function() {
				window.location.href = '<%=contextPath%>/mvc/s/config?op=projects';
			});
			$('#nav_document').click(function() {
				window.location.href = '<%=contextPath%>/mvc/r/home?op=view&docName=index';
			});
		});
	</script>
</body>
</html>
<%!
	private String html(String value) {
		if (value == null) {
			return "";
		}
		return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
	}

	private String messageUrl(String failMessageUrl, String successMessageUrl) {
		if (failMessageUrl != null && failMessageUrl.length() > 0) {
			return failMessageUrl;
		}
		return successMessageUrl == null ? "" : successMessageUrl;
	}

	private String js(String value) {
		if (value == null) {
			return "";
		}
		return value.replace("\\", "\\\\").replace("'", "\\'").replace("\r", "\\r").replace("\n", "\\n");
	}

	private String shorten(String value, int max) {
		if (value == null) {
			return "";
		}
		if (value.length() <= max) {
			return value;
		}
		return value.substring(0, max);
	}
%>
