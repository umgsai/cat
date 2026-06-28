<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="contextPath" value="${empty contextPath ? pageContext.request.contextPath : contextPath}" />
<c:set var="activeConfigMenu" value="topologyGraphNodeConfigList" scope="request" />
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
	<script src="${contextPath}/js/dependencyConfig.js"></script>
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
			<div style="padding-top:2px;padding-right:8px;">
				<form name="topologyGraphNodeConfigAddSumbit" id="form" method="post" action="${contextPath}/mvc/s/config?op=topologyGraphNodeConfigAddSumbit">
					<h4 class="text-center text-danger" id="state">&nbsp;</h4>
					<h4 class="text-center text-danger">修改拓扑节点配置信息</h4>
					<table class="table table-striped table-condensed">
						<tr>
							<td width="40%" style="text-align:right" class="text-success">节点规则类型</td>
							<td><input id="type" name="type" value="${type}" readonly /></td>
						</tr>
						<tr>
							<td style="text-align:right" class="text-success">项目名称</td>
							<td>
								<c:choose>
									<c:when test="${not empty domain}">
										<input id="id" name="domainConfig.id" value="${domain}" readonly required />
									</c:when>
									<c:otherwise>
										<select style="width:200px;" name="domainConfig.id" id="id">
											<c:forEach var="item" items="${projects}">
												<option value="${item.domain}">${item.domain}</option>
											</c:forEach>
										</select>
									</c:otherwise>
								</c:choose>
							</td>
						</tr>
						<tr>
							<td style="text-align:right" class="text-success">最少访问次数</td>
							<td><input id="minCountThreshold" name="domainConfig.minCountThreshold" value="${domainConfig.minCountThreshold}" required /></td>
						</tr>
						<tr>
							<td style="text-align:right" class="text-success">一分钟异常数warning阈值</td>
							<td><input id="warningThreshold" name="domainConfig.warningThreshold" value="${domainConfig.warningThreshold}" required /></td>
						</tr>
						<tr>
							<td style="text-align:right" class="text-success">一分钟异常数error阈值</td>
							<td><input id="errorThreshold" name="domainConfig.errorThreshold" value="${domainConfig.errorThreshold}" required /></td>
						</tr>
						<c:choose>
							<c:when test="${type ne 'Exception'}">
								<tr>
									<td style="text-align:right" class="text-success">响应时间warning阈值</td>
									<td><input id="warningResponseTime" name="domainConfig.warningResponseTime" value="${domainConfig.warningResponseTime}" required /></td>
								</tr>
								<tr>
									<td style="text-align:right" class="text-success">响应时间error阈值</td>
									<td><input id="errorResponseTime" name="domainConfig.errorResponseTime" value="${domainConfig.errorResponseTime}" required /></td>
								</tr>
							</c:when>
							<c:otherwise>
								<tr style="display:none">
									<td style="text-align:right" class="text-success">响应时间warning阈值</td>
									<td><input id="warningResponseTime" name="domainConfig.warningResponseTime" value="100" required /></td>
								</tr>
								<tr style="display:none">
									<td style="text-align:right" class="text-success">响应时间error阈值</td>
									<td><input id="errorResponseTime" name="domainConfig.errorResponseTime" value="100" required /></td>
								</tr>
							</c:otherwise>
						</c:choose>
						<tr>
							<td>&nbsp;</td>
							<td><input class="btn btn-primary btn-sm" id="addOrUpdateNodeSubmit" type="submit" name="submit" value="提交" /></td>
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
