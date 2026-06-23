<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8" %>
<%@ page import="java.text.DecimalFormat" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.dianping.cat.Constants" %>
<%@ page import="com.dianping.cat.consumer.event.model.entity.EventName" %>
<%@ page import="com.dianping.cat.consumer.event.model.entity.EventReport" %>
<%@ page import="com.dianping.cat.consumer.event.model.entity.EventType" %>
<%@ page import="com.dianping.cat.mvc.UrlNav" %>
<%@ page import="com.dianping.cat.report.page.event.DisplayNames" %>
<%@ page import="com.dianping.cat.report.page.event.DisplayNames.EventNameModel" %>
<%@ page import="com.dianping.cat.report.page.event.DisplayTypes" %>
<%@ page import="com.dianping.cat.report.page.event.DisplayTypes.EventTypeModel" %>
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
	String reportStart = (String) request.getAttribute("reportStart");
	String reportEnd = (String) request.getAttribute("reportEnd");
	String ipToHostnameStr = (String) request.getAttribute("ipToHostnameStr");
	String pieChart = (String) request.getAttribute("pieChart");
	EventReport report = (EventReport) request.getAttribute("report");
	DisplayTypes displayTypeReport = (DisplayTypes) request.getAttribute("displayTypeReport");
	DisplayNames displayNameReport = (DisplayNames) request.getAttribute("displayNameReport");
	List<String> ips = (List<String>) request.getAttribute("ips");
	List<String> groups = (List<String>) request.getAttribute("groups");
	Map<String, Department> domainGroups = (Map<String, Department>) request.getAttribute("domainGroups");
	UrlNav[] navs = (UrlNav[]) request.getAttribute("navs");
	Number sample = (Number) request.getAttribute("sample");
	DecimalFormat integerFormat = new DecimalFormat("#,###,###,###,##0");
	DecimalFormat percentFormat = new DecimalFormat("0.0000%");
	DecimalFormat oneDecimalFormat = new DecimalFormat("###,##0.0");
	DecimalFormat totalPercentFormat = new DecimalFormat("0.0000%");
	int lastIndex = -1;

	if (contextPath == null) {
		contextPath = request.getContextPath();
	}
	if (domain == null) {
		domain = Constants.CAT;
	}
	if (displayDomain == null) {
		displayDomain = domain;
	}
	if (ipAddress == null) {
		ipAddress = Constants.ALL;
	}
	if (date == null) {
		date = "";
	}
	if (reportType == null || reportType.length() == 0) {
		reportType = "day";
	}
	if (encodedType == null) {
		encodedType = "";
	}
	String navPrefix = "ip=" + ipAddress + "&domain=" + domain + (type == null ? "" : "&type=" + encodedType);
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
				<li id="Transaction_report">
					<a href="<%=contextPath%>/mvc/r/t?domain=<%=domain%>&ip=<%=ipAddress%>&date=<%=date%>&reportType=<%=reportType%>&op=view">
						<i class="menu-icon glyphicon glyphicon-time"></i>
						<span class="menu-text">Transaction</span>
					</a>
				</li>
				<li id="Event_report" class="active open">
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
													<input id="search" type="text" value="<%=html(domain)%>" class="search-input search-input form-control ui-autocomplete-input" placeholder="input domain for search" autocomplete="off"/>
													<i class="ace-icon fa fa-search nav-search-icon"></i>
												</span>
												<span class="input-group-btn"><button class="btn btn-sm btn-pink" type="button" id="search_go">Go</button></span>
											</div>
										</form>
									</div>
								</td>
								<td>
									<div class="nav-search nav" id="nav-search">
										<span class="text-danger switch">【<a class="switch" href="<%=contextPath%>/mvc/r/e?op=history&domain=<%=domain%>&ip=<%=ipAddress%>"><span class="text-danger">切到历史模式</span></a>】</span>
										<% if (navs != null) { for (UrlNav nav : navs) { %>
											&nbsp;[ <a href="<%=contextPath%>/mvc/r/e?date=<%=date%>&ip=<%=ipAddress%>&step=<%=nav.getHours()%>&<%=navPrefix%>"><%=nav.getTitle()%></a> ]
										<% } } %>
										&nbsp;[ <a href="<%=contextPath%>/mvc/r/e?<%=navPrefix%>">now</a> ]&nbsp;
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
												&nbsp;<a class="domainItem" href="<%=contextPath%>/mvc/r/e?op=view&domain=<%=itemDomain%>&date=<%=date%>&reportType=<%=reportType%>">[&nbsp;<%=html(itemDomain)%>&nbsp;]</a>&nbsp;
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
					<table class="machines">
						<tr class="left">
							<th>&nbsp;[&nbsp;
								<a href="<%=contextPath%>/mvc/r/e?domain=<%=domain%>&date=<%=date%>&type=<%=encodedType%>" class="<%=Constants.ALL.equals(ipAddress) ? "current" : ""%>">All</a>
								&nbsp;]&nbsp;
								<% if (ips != null) { for (String ip : ips) { %>
									&nbsp;[&nbsp;<a href="<%=contextPath%>/mvc/r/e?domain=<%=domain%>&ip=<%=ip%>&date=<%=date%>&type=<%=encodedType%>" class="<%=ipAddress.equals(ip) ? "current" : ""%>"><%=html(ip)%></a>&nbsp;]&nbsp;
								<% } } %>
							</th>
						</tr>
					</table>
					<script type="text/javascript" src="<%=contextPath%>/js/appendHostname.js"></script>
					<script type="text/javascript">
						$(document).ready(function() {
							appendHostname(<%=ipToHostnameStr == null ? "{}" : ipToHostnameStr%>);
						});
					</script>
					<table class="groups">
						<tr class="left">
							<th>
								<% if (groups != null) { for (String itemGroup : groups) { %>
									&nbsp;[&nbsp;<a href="<%=contextPath%>/mvc/r/e?op=groupReport&domain=<%=domain%>&date=<%=date%>&group=<%=itemGroup%>"><%=html(itemGroup)%></a>&nbsp;]&nbsp;
								<% } } %>
							</th>
						</tr>
					</table>
					<table class="table table-hover table-striped table-condensed" style="width:100%;">
						<% if (type == null || type.length() == 0) { %>
							<tr>
								<th class="left"><a href="<%=contextPath%>/mvc/r/e?domain=<%=domain%>&date=<%=date%>&ip=<%=ipAddress%>&sort=type">Type</a></th>
								<th class="right"><a href="<%=contextPath%>/mvc/r/e?domain=<%=domain%>&date=<%=date%>&ip=<%=ipAddress%>&sort=total">Total</a></th>
								<th class="right"><a href="<%=contextPath%>/mvc/r/e?domain=<%=domain%>&date=<%=date%>&ip=<%=ipAddress%>&sort=failure">Failure</a></th>
								<th class="right"><a href="<%=contextPath%>/mvc/r/e?domain=<%=domain%>&date=<%=date%>&ip=<%=ipAddress%>&sort=failurePercent">Failure%</a></th>
								<th class="right">Sample Link</th>
								<th class="right">QPS</th>
							</tr>
							<% if (displayTypeReport != null) {
								int index = 0;
								for (EventTypeModel item : displayTypeReport.getResults()) {
									EventType e = item.getDetail();
									lastIndex = index;
							%>
								<tr class="right">
									<td class="left"><a href="<%=contextPath%>/mvc/r/e?op=graphs&domain=<%=domain%>&date=<%=date%>&type=<%=item.getType()%>&ip=<%=ipAddress%>" class="graph_link" data-status="<%=index%>">[:: show ::]</a>
									&nbsp;&nbsp;<a href="<%=contextPath%>/mvc/r/e?domain=<%=report.getDomain()%>&date=<%=date%>&ip=<%=ipAddress%>&type=<%=item.getType()%>"><%=html(e.getId())%></a></td>
									<td><%=integerFormat.format(e.getTotalCount())%></td>
									<td><%=integerFormat.format(e.getFailCount())%></td>
									<td>&nbsp;<%=percentFormat.format(e.getFailPercent() / 100)%></td>
									<td><a href="<%=contextPath%>/mvc/r/m/<%=messageUrl(e.getFailMessageUrl(), e.getSuccessMessageUrl())%>?domain=<%=domain%>">Log View</a></td>
									<td><%=oneDecimalFormat.format(e.getTps())%></td>
								</tr>
								<tr class="graphs"><td colspan="7" style="display:none"><div id="<%=index%>" style="display:none"></div></td></tr>
								<tr></tr>
							<% index++; } } %>
						<% } else { %>
							<tr>
								<th class="left"><a href="<%=contextPath%>/mvc/r/e?op=graphs&domain=<%=domain%>&date=<%=date%>&type=<%=encodedType%>&ip=<%=ipAddress%>" class="graph_link" data-status="-1">[:: show ::]</a>
								<a href="<%=contextPath%>/mvc/r/e?domain=<%=domain%>&date=<%=date%>&ip=<%=ipAddress%>&type=<%=encodedType%>&sort=type"> Name</a></th>
								<th class="right"><a href="<%=contextPath%>/mvc/r/e?domain=<%=domain%>&date=<%=date%>&ip=<%=ipAddress%>&type=<%=encodedType%>&sort=total">Total</a></th>
								<th class="right"><a href="<%=contextPath%>/mvc/r/e?domain=<%=domain%>&date=<%=date%>&ip=<%=ipAddress%>&type=<%=encodedType%>&sort=failure">Failure</a></th>
								<th class="right"><a href="<%=contextPath%>/mvc/r/e?domain=<%=domain%>&date=<%=date%>&ip=<%=ipAddress%>&type=<%=encodedType%>&sort=failurePercent">Failure%</a></th>
								<th class="center">Sample Link</th>
								<th class="right"><a href="<%=contextPath%>/mvc/r/e?domain=<%=domain%>&date=<%=date%>&ip=<%=ipAddress%>&type=<%=encodedType%>&sort=total">QPS</a></th>
								<th class="right"><a href="<%=contextPath%>/mvc/r/e?domain=<%=domain%>&date=<%=date%>&ip=<%=ipAddress%>&type=<%=encodedType%>&sort=total">Percent%</a></th>
							</tr>
							<tr class="graphs"><td colspan="7" style="display:none"><div id="-1" style="display:none"></div></td></tr>
							<% if (displayNameReport != null) {
								int index = 0;
								for (EventNameModel item : displayNameReport.getResults()) {
									EventName e = item.getDetail();
									lastIndex = index;
							%>
								<tr class="right">
									<td class="left">
										<% if (index > 0) { %>
											<a href="<%=contextPath%>/mvc/r/e?op=graphs&domain=<%=report.getDomain()%>&ip=<%=ipAddress%>&date=<%=date%>&type=<%=encodedType%>&name=<%=item.getName()%>" class="graph_link" data-status="<%=index%>">[:: show ::]</a>
										<% } %>
										&nbsp;&nbsp;<%=html(e.getId())%>
									</td>
									<td><%=integerFormat.format(e.getTotalCount())%></td>
									<td><%=integerFormat.format(e.getFailCount())%></td>
									<td>&nbsp;<%=percentFormat.format(e.getFailPercent() / 100)%></td>
									<td class="center"><a href="<%=contextPath%>/mvc/r/m/<%=messageUrl(e.getFailMessageUrl(), e.getSuccessMessageUrl())%>?domain=<%=domain%>">Log View</a></td>
									<td><%=oneDecimalFormat.format(e.getTps())%></td>
									<td><%=totalPercentFormat.format(e.getTotalPercent())%></td>
								</tr>
								<tr class="graphs"><td colspan="7" style="display:none"><div id="<%=index%>" style="display:none"></div></td></tr>
								<tr></tr>
							<% index++; } } %>
						<% } %>
					</table>
					<font color="white"><%=lastIndex + 1%></font>
					<% if (type != null && type.length() > 0) { %>
						<table><tr><td><div id="eventGraph" class="pieChart"></div></td></tr></table>
						<script type="text/javascript">
							var data = <%=pieChart == null ? "{}" : pieChart%>;
							graphPieChart(document.getElementById('eventGraph'), data);
						</script>
					<% } %>
				</div>
			</div>
		</div>
	</div>
	<script src="<%=contextPath%>/js/event.js"></script>
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
			return '<a href="<%=contextPath%>/mvc/r/e?op=view&domain=' + domain + '&date=<%=date%>">&nbsp;[&nbsp;' + domain + '&nbsp;]&nbsp;</a>';
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
				window.location.href = '<%=contextPath%>/mvc/r/e?op=view&domain=' + $("#search").val() + '&date=<%=date%>';
			});
			$('#wrap_search').submit(function() {
				window.location.href = '<%=contextPath%>/mvc/r/e?op=view&domain=' + $("#search").val() + '&date=<%=date%>';
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
			$('#Event_report').addClass("open active");
			$('#nav_application').click(function() {
				window.location.href = '<%=contextPath%>/mvc/r/e?domain=<%=domain%>&ip=<%=ipAddress%>&date=<%=date%>&reportType=<%=reportType%>&op=view';
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

	private String js(String value) {
		if (value == null) {
			return "";
		}
		return value.replace("\\", "\\\\").replace("'", "\\'").replace("\r", "\\r").replace("\n", "\\n");
	}

	private String messageUrl(String failMessageUrl, String successMessageUrl) {
		if (failMessageUrl != null && failMessageUrl.length() > 0) {
			return failMessageUrl;
		}
		return successMessageUrl == null ? "" : successMessageUrl;
	}
%>
