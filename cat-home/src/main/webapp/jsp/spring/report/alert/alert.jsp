<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="contextPath" value="${empty contextPath ? pageContext.request.contextPath : contextPath}" />
<c:set var="domain" value="${empty domain ? '' : domain}" />
<c:set var="count" value="${empty count ? 10 : count}" />
<fmt:formatDate var="startTimeText" value="${startTime}" pattern="yyyy-MM-dd HH:mm" />
<fmt:formatDate var="endTimeText" value="${endTime}" pattern="yyyy-MM-dd HH:mm" />
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
	<style>
		.smallTable { margin: 4px; }
		.alert-modal { cursor: pointer; }
	</style>
</head>
<body class="no-skin">
	<c:set var="navbarApplicationDisabled" value="true" scope="request" />
	<c:set var="navbarShowLogin" value="true" scope="request" />
	<jsp:include page="../common/navbar.jsp" />
	<div class="main-container" id="main-container">
		<c:set var="activeReport" value="Alert" scope="request" />
		<jsp:include page="../common/reportSidebar.jsp" />
		<div class="main-content">
			<div style="padding-top:2px;padding-left:2px;padding-right:8px;">
				<div class="report">
					<div id="queryBar">
						<div style="float:left;">
							&nbsp;开始
							<input type="text" id="startTime" style="width:150px;" value="${startTimeText}">
							结束
							<input type="text" id="endTime" style="width:150px;" value="${endTimeText}">
							&nbsp;&nbsp;项目
							<input type="text" name="domain" id="domain" value="${fn:escapeXml(domain)}" style="height:auto" class="input-small">
							&nbsp;&nbsp;每分钟显示个数
							<input type="text" id="count" value="${count}" style="width:100px;height:auto" class="input-small">
							<input class="btn btn-primary btn-sm" style="margin-bottom:4px;" value="查询" onclick="queryNew()" type="submit">
						</div>
						<div style="float:left;margin-left:6px;" id="type-group">
							<label class="btn btn-info btn-sm"><input id="select-all" type="checkbox"> All</label>
							<label class="btn btn-info btn-sm"><input class="type" type="checkbox" value="business"> 业务告警</label>
							<label class="btn btn-info btn-sm"><input class="type" type="checkbox" value="network"> 网络告警</label>
							<label class="btn btn-info btn-sm"><input class="type" type="checkbox" value="system"> 系统告警</label>
							<label class="btn btn-info btn-sm"><input class="type" type="checkbox" value="exception"> 异常告警</label>
							<label class="btn btn-info btn-sm"><input class="type" type="checkbox" value="heartbeat"> 心跳告警</label>
							<label class="btn btn-info btn-sm"><input class="type" type="checkbox" value="thirdParty"> 第三方告警</label>
							<label class="btn btn-info btn-sm"><input class="type" type="checkbox" value="frontEnd"> 前端告警</label>
							<label class="btn btn-info btn-sm"><input class="type" type="checkbox" value="app"> App告警</label>
							<label class="btn btn-info btn-sm"><input class="type" type="checkbox" value="web"> Web告警</label>
							<label class="btn btn-info btn-sm"><input class="type" type="checkbox" value="zabbix"> Zabbix告警</label>
							<label class="btn btn-info btn-sm"><input class="type" type="checkbox" value="database"> DB告警</label>
							<label class="btn btn-info btn-sm"><input class="type" type="checkbox" value="transaction"> Transaction告警</label>
						</div>
					</div>
					<br><br>
					<div id="alert-minutes">
						<br><br>
						<c:set var="modalId" value="0" />
						<c:choose>
							<c:when test="${empty alertMinutes}">
								<h3 class="text-center text-danger">该项目在该时间段内状态正常，没有告警信息。</h3>
							</c:when>
							<c:otherwise>
								<c:forEach var="minuteEntry" items="${alertMinutes}">
									<table class="smallTable" style="float:left" border="1">
										<tr><th colspan="2" class="text-danger">${minuteEntry.key}</th></tr>
										<tr><th>项目名</th><th>个</th></tr>
										<c:set var="length" value="${fn:length(minuteEntry.value.alertDomains)}" />
										<c:forEach var="alertDomain" items="${minuteEntry.value.alertDomains}" end="${count - 1}">
											<tr>
												<td style="background-color:red;color:white;">
													<c:set var="modalIdText" value="modal${modalId}" />
													<c:set var="modalId" value="${modalId + 1}" />
													<span data-id="${modalIdText}" class="alert-modal">
														<c:choose>
															<c:when test="${fn:length(alertDomain.name) > 30}">${fn:substring(alertDomain.name, 0, 30)}...</c:when>
															<c:otherwise><c:out value="${alertDomain.name}" /></c:otherwise>
														</c:choose>
													</span>
													<div class="modal fade" id="${modalIdText}" tabindex="-1" role="dialog" aria-hidden="true">
														<div class="modal-dialog" style="width:1100px">
															<div class="modal-content">
																<div class="modal-body">
																	<h4 class="text-danger text-center">项目：<c:out value="${alertDomain.name}" />&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;告警时间：${minuteEntry.key}</h4>
																	<c:forEach var="alertCategory" items="${alertDomain.alertCategories}">
																		<h5 class="text-warning text-center">告警类型：<c:out value="${alertCategory.key}" /></h5>
																		<table class="table table-striped table-condensed table-hover">
																			<tr class="text-success">
																				<th width="8%">级别</th>
																				<th width="72%">内容</th>
																			</tr>
																			<c:forEach var="alert" items="${alertCategory.value}">
																				<tr>
																					<td><c:out value="${alert.type}" /></td>
																					<td><span class="text-primary"><c:out value="${alert.metric}" /></span><br>${alert.content}</td>
																				</tr>
																			</c:forEach>
																		</table>
																	</c:forEach>
																</div>
																<div class="modal-footer">
																	<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
																</div>
															</div>
														</div>
													</div>
												</td>
												<td style="background-color:red;color:white;text-align:right"><fmt:formatNumber value="${alertDomain.count}" pattern="0" /></td>
											</tr>
										</c:forEach>
										<c:if test="${length lt count}">
											<c:forEach begin="1" end="${count - length}">
												<tr><td>&nbsp;</td><td>&nbsp;</td></tr>
											</c:forEach>
										</c:if>
									</table>
								</c:forEach>
							</c:otherwise>
						</c:choose>
					</div>
				</div>
			</div>
		</div>
	</div>
	<script type="text/javascript">
		function checkIfAllChecked() {
			var isAllChecked = true;
			$('.type').each(function() {
				if ($(this).prop('checked') == false) {
					isAllChecked = false;
				}
			});
			$('#select-all').prop("checked", isAllChecked);
		}
		function initType(rawStr) {
			if (rawStr == null || rawStr == "") {
				$(".type").each(function() {
					$(this).prop("checked", true);
				});
			} else {
				var strs = rawStr.split(",");
				for (var count in strs) {
					var str = strs[count];
					if (str != null && str != "") {
						$("input[value='" + str + "']").prop("checked", true);
					}
				}
			}
		}
		function getType() {
			var typeStr = "";
			$(".type").filter(function() {
				return $(this).prop("checked");
			}).each(function() {
				typeStr += $(this).val() + ",";
			});
			return typeStr;
		}
		function queryNew() {
			var startTime = $("#startTime").val();
			var endTime = $("#endTime").val();
			var domain = $("#domain").val();
			var count = $("#count").val();
			window.location.href = "${contextPath}/mvc/r/alert?op=view&domain=" + encodeURIComponent(domain) + "&startTime=" + encodeURIComponent(startTime) + "&endTime=" + encodeURIComponent(endTime) + "&fullScreen=${fullScreen}&alertType=" + encodeURIComponent(getType()) + "&count=" + encodeURIComponent(count);
		}
		$(document).ready(function() {
			initType("${alertType}");
			$(".alert-modal").click(function() {
				var targetId = $(this).data("id");
				$("#" + targetId).modal();
			});
			checkIfAllChecked();
			$('#type-group').click(checkIfAllChecked);
			$("#select-all").click(function() {
				var originVal = $(this).prop("checked");
				$(".type").each(function() {
					$(this).prop("checked", originVal);
				});
			});
			$('#startTime').datetimepicker({ format:'Y-m-d H:i', step:30, maxDate:0 });
			$('#endTime').datetimepicker({ format:'Y-m-d H:i', step:30, maxDate:0 });
		});
	</script>
</body>
</html>
