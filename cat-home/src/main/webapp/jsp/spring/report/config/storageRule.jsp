<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="contextPath" value="${empty contextPath ? pageContext.request.contextPath : contextPath}" />
<c:set var="activeConfigMenu" value="storageRule" scope="request" />
<c:set var="storageType" value="${empty type ? 'SQL' : type}" />
<c:set var="storageName" value="数据库" />
<c:if test="${storageType eq 'Cache'}"><c:set var="storageName" value="缓存" /></c:if>
<c:if test="${storageType eq 'RPC'}"><c:set var="storageName" value="服务" /></c:if>
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
		.storage-rule-content { padding-top: 2px; padding-right: 8px; }
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
			<div class="storage-rule-content">
				<table class="table table-striped table-condensed table-bordered table-hover" id="contents" width="100%">
					<thead>
						<tr>
							<th width="20%" class="center">${storageName}</th>
							<th width="20%" class="center">机器</th>
							<th width="20%" class="center">方法</th>
							<th width="20%" class="center">监控项</th>
							<th width="10%" class="center">与条件</th>
							<th width="10%" class="center">
								操作
								<a href="${contextPath}/mvc/s/config?op=storageRuleUpdate&type=${fn:escapeXml(storageType)}" class="btn btn-primary btn-xs">
									<i class="ace-icon glyphicon glyphicon-plus bigger-120"></i>
								</a>
							</th>
						</tr>
					</thead>
					<tbody>
						<c:forEach var="item" items="${rules}">
							<c:set var="conditions" value="${fn:split(item.id, ';')}" />
							<tr class="center">
								<td><c:out value="${conditions[0]}" /></td>
								<td><c:out value="${conditions[1]}" /></td>
								<td><c:out value="${conditions[2]}" /></td>
								<td>
									<c:if test="${conditions[3] eq 'error'}">错误数</c:if>
									<c:if test="${conditions[3] eq 'errorPercent'}">错误率</c:if>
									<c:if test="${conditions[3] eq 'avg'}">响应时间</c:if>
								</td>
								<td>
									<c:choose>
										<c:when test="${conditions[4] eq 'true'}">
											<i class="ace-icon glyphicon glyphicon-ok bigger-120 btn-success"></i>
										</c:when>
										<c:otherwise>
											<i class="ace-icon glyphicon glyphicon-remove bigger-120 btn-danger"></i>
										</c:otherwise>
									</c:choose>
								</td>
								<td>
									<a href="${contextPath}/mvc/s/config?op=storageRuleUpdate&ruleId=${fn:escapeXml(item.id)}&type=${fn:escapeXml(storageType)}" class="btn btn-primary btn-xs">
										<i class="ace-icon fa fa-pencil-square-o bigger-120"></i>
									</a>
									<a href="${contextPath}/mvc/s/config?op=storageRuleDelete&ruleId=${fn:escapeXml(item.id)}&type=${fn:escapeXml(storageType)}" class="btn btn-danger btn-xs delete">
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
