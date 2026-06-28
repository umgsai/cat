<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="contextPath" value="${empty contextPath ? pageContext.request.contextPath : contextPath}" />
<c:set var="domain" value="${empty domain ? '' : domain}" />
<c:set var="hostname" value="${empty hostname ? '' : hostname}" />
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
		.alter-modal { cursor: pointer; color: #428bca; }
	</style>
</head>
<body class="no-skin">
	<c:set var="navbarApplicationDisabled" value="true" scope="request" />
	<c:set var="navbarShowLogin" value="true" scope="request" />
	<jsp:include page="../common/navbar.jsp" />
	<div class="main-container" id="main-container">
		<c:set var="activeReport" value="Alteration" scope="request" />
		<jsp:include page="../common/reportSidebar.jsp" />
		<div class="main-content">
			<div style="padding-top:2px;padding-left:2px;padding-right:8px;">
				<div class="report">
					<div class="text-left"></div>
					<div style="float:left;">
						&nbsp;开始
						<input type="text" id="startTime" style="width:150px;" value="${startTimeText}">
						结束
						<input type="text" id="endTime" style="width:150px;" value="${endTimeText}">
					</div>
					应用名
					<input type="text" name="domain" id="domain" value="${fn:escapeXml(domain)}" style="height:auto" class="input-small">
					机器名
					<input type="text" name="hostname" id="hostname" value="${fn:escapeXml(hostname)}" style="height:auto" class="input-small">
					每分钟显示个数
					<input type="text" name="count" id="count" value="${count}" style="height:auto" class="input-small">
					<input class="btn btn-primary btn-sm" style="margin-bottom:4px;" value="查询" onclick="queryNew()" type="submit">
					<br>
					<div id="label-group">
						<label class="btn btn-info btn-sm"><input type="checkbox" style="margin-bottom:0px;" id="select-all-type">All</label>
						<label class="btn btn-info btn-sm"><input type="checkbox" style="margin-bottom:0px;" class="altType" data-type="puppet">puppet</label>
						<label class="btn btn-info btn-sm"><input type="checkbox" style="margin-bottom:0px;" class="altType" data-type="workflow">workflow</label>
						<label class="btn btn-info btn-sm"><input type="checkbox" style="margin-bottom:0px;" class="altType" data-type="lazyman">lazyman</label>
					</div>
					<br>
					<div id="alt-minutes">
						<c:set var="modalId" value="0" />
						<c:choose>
							<c:when test="${empty alterationMinutes}">
								<h3 class="text-center text-danger">该项目在该时间段内没有变更信息。</h3>
							</c:when>
							<c:otherwise>
								<c:forEach var="minuteEntry" items="${alterationMinutes}">
									<table class="smallTable" style="float:left" border="1">
										<tr><th colspan="2" class="text-danger">${minuteEntry.key}</th></tr>
										<tr><th>项目名</th><th>个</th></tr>
										<c:set var="length" value="${fn:length(minuteEntry.value.alterationDomains)}" />
										<c:forEach var="alterDomain" items="${minuteEntry.value.alterationDomains}" end="${count - 1}">
											<tr>
												<td>
													<c:set var="modalIdText" value="modal${modalId}" />
													<c:set var="modalId" value="${modalId + 1}" />
													<span class="alter-modal" data-toggle="modal" data-target="#${modalIdText}">
														<c:choose>
															<c:when test="${fn:length(alterDomain.name) > 18}">${fn:substring(alterDomain.name, 0, 18)}...</c:when>
															<c:otherwise><c:out value="${alterDomain.name}" /></c:otherwise>
														</c:choose>
													</span>
													<div class="modal fade" id="${modalIdText}" tabindex="-1" role="dialog" aria-hidden="true">
														<div class="modal-dialog" style="width:1100px">
															<div class="modal-content">
																<div class="modal-body">
																	<h4 class="text-danger text-center">项目名：<c:out value="${alterDomain.name}" />&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;变更时间：${minuteEntry.key}</h4>
																	<c:forEach var="alterType" items="${alterDomain.alterationTypes}">
																		<h5 class="text-warning text-center">变更类型：<c:out value="${alterType.key}" /></h5>
																		<table class="table table-striped table-condensed table-hover">
																			<tr class="text-success">
																				<th width="25%">机器名</th>
																				<th width="75%">内容</th>
																			</tr>
																			<c:forEach var="item" items="${alterType.value}">
																				<tr>
																					<td><c:out value="${item.hostname}" /></td>
																					<td>
																						<c:choose>
																							<c:when test="${empty item.url}">
																								<span class="text-primary"><c:out value="${item.title}" /></span>
																							</c:when>
																							<c:otherwise>
																								<a class="hreftip out_url" target="_blank" href="${fn:escapeXml(item.url)}"><c:out value="${item.title}" /></a>
																							</c:otherwise>
																						</c:choose>
																						<br>
																						${item.content}
																					</td>
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
												<td style="text-align:right"><fmt:formatNumber value="${alterDomain.count}" pattern="0" /></td>
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
		function getAltTypeStr() {
			var result = "&altType=";
			$(".altType").filter(function() {
				return $(this).prop("checked");
			}).each(function() {
				result += $(this).data("type") + ",";
			});
			return result;
		}
		function queryNew() {
			var startTime = $("#startTime").val();
			var endTime = $("#endTime").val();
			var domain = $("#domain").val();
			var hostname = $("#hostname").val();
			var count = $("#count").val();
			window.location.href = "${contextPath}/mvc/r/alteration?op=view&domain=" + encodeURIComponent(domain) + "&startTime=" + encodeURIComponent(startTime) + "&endTime=" + encodeURIComponent(endTime) + "&hostname=" + encodeURIComponent(hostname) + "&count=" + encodeURIComponent(count) + getAltTypeStr();
		}
		function dealAllType() {
			var isAllButtonChecked = $("#select-all-type").prop("checked");
			$(".altType").each(function() {
				$(this).prop("checked", isAllButtonChecked);
			});
		}
		function checkAllType() {
			var isAllChecked = true;
			$(".altType").each(function() {
				if (!$(this).prop("checked")) {
					isAllChecked = false;
				}
			});
			$("#select-all-type").prop("checked", isAllChecked);
		}
		$(document).ready(function() {
			var types = '${altType}';
			if (types == null || types == "") {
				$(".altType").each(function() {
					$(this).prop("checked", true);
				});
			} else {
				var strs = types.split(",");
				for (var count in strs) {
					var str = strs[count];
					if (str != null && str != "") {
						$("[data-type='" + str + "']").prop("checked", true);
					}
				}
			}
			checkAllType();
			$("#select-all-type").click(dealAllType);
			$("#label-group").click(checkAllType);
			$('#startTime').datetimepicker({ format:'Y-m-d H:i', step:30, maxDate:0 });
			$('#endTime').datetimepicker({ format:'Y-m-d H:i', step:30, maxDate:0 });
			$(".out_url").each(function() {
				var cur = $(this);
				cur.attr("href", decodeURIComponent(cur.attr("href")));
			});
		});
	</script>
</body>
</html>
