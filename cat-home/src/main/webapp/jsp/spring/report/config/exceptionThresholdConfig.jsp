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
				<h4 class="text-center text-danger">修改异常阈值配置信息</h4>
				<form name="exceptionConfig" id="form" method="post" action="${contextPath}/mvc/s/config?op=exceptionThresholdUpdateSubmit">
					<table class="table table-striped table-condensed table-hover">
						<tr>
							<td style="text-align:right" class="text-success" width="20%">项目名称</td>
							<td>
								<c:choose>
									<c:when test="${not empty exceptionLimit.domain}">
										<input name="exceptionLimit.domain" size="50" value="${fn:escapeXml(exceptionLimit.domain)}" readonly required />
									</c:when>
									<c:otherwise>
										<div class="navbar-header pull-left position"><div class="input-group">
											<input name="exceptionLimit.domain" id="search_domain" size="60" type="text" class="search-input form-control ui-autocomplete-input" placeholder="input domain for search" autocomplete="off" required />
										</div></div><span class="text-danger">&nbsp;&nbsp;*</span>
									</c:otherwise>
								</c:choose>
							</td>
						</tr>
						<tr>
							<td style="text-align:right" class="text-success" width="20%">异常名称</td>
							<td>
								<c:choose>
									<c:when test="${not empty exceptionLimit.name}">
										<input name="exceptionLimit.name" size="50" value="${fn:escapeXml(exceptionLimit.name)}" readonly required />
									</c:when>
									<c:otherwise>
										<div class="navbar-header pull-left position"><div class="input-group">
											<input name="exceptionLimit.name" id="search_exception" size="60" type="text" class="search-input form-control ui-autocomplete-input" placeholder="input exception for search" autocomplete="off" required />
										</div></div><span class="text-danger">&nbsp;&nbsp;*</span>
									</c:otherwise>
								</c:choose>
							</td>
						</tr>
						<tr>
							<td style="text-align:right" class="text-success" width="20%">warning阈值</td>
							<td><input id="warningThreshold" name="exceptionLimit.warning" value="${exceptionLimit.warning}" required /><span class="text-danger">&nbsp;&nbsp;*</span>（仅支持数字）</td>
						</tr>
						<tr>
							<td style="text-align:right" class="text-success" width="20%">error阈值</td>
							<td><input id="errorThreshold" name="exceptionLimit.error" value="${exceptionLimit.error}" required /><span class="text-danger">&nbsp;&nbsp;*</span>（仅支持数字）</td>
						</tr>
						<tr>
							<td style="text-align:right" class="text-success" width="20%">是否告警</td>
							<td>
								<c:choose>
									<c:when test="${exceptionLimit.available}">
										<input type="radio" name="exceptionLimit.available" value="true" checked />是&nbsp;&nbsp;&nbsp;
										<input type="radio" name="exceptionLimit.available" value="false" />否
									</c:when>
									<c:otherwise>
										<input type="radio" name="exceptionLimit.available" value="true" />是&nbsp;&nbsp;&nbsp;
										<input type="radio" name="exceptionLimit.available" value="false" checked />否
									</c:otherwise>
								</c:choose>
								<span class="text-danger">&nbsp;&nbsp;*</span>
							</td>
						</tr>
						<tr>
							<td colspan="2" style="text-align:center"><input class="btn btn-primary" id="addOrUpdateExceptionConfigSubmit" type="submit" name="submit" value="提交" /></td>
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
