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
<c:set var="action" value="${empty action ? 'view' : action}" />
<c:set var="historyMode" value="${historyMode == true}" />
<c:set var="project" value="${empty project ? 'All' : project}" />
<c:set var="remoteIp" value="${empty remoteIp ? '' : remoteIp}" />
<c:set var="queryName" value="${empty queryName ? '' : queryName}" />
<c:choose>
	<c:when test="${historyMode}">
		<c:set var="projectAction" value="history" />
		<c:set var="hostAction" value="historyHost" />
		<c:set var="methodAction" value="historyMethod" />
		<c:set var="switchText" value="切到小时模式" />
		<c:set var="switchHref" value="${contextPath}/mvc/r/cross?domain=${domain}&ip=${ipAddress}" />
	</c:when>
	<c:otherwise>
		<c:set var="projectAction" value="view" />
		<c:set var="hostAction" value="host" />
		<c:set var="methodAction" value="method" />
		<c:set var="switchText" value="切到历史模式" />
		<c:set var="switchHref" value="${contextPath}/mvc/r/cross?op=history&domain=${domain}&ip=${ipAddress}" />
	</c:otherwise>
</c:choose>
<c:set var="projectView" value="${action eq 'view' || action eq 'history' || action eq 'query'}" />
<c:set var="hostView" value="${action eq 'host' || action eq 'historyHost'}" />
<c:set var="methodView" value="${action eq 'method' || action eq 'historyMethod'}" />
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
	<c:set var="navbarApplicationDisabled" value="true" scope="request" />
	<jsp:include page="../common/navbar.jsp" />
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
										<span class="text-danger switch">[ <a class="switch" href="${switchHref}"><span class="text-danger">${switchText}</span></a> ]</span>
										<c:choose>
											<c:when test="${historyMode}">
												<c:forEach var="nav" items="${historyNavs}">
													&nbsp;[ <a href="${contextPath}/mvc/r/cross?op=history&domain=${domain}&ip=${ipAddress}&date=${date}&reportType=${nav.title}" class="${nav.title eq reportType ? 'current' : ''}">${nav.title}</a> ]
												</c:forEach>
												&nbsp;[ <a href="${contextPath}/mvc/r/cross?op=history&domain=${domain}&ip=${ipAddress}&date=${date}&reportType=${reportType}&step=-1">${currentNav.last}</a> ]
												&nbsp;[ <a href="${contextPath}/mvc/r/cross?op=history&domain=${domain}&ip=${ipAddress}&date=${date}&reportType=${reportType}&step=1">${currentNav.next}</a> ]
												&nbsp;[ <a href="${contextPath}/mvc/r/cross?op=history&domain=${domain}&ip=${ipAddress}&reportType=${reportType}">now</a> ]&nbsp;
											</c:when>
											<c:otherwise>
												<c:forEach var="nav" items="${navs}">
													&nbsp;[ <a href="${contextPath}/mvc/r/cross?date=${date}&step=${nav.hours}&${navPrefix}">${nav.title}</a> ]
												</c:forEach>
												&nbsp;[ <a href="${contextPath}/mvc/r/cross?${navPrefix}">now</a> ]&nbsp;
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
												&nbsp;<a class="domainItem" href="${contextPath}/mvc/r/cross?op=${projectAction}&domain=${itemDomain}&date=${date}&reportType=${reportType}">[&nbsp;<c:out value="${itemDomain}" />&nbsp;]</a>&nbsp;
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
								<span class="text-danger" style="padding-left:5px;">查询当前这个时间段内，一个方法被哪些应用调用</span>
								<input type="text" class="input-xxlarge" id="method" size="100" value="${fn:escapeXml(method)}">
								<input type="submit" class="btn btn-primary btn-sm" onclick="queryCrossMethod()">
							</th>
						</tr>
					</table>
					<table class="machines">
						<tr class="left">
							<th>&nbsp;[&nbsp;
								<a href="${contextPath}/mvc/r/cross?op=${action}&domain=${domain}&date=${date}&reportType=${reportType}&project=${project}&remote=${remoteIp}&queryName=${queryName}${customDate}" class="${ipAddress eq 'All' ? 'current' : ''}">All</a>
								&nbsp;]&nbsp;
								<c:forEach var="ip" items="${ips}">
									&nbsp;[&nbsp;<a href="${contextPath}/mvc/r/cross?op=${action}&domain=${domain}&ip=${ip}&date=${date}&reportType=${reportType}&project=${project}&remote=${remoteIp}&queryName=${queryName}${customDate}" class="${ipAddress eq ip ? 'current' : ''}"><c:out value="${ip}" /></a>&nbsp;]&nbsp;
								</c:forEach>
							</th>
						</tr>
					</table>
					<script type="text/javascript" src="${contextPath}/js/appendHostname.js"></script>
					<script type="text/javascript">
						$(document).ready(function() { appendHostname(${empty ipToHostnameStr ? "{}" : ipToHostnameStr}); });
					</script>
					<c:choose>
						<c:when test="${hostView}">
							<table class="table table-striped table-condensed">
								<c:if test="${not empty hostInfo.callProjectsInfo}">
									<tr><td colspan="7" style="text-align:center"><strong>调用其他 Pigeon 服务</strong></td></tr>
									<tr>
										<th class="left">Type</th>
										<th class="left"><a href="${contextPath}/mvc/r/cross?op=${hostAction}&domain=${domain}&date=${date}&ip=${ipAddress}&project=${project}&reportType=${reportType}&serviceSort=${serviceSort}&callSort=name${customDate}">RemoteId</a></th>
										<th class="right"><a href="${contextPath}/mvc/r/cross?op=${hostAction}&domain=${domain}&date=${date}&ip=${ipAddress}&project=${project}&reportType=${reportType}&serviceSort=${serviceSort}&callSort=total${customDate}">Total</a></th>
										<th class="right"><a href="${contextPath}/mvc/r/cross?op=${hostAction}&domain=${domain}&date=${date}&ip=${ipAddress}&project=${project}&reportType=${reportType}&serviceSort=${serviceSort}&callSort=failure${customDate}">Failure</a></th>
										<th class="right"><a href="${contextPath}/mvc/r/cross?op=${hostAction}&domain=${domain}&date=${date}&ip=${ipAddress}&project=${project}&reportType=${reportType}&serviceSort=${serviceSort}&callSort=failurePercent${customDate}">Failure%</a></th>
										<th class="right"><a href="${contextPath}/mvc/r/cross?op=${hostAction}&domain=${domain}&date=${date}&ip=${ipAddress}&project=${project}&reportType=${reportType}&serviceSort=${serviceSort}&callSort=avg${customDate}">Avg(ms)</a></th>
										<th class="right">QPS</th>
									</tr>
									<c:forEach var="callInfo" items="${hostInfo.callProjectsInfo}">
										<tr class="right">
											<td class="left"><c:out value="${callInfo.type}" /></td>
											<td class="left"><a href="${contextPath}/mvc/r/cross?op=${methodAction}&domain=${domain}&date=${date}&ip=${ipAddress}&remote=${callInfo.ip}&project=${project}&reportType=${reportType}${customDate}"><c:out value="${callInfo.ip}" /></a></td>
											<td><fmt:formatNumber value="${callInfo.totalCount}" pattern="#,###,###,###,##0" /></td>
											<td><fmt:formatNumber value="${callInfo.failureCount}" pattern="#,###,###,###,##0" /></td>
											<td><fmt:formatNumber value="${callInfo.failurePercent}" pattern="0.0000%" /></td>
											<td><fmt:formatNumber value="${callInfo.avg}" pattern="0.00" /></td>
											<td><fmt:formatNumber value="${callInfo.tps}" pattern="0.00" /></td>
										</tr>
									</c:forEach>
								</c:if>
								<c:if test="${not empty hostInfo.serviceProjectsInfo}">
									<tr>
										<td colspan="7" style="text-align:center"><strong>提供 Pigeon 服务 [ 服务端数据 ]</strong></td>
										<c:if test="${not empty hostInfo.callerProjectsInfo}">
											<td></td>
											<td colspan="7" style="text-align:center"><strong>提供 Pigeon 服务 [ 客户端数据 ]</strong></td>
										</c:if>
									</tr>
									<tr>
										<th class="left">Type</th>
										<th class="left"><a href="${contextPath}/mvc/r/cross?op=${hostAction}&domain=${domain}&date=${date}&ip=${ipAddress}&project=${project}&reportType=${reportType}&callSort=${callSort}&serviceSort=name${customDate}">RemoteId</a></th>
										<th class="right"><a href="${contextPath}/mvc/r/cross?op=${hostAction}&domain=${domain}&date=${date}&ip=${ipAddress}&project=${project}&reportType=${reportType}&callSort=${callSort}&serviceSort=total${customDate}">Total</a></th>
										<th class="right"><a href="${contextPath}/mvc/r/cross?op=${hostAction}&domain=${domain}&date=${date}&ip=${ipAddress}&project=${project}&reportType=${reportType}&callSort=${callSort}&serviceSort=failure${customDate}">Failure</a></th>
										<th class="right"><a href="${contextPath}/mvc/r/cross?op=${hostAction}&domain=${domain}&date=${date}&ip=${ipAddress}&project=${project}&reportType=${reportType}&callSort=${callSort}&serviceSort=failurePercent${customDate}">Failure%</a></th>
										<th class="right"><a href="${contextPath}/mvc/r/cross?op=${hostAction}&domain=${domain}&date=${date}&ip=${ipAddress}&project=${project}&reportType=${reportType}&callSort=${callSort}&serviceSort=avg${customDate}">Avg(ms)</a></th>
										<th class="right">QPS</th>
										<c:if test="${not empty hostInfo.callerProjectsInfo}">
											<th></th><th class="left">Type</th><th class="left">RemoteId</th><th class="right">Total</th><th class="right">Failure</th><th class="right">Failure%</th><th class="right">Avg(ms)</th><th class="right">QPS</th>
										</c:if>
									</tr>
									<c:forEach var="serviceInfo" items="${hostInfo.serviceProjectsInfo}">
										<c:set var="ip" value="${serviceInfo.ip}" />
										<c:set var="callerInfo" value="${hostInfo.callerProjectsInfo}" />
										<tr class="right">
											<td class="left"><c:out value="${serviceInfo.type}" /></td>
											<td class="left"><a href="${contextPath}/mvc/r/cross?op=${methodAction}&domain=${domain}&date=${date}&ip=${ipAddress}&remote=${serviceInfo.ip}&project=${project}&reportType=${reportType}${customDate}"><c:out value="${serviceInfo.ip}" /></a></td>
											<td><fmt:formatNumber value="${serviceInfo.totalCount}" pattern="#,###,###,###,##0" /></td>
											<td><fmt:formatNumber value="${serviceInfo.failureCount}" pattern="#,###,###,###,##0" /></td>
											<td><fmt:formatNumber value="${serviceInfo.failurePercent}" pattern="0.0000%" /></td>
											<td><fmt:formatNumber value="${serviceInfo.avg}" pattern="0.00" /></td>
											<td><fmt:formatNumber value="${serviceInfo.tps}" pattern="0.00" /></td>
											<c:if test="${not empty callerInfo}">
												<td></td>
												<td class="left"><c:out value="${callerInfo[ip].type}" /></td>
												<td class="left"><a href="${contextPath}/mvc/r/cross?op=${methodAction}&domain=${domain}&date=${date}&ip=${ipAddress}&remote=${callerInfo[ip].ip}&project=${project}&reportType=${reportType}${customDate}"><c:out value="${callerInfo[ip].ip}" /></a></td>
												<td><fmt:formatNumber value="${callerInfo[ip].totalCount}" pattern="#,###,###,###,##0" /></td>
												<td><fmt:formatNumber value="${callerInfo[ip].failureCount}" pattern="#,###,###,###,##0" /></td>
												<td><fmt:formatNumber value="${callerInfo[ip].failurePercent}" pattern="0.0000%" /></td>
												<td><fmt:formatNumber value="${callerInfo[ip].avg}" pattern="0.00" /></td>
												<td><fmt:formatNumber value="${callerInfo[ip].tps}" pattern="0.00" /></td>
											</c:if>
										</tr>
									</c:forEach>
								</c:if>
							</table>
						</c:when>
						<c:when test="${methodView}">
							<table class="table table-striped table-condensed">
								<tr><th style="text-align:left" colspan="17">
									<input type="text" name="queryname" id="queryname" size="40" value="${fn:escapeXml(queryName)}">
									<input class="btn btn-primary btn-sm" value="Filter" onclick="filterByName('${date}','${domain}','${ipAddress}')" type="submit">
									支持多个字符串查询，例如 sql|url|task，查询结果为包含任一 sql、url、task 的列
								</th></tr>
								<c:if test="${not empty methodInfo.callProjectsInfo}">
									<tr><td colspan="8" style="text-align:center"><strong>调用其他 Pigeon 服务</strong></td></tr>
									<tr>
										<th class="left">Type</th><th class="left">RemoteId</th><th class="left">Method</th>
										<th class="right"><a href="${contextPath}/mvc/r/cross?op=${methodAction}&domain=${domain}&date=${date}&ip=${ipAddress}&remote=${remoteIp}&project=${project}&reportType=${reportType}&serviceSort=${serviceSort}&callSort=total&queryName=${queryName}${customDate}">Total</a></th>
										<th class="right"><a href="${contextPath}/mvc/r/cross?op=${methodAction}&domain=${domain}&date=${date}&ip=${ipAddress}&remote=${remoteIp}&project=${project}&reportType=${reportType}&serviceSort=${serviceSort}&callSort=failure&queryName=${queryName}${customDate}">Failure</a></th>
										<th class="right"><a href="${contextPath}/mvc/r/cross?op=${methodAction}&domain=${domain}&date=${date}&ip=${ipAddress}&remote=${remoteIp}&project=${project}&reportType=${reportType}&serviceSort=${serviceSort}&callSort=failurePercent&queryName=${queryName}${customDate}">Failure%</a></th>
										<th class="right"><a href="${contextPath}/mvc/r/cross?op=${methodAction}&domain=${domain}&date=${date}&ip=${ipAddress}&remote=${remoteIp}&project=${project}&reportType=${reportType}&serviceSort=${serviceSort}&callSort=avg&queryName=${queryName}${customDate}">Avg(ms)</a></th>
										<th class="right">QPS</th>
									</tr>
									<c:forEach var="callInfo" items="${methodInfo.callProjectsInfo}">
										<tr class="right">
											<td class="left"><c:out value="${callInfo.type}" /></td>
											<td class="left"><c:out value="${callInfo.ip}" /></td>
											<td class="left"><c:out value="${callInfo.id}" /></td>
											<td><fmt:formatNumber value="${callInfo.totalCount}" pattern="#,###,###,###,##0" /></td>
											<td><fmt:formatNumber value="${callInfo.failureCount}" pattern="#,###,###,###,##0" /></td>
											<td><fmt:formatNumber value="${callInfo.failurePercent}" pattern="0.0000%" /></td>
											<td><fmt:formatNumber value="${callInfo.avg}" pattern="0.00" /></td>
											<td><fmt:formatNumber value="${callInfo.tps}" pattern="0.00" /></td>
										</tr>
									</c:forEach>
								</c:if>
								<c:if test="${not empty methodInfo.serviceProjectsInfo}">
									<tr>
										<td colspan="8" style="text-align:center"><strong>提供 Pigeon 服务 [ 服务端数据 ]</strong></td>
										<c:if test="${not empty methodInfo.callerProjectsInfo}">
											<td></td>
											<td colspan="8" style="text-align:center"><strong>提供 Pigeon 服务 [ 客户端数据 ]</strong></td>
										</c:if>
									</tr>
									<tr>
										<th class="left">Type</th><th class="left">RemoteId</th><th class="left">Method</th>
										<th class="right"><a href="${contextPath}/mvc/r/cross?op=${methodAction}&domain=${domain}&date=${date}&ip=${ipAddress}&remote=${remoteIp}&project=${project}&reportType=${reportType}&callSort=${callSort}&serviceSort=total&queryName=${queryName}${customDate}">Total</a></th>
										<th class="right"><a href="${contextPath}/mvc/r/cross?op=${methodAction}&domain=${domain}&date=${date}&ip=${ipAddress}&remote=${remoteIp}&project=${project}&reportType=${reportType}&callSort=${callSort}&serviceSort=failure&queryName=${queryName}${customDate}">Failure</a></th>
										<th class="right"><a href="${contextPath}/mvc/r/cross?op=${methodAction}&domain=${domain}&date=${date}&ip=${ipAddress}&remote=${remoteIp}&project=${project}&reportType=${reportType}&callSort=${callSort}&serviceSort=failurePercent&queryName=${queryName}${customDate}">Failure%</a></th>
										<th class="right"><a href="${contextPath}/mvc/r/cross?op=${methodAction}&domain=${domain}&date=${date}&ip=${ipAddress}&remote=${remoteIp}&project=${project}&reportType=${reportType}&callSort=${callSort}&serviceSort=avg&queryName=${queryName}${customDate}">Avg(ms)</a></th>
										<th class="right">QPS</th>
										<c:if test="${not empty methodInfo.callerProjectsInfo}">
											<th></th><th class="left">Type</th><th class="left">RemoteId</th><th class="left">Method</th><th class="right">Total</th><th class="right">Failure</th><th class="right">Failure%</th><th class="right">Avg(ms)</th><th class="right">QPS</th>
										</c:if>
									</tr>
									<c:forEach var="serviceInfo" items="${methodInfo.serviceProjectsInfo}">
										<c:set var="id" value="${serviceInfo.id}" />
										<c:set var="callerInfo" value="${methodInfo.callerProjectsInfo}" />
										<tr class="right">
											<td class="left"><c:out value="${serviceInfo.type}" /></td>
											<td class="left"><c:out value="${serviceInfo.ip}" /></td>
											<td class="left"><c:out value="${serviceInfo.id}" /></td>
											<td><fmt:formatNumber value="${serviceInfo.totalCount}" pattern="#,###,###,###,##0" /></td>
											<td><fmt:formatNumber value="${serviceInfo.failureCount}" pattern="#,###,###,###,##0" /></td>
											<td><fmt:formatNumber value="${serviceInfo.failurePercent}" pattern="0.0000%" /></td>
											<td><fmt:formatNumber value="${serviceInfo.avg}" pattern="0.00" /></td>
											<td><fmt:formatNumber value="${serviceInfo.tps}" pattern="0.00" /></td>
											<c:if test="${not empty callerInfo}">
												<td></td>
												<td class="left"><c:out value="${callerInfo[id].type}" /></td>
												<td class="left"><c:out value="${callerInfo[id].ip}" /></td>
												<td class="left"><c:out value="${callerInfo[id].id}" /></td>
												<td><fmt:formatNumber value="${callerInfo[id].totalCount}" pattern="#,###,###,###,##0" /></td>
												<td><fmt:formatNumber value="${callerInfo[id].failureCount}" pattern="#,###,###,###,##0" /></td>
												<td><fmt:formatNumber value="${callerInfo[id].failurePercent}" pattern="0.0000%" /></td>
												<td><fmt:formatNumber value="${callerInfo[id].avg}" pattern="0.00" /></td>
												<td><fmt:formatNumber value="${callerInfo[id].tps}" pattern="0.00" /></td>
											</c:if>
										</tr>
									</c:forEach>
								</c:if>
							</table>
						</c:when>
						<c:otherwise>
							<table class="table table-striped table-condensed">
								<c:if test="${not empty projectInfo.callProjectsInfo}">
									<tr><td colspan="7" style="text-align:center"><strong>调用其他 Pigeon 服务</strong></td></tr>
									<tr>
										<th class="left">Type</th>
										<th class="left"><a href="${contextPath}/mvc/r/cross?op=${projectAction}&domain=${domain}&date=${date}&ip=${ipAddress}&reportType=${reportType}&serviceSort=${serviceSort}&callSort=name${customDate}">RemoteProject</a></th>
										<th class="right"><a href="${contextPath}/mvc/r/cross?op=${projectAction}&domain=${domain}&date=${date}&ip=${ipAddress}&reportType=${reportType}&serviceSort=${serviceSort}&callSort=total${customDate}">Total</a></th>
										<th class="right"><a href="${contextPath}/mvc/r/cross?op=${projectAction}&domain=${domain}&date=${date}&ip=${ipAddress}&reportType=${reportType}&serviceSort=${serviceSort}&callSort=failure${customDate}">Failure</a></th>
										<th class="right"><a href="${contextPath}/mvc/r/cross?op=${projectAction}&domain=${domain}&date=${date}&ip=${ipAddress}&reportType=${reportType}&serviceSort=${serviceSort}&callSort=failurePercent${customDate}">Failure%</a></th>
										<th class="right"><a href="${contextPath}/mvc/r/cross?op=${projectAction}&domain=${domain}&date=${date}&ip=${ipAddress}&reportType=${reportType}&serviceSort=${serviceSort}&callSort=avg${customDate}">Avg(ms)</a></th>
										<th class="right">QPS</th>
									</tr>
									<c:forEach var="callInfo" items="${projectInfo.callProjectsInfo}">
										<tr class="right">
											<td class="left"><c:out value="${callInfo.type}" /></td>
											<td class="left"><a href="${contextPath}/mvc/r/cross?op=${hostAction}&domain=${domain}&date=${date}&ip=${ipAddress}&project=${callInfo.projectName}&reportType=${reportType}${customDate}"><c:out value="${callInfo.projectName}" /></a></td>
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
										<th class="left"><a href="${contextPath}/mvc/r/cross?op=${projectAction}&domain=${domain}&date=${date}&ip=${ipAddress}&reportType=${reportType}&callSort=${callSort}&serviceSort=name${customDate}">RemoteProject</a></th>
										<th class="right"><a href="${contextPath}/mvc/r/cross?op=${projectAction}&domain=${domain}&date=${date}&ip=${ipAddress}&reportType=${reportType}&callSort=${callSort}&serviceSort=total${customDate}">Total</a></th>
										<th class="right"><a href="${contextPath}/mvc/r/cross?op=${projectAction}&domain=${domain}&date=${date}&ip=${ipAddress}&reportType=${reportType}&callSort=${callSort}&serviceSort=failure${customDate}">Failure</a></th>
										<th class="right"><a href="${contextPath}/mvc/r/cross?op=${projectAction}&domain=${domain}&date=${date}&ip=${ipAddress}&reportType=${reportType}&callSort=${callSort}&serviceSort=failurePercent${customDate}">Failure%</a></th>
										<th class="right"><a href="${contextPath}/mvc/r/cross?op=${projectAction}&domain=${domain}&date=${date}&ip=${ipAddress}&reportType=${reportType}&callSort=${callSort}&serviceSort=avg${customDate}">Avg(ms)</a></th>
										<th class="right">QPS</th>
										<c:if test="${not empty projectInfo.callerProjectsInfo}">
											<th></th><th class="left">Type</th><th class="left">RemoteProject</th><th class="right">Total</th><th class="right">Failure</th><th class="right">Failure%</th><th class="right">Avg(ms)</th><th class="right">QPS</th>
										</c:if>
									</tr>
									<c:forEach var="serviceInfo" items="${projectInfo.serviceProjectsInfo}">
										<c:set var="projectName" value="${serviceInfo.projectName}" />
										<c:set var="callerInfo" value="${projectInfo.callerProjectsInfo}" />
										<tr class="right">
											<td class="left"><c:out value="${serviceInfo.type}" /></td>
											<td class="left"><a href="${contextPath}/mvc/r/cross?op=${hostAction}&domain=${domain}&date=${date}&ip=${ipAddress}&project=${serviceInfo.projectName}&reportType=${reportType}${customDate}"><c:out value="${serviceInfo.projectName}" /></a></td>
											<td><fmt:formatNumber value="${serviceInfo.totalCount}" pattern="#,###,###,###,##0" /></td>
											<td><fmt:formatNumber value="${serviceInfo.failureCount}" pattern="#,###,###,###,##0" /></td>
											<td><fmt:formatNumber value="${serviceInfo.failurePercent}" pattern="0.0000%" /></td>
											<td><fmt:formatNumber value="${serviceInfo.avg}" pattern="0.00" /></td>
											<td><fmt:formatNumber value="${serviceInfo.tps}" pattern="0.00" /></td>
											<c:if test="${not empty callerInfo}">
												<td></td>
												<td class="left"><c:out value="${callerInfo[projectName].type}" /></td>
												<td class="left"><a href="${contextPath}/mvc/r/cross?op=${hostAction}&domain=${domain}&date=${date}&ip=${ipAddress}&project=${callerInfo[projectName].projectName}&reportType=${reportType}${customDate}"><c:out value="${callerInfo[projectName].projectName}" /></a></td>
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
						</c:otherwise>
					</c:choose>
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
			return '<a href="${contextPath}/mvc/r/cross?op=${projectAction}&domain=' + encodeURIComponent(domain) + '&date=${date}&reportType=${reportType}">&nbsp;[&nbsp;' + domain + '&nbsp;]&nbsp;</a>';
		}
		function filterByName(date, domain, ip) {
			var queryName = $("#queryname").val();
			window.location.href = '${contextPath}/mvc/r/cross?op=${methodAction}&domain=' + domain + '&ip=' + ip + '&date=' + date
					+ '&queryName=' + encodeURIComponent(queryName) + '&remote=${remoteIp}&project=${project}'
					+ '&reportType=${reportType}&serviceSort=${serviceSort}&callSort=${callSort}${customDate}';
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
				window.location.href = '${contextPath}/mvc/r/cross?op=${projectAction}&domain=' + encodeURIComponent($("#search").val()) + '&date=${date}&reportType=${reportType}';
			});
			$('#wrap_search').submit(function() {
				window.location.href = '${contextPath}/mvc/r/cross?op=${projectAction}&domain=' + encodeURIComponent($("#search").val()) + '&date=${date}&reportType=${reportType}';
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
