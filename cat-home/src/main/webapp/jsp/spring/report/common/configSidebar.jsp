<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="contextPath" value="${empty contextPath ? pageContext.request.contextPath : contextPath}" />
<c:set var="projectConfigActive" value="${activeConfigMenu eq 'projects' || activeConfigMenu eq 'domainGroupConfigs'}" />
<c:set var="applicationConfigActive" value="${activeConfigMenu eq 'businessConfig' || activeConfigMenu eq 'businessTag' || activeConfigMenu eq 'displayPolicy' || activeConfigMenu eq 'storageGroupConfigUpdate' || activeConfigMenu eq 'allReportConfig'}" />
<c:set var="alertConfigActive" value="${activeConfigMenu eq 'transactionRule' || activeConfigMenu eq 'eventRule' || activeConfigMenu eq 'heartbeatRule'}" />
<c:set var="overallConfigActive" value="${activeConfigMenu eq 'alertPolicy' || activeConfigMenu eq 'alertDefaultReceivers' || activeConfigMenu eq 'alertSenderConfig' || activeConfigMenu eq 'serverConfigUpdate' || activeConfigMenu eq 'serverFilterConfigUpdate' || activeConfigMenu eq 'sampleConfigUpdate' || activeConfigMenu eq 'routerConfigUpdate' || activeConfigMenu eq 'reportReloadConfigUpdate' || activeConfigMenu eq 'resourceUpdate' || activeConfigMenu eq 'userUpdate'}" />
<div id="sidebar" class="sidebar responsive">
	<script type="text/javascript">
		try { ace.settings.check('sidebar', 'fixed'); } catch(e) {}
	</script>
	<ul class="nav nav-list" style="top: 0px;">
		<li id="projects_config" class="${projectConfigActive ? 'hsub active open' : 'hsub'}">
			<a href="${contextPath}/mvc/s/config?op=projects" class="dropdown-toggle">
				<i class="menu-icon fa fa-cogs"></i>
				<span class="menu-text">项目配置</span>
				<b class="arrow fa fa-angle-down"></b>
			</a>
			<b class="arrow"></b>
			<ul class="submenu">
				<li id="projects" class="${activeConfigMenu eq 'projects' ? 'active' : ''}">
					<a href="${contextPath}/mvc/s/config?op=projects">
						<i class="menu-icon fa fa-caret-right"></i>项目基本信息
					</a>
					<b class="arrow"></b>
				</li>
				<li id="domainGroupConfigUpdate" class="${activeConfigMenu eq 'domainGroupConfigs' ? 'active' : ''}">
					<a href="${contextPath}/mvc/s/config?op=domainGroupConfigs">
						<i class="menu-icon fa fa-caret-right"></i>机器分组配置
					</a>
					<b class="arrow"></b>
				</li>
			</ul>
		</li>
		<li id="application_config" class="${applicationConfigActive ? 'hsub active open' : 'hsub'}">
			<a href="${contextPath}/mvc/s/config?op=metricConfigList" class="dropdown-toggle">
				<i class="menu-icon fa fa-cloud"></i>
				<span class="menu-text">应用监控配置</span>
				<b class="arrow fa fa-angle-down"></b>
			</a>
			<b class="arrow"></b>
			<ul class="submenu">
				<li id="businessConfig" class="${activeConfigMenu eq 'businessConfig' ? 'active' : ''}">
					<a href="${contextPath}/mvc/s/business?op=list">
						<i class="menu-icon fa fa-caret-right"></i>业务监控配置
					</a>
					<b class="arrow"></b>
				</li>
				<li id="businessTag" class="${activeConfigMenu eq 'businessTag' ? 'active' : ''}">
					<a href="${contextPath}/mvc/s/business?op=tagConfig">
						<i class="menu-icon fa fa-caret-right"></i>业务标签配置
					</a>
					<b class="arrow"></b>
				</li>
				<li id="displayPolicy" class="${activeConfigMenu eq 'displayPolicy' ? 'active' : ''}">
					<a href="${contextPath}/mvc/s/config?op=displayPolicy">
						<i class="menu-icon fa fa-caret-right"></i>心跳报表展示
					</a>
					<b class="arrow"></b>
				</li>
			</ul>
		</li>
		<li id="alert_config" class="${alertConfigActive ? 'hsub active open' : 'hsub'}">
			<a href="${contextPath}/mvc/s/config?op=metricConfigList" class="dropdown-toggle">
				<i class="menu-icon fa fa-bolt"></i>
				<span class="menu-text">应用告警配置</span>
				<b class="arrow fa fa-angle-down"></b>
			</a>
			<b class="arrow"></b>
			<ul class="submenu">
				<li id="transactionRule" class="${activeConfigMenu eq 'transactionRule' ? 'active' : ''}">
					<a href="${contextPath}/mvc/s/config?op=transactionRule">
						<i class="menu-icon fa fa-caret-right"></i>Transaction告警
					</a>
					<b class="arrow"></b>
				</li>
				<li id="eventRule" class="${activeConfigMenu eq 'eventRule' ? 'active' : ''}">
					<a href="${contextPath}/mvc/s/config?op=eventRule">
						<i class="menu-icon fa fa-caret-right"></i>Event告警
					</a>
					<b class="arrow"></b>
				</li>
				<li id="exception" class="${activeConfigMenu eq 'exception' ? 'active' : ''}">
					<a href="${contextPath}/mvc/s/config?op=exception">
						<i class="menu-icon fa fa-caret-right"></i>异常告警配置
					</a>
					<b class="arrow"></b>
				</li>
				<li id="heartbeatRuleConfigList" class="${activeConfigMenu eq 'heartbeatRule' ? 'active' : ''}">
					<a href="${contextPath}/mvc/s/config?op=heartbeatRuleConfigList">
						<i class="menu-icon fa fa-caret-right"></i>心跳告警配置
					</a>
					<b class="arrow"></b>
				</li>
			</ul>
		</li>
		<li id="overall_config" class="${overallConfigActive ? 'hsub active open' : 'hsub'}">
			<a href="${contextPath}/mvc/s/config?op=networkRuleConfigList" class="dropdown-toggle">
				<i class="menu-icon glyphicon glyphicon-cog"></i>
				<span class="menu-text">全局系统配置</span>
				<b class="arrow fa fa-angle-down"></b>
			</a>
			<b class="arrow"></b>
			<ul class="submenu">
				<li id="alertPolicy" class="${activeConfigMenu eq 'alertPolicy' ? 'active' : ''}">
					<a href="${contextPath}/mvc/s/config?op=alertPolicy">
						<i class="menu-icon fa fa-caret-right"></i>告警策略
					</a>
					<b class="arrow"></b>
				</li>
				<li id="alertDefaultReceivers" class="${activeConfigMenu eq 'alertDefaultReceivers' ? 'active' : ''}">
					<a href="${contextPath}/mvc/s/config?op=alertDefaultReceivers">
						<i class="menu-icon fa fa-caret-right"></i>默认告警人
					</a>
					<b class="arrow"></b>
				</li>
				<li id="alertSenderConfig" class="${activeConfigMenu eq 'alertSenderConfig' ? 'active' : ''}">
					<a href="${contextPath}/mvc/s/config?op=alertSenderConfigUpdate">
						<i class="menu-icon fa fa-caret-right"></i>告警服务端
					</a>
					<b class="arrow"></b>
				</li>
				<li id="serverConfigUpdate" class="${activeConfigMenu eq 'serverConfigUpdate' ? 'active' : ''}">
					<a href="${contextPath}/mvc/s/config?op=serverConfigUpdate">
						<i class="menu-icon fa fa-caret-right"></i>服务端配置
					</a>
					<b class="arrow"></b>
				</li>
				<li id="sampleConfigUpdate" class="${activeConfigMenu eq 'sampleConfigUpdate' ? 'active' : ''}">
					<a href="${contextPath}/mvc/s/config?op=sampleConfigUpdate">
						<i class="menu-icon fa fa-caret-right"></i>消息采样配置
					</a>
					<b class="arrow"></b>
				</li>
				<li id="routerConfigUpdate" class="${activeConfigMenu eq 'routerConfigUpdate' ? 'active' : ''}">
					<a href="${contextPath}/mvc/s/config?op=routerConfigUpdate">
						<i class="menu-icon fa fa-caret-right"></i>客户端路由
					</a>
					<b class="arrow"></b>
				</li>
				<li id="resourceUpdate" class="${activeConfigMenu eq 'resourceUpdate' ? 'active' : ''}">
					<a href="${contextPath}/mvc/s/permission?op=resource">
						<i class="menu-icon fa fa-caret-right"></i>资源权限配置
					</a>
					<b class="arrow"></b>
				</li>
				<li id="userUpdate" class="${activeConfigMenu eq 'userUpdate' ? 'active' : ''}">
					<a href="${contextPath}/mvc/s/permission?op=user">
						<i class="menu-icon fa fa-caret-right"></i>用户权限配置
					</a>
					<b class="arrow"></b>
				</li>
			</ul>
		</li>
	</ul>
	<div class="sidebar-toggle sidebar-collapse" id="sidebar-collapse">
		<i class="ace-icon fa fa-angle-double-left" data-icon1="ace-icon fa fa-angle-double-left" data-icon2="ace-icon fa fa-angle-double-right"></i>
	</div>
	<script type="text/javascript">
		try { ace.settings.check('sidebar', 'collapsed'); } catch(e) {}
	</script>
</div>
