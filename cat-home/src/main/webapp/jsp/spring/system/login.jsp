<%@ page contentType="text/html; charset=utf-8" %>
<%
	String contextPath = request.getContextPath();
	String rtnUrl = request.getParameter("rtnUrl");
	String error = (String) request.getAttribute("loginError");

	if (rtnUrl == null || rtnUrl.length() == 0) {
		rtnUrl = contextPath + "/mvc/r/home";
	}
%>
<html lang="en">
	<head>
		<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1" />
		<meta charset="utf-8" />
		<title>CAT</title>

		<meta name="description" content="User login page" />
		<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0" />
		<link rel="stylesheet" href="<%=contextPath%>/assets/css/bootstrap.min.css" />
		<link rel="stylesheet" href="<%=contextPath%>/assets/css/font-awesome.min.css" />
		<link rel="stylesheet" href="<%=contextPath%>/assets/css/ace-fonts.css" />
		<link rel="stylesheet" href="<%=contextPath%>/assets/css/ace.min.css" />
		<link rel="stylesheet" href="<%=contextPath%>/assets/css/ace-rtl.min.css" />
		<link rel="stylesheet" href="<%=contextPath%>/assets/css/ace.onpage-help.css" />
	</head>

	<body class="login-layout">
		<div class="main-container">
			<div class="main-content">
				<div class="row">
					<div class="col-sm-10 col-sm-offset-1">
						<div class="login-container">
							<div class="center">
								<h1>
									<i class="ace-icon fa fa-leaf green"></i>
									<span class="red">CAT</span>
									<span class="white" id="id-text2">Application</span>
								</h1>
							</div>

							<div class="space-6"></div>

							<div class="position-relative">
								<div id="login-box" class="login-box visible widget-box no-border">
									<div class="widget-body">
										<div class="widget-main">
											<h4 class="header blue lighter bigger">
												<i class="ace-icon fa fa-coffee green"></i>
												CAT 管理员账号登录
											</h4>

											<% if (error != null) { %>
											<div class="alert alert-danger">登录失败，请检查账号和密码。</div>
											<% } %>

											<div class="space-6"></div>
											<form class="form-horizontal" name="login" id="form" method="post" action="<%=contextPath%>/mvc/s/login">
												<fieldset>
													<label class="block clearfix">
														<span class="block input-icon input-icon-right">
															<input type="text" name="account" class="form-control" placeholder="Username" />
															<i class="ace-icon fa fa-user"></i>
														</span>
													</label>

													<label class="block clearfix">
														<span class="block input-icon input-icon-right">
															<input type="password" name="password" class="form-control" placeholder="Password" />
															<i class="ace-icon fa fa-lock"></i>
														</span>
													</label>

													<div class="space">
														<input type="hidden" id="rtnUrl" name="rtnUrl" value="<%=rtnUrl%>" />
													</div>

													<div class="clearfix text-right">
														<input type="submit" class="btn btn-primary" name="login" value="登录" />
													</div>

													<div class="space-4"></div>
												</fieldset>
											</form>

											<div class="space-6"></div>
										</div>
									</div>
								</div>
							</div>

							<div class="navbar-fixed-top align-right">
								<br />
								&nbsp;
								<a id="btn-login-dark" href="#">Dark</a>
								&nbsp;
								<span class="blue">/</span>
								&nbsp;
								<a id="btn-login-blur" href="#">Blur</a>
								&nbsp;
								<span class="blue">/</span>
								&nbsp;
								<a id="btn-login-light" href="#">Light</a>
								&nbsp; &nbsp; &nbsp;
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>

		<script type="text/javascript">
			window.jQuery || document.write("<script src='<%=contextPath%>/assets/js/jquery.min.js'>"+"<"+"/script>");
		</script>
		<script type="text/javascript">
			if('ontouchstart' in document.documentElement) document.write("<script src='<%=contextPath%>/assets/js/jquery.mobile.custom.min.js'>"+"<"+"/script>");
		</script>
		<script type="text/javascript">
			jQuery(function($) {
				$('#btn-login-dark').on('click', function(e) {
					$('body').attr('class', 'login-layout');
					$('#id-text2').attr('class', 'white');
					e.preventDefault();
				});
				$('#btn-login-light').on('click', function(e) {
					$('body').attr('class', 'login-layout light-login');
					$('#id-text2').attr('class', 'grey');
					e.preventDefault();
				});
				$('#btn-login-blur').on('click', function(e) {
					$('body').attr('class', 'login-layout blur-login');
					$('#id-text2').attr('class', 'white');
					e.preventDefault();
				});
			});
		</script>
	</body>
</html>
