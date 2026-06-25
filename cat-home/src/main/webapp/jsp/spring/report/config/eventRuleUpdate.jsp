<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="contextPath" value="${empty contextPath ? pageContext.request.contextPath : contextPath}" />
<c:set var="activeConfigMenu" value="eventRule" scope="request" />
<c:set var="conditions" value="${fn:split(ruleId, ';')}" />
<c:set var="domainValue" value="${conditions[0]}" />
<c:set var="typeValue" value="${conditions[1]}" />
<c:set var="nameValue" value="${conditions[2]}" />
<c:set var="monitorValue" value="${conditions[3]}" />
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
		.event-rule-content { padding-top: 2px; padding-right: 8px; }
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
			<div class="event-rule-content">
				<h3 class="text-center text-success">编辑Event监控规则</h3>
				<form name="eventRuleUpdate" id="form" method="post">
					<table style="width:100%" class="table table-striped table-condensed">
						<tr>
							<td>
								&nbsp;&nbsp;项目&nbsp;&nbsp;<input name="domain" id="domain" value="${fn:escapeXml(domainValue)}" />
								&nbsp;&nbsp;Type&nbsp;&nbsp;<input name="type" id="type" value="${fn:escapeXml(typeValue)}" />
								&nbsp;&nbsp;Name&nbsp;&nbsp;<input name="name" id="name" value="${fn:escapeXml(nameValue)}" />（默认为All）
								&nbsp;&nbsp;监控项&nbsp;&nbsp;
								<select name="monitor" id="monitor" style="width:200px;">
									<option value="count">执行次数</option>
									<option value="failRatio">失败率</option>
								</select>
								&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;是否告警&nbsp;&nbsp;
								<c:choose>
									<c:when test="${available}">
										<input type="radio" name="event.available" value="true" checked />是&nbsp;&nbsp;&nbsp;
										<input type="radio" name="event.available" value="false" />否
									</c:when>
									<c:otherwise>
										<input type="radio" name="event.available" value="true" />是&nbsp;&nbsp;&nbsp;
										<input type="radio" name="event.available" value="false" checked />否
									</c:otherwise>
								</c:choose>
							</td>
						</tr>
						<tr><th>${content}</th></tr>
						<tr>
							<td style="text-align:center" colspan="2">
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
			var domain = $('#domain').val().trim();
			if (domain === 'undefined' || domain === '') {
				if ($('#errorMessage').length === 0) {
					$('#domain').after($('<span class="text-danger" id="errorMessage">  该字段不能为空</span>'));
				}
				return;
			}
			var type = $('#type').val().trim();
			if (type === 'undefined' || type === '') {
				if ($('#errorMessage').length === 0) {
					$('#type').after($('<span class="text-danger" id="errorMessage">  该字段不能为空</span>'));
				}
				return;
			}
			var name = $('#name').val().trim();
			if (name === 'undefined' || name === '') {
				name = 'All';
				$('#name').val('All');
			}
			var available = $('input[name="event.available"]:checked').val();
			var monitor = $('#monitor').val();
			var id = domain + ';' + type + ';' + name + ';' + monitor;
			window.location.href = '${contextPath}/mvc/s/config?op=eventRuleSubmit&configs='
					+ encodeURIComponent(configStr) + '&ruleId=' + encodeURIComponent(id)
					+ '&available=' + encodeURIComponent(available);
		}

		$(document).ready(function() {
			initRuleConfigs(['DescVal', 'DescPer', 'AscVal', 'AscPer']);
			var ruleId = '${fn:escapeXml(ruleId)}';
			if (ruleId.length > 0) {
				document.getElementById('domain').disabled = true;
				document.getElementById('type').disabled = true;
				document.getElementById('name').disabled = true;
				document.getElementById('monitor').disabled = true;
				$('#monitor').val(ruleId.split(';')[3]);
			}
			var name = $('#name').val().trim();
			if (name === '' || name.length === 0) {
				$('#name').val('All');
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
