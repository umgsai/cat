<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html lang="en">
<head>
	<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
	<meta charset="utf-8">
	<title>CAT</title>
	<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0">
	<link rel="stylesheet" type="text/css" href="${contextPath}/assets/css/bootstrap.min.css">
	<link rel="stylesheet" type="text/css" href="${contextPath}/assets/css/font-awesome.min.css">
	<link rel="stylesheet" type="text/css" href="${contextPath}/assets/css/ace.min.css" id="main-ace-style">
	<link rel="stylesheet" type="text/css" href="${contextPath}/css/body.css">
	<script src="${contextPath}/assets/js/jquery.min.js"></script>
	<script src="${contextPath}/assets/js/bootstrap.min.js"></script>
	<script src="${contextPath}/assets/js/ace.min.js"></script>
	<script src="${contextPath}/js/jquery.dataTables.min.js"></script>
	<script src="${contextPath}/js/raphael-min.js"></script>
	<script src="${contextPath}/js/startopo.js"></script>
	<script src="${contextPath}/js/dependencyConfig.js"></script>
	<style>
		.pagination { margin: 4px 0; }
		.pagination ul { margin-top: 0px; }
		.pagination ul > li > a, .pagination ul > li > span { padding: 3px 10px; }
	</style>
</head>
<body class="no-skin">
	<c:set var="navbarApplicationDisabled" value="true" scope="request" />
	<jsp:include page="../common/navbar.jsp" />
	<div class="main-container" id="main-container">
		<c:set var="activeReport" value="Dependency" scope="request" />
		<jsp:include page="../common/reportSidebar.jsp" />
		<div class="main-content">
			<div style="padding-top:2px;padding-left:2px;padding-right:8px;">
				<div class="report">
					<div class="breadcrumbs" id="breadcrumbs">
						<span class="text-success">${reportStart} to ${reportEnd}</span>
						<span id="warp_search_group" style="display:inline-block;width:250px;margin-left:8px;vertical-align:middle;white-space:nowrap;">
							<button class="btn btn-sm btn-default" type="button">全部</button><button class="btn btn-sm btn-default" type="button">常用</button><input id="search" type="text" value="${domain}" style="height:30px;width:110px;" autocomplete="off"><button class="btn btn-sm btn-pink" type="button" id="search_go">Go</button>
						</span>
						<span class="pull-right">
							<c:forEach var="nav" items="${navs}">
								[ <a href="${baseUri}?op=dependencyGraph&domain=${domain}&date=${date}&step=${nav.hours}">${nav.title}</a> ]
							</c:forEach>
							[ <a href="${baseUri}?op=dependencyGraph&domain=${domain}">now</a> ]
						</span>
					</div>
					<div class="text-center"><jsp:include page="dependencyTimeNav.jsp" /></div>
					<div class="text-center" id="container" style="margin-left:75px;width:1000px;height:800px;border:solid 1px #ccc;"></div>
				</div>
			</div>
		</div>
	</div>
	<script type="text/javascript">
		$(document).ready(function() {
			var data = ${topologyGraph};
			var nodeSize = 0;
			function parse(data) {
				var nodes = data.nodes;
				var edges = data.edges;
				var points = [];
				var sides = [];
				for (var o in nodes) {
					if (nodes.hasOwnProperty(o)) {
						points.push(nodes[o]);
						nodeSize++;
					}
				}
				for (var e in edges) {
					if (edges.hasOwnProperty(e)) {
						sides.push(edges[e]);
					}
				}
				data.points = points;
				data.sides = sides;
				delete data.nodes;
				delete data.edges;
				return data;
			}
			var convertData = parse(data);
			var defaultWeight = 0.8;
			if (nodeSize > 30) {
				defaultWeight = 0.5;
			} else if (nodeSize > 20) {
				defaultWeight = 0.6;
			} else if (nodeSize > 10) {
				defaultWeight = 0.8;
			} else if (nodeSize > 0) {
				defaultWeight = 1.0;
			}
			try {
				new StarTopo('container', convertData, {
					typeMap:{ database:'rect', project:'circle', service:'lozenge', cache:'lozenge' },
					colorMap:{ "1":'#2fbf2f', "2":'#bfa22f', "3":'#b94a48', "4":'#772fbf' },
					radius:300,
					sideWeight:function(weight) { return weight + 3; },
					nodeWeight:function(weight) { return weight / 5 + defaultWeight; }
				});
			} catch (e) {
				console.log(e);
			}
			$('#search_go').click(function() {
				var target = $('#search').val();
				window.location.href = '${baseUri}?op=dependencyGraph&domain=' + encodeURIComponent(target);
			});
		});
	</script>
</body>
</html>
