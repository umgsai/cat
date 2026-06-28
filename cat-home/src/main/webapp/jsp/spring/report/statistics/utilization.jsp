<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<div class="row-fluid">
	<div class="tabbable">
		<ul class="nav nav-tabs" style="height:50px">
			<li class="text-right active"><a href="#tab1" data-toggle="tab"><strong>Web</strong></a></li>
			<li class="text-right"><a href="#tab2" data-toggle="tab"><strong>Service</strong></a></li>
		</ul>
		<div class="tab-content">
			<div class="tab-pane active" id="tab1">
				<table id="web_content" class="table table-striped table-condensed table-hover">
					<thead>
						<tr>
							<th>Web应用</th>
							<th>CMDB</th>
							<th>机器数</th>
							<th>访问量</th>
							<th>集群QPS</th>
							<th>单机QPS</th>
							<th>错误量</th>
							<th>错误量%</th>
							<th>响应时间(ms)</th>
							<th>95Line(ms)</th>
							<th>Load(平均)</th>
							<th>Load(最大)</th>
							<th>FullGc(小时平均)</th>
							<th>FullGc(最大)</th>
						</tr>
					</thead>
					<tbody>
						<c:forEach var="item" items="${utilizationWebList}">
							<tr>
								<td><c:out value="${item.id}" /></td>
								<td><c:out value="${item.cmdbId}" /></td>
								<td class="right">${item.machineNumber}</td>
								<td class="right"><fmt:formatNumber value="${item.applicationStates.URL.count}" pattern="###0" /></td>
								<td class="right"><fmt:formatNumber value="${item.applicationStates.URL.maxQps}" pattern="###0" /></td>
								<td class="right"><fmt:formatNumber value="${item.applicationStates.URL.maxQps / item.machineNumber}" pattern="###0" /></td>
								<td class="right"><fmt:formatNumber value="${item.applicationStates.URL.failureCount}" pattern="###0" /></td>
								<td class="right"><fmt:formatNumber value="${item.applicationStates.URL.failurePercent}" pattern="###%" /></td>
								<td class="right"><fmt:formatNumber value="${item.applicationStates.URL.avg}" pattern="#0.0" /></td>
								<td class="right"><fmt:formatNumber value="${item.applicationStates.URL.avg95}" pattern="#0.0" /></td>
								<td class="right"><fmt:formatNumber value="${item.machineStates.load.avg}" pattern="#0.0" /></td>
								<td class="right"><fmt:formatNumber value="${item.machineStates.load.avgMax}" pattern="#0.0" /></td>
								<td class="right"><fmt:formatNumber value="${item.machineStates.fullGc.avg}" pattern="#0.0" /></td>
								<td class="right"><fmt:formatNumber value="${item.machineStates.fullGc.avgMax}" pattern="#0.0" /></td>
							</tr>
						</c:forEach>
					</tbody>
				</table>
			</div>
			<div class="tab-pane" id="tab2">
				<table id="service_content" class="table table-striped table-condensed table-hover">
					<thead>
						<tr>
							<th>Service应用</th>
							<th>CMDB</th>
							<th>机器数</th>
							<th>访问量</th>
							<th>集群QPS</th>
							<th>单机QPS</th>
							<th>错误量</th>
							<th>错误量%</th>
							<th>响应时间</th>
							<th>95Line</th>
							<th>Load(平均)</th>
							<th>Load(最大)</th>
							<th>FullGc(小时平均)</th>
							<th>FullGc(最大)</th>
						</tr>
					</thead>
					<tbody>
						<c:forEach var="item" items="${utilizationServiceList}">
							<tr>
								<td><c:out value="${item.id}" /></td>
								<td><c:out value="${item.cmdbId}" /></td>
								<td class="right">${item.machineNumber}</td>
								<td class="right"><fmt:formatNumber value="${item.applicationStates.PigeonService.count}" pattern="###0" /></td>
								<td class="right"><fmt:formatNumber value="${item.applicationStates.PigeonService.maxQps}" pattern="###0" /></td>
								<td class="right"><fmt:formatNumber value="${item.applicationStates.PigeonService.maxQps / item.machineNumber}" pattern="###0" /></td>
								<td class="right"><fmt:formatNumber value="${item.applicationStates.PigeonService.failureCount}" pattern="###0" /></td>
								<td class="right"><fmt:formatNumber value="${item.applicationStates.PigeonService.failurePercent}" pattern="###%" /></td>
								<td class="right"><fmt:formatNumber value="${item.applicationStates.PigeonService.avg}" pattern="#0.0" /></td>
								<td class="right"><fmt:formatNumber value="${item.applicationStates.PigeonService.avg95}" pattern="#0.0" /></td>
								<td class="right"><fmt:formatNumber value="${item.machineStates.load.avg}" pattern="#0.0" /></td>
								<td class="right"><fmt:formatNumber value="${item.machineStates.load.avgMax}" pattern="#0.0" /></td>
								<td class="right"><fmt:formatNumber value="${item.machineStates.fullGc.avg}" pattern="#0.0" /></td>
								<td class="right"><fmt:formatNumber value="${item.machineStates.fullGc.avgMax}" pattern="#0.0" /></td>
							</tr>
						</c:forEach>
					</tbody>
				</table>
			</div>
		</div>
	</div>
</div>
<script type="text/javascript">
	$(document).ready(function() {
		if (typeof initTable == 'function') {
			initTable($('#web_content'));
			initTable($('#service_content'));
		}
	});
</script>
