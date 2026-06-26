<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="contextPath" value="${empty contextPath ? pageContext.request.contextPath : contextPath}" />
<c:set var="activeConfigMenu" value="heartbeatRule" scope="request" />
<c:set var="ruleKey" value="${empty ruleId ? param.key : ruleId}" />
<c:set var="conditions" value="${fn:split(ruleKey, ';')}" />
<c:set var="domainValue" value="${conditions[0]}" />
<c:set var="metricValue" value="${conditions[1]}" />
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
	<script src="${contextPath}/js/dependencyConfig.js"></script>
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
			<div class="heartbeat-rule-content">
				<div id="heartbeat-rule-data"
					 data-config-header="${fn:escapeXml(configHeader)}"
					 data-rule-key="${fn:escapeXml(ruleKey)}"></div>
				<h3 class="text-center text-success">编辑心跳告警规则</h3>
				<form name="heartbeatRuleUpdate" id="form" method="post">
					<table style="width:100%" class="table table-striped table-condensed">
						<tr>
							<td>
								&nbsp;&nbsp;项目&nbsp;&nbsp;<input name="domain" id="domain" value="${fn:escapeXml(domainValue)}" />
								&nbsp;&nbsp;指标&nbsp;&nbsp;
								<select name="metric" id="metric" style="width:220px;">
									<c:forEach var="metric" items="${heartbeatExtensionMetrics}">
										<option value="${metric}" <c:if test="${metric eq metricValue}">selected</c:if>>${metric}</option>
									</c:forEach>
								</select>
								&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;是否告警&nbsp;&nbsp;
								<c:choose>
									<c:when test="${available}">
										<input type="radio" name="heartbeat.available" value="true" checked />是&nbsp;&nbsp;&nbsp;
										<input type="radio" name="heartbeat.available" value="false" />否
									</c:when>
									<c:otherwise>
										<input type="radio" name="heartbeat.available" value="true" />是&nbsp;&nbsp;&nbsp;
										<input type="radio" name="heartbeat.available" value="false" checked />否
									</c:otherwise>
								</c:choose>
							</td>
						</tr>
						<tr>
							<th>${content}</th>
						</tr>
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
		function drawMetricItems(metricsStr, newMetric) {
			var metrics = null;

			if (metricsStr == undefined || metricsStr == "") {
				return;
			}

			try {
				metrics = JSON.parse(metricsStr);
			} catch (e) {
				alert("读取规则错误，请刷新重试或联系管理员");
				return;
			}

			if (metrics != undefined) {
				for (count in metrics) {
					var metric = metrics[count];
					var productlineText = metric["productText"];
					var metricText = metric["metricItemText"];

					if (count > 0) {
						addMetricHeader(newMetric.clone());
					}
					var metricForm = $(".metric").last();
					if (productlineText) {
						metricForm.find(".productlineText").val(productlineText);
					}
					if (metricText) {
						metricForm.find(".metricText").val(metricText);
					}
				}
			}
		}

		function generateMetricsJsonString() {
			var metricLength = $(".metric").length;
			if (metricLength > 0) {
				var metricList = [];
				$(".metric").each(function () {
					var metric = {};
					var hasPro = false;
					var productLineText = $(this).find(".productlineText").val();
					var metricText = $(this).find(".metricText").val();

					if (productLineText != "") {
						metric["productText"] = productLineText;
						hasPro = true;
					}
					if (metricText != "") {
						metric["metricItemText"] = metricText;
						hasPro = true;
					}

					if (hasPro) {
						metricList.push(metric);
					}
				});
				if (metricList.length > 0) {
					return JSON.stringify(metricList);
				}
				return "";
			}
		}

		function addMetricHeader(newMetric) {
			$("#metrics").append(newMetric.clone());
		}

		function update() {
			var configStr = generateConfigsJsonString();
			var metrics = generateMetricsJsonString();
			var domain = $('#domain').val().trim();
			if (domain === 'undefined' || domain === '') {
				if ($('#errorMessage').length === 0) {
					$('#domain').after($('<span class="text-danger" id="errorMessage">  该字段不能为空</span>'));
				}
				return;
			}
			var available = $('input[name="heartbeat.available"]:checked').val();
			var metric = $('#metric').val();
			var ruleId = '${fn:escapeXml(ruleKey)}';
			if (ruleId.length === 0) {
				ruleId = domain + ';' + metric;
			}
			window.location.href = '${contextPath}/mvc/s/config?op=heartbeatRuleSubmit&configs='
					+ encodeURIComponent(configStr) + '&ruleId=' + encodeURIComponent(ruleId)
					+ '&metrics=' + encodeURIComponent(metrics) + '&available=' + encodeURIComponent(available);
		}

		$(document).ready(function() {
			var heartbeatRuleData = document.getElementById('heartbeat-rule-data');
			var configHeader = heartbeatRuleData.getAttribute('data-config-header');
			var ruleKey = heartbeatRuleData.getAttribute('data-rule-key');
			initRuleConfigs(['DescVal', 'DescPer', 'AscVal', 'AscPer']);
			var newMetric = $('#metricItem').clone();
			if (ruleKey.length > 0) {
				document.getElementById('domain').disabled = true;
				$('#metric').val(ruleKey.split(';')[1]);
			} else if ($('#metric option').length > 0) {
				$('#metric').val($('#metric option:first').val());
			}
			drawMetricItems(configHeader, newMetric);
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
