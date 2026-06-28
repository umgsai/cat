<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="contextPath" value="${empty contextPath ? pageContext.request.contextPath : contextPath}" />
<table class="table table-hover table-striped table-condensed">
	<tr>
		<th colspan="5"><c:out value="${detailStatistics.subTitle}" /></th>
	</tr>
	<tr>
		<td colspan="5">
			<a href="${contextPath}/mvc/r/p?op=detail&domain=${domain}&ip=${ipAddress}&minute=${minuteLast}&date=${date}${detailStatistics.url}" class="minute" onclick="return show(this);">上一分钟</a>
			&nbsp;&nbsp;
			<a href="${contextPath}/mvc/r/p?op=detail&domain=${domain}&ip=${ipAddress}&minute=${minuteNext}&date=${date}${detailStatistics.url}" class="minute" onclick="return show(this);">下一分钟</a>
			&nbsp;&nbsp;&nbsp;CurrentMinute: ${currentMinute}
		</td>
	</tr>
	<tr>
		<th>Type</th>
		<th>Total</th>
		<th>Status</th>
		<th>Count</th>
		<th>SampleLinks</th>
	</tr>
	<c:forEach var="statistics" items="${detailStatistics.status}">
		<c:set var="typeStat" value="${statistics.value}" />
		<c:set var="statusSize" value="${fn:length(typeStat.status)}" />
		<tr>
			<td rowspan="${statusSize}">
				<a href="#" class="${typeStat.type}">&nbsp;&nbsp;</a>
				&nbsp;&nbsp;<c:out value="${typeStat.type}" />
			</td>
			<td rowspan="${statusSize}"><fmt:formatNumber value="${typeStat.count}" pattern="#,###,###,###,##0" /></td>
			<c:forEach var="status" items="${typeStat.status}" varStatus="index">
				<c:if test="${!index.first}"><tr></c:if>
				<td><c:out value="${status.value.status}" /></td>
				<td><fmt:formatNumber value="${status.value.count}" pattern="#,###,###,###,##0" /></td>
				<td>
					<c:forEach var="link" items="${status.value.links}" varStatus="linkIndex">
						<a href="${contextPath}/mvc/r/m/${link}?domain=${domain}">${linkIndex.first ? 'L' : (linkIndex.last ? 'g' : 'o')}</a>
					</c:forEach>
				</td>
				<c:if test="${!index.first}"></tr></c:if>
			</c:forEach>
		</tr>
	</c:forEach>
</table>
