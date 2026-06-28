<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="contextPath" value="${empty contextPath ? pageContext.request.contextPath : contextPath}" />
<c:set var="activeConfigMenu" value="businessConfig" scope="request" />
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
	<script src="${contextPath}/js/alarm.js"></script>
	<script src="${contextPath}/js/dependencyConfig.js"></script>
	<style>
		.business-alert-content { padding-top: 2px; padding-right: 8px; }
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
			<div class="business-alert-content">
				<form method="post">
					<h3 class="text-center text-success">编辑应用监控规则</h3>
					<div class="config" style="display:none">
						<strong class="text-success">规则ID</strong>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<input id="ruleId" type="text" value="${fn:escapeXml(id)}" /> <span class="text-danger">String，唯一性</span>
					</div>
					<div id="metrics" class="config">
						<h4 class="btn btn-success btn-xs">匹配对象<i class="icon-plus icon-white"></i></h4>
						<div id="metricItem" class="metric config">
							监控类型：
							<label class="checkbox inline"><input name="metricType" value="COUNT" id="COUNT" type="radio">count</label>
							<label class="checkbox inline"><input name="metricType" value="AVG" id="AVG" type="radio">avg</label>
							<label class="checkbox inline"><input name="metricType" value="SUM" id="SUM" type="radio">sum</label>
						</div>
					</div>
					${content}
					<div style="text-align:center">
						<input class="btn btn-primary btn-sm" id="ruleSubmitButton" type="text" name="submit" value="提交">
					</div>
				</form>
			</div>
		</div>
	</div>
	<script type="text/javascript">
		$(document).ready(function() {
			initRuleConfigs();
			var attributes = '${fn:escapeXml(attributes)}';
			if (attributes !== null && attributes !== '' && document.getElementById(attributes)) {
				document.getElementById(attributes).checked = true;
			}
			$(document).delegate('#ruleSubmitButton', 'click', function() {
				var metrics = $('input:radio[name="metricType"]:checked').val();
				var configStr = generateConfigsJsonString();
				window.location.href = '${contextPath}/mvc/s/business?op=alertRuleAddSubmit&content='
						+ encodeURIComponent(configStr) + '&key=' + encodeURIComponent('${fn:escapeXml(key)}')
						+ '&attributes=' + encodeURIComponent(metrics) + '&domain=' + encodeURIComponent('${fn:escapeXml(domain)}');
			});
			$('#nav_application').click(function() { window.location.href = '${contextPath}/mvc/r/t'; });
			$('#nav_config').click(function() { window.location.href = '${contextPath}/mvc/s/config?op=projects'; });
			$('#nav_document').click(function() { window.location.href = '${contextPath}/mvc/r/home?op=view&docName=index'; });
		});
	</script>
</body>
</html>
