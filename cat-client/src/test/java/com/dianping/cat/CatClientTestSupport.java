package com.dianping.cat;

import org.junit.After;
import org.junit.Before;

import com.dianping.cat.component.ComponentContext;
import com.dianping.cat.message.context.MetricContextHelper;
import com.dianping.cat.message.context.TraceContextHelper;

public abstract class CatClientTestSupport {
	protected ComponentContext componentContext() {
		return Cat.getBootstrap().getComponentContext();
	}

	@Before
	public void setUp() throws Exception {
		Cat.destroy();
		Cat.getBootstrap().testMode();
		TraceContextHelper.reset();
		MetricContextHelper.reset();
	}

	@After
	public void tearDown() throws Exception {
		Cat.destroy();
		TraceContextHelper.reset();
		MetricContextHelper.reset();
	}
}
