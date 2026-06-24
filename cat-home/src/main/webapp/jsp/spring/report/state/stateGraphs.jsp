<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<style type="text/css">
	.graph { width: 47%; height: 400px; margin: 0; }
</style>
<c:choose>
	<c:when test="${ipAddress eq 'All' and key ne 'delayAvg'}">
		<table>
			<tr>
				<td width="50%"><div id="trendGraph" class="graph"></div></td>
				<td width="50%"><div id="distributionChart" class="graph"></div></td>
			</tr>
			<tr>
				<td style="display:none"><div id="trendMeta">${graph}</div></td>
				<td><div id="distributionMeta" style="display:none">${pieChart}</div></td>
			</tr>
		</table>
	</c:when>
	<c:otherwise>
		<table>
			<tr><td><div id="trendGraph" class="graph"></div></td></tr>
			<tr><td style="display:none"><div id="trendMeta">${graph}</div></td></tr>
		</table>
	</c:otherwise>
</c:choose>
