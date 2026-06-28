<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div class="tabbable">
	<ul class="nav nav-tabs" style="height:60px">
		<li class="text-right active"><a href="#tab1" data-toggle="tab"><strong>远程调用最多【URL】</strong></a></li>
		<li class="text-right"><a href="#tab2" data-toggle="tab"><strong>远程调用最多【Service】</strong></a></li>
		<li class="text-right"><a href="#tab3" data-toggle="tab"><strong>数据库最多【URL】</strong></a></li>
		<li class="text-right"><a href="#tab4" data-toggle="tab"><strong>数据库最多【Service】</strong></a></li>
		<li class="text-right"><a href="#tab5" data-toggle="tab"><strong>缓存最多【URL】</strong></a></li>
		<li class="text-right"><a href="#tab6" data-toggle="tab"><strong>缓存最多【Service】</strong></a></li>
	</ul>
	<div class="tab-content">
		<div class="tab-pane active" id="tab1">
			<table class="table table-striped table-condensed table-hover">
				<tr><th width="10%">项目名</th><th width="70%">URL名称</th><th width="10%">链接</th><th width="10%" class="right">访问次数</th></tr>
				<c:forEach var="item" items="${callUrls}">
					<tr><td><c:out value="${item.domain}" /></td><td><c:out value="${item.name}" /></td><td><a href="${contextPath}/mvc/r/m/${item.logview}?domain=${domain}">Log View</a></td><td class="right">${item.count}</td></tr>
				</c:forEach>
			</table>
		</div>
		<div class="tab-pane" id="tab2">
			<table class="table table-striped table-condensed table-hover">
				<tr><th width="10%">项目名</th><th width="70%">Service名称</th><th width="10%">链接</th><th width="10%" class="right">访问次数</th></tr>
				<c:forEach var="item" items="${callServices}">
					<tr><td><c:out value="${item.domain}" /></td><td><c:out value="${item.name}" /></td><td><a href="${contextPath}/mvc/r/m/${item.logview}?domain=${domain}">Log View</a></td><td class="right">${item.count}</td></tr>
				</c:forEach>
			</table>
		</div>
		<div class="tab-pane" id="tab3">
			<table class="table table-striped table-condensed table-hover">
				<tr><th width="10%">项目名</th><th width="70%">URL名称</th><th width="10%">链接</th><th width="10%" class="right">访问次数</th></tr>
				<c:forEach var="item" items="${sqlUrls}">
					<tr><td><c:out value="${item.domain}" /></td><td><c:out value="${item.name}" /></td><td><a href="${contextPath}/mvc/r/m/${item.logview}?domain=${domain}">Log View</a></td><td class="right">${item.count}</td></tr>
				</c:forEach>
			</table>
		</div>
		<div class="tab-pane" id="tab4">
			<table class="table table-striped table-condensed table-hover">
				<tr><th width="10%">项目名</th><th width="70%">Service名称</th><th width="10%">链接</th><th width="10%" class="right">访问次数</th></tr>
				<c:forEach var="item" items="${sqlServices}">
					<tr><td><c:out value="${item.domain}" /></td><td><c:out value="${item.name}" /></td><td><a href="${contextPath}/mvc/r/m/${item.logview}?domain=${domain}">Log View</a></td><td class="right">${item.count}</td></tr>
				</c:forEach>
			</table>
		</div>
		<div class="tab-pane" id="tab5">
			<table class="table table-striped table-condensed table-hover">
				<tr><th width="10%">项目名</th><th width="70%">URL名称</th><th width="10%">链接</th><th width="10%" class="right">访问次数</th></tr>
				<c:forEach var="item" items="${cacheUrls}">
					<tr><td><c:out value="${item.domain}" /></td><td><c:out value="${item.name}" /></td><td><a href="${contextPath}/mvc/r/m/${item.logview}?domain=${domain}">Log View</a></td><td class="right">${item.count}</td></tr>
				</c:forEach>
			</table>
		</div>
		<div class="tab-pane" id="tab6">
			<table class="table table-striped table-condensed table-hover">
				<tr><th width="10%">项目名</th><th width="70%">Service名称</th><th width="10%">链接</th><th width="10%" class="right">访问次数</th></tr>
				<c:forEach var="item" items="${cacheServices}">
					<tr><td><c:out value="${item.domain}" /></td><td><c:out value="${item.name}" /></td><td><a href="${contextPath}/mvc/r/m/${item.logview}?domain=${domain}">Log View</a></td><td class="right">${item.count}</td></tr>
				</c:forEach>
			</table>
		</div>
	</div>
</div>
