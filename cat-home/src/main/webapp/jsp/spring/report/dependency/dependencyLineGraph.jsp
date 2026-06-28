<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div id="fullScreenData">
	<div class="row-fluid">
		<div class="span12">
			<h4 class="text-danger text-center">当前小时内项目本身指标趋势图</h4>
			<table>
				<tr>
					<c:forEach var="item" items="${indexGraph}" varStatus="status">
						<td><div id="item${status.index}" style="width:380px;height:300px;"></div></td>
					</c:forEach>
				</tr>
			</table>
		</div>
	</div>
	<div class="row-fluid">
		<div class="span12">
			<h4 class="text-danger text-center">当前小时内项目依赖指标趋势图</h4>
			<table>
				<c:forEach var="charts" items="${dependencyGraph}" varStatus="type">
					<tr><th colspan="3"><h4 class="text-center text-success">${charts.key}</h4></th></tr>
					<tr>
						<c:forEach var="item" items="${charts.value}" varStatus="status">
							<td><div id="item${type.index}-${status.index}" style="width:380px;height:300px;"></div></td>
						</c:forEach>
					</tr>
				</c:forEach>
			</table>
		</div>
	</div>
</div>
<script type="text/javascript">
	<c:forEach var="item" items="${indexGraph}" varStatus="status">
		graphLineChart(document.getElementById('item${status.index}'), ${item});
	</c:forEach>
	<c:forEach var="charts" items="${dependencyGraph}" varStatus="type">
		<c:forEach var="item" items="${charts.value}" varStatus="status">
			graphLineChart(document.getElementById('item${type.index}-${status.index}'), ${item});
		</c:forEach>
	</c:forEach>
</script>
