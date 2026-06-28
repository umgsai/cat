<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<table class="machines">
	<tr class="left">
		<th>&nbsp;[&nbsp;
			<a href="${contextPath}/mvc/r/cache?${listQueryPrefix}&type=${encodedType}&queryname=${encodedQueryName}" class="${ipAddress eq 'All' ? 'current' : ''}">All</a>
			&nbsp;]&nbsp;
			<c:forEach var="ip" items="${ips}">
				&nbsp;[&nbsp;<a href="${contextPath}/mvc/r/cache?${listQueryPrefix}&ip=${ip}&type=${encodedType}&queryname=${encodedQueryName}" class="${ip eq ipAddress ? 'current' : ''}"><c:out value="${ip}" /></a>&nbsp;]&nbsp;
			</c:forEach>
		</th>
	</tr>
</table>
<script type="text/javascript" src="${contextPath}/js/appendHostname.js"></script>
<script type="text/javascript">
	$(document).ready(function() { appendHostname(${empty ipToHostnameStr ? "{}" : ipToHostnameStr}); });
</script>
<c:choose>
	<c:when test="${empty type}">
		<table class="table table-hover table-striped table-condensed">
			<tr>
				<th class="left"><a href="${contextPath}/mvc/r/cache?${sortQueryPrefix}&sort=type">Type</a></th>
				<th class="right"><a href="${contextPath}/mvc/r/cache?${sortQueryPrefix}&sort=total">Total</a></th>
				<th class="right"><a href="${contextPath}/mvc/r/cache?${sortQueryPrefix}&sort=missed">Missed</a></th>
				<th class="right"><a href="${contextPath}/mvc/r/cache?${sortQueryPrefix}&sort=hitPercent">Hit Rate(%)</a></th>
				<th class="right"><a href="${contextPath}/mvc/r/cache?${sortQueryPrefix}&sort=avg">Avg</a>(ms)</th>
				<th class="right">QPS</th>
			</tr>
			<c:forEach var="item" items="${report.typeItems}" varStatus="status">
				<c:set var="e" value="${item.type}" />
				<tr class="right">
					<td class="left"><a href="${contextPath}/mvc/r/cache?${listQueryPrefix}&ip=${ipAddress}&type=${e.id}"><c:out value="${e.id}" /></a></td>
					<td><fmt:formatNumber value="${e.totalCount}" pattern="#,###,###,###,##0" /></td>
					<td><fmt:formatNumber value="${item.missed}" pattern="#,###,###,###,##0" /></td>
					<td><fmt:formatNumber value="${item.hited}" pattern="0.0000%" /></td>
					<td><fmt:formatNumber value="${e.avg}" pattern="0.0" /></td>
					<td><fmt:formatNumber value="${e.tps}" pattern="0.0" /></td>
				</tr>
			</c:forEach>
		</table>
	</c:when>
	<c:otherwise>
		<div class="row-fluid">
			<div class="span7">
				<table class="table table-hover table-striped table-condensed">
					<tr>
						<th class="left" colspan="10">
							<input type="text" name="queryname" id="queryname" size="40" value="${fn:escapeXml(queryName)}">
							<input style="width:60px" class="btn btn-sm btn-primary" onclick="filterByName()" type="submit">
							支持多个字符串查询，例如sql|url|task，查询结果为包含任一sql、url、task的列
						</th>
					</tr>
					<tr><th class="left" colspan="10">命中率计算方式: 1-missed/Get, mGet不在统计范围之内</th></tr>
					<tr>
						<th class="left"><a href="${contextPath}/mvc/r/cache?${sortQueryPrefix}&type=${encodedType}&sort=name&queryname=${encodedQueryName}">Name</a></th>
						<th class="right"><a href="${contextPath}/mvc/r/cache?${sortQueryPrefix}&type=${encodedType}&sort=total&queryname=${encodedQueryName}">Total</a></th>
						<c:forEach var="method" items="${report.methods}">
							<th class="right"><a href="${contextPath}/mvc/r/cache?${sortQueryPrefix}&type=${encodedType}&sort=${method}&queryname=${encodedQueryName}"><c:out value="${method}" /></a></th>
						</c:forEach>
						<th class="right"><a href="${contextPath}/mvc/r/cache?${sortQueryPrefix}&type=${encodedType}&sort=missed&queryname=${encodedQueryName}">Missed</a></th>
						<th class="right"><a href="${contextPath}/mvc/r/cache?${sortQueryPrefix}&type=${encodedType}&sort=hitPercent&queryname=${encodedQueryName}">Hit Rate(%)</a></th>
						<th class="right"><a href="${contextPath}/mvc/r/cache?${sortQueryPrefix}&type=${encodedType}&sort=avg&queryname=${encodedQueryName}">Avg</a>(ms)</th>
						<th class="right">QPS</th>
					</tr>
					<c:forEach var="item" items="${report.nameItems}">
						<c:set var="e" value="${item.name}" />
						<tr class="right">
							<td class="left" style="word-wrap:break-word;word-break:break-all;">
								<c:choose>
									<c:when test="${fn:length(e.id) > 80}"><c:out value="${fn:substring(e.id, 0, 80)}" />...</c:when>
									<c:otherwise><c:out value="${e.id}" /></c:otherwise>
								</c:choose>
							</td>
							<td><fmt:formatNumber value="${e.totalCount}" pattern="#,###,###,###,##0" /></td>
							<c:forEach var="method" items="${report.methods}">
								<td><fmt:formatNumber value="${empty item.methodCounts[method] ? 0 : item.methodCounts[method]}" pattern="#,###,###,###,##0" /></td>
							</c:forEach>
							<td><fmt:formatNumber value="${item.missed}" pattern="#,###,###,###,##0" /></td>
							<td><fmt:formatNumber value="${item.hited}" pattern="0.0000%" /></td>
							<td><fmt:formatNumber value="${e.avg}" pattern="0.0" /></td>
							<td><fmt:formatNumber value="${e.tps}" pattern="0.0" /></td>
						</tr>
					</c:forEach>
				</table>
			</div>
			<div class="span5">
				<div id="cacheGraph" class="pieChart"></div>
				<script type="text/javascript">
					var data = ${empty pieChart ? "{}" : pieChart};
					graphPieChart(document.getElementById('cacheGraph'), data);
				</script>
			</div>
		</div>
		<script type="text/javascript">
			function filterByName() {
				var queryname = $('#queryname').val();
				window.location.href = '${contextPath}/mvc/r/cache?${sortQueryPrefix}&type=${encodedType}&queryname=' + encodeURIComponent(queryname);
			}
		</script>
	</c:otherwise>
</c:choose>
