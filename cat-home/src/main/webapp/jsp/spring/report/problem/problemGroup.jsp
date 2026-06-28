<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="contextPath" value="${empty contextPath ? pageContext.request.contextPath : contextPath}" />
<table class="table table-hover table-striped table-condensed">
	<tr>
		<td title="time\group">T\G</td>
		<c:forEach var="group" items="${groupLevelInfo.groups}">
			<td title="${group}">
				<a href="${contextPath}/mvc/r/p?op=thread&domain=${domain}&ip=${ipAddress}&group=${group}&date=${date}" onclick="return requestGroupInfo(this)">
					<c:out value="${fn:length(group) > 20 ? fn:substring(group, 0, 20) : group}" />
				</a>
			</td>
		</c:forEach>
	</tr>
	<c:forEach var="minute" items="${groupLevelInfo.datas}">
		<tr>${minute}</tr>
	</c:forEach>
</table>
<br>
