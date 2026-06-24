<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:choose>
	<c:when test="${empty logView}">
		<div class="error">Sorry, this message had already been archived.</div>
	</c:when>
	<c:otherwise>
		<c:out value="${logView}" escapeXml="false" />
	</c:otherwise>
</c:choose>
