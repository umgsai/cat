<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<div id="myModal" class="modal fade" tabindex="-1" role="dialog" aria-hidden="true"></div>
<div class="row">
	<div class="col-xs-6">
		<h5 class="text-danger text-center">项目本身详细数据</h5>
		<table class="contents table table-striped table-condensed">
			<thead>
				<tr>
					<th>Name</th>
					<th>Total</th>
					<th>Failure</th>
					<th>Failure%</th>
					<th>Avg(ms)</th>
					<th>Config</th>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="item" items="${segment.indexs}">
					<c:set var="itemValue" value="${item.value}" />
					<tr>
						<td>${itemValue.name}</td>
						<td style="text-align:right;">${itemValue.totalCount}</td>
						<td style="text-align:right;">${itemValue.errorCount}</td>
						<td style="text-align:right;"><fmt:formatNumber value="${itemValue.totalCount == 0 ? 0 : itemValue.errorCount / itemValue.totalCount}" pattern="0.0000" /></td>
						<td style="text-align:right;"><fmt:formatNumber value="${itemValue.avg}" pattern="0.0" /></td>
						<td><a class="nodeConfigUpdate btn btn-primary btn-sm" target="_blank" href="${contextPath}/mvc/s/config?op=topologyGraphNodeConfigAdd&type=${itemValue.name}&domain=${domain}">配置阀值</a></td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
	</div>
	<div class="col-xs-6">
		<h5 class="text-danger text-center">依赖项目详细数据</h5>
		<table class="contentsDependency table table-striped table-condensed">
			<thead>
				<tr>
					<th>Type</th>
					<th>Target</th>
					<th>Total</th>
					<th>Failure</th>
					<th>Failure%</th>
					<th>Avg(ms)</th>
					<th>Config</th>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="item" items="${segment.dependencies}">
					<c:set var="itemValue" value="${item.value}" />
					<tr>
						<td>${itemValue.type}</td>
						<td>${itemValue.target}</td>
						<td style="text-align:right;">${itemValue.totalCount}</td>
						<td style="text-align:right;">${itemValue.errorCount}</td>
						<td style="text-align:right;"><fmt:formatNumber value="${itemValue.totalCount == 0 ? 0 : itemValue.errorCount / itemValue.totalCount}" pattern="0.0000" /></td>
						<td style="text-align:right;"><fmt:formatNumber value="${itemValue.avg}" pattern="0.0" /></td>
						<td>
							<c:choose>
								<c:when test="${itemValue.type eq 'PigeonServer' || itemValue.type eq 'PigeonService'}">
									<a class="btn btn-primary edgeConfigUpdate btn-sm" target="_blank" href="${contextPath}/mvc/s/config?op=topologyGraphEdgeConfigAdd&type=PigeonCall&to=${domain}&from=${itemValue.target}">配置阀值</a>
								</c:when>
								<c:otherwise>
									<a class="btn btn-primary edgeConfigUpdate btn-sm" target="_blank" href="${contextPath}/mvc/s/config?op=topologyGraphEdgeConfigAdd&type=${itemValue.type}&from=${domain}&to=${itemValue.target}">配置阀值</a>
								</c:otherwise>
							</c:choose>
						</td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
	</div>
</div>
