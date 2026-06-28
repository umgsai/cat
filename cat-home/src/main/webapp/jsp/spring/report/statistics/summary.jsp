<%@ page session="false" language="java" pageEncoding="UTF-8" %>
<%@ page contentType="text/html; charset=utf-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<form id="form" method="post" action="${baseUri}?op=summary">
	<div style="float:left;">
		&nbsp;&nbsp;时间
		<input type="text" name="summarytime" id="summarytime" value="${summarytimeText}" style="width:130px;">
	</div>
	&nbsp;应用名
	<input type="text" name="summarydomain" id="summarydomain" value="${summarydomain}" style="height:auto" class="input-small">
	发送邮箱
	<input type="text" name="summaryemails" id="summaryemails" value="${summaryemails}" style="height:auto;width:200px" class="input-small" placeholder="用半角逗号分割，可为空">
	<input class="btn btn-primary btn-sm" value="查询" type="submit">
</form>
<c:out value="${summaryContent}" escapeXml="false" />
<script type="text/javascript">
	$(document).ready(function() {
		$('#summarytime').datetimepicker({ format:'Y-m-d H:i', step:30, maxDate:0 });
	});
</script>
