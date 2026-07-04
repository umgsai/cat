package com.dianping.cat.status.http;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class HttpStatsCollectorTest {
	@Test
	public void skipHttpStatusWhenNoRequest() {
		HttpStats.getAndReset();

		Map<String, String> properties = new HttpStatsCollector().getProperties();

		Assert.assertTrue(properties.isEmpty());
	}

	@Test
	public void collectHttpStatusWhenRequestExists() {
		HttpStats.getAndReset();
		HttpStats.currentStatsHolder().doRequestStats(20, 500);

		Map<String, String> properties = new HttpStatsCollector().getProperties();

		Assert.assertEquals("1", properties.get("http.count"));
		Assert.assertEquals("20", properties.get("http.meantime"));
		Assert.assertEquals("0", properties.get("http.status400.count"));
		Assert.assertEquals("1", properties.get("http.status500.count"));
	}
}
