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
package org.unidal.cat.message.storage.clean;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hdfs.protocol.AlreadyBeingCreatedException;
import org.apache.hadoop.ipc.RemoteException;
import org.apache.hadoop.security.AccessControlException;
import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.cat.message.storage.hdfs.HdfsSystemManager;
import com.dianping.cat.support.Threads.Task;

import com.dianping.cat.Cat;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;

public class HdfsUploader implements LogEnabled, Initializable {

	private HdfsSystemManager m_fileSystemManager;

	private ServerConfigManager m_serverConfigManager;

	private ThreadPoolExecutor m_executors;

	private File m_localBaseDir;

	private Logger m_logger;

	private void deleteFile(String path) {
		File file = new File(m_localBaseDir, path);
		File parent = file.getParentFile();

		file.delete();
		parent.delete();
		parent.getParentFile().delete();
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public void initialize() throws InitializationException {
		int thread = m_serverConfigManager.getHdfsUploadThreadsCount();

		m_localBaseDir = new File(m_serverConfigManager.getHdfsLocalBaseDir(HdfsSystemManager.DUMP));
		m_executors = new ThreadPoolExecutor(thread, thread, 10, TimeUnit.SECONDS,	new ArrayBlockingQueue<Runnable>(5000),
								new ThreadPoolExecutor.CallerRunsPolicy());
	}

	private String formatBytes(Number data, String pattern, String suffix) {
		if (pattern == null || pattern.length() == 0 || data == null) {
			return "";
		}

		StringBuilder builder = new StringBuilder(32);
		double value = data.doubleValue();
		long base = 1024;
		long scale = 1;
		boolean positive = value > 0;

		if (!positive) {
			value = -value;
		}

		while (value >= base) {
			scale *= base;
			value /= base;
		}

		builder.append("{0,number,").append(pattern).append('}');

		if (scale == base) {
			builder.append('K');
		} else if (scale == base * base) {
			builder.append('M');
		} else if (scale == base * base * base) {
			builder.append('G');
		} else if (scale == base * base * base * base) {
			builder.append('T');
		} else if (scale == base * base * base * base * base) {
			builder.append('P');
		}

		if (suffix != null) {
			builder.append(suffix);
		}

		return new MessageFormat(builder.toString()).format(new Object[] { positive ? value : -value });
	}

	private FSDataOutputStream makeHdfsOutputStream(String path) throws IOException {
		FileSystem fs = m_fileSystemManager.getFileSystem();
		String baseDir = m_fileSystemManager.getBaseDir();
		Path file = new Path(baseDir, path);
		FSDataOutputStream out;

		try {
			out = fs.create(file, true);
		} catch (RemoteException re) {
			fs.delete(file, false);

			out = fs.create(file);
		} catch (AlreadyBeingCreatedException e) {
			fs.delete(file, false);

			out = fs.create(file);
		}
		return out;
	}

	public boolean upload(String path, File file) {
		if (file.exists()) {
			Transaction t = Cat.newTransaction("System", "UploadDump");
			t.addData("file", path);

			try {
				long start;

				try (FSDataOutputStream fdos = makeHdfsOutputStream(path);
				      FileInputStream fis = new FileInputStream(file)) {
					start = System.currentTimeMillis();
					IOUtils.copy(fis, fdos);
				}

				double sec = (System.currentTimeMillis() - start) / 1000d;
				String size = formatBytes(file.length(), "0.#", "B");
				String speed = sec <= 0 ? "N/A" : formatBytes(file.length() / sec, "0.0", "B/s");

				t.addData("size", size);
				t.addData("speed", speed);
				t.setStatus(Message.SUCCESS);

				deleteFile(path);
				return true;
			} catch (AlreadyBeingCreatedException e) {
				Cat.logError(e);
				t.setStatus(e);

				deleteFile(path);
				m_logger.error(String.format("Already being created (%s)!", path), e);
			} catch (AccessControlException e) {
				Cat.logError(e);
				t.setStatus(e);

				deleteFile(path);
				m_logger.error(String.format("No permission to create HDFS file(%s)!", path), e);
			} catch (Exception e) {
				Cat.logError(e);
				t.setStatus(e);
				m_logger.error(String.format("Uploading file(%s) to HDFS(%s) failed!", file, path), e);
			} finally {
				t.complete();
			}
		}
		return false;
	}

	public void uploadLogviewFile(String path, File file) {
		try {
			m_executors.submit(new Uploader(path, file));
		} catch (Exception e) {
			Cat.logError(e);
		}
	}

	public class Uploader implements Task {

		private String m_path;

		private File m_file;

		public Uploader(String path, File file) {
			m_path = path;
			m_file = file;
		}

		@Override
		public String getName() {
			return "hdfs-uploader";
		}

		@Override
		public void run() {
			upload(m_path, m_file);
		}

		@Override
		public void shutdown() {
		}
	}

}
