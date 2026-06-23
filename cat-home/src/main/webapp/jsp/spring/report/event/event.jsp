<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="contextPath" value="${empty contextPath ? pageContext.request.contextPath : contextPath}" />
<c:set var="domain" value="${empty domain ? 'cat' : domain}" />
<c:set var="displayDomain" value="${empty displayDomain ? domain : displayDomain}" />
<c:set var="ipAddress" value="${empty ipAddress ? 'All' : ipAddress}" />
<c:set var="date" value="${empty date ? '' : date}" />
<c:set var="encodedType" value="${empty encodedType ? '' : encodedType}" />
<c:set var="reportType" value="${empty reportType ? 'day' : reportType}" />
<!doctype html>
<html lang="en">
<head>
	<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
	<meta charset="utf-8">
	<title>CAT</title>
	<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0">
	<link rel="stylesheet" type="text/css" href="${contextPath}/assets/css/bootstrap.min.css">
	<link rel="stylesheet" type="text/css" href="${contextPath}/assets/css/font-awesome.min.css">
	<link rel="stylesheet" type="text/css" href="${contextPath}/assets/css/jquery-ui.min.css">
	<link rel="stylesheet" type="text/css" href="${contextPath}/assets/css/ace-fonts.css">
	<link rel="stylesheet" type="text/css" href="${contextPath}/assets/css/ace.min.css" id="main-ace-style">
	<link rel="stylesheet" type="text/css" href="${contextPath}/assets/css/ace-skins.min.css">
	<link rel="stylesheet" type="text/css" href="${contextPath}/assets/css/ace-rtl.min.css">
	<link rel="stylesheet" type="text/css" href="${contextPath}/css/body.css">
	<script src="${contextPath}/assets/js/jquery.min.js"></script>
	<script src="${contextPath}/assets/js/ace-extra.min.js"></script>
	<script src="${contextPath}/assets/js/bootstrap.min.js"></script>
	<script src="${contextPath}/js/highcharts.js"></script>
	<script src="${contextPath}/js/baseGraph.js"></script>
	<script src="${contextPath}/assets/js/jquery-ui.min.js"></script>
	<script src="${contextPath}/assets/js/jquery.ui.touch-punch.min.js"></script>
	<script src="${contextPath}/assets/js/ace-elements.min.js"></script>
	<script src="${contextPath}/assets/js/ace.min.js"></script>
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
					<small style="font-size:65%">锛圕entral Application Tracking锛?/small>
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
						<a href="${contextPath}/mvc/r/home?op=view&docName=index">
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
					<a href="${contextPath}/mvc/r/top?op=view&domain=${domain}">
						<i class="menu-icon fa fa-tachometer"></i>
						<span class="menu-text">Dashboard</span>
					</a>
				</li>
				<li id="Transaction_report">
					<a href="${contextPath}/mvc/r/t?domain=${domain}&ip=${ipAddress}&date=${date}&reportType=${reportType}&op=view">
						<i class="menu-icon glyphicon glyphicon-time"></i>
						<span class="menu-text">Transaction</span>
					</a>
				</li>
				<li id="Event_report" class="active open">
					<a href="${contextPath}/mvc/r/e?domain=${domain}&ip=${ipAddress}&date=${date}&reportType=${reportType}&op=view">
						<i class="menu-icon fa fa-flag"></i>
						<span class="menu-text">Event</span>
					</a>
				</li>
				<li id="Problem_report">
					<a href="${contextPath}/mvc/r/p?domain=${domain}&ip=${ipAddress}&date=${date}&reportType=${reportType}&op=view">
						<i class="menu-icon fa fa-bug"></i>
						<span class="menu-text">Problem</span>
					</a>
				</li>
				<li id="Heartbeat_report">
					<a href="${contextPath}/mvc/r/h?domain=${domain}&ip=${ipAddress}&date=${date}&reportType=${reportType}&op=view">
						<i class="menu-icon fa fa-heart"></i>
						<span class="menu-text">Heartbeat</span>
					</a>
				</li>
				<li id="Cross_report">
					<a href="${contextPath}/mvc/r/cross?domain=${domain}&ip=${ipAddress}&date=${date}&reportType=${reportType}&op=view">
						<i class="menu-icon glyphicon glyphicon-random"></i>
						<span class="menu-text">Cross</span>
					</a>
				</li>
				<li id="Business_report">
					<a href="${contextPath}/mvc/r/business?name=${domain}&type=domain">
						<i class="menu-icon fa fa-list-alt"></i>
						<span class="menu-text">Business</span>
					</a>
				</li>
				<li id="State_report">
					<a href="${contextPath}/mvc/r/state?domain=${domain}&ip=${ipAddress}&date=${date}&reportType=${reportType}&op=view">
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
								<td><span class="text-success">${reportStart} to ${reportEnd}</span></td>
								<td>
									<div id="warp_search_group" class="" style="width:250px;">
										<form id="wrap_search" style="margin-left:10px;margin-bottom:0px;">
											<div class="input-group">
												<span class="input-group-btn"><button class="btn btn-sm btn-default" onclick="showDomain()" type="button" id="switch">鍏ㄩ儴</button></span>
												<span class="input-group-btn"><button class="btn btn-sm btn-default" onclick="showFrequent()" type="button" id="frequent">甯哥敤</button></span>
												<span class="input-icon" style="width:200px;">
													<input id="search" type="text" value="${fn:escapeXml(domain)}" class="search-input search-input form-control ui-autocomplete-input" placeholder="input domain for search" autocomplete="off"/>
													<i class="ace-icon fa fa-search nav-search-icon"></i>
												</span>
												<span class="input-group-btn"><button class="btn btn-sm btn-pink" type="button" id="search_go">Go</button></span>
											</div>
										</form>
									</div>
								</td>
								<td>
									<div class="nav-search nav" id="nav-search">
										<span class="text-danger switch">銆?a class="switch" href="${contextPath}/mvc/r/e?op=history&domain=${domain}&ip=${ipAddress}"><span class="text-danger">鍒囧埌鍘嗗彶妯″紡</span></a>銆?/span>
										<c:forEach var="nav" items="${navs}">
											&nbsp;[ <a href="${contextPath}/mvc/r/e?date=${date}&ip=${ipAddress}&step=${nav.hours}&${navPrefix}">${nav.title}</a> ]
										</c:forEach>
										&nbsp;[ <a href="${contextPath}/mvc/r/e?${navPrefix}">now</a> ]&nbsp;
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
							<c:forEach var="department" items="${domainGroups}">
								<c:set var="lines" value="${department.value.projectLines}" />
								<c:forEach var="line" items="${lines}" varStatus="lineStatus">
									<tr>
										<c:if test="${lineStatus.first}">
											<td class="department" rowspan="${fn:length(lines)}"><c:out value="${department.key}" /></td>
										</c:if>
										<td class="department"><c:out value="${line.key}" /></td>
										<td><div class="domain">
											<c:forEach var="itemDomain" items="${line.value.lineDomains}">
												&nbsp;<a class="domainItem" href="${contextPath}/mvc/r/e?op=view&domain=${itemDomain}&date=${date}&reportType=${reportType}">[&nbsp;<c:out value="${itemDomain}" />&nbsp;]</a>&nbsp;
											</c:forEach>
										</div></td>
									</tr>
								</c:forEach>
							</c:forEach>
						</table>
					</div>
					<div class="frequentNavbar" style="display:none;font-size:small">
						<table border="1" rules="all"><tr><td class="domain" style="word-break:break-all" id="frequentNavbar"></td></tr></table>
					</div>
					<table class="machines">
						<tr class="left">
							<th>&nbsp;[&nbsp;
								<a href="${contextPath}/mvc/r/e?domain=${domain}&date=${date}&type=${encodedType}" class="${ipAddress eq 'All' ? 'current' : ''}">All</a>
								&nbsp;]&nbsp;
								<c:forEach var="ip" items="${ips}">
									&nbsp;[&nbsp;<a href="${contextPath}/mvc/r/e?domain=${domain}&ip=${ip}&date=${date}&type=${encodedType}" class="${ip eq ipAddress ? 'current' : ''}"><c:out value="${ip}" /></a>&nbsp;]&nbsp;
								</c:forEach>
							</th>
						</tr>
					</table>
					<script type="text/javascript" src="${contextPath}/js/appendHostname.js"></script>
					<script type="text/javascript">
						$(document).ready(function() { appendHostname(${empty ipToHostnameStr ? "{}" : ipToHostnameStr}); });
					</script>
					<table class="groups">
						<tr class="left">
							<th>
								<c:forEach var="itemGroup" items="${groups}">
									&nbsp;[&nbsp;<a href="${contextPath}/mvc/r/e?op=groupReport&domain=${domain}&date=${date}&group=${itemGroup}"><c:out value="${itemGroup}" /></a>&nbsp;]&nbsp;
								</c:forEach>
							</th>
						</tr>
					</table>
					<table class="table table-hover table-striped table-condensed" style="width:100%;">
						<c:choose>
							<c:when test="${empty type}">
								<tr>
									<th class="left"><a href="${contextPath}/mvc/r/e?domain=${domain}&date=${date}&ip=${ipAddress}&sort=type">Type</a></th>
									<th class="right"><a href="${contextPath}/mvc/r/e?domain=${domain}&date=${date}&ip=${ipAddress}&sort=total">Total</a></th>
									<th class="right"><a href="${contextPath}/mvc/r/e?domain=${domain}&date=${date}&ip=${ipAddress}&sort=failure">Failure</a></th>
									<th class="right"><a href="${contextPath}/mvc/r/e?domain=${domain}&date=${date}&ip=${ipAddress}&sort=failurePercent">Failure%</a></th>
									<th class="right">Sample Link</th>
									<th class="right">QPS</th>
								</tr>
								<c:forEach var="item" items="${displayTypeReport.results}" varStatus="status">
									<c:set var="e" value="${item.detail}" />
									<tr class="right">
										<td class="left"><a href="${contextPath}/mvc/r/e?op=graphs&domain=${domain}&date=${date}&type=${item.type}&ip=${ipAddress}" class="graph_link" data-status="${status.index}">[:: show ::]</a>
										&nbsp;&nbsp;<a href="${contextPath}/mvc/r/e?domain=${report.domain}&date=${date}&ip=${ipAddress}&type=${item.type}"><c:out value="${e.id}" /></a></td>
										<td><fmt:formatNumber value="${e.totalCount}" pattern="#,###,###,###,##0" /></td>
										<td><fmt:formatNumber value="${e.failCount}" pattern="#,###,###,###,##0" /></td>
										<td>&nbsp;<fmt:formatNumber value="${e.failPercent / 100}" pattern="0.0000%" /></td>
										<td><a href="${contextPath}/mvc/r/m/${empty e.failMessageUrl ? e.successMessageUrl : e.failMessageUrl}?domain=${domain}">Log View</a></td>
										<td><fmt:formatNumber value="${e.tps}" pattern="###,##0.0" /></td>
									</tr>
									<tr class="graphs"><td colspan="7" style="display:none"><div id="${status.index}" style="display:none"></div></td></tr>
									<tr></tr>
								</c:forEach>
							</c:when>
							<c:otherwise>
								<tr>
									<th class="left"><a href="${contextPath}/mvc/r/e?op=graphs&domain=${domain}&date=${date}&type=${encodedType}&ip=${ipAddress}" class="graph_link" data-status="-1">[:: show ::]</a>
									<a href="${contextPath}/mvc/r/e?domain=${domain}&date=${date}&ip=${ipAddress}&type=${encodedType}&sort=type"> Name</a></th>
									<th class="right"><a href="${contextPath}/mvc/r/e?domain=${domain}&date=${date}&ip=${ipAddress}&type=${encodedType}&sort=total">Total</a></th>
									<th class="right"><a href="${contextPath}/mvc/r/e?domain=${domain}&date=${date}&ip=${ipAddress}&type=${encodedType}&sort=failure">Failure</a></th>
									<th class="right"><a href="${contextPath}/mvc/r/e?domain=${domain}&date=${date}&ip=${ipAddress}&type=${encodedType}&sort=failurePercent">Failure%</a></th>
									<th class="center">Sample Link</th>
									<th class="right"><a href="${contextPath}/mvc/r/e?domain=${domain}&date=${date}&ip=${ipAddress}&type=${encodedType}&sort=total">QPS</a></th>
									<th class="right"><a href="${contextPath}/mvc/r/e?domain=${domain}&date=${date}&ip=${ipAddress}&type=${encodedType}&sort=total">Percent%</a></th>
								</tr>
								<tr class="graphs"><td colspan="7" style="display:none"><div id="-1" style="display:none"></div></td></tr>
								<c:forEach var="item" items="${displayNameReport.results}" varStatus="status">
									<c:set var="e" value="${item.detail}" />
									<tr class="right">
										<td class="left">
											<c:if test="${status.index > 0}">
												<a href="${contextPath}/mvc/r/e?op=graphs&domain=${report.domain}&ip=${ipAddress}&date=${date}&type=${encodedType}&name=${item.name}" class="graph_link" data-status="${status.index}">[:: show ::]</a>
											</c:if>
											&nbsp;&nbsp;<c:out value="${e.id}" />
										</td>
										<td><fmt:formatNumber value="${e.totalCount}" pattern="#,###,###,###,##0" /></td>
										<td><fmt:formatNumber value="${e.failCount}" pattern="#,###,###,###,##0" /></td>
										<td>&nbsp;<fmt:formatNumber value="${e.failPercent / 100}" pattern="0.0000%" /></td>
										<td class="center"><a href="${contextPath}/mvc/r/m/${empty e.failMessageUrl ? e.successMessageUrl : e.failMessageUrl}?domain=${domain}">Log View</a></td>
										<td><fmt:formatNumber value="${e.tps}" pattern="###,##0.0" /></td>
										<td><fmt:formatNumber value="${e.totalPercent}" pattern="0.0000%" /></td>
									</tr>
									<tr class="graphs"><td colspan="7" style="display:none"><div id="${status.index}" style="display:none"></div></td></tr>
									<tr></tr>
								</c:forEach>
							</c:otherwise>
						</c:choose>
					</table>
					<font color="white"></font>
					<c:if test="${not empty type}">
						<table><tr><td><div id="eventGraph" class="pieChart"></div></td></tr></table>
						<script type="text/javascript">
							var data = ${empty pieChart ? "{}" : pieChart};
							graphPieChart(document.getElementById('eventGraph'), data);
						</script>
					</c:if>
				</div>
			</div>
		</div>
	</div>
	<script src="${contextPath}/js/event.js"></script>
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
			if (b == '鍏ㄩ儴') {
				$('.domainNavbar').slideDown();
				$('#switch').html("鏀惰捣");
			} else {
				$('.domainNavbar').slideUp();
				$('#switch').html("鍏ㄩ儴");
			}
		}
		function showFrequent() {
			var b = $('#frequent').html();
			if (b == '甯哥敤') {
				$('.frequentNavbar').slideDown();
				$('#frequent').html("鏀惰捣");
			} else {
				$('.frequentNavbar').slideUp();
				$('#frequent').html("甯哥敤");
			}
		}
		function buildHref(domain) {
			return '<a href="${contextPath}/mvc/r/e?op=view&domain=' + domain + '&date=${date}">&nbsp;[&nbsp;' + domain + '&nbsp;]&nbsp;</a>';
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
				html = buildHref('${fn:escapeXml(domain)}');
			}
			$('#frequentNavbar').html(html);
			$("#search_go").bind("click", function() {
				window.location.href = '${contextPath}/mvc/r/e?op=view&domain=' + $("#search").val() + '&date=${date}';
			});
			$('#wrap_search').submit(function() {
				window.location.href = '${contextPath}/mvc/r/e?op=view&domain=' + $("#search").val() + '&date=${date}';
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
			<c:forEach var="department" items="${domainGroups}">
				<c:forEach var="line" items="${department.value.projectLines}">
					<c:forEach var="itemDomain" items="${line.value.lineDomains}">
			data.push({ label: '<c:out value="${itemDomain}" />', category: '<c:out value="${line.key}" />' });
					</c:forEach>
				</c:forEach>
			</c:forEach>
			$("#search").catcomplete({
				delay: 0,
				source: data
			});
			$('.position').hide();
			$('#Event_report').addClass("open active");
			$('#nav_application').click(function() {
				window.location.href = '${contextPath}/mvc/r/e?domain=${domain}&ip=${ipAddress}&date=${date}&reportType=${reportType}&op=view';
			});
			$('#nav_config').click(function() {
				window.location.href = '${contextPath}/mvc/s/config?op=projects';
			});
			$('#nav_document').click(function() {
				window.location.href = '${contextPath}/mvc/r/home?op=view&docName=index';
			});
		});
	</script>
</body>
</html>
