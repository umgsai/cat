<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="contextPath" value="${empty contextPath ? pageContext.request.contextPath : contextPath}" />
<table class="problem">
	<tr>
		<td title="time\group">T\G</td>
		<c:forEach var="group" items="${threadLevelInfo.groups}">
			<td colspan="${group.number}" title="${group.name}">
				<a href="${contextPath}/mvc/r/p?op=thread&domain=${domain}&ip=${ipAddress}&groupName=${group.name}&date=${date}" onclick="return requestGroupInfo(this)">
					<c:out value="${fn:length(group.name) > 20 ? fn:substring(group.name, 0, 20) : group.name}" />
				</a>
			</td>
		</c:forEach>
	</tr>
	<tr>
		<td title="time\thread">T\T</td>
		<c:forEach var="thread" items="${threadLevelInfo.threads}">
			<td><c:out value="${thread}" /></td>
		</c:forEach>
	</tr>
	<c:forEach var="minute" items="${threadLevelInfo.datas}">
		<tr>${minute}</tr>
	</c:forEach>
</table>
<br>
