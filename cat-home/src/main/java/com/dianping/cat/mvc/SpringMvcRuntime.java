package com.dianping.cat.mvc;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.unidal.web.lifecycle.ActionResolver;
import org.unidal.web.lifecycle.DefaultActionResolver;
import org.unidal.web.lifecycle.UrlMapping;
import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.ActionException;
import org.unidal.web.mvc.ActionPayload;
import org.unidal.web.mvc.Page;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.PayloadProvider;
import org.unidal.web.mvc.Validator;
import org.unidal.web.mvc.annotation.ErrorActionMeta;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.ModuleMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;
import org.unidal.web.mvc.annotation.PreInboundActionMeta;
import org.unidal.web.mvc.annotation.TransitionMeta;
import org.unidal.web.mvc.annotation.ValidationMeta;
import org.unidal.web.mvc.lifecycle.RequestContext;
import org.unidal.web.mvc.model.entity.ErrorModel;
import org.unidal.web.mvc.model.entity.InboundActionModel;
import org.unidal.web.mvc.model.entity.ModuleModel;
import org.unidal.web.mvc.model.entity.OutboundActionModel;
import org.unidal.web.mvc.model.entity.TransitionModel;
import org.unidal.web.mvc.payload.DefaultParameterProvider;
import org.unidal.web.mvc.payload.DefaultPayloadProvider;
import org.unidal.web.mvc.payload.MultipartParameterProvider;
import org.unidal.web.mvc.payload.ParameterProvider;
import org.unidal.web.mvc.payload.UrlEncodedParameterProvider;
import org.unidal.web.mvc.payload.annotation.PayloadProviderMeta;

import com.dianping.cat.Cat;
import com.dianping.cat.CatClientConstants;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.context.TraceContextHelper;
import com.dianping.cat.message.internal.NullMessage;
import com.dianping.cat.report.ReportModule;
import com.dianping.cat.system.SystemModule;

@SuppressWarnings({ "rawtypes", "unchecked" })
final class SpringMvcRuntime {
	private static final Logger LOGGER = LoggerFactory.getLogger(SpringMvcRuntime.class);

	private final ActionResolver m_actionResolver = new DefaultActionResolver();

	private final ApplicationContext m_applicationContext;

	private final PayloadProvider m_defaultPayloadProvider = new DefaultPayloadProvider();

	private final ModuleModel m_defaultModule;

	private final Map<String, List<ModuleModel>> m_modules = new HashMap<String, List<ModuleModel>>();

	private final ServletContext m_servletContext;

	SpringMvcRuntime(ApplicationContext applicationContext, ServletContext servletContext) {
		m_applicationContext = applicationContext;
		m_servletContext = servletContext;
		m_defaultModule = register(new ReportModule(), true);
		register(new SystemModule(), false);
		LOGGER.info("Spring MVC runtime initialized, modules={}.", m_modules.keySet());
	}

	private void assertErrorExists(ModuleModel module, String errorActionName) {
		if (!module.getErrors().containsKey(errorActionName)) {
			throw new IllegalArgumentException("No method annotated by @" + ErrorActionMeta.class.getSimpleName()
			      + "(name = \"" + errorActionName + "\") defined in " + module.getModuleClass());
		}
	}

	private void assertParameter(Method method) {
		Class<?>[] parameters = method.getParameterTypes();

		if (parameters.length != 1 || !ActionContext.class.isAssignableFrom(parameters[0])) {
			throw new IllegalArgumentException("Method must accept one ActionContext parameter: " + method);
		}
	}

	private void assertTransitionExists(ModuleModel module, String transitionName) {
		if (!module.getTransitions().containsKey(transitionName)) {
			throw new IllegalArgumentException("No method annotated by @" + TransitionMeta.class.getSimpleName()
			      + "(name = \"" + transitionName + "\") in " + module.getModuleClass());
		}
	}

	private ModuleModel build(org.unidal.web.mvc.Module module) {
		Class<?> moduleClass = module.getClass();
		ModuleMeta moduleMeta = moduleClass.getAnnotation(ModuleMeta.class);

		if (moduleMeta == null) {
			throw new IllegalArgumentException(moduleClass + " must be annotated by " + ModuleMeta.class);
		}

		ModuleModel model = new ModuleModel();

		model.setModuleName(moduleMeta.name());
		model.setModuleClass(moduleClass);
		model.setDefaultInboundActionName(moduleMeta.defaultInboundAction());
		model.setDefaultTransitionName(moduleMeta.defaultTransition());
		model.setDefaultErrorActionName(moduleMeta.defaultErrorAction());
		model.setActionResolverInstance(m_actionResolver);
		model.setModuleInstance(module);

		buildModuleFromMethods(model, moduleClass.getMethods(), module);

		Class<? extends PageHandler<?>>[] handlerClasses = module.getPageHandlers();

		if (handlerClasses != null) {
			for (Class<? extends PageHandler<?>> handlerClass : handlerClasses) {
				PageHandler<?> handler = getBeanOrCreate(handlerClass);

				buildModuleFromMethods(model, handlerClass.getMethods(), handler);
			}
		}

		validateModule(model);
		return model;
	}

	private ErrorModel buildError(Method method, ErrorActionMeta errorMeta) {
		assertParameter(method);

		ErrorModel error = new ErrorModel();

		error.setActionName(errorMeta.name());
		error.setMethod(method);
		return error;
	}

	private InboundActionModel buildInbound(ModuleModel module, Method method, InboundActionMeta inMeta,
	      PreInboundActionMeta preInMeta) {
		if (preInMeta != null && inMeta == null) {
			throw new IllegalArgumentException(PreInboundActionMeta.class + " can only be used with "
			      + InboundActionMeta.class + " for " + method);
		}

		InboundActionModel existing = module.getInbounds().get(inMeta.name());

		if (existing != null) {
			return existing;
		}

		assertParameter(method);

		InboundActionModel inbound = new InboundActionModel();

		inbound.setActionName(inMeta.name());
		inbound.setTransitionName(isEmpty(inMeta.transition()) ? module.getDefaultTransitionName() : inMeta.transition());
		inbound.setErrorActionName(isEmpty(inMeta.errorAction()) ? module.getDefaultErrorActionName() : inMeta
		      .errorAction());
		inbound.setActionMethod(method);
		inbound.setContextClass(method.getParameterTypes()[0]);

		if (preInMeta != null) {
			inbound.setPreActionNames(preInMeta.value());
		}

		PayloadMeta payloadMeta = method.getAnnotation(PayloadMeta.class);

		if (payloadMeta != null) {
			inbound.setPayloadClass(payloadMeta.value());
			registerPayload(payloadMeta.value());
		}

		ValidationMeta moduleValidationMeta = module.getModuleClass().getAnnotation(ValidationMeta.class);

		if (moduleValidationMeta != null) {
			for (Class<?> validationClass : moduleValidationMeta.value()) {
				inbound.addValidationClass(validationClass);
			}
		}

		ValidationMeta actionValidationMeta = method.getAnnotation(ValidationMeta.class);

		if (actionValidationMeta != null) {
			for (Class<?> validationClass : actionValidationMeta.value()) {
				inbound.addValidationClass(validationClass);
			}
		}

		return inbound;
	}

	private void buildModuleFromMethods(ModuleModel module, Method[] methods, Object instance) {
		for (Method method : methods) {
			int modifier = method.getModifiers();

			if (Modifier.isStatic(modifier) || Modifier.isAbstract(modifier) || method.isBridge() || method.isSynthetic()) {
				continue;
			}

			InboundActionMeta inMeta = method.getAnnotation(InboundActionMeta.class);
			PreInboundActionMeta preInMeta = method.getAnnotation(PreInboundActionMeta.class);
			OutboundActionMeta outMeta = method.getAnnotation(OutboundActionMeta.class);
			TransitionMeta transitionMeta = method.getAnnotation(TransitionMeta.class);
			ErrorActionMeta errorMeta = method.getAnnotation(ErrorActionMeta.class);
			int count = (inMeta == null ? 0 : 1) + (outMeta == null ? 0 : 1) + (transitionMeta == null ? 0 : 1)
			      + (errorMeta == null ? 0 : 1);

			if (count == 0) {
				continue;
			} else if (count > 1) {
				throw new IllegalArgumentException(method + " can only be annotated by one MVC action annotation.");
			}

			if (inMeta != null) {
				InboundActionModel inbound = buildInbound(module, method, inMeta, preInMeta);

				inbound.setModuleInstance(instance);
				module.addInbound(inbound);
			} else if (outMeta != null) {
				OutboundActionModel outbound = new OutboundActionModel();

				assertParameter(method);
				outbound.setActionName(outMeta.name());
				outbound.setMethod(method);
				outbound.setModuleInstance(instance);
				module.addOutbound(outbound);
			} else if (transitionMeta != null) {
				TransitionModel transition = new TransitionModel();

				assertParameter(method);
				transition.setTransitionName(transitionMeta.name());
				transition.setMethod(method);
				transition.setModuleInstance(instance);

				if (!module.getTransitions().containsKey(transition.getTransitionName())) {
					module.addTransition(transition);
				}
			} else if (errorMeta != null) {
				ErrorModel error = buildError(method, errorMeta);

				error.setModuleInstance(instance);

				if (!module.getErrors().containsKey(error.getActionName())) {
					module.addError(error);
				}
			}
		}
	}

	private ParameterProvider buildParameterProvider(HttpServletRequest request) {
		ParameterProvider provider;
		String contentType = request.getContentType();
		String mimeType = getMimeType(contentType);

		if ("multipart/form-data".equals(mimeType)) {
			provider = new MultipartParameterProvider();
		} else if ("application/x-www-form-urlencoded".equals(mimeType)) {
			provider = new UrlEncodedParameterProvider();
		} else {
			provider = new DefaultParameterProvider();
		}

		provider.setRequest(request);
		return provider;
	}

	private ActionContext<?> createActionContext(HttpServletRequest request, HttpServletResponse response,
	      RequestContext requestContext, InboundActionModel inboundAction) {
		ActionContext<?> context = (ActionContext<?>) createInstance(inboundAction.getContextClass());

		context.initialize(request, response);
		context.setRequestContext(requestContext);
		context.setInboundPage(inboundAction.getActionName());
		context.setOutboundPage(inboundAction.getActionName());
		context.setServletContext(m_servletContext);
		return context;
	}

	private <T> T getBeanOrCreate(Class<T> type) {
		try {
			return m_applicationContext.getBean(type);
		} catch (NoSuchBeanDefinitionException e) {
			LOGGER.warn("No Spring bean found for {}, creating a plain instance.", type.getName());
			return type.cast(createInstance(type));
		}
	}

	private String getMimeType(String contentType) {
		if (contentType != null) {
			int pos = contentType.indexOf(';');

			return pos > 0 ? contentType.substring(0, pos) : contentType;
		}

		return "application/x-www-form-urlencoded";
	}

	private ModuleModel getModule(String moduleName, String action) {
		List<ModuleModel> list = m_modules.get(moduleName);

		if (list != null) {
			for (ModuleModel module : list) {
				if (module.findInbound(action) != null) {
					return module;
				}
			}

			return list.get(0);
		}

		return m_defaultModule;
	}

	private void handleException(HttpServletRequest request, Throwable e, ActionContext<?> actionContext) {
		RequestContext requestContext = actionContext.getRequestContext();
		ErrorModel error = requestContext.getError();

		if (error != null) {
			try {
				actionContext.setException(e);
				invokeMethod(error.getMethod(), error.getModuleInstance(), actionContext);
				actionContext.setException(null);
			} catch (RuntimeException re) {
				Cat.logError(re);
				throw re;
			}
		} else {
			LOGGER.error(e.getMessage(), e);
		}

		if (!actionContext.isProcessStopped()) {
			request.setAttribute(CatClientConstants.CAT_STATE, e.getClass().getSimpleName());
			Cat.logError(e);

			if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			}

			throw new RuntimeException(e.getMessage(), e);
		}
	}

	void handle(HttpServletRequest request, HttpServletResponse response) throws IOException {
		RequestContext requestContext = buildRequestContext(request);

		if (requestContext == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Not found");
			return;
		}

		try {
			handleRequest(request, response, requestContext);
		} catch (Throwable e) {
			String message = "Error occured when handling uri: " + request.getRequestURI();

			LOGGER.error(message, e);

			if (!response.isCommitted()) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message);
			}
		}
	}

	private RequestContext buildRequestContext(HttpServletRequest request) {
		ParameterProvider provider = buildParameterProvider(request);
		String moduleName = provider.getModuleName();
		UrlMapping urlMapping = m_actionResolver.parseUrl(provider);
		String action = urlMapping.getAction();
		ModuleModel module = getModule(moduleName, action);
		InboundActionModel inboundAction = module.getInbounds().get(action);

		if (inboundAction == null && module.getDefaultInboundActionName() != null) {
			inboundAction = module.getInbounds().get(module.getDefaultInboundActionName());
		}

		if (inboundAction == null) {
			return null;
		}

		RequestContext context = new RequestContext();

		urlMapping.setModule(module.getModuleName());
		context.setActionResolver(m_actionResolver);
		context.setParameterProvider(provider);
		context.setUrlMapping(urlMapping);
		context.setModule(module);
		context.setInboundAction(inboundAction);
		context.setTransition(module.findTransition(inboundAction.getTransitionName()));
		context.setError(module.findError(inboundAction.getErrorActionName()));
		return context;
	}

	private void handleInboundAction(ActionContext<?> actionContext) throws ActionException {
		InboundActionModel inboundAction = actionContext.getRequestContext().getInboundAction();

		try {
			invokeMethod(inboundAction.getActionMethod(), inboundAction.getModuleInstance(), actionContext);
		} catch (RuntimeException e) {
			throw new ActionException("Error occured during handling inbound action(" + inboundAction.getActionName()
			      + ")!", e);
		}
	}

	private void handleOutboundAction(ModuleModel module, ActionContext<?> actionContext) throws ActionException {
		String outboundActionName = actionContext.getOutboundAction();
		OutboundActionModel outboundAction = module.getOutbounds().get(outboundActionName);

		if (outboundAction == null) {
			throw new ActionException("No outbound action found: " + outboundActionName + " in " + module.getModuleClass());
		}

		try {
			invokeMethod(outboundAction.getMethod(), outboundAction.getModuleInstance(), actionContext);
		} catch (RuntimeException e) {
			throw new ActionException("Error occured during handling outbound action(" + outboundActionName + ")", e);
		}
	}

	private boolean handlePreActions(HttpServletRequest request, HttpServletResponse response, ModuleModel module,
	      RequestContext requestContext, InboundActionModel inboundAction, ActionContext<?> actionContext) {
		if (inboundAction.getPreActionNames() != null) {
			for (String actionName : inboundAction.getPreActionNames()) {
				InboundActionModel action = module.getInbounds().get(actionName);
				ActionContext<?> ctx = createActionContext(request, response, requestContext, action);

				ctx.setParent(actionContext);

				try {
					preparePayload(ctx);
					handleInboundAction(ctx);

					if (!ctx.isProcessStopped() && !ctx.isSkipAction()) {
						continue;
					}

					if (ctx.isSkipAction()) {
						handleOutboundAction(module, ctx);
					}
				} catch (ActionException e) {
					handleException(request, e, ctx);
				}

				return false;
			}
		}

		return true;
	}

	private void handleRequest(HttpServletRequest request, HttpServletResponse response, RequestContext requestContext)
	      throws IOException {
		ModuleModel module = requestContext.getModule();
		InboundActionModel inboundAction = requestContext.getInboundAction();
		ActionContext<?> actionContext = createActionContext(request, response, requestContext, inboundAction);
		Transaction transaction = TraceContextHelper.threadLocal().peekTransaction();

		if (transaction == null) {
			transaction = NullMessage.TRANSACTION;
		}

		request.setAttribute(CatClientConstants.CAT_PAGE_URI, actionContext.getRequestContext()
		      .getActionUri(inboundAction.getActionName()));

		try {
			preparePayload(actionContext);

			if (!handlePreActions(request, response, module, requestContext, inboundAction, actionContext)) {
				return;
			}

			handleInboundAction(actionContext);
			transaction.addData("module", module.getModuleName());
			transaction.addData("in", actionContext.getInboundAction());

			if (actionContext.isProcessStopped()) {
				transaction.addData("processStopped=true");
				return;
			}

			handleTransition(actionContext);
			transaction.addData("out", actionContext.getOutboundAction());
			handleOutboundAction(module, actionContext);
		} catch (Throwable e) {
			handleException(request, e, actionContext);
		}
	}

	private void handleTransition(ActionContext<?> actionContext) throws ActionException {
		TransitionModel transition = actionContext.getRequestContext().getTransition();

		try {
			invokeMethod(transition.getMethod(), transition.getModuleInstance(), actionContext);
		} catch (RuntimeException e) {
			throw new ActionException("Error occured during handling transition(" + transition.getTransitionName() + ")", e);
		}
	}

	private void preparePayload(ActionContext<?> ctx) {
		InboundActionModel inboundAction = ctx.getRequestContext().getInboundAction();
		Class<?> payloadClass = inboundAction.getPayloadClass();

		if (payloadClass != null && ctx.getPayload() == null) {
			ActionPayload payload = (ActionPayload) createInstance(payloadClass);
			PayloadProvider provider = getPayloadProvider(payloadClass);

			payload.setPage(ctx.getRequestContext().getAction());
			provider.process(ctx.getRequestContext().getUrlMapping(), ctx.getRequestContext().getParameterProvider(), payload);
			payload.validate(ctx);
			((ActionContext) ctx).setPayload(payload);
		}

		for (Class<?> validatorClass : inboundAction.getValidationClasses()) {
			Validator<ActionContext<?>> validator = (Validator<ActionContext<?>>) getBeanOrCreate(validatorClass);

			try {
				validator.validate(ctx);
			} catch (Exception e) {
				throw new RuntimeException("Error occured during validating " + validatorClass.getName(), e);
			}
		}
	}

	private Object createInstance(Class<?> type) {
		try {
			Constructor<?> constructor = type.getDeclaredConstructor();

			constructor.setAccessible(true);
			return constructor.newInstance();
		} catch (InvocationTargetException e) {
			Throwable cause = e.getCause();

			if (cause instanceof RuntimeException) {
				throw (RuntimeException) cause;
			} else if (cause instanceof Error) {
				throw (Error) cause;
			}
			throw new RuntimeException("Unable to create instance: " + type.getName(), cause);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException("Unable to create instance: " + type.getName(), e);
		}
	}

	private static boolean isEmpty(String value) {
		return value == null || value.length() == 0;
	}

	private static boolean isNotEmpty(String value) {
		return !isEmpty(value);
	}

	private Object invokeMethod(Method method, Object instance, Object... args) {
		try {
			method.setAccessible(true);
			return method.invoke(instance, args);
		} catch (InvocationTargetException e) {
			Throwable cause = e.getCause();

			if (cause instanceof RuntimeException) {
				throw (RuntimeException) cause;
			} else if (cause instanceof Error) {
				throw (Error) cause;
			}
			throw new RuntimeException("Error occured during invoking method: " + method + " with parameters("
			      + Arrays.toString(args) + ")", cause);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException("Error occured during invoking method: " + method + " with parameters("
			      + Arrays.toString(args) + ")", e);
		}
	}

	private ModuleModel register(org.unidal.web.mvc.Module module, boolean defaultModule) {
		ModuleModel model = build(module);
		List<ModuleModel> list = m_modules.get(model.getModuleName());

		if (list == null) {
			list = new ArrayList<ModuleModel>();
			m_modules.put(model.getModuleName(), list);
		}

		list.add(model);

		if (defaultModule) {
			return model;
		}

		return null;
	}

	private void registerPayload(Class<?> payloadClass) {
		m_defaultPayloadProvider.register(payloadClass);
	}

	private PayloadProvider getPayloadProvider(Class<?> payloadClass) {
		PayloadProviderMeta providerMeta = payloadClass.getAnnotation(PayloadProviderMeta.class);

		if (providerMeta == null) {
			return m_defaultPayloadProvider;
		}

		PayloadProvider provider = (PayloadProvider) getBeanOrCreate(providerMeta.value());

		provider.register(payloadClass);
		return provider;
	}

	private void validateModule(ModuleModel module) {
		if (isNotEmpty(module.getDefaultTransitionName())) {
			assertTransitionExists(module, module.getDefaultTransitionName());
		}

		if (isNotEmpty(module.getDefaultErrorActionName())) {
			assertErrorExists(module, module.getDefaultErrorActionName());
		}

		for (InboundActionModel inbound : module.getInbounds().values()) {
			if (isEmpty(inbound.getTransitionName())) {
				TransitionModel transition = module.findTransition("default");

				if (transition != null) {
					inbound.setTransitionName("default");
				} else {
					throw new IllegalArgumentException("Please specify transition() of @"
					      + InboundActionMeta.class.getSimpleName() + " of " + inbound.getActionMethod());
				}
			} else {
				assertTransitionExists(module, inbound.getTransitionName());
			}

			if (isEmpty(inbound.getErrorActionName())) {
				ErrorModel error = module.findError("default");

				if (error != null) {
					inbound.setErrorActionName("default");
				}
			} else {
				assertErrorExists(module, inbound.getErrorActionName());
			}
		}
	}
}
