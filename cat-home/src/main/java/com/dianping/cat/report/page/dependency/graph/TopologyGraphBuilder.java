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
package com.dianping.cat.report.page.dependency.graph;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import com.dianping.cat.consumer.dependency.model.entity.Dependency;
import com.dianping.cat.consumer.dependency.model.entity.DependencyReport;
import com.dianping.cat.consumer.dependency.model.entity.Index;
import com.dianping.cat.consumer.dependency.model.entity.Segment;
import com.dianping.cat.consumer.dependency.model.transform.BaseVisitor;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.dependency.graph.entity.TopologyEdge;
import com.dianping.cat.home.dependency.graph.entity.TopologyGraph;
import com.dianping.cat.home.dependency.graph.entity.TopologyNode;

import jakarta.annotation.Resource;

@Component
public class TopologyGraphBuilder extends BaseVisitor {

	@Resource
	private DependencyItemBuilder dependencyItemBuilder;

	private String domain;

	private Map<Long, TopologyGraph> graphs = new HashMap<Long, TopologyGraph>();

	private int minute;

	private Date date;

	private Set<String> pigeonServiceTypes = new HashSet<String>(Arrays.asList("Service", "PigeonService", "PigeonServer"));

	public TopologyEdge cloneEdge(TopologyEdge edge) {
		TopologyEdge result = new TopologyEdge();

		result.setDes(edge.getDes());
		result.setKey(edge.getKey());
		result.setLink(edge.getLink());
		result.setOpposite(edge.getOpposite());
		result.setSelf(edge.getSelf());
		result.setStatus(edge.getStatus());
		result.setTarget(edge.getTarget());
		result.setType(edge.getType());
		result.setWeight(edge.getWeight());
		return result;
	}

	public TopologyNode cloneNode(TopologyNode node) {
		TopologyNode result = new TopologyNode();

		result.setDes(node.getDes());
		result.setId(node.getId());
		result.setLink(node.getLink());
		result.setStatus(node.getStatus());
		result.setType(node.getType());
		result.setWeight(node.getWeight());
		return result;
	}

	public TopologyNode createNode(String domain) {
		return dependencyItemBuilder.createNode(domain);
	}

	private TopologyGraph findOrCreateGraph() {
		long time = date.getTime() + minute * TimeHelper.ONE_MINUTE;
		TopologyGraph graph = graphs.get(time);

		if (graph == null) {
			graph = new TopologyGraph();
			graphs.put(time, graph);
		}

		return graph;
	}

	public Map<Long, TopologyGraph> getGraphs() {
		return graphs;
	}

	public String mergeDes(String old, String des) {
		if (StringUtils.isEmpty(old)) {
			return des;
		} else if (StringUtils.isEmpty(des)) {
			return old;
		} else {
			return old + des;
		}
	}

	private TopologyEdge mergeEdge(TopologyEdge old, TopologyEdge edge) {
		if (old == null) {
			return edge;
		} else {
			if (edge.getStatus() > old.getStatus()) {
				old.setStatus(edge.getStatus());
			}
			if (edge.getWeight() > old.getWeight()) {
				old.setWeight(edge.getWeight());
			}
			old.setDes(mergeDes(old.getDes(), edge.getDes()));
			return old;
		}
	}

	private TopologyNode mergeNode(TopologyNode old, TopologyNode node) {
		if (old == null) {
			return node;
		} else {
			if (node.getStatus() > old.getStatus()) {
				old.setStatus(node.getStatus());
			}
			if (node.getWeight() > old.getWeight()) {
				old.setWeight(node.getWeight());
			}
			old.setDes(mergeDes(old.getDes(), node.getDes()));
			return old;
		}
	}

	@Override
	public void visitDependency(Dependency dependency) {
		String type = dependency.getType();

		if (!pigeonServiceTypes.contains(type)) {
			TopologyEdge edge = dependencyItemBuilder.buildEdge(domain, dependency);
			TopologyGraph graph = findOrCreateGraph();
			TopologyEdge old = graph.findTopologyEdge(edge.getKey());

			graph.getEdges().put(edge.getKey(), mergeEdge(old, edge));
			if ("Database".equals(type)) {
				String target = dependency.getTarget();
				TopologyNode nodeOld = graph.findTopologyNode(target);

				graph.getNodes().put(target, mergeNode(nodeOld, dependencyItemBuilder.createDatabaseNode(target)));
			} else if ("Cache".equals(type)) {
				String target = dependency.getTarget();
				TopologyNode nodeOld = graph.findTopologyNode(target);

				graph.getNodes().put(target, mergeNode(nodeOld, dependencyItemBuilder.createCacheNode(target)));
			}
		}
	}

	@Override
	public void visitDependencyReport(DependencyReport dependencyReport) {
		date = dependencyReport.getStartTime();
		domain = dependencyReport.getDomain();
		super.visitDependencyReport(dependencyReport);
	}

	@Override
	public void visitIndex(Index index) {
		TopologyGraph graph = findOrCreateGraph();
		TopologyNode node = dependencyItemBuilder.buildNode(domain, index);
		TopologyNode old = graph.findTopologyNode(node.getId());

		graph.getNodes().put(node.getId(), mergeNode(old, node));
	}

	@Override
	public void visitSegment(Segment segment) {
		minute = segment.getId();
		super.visitSegment(segment);
	}

	public TopologyGraphBuilder setItemBuilder(DependencyItemBuilder itemBuilder) {
		dependencyItemBuilder = itemBuilder;
		return this;
	}

}
