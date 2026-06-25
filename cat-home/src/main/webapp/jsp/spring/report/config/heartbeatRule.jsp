<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="contextPath" value="${empty contextPath ? pageContext.request.contextPath : contextPath}" />
<c:set var="activeConfigMenu" value="heartbeatRule" scope="request" />
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
	<style>
		.heartbeat-rule-content { padding-top: 2px; padding-right: 8px; }
	</style>
</head>
<body class="no-skin">
	<c:set var="navbarConfigDisabled" value="true" scope="request" />
	<c:set var="navbarShowLogin" value="true" scope="request" />
	<jsp:include page="../common/navbar.jsp" />
	<div class="main-container" id="main-container">
		<script type="text/javascript">
			try { ace.settings.check('main-container', 'fixed'); } catch(e) {}
		</script>
		<jsp:include page="../common/configSidebar.jsp" />
		<div class="main-content">
			<div id="dialog-message" class="hide">
				<p>你确定要删除吗？(不可恢复)</p>
			</div>
			<div class="heartbeat-rule-content">
				<table class="table table-striped table-condensed table-bordered table-hover">
					<thead>
						<tr>
							<th width="30%">规则id</th>
							<th width="26%">项目配置</th>
							<th width="21%">指标配置</th>
							<th width="8%">是否告警</th>
							<th width="8%">操作
								<a href="${contextPath}/mvc/s/config?op=heartbeatRuleUpdate" class="btn btn-primary btn-xs">
									<i class="ace-icon glyphicon glyphicon-plus bigger-120"></i>
								</a>
							</th>
						</tr>
					</thead>
					<tbody>
						<c:forEach var="item" items="${ruleItems}">
							<tr>
								<td><c:out value="${item.id}" /></td>
								<td><c:out value="${item.productlineText}" /></td>
								<td><c:out value="${item.metricText}" /></td>
								<td>
									<c:choose>
										<c:when test="${item.available == false}">
											<span>否</span>
										</c:when>
										<c:otherwise>
											<span class="text-danger">是</span>
										</c:otherwise>
									</c:choose>
								</td>
								<td>
									<a href="${contextPath}/mvc/s/config?op=heartbeatRuleUpdate&key=${fn:escapeXml(item.id)}" class="btn btn-primary btn-xs">
										<i class="ace-icon fa fa-pencil-square-o bigger-120"></i>
									</a>
									<a href="${contextPath}/mvc/s/config?op=heartbeatRulDelete&key=${fn:escapeXml(item.id)}" class="btn btn-danger btn-xs delete">
										<i class="ace-icon fa fa-trash-o bigger-120"></i>
									</a>
								</td>
							</tr>
						</c:forEach>
					</tbody>
				</table>
			</div>
		</div>
	</div>
	<script type="text/javascript">
		$(document).ready(function() {
			$('.delete').click(function(e) {
				e.preventDefault();
				var anchor = this;
				$('#dialog-message').removeClass('hide').dialog({
					modal: true,
					title: 'CAT提示',
					buttons: [
						{ text: 'Cancel', 'class': 'btn btn-xs', click: function() { $(this).dialog('close'); } },
						{ text: 'OK', 'class': 'btn btn-primary btn-xs', click: function() { window.location.href = anchor.href; } }
					]
				});
			});
			$('#nav_application').click(function() {
				window.location.href = '${contextPath}/mvc/r/t';
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
