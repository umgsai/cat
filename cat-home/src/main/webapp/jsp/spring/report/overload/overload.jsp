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
	<link rel="stylesheet" type="text/css" href="${contextPath}/css/table.css">
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
</head>
<body class="no-skin">
	<c:set var="navbarApplicationDisabled" value="true" scope="request" />
	<c:set var="navbarShowLogin" value="true" scope="request" />
	<jsp:include page="../common/navbar.jsp" />
	<div class="main-container" id="main-container">
		<script type="text/javascript">
			try { ace.settings.check('main-container', 'fixed'); } catch(e) {}
		</script>
		<jsp:include page="../common/reportSidebar.jsp" />
		<div class="main-content">
			<div style="padding-top:2px;padding-right:8px;">
				<div class="row-fluid">
					<div id="queryBar">
						<div style="float:left;">
							&nbsp;开始
							<input type="text" id="startTime" style="width:150px;" value="<fmt:formatDate value='${startTime}' pattern='yyyy-MM-dd HH:mm'/>"/>
							结束
							<input type="text" id="endTime" style="width:150px;" value="<fmt:formatDate value='${endTime}' pattern='yyyy-MM-dd HH:mm'/>"/>
						</div>
						&nbsp;&nbsp;<input class="btn btn-primary btn-sm" value="查询" onclick="queryNew()" type="submit">
						&nbsp;&nbsp;<button id="fullScreen" class="btn btn-sm">全屏</button>
						<input type="hidden" id="fullScreenStr" value="${fullScreen}">
						&nbsp;&nbsp;
						<div class="btn-group" data-toggle="buttons">
							<label id="hourlyButton" class="btn btn-sm btn-info active"><input type="checkbox" checked>小时报表</label>
							<label id="dailyButton" class="btn btn-sm btn-info active"><input type="checkbox" checked>天报表</label>
							<label id="weeklyButton" class="btn btn-sm btn-info active"><input type="checkbox" checked>周报表</label>
							<label id="monthlyButton" class="btn btn-sm btn-info active"><input type="checkbox" checked>月报表</label>
						</div>
						<input type="hidden" id="hourlyStatus" value="${showHourly}">
						<input type="hidden" id="dailyStatus" value="${showDaily}">
						<input type="hidden" id="weeklyStatus" value="${showWeekly}">
						<input type="hidden" id="monthlyStatus" value="${showMonthly}">
					</div>
					<div id="DatabaseReport" style="display:inline-flex;padding-top:3px;">
						<table class="table table-striped table-condensed table-hover" style="width:100%" id="contents">
							<thead>
								<tr class="text-success">
									<th width="20%">日期</th>
									<th width="10%">报表类型</th>
									<th width="15%">报表名称</th>
									<th width="20%">项目</th>
									<th width="15%">ip</th>
									<th width="10%">报表格式</th>
									<th width="10%">报表长度</th>
								</tr>
							</thead>
							<tbody>
								<c:forEach var="report" items="${reports}">
									<tr class="reportType${report.reportType}">
										<td>${report.period}</td>
										<c:choose>
											<c:when test="${report.reportType eq 1}"><td>小时报表</td></c:when>
											<c:when test="${report.reportType eq 2}"><td>天报表</td></c:when>
											<c:when test="${report.reportType eq 3}"><td>周报表</td></c:when>
											<c:when test="${report.reportType eq 4}"><td>月报表</td></c:when>
											<c:otherwise><td></td></c:otherwise>
										</c:choose>
										<td><c:out value="${report.name}" /></td>
										<td><c:out value="${report.domain}" /></td>
										<td><c:out value="${report.ip}" /></td>
										<c:choose>
											<c:when test="${report.type eq 1}"><td>binary</td></c:when>
											<c:when test="${report.type eq 2}"><td>xml</td></c:when>
											<c:otherwise><td></td></c:otherwise>
										</c:choose>
										<td><fmt:formatNumber type="number" maxFractionDigits="1" minFractionDigits="1" value="${report.reportLength}" /></td>
									</tr>
								</c:forEach>
							</tbody>
						</table>
					</div>
				</div>
			</div>
		</div>
	</div>
	<script type="text/javascript">
		$(document).ready(function() {
			init();

			<c:if test="${fullScreen}">
				$('#fullScreen').addClass('btn-danger');
				$('.navbar').hide();
				$('.footer').hide();
			</c:if>
			<c:if test="${showHourly == false}">
				toggleButton("hourly", true);
			</c:if>
			<c:if test="${showDaily == false}">
				toggleButton("daily", true);
			</c:if>
			<c:if test="${showWeekly == false}">
				toggleButton("weekly", true);
			</c:if>
			<c:if test="${showMonthly == false}">
				toggleButton("monthly", true);
			</c:if>

			$('#startTime').datetimepicker({
				format:'Y-m-d H:i',
				step:30,
				maxDate:0
			});
			$('#endTime').datetimepicker({
				format:'Y-m-d H:i',
				step:30,
				maxDate:0
			});
			$("#fullScreen").click(clickFullScreen);
			$("#hourlyButton").click(function() {
				toggleButton("hourly", false);
			});
			$("#dailyButton").click(function() {
				toggleButton("daily", false);
			});
			$("#weeklyButton").click(function() {
				toggleButton("weekly", false);
			});
			$("#monthlyButton").click(function() {
				toggleButton("monthly", false);
			});
			$("#nav_application").click(function() {
				window.location.href = "${contextPath}/mvc/r/t?domain=${domain}&ip=${ipAddress}&date=${date}&reportType=${reportType}&op=view";
			});
			$("#nav_config").click(function() {
				window.location.href = "${contextPath}/mvc/s/config?op=projects";
			});
			$("#nav_document").click(function() {
				window.location.href = "${contextPath}/mvc/r/home?domain=${domain}&ip=${ipAddress}&date=${date}&reportType=${reportType}&op=view";
			});
		});

		var buttonToInt = {'hourly':1, 'daily':2, 'weekly':3, 'monthly':4};

		function clickFullScreen() {
			var isFullScreen = $('#fullScreenStr').val() === 'true';
			if (isFullScreen) {
				$('#fullScreen').removeClass('btn-danger');
				$('.navbar').show();
				$('.footer').show();
			} else {
				$('#fullScreen').addClass('btn-danger');
				$('.navbar').hide();
				$('.footer').hide();
			}
			$('#fullScreenStr').val(!isFullScreen);
		}
		function toggleButton(button, isInitialized) {
			var targetStatus = $("#" + button + "Status").val() === 'false';
			if (isInitialized) {
				$("#" + button + "Button").button('toggle');
				targetStatus = !targetStatus;
			}

			if (targetStatus) {
				$(".reportType" + buttonToInt[button]).css("display", "table-row");
			} else {
				$(".reportType" + buttonToInt[button]).css("display", "none");
			}
			$("#" + button + "Status").val(String(targetStatus));
		}
		function getType() {
			var hourlyStr = $('#hourlyStatus').val();
			var dailyStr = $('#dailyStatus').val();
			var weeklyStr = $('#weeklyStatus').val();
			var monthlyStr = $('#monthlyStatus').val();
			return "showHourly=" + hourlyStr + "&showDaily=" + dailyStr + "&showWeekly=" + weeklyStr + "&showMonthly=" + monthlyStr;
		}
		function queryNew() {
			var startTime = $("#startTime").val();
			var endTime = $("#endTime").val();
			window.location.href = "${contextPath}/mvc/r/overload?op=view&startTime=" + encodeURIComponent(startTime) + "&endTime=" + encodeURIComponent(endTime) + "&" + getType() + "&fullScreen=" + $('#fullScreenStr').val();
		}
	</script>
</body>
</html>
