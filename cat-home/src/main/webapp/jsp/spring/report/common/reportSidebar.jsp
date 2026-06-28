<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="contextPath" value="${empty contextPath ? pageContext.request.contextPath : contextPath}" />
<c:set var="domain" value="${empty domain ? 'cat' : domain}" />
<c:set var="ipAddress" value="${empty ipAddress ? 'All' : ipAddress}" />
<c:set var="date" value="${empty date ? '' : date}" />
<c:set var="reportType" value="${empty reportType ? 'day' : reportType}" />
<div id="sidebar" class="sidebar responsive">
	<script type="text/javascript">
		try { ace.settings.check('sidebar', 'fixed'); } catch(e) {}
	</script>
	<ul class="nav nav-list" style="top: 0px;">
		<li id="Dashboard_report" class="${activeReport eq 'Dashboard' ? 'active open' : ''}">
			<a href="${contextPath}/mvc/r/top?op=view&domain=${domain}">
				<i class="menu-icon fa fa-tachometer"></i>
				<span class="menu-text">Dashboard</span>
			</a>
			<b class="arrow"></b>
		</li>
		<li id="Transaction_report" class="${activeReport eq 'Transaction' ? 'active open' : ''}">
			<a href="${contextPath}/mvc/r/t?domain=${domain}&ip=${ipAddress}&date=${date}&reportType=${reportType}&op=view">
				<i class="menu-icon glyphicon glyphicon-time"></i>
				<span class="menu-text">Transaction</span>
			</a>
			<b class="arrow"></b>
		</li>
		<li id="Event_report" class="${activeReport eq 'Event' ? 'active open' : ''}">
			<a href="${contextPath}/mvc/r/e?domain=${domain}&ip=${ipAddress}&date=${date}&reportType=${reportType}&op=view">
				<i class="menu-icon fa fa-flag"></i>
				<span class="menu-text">Event</span>
			</a>
			<b class="arrow"></b>
		</li>
		<li id="Problem_report" class="${activeReport eq 'Problem' ? 'active open' : ''}">
			<a href="${contextPath}/mvc/r/p?domain=${domain}&ip=${ipAddress}&date=${date}&reportType=${reportType}&op=view">
				<i class="menu-icon fa fa-bug"></i>
				<span class="menu-text">Problem</span>
			</a>
			<b class="arrow"></b>
		</li>
		<li id="Heartbeat_report" class="${activeReport eq 'Heartbeat' ? 'active open' : ''}">
			<a href="${contextPath}/mvc/r/h?domain=${domain}&ip=${ipAddress}&date=${date}&reportType=${reportType}&op=view">
				<i class="menu-icon fa fa-heart"></i>
				<span class="menu-text">Heartbeat</span>
			</a>
			<b class="arrow"></b>
		</li>
		<li id="Cross_report" class="${activeReport eq 'Cross' ? 'active open' : ''}">
			<a href="${contextPath}/mvc/r/cross?domain=${domain}&ip=${ipAddress}&date=${date}&reportType=${reportType}&op=view">
				<i class="menu-icon glyphicon glyphicon-random"></i>
				<span class="menu-text">Cross</span>
			</a>
			<b class="arrow"></b>
		</li>
		<li id="Business_report" class="${activeReport eq 'Business' ? 'active open' : ''}">
			<a href="${contextPath}/mvc/r/business?name=${domain}&type=domain">
				<i class="menu-icon fa fa-list-alt"></i>
				<span class="menu-text">Business</span>
			</a>
			<b class="arrow"></b>
		</li>
		<li id="State_report" class="${activeReport eq 'State' ? 'active open' : ''}">
			<a href="${contextPath}/mvc/r/state?domain=${domain}&ip=${ipAddress}&date=${date}&reportType=${reportType}&op=view">
				<i class="menu-icon fa fa-bar-chart-o"></i>
				<span class="menu-text">State</span>
			</a>
			<b class="arrow"></b>
		</li>
	</ul>
	<div class="sidebar-toggle sidebar-collapse" id="sidebar-collapse">
		<i class="ace-icon fa fa-angle-double-left" data-icon1="ace-icon fa fa-angle-double-left" data-icon2="ace-icon fa fa-angle-double-right"></i>
	</div>
	<script type="text/javascript">
		try { ace.settings.check('sidebar', 'collapsed'); } catch(e) {}
	</script>
</div>
