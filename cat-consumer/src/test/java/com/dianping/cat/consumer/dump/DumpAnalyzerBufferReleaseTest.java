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
package com.dianping.cat.consumer.dump;

import org.junit.Assert;
import org.junit.Test;
import org.unidal.cat.message.storage.MessageDumper;
import org.unidal.cat.message.storage.MessageDumperManager;

import com.dianping.cat.message.spi.DefaultMessageTree;
import com.dianping.cat.statistic.ServerStatisticManager;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class DumpAnalyzerBufferReleaseTest {
	@Test
	public void releaseBufferWhenDumperMissing() {
		DumpAnalyzer analyzer = new DumpAnalyzer();
		ByteBuf buffer = Unpooled.buffer(4);
		DefaultMessageTree tree = new DefaultMessageTree();

		analyzer.setDumperManager(new MissingDumperManager());
		analyzer.setServerStatisticManager(new ServerStatisticManager());
		tree.setDomain("cat");
		tree.setMessageId("cat-c0a8016d-0-1");
		tree.setBuffer(buffer);

		analyzer.process(tree);

		Assert.assertEquals(0, buffer.refCnt());
	}

	private static class MissingDumperManager implements MessageDumperManager {
		@Override
		public void close(int hour) {
		}

		@Override
		public MessageDumper find(int hour) {
			return null;
		}

		@Override
		public MessageDumper findOrCreate(int hour) {
			return null;
		}
	}
}
