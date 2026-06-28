<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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
	<link rel="stylesheet" type="text/css" href="${contextPath}/js/jquery.datetimepicker.css">
	<script src="${contextPath}/assets/js/jquery.min.js"></script>
	<script src="${contextPath}/assets/js/ace-extra.min.js"></script>
	<script src="${contextPath}/assets/js/bootstrap.min.js"></script>
	<script src="${contextPath}/assets/js/jquery-ui.min.js"></script>
	<script src="${contextPath}/assets/js/jquery.ui.touch-punch.min.js"></script>
	<script src="${contextPath}/assets/js/ace-elements.min.js"></script>
	<script src="${contextPath}/assets/js/ace.min.js"></script>
	<script src="${contextPath}/js/jquery.datetimepicker.js"></script>
	<script src="${contextPath}/js/jquery.dataTables.min.js"></script>
	<script src="${contextPath}/js/tableInit.js"></script>
	<style>
		.left { text-align: left; }
		.right { text-align: right; }
		.current { color: red; font-weight: bold; }
		.statistics-tabs { margin: 6px 0 10px; }
	</style>
</head>
<body class="no-skin">
	<c:set var="navbarApplicationDisabled" value="true" scope="request" />
	<jsp:include page="../common/navbar.jsp" />
	<div class="main-container" id="main-container">
		<c:set var="activeReport" value="Statistics" scope="request" />
		<jsp:include page="../common/reportSidebar.jsp" />
		<div class="main-content">
			<div style="padding-top:2px;padding-left:2px;padding-right:8px;">
				<div class="report">
					<div class="breadcrumbs" id="breadcrumbs">
						<table style="line-height:normal;">
							<tr>
								<td><span class="text-danger title">【报表时间】</span><span class="text-success">&nbsp;&nbsp;${reportStart} to ${reportEnd}</span></td>
								<td>
									<div class="nav-search nav" id="nav-search">
										<c:choose>
											<c:when test="${historyMode}">
												<span class="text-danger switch">【<a class="switch" href="${baseUri}?domain=${encodedDomain}&ip=${encodedIpAddress}&op=${navAction}"><span class="text-danger">切到小时模式</span></a>】</span>
												<c:forEach var="nav" items="${historyNavs}">
													&nbsp;[ <a href="${baseUri}?op=${action}&domain=${encodedDomain}&ip=${encodedIpAddress}&date=${date}&reportType=${nav.title}" class="${nav.title eq reportType ? 'current' : ''}">${nav.title}</a> ]
												</c:forEach>
												&nbsp;[ <a href="${baseUri}?op=${action}&domain=${encodedDomain}&ip=${encodedIpAddress}&date=${date}&reportType=${reportType}&step=-1">${currentNav.last}</a> ]
												&nbsp;[ <a href="${baseUri}?op=${action}&domain=${encodedDomain}&ip=${encodedIpAddress}&date=${date}&reportType=${reportType}&step=1">${currentNav.next}</a> ]
												&nbsp;[ <a href="${baseUri}?op=${action}&domain=${encodedDomain}&ip=${encodedIpAddress}&reportType=${reportType}">now</a> ]&nbsp;
											</c:when>
											<c:when test="${action eq 'service' || action eq 'heavy' || action eq 'utilization'}">
												<span class="text-danger switch">【<a class="switch" href="${baseUri}?op=history${action eq 'service' ? 'Service' : action eq 'heavy' ? 'Heavy' : 'Utilization'}&domain=${encodedDomain}&ip=${encodedIpAddress}"><span class="text-danger">切到历史模式</span></a>】</span>
												<c:forEach var="nav" items="${navs}">
													&nbsp;[ <a href="${baseUri}?op=${action}&date=${date}&step=${nav.hours}">${nav.title}</a> ]
												</c:forEach>
												&nbsp;[ <a href="${baseUri}?op=${action}">now</a> ]&nbsp;
											</c:when>
											<c:when test="${action eq 'jar'}">
												<c:forEach var="nav" items="${navs}">
													&nbsp;[ <a href="${baseUri}?op=jar&date=${date}&step=${nav.hours}">${nav.title}</a> ]
												</c:forEach>
												&nbsp;[ <a href="${baseUri}?op=jar">now</a> ]&nbsp;
											</c:when>
										</c:choose>
									</div>
								</td>
							</tr>
						</table>
						<script type="text/javascript">
							try { ace.settings.check('breadcrumbs', 'fixed'); } catch(e) {}
						</script>
					</div>
					<ul class="nav nav-tabs statistics-tabs">
						<li class="${action eq 'service' || action eq 'historyService' ? 'active' : ''}"><a href="${baseUri}?op=service">服务可用排行</a></li>
						<li class="${action eq 'client' ? 'active' : ''}"><a href="${baseUri}?op=client">服务调用排行</a></li>
						<li class="${action eq 'utilization' || action eq 'historyUtilization' ? 'active' : ''}"><a href="${baseUri}?op=utilization">线上容量规划</a></li>
						<li class="${action eq 'jar' ? 'active' : ''}"><a href="${baseUri}?op=jar">线上JAR版本</a></li>
						<li class="${action eq 'heavy' || action eq 'historyHeavy' ? 'active' : ''}"><a href="${baseUri}?op=heavy">重量访问排行</a></li>
						<li class="${action eq 'summary' ? 'active' : ''}"><a href="${baseUri}?op=summary">告警智能分析</a></li>
					</ul>
					<c:choose>
						<c:when test="${action eq 'service' || action eq 'historyService'}"><jsp:include page="service.jsp" /></c:when>
						<c:when test="${action eq 'client'}"><jsp:include page="client.jsp" /></c:when>
						<c:when test="${action eq 'utilization' || action eq 'historyUtilization'}"><jsp:include page="utilization.jsp" /></c:when>
						<c:when test="${action eq 'jar'}"><jsp:include page="jar.jsp" /></c:when>
						<c:when test="${action eq 'heavy' || action eq 'historyHeavy'}"><jsp:include page="heavy.jsp" /></c:when>
						<c:when test="${action eq 'summary'}"><jsp:include page="summary.jsp" /></c:when>
					</c:choose>
				</div>
			</div>
		</div>
	</div>
</body>
</html>
