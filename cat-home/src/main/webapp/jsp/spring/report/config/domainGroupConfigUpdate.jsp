<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="contextPath" value="${empty contextPath ? pageContext.request.contextPath : contextPath}" />
<c:set var="activeConfigMenu" value="domainGroupConfigs" scope="request" />
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
		.domain-group-editor { padding: 5px; }
		.domain-group-editor .group-name { width: 220px; }
		.domain-group-editor .group-ips { width: 95%; }
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
			<div style="padding-top:2px;padding-right:8px;">
				<div class="domain-group-editor">
					<h3 class="text-center text-success">编辑机器分组配置</h3>
					<table class="table table-striped table-condensed" id="content">
						<tr>
							<th width="10%">项目组</th>
							<c:choose>
								<c:when test="${not empty groupDomain.id}">
									<th><input type="text" id="domain" value="${fn:escapeXml(groupDomain.id)}" size="50" readonly /></th>
								</c:when>
								<c:otherwise>
									<th><input type="text" id="domain" value="" size="50" /></th>
								</c:otherwise>
							</c:choose>
							<th width="5%">
								<a href="javascript:addRow();" class="btn btn-primary btn-sm">
									<i class="ace-icon glyphicon glyphicon-plus bigger-120"></i>
								</a>
							</th>
						</tr>
						<c:forEach var="item" items="${groupRows}" varStatus="status">
							<tr id="row_${status.index}">
								<td width="10%">
									<input type="text" class="group group-name" id="group_${status.index}" value="${fn:escapeXml(item.id)}" readonly />
								</td>
								<td>
									<input type="text" name="pars" class="group-ips" id="tag_${status.index}" value="${fn:escapeXml(item.ips)}" placeholder="Enter ip ..." />
								</td>
								<td width="5%">
									<a href="javascript:removeRow(${status.index});" class="btn btn-danger btn-sm">
										<i class="ace-icon fa fa-trash-o bigger-120"></i>
									</a>
								</td>
							</tr>
						</c:forEach>
					</table>
					<input class="btn btn-primary btn-sm" style="margin-left:45%" type="button" value="提交" onclick="submitDomainGroup();" />
				</div>
			</div>
		</div>
	</div>
	<script type="text/javascript">
		function removeRow(row) {
			$('#row_' + row).remove();
		}

		function addRow() {
			var n = $('.group').length + 1;
			var html = '<tr id="row_' + n + '">'
				+ '<td width="10%"><input type="text" class="group group-name" id="group_' + n + '" placeholder="Enter group ..." /></td>'
				+ '<td><input type="text" name="pars" class="group-ips" id="tag_' + n + '" placeholder="Enter ip ..." /></td>'
				+ '<td width="5%"><a href="javascript:removeRow(' + n + ');" class="btn btn-danger btn-sm"><i class="ace-icon fa fa-trash-o bigger-120"></i></a></td>'
				+ '</tr>';
			$('#content').append(html);
		}

		function submitDomainGroup() {
			var domain = {};
			var groups = {};

			domain.id = $('#domain').val();
			domain.groups = groups;
			$('.group').each(function() {
				var name = $.trim($(this).val());

				if (name.length === 0) {
					return;
				}

				var index = $(this).attr('id').split('_')[1];
				var ipstr = $('#tag_' + index).val() || '';
				var ips = [];

				$.each(ipstr.split(','), function(_, ip) {
					ip = $.trim(ip);
					if (ip.length > 0) {
						ips.push(ip);
					}
				});
				groups[name] = { id: name, ips: ips };
			});
			window.location.href = '${contextPath}/mvc/s/config?op=domainGroupConfigSubmit&domain='
				+ encodeURIComponent(domain.id) + '&content=' + encodeURIComponent(JSON.stringify(domain));
		}

		$(document).ready(function() {
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
