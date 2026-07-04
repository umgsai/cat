package com.dianping.cat.configuration;

import java.util.List;

import com.dianping.cat.configuration.model.entity.Host;
import com.dianping.cat.configuration.model.entity.Server;

public interface ConfigureManager {
	boolean getBooleanProperty(String name, boolean defaultValue);

	String getDomain();

	double getDoubleProperty(String name, double defaultValue);

	Host getHost();

	int getIntProperty(String name, int defaultValue);

	long getLongProperty(String name, long defaultValue);

	String getProperty(String name, String defaultValue);

	List<Server> getServers();

	boolean isEnabled();
}
