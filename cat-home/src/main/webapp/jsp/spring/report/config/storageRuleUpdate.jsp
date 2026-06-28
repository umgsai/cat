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
<c:set var="conditions" value="${fn:split(ruleId, ';')}" />
<c:set var="nameValue" value="${conditions[0]}" />
<c:set var="machineValue" value="${conditions[1]}" />
<c:set var="methodValue" value="${conditions[2]}" />
<c:set var="targetValue" value="${conditions[3]}" />
<c:set var="andValue" value="${conditions[4]}" />
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
			<div class="storage-rule-content">
				<h3 class="text-center text-success">编辑${storageName}监控规则</h3>
				<form name="storageRuleUpdate" id="form" method="post">
					<table style="width:100%" class="table table-striped table-condensed">
						<tr>
							<td>名字&nbsp;&nbsp;<input name="name" id="name" value="${fn:escapeXml(nameValue)}" /></td>
							<td>机器&nbsp;&nbsp;<input name="machine" id="machine" value="${fn:escapeXml(machineValue)}" /></td>
							<td>方法&nbsp;&nbsp;<input name="method" id="method" value="${fn:escapeXml(methodValue)}" /></td>
							<td>监控项&nbsp;&nbsp;
								<select name="target" id="target" style="width:200px;">
									<option value="avg">响应时间</option>
									<option value="errorPercent">错误率</option>
									<option value="error">错误数</option>
								</select>
							</td>
							<td>&nbsp;&nbsp;与条件&nbsp;&nbsp;
								<select name="and" id="and" style="width:200px;">
									<option value="false">否</option>
									<option value="true">是</option>
								</select>
							</td>
						</tr>
						<tr><th colspan="6">${content}</th></tr>
						<tr>
							<td style="text-align:center" colspan="6">
								<input class="btn btn-primary btn-sm" id="ruleSubmitButton" type="text" name="submit" value="提交" />
							</td>
						</tr>
					</table>
				</form>
			</div>
		</div>
	</div>
	<script type="text/javascript">
		function update() {
			var configStr = generateConfigsJsonString();
			var name = $('#name').val().trim();
			if (name === 'undefined' || name === '') {
				if ($('#errorMessage').length === 0) {
					$('#name').after($('<span class="text-danger" id="errorMessage">  该字段不能为空</span>'));
				}
				return;
			}
			var machine = $('#machine').val().trim();
			if (machine === 'undefined' || machine === '') {
				if ($('#errorMessage').length === 0) {
					$('#machine').after($('<span class="text-danger" id="errorMessage">  该字段不能为空</span>'));
				}
				return;
			}
			var method = $('#method').val().trim();
			if (method === 'undefined' || method === '') {
				if ($('#errorMessage').length === 0) {
					$('#method').after($('<span class="text-danger" id="errorMessage">  该字段不能为空</span>'));
				}
				return;
			}
			var target = $('#target').val();
			var andStr = $('#and').val();
			var id = name + ';' + machine + ';' + method + ';' + target + ';' + andStr;

			window.location.href = '${contextPath}/mvc/s/config?op=storageRuleSubmit&configs='
					+ encodeURIComponent(configStr) + '&type=${fn:escapeXml(storageType)}&ruleId='
					+ encodeURIComponent(id);
		}

		$(document).ready(function() {
			initRuleConfigs(['DescVal', 'DescPer', 'AscVal', 'AscPer']);
			var ruleId = '${fn:escapeXml(ruleId)}';
			if (ruleId.length > 0) {
				document.getElementById('name').disabled = true;
				document.getElementById('machine').disabled = true;
				document.getElementById('method').disabled = true;
				document.getElementById('target').disabled = true;
				document.getElementById('and').disabled = true;
				var conditions = ruleId.split(';');
				$('#target').val(conditions[3]);
				$('#and').val(conditions[4]);
			}
			if ($('#name').val().trim() === '') {
				$('#name').val('*');
			}
			if ($('#machine').val().trim() === '') {
				$('#machine').val('*');
			}
			$(document).delegate('#ruleSubmitButton', 'click', function() {
				update();
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
