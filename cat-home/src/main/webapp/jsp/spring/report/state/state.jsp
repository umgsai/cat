<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="contextPath" value="${empty contextPath ? pageContext.request.contextPath : contextPath}" />
<c:set var="domain" value="${empty domain ? 'cat' : domain}" />
<c:set var="ipAddress" value="${empty ipAddress ? 'All' : ipAddress}" />
<c:set var="date" value="${empty date ? '' : date}" />
<c:set var="reportType" value="${empty reportType ? 'day' : reportType}" />
<!doctype html>
<html lang="en">
<head>
	<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
	<meta charset="utf-8">
	<title>CAT</title>
	<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0">
	<link rel="stylesheet" type="text/css" href="${contextPath}/assets/css/bootstrap.min.css">
	<link rel="stylesheet" type="text/css" href="${contextPath}/assets/css/font-awesome.min.css">
	<link rel="stylesheet" type="text/css" href="${contextPath}/assets/css/jquery-ui.min.css">
	<link rel="stylesheet" type="text/css" href="${contextPath}/assets/css/ace-fonts.css">
	<link rel="stylesheet" type="text/css" href="${contextPath}/assets/css/ace.min.css" id="main-ace-style">
	<link rel="stylesheet" type="text/css" href="${contextPath}/assets/css/ace-skins.min.css">
	<link rel="stylesheet" type="text/css" href="${contextPath}/assets/css/ace-rtl.min.css">
	<link rel="stylesheet" type="text/css" href="${contextPath}/css/body.css">
	<script src="${contextPath}/assets/js/jquery.min.js"></script>
	<script src="${contextPath}/assets/js/ace-extra.min.js"></script>
	<script src="${contextPath}/assets/js/bootstrap.min.js"></script>
	<script src="${contextPath}/js/highcharts.js"></script>
	<script src="${contextPath}/js/baseGraph.js"></script>
	<script src="${contextPath}/assets/js/jquery-ui.min.js"></script>
	<script src="${contextPath}/assets/js/jquery.ui.touch-punch.min.js"></script>
	<script src="${contextPath}/assets/js/ace-elements.min.js"></script>
	<script src="${contextPath}/assets/js/ace.min.js"></script>
	<style>
		.left { text-align: left; }
		.right { text-align: right; }
		.current { color: red; font-weight: bold; }
	</style>
</head>
<body class="no-skin">
	<c:set var="navbarApplicationDisabled" value="true" scope="request" />
	<jsp:include page="../common/navbar.jsp" />
	<div class="main-container" id="main-container">
		<c:set var="activeReport" value="State" scope="request" />
		<jsp:include page="../common/reportSidebar.jsp" />
		<div class="main-content">
			<div style="padding-top:2px;padding-left:2px;padding-right:8px;">
				<div class="report">
					<div class="breadcrumbs" id="breadcrumbs">
						<table style="line-height:normal;">
							<tr>
								<td><span class="text-success">${reportStart} to ${reportEnd}</span></td>
								<td>
									<div class="nav-search nav" id="nav-search">
										<span class="text-danger switch">【<a class="switch" href="${contextPath}/mvc/r/state?op=history&domain=${domain}&ip=${ipAddress}"><span class="text-danger">切到历史模式</span></a>】</span>
										<c:forEach var="nav" items="${navs}">
											&nbsp;[ <a href="${contextPath}/mvc/r/state?date=${date}&step=${nav.hours}&${navPrefix}">${nav.title}</a> ]
										</c:forEach>
										&nbsp;[ <a href="${contextPath}/mvc/r/state?${navPrefix}">now</a> ]&nbsp;
									</div>
								</td>
							</tr>
						</table>
						<script type="text/javascript">
							try { ace.settings.check('breadcrumbs', 'fixed'); } catch(e) {}
						</script>
					</div>
					<table class="machines">
						<tr class="left">
							<th>&nbsp;[&nbsp;
								<a href="${contextPath}/mvc/r/state?show=${show}&domain=${domain}&date=${date}" class="${ipAddress eq 'All' ? 'current' : ''}">All</a>
								&nbsp;]&nbsp;
								<c:forEach var="ip" items="${ips}">
									&nbsp;[&nbsp;<a href="${contextPath}/mvc/r/state?show=${show}&domain=${domain}&ip=${ip}&date=${date}" class="${ipAddress eq ip ? 'current' : ''}"><c:out value="${ip}" /></a>&nbsp;]&nbsp;
								</c:forEach>
							</th>
						</tr>
					</table>
					<c:choose>
						<c:when test="${not empty message}">
							<h3 class="text-center text-danger">出问题CAT的服务端:<c:out value="${message}" /></h3>
						</c:when>
						<c:otherwise>
							<h3 class="text-center text-success">CAT服务端正常</h3>
						</c:otherwise>
					</c:choose>
					<c:set var="machine" value="${state.machine}" />
					<table class="table table-hover table-striped table-condensed" width="100%">
						<tr>
							<th width="30%" colspan="2">指标</th>
							<th class="right" width="20%">值</th>
							<th width="50%">备注</th>
						</tr>
						<tr>
							<td><a href="${contextPath}/mvc/r/state?op=graph&ip=${ipAddress}&date=${date}&key=total" data-status="total" class="state_graph_link">[:: show ::]</a></td>
							<td>处理消息总量</td>
							<td class="right"><fmt:formatNumber value="${machine.total}" pattern="#,###,###,###,##0.#" /></td>
							<td>服务器接受到消息总量</td>
						</tr>
						<tr class="graphs"><td colspan="4" style="display:none"><div id="total" style="display:none"></div></td></tr>
						<tr>
							<td><a href="${contextPath}/mvc/r/state?op=graph&ip=${ipAddress}&date=${date}&key=totalLoss" data-status="totalLoss" class="state_graph_link">[:: show ::]</a></td>
							<td>丢失消息总量</td>
							<td class="right" style="${machine.totalLoss > 0 ? 'color:red;' : ''}"><fmt:formatNumber value="${machine.totalLoss}" pattern="#,###,###,###,##0.#" /></td>
							<td>服务器进行encode以及analyze处理来不及而丢失消息总量</td>
						</tr>
						<tr class="graphs"><td colspan="4" style="display:none"><div id="totalLoss" style="display:none"></div></td></tr>
						<tr>
							<td><a href="${contextPath}/mvc/r/state?op=graph&ip=${ipAddress}&date=${date}&key=avgTps" data-status="avgTps" class="state_graph_link">[:: show ::]</a></td>
							<td>每分钟平均处理数</td>
							<td class="right"><fmt:formatNumber value="${machine.avgTps}" pattern="###,###,###,##0" /></td>
							<td>平均每分钟处理消息量</td>
						</tr>
						<tr class="graphs"><td colspan="4" style="display:none"><div id="avgTps" style="display:none"></div></td></tr>
						<tr>
							<td><a href="${contextPath}/mvc/r/state?op=graph&ip=${ipAddress}&date=${date}&key=maxTps" data-status="maxTps" class="state_graph_link">[:: show ::]</a></td>
							<td>单台机器每分钟最大处理数</td>
							<td class="right"><fmt:formatNumber value="${machine.maxTps}" pattern="###,###,###,##0" /></td>
							<td>单台机器平均每分钟最大处理消息数目</td>
						</tr>
						<tr class="graphs"><td colspan="4" style="display:none"><div id="maxTps" style="display:none"></div></td></tr>
						<tr>
							<td><a href="${contextPath}/mvc/r/state?op=graph&ip=${ipAddress}&date=${date}&key=dump" data-status="dump" class="state_graph_link">[:: show ::]</a></td>
							<td>压缩成功消息数量</td>
							<td class="right"><fmt:formatNumber value="${machine.dump}" pattern="###,###,###,##0" /></td>
							<td>将消息进行压缩消息数目</td>
						</tr>
						<tr class="graphs"><td colspan="4" style="display:none"><div id="dump" style="display:none"></div></td></tr>
						<tr>
							<td><a href="${contextPath}/mvc/r/state?op=graph&ip=${ipAddress}&date=${date}&key=dumpLoss" data-status="dumpLoss" class="state_graph_link">[:: show ::]</a></td>
							<td>来不及压缩丢失消息数量</td>
							<td class="right" style="${machine.dumpLoss > 0 ? 'color:red;' : ''}"><fmt:formatNumber value="${machine.dumpLoss}" pattern="#,###,###,###,##0.#" /></td>
							<td>将消息进行压缩，线程太忙而丢失消息丢失数目</td>
						</tr>
						<tr class="graphs"><td colspan="4" style="display:none"><div id="dumpLoss" style="display:none"></div></td></tr>
						<tr>
							<td><a href="${contextPath}/mvc/r/state?op=graph&ip=${ipAddress}&date=${date}&key=pigeonTimeError" data-status="pigeonTimeError" class="state_graph_link">[:: show ::]</a></td>
							<td>两台机器时钟不准导致消息存储丢失</td>
							<td class="right"><fmt:formatNumber value="${machine.pigeonTimeError}" pattern="###,###,###,##0" /></td>
							<td>这个场景用于Pigeon，服务端id是由客户端产生，客户端和服务端时钟差2小时，会导致存储丢失</td>
						</tr>
						<tr class="graphs"><td colspan="4" style="display:none"><div id="pigeonTimeError" style="display:none"></div></td></tr>
						<tr>
							<td><a href="${contextPath}/mvc/r/state?op=graph&ip=${ipAddress}&date=${date}&key=networkTimeError" data-status="networkTimeError" class="state_graph_link">[:: show ::]</a></td>
							<td>网络传输或者客户端延迟发送导致消息丢失</td>
							<td class="right"><fmt:formatNumber value="${machine.networkTimeError}" pattern="###,###,###,##0" /></td>
							<td>CAT分小时处理，当一个小时过去了，默认会延迟3分钟结束当前小时，在3分钟后还接受上个小时消息，直接丢弃</td>
						</tr>
						<tr class="graphs"><td colspan="4" style="display:none"><div id="networkTimeError" style="display:none"></div></td></tr>
						<tr>
							<td><a href="${contextPath}/mvc/r/state?op=graph&ip=${ipAddress}&date=${date}&key=blockTotal" data-status="blockTotal" class="state_graph_link">[:: show ::]</a></td>
							<td>存储消息块数量</td>
							<td class="right"><fmt:formatNumber value="${machine.blockTotal}" pattern="###,###,###,##0" /></td>
							<td>CAT是分块存储，消息块成功放入存储队列</td>
						</tr>
						<tr class="graphs"><td colspan="4" style="display:none"><div id="blockTotal" style="display:none"></div></td></tr>
						<tr>
							<td><a href="${contextPath}/mvc/r/state?op=graph&ip=${ipAddress}&date=${date}&key=blockLoss" data-status="blockLoss" class="state_graph_link">[:: show ::]</a></td>
							<td>存储消息块丢失数量</td>
							<td class="right" style="${machine.blockLoss > 0 ? 'color:red;' : ''}"><fmt:formatNumber value="${machine.blockLoss}" pattern="#,###,###,###,##0.#" /></td>
							<td>将存储块写入磁盘的线程太忙，存储队列溢出的消息块数量</td>
						</tr>
						<tr class="graphs"><td colspan="4" style="display:none"><div id="blockLoss" style="display:none"></div></td></tr>
						<tr>
							<td><a href="${contextPath}/mvc/r/state?op=graph&ip=${ipAddress}&date=${date}&key=blockTime" data-status="blockTime" class="state_graph_link">[:: show ::]</a></td>
							<td>存储消息块花费时间(分钟)</td>
							<td class="right"><fmt:formatNumber value="${machine.blockTime / 1000 / 60}" pattern="###,###,###,##0" /></td>
							<td>存储消息花费的CPU时间</td>
						</tr>
						<tr class="graphs"><td colspan="4" style="display:none"><div id="blockTime" style="display:none"></div></td></tr>
						<tr>
							<td><a href="${contextPath}/mvc/r/state?op=graph&ip=${ipAddress}&date=${date}&key=size" data-status="size" class="state_graph_link">[:: show ::]</a></td>
							<td>压缩前消息大小(GB)</td>
							<td class="right"><fmt:formatNumber value="${machine.size / 1024 / 1024 / 1024}" pattern="0.00#" /></td>
							<td>压缩前所有存储消息的总大小</td>
						</tr>
						<tr class="graphs"><td colspan="4" style="display:none"><div id="size" style="display:none"></div></td></tr>
						<tr>
							<td><a href="${contextPath}/mvc/r/state?op=graph&ip=${ipAddress}&date=${date}&key=delayAvg" data-status="delayAvg" class="state_graph_link">[:: show ::]</a></td>
							<td>系统处理延迟(ms)</td>
							<td class="right"><fmt:formatNumber value="${machine.delayAvg}" pattern="0.#" /></td>
							<td>客户端产生消息，到服务端存储之间的时钟误差。（在机器时钟完全准确的情况下）</td>
						</tr>
						<tr class="graphs"><td colspan="4" style="display:none"><div id="delayAvg" style="display:none"></div></td></tr>
					</table>
					<c:if test="${show}">
						<table class="table table-hover table-striped table-condensed" width="100%">
							<tr>
								<td width="10%"><a href="${contextPath}/mvc/r/state?domain=${domain}&ip=${ipAddress}&date=${date}&sort=domain&show=true">处理项目列表</a></td>
								<td width="10%" class="right"><a href="${contextPath}/mvc/r/state?domain=${domain}&ip=${ipAddress}&date=${date}&sort=total&show=true">处理消息总量</a></td>
								<td width="10%" class="right"><a href="${contextPath}/mvc/r/state?domain=${domain}&ip=${ipAddress}&date=${date}&sort=loss&show=true">丢失消息总量</a></td>
								<td width="10%" class="right"><a href="${contextPath}/mvc/r/state?domain=${domain}&ip=${ipAddress}&date=${date}&sort=size&show=true">压缩前消息大小(GB)</a></td>
								<td width="15%" class="right"><a href="${contextPath}/mvc/r/state?domain=${domain}&ip=${ipAddress}&date=${date}&sort=avg&show=true">平均消息大小(KB)</a></td>
								<td width="5%" class="right"><a href="${contextPath}/mvc/r/state?domain=${domain}&ip=${ipAddress}&date=${date}&sort=machine&show=true">机器总数</a></td>
								<td width="45%">项目对应机器列表</td>
							</tr>
							<c:forEach var="item" items="${state.processDomains}" varStatus="status">
								<tr>
									<td><c:out value="${item.name}" /></td>
									<td class="right"><fmt:formatNumber value="${item.total}" pattern="#,###,###,###,##0.#" /><br>
										<a href="${contextPath}/mvc/r/state?op=graph&ip=${ipAddress}&date=${date}&key=${item.name}:total" data-status="${item.name}:total" class="state_graph_link">[:: show ::]</a></td>
									<td class="right"><fmt:formatNumber value="${item.totalLoss}" pattern="#,###,###,###,##0.#" /><br>
										<a href="${contextPath}/mvc/r/state?op=graph&ip=${ipAddress}&date=${date}&key=${item.name}:totalLoss" data-status="${item.name}:totalLoss" class="state_graph_link">[:: show ::]</a></td>
									<td class="right"><fmt:formatNumber value="${item.size / 1024 / 1024 / 1024}" pattern="#,###,##0.000" /><br>
										<a href="${contextPath}/mvc/r/state?op=graph&ip=${ipAddress}&date=${date}&key=${item.name}:size" data-status="${item.name}:size" class="state_graph_link">[:: show ::]</a></td>
									<td class="right"><fmt:formatNumber value="${item.avg / 1024}" pattern="#,###,##0.000" /></td>
									<td class="right">${fn:length(item.ips)}</td>
									<td style="white-space:normal"><c:out value="${item.ips}" /></td>
								</tr>
								<tr class="graphs"><td colspan="7" style="display:none"><div id="${item.name}:total" style="display:none"></div></td></tr>
								<tr class="graphs"><td colspan="7" style="display:none"><div id="${item.name}:totalLoss" style="display:none"></div></td></tr>
								<tr class="graphs"><td colspan="7" style="display:none"><div id="${item.name}:size" style="display:none"></div></td></tr>
							</c:forEach>
							<tr style="color:white;">
								<td></td><td></td><td></td><td></td><td></td><td>${state.totalSize}</td><td></td>
							</tr>
						</table>
					</c:if>
				</div>
			</div>
		</div>
	</div>
	<script src="${contextPath}/js/state.js"></script>
	<script type="text/javascript">
		$(document).ready(function() {
			$('.position').hide();
			$('#State_report').addClass("open active");
			$('#nav_application').click(function() {
				window.location.href = '${contextPath}/mvc/r/state?domain=${domain}&ip=${ipAddress}&date=${date}&reportType=${reportType}&op=view';
			});
			$('#nav_config').click(function() {
				window.location.href = '${contextPath}/mvc/s/config?op=projects';
			});
			$('#nav_document').click(function() {
				window.location.href = '${contextPath}/mvc/r/home?op=view&docName=index';
			});
		});
	</script>
</body>
</html>
