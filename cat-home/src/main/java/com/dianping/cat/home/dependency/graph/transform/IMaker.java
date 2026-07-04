package com.dianping.cat.home.dependency.graph.transform;

import com.dianping.cat.home.dependency.graph.entity.TopologyEdge;
import com.dianping.cat.home.dependency.graph.entity.TopologyGraph;
import com.dianping.cat.home.dependency.graph.entity.TopologyNode;

public interface IMaker<T> {

   TopologyEdge buildTopologyEdge(T node);

   TopologyGraph buildTopologyGraph(T node);

   TopologyNode buildTopologyNode(T node);
}
