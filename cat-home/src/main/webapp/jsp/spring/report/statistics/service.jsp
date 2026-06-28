<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<table class="table table-striped table-condensed table-hover">
	<tr>
		<th class="left">Server(Domain)</th>
		<th class="right"><a href="${baseUri}?domain=${encodedDomain}&date=${date}&ip=${encodedIpAddress}&op=${action}&sort=total&reportType=${reportType}">Total</a></th>
		<th class="right"><a href="${baseUri}?domain=${encodedDomain}&date=${date}&ip=${encodedIpAddress}&op=${action}&sort=failure&reportType=${reportType}">Failure</a></th>
		<th class="right"><a href="${baseUri}?domain=${encodedDomain}&date=${date}&ip=${encodedIpAddress}&op=${action}&sort=failurePercent&reportType=${reportType}">Failure%</a></th>
		<th class="right"><a href="${baseUri}?domain=${encodedDomain}&date=${date}&ip=${encodedIpAddress}&op=${action}&sort=availability&reportType=${reportType}">Availability%</a></th>
		<th class="right"><a href="${baseUri}?domain=${encodedDomain}&date=${date}&ip=${encodedIpAddress}&op=${action}&sort=avg&reportType=${reportType}">Avg(ms)</a></th>
	</tr>
	<c:forEach var="item" items="${serviceList}">
		<tr>
			<td><c:out value="${item.id}" /></td>
			<td class="right"><fmt:formatNumber value="${item.totalCount}" pattern="#,###,###,###,##0" /></td>
			<td class="right"><fmt:formatNumber value="${item.failureCount}" pattern="#,###,###,###,##0" /></td>
			<c:choose>
				<c:when test="${item.failurePercent > 0.0001}">
					<td class="right" style="color:red"><fmt:formatNumber value="${item.failurePercent}" pattern="0.00000%" /></td>
					<td class="right" style="color:red"><fmt:formatNumber value="${1 - item.failurePercent}" pattern="0.00000%" /></td>
				</c:when>
				<c:otherwise>
					<td class="right"><fmt:formatNumber value="${item.failurePercent}" pattern="0.00000%" /></td>
					<td class="right"><fmt:formatNumber value="${1 - item.failurePercent}" pattern="0.00000%" /></td>
				</c:otherwise>
			</c:choose>
			<td class="right"><fmt:formatNumber value="${item.avg}" pattern="0.00" /></td>
		</tr>
	</c:forEach>
</table>
