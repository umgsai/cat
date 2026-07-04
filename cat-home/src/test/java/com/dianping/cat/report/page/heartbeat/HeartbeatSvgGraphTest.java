package com.dianping.cat.report.page.heartbeat;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.dianping.cat.consumer.heartbeat.model.entity.Detail;
import com.dianping.cat.consumer.heartbeat.model.entity.Extension;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.heartbeat.model.entity.Machine;
import com.dianping.cat.consumer.heartbeat.model.entity.Period;
import com.dianping.cat.home.heartbeat.entity.Metric;
import com.dianping.cat.report.page.heartbeat.config.HeartbeatDisplayPolicyManager;

public class HeartbeatSvgGraphTest {
	@Test
	public void skipEmptyExtensionGroup() {
		HeartbeatReport report = new HeartbeatReport("cat");
		Machine machine = report.findOrCreateMachine("127.0.0.1");
		Period period = new Period(1);

		period.findOrCreateExtension("system.static");
		machine.addPeriod(period);

		HeartbeatSvgGraph graph = new HeartbeatSvgGraph(null, new MockHeartbeatDisplayPolicyManager()).display(report,
				"127.0.0.1");

		Assert.assertTrue(graph.getExtensionGraph().isEmpty());
		Assert.assertTrue(graph.getExtensionChartGraph().isEmpty());
	}

	@Test
	public void skipAllZeroHttpStatusGroup() {
		HeartbeatReport report = new HeartbeatReport("cat");
		Machine machine = report.findOrCreateMachine("127.0.0.1");
		Period period = new Period(1);
		Extension extension = period.findOrCreateExtension("http.status");

		extension.addDetail(new Detail("http.count").setValue(0));
		extension.addDetail(new Detail("http.meantime").setValue(0));
		extension.addDetail(new Detail("http.status400.count").setValue(0));
		extension.addDetail(new Detail("http.status500.count").setValue(0));
		machine.addPeriod(period);

		HeartbeatSvgGraph graph = new HeartbeatSvgGraph(null, new MockHeartbeatDisplayPolicyManager()).display(report,
				"127.0.0.1");

		Assert.assertTrue(graph.getExtensionGraph().isEmpty());
		Assert.assertTrue(graph.getExtensionChartGraph().isEmpty());
	}

	@Test
	public void keepHttpStatusGroupWhenItHasData() {
		HeartbeatReport report = new HeartbeatReport("cat");
		Machine machine = report.findOrCreateMachine("127.0.0.1");
		Period period = new Period(1);
		Extension extension = period.findOrCreateExtension("http.status");

		extension.addDetail(new Detail("http.count").setValue(3));
		extension.addDetail(new Detail("http.meantime").setValue(10));
		extension.addDetail(new Detail("http.status400.count").setValue(0));
		extension.addDetail(new Detail("http.status500.count").setValue(0));
		machine.addPeriod(period);

		HeartbeatSvgGraph graph = new HeartbeatSvgGraph(null, new MockHeartbeatDisplayPolicyManager()).display(report,
				"127.0.0.1");

		Assert.assertTrue(graph.getExtensionChartGraph().containsKey("http.status"));
	}

	private static class MockHeartbeatDisplayPolicyManager extends HeartbeatDisplayPolicyManager {
		@Override
		public List<String> sortGroupNames(Set<String> originGroupNameSet) {
			return new ArrayList<String>(originGroupNameSet);
		}

		@Override
		public List<String> sortMetricNames(String groupName, Set<String> originMetricNames) {
			return new ArrayList<String>(originMetricNames);
		}

		@Override
		public boolean isDelta(String groupName, String metricName) {
			return false;
		}

		@Override
		public Metric queryMetric(String groupName, String metricName) {
			return null;
		}

		@Override
		public int queryUnit(String groupName, String metricName) {
			return 1;
		}
	}
}
