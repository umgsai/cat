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
	<style>
		.business-add-content { padding-top: 2px; padding-right: 8px; }
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
			<div class="business-add-content">
				<h4 class="text-success text-center">修改业务监控规则</h4>
				<form name="addSubmit" id="form" method="post" action="${contextPath}/mvc/s/business?op=addSubmit&domain=${fn:escapeXml(domain)}">
					<table class="table table-striped table-condensed">
						<tr>
							<td width="20%" style="text-align:right" class="text-success">项目名称</td>
							<td width="20%"><input value="${fn:escapeXml(domain)}" readonly required /></td>
							<td width="25%" style="text-align:right" class="text-success">BusinessKey</td>
							<td width="35%">
								<c:choose>
									<c:when test="${not empty businessItemConfig.id}">
										<input name="businessItemConfig.id" id="businessItemConfig_id" value="${fn:escapeXml(businessItemConfig.id)}" readonly required />
									</c:when>
									<c:otherwise>
										<input name="businessItemConfig.id" id="businessItemConfig_id" value="${fn:escapeXml(businessItemConfig.id)}" required />
									</c:otherwise>
								</c:choose>
							</td>
						</tr>
						<tr>
							<td style="text-align:right" class="text-success">显示标题</td>
							<td><input name="businessItemConfig.title" id="businessItemConfig_title" value="${fn:escapeXml(businessItemConfig.title)}" required /></td>
							<td style="text-align:right" class="text-success">显示顺序（数字）</td>
							<td><input name="businessItemConfig.viewOrder" id="businessItemConfig_viewOrder" value="${businessItemConfig.viewOrder}" required /></td>
						</tr>
						<tr>
							<td style="text-align:right" class="text-success">是否告警</td>
							<td>
								<input type="radio" name="businessItemConfig.alarm" value="true" ${businessItemConfig.alarm ? 'checked' : ''} />是&nbsp;&nbsp;&nbsp;
								<input type="radio" name="businessItemConfig.alarm" value="false" ${businessItemConfig.alarm ? '' : 'checked'} />否
							</td>
							<td style="text-align:right" class="text-success">显示次数曲线</td>
							<td>
								<input type="radio" name="businessItemConfig.showCount" value="true" ${businessItemConfig.showCount ? 'checked' : ''} />是&nbsp;&nbsp;&nbsp;
								<input type="radio" name="businessItemConfig.showCount" value="false" ${businessItemConfig.showCount ? '' : 'checked'} />否
							</td>
						</tr>
						<tr>
							<td style="text-align:right" class="text-success">显示平均曲线</td>
							<td>
								<input type="radio" name="businessItemConfig.showAvg" value="true" ${businessItemConfig.showAvg ? 'checked' : ''} />是&nbsp;&nbsp;&nbsp;
								<input type="radio" name="businessItemConfig.showAvg" value="false" ${businessItemConfig.showAvg ? '' : 'checked'} />否
							</td>
							<td style="text-align:right" class="text-success">显示求和曲线</td>
							<td>
								<input type="radio" name="businessItemConfig.showSum" value="true" ${businessItemConfig.showSum ? 'checked' : ''} />是&nbsp;&nbsp;&nbsp;
								<input type="radio" name="businessItemConfig.showSum" value="false" ${businessItemConfig.showSum ? '' : 'checked'} />否
							</td>
						</tr>
						<tr>
							<td style="text-align:right" class="text-success">是否为敏感数据</td>
							<td colspan="3">
								<input type="radio" name="businessItemConfig.privilege" value="true" ${businessItemConfig.privilege ? 'checked' : ''} />是&nbsp;&nbsp;&nbsp;
								<input type="radio" name="businessItemConfig.privilege" value="false" ${businessItemConfig.privilege ? '' : 'checked'} />否
							</td>
						</tr>
						<tr>
							<td style="text-align:center" colspan="4">
								<input class="btn btn-primary btn-xs" id="addOrUpdateNodeSubmit" type="submit" name="submit" value="提交" />
							</td>
						</tr>
					</table>
				</form>
			</div>
		</div>
	</div>
	<script type="text/javascript">
		$(document).ready(function() {
			$('#nav_application').click(function() { window.location.href = '${contextPath}/mvc/r/t'; });
			$('#nav_config').click(function() { window.location.href = '${contextPath}/mvc/s/config?op=projects'; });
			$('#nav_document').click(function() { window.location.href = '${contextPath}/mvc/r/home?op=view&docName=index'; });
		});
	</script>
</body>
</html>
