<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div style="display:inline-flex;padding-top:3px;">
	<table id="contents" class="table table-striped table-condensed table-hover">
		<thead>
			<tr>
				<th>domain</th>
				<th>ip</th>
				<c:forEach var="item" items="${jars}">
					<th><c:out value="${item}" /></th>
				</c:forEach>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="item" items="${jarReport.domains}">
				<c:forEach var="machine" items="${item.value.machines}">
					<tr>
						<td><c:out value="${item.key}" /></td>
						<td><c:out value="${machine.key}" /></td>
						<c:forEach var="jar" items="${machine.value.jars}">
							<td><c:out value="${jar.version}" /></td>
						</c:forEach>
					</tr>
				</c:forEach>
			</c:forEach>
		</tbody>
	</table>
</div>
<script type="text/javascript">
	$(document).ready(function() {
		if (typeof init == 'function') { init(); }
	});
</script>
