<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
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
			<div id="dialog-message" class="hide">
				<p>你确定要删除吗？不可恢复。</p>
			</div>
			<div style="padding-top:2px;padding-right:8px;">
				<c:if test="${opState eq true}">
					<h4 class="text-center text-danger" id="state">操作成功</h4>
				</c:if>
				<c:if test="${opState eq false}">
					<h4 class="text-center text-danger" id="state">操作失败</h4>
				</c:if>
				<div class="tabbable tabs-left" id="content">
					<ul class="nav nav-tabs">
						<c:forEach var="item" items="${config.nodeConfigs}">
							<c:set var="key" value="${item.key}" />
							<li id="tab-${fn:escapeXml(key)}" class="text-right">
								<a href="#tabContent-${fn:escapeXml(key)}" data-toggle="tab"><c:out value="${key}" /></a>
							</li>
						</c:forEach>
					</ul>
					<div class="tab-content">
						<c:forEach var="item" items="${config.nodeConfigs}">
							<c:set var="key" value="${item.key}" />
							<c:set var="value" value="${item.value}" />
							<div class="tab-pane" id="tabContent-${fn:escapeXml(key)}">
								<h4 class="text-center text-danger">拓扑图节点配置信息：<c:out value="${key}" /></h4>
								<table class="table table-striped table-condensed table-bordered table-hover">
									<thead>
										<tr>
											<th>项目</th>
											<th>最小的个数</th>
											<th>异常Warning阀值</th>
											<th>异常Error阀值</th>
											<th>响应时间Warning阀值</th>
											<th>响应时间Error阀值</th>
											<th width="8%">操作
												<a href="${contextPath}/mvc/s/config?op=topologyGraphNodeConfigAdd&type=${fn:escapeXml(key)}" class="btn btn-primary btn-xs">
													<i class="ace-icon glyphicon glyphicon-plus bigger-120"></i>
												</a>
											</th>
										</tr>
									</thead>
									<tbody>
										<tr class="text-danger">
											<td><h5>默认值</h5></td>
											<td><h5 class="text-right"><c:out value="${value.defaultMinCountThreshold}" /></h5></td>
											<td><h5 class="text-right"><c:out value="${value.defaultWarningThreshold}" /></h5></td>
											<td><h5 class="text-right"><c:out value="${value.defaultErrorThreshold}" /></h5></td>
											<c:choose>
												<c:when test="${key eq 'Exception'}">
													<td><h5 class="text-right">--</h5></td>
													<td><h5 class="text-right">--</h5></td>
												</c:when>
												<c:otherwise>
													<td><h5 class="text-right"><c:out value="${value.defaultWarningResponseTime}" /></h5></td>
													<td><h5 class="text-right"><c:out value="${value.defaultErrorResponseTime}" /></h5></td>
												</c:otherwise>
											</c:choose>
											<td style="text-align:center;vertical-align:middle;">
												<a href="${contextPath}/mvc/s/config?op=topologyGraphNodeConfigAdd&type=${fn:escapeXml(key)}&domain=ALL" class="btn btn-primary btn-xs">
													<i class="ace-icon fa fa-pencil-square-o bigger-120"></i>
												</a>
											</td>
										</tr>
										<c:forEach var="domainConfig" items="${value.domainConfigs}">
											<c:set var="temp" value="${domainConfig.value}" />
											<tr>
												<td><c:out value="${temp.id}" /></td>
												<td style="text-align:right"><c:out value="${temp.minCountThreshold}" /></td>
												<td style="text-align:right"><c:out value="${temp.warningThreshold}" /></td>
												<td style="text-align:right"><c:out value="${temp.errorThreshold}" /></td>
												<c:choose>
													<c:when test="${key eq 'Exception'}">
														<td style="text-align:right">--</td>
														<td style="text-align:right">--</td>
													</c:when>
													<c:otherwise>
														<td style="text-align:right"><c:out value="${temp.warningResponseTime}" /></td>
														<td style="text-align:right"><c:out value="${temp.errorResponseTime}" /></td>
													</c:otherwise>
												</c:choose>
												<td>
													<a href="${contextPath}/mvc/s/config?op=topologyGraphNodeConfigAdd&type=${fn:escapeXml(key)}&domain=${fn:escapeXml(temp.id)}" class="btn btn-primary btn-xs">
														<i class="ace-icon fa fa-pencil-square-o bigger-120"></i>
													</a>
													<a href="${contextPath}/mvc/s/config?op=topologyGraphNodeConfigDelete&type=${fn:escapeXml(key)}&domain=${fn:escapeXml(temp.id)}" class="btn btn-danger btn-xs delete">
														<i class="ace-icon fa fa-trash-o bigger-120"></i>
													</a>
												</td>
											</tr>
										</c:forEach>
									</tbody>
								</table>
							</div>
						</c:forEach>
					</div>
				</div>
			</div>
		</div>
	</div>
	<script type="text/javascript">
		$(document).ready(function() {
			var type = '${fn:escapeXml(type)}';
			if (type == '') {
				type = 'URL';
			}
			$('#tab-' + type).addClass('active');
			$('#tabContent-' + type).addClass('active');
			$('.delete').click(function(e) {
				e.preventDefault();
				var anchor = this;
				$('#dialog-message').removeClass('hide').dialog({
					modal: true,
					title: 'CAT提示',
					buttons: [
						{ text: 'Cancel', 'class': 'btn btn-xs', click: function() { $(this).dialog('close'); } },
						{ text: 'OK', 'class': 'btn btn-primary btn-xs', click: function() { window.location.href = anchor.href; } }
					]
				});
			});
			$('#nav_application').click(function() { window.location.href = '${contextPath}/mvc/r/t'; });
			$('#nav_config').click(function() { window.location.href = '${contextPath}/mvc/s/config?op=projects'; });
			$('#nav_document').click(function() { window.location.href = '${contextPath}/mvc/r/home?op=view&docName=index'; });
			setTimeout(function() { $('#state').html('&nbsp;'); }, 3000);
		});
	</script>
</body>
</html>
