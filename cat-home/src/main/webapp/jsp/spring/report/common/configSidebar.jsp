<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="contextPath" value="${empty contextPath ? pageContext.request.contextPath : contextPath}" />
<c:set var="projectConfigActive" value="${activeConfigMenu eq 'projects' || activeConfigMenu eq 'domainGroupConfigs'}" />
<c:set var="applicationConfigActive" value="${activeConfigMenu eq 'businessConfig' || activeConfigMenu eq 'businessTag' || activeConfigMenu eq 'displayPolicy'}" />
<div id="sidebar" class="sidebar responsive">
	<script type="text/javascript">
		try { ace.settings.check('sidebar', 'fixed'); } catch(e) {}
	</script>
	<ul class="nav nav-list" style="top: 0px;">
		<li id="projects_config" class="${projectConfigActive ? 'hsub active open' : 'hsub'}">
			<a href="${contextPath}/mvc/s/config?op=projects" class="dropdown-toggle">
				<i class="menu-icon fa fa-cogs"></i>
				<span class="menu-text">项目配置信息</span>
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
		<li id="alert_config" class="hsub">
			<a href="${contextPath}/mvc/s/config?op=metricConfigList" class="dropdown-toggle">
				<i class="menu-icon fa fa-bolt"></i>
				<span class="menu-text">应用告警配置</span>
				<b class="arrow fa fa-angle-down"></b>
			</a>
			<b class="arrow"></b>
			<ul class="submenu">
				<li id="transactionRule">
					<a href="${contextPath}/mvc/s/config?op=transactionRule">
						<i class="menu-icon fa fa-caret-right"></i>Transaction告警
					</a>
					<b class="arrow"></b>
				</li>
				<li id="eventRule">
					<a href="${contextPath}/mvc/s/config?op=eventRule">
						<i class="menu-icon fa fa-caret-right"></i>Event告警
					</a>
					<b class="arrow"></b>
				</li>
				<li id="exception">
					<a href="${contextPath}/mvc/s/config?op=exception">
						<i class="menu-icon fa fa-caret-right"></i>异常告警配置
					</a>
					<b class="arrow"></b>
				</li>
				<li id="heartbeatRuleConfigList">
					<a href="${contextPath}/mvc/s/config?op=heartbeatRuleConfigList">
						<i class="menu-icon fa fa-caret-right"></i>心跳告警配置
					</a>
					<b class="arrow"></b>
				</li>
			</ul>
		</li>
		<li id="overall_config" class="hsub">
			<a href="${contextPath}/mvc/s/config?op=networkRuleConfigList" class="dropdown-toggle">
				<i class="menu-icon glyphicon glyphicon-cog"></i>
				<span class="menu-text">全局系统配置</span>
				<b class="arrow fa fa-angle-down"></b>
			</a>
			<b class="arrow"></b>
			<ul class="submenu">
				<li id="alertPolicy">
					<a href="${contextPath}/mvc/s/config?op=alertPolicy">
						<i class="menu-icon fa fa-caret-right"></i>告警策略
					</a>
					<b class="arrow"></b>
				</li>
				<li id="alertDefaultReceivers">
					<a href="${contextPath}/mvc/s/config?op=alertDefaultReceivers">
						<i class="menu-icon fa fa-caret-right"></i>默认告警人
					</a>
					<b class="arrow"></b>
				</li>
				<li id="alertSenderConfig">
					<a href="${contextPath}/mvc/s/config?op=alertSenderConfigUpdate">
						<i class="menu-icon fa fa-caret-right"></i>告警服务端
					</a>
					<b class="arrow"></b>
				</li>
				<li id="serverConfigUpdate">
					<a href="${contextPath}/mvc/s/config?op=serverConfigUpdate">
						<i class="menu-icon fa fa-caret-right"></i>服务端配置
					</a>
					<b class="arrow"></b>
				</li>
				<li id="sampleConfigUpdate">
					<a href="${contextPath}/mvc/s/config?op=sampleConfigUpdate">
						<i class="menu-icon fa fa-caret-right"></i>消息采样配置
					</a>
					<b class="arrow"></b>
				</li>
				<li id="routerConfigUpdate">
					<a href="${contextPath}/mvc/s/config?op=routerConfigUpdate">
						<i class="menu-icon fa fa-caret-right"></i>客户端路由
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
