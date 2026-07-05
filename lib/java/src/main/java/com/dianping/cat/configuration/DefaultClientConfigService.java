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
package com.dianping.cat.configuration;

import com.dianping.cat.log.CatLogger;
import com.dianping.cat.util.NetworkHelper;
import com.dianping.cat.util.Properties;
import com.dianping.cat.util.Splitters;
import com.dianping.cat.util.StringUtils;
import com.dianping.cat.Cat;
import com.dianping.cat.analyzer.MetricTagAggregator;
import com.dianping.cat.configuration.client.entity.ClientConfig;
import com.dianping.cat.configuration.client.entity.Server;
import com.dianping.cat.configuration.property.entity.Property;
import com.dianping.cat.configuration.property.entity.PropertyConfig;
import com.dianping.cat.message.spi.MessageTree;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DefaultClientConfigService implements ClientConfigService {
    private ClientConfig config;
    private String routers;
    private volatile double samplingRate = 1d;
    private volatile boolean block = false;
    private volatile int timeout = 1000;
    private MessageTreeTypeParser treeParser = new MessageTreeTypeParser();
    private Map<String, List<Integer>> longConfigs = new LinkedHashMap<String, List<Integer>>();
    private static CatLogger LOGGER = CatLogger.getInstance();
    private static DefaultClientConfigService instance = new DefaultClientConfigService();

    public static DefaultClientConfigService getInstance() {
        return instance;
    }

    private DefaultClientConfigService() {
        String config = System.getProperty(Cat.CLIENT_CONFIG);

        if (StringUtils.isNotEmpty(config)) {
            try {
                this.config = com.dianping.cat.configuration.client.transform.DefaultSaxParser.parse(config);
                LOGGER.info("Setup CAT with config: " + config);
            } catch (Exception e) {
                LOGGER.error("Invalid CAT client config: " + config, e);
            }
        }

        if (this.config == null) {
            String appName = ApplicationEnvironment.loadAppName(Cat.UNKNOWN);
            ClientConfig defaultConfig = ApplicationEnvironment.loadClientConfig(appName);

            defaultConfig.setDomain(appName);
            this.config = defaultConfig;
            LOGGER.info("Setup CAT with default configuration: " + this.config);
        }
    }

    @Override
    public int getClientConnectTimeout() {
        return timeout;
    }

    @Override
    public String getDomain() {
        return config.getDomain();
    }

    @Override
    public int getLongConfigThreshold(String key) {
        List<Integer> values = longConfigs.get(key);
        int value;

        if (values != null && !values.isEmpty()) {
            value = values.get(0);
        } else {
            value = ProblemLongType.findByName(key).getThreshold();
        }

        return value;
    }

    @Override
    public int getLongThresholdByDuration(String key, int duration) {
        List<Integer> values = longConfigs.get(key);

        if (values != null) {
            for (int i = values.size() - 1; i >= 0; i--) {
                int userThreshold = values.get(i);

                if (duration >= userThreshold) {
                    return userThreshold;
                }
            }
        }

        return -1;
    }

    @Override
    public String getRouters() {
        if (routers == null) {
            refreshConfig();
        }
        return routers;
    }

    public double getSamplingRate() {
        return samplingRate;
    }

    private String getServerConfigUrl(ClientConfig config, int index) {
        List<Server> servers = config.getServers();
        int size = servers.size();
        Server server = servers.get(index % size);
        int httpPort = server.getHttpPort();
        String serverIp = server.getIp();

        String hostname = NetworkInterfaceManager.INSTANCE.getLocalHostName();
        String ip = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();

        try {
            hostname = URLEncoder.encode(hostname, "utf-8");
        } catch (UnsupportedEncodingException ignored) {
        }

        return String.format("http://%s:%d/cat/mvc/s/router?domain=%s&ip=%s&op=properties&env=%s&hostname=%s", serverIp.trim(),
                httpPort, getDomain(), ip, ApplicationEnvironment.ENVIRONMENT, hostname);
    }

    @Override
    public List<Server> getServers() {
        return config.getServers();
    }

    private boolean isDevMode() {
        String devMode = Properties.forString().fromEnv().fromSystem().getProperty("devMode", "false");

        return "true".equals(devMode);
    }

    public boolean isMessageBlock() {
        return block;
    }

    @Override
    public MessageType parseMessageType(MessageTree tree) {
        if (!tree.canDiscard()) {
            return MessageType.NORMAL_MESSAGE;
        } else {
            return treeParser.parseMessageType(tree);
        }
    }

    public void refreshConfig() {
        int retry = 0;
        int start = (int) (Math.random() * 10);
        int maxRetryCount = 3;
        boolean refreshStatus = false;

        while (retry < maxRetryCount) {
            String url = getServerConfigUrl(config, start + retry);

            try {
                refreshConfig(url);
                refreshStatus = true;
                LOGGER.info("Connected to CAT server config url, retry={}, url={}.", retry, url);
                break;
            } catch (Exception e) {
                retry++;
                LOGGER.error("Unable to connect to CAT server config url, retry={}, url={}.", retry, url, e);
            }
        }

        if ((!refreshStatus) && (!isDevMode())) {
            try {
                String xml = ApplicationEnvironment.loadRemoteClientConfig();
                ClientConfig config = com.dianping.cat.configuration.client.transform.DefaultSaxParser.parse(xml);

                config.setDomain(getDomain());

                String url = getServerConfigUrl(config, start);

                refreshConfig(url);
            } catch (Exception e) {
                LOGGER.error("Unable to connect to CAT server config url from remote config.", e);
            }
        }
    }

    @Override
    public void refreshConfig(PropertyConfig routerConfig) {
        refreshRouters(routerConfig);
        refreshInnerConfig(routerConfig);
    }

    private void refreshConfig(String url) throws Exception {
        String content = NetworkHelper.readFromUrlWithRetry(url);
        PropertyConfig routerConfig = parseConfig(content.trim());

        //判断客户端routers是否有更新
        if (refreshRouters(routerConfig)) {
            //缓存到client_cache.xml
            storeServersByUrl(url);
        }
        //更新采样率等指标
        refreshInnerConfig(routerConfig);
    }

    static PropertyConfig parseConfig(String content) {
        if (!content.startsWith("{") || !content.endsWith("}")) {
            throw new IllegalArgumentException("Invalid CAT router config: " + content);
        }

        PropertyConfig config = new PropertyConfig();
        String keyValuePairs = content.substring(1, content.length() - 1);
        List<String> items = Splitters.by(',').trim().noEmptyItem().split(keyValuePairs);

        for (String item : items) {
            int index = item.indexOf('=');

            if (index <= 0) {
                throw new IllegalArgumentException("Invalid CAT router property: " + item);
            }

            String key = item.substring(0, index).trim();
            String value = item.substring(index + 1).trim();

            config.addProperty(new Property(key).setValue(value));
        }

        return config;
    }

    private void refreshInnerConfig(PropertyConfig routerConfig) {

        Property sample = routerConfig.findProperty("sample");
        if (null != sample) {
            samplingRate = Double.parseDouble(sample.getValue());
            if (samplingRate <= 0) {
                samplingRate = 0;
            }
        }

        Property blocks = routerConfig.findProperty("block");
        if (null != blocks) {
            block = Boolean.parseBoolean(blocks.getValue());
            if (block) {
                Cat.disable();
            } else {
                Cat.enable();
            }
        }

        Property multiInstance = routerConfig.findProperty("multiInstances");
        if (null != multiInstance) {
            boolean multiInstances = Boolean.parseBoolean(multiInstance.getValue());
            if (multiInstances) {
                Cat.enableMultiInstances();
            } else {
                Cat.disableMultiInstances();
            }
        }

        Property startTransactionTypes = routerConfig.findProperty("startTransactionTypes");
        Property matchTransactionTypes = routerConfig.findProperty("matchTransactionTypes");
        if (null != startTransactionTypes && null != matchTransactionTypes) {
            String startTypes = startTransactionTypes.getValue();
            String matchTypes = matchTransactionTypes.getValue();
            for (ProblemLongType longType : ProblemLongType.values()) {
                final String name = longType.getName();
                String propertyName = name + "s";
                Property property = routerConfig.findProperty(propertyName);

                if (property != null) {
                    String values = property.getValue();

                    if (values != null) {
                        List<String> valueStrs = Splitters.by(',').trim().split(values);
                        List<Integer> thresholds = new LinkedList<Integer>();

                        for (String valueStr : valueStrs) {
                            try {
                                thresholds.add(Integer.parseInt(valueStr));
                            } catch (Exception e) {
                                // ignore
                            }
                        }
                        if (!thresholds.isEmpty()) {
                            longConfigs.put(name, thresholds);
                        }
                    }
                }
            }
            treeParser.refresh(startTypes, matchTypes);
        }

        Property maxMetricProperty = routerConfig.findProperty("maxMetricTagValues");
        if (maxMetricProperty != null) {
            int maxMetricTagValues = Integer.parseInt(maxMetricProperty.getValue());

            if (maxMetricTagValues != MetricTagAggregator.MAX_KEY_SIZE) {
                MetricTagAggregator.MAX_KEY_SIZE = maxMetricTagValues;
            }
        }

        Property timeout = routerConfig.findProperty("clientConnectTimeout");
        if (timeout != null) {
            this.timeout = Integer.parseInt(timeout.getValue());
        }
    }

    private boolean refreshRouters(PropertyConfig routerConfig) {
        String newRouters = routerConfig.findProperty("routers").getValue();
        if ((routers == null) || (!routers.equals(newRouters))) {
            routers = newRouters;
            return true;
        }
        return false;
    }

    public void setSample(double sample) {
        samplingRate = sample;
    }

    private void storeServersByUrl(String url) {
        try {
            URL u = new URL(url);
            int httpPort = u.getPort();
            ApplicationEnvironment.storeServers(routers, httpPort);
        } catch (Exception e) {
            // ignore
        }
    }

}
