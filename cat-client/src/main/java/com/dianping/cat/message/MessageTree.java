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
package com.dianping.cat.message;

public interface MessageTree extends Cloneable {
	String getDomain();

	String getHostName();

	String getIpAddress();

	Message getMessage();

	String getMessageId();

	String getParentMessageId();

	String getRootMessageId();

	String getSessionToken();

	String getThreadGroupName();

	String getThreadId();

	String getThreadName();

	void setDomain(String domain);

	void setHostName(String hostName);

	void setIpAddress(String ipAddress);

	void setMessage(Message message);

	void setMessageId(String messageId);

	void setParentMessageId(String parentMessageId);

	void setRootMessageId(String rootMessageId);

	void setSessionToken(String session);

	void setThreadGroupName(String name);

	void setThreadId(String threadId);

	void setThreadName(String id);

}
