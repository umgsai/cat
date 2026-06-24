<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="contextPath" value="${empty contextPath ? pageContext.request.contextPath : contextPath}" />
<div id="navbar" class="navbar navbar-default">
	<script type="text/javascript">
		try { ace.settings.check('navbar', 'fixed'); } catch(e) {}
	</script>
	<div class="navbar-container" id="navbar-container">
		<button type="button" class="navbar-toggle menu-toggler pull-left" id="menu-toggler">
			<span class="sr-only">Toggle sidebar</span>
			<span class="icon-bar"></span>
			<span class="icon-bar"></span>
			<span class="icon-bar"></span>
		</button>
		<div class="navbar-header pull-left">
			<i class="navbar-brand">
				<span>CAT</span>
				<small style="font-size:65%">(Central Application Tracking)</small>
				<button class="btn btn-success btn-sm ${navbarApplicationDisabled ? 'disabled' : ''}" id="nav_application">
					<i class="ace-icon fa fa-signal"></i>Application
				</button>
				<button class="btn btn-inverse btn-sm ${navbarConfigDisabled ? 'disabled' : ''}" id="nav_config">
					<i class="ace-icon fa fa-cogs"></i>Configs
				</button>
				<button class="btn btn-yellow btn-sm ${navbarDocumentDisabled ? 'disabled' : ''}" id="nav_document">
					<i class="ace-icon fa fa-cogs"></i>Documents
				</button>
			</i>
		</div>
		<div class="navbar-buttons navbar-header pull-right" role="navigation">
			<ul class="nav ace-nav" style="height:auto;">
				<li class="light-blue">
					<a href="${contextPath}/mvc/r/home?op=view&docName=index">
						<i class="ace-icon glyphicon glyphicon-star"></i>
						<span>Star</span>
					</a>
				</li>
				<c:if test="${navbarShowLogin}">
					<li class="light-blue">
						<a data-toggle="dropdown" href="#" class="dropdown-toggle">
							<span class="user-info" style="max-width:200px">
								<span id="loginInfo"></span>
							</span>
						</a>
					</li>
				</c:if>
			</ul>
		</div>
	</div>
</div>
