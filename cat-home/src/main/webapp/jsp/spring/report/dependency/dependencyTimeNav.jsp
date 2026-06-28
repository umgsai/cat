<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<ul class="pagination">
	<c:forEach var="item" items="${minutes}">
		<li id="minute${item}" class="${item > maxMinute ? 'disabled' : ''}">
			<a class="href${item}" href="${baseUri}?op=${action}&domain=${domain}&date=${date}&minute=${item}&productLine=${productLine}&fullScreen=${fullScreen}&refresh=${refresh}&frequency=${frequency}">
				<c:if test="${item < 10}">0${item}</c:if><c:if test="${item >= 10}">${item}</c:if>
			</a>
		</li>
	</c:forEach>
</ul>
<script type="text/javascript">
	$('.href${minute}').css('color', 'red');
	$('.href${minute}').css('font-weight', 'bold');
</script>
