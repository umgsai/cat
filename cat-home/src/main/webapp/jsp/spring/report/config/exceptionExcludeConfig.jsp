<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
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
	<script src="${contextPath}/js/jquery.validate.min.js"></script>
	<script src="${contextPath}/js/editor.js"></script>
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
			<div class="exception-content">
				<h4 class="text-center text-danger" id="state">&nbsp;</h4>
				<h4 class="text-center text-danger">修改异常过滤配置信息</h4>
				<form name="exceptionConfig" id="form" method="post" action="${contextPath}/mvc/s/config?op=exceptionExcludeUpdateSubmit">
					<table class="table table-striped table-condensed table-hover">
						<tr>
							<td style="text-align:right" class="text-success" width="20%">项目名称</td>
							<td>
								<div class="navbar-header pull-left position"><div class="input-group">
									<input name="exceptionExclude.domain" id="search_domain" size="60" type="text" class="search-input form-control ui-autocomplete-input" placeholder="input domain for search" autocomplete="off" required />
								</div></div><span class="text-danger">&nbsp;&nbsp;*</span>
							</td>
						</tr>
						<tr>
							<td style="text-align:right" class="text-success" width="20%">异常名称</td>
							<td width="80%">
								<div class="navbar-header pull-left position"><div class="input-group">
									<input name="exceptionExclude.name" id="search_exception" size="60" type="text" class="search-input form-control ui-autocomplete-input" placeholder="input exception for search" autocomplete="off" required />
								</div></div><span class="text-danger">&nbsp;&nbsp;*</span>
							</td>
						</tr>
						<tr>
							<td colspan="2"><input class="btn btn-primary" style="margin-left:30%" id="addOrUpdateExceptionConfigSubmit" type="submit" name="submit" value="提交" /></td>
						</tr>
					</table>
				</form>
			</div>
		</div>
	</div>
	<script type="text/javascript">
		$(document).ready(function() {
			$('#alert_config').addClass('active open');
			$('#exception').addClass('active');
			$('#nav_application').click(function() { window.location.href = '${contextPath}/mvc/r/t'; });
			$('#nav_config').click(function() { window.location.href = '${contextPath}/mvc/s/config?op=projects'; });
			$('#nav_document').click(function() { window.location.href = '${contextPath}/mvc/r/home?op=view&docName=index'; });
		});
	</script>
</body>
</html>
