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
package com.dianping.cat.alarm.spi.sender;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

import org.slf4j.LoggerFactory;

import com.dianping.cat.Cat;
import com.dianping.cat.alarm.spi.config.SenderConfigManager;

public abstract class AbstractSender implements Sender {
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(AbstractSender.class);

    protected SenderConfigManager m_senderConfigManager;

    public void setSenderConfigManager(SenderConfigManager senderConfigManager) {
        m_senderConfigManager = senderConfigManager;
    }

    private boolean httpGetSend(String successCode, String urlPrefix, String urlPars) {
        URL url = null;
        InputStream in = null;
        URLConnection conn = null;
        boolean sendSuccess = false;

        try {
            url = new URL(urlPrefix + "?" + urlPars);
            conn = url.openConnection();

            conn.setConnectTimeout(2000);
            conn.setReadTimeout(3000);

            in = conn.getInputStream();
            String response = new String(in.readAllBytes(), StandardCharsets.UTF_8);

            if (response.contains(successCode)) {
                sendSuccess = true;
                return true;
            } else {
                LOGGER.warn("Alert HTTP GET returned unexpected response, urlPrefix={}, successCode={}, response={}.",
                        urlPrefix, successCode, response);
                return false;
            }
        } catch (Exception e) {
            LOGGER.error("Unable to send alert by HTTP GET, urlPrefix={}, urlPars={}.", urlPrefix, urlPars, e);
            return false;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                LOGGER.warn("Unable to close alert HTTP GET response stream, urlPrefix={}.", urlPrefix, e);
            }
            if (!sendSuccess) {
                recordSendLog(urlPrefix, urlPars);
            }
        }
    }

    private boolean httpPostSend(String successCode, String urlPrefix, String content) {
        URL url = null;
        InputStream in = null;
        OutputStreamWriter writer = null;
        URLConnection conn = null;
        boolean sendSuccess = false;

        try {
            url = new URL(urlPrefix);
            conn = url.openConnection();

            conn.setConnectTimeout(2000);
            conn.setReadTimeout(3000);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestProperty("content-type", "application/x-www-form-urlencoded;charset=UTF-8");
            writer = new OutputStreamWriter(conn.getOutputStream());

            writer.write(content);
            writer.flush();

            in = conn.getInputStream();
            String response = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            if (response.contains(successCode)) {
                sendSuccess = true;
                return true;
            } else {
                LOGGER.warn("Alert HTTP POST returned unexpected response, urlPrefix={}, successCode={}, response={}.",
                        urlPrefix, successCode, response);
                return false;
            }
        } catch (Exception e) {
            LOGGER.error("Unable to send alert by HTTP POST, urlPrefix={}, contentLength={}.", urlPrefix,
                    content == null ? 0 : content.length(), e);
            return false;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                LOGGER.warn("Unable to close alert HTTP POST resources, urlPrefix={}.", urlPrefix, e);
            }
            if (!sendSuccess) {
                recordSendLog(urlPrefix, content);
            }
        }
    }

    public boolean httpSend(String successCode, String type, String urlPrefix, String urlPars) {
        if ("get".equalsIgnoreCase(type)) {
            return httpGetSend(successCode, urlPrefix, urlPars);
        } else if ("post".equalsIgnoreCase(type)) {
            return httpPostSend(successCode, urlPrefix, urlPars);
        } else {
            LOGGER.error("Illegal alert sender request type, type={}, urlPrefix={}.", type, urlPrefix);
            Cat.logError(new RuntimeException("Illegal request type: " + type));
            return false;
        }
    }

    public com.dianping.cat.alarm.sender.entity.Sender querySender() {
        String id = getId();

        return m_senderConfigManager.querySender(id);
    }

    private void recordSendLog(String urlPrefix, String paras) {
        LOGGER.error("Alert send failed, urlPrefix={}, paras={}.", urlPrefix, paras, new AlertSendException());
        Cat.logError(urlPrefix + "---" + paras, new AlertSendException());
    }

    private class AlertSendException extends Exception {

      private static final long serialVersionUID = 1L;

    }

}
