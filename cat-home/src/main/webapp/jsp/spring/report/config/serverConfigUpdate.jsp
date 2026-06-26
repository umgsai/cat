<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="contextPath" value="${empty contextPath ? pageContext.request.contextPath : contextPath}" />
<c:set var="activeConfigMenu" value="serverConfigUpdate" scope="request" />
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
	<script src="${contextPath}/assets/js/editor/ace.js"></script>
	<script src="${contextPath}/js/jquery.validate.min.js"></script>
	<script src="${contextPath}/js/editor.js"></script>
	<style>
		.server-config-content { padding-top: 2px; padding-right: 8px; }
		.server-config-tip { padding: 1em; }
		.server-config-tip-container {
			padding: 0.5em;
			border: 1px solid #ffeeba;
			background-color: #fff3cd;
			color: #856404;
		}
		.server-config-tip-container h4 {
			font-size: 1.5em;
			padding: 0;
			margin: 0 0 0.3em;
		}
		.server-config-tip-container p {
			margin-bottom: 0.3em;
		}
		.server-config-content .editor {
			min-height: 420px;
			border: 1px solid #d5d5d5;
		}
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
			<div class="server-config-content">
				<div class="server-config-tip">
					<div class="server-config-tip-container">
						<h4>配置说明：</h4>
						<p>* local-mode : 定义服务是否为本地模式（开发模式），在生产环境时，设置为false,启动远程监听模式。默认为 false;</p>
						<p>* hdfs-machine : 定义是否启用HDFS存储方式，默认为 false；</p>
						<p>* job-machine : 定义当前服务是否为报告工作机（开启生成汇总报告和统计报告的任务，只需要一台服务机开启此功能），默认为false；</p>
						<p>* alarm-machine : 定义当前服务是否为报警机（开启各类报警监听，只需要一台服务机开启此功能），默认为false；</p>
						<p>* storage : 定义数据存储配置信息</p>
						<p>* local-report-storage-time : 定义本地报告存放时长，单位为（天）</p>
						<p>* local-logivew-storage-time : 定义本地日志存放时长，单位为（天）</p>
						<p>* local-base-dir : 定义本地数据存储目录</p>
						<p>* hdfs : 定义HDFS配置信息，便于直接登录系统</p>
						<p>* server-uri : 定义HDFS服务地址</p>
						<p>* remote-servers : 定义HTTP服务列表，（远程监听端同步更新服务端信息即取此值）</p>
					</div>
				</div>
				<form name="serverConfigUpdate" id="form" method="post" action="${contextPath}/mvc/s/config?op=serverConfigUpdate">
					<table class="table table-striped table-condensed table-hover">
						<tr>
							<td>
								<input id="content" name="content" value="" type="hidden" />
								<div id="editor" class="editor">${content}</div>
							</td>
						</tr>
						<tr>
							<td style="text-align:center">
								<input class="btn btn-primary" type="submit" name="submit" id="submit" value="提交" />
							</td>
						</tr>
					</table>
				</form>
				<c:choose>
					<c:when test="${param.submit ne null && opState eq true}">
						<h4 class="text-center text-danger" id="state">操作成功</h4>
					</c:when>
					<c:when test="${param.submit ne null}">
						<h4 class="text-center text-danger" id="state">操作失败</h4>
					</c:when>
					<c:otherwise>
						<h4 class="text-center text-danger" id="state">&nbsp;</h4>
					</c:otherwise>
				</c:choose>
			</div>
		</div>
	</div>
	<script type="text/javascript">
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
			setTimeout(function() {
				$('#state').html('&nbsp;');
			}, 3000);
		});
	</script>
</body>
</html>
