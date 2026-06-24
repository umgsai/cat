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
	<link rel="stylesheet" type="text/css" href="${contextPath}/assets/css/select2.css">
	<script src="${contextPath}/assets/js/jquery.min.js"></script>
	<script src="${contextPath}/assets/js/ace-extra.min.js"></script>
	<script src="${contextPath}/assets/js/bootstrap.min.js"></script>
	<script src="${contextPath}/assets/js/jquery-ui.min.js"></script>
	<script src="${contextPath}/assets/js/jquery.ui.touch-punch.min.js"></script>
	<script src="${contextPath}/assets/js/ace-elements.min.js"></script>
	<script src="${contextPath}/assets/js/ace.min.js"></script>
	<script src="${contextPath}/js/jquery.validate.min.js"></script>
	<script src="${contextPath}/js/alarm.js"></script>
	<script src="${contextPath}/js/select2.min.js"></script>
	<script src="${contextPath}/js/jquery.multiple.select.js"></script>
	<style>
		.business-content { padding-top: 2px; padding-right: 8px; }
		.business-content table th h5 { margin: 6px 0; }
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
			<div id="dialog-message" class="hide">
				<p>你确定要删除吗？不可恢复。</p>
			</div>
			<div class="business-content">
				<form id="wrap_search">
					<table align="center">
						<tr>
							<th>
								<div class="input-group" style="float:left;">
									<span class="input-group-addon">Domain</span>
									<span class="input-icon" style="width:250px;">
										<input type="text" class="search-input search-input form-control ui-autocomplete-input" id="domain" autocomplete="on" value="${fn:escapeXml(payload.domain)}" />
										<i class="ace-icon fa fa-search nav-search-icon"></i>
									</span>
								</div>
								<input class="btn btn-primary btn-sm" value="查询" onclick="query()" type="submit" />
							</th>
						</tr>
					</table>
				</form>
				<h4 class="text-center text-danger">业务大盘标签会默认进行基线告警</h4>
				<h4 class="text-center text-danger" id="state">&nbsp;</h4>
				<table class="table table-striped table-condensed table-bordered table-hover">
					<tr class="text-success">
						<th width="9%"><h5 class="text-center">项目</h5></th>
						<th width="4%"><h5 class="text-center">显示顺序</h5></th>
						<th width="4%"><h5 class="text-center">敏感数据</h5></th>
						<th width="4%"><h5 class="text-center">是否告警</h5></th>
						<th width="12%"><h5 class="text-center">BusinessKey</h5></th>
						<th width="16%"><h5 class="text-center">标题</h5></th>
						<th width="18%"><h5 class="text-center">标签</h5></th>
						<th width="9%"><h5 class="text-center">次数</h5></th>
						<th width="9%"><h5 class="text-center">平均值</h5></th>
						<th width="9%"><h5 class="text-center">总和</h5></th>
						<th width="13%"><h5 class="text-center">操作&nbsp;&nbsp;<a class="btn update btn-primary btn-xs" href="${contextPath}/mvc/s/business?op=customAdd&domain=${payload.domain}">新增</a></h5></th>
					</tr>
					<c:forEach var="config" items="${model.configs}">
						<tr>
							<td><c:out value="${payload.domain}" /></td>
							<td><c:out value="${config.viewOrder}" /></td>
							<td>
								<c:choose>
									<c:when test="${config.privilege}"><span class="text-danger">是</span></c:when>
									<c:otherwise><span>否</span></c:otherwise>
								</c:choose>
							</td>
							<td>
								<c:choose>
									<c:when test="${config.alarm}"><span class="text-danger">是</span></c:when>
									<c:otherwise><span>否</span></c:otherwise>
								</c:choose>
							</td>
							<td style="word-wrap:break-word;word-break:break-all;"><c:out value="${config.id}" /></td>
							<td style="word-wrap:break-word;word-break:break-all;"><c:out value="${config.title}" /></td>
							<td>
								<c:forEach var="tag" items="${model.tags[config.id]}">
									<span class="label label-info"><c:out value="${tag}" /></span>&nbsp;
								</c:forEach>
							</td>
							<td align="right">
								<c:if test="${config.showCount}"><span class="dashboard">&nbsp;&nbsp;&nbsp;&nbsp;</span></c:if>&nbsp;&nbsp;&nbsp;&nbsp;
								<a href="${contextPath}/mvc/s/business?op=alertRuleAdd&key=${config.id}&domain=${payload.domain}&attributes=COUNT" class="btn btn-primary btn-xs">告警</a>
							</td>
							<td align="right">
								<c:if test="${config.showAvg}"><span class="dashboard">&nbsp;&nbsp;&nbsp;&nbsp;</span></c:if>&nbsp;&nbsp;&nbsp;&nbsp;
								<a href="${contextPath}/mvc/s/business?op=alertRuleAdd&key=${config.id}&domain=${payload.domain}&attributes=AVG" class="btn btn-primary btn-xs">告警</a>
							</td>
							<td align="right">
								<c:if test="${config.showSum}"><span class="dashboard">&nbsp;&nbsp;&nbsp;&nbsp;</span></c:if>&nbsp;&nbsp;&nbsp;&nbsp;
								<a href="${contextPath}/mvc/s/business?op=alertRuleAdd&key=${config.id}&domain=${payload.domain}&attributes=SUM" class="btn btn-primary btn-xs">告警</a>
							</td>
							<td style="text-align:center;white-space:nowrap">
								<a href="${contextPath}/mvc/s/business?op=add&key=${config.id}&domain=${payload.domain}" class="btn btn-primary btn-xs"><i class="ace-icon fa fa-pencil-square-o bigger-120"></i></a>
								<a href="${contextPath}/mvc/s/business?op=delete&key=${config.id}&domain=${payload.domain}" class="btn btn-danger btn-xs delete"><i class="ace-icon fa fa-trash-o bigger-120"></i></a>
							</td>
						</tr>
					</c:forEach>
					<c:forEach var="config" items="${model.customConfigs}">
						<tr>
							<td><c:out value="${payload.domain}" /></td>
							<td><c:out value="${config.viewOrder}" /></td>
							<td>
								<c:choose>
									<c:when test="${config.privilege}"><span class="text-danger">是</span></c:when>
									<c:otherwise><span>否</span></c:otherwise>
								</c:choose>
							</td>
							<td>
								<c:choose>
									<c:when test="${config.alarm}"><span class="text-danger">是</span></c:when>
									<c:otherwise><span>否</span></c:otherwise>
								</c:choose>
							</td>
							<td style="word-wrap:break-word;word-break:break-all;"><c:out value="${config.id}" /></td>
							<td style="word-wrap:break-word;word-break:break-all;"><c:out value="${config.title}" /></td>
							<td>
								<c:forEach var="tag" items="${model.tags[config.id]}">
									<span class="label label-info"><c:out value="${tag}" /></span>&nbsp;
								</c:forEach>
							</td>
							<td align="center"></td>
							<td align="center">
								<span class="dashboard">&nbsp;&nbsp;&nbsp;&nbsp;</span>
								<a href="${contextPath}/mvc/s/business?op=alertRuleAdd&key=${config.id}&domain=${payload.domain}&attributes=AVG" class="btn btn-primary btn-xs">告警</a>
							</td>
							<td align="center"></td>
							<td style="text-align:center;white-space:nowrap">
								<a href="${contextPath}/mvc/s/business?op=customAdd&key=${config.id}&domain=${payload.domain}" class="btn btn-primary btn-xs"><i class="ace-icon fa fa-pencil-square-o bigger-120"></i></a>
								<a href="${contextPath}/mvc/s/business?op=customDelete&key=${config.id}&domain=${payload.domain}" class="btn btn-danger btn-xs delete"><i class="ace-icon fa fa-trash-o bigger-120"></i></a>
							</td>
						</tr>
					</c:forEach>
				</table>
			</div>
		</div>
	</div>
	<script type="text/javascript">
		$(document).ready(function() {
			var data = [];
			<c:forEach var="item" items="${model.domains}">
				data.push({ label: '${fn:escapeXml(item)}' });
			</c:forEach>
			$('#domain').autocomplete({ delay: 0, source: data });
			$('#wrap_search').submit(function() {
				query();
				return false;
			});
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

		function query() {
			window.location.href = '${contextPath}/mvc/s/business?op=list&domain=' + encodeURIComponent($('#domain').val());
		}
	</script>
</body>
</html>
