<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.text.DecimalFormat" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.dianping.cat.report.page.dependency.TopMetric" %>
<%
	String contextPath = request.getContextPath();
	String domain = (String) request.getAttribute("domain");
	String ipAddress = (String) request.getAttribute("ipAddress");
	String date = (String) request.getAttribute("date");
	String reportStart = (String) request.getAttribute("reportStart");
	String reportEnd = (String) request.getAttribute("reportEnd");
	String message = (String) request.getAttribute("message");
	String navPrefix = "domain=" + domain + "&op=view";
	Integer minute = (Integer) request.getAttribute("minute");
	Integer maxMinute = (Integer) request.getAttribute("maxMinute");
	Integer frequency = (Integer) request.getAttribute("frequency");
	Boolean refresh = (Boolean) request.getAttribute("refresh");
	Boolean fullScreen = (Boolean) request.getAttribute("fullScreen");
	List<Integer> minutes = (List<Integer>) request.getAttribute("minutes");
	Map<String, List<TopMetric.Item>> topResult = (Map<String, List<TopMetric.Item>>) request.getAttribute("topResult");
	DecimalFormat countFormat = new DecimalFormat("0");

	if (domain == null || domain.length() == 0) {
		domain = "cat";
	}
	if (ipAddress == null || ipAddress.length() == 0) {
		ipAddress = "All";
	}
	if (date == null) {
		date = "";
	}
	navPrefix = "domain=" + domain + "&op=view";
	if (minute == null) {
		minute = Integer.valueOf(0);
	}
	if (maxMinute == null) {
		maxMinute = Integer.valueOf(60);
	}
	if (frequency == null) {
		frequency = Integer.valueOf(10);
	}
	if (refresh == null) {
		refresh = Boolean.FALSE;
	}
	if (fullScreen == null) {
		fullScreen = Boolean.FALSE;
	}
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
	<script src="<%=contextPath%>/assets/js/jquery-ui.min.js"></script>
	<script src="<%=contextPath%>/assets/js/jquery.ui.touch-punch.min.js"></script>
	<script src="<%=contextPath%>/assets/js/ace-elements.min.js"></script>
	<script src="<%=contextPath%>/assets/js/ace.min.js"></script>
	<style>
		.tooltip-inner { max-width: 36555px; }
		.smallTable { font-size: small; }
		.pagination { margin: 4px 0; }
		.pagination > li > a, .pagination > li > span { padding: 3px 10px; }
		.tab-content table {
			max-width: 100%;
			background-color: transparent;
			border-collapse: collapse;
			border-spacing: 0;
		}
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
					<button class="btn btn-success btn-sm" id="nav_application">
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
				<li id="Dashboard_report" class="active open">
					<a href="<%=contextPath%>/mvc/r/top?op=view&domain=<%=domain%>">
						<i class="menu-icon fa fa-tachometer"></i>
						<span class="menu-text">Dashboard</span>
					</a>
					<b class="arrow"></b>
				</li>
				<li id="Transaction_report">
					<a href="<%=contextPath%>/mvc/r/t?domain=<%=domain%>&ip=<%=ipAddress%>&date=<%=date%>&reportType=day&op=view">
						<i class="menu-icon glyphicon glyphicon-time"></i>
						<span class="menu-text">Transaction</span>
					</a>
					<b class="arrow"></b>
				</li>
				<li id="Event_report">
					<a href="<%=contextPath%>/mvc/r/e?domain=<%=domain%>&ip=<%=ipAddress%>&date=<%=date%>&reportType=day&op=view">
						<i class="menu-icon fa fa-flag"></i>
						<span class="menu-text">Event</span>
					</a>
					<b class="arrow"></b>
				</li>
				<li id="Problem_report">
					<a href="<%=contextPath%>/mvc/r/p?domain=<%=domain%>&ip=<%=ipAddress%>&date=<%=date%>&reportType=day&op=view">
						<i class="menu-icon fa fa-bug"></i>
						<span class="menu-text">Problem</span>
					</a>
					<b class="arrow"></b>
				</li>
				<li id="Heartbeat_report">
					<a href="<%=contextPath%>/mvc/r/h?domain=<%=domain%>&ip=<%=ipAddress%>&date=<%=date%>&reportType=day&op=view">
						<i class="menu-icon fa fa-heart"></i>
						<span class="menu-text">Heartbeat</span>
					</a>
					<b class="arrow"></b>
				</li>
				<li id="Cross_report">
					<a href="<%=contextPath%>/mvc/r/cross?domain=<%=domain%>&ip=<%=ipAddress%>&date=<%=date%>&reportType=day&op=view">
						<i class="menu-icon glyphicon glyphicon-random"></i>
						<span class="menu-text">Cross</span>
					</a>
					<b class="arrow"></b>
				</li>
				<li id="Business_report">
					<a href="<%=contextPath%>/mvc/r/business?name=<%=domain%>&type=domain">
						<i class="menu-icon fa fa-list-alt"></i>
						<span class="menu-text">Business</span>
					</a>
					<b class="arrow"></b>
				</li>
				<li id="State_report">
					<a href="<%=contextPath%>/mvc/r/state?domain=<%=domain%>&ip=<%=ipAddress%>&date=<%=date%>&reportType=day&op=view">
						<i class="menu-icon fa fa-bar-chart-o"></i>
						<span class="menu-text">State</span>
					</a>
					<b class="arrow"></b>
				</li>
			</ul>
			<div class="sidebar-toggle sidebar-collapse" id="sidebar-collapse">
				<i class="ace-icon fa fa-angle-double-left" data-icon1="ace-icon fa fa-angle-double-left" data-icon2="ace-icon fa fa-angle-double-right"></i>
			</div>
		</div>
		<div class="main-content">
			<div style="padding-top:2px;padding-right:8px;">
				<div class="report">
				<div class="breadcrumbs" id="breadcrumbs">
					<table>
						<tr>
							<td><span class="text-success"><%=reportStart%> to <%=reportEnd%></span></td>
							<td>
								<div class="nav-search nav" id="nav-search">
									<span class="text-danger switch">【<a class="switch" href="<%=contextPath%>/mvc/r/top?op=history&domain=<%=domain%>&ip=<%=ipAddress%>"><span class="text-danger">切到历史模式</span></a>】</span>
									&nbsp;[ <a href="<%=contextPath%>/mvc/r/top?date=<%=date%>&ip=<%=ipAddress%>&step=-168&<%=navPrefix%>">-7d</a> ]
									&nbsp;[ <a href="<%=contextPath%>/mvc/r/top?date=<%=date%>&ip=<%=ipAddress%>&step=-24&<%=navPrefix%>">-1d</a> ]
									&nbsp;[ <a href="<%=contextPath%>/mvc/r/top?date=<%=date%>&ip=<%=ipAddress%>&step=-1&<%=navPrefix%>">-1h</a> ]
									&nbsp;[ <a href="<%=contextPath%>/mvc/r/top?date=<%=date%>&ip=<%=ipAddress%>&step=1&<%=navPrefix%>">+1h</a> ]
									&nbsp;[ <a href="<%=contextPath%>/mvc/r/top?date=<%=date%>&ip=<%=ipAddress%>&step=24&<%=navPrefix%>">+1d</a> ]
									&nbsp;[ <a href="<%=contextPath%>/mvc/r/top?date=<%=date%>&ip=<%=ipAddress%>&step=168&<%=navPrefix%>">+7d</a> ]
									&nbsp;[ <a href="<%=contextPath%>/mvc/r/top?<%=navPrefix%>">now</a> ]&nbsp;
								</div>
							</td>
						</tr>
					</table>
				</div>
				<div class="text-center">
					<ul class="pagination">
						<%
							if (minutes != null) {
								for (Integer item : minutes) {
									boolean disabled = item.intValue() > maxMinute.intValue();
									String label = item.intValue() < 10 ? "0" + item : String.valueOf(item);
						%>
							<li id="minute<%=item%>" class="<%=disabled ? "disabled" : ""%>">
								<a class="href<%=item%>" href="<%=contextPath%>/mvc/r/top?op=view&domain=<%=domain%>&date=<%=date%>&minute=<%=item%>&fullScreen=<%=fullScreen%>&refresh=<%=refresh%>&frequency=<%=frequency%>"><%=label%></a>
							</li>
						<%
								}
							}
						%>
					</ul>
				</div>
				<div class="">
					<%
						if (message != null && message.length() > 0) {
					%>
						<h3 class="text-center text-danger">出问题CAT的服务端:<%=message%></h3>
					<%
						} else {
					%>
						<h3 class="text-center text-success">CAT服务端正常</h3>
					<%
						}
						if (topResult != null) {
							for (Map.Entry<String, List<TopMetric.Item>> entry : topResult.entrySet()) {
					%>
						<table class="smallTable" style="float:left" border="1">
							<tr><th colspan="2" class="text-danger"><%=entry.getKey()%></th></tr>
							<tr><th>系统</th><th>个</th></tr>
							<%
								for (TopMetric.Item item : entry.getValue()) {
									String style = "";
									String linkStyle = "";

									if (item.getAlert() == 2) {
										style = "background-color:red;color:white;";
										linkStyle = "color:white;";
									} else if (item.getAlert() == 1) {
										style = "background-color:#bfa22f;color:white;";
										linkStyle = "color:white;";
									}
							%>
								<tr>
									<td style="<%=style%>">
										<a class="hreftip" style="<%=linkStyle%>" href="<%=contextPath%>/mvc/r/p?domain=<%=item.getDomain()%>&date=<%=date%>" title="<%=item.getErrorInfo()%>"><%=shorten(item.getDomain(), 18)%></a>
									</td>
									<td style="<%=style%>text-align:right"><%=countFormat.format(item.getValue())%></td>
								</tr>
							<%
								}
							%>
						</table>
					<%
							}
						}
					%>
				</div>
				</div>
			</div>
		</div>
	</div>
	<script type="text/javascript">
		$(document).ready(function() {
			$('.switch').hide();
			$('.href<%=minute%>').css('color', 'red').css('font-weight', 'bold');
			$('#minute<%=minute%>').addClass('disabled');
			$('.hreftip').tooltip({container:'body', html:true, delay:{show:0, hide:0}});
			$('#nav_application').click(function() {
				window.location.href = '<%=contextPath%>/mvc/r/t?domain=<%=domain%>&ip=<%=ipAddress%>&date=<%=date%>&reportType=day&op=view';
			});
			$('#nav_config').click(function() {
				window.location.href = '<%=contextPath%>/mvc/s/config?op=projects';
			});
			$('#nav_document').click(function() {
				window.location.href = '<%=contextPath%>/mvc/r/home?op=view&docName=index';
			});
			<%
				if (refresh.booleanValue()) {
			%>
			setInterval(function() { location.reload(); }, <%=frequency%> * 1000);
			<%
				}
			%>
		});
	</script>
</body>
</html>
<%!
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
