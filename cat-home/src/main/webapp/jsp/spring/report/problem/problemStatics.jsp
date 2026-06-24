<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="contextPath" value="${empty contextPath ? pageContext.request.contextPath : contextPath}" />
<c:set var="domain" value="${empty domain ? 'cat' : domain}" />
<c:set var="ipAddress" value="${empty ipAddress ? 'All' : ipAddress}" />
<c:set var="date" value="${empty date ? '' : date}" />
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
		.top { vertical-align: top; }
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
		<c:set var="activeReport" value="Problem" scope="request" />
		<jsp:include page="../common/reportSidebar.jsp" />
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
												<span class="input-group-btn"><button class="btn btn-sm btn-default" onclick="showDomain()" type="button" id="switch">全部</button></span>
												<span class="input-group-btn"><button class="btn btn-sm btn-default" onclick="showFrequent()" type="button" id="frequent">常用</button></span>
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
										<span class="text-danger switch">【<a class="switch" href="${contextPath}/mvc/r/p?op=history&domain=${domain}&ip=${ipAddress}"><span class="text-danger">切到历史模式</span></a>】</span>
										<c:forEach var="nav" items="${navs}">
											&nbsp;[ <a href="${contextPath}/mvc/r/p?date=${date}&ip=${ipAddress}&step=${nav.hours}&${navPrefix}">${nav.title}</a> ]
										</c:forEach>
										&nbsp;[ <a href="${contextPath}/mvc/r/p?${navPrefix}">now</a> ]&nbsp;
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
												&nbsp;<a class="domainItem" href="${contextPath}/mvc/r/p?op=view&domain=${itemDomain}&date=${date}&reportType=${reportType}">[&nbsp;<c:out value="${itemDomain}" />&nbsp;]</a>&nbsp;
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
						<tr style="text-align:left">
							<th>&nbsp;[&nbsp;
								<a href="${contextPath}/mvc/r/p?domain=${domain}&date=${date}${queryString}" class="${ipAddress eq 'All' ? 'current' : ''}">All</a>
								&nbsp;]&nbsp;
								<c:forEach var="ip" items="${ips}">
									&nbsp;[&nbsp;<a href="${contextPath}/mvc/r/p?op=view&domain=${domain}&ip=${ip}&date=${date}${queryString}" class="${ip eq ipAddress ? 'current' : ''}"><c:out value="${ip}" /></a>&nbsp;]&nbsp;
								</c:forEach>
							</th>
						</tr>
						<tr class="left">
							<th>
								<c:forEach var="itemGroup" items="${groups}">
									&nbsp;[&nbsp;<a href="${contextPath}/mvc/r/p?op=groupReport&domain=${domain}&date=${date}&group=${itemGroup}${queryString}"><c:out value="${itemGroup}" /></a>&nbsp;]&nbsp;
								</c:forEach>
							</th>
						</tr>
						<tr><th>
							<div class="text-left">Long-url <select class="input-small" size="1" id="p_longUrl">
									<c:out value="${defaultThreshold}" escapeXml="false" />
									<option value="500">0.5 Sec</option>
									<option value="1000">1.0 Sec</option>
									<option value="1500">1.5 Sec</option>
									<option value="2000">2.0 Sec</option>
									<option value="3000">3.0 Sec</option>
									<option value="5000">5.0 Sec</option>
								</select>
								Long-sql <select size="1" id="p_longSql" class="input-small">
									<c:out value="${defaultSqlThreshold}" escapeXml="false" />
									<option value="100">100 ms</option>
									<option value="500">500 ms</option>
									<option value="1000">1000 ms</option>
									<option value="3000">3000 ms</option>
									<option value="5000">5000 ms</option>
								</select>
								Long-service <select size="1" id="p_longService" class="input-small">
									<c:out value="${defaultSqlThreshold}" escapeXml="false" />
									<option value="50">50 ms</option>
									<option value="100">100 ms</option>
									<option value="500">500 ms</option>
									<option value="1000">1000 ms</option>
									<option value="3000">3000 ms</option>
									<option value="5000">5000 ms</option>
								</select>
								Long-cache <select size="1" id="p_longCache" class="input-small">
									<option value="10">10 ms</option>
									<option value="50">50 ms</option>
									<option value="100">100 ms</option>
									<option value="500">500 ms</option>
								</select>
								Long-call <select size="1" id="p_longCall" class="input-small">
									<option value="50">50 ms</option>
									<option value="100">100 ms</option>
									<option value="500">500 ms</option>
									<option value="1000">1000 ms</option>
									<option value="3000">3000 ms</option>
									<option value="5000">5000 ms</option>
								</select>
								<input class="btn btn-primary btn-sm" value="查询" onclick="longTimeChange('${date}','${domain}','${ipAddress}')" type="submit">
							</div>
							<script type="text/javascript" src="${contextPath}/js/appendHostname.js"></script>
							<script type="text/javascript">
								$(document).ready(function() { appendHostname(${empty ipToHostnameStr ? "{}" : ipToHostnameStr}); });
							</script>
						</th></tr>
					</table>
					<table class="table table-hover table-striped table-condensed" style="width:100%">
						<tr>
							<th width="7%">Type</th>
							<th width="4%">Total</th>
							<th width="30%">Status</th>
							<th width="4%">Count</th>
							<th width="55%">SampleLinks</th>
						</tr>
						<c:forEach var="statistics" items="${allStatistics.status}" varStatus="typeStatus">
							<c:set var="typeStat" value="${statistics.value}" />
							<c:set var="statusSize" value="${fn:length(typeStat.status)}" />
							<tr>
								<td rowspan="${statusSize * 3}" class="top">
									&nbsp;<a href="#" class="${typeStat.type}">&nbsp;&nbsp;</a>
									&nbsp;&nbsp;<c:out value="${typeStat.type}" />
									<br>
									<a href="${contextPath}/mvc/r/p?op=hourlyGraph&domain=${domain}&date=${date}&ip=${ipAddress}&reportType=${reportType}&type=${typeStat.type}" class="history_graph_link" data-status="${typeStatus.index}">[:: show ::]</a>
								</td>
								<td rowspan="${statusSize * 3}" class="right top"><fmt:formatNumber value="${typeStat.count}" pattern="#,###,###,###,##0" />&nbsp;</td>
								<c:forEach var="statusEntry" items="${typeStat.status}" varStatus="statusIndex">
									<c:if test="${!statusIndex.first}"><tr></c:if>
									<c:set var="statusStat" value="${statusEntry.value}" />
									<td>
										<a href="${contextPath}/mvc/r/p?op=hourlyGraph&domain=${domain}&date=${date}&ip=${ipAddress}&reportType=${reportType}&type=${typeStat.type}&status=${statusStat.encodeStatus}" class="problem_status_graph_link" data-status="${typeStat.type}${statusStat.status}">[:: show ::]</a>
										&nbsp;<c:out value="${statusStat.status}" />
									</td>
									<td class="right"><fmt:formatNumber value="${statusStat.count}" pattern="#,###,###,###,##0" />&nbsp;</td>
									<td>
										<c:forEach var="link" items="${statusStat.links}" varStatus="linkIndex">
											<a href="${contextPath}/mvc/r/m/${link}?domain=${domain}">${linkIndex.first ? 'L' : (linkIndex.last ? 'g' : 'o')}</a>
										</c:forEach>
									</td>
									<c:if test="${!statusIndex.first}"></tr></c:if>
									<tr><td colspan="3" style="display:none"></td></tr>
									<tr><td colspan="3" style="display:none"><div id="${typeStat.type}${statusStat.status}" style="display:none"></div></td></tr>
								</c:forEach>
							</tr>
							<tr class="graphs"><td colspan="5" style="display:none"><div id="${typeStatus.index}" style="display:none"></div></td></tr>
							<tr></tr>
						</c:forEach>
					</table>
					<c:if test="${ipAddress ne 'All'}">
						<a href="${contextPath}/mvc/r/p?domain=${domain}&ip=${ipAddress}&date=${date}&op=group" onclick="return requestGroupInfo(this)">Threads Details</a>
						<div id="machineThreadGroupInfo"></div>
					</c:if>
				</div>
			</div>
		</div>
	</div>
	<script src="${contextPath}/js/problem.js"></script>
	<script src="${contextPath}/js/problemHistory.js"></script>
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
			return '<a href="${contextPath}/mvc/r/p?op=view&domain=' + domain + '&date=${date}">&nbsp;[&nbsp;' + domain + '&nbsp;]&nbsp;</a>';
		}
		function longTimeChange(date, domain, ip) {
			var longUrlTime = $("#p_longUrl").val();
			var longSqlTime = $("#p_longSql").val();
			var longServiceTime = $("#p_longService").val();
			var longCacheTime = $("#p_longCache").val();
			var longCallTime = $("#p_longCall").val();
			window.location.href = "${contextPath}/mvc/r/p?op=view&domain=" + domain + "&ip=" + ip + "&date=" + date
				+ "&urlThreshold=" + longUrlTime + "&sqlThreshold=" + longSqlTime + "&serviceThreshold=" + longServiceTime
				+ "&cacheThreshold=" + longCacheTime + "&callThreshold=" + longCallTime;
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
				window.location.href = '${contextPath}/mvc/r/p?op=view&domain=' + $("#search").val() + '&date=${date}';
			});
			$('#wrap_search').submit(function() {
				window.location.href = '${contextPath}/mvc/r/p?op=view&domain=' + $("#search").val() + '&date=${date}';
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
			$("#p_longUrl").val('${urlThreshold}');
			$("#p_longSql").val('${sqlThreshold}');
			$("#p_longService").val('${serviceThreshold}');
			$("#p_longCache").val('${cacheThreshold}');
			$("#p_longCall").val('${callThreshold}');
			$('.position').hide();
			$('#Problem_report').addClass("open active");
			$('#nav_application').click(function() {
				window.location.href = '${contextPath}/mvc/r/t?domain=${domain}&ip=${ipAddress}&date=${date}&reportType=${reportType}&op=view';
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
