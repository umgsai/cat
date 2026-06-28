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
	<script src="${contextPath}/assets/js/jquery-ui.min.js"></script>
	<script src="${contextPath}/assets/js/jquery.ui.touch-punch.min.js"></script>
	<script src="${contextPath}/assets/js/ace-elements.min.js"></script>
	<script src="${contextPath}/assets/js/ace.min.js"></script>
	<script src="${contextPath}/js/highcharts.js"></script>
	<script src="${contextPath}/js/baseGraph.js"></script>
	<style>
		.left { text-align: left; }
		.right { text-align: right; }
		.center { text-align: center; }
		.current { color: red; font-weight: bold; }
	</style>
</head>
<body class="no-skin">
	<c:set var="navbarApplicationDisabled" value="true" scope="request" />
	<jsp:include page="../common/navbar.jsp" />
	<div class="main-container" id="main-container">
		<c:set var="activeReport" value="Storage" scope="request" />
		<jsp:include page="../common/reportSidebar.jsp" />
		<div class="main-content">
			<div style="padding-top:2px;padding-left:2px;padding-right:8px;">
				<div class="report">
					<div class="breadcrumbs" id="breadcrumbs">
						<table style="line-height:normal;">
							<tr>
								<td><span class="text-success">${reportStart} to ${reportEnd}</span></td>
								<td>
									<div id="warp_search_group" style="width:250px;">
										<form id="wrap_search" style="margin-bottom:0px;">
											<div class="input-group">
												<span class="input-group-btn"><button class="btn btn-sm btn-default" onclick="showDomain()" type="button" id="switch">全部</button></span>
												<span class="input-group-btn"><button class="btn btn-sm btn-default" onclick="showFrequent()" type="button" id="frequent">常用</button></span>
												<span class="input-icon" style="width:200px;">
													<input id="search" type="text" value="${fn:escapeXml(id)}" class="search-input search-input form-control ui-autocomplete-input" placeholder="input domain for search" autocomplete="off">
													<i class="ace-icon fa fa-search nav-search-icon"></i>
												</span>
												<span class="input-group-btn"><button class="btn btn-sm btn-pink" type="button" id="search_go">Go</button></span>
											</div>
										</form>
									</div>
								</td>
								<td>
									<div class="nav-search nav" id="nav-search">
										<c:choose>
											<c:when test="${historyMode}">
												<span class="text-danger switch">【<a class="switch" href="${baseUri}?op=view&type=${encodedType}&domain=${encodedDomain}&id=${encodedId}&ip=${encodedIpAddress}&operations=${encodedOperations}"><span class="text-danger">切到小时模式</span></a>】</span>
												<c:forEach var="nav" items="${historyNavs}">
													&nbsp;[ <a href="${baseUri}?op=history&type=${encodedType}&domain=${encodedDomain}&id=${encodedId}&ip=${encodedIpAddress}&date=${date}&reportType=${nav.title}&operations=${encodedOperations}" class="${nav.title eq reportType ? 'current' : ''}">${nav.title}</a> ]
												</c:forEach>
												&nbsp;[ <a href="${baseUri}?op=history&type=${encodedType}&domain=${encodedDomain}&id=${encodedId}&ip=${encodedIpAddress}&date=${date}&reportType=${reportType}&step=-1&operations=${encodedOperations}">${currentNav.last}</a> ]
												&nbsp;[ <a href="${baseUri}?op=history&type=${encodedType}&domain=${encodedDomain}&id=${encodedId}&ip=${encodedIpAddress}&date=${date}&reportType=${reportType}&step=1&operations=${encodedOperations}">${currentNav.next}</a> ]
												&nbsp;[ <a href="${baseUri}?op=history&type=${encodedType}&domain=${encodedDomain}&id=${encodedId}&ip=${encodedIpAddress}&reportType=${reportType}&operations=${encodedOperations}">now</a> ]&nbsp;
											</c:when>
											<c:otherwise>
												<span class="text-danger switch">【<a class="switch" href="${baseUri}?op=history&type=${encodedType}&domain=${encodedDomain}&id=${encodedId}&ip=${encodedIpAddress}&operations=${encodedOperations}"><span class="text-danger">切到历史模式</span></a>】</span>
												<c:forEach var="nav" items="${navs}">
													&nbsp;[ <a href="${baseUri}?op=view&type=${encodedType}&domain=${encodedDomain}&id=${encodedId}&ip=${encodedIpAddress}&date=${date}&step=${nav.hours}&operations=${encodedOperations}">${nav.title}</a> ]
												</c:forEach>
												&nbsp;[ <a href="${baseUri}?op=view&type=${encodedType}&domain=${encodedDomain}&id=${encodedId}&ip=${encodedIpAddress}&operations=${encodedOperations}">now</a> ]&nbsp;
											</c:otherwise>
										</c:choose>
									</div>
								</td>
							</tr>
						</table>
						<script type="text/javascript">
							try { ace.settings.check('breadcrumbs', 'fixed'); } catch(e) {}
						</script>
					</div>
					<div class="domainNavbar" style="display:none;font-size:small">
						<table border="1" rules="all">
							<c:forEach var="item" items="${departments}">
								<tr>
									<c:set var="detail" value="${item.value}" />
									<td class="department" rowspan="${fn:length(detail.productlines)}">${item.key}</td>
									<c:forEach var="productline" items="${detail.productlines}" varStatus="index">
										<c:if test="${index.index != 0}"><tr></c:if>
										<td class="department">${productline.key}</td>
										<td>
											<div class="domain">
												<c:forEach var="storageId" items="${productline.value.storages}">
													&nbsp;<a class="domainItem ${id eq storageId ? 'current' : ''}" href="${baseUri}?op=${action}&type=${encodedType}&domain=${encodedDomain}&id=${fn:escapeXml(storageId)}&date=${date}&reportType=${reportType}">[&nbsp;<c:out value="${storageId}" />&nbsp;]</a>&nbsp;
												</c:forEach>
											</div>
										</td>
										<c:if test="${index.index != 0}"></tr></c:if>
									</c:forEach>
								</tr>
							</c:forEach>
						</table>
					</div>
					<div class="frequentNavbar" style="display:none;font-size:small">
						<table border="1" rules="all"><tr><td class="domain" style="word-break:break-all" id="frequentNavbar"></td></tr></table>
					</div>
					<table class="machines">
						<tr class="left">
							<th>&nbsp;[&nbsp;<a href="${baseUri}?op=${action}&type=${encodedType}&domain=${encodedDomain}&id=${encodedId}&date=${date}&reportType=${reportType}&operations=${encodedOperations}" class="${ipAddress eq 'All' ? 'current' : ''}">All</a>&nbsp;]&nbsp;
								<c:forEach var="ip" items="${ips}">
									&nbsp;[&nbsp;<a href="${baseUri}?op=${action}&type=${encodedType}&domain=${encodedDomain}&id=${encodedId}&ip=${ip}&date=${date}&reportType=${reportType}&operations=${encodedOperations}" class="${ipAddress eq ip ? 'current' : ''}"><c:out value="${ip}" /></a>&nbsp;]&nbsp;
								</c:forEach>
							</th>
						</tr>
					</table>
					<table>
						<tr>
							<td>
								<label class="btn btn-info btn-sm"><input type="checkbox" id="operation_All" onclick="clickAll()">All</label>
								<c:forEach var="item" items="${operations}">
									<label class="btn btn-info btn-sm"><input type="checkbox" id="operation_${item}" value="${item}" onclick="clickMe()"><c:out value="${item}" /></label>
								</c:forEach>
							</td>
							<td><input class="btn btn-primary btn-sm" value="   查询   " onclick="query()" type="submit"></td>
						</tr>
					</table>
					<table class="table table-hover table-striped table-condensed table-bordered" style="width:100%">
						<tr>
							<th colspan="${historyMode ? 1 : 2}" rowspan="2" class="center" style="vertical-align:middle">
								<a href="${baseUri}?op=${action}&type=${encodedType}&domain=${encodedDomain}&id=${encodedId}&ip=${encodedIpAddress}&date=${date}&reportType=${reportType}&operations=${encodedOperations}&sort=domain">Domain</a>
							</th>
							<c:forEach var="item" items="${currentOperations}">
								<th class="center" colspan="4"><c:out value="${item}" /></th>
							</c:forEach>
						</tr>
						<tr>
							<c:forEach var="item" items="${currentOperations}">
								<th class="right"><a href="${baseUri}?op=${action}&type=${encodedType}&domain=${encodedDomain}&id=${encodedId}&ip=${encodedIpAddress}&date=${date}&reportType=${reportType}&operations=${encodedOperations}&sort=${item};count">Count</a></th>
								<th class="right"><a href="${baseUri}?op=${action}&type=${encodedType}&domain=${encodedDomain}&id=${encodedId}&ip=${encodedIpAddress}&date=${date}&reportType=${reportType}&operations=${encodedOperations}&sort=${item};long">Long</a></th>
								<th class="right"><a href="${baseUri}?op=${action}&type=${encodedType}&domain=${encodedDomain}&id=${encodedId}&ip=${encodedIpAddress}&date=${date}&reportType=${reportType}&operations=${encodedOperations}&sort=${item};avg">Avg</a></th>
								<th class="right"><a href="${baseUri}?op=${action}&type=${encodedType}&domain=${encodedDomain}&id=${encodedId}&ip=${encodedIpAddress}&date=${date}&reportType=${reportType}&operations=${encodedOperations}&sort=${item};error">Error</a></th>
							</c:forEach>
						</tr>
						<c:forEach var="itemDomain" items="${machine.domains}">
							<tr>
								<c:if test="${!historyMode}">
									<td><a href="${baseUri}?op=hourlyGraph&type=${encodedType}&domain=${encodedDomain}&date=${date}&id=${encodedId}&ip=${encodedIpAddress}&project=${itemDomain.key}&operations=${encodedOperations}" class="storage_graph_link" data-status="${itemDomain.key}">[:: show ::]</a></td>
								</c:if>
								<td class="${historyMode ? 'center' : 'left'}">
									<c:choose>
										<c:when test="${itemDomain.key eq 'All'}"><c:out value="${itemDomain.key}" /></c:when>
										<c:otherwise><a href="${contextPath}/mvc/r/p?op=view&domain=${itemDomain.key}&ip=All&date=${date}&reportType=day" target="_blank"><c:out value="${itemDomain.key}" /></a></c:otherwise>
									</c:choose>
								</td>
								<c:forEach var="op" items="${currentOperations}">
									<td class="right"><fmt:formatNumber value="${itemDomain.value.operations[op].count}" pattern="#,###,###,###,##0" /></td>
									<td class="right"><fmt:formatNumber value="${itemDomain.value.operations[op].longCount}" pattern="#,###,###,###,##0" /></td>
									<td class="right"><fmt:formatNumber value="${itemDomain.value.operations[op].avg}" pattern="###,##0.0" /></td>
									<td class="right"><c:choose><c:when test="${itemDomain.value.operations[op].error > 0}"><span class="badge badge-danger"><fmt:formatNumber value="${itemDomain.value.operations[op].error}" pattern="#,###,###,###,##0" /></span></c:when><c:otherwise><fmt:formatNumber value="${itemDomain.value.operations[op].error}" pattern="#,###,###,###,##0" /></c:otherwise></c:choose></td>
								</c:forEach>
							</tr>
							<c:if test="${!historyMode}">
								<tr class="graphs"><td colspan="${operationColumnCount}" style="display:none"><div id="${itemDomain.key}" style="display:none"></div></td></tr>
								<tr style="display:none"></tr>
							</c:if>
						</c:forEach>
					</table>
				</div>
			</div>
		</div>
	</div>
	<script src="${contextPath}/js/storage.js"></script>
	<script type="text/javascript">
		var fs = "${currentOperations}".replace(/[\[\]]/g,'').split(', ');
		var allfs = "${operations}".replace(/[\[\]]/g,'').split(', ');
		function getcookie(objname) {
			var arrstr = document.cookie.split("; ");
			for (var i = 0; i < arrstr.length; i++) {
				var temp = arrstr[i].split("=");
				if (temp[0] == objname) {
					return temp[1];
				}
			}
			return "";
		}
		function showDomain() {
			var b = $('#switch').html();
			if (b == '全部') {
				$('.domainNavbar').slideDown();
				$('#switch').html("收起");
			} else {
				$('.domainNavbar').slideUp();
				$('#switch').html("全部");
			}
		}
		function showFrequent() {
			var b = $('#frequent').html();
			if (b == '常用') {
				$('.frequentNavbar').slideDown();
				$('#frequent').html("收起");
			} else {
				$('.frequentNavbar').slideUp();
				$('#frequent').html("常用");
			}
		}
		function buildStorageHref(storageId) {
			return '<a href="${baseUri}?op=${action}&type=${encodedType}&domain=${encodedDomain}&id=' + encodeURIComponent(storageId) + '&date=${date}">&nbsp;[&nbsp;' + storageId + '&nbsp;]&nbsp;</a>';
		}
		function clickMe() {
			var num = 0;
			for (var i = 0; i < allfs.length; i++) {
				var checkbox = document.getElementById("operation_" + allfs[i]);
				if (checkbox && checkbox.checked) { num++; } else { document.getElementById("operation_All").checked = false; }
			}
			if (num > 0 && num == allfs.length) { document.getElementById("operation_All").checked = true; }
		}
		function clickAll() {
			for (var i = 0; i < allfs.length; i++) {
				var checkbox = document.getElementById("operation_" + allfs[i]);
				if (checkbox) { checkbox.checked = document.getElementById("operation_All").checked; }
			}
		}
		function query() {
			var url = "";
			if (!document.getElementById("operation_All").checked && allfs.length > 0) {
				for (var i = 0; i < allfs.length; i++) {
					var checkbox = document.getElementById("operation_" + allfs[i]);
					if (checkbox && checkbox.checked) { url += allfs[i] + ";"; }
				}
				url = url.substring(0, url.length - 1);
			}
			window.location.href = "${baseUri}?op=${action}&type=${encodedType}&domain=${encodedDomain}&id=${encodedId}&ip=${encodedIpAddress}&reportType=${reportType}&date=${date}&operations=" + encodeURIComponent(url);
		}
		function initOperations() {
			for (var i = 0; i < fs.length; i++) {
				var checkbox = document.getElementById("operation_" + fs[i]);
				if (checkbox) { checkbox.checked = true; }
			}
			if (allfs.length == fs.length) { document.getElementById("operation_All").checked = true; }
		}
		$(document).ready(function() {
			var domains = getcookie('CAT_DOMAINS') || '';
			var domainArray = domains.split("|");
			var html = '';
			for (var i = 0; i < domainArray.length; i++) {
				if (domainArray[i]) {
					html += buildStorageHref(domainArray[i]);
				}
			}
			$('#frequentNavbar').html(html);
			$("#search_go").bind("click", function() {
				window.location.href = '${baseUri}?op=${action}&type=${encodedType}&domain=${encodedDomain}&id=' + encodeURIComponent($("#search").val()) + '&date=${date}';
			});
			$('#wrap_search').submit(function() {
				window.location.href = '${baseUri}?op=${action}&type=${encodedType}&domain=${encodedDomain}&id=' + encodeURIComponent($("#search").val()) + '&date=${date}';
				return false;
			});
			$.widget("custom.catcomplete", $.ui.autocomplete, {
				_renderMenu: function(ul, items) {
					var that = this;
					var currentCategory = "";
					$.each(items, function(index, item) {
						if (item.category != currentCategory) {
							ul.append("<li class='ui-autocomplete-category'>" + item.category + "</li>");
							currentCategory = item.category;
						}
						that._renderItemData(ul, item);
					});
				}
			});
			var data = [];
					<c:forEach var="item" items="${departments}">
						<c:set var="department" value="${item.value}" />
						<c:forEach var="entry" items="${department.productlines}">
							<c:set var="productline" value="${entry.value}" />
							<c:forEach var="storageId" items="${productline.storages}">
								data.push({ label: '<c:out value="${storageId}" />', category: '<c:out value="${entry.key}" />' });
							</c:forEach>
						</c:forEach>
					</c:forEach>
			$("#search").catcomplete({ delay: 0, source: data });
			$('[data-rel=tooltip]').tooltip();
			initOperations();
		});
	</script>
</body>
</html>
