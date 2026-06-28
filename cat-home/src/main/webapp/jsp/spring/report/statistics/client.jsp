<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<div id="queryBar">
	<div style="float:left;">
		&nbsp;日期
		<input type="text" id="time" style="width:100px;" value="${day}">
	</div>
	&nbsp;&nbsp;&nbsp;&nbsp;<input class="btn btn-primary btn-sm" value="查询" onclick="queryNew()" type="submit">
</div>
<br>
<table class="table table-striped table-condensed table-hover" style="width:100%" id="contents">
	<thead>
		<tr>
			<th>项目</th>
			<th>接口</th>
			<th width="8%">总量</th>
			<th width="5%">失败</th>
			<th width="8%">失败率</th>
			<th width="8%">响应时间</th>
			<th width="8%">超时时间</th>
		</tr>
	</thead>
	<tbody>
		<c:forEach var="entry" items="${clientReport.domains}">
			<c:forEach var="method" items="${entry.value.methods}">
				<tr>
					<td><c:out value="${entry.key}" /></td>
					<td><c:out value="${method.value.id}" /></td>
					<td><fmt:formatNumber value="${method.value.totalCount}" pattern="##0" /></td>
					<td><fmt:formatNumber value="${method.value.failureCount}" pattern="##0" /></td>
					<td><fmt:formatNumber value="${method.value.failurePercent}" pattern="##0.0000" /></td>
					<td><fmt:formatNumber value="${method.value.avg}" pattern="##0.00" /></td>
					<td><fmt:formatNumber value="${method.value.timeout}" pattern="##0.00" /></td>
				</tr>
			</c:forEach>
		</c:forEach>
	</tbody>
</table>
<script type="text/javascript">
	$(document).ready(function() {
		if (typeof init == 'function') { init(); }
		$('#time').datetimepicker({ format:'Y-m-d', timepicker:false, maxDate:0 });
	});
	function queryNew() {
		window.location.href = '${baseUri}?op=client&day=' + encodeURIComponent($('#time').val());
	}
</script>
