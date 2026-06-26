<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="contextPath" value="${empty contextPath ? pageContext.request.contextPath : contextPath}" />
<!doctype html>
<script type="text/javascript" src="${contextPath}/js/jquery-1.7.1.js"></script>
<script type="text/javascript" src="${contextPath}/js/highcharts.js"></script>
<script type="text/javascript" src="${contextPath}/js/baseGraph.js"></script>
<script type="text/javascript" src="${contextPath}/js/heartbeatHistory.js"></script>
<style type="text/css">
.graph {
	width: 430px;
	height: 350px;
	margin: 4px auto;
}
</style>
<br>
<table class="graph" id="graph">
	<c:choose>
		<c:when test="${type eq 'extension'}">
			<tr id="extensionGraph"></tr>
			<script type="text/javascript">
				var extensionHistoryGraphs = ${empty extensionHistoryGraphs ? "[]" : extensionHistoryGraphs};
				var count = ${empty extensionCount ? 0 : extensionCount};

				buildExtensionGraph(count, extensionHistoryGraphs);
			</script>
		</c:when>
		<c:otherwise>
			<tr><td></td></tr>
		</c:otherwise>
	</c:choose>
</table>
