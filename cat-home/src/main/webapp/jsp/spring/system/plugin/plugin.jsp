<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html>
<head>
	<meta charset="utf-8">
	<title>CAT Plugin</title>
	<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/bootstrap.min.css">
	<style>
		body {
			padding: 24px;
		}

		.page-header {
			margin-top: 0;
		}

		.plugin-actions a {
			margin-right: 12px;
		}
	</style>
</head>
<body>
	<div class="page-header">
		<h3>CAT Plugin</h3>
	</div>
	<div class="alert alert-info">
		<strong>Runtime:</strong> <c:out value="${runtime}" />
	</div>
	<table class="table table-bordered table-striped">
		<tbody>
			<tr>
				<th style="width: 180px;">Path</th>
				<td><c:out value="${pageContext.request.requestURI}" /></td>
			</tr>
			<tr>
				<th>Legacy Path</th>
				<td><c:out value="${legacyPluginUrl}" /></td>
			</tr>
		</tbody>
	</table>
	<p class="plugin-actions">
		<a class="btn btn-primary" href="${chromeExtensionUrl}">Chrome extension</a>
		<a class="btn btn-default" href="${chromeSourceUrl}">Source zip</a>
		<a class="btn btn-default" href="${chromeMappingUrl}">Server mapping</a>
		<a class="btn btn-default" href="${legacyChromeExtensionUrl}">Legacy chrome</a>
		<a class="btn btn-default" href="${homeUrl}">Home</a>
	</p>
</body>
</html>
