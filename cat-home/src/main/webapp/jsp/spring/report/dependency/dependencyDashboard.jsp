<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html lang="en">
<head>
	<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
	<meta charset="utf-8">
	<title>CAT</title>
	<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0">
	<link rel="stylesheet" type="text/css" href="${contextPath}/assets/css/bootstrap.min.css">
	<link rel="stylesheet" type="text/css" href="${contextPath}/assets/css/font-awesome.min.css">
	<link rel="stylesheet" type="text/css" href="${contextPath}/assets/css/ace.min.css" id="main-ace-style">
	<link rel="stylesheet" type="text/css" href="${contextPath}/css/body.css">
	<script src="${contextPath}/assets/js/jquery.min.js"></script>
	<script src="${contextPath}/assets/js/bootstrap.min.js"></script>
	<script src="${contextPath}/assets/js/ace.min.js"></script>
	<script src="${contextPath}/js/raphael-min.js"></script>
	<script src="${contextPath}/js/startopo.js"></script>
	<style>
		.tooltip-inner { max-width: 36555px; }
		.tab-content table { max-width: 100%; background-color: transparent; border-collapse: collapse; border-spacing: 0; }
		.pagination { margin: 4px 0; }
		.pagination ul { margin-top: 0px; }
		.pagination ul > li > a, .pagination ul > li > span { padding: 3px 10px; }
	</style>
</head>
<body class="no-skin">
	<c:set var="navbarApplicationDisabled" value="true" scope="request" />
	<jsp:include page="../common/navbar.jsp" />
	<div class="main-container" id="main-container">
		<c:set var="activeReport" value="Dependency" scope="request" />
		<jsp:include page="../common/reportSidebar.jsp" />
		<div class="main-content">
			<div style="padding-top:2px;padding-left:2px;padding-right:8px;">
				<div class="report">
					<div class="breadcrumbs" id="breadcrumbs">
						<span class="text-success">${reportStart} to ${reportEnd}</span>
						<span class="pull-right">
							<span class="text-danger switch">【<a class="switch" href="${baseUri}?op=dashboard&domain=${domain}"><span class="text-danger">切到历史模式</span></a>】</span>
							<c:forEach var="nav" items="${navs}">
								[ <a href="${baseUri}?op=dashboard&domain=${domain}&date=${date}&step=${nav.hours}">${nav.title}</a> ]
							</c:forEach>
							[ <a href="${baseUri}?op=dashboard&domain=${domain}">now</a> ]
						</span>
					</div>
					<div class="text-center"><jsp:include page="dependencyTimeNav.jsp" /></div>
					<div id="fullScreenData">
						<div class="text-center" id="container" style="width:100%;height:1600px;border:solid 1px #ccc;"></div>
						<br>
					</div>
				</div>
			</div>
		</div>
	</div>
	<script type="text/javascript">
		$(document).ready(function() {
			$('#minute${minute}').addClass('disabled');
			var data = ${dashboardGraph};
			var format = ${format};
			var option = {
				typeMap:{ database:'circle', project:'rect', service:'lozenge' },
				colorMap:{ "1":'#2fbf2f', "2":'#bfa22f', "3":'#b94a48', "4":'#772fbf' },
				legendMap:{ "1":"good", "2":"warning", "3":"error" },
				paddingInside:5,
				col:3,
				colInside:5,
				paddingUp:10,
				blockPaddingRatio:0.2,
				leftTitlePaddingRatio:0.05,
				showLeft:false,
				showUp:true
			};
			option['format'] = format;
			new StarTopoList('container', data, option);
		});
	</script>
</body>
</html>
