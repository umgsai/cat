<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="contextPath" value="${pageContext.request.contextPath}" />
<c:set var="domain" value="${empty domain ? 'cat' : domain}" />
<c:set var="ipAddress" value="${empty ipAddress ? 'All' : ipAddress}" />
<c:set var="date" value="${empty date ? '' : date}" />
<c:set var="minute" value="${empty minute ? 0 : minute}" />
<c:set var="maxMinute" value="${empty maxMinute ? 60 : maxMinute}" />
<c:set var="frequency" value="${empty frequency ? 10 : frequency}" />
<c:set var="refresh" value="${empty refresh ? false : refresh}" />
<c:set var="fullScreen" value="${empty fullScreen ? false : fullScreen}" />
<c:set var="navPrefix" value="domain=${domain}&op=view" />
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
	<style>
		.tooltip-inner { max-width: 36555px; }
		.ui-tooltip { max-width: 36555px; }
		.smallTable { font-size: small; }
		.pagination { margin: 4px 0; }
		.pagination ul { margin-top: 0; }
		.pagination > li > a, .pagination > li > span { padding: 3px 10px; }
		.tab-content table {
			max-width: 100%;
			background-color: transparent;
			border-collapse: collapse;
			border-spacing: 0;
		}
	</style>
</head>
<body class="no-skin">
	<c:set var="navbarApplicationDisabled" value="true" scope="request" />
	<jsp:include page="../common/navbar.jsp" />
	<div class="main-container" id="main-container">
		<script type="text/javascript">
			try { ace.settings.check('main-container', 'fixed'); } catch(e) {}
		</script>
		<c:set var="activeReport" value="Dashboard" scope="request" />
		<jsp:include page="../common/reportSidebar.jsp" />
		<div class="main-content">
			<div style="padding-top:2px;padding-right:8px;">
				<div class="report">
					<div class="breadcrumbs" id="breadcrumbs">
						<table>
							<tr>
								<td><span class="text-success">${reportStart} to ${reportEnd}</span></td>
								<td>
									<div id="warp_search_group" class="" style="width:250px;">
										<form id="wrap_search" style="margin-left:10px;margin-bottom:0px;">
											<div class="input-group">
												<span class="input-group-btn"><button class="btn btn-sm btn-default" onclick="showDomain()" type="button" id="switch">全部</button></span>
												<span class="input-group-btn"><button class="btn btn-sm btn-default" onclick="showFrequent()" type="button" id="frequent">常用</button></span>
												<span class="input-icon" style="width:200px;">
													<input id="search" type="text" value="${fn:escapeXml(domain)}" class="search-input search-input form-control ui-autocomplete-input" placeholder="input domain for search" autocomplete="off"/>
													<i class="ace-icon fa fa-search nav-search-icon"></i>
												</span>
												<span class="input-group-btn"><button class="btn btn-sm btn-pink" type="button" id="search_go">Go</button></span>
											</div>
										</form>
									</div>
								</td>
								<td>
									<div class="nav-search nav" id="nav-search">
										<span class="text-danger switch">【<a class="switch" href="${contextPath}/mvc/r/top?op=history&domain=${domain}&ip=${ipAddress}"><span class="text-danger">切到历史模式</span></a>】</span>
										&nbsp;[ <a href="${contextPath}/mvc/r/top?date=${date}&ip=${ipAddress}&step=-168&${navPrefix}">-7d</a> ]
										&nbsp;[ <a href="${contextPath}/mvc/r/top?date=${date}&ip=${ipAddress}&step=-24&${navPrefix}">-1d</a> ]
										&nbsp;[ <a href="${contextPath}/mvc/r/top?date=${date}&ip=${ipAddress}&step=-1&${navPrefix}">-1h</a> ]
										&nbsp;[ <a href="${contextPath}/mvc/r/top?date=${date}&ip=${ipAddress}&step=1&${navPrefix}">+1h</a> ]
										&nbsp;[ <a href="${contextPath}/mvc/r/top?date=${date}&ip=${ipAddress}&step=24&${navPrefix}">+1d</a> ]
										&nbsp;[ <a href="${contextPath}/mvc/r/top?date=${date}&ip=${ipAddress}&step=168&${navPrefix}">+7d</a> ]
										&nbsp;[ <a href="${contextPath}/mvc/r/top?${navPrefix}">now</a> ]&nbsp;
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
							<c:forEach var="department" items="${domainGroups}">
								<c:set var="lines" value="${department.value.projectLines}" />
								<c:forEach var="line" items="${lines}" varStatus="lineStatus">
									<tr>
										<c:if test="${lineStatus.first}">
											<td class="department" rowspan="${fn:length(lines)}"><c:out value="${department.key}" /></td>
										</c:if>
										<td class="department"><c:out value="${line.key}" /></td>
										<td><div class="domain">
											<c:forEach var="itemDomain" items="${line.value.lineDomains}">
												&nbsp;<a class="domainItem" href="${contextPath}/mvc/r/top?op=view&domain=${itemDomain}&date=${date}&reportType=day">[&nbsp;<c:out value="${itemDomain}" />&nbsp;]</a>&nbsp;
											</c:forEach>
										</div></td>
									</tr>
								</c:forEach>
							</c:forEach>
						</table>
					</div>
					<div class="frequentNavbar" style="display:none;font-size:small">
						<table border="1" rules="all"><tr><td class="domain" style="word-break:break-all" id="frequentNavbar"></td></tr></table>
					</div>
					<div class="text-center">
						<ul class="pagination">
							<c:forEach var="item" items="${minutes}">
								<li id="minute${item}" class="${item > maxMinute ? 'disabled' : ''}">
									<a class="href${item}" href="${contextPath}/mvc/r/top?op=view&domain=${domain}&date=${date}&minute=${item}&fullScreen=${fullScreen}&refresh=${refresh}&frequency=${frequency}">
										<fmt:formatNumber value="${item}" pattern="00" />
									</a>
								</li>
							</c:forEach>
						</ul>
					</div>
					<div class="">
						<c:choose>
							<c:when test="${not empty message}">
								<h3 class="text-center text-danger">出问题CAT的服务端:<c:out value="${message}" /></h3>
							</c:when>
							<c:otherwise>
								<h3 class="text-center text-success">CAT服务端正常</h3>
							</c:otherwise>
						</c:choose>
						<c:forEach var="entry" items="${topResultView}">
							<table class="smallTable" style="float:left" border="1">
								<tr><th colspan="2" class="text-danger"><c:out value="${entry.key}" /></th></tr>
								<tr><th>系统</th><th>个</th></tr>
								<c:forEach var="item" items="${entry.value}">
									<tr>
										<td style="${item.style}">
											<a class="hreftip" style="${item.linkStyle}" href="${contextPath}/mvc/r/p?domain=${item.domain}&date=${date}" title="${fn:escapeXml(item.errorInfo)}"><c:out value="${item.shortDomain}" /></a>
										</td>
										<td style="${item.style}text-align:right"><fmt:formatNumber value="${item.value}" pattern="0" /></td>
									</tr>
								</c:forEach>
							</table>
						</c:forEach>
					</div>
				</div>
			</div>
		</div>
	</div>
	<script type="text/javascript">
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
		function buildHref(domain) {
			return '<a href="${contextPath}/mvc/r/top?op=view&domain=' + domain + '&date=${date}">&nbsp;[&nbsp;' + domain + '&nbsp;]&nbsp;</a>';
		}
		$(document).ready(function() {
			var domains = getcookie('CAT_DOMAINS') || '';
			var domainArray = domains.split("|");
			var html = '';

			for (var i = 0; i < domainArray.length; i++) {
				if (domainArray[i]) {
					html += buildHref(domainArray[i]);
				}
			}
			if (!html) {
				html = buildHref('${fn:escapeXml(domain)}');
			}
			$('#frequentNavbar').html(html);
			$("#search_go").bind("click", function() {
				window.location.href = '${contextPath}/mvc/r/top?op=view&domain=' + $("#search").val() + '&date=${date}';
			});
			$('#wrap_search').submit(function() {
				window.location.href = '${contextPath}/mvc/r/top?op=view&domain=' + $("#search").val() + '&date=${date}';
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
			<c:forEach var="department" items="${domainGroups}">
				<c:forEach var="line" items="${department.value.projectLines}">
					<c:forEach var="itemDomain" items="${line.value.lineDomains}">
			data.push({ label: '<c:out value="${itemDomain}" />', category: '<c:out value="${line.key}" />' });
					</c:forEach>
				</c:forEach>
			</c:forEach>
			$("#search").catcomplete({
				delay: 0,
				source: data
			});
			$("#warp_search_group").hide();
			$('.position').hide();
			$('.switch').hide();
			$('.href${minute}').css('color', 'red').css('font-weight', 'bold');
			$('#minute${minute}').addClass('disabled');
			$('.hreftip').tooltip({
				show: true,
				delay: {show: 10000, hide: 100000},
				position: {
					my: "left top",
					at: "left bottom"
				},
				content: function() {
					return $(this).attr("title");
				},
				open: function(event, ui) {
					ui.tooltip.animate({ top: ui.tooltip.position().top + 10 }, "fast");
				}
			});
			$('#Dashboard_report').addClass("open active");
			$('#dashbord_system').addClass("active");
			$('#Dependency_report').removeClass("open active");
			$('#nav_application').click(function() {
				window.location.href = '${contextPath}/mvc/r/t?domain=${domain}&ip=${ipAddress}&date=${date}&reportType=day&op=view';
			});
			$('#nav_config').click(function() {
				window.location.href = '${contextPath}/mvc/s/config?op=projects';
			});
			$('#nav_document').click(function() {
				window.location.href = '${contextPath}/mvc/r/home?op=view&docName=index';
			});
			<c:if test="${refresh}">
			setInterval(function() { location.reload(); }, ${frequency} * 1000);
			</c:if>
		});
	</script>
</body>
</html>
