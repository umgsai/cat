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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unidal.cat.message.storage.Bucket;
import org.unidal.cat.message.storage.BucketManager;
import org.unidal.cat.message.storage.MessageFinderManager;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.dump.DumpAnalyzer;
import com.dianping.cat.consumer.dump.LocalMessageBucketManager;
import com.dianping.cat.message.CodecHandler;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.codec.HtmlMessageCodec;
import com.dianping.cat.message.codec.WaterfallMessageCodec;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.storage.MessageBucketManager;
import com.dianping.cat.message.tree.MessageId;
import com.dianping.cat.mvc.ApiPayload;
import com.dianping.cat.report.service.LocalModelService;
import com.dianping.cat.report.service.ModelPeriod;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;
import com.dianping.cat.report.service.ModelService;

public class LocalMessageService extends LocalModelService<String> implements ModelService<String> {
	private static final Logger LOGGER = LoggerFactory.getLogger(LocalMessageService.class);

	public static final String ID = DumpAnalyzer.ID;

	private MessageFinderManager m_finderManager;

	private BucketManager m_bucketManager;

	private MessageBucketManager m_messageBucketManager;

	private WaterfallMessageCodec m_waterfall = new WaterfallMessageCodec();

	private HtmlMessageCodec m_html = new HtmlMessageCodec();

	public LocalMessageService() {
		super("logview");
	}

	@Override
	public String buildReport(ModelRequest request, ModelPeriod period, String domain, ApiPayload payload)
	      throws Exception {
		String result = buildOldReport(request, period, domain, payload);

		if (result == null) {
			result = buildNewReport(request, period, domain, payload);
		}
		return result;
	}

	private String buildNewReport(ModelRequest request, ModelPeriod period, String domain, ApiPayload payload)
	      throws Exception {
		String messageId = payload.getMessageId();
		boolean waterfall = payload.isWaterfall();
		MessageId id = MessageId.parse(messageId);
		ByteBuf buf = null;
		MessageTree tree = null;
		String localHostAddress = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();

		if (m_finderManager != null) {
			buf = m_finderManager.find(id);
		} else {
			LOGGER.warn("Message finder manager is not configured for local logview lookup, messageId={}, period={}, domain={}.",
					messageId, period, domain);
		}
		if (buf != null) {
			tree = CodecHandler.decode(changeBuf(buf));
		}

		if (tree == null && m_bucketManager != null) {
			Bucket bucket = m_bucketManager.getBucket(id.getDomain(), localHostAddress, id.getHour(), false);

			if (bucket != null) {
				bucket.flush();

				ByteBuf data = bucket.get(id);

				if (data != null) {
					tree = CodecHandler.decode(changeBuf(data));
				}
			} else {
				LOGGER.warn(
						"Local bucket is not available for logview lookup, messageId={}, messageDomain={}, localHost={}, hour={}, period={}, domain={}.",
						messageId, id.getDomain(), localHostAddress, id.getHour(), period, domain);
			}
		} else if (tree == null) {
			LOGGER.warn("Local bucket manager is not configured for logview lookup, messageId={}, period={}, domain={}.",
					messageId, period, domain);
		}

		if (tree != null) {
			ByteBuf content = ByteBufAllocator.DEFAULT.buffer(8192);

			if (tree.getMessage() instanceof Transaction && waterfall) {
				m_waterfall.encode(tree, content);
			} else {
				m_html.encode(tree, content);
			}

			try {
				content.readInt(); // get rid of length
				return content.toString(Charset.forName("utf-8"));
			} catch (Exception e) {
				LOGGER.error("Unable to render new local logview message, messageId={}, waterfall={}.", messageId,
						waterfall, e);
			}
		}

		LOGGER.warn(
				"New local logview message not found, messageId={}, messageDomain={}, localHost={}, hour={}, period={}, domain={}, finderManagerConfigured={}, bucketManagerConfigured={}.",
				messageId, id.getDomain(), localHostAddress, id.getHour(), period, domain, m_finderManager != null,
				m_bucketManager != null);
		return null;
	}

	private ByteBuf changeBuf(ByteBuf data) {
		data.markReaderIndex();
		int length = data.readInt();
		data.resetReaderIndex();
		ByteBuf readBytes = data.readBytes(length + 4);

		readBytes.markReaderIndex();
		readBytes.readInt();
		return readBytes;
	}

	public String buildOldReport(ModelRequest request, ModelPeriod period, String domain, ApiPayload payload)
	      throws Exception {
		String messageId = payload.getMessageId();
		boolean waterfall = payload.isWaterfall();
		MessageTree tree = null;

		if (m_messageBucketManager != null) {
			tree = m_messageBucketManager.loadMessage(messageId);
		} else {
			LOGGER.warn("Old local message bucket manager is not configured for logview lookup, messageId={}, period={}, domain={}.",
					messageId, period, domain);
		}

		if (tree != null) {
			ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(8192);

			if (tree.getMessage() instanceof Transaction && waterfall) {
				m_waterfall.encode(tree, buf);
			} else {
				m_html.encode(tree, buf);
			}

			try {
				buf.readInt(); // get rid of length
				return buf.toString(Charset.forName("utf-8"));
			} catch (Exception e) {
				LOGGER.error("Unable to render old local logview message, messageId={}, waterfall={}.", messageId,
						waterfall, e);
			}
		}
		LOGGER.warn(
				"Old local logview message not found, messageId={}, period={}, domain={}, waterfall={}, messageBucketManagerConfigured={}.",
				messageId, period, domain, waterfall, m_messageBucketManager != null);
		return null;
	}

	public void setBucketManager(BucketManager bucketManager) {
		m_bucketManager = bucketManager;
	}

	public void setFinderManager(MessageFinderManager finderManager) {
		m_finderManager = finderManager;
	}

	public void setMessageBucketManager(MessageBucketManager messageBucketManager) {
		m_messageBucketManager = messageBucketManager;
	}

	@Override
	public ModelResponse<String> invoke(ModelRequest request) {
		ModelResponse<String> response = new ModelResponse<String>();
		Transaction t = Cat.newTransaction("ModelService", getClass().getSimpleName());

		try {
			ModelPeriod period = request.getPeriod();
			String domain = request.getDomain();
			ApiPayload payload = new ApiPayload();

			payload.setMessageId(request.getProperty("messageId"));
			payload.setWaterfall(Boolean.valueOf(request.getProperty("waterfall", "false")));

			String report = getReport(request, period, domain, payload);

			response.setModel(report);

			t.addData("period", period);
			t.addData("domain", domain);
			t.setStatus(Message.SUCCESS);
		} catch (Exception e) {
			LOGGER.error("Unable to invoke local logview service, request={}, domain={}.", request, request.getDomain(), e);
			Cat.logError(e);
			t.setStatus(e);
			response.setException(e);
		} finally {
			t.complete();
		}
		return response;
	}

	@Override
	public boolean isEligable(ModelRequest request) {
		if (m_configManager.isHdfsOn()) {
			return request.getPeriod().isCurrent();
		} else {
			return true;
		}
	}

}
