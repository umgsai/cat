/*
 * Copyright (c) 2011-2018, Meituan Dianping. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.Component;
import org.unidal.web.configuration.AbstractWebComponentsConfigurator;
import org.unidal.web.lifecycle.DefaultActionResolver;
import org.unidal.web.lifecycle.RequestLifecycle;
import org.unidal.web.mvc.lifecycle.ActionHandlerManager;
import org.unidal.web.mvc.lifecycle.DefaultActionHandlerManager;
import org.unidal.web.mvc.lifecycle.DefaultErrorHandler;
import org.unidal.web.mvc.lifecycle.DefaultInboundActionHandler;
import org.unidal.web.mvc.lifecycle.DefaultOutboundActionHandler;
import org.unidal.web.mvc.lifecycle.DefaultRequestContextBuilder;
import org.unidal.web.mvc.lifecycle.DefaultRequestLifecycle;
import org.unidal.web.mvc.lifecycle.DefaultTransitionHandler;
import org.unidal.web.mvc.lifecycle.RequestContextBuilder;
import org.unidal.web.mvc.model.AnnotationMatrix;
import org.unidal.web.mvc.model.ModelManager;
import org.unidal.web.mvc.model.ModuleRegistry;
import org.unidal.web.mvc.payload.DefaultParameterProvider;
import org.unidal.web.mvc.payload.DefaultPayloadProvider;
import org.unidal.web.mvc.payload.MultipartParameterProvider;
import org.unidal.web.mvc.payload.UrlEncodedParameterProvider;
import org.unidal.web.mvc.view.model.DefaultModelHandler;
import org.unidal.web.mvc.view.model.JsonModelBuilder;
import org.unidal.web.mvc.view.model.ModelHandler;
import org.unidal.web.mvc.view.model.XmlModelBuilder;

import com.dianping.cat.report.ReportModule;
import com.dianping.cat.service.ProjectService;
import com.dianping.cat.system.SystemModule;
import com.dianping.cat.system.page.login.service.SigninService;

class WebComponentConfigurator extends AbstractWebComponentsConfigurator {
	@SuppressWarnings("unchecked")
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		defineWebMvcComponents(all);
		defineModuleRegistry(all, ReportModule.class, ReportModule.class, SystemModule.class);
		replaceSmallSystemPageHandlers(all);

		return all;
	}

	private void defineWebMvcComponents(List<Component> all) {
		all.add(A(AnnotationMatrix.class));
		all.add(C(ModelManager.class).req(ModuleRegistry.class, (String) null, "m_registry"));
		all.add(A(DefaultActionResolver.class));
		all.add(A(DefaultInboundActionHandler.class));
		all.add(A(DefaultOutboundActionHandler.class));
		all.add(A(DefaultTransitionHandler.class));
		all.add(A(DefaultErrorHandler.class));
		all.add(A(DefaultPayloadProvider.class));
		all.add(A(DefaultActionHandlerManager.class));
		all.add(C(RequestLifecycle.class, "mvc", DefaultRequestLifecycle.class) //
								.req(RequestContextBuilder.class, (String) null, "m_builder") //
								.req(ActionHandlerManager.class, (String) null, "m_actionHandlerManager"));
		all.add(C(RequestContextBuilder.class, DefaultRequestContextBuilder.class) //
								.req(ModelManager.class, (String) null, "m_modelManager"));
		all.add(A(UrlEncodedParameterProvider.class));
		all.add(A(MultipartParameterProvider.class));
		all.add(A(DefaultParameterProvider.class));
		all.add(A(DefaultModelHandler.class));
		all.add(A(XmlModelBuilder.class));
		all.add(A(JsonModelBuilder.class));
	}

	private void replaceSmallSystemPageHandlers(List<Component> all) {
		removeComponent(all, com.dianping.cat.system.page.login.Handler.class);
		removeComponent(all, com.dianping.cat.system.page.plugin.Handler.class);
		removeComponent(all, com.dianping.cat.system.page.project.Handler.class);

		all.add(C(com.dianping.cat.system.page.login.Handler.class) //
								.req(com.dianping.cat.system.page.login.JspViewer.class, (String) null, "m_jspViewer") //
								.req(SigninService.class, (String) null, "m_signinService"));
		all.add(C(com.dianping.cat.system.page.login.JspViewer.class).req(ModelHandler.class));
		all.add(C(com.dianping.cat.system.page.plugin.Handler.class) //
								.req(com.dianping.cat.system.page.plugin.JspViewer.class, (String) null, "m_jspViewer"));
		all.add(C(com.dianping.cat.system.page.plugin.JspViewer.class).req(ModelHandler.class));
		all.add(C(com.dianping.cat.system.page.project.Handler.class) //
								.req(ProjectService.class, (String) null, "m_projectService") //
								.req(com.dianping.cat.system.page.project.JspViewer.class, (String) null, "m_jspViewer"));
		all.add(C(com.dianping.cat.system.page.project.JspViewer.class).req(ModelHandler.class));
	}

	private void removeComponent(List<Component> all, Class<?> role) {
		String roleName = role.getName();

		all.removeIf(component -> roleName.equals(component.getModel().getRole()));
	}
}
