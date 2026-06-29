<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="contextPath" value="${empty contextPath ? pageContext.request.contextPath : contextPath}" />
<c:set var="activeConfigMenu" value="projects" scope="request" />
<c:set var="currentDomain" value="${empty domain ? 'cat' : domain}" />
<c:if test="${not empty project.domain}">
	<c:set var="currentDomain" value="${project.domain}" />
</c:if>
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
	<link rel="stylesheet" type="text/css" href="${contextPath}/js/jquery.datetimepicker.css">
	<script src="${contextPath}/assets/js/jquery.min.js"></script>
	<script src="${contextPath}/assets/js/ace-extra.min.js"></script>
	<script src="${contextPath}/assets/js/bootstrap.min.js"></script>
	<script src="${contextPath}/js/highcharts.js"></script>
	<script src="${contextPath}/js/baseGraph.js"></script>
	<script src="${contextPath}/js/jquery.datetimepicker.js"></script>
	<script src="${contextPath}/assets/js/jquery-ui.min.js"></script>
	<script src="${contextPath}/assets/js/jquery.ui.touch-punch.min.js"></script>
	<script src="${contextPath}/assets/js/ace-elements.min.js"></script>
	<script src="${contextPath}/assets/js/ace.min.js"></script>
	<style>
		.input-icon > .ace-icon { z-index: 0; }
		.project-search { float: none; margin-left: 10%; margin-top: 5px; margin-bottom: 12px; width: 860px; padding: 5px; }
		.project-search .input-group { display: flex; align-items: stretch; width: 100%; }
		.project-search .input-icon { display: block; flex: 0 0 300px; }
		.project-search .input-group-btn { display: block; flex: 0 0 auto; width: auto !important; }
		.project-search .input-group-addon { display: block; flex: 0 0 auto; width: auto; max-width: 460px; white-space: nowrap; text-align: left; line-height: 20px; }
		.project-form { padding: 5px; }
		.project-form td:first-child { width: 10%; white-space: nowrap; }
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
			<div style="padding-top:2px;padding-right:8px;">
				<c:choose>
					<c:when test="${projectAdd}">
						<div class="project-form">
							<form name="projectUpdate" id="form" method="get" action="${configUrl}">
								<input type="hidden" name="op" value="updateSubmit" />
								<table class="table table-striped table-condensed">
									<tr>
										<td>CAT上项目名称</td>
										<td><input type="text" class="input-xlarge" name="project.domain" /></td>
										<td style="color:red">注意：建议使用半角英文和半角符号(. -)。</td>
									</tr>
									<tr style="display:none">
										<td>CMDB项目名称</td>
										<td><input type="text" class="input-xlarge" name="project.cmdbDomain" value="default" /></td>
										<td>CMDB中项目统一名称</td>
									</tr>
									<tr style="display:none">
										<td>CMDB项目级别</td>
										<td><input type="text" class="input-xlarge" name="project.level" value="1" /></td>
										<td>CMDB中项目统一级别</td>
									</tr>
									<tr>
										<td>事业部</td>
										<td><input type="text" class="input-xlarge" name="project.bu" /></td>
										<td>所属部门名称</td>
									</tr>
									<tr>
										<td>产品线</td>
										<td><input type="text" class="input-xlarge" name="project.cmdbProductline" /></td>
										<td>所属产品线名称</td>
									</tr>
									<tr>
										<td>负责人</td>
										<td><input type="text" class="input-xlarge" name="project.owner" /></td>
										<td>项目负责人</td>
									</tr>
									<tr>
										<td>项目组邮件</td>
										<td><input type="text" name="project.email" class="input-xxlarge" /></td>
										<td>字段，多个以逗号分隔</td>
									</tr>
									<tr>
										<td>项目组号码</td>
										<td><input type="text" name="project.phone" class="input-xxlarge" /></td>
										<td>字段，多个以逗号分隔</td>
									</tr>
									<tr>
										<td colspan="2" align="center">
											<input class="btn btn-primary btn-sm" type="submit" name="submit" value="提交" />
										</td>
									</tr>
								</table>
							</form>
						</div>
					</c:when>
					<c:otherwise>
							<div class="navbar-header pull-left position project-search">
								<form id="wrap_search" style="margin-bottom:0px;">
									<div class="input-group">
										<span class="input-icon" style="width:300px;">
											<input type="text" placeholder="input domain for search" value="${fn:escapeXml(currentDomain)}" class="search-input search-input form-control ui-autocomplete-input" id="search" autocomplete="off" />
											<i class="ace-icon fa fa-search nav-search-icon"></i>
										</span>
										<span class="input-group-btn" style="width:50px">
											<button class="btn btn-sm btn-primary" type="submit" id="search_go">Go</button>
										</span>
										<span class="input-group-addon">请输入你的项目，默认是cat。找不到你的项目？请点<a href="${contextPath}/mvc/s/config?op=projectAdd"><strong>添加</strong></a></span>
									</div>
								</form>
							</div>
						<br />
						<br />
						<br />
						<div class="project-form">
							<form name="projectUpdate" id="form" method="get" action="${configUrl}">
								<input type="hidden" name="project.id" value="${fn:escapeXml(project.id)}" />
								<input type="hidden" name="project.domain" value="${fn:escapeXml(project.domain)}" />
								<input type="hidden" name="op" value="updateSubmit" />
								<table class="table table-striped table-condensed">
									<tr>
										<td>CAT上项目名称</td>
										<td><c:out value="${project.domain}" /></td>
										<td style="color:red">注意：建议使用半角英文和半角符号(. -)。</td>
									</tr>
									<tr style="display:none">
										<td>CMDB项目名称</td>
										<td><input type="text" class="input-xlarge" name="project.cmdbDomain" value="${fn:escapeXml(project.cmdbDomain)}" /></td>
										<td>CMDB中项目统一名称</td>
									</tr>
									<tr style="display:none">
										<td>CMDB项目级别</td>
										<td><input type="text" class="input-xlarge" name="project.level" value="${fn:escapeXml(project.level)}" /></td>
										<td>CMDB中项目统一级别</td>
									</tr>
									<tr>
										<td>事业部</td>
										<td><input type="text" class="input-xlarge" name="project.bu" value="${fn:escapeXml(project.bu)}" /></td>
										<td>所属部门名称</td>
									</tr>
									<tr>
										<td>产品线</td>
										<td><input type="text" class="input-xlarge" name="project.cmdbProductline" value="${fn:escapeXml(project.cmdbProductline)}" /></td>
										<td>所属产品线名称</td>
									</tr>
									<tr>
										<td>负责人</td>
										<td><input type="text" class="input-xlarge" name="project.owner" value="${fn:escapeXml(project.owner)}" /></td>
										<td>项目负责人</td>
									</tr>
									<tr>
										<td>项目组邮件</td>
										<td><input type="text" name="project.email" class="input-xxlarge" value="${fn:escapeXml(project.email)}" /></td>
										<td>字段，多个以逗号分隔</td>
									</tr>
									<tr>
										<td>项目组号码</td>
										<td><input type="text" name="project.phone" class="input-xxlarge" value="${fn:escapeXml(project.phone)}" /></td>
										<td>字段，多个以逗号分隔</td>
									</tr>
									<tr>
										<td colspan="2" align="center">
											<input class="btn btn-primary btn-sm" type="submit" name="submit" value="更新" />
											&nbsp;
											<a href="${contextPath}/mvc/s/config?op=projectDelete&projectId=${project.id}" class="btn btn-danger btn-sm delete">
												<i class="ace-icon fa fa-trash-o bigger-140"></i>
											</a>
											<h4 class="text-center text-danger" id="state">
												<c:choose>
													<c:when test="${opState eq true}">操作成功</c:when>
													<c:when test="${opState eq false}">操作失败</c:when>
													<c:otherwise>&nbsp;</c:otherwise>
												</c:choose>
											</h4>
										</td>
									</tr>
								</table>
							</form>
						</div>
					</c:otherwise>
				</c:choose>
			</div>
		</div>
	</div>
	<script type="text/javascript">
		$(document).ready(function() {
			$.widget("custom.catcomplete", $.ui.autocomplete, {
				_renderMenu: function(ul, items) {
					var that = this;
					var currentCategory = "";

					$.each(items, function(index, item) {
						if (item.category != currentCategory) {
							ul.append("<li class='ui-autocomplete-category'>" + item.category + "</li>");
							currentCategory = item.category;
						}
						that._renderItemData(ul, item);
					});
				}
			});

			var data = [];
			<c:forEach var="item" items="${projects}">
			data.push({ label: '<c:out value="${item.domain}" />', category: '<c:out value="${item.bu}" /> - <c:out value="${item.cmdbProductline}" />' });
			</c:forEach>
			$('#search').catcomplete({
				delay: 0,
				source: data
			});
			$('#wrap_search').submit(function() {
				window.location.href = '${contextPath}/mvc/s/config?op=projects&domain=' + encodeURIComponent($('#search').val());
				return false;
			});
			$('.delete').click(function(e) {
				e.preventDefault();
				var anchor = this;
				$('#dialog-message').removeClass('hide').dialog({
					modal: true,
					title: 'CAT提示',
					buttons: [
						{
							text: 'Cancel',
							'class': 'btn btn-xs',
							click: function() {
								$(this).dialog('close');
							}
						},
						{
							text: 'OK',
							'class': 'btn btn-primary btn-xs',
							click: function() {
								window.location.href = anchor.href;
							}
						}
					]
				});
			});
			var ct = getCookie('ct');
			if (ct !== '') {
				var realName = ct.split('|');
				var name = decodeURIComponent(realName[0].replace(/^"|"$/g, ''));
				$('#loginInfo').html('欢迎，' + name);
			}
			$('#nav_application').click(function() {
				window.location.href = '${contextPath}/mvc/r/t';
			});
			$('#nav_config').click(function() {
				window.location.href = '${contextPath}/mvc/s/config?op=projects';
			});
			$('#nav_document').click(function() {
				window.location.href = '${contextPath}/mvc/r/home?op=view&docName=index';
			});
			setTimeout(function() {
				$('#state').html('&nbsp;');
			}, 3000);
		});
		function getCookie(name) {
			var cookies = document.cookie.split('; ');

			for (var i = 0; i < cookies.length; i++) {
				var item = cookies[i].split('=');

				if (item[0] === name) {
					return item[1] || '';
				}
			}
			return '';
		}
	</script>
</body>
</html>
