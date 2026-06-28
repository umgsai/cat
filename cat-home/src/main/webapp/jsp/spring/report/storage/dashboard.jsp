<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="contextPath" value="${empty contextPath ? pageContext.request.contextPath : contextPath}" />
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
	<link rel="stylesheet" type="text/css" href="${contextPath}/css/body.css">
	<script src="${contextPath}/assets/js/jquery.min.js"></script>
	<script src="${contextPath}/assets/js/ace-extra.min.js"></script>
	<script src="${contextPath}/assets/js/bootstrap.min.js"></script>
	<script src="${contextPath}/assets/js/jquery-ui.min.js"></script>
	<script src="${contextPath}/assets/js/ace-elements.min.js"></script>
	<script src="${contextPath}/assets/js/ace.min.js"></script>
	<style>
		.center { text-align: center; }
		.left { text-align: left; }
		.smallTable { margin: 4px; }
	</style>
</head>
<body class="no-skin">
	<c:set var="navbarApplicationDisabled" value="true" scope="request" />
	<jsp:include page="../common/navbar.jsp" />
	<div class="main-container" id="main-container">
		<c:set var="activeReport" value="Storage" scope="request" />
		<jsp:include page="../common/reportSidebar.jsp" />
		<div class="main-content">
			<div style="padding-top:2px;padding-left:2px;padding-right:8px;">
				<div class="report">
					<div class="breadcrumbs" id="breadcrumbs">
						<span class="text-danger title">【报表时间】</span><span class="text-success">&nbsp;&nbsp;${reportStart} to ${reportEnd}</span>
						<script type="text/javascript">try { ace.settings.check('breadcrumbs', 'fixed'); } catch(e) {}</script>
					</div>
					<div class="text-center">
						<c:forEach var="item" items="${minutes}">
							<a id="minute${item}" class="btn btn-xs ${item <= maxMinute ? 'btn-info' : 'disabled'}" href="${baseUri}?op=dashboard&type=${encodedType}&domain=${encodedDomain}&date=${date}&minute=${item}&count=${minuteCounts}">${item}</a>
						</c:forEach>
					</div>
					<span>
						<c:forEach var="entry" items="${alertInfos}">
							<table class="smallTable" style="float:left" border="1">
								<tr><th class="text-danger center" colspan="2">${entry.key}</th></tr>
								<c:if test="${empty entry.value.storages}">
									<tr><td><button class="btn btn-app btn-sm radius-4 btn-success" style="height:40px;min-width:130px;width:auto">${storageName}访问正常</button></td></tr>
								</c:if>
								<c:forEach var="storage" items="${entry.value.storages}">
									<c:set var="storageInfo" value="${storage.value}" />
									<c:set var="times" value="${fn:split(entry.key, ':')}" />
									<c:set var="hour" value="${times[0]}" />
									<c:set var="minuteText" value="${times[1]}" />
									<tr>
										<td>
											<c:if test="${storageInfo != null && storageInfo.level > 0}">
												<div class="hide dialog-message" id="dialog-message-${storageInfo.id}-${hour}-${minuteText}">
													<table class="table table-striped table-condensed table-hover table-bordered">
														<thead><tr><td colspan="4" class="center"><h5><strong>${storageName}：[&nbsp;<a href="${baseUri}?domain=${encodedDomain}&id=${storageInfo.id}&ip=All&date=${date}&type=${encodedType}" target="_blank">${storageInfo.id}</a>&nbsp;]&nbsp;&nbsp;时间：<span class="text-danger">${hour}:${minuteText}</span></strong></h5></td></tr></thead>
														<thead><tr><th class="center">机器</th><th class="center">方法</th><th class="center">指标</th><th class="center">内容</th></tr></thead>
														<c:forEach var="machineEntry" items="${storageInfo.machines}">
															<c:forEach var="operationEntry" items="${machineEntry.value.operations}">
																<c:forEach var="targetEntry" items="${operationEntry.value.targets}">
																	<c:forEach var="detail" items="${targetEntry.value.details}">
																		<tr>
																			<td class="center"><a href="${baseUri}?domain=${encodedDomain}&id=${storageInfo.id}&ip=${machineEntry.key}&date=${date}&type=${encodedType}" target="_blank"><c:out value="${machineEntry.key}" /></a></td>
																			<td class="center"><c:out value="${operationEntry.key}" /></td>
																			<td class="center"><c:out value="${targetEntry.key}" /></td>
																			<td><c:out value="${detail.content}" /></td>
																		</tr>
																	</c:forEach>
																</c:forEach>
															</c:forEach>
														</c:forEach>
													</table>
												</div>
											</c:if>
											<c:choose>
												<c:when test="${storageInfo != null && storageInfo.level == 1}">
													<button class="btn btn-app btn-sm radius-4 btn-warning alert-modal" data-id="${storageInfo.id}" data-hour="${hour}" data-minute="${minuteText}" style="height:40px;min-width:130px;width:auto">${storageInfo.id}<span class="label label-inverse arrowed-in">${storageInfo.count}</span></button>
												</c:when>
												<c:when test="${storageInfo != null && storageInfo.level == 2}">
													<button class="btn btn-app btn-sm radius-4 btn-danger alert-modal" data-id="${storageInfo.id}" data-hour="${hour}" data-minute="${minuteText}" style="height:40px;min-width:130px;width:auto">${storageInfo.id}<span class="label label-inverse arrowed-in">${storageInfo.count}</span></button>
												</c:when>
											</c:choose>
										</td>
										<td>
											<c:forEach var="link" items="${links[entry.key][storageInfo.id]}">
												<a href="${link}" target="_blank"><i class="ace-icon fa fa-bolt bigger-200"></i></a>
											</c:forEach>
										</td>
									</tr>
								</c:forEach>
							</table>
						</c:forEach>
					</span>
					<div style="clear:both"></div>
					<table class="table table-hover table-striped table-condensed table-bordered center" style="width:100%">
						<c:if test="${not empty alterations}">
							<tr class="text-success"><th class="center">时间</th><th class="center">${storageName}</th><th class="center">主机名</th><th class="center">IP</th><th class="center">标题</th><th class="left">内容</th><th class="center">状态</th></tr>
							<c:forEach var="alt" items="${alterations}">
								<tr>
									<td><fmt:formatDate value="${alt.date}" pattern="HH:mm:ss" /></td>
									<td><c:out value="${alt.domain}" /></td>
									<td><c:out value="${alt.hostname}" /></td>
									<td><c:out value="${alt.ip}" /></td>
									<td><c:out value="${alt.title}" /></td>
									<td class="left"><c:out value="${alt.content}" /></td>
									<td><c:choose><c:when test="${alt.status == 0}"><button class="btn btn-xs btn-success"><i class="ace-icon glyphicon glyphicon-ok bigger-120 btn-success"></i></button></c:when><c:otherwise><button class="btn btn-xs btn-danger"><i class="ace-icon glyphicon glyphicon-remove bigger-120 btn-danger"></i></button></c:otherwise></c:choose></td>
								</tr>
							</c:forEach>
						</c:if>
					</table>
				</div>
			</div>
		</div>
	</div>
	<script type="text/javascript">
		$(document).ready(function() {
			$('#minute${minute}').addClass('disabled');
			$('.alert-modal').on('click', function(e) {
				e.preventDefault();
				var id = $(this).data('id');
				var hour = $(this).data('hour');
				var minute = $(this).data('minute');
				$('#dialog-message-' + id + '-' + hour + '-' + minute).removeClass('hide').dialog({ width:'auto', modal:true, title_html:true });
			});
		});
	</script>
</body>
</html>
