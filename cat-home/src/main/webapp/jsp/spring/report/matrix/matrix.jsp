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
<c:set var="sortQueryPrefix" value="date=${date}&domain=${domain}&ip=${ipAddress}&reportType=${reportType}" />
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
	<script src="${contextPath}/assets/js/jquery-ui.min.js"></script>
	<script src="${contextPath}/assets/js/jquery.ui.touch-punch.min.js"></script>
	<script src="${contextPath}/assets/js/ace-elements.min.js"></script>
	<script src="${contextPath}/assets/js/ace.min.js"></script>
	<style>
		.left { text-align: left; }
		.right { text-align: right; }
		.center { text-align: center; }
		.current { color: red; font-weight: bold; }
	</style>
</head>
<body class="no-skin">
	<c:set var="navbarApplicationDisabled" value="true" scope="request" />
	<jsp:include page="../common/navbar.jsp" />
	<div class="main-container" id="main-container">
		<c:set var="activeReport" value="Matrix" scope="request" />
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
													<input id="search" type="text" value="${fn:escapeXml(domain)}" class="search-input search-input form-control ui-autocomplete-input" placeholder="input domain for search" autocomplete="off">
													<i class="ace-icon fa fa-search nav-search-icon"></i>
												</span>
												<span class="input-group-btn"><button class="btn btn-sm btn-pink" type="button" id="search_go">Go</button></span>
											</div>
										</form>
									</div>
								</td>
								<td>
									<div class="nav-search nav" id="nav-search">
										<span class="text-danger switch">【<a class="switch" href="${contextPath}/mvc/r/matrix?op=history&domain=${domain}&ip=${ipAddress}"><span class="text-danger">切到历史模式</span></a>】</span>
										<c:forEach var="nav" items="${navs}">
											&nbsp;[ <a href="${contextPath}/mvc/r/matrix?date=${date}&ip=${ipAddress}&step=${nav.hours}&domain=${domain}">${nav.title}</a> ]
										</c:forEach>
										&nbsp;[ <a href="${contextPath}/mvc/r/matrix?domain=${domain}&ip=${ipAddress}">now</a> ]&nbsp;
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
												&nbsp;<a class="domainItem" href="${contextPath}/mvc/r/matrix?op=view&domain=${itemDomain}&date=${date}&reportType=${reportType}">[&nbsp;<c:out value="${itemDomain}" />&nbsp;]</a>&nbsp;
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
					<div class="row-fluid">
						<table class="table table-hover table-bordered table-striped table-condensed">
							<tr>
								<th class="left" rowspan="2">Type</th>
								<th class="left" width="10%" rowspan="2"><a href="${contextPath}/mvc/r/matrix?${sortQueryPrefix}&sort=Name">Name</a></th>
								<th rowspan="2" title="所有请求中总次数"><a href="${contextPath}/mvc/r/matrix?${sortQueryPrefix}&sort=Count">Total<br>Hits</a></th>
								<th rowspan="2" title="所有请求中平均响应时间"><a href="${contextPath}/mvc/r/matrix?${sortQueryPrefix}&sort=Time">Avg<br>Duration(ms)</a></th>
								<th rowspan="2">Log</th>
								<th colspan="3" title="一次请求中远程调用次数统计">Call Ratio</th>
								<th colspan="3" title="一次请求中远程调用时间统计">Call Cost</th>
								<th colspan="3" title="一次请求中数据库调用次数统计">SQL Ratio</th>
								<th colspan="3" title="一次请求中数据库调用时间统计">SQL Cost</th>
								<th colspan="3" title="一次请求中缓存调用次数统计">Cache Ratio</th>
								<th colspan="3" title="一次请求中缓存调用时间统计">Cache Cost</th>
							</tr>
							<tr>
								<td><a href="${contextPath}/mvc/r/matrix?${sortQueryPrefix}&sort=callMinCount">Min</a></td>
								<td><a href="${contextPath}/mvc/r/matrix?${sortQueryPrefix}&sort=callMaxCount">Max</a></td>
								<td><a href="${contextPath}/mvc/r/matrix?${sortQueryPrefix}&sort=callAvgCount">Avg</a></td>
								<td><a href="${contextPath}/mvc/r/matrix?${sortQueryPrefix}&sort=callAvgTotalTime">Time(ms)</a></td>
								<td><a href="${contextPath}/mvc/r/matrix?${sortQueryPrefix}&sort=callTimePercent">Time%</a></td>
								<td>Log</td>
								<td><a href="${contextPath}/mvc/r/matrix?${sortQueryPrefix}&sort=sqlMinCount">Min</a></td>
								<td><a href="${contextPath}/mvc/r/matrix?${sortQueryPrefix}&sort=sqlMaxCount">Max</a></td>
								<td><a href="${contextPath}/mvc/r/matrix?${sortQueryPrefix}&sort=sqlAvgCount">Avg</a></td>
								<td><a href="${contextPath}/mvc/r/matrix?${sortQueryPrefix}&sort=sqlAvgTotalTime">Time(ms)</a></td>
								<td><a href="${contextPath}/mvc/r/matrix?${sortQueryPrefix}&sort=sqlTimePercent">Time%</a></td>
								<td>Log</td>
								<td><a href="${contextPath}/mvc/r/matrix?${sortQueryPrefix}&sort=cacheMinCount">Min</a></td>
								<td><a href="${contextPath}/mvc/r/matrix?${sortQueryPrefix}&sort=cacheMaxCount">Max</a></td>
								<td><a href="${contextPath}/mvc/r/matrix?${sortQueryPrefix}&sort=cacheAvgCount">Avg</a></td>
								<td><a href="${contextPath}/mvc/r/matrix?${sortQueryPrefix}&sort=cacheAvgTotalTime">Time(ms)</a></td>
								<td><a href="${contextPath}/mvc/r/matrix?${sortQueryPrefix}&sort=cacheTimePercent">Time%</a></td>
								<td>Log</td>
							</tr>
							<c:forEach var="item" items="${matrix.matrixs}">
								<tr class="right">
									<td class="left"><c:out value="${item.type}" /></td>
									<td class="left longText" style="white-space:normal">
										<c:choose>
											<c:when test="${fn:length(item.name) > 120}"><c:out value="${fn:substring(item.name, 0, 120)}" />...</c:when>
											<c:otherwise><c:out value="${item.name}" /></c:otherwise>
										</c:choose>
									</td>
									<td><fmt:formatNumber value="${item.count}" pattern="#,###,##0" /></td>
									<td><fmt:formatNumber value="${item.avg}" pattern="0.0" /></td>
									<td class="center"><a href="${contextPath}/mvc/r/m/${item.url}?domain=${domain}">L</a></td>
									<td>${item.callMin}</td>
									<td>${item.callMax}</td>
									<td><fmt:formatNumber value="${item.callAvg}" pattern="0.0" /></td>
									<td>${item.callTime}</td>
									<td><fmt:formatNumber value="${item.callTimePercent}" pattern="00.0%" /></td>
									<td><a href="${contextPath}/mvc/r/m/${item.callUrl}?domain=${domain}">L</a></td>
									<td>${item.sqlMin}</td>
									<td>${item.sqlMax}</td>
									<td><fmt:formatNumber value="${item.sqlAvg}" pattern="0.0" /></td>
									<td>${item.sqlTime}</td>
									<td><fmt:formatNumber value="${item.sqlTimePercent}" pattern="00.0%" /></td>
									<td><a href="${contextPath}/mvc/r/m/${item.sqlUrl}?domain=${domain}">L</a></td>
									<td>${item.cacheMin}</td>
									<td>${item.cacheMax}</td>
									<td><fmt:formatNumber value="${item.cacheAvg}" pattern="0.0" /></td>
									<td>${item.cacheTime}</td>
									<td><fmt:formatNumber value="${item.cacheTimePercent}" pattern="00.0%" /></td>
									<td><a href="${contextPath}/mvc/r/m/${item.cacheUrl}?domain=${domain}">L</a></td>
								</tr>
							</c:forEach>
						</table>
					</div>
				</div>
			</div>
		</div>
	</div>
	<script type="text/javascript">
		function getcookie(name) {
			var arr = document.cookie.match(new RegExp("(^| )" + name + "=([^;]*)(;|$)"));

			if (arr != null) {
				return unescape(arr[2]);
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
			return '<a href="${contextPath}/mvc/r/matrix?op=view&domain=' + domain + '&date=${date}&reportType=${reportType}">&nbsp;[&nbsp;' + domain + '&nbsp;]&nbsp;</a>';
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
				window.location.href = '${contextPath}/mvc/r/matrix?op=view&domain=' + $("#search").val() + '&date=${date}&reportType=${reportType}';
			});
			$('#wrap_search').submit(function() {
				window.location.href = '${contextPath}/mvc/r/matrix?op=view&domain=' + $("#search").val() + '&date=${date}&reportType=${reportType}';
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
		});
	</script>
</body>
</html>
