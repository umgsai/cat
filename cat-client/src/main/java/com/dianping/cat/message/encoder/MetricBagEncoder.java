package com.dianping.cat.message.encoder;

import com.dianping.cat.message.MetricBag;

import io.netty.buffer.ByteBuf;

public interface MetricBagEncoder {
	void encode(MetricBag bag, ByteBuf buf);
}
