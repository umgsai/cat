<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="contextPath" value="${empty contextPath ? pageContext.request.contextPath : contextPath}" />
<c:set var="activeConfigMenu" value="exception" scope="request" />
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
		.exception-content { padding-top: 2px; padding-right: 8px; }
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
			<div class="exception-content">
				<div class="tabbable tabs-left" id="content">
					<ul class="nav nav-tabs">
						<li id="tab-threshold" class="text-right"><a href="#tabContent-threshold" data-toggle="tab">异常阈值</a></li>
						<li id="tab-exclude" class="text-right"><a href="#tabContent-exclude" data-toggle="tab">异常过滤</a></li>
					</ul>
					<div class="tab-content">
						<div class="tab-pane" id="tabContent-threshold">
							<h5 class="text-center text-danger">异常阈值配置</h5>
							<table class="table table-striped table-condensed table-bordered table-hover" id="content-threshold" width="100%">
								<thead>
									<tr>
										<th width="25%">域名</th>
										<th width="37%">异常名称</th>
										<th width="12%">Warning阈值</th>
										<th width="10%">Error阈值</th>
										<th width="8%">是否告警</th>
										<th width="8%">操作 <a href="${contextPath}/mvc/s/config?op=exceptionThresholdAdd" class="btn btn-primary btn-xs"><i class="ace-icon glyphicon glyphicon-plus bigger-120"></i></a></th>
									</tr>
								</thead>
								<tbody>
									<c:forEach var="item" items="${exceptionLimits}">
										<tr>
											<td><c:out value="${item.domain}" /></td>
											<td><c:out value="${item.name}" /></td>
											<td><c:out value="${item.warning}" /></td>
											<td><c:out value="${item.error}" /></td>
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
												<a href="${contextPath}/mvc/s/config?op=exceptionThresholdUpdate&domain=${item.domain}&exception=${item.name}" class="btn btn-primary btn-xs"><i class="ace-icon fa fa-pencil-square-o bigger-120"></i></a>
												<a href="${contextPath}/mvc/s/config?op=exceptionThresholdDelete&domain=${item.domain}&exception=${item.name}&type=threshold" class="btn btn-danger btn-xs delete"><i class="ace-icon fa fa-trash-o bigger-120"></i></a>
											</td>
										</tr>
									</c:forEach>
								</tbody>
							</table>
						</div>
						<div class="tab-pane" id="tabContent-exclude">
							<h5 class="text-center text-danger">异常过滤配置</h5>
							<table class="table table-striped table-condensed table-bordered table-hover" id="contents-exclude" width="100%">
								<thead>
									<tr>
										<th width="35%">域名</th>
										<th width="60%">异常名称</th>
										<th width="5%"><a href="${contextPath}/mvc/s/config?op=exceptionExcludeAdd" class="btn btn-primary btn-xs"><i class="ace-icon glyphicon glyphicon-plus bigger-120"></i></a></th>
									</tr>
								</thead>
								<tbody>
									<c:forEach var="item" items="${exceptionExcludes}">
										<tr>
											<td><c:out value="${item.domain}" /></td>
											<td><c:out value="${item.name}" /></td>
											<td><a href="${contextPath}/mvc/s/config?op=exceptionExcludeDelete&domain=${item.domain}&exception=${item.name}&type=exclude" class="btn btn-danger btn-xs delete"><i class="ace-icon fa fa-trash-o bigger-120"></i></a></td>
										</tr>
									</c:forEach>
								</tbody>
							</table>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
	<script type="text/javascript">
		$(document).ready(function() {
			$('#alert_config').addClass('active open');
			$('#exception').addClass('active');

			var type = '${param.type}';
			if (!type) {
				type = 'threshold';
			}
			$('#tab-' + type).addClass('active');
			$('#tabContent-' + type).addClass('active');

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
