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
	<script src="${contextPath}/js/highcharts.js"></script>
	<script src="${contextPath}/js/baseGraph.js"></script>
	<script src="${contextPath}/js/jquery.datetimepicker.js"></script>
	<script src="${contextPath}/assets/js/jquery-ui.min.js"></script>
	<script src="${contextPath}/assets/js/jquery.ui.touch-punch.min.js"></script>
	<script src="${contextPath}/assets/js/ace-elements.min.js"></script>
	<script src="${contextPath}/assets/js/ace.min.js"></script>
	<style>
		.left { text-align: left; }
		.current { color: red; font-weight: bold; }
	</style>
</head>
<body class="no-skin">
	<c:set var="navbarApplicationDisabled" value="true" scope="request" />
	<jsp:include page="../common/navbar.jsp" />
	<div class="main-container" id="main-container">
		<c:set var="activeReport" value="Business" scope="request" />
		<jsp:include page="../common/reportSidebar.jsp" />
		<div class="main-content">
			<div style="padding-top:2px;padding-left:2px;padding-right:8px;">
				<div class="report">
					<div class="breadcrumbs" id="breadcrumbs">
						&nbsp;&nbsp;时间段
						<c:forEach var="range" items="${ranges}">
							&nbsp;&nbsp;&nbsp;[
							<a href="${contextPath}/mvc/r/business?op=view&name=${name}&type=${type}&timeRange=${range.duration}&endDate=${endTime}" class="${timeRange eq range.duration ? 'current' : ''}">${range.title}</a>
							]
						</c:forEach>
						<div class="nav-search nav" id="nav-search">
							<c:forEach var="nav" items="${navs}">
								&nbsp;[ <a href="${contextPath}/mvc/r/business?op=view&name=${name}&type=${type}&endDate=${endTime}&step=${nav.hours}&timeRange=${timeRange}">${nav.title}</a> ]&nbsp;
							</c:forEach>
							&nbsp;[ <a href="${contextPath}/mvc/r/business?op=view&name=${name}&type=${type}&timeRange=${timeRange}">now</a> ]&nbsp;
						</div>
						<script type="text/javascript">
							try { ace.settings.check('breadcrumbs', 'fixed'); } catch(e) {}
						</script>
					</div>
					<table>
						<tr>
							<th class="left">
								<div style="float:left;">
									&nbsp;开始 <input type="text" id="startTime" style="width:150px;">
									结束 <input type="text" id="endTime" style="width:150px;">
								</div>
							</th>
							<th>&nbsp;&nbsp;查询条件
								<i data-rel="tooltip" data-placement="left" title="输入 domain 或者标签，标签以 TAG_ 开头" class="glyphicon glyphicon-question-sign"></i>&nbsp;&nbsp;
							</th>
							<th>
								<div class="navbar-header pull-left" style="width:350px;">
									<form id="wrap_search" style="margin-bottom:0px;">
										<div class="input-group">
											<input id="search" type="text" class="search-input form-control ui-autocomplete-input" placeholder="input domain for search" autocomplete="off">
											<span class="input-group-btn"><button class="btn btn-sm btn-primary" type="button" id="search_go">Go</button></span>
										</div>
									</form>
								</div>
							</th>
						</tr>
					</table>
					<div>
						<c:forEach var="item" items="${lineCharts}">
							<div style="float:left;">
								<div id="${item.id}" class="metricGraph"></div>
							</div>
						</c:forEach>
					</div>
				</div>
			</div>
		</div>
	</div>
	<script type="text/javascript">
		function query() {
			var queryName = $("#search").val();
			var queryType = 'domain';
			var start = $("#startTime").val();
			var end = $("#endTime").val();

			if (queryName.substring(0, 4) == 'TAG_') {
				queryType = 'tag';
				queryName = queryName.substring(4);
			}

			var startDate = new Date(Date.parse(start.replace(/-/g, "/")));
			var endDate = new Date(Date.parse(end.replace(/-/g, "/")));
			if (endDate - startDate > 2 * 24 * 60 * 60 * 1000) {
				alert("选择的时间间隔不要超过两天");
			} else {
				window.location.href = "${contextPath}/mvc/r/business?name=" + queryName + "&type=" + queryType
					+ "&startDate=" + start + "&endDate=" + end;
			}
		}
		$(document).ready(function() {
			$('[data-rel=tooltip]').tooltip();
			$('#startTime').datetimepicker({
				format: 'Y-m-d H:i',
				step: 30,
				maxDate: 0
			});
			$('#endTime').datetimepicker({
				format: 'Y-m-d H:i',
				step: 30,
				maxDate: 0
			});
			$('#startTime').val("${startTime}");
			$('#endTime').val("${endTime}");
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
			<c:forEach var="item" items="${tags}">
			data.push({ label: 'TAG_<c:out value="${item}" />', category: '标签' });
			</c:forEach>
			<c:forEach var="item" items="${domains}">
			data.push({ label: '<c:out value="${item}" />', category: '项目' });
			</c:forEach>
			$("#search").catcomplete({
				delay: 0,
				source: data
			});
			$("#search_go").bind("click", function() {
				query();
			});
			$('#wrap_search').submit(function() {
				query();
				return false;
			});
			if ('${type}' == 'tag') {
				$('#search').val("TAG_${fn:escapeXml(name)}");
			} else {
				$('#search').val('${fn:escapeXml(name)}');
			}
			<c:forEach var="item" items="${lineCharts}">
			graphMetricChart(document.getElementById('${item.id}'), ${item.jsonString});
			</c:forEach>
			$('.position').hide();
			$('#Business_report').addClass("open active");
			$('#nav_application').click(function() {
				window.location.href = '${contextPath}/mvc/r/business?name=${name}&type=${type}';
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
