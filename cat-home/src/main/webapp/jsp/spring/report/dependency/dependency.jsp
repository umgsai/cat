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
	<script src="${contextPath}/js/highcharts.js"></script>
	<script src="${contextPath}/js/baseGraph.js"></script>
	<script src="${contextPath}/js/jquery.dataTables.min.js"></script>
	<style>
		.pagination { margin: 4px 0; }
		.pagination ul { margin-top: 0px; }
		.pagination ul > li > a, .pagination ul > li > span { padding: 3px 10px; }
		.graph { width: 450px; height: 200px; margin: 4px auto; }
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
						<span id="warp_search_group" style="display:inline-block;width:250px;margin-left:8px;vertical-align:middle;white-space:nowrap;">
							<button class="btn btn-sm btn-default" type="button">全部</button><button class="btn btn-sm btn-default" type="button">常用</button><input id="search" type="text" value="${domain}" style="height:30px;width:110px;" autocomplete="off"><button class="btn btn-sm btn-pink" type="button" id="search_go">Go</button>
						</span>
						<span class="pull-right">
							<span class="text-danger switch">【<a class="switch" href="${baseUri}?op=lineChart&domain=${domain}"><span class="text-danger">切到历史模式</span></a>】</span>
							<c:forEach var="nav" items="${navs}">
								[ <a href="${baseUri}?op=lineChart&domain=${domain}&date=${date}&step=${nav.hours}">${nav.title}</a> ]
							</c:forEach>
							[ <a href="${baseUri}?op=lineChart&domain=${domain}">now</a> ]
						</span>
					</div>
					<div class="tabbable text-danger" id="content">
						<jsp:include page="dependencyLineGraph.jsp" />
					</div>
				</div>
			</div>
		</div>
	</div>
	<script type="text/javascript">
		$(document).ready(function() {
			var tab = '${tab}';
			if (tab == 'tab3') {
				$('#tab3Href').trigger('click');
			} else if (tab == 'tab2') {
				$('#tab2Href').trigger('click');
			} else if (tab == 'tab1') {
				$('#tab1Href').trigger('click');
			}
			$('.contents').dataTable({ "sPaginationType": "full_numbers", 'iDisplayLength': 50, "bPaginate": false });
			$('.contentsDependency').dataTable({ "sPaginationType": "full_numbers", 'iDisplayLength': 50, "bPaginate": false });
			$('.dataTables_info').css('display', 'none');
			$('#search_go').click(function() {
				var target = $('#search').val();
				window.location.href = '${baseUri}?op=lineChart&domain=' + encodeURIComponent(target);
			});
		});
	</script>
</body>
</html>
