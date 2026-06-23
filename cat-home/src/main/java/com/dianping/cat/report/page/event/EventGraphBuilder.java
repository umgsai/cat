package com.dianping.cat.report.page.event;

import java.util.LinkedHashMap;
import java.util.Map;

import com.dianping.cat.consumer.event.model.entity.EventName;
import com.dianping.cat.report.graph.svg.GraphBuilder;

public class EventGraphBuilder {
	public Map<String, String> build(GraphBuilder builder, EventName eventName) {
		Map<String, String> graphs = new LinkedHashMap<String, String>();

		graphs.put("graph1", builder.build(new HitPayload("Hits Over Time", "Time (min)", "Count", eventName)));
		graphs.put("graph2", builder.build(new FailurePayload("Failures Over Time", "Time (min)", "Count", eventName)));
		return graphs;
	}
}
