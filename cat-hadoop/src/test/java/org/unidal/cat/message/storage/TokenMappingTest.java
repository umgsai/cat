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
package org.unidal.cat.message.storage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.concurrent.CountDownLatch;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.dianping.cat.support.Threads;
import com.dianping.cat.support.Threads.Task;

public class TokenMappingTest {
	private LocalStorageTestSupport m_storage;

	@Before
	public void before() throws IOException {
		File baseDir = new File("target");

		deleteDirectory(new File(baseDir, "dump").toPath());
		m_storage = new LocalStorageTestSupport(baseDir);
	}

	private void deleteDirectory(Path path) throws IOException {
		if (!Files.exists(path)) {
			return;
		}

		try (java.util.stream.Stream<Path> stream = Files.walk(path)) {
			stream.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
		}
	}

	@Test
	public void test() throws IOException {
		int hour = 405845;

		for (int times = 0; times < 3; times++) {
			TokenMapping mapping = m_storage.createTokenMapping(hour, "127.0.0.1");

			for (int i = 0; i < 64 * 1024; i++) {
				String expected = "token-mapping-" + i;
				int index = mapping.map(expected);
				String actual = mapping.find(index);

				Assert.assertEquals(i + 1, index);
				Assert.assertEquals(expected, actual);
			}

			mapping.close();
		}
	}

	@Test
	public void testMany() throws IOException {
		int hour = 405845;

		for (int times = 0; times < 3; times++) {
			TokenMapping mapping = m_storage.createTokenMapping(hour, "127.0.0.1");

			for (int i = 0; i < 64 * 1024 * 10; i++) {
				String expected = "token-mapping-" + i;
				int index = mapping.map(expected);
				String actual = mapping.find(index);

				Assert.assertEquals(i + 1, index);
				Assert.assertEquals(expected, actual);
			}

			mapping.close();
		}
	}

	public void testMuliThreadMap() throws IOException {
		int total = 50;
		CountDownLatch latch = new CountDownLatch(total);

		for (int i = 0; i < total; i++) {
			Threads.forGroup("cat").start(new MapThread(latch, i));
			latch.countDown();
		}

		System.in.read();
	}

	public class MapThread implements Task {

		private CountDownLatch m_latch;

		private int m_index;

		public MapThread(CountDownLatch latch, int index) {
			m_latch = latch;
			m_index = index;
		}

		@Override
		public void run() {
			try {
				m_latch.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			try {
				TokenMappingManager mappingManager = m_storage.getTokenMappingManager();
				int hour = 405845;
				TokenMapping mapping = mappingManager.getTokenMapping(hour, "127.0.0.1");

				for (int times = 0; times < 3; times++) {

					for (int i = 0; i < 64 * 1024; i++) {
						String expected = "token-mapping-" + i;

						mapping.map(expected);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		@Override
		public String getName() {
			return "map-thread " + m_index;
		}

		@Override
		public void shutdown() {

		}

	}

}
