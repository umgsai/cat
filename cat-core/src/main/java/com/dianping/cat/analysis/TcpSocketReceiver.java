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
package com.dianping.cat.analysis;

import java.util.List;

import jakarta.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.dianping.cat.CatConstants;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.message.CodecHandler;
import com.dianping.cat.message.spi.BufReleaseHelper;
import com.dianping.cat.message.spi.DefaultMessageTree;
import com.dianping.cat.statistic.ServerStatisticManager;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;

@Component("tcpSocketReceiver")
public final class TcpSocketReceiver {
	private static final Logger LOGGER = LoggerFactory.getLogger(TcpSocketReceiver.class);

	@Resource(name = "serverConfigManager")
	protected ServerConfigManager serverConfigManager;

	@Resource(name = "messageHandler")
	private MessageHandler messageHandler;

	@Resource(name = "serverStatisticManager")
	private ServerStatisticManager serverStatisticManager;

	private ChannelFuture future;

	private EventLoopGroup bossGroup;

	private EventLoopGroup workerGroup;

	private final int port = Integer.getInteger("cat.tcp.port", 2280); // default port number from phone, C:2, A:2, T:8

	public synchronized void destroy() {
		try {
			info("start shutdown socket, port " + port);
			if (future != null) {
				future.channel().closeFuture();
			}
			if (bossGroup != null) {
				bossGroup.shutdownGracefully();
			}
			if (workerGroup != null) {
				workerGroup.shutdownGracefully();
			}
			info("shutdown socket success");
		} catch (Exception e) {
			warn(e.getMessage(), e);
		}
	}

	protected boolean getOSMatches(String osNamePrefix) {
		String os = System.getProperty("os.name");

		if (os == null) {
			return false;
		}
		return os.startsWith(osNamePrefix);
	}

	public void init() {
		try {
			if (messageHandler == null) {
				throw new IllegalStateException("MessageHandler is required for TcpSocketReceiver.");
			}
			if (serverStatisticManager == null) {
				throw new IllegalStateException("ServerStatisticManager is required for TcpSocketReceiver.");
			}
			startServer(port);
		} catch (Exception e) {
			error(e.getMessage(), e);
		}
	}

	public synchronized void startServer(int port) throws InterruptedException {
		boolean linux = getOSMatches("Linux") || getOSMatches("LINUX");
		int threads = 24;
		ServerBootstrap bootstrap = new ServerBootstrap();

		bossGroup = linux ? new EpollEventLoopGroup(threads) : new NioEventLoopGroup(threads);
		workerGroup = linux ? new EpollEventLoopGroup(threads) : new NioEventLoopGroup(threads);
		bootstrap.group(bossGroup, workerGroup);
		bootstrap.channel(linux ? EpollServerSocketChannel.class : NioServerSocketChannel.class);

		bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				ChannelPipeline pipeline = ch.pipeline();

				pipeline.addLast("decode", new MessageDecoder());
			}
		});

		bootstrap.childOption(ChannelOption.SO_REUSEADDR, true);
		bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
		bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
		bootstrap.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);

		try {
			future = bootstrap.bind(port).sync();
			info("start netty server!");
		} catch (Exception e) {
			error("Started Netty Server Failed:" + port, e);
		}
	}

	public void setHandler(MessageHandler handler) {
		messageHandler = handler;
	}

	public void setServerConfigManager(ServerConfigManager serverConfigManager) {
		this.serverConfigManager = serverConfigManager;
	}

	public void setServerStateManager(ServerStatisticManager serverStateManager) {
		serverStatisticManager = serverStateManager;
	}

	private void error(String message, Throwable cause) {
		LOGGER.error(message, cause);
	}

	private void info(String message) {
		LOGGER.info(message);
	}

	private void warn(String message, Throwable cause) {
		LOGGER.warn(message, cause);
	}

	private class MessageDecoder extends ByteToMessageDecoder {
		private long processCount;

		@Override
		protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out) throws Exception {
			if (buffer.readableBytes() < 4) {
				return;
			}
			
			buffer.markReaderIndex();
			int length = buffer.readInt();
			buffer.resetReaderIndex();
			
			if (buffer.readableBytes() < length + 4) {
				return;
			}
			
			try {
				if (length > 0) {
					ByteBuf readBytes = buffer.readBytes(length + 4);

					readBytes.markReaderIndex();
					readBytes.readInt();

					DefaultMessageTree tree = (DefaultMessageTree) CodecHandler.decode(readBytes);

					// readBytes.retain();
					readBytes.resetReaderIndex();
					tree.setBuffer(readBytes);
					messageHandler.handle(tree);
					processCount++;

					long flag = processCount % CatConstants.SUCCESS_COUNT;

					if (flag == 0) {
						serverStatisticManager.addMessageTotal(CatConstants.SUCCESS_COUNT);
					}
				} else {
					// client message is error
					buffer.readBytes(length);
					BufReleaseHelper.release(buffer);
				}
			} catch (Exception e) {
				serverStatisticManager.addMessageTotalLoss(1);
				error(e.getMessage(), e);
			}
		}
	}

}
