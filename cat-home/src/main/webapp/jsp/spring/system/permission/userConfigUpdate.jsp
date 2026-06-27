<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="contextPath" value="${empty contextPath ? pageContext.request.contextPath : contextPath}" />
<c:set var="activeConfigMenu" value="userUpdate" scope="request" />
<!doctype html>
<html lang="en">
<head>
	<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
	<meta charset="utf-8">
	<title>CAT</title>
	<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0">
	<link rel="stylesheet" type="text/css" href="${contextPath}/assets/css/bootstrap.min.css">
	<link rel="stylesheet" type="text/css" href="${contextPath}/assets/css/font-awesome.min.css">
	<link rel="stylesheet" type="text/css" href="${contextPath}/assets/css/ace-fonts.css">
	<link rel="stylesheet" type="text/css" href="${contextPath}/assets/css/ace.min.css" id="main-ace-style">
	<link rel="stylesheet" type="text/css" href="${contextPath}/assets/css/ace-skins.min.css">
	<link rel="stylesheet" type="text/css" href="${contextPath}/assets/css/ace-rtl.min.css">
	<link rel="stylesheet" type="text/css" href="${contextPath}/css/body.css">
	<script src="${contextPath}/assets/js/jquery.min.js"></script>
	<script src="${contextPath}/assets/js/ace-extra.min.js"></script>
	<script src="${contextPath}/assets/js/bootstrap.min.js"></script>
	<script src="${contextPath}/assets/js/ace-elements.min.js"></script>
	<script src="${contextPath}/assets/js/ace.min.js"></script>
	<script src="${contextPath}/assets/js/editor/ace.js"></script>
	<script src="${contextPath}/js/editor.js"></script>
	<style>
		.editor { min-height: 520px; width: 100%; border: 1px solid #ddd; }
	</style>
</head>
<body class="no-skin">
	<c:set var="navbarConfigDisabled" value="true" scope="request" />
	<c:set var="navbarShowLogin" value="true" scope="request" />
	<jsp:include page="../../report/common/navbar.jsp" />
	<div class="main-container" id="main-container">
		<jsp:include page="../../report/common/configSidebar.jsp" />
		<div class="main-content">
			<div style="padding-top:2px;padding-right:8px;">
				<form name="user" id="form" method="post" action="${permissionUrl}?op=user">
					<table class="table table-striped table-condensed table-hover">
						<tr>
							<td>
								<input id="content" name="content" value="" type="hidden" />
								<div id="editor" class="editor">${content}</div>
							</td>
						</tr>
						<tr>
							<td style="text-align:center">
								<input class="btn btn-primary" type="submit" name="submit" value="提交" />
							</td>
						</tr>
					</table>
				</form>
				<h4 class="text-center text-danger" id="state">&nbsp;</h4>
			</div>
		</div>
	</div>
	<script type="text/javascript">
		$(document).ready(function() {
			var state = '${opState}';
			if (state == 'Success') {
				$('#state').html('操作成功');
			} else if (state == 'Fail') {
				$('#state').html('操作失败');
			}
			setInterval(function() { $('#state').html('&nbsp;'); }, 3000);
			$('#nav_application').click(function() { window.location.href = '${contextPath}/mvc/r/t'; });
			$('#nav_config').click(function() { window.location.href = '${contextPath}/mvc/s/config?op=projects'; });
			$('#nav_document').click(function() { window.location.href = '${contextPath}/mvc/r/home?op=view&docName=index'; });
		});
	</script>
</body>
</html>
