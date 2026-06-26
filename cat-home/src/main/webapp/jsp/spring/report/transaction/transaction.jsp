<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="contextPath" value="${empty contextPath ? pageContext.request.contextPath : contextPath}" />
<c:set var="queryName" value="${empty queryName ? '' : queryName}" />
<c:set var="encodedType" value="${empty encodedType ? '' : encodedType}" />
<c:set var="encodedQueryName" value="${empty encodedQueryName ? '' : encodedQueryName}" />
<c:set var="reportType" value="${empty reportType ? 'day' : reportType}" />
<c:set var="historyMode" value="${historyMode == true}" />
<c:choose>
	<c:when test="${historyMode}">
		<c:set var="listAction" value="history" />
		<c:set var="groupAction" value="historyGroupReport" />
		<c:set var="graphAction" value="historyGraph" />
		<c:set var="graphLinkClass" value="history_graph_link" />
		<c:set var="listQueryPrefix" value="op=history&domain=${domain}&date=${date}&reportType=${reportType}${customDate}" />
		<c:set var="sortQueryPrefix" value="op=history&domain=${domain}&date=${date}&ip=${ipAddress}&reportType=${reportType}${customDate}" />
	</c:when>
	<c:otherwise>
		<c:set var="listAction" value="view" />
		<c:set var="groupAction" value="groupReport" />
		<c:set var="graphAction" value="graphs" />
		<c:set var="graphLinkClass" value="graph_link" />
		<c:set var="listQueryPrefix" value="domain=${domain}&date=${date}" />
		<c:set var="sortQueryPrefix" value="domain=${domain}&date=${date}&ip=${ipAddress}" />
	</c:otherwise>
</c:choose>
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
	<c:set var="navbarApplicationDisabled" value="true" scope="request" />
	<jsp:include page="../common/navbar.jsp" />
	<div class="main-container" id="main-container">
		<c:set var="activeReport" value="Transaction" scope="request" />
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
										<c:choose>
											<c:when test="${historyMode}">
												<span class="text-danger switch">【<a class="switch" href="${contextPath}/mvc/r/t?domain=${domain}&ip=${ipAddress}"><span class="text-danger">切到小时模式</span></a>】</span>
												<c:forEach var="nav" items="${historyNavs}">
													&nbsp;[ <a href="${contextPath}/mvc/r/t?op=history&domain=${domain}&ip=${ipAddress}&date=${date}&reportType=${nav.title}" class="${nav.title eq reportType ? 'current' : ''}">${nav.title}</a> ]
												</c:forEach>
												&nbsp;[ <a href="${contextPath}/mvc/r/t?op=history&domain=${domain}&ip=${ipAddress}&date=${date}&reportType=${reportType}&step=-1&type=${encodedType}&queryname=${encodedQueryName}">${currentNav.last}</a> ]
												&nbsp;[ <a href="${contextPath}/mvc/r/t?op=history&domain=${domain}&ip=${ipAddress}&date=${date}&reportType=${reportType}&step=1&type=${encodedType}&queryname=${encodedQueryName}">${currentNav.next}</a> ]
												&nbsp;[ <a href="${contextPath}/mvc/r/t?op=history&domain=${domain}&ip=${ipAddress}&reportType=${reportType}&type=${encodedType}&queryname=${encodedQueryName}">now</a> ]&nbsp;
											</c:when>
											<c:otherwise>
												<span class="text-danger switch">【<a class="switch" href="${contextPath}/mvc/r/t?op=history&domain=${domain}&ip=${ipAddress}"><span class="text-danger">切到历史模式</span></a>】</span>
												<c:forEach var="nav" items="${navs}">
													&nbsp;[ <a href="${contextPath}/mvc/r/t?date=${date}&ip=${ipAddress}&step=${nav.hours}&${navPrefix}">${nav.title}</a> ]
												</c:forEach>
												&nbsp;[ <a href="${contextPath}/mvc/r/t?${navPrefix}">now</a> ]&nbsp;
											</c:otherwise>
										</c:choose>
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
												&nbsp;<a class="domainItem" href="${contextPath}/mvc/r/t?op=${listAction}&domain=${itemDomain}&date=${date}&reportType=${reportType}">[&nbsp;<c:out value="${itemDomain}" />&nbsp;]</a>&nbsp;
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
					<c:if test="${sample != null && sample != 1.0}">
						<div class="ace-settings-container" id="ace-settings-container">
							<div class="btn btn-app btn-xs btn-warning ace-settings-btn" id="ace-settings-btn">采样</div>
							<div class="ace-settings-box clearfix" id="ace-settings-box">
								<div class="pull-left width-50">
									<div class="ace-settings-item"><label class="lbl">采样比例<strong><span class="text-danger"><fmt:formatNumber value="${sample * 100}" pattern="#0.00" />%</span></strong></label></div>
									<div class="ace-settings-item"><label class="lbl">采样直接影响的是Transaction、Event的总量和QPS</label></div>
									<div class="ace-settings-item"><label class="lbl">采样不影响Metric、Heartbeat、Exception等数据</label></div>
								</div>
							</div>
						</div>
					</c:if>
					<table class="machines">
						<tr class="left">
							<th>&nbsp;[&nbsp;
								<a href="${contextPath}/mvc/r/t?${listQueryPrefix}&type=${encodedType}&queryname=${encodedQueryName}" class="${ipAddress eq 'All' ? 'current' : ''}">All</a>
								&nbsp;]&nbsp;
								<c:forEach var="ip" items="${ips}">
									&nbsp;[&nbsp;<a href="${contextPath}/mvc/r/t?${listQueryPrefix}&ip=${ip}&type=${encodedType}&queryname=${encodedQueryName}" class="${ip eq ipAddress ? 'current' : ''}"><c:out value="${ip}" /></a>&nbsp;]&nbsp;
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
									&nbsp;[&nbsp;<a href="${contextPath}/mvc/r/t?op=${groupAction}&domain=${domain}&date=${date}&group=${itemGroup}&reportType=${reportType}&type=${encodedType}"><c:out value="${itemGroup}" /></a>&nbsp;]&nbsp;
								</c:forEach>
							</th>
						</tr>
					</table>
					<table class="table table-striped table-condensed table-hover" style="width:100%;">
						<c:choose>
							<c:when test="${empty type}">
								<tr>
									<th class="left"><a href="${contextPath}/mvc/r/t?${sortQueryPrefix}&sort=type">Type</a></th>
									<th class="right"><a href="${contextPath}/mvc/r/t?${sortQueryPrefix}&sort=total">Total</a></th>
									<th class="right"><a href="${contextPath}/mvc/r/t?${sortQueryPrefix}&sort=failure">Failure</a></th>
									<th class="right"><a href="${contextPath}/mvc/r/t?${sortQueryPrefix}&sort=failurePercent">Failure%</a></th>
									<th class="right">Sample Link</th>
									<th class="right"><a href="${contextPath}/mvc/r/t?${sortQueryPrefix}&sort=min">Min</a>(ms)</th>
									<th class="right"><a href="${contextPath}/mvc/r/t?${sortQueryPrefix}&sort=max">Max</a>(ms)</th>
									<th class="right"><a href="${contextPath}/mvc/r/t?${sortQueryPrefix}&sort=avg">Avg</a>(ms)</th>
									<th class="right"><a href="${contextPath}/mvc/r/t?${sortQueryPrefix}&sort=95line">95Line</a>(ms)</th>
									<th class="right"><a href="${contextPath}/mvc/r/t?${sortQueryPrefix}&sort=99line">99.9Line</a>(ms)</th>
									<th class="right"><a href="${contextPath}/mvc/r/t?${sortQueryPrefix}&sort=std">Std</a>(ms)</th>
									<th class="right"><a href="${contextPath}/mvc/r/t?${sortQueryPrefix}&sort=total">QPS</a></th>
								</tr>
								<c:forEach var="item" items="${displayTypeReport.results}" varStatus="status">
									<c:set var="e" value="${item.detail}" />
									<tr class="right">
										<td class="left"><a href="${contextPath}/mvc/r/t?op=${graphAction}&domain=${report.domain}&date=${date}&ip=${ipAddress}&reportType=${reportType}&type=${item.type}${customDate}" class="${graphLinkClass}" data-status="${status.index}">[:: show ::]</a>
										&nbsp;&nbsp;<a href="${contextPath}/mvc/r/t?${listQueryPrefix}&ip=${ipAddress}&type=${item.type}"> <c:out value="${e.id}" /></a></td>
										<td><fmt:formatNumber value="${e.totalCount}" pattern="#,###,###,###,##0" /></td>
										<td><fmt:formatNumber value="${e.failCount}" pattern="#,###,###,###,##0" /></td>
										<td>&nbsp;<fmt:formatNumber value="${e.failPercent / 100}" pattern="0.0000%" /></td>
										<td><a href="${contextPath}/mvc/r/m/${empty e.failMessageUrl ? e.successMessageUrl : e.failMessageUrl}?domain=${domain}">Log View</a></td>
										<td><fmt:formatNumber value="${e.min}" pattern="###,##0.#" /></td>
										<td><fmt:formatNumber value="${e.max}" pattern="###,##0.#" /></td>
										<td><fmt:formatNumber value="${e.avg}" pattern="###,##0.0" /></td>
										<td><fmt:formatNumber value="${e.line95Value}" pattern="###,##0.0" /></td>
										<td><fmt:formatNumber value="${e.line99Value}" pattern="###,##0.0" /></td>
										<td><fmt:formatNumber value="${e.std}" pattern="###,##0.0" /></td>
										<td><fmt:formatNumber value="${e.tps}" pattern="###,##0.0" /></td>
									</tr>
									<tr class="graphs"><td colspan="13" style="display:none"><div id="${status.index}" style="display:none"></div></td></tr>
									<tr style="display:none"></tr>
								</c:forEach>
							</c:when>
							<c:otherwise>
								<tr><th class="left" colspan="13"><input type="text" name="queryname" id="queryname" size="40" value="${fn:escapeXml(queryName)}">
									<input class="btn btn-primary btn-sm" value="Filter" onclick="filterByName('${date}','${domain}','${ipAddress}','${encodedType}','${reportType}','${historyMode}','${customDate}')" type="submit">
									支持多个字符串查询，例如sql|url|task，查询结果为包含任一sql、url、task的列。
								</th></tr>
								<tr>
									<th style="text-align:left;"><a href="${contextPath}/mvc/r/t?op=${graphAction}&domain=${report.domain}&date=${date}&ip=${ipAddress}&reportType=${reportType}&type=${encodedType}${customDate}" class="${graphLinkClass}" data-status="-1">[:: show ::]</a>
									<a href="${contextPath}/mvc/r/t?${sortQueryPrefix}&type=${encodedType}&sort=type&queryname=${encodedQueryName}">Name</a></th>
									<th class="right"><a href="${contextPath}/mvc/r/t?${sortQueryPrefix}&type=${encodedType}&sort=total&queryname=${encodedQueryName}">Total</a></th>
									<th class="right"><a href="${contextPath}/mvc/r/t?${sortQueryPrefix}&type=${encodedType}&sort=failure&queryname=${encodedQueryName}">Failure</a></th>
									<th class="right"><a href="${contextPath}/mvc/r/t?${sortQueryPrefix}&type=${encodedType}&sort=failurePercent&queryname=${encodedQueryName}">Failure%</a></th>
									<th class="right">Sample Link</th>
									<th class="right"><a href="${contextPath}/mvc/r/t?${sortQueryPrefix}&type=${encodedType}&sort=min&queryname=${encodedQueryName}">Min</a>(ms)</th>
									<th class="right"><a href="${contextPath}/mvc/r/t?${sortQueryPrefix}&type=${encodedType}&sort=max&queryname=${encodedQueryName}">Max</a>(ms)</th>
									<th class="right"><a href="${contextPath}/mvc/r/t?${sortQueryPrefix}&type=${encodedType}&sort=avg&queryname=${encodedQueryName}">Avg</a>(ms)</th>
									<th class="right"><a href="${contextPath}/mvc/r/t?${sortQueryPrefix}&type=${encodedType}&sort=95line&queryname=${encodedQueryName}">95Line</a>(ms)</th>
									<th class="right"><a href="${contextPath}/mvc/r/t?${sortQueryPrefix}&type=${encodedType}&sort=99line&queryname=${encodedQueryName}">99.9Line</a>(ms)</th>
									<th class="right"><a href="${contextPath}/mvc/r/t?${sortQueryPrefix}&type=${encodedType}&sort=std&queryname=${encodedQueryName}">Std</a>(ms)</th>
									<th class="right"><a href="${contextPath}/mvc/r/t?${sortQueryPrefix}&type=${encodedType}&sort=total&queryname=${encodedQueryName}">QPS</a></th>
									<th class="right"><a href="${contextPath}/mvc/r/t?${sortQueryPrefix}&type=${encodedType}&sort=total&queryname=${encodedQueryName}">Percent%</a></th>
								</tr>
								<tr class="graphs"><td colspan="13" style="display:none"><div id="-1" style="display:none"></div></td></tr>
								<c:forEach var="item" items="${displayNameReport.results}" varStatus="status">
									<c:set var="e" value="${item.detail}" />
									<tr class="right">
										<c:choose>
											<c:when test="${status.index > 0}">
												<td class="left longText" style="white-space:normal">
													<a href="${contextPath}/mvc/r/t?op=${graphAction}&domain=${report.domain}&date=${date}&ip=${ipAddress}&reportType=${reportType}&type=${encodedType}&name=${item.name}${customDate}" class="${graphLinkClass}" data-status="${status.index}">[:: show ::]</a>
													&nbsp;&nbsp;<c:out value="${fn:substring(e.id, 0, 120)}" />
												</td>
											</c:when>
											<c:otherwise>
												<td class="center" style="white-space:normal"><c:out value="${fn:substring(e.id, 0, 120)}" /></td>
											</c:otherwise>
										</c:choose>
										<td><fmt:formatNumber value="${e.totalCount}" pattern="#,###,###,###,##0" /></td>
										<td><fmt:formatNumber value="${e.failCount}" pattern="#,###,###,###,##0" /></td>
										<td>&nbsp;<fmt:formatNumber value="${e.failPercent / 100}" pattern="0.0000%" /></td>
										<td class="center"><a href="${contextPath}/mvc/r/m/${empty e.failMessageUrl ? e.successMessageUrl : e.failMessageUrl}?domain=${domain}">Log View</a></td>
										<td><fmt:formatNumber value="${e.min}" pattern="###,##0.#" /></td>
										<td><fmt:formatNumber value="${e.max}" pattern="###,##0.#" /></td>
										<td><fmt:formatNumber value="${e.avg}" pattern="###,##0.0" /></td>
										<c:choose>
											<c:when test="${status.index > 0}">
												<td><fmt:formatNumber value="${e.line95Value}" pattern="###,##0.0" /></td>
												<td><fmt:formatNumber value="${e.line99Value}" pattern="###,##0.0" /></td>
											</c:when>
											<c:otherwise>
												<td class="center">-</td>
												<td class="center">-</td>
											</c:otherwise>
										</c:choose>
										<td><fmt:formatNumber value="${e.std}" pattern="###,##0.0" /></td>
										<td><fmt:formatNumber value="${e.tps}" pattern="###,##0.0" /></td>
										<td><fmt:formatNumber value="${e.totalPercent}" pattern="0.00%" /></td>
									</tr>
									<tr class=""><td colspan="13" style="display:none"><div id="${status.index}" style="display:none"></div></td></tr>
									<tr></tr>
								</c:forEach>
							</c:otherwise>
						</c:choose>
					</table>
					<font color="white"></font>
					<c:if test="${not empty type}">
						<table><tr><td><div id="transactionGraph" class="pieChart"></div></td></tr></table>
						<script type="text/javascript">
							var data = ${empty pieChart ? "{}" : pieChart};
							graphPieChart(document.getElementById('transactionGraph'), data);
						</script>
					</c:if>
				</div>
			</div>
		</div>
	</div>
	<script src="${contextPath}/js/transaction.js"></script>
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
			return '<a href="${contextPath}/mvc/r/t?op=${listAction}&domain=' + domain + '&date=${date}&reportType=${reportType}">&nbsp;[&nbsp;' + domain + '&nbsp;]&nbsp;</a>';
		}
		function filterByName(date, domain, ip, type, reportType, historyMode, customDate) {
			var queryname = $("#queryname").val();
			var op = historyMode == 'true' ? 'history' : 'view';
			var url = '${contextPath}/mvc/r/t?op=' + op + '&domain=' + domain + '&type=' + type + '&date='
					+ date + '&queryname=' + encodeURIComponent(queryname) + '&ip=' + ip;

			if (historyMode == 'true') {
				url += '&reportType=' + reportType + customDate;
			}
			window.location.href = url;
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
				window.location.href = '${contextPath}/mvc/r/t?op=${listAction}&domain=' + $("#search").val() + '&date=${date}&reportType=${reportType}';
			});
			$('#wrap_search').submit(function() {
				window.location.href = '${contextPath}/mvc/r/t?op=${listAction}&domain=' + $("#search").val() + '&date=${date}&reportType=${reportType}';
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
			$('#Transaction_report').addClass("open active");
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
