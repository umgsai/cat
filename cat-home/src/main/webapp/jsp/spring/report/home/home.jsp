<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="docName" value="${empty docName ? 'index' : docName}" />
<c:set var="domain" value="${empty domain ? 'cat' : domain}" />
<c:set var="ipAddress" value="${empty ipAddress ? 'All' : ipAddress}" />
<c:set var="date" value="${empty date ? '' : date}" />
<c:set var="reportType" value="${empty reportType ? 'day' : reportType}" />
<c:set var="actionName" value="${empty actionName ? 'view' : actionName}" />
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
	<script src="${contextPath}/js/highcharts.js"></script>
	<script src="${contextPath}/js/baseGraph.js"></script>
	<script src="${contextPath}/js/jquery.datetimepicker.js"></script>
	<script src="${contextPath}/assets/js/jquery-ui.min.js"></script>
	<script src="${contextPath}/assets/js/jquery.ui.touch-punch.min.js"></script>
	<script src="${contextPath}/assets/js/ace-elements.min.js"></script>
	<script src="${contextPath}/assets/js/ace.min.js"></script>
</head>
<body class="no-skin">
	<div id="navbar" class="navbar navbar-default">
		<script type="text/javascript">
			try { ace.settings.check('navbar', 'fixed'); } catch(e) {}
		</script>
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
					<button class="btn btn-success btn-sm" id="nav_application">
						<i class="ace-icon fa fa-signal"></i>Application
					</button>
					<button class="btn btn-inverse btn-sm" id="nav_config">
						<i class="ace-icon fa fa-cogs"></i>Configs
					</button>
					<button class="btn btn-yellow btn-sm disabled" id="nav_document">
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
					<li class="light-blue">
						<a data-toggle="dropdown" href="#" class="dropdown-toggle">
							<span class="user-info" style="max-width:200px">
								<span id="loginInfo"></span>
							</span>
						</a>
					</li>
				</ul>
			</div>
		</div>
	</div>
	<script type="text/javascript">
		$(document).ready(function() {
			$("#nav_document").addClass("disabled");
		});
	</script>
	<div class="main-container" id="main-container">
		<script type="text/javascript">
			try { ace.settings.check('main-container', 'fixed'); } catch(e) {}
		</script>
		<div id="sidebar" class="sidebar responsive">
			<script type="text/javascript">
				try { ace.settings.check('sidebar', 'fixed'); } catch(e) {}
			</script>
			<ul class="nav nav-list" style="top: 0px;">
				<li id="indexButton">
					<a href="${contextPath}/mvc/r/home?op=view&docName=index">
						<i class="menu-icon glyphicon glyphicon-home"></i>
						<span class="menu-text">椤圭洰棣栭〉</span>
					</a>
					<b class="arrow"></b>
				</li>
				<li id="releaseButton">
					<a href="${contextPath}/mvc/r/home?op=view&docName=release">
						<i class="menu-icon glyphicon glyphicon-book"></i>
						<span class="menu-text">鐗堟湰璇存槑</span>
					</a>
					<b class="arrow"></b>
				</li>
				<li id="pluginButton">
					<a href="${contextPath}/mvc/r/home?op=view&docName=plugin">
						<i class="menu-icon fa fa-key"></i>
						<span class="menu-text">鎻掍欢鎵╁睍</span>
					</a>
					<b class="arrow"></b>
				</li>
			</ul>
			<div class="sidebar-toggle sidebar-collapse" id="sidebar-collapse">
				<i class="ace-icon fa fa-angle-double-left" data-icon1="ace-icon fa fa-angle-double-left" data-icon2="ace-icon fa fa-angle-double-right"></i>
			</div>
			<script type="text/javascript">
				try { ace.settings.check('sidebar', 'collapsed'); } catch(e) {}
			</script>
		</div>
		<div class="main-content">
			<div style="padding-top:2px;padding-right:8px;">
				<div class="tab-content">
					<c:choose>
						<c:when test="${docName eq 'release'}">
							<jsp:include page="../../../report/home/releasenotes.jsp" />
						</c:when>
						<c:when test="${docName eq 'plugin'}">
							<jsp:include page="../../../report/home/plugin.jsp" />
						</c:when>
						<c:otherwise>
							<jsp:include page="../../../report/home/index.jsp" />
						</c:otherwise>
					</c:choose>
				</div>
				<br>
				<br>
				<a href="${contextPath}/mvc/r/home?op=checkpoint&domain=${domain}&date=${date}" style="color:#FFF">Do checkpoint here</a>
			</div>
		</div>
	</div>
	<script type="text/javascript">
		$(document).ready(function() {
			var liElement = $('#${docName}Button');
			if (liElement.size && liElement.size() == 0) {
				liElement = $('#indexButton');
			}
			liElement.addClass('active');

			$("#nav_application").click(function() {
				window.location.href = "${contextPath}/mvc/r/t?domain=${domain}&ip=${ipAddress}&date=${date}&reportType=${reportType}&op=${actionName}";
			});
			$("#nav_config").click(function() {
				window.location.href = "${contextPath}/mvc/s/config?op=projects";
			});
			$("#nav_document").click(function() {
				window.location.href = "${contextPath}/mvc/r/home?domain=${domain}&ip=${ipAddress}&date=${date}&reportType=${reportType}&op=${actionName}";
			});
			$("a[href]").each(function() {
				var href = $(this).attr("href");
				var origin = window.location.protocol + "//" + window.location.host;

				if (href.indexOf("${contextPath}/r/") == 0) {
					$(this).attr("href", "${contextPath}/mvc/r/" + href.substring("${contextPath}/r/".length));
				} else if (href.indexOf("${contextPath}/s/") == 0) {
					$(this).attr("href", "${contextPath}/mvc/s/" + href.substring("${contextPath}/s/".length));
				} else if (href.indexOf(origin + "${contextPath}/r/") == 0) {
					$(this).attr("href", "${contextPath}/mvc/r/" + href.substring((origin + "${contextPath}/r/").length));
				} else if (href.indexOf(origin + "${contextPath}/s/") == 0) {
					$(this).attr("href", "${contextPath}/mvc/s/" + href.substring((origin + "${contextPath}/s/").length));
				}
			});
		});
	</script>
</body>
</html>
