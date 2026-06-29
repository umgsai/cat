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
package com.dianping.cat.report.page.logview.service;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.unidal.cat.message.storage.hdfs.HdfsBucketManager;

import com.dianping.cat.Cat;
import com.dianping.cat.hadoop.hdfs.HdfsMessageBucketManager;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.codec.HtmlMessageCodec;
import com.dianping.cat.message.codec.WaterfallMessageCodec;
import com.dianping.cat.message.spi.BufReleaseHelper;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.storage.MessageBucketManager;
import com.dianping.cat.message.tree.MessageId;
import com.dianping.cat.report.service.BaseHistoricalModelService;
import com.dianping.cat.report.service.ModelRequest;

@Component("historicalMessageService")
public class HistoricalMessageService extends BaseHistoricalModelService<String> {
	private static final Logger LOGGER = LoggerFactory.getLogger(HistoricalMessageService.class);

	@Resource
	private HdfsBucketManager bucketManager;

	@Resource(name = "hdfsMessageBucketManager")
	private MessageBucketManager hdfsBucketManager;

	private WaterfallMessageCodec waterfallCodec = new WaterfallMessageCodec();

	private HtmlMessageCodec htmlCodec = new HtmlMessageCodec();

	public HistoricalMessageService() {
		super("logview");
	}

	@Override
	protected String buildModel(ModelRequest request) throws Exception {
		String result = buildNewMessageModel(request);

		if (result == null) {
			result = buildOldMessageModel(request);
		}
		return result;
	}

	protected String buildOldMessageModel(ModelRequest request) throws Exception {
		String messageId = request.getProperty("messageId");
		Cat.logEvent("LoadMessage", "messageTree", Event.SUCCESS, messageId);
		if (hdfsBucketManager == null) {
			LOGGER.warn("HDFS message bucket manager is not configured for historical old logview lookup, request={}.",
					request);
			return null;
		}
		MessageTree tree = hdfsBucketManager.loadMessage(messageId);

		if (tree != null) {
			return toString(request, tree);
		} else {
			LOGGER.warn("Historical old logview message not found, messageId={}, request={}.", messageId, request);
			return null;
		}
	}

	protected String buildNewMessageModel(ModelRequest request) throws Exception {
		String messageId = request.getProperty("messageId");
		Cat.logEvent("LoadMessage", "messageTree", Event.SUCCESS, messageId);
		MessageId id = MessageId.parse(messageId);
		if (bucketManager == null) {
			LOGGER.warn("HDFS bucket manager is not configured for historical new logview lookup, request={}.", request);
			return null;
		}
		MessageTree tree = bucketManager.loadMessage(id);

		if (tree != null) {
			return toString(request, tree);
		} else {
			LOGGER.warn("Historical new logview message not found, messageId={}, request={}.", messageId, request);
			return null;
		}
	}

	@Override
	public boolean isEligible(ModelRequest request) {
		boolean eligibale = request.getPeriod().isHistorical();

		return eligibale;
	}

	protected String toString(ModelRequest request, MessageTree tree) {
		ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(8192);

		try {
			if (tree.getMessage() instanceof Transaction && request.getProperty("waterfall", "false").equals("true")) {
				waterfallCodec.encode(tree, buf);
			} else {
				htmlCodec.encode(tree, buf);
			}
			buf.readInt(); // get rid of length
			return buf.toString(StandardCharsets.UTF_8);
		} catch (Exception e) {
			LOGGER.error("Unable to render historical logview message, messageId={}, waterfall={}.",
					request.getProperty("messageId"), request.getProperty("waterfall", "false"), e);
		} finally {
			BufReleaseHelper.release(buf);
		}
		return null;
	}

	public void setBucketManager(HdfsBucketManager bucketManager) {
		this.bucketManager = bucketManager;
	}

	public void setHdfsBucketManager(MessageBucketManager hdfsBucketManager) {
		this.hdfsBucketManager = hdfsBucketManager;
	}
}
