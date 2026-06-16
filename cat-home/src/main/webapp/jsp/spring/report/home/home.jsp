<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8" %>
<%
	String contextPath = request.getContextPath();
	String docName = (String) request.getAttribute("docName");
	String runtime = (String) request.getAttribute("runtime");
	String legacyHomeUrl = (String) request.getAttribute("legacyHomeUrl");
	String loginUrl = (String) request.getAttribute("loginUrl");

	if (docName == null || docName.length() == 0) {
		docName = "index";
	}
%>
<!doctype html>
<html>
<head>
	<meta charset="utf-8">
	<title>CAT Spring MVC Home</title>
	<link rel="stylesheet" href="<%=contextPath%>/assets/css/bootstrap.min.css">
	<style>
		body {
			padding: 24px;
		}

		.page-header {
			margin-top: 0;
		}

		.migration-links a {
			margin-right: 12px;
		}
	</style>
</head>
<body>
	<div class="page-header">
		<h3>CAT Home</h3>
	</div>
	<div class="alert alert-info">
		<strong>Runtime:</strong> <%=runtime%>
	</div>
	<table class="table table-bordered table-striped">
		<tbody>
			<tr>
				<th style="width: 180px;">Path</th>
				<td><%=request.getRequestURI()%></td>
			</tr>
			<tr>
				<th>Document</th>
				<td><%=docName%></td>
			</tr>
		</tbody>
	</table>
	<p class="migration-links">
		<a class="btn btn-primary" href="<%=legacyHomeUrl%>">Open legacy home</a>
		<a class="btn btn-default" href="<%=loginUrl%>">Login</a>
	</p>
</body>
</html>
