package com.dianping.cat.report.page.transaction;

import java.util.LinkedHashMap;
import java.util.Map;

import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.report.graph.svg.GraphBuilder;
import com.dianping.cat.report.page.transaction.GraphPayload.AverageTimePayload;
import com.dianping.cat.report.page.transaction.GraphPayload.DurationPayload;
import com.dianping.cat.report.page.transaction.GraphPayload.FailurePayload;
import com.dianping.cat.report.page.transaction.GraphPayload.HitPayload;

public class TransactionGraphBuilder {
	public Map<String, String> build(GraphBuilder builder, TransactionName transactionName) {
		Map<String, String> graphs = new LinkedHashMap<String, String>();

		graphs.put("graph1", builder.build(new DurationPayload("Duration Distribution", "Duration (ms)", "Count", transactionName)));
		graphs.put("graph2", builder.build(new HitPayload("Hits Over Time", "Time (min)", "Count", transactionName)));
		graphs.put("graph3", builder.build(new AverageTimePayload("Average Duration Over Time", "Time (min)",
				"Average Duration (ms)", transactionName)));
		graphs.put("graph4", builder.build(new FailurePayload("Failures Over Time", "Time (min)", "Count", transactionName)));
		return graphs;
	}
}
