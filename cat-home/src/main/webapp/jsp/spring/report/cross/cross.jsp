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
	<script src="${contextPath}/js/jquery.datetimepicker.js"></script>
	<script src="${contextPath}/assets/js/jquery-ui.min.js"></script>
	<script src="${contextPath}/assets/js/jquery.ui.touch-punch.min.js"></script>
	<script src="${contextPath}/assets/js/ace-elements.min.js"></script>
	<script src="${contextPath}/assets/js/ace.min.js"></script>
	<style>
		.left { text-align: left; }
		.right { text-align: right; }
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
					<small style="font-size:65%">(Central Application Tracking)</small>
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
		<c:set var="activeReport" value="Cross" scope="request" />
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
										<span class="text-danger switch">【<a class="switch" href="${contextPath}/mvc/r/cross?op=history&domain=${domain}&ip=${ipAddress}"><span class="text-danger">切到历史模式</span></a>】</span>
										<c:forEach var="nav" items="${navs}">
											&nbsp;[ <a href="${contextPath}/mvc/r/cross?date=${date}&step=${nav.hours}&${navPrefix}">${nav.title}</a> ]
										</c:forEach>
										&nbsp;[ <a href="${contextPath}/mvc/r/cross?${navPrefix}">now</a> ]&nbsp;
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
												&nbsp;<a class="domainItem" href="${contextPath}/mvc/r/cross?op=view&domain=${itemDomain}&date=${date}&reportType=${reportType}">[&nbsp;<c:out value="${itemDomain}" />&nbsp;]</a>&nbsp;
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
					<table>
						<tr>
							<th>
								<span class="text-danger" style="padding-left:5px;">查询当前这个时段内，一个方法被哪些应用调用</span>
								<input type="text" class="input-xxlarge" id="method" size="100" value="${fn:escapeXml(method)}">
								<input type="submit" class="btn btn-primary btn-sm" onclick="queryCrossMethod()">
							</th>
						</tr>
					</table>
					<table class="machines">
						<tr class="left">
							<th>&nbsp;[&nbsp;
								<a href="${contextPath}/mvc/r/cross?domain=${domain}&date=${date}" class="${ipAddress eq 'All' ? 'current' : ''}">All</a>
								&nbsp;]&nbsp;
								<c:forEach var="ip" items="${ips}">
									&nbsp;[&nbsp;<a href="${contextPath}/mvc/r/cross?domain=${domain}&ip=${ip}&date=${date}" class="${ipAddress eq ip ? 'current' : ''}"><c:out value="${ip}" /></a>&nbsp;]&nbsp;
								</c:forEach>
							</th>
						</tr>
					</table>
					<script type="text/javascript" src="${contextPath}/js/appendHostname.js"></script>
					<script type="text/javascript">
						$(document).ready(function() { appendHostname(${empty ipToHostnameStr ? "{}" : ipToHostnameStr}); });
					</script>
					<table class="table table-striped table-condensed">
						<c:if test="${not empty projectInfo.callProjectsInfo}">
							<tr><td colspan="7" style="text-align:center"><strong>调用其他 Pigeon 服务</strong></td></tr>
							<tr>
								<th class="left">Type</th>
								<th class="left"><a href="${contextPath}/mvc/r/cross?domain=${domain}&date=${date}&ip=${ipAddress}&serviceSort=${serviceSort}&callSort=name">RemoteProject</a></th>
								<th class="right"><a href="${contextPath}/mvc/r/cross?domain=${domain}&date=${date}&ip=${ipAddress}&serviceSort=${serviceSort}&callSort=total">Total</a></th>
								<th class="right"><a href="${contextPath}/mvc/r/cross?domain=${domain}&date=${date}&ip=${ipAddress}&serviceSort=${serviceSort}&callSort=failure">Failure</a></th>
								<th class="right"><a href="${contextPath}/mvc/r/cross?domain=${domain}&date=${date}&ip=${ipAddress}&serviceSort=${serviceSort}&callSort=failurePercent">Failure%</a></th>
								<th class="right"><a href="${contextPath}/mvc/r/cross?domain=${domain}&date=${date}&ip=${ipAddress}&serviceSort=${serviceSort}&callSort=avg">Avg(ms)</a></th>
								<th class="right">QPS</th>
							</tr>
							<c:forEach var="callInfo" items="${projectInfo.callProjectsInfo}">
								<tr class="right">
									<td class="left"><c:out value="${callInfo.type}" /></td>
									<td class="left"><a href="${contextPath}/mvc/r/cross?op=host&domain=${domain}&date=${date}&ip=${ipAddress}&project=${callInfo.projectName}"><c:out value="${callInfo.projectName}" /></a></td>
									<td><fmt:formatNumber value="${callInfo.totalCount}" pattern="#,###,###,###,##0" /></td>
									<td><fmt:formatNumber value="${callInfo.failureCount}" pattern="#,###,###,###,##0" /></td>
									<td><fmt:formatNumber value="${callInfo.failurePercent}" pattern="0.0000%" /></td>
									<td><fmt:formatNumber value="${callInfo.avg}" pattern="0.00" /></td>
									<td><fmt:formatNumber value="${callInfo.tps}" pattern="0.00" /></td>
								</tr>
							</c:forEach>
						</c:if>
						<c:if test="${not empty projectInfo.serviceProjectsInfo}">
							<tr>
								<td colspan="7" style="text-align:center"><strong>提供 Pigeon 服务 [ 服务端数据 ]</strong></td>
								<c:if test="${not empty projectInfo.callerProjectsInfo}">
									<td></td>
									<td colspan="7" style="text-align:center"><strong>提供 Pigeon 服务 [ 客户端数据 ]</strong></td>
								</c:if>
							</tr>
							<tr>
								<th class="left">Type</th>
								<th class="left"><a href="${contextPath}/mvc/r/cross?domain=${domain}&date=${date}&ip=${ipAddress}&callSort=${callSort}&serviceSort=name">RemoteProject</a></th>
								<th class="right"><a href="${contextPath}/mvc/r/cross?domain=${domain}&date=${date}&ip=${ipAddress}&callSort=${callSort}&serviceSort=total">Total</a></th>
								<th class="right"><a href="${contextPath}/mvc/r/cross?domain=${domain}&date=${date}&ip=${ipAddress}&callSort=${callSort}&serviceSort=failure">Failure</a></th>
								<th class="right"><a href="${contextPath}/mvc/r/cross?domain=${domain}&date=${date}&ip=${ipAddress}&callSort=${callSort}&serviceSort=failurePercent">Failure%</a></th>
								<th class="right"><a href="${contextPath}/mvc/r/cross?domain=${domain}&date=${date}&ip=${ipAddress}&callSort=${callSort}&serviceSort=avg">Avg(ms)</a></th>
								<th class="right">QPS</th>
								<c:if test="${not empty projectInfo.callerProjectsInfo}">
									<th></th>
									<th class="left">Type</th>
									<th class="left"><a href="${contextPath}/mvc/r/cross?domain=${domain}&date=${date}&ip=${ipAddress}&callSort=${callSort}&serviceSort=name">RemoteProject</a></th>
									<th class="right"><a href="${contextPath}/mvc/r/cross?domain=${domain}&date=${date}&ip=${ipAddress}&callSort=${callSort}&serviceSort=total">Total</a></th>
									<th class="right"><a href="${contextPath}/mvc/r/cross?domain=${domain}&date=${date}&ip=${ipAddress}&callSort=${callSort}&serviceSort=failure">Failure</a></th>
									<th class="right"><a href="${contextPath}/mvc/r/cross?domain=${domain}&date=${date}&ip=${ipAddress}&callSort=${callSort}&serviceSort=failurePercent">Failure%</a></th>
									<th class="right"><a href="${contextPath}/mvc/r/cross?domain=${domain}&date=${date}&ip=${ipAddress}&callSort=${callSort}&serviceSort=avg">Avg(ms)</a></th>
									<th class="right">QPS</th>
								</c:if>
							</tr>
							<c:forEach var="serviceInfo" items="${projectInfo.serviceProjectsInfo}">
								<c:set var="projectName" value="${serviceInfo.projectName}" />
								<c:set var="callerInfo" value="${projectInfo.callerProjectsInfo}" />
								<tr class="right">
									<td class="left"><c:out value="${serviceInfo.type}" /></td>
									<td class="left"><a href="${contextPath}/mvc/r/cross?op=host&domain=${domain}&date=${date}&ip=${ipAddress}&project=${serviceInfo.projectName}"><c:out value="${serviceInfo.projectName}" /></a></td>
									<td><fmt:formatNumber value="${serviceInfo.totalCount}" pattern="#,###,###,###,##0" /></td>
									<td><fmt:formatNumber value="${serviceInfo.failureCount}" pattern="#,###,###,###,##0" /></td>
									<td><fmt:formatNumber value="${serviceInfo.failurePercent}" pattern="0.0000%" /></td>
									<td><fmt:formatNumber value="${serviceInfo.avg}" pattern="0.00" /></td>
									<td><fmt:formatNumber value="${serviceInfo.tps}" pattern="0.00" /></td>
									<c:if test="${not empty callerInfo}">
										<td></td>
										<td class="left"><c:out value="${callerInfo[projectName].type}" /></td>
										<td class="left"><a href="${contextPath}/mvc/r/cross?op=host&domain=${domain}&date=${date}&ip=${ipAddress}&project=${callerInfo[projectName].projectName}"><c:out value="${callerInfo[projectName].projectName}" /></a></td>
										<td><fmt:formatNumber value="${callerInfo[projectName].totalCount}" pattern="#,###,###,###,##0" /></td>
										<td><fmt:formatNumber value="${callerInfo[projectName].failureCount}" pattern="#,###,###,###,##0" /></td>
										<td><fmt:formatNumber value="${callerInfo[projectName].failurePercent}" pattern="0.0000%" /></td>
										<td><fmt:formatNumber value="${callerInfo[projectName].avg}" pattern="0.00" /></td>
										<td><fmt:formatNumber value="${callerInfo[projectName].tps}" pattern="0.00" /></td>
									</c:if>
								</tr>
							</c:forEach>
						</c:if>
					</table>
				</div>
			</div>
		</div>
	</div>
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
			return '<a href="${contextPath}/mvc/r/cross?op=view&domain=' + domain + '&date=${date}">&nbsp;[&nbsp;' + domain + '&nbsp;]&nbsp;</a>';
		}
		function queryCrossMethod() {
			var method = $('#method').val();
			window.location.href = '${contextPath}/mvc/r/cross?op=query&domain=${domain}&date=${date}&reportType=${reportType}&method=' + encodeURIComponent(method);
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
				window.location.href = '${contextPath}/mvc/r/cross?op=view&domain=' + $("#search").val() + '&date=${date}';
			});
			$('#wrap_search').submit(function() {
				window.location.href = '${contextPath}/mvc/r/cross?op=view&domain=' + $("#search").val() + '&date=${date}';
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
			$('#Cross_report').addClass("open active");
			$('#nav_application').click(function() {
				window.location.href = '${contextPath}/mvc/r/cross?domain=${domain}&ip=${ipAddress}&date=${date}&reportType=${reportType}&op=view';
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
