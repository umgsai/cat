<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="contextPath" value="${empty contextPath ? pageContext.request.contextPath : contextPath}" />
<c:set var="activeConfigMenu" value="routerConfigUpdate" scope="request" />
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
	<script src="${contextPath}/assets/js/editor/ace.js"></script>
	<script src="${contextPath}/js/jquery.validate.min.js"></script>
	<script src="${contextPath}/js/editor.js"></script>
	<style>
		.router-config-content { padding-top: 2px; padding-right: 8px; }
		.router-config-content .editor { min-height: 520px; border: 1px solid #d5d5d5; }
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
			<div class="router-config-content">
				<form name="routerConfigUpdate" id="form" method="post" action="${contextPath}/mvc/s/config?op=routerConfigUpdate">
					<table class="table table-striped table-condensed table-hover">
						<tr>
							<td>
								<input id="content" name="content" value="" type="hidden" />
								<div id="editor" class="editor">${content}</div>
							</td>
						</tr>
						<tr>
							<td style="text-align:center">
								<input class="btn btn-primary" type="submit" name="submit" id="submit" value="提交" />
								&nbsp; &nbsp;&nbsp;&nbsp;
								<a href="${contextPath}/s/router?op=build" class="btn btn-primary" id="routerRebuild" target="_blank">重算路由</a>
							</td>
						</tr>
					</table>
				</form>
				<h4 class="text-center text-danger" id="state">&nbsp;</h4>
			</div>
		</div>
	</div>
	<div id="rebuild-router-message" class="hide">确认重算路由？</div>
	<script type="text/javascript">
		$("#routerRebuild").on('click', function(e) {
			e.preventDefault();
			var anchor = this;
			$("#rebuild-router-message").removeClass('hide').dialog({
				modal: true,
				title: "<div class='widget-header widget-header-small'><h4 class='smaller'><i class='ace-icon fa fa-check'></i>CAT提示</h4></div>",
				title_html: true,
				buttons: [{
					text: "Cancel",
					"class": "btn btn-xs",
					click: function() { $(this).dialog("close"); }
				}, {
					text: "OK",
					"class": "btn btn-primary btn-xs",
					click: function() {
						$(this).dialog("close");
						window.open(anchor.href);
					}
				}]
			});
		});
		$(document).ready(function() {
			$('#nav_application').click(function() {
				window.location.href = '${contextPath}/mvc/r/t';
			});
			$('#nav_config').click(function() {
				window.location.href = '${contextPath}/mvc/s/config?op=projects';
			});
			$('#nav_document').click(function() {
				window.location.href = '${contextPath}/mvc/r/home?op=view&docName=index';
			});
			setTimeout(function() {
				$('#state').html('&nbsp;');
			}, 3000);
		});
	</script>
</body>
</html>
